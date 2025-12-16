# Epic FB-6: External API Search & Import UI

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-6 |
| **Epic Name** | External API Search & Import UI |
| **Epic Type** | Feature / UI Enhancement |
| **Priority** | MEDIUM |
| **Status** | **Superseded by ADMIN-1** |
| **Created** | 2025-12-02 |
| **Updated** | 2025-12-12 |
| **Owner** | Sarah (PO) |
| **Depends On** | FB-4 (Admin Dashboard Redesign) |

> **SUPERSEDED (2025-12-12):** This draft epic was never formally approved. All planned functionality was implemented through the **ADMIN-1** story set (stories 1.6, 1.7, 1.8, 1.9). See [ADMIN-1 Epic](../ADMIN-1/ADMIN-1.epic-admin-dashboard-improvements.md) for completion details.

## Executive Summary

Create a unified admin interface for searching and importing records from all external APIs that have backend services. Administrators should be able to search external sources, preview results with source attribution, and import records with options to edit, merge with existing, or validate before commit.

## Business Value

### Why This Epic Matters

1. **Unified Experience** - Consistent pattern across all external data sources
2. **Data Quality** - Preview/merge prevents duplicates and errors
3. **Source Transparency** - Clear attribution for fact-checking credibility
4. **Admin Efficiency** - One interface pattern to learn, applies to all APIs

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Coverage | 100% | All backend services have UI |
| Import Success Rate | >99% | Imports without errors |
| Duplicate Prevention | >95% | Merge suggestions accuracy |
| Source Attribution | 100% | All imports show data source |

## Scope

### In Scope

#### External APIs to Support

| API | Backend Service | Entity Types | Status |
|-----|-----------------|--------------|--------|
| Congress.gov | `CongressApiClient` | Members, Committees, Bills | Exists |
| Federal Register | `FederalRegisterClient` | Agencies, Regulations | Exists |
| Legislators Repo | `LegislatorsRepoClient` | Member enrichment data | Exists |

#### Import Workflow Features

1. **Search Interface**
   - API-specific search fields (e.g., name, state, party for members)
   - Clear indication of which external API is being searched
   - Pagination for large result sets

2. **Preview Mode**
   - Display full record details before import
   - Show data source and retrieval timestamp
   - Allow field-level editing
   - Highlight required vs optional fields

3. **Merge Detection**
   - Identify potential duplicates in NewsAnalyzer DB
   - Side-by-side comparison view
   - Field-level merge decisions (keep existing, use new, manual edit)
   - Confidence score for match suggestions

4. **Validation & Import**
   - Pre-commit validation (DB constraints, required fields)
   - Clear error messages for validation failures
   - Success confirmation with link to imported record
   - Audit trail (source API, timestamp, admin user)

### Out of Scope

- New external API integrations (use existing backend services only)
- Bulk import from API (single record focus for this epic)
- API credential management
- Rate limit UI (handled by backend)

## Architecture

### UI Component Pattern

```
ApiSearchImportPanel
├── ApiSelector (dropdown: Congress.gov, Federal Register, etc.)
├── SearchForm (dynamic fields based on selected API)
├── ResultsList
│   └── ResultCard (preview button, source badge)
├── PreviewModal
│   ├── RecordDetails (editable fields)
│   ├── SourceAttribution (API name, timestamp)
│   └── ActionButtons (Import, Merge Check, Cancel)
├── MergeDialog
│   ├── ComparisonTable (existing vs new)
│   └── MergeDecisions (per field)
└── ValidationFeedback
    ├── ErrorList
    └── SuccessConfirmation
```

### API Search Endpoints (New/Extended)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/search/congress/members` | GET | Search Congress.gov members |
| `/api/search/congress/committees` | GET | Search Congress.gov committees |
| `/api/search/federal-register/agencies` | GET | Search FR agencies |
| `/api/search/federal-register/documents` | GET | Search FR regulations |
| `/api/admin/import/preview` | POST | Preview import with merge check |
| `/api/admin/import/commit` | POST | Commit validated import |

### Data Flow

```
┌─────────────────────────────────────────────────────────────┐
│                  Admin: API Search UI                        │
│         Select API → Search → Preview → Import               │
└─────────────────────┬───────────────────────────────────────┘
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
┌─────────────────┐     ┌─────────────────────┐
│   Search Proxy  │     │   Import Service    │
│   Endpoints     │     │   (preview, merge,  │
│                 │     │    validate, commit)│
└────────┬────────┘     └──────────┬──────────┘
         │                         │
         ▼                         ▼
┌─────────────────┐     ┌─────────────────────┐
│ External APIs   │     │ NewsAnalyzer DB     │
│ (Congress.gov,  │     │ (PostgreSQL)        │
│  Federal Reg.)  │     │                     │
└─────────────────┘     └─────────────────────┘
```

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| FB-6.1 | Reusable API Search Component | P0 | 1 sprint | FB-4.2 |
| FB-6.2 | Congress.gov Search & Import UI | P0 | 1 sprint | FB-6.1 |
| FB-6.3 | Federal Register Search & Import UI | P0 | 0.5 sprint | FB-6.1 |
| FB-6.4 | Preview & Edit Modal | P0 | 0.5 sprint | FB-6.1 |
| FB-6.5 | Merge Detection & Resolution UI | P1 | 1 sprint | FB-6.4 |
| FB-6.6 | Validation & Commit Flow | P0 | 0.5 sprint | FB-6.4 |
| FB-6.7 | Source Attribution Display | P1 | 0.25 sprint | FB-6.1 |

