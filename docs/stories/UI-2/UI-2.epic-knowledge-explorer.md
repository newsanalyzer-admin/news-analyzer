# Epic UI-2: Knowledge Explorer UI Refactoring

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | UI-2 |
| **Epic Name** | Knowledge Explorer UI Refactoring |
| **Epic Type** | UI/UX Refactoring |
| **Priority** | HIGH |
| **Status** | Ready for Development |
| **Created** | 2025-12-22 |
| **Owner** | Sarah (PO) |
| **Depends On** | UI-1 Complete (uses data imports from UI-1.9, 1.10, 1.11) |
| **Supersedes** | UI-1 page components (replaces bespoke pages with reusable patterns) |
| **Source Brief** | `docs/briefs/knowledge-explorer-ui-brief.md` |

## Executive Summary

Replace the current fragmented factbase UI with a unified **Knowledge Explorer** - a pattern-based, extensible interface for exploring authoritative data across all entity types. This refactoring addresses organic UI growth that created overlapping navigation paths and inconsistent implementations.

The core innovation is establishing **reusable UI patterns** (EntityBrowser, EntityDetail, HierarchyView) that adapt based on entity type configuration rather than requiring new components for each data domain.

## Relationship to UI-1

**UI-1** (Public Navigation & Factbase Pages) delivered the initial `/factbase` implementation with bespoke page components. **UI-2** builds on this foundation:

| Aspect | UI-1 Delivered | UI-2 Action |
|--------|----------------|-------------|
| **Data Imports** | Legislative orgs, Judicial orgs, Federal Judges | **Uses** - these data sources are prerequisites |
| **Page Components** | Congressional Members, Executive Appointees, Federal Judges, Gov Org pages | **Supersedes** - replaces with reusable EntityBrowser/EntityDetail |
| **Navigation** | PublicSidebar with static menu | **Supersedes** - replaces with KnowledgeExplorer shell |
| **Routes** | `/factbase/*` pages | **Preserves** - redirects to `/knowledge-base/*` |
| **Shared Components** | SidebarMenuItem, BaseSidebar | **Keeps** - may reuse for other areas |

**Key Insight:** UI-2 doesn't invalidate UI-1's value - it evolves the UI architecture while preserving all data and user expectations (via redirects).

## Business Value

### Why This Epic Matters

1. **User Clarity** - Eliminate confusion from overlapping paths ("Explore Factbase" vs "View Government Orgs")
2. **Developer Efficiency** - Reusable patterns reduce effort to add new entity types from days to hours
3. **Educational Value** - Unified, intentional UX improves platform's civic education mission
4. **Extensibility** - Architecture ready for corporations, universities, and future fact domains

### Success Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Navigation entry points | 1 unified path | 2+ overlapping paths |
| Time to add new entity type | < 1 day | Multiple days |
| Reusable pattern components | 4 components | 0 (all bespoke) |
| Entity types using patterns | 3+ | 0 |

## Scope

### In Scope

- Unified Knowledge Explorer entry point and navigation shell
- Entity Type Selector component
- View Mode Selector component (List/Grid, Hierarchy)
- Reusable EntityBrowser pattern (list/grid with filtering, sorting, pagination)
- Reusable EntityDetail pattern (attributes + source citations)
- Reusable HierarchyView pattern (tree visualization)
- Cross-entity search
- Migration of Government Orgs and Federal Judges to new patterns
- Deprecation of old routes with redirects

### Out of Scope (Phase 2)

- Relationship Graph visualization
- Timeline view
- Advanced faceted filtering
- Entity comparison views
- User annotations/bookmarks
- Export functionality

## Architecture

### Component Hierarchy

