package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "分類名稱不能為空")
        @Size(max = 100, message = "分類名稱不可超過 100 字元")
        String name,

        @Size(max = 500, message = "分類描述不可超過 500 字元")
        String description
) {
}
