package com.mamba.immopulse_backend.service;

import com.mamba.immopulse_backend.model.dto.bails.BailRenewRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailRequest;
import com.mamba.immopulse_backend.model.dto.bails.BailResponse;
import com.mamba.immopulse_backend.model.dto.bails.ContractPdfData;
import com.mamba.immopulse_backend.model.entity.Bail;
import com.mamba.immopulse_backend.model.entity.Contract;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.entity.Tenant;
import com.mamba.immopulse_backend.model.enums.bail.BailStatus;
import com.mamba.immopulse_backend.model.enums.contract.ContractStatus;
import com.mamba.immopulse_backend.model.enums.property.PropertyStatus;
import com.mamba.immopulse_backend.model.enums.tenant.TenantStatus;
import com.mamba.immopulse_backend.repository.BailRepository;
import com.mamba.immopulse_backend.repository.ContractRepository;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.mamba.immopulse_backend.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final ContractRepository contractRepository;
    private final ContractService contractService;

    // Transforme un Bail en BailResponse pour l'API
    private BailResponse mapBailResponse(Bail bail) {
        return new BailResponse(
                bail.getId(),
                bail.getTenant().getId(),
                bail.getTenant() != null && bail.getTenant().getUser() != null
                        ? bail.getTenant().getUser().getFullname() : "N/A",
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

    // Crée un bail actif avec un contrat spécifique
    @Transactional
    public BailResponse createBail(BailRequest request) {
        // Valide les dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate = LocalDate.parse(request.startDate(), formatter);
        LocalDate endDate = LocalDate.parse(request.endDate(), formatter);
        if (!startDate.isBefore(endDate)) {
            throw new RuntimeException("Date non valide !");
        }

        // Récupère propriété et locataire
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new RuntimeException("Propriété introuvable !"));
        Tenant tenant = tenantRepository.findById(request.tenantId())
                .orElseThrow(() -> new RuntimeException("Locataire introuvable !"));

        // Vérifie les conditions du bail
        if (property.getStatus() != PropertyStatus.DISPONIBLE) {
            throw new RuntimeException("La propriété n'est pas disponible");
        }
        if (bailRepository.findByTenantIdAndStatus(request.tenantId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("Le locataire a déjà un bail actif");
        }
        if (bailRepository.findByPropertyIdAndStatus(request.propertyId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("La propriété a déjà un bail actif");
        }

        // Vérifie les finances (exigences guinéennes)
        BigDecimal rentAmount = request.rentAmount() != null ? request.rentAmount() : property.getPrice();
        if (rentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loyer invalide !");
        }
        BigDecimal oneMonth = rentAmount;
        BigDecimal twoMonths = rentAmount.multiply(BigDecimal.valueOf(2));
        if (request.depositAmount().compareTo(oneMonth) < 0 || request.depositAmount().compareTo(twoMonths) > 0) {
            throw new RuntimeException("La caution doit être égale à 1 ou 2 mois de loyer");
        }
        BigDecimal agencyFee = request.agencyFee() != null ? request.agencyFee() : rentAmount;

        // Récupère le contrat type
        Contract typeContract = contractRepository.findByPropertyIdAndBailIsNull(request.propertyId())
                .orElseThrow(() -> new RuntimeException("Contrat type introuvable"));

        // Crée le bail
        Bail bail = Bail.builder()
                .tenant(tenant)
                .property(property)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .depositAmount(request.depositAmount())
                .rentAmount(rentAmount)
                .agencyFee(agencyFee)
                .status(BailStatus.ACTIF)
                .build();

        // Crée le contrat spécifique
        Contract contract = Contract.builder()
                .property(property)
                .bail(bail)
                .usageConditions(typeContract.getUsageConditions())
                .contractUrl(contractService.generateContractPdf(ContractPdfData.fromContract(
                        Contract.builder().usageConditions(typeContract.getUsageConditions()).build(),
                        bail, property)))
                .status(ContractStatus.ACTIVE)
                .build();

        bail.setContractUrl(contract.getContractUrl());
        tenant.setStatus(TenantStatus.ACTIF);
        property.setStatus(PropertyStatus.LOUER);

        bailRepository.save(bail);
        contractRepository.save(contract);
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

        Contract contract = contractRepository.findByBailId(bailId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable !"));

        bail.setStatus(BailStatus.ANNULE);
        contract.setStatus(ContractStatus.CANCELLED);
        bail.getTenant().setStatus(TenantStatus.INACTIF);
        bail.getProperty().setStatus(PropertyStatus.DISPONIBLE);

        bailRepository.save(bail);
        contractRepository.save(contract);
        tenantRepository.save(bail.getTenant());
        propertyRepository.save(bail.getProperty());
        return mapBailResponse(bail);
    }

    // Suspend un bail actif
    @Transactional
    public BailResponse suspendBail(Long bailId) {
        Bail bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new RuntimeException("Bail introuvable !"));
        if (bail.getStatus() != BailStatus.ACTIF) {
            throw new RuntimeException("Le bail n'est pas actif");
        }

        Contract contract = contractRepository.findByBailId(bailId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable !"));

        bail.setStatus(BailStatus.SUSPENDU);
        contract.setStatus(ContractStatus.SUSPENDED);
        bail.getTenant().setStatus(TenantStatus.INACTIF);
        bail.getProperty().setStatus(PropertyStatus.DISPONIBLE);

        bailRepository.save(bail);
        contractRepository.save(contract);
        tenantRepository.save(bail.getTenant());
        propertyRepository.save(bail.getProperty());
        return mapBailResponse(bail);
    }

    // Reprend un bail suspendu
    @Transactional
    public BailResponse resumeBail(Long bailId) {
        Bail bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new RuntimeException("Bail introuvable !"));
        if (bail.getStatus() != BailStatus.SUSPENDU) {
            throw new RuntimeException("Le bail n'est pas suspendu");
        }

        Contract contract = contractRepository.findByBailId(bailId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable !"));

        // Vérifie que la propriété est disponible
        Property property = bail.getProperty();
        if (property.getStatus() != PropertyStatus.DISPONIBLE) {
            throw new RuntimeException("La propriété n'est pas disponible");
        }
        if (bailRepository.findByPropertyIdAndStatus(property.getId(), BailStatus.ACTIF).isPresent()) {
            throw new RuntimeException("La propriété a un bail actif");
        }

        bail.setStatus(BailStatus.ACTIF);
        contract.setStatus(ContractStatus.ACTIVE);
        bail.getTenant().setStatus(TenantStatus.ACTIF);
        bail.getProperty().setStatus(PropertyStatus.LOUER);

        bailRepository.save(bail);
        contractRepository.save(contract);
        tenantRepository.save(bail.getTenant());
        propertyRepository.save(bail.getProperty());
        return mapBailResponse(bail);
    }

    // Termine les baux expirés (tâche planifiée)
    @Scheduled(cron = "0 0 0 * * ?") // Exécute tous les jours à minuit
    @Transactional
    public void terminateExpiredBails() {
        LocalDate today = LocalDate.now();
        bailRepository.findByStatusAndEndDateBefore(BailStatus.ACTIF, today.toString())
                .forEach(bail -> {
                    Contract contract = contractRepository.findByBailId(bail.getId())
                            .orElseThrow(() -> new RuntimeException("Contrat introuvable pour bail " + bail.getId()));
                    bail.setStatus(BailStatus.TERMINE);
                    contract.setStatus(ContractStatus.TERMINATED);
                    bail.getTenant().setStatus(TenantStatus.INACTIF);
                    bail.getProperty().setStatus(PropertyStatus.DISPONIBLE);
                    bailRepository.save(bail);
                    contractRepository.save(contract);
                    tenantRepository.save(bail.getTenant());
                    propertyRepository.save(bail.getProperty());
                });
    }

    // Renouvelle un bail actif
    @Transactional
    public BailResponse renewBail(Long bailId, BailRenewRequest request) {
        Bail bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new RuntimeException("Bail introuvable"));
        if (bail.getStatus() != BailStatus.ACTIF) {
            throw new RuntimeException("Ce bail n'est pas actif");
        }

        // Valide la nouvelle date de fin
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate newEndDate = LocalDate.parse(request.newEndDate(), formatter);
        LocalDate currentEndDate = LocalDate.parse(bail.getEndDate(), formatter);
        if (!newEndDate.isAfter(currentEndDate)) {
            throw new RuntimeException("Date invalide !");
        }

        // Valide le nouveau loyer
        BigDecimal newAmount = request.newRentAmount() != null ? request.newRentAmount() : bail.getRentAmount();
        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Loyer invalide");
        }

        // Crée un nouveau contrat pour le renouvellement
        Contract oldContract = contractRepository.findByBailId(bailId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable"));
        Contract newContract = Contract.builder()
                .property(bail.getProperty())
                .bail(bail)
                .usageConditions(oldContract.getUsageConditions())
                .contractUrl(contractService.generateContractPdf(ContractPdfData.fromContract(
                        Contract.builder().usageConditions(oldContract.getUsageConditions()).build(),
                        bail, bail.getProperty())))
                .status(ContractStatus.ACTIVE)
                .build();

        bail.setEndDate(request.newEndDate());
        bail.setRentAmount(newAmount);
        bail.setContractUrl(newContract.getContractUrl());

        bailRepository.save(bail);
        contractRepository.save(newContract);
        return mapBailResponse(bail);
    }

    // Récupère le bail actif d’un locataire
    public BailResponse getBailByTenant(Long tenantId) {
        Bail bail = bailRepository.findByTenantIdAndStatus(tenantId, BailStatus.ACTIF)
                .orElseThrow(() -> new RuntimeException("Aucun bail actif trouvé"));
        return mapBailResponse(bail);
    }

    // Liste l’historique des baux d’un locataire avec pagination
    public Page<BailResponse> getBailByTenant(Long tenantId, int page, int size) {
        tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Locataire introuvable"));
        Pageable pageable = PageRequest.of(page, size);
        return bailRepository.findByTenantId(tenantId, pageable)
                .map(this::mapBailResponse);
    }

    // Liste l’historique des baux d’une propriété avec pagination
    public Page<BailResponse> getBailsByProperty(Long propertyId, int page, int size) {
        propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propriété introuvable"));
        Pageable pageable = PageRequest.of(page, size);
        return bailRepository.findByPropertyId(propertyId, pageable)
                .map(this::mapBailResponse);
    }
}