```
┌─────────────────────────────────────────────────────────────┐
│                    Knowledge Explorer                        │
│                   /knowledge-base (unified entry)            │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────┐  ┌─────────────────────────────┐   │
│  │ EntityTypeSelector  │  │     ViewModeSelector        │   │
│  │ [Orgs][People][...]│  │ [List/Grid][Hierarchy]      │   │
│  └─────────────────────┘  └─────────────────────────────┘   │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────────────────────────────────────────────┐│
│  │                    SearchBar                            ││
│  │           (searches current entity type)                ││
│  └─────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────┤
│                    Adaptive Content Area                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │EntityBrowser│  │EntityDetail │  │   HierarchyView     │  │
│  │(list/grid)  │  │(detail page)│  │   (tree view)       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Configuration-Driven Entity Types

```typescript
// Entity type configuration drives UI behavior
interface EntityTypeConfig {
  id: string;                    // 'government-orgs', 'people', etc.
  label: string;                 // Display name
  apiEndpoint: string;           // REST endpoint
  supportedViews: ViewMode[];    // ['list', 'hierarchy', etc.]
  columns: ColumnConfig[];       // List view columns
  detailSections: SectionConfig[]; // Detail view sections
  filterOptions: FilterConfig[]; // Available filters
  searchFields: string[];        // Fields to search
}
```

### Route Structure

| Route | Component | Description |
|-------|-----------|-------------|
| `/knowledge-base` | KnowledgeExplorer | Unified entry point |
| `/knowledge-base/:entityType` | EntityBrowser | List/grid view for entity type |
| `/knowledge-base/:entityType/:id` | EntityDetail | Detail view for specific entity |
| `/knowledge-base/:entityType/hierarchy` | HierarchyView | Tree view (if supported) |

### Migration Redirects

| Old Route | New Route |
|-----------|-----------|
| `/factbase` | `/knowledge-base` |
| `/factbase/organizations/*` | `/knowledge-base/organizations` |
| `/factbase/people/federal-judges` | `/knowledge-base/people?type=judges` |
| `/factbase/people/congressional-members` | `/knowledge-base/people?type=members` |
| `/factbase/people/executive-appointees` | `/knowledge-base/people?type=appointees` |

## Stories

### UI-2.1: Knowledge Explorer Shell & Navigation

**Goal:** Create the unified entry point and navigation framework

**Scope:**
- Create `/knowledge-base` route with KnowledgeExplorer layout component
- Implement EntityTypeSelector component
- Implement ViewModeSelector component
- Update hero page to single "Knowledge Base" entry point
- Remove/redirect deprecated navigation paths

**Acceptance Criteria:**
- [ ] Single "Knowledge Base" link on hero page
- [ ] EntityTypeSelector shows available entity types
- [ ] ViewModeSelector shows available views for selected type
- [ ] URL updates reflect entity type and view mode selections
- [ ] Old factbase routes redirect to new /knowledge-base routes

---

### UI-2.2: EntityBrowser Pattern Component

**Goal:** Create reusable list/grid component for browsing entities

**Scope:**
- Create EntityBrowser component with configuration-driven columns
- Implement sorting, filtering, and pagination
- Support list and grid display modes
- Create entity type configuration system

**Acceptance Criteria:**
- [ ] EntityBrowser renders any entity type based on configuration
- [ ] Sorting works on configured columns
- [ ] Pagination handles large datasets
- [ ] List/grid toggle works
- [ ] Filters render based on entity type configuration

---

### UI-2.3: EntityDetail Pattern Component

**Goal:** Create reusable detail view component

**Scope:**
- Create EntityDetail component with configuration-driven sections
- Implement source attribution display (expandable)
- Support related entities display
- Create consistent header/metadata pattern

**Acceptance Criteria:**
- [ ] EntityDetail renders any entity type based on configuration
- [ ] Source citations displayed as expandable icons
- [ ] Related entities linked appropriately
- [ ] Consistent visual pattern across entity types

---

### UI-2.4: HierarchyView Pattern Component

**Goal:** Create reusable tree visualization for hierarchical entities

**Scope:**
- Create HierarchyView component
- Support expand/collapse navigation
- Link nodes to EntityDetail views
- Handle large hierarchies efficiently

**Acceptance Criteria:**
- [ ] Tree visualization renders hierarchical data
- [ ] Nodes expandable/collapsible
- [ ] Clicking node navigates to detail view
- [ ] Performance acceptable for 1000+ node trees

---

### UI-2.5: Cross-Entity Search

**Goal:** Implement search within current entity type

**Scope:**
- Create SearchBar component for Knowledge Explorer
- Implement backend search endpoint (if not exists)
- Display search results within current entity type
- Support keyboard navigation

**Acceptance Criteria:**
- [ ] Search bar visible in Knowledge Explorer header
- [ ] Search scoped to current entity type
- [ ] Results displayed in EntityBrowser format
- [ ] Clicking result navigates to EntityDetail

---

### UI-2.6: Migrate Government Organizations

**Goal:** Refactor Government Orgs pages to use new patterns

**Scope:**
- Create entity type configuration for Government Organizations
- Migrate list view to EntityBrowser
- Migrate detail view to EntityDetail
- Migrate hierarchy view to HierarchyView
- Remove old bespoke components

**Acceptance Criteria:**
- [ ] Gov Orgs accessible via `/knowledge-base/organizations`
- [ ] All existing functionality preserved
- [ ] Uses shared pattern components
- [ ] Old routes redirect correctly

---

### UI-2.7: Migrate People (Judges, Members, Appointees)

**Goal:** Refactor ALL People pages to use new patterns

**Scope:**
- Create entity type configuration for People with subtype support
- Migrate Federal Judges to EntityBrowser/EntityDetail
- Migrate Congressional Members to EntityBrowser/EntityDetail
- Migrate Executive Appointees to EntityBrowser/EntityDetail
- Support filtering by person type via URL param

**Acceptance Criteria:**
- [ ] Judges accessible via `/knowledge-base/people?type=judges`
- [ ] Congressional Members accessible via `/knowledge-base/people?type=members`
- [ ] Executive Appointees accessible via `/knowledge-base/people?type=appointees`
- [ ] All existing functionality preserved for each person type
- [ ] Uses shared pattern components
- [ ] Old routes redirect correctly:
  - `/factbase/people/federal-judges` → `/knowledge-base/people?type=judges`
  - `/factbase/people/congressional-members` → `/knowledge-base/people?type=members`
  - `/factbase/people/executive-appointees` → `/knowledge-base/people?type=appointees`

---

### UI-2.8: Cleanup & Documentation

**Goal:** Remove deprecated code and document new architecture

**Scope:**
- Remove old factbase page components
- Update source-tree.md with new component structure
- Document entity type configuration process
- Update any affected tests

**Acceptance Criteria:**
- [ ] No dead code from old factbase implementation
- [ ] Architecture documentation updated
- [ ] "Adding a new entity type" guide created
- [ ] All tests passing

## Story Sequencing

```
UI-2.1 (Shell)
    │
    ├──► UI-2.2 (EntityBrowser) ──┐
    │                             │
    ├──► UI-2.3 (EntityDetail) ───┼──► UI-2.6 (Migrate Gov Orgs)
    │                             │           │
    └──► UI-2.4 (HierarchyView) ──┘           │
                                              ▼
    UI-2.5 (Search) ──────────────────► UI-2.7 (Migrate Judges)
                                              │
                                              ▼
                                        UI-2.8 (Cleanup)
```

## Compatibility Requirements

- [ ] Existing backend APIs remain unchanged
- [ ] Database schema unchanged
- [ ] Admin import functionality unaffected
- [ ] All existing data accessible through new UI
- [ ] Old URLs redirect to new equivalents

## Risk Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Pattern over-generalization | Patterns too generic, lose usability | Start with concrete types, extract patterns iteratively |
| Scope creep | Graph/timeline added before core solid | Strict MVP; defer Phase 2 features |
| Migration disruption | Broken bookmarks, user confusion | Implement redirects; staged rollout |
| Performance regression | Slower than bespoke components | Performance test each pattern component |

## Rollback Plan

If critical issues discovered post-deployment:
1. Redirect `/knowledge-base/*` back to old `/factbase/*` routes
2. Re-enable old navigation links
3. Old components preserved until UI-2.8 cleanup story

## Definition of Done

- [ ] All 8 stories completed with acceptance criteria met
- [ ] All existing functionality accessible through new UI
- [ ] Pattern components reusable across 2+ entity types
- [ ] Old routes redirect correctly
- [ ] Documentation updated
- [ ] No regression in existing features
- [ ] Performance targets met (< 2s page load)

## Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Terminology** | "Knowledge Base" | Conveys structured, authoritative information |
| **Search scope** | Current entity type only | Focused lookup with less noise; users can navigate to different types |
| **Source citations** | Expandable | Clean UI with icon; click to reveal source details |

## References

- Project Brief: `docs/briefs/knowledge-explorer-ui-brief.md`
- Existing UI-1 Epic: `docs/stories/UI-1/UI-1.epic-public-navigation-ux.md`
- Architecture Docs: `docs/architecture/`
- Current Frontend: `frontend/src/app/factbase/`

---

*Created by Sarah (PO) - BMAD Workflow*
