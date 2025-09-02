package com.mamba.immopulse_backend.model.entity;

import java.math.BigDecimal;

import com.mamba.immopulse_backend.model.enums.bail.BailStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "bails")
public class Bail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull
    private Tenant tenant;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    private Property property;

    @NotNull
    private String startDate;

    @NotNull
    private String endDate;

    @NotNull

    @NotNull
    private BigDecimal rentAmount;

    private BigDecimal agencyFee;

    @NotNull
    private BigDecimal depositAmount;

    private String contractUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    private BailStatus status;
}
