# Story UI-2.6: Migrate Government Organizations to Knowledge Explorer

## Status

**Draft**

---

## Story

**As a** user exploring government organizations,
**I want** to access organizations through the unified Knowledge Explorer interface,
**so that** I have a consistent experience with the same powerful browsing patterns used across all entity types.

---

## Acceptance Criteria

1. Government Organizations accessible via `/knowledge-base/organizations`
2. Entity type configuration created for Government Organizations with proper columns and filters
3. List view uses EntityBrowser pattern component (from UI-2.2)
4. Detail view uses EntityDetail pattern component (from UI-2.3)
5. Hierarchy view uses HierarchyView pattern component (from UI-2.4)
6. All existing functionality preserved (search, filter by branch/type, expand/collapse)
7. Branch filtering available as a filter option (Executive, Legislative, Judicial)
8. Type filtering available (Department, Agency, Bureau, Office, etc.)
9. Old routes redirect correctly:
   - `/factbase/organizations` → `/knowledge-base/organizations`
   - `/factbase/organizations/executive` → `/knowledge-base/organizations?branch=executive`
   - `/factbase/organizations/legislative` → `/knowledge-base/organizations?branch=legislative`
   - `/factbase/organizations/judicial` → `/knowledge-base/organizations?branch=judicial`
10. URL query params work for deep linking (`?branch=executive&view=hierarchy`)
    - **Note:** Combined filters (branch + type) require backend enhancement (see Prerequisites)
11. Mobile-responsive layout maintained
12. Keyboard navigation works throughout

---

## Prerequisites (Must Complete First)

> **IMPORTANT:** The following issues must be resolved before or during this story implementation.

1. **Search Param Bug Fix:** Frontend hook `useGovernmentOrgsSearch` uses `?q=` but backend expects `?query=`. Fix in `frontend/src/hooks/useGovernmentOrgs.ts` line 48.

2. **Create `useGovernmentOrgs()` Hook:** A paginated list hook doesn't exist. Create one that calls `GET /api/government-organizations` with Pageable params.

3. **Backend Filter Limitation:** The backend uses **separate endpoints** for filtering (e.g., `/by-branch`, `/by-type`), NOT query params on the main list endpoint. Options:
   - **Option A (Recommended):** Enhance backend to support filter params on main endpoint
   - **Option B:** EntityBrowser calls different endpoints based on active filter
   - **Option C:** Limit to one filter at a time (no combined filters)

---

## Tasks / Subtasks

- [ ] Fix prerequisite issues (see Prerequisites section above)
  - [ ] Fix search param: change `?q=` to `?query=` in useGovernmentOrgsSearch
  - [ ] Create `useGovernmentOrgs()` hook for paginated list with filters
  - [ ] Decide on filter approach (backend enhancement or frontend adaptation)

- [ ] Create Government Organizations EntityTypeConfig (AC: 2, 7, 8)
  - [ ] Add configuration to `frontend/src/lib/config/entityTypes.ts`
  - [ ] Define columns: Name, Acronym, Type, Branch, Level
  - [ ] Define filters: branch (select), orgType (multi-select), active (toggle)
  - [ ] Set supportedViews: ['list', 'hierarchy']
  - [ ] Configure sort options (name, type, level)

- [ ] Create organization-specific column renderers (AC: 2, 6)
  - [ ] Create `frontend/src/lib/config/organizationConfig.ts`
  - [ ] Define TypeBadge renderer with color coding
  - [ ] Define BranchBadge renderer
  - [ ] Define ActiveStatus renderer (Active/Dissolved)
  - [ ] Export column configurations

- [ ] Wire EntityBrowser to Government Orgs API (AC: 3, 6)
  - [ ] Create/update `frontend/src/app/knowledge-base/organizations/page.tsx`
  - [ ] Use existing `useGovernmentOrgs` or `useGovernmentOrgsByBranch` hook
  - [ ] Pass search, filter, sort, pagination params to API
  - [ ] Map API response to EntityBrowser props

- [ ] Create Government Organizations EntityDetail config (AC: 4)
  - [ ] Define detail sections: Basic Info, History, About, Links
  - [ ] Map existing OrgDetailPanel fields to config structure
  - [ ] Handle optional fields (mission, description, jurisdiction)
  - [ ] Source attribution: link to Federal Register if available

- [ ] Wire EntityDetail to organization data (AC: 4)
  - [ ] Create/update `frontend/src/app/knowledge-base/organizations/[id]/page.tsx`
  - [ ] Fetch single organization by ID
  - [ ] Pass to EntityDetail component with config

