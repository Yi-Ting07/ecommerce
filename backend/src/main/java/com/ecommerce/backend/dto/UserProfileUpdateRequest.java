package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 會員更新個人資料請求
 */
public record UserProfileUpdateRequest(

        @NotBlank(message = "暱稱不能為空")
        @Size(min = 2, max = 50, message = "暱稱長度須為 2~50 字元")
        String username,

        @NotBlank(message = "Email 不能為空")
        @Email(message = "請輸入合法的 Email 格式")
        String email,

        // 聯絡電話（含國碼，可選填）
        @Size(max = 30, message = "電話不超過 30 字元")
        String phone,

        // 常用地址（可選填）
        @Size(max = 300, message = "地址不超過 300 字元")
        String address
) {
}
