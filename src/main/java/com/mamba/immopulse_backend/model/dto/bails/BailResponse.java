package com.mamba.immopulse_backend.model.dto.bails;

import java.math.BigDecimal;

import com.mamba.immopulse_backend.model.enums.bail.BailStatus;

public record BailResponse(
    Long id,
    Long tenantId,
    String tenantFullname,
    Long propertyId,
    String propertyTitle,
    String startDate,
    String endDate,
    BigDecimal depositAmount,
    BigDecimal rentAmount,
    BigDecimal agencyFee,
    String contractUrl,
    BailStatus status
) {}
