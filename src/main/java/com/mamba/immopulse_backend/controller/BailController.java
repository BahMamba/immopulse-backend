package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.bails.BailRenewRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailResponse;
import com.mamba.immopulse_backend.service.BailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bails")
@RequiredArgsConstructor
public class BailController {
    private final BailService bailService;

    // Crée un nouveau bail actif
    @PostMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<BailResponse> createBail(@Valid @RequestBody BailRequest request) {
        BailResponse response = bailService.createBail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Annule un bail actif
    @PostMapping("/{bailId}/cancel")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BailResponse> cancelBail(@PathVariable Long bailId) {
        BailResponse response = bailService.cancelBail(bailId);
        return ResponseEntity.ok(response);
    }

    // Suspend un bail actif
    @PostMapping("/{bailId}/suspend")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BailResponse> suspendBail(@PathVariable Long bailId) {
        BailResponse response = bailService.suspendBail(bailId);
        return ResponseEntity.ok(response);
    }

    // Reprend un bail suspendu
    @PostMapping("/{bailId}/resume")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BailResponse> resumeBail(@PathVariable Long bailId) {
        BailResponse response = bailService.resumeBail(bailId);
        return ResponseEntity.ok(response);
    }

    // Renouvelle un bail actif
    @PostMapping("/{bailId}/renew")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BailResponse> renewBail(
            @PathVariable Long bailId,
            @Valid @RequestBody BailRenewRequest request) {
        BailResponse response = bailService.renewBail(bailId, request);
        return ResponseEntity.ok(response);
    }

    // Récupère le bail actif d’un locataire
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<BailResponse> getBailByTenantId(@PathVariable Long tenantId) {
        return ResponseEntity.ok(bailService.getBailByTenant(tenantId));
    }

    // Liste l’historique des baux d’un locataire avec pagination
    @GetMapping("/tenant/{tenantId}/history")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BailResponse>> getBailHistoryByTenant(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bailService.getBailByTenant(tenantId, page, size));
    }

    // Liste l’historique des baux d’une propriété avec pagination
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Page<BailResponse>> getBailHistory(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bailService.getBailsByProperty(propertyId, page, size));
    }
}