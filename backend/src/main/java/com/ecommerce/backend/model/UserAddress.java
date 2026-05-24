package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 會員常用收貨地址
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 標籤（如：家、公司、學校）
    @Column(nullable = false, length = 30)
    private String label;

    // 收貨人姓名
    @Column(nullable = false, length = 50)
    private String recipientName;

    // 收貨人電話（含國碼）
    @Column(nullable = false, length = 30)
    private String recipientPhone;

    // 地址
    @Column(nullable = false, length = 300)
    private String address;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
