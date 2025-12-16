package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for Congress.gov member search.
 *
 * Matches the SearchResponse interface expected by the frontend SearchImportPanel.
 *
 * @param <T> The type of data in each search result
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class CongressSearchResponse<T> {

    /** List of search results */
    private List<CongressSearchResult<T>> results;

    /** Total number of results matching the search criteria */
    private int total;

    /** Current page number (1-indexed) */
    private int page;

    /** Number of results per page */
    private int pageSize;

    /** Rate limit remaining (from Congress.gov API) */
    private Integer rateLimitRemaining;

    /** Time until rate limit resets (seconds) */
    private Integer rateLimitResetSeconds;
}
