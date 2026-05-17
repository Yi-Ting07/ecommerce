package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.PasswordChangeRequest;
import com.ecommerce.backend.dto.UserProfileResponse;
import com.ecommerce.backend.dto.UserProfileUpdateRequest;
import com.ecommerce.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController — 會員個人中心 API
 *
 * 所有端點皆需要登入（anyRequest().authenticated()）
 *
 *   GET  /api/user/profile    → 取得個人資料
 *   PUT  /api/user/profile    → 修改暱稱 & Email
 *   PUT  /api/user/password   → 更改密碼
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 取得個人資料
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userService.getProfile(username));
    }

    /**
     * 修改個人資料（暱稱 & Email）
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }

    /**
     * 更改密碼
     */
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequest request,
            @AuthenticationPrincipal String username) {
        userService.changePassword(username, request);
        return ResponseEntity.noContent().build();
    }
}
