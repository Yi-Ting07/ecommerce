package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.TwoFactorSetupTotpResponse;
import com.ecommerce.backend.model.TwoFactorMethod;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtils;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 雙重驗證（2FA）服務
 *
 * 支援兩種驗證方式：
 * - TOTP：Google Authenticator 時間型一次性密碼
 * - EMAIL：6 位數字 OTP 寄至使用者信箱
 */
@Service
public class TwoFactorService {

    private static final Logger log = LoggerFactory.getLogger(TwoFactorService.class);

    /** 臨時 2FA 待驗證狀態（tempToken → 待驗資訊），有效期 5 分鐘 */
    private final Map<String, PendingSession> pendingSessions = new ConcurrentHashMap<>();

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    private final SecureRandom secureRandom = new SecureRandom();

    /** 可選：若未設定 spring.mail.host 則 JavaMailSender 不會被注入 */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${app.name:E-Commerce Shop}")
    private String appName;

    public TwoFactorService(UserRepository userRepository, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.jwtUtils = jwtUtils;
    }

    // ============================
    // 登入流程 — 建立 / 驗證 2FA 階段
    // ============================

    /**
     * 建立一個臨時 2FA 待驗 session，回傳 tempToken UUID
     */
    public String createPendingSession(Long userId, TwoFactorMethod method) {
        cleanExpiredSessions();
        String tempToken = UUID.randomUUID().toString();
        pendingSessions.put(tempToken, new PendingSession(userId, method, null,
                LocalDateTime.now().plusMinutes(5)));
        return tempToken;
    }

