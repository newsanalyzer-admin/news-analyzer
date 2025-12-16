# Story FB-1-UI.2: Members Listing & Search Page

## Status

**Done**

## Story

**As a** fact-checker or researcher,
**I want** to browse, search, and filter Congressional members,
**so that** I can quickly find information about specific legislators for verification purposes.

## Acceptance Criteria

1. A new `/members` page is accessible from the main navigation
2. The page displays a paginated table of Congressional members using shadcn/ui Table component
3. Each row shows: Name (with photo), Party, State, Chamber, and a link to detail page
4. Search by name functionality with debounced input (300ms)
5. Filter by Chamber (All, Senate, House) using shadcn/ui Select
6. Filter by State using shadcn/ui Select (all 50 states + territories)
7. Filter by Party using shadcn/ui Select (Democrat, Republican, Independent)
8. Filters are persisted in URL params for shareable links (e.g., `/members?chamber=SENATE&state=CA`)
9. Statistics section shows total member count, party distribution chart/badges
10. Pagination controls allow navigation through results (20 items per page default)
11. Loading states use shadcn/ui Skeleton components
12. Error states display user-friendly messages with retry option
13. Empty states (no results) display appropriate messaging
14. Page is responsive and works on mobile devices
15. Existing pages (`/entities`, `/government-orgs`) have no regression

## Tasks / Subtasks

- [x] **Task 1: Create page structure** (AC: 1)
  - [x] Create `app/members/page.tsx`
  - [x] Create `app/members/loading.tsx` with skeleton UI
  - [x] Create `app/members/error.tsx` with error boundary
  - [x] Add "Members" link to `app/page.tsx` in the navigation button group (alongside existing "Entity Extraction" and "Government Orgs" buttons)

- [x] **Task 2: Create MemberFilters component** (AC: 5, 6, 7, 8)
  - [x] Create `components/congressional/MemberFilters.tsx`
  - [x] Implement Chamber filter (Select: All, Senate, House)
  - [x] Implement State filter (Select with all states)
  - [x] Implement Party filter (Select: All, Democrat, Republican, Independent)
  - [x] Use `useSearchParams()` to read current filters from URL
  - [x] Use `useRouter().push()` to update URL when filters change
  - [x] Ensure filters are properly URL-encoded

- [x] **Task 3: Create MemberTable component** (AC: 2, 3, 10)
  - [x] Create `components/congressional/MemberTable.tsx`
  - [x] Use shadcn/ui Table component for layout
  - [x] Display columns: Photo, Name, Party, State, Chamber, Actions
  - [x] Implement member photo with Next.js Image (fallback to initials)
  - [x] Add link to `/members/[bioguideId]` for each row
  - [x] Implement pagination controls using shadcn/ui Button
  - [x] Handle page size and current page state

- [x] **Task 4: Implement search functionality** (AC: 4)
  - [x] Add search Input above the table
  - [x] Implement 300ms debounce using custom hook or lodash.debounce
  - [x] Update URL params with search query
  - [x] Clear search button functionality

- [x] **Task 5: Create statistics section** (AC: 9)
  - [x] Create stats summary showing total member count
  - [x] Display party distribution using Badges (D: X, R: Y, I: Z)
  - [x] Use `useMemberStats()` hook for data

- [x] **Task 6: Implement loading and error states** (AC: 11, 12, 13)
  - [x] Create skeleton loading state for table
  - [x] Create skeleton loading state for filters
  - [x] Implement error display with retry button
  - [x] Implement empty state with helpful message

- [x] **Task 7: Responsive design** (AC: 14)
  - [x] Test on mobile viewport sizes
  - [x] Implement responsive table (horizontal scroll or card view on mobile)
  - [x] Ensure filters stack appropriately on small screens

- [x] **Task 8: Integration and testing** (AC: 15)
  - [x] Verify `/entities` page works correctly
  - [x] Verify `/government-orgs` page works correctly
  - [x] Test all filter combinations
  - [x] Test pagination behavior
  - [x] Test search functionality
  - [x] Test URL param persistence (refresh maintains filters)

## Dev Notes

### Dependencies (from FB-1-UI.1)

This story depends on:
- shadcn/ui components: Table, Select, Input, Button, Badge, Skeleton
- Types: Person, Page<T>
- API client: `membersApi` functions
- Hooks: `useMembers`, `useMemberSearch`, `useMemberStats`

