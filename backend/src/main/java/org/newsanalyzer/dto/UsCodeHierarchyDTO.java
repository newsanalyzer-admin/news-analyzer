package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Hierarchical structure DTO for US Code tree view display.
 * Represents Title -> Chapters -> Sections hierarchy.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsCodeHierarchyDTO {

    /**
     * US Code title number (1-54).
     */
    private Integer titleNumber;

    /**
     * Title name (e.g., "GOVERNMENT ORGANIZATION AND EMPLOYEES").
     */
    private String titleName;

    /**
     * Total section count in this title.
     */
    private long sectionCount;

    /**
     * Chapters within this title.
     */
    private List<ChapterDTO> chapters;

    /**
     * Chapter within a US Code title.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChapterDTO {

        /**
         * Chapter number (e.g., "1", "2", "3A").
         */
        private String chapterNumber;

        /**
         * Chapter name/heading.
         */
        private String chapterName;

        /**
         * Number of sections in this chapter.
         */
        private int sectionCount;

        /**
         * Sections within this chapter.
         */
        private List<SectionSummaryDTO> sections;
    }

    /**
     * Section summary for tree view (lightweight).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectionSummaryDTO {

        /**
         * Database UUID.
         */
        private UUID id;

        /**
         * Section number (e.g., "101", "102a").
         */
        private String sectionNumber;

        /**
         * Section heading.
         */
        private String heading;

        /**
         * Preview of content text (first 200 characters).
         */
        private String contentPreview;

        /**
         * USC identifier path (e.g., "/us/usc/t5/s101").
         */
        private String uscIdentifier;
    }
}
