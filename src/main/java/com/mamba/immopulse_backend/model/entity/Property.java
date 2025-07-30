package com.mamba.immopulse_backend.model.entity;

import java.math.BigDecimal;

import com.mamba.immopulse_backend.model.enums.PropertyStatus;
import com.mamba.immopulse_backend.model.enums.PropertyType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

// Model pour la gestion des biens par proprietaire(OWNER)

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "properties")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String address;

    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private PropertyType type;

    @Enumerated(EnumType.STRING)
    private PropertyStatus status;

    private String coverImageUrl;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

}