    /**
     * 發送 Email OTP（在 createPendingSession 之後呼叫）
     */
    public void sendEmailOtp(String tempToken, String email, String username) {
        PendingSession session = getValidSession(tempToken);

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        // 更新 session 存入 OTP
        pendingSessions.put(tempToken, new PendingSession(
                session.userId(), TwoFactorMethod.EMAIL, otp, session.expiresAt()));

        if (mailSender == null) {
            // 開發模式：沒設定 SMTP，直接 log 出來方便測試
            log.warn("========================================");
            log.warn("【開發模式】Email OTP for {}: {}", email, otp);
            log.warn("（請設定 spring.mail.* 以啟用真實寄信功能）");
            log.warn("========================================");
            return;
        }

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailFrom.isEmpty() ? "noreply@example.com" : mailFrom);
            helper.setTo(email);
            helper.setSubject(appName + " — 登入驗證碼");
            helper.setText(buildEmailHtml(username, otp), true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("寄送 OTP 信件失敗", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "驗證信件寄送失敗，請稍後再試");
        }
    }

    /**
     * 驗證 2FA 代碼，成功則回傳含 JWT 的 AuthResponse
     */
    @Transactional(readOnly = true)
    public AuthResponse verifyAndLogin(String tempToken, String code) {
        PendingSession session = getValidSession(tempToken);

        User user = userRepository.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));

        boolean valid = switch (session.method()) {
            case TOTP  -> verifyTotp(user.getTwoFactorSecret(), code);
            case EMAIL -> verifyEmailOtp(session, code);
            case SMS   -> verifyEmailOtp(session, code); // OTP 驗證邏輯相同
        };

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "驗證碼錯誤或已過期");
        }

        // 驗證成功：移除 pending session，發 JWT
        pendingSessions.remove(tempToken);
        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return AuthResponse.success(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }

    // ============================
    // 個人設定 — TOTP 管理
    // ============================

    /**
     * 產生 TOTP 設定資訊（QR URI + secret），但尚未儲存到 DB（等使用者確認後才儲存）
     * 此處將暫存的 secret 記錄在 pendingSessions（重用 tempToken 機制）
     */
    public TwoFactorSetupTotpResponse generateTotpSetup(String username) {
        User user = getUser(username);

        GoogleAuthenticatorKey credentials = googleAuthenticator.createCredentials();
        String secret = credentials.getKey();

        // 將暫存 secret 存在 pending session 中等待確認
        String tempToken = UUID.randomUUID().toString();
        pendingSessions.put(tempToken, new PendingSession(
                user.getId(), TwoFactorMethod.TOTP, secret,
                LocalDateTime.now().plusMinutes(10)));

        String qrUri = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(appName, username, credentials);

        return new TwoFactorSetupTotpResponse(secret, qrUri, tempToken);
    }

    /**
     * 確認 TOTP 設定：使用者輸入 Authenticator 產生的代碼，驗證通過後啟用
     */
    @Transactional
    public void confirmTotpSetup(String username, String setupToken, String code) {
        PendingSession session = getValidSession(setupToken);

        if (!verifyTotp(session.emailOtp(), code)) {   // emailOtp 欄位此處存的是 secret
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "驗證碼錯誤，請確認 Authenticator App 時間是否正確");
        }

        User user = getUser(username);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(TwoFactorMethod.TOTP);
        user.setTwoFactorSecret(session.emailOtp()); // 存儲 secret
        userRepository.save(user);
        pendingSessions.remove(setupToken);
    }

    // ============================
    // 個人設定 — Email 2FA 管理
    // ============================

    /**
     * 啟用 Email OTP（直接啟用，使用帳號 Email）
     */
    @Transactional
    public void enableEmailTwoFactor(String username) {
        User user = getUser(username);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(TwoFactorMethod.EMAIL);
        user.setTwoFactorSecret(null);
        user.setSmsPhone(null);
        userRepository.save(user);
    }

    // ============================
    // 個人設定 — SMS 管理
    // ============================

    /**
     * 啟用 SMS OTP（需提供手機號碼）
     */
    @Transactional
    public void enableSmsTwoFactor(String username, String phone) {
        if (phone == null || !phone.matches("^\\+?[0-9]{8,15}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "手機號碼格式不正確（如：+886912345678）");
        }
        User user = getUser(username);
        user.setTwoFactorEnabled(true);
        user.setTwoFactorMethod(TwoFactorMethod.SMS);
        user.setTwoFactorSecret(null);
        user.setSmsPhone(phone);
        userRepository.save(user);
    }

    /**
     * 登入流程：發送 SMS OTP（在 createPendingSession 之後呼叫）
     */
    public void sendSmsOtp(String tempToken, String phone, String username) {
        PendingSession session = getValidSession(tempToken);

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        pendingSessions.put(tempToken, new PendingSession(
                session.userId(), TwoFactorMethod.SMS, otp, session.expiresAt()));

        // 開發模式：log 出來（正式環境請串接 Twilio / 三竹 SMSAPI 等服務）
        log.warn("========================================");
        log.warn("【開發模式】SMS OTP for {} ({}): {}", username, phone, otp);
        log.warn("（正式環境請設定 SMS 服務商，如 Twilio）");
        log.warn("========================================");
    }

    /**
     * 重新發送 OTP（Email 或 SMS，依 session 方法決定）
     */
    public void resendEmailOtp(String tempToken) {
        PendingSession session = getValidSession(tempToken);
        User user = userRepository.findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "使用者不存在"));

        if (session.method() == TwoFactorMethod.SMS) {
            sendSmsOtp(tempToken, user.getSmsPhone(), user.getUsername());
        } else if (session.method() == TwoFactorMethod.EMAIL) {
            sendEmailOtp(tempToken, user.getEmail(), user.getUsername());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "此驗證方式不支援重新發送");
        }
    }

    // ============================
    // 個人設定 — 停用 2FA
    // ============================

    @Transactional
    public void disableTwoFactor(String username) {
        User user = getUser(username);
        user.setTwoFactorEnabled(false);
        user.setTwoFactorMethod(null);
        user.setTwoFactorSecret(null);
        user.setSmsPhone(null);
        userRepository.save(user);
    }

    // ============================
    // 私有工具方法
    // ============================

    private boolean verifyTotp(String secret, String code) {
        try {
            int intCode = Integer.parseInt(code.trim());
            return googleAuthenticator.authorize(secret, intCode);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean verifyEmailOtp(PendingSession session, String code) {
        return session.emailOtp() != null && session.emailOtp().equals(code.trim());
    }

    private PendingSession getValidSession(String tempToken) {
        PendingSession session = pendingSessions.get(tempToken);
        if (session == null || session.expiresAt().isBefore(LocalDateTime.now())) {
            pendingSessions.remove(tempToken);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "驗證已過期，請重新登入");
        }
        return session;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }

    private void cleanExpiredSessions() {
        LocalDateTime now = LocalDateTime.now();
        pendingSessions.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(now));
    }

    private String buildEmailHtml(String username, String otp) {
        return """
                <div style="font-family: Arial, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; border: 1px solid #e0e0e0; border-radius: 8px;">
                  <h2 style="color: #333; margin-top: 0;">登入驗證碼</h2>
                  <p>嗨 <strong>%s</strong>，</p>
                  <p>您的登入驗證碼為：</p>
                  <div style="background: #f5f5f5; padding: 16px; text-align: center; font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #409eff; border-radius: 4px;">
                    %s
                  </div>
                  <p style="color: #888; font-size: 13px; margin-top: 16px;">此驗證碼有效期為 <strong>5 分鐘</strong>，請勿分享給任何人。</p>
                </div>
                """.formatted(username, otp);
    }

    /** 待驗 session 資料 */
    private record PendingSession(
            Long userId,
            TwoFactorMethod method,
            String emailOtp,        // EMAIL 方式時存 OTP；TOTP 設定時存 secret
            LocalDateTime expiresAt
    ) {}
}
