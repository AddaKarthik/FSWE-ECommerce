export interface SignupRequest {
    name: string;
    email: string;
    password: string;
    role: "ADMIN" | "USER";
}

export interface SigninRequest {
    email: string;
    password: string;
}

export interface AuthResponse {
    userId: number;
    name: string;
    email: string;
    role: "ADMIN" | "USER";
    message: string;
    token: string;
}
