package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a US Government organization from the Government Manual.
 *
 * This entity stores official government organizational data including:
 * - Official names and acronyms
 * - Hierarchical structure (parent-child relationships)
 * - Jurisdictions and responsibilities
 * - Schema.org JSON-LD representation
 * - Historical tracking (establishment, dissolution dates)
 *
 * Data Source: GovInfo API (https://api.govinfo.gov)
 * Collection: GOVMAN (US Government Manual)
 *
 * @author Winston (Architect Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "government_organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GovernmentOrganization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Official Information
    // =====================================================================

    @Column(name = "official_name", nullable = false, length = 500)
    @NotBlank(message = "Official name is required")
    @Size(max = 500, message = "Official name must be less than 500 characters")
    private String officialName;

    @Column(name = "acronym", length = 50)
    @Size(max = 50, message = "Acronym must be less than 50 characters")
    private String acronym;

    @Convert(converter = org.newsanalyzer.model.converter.StringListConverter.class)
    @Column(name = "former_names", columnDefinition = "text[]")
    private List<String> formerNames;

    // =====================================================================
    // Classification
    // =====================================================================

    @Column(name = "org_type", nullable = false, length = 100)
    @NotNull(message = "Organization type is required")
    @Convert(converter = org.newsanalyzer.model.converter.OrganizationTypeConverter.class)
    private OrganizationType orgType;

    @Column(name = "branch", nullable = false, length = 50)
    @NotNull(message = "Branch is required")
    @Convert(converter = org.newsanalyzer.model.converter.GovernmentBranchConverter.class)
    private GovernmentBranch branch;

    // =====================================================================
    // Hierarchy
    // =====================================================================

    @Column(name = "parent_id")
    private UUID parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private GovernmentOrganization parent;

    @Column(name = "org_level")
    @Min(value = 1, message = "Organization level must be at least 1")
    @Max(value = 10, message = "Organization level must be at most 10")
    private Integer orgLevel;

    // =====================================================================
    // Historical Information
    // =====================================================================

    @Column(name = "established_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate establishedDate;

    @Column(name = "dissolved_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dissolvedDate;

    @Column(name = "authorizing_legislation", columnDefinition = "TEXT")
    private String authorizingLegislation;

    // =====================================================================
    // Descriptive Information
    // =====================================================================

    @Column(name = "mission_statement", columnDefinition = "TEXT")
    @Size(max = 10000, message = "Mission statement must be less than 10000 characters")
    private String missionStatement;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // =====================================================================
    // Contact Information
    // =====================================================================

    @Column(name = "website_url", length = 500)
    @Size(max = 500, message = "Website URL must be less than 500 characters")
    private String websiteUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "contact_info", columnDefinition = "jsonb")
    private JsonNode contactInfo;

    // =====================================================================
    // Jurisdiction and Responsibilities
    // =====================================================================

    @Convert(converter = org.newsanalyzer.model.converter.StringListConverter.class)
    @Column(name = "jurisdiction_areas", columnDefinition = "text[]")
    private List<String> jurisdictionAreas;

    @Convert(converter = org.newsanalyzer.model.converter.StringListConverter.class)
    @Column(name = "primary_functions", columnDefinition = "text[]")
    private List<String> primaryFunctions;

    // =====================================================================
    // Metadata
    // =====================================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private JsonNode metadata;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_org_data", columnDefinition = "jsonb")
    private JsonNode schemaOrgData;

    // =====================================================================
    // Data Source Tracking
    // =====================================================================

    /**
     * Federal Register API agency ID for high-confidence matching.
     * Used to link regulations to government organizations.
     */
    @Column(name = "federal_register_agency_id")
    private Integer federalRegisterAgencyId;

    /**
     * Source of the import (e.g., GOVMAN, MANUAL, FEDERAL_REGISTER).
     * Tracks where this organization record originated.
     */
    @Column(name = "import_source", length = 50)
    private String importSource;

    @Column(name = "govinfo_package_id", length = 200)
    private String govinfoPackageId;

    @Column(name = "govinfo_year")
    private Integer govinfoYear;

    @Column(name = "govinfo_last_sync")
    private LocalDateTime govinfoLastSync;

    @Column(name = "data_quality_score", precision = 3, scale = 2)
    @DecimalMin(value = "0.00", message = "Data quality score must be at least 0.00")
    @DecimalMax(value = "1.00", message = "Data quality score must be at most 1.00")
    private BigDecimal dataQualityScore;

    // =====================================================================
    // Audit Fields
    // =====================================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // =====================================================================
    // Lifecycle Callbacks
    // =====================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (createdBy == null) {
            createdBy = "system";
        }
        if (updatedBy == null) {
            updatedBy = "system";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    /**
     * Check if organization is currently active
     */
    public boolean isActive() {
        return dissolvedDate == null;
    }

    /**
     * Check if organization is a top-level entity
     */
    public boolean isTopLevel() {
        return parentId == null || Integer.valueOf(1).equals(orgLevel);
    }

    /**
     * Check if organization is a Cabinet department
     */
    public boolean isCabinetDepartment() {
        return orgType == OrganizationType.DEPARTMENT
                && branch == GovernmentBranch.EXECUTIVE
                && Integer.valueOf(1).equals(orgLevel)
                && isActive();
    }

    /**
     * Check if organization is an independent agency
     */
    public boolean isIndependentAgency() {
        return orgType == OrganizationType.INDEPENDENT_AGENCY
                && branch == GovernmentBranch.EXECUTIVE
                && parentId == null
                && isActive();
    }

    /**
     * Get display name (acronym if available, otherwise official name)
     */
    public String getDisplayName() {
        return acronym != null && !acronym.isEmpty() ? acronym : officialName;
    }

    /**
     * Get full name with acronym
     */
    public String getFullNameWithAcronym() {
        if (acronym != null && !acronym.isEmpty()) {
            return String.format("%s (%s)", officialName, acronym);
        }
        return officialName;
    }

    // =====================================================================
    // Enums
    // =====================================================================

    /**
     * Type of government organization
     */
    public enum OrganizationType {
        BRANCH("branch"),
        DEPARTMENT("department"),
        INDEPENDENT_AGENCY("independent_agency"),
        BUREAU("bureau"),
        OFFICE("office"),
        COMMISSION("commission"),
        BOARD("board");

        private final String value;

        OrganizationType(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static OrganizationType fromValue(String value) {
            for (OrganizationType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown organization type: " + value);
        }
    }

    /**
     * Branch of US Government
     */
    public enum GovernmentBranch {
        EXECUTIVE("executive"),
        LEGISLATIVE("legislative"),
        JUDICIAL("judicial");

        private final String value;

        GovernmentBranch(String value) {
            this.value = value;
        }

        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }

        @com.fasterxml.jackson.annotation.JsonCreator
        public static GovernmentBranch fromValue(String value) {
            for (GovernmentBranch branch : values()) {
                if (branch.value.equals(value)) {
                    return branch;
                }
            }
            throw new IllegalArgumentException("Unknown government branch: " + value);
        }
    }
}
