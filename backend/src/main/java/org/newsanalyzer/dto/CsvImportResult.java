package org.newsanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of a CSV import operation.
 *
 * Contains counts of added, updated, skipped, and error records,
 * plus detailed validation errors when applicable.
 *
 * @author James (Dev Agent)
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CsvImportResult {
    private boolean success;
    private int added;
    private int updated;
    private int skipped;
    private int errors;

    @Builder.Default
    private List<CsvValidationError> validationErrors = new ArrayList<>();

    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();

    public void addValidationError(int line, String field, String value, String message) {
        this.errors++;
        this.validationErrors.add(CsvValidationError.builder()
                .line(line)
                .field(field)
                .value(value)
                .message(message)
                .build());
    }

    public void addError(String message) {
        this.errors++;
        this.errorMessages.add(message);
    }

    public int getTotal() {
        return added + updated + skipped + errors;
    }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("CsvImportResult{success=%s, added=%d, updated=%d, skipped=%d, errors=%d}",
                success, added, updated, skipped, errors);
    }
}
