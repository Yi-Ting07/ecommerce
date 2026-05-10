package com.ecommerce.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "商品名稱不能為空")
        @Size(max = 150, message = "商品名稱不可超過 150 字元")
        String name,

        @Size(max = 2000, message = "商品描述不可超過 2000 字元")
        String description,

        @NotNull(message = "商品價格不能為空")
        @DecimalMin(value = "0.0", inclusive = true, message = "商品價格不能小於 0")
        BigDecimal price,

        @NotNull(message = "庫存不能為空")
        @Min(value = 0, message = "庫存不能小於 0")
        Integer stock,

        @Size(max = 1000, message = "圖片網址不可超過 1000 字元")
        String imageUrl,

        Boolean active,

        @NotNull(message = "商品分類不能為空")
        Long categoryId
) {
}
