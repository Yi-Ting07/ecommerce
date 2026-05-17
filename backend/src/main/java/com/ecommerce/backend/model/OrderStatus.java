package com.ecommerce.backend.model;

/**
 * 訂單狀態列舉
 *
 * 狀態流程：
 *   PENDING（待確認）→ CONFIRMED（已確認）→ SHIPPED（已出貨）→ DELIVERED（已完成）
 *   任何狀態（尚未出貨）都可以 → CANCELLED（已取消）
 *
 * 只有 PENDING 狀態的訂單，顧客才能自行取消。
 * 管理員可以從 PENDING/CONFIRMED 推進到下一個狀態，或直接取消。
 */
public enum OrderStatus {
    PENDING,    // 待確認（剛下單）
    CONFIRMED,  // 已確認（管理員確認）
    SHIPPED,    // 已出貨
    DELIVERED,  // 已完成（顧客收到）
    CANCELLED   // 已取消
}
