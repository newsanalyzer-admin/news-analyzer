# Epic FB-2-GOV: Government Organization Sync Service - Brownfield Enhancement

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-2-GOV |
| **Epic Name** | Government Organization Sync Service |
| **Epic Type** | Brownfield Enhancement / Data Integration |
| **Priority** | HIGH |
| **Status** | Done |
| **Created** | 2025-11-30 |
| **Owner** | Sarah (PO) |
| **Phase** | Phase 2 (Prerequisite for FB-2) |

## Epic Goal

Implement automated synchronization of US government organization data from the Federal Register API into NewsAnalyzer's factbase, enabling the Government Organizations UI to display ~300+ executive branch agencies and providing the foundation for FB-2 executive appointee linkage.

## Epic Description

**Existing System Context:**

- Current relevant functionality: GovernmentOrganization entity exists with 20 seeded organizations (V3 migration)
- Technology stack: Spring Boot, PostgreSQL, React/Next.js with existing sync service patterns
- Integration points: GovernmentOrganizationRepository, Admin dashboard (/admin), existing SyncButton pattern

**Enhancement Details:**

- What's being added: FederalRegisterClient API integration, GovernmentOrgSyncService, CSV import for Legislative/Judicial branches
- How it integrates: Follows existing *SyncService pattern (MemberSyncService, CommitteeSyncService), extends admin dashboard
- Success criteria:
  1. Government Organizations page displays 300+ organizations
  2. Admin can trigger Federal Register sync
  3. Admin can import CSV for Legislative/Judicial branches
  4. Weekly scheduled sync runs automatically (disabled by default)

**Architecture Reference:** [docs/architecture/government-org-sync-design.md](../architecture/government-org-sync-design.md)

## Stories

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-2-GOV.1 | Federal Register API Integration | P0 | 1 sprint | None |
| FB-2-GOV.2 | Admin Dashboard Gov Org Sync UI | P0 | 0.5 sprint | FB-2-GOV.1, FB-1-UI.5 |
| FB-2-GOV.3 | CSV Import for Legislative/Judicial Branches | P1 | 0.5 sprint | FB-2-GOV.1 |
| FB-2-GOV.4 | Gov Org Sync API Integration Tests | P0 | 0.5 sprint | FB-2-GOV.1 |

### Story Details

#### FB-2-GOV.1: Federal Register API Integration

**Scope:**
- Create `FederalRegisterClient` service to fetch agencies from Federal Register API
- Create `FederalRegisterConfig` for configuration properties
- Create `GovernmentOrgSyncService` with merge strategy (match by acronym/name)
- Add sync controller endpoints to GovernmentOrganizationController:
  - `POST /api/government-organizations/sync/federal-register`
  - `GET /api/government-organizations/sync/status`
- Implement weekly scheduler (disabled by default via config)
- Follow existing sync service patterns from MemberSyncService

**Key Files:**
- `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java`
- `backend/src/main/java/org/newsanalyzer/service/GovernmentOrgSyncService.java`
- `backend/src/main/java/org/newsanalyzer/config/FederalRegisterConfig.java`
- `backend/src/main/java/org/newsanalyzer/scheduler/GovernmentOrgScheduler.java`

#### FB-2-GOV.2: Admin Dashboard Gov Org Sync UI

**Scope:**
- Add Government Org sync status card to admin dashboard
- Add "Sync Government Orgs" button with confirmation dialog
- Display sync results (added/updated/errors)
- Follow existing SyncButton component pattern from FB-1-UI.5

**Key Files:**
- `frontend/src/app/admin/page.tsx` (extend)
- `frontend/src/hooks/useGovernmentOrgs.ts` (add sync hooks)

#### FB-2-GOV.3: CSV Import for Legislative/Judicial Branches

**Scope:**
- Add CSV parser for manual organization import
- Add file upload endpoint with validation:
  - `POST /api/government-organizations/import/csv`
- Add admin UI for CSV upload
- Include sample CSV format documentation
- Support Legislative and Judicial branch organizations

**Key Files:**
- `backend/src/main/java/org/newsanalyzer/service/GovOrgCsvImportService.java`
- `frontend/src/components/admin/CsvImportButton.tsx`

#### FB-2-GOV.4: Gov Org Sync API Integration Tests

**Scope:**
- Add `GovOrgSyncApiClient.java` for sync endpoint calls
- Add `GovOrgSyncTest.java` testing:
  - `POST /api/government-organizations/sync/federal-register` triggers sync
  - `GET /api/government-organizations/sync/status` returns valid status
  - Sync results contain expected fields (added, updated, skipped, errors)
- Add `GovOrgCsvImportTest.java` testing:
  - `POST /api/government-organizations/import/csv` accepts valid CSV
  - Validation errors returned for malformed CSV
  - Legislative/Judicial branches imported correctly
