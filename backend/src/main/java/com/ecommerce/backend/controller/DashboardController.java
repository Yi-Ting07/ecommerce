package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.DashboardStatsResponse;
import com.ecommerce.backend.model.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 後台儀表板 Controller
 *
 * GET /api/admin/dashboard/stats
 *
 * 回傳目前系統的各項統計數據，供前台儀表板頁面顯示。
 * 因為放在 /api/admin/** 下，SecurityConfig 已確保只有 ROLE_ADMIN 能存取。
 *
 * 設計考量：
 * - 這裡直接注入 Repository 而非 Service，
 *   是因為這個 Controller 的邏輯很簡單（聚合查詢），
 *   不需要額外建立 DashboardService 增加複雜度。
 * - 若未來報表功能變複雜，再抽出 DashboardService。
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public DashboardController(ProductRepository productRepository,
                               OrderRepository orderRepository,
                               UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * 取得儀表板統計數據
     *
     * 回應範例：
     * {
     *   "totalProducts":  50,
     *   "activeProducts": 42,
     *   "totalOrders":    128,
     *   "totalUsers":     35,
     *   "totalRevenue":   256800.00,
     *   "ordersByStatus": {
     *     "PENDING":   5,
     *     "CONFIRMED": 10,
     *     "SHIPPED":   8,
     *     "DELIVERED": 100,
     *     "CANCELLED": 5
     *   }
     * }
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        // 商品統計
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.count(
                (Specification<com.ecommerce.backend.model.Product>)
                        (root, query, cb) -> cb.isTrue(root.get("active"))
        );

        // 訂單統計
        long totalOrders = orderRepository.count();

        // 會員統計
        long totalUsers = userRepository.count();

        // 總營收（排除已取消訂單）
        BigDecimal totalRevenue = orderRepository.getTotalRevenue();

        // 各狀態訂單數量
        List<Object[]> statusCounts = orderRepository.countGroupByStatus();
        Map<String, Long> ordersByStatus = new HashMap<>();

        // 初始化所有狀態為 0（確保前端不會因缺少 key 而出錯）
        for (OrderStatus status : OrderStatus.values()) {
            ordersByStatus.put(status.name(), 0L);
        }

        // 填入實際數量
        for (Object[] row : statusCounts) {
            OrderStatus status = (OrderStatus) row[0];
            Long count = (Long) row[1];
            ordersByStatus.put(status.name(), count);
        }

        return ResponseEntity.ok(new DashboardStatsResponse(
                totalProducts,
                activeProducts,
                totalOrders,
                totalUsers,
                totalRevenue,
                ordersByStatus
        ));
    }
}
