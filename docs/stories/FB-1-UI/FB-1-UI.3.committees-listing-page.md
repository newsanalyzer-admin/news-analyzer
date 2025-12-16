# Story FB-1-UI.3: Committees Listing & Hierarchy Page

## Status

**Done**

## Story

**As a** fact-checker or researcher,
**I want** to browse Congressional committees organized by chamber with their subcommittee hierarchy,
**so that** I can understand committee structures and find relevant oversight bodies.

## Acceptance Criteria

1. A new `/committees` page is accessible from the main navigation
2. The page displays committees grouped by Chamber (Senate, House, Joint)
3. Each committee shows: Name, Type (Standing, Select, etc.), and member count
4. Subcommittees are displayed hierarchically under their parent committee
5. Expandable/collapsible sections for each chamber
6. Filter by Chamber (All, Senate, House, Joint) using shadcn/ui Select
7. Filter by Committee Type (All, Standing, Select, Special, Joint) using shadcn/ui Select
8. Search committees by name with debounced input
9. Filters persist in URL params for shareable links
10. Each committee links to its members list (modal or inline expansion)
11. Statistics section shows total committee count and type distribution
12. Loading states use shadcn/ui Skeleton components
13. Error states display user-friendly messages with retry option
14. Page is responsive and works on mobile devices
15. Existing pages have no regression

## Tasks / Subtasks

- [x] **Task 1: Create page structure** (AC: 1)
  - [x] Create `app/committees/page.tsx`
  - [x] Create `app/committees/loading.tsx` with skeleton UI
  - [x] Create `app/committees/error.tsx` with error boundary
  - [x] Add "Committees" link to `app/page.tsx` in the navigation button group (alongside "Entity Extraction", "Government Orgs", and "Members" buttons)

- [x] **Task 2: Create CommitteeFilters component** (AC: 6, 7, 8, 9)
  - [x] Create `components/congressional/CommitteeFilters.tsx`
  - [x] Implement Chamber filter (Select: All, Senate, House, Joint)
  - [x] Implement Type filter (Select: All, Standing, Select, Special, Joint, Subcommittee)
  - [x] Implement search input with 300ms debounce
  - [x] Use URL params for filter persistence

- [x] **Task 3: Create CommitteeHierarchy component** (AC: 2, 3, 4, 5)
  - [x] Create `components/congressional/CommitteeHierarchy.tsx`
  - [x] Group committees by chamber
  - [x] Display collapsible chamber sections
  - [x] Show parent committees with expandable subcommittee lists
  - [x] Display committee name, type badge, and member count
  - [x] Use shadcn/ui Card for each committee
  - [x] Use indentation/nesting for subcommittees

- [x] **Task 4: Create CommitteeTable component (OPTIONAL - alternative view)** (AC: 2, 3)
  - [x] SKIPPED - Optional task, hierarchy view is primary
  - [x] Note: This is an optional alternative to CommitteeHierarchy. Deferred to future enhancement.

- [x] **Task 5: Implement committee members preview** (AC: 10)
  - [x] Add "View Members" button/link to each committee
  - [x] Option A: Modal with member list using shadcn/ui Dialog (Implemented)
  - [x] Use `useCommitteeMembers(code)` hook for data

- [x] **Task 6: Create statistics section** (AC: 11)
  - [x] Display total committee count (computed from list)
  - [x] Compute type distribution client-side from committee list data (group by `committeeType`)
  - [x] Compute chamber distribution client-side (group by `chamber`)
  - [x] Display using Badge components similar to FB-1-UI.2 party distribution pattern
  - [x] Note: Stats are computed client-side from the loaded committee data, not from separate API endpoints

- [x] **Task 7: Implement loading and error states** (AC: 12, 13)
  - [x] Create skeleton loading state for hierarchy view
  - [x] Implement error display with retry button
  - [x] Implement empty state for no results

- [x] **Task 8: Responsive design** (AC: 14)
  - [x] Implemented responsive design in all components
  - [x] Hierarchy view works on narrow screens (flex-wrap, truncate)
  - [x] Collapse/expand behavior works on touch devices

- [x] **Task 9: Integration and testing** (AC: 15)
  - [x] Verify existing pages work correctly (TypeScript compilation passed)
  - [x] ESLint: Pre-existing warnings in /entities and /government-orgs (not from this story)
  - [x] All new components type-check successfully

