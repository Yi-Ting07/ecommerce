package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CartAddRequest;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.CartUpdateRequest;
import com.ecommerce.backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 購物車 Controller
 *
 * 所有 /api/cart/** 的 API。
 * 需要登入（SecurityConfig 中設定 anyRequest().authenticated()）。
 *
 * @AuthenticationPrincipal String username
 * → 從 JWT Token 取出 username（就是 JwtAuthenticationFilter 放入 SecurityContext 的 principal）
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /** 取得目前使用者的購物車 */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(cartService.getCart(username));
    }

    /** 加入商品到購物車（若已存在則增加數量） */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody CartAddRequest request) {
        return ResponseEntity.ok(cartService.addItem(username, request));
    }

    /** 更新購物車中某項目的數量 */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal String username,
            @PathVariable Long itemId,
            @Valid @RequestBody CartUpdateRequest request) {
        return ResponseEntity.ok(cartService.updateItem(username, itemId, request));
    }

    /** 從購物車移除某項目 */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal String username,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeItem(username, itemId));
    }

    /** 清空整個購物車 */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal String username) {
        cartService.clearCart(username);
        return ResponseEntity.noContent().build();
    }
}
