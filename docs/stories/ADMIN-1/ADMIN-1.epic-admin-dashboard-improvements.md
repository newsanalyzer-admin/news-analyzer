# Epic ADMIN-1: Admin Dashboard & Data Import Improvements

## Status

**Done**

---

## Epic Summary

This epic delivers comprehensive improvements to the admin dashboard, including a new sidebar navigation system, restructured branch management pages, authoritative data import capabilities (GOVMAN XML, US Code), and unified external API search/import functionality for Congress.gov, Federal Register, and the Legislators Repository.

---

## Business Value

- **Improved Admin UX**: Hierarchical sidebar navigation with 3-level nesting replaces flat navigation
- **Data Authority**: Import official government structure from GOVMAN XML and US Code sources
- **On-Demand Import**: Search and import specific records from external APIs without full sync
- **Data Enrichment**: Enrich member records with social media and external IDs from legislators repo
- **Complete Coverage**: All three branches of government now have dedicated admin sections

---

## Scope

### Supersedes Draft Epics

This epic consolidates and completes the scope originally planned in:
- **FB-4**: Admin Dashboard Redesign
- **FB-5**: Authoritative Data Import (GOVMAN)
- **FB-6**: External API Search & Import UI

All functionality from these draft epics has been implemented via ADMIN-1 stories.

### Features Delivered

| Feature Area | Stories | Description |
|--------------|---------|-------------|
| **Sidebar Navigation** | 1.1 | Collapsible 3-level sidebar with Factbase menu structure |
| **Executive Branch Pages** | 1.2, 1.12 | Agencies, Positions, GOVMAN Import pages |
| **Legislative Branch Pages** | 1.3 | Members, Committees pages with sync controls |
| **Judicial Branch Pages** | 1.12 | Courts placeholder with branch filtering |
| **GOVMAN Import** | 1.4, 1.5 | XML parser service + admin upload UI |
| **US Code Import** | 1.10, 1.11, 1.13 | Research spike, backend parser, frontend tree view |
| **Search Framework** | 1.6 | Reusable SearchImportPanel component |
| **Congress.gov Search** | 1.7 | Member search with duplicate detection |
| **Federal Register Search** | 1.8 | Document search with agency linkage |
| **Legislators Enrichment** | 1.9 | Member enrichment from GitHub repo |

---

## Stories

| Story | Title | Status | Points |
|-------|-------|--------|--------|
| ADMIN-1.1 | Sidebar Navigation Component | Done | 5 |
| ADMIN-1.2 | Executive Branch Restructure | Done | 3 |
| ADMIN-1.3 | Legislative Branch Restructure | Done | 3 |
| ADMIN-1.4 | GOVMAN XML Parser Service | Done | 5 |
| ADMIN-1.5 | GOVMAN Import UI | Done | 3 |
| ADMIN-1.6 | Unified Search/Import Component | Done | 8 |
| ADMIN-1.7 | Congress.gov Search | Done | 5 |
| ADMIN-1.8 | Federal Register Search | Done | 5 |
| ADMIN-1.9 | Legislators Repo Search | Done | 5 |
| ADMIN-1.10 | US Code Research Spike | Done | 2 |
| ADMIN-1.11 | US Code Import Backend | Done | 8 |
| ADMIN-1.12 | Judicial Branch Final Polish | Done | 3 |
| ADMIN-1.13 | US Code Frontend | Done | 5 |
| **Total** | | **13/13 Done** | **60** |

---

## Technical Architecture

### Frontend Components Created

```
frontend/src/
├── app/admin/
│   ├── layout.tsx                           # AdminLayout with sidebar
│   └── factbase/
│       ├── executive/
│       │   ├── page.tsx                     # Executive hub
│       │   ├── agencies/page.tsx            # Gov org management
│       │   ├── positions/page.tsx           # PLUM appointees
│       │   └── govman/page.tsx              # GOVMAN XML import
│       ├── legislative/
│       │   ├── page.tsx                     # Legislative hub
│       │   ├── members/
│       │   │   ├── page.tsx                 # Members management
│       │   │   └── search/page.tsx          # Congress.gov search
│       │   ├── committees/page.tsx          # Committees management
│       │   └── legislators-repo/page.tsx    # Enrichment from GitHub
│       ├── judicial/
│       │   └── page.tsx                     # Judicial branch hub
│       ├── regulations/
│       │   ├── search/page.tsx              # Federal Register search
│       │   └── us-code/page.tsx             # US Code import + tree
│       └── search-test/page.tsx             # SearchImportPanel demo
├── components/admin/
│   ├── AdminSidebar.tsx                     # Collapsible sidebar
│   ├── SidebarMenuItem.tsx                  # Recursive menu item
│   ├── AdminBreadcrumb.tsx                  # Navigation breadcrumbs
│   ├── SearchImportPanel.tsx                # Reusable search component
│   ├── SearchResultCard.tsx                 # Result display
│   ├── SearchFilters.tsx                    # Dynamic filters
│   ├── ImportPreviewModal.tsx               # Preview before import
│   ├── MergeConflictModal.tsx               # Duplicate resolution
│   ├── GovmanImportButton.tsx               # GOVMAN upload
│   ├── UsCodeImportButton.tsx               # US Code upload
│   └── UsCodeTreeView.tsx                   # Hierarchical display
├── hooks/
│   ├── useSearchImport.ts                   # Search query hook
│   ├── useImportRecord.ts                   # Import mutation hook
│   ├── useGovmanImport.ts                   # GOVMAN import hook
│   └── useUsCodeImport.ts                   # US Code import hook
├── stores/
│   └── sidebarStore.ts                      # Sidebar state (Zustand)
└── types/
    ├── search-import.ts                     # Generic search types
    ├── congress-search.ts                   # Congress.gov types
    ├── federal-register.ts                  # Federal Register types
    └── legislators-search.ts                # Legislators repo types
```

