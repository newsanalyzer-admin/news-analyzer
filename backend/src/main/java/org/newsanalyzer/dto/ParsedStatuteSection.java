package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for a parsed US Code section from USLM XML.
 * Used as intermediate format between XML parser and Statute entity.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParsedStatuteSection {

    /**
     * USC identifier from section/@identifier attribute.
     * Example: "/us/usc/t5/s101"
     */
    private String uscIdentifier;

    /**
     * Title number extracted from identifier.
     */
    private Integer titleNumber;

    /**
     * Title name from document context.
     */
    private String titleName;

    /**
     * Chapter number from parent context.
     */
    private String chapterNumber;

    /**
     * Chapter name from parent context.
     */
    private String chapterName;

    /**
     * Section number from num element.
     * Example: "§ 101" → "101"
     */
    private String sectionNumber;

    /**
     * Section heading from heading element.
     */
    private String heading;

    /**
     * Plain text content extracted from content element.
     */
    private String contentText;

    /**
     * Original XML of the section for preservation.
     */
    private String contentXml;

    /**
     * Source credit from sourceCredit element.
     */
    private String sourceCredit;

    /**
     * Indicates if parsing encountered issues.
     */
    private boolean hasParseErrors;

    /**
     * Error message if parsing had issues.
     */
    private String parseErrorMessage;
}