### File Structure

```
frontend/src/
├── app/
│   └── members/
│       ├── page.tsx              # Main members listing page
│       ├── loading.tsx           # Loading skeleton
│       └── error.tsx             # Error boundary
├── components/
│   └── congressional/
│       ├── MemberTable.tsx       # Table with pagination
│       ├── MemberFilters.tsx     # Filter controls
│       └── MemberPhoto.tsx       # Photo with initials fallback
├── hooks/
│   └── useDebounce.ts            # Debounce hook for search input
└── lib/
    └── constants/
        └── states.ts             # US_STATES array for state filter
```

### URL Param Structure

```
/members?chamber=SENATE&state=CA&party=Democrat&search=warren&page=1
```

All params are optional. Defaults:
- chamber: undefined (all)
- state: undefined (all)
- party: undefined (all)
- search: undefined (none)
- page: 0 (first page, 0-indexed to match Spring)

### API Endpoints Used

| Endpoint | Hook | Purpose |
|----------|------|---------|
| GET /api/members | `useMembers` | Main paginated list |
| GET /api/members/search | `useMemberSearch` | Name search |
| GET /api/members/by-state/{state} | `useMembers` | State filter |
| GET /api/members/by-chamber/{chamber} | `useMembers` | Chamber filter |
| GET /api/members/count | `useMemberStats` | Total count |
| GET /api/members/stats/party | `useMemberStats` | Party distribution |

### State Options

```typescript
// Place in: lib/constants/states.ts
export const US_STATES = [
  { value: 'AL', label: 'Alabama' },
  { value: 'AK', label: 'Alaska' },
  { value: 'AZ', label: 'Arizona' },
  { value: 'AR', label: 'Arkansas' },
  { value: 'CA', label: 'California' },
  { value: 'CO', label: 'Colorado' },
  { value: 'CT', label: 'Connecticut' },
  { value: 'DE', label: 'Delaware' },
  { value: 'FL', label: 'Florida' },
  { value: 'GA', label: 'Georgia' },
  { value: 'HI', label: 'Hawaii' },
  { value: 'ID', label: 'Idaho' },
  { value: 'IL', label: 'Illinois' },
  { value: 'IN', label: 'Indiana' },
  { value: 'IA', label: 'Iowa' },
  { value: 'KS', label: 'Kansas' },
  { value: 'KY', label: 'Kentucky' },
  { value: 'LA', label: 'Louisiana' },
  { value: 'ME', label: 'Maine' },
  { value: 'MD', label: 'Maryland' },
  { value: 'MA', label: 'Massachusetts' },
  { value: 'MI', label: 'Michigan' },
  { value: 'MN', label: 'Minnesota' },
  { value: 'MS', label: 'Mississippi' },
  { value: 'MO', label: 'Missouri' },
  { value: 'MT', label: 'Montana' },
  { value: 'NE', label: 'Nebraska' },
  { value: 'NV', label: 'Nevada' },
  { value: 'NH', label: 'New Hampshire' },
  { value: 'NJ', label: 'New Jersey' },
  { value: 'NM', label: 'New Mexico' },
  { value: 'NY', label: 'New York' },
  { value: 'NC', label: 'North Carolina' },
  { value: 'ND', label: 'North Dakota' },
  { value: 'OH', label: 'Ohio' },
  { value: 'OK', label: 'Oklahoma' },
  { value: 'OR', label: 'Oregon' },
  { value: 'PA', label: 'Pennsylvania' },
  { value: 'RI', label: 'Rhode Island' },
  { value: 'SC', label: 'South Carolina' },
  { value: 'SD', label: 'South Dakota' },
  { value: 'TN', label: 'Tennessee' },
  { value: 'TX', label: 'Texas' },
  { value: 'UT', label: 'Utah' },
  { value: 'VT', label: 'Vermont' },
  { value: 'VA', label: 'Virginia' },
  { value: 'WA', label: 'Washington' },
  { value: 'WV', label: 'West Virginia' },
  { value: 'WI', label: 'Wisconsin' },
  { value: 'WY', label: 'Wyoming' },
  // Territories
  { value: 'DC', label: 'District of Columbia' },
  { value: 'PR', label: 'Puerto Rico' },
  { value: 'GU', label: 'Guam' },
  { value: 'VI', label: 'U.S. Virgin Islands' },
  { value: 'AS', label: 'American Samoa' },
  { value: 'MP', label: 'Northern Mariana Islands' },
];
```

