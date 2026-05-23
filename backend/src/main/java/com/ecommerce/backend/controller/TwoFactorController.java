package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.TwoFactorSetupTotpResponse;
import com.ecommerce.backend.dto.TwoFactorStatusResponse;
import com.ecommerce.backend.dto.TwoFactorVerifyRequest;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.TwoFactorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * TwoFactorController — 雙重驗證 API
 *
 * 不需登入（登入流程用）：
 *   POST  /api/auth/2fa/verify        → 驗證 2FA 代碼，成功回傳 JWT
 *   POST  /api/auth/2fa/resend-email  → 重新寄送 Email OTP
 *
 * 需要登入（個人設定用）：
 *   GET    /api/user/2fa/status        → 查詢目前 2FA 狀態
 *   POST   /api/user/2fa/setup/totp   → 取得 TOTP 設定資訊（QR URI）
 *   POST   /api/user/2fa/confirm/totp → 確認 TOTP 設定（輸入 App 產生的代碼）
 *   POST   /api/user/2fa/setup/email  → 啟用 Email OTP
 *   DELETE /api/user/2fa              → 停用 2FA
 */
@RestController
public class TwoFactorController {

    private final TwoFactorService twoFactorService;
    private final UserRepository userRepository;

    public TwoFactorController(TwoFactorService twoFactorService, UserRepository userRepository) {
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
    }

    // ============================
    // 登入流程 — 不需要 JWT
    // ============================

    /** 驗證 2FA 代碼，通過後回傳 JWT */
    @PostMapping("/api/auth/2fa/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody TwoFactorVerifyRequest request) {
        AuthResponse response = twoFactorService.verifyAndLogin(request.tempToken(), request.code());
        return ResponseEntity.ok(response);
    }

    /** 重新寄送 Email OTP（只在方法是 EMAIL 時有效） */
    @PostMapping("/api/auth/2fa/resend-email")
    public ResponseEntity<Map<String, String>> resendEmail(@RequestBody Map<String, String> body) {
        String tempToken = body.get("tempToken");
        if (tempToken == null || tempToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tempToken 必填");
        }
        // 這裡需要從 pending session 取回 email，透過 user id 查詢
        // TwoFactorService 提供了重送方法
        twoFactorService.resendEmailOtp(tempToken);
        return ResponseEntity.ok(Map.of("message", "驗證碼已重新發送"));
    }

    // ============================
    // 個人設定 — 需要 JWT
    // ============================

    /** 查詢目前 2FA 狀態 */
    @GetMapping("/api/user/2fa/status")
    public ResponseEntity<TwoFactorStatusResponse> getStatus(
            @AuthenticationPrincipal String username) {
        User user = getUser(username);
        return ResponseEntity.ok(new TwoFactorStatusResponse(
                user.isTwoFactorEnabled(),
                user.getTwoFactorMethod() != null ? user.getTwoFactorMethod().name() : null));
    }

    /** 取得 TOTP 設定 QR code 資訊 */
    @PostMapping("/api/user/2fa/setup/totp")
    public ResponseEntity<TwoFactorSetupTotpResponse> setupTotp(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(twoFactorService.generateTotpSetup(username));
    }

    /** 確認 TOTP 設定（輸入 Authenticator App 產生的代碼） */
    @PostMapping("/api/user/2fa/confirm/totp")
    public ResponseEntity<Map<String, String>> confirmTotp(
            @AuthenticationPrincipal String username,
            @RequestBody Map<String, String> body) {
        String setupToken = body.get("setupToken");
        String code = body.get("code");
        if (setupToken == null || code == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "setupToken 與 code 必填");
        }
        twoFactorService.confirmTotpSetup(username, setupToken, code);
        return ResponseEntity.ok(Map.of("message", "Google Authenticator 已成功啟用"));
    }

    /** 啟用 Email OTP */
    @PostMapping("/api/user/2fa/setup/email")
    public ResponseEntity<Map<String, String>> setupEmail(
            @AuthenticationPrincipal String username) {
        twoFactorService.enableEmailTwoFactor(username);
        return ResponseEntity.ok(Map.of("message", "Email 雙重驗證已成功啟用"));
    }

    /** 啟用 SMS OTP（需提供手機號碼） */
    @PostMapping("/api/user/2fa/setup/sms")
    public ResponseEntity<Map<String, String>> setupSms(
            @AuthenticationPrincipal String username,
            @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "手機號碼必填");
        }
        twoFactorService.enableSmsTwoFactor(username, phone);
        return ResponseEntity.ok(Map.of("message", "手機簡訊雙重驗證已成功啟用"));
    }

    /** 停用 2FA */
    @DeleteMapping("/api/user/2fa")
    public ResponseEntity<Map<String, String>> disable(
            @AuthenticationPrincipal String username) {
        twoFactorService.disableTwoFactor(username);
        return ResponseEntity.ok(Map.of("message", "雙重驗證已停用"));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }
}
