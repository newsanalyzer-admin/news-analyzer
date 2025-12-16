# Project Brief: Admin Dashboard Improvements

**Document Version:** 1.0
**Created:** 2025-12-02
**Author:** Mary (Business Analyst)
**Status:** Draft

---

## Executive Summary

The NewsAnalyzer Admin Dashboard requires significant improvements to support the platform's growing functionality. The current implementation is functionally adequate but lacks intuitive organization and scalability for future features. This initiative will restructure the admin experience, add critical data import capabilities, and provide unified access to all external API integrations.

---

## Problem Statement

### Current State
- Admin dashboard exists but organization is un-intuitive
- Limited to sync status cards for existing data sources
- No support for Government Manual (GOVMAN) XML import
- No support for US Code import
- No unified interface for searching/importing from external APIs
- UI structure doesn't accommodate future expansion (analysis workflows, entity extraction, findings reporting)

### Desired Future State
- Hierarchical, expandable navigation supporting current and future admin functions
- Complete import capabilities for authoritative government data sources
- Unified search/preview/import interface for all external APIs
- Scalable architecture ready for: Article Search, Workflows, Entity Extraction, Analysis, Reporting

---

## Business Value

| Value Driver | Impact |
|--------------|--------|
| **Complete Factbase** | All 3 federal branches with organizational structure and position holders |
| **Admin Efficiency** | Intuitive UI reduces training time and errors |
| **Data Quality** | Preview/merge capabilities prevent duplicate and corrupt data |
| **Scalability** | Architecture supports 2+ years of feature growth |
| **Source Transparency** | Clear attribution of data sources for fact-checking credibility |

---

## Scope

### In Scope

#### 1. Admin Dashboard UI Redesign
- Left sidebar navigation with expandable menu structure
- Main categories:
  - **Factbase** (current focus)
  - **Article Search** (future placeholder)
  - **Workflows** (future placeholder)
- Factbase subcategories:
  - Government Entities (Executive, Legislative, Judicial)
  - Federal Laws & Regulations
  - Universities & Think Tanks (future)
  - Charities (future)
  - Fortune 500 (future)
  - Public Figures (future)

#### 2. Data Import Capabilities
| Source | Format | Priority | Backend Work Needed |
|--------|--------|----------|---------------------|
| Government Manual (GOVMAN) | XML | P0 | Yes - new XML parser |
| US Code | TBD | P1 | Yes - new parser |
| PLUM Book (OPM) | CSV | Exists | None (already implemented) |
| Legislative/Judicial Orgs | CSV | Exists | None (already implemented) |

#### 3. External API Search & Import UI
- Unified interface pattern for all external API services
- Required for each API:
  - Search interface with relevant filters
  - Results preview with source attribution
  - Import options: Preview/Edit, Merge with existing, Direct import (with validation)
  - Clear indication of API source and data type

| API | Current Backend | Admin UI Needed |
|-----|-----------------|-----------------|
| Congress.gov | ✅ CongressApiClient | ✅ New |
| Federal Register | ✅ FederalRegisterClient | ✅ New |
| Legislators Repo | ✅ LegislatorsRepoClient | ✅ New |

#### 4. Import Workflow Requirements
- **Preview/Edit Mode**: Display record details, allow field modifications before import
- **Merge Detection**: Identify potential duplicates, show comparison, allow merge decisions
- **Validation Check**: Pre-commit validation to prevent DB constraint violations
- **Audit Trail**: Track import source, timestamp, and user for all records

### Out of Scope (Future Phases)
- Article Search functionality (placeholder only)
- Analysis Workflows (placeholder only)
- Entity Extraction management
- Findings Reporting
- Non-government entity categories (Universities, Charities, Fortune 500, Public Figures)

---

## User Personas

### Primary: System Administrator
- **Role**: Maintains NewsAnalyzer factbase with authoritative data
- **Goals**:
  - Keep factbase current and complete
  - Import data from multiple official sources
  - Resolve duplicates and data conflicts
- **Pain Points**:
  - Current UI doesn't scale
  - Manual data import processes
  - No visibility into external API data before import

---

## Success Criteria