- [ ] Create Government Organizations HierarchyConfig (AC: 5)
  - [ ] Define hierarchy config in `organizationConfig.ts`
  - [ ] Set labelField: 'officialName', metaFields: ['acronym', 'orgType']
  - [ ] Set childrenField based on API structure (or build from parentId)
  - [ ] Configure defaultExpandDepth: 1

- [ ] Wire HierarchyView to organization hierarchy (AC: 5, 6)
  - [ ] Create/update hierarchy view route or query param handler
  - [ ] Fetch hierarchy data (full tree or lazy load)
  - [ ] Transform flat list to tree structure if needed
  - [ ] Support branch-filtered hierarchy (`?branch=executive&view=hierarchy`)

- [ ] Implement branch-specific views (AC: 7, 9)
  - [ ] Branch filter auto-applies from URL param
  - [ ] `?branch=executive` filters to Executive organizations only
  - [ ] Filter persists across view mode changes
  - [ ] Clear filter returns to all organizations

- [ ] Implement route redirects (AC: 9)
  - [ ] Add redirects to `next.config.js` or middleware
  - [ ] `/factbase/organizations` → `/knowledge-base/organizations`
  - [ ] `/factbase/organizations/executive` → `/knowledge-base/organizations?branch=executive`
  - [ ] `/factbase/organizations/legislative` → `/knowledge-base/organizations?branch=legislative`
  - [ ] `/factbase/organizations/judicial` → `/knowledge-base/organizations?branch=judicial`
  - [ ] Test all redirects return 301 status

- [ ] Verify feature parity (AC: 6, 10, 11, 12)
  - [ ] Search works (name, acronym)
  - [ ] Filter by branch works
  - [ ] Filter by type works
  - [ ] Expand/collapse hierarchy works
  - [ ] Click opens detail view
  - [ ] External website links work
  - [ ] Mobile layout responsive
  - [ ] Keyboard navigation functional

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── app/
│   ├── factbase/organizations/           # EXISTING - to be deprecated
│   │   ├── page.tsx                      # Branch selection cards
│   │   ├── OrgDetailPanel.tsx            # Slide-out detail panel
│   │   ├── BranchOrgsPage.tsx            # Shared branch list component
│   │   ├── executive/page.tsx
│   │   ├── legislative/page.tsx
│   │   └── judicial/page.tsx
│   └── knowledge-base/
│       └── organizations/                 # NEW - migrate here
│           ├── page.tsx                   # EntityBrowser with org config
│           └── [id]/
│               └── page.tsx               # EntityDetail with org config
├── components/
│   └── knowledge-base/
│       ├── EntityBrowser.tsx              # From UI-2.2
│       ├── EntityDetail.tsx               # From UI-2.3
│       ├── HierarchyView.tsx              # From UI-2.4
│       └── index.ts
├── lib/
│   └── config/
│       ├── entityTypes.ts                 # Entity type registry
│       └── organizationConfig.ts          # NEW - org-specific config
├── hooks/
│   ├── useGovernmentOrgs.ts               # MODIFY: fix search param, add useGovernmentOrgs()
│   └── useDebounce.ts                     # Existing utility
└── types/
    └── government-org.ts                  # Existing TypeScript types