### Party Badge Colors

```tsx
const partyColors = {
  Democrat: 'bg-blue-100 text-blue-800',
  Republican: 'bg-red-100 text-red-800',
  Independent: 'bg-purple-100 text-purple-800',
};
```

### Member Photo Handling

```tsx
// Use Next.js Image with fallback
<Image
  src={member.imageUrl || '/placeholder-avatar.png'}
  alt={`${member.firstName} ${member.lastName}`}
  width={40}
  height={40}
  className="rounded-full"
/>

// Or generate initials fallback
function getInitials(firstName: string, lastName: string) {
  return `${firstName[0]}${lastName[0]}`.toUpperCase();
}
```

### Debounce Implementation

```typescript
// Place in: hooks/useDebounce.ts
import { useState, useEffect } from 'react';

/**
 * Hook to debounce a value by a specified delay
 * @param value - The value to debounce
 * @param delay - Delay in milliseconds (use 300 for search input per AC)
 */
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState(value);

  useEffect(() => {
    const handler = setTimeout(() => setDebouncedValue(value), delay);
    return () => clearTimeout(handler);
  }, [value, delay]);

  return debouncedValue;
}
```

### Testing

#### Test Scenarios
1. Initial load shows all members paginated
2. Selecting "Senate" filter shows only senators
3. Selecting "CA" state shows only California members
4. Searching "Warren" shows Elizabeth Warren
5. Combining filters works correctly (Senate + CA)
6. Refreshing page maintains URL params
7. Pagination next/prev works
8. Empty search results shows appropriate message
9. API error shows error state with retry

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-29 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-29 | 1.1 | Added complete US_STATES list, specified useDebounce.ts location, clarified navigation placement | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20250101)

### Debug Log References
- TypeScript compilation: No errors
- ESLint: Pre-existing warnings in /entities and /government-orgs (not related to this story)

### Completion Notes List
- Created Members listing page with full search, filter, and pagination functionality
- Implemented useDebounce hook for 300ms search debounce per AC
- Created MemberPhoto component with initials fallback for missing images
- Created MemberFilters with URL param persistence for shareable links
- Created MemberTable with desktop table and mobile card views
- Created MemberStats showing total count and party distribution badges
- Added navigation link on home page alongside existing Entity Extraction and Government Orgs buttons
- All filter combinations supported via URL params (chamber, state, party, search, page)

### File List
| File | Action |
|------|--------|
| `frontend/src/app/members/page.tsx` | Created |
| `frontend/src/app/members/loading.tsx` | Created |
| `frontend/src/app/members/error.tsx` | Created |
| `frontend/src/components/congressional/MemberFilters.tsx` | Created |
| `frontend/src/components/congressional/MemberTable.tsx` | Created |
| `frontend/src/components/congressional/MemberStats.tsx` | Created |
| `frontend/src/components/congressional/MemberPhoto.tsx` | Created |
| `frontend/src/hooks/useDebounce.ts` | Created |
| `frontend/src/lib/constants/states.ts` | Created |
| `frontend/src/app/page.tsx` | Modified (added Members nav link) |

---

## QA Results

### Review Date: 2025-11-30

### Reviewed By: Quinn (Test Architect)

### Risk Assessment

**Review Depth: Standard** - No auto-escalation triggers:
- No auth/payment/security files touched
- Story has 15 ACs (triggers deep review consideration, but all are UI-focused)
- No previous gate failures
- All changes are frontend/UI only

### Code Quality Assessment

**Overall: GOOD** - The implementation is clean, well-structured, and follows established patterns.

**Architecture & Patterns:**
- Proper component decomposition (MemberTable, MemberFilters, MemberStats, MemberPhoto)
- Good separation of concerns between page, components, and hooks
- Consistent use of React Query for data fetching
- Proper use of URL params for state persistence (shareable links)
- Mobile-first responsive design with desktop table / mobile card views

**Strengths:**
1. Clean, readable component code with clear documentation headers
2. Proper TypeScript typing throughout
3. Good error handling with retry functionality
4. Loading states implemented correctly with skeletons
5. Debounce implementation is correct and reusable
6. URL parameter handling is well-implemented with proper encoding

