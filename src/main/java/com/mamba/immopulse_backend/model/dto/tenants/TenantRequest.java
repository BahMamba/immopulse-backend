package com.mamba.immopulse_backend.model.dto.tenants;

import java.math.BigDecimal;

public record TenantRequest(
    String startDate,
    String endDate,
    BigDecimal depositAmount,
    String phoneNumber,
    String status
) { }
