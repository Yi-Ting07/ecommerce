package com.ecommerce.backend.model;

public enum TwoFactorMethod {
    TOTP,   // Google Authenticator（時間型一次性密碼）
    EMAIL,  // Email OTP
    SMS     // 簡訊 OTP
}