| Metric | Target |
|--------|--------|
| Government Manual Import | 100% of GOVMAN entities importable |
| UI Navigation Depth | Max 2 clicks to any admin function |
| Import Preview | All external API records previewable before import |
| Duplicate Detection | 95%+ accuracy on merge suggestions |
| Federal Branch Coverage | All 3 branches with complete org hierarchy |

---

## Technical Considerations

### Frontend
- Extend existing Next.js admin pages
- Add sidebar navigation component
- Create reusable "API Search & Import" component pattern
- Leverage existing shadcn/ui components

### Backend
- **New Service**: `GovManXmlImportService` - Parse GOVMAN XML
- **New Service**: `UsCodeImportService` - Parse US Code (format TBD)
- **New Endpoints**: Search proxies for external APIs (if not already exposed)
- **Existing**: Leverage `GovOrgCsvImportService`, `PlumCsvImportService` patterns

### Data Model
- Existing `GovernmentOrganization` model should support GOVMAN data
- May need new fields for US Code integration
- Consider `import_source` and `import_timestamp` audit fields

---

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| GOVMAN XML structure complexity | Medium | Medium | Spike to analyze full XML schema |
| US Code format unknown | High | High | Research phase before implementation |
| UI redesign scope creep | Medium | Medium | Strict focus on Factbase for Phase 1 |
| External API rate limits | Low | Low | Implement request throttling |

---

## Proposed Phases

### Phase 1: Foundation (This Initiative)
- Admin UI restructure with sidebar navigation
- GOVMAN XML import capability
- External API search/import UI for existing services
- Factbase > Government Entities fully functional

### Phase 2: Expanded Factbase
- US Code import
- Federal Laws & Regulations section
- Enhanced position holder tracking

### Phase 3: Future Categories
- Universities & Think Tanks
- Charities
- Fortune 500
- Public Figures

### Phase 4: Advanced Features
- Article Search
- Workflows
- Entity Extraction management
- Findings Reporting

---

## Dependencies

- Existing admin dashboard (`frontend/src/app/admin/`)
- Existing API clients (Congress.gov, Federal Register, Legislators)
- Existing import services (CSV, PLUM)
- GOVMAN XML file (`data/GOVINFO/GOVMAN-2025-01-13.xml`)

---

## Stakeholders

| Role | Responsibility |
|------|----------------|
| Product Owner | Approve scope, prioritize features |
| Architect | Technical design, API patterns |
| Dev Team | Implementation |
| QA | Test import accuracy, UI usability |

---

## Next Steps

1. ✅ Project Brief (this document)
2. ⏳ Create Epics for each major component
3. ⏳ Architect review of technical approach
4. ⏳ Story breakdown and estimation
5. ⏳ Sprint planning

---

## Appendix A: Current Admin Dashboard Structure

```
/admin
├── SyncStatusCard (Members)
├── SyncStatusCard (Committees)
├── GovOrgSyncStatusCard (Government Orgs)
├── PlumSyncCard (PLUM Appointees)
└── EnrichmentStatus
```

## Appendix B: Proposed Navigation Structure

```
Admin Dashboard
├── Factbase
│   ├── Government Entities
│   │   ├── Executive Branch
│   │   │   ├── Agencies & Departments
│   │   │   ├── Positions & Appointees
│   │   │   └── PLUM Import
│   │   ├── Legislative Branch
│   │   │   ├── Congress Structure
│   │   │   ├── Members
│   │   │   ├── Committees
│   │   │   └── Legislators Enrichment
│   │   └── Judicial Branch
│   │       ├── Courts
│   │       └── Judges
│   ├── Federal Laws & Regulations
│   │   ├── Regulations (Federal Register)
│   │   └── US Code (future)
│   └── [Future Categories...]
├── Article Search (placeholder)
└── Workflows (placeholder)
```

## Appendix C: GOVMAN XML Sample Structure

```xml
<Entity EntityId="70" ParentId="0" SortOrder="1">
    <EntityType>Parent</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>Congress</AgencyName>
    <MissionStatement>...</MissionStatement>
    <Addresses>
        <WebAddress>http://www.congress.gov</WebAddress>
    </Addresses>
    <LeaderShipTables/>
    <ProgramAndActivities/>
</Entity>
```

---

*End of Project Brief*
