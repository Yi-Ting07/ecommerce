package com.ecommerce.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 健康檢查 Controller
 *
 * 提供一個簡單的 API 端點，用來確認後端是否正常運行。
 * 前端開發時也可以用這個 API 來測試串接是否成功。
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "E-Commerce Backend is running!",
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
