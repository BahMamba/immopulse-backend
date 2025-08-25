package com.mamba.immopulse_backend.model.dto.maintenances;

import java.time.LocalDateTime;

public record MaintenanceResponse(
    Long id,
    String description,
    String urgency,
    String status,
    String imageUrl,
    String category,
    LocalDateTime createdAt,
    LocalDateTime resolvedAt
) { }
