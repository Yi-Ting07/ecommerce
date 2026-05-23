package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @NotBlank(message = "Email 不能為空")
        @Email(message = "Email 格式不正確")
        String email,

        @NotBlank(message = "密碼不能為空")
        String password,

        String captchaId,

        String captchaCode
) {
}
