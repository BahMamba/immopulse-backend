package com.mamba.immopulse_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Bail;
import com.mamba.immopulse_backend.model.enums.bail.BailStatus;

@Repository
public interface BailRepository extends JpaRepository<Bail, Long> {
    Optional<Bail> findByTenantIdAndStatus(Long tenantId, BailStatus status);
    Optional<Bail> findByPropertyIdAndStatus(Long propertyId, BailStatus status);    
}
