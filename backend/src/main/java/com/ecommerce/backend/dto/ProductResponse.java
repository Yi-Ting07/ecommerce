package com.ecommerce.backend.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String imageUrl,
        boolean active,
        Long categoryId,
        String categoryName
) {
}
