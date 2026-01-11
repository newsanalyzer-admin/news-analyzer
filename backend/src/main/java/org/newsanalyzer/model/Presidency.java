package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Entity representing a Presidential term (presidency).
 *
 * Separates the office/term from the individual person to properly handle
 * non-consecutive terms (e.g., Cleveland 22nd/24th, Trump 45th/47th).
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "presidencies",
        indexes = {
                @Index(name = "idx_presidency_individual", columnList = "individual_id"),
                @Index(name = "idx_presidency_number", columnList = "number"),
                @Index(name = "idx_presidency_dates", columnList = "start_date, end_date")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_presidency_number", columnNames = "number")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Presidency {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Relationships
    // =====================================================================

    @Column(name = "individual_id", nullable = false)
    @NotNull(message = "Individual ID is required")
    private UUID individualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id", insertable = false, updatable = false)
    @JsonIgnore
    private Individual individual;

    /**
     * Reference to the previous presidency (null for #1 Washington).
     */
    @Column(name = "predecessor_id")
    private UUID predecessorId;

    /**
     * Reference to the next presidency (null if current).
     */
    @Column(name = "successor_id")
    private UUID successorId;

    // =====================================================================
    // Presidency Information
    // =====================================================================

    /**
     * Presidency number (1-47). Unique identifier for historical ordering.
     */
    @Column(name = "number", nullable = false, unique = true)
    @NotNull(message = "Presidency number is required")
    @Min(value = 1, message = "Presidency number must be at least 1")
    private Integer number;

    /**
     * Political party at time of presidency.
     */
    @Column(name = "party", length = 100)
    @Size(max = 100, message = "Party must be less than 100 characters")
    private String party;

    /**
     * Year elected (null if succeeded to office without election, e.g., Ford).
     */
    @Column(name = "election_year")
    private Integer electionYear;

    // =====================================================================
    // Temporal Information
    // =====================================================================

    /**
     * Inauguration date (start of term).
     */
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * End of term (null if current president).
     */
    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * How the presidency ended.
     */
    @Column(name = "end_reason", length = 20)
    @Enumerated(EnumType.STRING)
    private PresidencyEndReason endReason;

    // =====================================================================
    // Data Source Tracking
    // =====================================================================

    @Column(name = "data_source", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Data source is required")
    private DataSource dataSource;

    @Column(name = "source_reference", length = 200)
    @Size(max = 200, message = "Source reference must be less than 200 characters")
    private String sourceReference;

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
        if (dataSource == null) {
            dataSource = DataSource.WHITE_HOUSE_HISTORICAL;
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
     * Check if this is the current presidency (no end date).
     */
    public boolean isCurrent() {
        return endDate == null;
    }

    /**
     * Get term duration in days (null if current).
     */
    public Long getTermDays() {
        if (endDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Get term label (e.g., "1789-1797" or "2021-present").
     */
    public String getTermLabel() {
        if (endDate != null) {
            return String.format("%d-%d", startDate.getYear(), endDate.getYear());
        } else {
            return String.format("%d-present", startDate.getYear());
        }
    }

    /**
     * Get ordinal label (e.g., "1st", "22nd", "47th").
     */
    public String getOrdinalLabel() {
        return number + getOrdinalSuffix(number);
    }

    private String getOrdinalSuffix(int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1: return "st";
            case 2: return "nd";
            case 3: return "rd";
            default: return "th";
        }
    }
}
