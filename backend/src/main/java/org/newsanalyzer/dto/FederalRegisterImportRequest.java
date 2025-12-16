package org.newsanalyzer.dto;

import lombok.Data;

/**
 * Request DTO for importing a Federal Register document.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
public class FederalRegisterImportRequest {

    /** Federal Register document number to import (e.g., "2024-12345") */
    private String documentNumber;

    /** If true, overwrite existing record even if duplicate exists */
    private boolean forceOverwrite;
}
