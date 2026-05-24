package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 訂單 Entity
 *
 * 對應資料庫 orders 資料表。
 * 代表一筆完整的訂單（可能包含多個商品）。
 *
 * 設計重點：
 * - 結帳時由購物車產生，同時建立多筆 OrderItem
 * - totalAmount 在結帳時計算並儲存，不動態計算（確保金額準確）
 * - status 由管理員推進，顧客只能取消 PENDING 狀態
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多對一：多筆訂單屬於同一個使用者
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 訂單總金額（結帳時計算，之後不再更動）
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    // 訂單狀態（用字串存入 DB，方便閱讀）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    // 收貨地址（舊欄位，保留相容性，宅配改用 recipientAddress）
    @Column(length = 300)
    private String shippingAddress;

    // ===== 訂購人資訊 =====
    @Column(length = 50)
    private String ordererName;

    @Column(length = 30)
    private String ordererPhone;

    @Column(length = 300)
    private String ordererAddress;

    // ===== 收貨人資訊 =====
    @Column(length = 50)
    private String recipientName;

    @Column(length = 30)
    private String recipientPhone;

    // ===== 送貨方式 =====
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DeliveryMethod deliveryMethod;

    // 收貨地址（宅配用）
    @Column(length = 300)
    private String recipientAddress;

    // 超商門市名稱（超商取貨用）
    @Column(length = 100)
    private String storeName;

    // 超商門市店號（超商取貨用）
    @Column(length = 20)
    private String storeCode;

    // ===== 付款方式 =====
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentMethod paymentMethod;

    // 一個訂單包含多個明細（CascadeType.ALL = 刪除訂單時也刪明細）
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
