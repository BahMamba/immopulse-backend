package com.mamba.immopulse_backend.service;

import com.mamba.immopulse_backend.model.dto.bails.ContractPdfData;
import com.mamba.immopulse_backend.model.dto.bails.ContractRequest;
import com.mamba.immopulse_backend.model.dto.bails.ContractResponse;
import com.mamba.immopulse_backend.model.entity.Contract;
import com.mamba.immopulse_backend.model.entity.Property;
import com.mamba.immopulse_backend.model.enums.contract.ContractStatus;
import com.mamba.immopulse_backend.repository.ContractRepository;
import com.mamba.immopulse_backend.repository.PropertyRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final PropertyRepository propertyRepository;
    private final Logger log = LoggerFactory.getLogger(ContractService.class);

    @Value("${app.contracts.storage.path}")
    private String storagePath;

    // Crée un contrat type pour une propriété
    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Property property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new RuntimeException("Propriété introuvable"));

        Contract contract = Contract.builder()
                .property(property)
                .usageConditions(request.usageConditions())
                .contractUrl(generateContractPdf(ContractPdfData.fromContract(
                        Contract.builder().usageConditions(request.usageConditions()).build(),
                        null, property)))
                .status(ContractStatus.DRAFT)
                .build();

        contractRepository.save(contract);
        return mapContractResponse(contract);
    }

    // Récupère les contrats d’une propriété (paginés)
    public Page<ContractResponse> getContractByProperty(Long propertyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Contract> contracts = contractRepository.findByPropertyId(pageable, propertyId);
        return contracts.map(this::mapContractResponse);
    }

    // Récupère un contrat spécifique par bail
    public ContractResponse getContractByBail(Long bailId) {
        Contract contract = contractRepository.findByBailId(bailId)
                .orElseThrow(() -> new RuntimeException("Contrat introuvable pour bailId: " + bailId));
        return mapContractResponse(contract);
    }

    // Génère un PDF pour un contrat (type ou spécifique)
    public String generateContractPdf(ContractPdfData pdfData) {
        if (pdfData.propertyId() == null || pdfData.propertyTitle() == null || pdfData.usageConditions() == null) {
            log.error("Données PDF incomplètes: {}", pdfData);
            throw new IllegalArgumentException("Données PDF incomplètes");
        }

        String fileName = pdfData.bailId() != null ? "bail_" + pdfData.bailId() + ".pdf" : "template_property_" + pdfData.propertyId() + ".pdf";
        String filePath = storagePath + fileName;

        try (PdfWriter writer = new PdfWriter(filePath); PdfDocument pdf = new PdfDocument(writer); Document document = new Document(pdf)) {
            PdfFont font = PdfFontFactory.createFont("Helvetica");

            // En-tête
            document.add(new Paragraph("Contrat Immobilier - ImmoPulse")
                    .setFont(font).setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Date: " + LocalDate.now())
                    .setFont(font).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            // Section Propriété
            document.add(new Paragraph("Propriété").setFont(font).setFontSize(12).setBold());
            document.add(new Paragraph("Titre: " + pdfData.propertyTitle()).setFont(font).setFontSize(10));
            document.add(new Paragraph("Adresse: " + pdfData.propertyAddress())).setFont(font).setFontSize(10);

            // Section Bail (si applicable)
            if (pdfData.bailId() != null) {
                document.add(new Paragraph("Détails du Bail").setFont(font).setFontSize(12).setBold());
                Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
                table.addCell("Bail ID").addCell(String.valueOf(pdfData.bailId()));
                table.addCell("Locataire").addCell(pdfData.tenantName() != null ? pdfData.tenantName() : "N/A");
                table.addCell("Loyer").addCell(pdfData.rentAmount() != null ? pdfData.rentAmount() + " GNF" : "N/A");
                table.addCell("Caution").addCell(pdfData.depositAmount() != null ? pdfData.depositAmount() + " GNF" : "N/A");
                table.addCell("Frais d’agence").addCell(pdfData.agencyFee() != null ? pdfData.agencyFee() + " GNF" : "N/A");
                document.add(table);
            }

            // Section Conditions
            document.add(new Paragraph("Conditions d’utilisation").setFont(font).setFontSize(12).setBold());
            document.add(new Paragraph(pdfData.usageConditions()).setFont(font).setFontSize(10));

            log.info("PDF généré: {}", filePath);
            return "/contracts/" + fileName;
        } catch (Exception e) {
            log.error("Erreur génération PDF: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    // Mappe une entité Contract vers ContractResponse
    private ContractResponse mapContractResponse(Contract contract) {
        return new ContractResponse(
                contract.getId(),
                contract.getProperty().getId(),
                contract.getBail() != null ? contract.getBail().getId() : null,
                contract.getUsageConditions(),
                contract.getContractUrl(),
                contract.getStatus().name()
        );
    }
}