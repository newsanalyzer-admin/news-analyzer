package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.FederalRegisterAgency;
import org.newsanalyzer.dto.FederalRegisterDocument;
import org.newsanalyzer.dto.FederalRegisterImportResult;
import org.newsanalyzer.model.CfrReference;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for importing individual Federal Register documents.
 *
 * Provides single-document import functionality with agency linkage,
 * for use by the Admin Import API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FederalRegisterImportService {

    private final FederalRegisterClient federalRegisterClient;
    private final RegulationRepository regulationRepository;
    private final AgencyLinkageService agencyLinkageService;

    /**
     * Import a single document by document number.
     *
     * @param documentNumber The Federal Register document number
     * @param forceOverwrite If true, overwrite existing record
     * @return Import result with details
     */
    @Transactional
    public FederalRegisterImportResult importDocument(String documentNumber, boolean forceOverwrite) {
        log.info("Importing Federal Register document: {}, forceOverwrite={}", documentNumber, forceOverwrite);

        // Check if document already exists
        Optional<Regulation> existing = regulationRepository.findByDocumentNumber(documentNumber);

        if (existing.isPresent() && !forceOverwrite) {
            Regulation reg = existing.get();
            return FederalRegisterImportResult.builder()
                    .id(reg.getId().toString())
                    .documentNumber(reg.getDocumentNumber())
                    .title(reg.getTitle())
                    .created(false)
                    .updated(false)
                    .error("Record already exists. Set forceOverwrite=true to update.")
                    .build();
        }

        // Fetch document from Federal Register API
        Optional<FederalRegisterDocument> docOpt = federalRegisterClient.fetchDocument(documentNumber);

        if (docOpt.isEmpty()) {
            return FederalRegisterImportResult.builder()
                    .documentNumber(documentNumber)
                    .error("Document not found on Federal Register")
                    .build();
        }

        FederalRegisterDocument doc = docOpt.get();
        boolean isNew = existing.isEmpty();

        Regulation regulation;
        if (isNew) {
            regulation = createRegulation(doc);
        } else {
            regulation = existing.get();
            updateRegulation(regulation, doc);
        }

        regulationRepository.save(regulation);

        // Link agencies and track results
        List<String> linkedAgencyNames = new ArrayList<>();
        List<String> unmatchedAgencyNames = new ArrayList<>();

        if (doc.getAgencies() != null && !doc.getAgencies().isEmpty()) {
            // Check which agencies can be matched before linking
            for (FederalRegisterAgency agency : doc.getAgencies()) {
                Optional<UUID> orgId = agencyLinkageService.findGovernmentOrganization(agency);
                if (orgId.isPresent()) {
                    linkedAgencyNames.add(agency.getName());
                } else {
                    if (agency.getName() != null) {
                        unmatchedAgencyNames.add(agency.getName());
                    }
                }
            }
            // Perform actual linkage
            agencyLinkageService.linkRegulationToAgencies(regulation, doc.getAgencies());
        }

        log.info("Imported document {}: isNew={}, linkedAgencies={}, unmatchedAgencies={}",
                documentNumber, isNew, linkedAgencyNames.size(), unmatchedAgencyNames.size());

        return FederalRegisterImportResult.builder()
                .id(regulation.getId().toString())
                .documentNumber(regulation.getDocumentNumber())
                .title(regulation.getTitle())
                .created(isNew)
                .updated(!isNew)
                .linkedAgencies(linkedAgencyNames.size())
                .linkedAgencyNames(linkedAgencyNames)
                .unmatchedAgencyNames(unmatchedAgencyNames)
                .build();
    }

    /**
     * Create a new Regulation entity from API document.
     */
    private Regulation createRegulation(FederalRegisterDocument doc) {
        return Regulation.builder()
                .documentNumber(doc.getDocumentNumber())
                .title(truncate(doc.getTitle(), 1000))
                .documentAbstract(doc.getDocumentAbstract())
                .documentType(DocumentType.fromFederalRegisterType(doc.getType()))
                .publicationDate(doc.getPublicationDate())
                .effectiveOn(doc.getEffectiveOn())
                .signingDate(doc.getSigningDate())
                .regulationIdNumber(doc.getRegulationIdNumber())
                .cfrReferences(convertCfrReferences(doc.getCfrReferences()))
                .docketIds(doc.getDocketIds())
                .sourceUrl(buildSourceUrl(doc.getDocumentNumber()))
                .pdfUrl(doc.getPdfUrl())
                .htmlUrl(doc.getHtmlUrl())
                .build();
    }

    /**
     * Update an existing Regulation entity from API document.
     */
    private void updateRegulation(Regulation regulation, FederalRegisterDocument doc) {
        regulation.setTitle(truncate(doc.getTitle(), 1000));
        regulation.setDocumentAbstract(doc.getDocumentAbstract());
        regulation.setDocumentType(DocumentType.fromFederalRegisterType(doc.getType()));
        regulation.setEffectiveOn(doc.getEffectiveOn());
        regulation.setSigningDate(doc.getSigningDate());
        regulation.setRegulationIdNumber(doc.getRegulationIdNumber());
        regulation.setCfrReferences(convertCfrReferences(doc.getCfrReferences()));
        regulation.setDocketIds(doc.getDocketIds());
        regulation.setPdfUrl(doc.getPdfUrl());
        regulation.setHtmlUrl(doc.getHtmlUrl());
    }

    /**
     * Convert API CFR references to model CFR references.
     */
    private List<CfrReference> convertCfrReferences(List<FederalRegisterDocument.FederalRegisterCfrReference> apiRefs) {
        if (apiRefs == null || apiRefs.isEmpty()) {
            return null;
        }
        return apiRefs.stream()
                .map(ref -> new CfrReference(ref.getTitle(), ref.getPart(), ref.getSection()))
                .collect(Collectors.toList());
    }

    /**
     * Build the source URL for a document.
     */
    private String buildSourceUrl(String documentNumber) {
        return "https://www.federalregister.gov/d/" + documentNumber;
    }

    /**
     * Truncate a string to max length.
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
