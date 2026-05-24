package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAddressRequest(
        @NotBlank(message = "標籤不能為空")
        @Size(max = 30, message = "標籤不超過 30 字")
        String label,

        @NotBlank(message = "收貨人姓名不能為空")
        @Size(max = 50, message = "姓名不超過 50 字")
        String recipientName,

        @NotBlank(message = "收貨人電話不能為空")
        @Size(max = 30, message = "電話不超過 30 字")
        String recipientPhone,

        @NotBlank(message = "地址不能為空")
        @Size(max = 300, message = "地址不超過 300 字")
        String address
) {}
