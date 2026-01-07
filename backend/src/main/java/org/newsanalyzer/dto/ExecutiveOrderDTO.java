package org.newsanalyzer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.newsanalyzer.model.ExecutiveOrder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object for Executive Order API responses.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveOrderDTO {

    private UUID id;
    private UUID presidencyId;
    private Integer eoNumber;
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate signingDate;

    private String summary;
    private String federalRegisterCitation;
    private String federalRegisterUrl;
    private String status;
    private Integer revokedByEo;

    // =====================================================================
    // Factory Methods
    // =====================================================================

    /**
     * Create ExecutiveOrderDTO from ExecutiveOrder entity.
     *
     * @param eo the executive order entity
     * @return the DTO
     */
    public static ExecutiveOrderDTO from(ExecutiveOrder eo) {
        if (eo == null) return null;

        return ExecutiveOrderDTO.builder()
                .id(eo.getId())
                .presidencyId(eo.getPresidencyId())
                .eoNumber(eo.getEoNumber())
                .title(eo.getTitle())
                .signingDate(eo.getSigningDate())
                .summary(eo.getSummary())
                .federalRegisterCitation(eo.getFederalRegisterCitation())
                .federalRegisterUrl(eo.getFederalRegisterUrl())
                .status(eo.getStatus() != null ? eo.getStatus().name() : null)
                .revokedByEo(eo.getRevokedByEo())
                .build();
    }
}
