package com.klu.ecommerce_backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.klu.ecommerce_backend.dto.AuthResponse;
import com.klu.ecommerce_backend.dto.SigninRequest;
import com.klu.ecommerce_backend.dto.SignupRequest;
import com.klu.ecommerce_backend.service.UserService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/signin")
    public AuthResponse signin(@RequestBody SigninRequest request) {
        return userService.signin(request);
    }
}