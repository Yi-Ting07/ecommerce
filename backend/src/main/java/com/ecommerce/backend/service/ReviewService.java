package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ReviewRequest;
import com.ecommerce.backend.dto.ReviewResponse;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.Review;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ReviewRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReviewService — 商品評論業務邏輯
 *
 * 【流程說明】
 * 1. 顧客查看評論：公開，任何人都能呼叫
 * 2. 顧客新增評論：需登入，同一商品每人只能留一則
 * 3. 顧客刪除評論：只能刪除自己的
 * 4. 管理員刪除：可刪除任意評論（處理不當言論）
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ===== 公開 =====

    /**
     * 取得商品的所有評論（依時間倒序）
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        // 確認商品存在
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在");
        }
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== 需要登入 =====

    /**
     * 顧客新增評論
     *
     * @param productId 商品 ID
     * @param username  目前登入的使用者名稱（從 JWT 取得）
     * @param request   評論請求（星評 + 內文）
     */
    @Transactional
    public ReviewResponse addReview(Long productId, String username, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));

        // 檢查是否已評論過此商品
        if (reviewRepository.findByProductIdAndUserId(productId, user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "您已對此商品留過評論，每個商品只能評論一次");
        }

        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(request.rating())
                .content(request.content())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    /**
     * 顧客刪除自己的評論
     */
    @Transactional
    public void deleteMyReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "評論不存在"));

        // 只能刪除自己的評論
        if (!review.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權刪除此評論");
        }

        reviewRepository.delete(review);
    }

    // ===== 管理員 =====

    /**
     * 後台分頁查詢所有評論
     */
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        return reviewRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    /**
     * 管理員刪除任意評論（不當言論）
     */
    @Transactional
    public void adminDeleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "評論不存在");
        }
        reviewRepository.deleteById(reviewId);
    }

    // ===== 工具方法 =====

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getProduct().getName(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
