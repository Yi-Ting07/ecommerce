package com.ecommerce.backend.dto;

import java.time.LocalDateTime;

/**
 * 新聞公告回應 DTO
 *
 * 將 Entity 轉成前端需要的格式回傳，不直接暴露 Entity。
 */
public record NewsResponse(
        Long id,
        String title,
        String content,
        boolean pinned,
        boolean published,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
