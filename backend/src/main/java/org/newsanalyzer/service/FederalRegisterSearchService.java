package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.*;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for searching Federal Register documents with duplicate detection.
 *
 * Wraps FederalRegisterClient and adds:
 * - Response transformation to DTOs
 * - Duplicate detection against local Regulation table
 * - Keyword filtering
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FederalRegisterSearchService {

    private static final String SOURCE_NAME = "Federal Register";

    private final FederalRegisterClient federalRegisterClient;
    private final RegulationRepository regulationRepository;

    /**
     * Search documents from Federal Register API with filtering and pagination.
     *
     * @param keyword      Keyword filter (searches title)
     * @param agencyId     Federal Register agency ID filter
     * @param documentType Document type filter (Rule, Proposed Rule, Notice, Presidential Document)
     * @param dateFrom     Publication date from (inclusive)
     * @param dateTo       Publication date to (inclusive)
     * @param page         Page number (1-indexed)
     * @param pageSize     Results per page
     * @return Search response with results and pagination info
     */
    public FederalRegisterSearchResponse<FederalRegisterSearchDTO> searchDocuments(
            String keyword,
            Integer agencyId,
            String documentType,
            LocalDate dateFrom,
            LocalDate dateTo,
            int page,
            int pageSize) {

        log.info("Searching Federal Register: keyword={}, agencyId={}, documentType={}, dateFrom={}, dateTo={}, page={}, pageSize={}",
                keyword, agencyId, documentType, dateFrom, dateTo, page, pageSize);

        // Build query parameters
        DocumentQueryParams.DocumentQueryParamsBuilder paramsBuilder = DocumentQueryParams.builder()
                .page(page)
                .perPage(pageSize);

        if (dateFrom != null) {
            paramsBuilder.publicationDateGte(dateFrom);
        }
        if (dateTo != null) {
            paramsBuilder.publicationDateLte(dateTo);
        }
        if (documentType != null && !documentType.isEmpty()) {
            paramsBuilder.documentTypes(List.of(documentType));
        }
        if (agencyId != null) {
            paramsBuilder.agencyIds(List.of(agencyId));
        }

        DocumentQueryParams params = paramsBuilder.build();

        // Fetch from Federal Register API
        FederalRegisterDocumentPage apiResponse = federalRegisterClient.fetchDocuments(params);

        if (apiResponse.isEmpty()) {
            log.warn("No response from Federal Register API");
            return FederalRegisterSearchResponse.<FederalRegisterSearchDTO>builder()
                    .results(new ArrayList<>())
                    .total(0)
                    .page(page)
                    .pageSize(pageSize)
                    .build();
        }

        List<FederalRegisterSearchResult<FederalRegisterSearchDTO>> results = new ArrayList<>();

        for (FederalRegisterDocument doc : apiResponse.getResults()) {
            FederalRegisterSearchDTO dto = mapToSearchDTO(doc);

            // Apply keyword filter (client-side filtering since API has limited support)
            if (keyword != null && !keyword.isEmpty()) {
                String lowerKeyword = keyword.toLowerCase();
                String title = dto.getTitle() != null ? dto.getTitle().toLowerCase() : "";
                if (!title.contains(lowerKeyword)) {
                    continue;
                }
            }

            // Check for duplicate in local database
            String duplicateId = null;
            if (dto.getDocumentNumber() != null) {
                Optional<Regulation> existing = regulationRepository.findByDocumentNumber(dto.getDocumentNumber());
                if (existing.isPresent()) {
                    duplicateId = existing.get().getId().toString();
                }
            }

            results.add(FederalRegisterSearchResult.<FederalRegisterSearchDTO>builder()
                    .data(dto)
                    .source(SOURCE_NAME)
                    .sourceUrl(doc.getHtmlUrl())
                    .duplicateId(duplicateId)
                    .build());
        }

        // Use the API's count which represents total matching documents
        // Fall back to results size only if count is 0 and we have results (unlikely edge case)
        int total = apiResponse.getCount() > 0 ? apiResponse.getCount() : results.size();

        return FederalRegisterSearchResponse.<FederalRegisterSearchDTO>builder()
                .results(results)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * Get document details by document number.
     *
     * @param documentNumber The Federal Register document number
     * @return Document details or empty if not found
     */
    public Optional<FederalRegisterDetailDTO> getDocumentByNumber(String documentNumber) {
        log.info("Fetching Federal Register document: {}", documentNumber);

        Optional<FederalRegisterDocument> doc = federalRegisterClient.fetchDocument(documentNumber);

        return doc.map(this::mapToDetailDTO);
    }

    /**
     * Get all agencies from Federal Register.
     *
     * @return List of agencies
     */
    public List<FederalRegisterAgency> getAllAgencies() {
        return federalRegisterClient.fetchAllAgencies();
    }

    /**
     * Check if a document exists in local database.
     *
     * @param documentNumber The document number to check
     * @return Optional containing the Regulation if found
     */
    public Optional<Regulation> findLocalRegulation(String documentNumber) {
        return regulationRepository.findByDocumentNumber(documentNumber);
    }

    /**
     * Map API document to search DTO.
     */
    private FederalRegisterSearchDTO mapToSearchDTO(FederalRegisterDocument doc) {
        List<String> agencyNames = new ArrayList<>();
        if (doc.getAgencies() != null) {
            agencyNames = doc.getAgencies().stream()
                    .map(FederalRegisterAgency::getName)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
        }

        return FederalRegisterSearchDTO.builder()
                .documentNumber(doc.getDocumentNumber())
                .title(doc.getTitle())
                .documentType(doc.getType())
                .publicationDate(doc.getPublicationDate())
                .agencies(agencyNames)
                .htmlUrl(doc.getHtmlUrl())
                .build();
    }

    /**
     * Map API document to detail DTO.
     */
    private FederalRegisterDetailDTO mapToDetailDTO(FederalRegisterDocument doc) {
        List<String> agencyNames = new ArrayList<>();
        if (doc.getAgencies() != null) {
            agencyNames = doc.getAgencies().stream()
                    .map(FederalRegisterAgency::getName)
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
        }

        List<FederalRegisterDetailDTO.CfrReferenceDTO> cfrRefs = new ArrayList<>();
        if (doc.getCfrReferences() != null) {
            cfrRefs = doc.getCfrReferences().stream()
                    .map(ref -> FederalRegisterDetailDTO.CfrReferenceDTO.builder()
                            .title(ref.getTitle())
                            .part(ref.getPart())
                            .section(ref.getSection())
                            .build())
                    .collect(Collectors.toList());
        }

        return FederalRegisterDetailDTO.builder()
                .documentNumber(doc.getDocumentNumber())
                .title(doc.getTitle())
                .documentAbstract(doc.getDocumentAbstract())
                .documentType(doc.getType())
                .publicationDate(doc.getPublicationDate())
                .effectiveDate(doc.getEffectiveOn())
                .signingDate(doc.getSigningDate())
                .agencies(agencyNames)
                .cfrReferences(cfrRefs)
                .docketIds(doc.getDocketIds())
                .regulationIdNumber(doc.getRegulationIdNumber())
                .htmlUrl(doc.getHtmlUrl())
                .pdfUrl(doc.getPdfUrl())
                .build();
    }
}
