package com.ecommerce.backend.dto;

import java.math.BigDecimal;

/**
 * 訂單明細回應 DTO
 */
public record OrderItemResponse(
        Long orderItemId,
        Long productId,       // 可能為 null（若商品已被刪除）
        String productName,   // 快照名稱，永遠有值
        BigDecimal price,     // 快照單價
        Integer quantity,
        BigDecimal subtotal
) {}
