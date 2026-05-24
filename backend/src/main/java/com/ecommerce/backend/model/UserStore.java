package com.ecommerce.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 會員常用超商門市
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_stores")
public class UserStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 超商類型（FAMILY_MART / SEVEN_ELEVEN）
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryMethod storeType;

    // 門市名稱
    @Column(nullable = false, length = 100)
    private String storeName;

    // 門市店號
    @Column(nullable = false, length = 20)
    private String storeCode;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
