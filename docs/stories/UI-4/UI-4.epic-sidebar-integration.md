# Epic UI-4: Public Sidebar Integration

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | UI-4 |
| **Epic Name** | Public Sidebar Integration |
| **Epic Type** | UI/UX Implementation |
| **Priority** | HIGH |
| **Status** | APPROVED |
| **Created** | 2026-01-01 |
| **Owner** | Sarah (PO) |
| **Depends On** | UI-3 Complete (layout structure exists) |
| **Triggered By** | User feedback: "There is no sidebar in the UI" |

## Executive Summary

The public-facing sections (Knowledge Base and Article Analyzer) are missing sidebar navigation, despite sidebar components being fully implemented and tested. The Admin section correctly uses `AdminSidebar` with a responsive layout pattern, but the public sections use header-only navigation (dropdowns and horizontal tabs).

This epic integrates the existing `PublicSidebar` component into the Knowledge Base and Article Analyzer layouts, matching the admin section's proven sidebar pattern.

### The Gap

| Component | Status | Location |
|-----------|--------|----------|
| `BaseSidebar` | Built & Tested | `components/sidebar/BaseSidebar.tsx` |
| `SidebarMenuItem` | Built & Tested | `components/sidebar/SidebarMenuItem.tsx` |
| `AdminSidebar` | Integrated in `/admin` | `components/admin/AdminSidebar.tsx` |
| `PublicSidebar` | **Built but NEVER imported** | `components/public/PublicSidebar.tsx` |
| `publicSidebarStore` | **Built but NEVER used** | `stores/publicSidebarStore.ts` |
| `publicMenuItemsFlat` | **Built but only referenced by unused PublicSidebar** | `lib/menu-config.ts` |

### Root Cause

The UI-2 and UI-3 epics documented sidebar-based navigation as deliverables, but the actual implementation pivoted to:
- `KnowledgeExplorer` component with header dropdowns (EntityTypeSelector, ViewModeSelector)
- `ArticleAnalyzerShell` component with horizontal tab navigation

This was a deviation from the documented architecture that wasn't captured in epic completion notes.

## Business Value

### Why This Epic Matters

1. **Consistent User Experience** - Public sections should match Admin's polished sidebar pattern
2. **Mobile Usability** - Sidebar provides better mobile navigation than header dropdowns
3. **Discoverability** - Hierarchical sidebar reveals navigation structure at a glance
4. **Architecture Compliance** - Matches documented architecture Section 8 navigation structure
5. **Component Reuse** - Leverages existing, tested components instead of creating new patterns

### Success Metrics

| Metric | Target |
|--------|--------|
| Knowledge Base has sidebar | Yes |
| Article Analyzer has sidebar | Yes |
| Mobile sidebar works (slide-in overlay) | Yes |
| Desktop sidebar works (fixed, collapsible) | Yes |
| Existing header functionality preserved | Yes |
| All existing tests pass | Yes |

## Scope

### In Scope

1. **Knowledge Base Layout Refactor**
   - Replace header-only navigation with sidebar layout pattern
   - Integrate `PublicSidebar` component
   - Preserve breadcrumbs and search functionality
   - Mobile responsive sidebar (slide-in overlay)
   - Desktop collapsible sidebar

2. **Article Analyzer Layout Refactor**
   - Create `ArticleAnalyzerSidebar` component
   - Replace horizontal tab navigation with sidebar
   - Preserve header links (KB, Admin)
   - Mobile responsive sidebar

3. **Menu Configuration Updates**
   - Update `publicMenuItemsFlat` to match current routes
   - Create Article Analyzer menu configuration
   - Ensure active state detection works

### Out of Scope

- Changes to Admin sidebar (already working)
- New navigation destinations
- Backend API changes
- New page content

## Architecture

### Target Layout Pattern

Following the working Admin layout pattern (`app/admin/layout.tsx`):

```
+------------------+----------------------------------------+
| Mobile Header    | [Hamburger] [Section Title]            |
| (md:hidden)      |                                        |
+------------------+----------------------------------------+
| Sidebar          | Main Content                           |
| (fixed left)     |                                        |
| - Collapsible    | - Breadcrumbs                          |
| - Mobile overlay | - Page content                         |
| - Desktop fixed  | - Search (if applicable)               |
+------------------+----------------------------------------+
```

