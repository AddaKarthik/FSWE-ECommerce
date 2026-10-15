package com.klu.ecommerce_backend.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.klu.ecommerce_backend.model.BackgroundJob;
import com.klu.ecommerce_backend.model.Order;
import com.klu.ecommerce_backend.repository.BackgroundJobRepository;
import com.klu.ecommerce_backend.repository.OrderRepository;

@Service
public class BackgroundJobProcessor {

    private final BackgroundJobRepository jobRepository;
    private final JobNotificationService notifier;
    private final JavaMailSender mailSender;
    private final OrderRepository orderRepository;

    @Value("${smartcart.mail.from}")
    private String mailFrom;

    public BackgroundJobProcessor(
            BackgroundJobRepository jobRepository,
            JobNotificationService notifier,
            JavaMailSender mailSender,
            OrderRepository orderRepository) {

        this.jobRepository = jobRepository;
        this.notifier = notifier;
        this.mailSender = mailSender;
        this.orderRepository = orderRepository;
    }

    // ==========================================
    // ASYNCHRONOUS BACKGROUND PROCESSING
    // ==========================================

    @Async("taskExecutor")
    public void process(Long jobId) {

        BackgroundJob job =
                jobRepository.findById(jobId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Background job not found"));

        try {

            // Job started
            update(
                    job,
                    "PROCESSING",
                    "Task started"
            );

            // Process job according to job type
            switch (job.getJobType()) {

                case "EMAIL":
                    sendOrderEmail(job);
                    break;

                case "INVOICE":
                    generateInvoice(job);
                    break;

                case "NOTIFICATION":
                    sendNotification(job);
                    break;

                default:
                    throw new RuntimeException(
                            "Unknown job type: "
                            + job.getJobType());
            }

            // Job completed successfully
            update(
                    job,
                    "COMPLETED",
                    "Task completed successfully"
            );

        } catch (Exception e) {

            // Job failed
            update(
                    job,
                    "FAILED",
                    e.getMessage()
            );
        }
    }

    // ==========================================
    // UPDATE JOB STATUS
    // ==========================================

    @Transactional
    public void update(
            BackgroundJob job,
            String status,
            String message) {

        job.setStatus(status);
        job.setMessage(message);

        BackgroundJob saved =
                jobRepository.save(job);

        // Send updated status through WebSocket
        notifier.publish(saved);
    }

    // ==========================================
    // EMAIL TASK
    // ==========================================

    private void sendOrderEmail(
            BackgroundJob job) {

        Order order =
                orderRepository
                        .findById(job.getOrderId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"));

        String customerEmail =
                order.getUser().getEmail();

        SimpleMailMessage mail =
                new SimpleMailMessage();

        mail.setFrom(mailFrom);

        mail.setTo(customerEmail);

        mail.setSubject(
                "SmartCart Order Confirmation"
        );

        mail.setText(
                "Dear "
                + order.getUser().getName()
                + ",\n\n"
                + "Your order "
                + order.getOrderId()
                + " has been confirmed successfully.\n"
                + "Total Amount: ₹"
                + order.getTotalAmount()
                + "\n\n"
                + "Thank you for shopping with SmartCart!"
        );

        mailSender.send(mail);
    }

    // ==========================================
    // INVOICE TASK
    // ==========================================

    private void generateInvoice(
            BackgroundJob job) {

        try {

            Path folder =
                    Paths.get("invoices");

            // Create invoices folder if it does not exist
            Files.createDirectories(folder);

            String html = """
                    <html>
                    <head>
                        <title>SmartCart Invoice</title>
                    </head>

                    <body>

                        <h1>SmartCart Invoice</h1>

                        <p>
                            Order ID: %d
                        </p>

                        <p>
                            Payment: Successful
                        </p>

                        <p>
                            Thank you for shopping with SmartCart!
                        </p>

                    </body>
                    </html>
                    """.formatted(
                            job.getOrderId()
                    );

            Path invoiceFile =
                    folder.resolve(
                            "invoice-"
                            + job.getOrderId()
                            + ".html"
                    );

            Files.writeString(
                    invoiceFile,
                    html,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invoice generation failed",
                    e
            );
        }
    }

    // ==========================================
    // NOTIFICATION TASK
    // ==========================================

    private void sendNotification(
            BackgroundJob job) {

        System.out.println(
                "===================================="
        );

        System.out.println(
                "SmartCart Notification"
        );

        System.out.println(
                "Order "
                + job.getOrderId()
                + " payment confirmed"
        );

        System.out.println(
                "===================================="
        );
    }
}