package com.ecommerce.backend.dto;

import com.ecommerce.backend.model.DeliveryMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserStoreRequest(
        @NotNull(message = "請選擇超商類型")
        DeliveryMethod storeType,  // FAMILY_MART 或 SEVEN_ELEVEN

        @NotBlank(message = "門市名稱不能為空")
        @Size(max = 100, message = "門市名稱不超過 100 字")
        String storeName,

        @NotBlank(message = "門市店號不能為空")
        @Size(max = 20, message = "門市店號不超過 20 字")
        String storeCode
) {}
