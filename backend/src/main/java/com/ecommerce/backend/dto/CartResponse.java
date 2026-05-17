package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 購物車回應 DTO
 *
 * 代表整個購物車，包含所有項目和總計。
 */
public record CartResponse(
        List<CartItemResponse> items,
        int totalItems,         // 商品種類數
        BigDecimal totalAmount  // 全部小計加總
) {}
