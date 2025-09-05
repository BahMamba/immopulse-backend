package com.mamba.immopulse_backend.model.entity;

import com.mamba.immopulse_backend.model.enums.contract.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    @NotNull
    private Property property;

    @OneToOne
    @JoinColumn(name = "bail_id", unique = true)
    private Bail bail;

    @NotEmpty(message = "Les conditions d’utilisation sont obligatoires")
    private String usageConditions;

    @NotNull(message = "L’URL du contrat est obligatoire")
    private String contractUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ContractStatus status;
}