// src/main/java/com/mamba/immopulse_backend/model/dto/tenants/TenantResponse.java
package com.mamba.immopulse_backend.model.dto.tenants;

public record TenantResponse(
    Long id,
    Long userId,
    String userEmail,
    String userFullname,
    String userPhoneNumber
) {}