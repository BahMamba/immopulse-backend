package com.mamba.immopulse_backend.service;

import com.mamba.immopulse_backend.model.dto.bails.BailRenewRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailResponse;
import com.mamba.immopulse_backend.model.entity.Bail;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.Tenant;
import com.mamba.immopulse_backend.model.enums.bail.BailStatus;
import com.mamba.immopulse_backend.model.enums.property.PropertyStatus;
import com.mamba.immopulse_backend.model.enums.tenant.TenantStatus;
import com.mamba.immopulse_backend.repository.BailRepository;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.mamba.immopulse_backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BailService {
    private final BailRepository bailRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    // Transforme un Bail en BailResponse pour l'API
    private BailResponse mapBailResponse(Bail bail) {
        return new BailResponse(
            bail.getId(),
            bail.getTenant().getId(),
            bail.getTenant().getUser().getFullname(), 
            bail.getProperty().getId(),
            bail.getProperty().getTitle(),
            bail.getStartDate(),
            bail.getEndDate(),
            bail.getDepositAmount(),
            bail.getRentAmount(),
            bail.getAgencyFee(),
            bail.getContractUrl(),
            bail.getStatus()
        );
    }

    // Crée un bail entre un locataire et une propriété
    @Transactional
    public BailResponse createBail(BailRequest request) {
        // Vérifie que startDate < endDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(request.startDate(), formatter);
        LocalDate endDate = LocalDate.parse(request.endDate(), formatter);
        if (!startDate.isBefore(endDate)) {
            throw new RuntimeException("Date non valide!");
        }

        // Recuperer la property et le tenant 
        Property property = propertyRepository.findById(request.propertyId())
            .orElseThrow(() -> new RuntimeException("Propriété introuvable !"));
        Tenant tenant = tenantRepository.findById(request.tenantId())
            .orElseThrow(() -> new RuntimeException("Locataire introuvable !"));

        // Verif des conditions du bails
        if (property.getStatus() != PropertyStatus.DISPONIBLE) {
            throw new RuntimeException("La propriété n'est pas disponible");
        }
        if (bailRepository.findByTenantIdAndStatus(request.tenantId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("Le locataire a déjà un bail actif");
        }
        if (bailRepository.findByPropertyIdAndStatus(request.propertyId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("La propriété a déjà un bail actif");
        }

        // Verif finances

        BigDecimal rentAmount = request.rentAmount() != null ? request.rentAmount() : property.getPrice();
        if (rentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loyer invalide!");
        }

        BigDecimal oneMonth = rentAmount;
        BigDecimal twoMonth = rentAmount.multiply(BigDecimal.valueOf(2));
        if (request.depositAmount().compareTo(oneMonth) < 0 || request.depositAmount().compareTo(twoMonth) > 0) {
            throw new RuntimeException("La caution doit être égale à 1 ou 2 mois de loyer");
        }

        // Definir agencyFee(frais d'agence, defaut = 1x rentAmount :))
        BigDecimal agencyFee = request.agencyFee() != null ? request.agencyFee() : rentAmount;

        // Creation du bail
        Bail bail = new Bail();
        bail.setTenant(tenant);
        bail.setProperty(property);
        bail.setStartDate(request.startDate());
        bail.setEndDate(request.endDate());
        bail.setDepositAmount(request.depositAmount());
        bail.setRentAmount(rentAmount);
        bail.setAgencyFee(agencyFee);
        bail.setContractUrl(request.contractUrl());
        bail.setStatus(BailStatus.ACTIF);

        tenant.setStatus(TenantStatus.ACTIF);
        property.setStatus(PropertyStatus.LOUER);

        bailRepository.save(bail);
        tenantRepository.save(tenant);
        propertyRepository.save(property);
        return mapBailResponse(bail);
    }

    // Annule un bail actif
    @Transactional
    public BailResponse cancelBail(Long bailId) {
        Bail bail = bailRepository.findById(bailId)
            .orElseThrow(() -> new RuntimeException("Bail introuvable !"));
        if (bail.getStatus() != BailStatus.ACTIF) {
            throw new RuntimeException("Le bail n'est pas actif");
        }

        Tenant tenant = bail.getTenant();
        Property property = bail.getProperty();

        bail.setStatus(BailStatus.ANNULE);
        tenant.setStatus(TenantStatus.INACTIF); // INACTIF au lieu de EXPIRE
        property.setStatus(PropertyStatus.DISPONIBLE);

        bailRepository.save(bail);
        tenantRepository.save(tenant);
        propertyRepository.save(property);
        return mapBailResponse(bail);
    }

    // Récupère le bail actif d’un locataire
    public BailResponse getBailByTenant(Long tenantId) {
        Bail bail = bailRepository.findByTenantIdAndStatus(tenantId, BailStatus.ACTIF)
            .orElseThrow(() -> new RuntimeException("Aucun bail actif trouvé"));
        return mapBailResponse(bail);
    }

    // Renouvellement d'un Bail ACTIF
    @Transactional
    public BailResponse renewBail(Long bailId, BailRenewRequest request) {
        Bail bail = bailRepository.findById(bailId)
            .orElseThrow(() -> new RuntimeException("Bail Introuvable"));
        
        if (bail.getStatus() != BailStatus.ACTIF) {
            throw new RuntimeException("Ce Bail n'est plus actif");
        }

        // Validation des dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate newEndDate = LocalDate.parse(request.newEndDate(), formatter);
        LocalDate currentEndDate = LocalDate.parse(bail.getEndDate(), formatter);
        if (!newEndDate.isAfter(currentEndDate)) {
            throw new RuntimeException("Date Invalide!");
        }

        // Définition du nouveau loyer
        BigDecimal newAmount = request.newRentAmount() != null ? request.newRentAmount() : bail.getRentAmount();
        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loyer Invalide");
        }

        bail.setEndDate(request.newEndDate());
        bail.setRentAmount(newAmount);

        bailRepository.save(bail);

        return mapBailResponse(bail);
    }
}