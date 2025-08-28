package com.mamba.immopulse_backend.model.dto.tenants;

import java.math.BigDecimal;

public record TenantResponse(
    Long id,
    Long userId,
    String userEmail,
    String userFullname,
    String userPhoneNumber,
    Long propertyId,
    String propertyTitle,
    String startDate,
    String endDate,
    BigDecimal depositAmount,
    String contractUrl
) {}
