package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 更改密碼請求
 *
 * @param currentPassword 目前的密碼
 * @param newPassword     新密碼（至少 8 字元，含大小寫與數字）
 */
public record PasswordChangeRequest(

        @NotBlank(message = "請輸入目前密碼")
        String currentPassword,

        @NotBlank(message = "請輸入新密碼")
        @Size(min = 8, message = "新密碼至少需要 8 個字元")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "新密碼必須包含大寫字母、小寫字母與數字"
        )
        String newPassword
) {
}
