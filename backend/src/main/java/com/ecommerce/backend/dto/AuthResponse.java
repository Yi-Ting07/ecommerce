package com.ecommerce.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 登入 / 驗證回應 DTO
 *
 * 一般登入：token 有值，requiresTwoFactor 為 false
 * 需要 2FA：token 為 null，requiresTwoFactor 為 true，tempToken 有值
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String token,
        String username,
        String email,
        String role,
        String message,
        Boolean requiresTwoFactor,
        String twoFactorMethod,
        String tempToken
) {
    /** 工廠方法：一般登入成功 */
    public static AuthResponse success(String token, String username, String email, String role) {
        return new AuthResponse(token, username, email, role, "登入成功", null, null, null);
    }

    /** 工廠方法：需要進行 2FA */
    public static AuthResponse pendingTwoFactor(String username, String email, String role,
                                                String method, String tempToken) {
        return new AuthResponse(null, username, email, role, "請完成雙重驗證",
                true, method, tempToken);
    }

    /** 工廠方法：註冊成功 */
    public static AuthResponse registered(String username, String email, String role) {
        return new AuthResponse(null, username, email, role, "註冊成功，請登入", null, null, null);
    }
}
