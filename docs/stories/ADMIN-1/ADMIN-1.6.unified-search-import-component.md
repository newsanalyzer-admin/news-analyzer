# Story ADMIN-1.6: Unified Search/Import Component

## Status

**Done**

---

## Story

**As a** developer,
**I want** a reusable SearchImportPanel component,
**so that** I can implement consistent search/import UIs for multiple external APIs.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | SearchImportPanel accepts props: apiName, searchEndpoint, filterConfig, resultRenderer |
| AC2 | Component renders search input with configurable filters |
| AC3 | Component displays results in a consistent card/table format |
| AC4 | Each result shows source attribution (e.g., "From Congress.gov") |
| AC5 | Each result has action buttons: Preview, Import, Compare (if duplicate detected) |
| AC6 | Preview action opens ImportPreviewModal with editable fields |
| AC7 | Import action validates and imports with confirmation |
| AC8 | Compare action opens MergeConflictModal showing existing vs. new record |
| AC9 | Component handles loading, error, and empty states |
| AC10 | Component is fully typed with TypeScript generics for result type |

---

## Integration Verification

| # | Verification |
|---|--------------|
| IV1 | Component does not break when no results returned |
| IV2 | Component handles API errors gracefully with retry option |
| IV3 | Import operations respect existing database constraints |

---

## Tasks / Subtasks

- [x] **Task 1: Define TypeScript Generic Types** (AC10)
  - [x] Create `frontend/src/types/search-import.ts`
  - [x] Define `SearchImportPanelProps<T>` generic interface
  - [x] Define `FilterConfig` type for configurable filters
  - [x] Define `SearchResult<T>` wrapper type with source attribution
  - [x] Define `ImportPreviewData<T>` type for preview modal
  - [x] Define `MergeConflictData<T>` type for compare modal

- [x] **Task 2: Create SearchImportPanel Component** (AC1, AC2, AC3, AC4, AC9)
  - [x] Install ScrollArea component if needed: `npx shadcn-ui@latest add scroll-area`
  - [x] Create `frontend/src/components/admin/SearchImportPanel.tsx`
  - [x] Accept generic props: `apiName`, `searchEndpoint`, `filterConfig`, `resultRenderer`
  - [x] Render search input with debounced onChange (use existing `useDebounce` hook)
  - [x] Render dynamic filters based on `filterConfig` prop
  - [x] Display results using `resultRenderer` render prop
  - [x] Show source attribution badge on each result (e.g., "From Congress.gov")
  - [x] Handle loading state with Loader2 spinner
  - [x] Handle error state with retry button
  - [x] Handle empty state with appropriate message

- [x] **Task 3: Create useSearchImport Hook** (AC9)
  - [x] Create `frontend/src/hooks/useSearchImport.ts`
  - [x] Use React Query `useQuery` for search requests
  - [x] Implement debounced search with configurable delay (default 300ms)
  - [x] Accept generic type parameter for result type
  - [x] Handle pagination if search endpoint supports it
  - [x] Return query state (data, isLoading, isError, error, refetch)

- [x] **Task 4: Create ResultCard Component** (AC3, AC4, AC5)
  - [x] Create `frontend/src/components/admin/SearchResultCard.tsx`
  - [x] Display result data using render prop pattern
  - [x] Show source attribution badge
  - [x] Add action buttons: Preview, Import, Compare
  - [x] Compare button only visible when `duplicateId` is present
  - [x] Use Button variants: Preview (outline), Import (default), Compare (secondary)

- [x] **Task 5: Create ImportPreviewModal** (AC6, AC7)
  - [x] Create `frontend/src/components/admin/ImportPreviewModal.tsx`
  - [x] Accept generic data type for preview fields
  - [x] Display all fields from external API data
  - [x] Allow editing of select fields (configurable via props)
  - [x] Show confirmation dialog before import
  - [x] Call import endpoint on confirm
  - [x] Display success/error feedback using toast

- [x] **Task 6: Create MergeConflictModal** (AC8)
  - [x] Create `frontend/src/components/admin/MergeConflictModal.tsx`
  - [x] Accept existing record and new record as props
  - [x] Display side-by-side comparison (existing vs. new)
  - [x] Highlight differing fields
  - [x] Allow user to choose: Keep Existing, Replace with New, Merge (select fields)
  - [x] Execute chosen action on confirm

