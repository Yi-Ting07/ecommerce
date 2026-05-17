package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 加入購物車請求 DTO
 *
 * 前端 POST /api/cart/items 時傳入的 JSON 格式：
 * { "productId": 3, "quantity": 2 }
 */
public record CartAddRequest(
        @NotNull(message = "商品 ID 不能為空")
        Long productId,

        @NotNull(message = "數量不能為空")
        @Min(value = 1, message = "數量至少為 1")
        Integer quantity
) {}
