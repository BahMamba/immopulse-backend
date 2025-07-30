package com.mamba.immopulse_backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mamba.immopulse_backend.model.dto.properties.PropertyListResponse;
import com.mamba.immopulse_backend.model.dto.properties.PropertyRequest;
import com.mamba.immopulse_backend.model.dto.properties.PropertyResponse;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.mamba.immopulse_backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    // Récupère l'utilisateur actuellement authentifié
    private User getAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable!"));
    }

    // Mapper : Entity -> DTO de réponse complet
    private PropertyResponse mapResponse(Property property) {
        return new PropertyResponse(
            property.getId(),
            property.getTitle(),
            property.getDescription(),
            property.getAddress(),
            property.getPrice(),
            property.getType(),
            property.getStatus(),
            property.getCoverImageUrl(),
            property.getOwner().getFullname(),
            property.getOwner().getEmail()
        );
    }

    // Mapper : Entity -> DTO de liste (plus léger)
    private PropertyListResponse mapResponseList(Property property) {
        return new PropertyListResponse(
            property.getId(),
            property.getTitle(),
            property.getDescription(),
            property.getAddress(),
            property.getPrice(),
            property.getType(),
            property.getStatus(),
            property.getCoverImageUrl()
        );
    }

    // Créer une propriété
    public PropertyResponse createProperty(PropertyRequest request){
        User user = getAuthenticated();

        Property property = new Property();
        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setAddress(request.address());
        property.setPrice(request.price());
        property.setType(request.type());
        property.setStatus(request.status());
        property.setCoverImageUrl(request.coverImageUrl());
        property.setOwner(user);

        return mapResponse(propertyRepository.save(property));
    }

    // Mettre à jour une propriété (vérifie que l'utilisateur est le propriétaire)
    public PropertyResponse updateProperty(Long id, PropertyRequest request){
        User user = getAuthenticated();

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriété Introuvable"));

        if (!property.getOwner().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Modification interdite : vous n'êtes pas le propriétaire !");
        }

        property.setTitle(request.title());
        property.setDescription(request.description());
        property.setAddress(request.address());
        property.setPrice(request.price());
        property.setStatus(request.status());
        property.setType(request.type());
        property.setCoverImageUrl(request.coverImageUrl());

        return mapResponse(propertyRepository.save(property));
    }

    // Supprimer une propriété
    public void deleteProperty(Long id){
        User user = getAuthenticated();

        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriété introuvable!"));

        if (!property.getOwner().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Suppression interdite : vous n'êtes pas le propriétaire !");
        }

        propertyRepository.delete(property);
    }

    // Lister toutes les propriétés (avec pagination + filtre simple)
    public Page<PropertyListResponse> getAllProperties(String title, String address, Pageable pageable) {
        Page<Property> page;

        if (title != null && !title.isBlank()) {
            page = propertyRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (address != null && !address.isBlank()) {
            page = propertyRepository.findByAddressContainingIgnoreCase(address, pageable);
        } else {
            page = propertyRepository.findAll(pageable);
        }

        return page.map(this::mapResponseList);
    }

    // Lister les propriétés du propriétaire connecté (avec pagination)
    public Page<PropertyListResponse> getPropertiesByOwner(Pageable pageable){
        User user = getAuthenticated();
        return propertyRepository.findByOwner(user, pageable)
                .map(this::mapResponseList);
    }

    // Détail d'une propriété
    public PropertyListResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propriété Introuvable!"));
        return mapResponseList(property);
    }
}
