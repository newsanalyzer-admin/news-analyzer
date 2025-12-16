package org.newsanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite primary key for RegulationAgency join table.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegulationAgencyId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID regulationId;
    private UUID organizationId;
}
