package com.mamba.immopulse_backend.model.dto.bails;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ContractRequest(
        @NotNull(message = "L’ID de la propriété est requis") Long propertyId,
        @NotEmpty(message = "Les conditions d’utilisation sont requises") String usageConditions
) {}