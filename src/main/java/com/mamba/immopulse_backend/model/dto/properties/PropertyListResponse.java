package com.mamba.immopulse_backend.model.dto.properties;

import java.math.BigDecimal;

import com.mamba.immopulse_backend.model.enums.property.PropertyStatus;
import com.mamba.immopulse_backend.model.enums.property.PropertyType;

public record PropertyListResponse(
    Long id,
    String title,
    String description,
    String address,
    BigDecimal price,
    PropertyType type,
    PropertyStatus status,
    String coverImageUrl
) {}
