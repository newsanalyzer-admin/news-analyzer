package org.newsanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Code of Federal Regulations (CFR) reference.
 * Used for JSONB storage in the Regulation entity.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CfrReference {

    /**
     * CFR Title number (e.g., 40 for Environment).
     */
    private Integer title;

    /**
     * CFR Part number.
     */
    private Integer part;

    /**
     * CFR Section (optional, may be null).
     */
    private String section;

    /**
     * Generate the full CFR citation string.
     *
     * @return formatted citation like "40 CFR 60" or "40 CFR 60.5"
     */
    public String getFullCitation() {
        StringBuilder citation = new StringBuilder();
        citation.append(title).append(" CFR ");
        if (part != null) {
            citation.append(part);
            if (section != null && !section.isBlank()) {
                citation.append(".").append(section);
            }
        }
        return citation.toString();
    }
}
