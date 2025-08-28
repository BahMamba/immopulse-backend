// src/main/java/com/mamba/immopulse_backend/service/TenantService.java
package com.mamba.immopulse_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mamba.immopulse_backend.model.dto.auth.UserRequest;
import com.mamba.immopulse_backend.model.dto.auth.UserResponse;
import com.mamba.immopulse_backend.model.dto.tenants.TenantResponse;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.Tenant;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.model.enums.property.PropertyStatus;
import com.mamba.immopulse_backend.model.enums.users.RoleUser;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.mamba.immopulse_backend.repository.TenantRepository;
import com.mamba.immopulse_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantService {
    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;

    // =============== METHODES UTILITAIRES =============== //

    private User getUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> {
                logger.error("Utilisateur authentifié introuvable : {}", email);
                return new RuntimeException("Utilisateur introuvable");
            });
    }

    private UserResponse mapUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFullname(),
            user.getEmail(),
            user.getPhoneNumber()
        );
    }

    private TenantResponse mapTenantResponse(Tenant tenant) {
        return new TenantResponse(
            tenant.getId(),
            tenant.getUser().getId(),
            tenant.getUser().getEmail(),
            tenant.getUser().getFullname(),
            tenant.getUser().getPhoneNumber(),
            tenant.getProperty() != null ? tenant.getProperty().getId() : null,
            tenant.getProperty() != null ? tenant.getProperty().getTitle() : null,
            tenant.getStartDate(),
            tenant.getEndDate(),
            tenant.getDepositAmount(),
            tenant.getContractUrl()
        );
    }

    // =============== METHODES CRUDS =============== //

    @Transactional
    public TenantResponse createTenant(UserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("Tentative de création avec email existant : {}", request.email());
            throw new RuntimeException("Email déjà utilisé");
        }
        logger.info("Création tenant avec email : {}", request.email());
        User user = new User();
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoleUser(RoleUser.TENANT);
        user = userRepository.save(user);

        Tenant tenant = new Tenant();
        tenant.setUser(user);
        Tenant savedTenant = tenantRepository.save(tenant);
        logger.info("Tenant créé avec ID : {}", savedTenant.getId());
        return mapTenantResponse(savedTenant);
    }

    @Transactional
    public UserResponse updateTenantProfile(Long tenantId, UserRequest request) {
        User user;
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Locataire introuvable avec ID : {}", tenantId);
                    return new RuntimeException("Locataire introuvable avec ID : " + tenantId);
                });
            user = tenant.getUser();
        } else {
            user = getUserAuthenticated();
        }

        if (!request.email().equals(user.getEmail()) && userRepository.findByEmail(request.email()).isPresent()) {
            logger.warn("Tentative de mise à jour avec email existant : {}", request.email());
            throw new RuntimeException("Email déjà utilisé");
        }
        logger.info("Mise à jour profil tenant : {}", user.getEmail());
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        User updatedUser = userRepository.save(user);
        logger.info("Profil tenant mis à jour : {}", updatedUser.getEmail());
        return mapUserResponse(updatedUser);
    }

    @Transactional
    public void deleteTenantProfile(Long tenantId) {
        User user;
        Tenant tenant;
        if (tenantId != null) {
            tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> {
                    logger.error("Locataire introuvable avec ID : {}", tenantId);
                    return new RuntimeException("Locataire introuvable avec ID : " + tenantId);
                });
            user = tenant.getUser();
        } else {
            user = getUserAuthenticated();
            tenant = tenantRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    logger.error("Locataire introuvable pour utilisateur : {}", user.getEmail());
                    return new RuntimeException("Locataire introuvable");
                });
        }
        logger.info("Suppression profil tenant : {}", user.getEmail());
        if (tenant.getProperty() != null) {
            Property property = tenant.getProperty();
            property.setStatus(PropertyStatus.DISPONIBLE);
            propertyRepository.save(property);
            logger.info("Propriété {} remise à DISPONIBLE", property.getId());
        }
        tenantRepository.delete(tenant);
        userRepository.delete(user);
        logger.info("Profil tenant supprimé : {}", user.getEmail());
    }

    public TenantResponse getTenantProfile() {
        User authUser = getUserAuthenticated();
        Tenant tenant = tenantRepository.findByUserId(authUser.getId())
            .orElseThrow(() -> {
                logger.error("Locataire introuvable pour utilisateur : {}", authUser.getEmail());
                return new RuntimeException("Locataire introuvable");
            });
        logger.info("Consultation profil tenant : {}", authUser.getEmail());
        return mapTenantResponse(tenant);
    }

    public TenantResponse getTenantById(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> {
                logger.error("Locataire introuvable avec ID : {}", tenantId);
                return new RuntimeException("Locataire introuvable avec ID : " + tenantId);
            });
        logger.info("Consultation tenant ID : {}", tenantId);
        return mapTenantResponse(tenant);
    }

    public Page<TenantResponse> getAllTenants(String fullname, Pageable pageable) {
        Page<Tenant> tenants = fullname == null || fullname.isBlank()
            ? tenantRepository.findAll(pageable)
            : tenantRepository.findByUserFullnameContainingIgnoreCase(fullname, pageable);
        logger.info("Consultation liste tenants, page : {}, taille : {}", pageable.getPageNumber(), pageable.getPageSize());
        return tenants.map(this::mapTenantResponse);
    }
}