package com.mamba.immopulse_backend.model.dto.bails;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record BailRequest(
    @NotNull Long tenantId,
    @NotNull Long propertyId,
    @NotBlank String startDate,
    @NotBlank String endDate,
    @NotNull @PositiveOrZero BigDecimal depositAmount,
    @PositiveOrZero BigDecimal rentAmount,
    @PositiveOrZero BigDecimal agencyFee
) {}
