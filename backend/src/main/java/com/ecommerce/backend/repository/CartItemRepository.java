package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 取得某使用者的所有購物車項目（依建立時間排序）
    List<CartItem> findByUserIdOrderByCreatedAtAsc(Long userId);

    // 檢查某使用者是否已加入某商品（避免重複加入，改為更新數量）
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    // 清空某使用者的購物車
    void deleteByUserId(Long userId);

    // 計算某使用者購物車中的商品種類數（用於顯示 badge）
    int countByUserId(Long userId);
}