```

### Key Implementation Details

**Entity Type Configuration:**
```typescript
// In entityTypes.ts
export const organizationsConfig: EntityTypeConfig = {
  id: 'organizations',
  label: 'Organizations',
  icon: Building2,
  apiEndpoint: '/api/government-organizations',  // CORRECT endpoint name
  supportedViews: ['list', 'hierarchy'],
  columns: [
    { id: 'officialName', label: 'Name', sortable: true },
    { id: 'acronym', label: 'Acronym', sortable: true, hideOnMobile: true },
    { id: 'orgType', label: 'Type', sortable: true, render: renderTypeBadge },
    { id: 'branch', label: 'Branch', sortable: true, render: renderBranchBadge },
    { id: 'orgLevel', label: 'Level', sortable: true, hideOnMobile: true },
    { id: 'active', label: 'Status', render: renderActiveStatus, hideOnMobile: true },
  ],
  // NOTE: Backend currently uses SEPARATE endpoints for filters.
  // If Option A (backend enhancement) is chosen, these apiParams will work.
  // If Option B/C, the EntityBrowser must handle endpoint switching.
  filters: [
    {
      id: 'branch',
      label: 'Branch',
      type: 'select',
      options: [
        { value: 'executive', label: 'Executive' },
        { value: 'legislative', label: 'Legislative' },
        { value: 'judicial', label: 'Judicial' },
      ],
      apiParam: 'branch',  // Requires backend enhancement OR use /by-branch endpoint
      altEndpoint: '/api/government-organizations/by-branch',  // Current backend pattern
    },
    {
      id: 'orgType',
      label: 'Type',
      type: 'select',  // Changed: multi-select not supported by backend
      options: [
        { value: 'DEPARTMENT', label: 'Department' },
        { value: 'AGENCY', label: 'Agency' },
        { value: 'BUREAU', label: 'Bureau' },
        { value: 'OFFICE', label: 'Office' },
        { value: 'COMMISSION', label: 'Commission' },
        { value: 'COURT', label: 'Court' },
        { value: 'COMMITTEE', label: 'Committee' },
      ],
      apiParam: 'type',  // Requires backend enhancement OR use /by-type endpoint
      altEndpoint: '/api/government-organizations/by-type',  // Current backend pattern
    },
    {
      id: 'active',
      label: 'Status',
      type: 'select',
      options: [
        { value: 'active', label: 'Active Only' },
        // NOTE: "Dissolved Only" not supported - backend only has /active endpoint
      ],
      altEndpoint: '/api/government-organizations/active',  // No param, just different endpoint
    },
  ],
  defaultSort: { column: 'officialName', direction: 'asc' },
};
```

**Detail Sections Configuration:**
```typescript
export const organizationDetailConfig: EntityDetailConfig = {
  sections: [
    {
      id: 'basic',
      title: 'Basic Information',
      fields: [
        { key: 'branch', label: 'Branch', render: capitalizeFirst },
        { key: 'orgType', label: 'Organization Type', render: formatOrgType },
        { key: 'orgLevel', label: 'Hierarchy Level', render: (v) => `Level ${v}` },
      ],
    },
    {
      id: 'history',
      title: 'History',
      condition: (org) => org.establishedDate || org.dissolvedDate,
      fields: [
        { key: 'establishedDate', label: 'Established', render: formatDate },
        { key: 'dissolvedDate', label: 'Dissolved', render: formatDate },
      ],
    },
    {
      id: 'about',
      title: 'About',
      condition: (org) => org.mission || org.description,
      fields: [
        { key: 'mission', label: 'Mission' },
        { key: 'description', label: 'Description' },
      ],
    },
    {
      id: 'jurisdiction',
      title: 'Jurisdiction Areas',
      condition: (org) => org.jurisdictionAreas?.length > 0,
      render: (org) => <BadgeList items={org.jurisdictionAreas} />,
    },
    {
      id: 'links',
      title: 'Links',
      condition: (org) => org.websiteUrl,
      render: (org) => <ExternalLinkButton url={org.websiteUrl} label="Official Website" />,
    },
  ],
};
```

**Type Badge Color Mapping (from existing):**
```typescript
const orgTypeColors: Record<string, string> = {
  DEPARTMENT: 'bg-blue-100 text-blue-800',
  AGENCY: 'bg-green-100 text-green-800',
  BUREAU: 'bg-amber-100 text-amber-800',
  OFFICE: 'bg-purple-100 text-purple-800',
  COMMISSION: 'bg-red-100 text-red-800',
  COURT: 'bg-indigo-100 text-indigo-800',
  COMMITTEE: 'bg-orange-100 text-orange-800',
};
```

**Building Tree from Flat List:**
```typescript
// If API returns flat list with parentId, build tree client-side
function buildOrgHierarchy(orgs: GovernmentOrganization[]): HierarchyNode[] {
  const map = new Map<string, HierarchyNode>();
  const roots: HierarchyNode[] = [];

  // First pass: create nodes
  orgs.forEach(org => {
    map.set(org.id, { ...org, children: [] });
  });

  // Second pass: link parents and children
  orgs.forEach(org => {
    const node = map.get(org.id)!;
    if (org.parentId && map.has(org.parentId)) {
      map.get(org.parentId)!.children!.push(node);
    } else {
      roots.push(node);
    }
  });

  return roots;
}
```

**Existing Hooks (Verified):**
```typescript
// frontend/src/hooks/useGovernmentOrgs.ts

// EXISTS - fetches all orgs for a branch (non-paginated)
useGovernmentOrgsByBranch(branch: GovernmentBranch)

// EXISTS - search orgs (BUT has bug: uses ?q= instead of ?query=)
useGovernmentOrgsSearch(query: string)

// DOES NOT EXIST - must be created for paginated list
// useGovernmentOrgs({ page, size, sort, filters })

