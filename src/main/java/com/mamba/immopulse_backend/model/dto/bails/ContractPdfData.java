package com.mamba.immopulse_backend.model.dto.bails;

import com.mamba.immopulse_backend.model.entity.Bail;
import com.mamba.immopulse_backend.model.entity.Contract;
import com.mamba.immopulse_backend.model.entity.Property;
import java.math.BigDecimal;

public record ContractPdfData(
        Long propertyId,
        String propertyTitle,
        String propertyAddress,
        Long bailId,
        String tenantName,
        BigDecimal rentAmount,
        BigDecimal depositAmount,
        BigDecimal agencyFee,
        String usageConditions
) {
    public static ContractPdfData fromContract(Contract contract, Bail bail, Property property) {
        return new ContractPdfData(
                property.getId(),
                property.getTitle(),
                property.getAddress() != null ? property.getAddress() : "N/A",
                bail != null ? bail.getId() : null,
                bail != null && bail.getTenant() != null && bail.getTenant().getUser() != null
                        ? bail.getTenant().getUser().getFullname() : null,
                bail != null ? bail.getRentAmount() : null,
                bail != null ? bail.getDepositAmount() : null,
                bail != null ? bail.getAgencyFee() : null,
                contract.getUsageConditions()
        );
    }
}