package com.ecommerce.backend.dto;

public record TwoFactorSetupTotpResponse(
        String secret,      // Base32 密鑰（供手動輸入至 Authenticator App）
        String qrUri,       // otpauth:// URI（供前端生成 QR Code）
        String setupToken   // 暫存 token，確認 TOTP 時需要帶入
) {}
