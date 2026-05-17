package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 管理員更新訂單狀態請求 DTO
 *
 * 前端 PUT /api/admin/orders/{id}/status 時傳入：
 * { "status": "CONFIRMED" }
 */
public record OrderStatusUpdateRequest(
        @NotBlank(message = "狀態不能為空")
        String status
) {}
