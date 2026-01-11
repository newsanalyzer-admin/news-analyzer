package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a committee membership - the link between a CongressionalMember and a Committee.
 *
 * Tracks which members serve on which committees, with their role and congressional session.
 *
 * Data Source: Congress.gov API
 *
 * Part of ARCH-1.5: Updated to reference CongressionalMember instead of Person.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://api.congress.gov">Congress.gov API</a>
 */
@Entity
@Table(name = "committee_memberships",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_member_committee_congress",
                columnNames = {"congressional_member_id", "committee_code", "congress"}
        ))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitteeMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Relationships
    // =====================================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "congressional_member_id", nullable = false)
    @NotNull(message = "Congressional member is required")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CongressionalMember congressionalMember;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "committee_code", nullable = false)
    @NotNull(message = "Committee is required")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Committee committee;

    // =====================================================================
    // Membership Details
    // =====================================================================

    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    @Builder.Default
    private MembershipRole role = MembershipRole.MEMBER;

    @Column(name = "congress", nullable = false)
    @NotNull(message = "Congress is required")
    @Min(value = 1, message = "Congress must be a positive number")
    private Integer congress;

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

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
        if (role == null) {
            role = MembershipRole.MEMBER;
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
     * Check if this membership is for a leadership role
     */
    public boolean isLeadershipRole() {
        return role == MembershipRole.CHAIR ||
               role == MembershipRole.VICE_CHAIR ||
               role == MembershipRole.RANKING_MEMBER;
    }

    /**
     * Check if this membership is currently active (no end date)
     */
    public boolean isActive() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }

    /**
     * Get member's BioGuide ID (convenience method)
     */
    public String getMemberBioguideId() {
        return congressionalMember != null ? congressionalMember.getBioguideId() : null;
    }

    /**
     * Get committee code (convenience method)
     */
    public String getCommitteeCode() {
        return committee != null ? committee.getCommitteeCode() : null;
    }
}
