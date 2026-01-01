# Epic UI-3: Frontend Architecture Realignment

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | UI-3 |
| **Epic Name** | Frontend Architecture Realignment |
| **Epic Type** | UI/UX Refactoring |
| **Priority** | HIGH |
| **Status** | COMPLETE |
| **Created** | 2025-12-30 |
| **Owner** | Sarah (PO) |
| **Depends On** | UI-2 Complete (reuses pattern components) |
| **Supersedes** | UI-2 route structure (preserves patterns) |
| **Triggered By** | Architecture Review v2.5 (Data Architecture Clarification) |
| **Change Proposal** | `docs/qa/SCP-2025-12-30-001-frontend-realignment.md` |

## Executive Summary

Realign the frontend implementation with the dual-layer data architecture documented in `architecture.md` v2.5. The current `/knowledge-base` route browses extracted entities (from the `entities` table) but should browse authoritative Knowledge Base data (from `persons`, `committees`, `government_organizations` tables). Additionally, create a new **Article Analyzer** navigation section for article analysis workflows.

### Why This Change Is Needed

The UI-2 epic was implemented before the architectural clarification that explicitly distinguished:
- **Knowledge Base** = Authoritative, curated reference data (persons, committees, gov_orgs)
- **Article Analyzer** = Analysis layer for extracted entities and article processing

UI-2's excellent pattern components (EntityBrowser, EntityDetail, HierarchyView) were applied to the wrong data layer. This epic preserves all pattern work while correcting the data source configuration.

## Relationship to UI-2

| Aspect | UI-2 Delivered | UI-3 Action |
|--------|----------------|-------------|
| **Pattern Components** | EntityBrowser, EntityDetail, HierarchyView | **PRESERVES** - reuses all patterns |
| **Route Structure** | `/knowledge-base` for extracted entities | **SUPERSEDES** - reconfigures for KB tables |
| **Entity Type Configs** | Generic entity types | **UPDATES** - points to KB tables |
| **Navigation** | EntityTypeSelector, ViewModeSelector | **EXTENDS** - adds hierarchical KB nav |

**Key Insight:** UI-3 doesn't invalidate UI-2's value - it corrects the data source while preserving all reusable components.

## Business Value

### Why This Epic Matters

1. **Architecture Alignment** - Frontend matches documented dual-layer data model
2. **User Clarity** - Knowledge Base shows authoritative data; Article Analyzer shows analysis
3. **Phase 4-6 Enablement** - Article Analyzer is prerequisite for Bias Detection, News Sources, Fact Validation
4. **Terminology Consistency** - "Knowledge Base" means the same thing in code and documentation

### Success Metrics

| Metric | Target | Current |
|--------|--------|---------|
| KB browses authoritative data | 100% | ✅ 100% (Phase A complete) |
| Article Analyzer section exists | Yes | ✅ Yes (Phase B complete) |
| Hierarchical KB navigation | Implemented | ✅ Implemented (UI-3.A.2) |
| Architecture Section 8 compliance | Full | ✅ Full (Phase A + B complete) |

## Scope

### In Scope

**Phase A: Knowledge Base Realignment**
- Reconfigure EntityBrowser to use KB tables (persons, committees, government_organizations)
- Implement hierarchical navigation per architecture Section 8
- Update entity type configurations and API endpoints
- Route restructuring with redirects for bookmarks

**Phase B: Article Analyzer Foundation**
- Create Article Analyzer navigation shell at `/article-analyzer`
- Move extracted entity browsing to `/article-analyzer/entities`
- Create Articles list page at `/article-analyzer/articles`
- Update hero page with dual navigation CTAs

### Out of Scope (Future)

- Article submission UI (Phase 4: Bias Detection)
- Article URL scraping
- Reasoning service integration for analysis
- News Source management UI (Phase 5)
- Full hierarchical tree for all KB categories

## Architecture

### Target Navigation Structure

Per `architecture.md` Section 8:

