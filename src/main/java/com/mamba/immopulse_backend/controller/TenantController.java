package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.auth.UserRequest;
import com.mamba.immopulse_backend.model.dto.auth.UserResponse;
import com.mamba.immopulse_backend.model.dto.tenants.TenantResponse;
import com.mamba.immopulse_backend.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tenants", description = "Gestion des profils locataires")
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // Créer un profil locataire
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@RequestBody UserRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Mettre à jour le profil locataire
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<UserResponse> updateTenantProfile(@RequestBody UserRequest request) {
        UserResponse response = tenantService.updateTenantProfile(request);
        return ResponseEntity.ok(response);
    }

    // Supprimer le profil locataire
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<String> deleteTenantProfile() {
        tenantService.deleteTenantProfile();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Profil locataire supprimé.");
    }

    // Consulter le profil locataire
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TenantResponse> getTenantProfile() {
        TenantResponse response = tenantService.getTenantProfile();
        return ResponseEntity.ok(response);
    }
}