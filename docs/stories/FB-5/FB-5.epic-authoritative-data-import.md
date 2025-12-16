# Epic FB-5: Authoritative Data Import

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-5 |
| **Epic Name** | Authoritative Data Import |
| **Epic Type** | Feature / Data Integration |
| **Priority** | HIGH |
| **Status** | **Superseded by ADMIN-1** |
| **Created** | 2025-12-02 |
| **Updated** | 2025-12-12 |
| **Owner** | Sarah (PO) |
| **Depends On** | FB-4 (Admin Dashboard Redesign) |

> **SUPERSEDED (2025-12-12):** This draft epic was never formally approved. All planned functionality was implemented through the **ADMIN-1** story set (stories 1.4, 1.5, 1.10, 1.11, 1.13). See [ADMIN-1 Epic](../ADMIN-1/ADMIN-1.epic-admin-dashboard-improvements.md) for completion details.

## Executive Summary

Enable import of authoritative government data sources that aren't available via existing APIs. The primary focus is the U.S. Government Manual (GOVMAN) XML which provides comprehensive data on all three federal branches, their organizational structure, mission statements, and leadership. This completes the factbase with legislative and judicial branch data that the Federal Register API doesn't provide.

## Business Value

### Why This Epic Matters

1. **Complete Federal Coverage** - All 3 branches (not just Executive)
2. **Official Source of Truth** - GOVMAN is the authoritative organizational reference
3. **Rich Metadata** - Mission statements, hierarchies, websites, programs
4. **Historical Context** - Organizational history and legislative authority

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| GOVMAN Import Coverage | 100% | All entities parsed successfully |
| Branch Completeness | 3/3 | Executive, Legislative, Judicial |
| Hierarchy Accuracy | 100% | Parent-child relationships preserved |
| Import Time | <5 minutes | Full GOVMAN import duration |

## Scope

### In Scope

#### Phase 1: GOVMAN XML Import (P0)
- Parse GOVMAN XML structure (`data/GOVINFO/GOVMAN-2025-01-13.xml`)
- Map to existing `GovernmentOrganization` model
- Handle all entity types:
  - Legislative Branch (Congress, House, Senate, Committees, GAO, CBO, etc.)
  - Executive Branch (Departments, Agencies, Commissions)
  - Judicial Branch (Supreme Court, Circuit Courts, District Courts)
- Preserve hierarchical relationships (ParentId)
- Extract: AgencyName, Category (branch), MissionStatement, WebAddress
- Admin UI for file upload and import status

#### Phase 2: US Code Import (P1) - Future
- Research US Code data format
- Create parser for legal code structure
- Link to agencies/regulations

### Out of Scope

- Real-time GOVMAN sync (manual import sufficient)
- Leadership/personnel data from GOVMAN (use Congress.gov/PLUM)
- State/local government data

## Architecture

### GOVMAN XML Structure Analysis

```xml
<GovernmentManual>
  <Entity EntityId="70" ParentId="0" SortOrder="1">
    <EntityType>Parent</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>Congress</AgencyName>
    <MissionStatement>
      <Record><Paragraph>...</Paragraph></Record>
    </MissionStatement>
    <Addresses>
      <Address>
        <FooterDetails>
          <WebAddress>http://www.congress.gov</WebAddress>
        </FooterDetails>
      </Address>
    </Addresses>
    <LeaderShipTables/>
    <ProgramAndActivities/>
  </Entity>
</GovernmentManual>
```

### Field Mapping

| GOVMAN Field | GovernmentOrganization Field | Notes |
|--------------|------------------------------|-------|
| `EntityId` | (internal tracking) | For parent resolution |
| `ParentId` | `parentId` | Hierarchical relationship |
| `Category` | `branch` | Map to EXECUTIVE/LEGISLATIVE/JUDICIAL |
| `AgencyName` | `officialName` | Primary name |
| `EntityType` | `orgType` | Map to existing enum |
| `MissionStatement` | `missionStatement` | Concatenate paragraphs |
| `WebAddress` | `websiteUrl` | Primary website |

### Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                    Admin Dashboard                           │
│               File Upload (GOVMAN XML)                       │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  GovManXmlImportService                      │
│  - Parse XML with JAXB/Jackson                              │
│  - Build entity map (EntityId → parsed record)              │
│  - Resolve ParentId relationships                           │
│  - Map Category → Branch enum                               │
│  - Merge with existing organizations                        │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              GovernmentOrganizationRepository                │
│                     PostgreSQL                               │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology |
|-----------|------------|
| XML Parser | Jackson XML (`jackson-dataformat-xml`) |
| Backend Service | `GovManXmlImportService` |
| Controller | `AdminSyncController` (extend) |
| Frontend | File upload component + progress indicator |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-5.1 | GOVMAN XML Parser Service | P0 | 1 sprint | None |
| FB-5.2 | GOVMAN Import API Endpoint | P0 | 0.5 sprint | FB-5.1 |
| FB-5.3 | Admin UI for GOVMAN Import | P0 | 0.5 sprint | FB-5.2, FB-4.3 |
| FB-5.4 | Branch & Hierarchy Validation | P0 | 0.5 sprint | FB-5.1 |
| FB-5.5 | US Code Research Spike | P1 | 0.25 sprint | None |

### Dependency Graph

```
FB-5.1 (XML Parser)
    │
    ├──────────────────┐
    ▼                  ▼
FB-5.2 (API)      FB-5.4 (Validation)
    │
    ▼
FB-5.3 (Admin UI)      FB-5.5 (US Code Spike)
```

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/admin/import/govman` | POST | Upload and import GOVMAN XML |
| `/admin/import/govman/status` | GET | Get import progress/status |
| `/admin/import/govman/preview` | POST | Preview import without committing |

## Acceptance Criteria (Epic Level)

1. **Full Parse**: All GOVMAN entities parsed without errors
2. **Branch Mapping**: Legislative, Executive, Judicial correctly categorized
3. **Hierarchy**: Parent-child relationships accurately preserved
4. **Merge Strategy**: Existing organizations updated, not duplicated
5. **Mission Statements**: Extracted and stored
6. **Admin UI**: File upload with progress indicator and results summary
7. **Preview Mode**: Admin can preview import before committing
8. **Audit Trail**: Import source and timestamp tracked

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| XML structure variations | Medium | Medium | Analyze multiple GOVMAN versions |
| Entity matching failures | Medium | Low | Fuzzy matching + manual override |
| Large file performance | Low | Low | Streaming parser if needed |
| Encoding issues | Low | Medium | Force UTF-8, handle special chars |

## Definition of Done

- [ ] All 4 core stories completed and merged
- [ ] GOVMAN XML fully importable
- [ ] All 3 branches populated in database
- [ ] Hierarchy relationships accurate
- [ ] Admin UI functional with progress display
- [ ] Preview mode available
- [ ] Error handling for malformed XML
- [ ] Import audit trail in place

## Sample GOVMAN Data Points

From `GOVMAN-2025-01-13.xml`:
- **Congress** (EntityId=70, ParentId=0, Category=Legislative Branch)
- **House of Representatives** (child of Congress)
- **Senate** (child of Congress)
- **Executive Departments** (Category=Executive Branch)
- **Federal Courts** (Category=Judicial Branch)

## Related Documentation

- [Project Brief](../analysis/ADMIN_DASHBOARD_IMPROVEMENTS_PROJECT_BRIEF.md)
- [GOVMAN XML File](../../data/GOVINFO/GOVMAN-2025-01-13.xml)
- [GovOrgCsvImportService](../../backend/src/main/java/org/newsanalyzer/service/GovOrgCsvImportService.java) - Reference pattern

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-02 | 1.0 | Initial epic creation | Mary (Analyst) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | _Pending_ | _Pending_ | _Pending_ |
| Architect | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
