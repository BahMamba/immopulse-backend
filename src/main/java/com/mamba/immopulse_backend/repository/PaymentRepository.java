package com.mamba.immopulse_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{
    List<Payment> findByTenantId(Long tenantId);

}
