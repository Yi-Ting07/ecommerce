package com.ecommerce.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 新聞公告 Entity
 *
 * 欄位說明：
 * - title     : 標題（最多 200 字元）
 * - content   : 內文（最多 5000 字元，支援多行）
 * - pinned    : 是否置頂（true = 永遠顯示在最上方）
 * - published : 是否發佈（false = 草稿，不對外顯示）
 * - createdAt : 建立時間
 * - updatedAt : 最後更新時間
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "news")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    /** 置頂：置頂公告在列表最前面顯示 */
    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    /** 發佈狀態：false 為草稿，不對外顯示 */
    @Column(name = "is_published", nullable = false)
    private boolean published;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
