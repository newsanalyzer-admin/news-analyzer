# Story UI-1.7: Federal Judges Page

## Status

**Ready** - BLOCKED by UI-1.11

---

## Story

**As a** user,
**I want** a public page to browse Federal Judges at `/factbase/people/federal-judges`,
**so that** I can learn about judges serving on federal courts.

---

## Blocked By

**UI-1.11 (Federal Judges Data Research & Import)** must be completed first to:
1. Identify and validate the data source (FJC API recommended)
2. Implement the data import service
3. Populate the database with federal judges data

This story cannot be implemented until judge data is available.

---

## Acceptance Criteria

1. Page is accessible at `/factbase/people/federal-judges`
2. Page displays `ContentPageHeader` with title and educational description
3. Page shows a list/table of federal judges from API
4. Judges can be filtered by court level (Supreme, Appeals, District)
5. Judges can be filtered by circuit (1st-11th, DC, Federal)
6. Judges can be searched by name
7. Page displays name, court, appointment date, appointing president, status
8. Clicking a judge shows more details
9. Page handles loading and error states gracefully
10. Page is read-only (no edit functionality)

---

## Tasks / Subtasks

*Note: Tasks depend on UI-1.11 outcome*

- [ ] Create page route (AC: 1)
  - [ ] Create `frontend/src/app/factbase/people/federal-judges/page.tsx`

- [ ] Add ContentPageHeader (AC: 2)
  - [ ] Import `ContentPageHeader` component
  - [ ] Import description from `page-descriptions.ts`
  - [ ] Add breadcrumbs: Factbase → People → Federal Judges

- [ ] Implement judge list display (AC: 3, 7)
  - [ ] Create table/list component for judges
  - [ ] Display: name, court, appointment date, appointing president, status
  - [ ] Use new `/api/judges` endpoint (from UI-1.11)
  - [ ] Paginate results

- [ ] Add filtering functionality (AC: 4, 5, 6)
  - [ ] Court level filter: All, Supreme Court, Courts of Appeals, District Courts
  - [ ] Circuit filter: Dropdown with circuits (for Appeals/District)
  - [ ] Search: Judge name

- [ ] Add judge details view (AC: 8)
  - [ ] Show expanded details on click
  - [ ] Include: full biography, career history, notable cases (if available)

- [ ] Handle loading and error states (AC: 9)
  - [ ] Show loading skeleton while fetching
  - [ ] Show error message if API fails
  - [ ] Show empty state if no results

- [ ] Ensure read-only (AC: 10)
  - [ ] No edit/delete buttons visible

---

## Dev Notes

### Blocked Status

This story is **BLOCKED** pending completion of UI-1.11.

**If UI-1.11 determines FJC API is not viable:**
- This story may be descoped or deferred
- Alternative: Show "Coming Soon" placeholder page

### Expected API Endpoint (pending UI-1.11)

```
GET /api/judges
Query params:
  - page (default: 0)
  - size (default: 20)
  - courtLevel (optional): SUPREME | APPEALS | DISTRICT
  - circuit (optional): 1-11, DC, FEDERAL
  - search (optional): judge name

Response:
{
  content: Judge[],
  totalElements: number,
  totalPages: number,
  ...
}
```

### Expected Data Model (pending UI-1.11)

```typescript
interface JudgeDisplay {
  id: string;
  fullName: string;
  courtName: string;           // e.g., "U.S. Court of Appeals for the 9th Circuit"
  courtLevel: string;          // SUPREME | APPEALS | DISTRICT
  circuit?: string;            // For Appeals/District courts
  appointingPresident: string; // e.g., "Joseph R. Biden"
  commissionDate: string;      // Date of commission
  status: string;              // Active | Senior | Deceased
}
```

### Federal Court Structure

| Level | Count | Description |
|-------|-------|-------------|
| Supreme Court | 9 | Chief Justice + 8 Associate Justices |
| Courts of Appeals | ~179 | 13 circuits (11 numbered + DC + Federal) |
| District Courts | ~670 | 94 judicial districts |

### Dependencies

- **BLOCKED BY UI-1.11** (Federal Judges Data Research & Import)
- **Requires UI-1.4** (Content Page Template) for `ContentPageHeader`

---

## Testing

### Test File Location
`frontend/src/app/factbase/people/federal-judges/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses

### Test Cases
1. Page renders with header, description, and judge list
2. Court level filter updates displayed judges
3. Circuit filter updates displayed judges (when applicable)
4. Search filters judges by name
5. Clicking judge shows details
6. Loading state displays skeleton
7. Error state displays error message
8. No edit/delete buttons are visible

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-15 | 1.0 | Initial story creation (BLOCKED status) | Winston (Architect) |

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
