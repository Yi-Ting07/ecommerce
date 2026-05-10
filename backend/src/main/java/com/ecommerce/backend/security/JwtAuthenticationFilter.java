package com.ecommerce.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 認證過濾器
 *
 * 這是 Spring Security 的「守門員」：
 * 每一個進來的 HTTP 請求都會經過這個 Filter。
 *
 * 它做的事情：
 * 1. 從 Request Header 的 "Authorization: Bearer xxxxx" 取出 Token
 * 2. 呼叫 JwtUtils 驗證 Token 是否合法
 * 3. 如果合法 → 把使用者身分放進 Spring Security 的 Context 中
 *    這樣後續的 Controller 就能知道「誰」在發這個請求
 * 4. 如果不合法 → 不設定身分，後續的 Security 規則會回傳 401
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public JwtAuthenticationFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 從 Header 取出 Token
        String token = parseJwt(request);

        // 2. 如果有 Token 且驗證通過
        if (token != null && jwtUtils.validateToken(token)) {
            // 3. 取出使用者資訊
            String username = jwtUtils.getUsernameFromToken(token);
            String role = jwtUtils.getRoleFromToken(token);

            // 4. 建立 Spring Security 的認證物件
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null, // 不需要密碼（已用 Token 驗證過了）
                            List.of(new SimpleGrantedAuthority(role))
                    );

            // 5. 放進 Security Context（之後 Controller 可用 @AuthenticationPrincipal 取得）
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 繼續執行下一個 Filter
        filterChain.doFilter(request, response);
    }

    /**
     * 從 Request Header 解析出 JWT Token
     * Header 格式：Authorization: Bearer eyJhbGciOiJI...
     */
    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // 去掉 "Bearer " 前綴
        }
        return null;
    }
}