```
NewsAnalyzer
|
+-- Article Analyzer                    <- Analysis Layer (extracted data)
|   +-- Analyze Article                 <- Submit new article (Future - Phase 4)
|   +-- Articles                        <- List of analyzed articles
|   |   +-- [Article Detail]
|   |       +-- Article Text
|   |       +-- Extracted Entities      <- Shows KB matches where found
|   |       +-- News Source
|   |       +-- Actions
|   +-- Entities                        <- Browse extracted entities (moved from /knowledge-base)
|
+-- Knowledge Base                      <- Authoritative Reference Layer
|   +-- News Sources                    <- (Future - Phase 5)
|   +-- U.S. Federal Government
|   |   +-- U.S. Code                   <- Federal statutes
|   |   +-- Branches
|   |       +-- Executive
|   |       |   +-- Executive Offices
|   |       |   +-- Executive Departments
|   |       |   +-- Independent Agencies
|   |       |   +-- Government Corporations
|   |       +-- Congressional
|   |       |   +-- Senate
|   |       |   +-- House of Representatives
|   |       |   +-- Committees
|   |       |   +-- Support Services
|   |       +-- Judicial
|   |           +-- Supreme Court
|   |           +-- Courts of Appeals
|   |           +-- District Courts
|   +-- International Organizations     <- (Future)
|   +-- State Governments               <- (Future)
|
+-- [Admin]                             <- Administrative functions (existing)
    +-- Data Sync
    +-- Review Queue                    <- (Future)
```

### Route Structure

| Route | Component | Data Source | Description |
|-------|-----------|-------------|-------------|
| `/knowledge-base` | KnowledgeBase | - | KB landing with category selection |
| `/knowledge-base/government` | GovBrowser | government_organizations | U.S. Federal Government section |
| `/knowledge-base/government/[branch]` | BranchView | government_organizations | Executive/Legislative/Judicial |
| `/knowledge-base/people` | PeopleBrowser | persons | All people types |
| `/knowledge-base/people?type=members` | PeopleBrowser | persons (filtered) | Congressional members |
| `/knowledge-base/people?type=judges` | PeopleBrowser | persons (filtered) | Federal judges |
| `/knowledge-base/people?type=appointees` | PeopleBrowser | persons (filtered) | Executive appointees |
| `/knowledge-base/committees` | CommitteesBrowser | committees | Congressional committees |
| `/article-analyzer` | ArticleAnalyzer | - | Article Analyzer landing |
| `/article-analyzer/entities` | EntityBrowser | entities | Extracted entities (moved) |
| `/article-analyzer/articles` | ArticlesList | articles | Analyzed articles list |

### Data Layer Mapping

| UI Section | Database Tables | Purpose |
|------------|-----------------|---------|
| **Knowledge Base** | `persons`, `committees`, `government_organizations`, `government_positions`, `statutes`, `regulations` | Authoritative reference data |
| **Article Analyzer** | `entities`, `articles`, `claims`, `entity_mentions`, `analyses` | Analysis/extracted data |

## Stories

### Phase A: Knowledge Base Realignment

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| UI-3.A.1 | Reconfigure EntityBrowser for KB Tables | P0 | 3 pts | ✅ Complete |
| UI-3.A.2 | Implement Hierarchical KB Navigation | P0 | 5 pts | ✅ Complete |
| UI-3.A.3 | Update Entity Type Configs | P1 | 2 pts | ✅ Complete (merged into A.1) |
| UI-3.A.4 | Route Restructuring & Redirects | P1 | 2 pts | ✅ Complete |
| UI-3.A.5 | Phase A Documentation | P2 | 1 pt | ✅ Complete |
| UI-3.A.6 | Update Test Coverage | P1 | 2 pts | ✅ Complete (across A.1, A.2, A.4) |

**Phase A Total:** 15 story points

### Phase B: Article Analyzer Foundation

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| UI-3.B.1 | Article Analyzer Navigation Shell | P0 | 3 pts | ✅ Complete |
| UI-3.B.2 | Move Extracted Entities to Article Analyzer | P0 | 3 pts | ✅ Complete |
| UI-3.B.3 | Articles List Page | P1 | 3 pts | ✅ Complete |
| UI-3.B.4 | Hero Page Dual Navigation | P2 | 1 pt | ✅ Complete |

**Phase B Total:** 10 story points ✅

**Epic Total:** 25 story points ✅

### Dependency Graph

```
UI-3.A.1 (Reconfigure EntityBrowser)
    |
    +-- UI-3.A.2 (Hierarchical Nav) -- depends on reconfigured browser
    |
    +-- UI-3.A.3 (Entity Configs) -- parallel with A.2
            |
            +-- UI-3.A.4 (Routes) -- after configs updated
                    |
                    +-- UI-3.A.6 (Tests) -- after routes finalized
                            |
                            +-- UI-3.A.5 (Docs) -- after tests pass

UI-3.B.1 (Article Analyzer Shell) -- can start parallel with Phase A
    |
    +-- UI-3.B.2 (Move Entities) -- after shell exists
    |
    +-- UI-3.B.3 (Articles List) -- after shell exists
            |
            +-- UI-3.B.4 (Hero Page) -- after both sections exist
```

