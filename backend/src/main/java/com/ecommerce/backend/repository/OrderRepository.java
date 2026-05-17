package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 查詢某使用者的所有訂單（依建立時間降序，最新在前）
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 管理員查詢：依狀態過濾（不分使用者）
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    // 管理員查詢：所有訂單
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
