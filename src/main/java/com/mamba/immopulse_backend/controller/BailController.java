package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.bails.BailRenewRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailResponse;
import com.mamba.immopulse_backend.service.BailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bails")
@RequiredArgsConstructor
public class BailController {
    private final BailService bailService;

    // Crée un nouveau bail
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<BailResponse> createBail(@Valid @RequestBody BailRequest request) {
        BailResponse response = bailService.createBail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Annule un bail existant
    @DeleteMapping("/{bailId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<BailResponse> cancelBail(@PathVariable Long bailId) {
        BailResponse response = bailService.cancelBail(bailId);
        return ResponseEntity.ok(response); 
    }

    // Récupère le bail actif d’un locataire
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<BailResponse> getBailByTenantId(@PathVariable Long tenantId) {
        return ResponseEntity.ok(bailService.getBailByTenant(tenantId));
    }

    // Renouvellement d'un Bail ACTIF
    @PatchMapping("/{bailId}/renew")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<BailResponse> renewBail(
            @PathVariable Long bailId,
            @Valid @RequestBody BailRenewRequest request) {
        BailResponse response = bailService.renewBail(bailId, request);
        return ResponseEntity.ok(response);
    }
}