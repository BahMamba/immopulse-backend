package com.mamba.immopulse_backend.controller;

import com.mamba.immopulse_backend.model.dto.bails.ContractRequest;
import com.mamba.immopulse_backend.model.dto.bails.ContractResponse;
import com.mamba.immopulse_backend.service.ContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

import java.io.File;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @Value("${app.contracts.storage.path}")
    private String storagePath;

    // Crée un contrat type
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ContractResponse> createContract(@RequestBody ContractRequest request) {
        return ResponseEntity.ok(contractService.createContract(request));
    }

    // Récupère les contrats d’une propriété (paginés)
    @GetMapping("/property/{propertyId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Page<ContractResponse>> getContractByProperty(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(contractService.getContractByProperty(propertyId, page, size));
    }

    // Récupère un contrat spécifique par bail
    @GetMapping("/bail/{bailId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ContractResponse> getContractByBail(@PathVariable Long bailId) {
        return ResponseEntity.ok(contractService.getContractByBail(bailId));
    }

    // Télécharge le PDF d’un contrat
    @GetMapping("/files/{filename}")
    @PreAuthorize("hasRole('TENANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Resource> serveContractFile(@PathVariable String filename) {
        File file = new File(storagePath + filename);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}