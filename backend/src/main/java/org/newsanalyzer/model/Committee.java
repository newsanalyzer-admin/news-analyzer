package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a Congressional committee from Congress.gov API.
 *
 * Committees are identified by their systemCode (e.g., 'hsju00' for House Judiciary).
 * Supports self-referencing relationship for subcommittees.
 *
 * Data Source: Congress.gov API
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://api.congress.gov">Congress.gov API</a>
 */
@Entity
@Table(name = "committees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Committee {

    @Id
    @Column(name = "committee_code", length = 20)
    @NotBlank(message = "Committee code is required")
    @Size(max = 20, message = "Committee code must be less than 20 characters")
    private String committeeCode;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank(message = "Committee name is required")
    @Size(max = 255, message = "Committee name must be less than 255 characters")
    private String name;

    @Column(name = "chamber", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Chamber is required")
    private CommitteeChamber chamber;

    @Column(name = "committee_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Committee type is required")
    private CommitteeType committeeType;

    // =====================================================================
    // Self-referencing relationship for subcommittees
    // =====================================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_committee_code", referencedColumnName = "committee_code")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Committee parentCommittee;

    @OneToMany(mappedBy = "parentCommittee", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Committee> subcommittees = new ArrayList<>();

    // =====================================================================
    // Committee memberships
    // =====================================================================

    @OneToMany(mappedBy = "committee", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CommitteeMembership> memberships = new ArrayList<>();

    // =====================================================================
    // External IDs for Cross-Referencing
    // =====================================================================

    @Column(name = "thomas_id", length = 20)
    @Size(max = 20, message = "Thomas ID must be less than 20 characters")
    private String thomasId;

    @Column(name = "url", length = 500)
    @Size(max = 500, message = "URL must be less than 500 characters")
    private String url;

    // =====================================================================
    // Data Source Tracking
    // =====================================================================

    @Column(name = "congress_last_sync")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime congressLastSync;

    @Column(name = "data_source", length = 50)
    @Size(max = 50, message = "Data source must be less than 50 characters")
    private String dataSource;

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
            dataSource = "CONGRESS_GOV";
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
     * Check if this committee is a subcommittee
     */
    public boolean isSubcommittee() {
        return parentCommittee != null || committeeType == CommitteeType.SUBCOMMITTEE;
    }

    /**
     * Check if this is a joint committee (Senate + House)
     */
    public boolean isJointCommittee() {
        return chamber == CommitteeChamber.JOINT || committeeType == CommitteeType.JOINT;
    }

    /**
     * Get parent committee code (null if not a subcommittee)
     */
    public String getParentCommitteeCode() {
        return parentCommittee != null ? parentCommittee.getCommitteeCode() : null;
    }
}
