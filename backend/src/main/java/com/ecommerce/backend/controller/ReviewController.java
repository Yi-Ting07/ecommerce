package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.ReviewRequest;
import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ReviewController — 商品評論 API
 *
 * 公開端點：
 *   GET  /api/products/{productId}/reviews     → 取得商品所有評論
 *
 * 需登入端點：
 *   POST   /api/products/{productId}/reviews   → 新增評論（每人每商品限一則）
 *   DELETE /api/reviews/{reviewId}             → 刪除自己的評論
 *
 * 【@AuthenticationPrincipal】
 * Spring Security 會把 JWT Filter 存進 SecurityContext 的使用者名稱
 * 注入進來，不需要手動解析 Token
 */
@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 取得商品評論列表（公開）
     */
    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    /**
     * 新增評論（需登入）
     */
    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewResponse> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.addReview(productId, username, request));
    }

    /**
     * 刪除自己的評論（需登入）
     */
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteMyReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal String username) {
        reviewService.deleteMyReview(reviewId, username);
        return ResponseEntity.noContent().build();
    }
}
