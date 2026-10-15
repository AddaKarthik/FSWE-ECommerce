package com.klu.ecommerce_backend.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.klu.ecommerce_backend.dto.PaymentVerifyRequest;
import com.klu.ecommerce_backend.model.Order;
import com.klu.ecommerce_backend.model.Payment;
import com.klu.ecommerce_backend.repository.OrderRepository;
import com.klu.ecommerce_backend.repository.PaymentRepository;
import com.razorpay.RazorpayClient;

@Service
public class PaymentService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    // CO5: Background job service
    private final BackgroundJobService backgroundJobService;

    public PaymentService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            BackgroundJobService backgroundJobService) {

        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;

        // CO5: Initialize background job service
        this.backgroundJobService = backgroundJobService;
    }

    // ==========================================
    // Create Razorpay Order
    // ==========================================

    public Map<String, Object> createRazorpayOrder(
            Long orderId) throws Exception {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        RazorpayClient razorpayClient =
                new RazorpayClient(keyId, keySecret);

        long amountInPaise =
                order.getTotalAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

        JSONObject options = new JSONObject();

        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "order_" + orderId);

        com.razorpay.Order razorpayOrder =
                razorpayClient.orders.create(options);

        // Save payment information
        Payment payment = new Payment();

        payment.setOrder(order);

        payment.setRazorpayOrderId(
                razorpayOrder.get("id"));

        payment.setAmount(
                order.getTotalAmount());

        payment.setStatus("CREATED");

        paymentRepository.save(payment);

        // Response to React
        Map<String, Object> response =
                new HashMap<>();

        response.put("keyId", keyId);
        response.put("orderId", orderId);
        response.put(
                "razorpayOrderId",
                razorpayOrder.get("id"));
        response.put("amount", amountInPaise);
        response.put("currency", "INR");

        return response;
    }

    // ==========================================
    // Verify Razorpay Payment
    // ==========================================

    public boolean verifyPayment(
            PaymentVerifyRequest request)
            throws Exception {

        Payment payment =
                paymentRepository
                        .findByOrderOrderId(
                                request.getOrderId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment not found"));

        String generatedSignature =
                hmacSha256(
                        request.getRazorpayOrderId()
                                + "|"
                                + request.getRazorpayPaymentId(),
                        keySecret);

        boolean signatureValid =
                MessageDigest.isEqual(
                        generatedSignature.getBytes(
                                StandardCharsets.UTF_8),
                        request.getRazorpaySignature()
                                .getBytes(
                                        StandardCharsets.UTF_8));

        // Payment verification failed
        if (!signatureValid) {
            return false;
        }

        // ==========================================
        // Payment successfully verified
        // ==========================================

        payment.setRazorpayOrderId(
                request.getRazorpayOrderId());

        payment.setRazorpayPaymentId(
                request.getRazorpayPaymentId());

        payment.setRazorpaySignature(
                request.getRazorpaySignature());

        payment.setStatus("PAID");

        paymentRepository.save(payment);

        // Mark our order as PAID
        Order order = payment.getOrder();

        order.setStatus("PAID");

        orderRepository.save(order);

        // ==========================================
        // CO5: Start Background Jobs
        // ==========================================
        // These jobs run asynchronously:
        // 1. EMAIL
        // 2. INVOICE
        // 3. NOTIFICATION

        backgroundJobService.startOrderJobs(
                order.getOrderId());

        return true;
    }

    // ==========================================
    // Razorpay Webhook
    // ==========================================

    public void processWebhook(
            String payload,
            String signature)
            throws Exception {

        String generatedSignature =
                hmacSha256(
                        payload,
                        webhookSecret);

        boolean signatureValid =
                MessageDigest.isEqual(
                        generatedSignature.getBytes(
                                StandardCharsets.UTF_8),
                        signature.getBytes(
                                StandardCharsets.UTF_8));

        if (!signatureValid) {
            throw new RuntimeException(
                    "Invalid webhook signature");
        }

        JSONObject json =
                new JSONObject(payload);

        String event =
                json.getString("event");

        if ("payment.captured".equals(event)) {

            JSONObject paymentEntity =
                    json.getJSONObject("payload")
                            .getJSONObject("payment")
                            .getJSONObject("entity");

            String razorpayPaymentId =
                    paymentEntity.getString("id");

            String razorpayOrderId =
                    paymentEntity.getString("order_id");

            Payment payment =
                    paymentRepository
                            .findByRazorpayOrderId(
                                    razorpayOrderId)
                            .orElse(null);

            if (payment == null) {
                return;
            }

            payment.setRazorpayPaymentId(
                    razorpayPaymentId);

            payment.setStatus("PAID");

            paymentRepository.save(payment);

            Order order =
                    payment.getOrder();

            if (!"PAID".equals(order.getStatus())) {

                order.setStatus("PAID");

                orderRepository.save(order);
            }
        }
    }

    // ==========================================
    // HMAC SHA-256
    // ==========================================

    private String hmacSha256(
            String data,
            String secret)
            throws Exception {

        Mac mac =
                Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey =
                new SecretKeySpec(
                        secret.getBytes(
                                StandardCharsets.UTF_8),
                        "HmacSHA256");

        mac.init(secretKey);

        byte[] hash =
                mac.doFinal(
                        data.getBytes(
                                StandardCharsets.UTF_8));

        StringBuilder result =
                new StringBuilder();

        for (byte b : hash) {

            result.append(
                    String.format("%02x", b));
        }

        return result.toString();
    }
}