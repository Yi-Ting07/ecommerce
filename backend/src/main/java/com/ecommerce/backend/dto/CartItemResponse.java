package com.ecommerce.backend.dto;

import java.math.BigDecimal;

/**
 * 購物車明細回應 DTO
 *
 * 代表購物車中的一個商品項目，回傳給前端的格式。
 */
public record CartItemResponse(
        Long cartItemId,        // cart_items 的 PK（更新/刪除時需要）
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,   // 商品目前單價
        Integer quantity,
        BigDecimal subtotal     // 小計 = unitPrice × quantity
) {}
