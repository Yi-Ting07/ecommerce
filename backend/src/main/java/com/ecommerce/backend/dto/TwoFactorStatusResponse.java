package com.ecommerce.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TwoFactorStatusResponse(
        boolean enabled,
        String method   // "TOTP" 或 "EMAIL"，未啟用時為 null
) {}
