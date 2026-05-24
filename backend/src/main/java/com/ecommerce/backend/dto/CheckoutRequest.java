package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.DeliveryMethod;
import com.ecommerce.backend.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 結帳請求 DTO
 */
public record CheckoutRequest(

        // ===== 訂購人資訊 =====
        @NotBlank(message = "訂購人姓名不能為空")
        @Size(max = 50, message = "訂購人姓名不超過 50 字")
        String ordererName,

        @NotBlank(message = "訂購人電話不能為空")
        @Size(max = 30, message = "訂購人電話不超過 30 字")
        String ordererPhone,

        @NotBlank(message = "訂購人地址不能為空")
        @Size(max = 300, message = "訂購人地址不超過 300 字")
        String ordererAddress,

        // ===== 收貨人資訊 =====
        @NotBlank(message = "收貨人姓名不能為空")
        @Size(max = 50, message = "收貨人姓名不超過 50 字")
        String recipientName,

        @NotBlank(message = "收貨人電話不能為空")
        @Size(max = 30, message = "收貨人電話不超過 30 字")
        String recipientPhone,

        // ===== 送貨方式 =====
        @NotNull(message = "請選擇送貨方式")
        DeliveryMethod deliveryMethod,

        // 宅配地址（deliveryMethod = HOME_DELIVERY 時必填）
        @Size(max = 300, message = "收貨地址不超過 300 字")
        String recipientAddress,

        // 超商門市名稱（deliveryMethod = FAMILY_MART / SEVEN_ELEVEN 時必填）
        @Size(max = 100, message = "門市名稱不超過 100 字")
        String storeName,

        // 超商門市店號（deliveryMethod = FAMILY_MART / SEVEN_ELEVEN 時必填）
        @Size(max = 20, message = "門市店號不超過 20 字")
        String storeCode,

        // ===== 付款方式 =====
        @NotNull(message = "請選擇付款方式")
        PaymentMethod paymentMethod,

        // ===== 是否儲存至常用清單 =====
        boolean saveAddress,  // 是否將收貨地址存入常用地址
        boolean saveStore     // 是否將門市存入常用門市
) {}

