package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank(message = "使用者名稱不能為空")
        @Size(min = 2, max = 50, message = "使用者名稱長度需介於 2 到 50 字元")
        String username,

        @NotBlank(message = "Email 不能為空")
        @Email(message = "Email 格式不正確")
        String email,

        @NotBlank(message = "密碼不能為空")
        @Size(min = 6, max = 100, message = "密碼長度需至少 6 字元")
        String password
) {
}
