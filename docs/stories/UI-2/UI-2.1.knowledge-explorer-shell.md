# Story UI-2.1: Knowledge Explorer Shell & Navigation

## Status

**Complete**

---

## Story

**As a** user exploring the Knowledge Base,
**I want** a unified entry point with clear navigation options,
**so that** I can easily find and explore authoritative data without confusion from multiple overlapping paths.

---

## Acceptance Criteria

1. Single "Knowledge Base" link on hero page replaces fragmented navigation
2. EntityTypeSelector component displays available entity types (Organizations, People)
3. ViewModeSelector component shows available views for selected entity type (List, Hierarchy)
4. URL structure follows `/knowledge-base/:entityType` pattern
5. Selecting entity type updates URL and loads appropriate content area
6. Selecting view mode updates URL query param and switches display
7. Old `/factbase/*` routes redirect to corresponding `/knowledge-base/*` routes
8. Layout is responsive (mobile-friendly)
9. Keyboard navigation works for selectors (Tab, Enter, Arrow keys)

---

## Tasks / Subtasks

- [x] Create Knowledge Base route structure (AC: 4)
  - [x] Create `frontend/src/app/knowledge-base/` directory
  - [x] Create `frontend/src/app/knowledge-base/page.tsx` (main entry)
  - [x] Create `frontend/src/app/knowledge-base/[entityType]/page.tsx`
  - [x] Create `frontend/src/app/knowledge-base/layout.tsx`

- [x] Create KnowledgeExplorer layout component (AC: 2, 3, 8)
  - [x] Create `frontend/src/components/knowledge-base/KnowledgeExplorer.tsx`
  - [x] Implement header with EntityTypeSelector and ViewModeSelector
  - [x] Implement responsive layout with content area placeholder
  - [x] Create `frontend/src/components/knowledge-base/index.ts` barrel export

- [x] Create EntityTypeSelector component (AC: 2, 5, 9)
  - [x] Create `frontend/src/components/knowledge-base/EntityTypeSelector.tsx`
  - [x] Define entity type configuration structure
  - [x] Implement tab/button group for type selection
  - [x] Wire to URL params via Next.js router
  - [x] Add keyboard navigation support

- [x] Create ViewModeSelector component (AC: 3, 6, 9)
  - [x] Create `frontend/src/components/knowledge-base/ViewModeSelector.tsx`
  - [x] Show only views supported by current entity type
  - [x] Wire to URL query params
  - [x] Add keyboard navigation support

- [x] Update hero page navigation (AC: 1)
  - [x] Add single "Knowledge Base" link to hero page
  - [x] Remove "Explore Factbase" link
  - [x] Remove "View Government Orgs" link (if separate)

- [x] Implement redirects (AC: 7)
  - [x] Create redirect from `/factbase` → `/knowledge-base`
  - [x] Create redirect from `/factbase/government-orgs` → `/knowledge-base/organizations`
  - [x] Create redirect from `/factbase/people/*` → `/knowledge-base/people`
  - [x] Test all redirects work correctly

---

## Dev Notes

### Relevant Source Tree

```
frontend/
├── next.config.js                        # EXISTS - add redirects here
└── src/
    ├── app/
    │   ├── page.tsx                      # Hero page (update navigation)
    │   ├── factbase/                     # Current structure (to be replaced)
    │   │   ├── layout.tsx                # Uses PublicSidebar - reference pattern
    │   │   ├── page.tsx                  # Current factbase landing
    │   │   ├── organizations/            # Gov orgs pages
    │   │   └── people/                   # People pages (judges, members, etc.)
    │   └── knowledge-base/               # NEW - create this
    │       ├── layout.tsx                # KnowledgeExplorer layout
    │       ├── page.tsx                  # Landing/redirect to default entity type
    │       └── [entityType]/
    │           └── page.tsx              # EntityBrowser placeholder
    ├── components/
    │   ├── public/
    │   │   └── PublicSidebar.tsx         # Existing - reference for mobile menu pattern
    │   ├── sidebar/                      # Shared sidebar components (UI-1.1)
    │   └── knowledge-base/               # NEW - create this
    │       ├── KnowledgeExplorer.tsx     # Main layout component
    │       ├── EntityTypeSelector.tsx    # Tab/button selector
    │       ├── ViewModeSelector.tsx      # View toggle
    │       └── index.ts                  # Barrel export
    ├── stores/
    │   └── publicSidebarStore.ts         # Existing Zustand store - reference
    └── lib/
        └── config/
            └── entityTypes.ts            # NEW - entity type configuration
```

### Key Implementation Details

**Hero Page Updates (`app/page.tsx`):**
- Replace "Explore Factbase" with "Knowledge Base" link to `/knowledge-base`
- Remove "View Government Orgs" link (now accessed via Knowledge Base)
- Keep other links (Entity Extraction, Members, Committees) for now

**KnowledgeExplorer Layout:**
- Reference existing `factbase/layout.tsx` for mobile menu pattern
- Header area: EntityTypeSelector + ViewModeSelector + SearchBar placeholder
- Content area: Renders child pages (EntityBrowser, etc.)
- Use Zustand store for UI state if needed

**EntityTypeSelector:**
```typescript
// Initial entity types for MVP
const entityTypes: EntityTypeConfig[] = [
  { id: 'organizations', label: 'Organizations', icon: Building2, supportedViews: ['list', 'hierarchy'] },
  { id: 'people', label: 'People', icon: Users, supportedViews: ['list'] },
];
```

