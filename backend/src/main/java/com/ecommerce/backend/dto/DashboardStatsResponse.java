package com.ecommerce.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 後台儀表板統計回應 DTO
 *
 * 包含：
 * - totalProducts  : 商品總數
 * - activeProducts : 上架中商品數
 * - totalOrders    : 訂單總數
 * - totalUsers     : 會員總數
 * - totalRevenue   : 總營收（不含已取消訂單）
 * - ordersByStatus : 各訂單狀態的數量，例如 { "PENDING": 3, "DELIVERED": 10 }
 */
public record DashboardStatsResponse(
        long totalProducts,
        long activeProducts,
        long totalOrders,
        long totalUsers,
        BigDecimal totalRevenue,
        Map<String, Long> ordersByStatus
) {
}