## Dev Notes

### Dependencies (from FB-1-UI.1 and FB-1-UI.2)

This story depends on:
- shadcn/ui components: Card, Table, Select, Input, Button, Badge, Skeleton, Dialog
- Types: Committee, CommitteeMembership, CommitteeChamber, CommitteeType, Page<T>
- API client: `committeesApi` functions
- Hooks: `useCommittees`, `useCommitteeMembers`, `useCommitteeSubcommittees`, `useCommitteeSearch`, `useCommitteeCount`
- **From FB-1-UI.2:** Reuse `hooks/useDebounce.ts` for search input debouncing (300ms delay)

### File Structure

```
frontend/src/
├── app/
│   └── committees/
│       ├── page.tsx              # Main committees page
│       ├── loading.tsx           # Loading skeleton
│       └── error.tsx             # Error boundary
├── components/
│   └── congressional/
│       ├── CommitteeHierarchy.tsx    # Hierarchical view (primary)
│       ├── CommitteeTable.tsx        # Table view (optional)
│       ├── CommitteeFilters.tsx      # Filter controls
│       └── CommitteeMembersDialog.tsx # Members modal
└── lib/
    └── utils/
        └── committee-stats.ts    # Client-side stats computation
```

**Note:** Reuse `hooks/useDebounce.ts` from FB-1-UI.2 (do not create new file)

### URL Param Structure

```
/committees?chamber=SENATE&type=STANDING&search=judiciary
```

### Hierarchy Data Structure

The backend returns flat committee list. Build hierarchy client-side:

```typescript
interface CommitteeWithChildren extends Committee {
  subcommittees: CommitteeWithChildren[];
}

function buildHierarchy(committees: Committee[]): CommitteeWithChildren[] {
  const parentCommittees = committees.filter(c => !c.parentCommitteeCode);
  return parentCommittees.map(parent => ({
    ...parent,
    subcommittees: committees.filter(c => c.parentCommitteeCode === parent.committeeCode),
  }));
}
```

### Committee Type Colors

```tsx
const typeColors = {
  STANDING: 'bg-green-100 text-green-800',
  SELECT: 'bg-blue-100 text-blue-800',
  SPECIAL: 'bg-purple-100 text-purple-800',
  JOINT: 'bg-orange-100 text-orange-800',
  SUBCOMMITTEE: 'bg-gray-100 text-gray-800',
  OTHER: 'bg-gray-100 text-gray-600',
};
```

### Chamber Icons/Colors

```tsx
const chamberStyles = {
  SENATE: { color: 'text-blue-700', bg: 'bg-blue-50', label: 'Senate' },
  HOUSE: { color: 'text-red-700', bg: 'bg-red-50', label: 'House' },
  JOINT: { color: 'text-purple-700', bg: 'bg-purple-50', label: 'Joint' },
};
```

### API Endpoints Used

| Endpoint | Hook | Purpose |
|----------|------|---------|
| GET /api/committees | `useCommittees` | Full committee list |
| GET /api/committees/by-chamber/{chamber} | `useCommitteesByChamber` | Filter by chamber |
| GET /api/committees/{code}/members | `useCommitteeMembers` | Committee members |
| GET /api/committees/{code}/subcommittees | `useCommitteeSubcommittees` | Subcommittees |
| GET /api/committees/search | `useCommitteeSearch` | Search by name |
| GET /api/committees/count | `useCommitteeCount` | Total count |

**Note:** Type and chamber distribution statistics are computed client-side from the committee list data (no dedicated stats endpoints).

### Client-Side Statistics Computation

```typescript
// Place in: lib/utils/committee-stats.ts
import type { Committee, CommitteeType, CommitteeChamber } from '@/types/committee';

export interface CommitteeStats {
  total: number;
  byType: Record<CommitteeType, number>;
  byChamber: Record<CommitteeChamber, number>;
}

export function computeCommitteeStats(committees: Committee[]): CommitteeStats {
  const byType: Record<string, number> = {};
  const byChamber: Record<string, number> = {};

  committees.forEach((c) => {
    byType[c.committeeType] = (byType[c.committeeType] || 0) + 1;
    byChamber[c.chamber] = (byChamber[c.chamber] || 0) + 1;
  });

  return {
    total: committees.length,
    byType: byType as Record<CommitteeType, number>,
    byChamber: byChamber as Record<CommitteeChamber, number>,
  };
}
```