**Minor Observations (non-blocking):**
1. `MemberFilters.tsx:74-77` - The useEffect for updating URL on debounced search runs on initial render, which could cause an unnecessary URL push. Consider adding a ref to track if it's the initial mount.
2. `MemberPhoto.tsx:20-22` - The `getInitials` function doesn't handle empty firstName/lastName gracefully (would return empty string). Current implementation uses `|| ''` which is adequate.
3. `partyColors` is duplicated in `MemberTable.tsx` and `MemberStats.tsx` - could be extracted to a shared constant, but acceptable for current scope.

### Refactoring Performed

None - Code quality is sufficient for the story scope. The minor observations noted are nice-to-have improvements that don't warrant immediate changes.

### Compliance Check

- Coding Standards: ✓ Follows TypeScript/React standards from `coding-standards.md`
- Project Structure: ✓ Files placed correctly per `source-tree.md` patterns
- Testing Strategy: N/A - No automated tests required per story specification
- All ACs Met: ✓ All 15 acceptance criteria verified

### Requirements Traceability (Given-When-Then)

| AC | Test Scenario | Coverage |
|----|---------------|----------|
| 1 | **Given** I'm on the home page, **When** I click "Browse Members", **Then** I navigate to `/members` | Manual ✓ |
| 2 | **Given** I'm on `/members`, **When** the page loads, **Then** I see a paginated table with shadcn/ui Table | Code ✓ |
| 3 | **Given** I'm viewing the table, **When** I look at a row, **Then** I see Photo, Name, Party, State, Chamber, View link | Code ✓ |
| 4 | **Given** I type in search, **When** I pause typing, **Then** search fires after 300ms debounce | Code ✓ |
| 5 | **Given** I'm on members page, **When** I use Chamber filter, **Then** I can select All/Senate/House | Code ✓ |
| 6 | **Given** I'm on members page, **When** I use State filter, **Then** I see all 50 states + territories | Code ✓ (56 options) |
| 7 | **Given** I'm on members page, **When** I use Party filter, **Then** I can select All/D/R/I | Code ✓ |
| 8 | **Given** I apply filters, **When** I share the URL, **Then** recipient sees same filtered results | Code ✓ |
| 9 | **Given** I'm on members page, **When** I look at stats section, **Then** I see total count and party badges | Code ✓ |
| 10 | **Given** results > 20, **When** I use pagination, **Then** I can navigate pages | Code ✓ |
| 11 | **Given** page is loading, **When** I view the page, **Then** I see skeleton placeholders | Code ✓ |
| 12 | **Given** API error occurs, **When** I view the page, **Then** I see error message with retry button | Code ✓ |
| 13 | **Given** no results match filters, **When** I view the page, **Then** I see empty state message | Code ✓ |
| 14 | **Given** I'm on mobile, **When** I view the page, **Then** layout is responsive (card view) | Code ✓ |
| 15 | **Given** I visit `/entities` or `/government-orgs`, **When** I navigate there, **Then** pages still work | Manual ✓ |

### Improvements Checklist

[Check off items handled, leave unchecked for optional future improvements]

- [x] All components properly typed with TypeScript
- [x] Loading states implemented with skeletons
- [x] Error states implemented with retry
- [x] Empty states implemented with helpful messaging
- [x] Responsive design with mobile card view
- [x] URL param persistence working
- [x] Debounce hook implemented correctly
- [ ] Consider extracting `partyColors` to shared constant (nice-to-have)
- [ ] Consider adding initial mount check for search useEffect (nice-to-have)
- [ ] Consider adding keyboard navigation for table rows (accessibility enhancement)

### Security Review

**Status: PASS** - No security concerns identified.
- No authentication/authorization requirements for this page
- No user input is sent to backend without proper encoding
- URL parameters are properly sanitized
- No PII exposure concerns

### Performance Considerations

**Status: PASS** - Implementation follows good performance practices.
- Uses React Query for efficient data caching
- Implements debounce to prevent excessive API calls
- Pagination limits data transfer to 20 items per request
- No unnecessary re-renders identified
- Skeleton loading provides good perceived performance

### Testability Evaluation

- **Controllability: GOOD** - Components accept props for all configurable behavior
- **Observability: GOOD** - State changes reflected in URL and UI
- **Debuggability: GOOD** - Clear component structure, error logging in place

### Files Modified During Review

None - No code modifications made during review.

### Gate Status

Gate: **PASS** → docs/qa/gates/FB-1-UI.2-members-listing-page.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, code quality is good, no blocking issues identified.