// EXISTS - debounce utility
useDebounce(value, delay)
```

**Hook to Create:**
```typescript
// Add to frontend/src/hooks/useGovernmentOrgs.ts
export function useGovernmentOrgs(params: {
  page?: number;
  size?: number;
  sort?: string;
  branch?: GovernmentBranch;
  type?: string;
}) {
  return useQuery({
    queryKey: ['government-organizations', 'list', params],
    queryFn: () => fetchGovernmentOrgs(params),
  });
}

async function fetchGovernmentOrgs(params: {...}): Promise<Page<GovernmentOrganization>> {
  const searchParams = new URLSearchParams();
  if (params.page) searchParams.set('page', params.page.toString());
  if (params.size) searchParams.set('size', params.size.toString());
  if (params.sort) searchParams.set('sort', params.sort);
  // Filter handling depends on chosen option (see Prerequisites)

  const response = await fetch(`${API_BASE}/api/government-organizations?${searchParams}`);
  return response.json();
}
```

**Existing Types (Verified):**
```typescript
// From types/government-org.ts - ACTUAL definitions (use | null, not optional)
interface GovernmentOrganization {
  id: string;
  officialName: string;
  acronym: string | null;           // nullable, not optional
  orgType: string;
  branch: GovernmentBranch;
  orgLevel: number;
  parentId: string | null;          // nullable, not optional
  active: boolean;
  establishedDate: string | null;   // nullable, not optional
  dissolvedDate: string | null;     // nullable, not optional
  websiteUrl: string | null;        // nullable, not optional
  jurisdictionAreas: string[] | null;
  description?: string | null;      // optional AND nullable
  mission?: string | null;          // optional AND nullable
}

type GovernmentBranch = 'executive' | 'legislative' | 'judicial';
```

**Backend API Endpoints (Verified):**
```
Listing:
  GET /api/government-organizations              - Paginated list (Pageable params only)
  GET /api/government-organizations/active       - All active orgs (no pagination)
  GET /api/government-organizations/{id}         - Single org by ID

Search:
  GET /api/government-organizations/search?query=...         - LIKE search
  GET /api/government-organizations/search/fuzzy?query=...   - Fuzzy/typo-tolerant
  GET /api/government-organizations/search/fulltext?query=...- Full-text search

Filtering (separate endpoints, NOT query params):
  GET /api/government-organizations/by-branch?branch=...     - Filter by branch
  GET /api/government-organizations/by-type?type=...         - Filter by org type
  GET /api/government-organizations/cabinet-departments      - Cabinet departments only
  GET /api/government-organizations/independent-agencies     - Independent agencies only

Hierarchy:
  GET /api/government-organizations/{id}/hierarchy           - Full hierarchy (ancestors + children)
  GET /api/government-organizations/{id}/descendants         - All child orgs recursively
```

### Architecture Reference

- Frontend: Next.js 14 App Router
- State: URL-based for filters, view mode
- UI: Shadcn/UI + Tailwind CSS
- Data: React Query with existing hooks

---

## Testing

### Test File Location
`frontend/src/app/knowledge-base/organizations/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses
- Test configuration-driven rendering
- Test all filter/sort/view combinations

### Test Cases

1. **Route Access**
   - `/knowledge-base/organizations` loads organization list
   - `/knowledge-base/organizations/[id]` loads organization detail
   - `?view=hierarchy` shows hierarchy view
   - `?branch=executive` filters to executive branch

2. **EntityBrowser Integration**
   - Columns render from organization config
   - Type badges have correct colors
   - Branch badges display
   - Sorting works on sortable columns
   - Pagination works

3. **Filtering**
   - Branch filter shows Executive/Legislative/Judicial options
   - Type filter allows multi-select
   - Active filter shows Active Only/Dissolved Only
   - Filters update URL params
   - Clearing filters resets view

4. **EntityDetail Integration**
   - Basic info section displays
   - History section conditionally displays
   - About section conditionally displays
   - External website link works
   - Jurisdiction badges display

5. **HierarchyView Integration**
   - Tree renders from organization data
   - Parent-child relationships correct
   - Expand/collapse works
   - Click navigates to detail

6. **Redirects**
   - Old factbase URLs redirect with 301
   - Branch-specific URLs redirect with query param
   - Bookmarks continue to work

7. **Feature Parity**
   - Search by name/acronym works
   - All existing filters available
   - Hierarchy expand/collapse preserved
   - Detail panel information complete
   - External links functional

8. **Responsive**
   - Mobile shows condensed columns
   - Touch-friendly tap targets
   - Hierarchy scrollable on mobile

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-24 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-12-24 | 1.1 | Validation fixes: correct API endpoint, document filter architecture, add prerequisites, fix hook/type docs | Sarah (PO) |

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
