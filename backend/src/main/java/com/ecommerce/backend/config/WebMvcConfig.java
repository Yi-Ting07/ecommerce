package com.ecommerce.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Spring MVC 靜態資源設定
 *
 * 【問題】
 * 圖片上傳後儲存在專案外部的 uploads/ 目錄，
 * Spring Boot 預設只提供 classpath 內的靜態資源，
 * 所以需要額外設定才能讓瀏覽器透過 /uploads/xxx.jpg 存取圖片。
 *
 * 【解法】
 * 使用 addResourceHandlers 將 URL 路徑 /uploads/**
 * 對應到實際的檔案系統目錄（絕對路徑）。
 *
 * 例如：
 *   GET /uploads/abc123.png
 *   → 讀取 {專案根目錄}/uploads/abc123.png
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 取得上傳目錄的絕對路徑
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().toString();

        // 將 /uploads/** 對應到檔案系統目錄
        // 注意：file: 前綴代表檔案系統路徑；末尾要加 /
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
