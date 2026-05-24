package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單回應 DTO
 */
public record OrderResponse(
        Long id,
        String username,
        BigDecimal totalAmount,
        String status,
        String statusLabel,

        // 訂購人資訊
        String ordererName,
        String ordererPhone,
        String ordererAddress,

        // 收貨人資訊
        String recipientName,
        String recipientPhone,

        // 送貨方式
        String deliveryMethod,
        String deliveryMethodLabel,
        String recipientAddress,   // 宅配用
        String storeName,          // 超商用
        String storeCode,          // 超商用

        // 付款方式
        String paymentMethod,

        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