- Mock Federal Register API using WireMock for deterministic testing
- Follow existing test patterns (GovOrgCrudTest, BaseApiTest)

**Key Files:**
- `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgSyncApiClient.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgSyncTest.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgCsvImportTest.java`

## Dependency Graph

```
FB-1-UI.5 (Admin Dashboard) ─────────────────┐
                                              │
                                              ▼
FB-2-GOV.1 (Federal Register Integration) ───┬───► FB-2-GOV.2 (Admin UI)
                                              │
                                              ├───► FB-2-GOV.3 (CSV Import)
                                              │
                                              └───► FB-2-GOV.4 (API Tests)

                                              │
                                              ▼
                                         FB-2 (PLUM Appointees)
                                         [Prerequisite satisfied]
```

## Compatibility Requirements

- [x] Existing APIs remain unchanged (extends GovernmentOrganizationController)
- [x] Database schema changes are backward compatible (no schema changes needed)
- [x] UI changes follow existing patterns (SyncButton, SyncStatusCard)
- [x] Performance impact is minimal (async sync, weekly schedule)

## Risk Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Federal Register API structure changes | Medium | Low | Modular client design, health check |
| Federal Register API unavailable | Medium | Low | Graceful fallback to seed data |
| Large data volume (~300 agencies) | Low | Medium | Batch processing, progress tracking |
| CSV validation edge cases | Low | Medium | Comprehensive validation, clear error messages |

**Rollback Plan:** Sync service can be disabled via configuration (`gov-org.sync.enabled=false`); no destructive operations (update/insert only, never delete)

## Definition of Done

- [x] All 4 stories completed with acceptance criteria met
- [x] Existing GovernmentOrganization functionality verified
- [x] Federal Register sync populates 300+ organizations
- [x] Admin dashboard displays sync controls
- [x] Government Organizations page shows hierarchical data
- [x] CSV import works for Legislative/Judicial branches
- [x] API integration tests pass in CI pipeline
- [x] No regression in existing features

## Data Sources

### Federal Register API (Primary)

| Attribute | Value |
|-----------|-------|
| **URL** | `https://www.federalregister.gov/api/v1/agencies` |
| **Auth** | None required |
| **Format** | JSON |
| **Coverage** | ~300 executive agencies |
| **Rate Limit** | None documented (be respectful) |

### CSV Import (Secondary)

For Legislative/Judicial branches not in Federal Register:

```csv
officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,websiteUrl,jurisdictionAreas
"United States Senate",Senate,legislative,branch,1,,1789-03-04,https://senate.gov,"legislation;confirmation"
"United States House of Representatives",House,legislative,branch,1,,1789-03-04,https://house.gov,"legislation;appropriations"
"Supreme Court of the United States",SCOTUS,judicial,branch,1,,1789-03-04,https://supremecourt.gov,"constitutional law;appeals"
```

## API Endpoints (New)

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/government-organizations/sync/federal-register` | POST | Trigger Federal Register sync | Admin |
| `/api/government-organizations/sync/status` | GET | Get sync status | Public |
| `/api/government-organizations/import/csv` | POST | Import CSV file | Admin |

## Related Documentation

- [Architecture Design](../architecture/government-org-sync-design.md)
- [FB-2 Epic (PLUM Appointees)](../FB-2/FB-2.epic-executive-branch-data.md)
- [FB-1-UI.5 Admin Dashboard](FB-1-UI.5.admin-sync-dashboard.md)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-30 | 1.0 | Initial epic creation | Sarah (PO) |
| 2025-11-30 | 1.1 | Epic status → Done: All 4 stories completed, DoD fulfilled | Sarah (PO) |

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Review Date:** 2025-11-30
**Status:** APPROVED with Minor Recommendations

### Recommendations Incorporated

1. **FB-2-GOV.1**: Add repository methods for case-insensitive acronym/name lookup
2. **FB-2-GOV.1**: Consider using Federal Register `parent_id` to establish hierarchy (second pass after initial sync)
3. **FB-2-GOV.1**: Add rate limiting (100ms between requests) as good API citizenship
4. **FB-2-GOV.3**: Support `parentId` by acronym OR UUID in CSV for user-friendliness
5. **FB-2-GOV.4**: Configure backend to use WireMock URL via test profile for proper integration test isolation

### Architecture Alignment

- Follows existing *SyncService patterns (MemberSyncService, CommitteeSyncService)
- No breaking changes to existing APIs
- Database schema compatibility maintained
- Frontend follows established component patterns
- Dependency chain validated

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | Sarah (PO) | 2025-11-30 | Drafted |
| Architect | Winston | 2025-11-30 | **APPROVED** |
| Tech Lead | _TBD_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
