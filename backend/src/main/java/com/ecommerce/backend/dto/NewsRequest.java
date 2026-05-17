package com.ecommerce.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 建立 / 更新新聞公告的請求 DTO
 *
 * 使用 Java Record，欄位自動 immutable。
 * @NotBlank  — 不能為 null 且不能全空白
 * @Size      — 長度限制，與 Entity Column 定義一致
 */
public record NewsRequest(
        @NotBlank(message = "標題不能為空")
        @Size(max = 200, message = "標題不可超過 200 字元")
        String title,

        @NotBlank(message = "內容不能為空")
        @Size(max = 5000, message = "內容不可超過 5000 字元")
        String content,

        Boolean pinned,

        Boolean published
) {
}
