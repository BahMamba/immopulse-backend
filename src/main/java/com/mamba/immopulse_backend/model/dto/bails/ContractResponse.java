package com.mamba.immopulse_backend.model.dto.bails;

public record ContractResponse(
    Long id,
    Long propertyId,
    Long bailId,
    String usageConditions,
    String contractUrl,
    String status 
) {}