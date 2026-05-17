package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 結帳請求 DTO
 *
 * 前端 POST /api/orders/checkout 時傳入：
 * { "shippingAddress": "台北市信義區..." }
 */
public record CheckoutRequest(
        @NotBlank(message = "收貨地址不能為空")
        @Size(max = 300, message = "收貨地址不超過 300 字")
        String shippingAddress
) {}
