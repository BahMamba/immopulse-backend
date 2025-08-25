package com.mamba.immopulse_backend.model.dto.payments;

import java.math.BigDecimal;

public record PaymentRequest(
    BigDecimal amount,
    String transactionId,
    String currency,
    String paymentMethod
) {}
