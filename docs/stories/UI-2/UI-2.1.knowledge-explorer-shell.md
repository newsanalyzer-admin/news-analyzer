# Story UI-2.1: Knowledge Explorer Shell & Navigation

## Status

**Draft**

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

- [ ] Create Knowledge Base route structure (AC: 4)
  - [ ] Create `frontend/src/app/knowledge-base/` directory
  - [ ] Create `frontend/src/app/knowledge-base/page.tsx` (main entry)
  - [ ] Create `frontend/src/app/knowledge-base/[entityType]/page.tsx`
  - [ ] Create `frontend/src/app/knowledge-base/layout.tsx`

- [ ] Create KnowledgeExplorer layout component (AC: 2, 3, 8)
  - [ ] Create `frontend/src/components/knowledge-base/KnowledgeExplorer.tsx`
  - [ ] Implement header with EntityTypeSelector and ViewModeSelector
  - [ ] Implement responsive layout with content area placeholder
  - [ ] Create `frontend/src/components/knowledge-base/index.ts` barrel export

- [ ] Create EntityTypeSelector component (AC: 2, 5, 9)
  - [ ] Create `frontend/src/components/knowledge-base/EntityTypeSelector.tsx`
  - [ ] Define entity type configuration structure
  - [ ] Implement tab/button group for type selection
  - [ ] Wire to URL params via Next.js router
  - [ ] Add keyboard navigation support

- [ ] Create ViewModeSelector component (AC: 3, 6, 9)
  - [ ] Create `frontend/src/components/knowledge-base/ViewModeSelector.tsx`
  - [ ] Show only views supported by current entity type
  - [ ] Wire to URL query params
  - [ ] Add keyboard navigation support

- [ ] Update hero page navigation (AC: 1)
  - [ ] Add single "Knowledge Base" link to hero page
  - [ ] Remove "Explore Factbase" link
  - [ ] Remove "View Government Orgs" link (if separate)

- [ ] Implement redirects (AC: 7)
  - [ ] Create redirect from `/factbase` → `/knowledge-base`
  - [ ] Create redirect from `/factbase/government-orgs` → `/knowledge-base/organizations`
  - [ ] Create redirect from `/factbase/people/*` → `/knowledge-base/people`
  - [ ] Test all redirects work correctly

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── app/
│   ├── page.tsx                          # Hero page (update navigation)
│   ├── factbase/                         # Current structure (to be replaced)
│   │   ├── layout.tsx                    # Uses PublicSidebar - reference pattern
│   │   ├── page.tsx                      # Current factbase landing
│   │   ├── organizations/                # Gov orgs pages
│   │   └── people/                       # People pages (judges, members, etc.)
│   └── knowledge-base/                   # NEW - create this
│       ├── layout.tsx                    # KnowledgeExplorer layout
│       ├── page.tsx                      # Landing/redirect to default entity type
│       └── [entityType]/
│           └── page.tsx                  # EntityBrowser placeholder
├── components/
│   ├── public/
│   │   └── PublicSidebar.tsx             # Existing - may reuse or replace
│   ├── sidebar/                          # Shared sidebar components (UI-1.1)
│   └── knowledge-base/                   # NEW - create this
│       ├── KnowledgeExplorer.tsx         # Main layout component
│       ├── EntityTypeSelector.tsx        # Tab/button selector
│       ├── ViewModeSelector.tsx          # View toggle
│       └── index.ts                      # Barrel export
├── stores/
│   └── publicSidebarStore.ts             # Existing store - reference
└── lib/
    └── config/
        └── entityTypes.ts                # NEW - entity type configuration
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
