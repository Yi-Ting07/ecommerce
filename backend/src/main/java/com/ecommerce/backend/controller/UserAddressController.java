package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.UserAddressRequest;
import com.ecommerce.backend.dto.UserAddressResponse;
import com.ecommerce.backend.service.UserAddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員常用收貨地址 API
 *
 *   GET    /api/user/addresses        → 取得常用地址清單
 *   POST   /api/user/addresses        → 新增常用地址
 *   DELETE /api/user/addresses/{id}   → 刪除常用地址
 */
@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {

    private final UserAddressService userAddressService;

    public UserAddressController(UserAddressService userAddressService) {
        this.userAddressService = userAddressService;
    }

    @GetMapping
    public ResponseEntity<List<UserAddressResponse>> getMyAddresses(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userAddressService.getMyAddresses(username));
    }

    @PostMapping
    public ResponseEntity<UserAddressResponse> addAddress(
            @Valid @RequestBody UserAddressRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userAddressService.addAddress(username, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserAddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody UserAddressRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userAddressService.updateAddress(username, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        userAddressService.deleteAddress(username, id);
        return ResponseEntity.noContent().build();
    }
}
