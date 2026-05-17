package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 購物車明細 Entity
 *
 * 對應資料庫 cart_items 資料表。
 * 每一筆記錄代表「某個使用者的購物車裡有幾個某商品」。
 *
 * 設計重點：
 * - 購物車是 server-side（存在 DB），所以登入後不同裝置也能看到同一個購物車
 * - 同一個使用者對同一個商品只會有一筆記錄，數量用 quantity 表示
 * - unique constraint 防止重複 (user + product)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多對一：多個購物車項目屬於同一個使用者
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 多對一：多個購物車項目可以是同一個商品
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
