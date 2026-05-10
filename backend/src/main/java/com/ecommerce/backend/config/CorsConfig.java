package com.ecommerce.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS（跨來源資源共用）設定
 *
 * 為什麼需要這個？
 * 前端跑在 localhost:5173，後端跑在 localhost:8080，
 * 瀏覽器預設會阻擋不同 port 之間的 HTTP 請求（跨域）。
 * 這個設定就是告訴後端：「允許 localhost:5173 來的請求通過」。
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允許的前端網址（開發環境）
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // 允許的 HTTP 方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允許的 Header（Authorization 用來傳 JWT Token）
        config.setAllowedHeaders(List.of("*"));

        // 允許攜帶 Cookie（若未來需要）
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 對所有 API 路徑套用此 CORS 設定
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
