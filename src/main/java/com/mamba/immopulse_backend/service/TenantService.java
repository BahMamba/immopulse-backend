package com.mamba.immopulse_backend.service;

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
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;
    
    // ===============METHODES UTILITAIRES=============== //
    
    private User getUserAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utisateur introuvable"));      
    }

    private UserResponse mapUserResponse(User user){
        return new UserResponse(
            user.getId(),
            user.getFullname(),
            user.getEmail(),
            user.getPhoneNumber()
        );
    }

    private TenantResponse mapTenantResponse(Tenant tenant){
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

    // ==================METHODES CRUDS POUR LA GESTION D'UN TENANT comme AJOUT/MODIF/DELETE================== //

    @Transactional
    public TenantResponse createTenant(UserRequest request){
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email deja utilise !");
        }
        User user = new User();
        user.setFullname(request.fullname());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoleUser(RoleUser.TENANT);
        user = userRepository.save(user);

        Tenant tenant = new Tenant();
        tenant.setUser(user);

        return mapTenantResponse(tenantRepository.save(tenant));
    }


    @Transactional
    public UserResponse updateTenantProfile(UserRequest request) {
        User authUser = getUserAuthenticated();
        
        if (!request.email().equals(authUser.getEmail()) && userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }
        authUser.setFullname(request.fullname());
        authUser.setEmail(request.email());
        authUser.setPhoneNumber(request.phoneNumber());
        if (request.password() != null && !request.password().isBlank()) {
            authUser.setPassword(passwordEncoder.encode(request.password()));
        }
        return mapUserResponse(userRepository.save(authUser));
    }

    @Transactional
    public void deleteTenantProfile() {
        User authUser = getUserAuthenticated();
        Tenant tenant = tenantRepository.findByUserId(authUser.getId())
            .orElseThrow(() -> new RuntimeException("Locataire introuvable"));
        if (tenant.getProperty() != null) {
            Property property = tenant.getProperty();
            property.setStatus(PropertyStatus.DISPONIBLE);
            propertyRepository.save(property);
        }
        tenantRepository.delete(tenant);
        userRepository.delete(authUser);
    }

    public TenantResponse getTenantProfile() {
        User authUser = getUserAuthenticated();
        Tenant tenant = tenantRepository.findByUserId(authUser.getId())
            .orElseThrow(() -> new RuntimeException("Locataire introuvable"));
        return mapTenantResponse(tenant);
    }


}
