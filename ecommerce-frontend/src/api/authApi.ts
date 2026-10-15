import axios from "axios";
import type { SignupRequest, SigninRequest, AuthResponse } from "../types/auth";

const API_URL = "http://localhost:8081/api/auth";

export const signup = async (data: SignupRequest): Promise<string> => {
    const response = await axios.post(`${API_URL}/signup`, data);
    return response.data;
};

export const signin = async (data: SigninRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(`${API_URL}/signin`, data);
    return response.data;
};
