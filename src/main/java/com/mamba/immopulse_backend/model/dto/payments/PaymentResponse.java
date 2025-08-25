package com.mamba.immopulse_backend.model.dto.payments;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
    Long id,
    BigDecimal amount,
    LocalDate paymentDate,
    String status,
    String transactionId,
    String currency,
    LocalDate dueDate,
    String paymentMethod 
) { }
