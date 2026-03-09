package org.newsanalyzer.apitests.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO matching backend GovernmentOrgSyncService.SyncStatus response.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SyncStatusDto {
    private LocalDateTime lastSync;
    private long totalOrganizations;
    private Map<String, Long> countByBranch;
    private boolean federalRegisterAvailable;
    private int maxNewOrgs;

    public LocalDateTime getLastSync() { return lastSync; }
    public void setLastSync(LocalDateTime lastSync) { this.lastSync = lastSync; }

    public long getTotalOrganizations() { return totalOrganizations; }
    public void setTotalOrganizations(long totalOrganizations) { this.totalOrganizations = totalOrganizations; }

    public Map<String, Long> getCountByBranch() { return countByBranch; }
    public void setCountByBranch(Map<String, Long> countByBranch) { this.countByBranch = countByBranch; }

    public boolean isFederalRegisterAvailable() { return federalRegisterAvailable; }
    public void setFederalRegisterAvailable(boolean federalRegisterAvailable) { this.federalRegisterAvailable = federalRegisterAvailable; }

    public int getMaxNewOrgs() { return maxNewOrgs; }
    public void setMaxNewOrgs(int maxNewOrgs) { this.maxNewOrgs = maxNewOrgs; }

    @Override
    public String toString() {
        return String.format("SyncStatusDto{totalOrganizations=%d, federalRegisterAvailable=%s, lastSync=%s}",
                totalOrganizations, federalRegisterAvailable, lastSync);
    }
}
