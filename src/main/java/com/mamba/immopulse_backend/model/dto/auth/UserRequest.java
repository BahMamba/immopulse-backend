package com.mamba.immopulse_backend.model.dto.auth;

public record UserRequest(
    String fullname,
    String email,
    String phoneNumber,
    String password
) { }
