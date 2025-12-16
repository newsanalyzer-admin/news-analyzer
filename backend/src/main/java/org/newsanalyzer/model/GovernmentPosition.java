package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a government position across all branches of government.
 *
 * For Legislative branch: Congressional seats (Senator from Vermont, Representative from CA-12)
 * For Executive branch: Appointed positions (Secretary of State, EPA Administrator)
 * For Judicial branch: Court positions (future)
 *
 * Used in conjunction with PositionHolding for temporal tracking of who held what position when.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "government_positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GovernmentPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Position Information (All Branches)
    // =====================================================================

    @Column(name = "title", nullable = false, length = 100)
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must be less than 100 characters")
    private String title;

    @Column(name = "branch", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Branch is required")
    private Branch branch;

    @Column(name = "position_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Position type is required")
    private PositionType positionType;

    // =====================================================================
    // Legislative Branch Fields (nullable for executive/judicial)
    // =====================================================================

    @Column(name = "chamber", length = 20)
    @Enumerated(EnumType.STRING)
    private Person.Chamber chamber;

    @Column(name = "state", length = 2)
    @Size(min = 2, max = 2, message = "State must be 2-letter code if provided")
    private String state;

    @Column(name = "district")
    private Integer district;

    @Column(name = "senate_class")
    @Min(value = 1, message = "Senate class must be 1, 2, or 3")
    @Max(value = 3, message = "Senate class must be 1, 2, or 3")
    private Integer senateClass;

    // =====================================================================
    // Executive Branch Fields (nullable for legislative/judicial)
    // =====================================================================

    @Column(name = "appointment_type", length = 10)
    @Enumerated(EnumType.STRING)
    private AppointmentType appointmentType;

    @Column(name = "pay_plan", length = 10)
    private String payPlan;

    @Column(name = "pay_grade", length = 10)
    private String payGrade;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    // =====================================================================
    // Organization Link
    // =====================================================================

    @Column(name = "organization_id")
    private UUID organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    @JsonIgnore
    private GovernmentOrganization organization;

    // =====================================================================
    // Descriptive Information
    // =====================================================================

    @Column(name = "description", length = 500)
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    // =====================================================================
    // Audit Fields
    // =====================================================================

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    // =====================================================================
    // Lifecycle Callbacks
    // =====================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // =====================================================================
    // Helper Methods
    // =====================================================================

    /**
     * Get display name for the position.
     * Legislative: "Senator from VT (Class 1)", "Representative from CA-12"
     * Executive: "Secretary of State" or position title
     */
    public String getDisplayName() {
        if (branch == Branch.LEGISLATIVE && chamber != null) {
            if (chamber == Person.Chamber.SENATE) {
                return String.format("Senator from %s (Class %d)", state, senateClass);
            } else {
                return String.format("Representative from %s-%d", state, district);
            }
        }
        // Executive or Judicial - just return the title
        return title;
    }

    /**
     * Get short identifier.
     * Legislative: "VT-Sen-1", "CA-12"
     * Executive: position title or abbreviated form
     */
    public String getShortIdentifier() {
        if (branch == Branch.LEGISLATIVE && chamber != null && state != null) {
            if (chamber == Person.Chamber.SENATE) {
                return String.format("%s-Sen-%d", state, senateClass);
            } else {
                return String.format("%s-%02d", state, district);
            }
        }
        // Executive or Judicial - return title (could be abbreviated in future)
        return title;
    }

    /**
     * Check if this is a Senate position
     */
    public boolean isSenatePosition() {
        return branch == Branch.LEGISLATIVE && chamber == Person.Chamber.SENATE;
    }

    /**
     * Check if this is a House position
     */
    public boolean isHousePosition() {
        return branch == Branch.LEGISLATIVE && chamber == Person.Chamber.HOUSE;
    }

    /**
     * Check if this is a legislative branch position
     */
    public boolean isLegislativePosition() {
        return branch == Branch.LEGISLATIVE;
    }

    /**
     * Check if this is an executive branch position
     */
    public boolean isExecutivePosition() {
        return branch == Branch.EXECUTIVE;
    }

    /**
     * Check if this is a judicial branch position
     */
    public boolean isJudicialPosition() {
        return branch == Branch.JUDICIAL;
    }

    /**
     * Check if this is a Senate-confirmed position (PAS)
     */
    public boolean isSenateConfirmed() {
        return appointmentType == AppointmentType.PAS;
    }
}
