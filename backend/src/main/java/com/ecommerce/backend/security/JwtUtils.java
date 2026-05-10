package com.ecommerce.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具類
 *
 * JWT（JSON Web Token）是前後端分離中最常用的身分驗證方式：
 * 1. 使用者登入成功 → 後端產生一個 JWT Token 回傳給前端
 * 2. 前端之後每次發 API 請求，都在 Header 帶上這個 Token
 * 3. 後端收到請求，驗證 Token 是否合法、是否過期
 *
 * 這個類別負責：產生 Token、從 Token 取出使用者名稱、驗證 Token
 */
@Component
public class JwtUtils {

    // 從 application.properties 讀取密鑰
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // 從 application.properties 讀取 Token 有效時間
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * 產生 JWT Token
     * @param username 使用者名稱（會存進 Token 裡）
     * @param role 使用者角色（ROLE_USER 或 ROLE_ADMIN）
     * @return 簽名過的 JWT Token 字串
     */
    public String generateToken(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(username)                              // Token 的主體（使用者名稱）
                .claim("role", role)                            // 自訂欄位：角色
                .issuedAt(new Date())                           // 簽發時間
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // 過期時間
                .signWith(key)                                  // 用密鑰簽名
                .compact();                                     // 產生 Token 字串
    }

    /**
     * 從 Token 中取出使用者名稱
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * 從 Token 中取出角色
     */
    public String getRoleFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    /**
     * 驗證 Token 是否合法（未過期、未被篡改）
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // Token 無效（過期、被篡改、格式錯誤等）
            return false;
        }
    }
}
