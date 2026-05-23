package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.AuthLoginRequest;
import com.ecommerce.backend.dto.AuthRegisterRequest;
import com.ecommerce.backend.dto.AuthResponse;
import com.ecommerce.backend.dto.CaptchaResponse;
import com.ecommerce.backend.service.AuthService;
import com.ecommerce.backend.service.CaptchaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    public AuthController(AuthService authService, CaptchaService captchaService) {
        this.authService = authService;
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        CaptchaService.CaptchaResult result = captchaService.generateCaptcha();
        return ResponseEntity.ok(new CaptchaResponse(result.captchaId(), result.captchaImage()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthLoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
