package com.mamba.immopulse_backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mamba.immopulse_backend.model.dto.properties.PropertyListResponse;
import com.mamba.immopulse_backend.model.dto.properties.PropertyRequest;
import com.mamba.immopulse_backend.model.dto.properties.PropertyResponse;
import com.mamba.immopulse_backend.service.PropertyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Properties", description = "Gestion des biens immobiliers")
@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // Créer une propriété
    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(@RequestBody PropertyRequest request) {
        PropertyResponse response = propertyService.createProperty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Modifier une propriété existante
    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> updateProperty(@PathVariable Long id, @RequestBody PropertyRequest request) {
        PropertyResponse response = propertyService.updateProperty(id, request);
        return ResponseEntity.ok(response);
    }

    // Lister toutes les propriétés avec pagination et filtres
    @GetMapping
    public ResponseEntity<Page<PropertyListResponse>> getAllProperties(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String address,
        Pageable pageable
    ) {
        return ResponseEntity.ok(propertyService.getAllProperties(title, address, pageable));
    }

    // Lister les propriétés du propriétaire connecté (avec pagination)
    @GetMapping("/owner")
    public ResponseEntity<Page<PropertyListResponse>> getPropertiesByOwner(Pageable pageable) {
        return ResponseEntity.ok(propertyService.getPropertiesByOwner(pageable));
    }

    // Détail d'une propriété
    @GetMapping("/{id}")
    public ResponseEntity<PropertyListResponse> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    // Supprimer une propriété
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok("Suppression effectuée.");
    }
}
