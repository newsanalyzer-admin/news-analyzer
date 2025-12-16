package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a paginated response of documents from the Federal Register API.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FederalRegisterDocumentPage {

    /**
     * Total count of matching documents.
     */
    private int count;

    /**
     * Total number of pages.
     */
    @JsonProperty("total_pages")
    private int totalPages;

    /**
     * Next page URL (null if on last page).
     */
    @JsonProperty("next_page_url")
    private String nextPageUrl;

    /**
     * Previous page URL (null if on first page).
     */
    @JsonProperty("previous_page_url")
    private String previousPageUrl;

    /**
     * List of documents on this page.
     */
    private List<FederalRegisterDocument> results;

    /**
     * Check if there are more pages.
     *
     * @return true if there is a next page
     */
    public boolean hasNextPage() {
        return nextPageUrl != null && !nextPageUrl.isEmpty();
    }

    /**
     * Check if the results are empty.
     *
     * @return true if no results
     */
    public boolean isEmpty() {
        return results == null || results.isEmpty();
    }
}
