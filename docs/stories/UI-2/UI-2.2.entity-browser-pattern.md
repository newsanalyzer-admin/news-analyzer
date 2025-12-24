# Story UI-2.2: EntityBrowser Pattern Component

## Status

**Draft**

---

## Story

**As a** developer building Knowledge Base views,
**I want** a reusable EntityBrowser component that renders any entity type based on configuration,
**so that** I can add new entity types without creating bespoke list components each time.

---

## Acceptance Criteria

1. EntityBrowser accepts an `EntityTypeConfig` and renders appropriate columns
2. Sorting works on any configured sortable column
3. Pagination handles large datasets (default 20 per page, configurable)
4. List/grid view toggle works when both modes are supported
5. Filters render dynamically based on entity type configuration
6. Loading and empty states are handled gracefully
7. Clicking a row/card navigates to EntityDetail view (`/knowledge-base/:type/:id`)
8. Component is fully typed with TypeScript generics
9. Responsive design: table on desktop, cards on mobile
10. Accessible: proper ARIA roles, keyboard navigation for rows

---

## Tasks / Subtasks

- [ ] Define EntityTypeConfig interface (AC: 1, 8)
  - [ ] Create `frontend/src/lib/config/entityTypes.ts`
  - [ ] Define EntityTypeConfig interface with columns, filters, views
  - [ ] Define ColumnConfig interface (id, label, sortable, render function)
  - [ ] Define FilterConfig interface (id, type, options)
  - [ ] Export TypeScript types

- [ ] Create EntityBrowser component (AC: 1, 7, 9, 10)
  - [ ] Create `frontend/src/components/knowledge-base/EntityBrowser.tsx`
  - [ ] Accept EntityTypeConfig as prop
  - [ ] Accept data array and loading state as props
  - [ ] Implement row click navigation to detail view
  - [ ] Add ARIA roles (table, rowgroup, row, cell)
  - [ ] Add keyboard navigation (Arrow keys, Enter to select)

- [ ] Implement list/table view (AC: 1, 2, 9)
  - [ ] Render columns based on config
  - [ ] Support custom render functions per column
  - [ ] Desktop: full table with all columns
  - [ ] Mobile: condensed view or card fallback

- [ ] Implement grid/card view (AC: 4, 9)
  - [ ] Create EntityCard subcomponent
  - [ ] Render card layout based on config
  - [ ] Responsive grid (1 col mobile, 2-3 cols tablet, 4 cols desktop)

- [ ] Implement sorting (AC: 2)
  - [ ] Add sort state (column, direction)
  - [ ] Clickable column headers for sortable columns
  - [ ] Visual indicator for current sort
  - [ ] Callback to parent for server-side sorting

- [ ] Implement pagination (AC: 3)
  - [ ] Create or reuse Pagination component
  - [ ] Support configurable page size (10, 20, 50)
  - [ ] Display total count and current range
  - [ ] Callback to parent for page changes

- [ ] Implement filters (AC: 5)
  - [ ] Create EntityFilters subcomponent
  - [ ] Render filter controls based on FilterConfig
  - [ ] Support filter types: select, multi-select, text search
  - [ ] Callback to parent for filter changes
  - [ ] Clear all filters button

- [ ] Handle loading and empty states (AC: 6)
  - [ ] Loading skeleton for table/grid
  - [ ] Empty state with message and optional action
  - [ ] Error state display

- [ ] Create barrel export and types (AC: 8)
  - [ ] Update `frontend/src/components/knowledge-base/index.ts`
  - [ ] Export EntityBrowser and all related types

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── components/
│   ├── knowledge-base/
│   │   ├── EntityBrowser.tsx             # NEW - main component
│   │   ├── EntityCard.tsx                # NEW - grid view card
│   │   ├── EntityFilters.tsx             # NEW - filter controls
│   │   ├── EntityTypeSelector.tsx        # From UI-2.1
│   │   ├── ViewModeSelector.tsx          # From UI-2.1
│   │   └── index.ts                      # Barrel export
│   └── ui/
│       ├── table.tsx                     # Shadcn table (if exists)
│       ├── pagination.tsx                # Shadcn pagination (if exists)
│       ├── select.tsx                    # For filters
│       └── skeleton.tsx                  # Loading states
├── lib/
│   └── config/
│       └── entityTypes.ts                # NEW - type definitions and configs
└── hooks/
    └── usePagination.ts                  # May exist or create
