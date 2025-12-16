# Epic FB-2: Expand Factbase with Executive Branch Data

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-2 |
| **Epic Name** | Expand Factbase with Executive Branch Data |
| **Epic Type** | Feature / Data Integration |
| **Priority** | HIGH |
| **Status** | Ready for Development (Architect Approved) |
| **Created** | 2024-11-27 |
| **Updated** | 2025-12-01 |
| **Owner** | Sarah (PO) |
| **Depends On** | FB-1 (Person entity) |

## Executive Summary

Integrate executive branch appointee data into NewsAnalyzer's factbase using OPM's publicly available PLUM CSV data files. This enables fact-checkers to verify claims about Cabinet members, agency heads, and other political appointees.

> **Technical Spike (2025-12-01):** Confirmed that OPM provides downloadable CSV files with 21,000+ position records. This eliminates the need for web scraping, significantly reducing complexity and risk.

## Business Value

### Why This Epic Matters

1. **Executive Branch Coverage** - Cabinet and agency leadership are frequently in the news
2. **Appointment Verification** - Verify claims like "Secretary X was appointed by President Y"
3. **Position Tracking** - Track who holds key leadership positions across agencies
4. **Complete Government Picture** - Combined with Congressional data, provides full federal leadership view

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Position Coverage | 8,000+ positions | Count of imported records |
| Data Import Success | 100% | Records parsed without errors |
| Agency Linkage | >90% | Auto-matched to GovernmentOrganization |
| Query Response Time | <500ms | API latency monitoring |

## Technical Spike Findings

> **Spike Date:** 2025-12-01

### Data Source Discovery

| Finding | Details |
|---------|---------|
| **robots.txt** | Allows access to PLUM pages |
| **CSV Available** | Direct download at known URL |
| **Data Size** | 21,313 records, 3.79 MB |
| **Format** | Well-structured CSV with 14 columns |

### CSV Structure

**URL:** `https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/plum-archive-biden-administration.csv`

| Column | Description | Example |
|--------|-------------|---------|
| AgencyName | Parent agency | DEPARTMENT OF STATE |
| OrganizationName | Sub-organization | BUREAU OF CONSULAR AFFAIRS |
| PositionTitle | Position name | ASSISTANT SECRETARY |
| PositionStatus | Filled/Vacant | Filled |
| AppointmentTypeDescription | PAS, PA, NA, etc. | PAS |
| ExpirationDate | Term expiration | 5/26/2027 |
| LevelGradePay | Pay grade | II |
| Location | Work location | Washington, DC |
| IncumbentFirstName | First name | JOHN |
| IncumbentLastName | Last name | DOE |
| PaymentPlanDescription | Pay plan | EX |
| Tenure | Tenure code | 5 |
| IncumbentBeginDate | Start date | 1/20/2021 |
| IncumbentVacateDate | End date (if vacated) | |

### Approach Change

| Original Plan | Revised Plan | Impact |
|---------------|--------------|--------|
| Web scraper (Jsoup/BeautifulSoup) | CSV HTTP download | -80% complexity |
| HTML parsing | CSV parsing (OpenCSV) | Standard library |
| Rate limiting concerns | Single file download | No throttling needed |
| Structure change risk | Stable CSV format | Low maintenance |
| **3 sprints** | **~1.5 sprints** | **50% effort reduction** |

## Scope

### In Scope

- CSV download and parsing service
- Executive appointee data import (PAS, PA, NA positions)
- Integration with existing Person entity model
- Executive position types in GovernmentPosition
- Linkage to existing GovernmentOrganization entities
- Manual sync trigger (admin UI)
- API endpoints for appointee lookup

### Out of Scope

- Real-time sync (CSV is updated quarterly by OPM)
- Automated scheduled sync (manual trigger sufficient for MVP)
- Historical trend analysis (future enhancement)
- Confirmation hearing data (future enhancement)

## Architecture

### CSV Import Design

