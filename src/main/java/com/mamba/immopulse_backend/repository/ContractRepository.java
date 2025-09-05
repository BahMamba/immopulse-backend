package com.mamba.immopulse_backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Contract;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    Optional<Contract> findByBailId(Long bailId);
    Optional<Contract> findByPropertyIdAndBailIsNull(Long propertyId);
    Page<Contract> findByPropertyId(Pageable pageable, Long propertyId);
}