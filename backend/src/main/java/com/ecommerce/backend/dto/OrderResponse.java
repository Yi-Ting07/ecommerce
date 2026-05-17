package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單回應 DTO
 *
 * 包含訂單基本資訊和所有明細項目。
 */
public record OrderResponse(
        Long id,
        String username,          // 下單的使用者名稱（管理員用）
        BigDecimal totalAmount,
        String status,            // "PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"
        String statusLabel,       // 中文標籤，例如 "待確認"
        String shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
