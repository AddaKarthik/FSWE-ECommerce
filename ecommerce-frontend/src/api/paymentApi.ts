import axios from "axios";

const API_URL = "http://localhost:8081/api";

export interface PaymentResponse {
    keyId: string;
    orderId: number;
    razorpayOrderId: string;
    amount: number;
    currency: string;
}

// Create Order in our backend
export const createOrder = async (
    items: {
        productId: number;
        quantity: number;
    }[]
) => {

    const token = localStorage.getItem("token");
    const user = JSON.parse(localStorage.getItem("user") || "null");

    if (!user) {
        throw new Error("User not logged in");
    }

    const response = await axios.post(
        `${API_URL}/orders?userId=${user.userId}`,
        { items },
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};

// Create Razorpay Order
export const createPayment = async (
    orderId: number
): Promise<PaymentResponse> => {

    const token = localStorage.getItem("token");

    const response = await axios.post(
        `${API_URL}/payment/create/${orderId}`,
        {},
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};

// Verify Razorpay Payment
export const verifyPayment = async (
    data: {
        orderId: number;
        razorpayOrderId: string;
        razorpayPaymentId: string;
        razorpaySignature: string;
    }
) => {

    const token = localStorage.getItem("token");

    const response = await axios.post(
        `${API_URL}/payment/verify`,
        data,
        {
            headers: {
                Authorization: `Bearer ${token}`
            }
        }
    );

    return response.data;
};