**URL Structure:**
- `/knowledge-base` → redirects to `/knowledge-base/organizations` (default)
- `/knowledge-base/organizations` → EntityBrowser with orgs config
- `/knowledge-base/organizations?view=hierarchy` → HierarchyView
- `/knowledge-base/people?type=judges` → EntityBrowser filtered

**Redirects (next.config.js or middleware):**
```javascript
// In next.config.js
redirects: async () => [
  { source: '/factbase', destination: '/knowledge-base', permanent: true },
  { source: '/factbase/government-orgs', destination: '/knowledge-base/organizations', permanent: true },
  { source: '/factbase/organizations/:path*', destination: '/knowledge-base/organizations/:path*', permanent: true },
  { source: '/factbase/people/:path*', destination: '/knowledge-base/people/:path*', permanent: true },
]
```

### Architecture Reference

- Frontend: Next.js 14 App Router
- State: Zustand with localStorage persistence
- UI: Shadcn/UI + Tailwind CSS
- Icons: Lucide React

---

## Testing

### Test File Location
`frontend/src/components/knowledge-base/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Test component rendering and user interactions
- Test URL/routing behavior with Next.js router mocks
- Test keyboard accessibility

### Next.js Router Mocking Pattern
```typescript
// Mock Next.js navigation hooks
import { vi } from 'vitest';

const mockPush = vi.fn();
const mockReplace = vi.fn();

vi.mock('next/navigation', () => ({
  useRouter: () => ({
    push: mockPush,
    replace: mockReplace,
  }),
  usePathname: () => '/knowledge-base/organizations',
  useSearchParams: () => new URLSearchParams('view=list'),
}));

// Reset mocks between tests
beforeEach(() => {
  mockPush.mockClear();
  mockReplace.mockClear();
});
```

### Test Cases

1. **KnowledgeExplorer Layout**
   - Renders header with EntityTypeSelector and ViewModeSelector
   - Renders content area with children
   - Responsive: mobile menu toggle works

2. **EntityTypeSelector**
   - Renders all configured entity types
   - Clicking type updates URL to `/knowledge-base/:type`
   - Current type is visually highlighted
   - Keyboard: Tab navigates, Enter selects

3. **ViewModeSelector**
   - Shows only views supported by current entity type
   - Clicking view updates URL query param `?view=`
   - Current view is visually highlighted
   - Hidden when entity type supports only one view

4. **Redirects**
   - `/factbase` redirects to `/knowledge-base`
   - `/factbase/government-orgs` redirects to `/knowledge-base/organizations`
   - Old routes return 301 permanent redirect

5. **Hero Page**
   - "Knowledge Base" link exists and points to `/knowledge-base`
   - Old "Explore Factbase" link removed
   - Old "View Government Orgs" link removed

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-22 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-12-26 | 1.1 | Validation: updated source tree paths, added router mocking pattern, status → Approved | James (Dev) |
| 2025-12-26 | 1.2 | Implementation complete: all tasks done, 22 tests passing, status → Complete | James (Dev) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Fixed React Hooks rules violation in ViewModeSelector (hooks called after early return)
- Added Suspense boundary in layout for SSR compatibility with useSearchParams
- Added testing infrastructure (vitest.config.ts, test setup with Next.js router mocks)
- Added testing dependencies (@testing-library/react, @testing-library/jest-dom, jsdom)

### Completion Notes List
1. Created Knowledge Base route structure with dynamic [entityType] routing
2. Implemented EntityTypeSelector with full keyboard navigation (Arrow keys, Home, End)
3. Implemented ViewModeSelector that conditionally renders based on supported views
4. Created entity type configuration system in `lib/config/entityTypes.ts`
5. Updated hero page: replaced "Explore Factbase" with "Knowledge Base", removed "View Government Orgs"
6. Added comprehensive redirects for all old /factbase/* routes to /knowledge-base/*
7. Created 22 unit tests covering all components with 100% pass rate
8. All acceptance criteria verified: AC 1-9 satisfied

### File List

**New Files Created:**
- `frontend/src/app/knowledge-base/page.tsx` - Landing page with redirect to default entity type
- `frontend/src/app/knowledge-base/layout.tsx` - Layout with Suspense boundary and mobile menu
- `frontend/src/app/knowledge-base/[entityType]/page.tsx` - Dynamic entity browser page
- `frontend/src/components/knowledge-base/KnowledgeExplorer.tsx` - Main layout shell component
- `frontend/src/components/knowledge-base/EntityTypeSelector.tsx` - Entity type tab selector
- `frontend/src/components/knowledge-base/ViewModeSelector.tsx` - View mode toggle (List/Hierarchy)
- `frontend/src/components/knowledge-base/index.ts` - Barrel export
- `frontend/src/lib/config/entityTypes.ts` - Entity type configuration with TypeScript interfaces
- `frontend/vitest.config.ts` - Vitest test runner configuration
- `frontend/src/test/setup.ts` - Test setup with Next.js router mocks
- `frontend/src/components/knowledge-base/__tests__/EntityTypeSelector.test.tsx` - 8 unit tests
- `frontend/src/components/knowledge-base/__tests__/ViewModeSelector.test.tsx` - 8 unit tests
- `frontend/src/components/knowledge-base/__tests__/KnowledgeExplorer.test.tsx` - 6 unit tests

**Modified Files:**
- `frontend/src/app/page.tsx` - Updated hero page navigation links
- `frontend/next.config.js` - Added redirects configuration for /factbase/* routes
- `frontend/package.json` - Added testing dependencies

---

## QA Results
_To be filled by QA Agent_
