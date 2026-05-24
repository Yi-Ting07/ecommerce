package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.UserStoreRequest;
import com.ecommerce.backend.dto.UserStoreResponse;
import com.ecommerce.backend.model.DeliveryMethod;
import com.ecommerce.backend.service.UserStoreService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員常用超商門市 API
 *
 *   GET    /api/user/stores?type=FAMILY_MART   → 取得常用門市清單
 *   POST   /api/user/stores                    → 新增常用門市
 *   DELETE /api/user/stores/{id}               → 刪除常用門市
 */
@RestController
@RequestMapping("/api/user/stores")
public class UserStoreController {

    private final UserStoreService userStoreService;

    public UserStoreController(UserStoreService userStoreService) {
        this.userStoreService = userStoreService;
    }

    @GetMapping
    public ResponseEntity<List<UserStoreResponse>> getMyStores(
            @RequestParam DeliveryMethod type,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userStoreService.getMyStores(username, type));
    }

    @PostMapping
    public ResponseEntity<UserStoreResponse> addStore(
            @Valid @RequestBody UserStoreRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userStoreService.addStore(username, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserStoreResponse> updateStore(
            @PathVariable Long id,
            @Valid @RequestBody UserStoreRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(userStoreService.updateStore(username, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        userStoreService.deleteStore(username, id);
        return ResponseEntity.noContent().build();
    }
}
