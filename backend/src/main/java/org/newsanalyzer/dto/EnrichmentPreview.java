package org.newsanalyzer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Preview DTO showing what will change during enrichment.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
public class EnrichmentPreview {

    /** BioGuide ID of the legislator */
    private String bioguideId;

    /** Whether a local Person match was found */
    private boolean localMatch;

    /** Current Person data if matched */
    private PersonSnapshot currentPerson;

    /** New data from Legislators Repo */
    private EnrichmentData newData;

    /** Fields that will be added (don't exist in current) */
    private List<String> fieldsToAdd;

    /** Fields that will be updated (have different values) */
    private List<String> fieldsToUpdate;

    /** Total number of changes that would be made */
    private int totalChanges;

    @Data
    @Builder
    public static class PersonSnapshot {
        private String id;
        private String name;
        private Map<String, Object> externalIds;
        private Map<String, Object> socialMedia;
        private String enrichmentSource;
        private String enrichmentVersion;
    }

    @Data
    @Builder
    public static class EnrichmentData {
        private Map<String, Object> externalIds;
        private Map<String, String> socialMedia;
    }
}
