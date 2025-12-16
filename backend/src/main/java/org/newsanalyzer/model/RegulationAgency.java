package org.newsanalyzer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

/**
 * Join table entity for many-to-many relationship between
 * Regulation and GovernmentOrganization.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@jakarta.persistence.Entity
@Table(name = "regulation_agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RegulationAgencyId.class)
public class RegulationAgency {

    @Id
    @Column(name = "regulation_id")
    private UUID regulationId;

    @Id
    @Column(name = "organization_id")
    private UUID organizationId;

    /**
     * Original agency name from Federal Register API.
     * Stored for reference even if matching fails.
     */
    @Column(name = "agency_name_raw", length = 255)
    private String agencyNameRaw;

    /**
     * Indicates if this is the primary agency for the regulation.
     * The first agency listed is typically the primary.
     */
    @Column(name = "is_primary_agency")
    private boolean primaryAgency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regulation_id", insertable = false, updatable = false)
    @JsonIgnore
    private Regulation regulation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    @JsonIgnore
    private GovernmentOrganization organization;
}
