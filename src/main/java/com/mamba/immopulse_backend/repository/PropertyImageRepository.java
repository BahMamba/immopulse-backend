package com.mamba.immopulse_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.PropertyImage;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {
    List<PropertyImage> findByProperty(Property property);
}
