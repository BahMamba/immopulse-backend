package com.mamba.immopulse_backend.model.dto.maintenances;

public record MaintenanceRequest(
    String description,
    String urgency,
    String category
) {}
