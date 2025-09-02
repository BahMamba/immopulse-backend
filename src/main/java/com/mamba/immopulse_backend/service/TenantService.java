// src/main/java/com/mamba/immopulse_backend/service/TenantService.java
package com.mamba.immopulse_backend.service;

import com.mamba.immopulse_backend.model.dto.auth.UserRequest;
import com.mamba.immopulse_backend.model.dto.auth.UserResponse;
import com.mamba.immopulse_backend.model.dto.tenants.TenantResponse;
import com.mamba.immopulse_backend.model.entity.Tenant;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.model.enums.bail.BailStatus;
import com.mamba.immopulse_backend.model.enums.tenant.TenantStatus;
import com.mamba.immopulse_backend.model.enums.users.RoleUser;
import com.mamba.immopulse_backend.repository.BailRepository;
import com.mamba.immopulse_backend.repository.TenantRepository;
import com.mamba.immopulse_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final BailRepository bailRepository;
    private final PasswordEncoder passwordEncoder;

    private User getUserAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
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
            tenant.getUser().getPhoneNumber()
        );
    }

    @Transactional
    public TenantResponse createTenant(UserRequest request, String password) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        User user = new User();
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(password));
        user.setRoleUser(RoleUser.TENANT);
        user = userRepository.save(user);

        Tenant tenant = new Tenant();
        tenant.setUser(user);
        tenant.setStatus(TenantStatus.INACTIF);
        Tenant savedTenant = tenantRepository.save(tenant);
        return mapTenantResponse(savedTenant);
    }

    @Transactional
    public UserResponse updateTenantProfile(Long tenantId, UserRequest request) {
        User user;
        if (tenantId != null) {
            Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Locataire introuvable avec ID : " + tenantId));
            user = tenant.getUser();
        } else {
            user = getUserAuthenticated();
        }

        if (!request.email().equals(user.getEmail()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        User updatedUser = userRepository.save(user);
        return mapUserResponse(updatedUser);
    }

    @Transactional
    public void deleteTenantProfile(Long tenantId) {
        User user;
        Tenant tenant;
        if (tenantId != null) {
            tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Locataire introuvable avec ID : " + tenantId));
            user = tenant.getUser();
        } else {
            user = getUserAuthenticated();
            tenant = tenantRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Locataire introuvable"));
        }
        if (bailRepository.findByTenantIdAndStatus(tenant.getId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("Impossible de supprimer : le locataire a un bail actif");
        }
        tenantRepository.delete(tenant);
        userRepository.delete(user);
    }

    public TenantResponse getTenantProfile() {
        User authUser = getUserAuthenticated();
        Tenant tenant = tenantRepository.findByUserId(authUser.getId())
            .orElseThrow(() -> new RuntimeException("Locataire introuvable"));
        return mapTenantResponse(tenant);
    }

    public TenantResponse getTenantById(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new RuntimeException("Locataire introuvable avec ID : " + tenantId));
        return mapTenantResponse(tenant);
    }

    public Page<TenantResponse> getAllTenants(String fullname, Pageable pageable) {
        Page<Tenant> tenants = fullname == null || fullname.isBlank()
            ? tenantRepository.findAll(pageable)
            : tenantRepository.findByUserFullnameContainingIgnoreCase(fullname, pageable);
        return tenants.map(this::mapTenantResponse);
    }
}