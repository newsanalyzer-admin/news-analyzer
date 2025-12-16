package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.Statute;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Statute API responses.
 * Used for US Code statutory law sections.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatuteDTO {

    private UUID id;

    /**
     * USC identifier (e.g., "/us/usc/t5/s101").
     */
    private String uscIdentifier;

    /**
     * Formatted citation for display (e.g., "5 U.S.C. § 101").
     */
    private String citation;

    /**
     * Title number (1-54).
     */
    private Integer titleNumber;

    /**
     * Title name.
     */
    private String titleName;

    /**
     * Chapter number.
     */
    private String chapterNumber;

    /**
     * Chapter name.
     */
    private String chapterName;

    /**
     * Section number.
     */
    private String sectionNumber;

    /**
     * Section heading.
     */
    private String heading;

    /**
     * Plain text content.
     */
    private String contentText;

    /**
     * Original XML content (optional, for detail view).
     */
    private String contentXml;

    /**
     * Legislative source credit.
     */
    private String sourceCredit;

    /**
     * URL to official source.
     */
    private String sourceUrl;

    /**
     * Release point identifier.
     */
    private String releasePoint;

    /**
     * Effective date if specified.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate effectiveDate;

    /**
     * Import source (e.g., "USCODE").
     */
    private String importSource;

    /**
     * When the record was last updated.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // =====================================================================
    // Factory Methods
    // =====================================================================

    /**
     * Create StatuteDTO from Statute entity (full detail).
     *
     * @param statute the statute entity
     * @return the DTO
     */
    public static StatuteDTO from(Statute statute) {
        return from(statute, true);
    }

    /**
     * Create StatuteDTO from Statute entity.
     *
     * @param statute the statute entity
     * @param includeXml whether to include XML content
     * @return the DTO
     */
    public static StatuteDTO from(Statute statute, boolean includeXml) {
        if (statute == null) return null;

        return StatuteDTO.builder()
                .id(statute.getId())
                .uscIdentifier(statute.getUscIdentifier())
                .citation(formatCitation(statute.getTitleNumber(), statute.getSectionNumber()))
                .titleNumber(statute.getTitleNumber())
                .titleName(statute.getTitleName())
                .chapterNumber(statute.getChapterNumber())
                .chapterName(statute.getChapterName())
                .sectionNumber(statute.getSectionNumber())
                .heading(statute.getHeading())
                .contentText(statute.getContentText())
                .contentXml(includeXml ? statute.getContentXml() : null)
                .sourceCredit(statute.getSourceCredit())
                .sourceUrl(statute.getSourceUrl())
                .releasePoint(statute.getReleasePoint())
                .effectiveDate(statute.getEffectiveDate())
                .importSource(statute.getImportSource())
                .updatedAt(statute.getUpdatedAt())
                .build();
    }

    /**
     * Create StatuteDTO from Statute entity (list view - no XML).
     *
     * @param statute the statute entity
     * @return the DTO without XML content
     */
    public static StatuteDTO forList(Statute statute) {
        return from(statute, false);
    }

    /**
     * Format a USC citation for display.
     * Example: 5 U.S.C. § 101
     *
     * @param titleNumber title number
     * @param sectionNumber section number
     * @return formatted citation string
     */
    public static String formatCitation(Integer titleNumber, String sectionNumber) {
        if (titleNumber == null || sectionNumber == null) {
            return null;
        }
        return String.format("%d U.S.C. § %s", titleNumber, sectionNumber);
    }
}