### Component Hierarchy

```
KnowledgeBaseLayout
├── Mobile Header (hamburger + title)
├── Mobile Backdrop (overlay when open)
├── PublicSidebar (slide-in on mobile, fixed on desktop)
└── Main Content
    ├── Breadcrumbs
    └── {children}

ArticleAnalyzerLayout
├── Mobile Header (hamburger + title)
├── Mobile Backdrop
├── ArticleAnalyzerSidebar (new component)
└── Main Content
    ├── Breadcrumbs
    └── {children}
```

### Files to Modify/Create

| File | Action | Description |
|------|--------|-------------|
| `components/layout/SidebarLayout.tsx` | **CREATE** | Shared responsive sidebar layout component |
| `app/knowledge-base/layout.tsx` | **MODIFY** | Use SidebarLayout with PublicSidebar |
| `app/article-analyzer/layout.tsx` | **MODIFY** | Use SidebarLayout with ArticleAnalyzerSidebar |
| `components/article-analyzer/ArticleAnalyzerSidebar.tsx` | **CREATE** | New sidebar for AA section |
| `stores/articleAnalyzerSidebarStore.ts` | **CREATE** | Independent store for AA sidebar state |
| `lib/menu-config.ts` | **MODIFY** | Add AA menu config, verify KB menu routes |
| `stores/publicSidebarStore.ts` | **VERIFY** | Already exists, ensure it's used |
| `components/knowledge-base/KnowledgeExplorer.tsx` | **DEPRECATE** | Keep for reference, remove from layout |
| `components/article-analyzer/ArticleAnalyzerShell.tsx` | **DEPRECATE** | Keep for reference, remove from layout |

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| UI-4.0 | Shared Sidebar Layout Component | P0 | 2 pts | Draft |
| UI-4.1 | Knowledge Base Sidebar Integration | P0 | 3 pts | Draft |
| UI-4.2 | Article Analyzer Sidebar | P0 | 3 pts | Draft |
| UI-4.3 | Menu Configuration Updates | P1 | 2 pts | Draft |
| UI-4.4 | Mobile Responsiveness Testing | P1 | 2 pts | Draft |
| UI-4.5 | Cleanup Deprecated Components | P2 | 1 pt | Draft |

**Epic Total:** 13 story points

### Dependency Graph

```
UI-4.0 (Shared Layout) ──┬──> UI-4.1 (KB Integration)
                         │
UI-4.3 (Menu Configs) ───┴──> UI-4.2 (AA Integration)
                                      │
                                      v
                              UI-4.4 (Mobile Testing)
                                      │
                                      v
                              UI-4.5 (Cleanup)
```

---

## Story Details

### UI-4.0: Shared Sidebar Layout Component

**Status:** Draft

**As a** developer,
**I want** a reusable sidebar layout component,
**So that** both KB and AA sections can share the same responsive layout pattern without code duplication.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `SidebarLayout` component created in `components/layout/` |
| AC2 | Component accepts: `children`, `sidebar`, `sectionTitle`, `sidebarStore` props |
| AC3 | Implements mobile header with hamburger toggle |
| AC4 | Implements mobile backdrop with click-to-close |
| AC5 | Implements mobile sidebar slide-in animation |
| AC6 | Implements desktop fixed sidebar with collapsible width |
| AC7 | Handles escape key to close mobile sidebar |
| AC8 | Handles body scroll lock when mobile sidebar open |
| AC9 | Main content area has responsive margin (ml-16/ml-64) |
| AC10 | Component is fully typed with TypeScript |

#### Technical Notes

```typescript
// components/layout/SidebarLayout.tsx
interface SidebarLayoutProps {
  children: React.ReactNode;
  sidebar: React.ReactNode;
  sectionTitle: string;
  store: {
    isCollapsed: boolean;
    isMobileOpen: boolean;
    toggleMobile: () => void;
    closeMobile: () => void;
  };
}
```

Extract the pattern from `app/admin/layout.tsx` (lines 1-97) into a reusable component.

---

### UI-4.1: Knowledge Base Sidebar Integration

**Status:** Draft

