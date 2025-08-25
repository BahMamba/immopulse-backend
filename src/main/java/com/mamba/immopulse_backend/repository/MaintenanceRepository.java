package com.mamba.immopulse_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Maintenance;
@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long>{
    List<Maintenance> findByTenantId(Long tenantId);

}
