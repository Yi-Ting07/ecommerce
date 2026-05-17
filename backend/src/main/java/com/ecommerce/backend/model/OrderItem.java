package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 訂單明細 Entity
 *
 * 對應資料庫 order_items 資料表。
 * 代表一筆訂單中的一個商品項目。
 *
 * ⚠️ 重要設計：快照（Snapshot）模式
 * productName 和 price 是「下單當下」的資訊快照。
 * 即使之後商品被改名或改價，這筆訂單的記錄仍然正確。
 * product 欄位可能為 null（若商品被刪除），但 productName 和 price 永遠存在。
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多對一：多個明細屬於同一筆訂單
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // 關聯到商品（nullable，因為商品可能被刪除）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    // ↓ 快照資料（商品被刪或改後仍保留）
    @Column(nullable = false, length = 150)
    private String productName;  // 下單時的商品名稱

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;    // 下單時的單價

    @Column(nullable = false)
    private Integer quantity;
}
