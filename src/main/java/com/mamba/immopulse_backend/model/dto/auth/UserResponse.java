package com.mamba.immopulse_backend.model.dto.auth;

public record UserResponse(
    Long id,
    String fullname,
    String email,
    String phoneNumber
) {}
