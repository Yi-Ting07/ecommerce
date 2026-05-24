package com.ecommerce.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    // ===== 雙重驗證（2FA）欄位 =====
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean twoFactorEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private TwoFactorMethod twoFactorMethod;

    // TOTP 的 base32 密鑰（只有 TOTP 方式才有值）
    @Column(length = 64)
    private String twoFactorSecret;

    // SMS 手機號碼（只有 SMS 方式才有值，格式：+886912345678）
    @Column(length = 20)
    private String smsPhone;

    // 會員聯絡電話（含國碼，格式：+886912345678）
    @Column(length = 30)
    private String phone;

    // 會員常用地址
    @Column(length = 300)
    private String address;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
