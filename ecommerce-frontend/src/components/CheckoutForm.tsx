import {
    useForm
} from "react-hook-form";

import {
    zodResolver
} from "@hookform/resolvers/zod";

import {
    checkoutSchema,
    type CheckoutFormData
} from "../schemas/checkoutSchema";

import {
    useCartStore
} from "../store/cartStore";

import {
    createOrder,
    createPayment,
    verifyPayment
} from "../api/paymentApi";

declare global {
    interface Window {
        Razorpay: any;
    }
}

function CheckoutForm() {

    // Get cart items
    const cartItems = useCartStore(
        (state) => state.cartItems
    );

    // Get total from Zustand store
    const getTotal = useCartStore(
        (state) => state.getTotal
    );

    // Clear cart after successful payment
    const clearCart = useCartStore(
        (state) => state.clearCart
    );

    const total = getTotal();

    // React Hook Form
    const {
        register,
        handleSubmit,
        formState: {
            errors
        }
    } = useForm<CheckoutFormData>({
        resolver: zodResolver(
            checkoutSchema
        )
    });

    // Form submit
    const onSubmit = async (
        data: CheckoutFormData
    ) => {

        try {

            // Check cart
            if (cartItems.length === 0) {
                alert("Your cart is empty");
                return;
            }

            // ==========================================
            // STEP 1: Create Order in our Backend
            // ==========================================

            const order = await createOrder(
                cartItems.map((item) => ({
                    productId: item.product.productId,
                    quantity: item.quantity
                }))
            );

            // ==========================================
            // STEP 2: Create Razorpay Order
            // ==========================================

            const payment = await createPayment(
                order.orderId
            );

            // ==========================================
            // STEP 3: Open Razorpay Checkout
            // ==========================================

            const options = {

                key: payment.keyId,

                amount: payment.amount,

                currency: payment.currency,

                name: "E-Commerce",

                description: "E-Commerce Order",

                order_id: payment.razorpayOrderId,

                prefill: {
                    name: data.name,
                    email: data.email,
                    contact: data.phone
                },

                notes: {
                    address: data.address
                },

                // ==========================================
                // Payment Successful
                // ==========================================

                handler: async function (
                    response: any
                ) {

                    try {

                        // ==========================================
                        // STEP 4: Verify Payment
                        // ==========================================

                        await verifyPayment({

                            orderId:
                                order.orderId,

                            razorpayOrderId:
                                response.razorpay_order_id,

                            razorpayPaymentId:
                                response.razorpay_payment_id,

                            razorpaySignature:
                                response.razorpay_signature
                        });

                        // ==========================================
                        // STEP 5: Clear Cart
                        // ==========================================

                        clearCart();

                        alert(
                            "Payment successful!\n\n" +
                            "Order ID: " +
                            order.orderId +
                            "\n" +
                            "Amount: ₹" +
                            total
                        );

                    } catch (error) {

                        console.error(
                            "Payment verification error:",
                            error
                        );

                        alert(
                            "Payment verification failed"
                        );
                    }
                },

                // ==========================================
                // Payment Window Closed
                // ==========================================

                modal: {

                    ondismiss: function () {

                        console.log(
                            "Payment window closed"
                        );
                    }
                }
            };

            // Create Razorpay instance
            const razorpay =
                new window.Razorpay(
                    options
                );

            // ==========================================
            // Payment Failed
            // ==========================================

            razorpay.on(
                "payment.failed",
                function (
                    response: any
                ) {

                    console.error(
                        "Payment failed:",
                        response.error
                    );

                    alert(
                        "Payment failed"
                    );
                }
            );

            // Open Razorpay Checkout
            razorpay.open();

        } catch (error) {

            console.error(
                "Order/payment error:",
                error
            );

            alert(
                "Unable to start payment"
            );
        }
    };

    return (
        <div className="checkout">

            <h2>
                Checkout
            </h2>

            <form
                onSubmit={
                    handleSubmit(onSubmit)
                }
            >

                {/* Name */}
                <div>

                    <label>
                        Name
                    </label>

                    <input
                        type="text"
                        {...register("name")}
                    />

                    {errors.name && (
                        <p className="error-message">
                            {errors.name.message}
                        </p>
                    )}

                </div>

                {/* Email */}
                <div>

                    <label>
                        Email
                    </label>

                    <input
                        type="email"
                        {...register("email")}
                    />

                    {errors.email && (
                        <p className="error-message">
                            {errors.email.message}
                        </p>
                    )}

                </div>

                {/* Phone */}
                <div>

                    <label>
                        Phone
                    </label>

                    <input
                        type="tel"
                        {...register("phone")}
                    />

                    {errors.phone && (
                        <p className="error-message">
                            {errors.phone.message}
                        </p>
                    )}

                </div>

                {/* Address */}
                <div>

                    <label>
                        Address
                    </label>

                    <textarea
                        {...register("address")}
                    />

                    {errors.address && (
                        <p className="error-message">
                            {errors.address.message}
                        </p>
                    )}

                </div>

                {/* Total */}
                <h3>
                    Order Total: ₹{total}
                </h3>

                {/* Pay Button */}
                <button type="submit">
                    Pay Order
                </button>

            </form>

        </div>
    );
}

export default CheckoutForm;