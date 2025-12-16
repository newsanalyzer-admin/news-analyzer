package org.newsanalyzer.apitests.backend.dto;

import java.util.List;

/**
 * DTO matching backend CsvImportResult response.
 */
public class CsvImportResultDto {
    private boolean success;
    private int added;
    private int updated;
    private int skipped;
    private int errors;
    private int total;
    private List<CsvValidationErrorDto> validationErrors;
    private List<String> errorMessages;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public int getAdded() { return added; }
    public void setAdded(int added) { this.added = added; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }

    public List<CsvValidationErrorDto> getValidationErrors() { return validationErrors; }
    public void setValidationErrors(List<CsvValidationErrorDto> validationErrors) { this.validationErrors = validationErrors; }

    public List<String> getErrorMessages() { return errorMessages; }
    public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public boolean hasValidationErrors() {
        return validationErrors != null && !validationErrors.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("CsvImportResultDto{success=%s, added=%d, updated=%d, skipped=%d, errors=%d}",
                success, added, updated, skipped, errors);
    }
}
