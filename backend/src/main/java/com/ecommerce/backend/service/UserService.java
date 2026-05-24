package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.PasswordChangeRequest;
import com.ecommerce.backend.dto.UserProfileResponse;
import com.ecommerce.backend.dto.UserProfileUpdateRequest;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * UserService — 會員個人中心業務邏輯
 *
 * 【功能說明】
 * 1. 取得個人資料：回傳目前登入使用者的基本資訊
 * 2. 修改個人資料：可修改暱稱（username）及 Email
 * 3. 更改密碼：需驗證舊密碼，才能設定新密碼（避免別人偷改）
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 取得個人資料
     *
     * @param username 目前登入的使用者名稱（從 JWT 取得）
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        User user = findUser(username);
        return toResponse(user);
    }

    /**
     * 修改個人資料（暱稱 & Email）
     *
     * @param username 目前登入的使用者名稱
     * @param request  新的暱稱與 Email
     */
    @Transactional
    public UserProfileResponse updateProfile(String username, UserProfileUpdateRequest request) {
        User user = findUser(username);

        // 若更改 Email，需確認新 Email 沒有被其他帳號佔用
        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "此 Email 已被其他帳號使用");
        }

        // 若更改暱稱，需確認沒有重複
        if (!user.getUsername().equals(request.username())
                && userRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "此暱稱已被其他帳號使用");
        }

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setAddress(request.address());

        return toResponse(userRepository.save(user));
    }

    /**
     * 更改密碼
     *
     * @param username 目前登入的使用者名稱
     * @param request  舊密碼 + 新密碼
     */
    @Transactional
    public void changePassword(String username, PasswordChangeRequest request) {
        User user = findUser(username);

        // 驗證目前密碼是否正確
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "目前密碼不正確");
        }

        // 新密碼不能與舊密碼相同
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "新密碼不能與目前密碼相同");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ===== 工具方法 =====

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name(),
                user.getPhone(),
                user.getAddress(),
                user.getCreatedAt()
        );
    }
}
