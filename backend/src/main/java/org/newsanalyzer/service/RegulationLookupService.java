package org.newsanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.newsanalyzer.dto.RegulationDTO;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.model.RegulationAgency;
import org.newsanalyzer.repository.GovernmentOrganizationRepository;
import org.newsanalyzer.repository.RegulationAgencyRepository;
import org.newsanalyzer.repository.RegulationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for regulation lookup and query operations.
 * Provides business logic for all regulation API endpoints.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RegulationLookupService {

    private final RegulationRepository regulationRepository;
    private final RegulationAgencyRepository regulationAgencyRepository;
    private final GovernmentOrganizationRepository governmentOrganizationRepository;

    // =====================================================================
    // List and Pagination
    // =====================================================================

    /**
     * List regulations with pagination, most recent first.
     *
     * @param page page number (0-based)
     * @param size page size
     * @return page of regulation DTOs
     */
    public Page<RegulationDTO> listRegulations(int page, int size) {
        log.debug("Listing regulations: page={}, size={}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Regulation> regulations = regulationRepository.findAll(pageRequest);
        return toDTOPage(regulations);
    }

    // =====================================================================
    // Single Regulation Lookup
    // =====================================================================

    /**
     * Find a regulation by its Federal Register document number.
     *
     * @param documentNumber the document number (e.g., "2024-12345")
     * @return optional containing the regulation DTO if found
     */
    public Optional<RegulationDTO> findByDocumentNumber(String documentNumber) {
        log.debug("Finding regulation by document number: {}", documentNumber);
        return regulationRepository.findByDocumentNumber(documentNumber)
                .map(this::toDTO);
    }

    // =====================================================================
    // Full-Text Search (Task 4)
    // =====================================================================

    /**
     * Search regulations by title and abstract using PostgreSQL full-text search.
     *
     * @param query search query
     * @param page page number
     * @param size page size
     * @return page of matching regulations, sorted by relevance
     */
    public Page<RegulationDTO> searchRegulations(String query, int page, int size) {
        log.debug("Searching regulations: query='{}', page={}, size={}", query, page, size);
        if (query == null || query.isBlank()) {
            return Page.empty();
        }
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Regulation> regulations = regulationRepository.searchByTitleOrAbstract(query.trim(), pageRequest);
        return toDTOPage(regulations);
    }

    // =====================================================================
    // Agency Filtering
    // =====================================================================

    /**
     * Find regulations by issuing agency using efficient JOIN query.
     *
     * @param orgId government organization ID
     * @param page page number
     * @param size page size
     * @return page of regulations issued by the specified agency
     */
    public Page<RegulationDTO> findByAgency(UUID orgId, int page, int size) {
        log.debug("Finding regulations by agency: orgId={}, page={}, size={}", orgId, page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Regulation> regulations = regulationRepository.findByAgencyId(orgId, pageRequest);
        return toDTOPage(regulations);
    }

    // =====================================================================
    // Document Type Filtering
    // =====================================================================

    /**
     * Find regulations by document type.
     *
     * @param type document type (RULE, PROPOSED_RULE, NOTICE, etc.)
     * @param page page number
     * @param size page size
     * @return page of regulations of the specified type
     */
    public Page<RegulationDTO> findByDocumentType(DocumentType type, int page, int size) {
        log.debug("Finding regulations by type: type={}, page={}, size={}", type, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Regulation> regulations = regulationRepository.findByDocumentType(type, pageRequest);
        return toDTOPage(regulations);
    }

    // =====================================================================
    // Date Range Filtering
    // =====================================================================

    /**
     * Find regulations published within a date range.
     *
     * @param start start date (inclusive)
     * @param end end date (inclusive)
     * @param page page number
     * @param size page size
     * @return page of regulations published in the date range
     */
    public Page<RegulationDTO> findByDateRange(LocalDate start, LocalDate end, int page, int size) {
        log.debug("Finding regulations by date range: start={}, end={}, page={}, size={}", start, end, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        Page<Regulation> regulations = regulationRepository.findByPublicationDateBetween(start, end, pageRequest);
        return toDTOPage(regulations);
    }

    // =====================================================================
    // Effective Date Query (Task 6)
    // =====================================================================

    /**
     * Find final rules effective on or before a specific date.
     * Only returns RULE type documents (not proposed rules or notices).
     *
     * @param date the effective date to check
     * @return list of rules effective on the specified date
     */
    public List<RegulationDTO> findRulesEffectiveOn(LocalDate date) {
        log.debug("Finding rules effective on: date={}", date);
        List<Regulation> regulations = regulationRepository.findRulesEffectiveOnOrBefore(date);
        return toDTOList(regulations);
    }

    // =====================================================================
    // CFR Lookup (Task 5)
    // =====================================================================

    /**
     * Find regulations by CFR citation (title and part).
     *
     * @param title CFR title number (e.g., 40 for environment)
     * @param part CFR part number
     * @return list of regulations referencing the specified CFR citation
     */
    public List<RegulationDTO> findByCfrReference(Integer title, Integer part) {
        log.debug("Finding regulations by CFR reference: title={}, part={}", title, part);
        // Build JSONB query for contains operator
        // The query matches any regulation with a CFR reference containing the specified title and part
        String cfrJson = String.format("{\"title\": %d, \"part\": %d}", title, part);
        List<Regulation> regulations = regulationRepository.findByCfrReference(cfrJson);
        return toDTOList(regulations);
    }

    // =====================================================================
    // DTO Mapping with Batch Agency Fetch
    // =====================================================================

    /**
     * Convert a page of regulations to DTOs with batch agency fetching.
     * Uses batch fetch to avoid N+1 query problem.
     */
    private Page<RegulationDTO> toDTOPage(Page<Regulation> regulations) {
        if (regulations.isEmpty()) {
            return Page.empty(regulations.getPageable());
        }

        // Batch fetch agencies for all regulations
        List<UUID> regulationIds = regulations.getContent().stream()
                .map(Regulation::getId)
                .toList();

        Map<UUID, List<RegulationAgency>> agenciesByRegulation =
                regulationAgencyRepository.findByRegulationIdIn(regulationIds).stream()
                        .collect(Collectors.groupingBy(RegulationAgency::getRegulationId));

        // Fetch organization details for agency names
        List<UUID> orgIds = agenciesByRegulation.values().stream()
                .flatMap(List::stream)
                .map(RegulationAgency::getOrganizationId)
                .distinct()
                .toList();

        Map<UUID, GovernmentOrganization> orgMap = governmentOrganizationRepository.findAllById(orgIds).stream()
                .collect(Collectors.toMap(GovernmentOrganization::getId, org -> org));

        // Convert to DTOs
        List<RegulationDTO> dtos = regulations.getContent().stream()
                .map(r -> RegulationDTO.from(r,
                        agenciesByRegulation.getOrDefault(r.getId(), List.of()),
                        orgMap))
                .toList();

        return new PageImpl<>(dtos, regulations.getPageable(), regulations.getTotalElements());
    }

    /**
     * Convert a list of regulations to DTOs with batch agency fetching.
     */
    private List<RegulationDTO> toDTOList(List<Regulation> regulations) {
        if (regulations.isEmpty()) {
            return List.of();
        }

        // Batch fetch agencies
        List<UUID> regulationIds = regulations.stream()
                .map(Regulation::getId)
                .toList();

        Map<UUID, List<RegulationAgency>> agenciesByRegulation =
                regulationAgencyRepository.findByRegulationIdIn(regulationIds).stream()
                        .collect(Collectors.groupingBy(RegulationAgency::getRegulationId));

        // Fetch organization details
        List<UUID> orgIds = agenciesByRegulation.values().stream()
                .flatMap(List::stream)
                .map(RegulationAgency::getOrganizationId)
                .distinct()
                .toList();

        Map<UUID, GovernmentOrganization> orgMap = governmentOrganizationRepository.findAllById(orgIds).stream()
                .collect(Collectors.toMap(GovernmentOrganization::getId, org -> org));

        return regulations.stream()
                .map(r -> RegulationDTO.from(r,
                        agenciesByRegulation.getOrDefault(r.getId(), List.of()),
                        orgMap))
                .toList();
    }

    /**
     * Convert a single regulation to DTO (fetches agencies individually).
     */
    private RegulationDTO toDTO(Regulation regulation) {
        List<RegulationAgency> agencies = regulationAgencyRepository.findByRegulationId(regulation.getId());

        List<UUID> orgIds = agencies.stream()
                .map(RegulationAgency::getOrganizationId)
                .toList();

        Map<UUID, GovernmentOrganization> orgMap = governmentOrganizationRepository.findAllById(orgIds).stream()
                .collect(Collectors.toMap(GovernmentOrganization::getId, org -> org));

        return RegulationDTO.from(regulation, agencies, orgMap);
    }
}
