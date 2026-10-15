package com.klu.ecommerce_backend.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.klu.ecommerce_backend.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // Secret key used to sign JWT
    private final String SECRET_KEY =
            "ThisIsMyVerySecretKeyForSmartCartJwtAuthentication2026";

    // Token expires after 24 hours
    private final long EXPIRATION_TIME =
            1000 * 60 * 60 * 24;

    // Create signing key
    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes()
        );
    }

    // Generate JWT Token
    public String generateToken(User user) {

        return Jwts.builder()

                // User email
                .subject(user.getEmail())

                // User role
                .claim(
                        "role",
                        user.getRole().name()
                )

                // User name
                .claim(
                        "name",
                        user.getName()
                )

                // Token creation time
                .issuedAt(new Date())

                // Token expiration
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                // Sign JWT
                .signWith(getSigningKey())

                // Create final token
                .compact();
    }

    // Extract email from JWT
    public String extractEmail(String token) {

        return extractAllClaims(token)
                .getSubject();
    }

    // Extract role from JWT
    public String extractRole(String token) {

        return extractAllClaims(token)
                .get(
                        "role",
                        String.class
                );
    }

    // Validate JWT
    public boolean isTokenValid(
            String token,
            UserDetails userDetails) {

        String email =
                extractEmail(token);

        return email.equals(
                userDetails.getUsername()
        )
        &&
        !isTokenExpired(token);
    }

    // Check token expiration
    private boolean isTokenExpired(
            String token) {

        return extractAllClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // Extract all JWT claims
    private Claims extractAllClaims(
            String token) {

        return Jwts.parser()

                .verifyWith(
                        getSigningKey()
                )

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }
}