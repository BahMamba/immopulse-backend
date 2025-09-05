package com.mamba.immopulse_backend.model.dto.bails;

import com.mamba.immopulse_backend.model.enums.bail.BailStatus;
import java.math.BigDecimal;

public record BailResponse(
        Long id,
        Long tenantId,
        String tenantName,
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