### Dependency Graph

```
FB-6.1 (Reusable Component)
    │
    ├────────────┬────────────┬─────────────┐
    ▼            ▼            ▼             ▼
FB-6.2       FB-6.3       FB-6.4        FB-6.7
(Congress)   (Fed Reg)   (Preview)    (Source)
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
                FB-6.5            FB-6.6
                (Merge)           (Validate)
```

## UI Wireframes

### Search Interface
```
┌──────────────────────────────────────────────────────────┐
│ Search External API                                       │
├──────────────────────────────────────────────────────────┤
│ Source: [Congress.gov        ▼]                          │
│                                                          │
│ ┌─ Search Fields (dynamic by source) ─────────────────┐  │
│ │ Name: [________________]  State: [__▼]  Party: [__▼]│  │
│ └────────────────────────────────────────────────────┘  │
│                                          [🔍 Search]     │
├──────────────────────────────────────────────────────────┤
│ Results (showing 1-20 of 535)                            │
│ ┌──────────────────────────────────────────────────────┐ │
│ │ 📋 Nancy Pelosi (D-CA)              [Preview] [Add]  │ │
│ │    Source: Congress.gov • Retrieved: 2025-12-02      │ │
│ ├──────────────────────────────────────────────────────┤ │
│ │ 📋 Kevin McCarthy (R-CA)            [Preview] [Add]  │ │
│ │    Source: Congress.gov • Retrieved: 2025-12-02      │ │
│ └──────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

### Preview Modal
```
┌──────────────────────────────────────────────────────────┐
│ Preview Import                                      [X]   │
├──────────────────────────────────────────────────────────┤
│ ⚠️ Potential Match Found - Review Before Import          │
├──────────────────────────────────────────────────────────┤
│                 EXISTING          NEW (Congress.gov)      │
│ ────────────────────────────────────────────────────────  │
│ Name:          Nancy Pelosi       Nancy P. Pelosi         │
│ Party:         Democratic         Democratic       ✓      │
│ State:         CA                 CA               ✓      │
│ BioGuide ID:   P000197            P000197          ✓      │
│ Website:       (none)             pelosi.house.gov  ←     │
│ ────────────────────────────────────────────────────────  │
│                                                          │
│ Source: Congress.gov API                                 │
│ Retrieved: 2025-12-02 14:32:05 UTC                       │
│                                                          │
│          [Cancel]  [Merge Selected Fields]  [Import New] │
└──────────────────────────────────────────────────────────┘
```

## Acceptance Criteria (Epic Level)

1. **API Selection**: Dropdown to select from available external APIs
2. **Dynamic Search**: Search fields appropriate to selected API
3. **Source Badge**: Every result shows originating API clearly
4. **Preview Mode**: Full record preview before any import
5. **Edit Capability**: Modify fields in preview before import
6. **Merge Detection**: System identifies potential duplicate matches
7. **Merge UI**: Side-by-side comparison with field-level decisions
8. **Validation**: Pre-commit check prevents DB constraint violations
9. **Audit Trail**: Imports logged with source, timestamp, user
10. **Error Handling**: Clear messages for API failures, validation errors

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| API rate limits | Medium | Medium | Queue requests, show wait indicator |
| Merge algorithm false positives | Medium | Low | High threshold + manual confirm |
| API response format changes | Medium | Low | Version backend clients, graceful degradation |
| Complex merge scenarios | Low | Medium | Default to manual decision |

## Definition of Done

- [ ] All 7 stories completed and merged
- [ ] All existing backend APIs have UI
- [ ] Preview/edit works for all entity types
- [ ] Merge detection identifies >95% of duplicates
- [ ] Validation catches constraint violations
- [ ] Source attribution on all imports
- [ ] Error handling for all failure modes
- [ ] Admin can complete full workflow without technical support

## Related Documentation

- [Project Brief](../analysis/ADMIN_DASHBOARD_IMPROVEMENTS_PROJECT_BRIEF.md)
- [CongressApiClient](../../backend/src/main/java/org/newsanalyzer/service/CongressApiClient.java)
- [FederalRegisterClient](../../backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java)

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
