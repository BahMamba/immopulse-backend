package com.mamba.immopulse_backend.model.dto.auth;

public record LoginResponse(String token, String email, String role) {}

