# Story UI-1.6: Executive Appointees Page

## Status

**Ready**

---

## Story

**As a** user,
**I want** a public page to browse Executive Branch appointees at `/factbase/people/executive-appointees`,
**so that** I can learn about presidential appointees and their positions.

---

## Acceptance Criteria

1. Page is accessible at `/factbase/people/executive-appointees`
2. Page displays `ContentPageHeader` with title and educational description
3. Page shows a list/table of executive appointees from existing API
4. Appointees can be filtered by appointment type (PAS, PA, NA, CA, XS)
5. Appointees can be filtered by agency/department
6. Appointees can be searched by name or position title
7. Page displays name, position title, agency, and appointment type
8. Clicking an appointee shows more details
9. Page handles loading and error states gracefully
10. Page is read-only (no edit functionality)

---

## Tasks / Subtasks

- [ ] Create page route (AC: 1)
  - [ ] Create `frontend/src/app/factbase/people/executive-appointees/page.tsx`

- [ ] Add ContentPageHeader (AC: 2)
  - [ ] Import `ContentPageHeader` component
  - [ ] Import description from `page-descriptions.ts`
  - [ ] Add breadcrumbs: Factbase → People → Executive Appointees

- [ ] Implement appointee list display (AC: 3, 7)
  - [ ] Create table/list component for appointees
  - [ ] Display: name, position title, agency, appointment type
  - [ ] Use existing `/api/appointees` endpoint
  - [ ] Paginate results

- [ ] Add filtering functionality (AC: 4, 5, 6)
  - [ ] Appointment type filter: All, PAS, PA, NA, CA, XS
  - [ ] Agency filter: Dropdown with agencies
  - [ ] Search: Name or position title

- [ ] Add appointee details view (AC: 8)
  - [ ] Show expanded details on click (modal or side panel)
  - [ ] Include: full position details, agency info, tenure

- [ ] Handle loading and error states (AC: 9)
  - [ ] Show loading skeleton while fetching
  - [ ] Show error message if API fails
  - [ ] Show empty state if no results

- [ ] Ensure read-only (AC: 10)
  - [ ] No edit/delete/import buttons visible

---

## Dev Notes

### Relevant Source Files

```
frontend/src/
├── app/
│   └── admin/
│       └── factbase/
│           └── executive/
│               └── positions/
│                   └── page.tsx      # Admin appointees page (reference)
├── lib/
│   └── api/
│       └── (may need appointees.ts)
└── types/
    └── (appointee types if defined)
```

### API Endpoint

```
GET /api/appointees
Query params:
  - page (default: 0)
  - size (default: 20)
  - type (optional): PAS | PA | NA | CA | XS
  - orgId (optional): agency UUID
  - search (optional): name/title search

Response:
{
  content: Appointee[],
  totalElements: number,
  totalPages: number,
  ...
}
```

### Appointment Types (for filter labels)

| Code | Full Name |
|------|-----------|
| PAS | Presidential Appointment with Senate Confirmation |
| PA | Presidential Appointment (no Senate confirmation) |
| NA | Noncareer Appointment |
| CA | Career Appointment |
| XS | Schedule C (political appointees) |

### Data Display

```typescript
interface AppointeeDisplay {
  id: string;
  name: string;           // Person's full name
  positionTitle: string;  // e.g., "Secretary of State"
  agencyName: string;     // e.g., "Department of State"
  appointmentType: string; // PAS, PA, etc.
  tenure?: number;        // Years in position
}
```

### Dependencies

- **Requires UI-1.4** (Content Page Template) for `ContentPageHeader`
- **Uses existing API**: `/api/appointees`

---

## Testing

### Test File Location
`frontend/src/app/factbase/people/executive-appointees/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses

### Test Cases
1. Page renders with header, description, and appointee list
2. Appointment type filter updates displayed appointees
3. Agency filter updates displayed appointees
4. Search filters appointees by name/title
5. Clicking appointee shows details
6. Loading state displays skeleton
7. Error state displays error message
8. Pagination works correctly
9. No edit/delete buttons are visible

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
