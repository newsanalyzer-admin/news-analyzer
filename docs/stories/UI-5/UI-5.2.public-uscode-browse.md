# Story UI-5.2: Public U.S. Code Browse Page

## Status

**Complete**

## Story

**As a** user exploring the Knowledge Base,
**I want** to browse U.S. Code (federal statutes) in a hierarchical tree view,
**So that** I can explore federal laws by Title, Chapter, and Section.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/knowledge-base/government/us-code` route exists and renders browse page |
| AC2 | Page shows list of imported U.S. Code titles (from `/api/statutes/titles`) |
| AC3 | Each title expands to show chapters (via `UsCodeTreeView` component) |
| AC4 | Each chapter expands to show sections |
| AC5 | Sections show section number and heading text |
| AC6 | Link to official source (uscode.house.gov) provided |
| AC7 | Empty state shown if no titles imported ("No U.S. Code data available") |
| AC8 | Page is read-only (no import functionality - that's admin only) |
| AC9 | Breadcrumbs show: Knowledge Base > U.S. Federal Government > U.S. Code |

## Tasks / Subtasks

- [x] **Task 1: Create U.S. Code page route** (AC: 1, 9)
  - [x] Create `frontend/src/app/knowledge-base/government/us-code/page.tsx`
  - [x] Add page metadata (title, description)
  - [x] Verify breadcrumbs render correctly (depends on UI-5.1 breadcrumb mapping)

- [x] **Task 2: Implement title list fetching** (AC: 2, 7)
  - [x] Fetch titles from `/api/statutes/titles` endpoint
  - [x] Display titles as expandable list items
  - [x] Implement loading state with spinner
  - [x] Implement error state with retry button
  - [x] Implement empty state with helpful message

- [x] **Task 3: Integrate UsCodeTreeView for hierarchy** (AC: 3, 4, 5)
  - [x] Move `UsCodeTreeView` to shared location OR import from admin
  - [x] Render `UsCodeTreeView` when title is expanded
  - [x] Ensure section preview/expand works

- [x] **Task 4: Add external source links** (AC: 6)
  - [x] Add link to uscode.house.gov in page footer
  - [x] Consider adding deep links to specific titles

- [x] **Task 5: Ensure read-only behavior** (AC: 8)
  - [x] No import buttons on this page
  - [x] No admin-specific actions visible

- [x] **Task 6: Write tests** (AC: 1-9)
  - [x] Create `frontend/src/app/knowledge-base/government/us-code/__tests__/page.test.tsx`
  - [x] Test loading state
  - [x] Test error state
  - [x] Test empty state
  - [x] Test title expansion
  - [x] Test breadcrumb rendering

## Dev Notes

### Reference: Admin US Code Page

The admin page at `frontend/src/app/admin/factbase/regulations/us-code/page.tsx` provides a reference implementation. Key differences for public page:

| Admin Page | Public Page |
|------------|-------------|
| Has `UsCodeImportButton` | No import functionality |
| Has stats cards | Optional - can include read-only stats |
| Has "How to Import" section | Remove - not relevant for public |
| Uses admin breadcrumbs | Uses KB breadcrumbs |

### UsCodeTreeView Component

Located at: `frontend/src/components/admin/UsCodeTreeView.tsx`

**Props:**
```typescript
interface UsCodeTreeViewProps {
  titleNumber: number;
  titleName: string;
  onSectionSelect?: (section: SectionSummary) => void;  // optional
}
```

**API Endpoint:**
```
GET /api/statutes/title/{titleNumber}/hierarchy
```

**Response Structure:**
```typescript
interface TitleHierarchy {
  titleNumber: number;
  titleName: string;
  sectionCount: number;
  chapters: Chapter[];
}

interface Chapter {
  chapterNumber: string;
  chapterName: string;
  sectionCount: number;
  sections: SectionSummary[];
}

interface SectionSummary {
  id: string;
  sectionNumber: string;
  heading: string;
  contentPreview: string;
  uscIdentifier: string;
}
```

### Component Sharing Strategy

**Option A: Import from admin (quick)**
```typescript
import { UsCodeTreeView } from '@/components/admin/UsCodeTreeView';
```
This works but couples public to admin components.

**Option B: Move to shared location (recommended)**
```
frontend/src/components/shared/UsCodeTreeView.tsx
```
Then import from shared in both admin and public pages.

**Option C: Create simplified public version**
```
frontend/src/components/knowledge-base/PublicUsCodeTree.tsx
```
Create a read-only variant without selection callbacks.

### Component Strategy Decision (Architect Approved)

✅ **Use Option A for this story:** Import directly from `@/components/admin/UsCodeTreeView`

**Tech Debt Item:** After UI-5.2 complete, create cleanup story to:
1. Move `UsCodeTreeView` to `components/shared/`
2. Update imports in both admin and KB pages
3. Add to tech-debt backlog (estimated: 1 pt)

### Page Structure

```typescript
// frontend/src/app/knowledge-base/government/us-code/page.tsx

