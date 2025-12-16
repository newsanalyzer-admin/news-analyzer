package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Federal Register document search results.
 *
 * Contains fields needed for search result display:
 * documentNumber, title, documentType, publicationDate, agencies, htmlUrl.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FederalRegisterSearchDTO {

    /** Federal Register document number (unique identifier, e.g., "2024-12345") */
    private String documentNumber;

    /** Document title */
    private String title;

    /** Document type (Rule, Proposed Rule, Notice, Presidential Document) */
    private String documentType;

    /** Publication date in the Federal Register */
    private LocalDate publicationDate;

    /** List of agency names associated with this document */
    private List<String> agencies;

    /** URL to the HTML version of the document */
    private String htmlUrl;
}