---

## Story Details

### UI-3.A.1: Reconfigure EntityBrowser for KB Tables

**Status:** ✅ Complete (2025-12-31)

**As a** user browsing the Knowledge Base,
**I want** to see authoritative data (persons, committees, government organizations),
**So that** I can explore verified reference information.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | EntityBrowser component accepts configuration for KB table endpoints |
| AC2 | "People" entity type fetches from `/api/persons` instead of `/api/entities` |
| AC3 | "Organizations" entity type fetches from `/api/government-orgs` |
| AC4 | "Committees" entity type fetches from `/api/committees` |
| AC5 | Column configurations updated to match KB table fields |
| AC6 | All existing UI-2 pattern features work (filtering, sorting, pagination) |

#### Technical Notes

- Modify `EntityTypeConfig` interface to support different API endpoints
- Create KB-specific configurations separate from analysis layer configs
- Preserve EntityBrowser component - only change configuration

---

### UI-3.A.2: Implement Hierarchical KB Navigation

**Status:** ✅ Complete (2025-12-31)

**As a** user exploring the Knowledge Base,
**I want** hierarchical navigation (Government > Branches > Departments),
**So that** I can understand organizational relationships.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | KB landing page shows top-level categories (U.S. Federal Government, etc.) |
| AC2 | U.S. Federal Government expands to show Branches (Executive, Legislative, Judicial) |
| AC3 | Each branch shows its organizational hierarchy |
| AC4 | Navigation breadcrumbs show current location in hierarchy |
| AC5 | Can navigate to any level directly via URL |
| AC6 | Mobile-responsive navigation (collapsible on small screens) |

#### Technical Notes

- Leverage existing HierarchyView pattern from UI-2
- Use `government_organizations.parent_id` for tree structure
- Consider lazy-loading for deep hierarchies

---

### UI-3.A.4: Route Restructuring & Redirects

**Status:** ✅ Complete (2025-12-31)

**As a** user with bookmarked pages,
**I want** old routes to redirect to new locations,
**So that** my bookmarks and shared links continue to work.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Old `/knowledge-base/[entityType]` routes redirect to appropriate new locations |
| AC2 | Redirects use 307 (temporary) status during transition period |
| AC3 | All old factbase routes continue to work via redirects |
| AC4 | No 404 errors for any previously valid routes |

#### Technical Notes

**Redirect Strategy (next.config.js):**

```javascript
// next.config.js
module.exports = {
  async redirects() {
    return [
      // Extracted entities → Article Analyzer
      {
        source: '/knowledge-base/:entityType(person|organization|event|location)',
        destination: '/article-analyzer/entities?type=:entityType',
        permanent: false,  // Use 307 during transition
      },
      // Old factbase routes
      {
        source: '/factbase/:path*',
        destination: '/knowledge-base/:path*',
        permanent: true,
      },
    ];
  },
};
```

---

### UI-3.A.6: Update Test Coverage

**Status:** ✅ Complete (2025-12-31) - Achieved across A.1, A.2, A.4

**As a** developer,
**I want** tests updated for the new route structure,
**So that** we maintain test coverage and catch regressions.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | EntityBrowser tests updated for new KB table configurations |
| AC2 | Route redirect tests added to verify all redirects work |
| AC3 | All 166+ existing frontend tests continue to pass |
| AC4 | New Article Analyzer components have test coverage |
| AC5 | Test coverage thresholds maintained (30% lines, 50% branches) |

#### Technical Notes

- Update existing `EntityBrowser.test.tsx` for KB configurations
- Add integration tests for redirect behavior
- Ensure Vitest coverage thresholds pass in CI

---

### UI-3.B.1: Article Analyzer Navigation Shell

**Status:** ✅ Complete (2025-12-31)

