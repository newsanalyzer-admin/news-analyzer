# Story UI-1.5: Congressional Members Page

## Status

**Ready**

---

## Story

**As a** user,
**I want** a public page to browse Congressional members at `/factbase/people/congressional-members`,
**so that** I can learn about current Senators and Representatives.

---

## Acceptance Criteria

1. Page is accessible at `/factbase/people/congressional-members`
2. Page displays `ContentPageHeader` with title and educational description
3. Page shows a list/table of Congressional members from existing API
4. Members can be filtered by chamber (House/Senate)
5. Members can be filtered by state
6. Members can be searched by name
7. Page displays member photo, name, party, state, and chamber
8. Clicking a member shows more details (modal or expand)
9. Page handles loading and error states gracefully
10. Page is read-only (no edit functionality)

---

## Tasks / Subtasks

- [ ] Create page route structure (AC: 1)
  - [ ] Create `frontend/src/app/factbase/people/page.tsx` (People hub)
  - [ ] Create `frontend/src/app/factbase/people/congressional-members/page.tsx`

- [ ] Add ContentPageHeader (AC: 2)
  - [ ] Import `ContentPageHeader` component
  - [ ] Import description from `page-descriptions.ts`
  - [ ] Add breadcrumbs: Factbase → People → Congressional Members

- [ ] Implement member list display (AC: 3, 7)
  - [ ] Reuse existing `MemberTable` component from `components/congressional/`
  - [ ] Or create new simplified read-only table
  - [ ] Display: photo, name, party, state, chamber
  - [ ] Use existing `/api/members` endpoint

- [ ] Add filtering functionality (AC: 4, 5, 6)
  - [ ] Reuse existing `MemberFilters` component or create simplified version
  - [ ] Chamber filter: All, House, Senate
  - [ ] State filter: Dropdown with all states
  - [ ] Name search: Text input with debounce

- [ ] Add member details view (AC: 8)
  - [ ] Reuse existing `MemberProfile` component or create modal
  - [ ] Show expanded details on click
  - [ ] Include committees, terms, social media if available

- [ ] Handle loading and error states (AC: 9)
  - [ ] Show loading skeleton while fetching
  - [ ] Show error message if API fails
  - [ ] Show empty state if no results

- [ ] Ensure read-only (AC: 10)
  - [ ] Remove any edit/delete buttons from reused components
  - [ ] No admin actions visible

---

## Dev Notes

### Relevant Source Files

```
frontend/src/
├── app/
│   └── members/
│       └── page.tsx                  # Existing members page (reference)
├── components/
│   └── congressional/
│       ├── MemberTable.tsx           # Can be reused
│       ├── MemberFilters.tsx         # Can be reused
│       ├── MemberStats.tsx           # Optional reuse
│       ├── MemberProfile.tsx         # Can be reused for details
│       ├── MemberPhoto.tsx           # Can be reused
│       └── MemberCommittees.tsx      # Can be reused in details
├── hooks/
│   └── useMembers.ts                 # Existing hook for API calls
└── lib/
    └── api/
        └── members.ts                # API client
```

### API Endpoint

```
GET /api/members
Query params:
  - page (default: 0)
  - size (default: 20)
  - chamber (optional): HOUSE | SENATE
  - state (optional): two-letter code
  - search (optional): name search

Response:
{
  content: Member[],
  totalElements: number,
  totalPages: number,
  ...
}
```

### Component Reuse Strategy

The existing `/members` page has comprehensive components. Options:
1. **Full reuse**: Import and use existing components directly
2. **Partial reuse**: Copy and simplify for read-only public use
3. **Wrapper approach**: Create thin wrappers that hide admin functionality

**Recommendation**: Start with full reuse, strip admin-only features via props or conditional rendering.

### Dependencies

- **Requires UI-1.4** (Content Page Template) for `ContentPageHeader`
- **Uses existing API**: `/api/members`
- **Uses existing components**: `MemberTable`, `MemberFilters`

---

## Testing

### Test File Location
`frontend/src/app/factbase/people/congressional-members/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses

### Test Cases
1. Page renders with header, description, and member list
2. Chamber filter updates displayed members
3. State filter updates displayed members
4. Name search filters members
5. Clicking member shows details
6. Loading state displays skeleton
7. Error state displays error message
8. No edit/delete buttons are visible

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
