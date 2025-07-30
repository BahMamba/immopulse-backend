package com.mamba.immopulse_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.User;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByOwner(User owner, Pageable pageable); // Pagination by owner

    Page<Property> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Property> findByAddressContainingIgnoreCase(String address, Pageable pageable);
}
