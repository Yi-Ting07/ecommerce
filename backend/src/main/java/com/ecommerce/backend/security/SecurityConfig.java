package com.ecommerce.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Security 設定
 *
 * 這個類別定義了整個後端的安全規則：
 * - 哪些 API 不需要登入就能用（公開）
 * - 哪些 API 需要特定角色才能用（如管理員）
 * - 密碼加密方式
 * - JWT Filter 的位置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    /**
     * 安全過濾鏈 — 定義 API 的存取規則
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 啟用 CORS（使用我們在 CorsConfig 定義的設定）
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // 關閉 CSRF（前後端分離用 JWT，不需要 CSRF 保護）
            .csrf(csrf -> csrf.disable())

            // 不使用 Session（前後端分離用 Token，不需要 Session）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 定義 URL 權限規則
            .authorizeHttpRequests(auth -> auth
                // ✅ 公開 API（不用登入就能用）
                .requestMatchers("/api/auth/**").permitAll()     // 註冊、登入
                .requestMatchers("/api/health").permitAll()      // 健康檢查
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()  // 瀏覽商品
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll() // 瀏覽分類
                .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()      // 瀏覽最新消息
                .requestMatchers("/uploads/**").permitAll()      // 商品圖片

                // 🔒 管理員專用 API
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                // 🔐 其他所有 API 都需要登入
                .anyRequest().authenticated()
            )

            // 把我們的 JWT Filter 加在 Spring Security 預設的帳密驗證 Filter 之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼加密器
     * BCrypt 是目前最安全的密碼雜湊演算法之一
     * 註冊時用它來加密密碼，登入時用它來比對密碼
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認證管理器（Spring Security 內建的登入驗證機制會用到）
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
