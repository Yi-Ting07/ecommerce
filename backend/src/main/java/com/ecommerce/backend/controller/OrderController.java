package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CheckoutRequest;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 顧客訂單 Controller
 *
 * /api/orders/** — 顧客自己的訂單操作（需要登入）
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 結帳：購物車 → 訂單 */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal String username,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(username, request));
    }

    /** 查詢我的訂單列表（分頁，最新在前） */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal String username,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getMyOrders(username, pageable));
    }

    /** 查詢我的訂單詳情 */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getMyOrderById(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getMyOrderById(username, id));
    }

    /** 取消訂單（只有 PENDING 狀態才能取消） */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelMyOrder(
            @AuthenticationPrincipal String username,
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelMyOrder(username, id));
    }
}