```

### Key Implementation Details

**EntityTypeConfig Structure:**
```typescript
interface EntityTypeConfig {
  id: string;                           // 'organizations', 'people'
  label: string;                        // 'Organizations'
  apiEndpoint: string;                  // '/api/government-orgs'
  supportedViews: ('list' | 'grid' | 'hierarchy')[];
  columns: ColumnConfig[];
  filters: FilterConfig[];
  defaultSort: { column: string; direction: 'asc' | 'desc' };
  cardConfig?: CardConfig;              // For grid view
}

interface ColumnConfig {
  id: string;                           // Field name
  label: string;                        // Header label
  sortable: boolean;
  width?: string;                       // Optional width
  render?: (value: any, row: any) => React.ReactNode;  // Custom render
  hideOnMobile?: boolean;
}

interface FilterConfig {
  id: string;
  label: string;
  type: 'select' | 'multi-select' | 'text';
  options?: { value: string; label: string }[];  // For select types
  apiParam: string;                     // Query param name
}
```

**EntityBrowser Props:**
```typescript
interface EntityBrowserProps<T> {
  config: EntityTypeConfig;
  data: T[];
  totalCount: number;
  isLoading: boolean;
  error?: string;
  currentPage: number;
  pageSize: number;
  sortColumn?: string;
  sortDirection?: 'asc' | 'desc';
  filters?: Record<string, any>;
  viewMode: 'list' | 'grid';
  onPageChange: (page: number) => void;
  onSortChange: (column: string, direction: 'asc' | 'desc') => void;
  onFilterChange: (filters: Record<string, any>) => void;
  onRowClick: (item: T) => void;
}
```

**Data Fetching Pattern:**
- EntityBrowser is a **presentational component** (receives data as props)
- Parent page (`/knowledge-base/[entityType]/page.tsx`) handles data fetching
- Use existing API hooks or create new ones as needed
- Pass callbacks for sort/filter/page changes to trigger refetch

**Existing Components to Reuse:**
- Check `frontend/src/components/ui/` for Shadcn table, pagination
- Check existing factbase pages for any reusable patterns:
  - `factbase/organizations/page.tsx` - may have table implementation
  - `factbase/people/federal-judges/page.tsx` - may have list implementation

### Architecture Reference

- UI Components: Shadcn/UI (Table, Select, Button, Skeleton)
- Styling: Tailwind CSS with `cn()` utility for class merging
- Icons: Lucide React (ChevronUp, ChevronDown for sort indicators)
- State: Props-driven (controlled component pattern)

---

## Testing

### Test File Location
`frontend/src/components/knowledge-base/__tests__/EntityBrowser.test.tsx`

### Testing Standards
- Use Vitest + React Testing Library
- Mock data for various entity types
- Test all interactive behaviors
- Test accessibility with jest-axe if available

### Test Cases

1. **Rendering**
   - Renders table with correct columns from config
   - Renders correct number of rows based on data
   - Renders grid view when viewMode='grid'
   - Renders loading skeleton when isLoading=true
   - Renders empty state when data=[]
   - Renders error state when error prop provided

2. **Sorting**
   - Clicking sortable column header triggers onSortChange
   - Sort indicator shows on current sort column
   - Non-sortable columns don't trigger sort
   - Clicking same column toggles direction (asc/desc)

3. **Pagination**
   - Displays correct page info (e.g., "1-20 of 100")
   - Next/prev buttons trigger onPageChange
   - Page size selector triggers callback
   - Disabled states for first/last page

4. **Filtering**
   - Filter controls render based on config
   - Changing filter triggers onFilterChange
   - Clear filters button resets all
   - Active filter count displayed

5. **Row Interaction**
   - Clicking row triggers onRowClick with item
   - Keyboard: Enter on focused row triggers click
   - Row hover state visible
   - Focus visible for accessibility

6. **Responsive**
   - Table columns hide on mobile per config
   - Grid view adjusts column count by viewport
   - Touch-friendly tap targets

7. **Accessibility**
   - Table has proper ARIA roles
   - Sort buttons have aria-label
   - Focus management works correctly
   - Screen reader announces sort changes

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-22 | 1.0 | Initial story creation | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
_To be filled by Dev Agent_

### Debug Log References
_To be filled by Dev Agent_

### Completion Notes List
_To be filled by Dev Agent_

### File List
_To be filled by Dev Agent_

---

## QA Results
_To be filled by QA Agent_