- [x] **Task 7: Create useImportRecord Hook** (AC7)
  - [x] Create generic import mutation hook in `frontend/src/hooks/useImportRecord.ts`
  - [x] Accept import endpoint and payload type
  - [x] Use React Query `useMutation`
  - [x] Return mutation state (mutate, isPending, isSuccess, isError)
  - [x] Invalidate relevant queries on success

- [x] **Task 8: Create Filter Components** (AC2)
  - [x] Create `frontend/src/components/admin/SearchFilters.tsx`
  - [x] Render filters dynamically from `filterConfig`
  - [x] Support filter types: text, select, multi-select, date-range
  - [x] Use shadcn/ui Select, Input, DatePicker components
  - [x] Emit filter changes via callback prop

- [x] **Task 9: Integration Testing**
  - [x] TypeScript type check passes with generics
  - [x] ESLint check passes
  - [x] Next.js build succeeds
  - [x] Create example usage page for testing: `frontend/src/app/admin/factbase/search-test/page.tsx`
  - [x] Verify all modal interactions work correctly
  - [x] Test empty, loading, and error states

---

## Dev Notes

### Source Tree - Relevant Files

**Existing Patterns to Follow:**
```
frontend/src/
├── components/admin/
│   ├── CsvImportButton.tsx              # Pattern: Dialog-based import
│   ├── GovmanImportButton.tsx           # Pattern: Multi-stage dialog
│   └── AdminBreadcrumb.tsx              # Pattern: Reusable admin component
├── hooks/
│   ├── useGovernmentOrgs.ts             # Pattern: React Query hooks
│   └── useGovmanImport.ts               # Pattern: Import mutation hooks
├── types/
│   ├── government-org.ts                # Pattern: Entity types
│   └── govman.ts                        # Pattern: Import result types
└── lib/
    └── utils.ts                         # cn() utility for classnames
```

**Files to Create:**
```
frontend/src/
├── types/
│   └── search-import.ts                 # NEW: Generic search/import types
├── hooks/
│   ├── useSearchImport.ts               # NEW: Search query hook
│   └── useImportRecord.ts               # NEW: Import mutation hook
├── components/admin/
│   ├── SearchImportPanel.tsx            # NEW: Main reusable component
│   ├── SearchResultCard.tsx             # NEW: Result display card
│   ├── SearchFilters.tsx                # NEW: Dynamic filter component
│   ├── ImportPreviewModal.tsx           # NEW: Preview/import dialog
│   └── MergeConflictModal.tsx           # NEW: Side-by-side compare dialog
└── app/admin/factbase/
    └── search-test/
        └── page.tsx                     # NEW: Test page for component
```

### Component Architecture

```
SearchImportPanel<T>
├── SearchFilters (dynamic from filterConfig)
├── SearchInput (debounced)
├── ResultsList
│   └── SearchResultCard<T> (multiple)
│       ├── resultRenderer(data)         # Custom render from prop
│       ├── Source Badge
│       └── Action Buttons
│           ├── Preview → ImportPreviewModal<T>
│           ├── Import → Confirmation Dialog
│           └── Compare → MergeConflictModal<T>
```

### TypeScript Generic Patterns

```typescript
// Props interface with generics
interface SearchImportPanelProps<T> {
  apiName: string;
  searchEndpoint: string;
  filterConfig: FilterConfig[];
  resultRenderer: (item: T) => React.ReactNode;
  onImport: (item: T) => Promise<void>;
  duplicateChecker?: (item: T) => Promise<string | null>; // returns existing ID if duplicate
}

// Filter configuration
interface FilterConfig {
  id: string;
  label: string;
  type: 'text' | 'select' | 'multi-select' | 'date-range';
  options?: { value: string; label: string }[]; // for select types
  placeholder?: string;
}

// Search result wrapper
interface SearchResult<T> {
  data: T;
  source: string;          // e.g., "Congress.gov"
  sourceUrl?: string;      // Link to original record
  duplicateId?: string;    // Existing record ID if duplicate detected
}
```

### shadcn/ui Components to Use

**Core Components:**
- Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter
- Button (variants: default, outline, secondary, ghost, destructive)
- Input, Select, SelectTrigger, SelectValue, SelectContent, SelectItem
- Card, CardHeader, CardTitle, CardDescription, CardContent
- Badge (for source attribution)
- Tabs, TabsList, TabsTrigger, TabsContent (for merge modal)
- ScrollArea (for long result lists)
- Separator (for visual division)

