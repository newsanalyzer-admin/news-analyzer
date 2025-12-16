package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Federal Register document full details (preview).
 *
 * Extends search DTO with additional fields: abstract, effectiveDate,
 * cfrReferences, docketIds, pdfUrl.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FederalRegisterDetailDTO {

    /** Federal Register document number (unique identifier) */
    private String documentNumber;

    /** Document title */
    private String title;

    /** Document abstract/summary */
    private String documentAbstract;

    /** Document type (Rule, Proposed Rule, Notice, Presidential Document) */
    private String documentType;

    /** Publication date in the Federal Register */
    private LocalDate publicationDate;

    /** Effective date (for rules) */
    private LocalDate effectiveDate;

    /** Signing date (for presidential documents) */
    private LocalDate signingDate;

    /** List of agency names associated with this document */
    private List<String> agencies;

    /** CFR reference citations */
    private List<CfrReferenceDTO> cfrReferences;

    /** Docket IDs */
    private List<String> docketIds;

    /** Regulation ID Number (RIN) */
    private String regulationIdNumber;

    /** URL to the HTML version */
    private String htmlUrl;

    /** URL to the PDF version */
    private String pdfUrl;

    /**
     * CFR reference DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CfrReferenceDTO {
        private Integer title;
        private Integer part;
        private String section;
    }
}
