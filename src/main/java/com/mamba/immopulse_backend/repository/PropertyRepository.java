package com.mamba.immopulse_backend.repository;

import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.User;
import com.mamba.immopulse_backend.model.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
    Page<Property> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Property> findByOwner(User owner, Pageable pageable);
    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);
}