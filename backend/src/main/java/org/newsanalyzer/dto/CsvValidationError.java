package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single validation error from CSV import.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvValidationError {
    private int line;
    private String field;
    private String value;
    private String message;
}