```
┌─────────────────────────────────────────────────────────────┐
│                   Admin Dashboard                            │
│              (Manual Sync Trigger)                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                 PlumCsvImportService                         │
│  - HTTP download CSV      - Parse with OpenCSV              │
│  - Validate records       - Map to entities                 │
│  - Upsert logic           - Error handling                  │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                   OPM PLUM Archive                           │
│   https://www.opm.gov/.../plum-archive-biden-administration.csv │
│   - 21,000+ records       - Quarterly updates               │
└─────────────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Data Transformation                         │
│  - Agency name → GovernmentOrganization ID                  │
│  - Position normalization    - Person deduplication         │
│  - Appointment type mapping  - Date parsing                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                     Factbase DB                              │
│   Person | GovernmentPosition | PositionHolding             │
└─────────────────────────────────────────────────────────────┘
```

### Entity Model Extensions

```
GovernmentPosition (extend from FB-1)
├── positionId (PK)
├── title
├── branch (LEGISLATIVE | EXECUTIVE | JUDICIAL)  # Add to existing
├── appointmentType (NEW)
│   ├── PAS (Presidential Appointment, Senate Confirmed)
│   ├── PA (Presidential Appointment, no Senate confirmation)
│   ├── NA (Non-career Appointment)
│   ├── CA (Career Appointment)
│   └── XS (Expected to change with administration)
├── payPlan (NEW - e.g., EX, ES, GS)
├── payGrade (NEW - e.g., Level I, II, III)
├── location (NEW)
├── organizationId (FK → GovernmentOrganization)
└── expirationDate (NEW - for term positions)

PositionHolding (extend from FB-1)
├── holdingId (PK)
├── personId (FK → Person)
├── positionId (FK → GovernmentPosition)
├── startDate
├── endDate (nullable = current)
├── tenure (NEW - tenure code from PLUM)
└── source (CONGRESS_GOV | LEGISLATORS_REPO | PLUM_CSV)  # Add PLUM_CSV
```

### Technology Stack

| Component | Technology | Notes |
|-----------|------------|-------|
| CSV Parser | OpenCSV | Standard Java CSV library |
| HTTP Client | RestTemplate / WebClient | Download CSV file |
| Validation | Bean Validation | Record-level validation |
| Database | PostgreSQL | Existing |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-2.1 | **PLUM CSV Import Service** - Create service to download and parse OPM PLUM CSV, map to entities, handle upserts | P0 | 0.5 sprint | None |
| FB-2.2 | **Executive Position Data Model** - Extend GovernmentPosition and PositionHolding for executive branch data | P0 | 0.5 sprint | None |
| FB-2.3 | **Admin PLUM Sync UI** - Add PLUM sync button to admin dashboard with progress/status display | P1 | 0.25 sprint | FB-2.1 |
| FB-2.4 | **Appointee Lookup API Endpoints** - REST endpoints for querying executive appointees | P1 | 0.25 sprint | FB-2.1, FB-2.2 |

### Dependency Graph

```
FB-2.1 (CSV Import)     FB-2.2 (Data Model)
    │                        │
    └───────────┬────────────┘
                │
    ┌───────────┴───────────┐
    ▼                       ▼
FB-2.3 (Admin UI)    FB-2.4 (API Endpoints)
```

### Implementation Order

**Sprint 1:**
- FB-2.1: PLUM CSV Import Service
- FB-2.2: Executive Position Data Model

**Sprint 2:**
- FB-2.3: Admin PLUM Sync UI
- FB-2.4: Appointee Lookup API Endpoints

## API Endpoints (New)

### Appointee Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/appointees` | GET | List all tracked appointees (paginated) |
| `/api/appointees/cabinet` | GET | List current Cabinet members |
| `/api/appointees/by-agency/{orgId}` | GET | List appointees by agency |
| `/api/appointees/by-type/{type}` | GET | List by appointment type (PAS, PA, etc.) |
| `/api/appointees/{id}` | GET | Get appointee details |
| `/api/appointees/search` | GET | Search appointees by name/title |
| `/api/positions/executive` | GET | List executive positions |
| `/api/positions/executive/vacant` | GET | List vacant positions |

### Admin Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/admin/sync/plum` | POST | Trigger PLUM data sync |
| `/api/admin/sync/plum/status` | GET | Get last sync status |

## Data Sources

### OPM PLUM Archive CSV

| Attribute | Value |
|-----------|-------|
| **URL** | https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/plum-archive-biden-administration.csv |
| **Auth** | None (public) |
| **Format** | CSV |
| **Update Frequency** | Quarterly (by OPM) |
| **Records** | ~21,000 positions |
| **Size** | ~3.8 MB |

