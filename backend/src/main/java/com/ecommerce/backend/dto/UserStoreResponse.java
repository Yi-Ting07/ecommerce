package com.ecommerce.backend.dto;

import java.time.LocalDateTime;

public record UserStoreResponse(
        Long id,
        String storeType,       // "FAMILY_MART" / "SEVEN_ELEVEN"
        String storeTypeLabel,  // "全家" / "7-11"
        String storeName,
        String storeCode,
        LocalDateTime createdAt
) {}
