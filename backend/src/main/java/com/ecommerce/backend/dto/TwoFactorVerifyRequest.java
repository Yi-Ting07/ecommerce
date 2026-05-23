package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TwoFactorVerifyRequest(
        @NotBlank String tempToken,
        @NotBlank @Size(min = 6, max = 6, message = "驗證碼必須為 6 位數字") String code
) {}