**As a** user browsing the Knowledge Base,
**I want** a collapsible sidebar navigation,
**So that** I can easily navigate the hierarchical KB structure.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/knowledge-base` layout uses `SidebarLayout` with `PublicSidebar` |
| AC2 | Desktop: Sidebar is fixed, collapsible (64px collapsed, 256px expanded) |
| AC3 | Mobile: Hamburger menu in header, sidebar slides in as overlay |
| AC4 | Mobile: Backdrop appears behind sidebar, clicking closes it |
| AC5 | Mobile: Escape key closes sidebar |
| AC6 | Mobile: Body scroll prevented when sidebar open |
| AC7 | Breadcrumbs remain visible in main content area |
| AC8 | All existing KB routes continue to work |
| AC9 | Uses `usePublicSidebarStore` for state management |
| AC10 | EntityTypeSelector, ViewModeSelector, and SearchBar preserved in content header |

#### Technical Notes

- Use `SidebarLayout` component from UI-4.0
- Pass `PublicSidebar` as the sidebar prop
- Move EntityTypeSelector/ViewModeSelector/SearchBar from KnowledgeExplorer to a content header within the main area
- Use `usePublicSidebarStore` (already exists and ready)

---

### UI-4.2: Article Analyzer Sidebar

**Status:** Draft

**As a** user in the Article Analyzer section,
**I want** sidebar navigation,
**So that** I can navigate between Analyze, Articles, and Entities views.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | New `ArticleAnalyzerSidebar` component created in `components/article-analyzer/` |
| AC2 | Sidebar includes: Analyze Article (disabled), Articles, Entities |
| AC3 | Footer includes link to Knowledge Base |
| AC4 | `/article-analyzer` layout uses `SidebarLayout` with `ArticleAnalyzerSidebar` |
| AC5 | Desktop: Fixed, collapsible sidebar |
| AC6 | Mobile: Slide-in overlay with backdrop |
| AC7 | Active state correctly highlights current route |
| AC8 | All existing AA routes continue to work |
| AC9 | Create `articleAnalyzerSidebarStore` for independent collapse state |

#### Technical Notes

Create new store for Article Analyzer sidebar:
```typescript
// stores/articleAnalyzerSidebarStore.ts
export const useArticleAnalyzerSidebarStore = create<SidebarState>()(
  persist(
    (set) => ({
      isCollapsed: false,
      isMobileOpen: false,
      toggle: () => set((state) => ({ isCollapsed: !state.isCollapsed })),
      toggleMobile: () => set((state) => ({ isMobileOpen: !state.isMobileOpen })),
      closeMobile: () => set({ isMobileOpen: false }),
    }),
    { name: 'aa-sidebar-storage' }
  )
);
```

Menu structure for ArticleAnalyzerSidebar:
```typescript
const menuItems: MenuItemData[] = [
  {
    label: 'Analyze Article',
    icon: FileText,
    href: '/article-analyzer/analyze',
    disabled: true, // Phase 4
  },
  {
    label: 'Articles',
    icon: List,
    href: '/article-analyzer/articles',
  },
  {
    label: 'Entities',
    icon: Database,
    href: '/article-analyzer/entities',
  },
];
```

---

### UI-4.3: Menu Configuration Updates

**Status:** Draft

**As a** developer,
**I want** menu configurations that match current routes,
**So that** sidebar navigation links work correctly.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `publicMenuItemsFlat` routes verified against actual KB routes |
| AC2 | Article Analyzer menu config added to `menu-config.ts` |
| AC3 | All menu hrefs resolve to valid routes |
| AC4 | Active state detection works for nested routes |

#### Technical Notes

Current `publicMenuItemsFlat` in `lib/menu-config.ts` needs verification:
- `/knowledge-base/people?type=members` - verify route exists
- `/knowledge-base/people?type=appointees` - verify route exists
- `/knowledge-base/people?type=judges` - verify route exists
- `/knowledge-base/organizations?branch=executive` - verify route exists
- etc.

---

### UI-4.4: Mobile Responsiveness Testing

**Status:** Draft

**As a** mobile user,
**I want** the sidebar to work smoothly on my device,
**So that** I can navigate the site without issues.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | KB sidebar tested on mobile viewport (< 768px) |
| AC2 | AA sidebar tested on mobile viewport |
| AC3 | Touch interactions work (swipe to close optional) |
| AC4 | Sidebar doesn't overlap content when collapsed on desktop |
| AC5 | Sidebar transitions are smooth (no jank) |
| AC6 | Focus management correct (focus trap in mobile overlay) |

#### Technical Notes

Test at breakpoints: 320px, 375px, 414px, 768px, 1024px, 1440px

---

### UI-4.5: Cleanup Deprecated Components

**Status:** Draft

**As a** developer,
**I want** unused code removed,
**So that** the codebase stays maintainable.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `KnowledgeExplorer` component usage removed from layouts |
| AC2 | `ArticleAnalyzerShell` component usage removed from layouts |
| AC3 | Components marked as deprecated or removed entirely |
| AC4 | No dead imports in layout files |
| AC5 | All tests updated to reflect new structure |

#### Technical Notes

Deprecation strategy:
- Option A: Delete components entirely if not used elsewhere
- Option B: Mark with `@deprecated` JSDoc and keep for one release

---

## Acceptance Criteria (Epic Level)

1. **KB Sidebar:** `/knowledge-base` has a working sidebar matching Admin pattern
2. **AA Sidebar:** `/article-analyzer` has a working sidebar
3. **Mobile:** Both sidebars work on mobile with slide-in overlay
4. **Desktop:** Both sidebars are fixed and collapsible
5. **Routes:** All existing routes continue to work
6. **Tests:** All existing tests pass, new tests added for sidebar integration
7. **Cleanup:** Deprecated header-only components removed or marked

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Breaking existing routes | HIGH | LOW | Preserve all route handlers, only change layout |
| Mobile UX regression | MEDIUM | LOW | Copy proven Admin pattern exactly |
| Test failures | MEDIUM | MEDIUM | Run full test suite before merging |
| Menu config mismatches | LOW | MEDIUM | Verify all routes before implementation |

## Definition of Done

- [ ] All 5 stories complete and passing QA
- [ ] Knowledge Base has working sidebar
- [ ] Article Analyzer has working sidebar
- [ ] Mobile responsiveness verified
- [ ] All existing tests pass
- [ ] New sidebar integration tests added
- [ ] ROADMAP.md updated with UI-4 entry
- [ ] Deprecated components cleaned up

## Related Documentation

- [Admin Layout (reference implementation)](../../frontend/src/app/admin/layout.tsx)
- [AdminSidebar (reference component)](../../frontend/src/components/admin/AdminSidebar.tsx)
- [PublicSidebar (unused but ready)](../../frontend/src/components/public/PublicSidebar.tsx)
- [UI-3 Epic (predecessor)](../UI-3/UI-3.epic-frontend-realignment.md)
- [Architecture Section 8](../../architecture/architecture.md)

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Date:** 2026-01-01

### Review Summary

The epic correctly identifies a genuine architectural gap. The documented architecture (Section 8) specifies a hierarchical, sidebar-based navigation structure for both Article Analyzer and Knowledge Base sections. The current implementation deviates from this by using header-only navigation patterns.

### Modifications Applied

1. **Added UI-4.0: Shared Sidebar Layout Component** (2 pts)
   - Both KB and AA layouts share 90% identical code
   - Extract to shared `SidebarLayout` component to avoid duplication
   - Reduced UI-4.1 and UI-4.2 estimates from 5 pts to 3 pts each

2. **Added AC9 to UI-4.2: Article Analyzer Store**
   - Article Analyzer needs its own sidebar store for independent collapse state
   - Create `articleAnalyzerSidebarStore` with persisted state

3. **Added AC10 to UI-4.1: Preserve Header Features**
   - EntityTypeSelector, ViewModeSelector, and SearchBar must be preserved
   - Move to content header within main area, not removed entirely

### Technical Alignment

| Aspect | Status |
|--------|--------|
| Architecture Section 8 compliance | ✅ Aligned |
| Component reuse principle | ✅ Excellent |
| Mobile-first approach | ✅ Correct |
| Accessibility (a11y) | ✅ Admin pattern is accessible |
| State management (Zustand) | ✅ Consistent with codebase |

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-01 | 1.0 | Initial epic creation from gap analysis | Sarah (PO) |
| 2026-01-01 | 1.1 | Architect review: added UI-4.0 (shared layout), added AC9/AC10, reduced estimates | Winston (Architect) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2026-01-01 | DRAFTED |
| Architect | Winston | 2026-01-01 | **APPROVED** |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
