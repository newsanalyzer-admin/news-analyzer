# Government Organization Sync Service Architecture

## Overview

This document outlines the architecture for populating and maintaining government organization data in NewsAnalyzer. Since the GovInfo GOVMAN collection is not available via API, we use a multi-source approach combining the Federal Register API with curated seed data.

## Data Sources

### Primary Source: Federal Register API

**URL:** `https://www.federalregister.gov/api/v1/agencies`

**Why Federal Register:**
- RESTful JSON API (no scraping needed)
- Authoritative federal source
- Includes agencies that publish federal regulations
- Free, no authentication required
- Well-documented

**Limitations:**
- Only includes agencies that publish in Federal Register
- Missing some independent agencies and all judicial/legislative orgs
- No hierarchical relationships provided

### Secondary Source: Seed Data (SQL Migration)

The existing V3 migration seeds 20 executive branch organizations:
- 15 Cabinet departments
- 5 Major independent agencies (EPA, NASA, CIA, FBI, SSA)

### Tertiary Source: Manual Enrichment

For Legislative and Judicial branches, manual entry from:
- congress.gov (Legislative)
- uscourts.gov (Judicial)
- Wikipedia cross-reference

## Architecture Design

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Data Sources                                     │
├──────────────────────┬──────────────────────┬──────────────────────────┤
│ Federal Register API │   Seed Data (SQL)    │  Manual CSV Import       │
│   (JSON REST)        │   (V3 Migration)     │  (admin upload)          │
└──────────┬───────────┴──────────┬───────────┴────────────┬─────────────┘
           │                      │                        │
           ▼                      ▼                        ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                    GovernmentOrgSyncService                              │
│  ┌─────────────────────┐  ┌────────────────────┐  ┌─────────────────┐   │
│  │ FederalRegisterClient│  │ SeedDataInitializer │  │ CsvImportService│   │
│  │ - fetchAllAgencies() │  │ - loadFromMigration │  │ - parseAndImport│   │
│  │ - fetchAgency(slug) │  │                     │  │                 │   │
│  └──────────┬──────────┘  └─────────┬──────────┘  └────────┬────────┘   │
│             │                       │                      │            │
│             └───────────────────────┼──────────────────────┘            │
│                                     ▼                                    │
│                        ┌────────────────────────┐                        │
│                        │   MergeStrategy        │                        │
│                        │  - matchByName/Acronym │                        │
│                        │  - updateOrCreate      │                        │
│                        │  - preserveHierarchy   │                        │
│                        └────────────┬───────────┘                        │
│                                     │                                    │
└─────────────────────────────────────┼────────────────────────────────────┘
                                      │
                                      ▼
                    ┌─────────────────────────────────────┐
                    │ GovernmentOrganizationRepository    │
                    │       (PostgreSQL)                  │
                    └─────────────────────────────────────┘
```

## Component Design

### 1. FederalRegisterClient

**Location:** `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java`

```java
@Service
public class FederalRegisterClient {

    private static final String BASE_URL = "https://www.federalregister.gov/api/v1";

    /**
     * Fetch all agencies from Federal Register API
     * Returns list of agency objects with:
     * - id (Integer)
     * - name (String)
     * - short_name (String) - acronym
     * - url (String)
     * - parent_id (Integer, nullable)
     * - description (String)
     */
    public List<FederalRegisterAgency> fetchAllAgencies();

    /**
     * Fetch single agency by slug
     */
    public Optional<FederalRegisterAgency> fetchAgency(String slug);

    /**
     * Health check for API availability
     */
    public boolean isApiAvailable();
}
```

**API Response Example:**
```json
{
  "agencies": [
    {
      "id": 1,
      "name": "Department of Agriculture",
      "short_name": "USDA",
      "url": "https://www.federalregister.gov/agencies/agriculture-department",
      "parent_id": null,
      "description": "The Department of Agriculture..."
    }
  ]
}
```

### 2. GovernmentOrgSyncService

**Location:** `backend/src/main/java/org/newsanalyzer/service/GovernmentOrgSyncService.java`

```java
@Service
@Transactional
public class GovernmentOrgSyncService {

    private final FederalRegisterClient federalRegisterClient;
    private final GovernmentOrganizationRepository repository;

    /**
     * Full sync from Federal Register API
     * - Fetches all agencies
     * - Matches against existing records by name/acronym
     * - Creates new or updates existing
     * - Does NOT delete (soft-delete handled separately)
     */
    public SyncResult syncFromFederalRegister();

    /**
     * Import organizations from CSV file
     * Used for manual data entry of Legislative/Judicial branches
     */
    public SyncResult importFromCsv(InputStream csvStream);

    /**
     * Get sync status
     */
    public SyncStatus getStatus();

    public static class SyncResult {
        private int added;
        private int updated;
        private int skipped;
        private int errors;
        private List<String> errorMessages;
    }

    public static class SyncStatus {
        private LocalDateTime lastSync;
        private int totalOrganizations;
        private Map<String, Integer> countByBranch;
        private boolean federalRegisterAvailable;
    }
}
```

### 3. FederalRegisterConfig

**Location:** `backend/src/main/java/org/newsanalyzer/config/FederalRegisterConfig.java`

```java
@Configuration
@ConfigurationProperties(prefix = "federal-register")
public class FederalRegisterConfig {
    private String baseUrl = "https://www.federalregister.gov/api/v1";
    private int timeout = 30000;
    private int retryAttempts = 3;
}
```

**application.yml:**
```yaml
federal-register:
  base-url: https://www.federalregister.gov/api/v1
  timeout: 30000
  retry-attempts: 3
