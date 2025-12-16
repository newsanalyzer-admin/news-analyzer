package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO representing a document from the Federal Register API.
 *
 * Maps to the JSON response from: https://www.federalregister.gov/api/v1/documents
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FederalRegisterDocument {

    /**
     * Federal Register document number (unique identifier).
     * Example: "2024-12345"
     */
    @JsonProperty("document_number")
    private String documentNumber;

    /**
     * Title of the document.
     */
    private String title;

    /**
     * Abstract/summary of the document.
     * Named 'abstract' in JSON but mapped to documentAbstract to avoid Java keyword.
     */
    @JsonProperty("abstract")
    private String documentAbstract;

    /**
     * Document type from Federal Register (Rule, Proposed Rule, Notice, Presidential Document).
     */
    private String type;

    /**
     * Date the document was published in the Federal Register.
     */
    @JsonProperty("publication_date")
    private LocalDate publicationDate;

    /**
     * Date the rule becomes effective (for final rules).
     */
    @JsonProperty("effective_on")
    private LocalDate effectiveOn;

    /**
     * Date the document was signed (for presidential documents).
     */
    @JsonProperty("signing_date")
    private LocalDate signingDate;

    /**
     * List of agencies associated with this document.
     */
    private List<FederalRegisterAgency> agencies;

    /**
     * Code of Federal Regulations references.
     */
    @JsonProperty("cfr_references")
    private List<FederalRegisterCfrReference> cfrReferences;

    /**
     * Docket IDs associated with this document.
     */
    @JsonProperty("docket_ids")
    private List<String> docketIds;

    /**
     * Regulation Identifier Number (RIN) assigned by OMB.
     */
    @JsonProperty("regulation_id_number")
    private String regulationIdNumber;

    /**
     * URL to the HTML version of the document.
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * URL to the PDF version of the document.
     */
    @JsonProperty("pdf_url")
    private String pdfUrl;

    /**
     * CFR reference from the Federal Register API response.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FederalRegisterCfrReference {
        private Integer title;
        private Integer part;
        private String section;
    }
}
