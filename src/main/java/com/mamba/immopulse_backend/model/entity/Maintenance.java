package com.mamba.immopulse_backend.model.entity;

import java.time.LocalDateTime;

import com.mamba.immopulse_backend.model.enums.maintenance.Status;
import com.mamba.immopulse_backend.model.enums.maintenance.Urgency;

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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Model pour la gestion des maintenances, reporting lies aux soucies de chaque appartement

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "maintenance_requests")
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    private String description;

    @Enumerated(EnumType.STRING)
    private Urgency urgency;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String category;
    private String assignedTo;
}
