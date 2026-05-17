package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // 基本 CRUD 由 JpaRepository 提供即可
    // OrderItem 的新增/刪除都透過 Order 的 Cascade 處理
}
