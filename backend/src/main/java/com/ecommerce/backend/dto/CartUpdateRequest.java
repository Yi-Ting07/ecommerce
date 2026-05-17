package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 更新購物車數量請求 DTO
 *
 * 前端 PUT /api/cart/items/{itemId} 時傳入：
 * { "quantity": 5 }
 */
public record CartUpdateRequest(
        @NotNull(message = "數量不能為空")
        @Min(value = 1, message = "數量至少為 1")
        Integer quantity
) {}
