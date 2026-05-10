package com.example.auth_backend.auth.payload;

public record LoginRequest(
        String email,
        String password
) {
}
