package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.OrderStatusUpdateRequest;
import com.ecommerce.backend.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 管理員訂單 Controller
 *
 * /api/admin/orders/** — 管理員查看/管理所有訂單（需要 ROLE_ADMIN）
 * SecurityConfig 中已設定 /api/admin/** 只允許 ROLE_ADMIN
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 查詢所有訂單（支援狀態過濾）
     *
     * GET /api/admin/orders?status=PENDING&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAdminOrders(status, pageable));
    }

    /** 查詢單筆訂單詳情 */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getAdminOrderById(id));
    }

    /**
     * 更新訂單狀態
     *
     * PUT /api/admin/orders/{id}/status
     * Body: { "status": "CONFIRMED" }
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request));
    }
}
