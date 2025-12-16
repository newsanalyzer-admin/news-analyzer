package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Query parameters for fetching documents from the Federal Register API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentQueryParams {

    /**
     * Publication date greater than or equal to (for incremental sync).
     */
    private LocalDate publicationDateGte;

    /**
     * Publication date less than or equal to.
     */
    private LocalDate publicationDateLte;

    /**
     * Document types to filter (Rule, Proposed Rule, Notice, Presidential Document).
     */
    private List<String> documentTypes;

    /**
     * Federal Register agency IDs to filter.
     */
    private List<Integer> agencyIds;

    /**
     * Number of results per page.
     */
    @Builder.Default
    private int perPage = 100;

    /**
     * Page number (1-based).
     */
    @Builder.Default
    private int page = 1;

    /**
     * Build URL query string for Federal Register API.
     *
     * @param baseUrl The base URL of the API
     * @return Complete URL with query parameters
     */
    public String buildUrl(String baseUrl) {
        StringBuilder url = new StringBuilder(baseUrl).append("/documents?");

        if (publicationDateGte != null) {
            url.append("conditions[publication_date][gte]=")
               .append(publicationDateGte)
               .append("&");
        }

        if (publicationDateLte != null) {
            url.append("conditions[publication_date][lte]=")
               .append(publicationDateLte)
               .append("&");
        }

        if (documentTypes != null && !documentTypes.isEmpty()) {
            for (String type : documentTypes) {
                url.append("conditions[type][]=").append(type).append("&");
            }
        }

        if (agencyIds != null && !agencyIds.isEmpty()) {
            for (Integer agencyId : agencyIds) {
                url.append("conditions[agencies][]=").append(agencyId).append("&");
            }
        }

        url.append("per_page=").append(perPage);
        url.append("&page=").append(page);

        // Request specific fields to reduce payload
        url.append("&fields[]=document_number");
        url.append("&fields[]=title");
        url.append("&fields[]=abstract");
        url.append("&fields[]=type");
        url.append("&fields[]=publication_date");
        url.append("&fields[]=effective_on");
        url.append("&fields[]=signing_date");
        url.append("&fields[]=agencies");
        url.append("&fields[]=cfr_references");
        url.append("&fields[]=docket_ids");
        url.append("&fields[]=regulation_id_number");
        url.append("&fields[]=html_url");
        url.append("&fields[]=pdf_url");

        return url.toString();
    }
}
