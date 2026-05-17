package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CartAddRequest;
import com.ecommerce.backend.dto.CartItemResponse;
import com.ecommerce.backend.dto.CartResponse;
import com.ecommerce.backend.dto.CartUpdateRequest;
import com.ecommerce.backend.model.CartItem;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 購物車 Service
 *
 * 業務邏輯說明：
 * 1. getCart：查詢使用者所有 CartItem，組裝成 CartResponse 回傳
 * 2. addItem：
 *    - 商品是否存在且有庫存
 *    - 若購物車已有此商品 → 疊加數量
 *    - 若沒有 → 新增一筆 CartItem
 * 3. updateItem：修改指定 CartItem 的數量（需確認是本人的 item）
 * 4. removeItem：刪除指定 CartItem（需確認是本人的 item）
 * 5. clearCart：刪除使用者所有 CartItem
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // ===========================
    // 取得購物車
    // ===========================
    @Transactional(readOnly = true)
    public CartResponse getCart(String username) {
        User user = findUser(username);
        List<CartItem> items = cartItemRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        return buildCartResponse(items);
    }

    // ===========================
    // 加入商品到購物車
    // ===========================
    @Transactional
    public CartResponse addItem(String username, CartAddRequest request) {
        User user = findUser(username);

        // 1. 確認商品存在且上架中
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "商品不存在"));

        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "商品已下架");
        }

        if (product.getStock() < request.quantity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "庫存不足，目前剩餘：" + product.getStock());
        }

        // 2. 若購物車已有此商品，疊加數量；否則新增
        CartItem cartItem = cartItemRepository
                .findByUserIdAndProductId(user.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            // 疊加後不能超過庫存
            int newQty = cartItem.getQuantity() + request.quantity();
            if (newQty > product.getStock()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "購物車數量超過庫存，目前剩餘：" + product.getStock());
            }
            cartItem.setQuantity(newQty);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.quantity())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        cartItemRepository.save(cartItem);

        // 回傳更新後的購物車
        return getCart(username);
    }

    // ===========================
    // 更新購物車項目數量
    // ===========================
    @Transactional
    public CartResponse updateItem(String username, Long itemId, CartUpdateRequest request) {
        User user = findUser(username);
        CartItem cartItem = findCartItemOwned(itemId, user.getId());

        // 確認新數量不超過庫存
        Product product = cartItem.getProduct();
        if (request.quantity() > product.getStock()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "庫存不足，目前剩餘：" + product.getStock());
        }

        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        return getCart(username);
    }

    // ===========================
    // 移除購物車項目
    // ===========================
    @Transactional
    public CartResponse removeItem(String username, Long itemId) {
        User user = findUser(username);
        findCartItemOwned(itemId, user.getId()); // 確認是本人的 item
        cartItemRepository.deleteById(itemId);
        return getCart(username);
    }

    // ===========================
    // 清空購物車
    // ===========================
    @Transactional
    public void clearCart(String username) {
        User user = findUser(username);
        cartItemRepository.deleteByUserId(user.getId());
    }

    // ===========================
    // 私有輔助方法
    // ===========================

    public User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }

    private CartItem findCartItemOwned(Long itemId, Long userId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "購物車項目不存在"));
        // 安全性檢查：確認這個 item 屬於發送請求的使用者
        if (!item.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權操作此項目");
        }
        return item;
    }

    private CartResponse buildCartResponse(List<CartItem> items) {
        List<CartItemResponse> responseItems = items.stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal total = responseItems.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(responseItems, responseItems.size(), total);
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                subtotal
        );
    }
}