'use client';

import { useState, useCallback } from 'react';
import { BookOpen, ExternalLink, AlertCircle, RefreshCw } from 'lucide-react';
import { UsCodeTreeView } from '@/components/admin/UsCodeTreeView';

interface TitleInfo {
  titleNumber: number;
  titleName: string;
  sectionCount: number;
}

export default function UsCodePage() {
  const [titles, setTitles] = useState<TitleInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedTitle, setExpandedTitle] = useState<number | null>(null);

  // Fetch titles...
  // Render UI...
}
```

### Empty State UI

When no titles are imported:

```tsx
<div className="text-center py-12">
  <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
  <h3 className="text-lg font-semibold mb-2">No U.S. Code Data Available</h3>
  <p className="text-muted-foreground mb-4">
    U.S. Code statutes have not been imported yet.
  </p>
  <a
    href="https://uscode.house.gov/"
    target="_blank"
    rel="noopener noreferrer"
    className="text-primary hover:underline inline-flex items-center gap-1"
  >
    <ExternalLink className="h-4 w-4" />
    Browse the official U.S. Code
  </a>
</div>
```

### API Endpoints Used

| Endpoint | Purpose |
|----------|---------|
| `GET /api/statutes/titles` | List all imported titles |
| `GET /api/statutes/title/{num}/hierarchy` | Get hierarchy for a title (used by UsCodeTreeView) |
| `GET /api/statutes/stats` | Optional: Get overall stats |

### External Links

- **Official Source:** https://uscode.house.gov/
- **Download Page:** https://uscode.house.gov/download/download.shtml
- **About U.S. Code:** https://uscode.house.gov/about.xhtml

## Testing

### Test File Location

- `frontend/src/app/knowledge-base/government/us-code/__tests__/page.test.tsx`

### Test Cases

1. **Rendering Tests**
   - Page renders with correct heading "U.S. Code"
   - Page includes description about federal statutes
   - External link to uscode.house.gov is present

2. **Loading State**
   - Shows loading spinner while fetching titles
   - Loading message is displayed

3. **Error State**
   - Shows error message when API fails
   - Retry button is present and functional

4. **Empty State**
   - Shows empty state message when no titles imported
   - External link to official source is present
   - No error styling (empty is valid state)

5. **Populated State**
   - Renders list of titles with title numbers and names
   - Shows section count for each title
   - Clicking title expands/collapses it

6. **Title Expansion**
   - Clicking title renders UsCodeTreeView component
   - UsCodeTreeView receives correct props (titleNumber, titleName)
   - Only one title expanded at a time

7. **Breadcrumbs**
   - Breadcrumbs show "Knowledge Base > U.S. Federal Government > U.S. Code"
   - All breadcrumb items except last are clickable links

8. **Accessibility**
   - Title list items are focusable
   - Expand/collapse has appropriate ARIA attributes
   - External links have `rel="noopener noreferrer"`

### Mock Data for Tests

```typescript
const mockTitles: TitleInfo[] = [
  { titleNumber: 1, titleName: 'General Provisions', sectionCount: 150 },
  { titleNumber: 5, titleName: 'Government Organization and Employees', sectionCount: 2500 },
  { titleNumber: 18, titleName: 'Crimes and Criminal Procedure', sectionCount: 3200 },
];
```

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-02 | 1.0 | Initial story creation | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

None - no issues encountered

### Completion Notes List

1. Created `/knowledge-base/government/us-code/page.tsx` with full page implementation
2. Implemented title list fetching from `/api/statutes/titles` with loading/error/empty states
3. Integrated `UsCodeTreeView` from admin (per architect-approved Option A strategy)
4. Added external links footer with official sources (uscode.house.gov, about page)
5. Page is read-only with no import buttons (admin-only functionality excluded)
6. Created comprehensive test suite with 19 tests covering all states and behaviors
7. Full frontend test suite passes (566 tests)
8. Breadcrumbs work correctly via UI-5.1 mapping

### File List

**Created:**
- `frontend/src/app/knowledge-base/government/us-code/page.tsx` - Public U.S. Code browse page
- `frontend/src/app/knowledge-base/government/us-code/__tests__/page.test.tsx` - Page tests (19 tests)

---

## QA Results

_To be filled by QA agent_
