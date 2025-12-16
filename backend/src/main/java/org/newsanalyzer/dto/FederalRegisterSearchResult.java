package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Wrapper for a single Federal Register search result with source attribution.
 *
 * @param <T> The type of data contained in this result
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class FederalRegisterSearchResult<T> {

    /** The actual data from Federal Register */
    private T data;

    /** Source name, e.g., "Federal Register" */
    private String source;

    /** Link to the original document on federalregister.gov */
    private String sourceUrl;

    /** If a duplicate was detected, the existing Regulation's UUID */
    private String duplicateId;
}