**As a** user analyzing articles,
**I want** a dedicated Article Analyzer section,
**So that** I can access all analysis-related features in one place.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/article-analyzer` route exists and renders shell layout |
| AC2 | Shell includes sidebar navigation with: Analyze Article (placeholder), Articles, Entities |
| AC3 | Shell uses consistent styling with Knowledge Base section |
| AC4 | Main navigation updated to show both "Knowledge Base" and "Article Analyzer" |
| AC5 | Shell is accessible via keyboard navigation |

---

### UI-3.B.2: Move Extracted Entities to Article Analyzer

**Status:** ✅ Complete (2025-12-31)

**As a** user viewing extracted entities,
**I want** them under Article Analyzer (not Knowledge Base),
**So that** I understand these are analysis results, not authoritative data.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/article-analyzer/entities` shows EntityBrowser with `entities` table data |
| AC2 | Old `/knowledge-base/[entityType]` routes for extracted entities redirect to new location |
| AC3 | EntityBrowser configuration preserved from UI-2 |
| AC4 | Clear labeling indicates these are "Extracted Entities" |

---

## Acceptance Criteria (Epic Level)

1. **KB Data Source:** `/knowledge-base` routes fetch from authoritative tables (persons, committees, gov_orgs)
2. **Hierarchical Navigation:** Users can navigate KB by organizational hierarchy
3. **Article Analyzer Exists:** `/article-analyzer` section accessible with entities and articles pages
4. **Extracted Entities Moved:** Extracted entity browsing at `/article-analyzer/entities`
5. **No Broken Links:** All old routes redirect appropriately
6. **Architecture Alignment:** Frontend matches `architecture.md` Section 8 structure
7. **Pattern Preservation:** All UI-2 patterns (EntityBrowser, EntityDetail, HierarchyView) still work

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| User confusion from navigation change | MEDIUM | MEDIUM | Clear redirects, announcement banner |
| Scope creep into Phase 4 features | MEDIUM | LOW | Strict scope boundaries, placeholder pages |
| API endpoint changes needed | LOW | LOW | Backend APIs already support needed queries |
| Pattern components need modification | MEDIUM | LOW | Patterns are designed for configuration |

## Definition of Done

- [x] All Phase A stories complete and passing QA
- [x] All Phase B stories complete and passing QA
- [x] No broken links (automated check) - 396 tests pass
- [x] Architecture Section 8 compliance verified (KB navigation)
- [x] Documentation updated (this epic, ROADMAP)
- [x] Redirects in place for old routes (UI-3.A.4)
- [x] Hero page shows dual navigation (UI-3.B.4)

## Related Documentation

- [Architecture v2.5](../../architecture/architecture.md) - Section 2 (Data Architecture) and Section 8 (Frontend)
- [Sprint Change Proposal](../../qa/SCP-2025-12-30-001-frontend-realignment.md) - Change analysis
- [UI-2 Epic](../UI-2/UI-2.epic-knowledge-explorer.md) - Superseded epic (patterns preserved)
- [ROADMAP](../../ROADMAP.md) - Project roadmap

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Date:** 2025-12-30

### Review Summary

The epic is architecturally sound and correctly addresses the dual-layer data model clarification. The phased approach (Phase A: KB Realignment, Phase B: Article Analyzer) allows for incremental delivery with clear value at each phase.

### Modifications Applied

1. **Added UI-3.A.6: Update Test Coverage** (2 pts) - Ensures test coverage is maintained during route restructuring
2. **Clarified people sub-routes** - Using query params (`/knowledge-base/people?type=members`) preserves single-component architecture
3. **Documented redirect strategy** - Added Next.js redirect configuration example to UI-3.A.4

### Technical Alignment

- Route structure aligns with `architecture.md` Section 8
- Pattern reuse from UI-2 is architecturally correct
- Data layer separation (KB tables vs entities table) properly reflected in routes

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-30 | 1.0 | Initial epic creation from Sprint Change Proposal | Sarah (PO) |
| 2025-12-30 | 1.1 | Architect review: added UI-3.A.6, clarified routes, added redirect strategy | Winston (Architect) |
| 2025-12-31 | 2.0 | Phase A Complete: A.1 (46 tests), A.2 (43 tests), A.3 (merged into A.1), A.4 (17 tests), A.5 (docs), A.6 (achieved across stories) | James (Dev) |
| 2025-12-31 | 3.0 | **EPIC COMPLETE** - Phase B Complete: B.1 (33 tests), B.2 (32 tests), B.3 (41 tests), B.4 (21 tests). Total: 396 tests, 25 story points delivered | James (Dev) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2025-12-30 | DRAFTED |
| Architect | Winston | 2025-12-30 | **APPROVED** |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
