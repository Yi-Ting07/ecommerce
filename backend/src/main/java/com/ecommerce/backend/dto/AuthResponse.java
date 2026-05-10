package com.ecommerce.backend.dto;

public record AuthResponse(
        String token,
        String username,
        String email,
        String role,
        String message
) {
}
