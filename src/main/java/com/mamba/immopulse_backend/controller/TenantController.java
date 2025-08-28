package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.auth.UserRequest;
import com.mamba.immopulse_backend.model.dto.auth.UserResponse;
import com.mamba.immopulse_backend.model.dto.tenants.TenantResponse;
import com.mamba.immopulse_backend.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateTenantProfile(@PathVariable Long tenantId, @RequestBody UserRequest request) {
        UserResponse response = tenantService.updateTenantProfile(tenantId, request);
        return ResponseEntity.ok(response);
    }

    // Mettre à jour son propre profil (JWT)
    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<UserResponse> updateOwnProfile(@RequestBody UserRequest request) {
        UserResponse response = tenantService.updateTenantProfile(null, request);
        return ResponseEntity.ok(response);
    }

    // Supprimer le profil locataire
    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTenantProfile(@PathVariable Long tenantId) {
        tenantService.deleteTenantProfile(tenantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Profil locataire supprimé.");
    }

    // Supprimer son propre profil (JWT)
    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<String> deleteOwnProfile() {
        tenantService.deleteTenantProfile(null);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Profil locataire supprimé.");
    }

    // Consulter son propre profil (JWT)
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TenantResponse> getTenantProfile() {
        TenantResponse response = tenantService.getTenantProfile();
        return ResponseEntity.ok(response);
    }

    // Consulter un locataire par ID (admin)
    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long tenantId) {
        TenantResponse response = tenantService.getTenantById(tenantId);
        return ResponseEntity.ok(response);
    }

    // Lister tous les locataires (admin, pagination)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
        @RequestParam(required = false) String fullname,
        Pageable pageable
    ) {
        return ResponseEntity.ok(tenantService.getAllTenants(fullname, pageable));
    }
}