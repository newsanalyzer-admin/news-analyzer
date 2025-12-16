package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Wrapper for a single Congress.gov search result with source attribution.
 *
 * @param <T> The type of data contained in this result
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class CongressSearchResult<T> {

    /** The actual data from Congress.gov */
    private T data;

    /** Source name, e.g., "Congress.gov" */
    private String source;

    /** Link to the original record on Congress.gov */
    private String sourceUrl;

    /** If a duplicate was detected, the existing Person's UUID */
    private String duplicateId;
}
