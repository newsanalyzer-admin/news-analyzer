# Story UI-1.8: Federal Government Organization Pages

## Status

**Ready**

---

## Story

**As a** user,
**I want** public pages to browse Federal Government organizations by branch,
**so that** I can explore Executive, Legislative, and Judicial branch agencies and departments.

---

## Acceptance Criteria

1. Executive Branch page accessible at `/factbase/organizations/executive`
2. Legislative Branch page accessible at `/factbase/organizations/legislative`
3. Judicial Branch page accessible at `/factbase/organizations/judicial`
4. Each page displays `ContentPageHeader` with title and educational description
5. Each page shows organizations filtered by branch
6. Organizations display in hierarchical tree structure (parent → children)
7. Organizations show: name, type, acronym, parent org
8. Organizations can be searched by name
9. Clicking an organization shows details (website, jurisdiction, etc.)
10. Page handles loading and error states gracefully
11. Bug fix: All three branches display correctly (not just Executive)

---

## Tasks / Subtasks

- [ ] Create Organizations hub page (AC: prerequisite)
  - [ ] Create `frontend/src/app/factbase/organizations/page.tsx`
  - [ ] Show overview with links to each branch

- [ ] Create Executive Branch page (AC: 1, 4, 5, 6, 7)
  - [ ] Create `frontend/src/app/factbase/organizations/executive/page.tsx`
  - [ ] Add ContentPageHeader with Executive Branch description
  - [ ] Fetch orgs with `?branch=executive` filter
  - [ ] Display hierarchical tree

- [ ] Create Legislative Branch page (AC: 2, 4, 5, 6, 7)
  - [ ] Create `frontend/src/app/factbase/organizations/legislative/page.tsx`
  - [ ] Add ContentPageHeader with Legislative Branch description
  - [ ] Fetch orgs with `?branch=legislative` filter
  - [ ] Display hierarchical tree
  - [ ] **Requires UI-1.9** (data import) to have data

- [ ] Create Judicial Branch page (AC: 3, 4, 5, 6, 7)
  - [ ] Create `frontend/src/app/factbase/organizations/judicial/page.tsx`
  - [ ] Add ContentPageHeader with Judicial Branch description
  - [ ] Fetch orgs with `?branch=judicial` filter
  - [ ] Display hierarchical tree
  - [ ] **Requires UI-1.10** (data import) to have data

- [ ] Add search functionality (AC: 8)
  - [ ] Add search input to filter organizations by name
  - [ ] Filter applies to visible tree

- [ ] Add organization details (AC: 9)
  - [ ] Click org to expand details or show modal
  - [ ] Display: website URL, jurisdiction areas, established date

- [ ] Handle loading and error states (AC: 10)
  - [ ] Show loading skeleton while fetching
  - [ ] Show error message if API fails
  - [ ] Show empty state if no organizations for branch

- [ ] Fix branch filter bug (AC: 11)
  - [ ] Verify API returns correct branch data
  - [ ] Verify frontend filters match backend enum values

---

## Dev Notes

### Relevant Source Files

```
frontend/src/
├── app/
│   └── government-orgs/
│       └── page.tsx                  # Existing gov orgs page (has bug)
├── lib/
│   └── api/
│       └── (government-orgs.ts if exists)
└── types/
    └── (GovernmentOrganization type)
```

### API Endpoint

```
GET /api/government-organizations
Query params:
  - page (default: 0)
  - size (default: 100)
  - branch (optional): executive | legislative | judicial

GET /api/government-organizations/by-branch/{branch}
Returns organizations for specific branch
```

### Bug Analysis (from Epic Investigation)

The current `/government-orgs` page only shows Executive Branch because:
- Legislative and Judicial organizations haven't been imported yet
- This is a **data gap**, not a code bug
- UI-1.9 and UI-1.10 will populate the missing data

### Branch Filter Values

Backend `GovernmentBranch` enum serializes as lowercase:
- `executive` (not `EXECUTIVE`)
- `legislative` (not `LEGISLATIVE`)
- `judicial` (not `JUDICIAL`)

Frontend must use lowercase values in API calls.

### Hierarchical Display

Organizations have parent-child relationships via `parentOrganizationId`. Display as collapsible tree:

```
Department of State
├── Bureau of Consular Affairs
├── Bureau of Diplomatic Security
└── Bureau of Educational and Cultural Affairs
```

### Existing Component Reference

Current `/government-orgs/page.tsx` has tree rendering logic that can be adapted:
- Groups orgs by branch
- Builds hierarchy from `parentId` relationships
- Expandable rows for children

### Dependencies

- **Requires UI-1.4** (Content Page Template) for `ContentPageHeader`
- **Requires UI-1.9** for Legislative Branch data
- **Requires UI-1.10** for Judicial Branch data
- **Uses existing API**: `/api/government-organizations`

---

## Testing

### Test File Location
`frontend/src/app/factbase/organizations/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses for each branch

### Test Cases
1. Executive page renders with orgs filtered by executive branch
2. Legislative page renders with orgs filtered by legislative branch
3. Judicial page renders with orgs filtered by judicial branch
4. Hierarchical tree displays parent-child relationships
5. Search filters organizations by name
6. Clicking org shows details
7. Loading state displays skeleton
8. Empty state shows when branch has no data
9. Each page has correct breadcrumbs

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-15 | 1.0 | Initial story creation | Winston (Architect) |

---

## Dev Agent Record

### Agent Model Used
*To be filled during implementation*

### Debug Log References
*To be filled during implementation*

### Completion Notes List
*To be filled during implementation*

### File List
*To be filled during implementation*

---

## QA Results
*To be filled after QA review*