**State Indicators:**
- Loader2 (loading spinner)
- AlertCircle (error state)
- SearchX (empty state)
- CheckCircle (success)

### Tech Stack Reference

| Technology | Version | Usage |
|------------|---------|-------|
| Next.js | 14.1.0 | App Router |
| TypeScript | 5.3.3 | Generic type definitions |
| React Query | 5.17.19 | useQuery, useMutation |
| shadcn/ui | Latest | UI components |
| Tailwind CSS | 3.4.1 | Styling |
| Lucide React | 0.314.0 | Icons |
| useDebounce (local) | - | Debounced search input (existing hook at `frontend/src/hooks/useDebounce.ts`) |

### API Contract (for testing)

The component is API-agnostic but expects endpoints to follow this pattern:

```typescript
// Search endpoint response
interface SearchResponse<T> {
  results: T[];
  total: number;
  page: number;
  pageSize: number;
}

// Import endpoint request/response
// POST /api/admin/import/{entity-type}
// Body: { data: T, source: string }
// Response: { id: string, created: boolean, updated: boolean }
```

### Future Consumer Stories

This component will be used by:
- **ADMIN-1.7:** Congress.gov Member Search (`/admin/factbase/legislative/members/search`)
- **ADMIN-1.8:** Federal Register Agency Search (`/admin/factbase/executive/agencies/search`)
- **Future:** US Code Search, Regulations Search, etc.

---

## Testing

### Testing Standards

| Aspect | Requirement |
|--------|-------------|
| Test Location | Manual testing (frontend test framework not established per QA-2) |
| Component Testing | Browser dev tools, React Query DevTools |
| Type Checking | `pnpm tsc --noEmit` must pass |
| Lint Check | `pnpm lint` must pass |
| Build | `pnpm build` must succeed |

### Manual Test Cases

**Test 1: Basic Rendering (AC1, AC2)**
- Import SearchImportPanel with mock props
- Verify search input renders
- Verify filters render based on filterConfig

**Test 2: Search Flow (AC3, AC9)**
- Enter search text
- Verify loading state shows
- Verify results display after debounce
- Verify empty state when no results

**Test 3: Result Display (AC3, AC4, AC5)**
- Verify source badge displays on each result
- Verify Preview, Import buttons present
- Verify Compare button only shows when duplicate detected

**Test 4: Preview Modal (AC6)**
- Click Preview on a result
- Verify modal opens with all fields
- Verify editable fields can be modified
- Close modal and verify no side effects

**Test 5: Import Flow (AC7)**
- Click Import on a result
- Verify confirmation dialog appears
- Confirm import
- Verify success toast and data refresh

**Test 6: Merge Conflict (AC8)**
- Click Compare on a duplicate result
- Verify side-by-side display
- Verify field differences are highlighted
- Test all merge options

**Test 7: Error Handling (AC9, IV2)**
- Simulate API error
- Verify error state displays
- Verify retry button works

**Test 8: TypeScript Generics (AC10)**
- Verify component works with different data types
- Verify type inference works correctly
- Verify no TypeScript errors

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-06 | 1.0 | Initial story creation from PRD | Sarah (PO) |
| 2025-12-06 | 1.1 | Validation fixes: use existing useDebounce hook, add ScrollArea install, fix test page path | Sarah (PO) |
| 2025-12-06 | 1.2 | Story validated and approved for development | Sarah (PO) |
| 2025-12-06 | 1.3 | Implementation complete - all tasks done, validations pass | James (Dev) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- Fixed TypeScript constraint error: `MockMember` interface extended with `Record<string, unknown>` to satisfy generic constraint

### Completion Notes List

- All 9 tasks completed successfully
- TypeScript type check passes (`npx tsc --noEmit`)
- ESLint passes with no warnings or errors
- Next.js build succeeds (17 pages generated)
- Test page available at `/admin/factbase/search-test`
- Component fully supports generics for type-safe usage with any data type

### File List

