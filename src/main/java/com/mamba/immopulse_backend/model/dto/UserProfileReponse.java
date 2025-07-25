package com.mamba.immopulse_backend.model.dto;

import com.mamba.immopulse_backend.model.enums.RoleUser;

public record UserProfileReponse(String fullname, String email, RoleUser role) {}
