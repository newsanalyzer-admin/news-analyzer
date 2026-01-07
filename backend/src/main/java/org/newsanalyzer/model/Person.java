package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Person from authoritative sources (Congress.gov, PLUM, etc.).
 *
 * This is master data for Congressional members and other government officials.
 * Separate from the Entity table which stores extracted/transient entities.
 *
 * Data Source: Congress.gov API
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://api.congress.gov">Congress.gov API</a>
 */
@jakarta.persistence.Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Congress.gov Identifiers
    // =====================================================================

    @Column(name = "bioguide_id", length = 20)
    @Size(max = 20, message = "BioGuide ID must be less than 20 characters")
    private String bioguideId;

    // =====================================================================
    // Personal Information
    // =====================================================================

    @Column(name = "first_name", nullable = false, length = 100)
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be less than 100 characters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be less than 100 characters")
    private String lastName;

    @Column(name = "middle_name", length = 100)
    @Size(max = 100, message = "Middle name must be less than 100 characters")
    private String middleName;

    @Column(name = "suffix", length = 20)
    @Size(max = 20, message = "Suffix must be less than 20 characters")
    private String suffix;

    // =====================================================================
    // Political Information
    // =====================================================================

    @Column(name = "party", length = 50)
    @Size(max = 50, message = "Party must be less than 50 characters")
    private String party;

    @Column(name = "state", length = 2)
    @Size(max = 2, message = "State must be 2-letter code")
    private String state;

    @Column(name = "chamber", length = 20)
    @Enumerated(EnumType.STRING)
    private Chamber chamber;

    // =====================================================================
    // Biographical Information
    // =====================================================================

    @Column(name = "birth_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @Column(name = "death_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate deathDate;

    @Column(name = "birth_place", length = 200)
    @Size(max = 200, message = "Birth place must be less than 200 characters")
    private String birthPlace;

    @Column(name = "gender", length = 10)
    @Size(max = 10, message = "Gender must be less than 10 characters")
    private String gender;

    @Column(name = "image_url", length = 500)
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String imageUrl;

    // =====================================================================
    // External IDs for Cross-Referencing
    // =====================================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    private JsonNode externalIds;

    // =====================================================================
    // Social Media
    // =====================================================================

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "social_media", columnDefinition = "jsonb")
    private JsonNode socialMedia;

    // =====================================================================
    // Enrichment Tracking
    // =====================================================================

    @Column(name = "enrichment_source", length = 50)
    @Size(max = 50, message = "Enrichment source must be less than 50 characters")
    private String enrichmentSource;

    @Column(name = "enrichment_version", length = 50)
    @Size(max = 50, message = "Enrichment version must be less than 50 characters")
    private String enrichmentVersion;

    // =====================================================================
    // Data Source Tracking
    // =====================================================================

    @Column(name = "congress_last_sync")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime congressLastSync;

    @Column(name = "data_source", length = 50)
    @Enumerated(EnumType.STRING)
    private DataSource dataSource;

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
        if (dataSource == null) {
            dataSource = DataSource.CONGRESS_GOV;
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
     * Get full name (first + middle + last + suffix)
     */
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(firstName);
        if (middleName != null && !middleName.isEmpty()) {
            sb.append(" ").append(middleName);
        }
        sb.append(" ").append(lastName);
        if (suffix != null && !suffix.isEmpty()) {
            sb.append(" ").append(suffix);
        }
        return sb.toString();
    }

    /**
     * Get display name (last, first)
     */
    public String getDisplayName() {
        return lastName + ", " + firstName;
    }

    /**
     * Check if member is currently serving in Senate
     */
    public boolean isSenator() {
        return chamber == Chamber.SENATE;
    }

    /**
     * Check if member is currently serving in House
     */
    public boolean isRepresentative() {
        return chamber == Chamber.HOUSE;
    }

    /**
     * Check if person is living (no death date)
     */
    public boolean isLiving() {
        return deathDate == null;
    }

    // =====================================================================
    // Enums
    // =====================================================================

    /**
     * Congressional chamber
     */
    public enum Chamber {
        SENATE,
        HOUSE
    }
}
