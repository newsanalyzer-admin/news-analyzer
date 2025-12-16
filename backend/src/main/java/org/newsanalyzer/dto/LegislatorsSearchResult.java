package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Wrapper for a single Legislators Repo search result with source attribution.
 *
 * @param <T> The type of data contained in this result
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class LegislatorsSearchResult<T> {

    /** The actual data from Legislators Repo */
    private T data;

    /** Source name */
    private String source;

    /** Link to the GitHub repository */
    private String sourceUrl;

    /** If a local Person match was found, the existing Person's UUID */
    private String localMatchId;
}
