package com.ecommerce.backend.dto;

import java.time.LocalDateTime;

/**
 * 評論回應
 *
 * @param id          評論 ID
 * @param productId   商品 ID
 * @param productName 商品名稱
 * @param userId      使用者 ID
 * @param username    使用者名稱
 * @param rating      星評（1~5）
 * @param content     評論文字
 * @param createdAt   建立時間
 */
public record ReviewResponse(
        Long id,
        Long productId,
        String productName,
        Long userId,
        String username,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
}
