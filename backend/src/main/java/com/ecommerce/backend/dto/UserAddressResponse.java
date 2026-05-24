package com.ecommerce.backend.dto;

import java.time.LocalDateTime;

public record UserAddressResponse(
        Long id,
        String label,
        String recipientName,
        String recipientPhone,
        String address,
        LocalDateTime createdAt
) {}
