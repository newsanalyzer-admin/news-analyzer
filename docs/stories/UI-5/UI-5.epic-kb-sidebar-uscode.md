# Epic UI-5: Knowledge Base Sidebar Reorganization & U.S. Code Integration

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | UI-5 |
| **Epic Name** | KB Sidebar Reorganization & U.S. Code Integration |
| **Epic Type** | UI/UX Enhancement |
| **Priority** | HIGH |
| **Status** | Complete |
| **Created** | 2026-01-02 |
| **Owner** | Sarah (PO) |
| **Depends On** | UI-4 Complete (sidebar infrastructure exists) |
| **Triggered By** | User feedback: Sidebar structure needs reorganization; U.S. Code missing from v1 migration |

## Executive Summary

Reorganize the Knowledge Base sidebar navigation to better represent the U.S. Federal Government structure and restore the U.S. Code browsing functionality that was lost during the v1 → v2 migration.

### Current State

The KB sidebar currently shows:
```
Knowledge Base
├── Government
│   ├── Executive Branch
│   ├── Legislative Branch
│   └── Judicial Branch
├── People (...)
├── Committees
└── Organizations
```

### Target State

```
Knowledge Base
├── U.S. Federal Government          ← renamed from "Government"
│   ├── Branches                     ← new grouping (non-clickable)
│   │   ├── Executive Branch
│   │   ├── Legislative Branch
│   │   └── Judicial Branch
│   └── U.S. Code (Federal Laws)     ← new section (v1 feature recovery)
├── People (...)
├── Committees
└── Organizations
```

### Why This Change Is Needed

1. **Clarity**: "U.S. Federal Government" is more descriptive than "Government"
2. **Structure**: "Branches" grouping creates logical hierarchy for future expansion
3. **Feature Recovery**: U.S. Code (statutes) browsing existed in v1 but was lost during migration
4. **Backend Ready**: `Statute` model, `StatuteController`, and admin UI already exist

## Business Value

### Why This Epic Matters

1. **Terminology Precision** - "U.S. Federal Government" matches official naming
2. **Future-Proofing** - "Branches" grouping allows adding branch info pages later
3. **v1 Parity** - Restores U.S. Code browsing that users expect
4. **Component Reuse** - Admin's `UsCodeTreeView` can be adapted for public use

### Success Metrics

| Metric | Target |
|--------|--------|
| Sidebar shows reorganized structure | Yes |
| "Branches" is non-clickable grouping | Yes |
| U.S. Code browse page works | Yes |
| All existing routes still work | Yes |

## Scope

### In Scope

1. **Sidebar Navigation Restructure**
   - Rename "Government" → "U.S. Federal Government"
   - Add "Branches" as non-clickable grouping label
   - Nest Executive/Legislative/Judicial under Branches
   - Add "U.S. Code (Federal Laws)" menu item

2. **Public U.S. Code Browse Page**
   - Create `/knowledge-base/government/us-code` route
   - Read-only hierarchical browse (Title → Chapter → Section)
   - Reuse/adapt `UsCodeTreeView` component from admin
   - Link to official sources (uscode.house.gov)

### Out of Scope

- Changes to admin U.S. Code import functionality
- Backend API changes (APIs already exist)
- U.S. Code search functionality (future enhancement)
- Clickable "Branches" landing page (explicitly deferred per requirements)

## Architecture

### Route Structure

| Route | Component | Data Source | Description |
|-------|-----------|-------------|-------------|
| `/knowledge-base/government/us-code` | USCodeBrowserPage | `/api/statutes/*` | Public U.S. Code browse |

### Menu Configuration Changes

```typescript
// lib/menu-config.ts - Target structure
{
  label: 'U.S. Federal Government',  // renamed
  icon: Building,
  href: '/knowledge-base/government',
  children: [
    {
      label: 'Branches',  // new grouping
      icon: GitBranch,    // or similar
      // NO href - non-clickable grouping
      children: [
        { label: 'Executive Branch', href: '/knowledge-base/government/executive', ... },
        { label: 'Legislative Branch', href: '/knowledge-base/government/legislative', ... },
        { label: 'Judicial Branch', href: '/knowledge-base/government/judicial', ... },
      ],
    },
    {
      label: 'U.S. Code (Federal Laws)',  // new
      icon: BookOpen,
      href: '/knowledge-base/government/us-code',
    },
  ],
}
```

