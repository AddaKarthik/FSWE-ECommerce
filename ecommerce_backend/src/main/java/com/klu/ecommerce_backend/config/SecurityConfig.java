package com.klu.ecommerce_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .cors(Customizer.withDefaults())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Authentication
                .requestMatchers("/api/auth/**").permitAll()

                // Users
                .requestMatchers("/api/users/**").permitAll()

                // Products
                .requestMatchers("/api/products/**").permitAll()

                // Razorpay webhook
                .requestMatchers("/api/payment/webhook").permitAll()

                // Background Job Monitor
                .requestMatchers("/api/jobs/**").permitAll()

                // Swagger
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // Other APIs
                .anyRequest().permitAll()
            );

        return http.build();
    }
}