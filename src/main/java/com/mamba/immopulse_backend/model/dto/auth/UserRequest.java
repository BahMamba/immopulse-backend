// src/main/java/com/mamba/immopulse_backend/model/dto/auth/UserRequest.java
package com.mamba.immopulse_backend.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank(message = "Nom complet requis")
    @Size(min = 3, message = "Nom complet doit avoir au moins 3 caractères")
    String fullname,

    @NotBlank(message = "Email requis")
    @Email(message = "Email invalide")
    String email,

    @NotBlank(message = "Numéro de téléphone requis")
    @Pattern(regexp = "^\\+224[0-9]{9}$", message = "Numéro doit être au format +224 suivi de 9 chiffres")
    String phoneNumber
) {}