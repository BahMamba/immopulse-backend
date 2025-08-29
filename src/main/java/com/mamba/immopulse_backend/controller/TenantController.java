package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.auth.UserRequest;
import com.mamba.immopulse_backend.model.dto.auth.UserResponse;
import com.mamba.immopulse_backend.model.dto.tenants.TenantResponse;
import com.mamba.immopulse_backend.service.TenantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tenants", description = "Gestion des locataires")
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
public class TenantController {
    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(
        @Valid @RequestBody UserRequest request,
        @RequestParam String password
    ) {
        return ResponseEntity.ok(tenantService.createTenant(request, password));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<UserResponse> updateTenantProfile(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(tenantService.updateTenantProfile(null, request));
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateTenantById(@PathVariable Long tenantId, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(tenantService.updateTenantProfile(tenantId, request));
    }

    @DeleteMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<String> deleteTenantProfile() {
        tenantService.deleteTenantProfile(null);
        return ResponseEntity.ok("Profil supprimé avec succès");
    }

    @DeleteMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTenantById(@PathVariable Long tenantId) {
        tenantService.deleteTenantProfile(tenantId);
        return ResponseEntity.ok("Locataire supprimé avec succès");
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<TenantResponse> getTenantProfile() {
        return ResponseEntity.ok(tenantService.getTenantProfile());
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable Long tenantId) {
        return ResponseEntity.ok(tenantService.getTenantById(tenantId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TenantResponse>> getAllTenants(
        @RequestParam(required = false) String fullname,
        Pageable pageable
    ) {
        return ResponseEntity.ok(tenantService.getAllTenants(fullname, pageable));
    }
}