### Backend Services Created

```
backend/src/main/java/org/newsanalyzer/
├── controller/
│   ├── AdminSearchController.java           # Search proxy endpoints
│   ├── AdminImportController.java           # Import endpoints
│   ├── GovmanImportController.java          # GOVMAN XML upload
│   └── StatuteImportController.java         # US Code XML upload
├── service/
│   ├── GovmanXmlImportService.java          # GOVMAN parser + import
│   ├── CongressSearchService.java           # Congress.gov search wrapper
│   ├── FederalRegisterSearchService.java    # Fed Register search wrapper
│   ├── FederalRegisterImportService.java    # Fed Register import
│   ├── LegislatorsSearchService.java        # Legislators repo search
│   ├── LegislatorEnrichmentImportService.java # Member enrichment
│   ├── UsCodeImportService.java             # US Code import
│   ├── UsCodeDownloadService.java           # US Code download
│   └── UslmXmlParser.java                   # USLM XML parser (StAX)
├── dto/
│   ├── govman/                              # GOVMAN JAXB classes
│   ├── CongressMemberSearchDTO.java
│   ├── CongressMemberDetailDTO.java
│   ├── FederalRegisterSearchDTO.java
│   ├── FederalRegisterDetailDTO.java
│   ├── LegislatorSearchDTO.java
│   ├── LegislatorDetailDTO.java
│   ├── UsCodeHierarchyDTO.java
│   └── ... (import results, responses)
└── model/
    └── Statute.java                         # US Code entity
```

### Database Migrations

| Migration | Description |
|-----------|-------------|
| V21 | Add `import_source` column to government_organizations |
| V22 | Create `statutes` table for US Code |
| V23 | Alter statutes source_credit column |

---

## Dependencies

### External APIs Used

| API | Purpose | Rate Limits |
|-----|---------|-------------|
| Congress.gov API | Member search | 5000/hour |
| Federal Register API | Document search | None |
| uscode.house.gov | US Code XML download | None (bulk files) |
| GitHub (congress-legislators) | Member enrichment | Standard GitHub limits |

### Internal Dependencies

- Existing `CongressApiClient` for Congress.gov integration
- Existing `FederalRegisterClient` for Federal Register integration
- Existing `LegislatorsRepoClient` for GitHub repository
- Existing `AgencyLinkageService` for agency-to-org mapping

---

## Acceptance Criteria Summary

All acceptance criteria across 13 stories have been verified and passed QA review.

### Key Deliverables Verified

- [x] Collapsible sidebar with 3-level menu hierarchy
- [x] All three government branches have dedicated admin pages
- [x] GOVMAN XML import with validation and progress indicator
- [x] US Code XML import with hierarchical tree view
- [x] Congress.gov member search with duplicate detection
- [x] Federal Register document search with agency linkage
- [x] Member enrichment with field diff preview
- [x] Reusable SearchImportPanel component for future integrations

---

## QA Summary

| Metric | Value |
|--------|-------|
| Total Stories | 13 |
| Stories Passed QA | 13 |
| Average QA Score | 93/100 |
| Total Unit Tests Added | 150+ |
| Backend Test Coverage | Good |
| Frontend Test Coverage | Manual (per QA-2) |

---

## Risks and Mitigations

| Risk | Status | Mitigation |
|------|--------|------------|
| Large XML file parsing performance | Mitigated | StAX streaming parser used |
| Congress.gov rate limiting | Mitigated | Rate limit tracking in UI |
| US Code data freshness | Accepted | Manual upload workflow |
| Federal Register agency matching | Mitigated | AgencyLinkageService handles |

---

## Future Considerations

1. **Automated US Code Updates**: Consider scheduled sync when new releases published
2. **Frontend Test Framework**: QA-2 epic will establish Vitest for component testing
3. **Search Result Caching**: Consider Redis cache for frequently searched terms
4. **Bulk Import Progress**: WebSocket for real-time progress on large imports

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-03 | 1.0 | Epic created, stories 1.1-1.6 defined | Sarah (PO) |
| 2025-12-07 | 1.1 | Added stories 1.7-1.9 for API search | Sarah (PO) |
| 2025-12-09 | 1.2 | Added stories 1.10-1.11 for US Code | Sarah (PO) |
| 2025-12-10 | 1.3 | Added stories 1.12-1.13, finalized scope | Sarah (PO) |
| 2025-12-12 | 2.0 | Epic completed - all 13 stories Done | Sarah (PO) |

---

## References

- [GOVMAN XML Source](https://www.govinfo.gov/bulkdata/GOVMAN)
- [US Code Download](https://uscode.house.gov/download/download.shtml)
- [Congress.gov API](https://api.congress.gov/)
- [Federal Register API](https://www.federalregister.gov/developers/documentation/api/v1)
- [Congress Legislators Repo](https://github.com/unitedstates/congress-legislators)