```

### 4. Controller Endpoints

**Location:** `backend/src/main/java/org/newsanalyzer/controller/GovernmentOrganizationController.java`

Add to existing controller:

```java
// Admin sync endpoints
@PostMapping("/sync/federal-register")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<SyncResult> syncFromFederalRegister();

@PostMapping("/import/csv")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<SyncResult> importFromCsv(@RequestParam MultipartFile file);

@GetMapping("/sync/status")
public ResponseEntity<SyncStatus> getSyncStatus();
```

## Data Mapping Strategy

### Federal Register → GovernmentOrganization

| Federal Register Field | GovernmentOrganization Field | Notes |
|----------------------|---------------------------|-------|
| `name` | `officialName` | Direct mapping |
| `short_name` | `acronym` | Direct mapping |
| `url` | `metadata.federalRegisterUrl` | Store in metadata |
| `parent_id` | `parentId` | Requires lookup by FR ID |
| `description` | `description` | Direct mapping |
| - | `branch` | Default EXECUTIVE |
| - | `orgType` | Infer from name patterns |
| - | `websiteUrl` | Derive from `url` or lookup |

### Type Inference Logic

```java
private OrganizationType inferOrgType(String name, String shortName) {
    String lower = name.toLowerCase();

    if (lower.startsWith("department of")) {
        return OrganizationType.DEPARTMENT;
    }
    if (lower.contains("agency") || lower.contains("administration")) {
        return OrganizationType.INDEPENDENT_AGENCY;
    }
    if (lower.contains("bureau")) {
        return OrganizationType.BUREAU;
    }
    if (lower.contains("office")) {
        return OrganizationType.OFFICE;
    }
    if (lower.contains("commission")) {
        return OrganizationType.COMMISSION;
    }
    if (lower.contains("board")) {
        return OrganizationType.BOARD;
    }

    // Default for executive agencies
    return OrganizationType.INDEPENDENT_AGENCY;
}
```

## Merge Strategy

When syncing, use this priority:

1. **Match by acronym** (most reliable)
2. **Match by official name** (fuzzy match)
3. **Create new** if no match

**Never overwrite:**
- `id` (database primary key)
- `parentId` (manually curated hierarchy)
- `branch` (manually assigned)
- `jurisdictionAreas` (manually curated)
- `missionStatement` (manually curated)

**Always update from Federal Register:**
- `description` (if currently null)
- `metadata.federalRegisterUrl`
- `metadata.federalRegisterId`

## CSV Import Format

For manual import of Legislative/Judicial organizations:

```csv
officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,websiteUrl,jurisdictionAreas
"United States Senate",Senate,legislative,branch,1,,1789-03-04,https://senate.gov,"legislation;confirmation"
"United States House of Representatives",House,legislative,branch,1,,1789-03-04,https://house.gov,"legislation;appropriations"
"Supreme Court of the United States",SCOTUS,judicial,branch,1,,1789-03-04,https://supremecourt.gov,"constitutional law;appeals"
```

## Scheduler Configuration

Weekly sync (disabled by default, enabled manually or via admin):

```java
@Component
public class GovernmentOrgScheduler {

    @Scheduled(cron = "${gov-org.sync.schedule:0 0 5 * * SUN}")
    @ConditionalOnProperty(name = "gov-org.sync.enabled", havingValue = "true")
    public void weeklySync() {
        governmentOrgSyncService.syncFromFederalRegister();
    }
}
```

## Implementation Phases

### Phase 1: Federal Register Integration (MVP)
1. Create `FederalRegisterClient`
2. Create `GovernmentOrgSyncService`
3. Add sync controller endpoints
4. Manual trigger from admin dashboard

### Phase 2: Scheduled Sync
1. Add scheduler configuration
2. Add last-sync tracking
3. Add sync history logging

### Phase 3: CSV Import
1. Add CSV parser
2. Add validation
3. Add admin UI upload

### Phase 4: Legislative/Judicial Data
1. Research congress.gov structure
2. Research uscourts.gov structure
3. Manual CSV preparation
4. Import and verify

## Estimated Counts

| Branch | Source | Est. Count |
|--------|--------|-----------|
| Executive | Federal Register API | ~300 agencies |
| Executive | Seed data | 20 (Cabinet + major) |
| Legislative | Manual CSV | ~50 (Congress, committees, etc.) |
| Judicial | Manual CSV | ~20 (Courts) |
| **Total** | Combined | ~400 organizations |

## Frontend Integration

The existing admin dashboard (`/admin`) can be extended with:

1. **Government Org Sync Status Card** - shows last sync, count by branch
2. **Sync Button** - triggers Federal Register sync
3. **CSV Import** - file upload for manual data

## Error Handling

| Scenario | Behavior |
|----------|----------|
| Federal Register API unavailable | Log warning, skip sync, return status |
| Duplicate organization detected | Update existing, log merge |
| Invalid data from API | Skip record, log error, continue |
| CSV parse error | Return detailed error message |

## Monitoring & Logging

Log events:
- Sync started/completed with stats
- Individual organization created/updated
- API errors with response details
- Merge conflicts resolved

Metrics:
- `gov_org_sync_duration_seconds`
- `gov_org_sync_records_total`
- `gov_org_api_errors_total`
