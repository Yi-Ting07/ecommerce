package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.AuthLoginRequest;
import com.ecommerce.backend.dto.AuthRegisterRequest;
import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.model.TwoFactorMethod;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.UserRole;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.security.JwtUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CaptchaService captchaService;
    private final TwoFactorService twoFactorService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       CaptchaService captchaService,
                       @Lazy TwoFactorService twoFactorService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.captchaService = captchaService;
        this.twoFactorService = twoFactorService;
    }

    @Transactional
    public AuthResponse register(AuthRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email 已經被註冊");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "使用者名稱已經存在");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.registered(savedUser.getUsername(), savedUser.getEmail(), savedUser.getRole().name());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthLoginRequest request) {
        // 驗證驗證碼
        if (!captchaService.validateCaptcha(request.captchaId(), request.captchaCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "驗證碼錯誤或已過期，請重新獲取");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "帳號或密碼錯誤");
        }

        // 若使用者啟用了雙重驗證，不直接發 JWT，改回傳臨時 token
        if (user.isTwoFactorEnabled()) {
            String tempToken = twoFactorService.createPendingSession(user.getId(), user.getTwoFactorMethod());
            // Email / SMS OTP：後端直接觸發寄送
            if (user.getTwoFactorMethod() == TwoFactorMethod.EMAIL) {
                twoFactorService.sendEmailOtp(tempToken, user.getEmail(), user.getUsername());
            } else if (user.getTwoFactorMethod() == TwoFactorMethod.SMS) {
                twoFactorService.sendSmsOtp(tempToken, user.getSmsPhone(), user.getUsername());
            }
            return AuthResponse.pendingTwoFactor(
                    user.getUsername(), user.getEmail(), user.getRole().name(),
                    user.getTwoFactorMethod().name(), tempToken);
        }

        String token = jwtUtils.generateToken(user.getUsername(), user.getRole().name());
        return AuthResponse.success(token, user.getUsername(), user.getEmail(), user.getRole().name());
    }
}
