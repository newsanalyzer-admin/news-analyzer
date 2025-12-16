package org.newsanalyzer.model;

/**
 * Enumeration of Federal Register document types.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
public enum DocumentType {
    RULE("Final Rule", "Final rules that have the force of law"),
    PROPOSED_RULE("Proposed Rule", "Notice of Proposed Rulemaking (NPRM)"),
    NOTICE("Notice", "Agency notices and announcements"),
    PRESIDENTIAL_DOCUMENT("Presidential Document", "Executive orders, proclamations, etc."),
    OTHER("Other", "Other document types");

    private final String displayName;
    private final String description;

    DocumentType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convert Federal Register API type string to DocumentType enum.
     *
     * @param type the type string from Federal Register API
     * @return the corresponding DocumentType, or OTHER if not recognized
     */
    public static DocumentType fromFederalRegisterType(String type) {
        if (type == null) return OTHER;
        return switch (type.toLowerCase()) {
            case "rule" -> RULE;
            case "proposed rule" -> PROPOSED_RULE;
            case "notice" -> NOTICE;
            case "presidential document" -> PRESIDENTIAL_DOCUMENT;
            default -> OTHER;
        };
    }
}
