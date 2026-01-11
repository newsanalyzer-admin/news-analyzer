package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a Congressional Member - a specialized view of an Individual
 * who serves or has served in the United States Congress.
 *
 * This entity contains Congress-specific data only. Biographical data (name, birth date, etc.)
 * is stored in the linked Individual entity.
 *
 * Part of ARCH-1 refactor: Separates Congressional-specific data from universal person data.
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 * @see Individual for biographical data
 */
@jakarta.persistence.Entity
@Table(name = "congressional_members",
        indexes = {
                @Index(name = "idx_congressional_member_individual", columnList = "individual_id"),
                @Index(name = "idx_congressional_member_state", columnList = "state"),
                @Index(name = "idx_congressional_member_chamber", columnList = "chamber"),
                @Index(name = "idx_congressional_member_party", columnList = "party")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_congressional_member_individual", columnNames = "individual_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CongressionalMember {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Link to Individual (biographical data)
    // =====================================================================

    @Column(name = "individual_id", nullable = false, unique = true)
    @NotNull(message = "Individual ID is required")
    private UUID individualId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id", insertable = false, updatable = false)
    @JsonIgnore
    private Individual individual;

    // =====================================================================
    // Congress.gov Identifiers
    // =====================================================================

    @Column(name = "bioguide_id", length = 20)
    @Size(max = 20, message = "BioGuide ID must be less than 20 characters")
    private String bioguideId;

    // =====================================================================
    // Congressional Information
    // =====================================================================

    @Column(name = "chamber", length = 20)
    @Enumerated(EnumType.STRING)
    private Chamber chamber;

    @Column(name = "state", length = 2)
    @Size(max = 2, message = "State must be 2-letter code")
    private String state;

    @Column(name = "party", length = 50)
    @Size(max = 50, message = "Party must be less than 50 characters")
    private String party;

    // =====================================================================
    // Sync Tracking
    // =====================================================================

    @Column(name = "congress_last_sync")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime congressLastSync;

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
     * Get full name from linked Individual (convenience method)
     * @return full name or null if individual not loaded
     */
    public String getFullName() {
        return individual != null ? individual.getFullName() : null;
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
