package org.newsanalyzer.apitests.backend.dto;

import java.util.List;

/**
 * DTO matching backend GovernmentOrgSyncService.SyncResult response.
 */
public class SyncResultDto {
    private int added;
    private int updated;
    private int skipped;
    private int errors;
    private int total;
    private List<String> errorMessages;

    public int getAdded() { return added; }
    public void setAdded(int added) { this.added = added; }

    public int getUpdated() { return updated; }
    public void setUpdated(int updated) { this.updated = updated; }

    public int getSkipped() { return skipped; }
    public void setSkipped(int skipped) { this.skipped = skipped; }

    public int getErrors() { return errors; }
    public void setErrors(int errors) { this.errors = errors; }

    public List<String> getErrorMessages() { return errorMessages; }
    public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    @Override
    public String toString() {
        return String.format("SyncResultDto{added=%d, updated=%d, skipped=%d, errors=%d}",
                added, updated, skipped, errors);
    }
}
