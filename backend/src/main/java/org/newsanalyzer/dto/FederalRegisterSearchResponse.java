package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for Federal Register document search.
 *
 * Matches the SearchResponse interface expected by the frontend SearchImportPanel.
 *
 * @param <T> The type of data in each search result
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class FederalRegisterSearchResponse<T> {

    /** List of search results */
    private List<FederalRegisterSearchResult<T>> results;

    /** Total number of results matching the search criteria */
    private int total;

    /** Current page number (1-indexed) */
    private int page;

    /** Number of results per page */
    private int pageSize;
}
