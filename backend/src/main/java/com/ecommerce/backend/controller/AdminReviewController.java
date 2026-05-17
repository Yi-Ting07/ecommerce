package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.service.ReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminReviewController — 後台評論管理 API
 *
 * 需要 ROLE_ADMIN（由 SecurityConfig 統一攔截 /api/admin/**）
 *
 *   GET    /api/admin/reviews         → 分頁查詢全部評論
 *   DELETE /api/admin/reviews/{id}    → 刪除不當評論
 */
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 分頁列出所有評論（最新的在前）
     */
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(reviewService.getAllReviews(pageable));
    }

    /**
     * 刪除指定評論（不當言論處理）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.adminDeleteReview(id);
        return ResponseEntity.noContent().build();
    }
}
