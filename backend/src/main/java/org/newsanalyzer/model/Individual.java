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
 * Entity representing an Individual - the master record for any person in the system.
 *
 * This provides a single source of truth for biographical data, regardless of
 * the person's roles (Congressional member, President, Judge, etc.).
 *
 * Part of ARCH-1 refactor: Separates universal person data from role-specific data.
 *
 * @author Claude (Dev Agent)
 * @since 3.0.0
 * @see CongressionalMember for Congress-specific data
 */
@jakarta.persistence.Entity
@Table(name = "individuals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Individual {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

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
    // Political Affiliation (MOD-2)
    // =====================================================================

    @Column(name = "party", length = 50)
    @Size(max = 50, message = "Party must be less than 50 characters")
    private String party;

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
    // Data Source Tracking (MOD-3)
    // =====================================================================

    @Column(name = "primary_data_source", length = 50)
    @Enumerated(EnumType.STRING)
    private DataSource primaryDataSource;

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
     * Check if person is living (no death date)
     */
    public boolean isLiving() {
        return deathDate == null;
    }
}
