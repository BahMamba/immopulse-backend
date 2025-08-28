// src/main/java/com/mamba/immopulse_backend/repository/TenantRepository.java
package com.mamba.immopulse_backend.repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mamba.immopulse_backend.model.entity.Tenant;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByUserId(Long userId);
    Page<Tenant> findByUserFullnameContainingIgnoreCase(String fullname, Pageable pageable);
}