### Alternative: Live Website Export

For current administration data (when CSV archive not yet available):
- Manual CSV export from https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/
- Contact OPM at PLUMreporting@opm.gov for bulk access

## Acceptance Criteria (Epic Level)

1. **Data Import**: Successfully import 8,000+ executive positions from CSV
2. **Agency Linkage**: >90% of positions auto-linked to GovernmentOrganization
3. **Person Records**: Create/update Person records for all incumbents
4. **Position Tracking**: Track filled vs vacant positions
5. **API Availability**: All endpoints return data with <500ms response time
6. **Admin Sync**: Manual sync trigger works from admin dashboard
7. **Error Handling**: Import errors logged with actionable messages

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| CSV format changes | Medium | Low | Validate headers on import, alert on mismatch |
| Agency name mismatches | Medium | Medium | Fuzzy matching + manual mapping table |
| Large file download timeout | Low | Low | Streaming download, retry logic |
| Duplicate person records | Medium | Medium | Name + agency deduplication logic |

## Definition of Done

- [ ] All 4 stories completed and merged
- [ ] CSV import successfully parses all records
- [ ] 8,000+ positions populated in factbase
- [ ] Admin sync UI operational
- [ ] API endpoints documented in OpenAPI spec
- [ ] Agency linkage >90% automated
- [ ] Unit and integration tests passing

## Related Documentation

- [Technical Spike Results](#technical-spike-findings) (above)
- [OPM PLUM Data](https://www.opm.gov/about-us/open-government/plum-reporting/plum-data/)
- [PLUM Archive](https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/)
- [Architect Handoff](../architecture/FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-27 | 1.0 | Initial epic creation (web scraper approach) | Sarah (PO) |
| 2025-12-01 | 2.0 | **Major revision**: Replaced web scraper with CSV import based on technical spike findings. Reduced effort estimate from 3 sprints to 1.5 sprints. Updated architecture, stories, and risks. | Sarah (PO) |
| 2025-12-01 | 2.1 | Created 4 story files, submitted for Architect review | Sarah (PO) |
| 2025-12-01 | 2.2 | **Architect Review Complete**: All stories approved. FB-2.2 updated with Branch enum and nullable field requirements. Status → Ready for Development | Winston (Architect) |

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Review Date:** 2025-12-01
**Status:** APPROVED WITH REQUIRED CHANGES

### Critical Issue: GovernmentPosition Entity Compatibility

The existing `GovernmentPosition` entity is Congressional-specific with `@NotNull` on `chamber` and `state`, plus a unique constraint on `(chamber, state, district)`. Executive positions cannot be stored without modifications.

**Required Changes (incorporated into FB-2.2):**

1. Add `Branch` enum (LEGISLATIVE, EXECUTIVE, JUDICIAL)
2. Add `branch` field to GovernmentPosition (required)
3. Make `chamber`, `state` nullable (executive positions don't have these)
4. Update unique constraints with partial indexes by branch
5. Update migration sequence: V12 (branch), V13-V15 (other fields)

### Story-by-Story Assessment

| Story | Status | Notes |
|-------|--------|-------|
| FB-2.1 | ✅ APPROVED | Add HTTP timeout config (30s connect, 5min read) |
| FB-2.2 | ⚠️ APPROVED WITH CHANGES | Must add Branch enum and update constraints |
| FB-2.3 | ✅ APPROVED | Follows established patterns |
| FB-2.4 | ✅ APPROVED | Consider adding position-title search endpoint |

### Additional Recommendations

1. **Agency Matching:** Document strategy (exact → case-insensitive → fuzzy)
2. **Progress Tracking:** Add records processed / total for admin UI
3. **Chunked Processing:** Consider 1000 records per transaction for memory efficiency

### Architecture Alignment

- CSV import approach significantly better than original web scraper
- Technology choices (OpenCSV, RestTemplate) are appropriate
- Follows existing *SyncService patterns
- No breaking changes to existing APIs

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2025-12-01 | **SUBMITTED FOR REVIEW** |
| Architect | Winston | 2025-12-01 | **APPROVED WITH CHANGES** |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
