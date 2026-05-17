package com.ecommerce.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 新增評論請求
 *
 * @param rating  星評（1~5，必填）
 * @param content 評論文字（選填，最多 1000 字）
 */
public record ReviewRequest(

        @NotNull(message = "請提供星評")
        @Min(value = 1, message = "星評最低 1 顆星")
        @Max(value = 5, message = "星評最高 5 顆星")
        Integer rating,

        @Size(max = 1000, message = "評論內容不能超過 1000 字")
        String content
) {
}
