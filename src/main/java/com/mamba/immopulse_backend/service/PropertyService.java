package com.mamba.immopulse_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mamba.immopulse_backend.model.dto.properties.PropertyDetailResponse;
import com.mamba.immopulse_backend.model.dto.properties.PropertyListResponse;
import com.mamba.immopulse_backend.model.dto.properties.PropertyRequest;
import com.mamba.immopulse_backend.model.dto.properties.PropertyResponse;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.PropertyImage;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.model.enums.PropertyStatus;
import com.mamba.immopulse_backend.repository.PropertyImageRepository;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.mamba.immopulse_backend.repository.UserRepository;
import com.mamba.immopulse_backend.service.utils.StorageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;


    // Ajouter ou mettre à jour une image de couverture
    public String addCoverImage(Long propertyId, MultipartFile file) {
        User user = getAuthenticated();
        Property property = propertyRepository.findById(propertyId)
            .orElseThrow(() -> new RuntimeException("Propriété non trouvée"));

        if (!property.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres propriétés");
        }

        // Supprime ancienne image si elle existe
        if (property.getCoverImageUrl() != null) {
            storageService.delete(property.getCoverImageUrl());
        }

        String imageUrl = storageService.save(file);
        property.setCoverImageUrl(imageUrl);
        propertyRepository.save(property);

        return imageUrl;
    }


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
    public Page<PropertyListResponse> getAllProperties(String title, String status, String sortBy, Pageable pageable) {
    Pageable sortedPageable = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize(),
        Sort.by(Sort.Direction.ASC, sortBy != null ? sortBy : "id")
    );

    Page<Property> page;
    if (title != null && !title.isBlank()) {
        page = propertyRepository.findByTitleContainingIgnoreCase(title, sortedPageable);
    } else if (status != null && !status.isBlank()) {
        try {
            PropertyStatus statusEnum = PropertyStatus.valueOf(status.toUpperCase());
            page = propertyRepository.findByStatus(statusEnum, sortedPageable);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + status);
        }
    } else {
        page = propertyRepository.findAll(sortedPageable);
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
    public PropertyDetailResponse getPropertyDetail(Long id){
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propriété introuvable!"));
        
        List<String> images = propertyImageRepository.findByProperty(property)
            .stream()
            .map(PropertyImage::getImageUrl)
            .toList();
        
        return new PropertyDetailResponse(
            property.getId(),
            property.getTitle(),
            property.getDescription(),
            property.getAddress(),
            property.getPrice().doubleValue(),
            property.getType().name(),
            property.getStatus().name(),
            property.getCoverImageUrl(),
            property.getOwner().getFullname(),
            property.getOwner().getEmail(),
            images
        );
    }

    // Methode pour ajouter des images sur une property
    public List<String> addImages(Long id, List<MultipartFile> files){
        User user = getAuthenticated();
        Property property = propertyRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Propriete introuvable"));
        
        if (!property.getOwner().getEmail().equals(user.getEmail())) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres propriétés");
        }

        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String imageUrl = storageService.save(file);

            PropertyImage image = PropertyImage.builder()
                .imageUrl(imageUrl)
                .property(property)
                .build();
            
            propertyImageRepository.save(image);
            imageUrls.add(imageUrl);
            
        }
        return imageUrls;
    }
}