**Created:**
- `frontend/src/types/search-import.ts` - Generic type definitions
- `frontend/src/hooks/useSearchImport.ts` - Search query hook with debounce
- `frontend/src/hooks/useImportRecord.ts` - Import/merge mutation hooks
- `frontend/src/components/admin/SearchImportPanel.tsx` - Main reusable component
- `frontend/src/components/admin/SearchResultCard.tsx` - Result display card
- `frontend/src/components/admin/SearchFilters.tsx` - Dynamic filter component
- `frontend/src/components/admin/ImportPreviewModal.tsx` - Preview/edit modal
- `frontend/src/components/admin/MergeConflictModal.tsx` - Side-by-side merge modal
- `frontend/src/app/admin/factbase/search-test/page.tsx` - Test page

---

## QA Results

### Review Date: 2025-12-07

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: Excellent** - This is a well-architected, reusable component system with proper separation of concerns and comprehensive TypeScript generics support. The implementation follows React best practices, uses proper state management patterns, and handles all UI states gracefully.

**Strengths:**
- Clean type definitions with full generic support (AC10)
- Proper use of React Query for data fetching with debouncing
- Comprehensive state handling (loading, error, empty, initial)
- Good component composition with clear responsibilities
- Proper use of shadcn/ui components and Tailwind CSS
- Well-structured callback handlers with useCallback optimization
- Source attribution and duplicate detection flow properly implemented

**Code Quality Score: 92/100**

### Refactoring Performed

None required - code quality is high and follows established patterns.

### Compliance Check

- Coding Standards: ✓ Follows TypeScript/React standards with 2-space indentation, PascalCase components, camelCase functions
- Project Structure: ✓ Files placed in correct locations per source-tree patterns
- Testing Strategy: ✓ Manual testing approach per story requirements (frontend framework not established per QA-2)
- All ACs Met: ✓ All 10 acceptance criteria implemented and verified

### Acceptance Criteria Validation

| AC | Status | Evidence |
|----|--------|----------|
| AC1 | ✓ | `SearchImportPanelProps<T>` accepts apiName, searchEndpoint, filterConfig, resultRenderer |
| AC2 | ✓ | `SearchFilters` renders dynamically from filterConfig with text/select/multi-select/date-range |
| AC3 | ✓ | `SearchResultCard` displays results in consistent card format with render prop pattern |
| AC4 | ✓ | Badge "From {source}" displayed on each result with optional sourceUrl link |
| AC5 | ✓ | Preview (outline), Import (default), Compare (secondary when duplicateId) buttons present |
| AC6 | ✓ | `ImportPreviewModal` opens with all fields, editable fields configurable |
| AC7 | ✓ | Import shows confirmation dialog, calls onImport, displays toast feedback |
| AC8 | ✓ | `MergeConflictModal` shows side-by-side comparison with Keep/Replace/Merge options |
| AC9 | ✓ | Loading spinner, error with retry button, empty state with message all implemented |
| AC10 | ✓ | Full TypeScript generics throughout all components and hooks |

### Integration Verification

| IV | Status | Notes |
|----|--------|-------|
| IV1 | ✓ | Empty state handled with showEmptyState logic and customizable emptyMessage |
| IV2 | ✓ | Error state with AlertCircle icon, error message, and Retry button |
| IV3 | ✓ | onImport callback returns ImportResult with error handling - respects constraints |

### Improvements Checklist

- [x] All components properly typed with generics
- [x] Proper error boundaries via try-catch in handlers
- [x] Loading states displayed correctly
- [x] Toast notifications for success/error feedback
- [ ] Consider adding aria-labels for accessibility on icon-only buttons (nice-to-have)
- [ ] Consider keyboard navigation for multi-select filter chips (nice-to-have)

### Security Review

**Status: PASS**
- No hardcoded secrets or credentials
- External links properly use `rel="noopener noreferrer"`
- Input values properly handled through React state (no direct DOM manipulation)
- API calls use configurable endpoints (no hardcoded URLs except environment defaults)

### Performance Considerations

**Status: PASS**
- Debounced search input (300ms default) prevents excessive API calls
- useCallback used for handler functions to prevent unnecessary re-renders
- Pagination implemented to limit result set size
- React Query caching (30s stale, 5min gc) reduces redundant fetches
- ScrollArea used for long result lists

### Files Modified During Review

None - code quality met standards without requiring changes.

### Gate Status

Gate: **PASS** → docs/qa/gates/ADMIN-1.6-unified-search-import-component.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, code quality excellent, validations pass.

(Story owner decides final status)
