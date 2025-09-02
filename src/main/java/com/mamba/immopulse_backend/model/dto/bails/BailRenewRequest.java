package com.mamba.immopulse_backend.model.dto.bails;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record BailRenewRequest(
    @NotBlank String newEndDate,
    @PositiveOrZero BigDecimal newRentAmount
) {}
