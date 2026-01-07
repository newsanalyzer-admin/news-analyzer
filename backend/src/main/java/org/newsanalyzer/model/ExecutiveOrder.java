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
 * Entity representing a Presidential Executive Order.
 *
 * Stores metadata and summary (not full text) of Executive Orders,
 * linked to the Presidency under which they were signed.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 * @see <a href="https://www.federalregister.gov/developers/documentation/api/v1">Federal Register API</a>
 */
@jakarta.persistence.Entity
@Table(name = "executive_orders",
        indexes = {
                @Index(name = "idx_eo_presidency", columnList = "presidency_id"),
                @Index(name = "idx_eo_number", columnList = "eo_number"),
                @Index(name = "idx_eo_signing_date", columnList = "signing_date"),
                @Index(name = "idx_eo_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_eo_number", columnNames = "eo_number")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutiveOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // =====================================================================
    // Relationships
    // =====================================================================

    @Column(name = "presidency_id", nullable = false)
    @NotNull(message = "Presidency ID is required")
    private UUID presidencyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "presidency_id", insertable = false, updatable = false)
    @JsonIgnore
    private Presidency presidency;

    // =====================================================================
    // Executive Order Information
    // =====================================================================

    /**
     * Executive Order number (unique identifier).
     */
    @Column(name = "eo_number", nullable = false, unique = true)
    @NotNull(message = "Executive Order number is required")
    private Integer eoNumber;

    /**
     * Title of the Executive Order.
     */
    @Column(name = "title", nullable = false, length = 500)
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must be less than 500 characters")
    private String title;

    /**
     * Date the Executive Order was signed.
     */
    @Column(name = "signing_date", nullable = false)
    @NotNull(message = "Signing date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate signingDate;

    /**
     * Abstract/summary of the Executive Order (not full text).
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    // =====================================================================
    // Federal Register Information
    // =====================================================================

    /**
     * Federal Register citation (e.g., "89 FR 12345").
     */
    @Column(name = "federal_register_citation", length = 100)
    @Size(max = 100, message = "Federal Register citation must be less than 100 characters")
    private String federalRegisterCitation;

    /**
     * URL to the Federal Register document.
     */
    @Column(name = "federal_register_url", length = 500)
    @Size(max = 500, message = "Federal Register URL must be less than 500 characters")
    private String federalRegisterUrl;

    // =====================================================================
    // Status Information
    // =====================================================================

    /**
     * Current status of the Executive Order.
     */
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private ExecutiveOrderStatus status;

    /**
     * EO number that revoked this order (if applicable).
     */
    @Column(name = "revoked_by_eo")
    private Integer revokedByEo;

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
            dataSource = DataSource.FEDERAL_REGISTER;
        }
        if (status == null) {
            status = ExecutiveOrderStatus.ACTIVE;
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
     * Check if this Executive Order is currently active.
     */
    public boolean isActive() {
        return status == ExecutiveOrderStatus.ACTIVE;
    }

    /**
     * Check if this Executive Order has been revoked.
     */
    public boolean isRevoked() {
        return status == ExecutiveOrderStatus.REVOKED;
    }

    /**
     * Get display label (e.g., "EO 14067").
     */
    public String getDisplayLabel() {
        return "EO " + eoNumber;
    }
}