### Backend API Endpoints (Already Exist)

| Endpoint | Purpose | Status |
|----------|---------|--------|
| `GET /api/statutes/stats` | Get statute statistics | Exists |
| `GET /api/statutes/titles` | List imported titles | Exists |
| `GET /api/statutes/titles/{num}/chapters` | Get chapters for title | Exists |
| `GET /api/statutes/chapters/{id}/sections` | Get sections for chapter | Exists |

### Component Reuse

| Admin Component | Public Adaptation |
|-----------------|-------------------|
| `UsCodeTreeView` | Reuse directly or create simplified `PublicUsCodeTree` |
| `UsCodeImportButton` | Not needed (admin only) |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| UI-5.1 | Reorganize KB Sidebar Navigation | P0 | 2 pts | Draft |
| UI-5.2 | Public U.S. Code Browse Page | P0 | 3 pts | Draft |

**Epic Total:** 5 story points

### Dependency Graph

```
UI-5.1 (Sidebar Restructure)
    |
    └──> UI-5.2 (U.S. Code Page) -- depends on menu item existing
```

---

## Story Details

### UI-5.1: Reorganize KB Sidebar Navigation

**Status:** Draft

**As a** user browsing the Knowledge Base,
**I want** the sidebar to show "U.S. Federal Government" with organized submenus,
**So that** I can understand the navigation structure and find U.S. Code content.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | "Government" is renamed to "U.S. Federal Government" in sidebar |
| AC2 | "Branches" appears as a non-clickable grouping label under U.S. Federal Government |
| AC3 | Executive Branch, Legislative Branch, Judicial Branch are nested under "Branches" |
| AC4 | "U.S. Code (Federal Laws)" appears as a sibling to "Branches" under U.S. Federal Government |
| AC5 | Existing routes (`/knowledge-base/government/*`) continue to work |
| AC6 | Breadcrumbs update to show new hierarchy labels |

#### Technical Notes

**Files to Modify:**
- `frontend/src/lib/menu-config.ts` - Update `publicMenuConfig` structure
- `frontend/src/components/knowledge-base/KBBreadcrumbs.tsx` - Update label mappings

**Menu Config Pattern for Non-Clickable Groups:**
```typescript
{
  label: 'Branches',
  icon: GitBranch,
  // Omit href to make non-clickable
  children: [...]
}
```

---

### UI-5.2: Public U.S. Code Browse Page

**Status:** Draft

**As a** user exploring the Knowledge Base,
**I want** to browse U.S. Code (federal statutes) in a hierarchical tree view,
**So that** I can explore federal laws by Title, Chapter, and Section.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/knowledge-base/government/us-code` route exists and renders browse page |
| AC2 | Page shows list of imported U.S. Code titles (from `/api/statutes/titles`) |
| AC3 | Each title expands to show chapters (lazy-loaded from `/api/statutes/titles/{num}/chapters`) |
| AC4 | Each chapter expands to show sections (lazy-loaded) |
| AC5 | Sections show section number and heading text |
| AC6 | Link to official source (uscode.house.gov) provided |
| AC7 | Empty state shown if no titles imported ("No U.S. Code data available") |
| AC8 | Page is read-only (no import functionality - that's admin only) |
| AC9 | Breadcrumbs show: Knowledge Base > U.S. Federal Government > U.S. Code |

#### Technical Notes

**Files to Create:**
- `frontend/src/app/knowledge-base/government/us-code/page.tsx` - Page component

**Component Strategy:**
- Option A: Reuse `UsCodeTreeView` directly from admin (may need to extract to shared location)
- Option B: Create simplified `PublicUsCodeTree` with read-only behavior

**API Calls:**
```typescript
// Fetch titles on mount
const titles = await fetch('/api/statutes/titles').then(r => r.json());

// Fetch chapters when title expanded
const chapters = await fetch(`/api/statutes/titles/${titleNum}/chapters`).then(r => r.json());

