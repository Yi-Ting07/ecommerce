package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 取得某商品的所有評論（依建立時間倒序）
     */
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);

    /**
     * 取得某使用者的某商品評論（用於檢查是否已評論）
     */
    Optional<Review> findByProductIdAndUserId(Long productId, Long userId);

    /**
     * 後台分頁查詢全部評論
     */
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
