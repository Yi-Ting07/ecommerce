package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 會員更新個人資料請求
 *
 * @param username 暱稱（必填，2~50字）
 * @param email    電子郵件（必填，合法格式）
 */
public record UserProfileUpdateRequest(

        @NotBlank(message = "暱稱不能為空")
        @Size(min = 2, max = 50, message = "暱稱長度須為 2~50 字元")
        String username,

        @NotBlank(message = "Email 不能為空")
        @Email(message = "請輸入合法的 Email 格式")
        String email
) {
}
