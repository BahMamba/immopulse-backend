package com.mamba.immopulse_backend.model.dto.tenants;

import java.math.BigDecimal;

public record TenantResponse(
    Long id,
    Long userId,
    String userEmail,
    Long propertyId,
    String propertyTitle,
    String startDate,
    String endDate,
    BigDecimal depositAmount,
    String phoneNumber,
    String status,
    String contractUrl
) {}
