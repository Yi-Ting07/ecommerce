package com.ecommerce.backend.dto;

import java.time.LocalDateTime;

/**
 * 會員個人資料回應
 */
public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role,
        LocalDateTime createdAt
) {
}
