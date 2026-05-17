package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Order;
import com.ecommerce.backend.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 查詢某使用者的所有訂單（依建立時間降序，最新在前）
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 管理員查詢：依狀態過濾（不分使用者）
    Page<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

    // 管理員查詢：所有訂單
    Page<Order> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // ===== 儀表板統計用 =====

    // 計算總營收（排除已取消的訂單）
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status <> 'CANCELLED'")
    BigDecimal getTotalRevenue();

    // 各訂單狀態的數量，回傳 Object[]{ OrderStatus, count }
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countGroupByStatus();
}