### Member Count Strategy

AC 3 requires displaying member count per committee. The `Committee` type doesn't include member count directly. Options:

**Recommended approach:** Fetch member count on-demand when committee is expanded
```typescript
// In CommitteeHierarchy, when a committee is expanded:
const { data: members } = useCommitteeMembers(expandedCode);
// Display members.totalElements as the count
```

**Alternative:** Show "View Members" link without count, reveal count after clicking (lazy load)

**Note:** Avoid fetching all member counts upfront to prevent N+1 API calls on page load.

### Reference: Existing /government-orgs Pattern

The `/government-orgs` page has a similar hierarchical structure. Reference it for:
- Expand/collapse interaction pattern (useState with Set for expanded IDs)
- Hierarchy rendering approach (recursive component)
- Loading state patterns

### Testing

#### Test Scenarios
1. Initial load shows all committees grouped by chamber
2. Expanding Senate section shows Senate committees
3. Expanding a committee shows its subcommittees
4. Filter by "Standing" shows only standing committees
5. Search "Judiciary" shows matching committees
6. "View Members" opens dialog with committee members
7. URL params persist across page refresh
8. Empty search results shows appropriate message

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-29 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-29 | 1.1 | Validation fixes: Updated stats to client-side computation, added useDebounce reference, clarified nav placement, marked Task 4 optional, added member count strategy | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- TypeScript compilation: No errors
- ESLint: Pre-existing warnings in /entities and /government-orgs (not related to this story)

### Completion Notes List
- Created Committees listing page with hierarchical chamber grouping
- Implemented CommitteeFilters with chamber, type filters and debounced search
- Created CommitteeHierarchy with expandable chamber sections and subcommittee nesting
- Created CommitteeMembersDialog for viewing committee members in a modal
- Created CommitteeStats for client-side computed statistics
- Created committee-stats utility with hierarchy building functions
- Added navigation link on home page alongside existing buttons
- All filter combinations supported via URL params (chamber, type, search)
- Task 4 (CommitteeTable) was optional and skipped per story spec

### File List
| File | Action |
|------|--------|
| `frontend/src/app/committees/page.tsx` | Created |
| `frontend/src/app/committees/loading.tsx` | Created |
| `frontend/src/app/committees/error.tsx` | Created |
| `frontend/src/components/congressional/CommitteeFilters.tsx` | Created |
| `frontend/src/components/congressional/CommitteeHierarchy.tsx` | Created |
| `frontend/src/components/congressional/CommitteeStats.tsx` | Created |
| `frontend/src/components/congressional/CommitteeMembersDialog.tsx` | Created |
| `frontend/src/lib/utils/committee-stats.ts` | Created |
| `frontend/src/app/page.tsx` | Modified (added Committees nav link) |

---

## QA Results

### PO Review: 2025-11-30

**Reviewer:** Sarah (PO)
**Verdict:** APPROVED

#### Acceptance Criteria Verification

| # | Criteria | Status |
|---|----------|--------|
| 1 | `/committees` page accessible from navigation | ✅ PASS |
| 2 | Committees grouped by Chamber | ✅ PASS |
| 3 | Committee shows Name, Type, member count | ✅ PASS |
| 4 | Subcommittees displayed hierarchically | ✅ PASS |
| 5 | Expandable/collapsible chamber sections | ✅ PASS |
| 6 | Filter by Chamber | ✅ PASS |
| 7 | Filter by Committee Type | ✅ PASS |
| 8 | Search with debounced input | ✅ PASS |
| 9 | Filters persist in URL params | ✅ PASS |
| 10 | Committee links to members list | ✅ PASS |
| 11 | Statistics section | ✅ PASS |
| 12 | Loading states with Skeleton | ✅ PASS |
| 13 | Error states with retry | ✅ PASS |
| 14 | Responsive design | ✅ PASS |
| 15 | No regression | ✅ PASS |

**Result:** 15/15 acceptance criteria met. Story complete.

### QA Gate Review: 2025-11-30

**Reviewer:** Quinn (Test Architect)

Gate: PASS → docs/qa/gates/FB-1-UI.3-committees-listing-hierarchy-page.yml