// Fetch sections when chapter expanded
const sections = await fetch(`/api/statutes/chapters/${chapterId}/sections`).then(r => r.json());
```

---

## Acceptance Criteria (Epic Level)

1. **Sidebar Structure:** KB sidebar shows new hierarchy with U.S. Federal Government, Branches grouping, and U.S. Code
2. **Non-Clickable Grouping:** "Branches" is a label only, not navigable
3. **U.S. Code Accessible:** Users can browse imported statutes via `/knowledge-base/government/us-code`
4. **Feature Parity:** U.S. Code browsing matches v1 functionality (read-only tree view)
5. **No Broken Routes:** All existing KB routes continue to work
6. **Tests Updated:** Test coverage for new components and menu structure

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| No U.S. Code data imported | LOW | MEDIUM | Show friendly empty state with admin instructions |
| Menu component doesn't support non-clickable groups | MEDIUM | LOW | Verify `BaseSidebar` handles items without href |
| Breadcrumb labels out of sync | LOW | MEDIUM | Update label mapping in KBBreadcrumbs |

## Definition of Done

- [x] UI-5.1 complete: Sidebar restructured
- [x] UI-5.2 complete: U.S. Code browse page works
- [x] All existing tests pass
- [x] New tests added for U.S. Code page
- [x] Breadcrumbs display correct hierarchy
- [x] Manual testing on mobile and desktop
- [x] ROADMAP.md updated with UI-5 entry

## Related Documentation

- [Admin US Code Page](../../frontend/src/app/admin/factbase/regulations/us-code/page.tsx)
- [UsCodeTreeView Component](../../frontend/src/components/admin/UsCodeTreeView.tsx)
- [Statute Model](../../backend/src/main/java/org/newsanalyzer/model/Statute.java)
- [StatuteController](../../backend/src/main/java/org/newsanalyzer/controller/StatuteController.java)
- [UI-4 Epic (predecessor)](../UI-4/UI-4.epic-sidebar-integration.md)
- [Menu Config](../../frontend/src/lib/menu-config.ts)

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Date:** 2026-01-02

### Review Summary

The epic is architecturally sound and demonstrates excellent planning. It correctly identifies a v1 feature gap, leverages existing backend infrastructure, and proposes minimal-impact frontend changes with strong component reuse.

### Technical Verification

1. **Non-Clickable Grouping:** VERIFIED - `SidebarMenuItem.tsx` (lines 155-186) already handles items without `href` as expandable buttons. No modifications needed.

2. **Backend APIs:** VERIFIED - All required endpoints exist in `StatuteController.java`:
   - `GET /api/statutes/titles` (line 263)
   - `GET /api/statutes/title/{num}/hierarchy` (line 296)
   - `GET /api/statutes/stats` (line 415)

3. **Component Reuse:** APPROVED - Import `UsCodeTreeView` from admin for quick delivery; add tech debt item for extraction to shared location.

### Modifications Applied

1. **UI-5.1 AC7 Added:** "In collapsed sidebar, U.S. Federal Government shows tooltip with full path"
2. **UI-5.1 Technical Note Added:** Verification that SidebarMenuItem already supports non-clickable groups
3. **UI-5.2 Strategy Clarified:** Use Option A (import from admin) with tech debt tracking for Option B refactor

### Estimate Validation

| Story | Estimate | Assessment |
|-------|----------|------------|
| UI-5.1 | 2 pts | ✅ Accurate |
| UI-5.2 | 3 pts | ✅ Accurate |

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-02 | 1.0 | Initial epic creation from user feedback | Sarah (PO) |
| 2026-01-02 | 1.1 | Architect review: verified technical feasibility, added AC7, clarified component strategy | Winston (Architect) |
| 2026-01-03 | 1.2 | Epic COMPLETE: Both stories implemented, 19 new tests, 585 frontend tests passing | James (Dev) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2026-01-02 | DRAFTED |
| Architect | Winston | 2026-01-02 | **APPROVED** |
| Developer | James | 2026-01-03 | **COMPLETE** |

---

*End of Epic Document*
