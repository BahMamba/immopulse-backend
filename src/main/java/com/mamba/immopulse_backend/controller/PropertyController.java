package com.mamba.immopulse_backend.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.mamba.immopulse_backend.model.dto.properties.PropertyDetailResponse;
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

    // Créer une propriété avec DTO et images en une seule requête
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PropertyResponse> createProperty(
        @RequestPart("property") PropertyRequest request,
        @RequestPart(name = "coverImage", required = false) MultipartFile coverImage,
        @RequestPart(name = "images", required = false) List<MultipartFile> images
    ) {
        PropertyResponse response = propertyService.createProperty(request, coverImage, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Mettre à jour une propriété avec DTO et images en une seule requête
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<PropertyResponse> updateProperty(
        @PathVariable Long id,
        @RequestPart("property") PropertyRequest request,
        @RequestPart(name = "coverImage", required = false) MultipartFile coverImage,
        @RequestPart(name = "images", required = false) List<MultipartFile> images
    ) {
        PropertyResponse response = propertyService.updateProperty(id, request, coverImage, images);
        return ResponseEntity.ok(response);
    }

    // Lister toutes les propriétés avec pagination et filtres
    @GetMapping
    public ResponseEntity<Page<PropertyListResponse>> getAllProperties(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String status,
        @RequestParam(required = false, defaultValue = "id") String sortBy,
        Pageable pageable
    ) {
        return ResponseEntity.ok(propertyService.getAllProperties(title, status, sortBy, pageable));
    }

    // Lister les propriétés du propriétaire connecté
    @GetMapping("/owner")
    public ResponseEntity<Page<PropertyListResponse>> getPropertiesByOwner(Pageable pageable) {
        return ResponseEntity.ok(propertyService.getPropertiesByOwner(pageable));
    }

    // Obtenir les détails d'une propriété
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDetailResponse> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyDetail(id));
    }

    // Supprimer une propriété
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok("Suppression effectuée.");
    }
}