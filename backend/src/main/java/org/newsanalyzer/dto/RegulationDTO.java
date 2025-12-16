package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.CfrReference;
import org.newsanalyzer.model.DocumentType;
import org.newsanalyzer.model.GovernmentOrganization;
import org.newsanalyzer.model.Regulation;
import org.newsanalyzer.model.RegulationAgency;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Regulation API responses.
 * Includes all regulation fields plus linked agencies with names.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegulationDTO {

    private UUID id;
    private String documentNumber;
    private String title;
    private String documentAbstract;
    private String documentType;
    private String documentTypeDescription;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate publicationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate signingDate;
    private String regulationIdNumber;
    private List<CfrReferenceDTO> cfrReferences;
    private List<String> docketIds;
    private List<AgencyDTO> agencies;
    private String sourceUrl;
    private String pdfUrl;
    private String htmlUrl;

    // =====================================================================
    // Nested DTOs
    // =====================================================================

    /**
     * DTO for CFR reference information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CfrReferenceDTO {
        private Integer title;
        private Integer part;
        private String section;
        private String fullCitation;

        /**
         * Create CfrReferenceDTO from CfrReference model.
         */
        public static CfrReferenceDTO from(CfrReference cfr) {
            if (cfr == null) return null;
            return CfrReferenceDTO.builder()
                    .title(cfr.getTitle())
                    .part(cfr.getPart())
                    .section(cfr.getSection())
                    .fullCitation(cfr.getFullCitation())
                    .build();
        }
    }

    /**
     * DTO for agency information linked to a regulation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AgencyDTO {
        private UUID id;
        private String name;
        private String acronym;
        private boolean primary;

        /**
         * Create AgencyDTO from RegulationAgency and GovernmentOrganization.
         */
        public static AgencyDTO from(RegulationAgency ra, GovernmentOrganization org) {
            return AgencyDTO.builder()
                    .id(ra.getOrganizationId())
                    .name(org != null ? org.getOfficialName() : ra.getAgencyNameRaw())
                    .acronym(org != null ? org.getAcronym() : null)
                    .primary(ra.isPrimaryAgency())
                    .build();
        }

        /**
         * Create AgencyDTO from RegulationAgency only (when org not available).
         */
        public static AgencyDTO from(RegulationAgency ra) {
            return AgencyDTO.builder()
                    .id(ra.getOrganizationId())
                    .name(ra.getAgencyNameRaw())
                    .acronym(null)
                    .primary(ra.isPrimaryAgency())
                    .build();
        }
    }

    // =====================================================================
    // Factory Methods
    // =====================================================================

    /**
     * Create RegulationDTO from Regulation entity and agency list.
     *
     * @param regulation the regulation entity
     * @param agencies list of regulation-agency links
     * @return the DTO
     */
    public static RegulationDTO from(Regulation regulation, List<RegulationAgency> agencies) {
        return from(regulation, agencies, Map.of());
    }

    /**
     * Create RegulationDTO from Regulation entity, agency list, and org map.
     *
     * @param regulation the regulation entity
     * @param agencies list of regulation-agency links
     * @param orgMap map of organization ID to GovernmentOrganization for name lookup
     * @return the DTO
     */
    public static RegulationDTO from(Regulation regulation,
                                     List<RegulationAgency> agencies,
                                     Map<UUID, GovernmentOrganization> orgMap) {
        if (regulation == null) return null;

        DocumentType docType = regulation.getDocumentType();

        List<CfrReferenceDTO> cfrDtos = null;
        if (regulation.getCfrReferences() != null) {
            cfrDtos = regulation.getCfrReferences().stream()
                    .map(CfrReferenceDTO::from)
                    .collect(Collectors.toList());
        }

        List<AgencyDTO> agencyDtos = null;
        if (agencies != null) {
            agencyDtos = agencies.stream()
                    .map(ra -> {
                        GovernmentOrganization org = orgMap.get(ra.getOrganizationId());
                        return org != null ? AgencyDTO.from(ra, org) : AgencyDTO.from(ra);
                    })
                    .collect(Collectors.toList());
        }

        return RegulationDTO.builder()
                .id(regulation.getId())
                .documentNumber(regulation.getDocumentNumber())
                .title(regulation.getTitle())
                .documentAbstract(regulation.getDocumentAbstract())
                .documentType(docType != null ? docType.name() : null)
                .documentTypeDescription(docType != null ? docType.getDisplayName() : null)
                .publicationDate(regulation.getPublicationDate())
                .effectiveOn(regulation.getEffectiveOn())
                .signingDate(regulation.getSigningDate())
                .regulationIdNumber(regulation.getRegulationIdNumber())
                .cfrReferences(cfrDtos)
                .docketIds(regulation.getDocketIds())
                .agencies(agencyDtos)
                .sourceUrl(regulation.getSourceUrl())
                .pdfUrl(regulation.getPdfUrl())
                .htmlUrl(regulation.getHtmlUrl())
                .build();
    }
}
