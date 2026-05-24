package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CheckoutRequest;
import com.ecommerce.backend.dto.OrderItemResponse;
import com.ecommerce.backend.dto.OrderResponse;
import com.ecommerce.backend.dto.OrderStatusUpdateRequest;
import com.ecommerce.backend.dto.UserAddressRequest;
import com.ecommerce.backend.dto.UserStoreRequest;
import com.ecommerce.backend.model.*;
import com.ecommerce.backend.repository.CartItemRepository;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 訂單 Service
 *
 * 最重要的業務邏輯：checkout（結帳）
 *
 * 結帳流程（在同一個 @Transactional 內完成）：
 * 1. 取得使用者購物車（CartItem 列表）
 * 2. 驗證每個商品都還有足夠庫存
 * 3. 建立 Order 和 OrderItem（使用快照：存當下的名稱和價格）
 * 4. 扣減每個商品的庫存
 * 5. 清空購物車
 *
 * 為什麼全部在同一個 @Transactional？
 * 這樣若任何一步失敗（如庫存扣減到負數），
 * 所有操作都會 rollback，確保資料一致性。
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final UserAddressService userAddressService;
    private final UserStoreService userStoreService;

    public OrderService(OrderRepository orderRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        CartService cartService,
                        UserAddressService userAddressService,
                        UserStoreService userStoreService) {
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.userAddressService = userAddressService;
        this.userStoreService = userStoreService;
    }

    // ===========================
    // 結帳（購物車 → 訂單）
    // ===========================
    @Transactional
    public OrderResponse checkout(String username, CheckoutRequest request) {
        User user = cartService.findUser(username);

        // 驗證條件式必填欄位
        if (request.deliveryMethod() == DeliveryMethod.HOME_DELIVERY) {
            if (request.recipientAddress() == null || request.recipientAddress().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "宅配需填寫收貨地址");
            }
        } else {
            if (request.storeName() == null || request.storeName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "超商取貨需填寫門市名稱");
            }
            if (request.storeCode() == null || request.storeCode().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "超商取貨需填寫門市店號");
            }
        }

        // 1. 取得購物車
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "購物車是空的");
        }

        // 2. 驗證庫存
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (!product.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "商品「" + product.getName() + "」已下架，請從購物車移除");
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "商品「" + product.getName() + "」庫存不足（剩餘：" + product.getStock() + "）");
            }
        }

        // 3. 計算訂單總金額
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. 建立訂單
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .ordererName(request.ordererName())
                .ordererPhone(request.ordererPhone())
                .ordererAddress(request.ordererAddress())
                .recipientName(request.recipientName())
                .recipientPhone(request.recipientPhone())
                .deliveryMethod(request.deliveryMethod())
                .recipientAddress(request.recipientAddress())
                .storeName(request.storeName())
                .storeCode(request.storeCode())
                .paymentMethod(request.paymentMethod())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // 5. 建立訂單明細（快照商品資訊）
        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .productName(cartItem.getProduct().getName())
                        .price(cartItem.getProduct().getPrice())
                        .quantity(cartItem.getQuantity())
                        .build())
                .toList();

        order.getItems().addAll(orderItems);
        orderRepository.save(order);

        // 6. 扣減庫存
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 7. 清空購物車
        cartItemRepository.deleteByUserId(user.getId());

        // 8. 若勾選「儲存至常用地址」，自動存入
        if (request.saveAddress() && request.deliveryMethod() == DeliveryMethod.HOME_DELIVERY) {
            userAddressService.addAddress(username, new UserAddressRequest(
                    "常用地址", request.recipientName(), request.recipientPhone(), request.recipientAddress()
            ));
        }

        // 9. 若勾選「儲存至常用門市」，自動存入
        if (request.saveStore() && request.deliveryMethod() != DeliveryMethod.HOME_DELIVERY) {
            userStoreService.addStore(username, new UserStoreRequest(
                    request.deliveryMethod(), request.storeName(), request.storeCode()
            ));
        }

        return toResponse(order);
    }

    // ===========================
    // 顧客：查詢我的訂單列表
    // ===========================
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(String username, Pageable pageable) {
        User user = cartService.findUser(username);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    // ===========================
    // 顧客：查詢訂單詳情
    // ===========================
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(String username, Long orderId) {
        User user = cartService.findUser(username);
        Order order = findOrderOwnedByUser(orderId, user.getId());
        return toResponse(order);
    }

    // ===========================
    // 顧客：取消訂單（只能取消 PENDING）
    // ===========================
    @Transactional
    public OrderResponse cancelMyOrder(String username, Long orderId) {
        User user = cartService.findUser(username);
        Order order = findOrderOwnedByUser(orderId, user.getId());

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "只有「待確認」狀態的訂單才能取消");
        }

        // 取消後退回庫存
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    // ===========================
    // 管理員：查詢所有訂單
    // ===========================
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAdminOrders(String statusFilter, Pageable pageable) {
        if (statusFilter != null && !statusFilter.isBlank()) {
            try {
                OrderStatus status = OrderStatus.valueOf(statusFilter.toUpperCase());
                return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
                        .map(this::toResponse);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的訂單狀態：" + statusFilter);
            }
        }
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    // ===========================
    // 管理員：查詢訂單詳情
    // ===========================
    @Transactional(readOnly = true)
    public OrderResponse getAdminOrderById(Long orderId) {
        return toResponse(findOrder(orderId));
    }

    // ===========================
    // 管理員：更新訂單狀態
    // ===========================
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = findOrder(orderId);

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "無效的訂單狀態：" + request.status());
        }

        // 狀態轉換規則（防止非法轉換）
        validateStatusTransition(order.getStatus(), newStatus);

        // 若從非取消狀態改為取消，退回庫存
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    // ===========================
    // 私有輔助方法
    // ===========================

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂單不存在"));
    }

    private Order findOrderOwnedByUser(Long orderId, Long userId) {
        Order order = findOrder(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權存取此訂單");
        }
        return order;
    }

    /** 合法的狀態轉換表 */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // 已取消或已完成的訂單不能再改
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "「" + statusLabel(current) + "」狀態的訂單無法再修改");
        }
        // 不能回退狀態（除了取消）
        Map<OrderStatus, Integer> order = Map.of(
                OrderStatus.PENDING, 0,
                OrderStatus.CONFIRMED, 1,
                OrderStatus.SHIPPED, 2,
                OrderStatus.DELIVERED, 3
        );
        if (next != OrderStatus.CANCELLED && order.containsKey(next) && order.containsKey(current)) {
            if (order.get(next) < order.get(current)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "訂單狀態不能回退");
            }
        }
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProduct() != null ? item.getProduct().getId() : null,
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        String deliveryLabel = order.getDeliveryMethod() != null
                ? deliveryMethodLabel(order.getDeliveryMethod()) : "";

        return new OrderResponse(
                order.getId(),
                order.getUser().getUsername(),
                order.getTotalAmount(),
                order.getStatus().name(),
                statusLabel(order.getStatus()),
                order.getOrdererName(),
                order.getOrdererPhone(),
                order.getOrdererAddress(),
                order.getRecipientName(),
                order.getRecipientPhone(),
                order.getDeliveryMethod() != null ? order.getDeliveryMethod().name() : null,
                deliveryLabel,
                order.getRecipientAddress(),
                order.getStoreName(),
                order.getStoreCode(),
                order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null,
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private String statusLabel(OrderStatus status) {
        return switch (status) {
            case PENDING -> "待確認";
            case CONFIRMED -> "已確認";
            case SHIPPED -> "已出貨";
            case DELIVERED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }

    private String deliveryMethodLabel(DeliveryMethod method) {
        return switch (method) {
            case HOME_DELIVERY -> "宅配";
            case FAMILY_MART -> "全家超商取貨";
            case SEVEN_ELEVEN -> "7-11 超商取貨";
        };
    }
}
