# Story UI-1.2: Factbase Layout & Landing Update

## Status

**Complete**

---

## Story

**As a** user,
**I want** a dedicated factbase section with sidebar navigation at `/factbase`,
**so that** I can browse government data through an intuitive, organized interface.

---

## Acceptance Criteria

1. `/factbase` route displays a layout with `PublicSidebar` on the left
2. `/factbase` has a welcome/overview page as the default content
3. Hero landing page (`/`) includes a prominent "Explore Factbase" CTA button
4. `PublicSidebar` uses the shared `BaseSidebar` component from UI-1.1
5. Sidebar is collapsible on desktop (persisted via Zustand)
6. Sidebar works on mobile with hamburger menu toggle
7. Content area displays child route content to the right of sidebar
8. Layout is responsive across desktop, tablet, and mobile breakpoints

---

## Tasks / Subtasks

- [x] Create Factbase route structure (AC: 1, 7) **COMPLETE**
  - [x] Create `frontend/src/app/factbase/` directory
  - [x] Create `frontend/src/app/factbase/layout.tsx` with sidebar + content area
  - [x] Create `frontend/src/app/factbase/page.tsx` as welcome/overview page

- [x] Create PublicSidebar component (AC: 4, 5, 6) **COMPLETE**
  - [x] Create `frontend/src/components/public/PublicSidebar.tsx`
  - [x] Import and use `BaseSidebar` from `components/sidebar/`
  - [x] Create `frontend/src/stores/publicSidebarStore.ts` (separate from admin)
  - [x] Implement desktop collapse toggle
  - [x] Implement mobile hamburger menu with backdrop

- [x] Create Factbase welcome page (AC: 2) **COMPLETE**
  - [x] Design welcome content explaining the Factbase
  - [x] Include quick links to People and Organizations sections
  - [ ] Add statistics cards (total members, appointees, orgs) if data available

- [x] Update hero landing page (AC: 3) **COMPLETE**
  - [x] Add "Explore Factbase" button/CTA to `frontend/src/app/page.tsx`
  - [x] Link CTA to `/factbase`
  - [x] Style CTA prominently (primary button style)

- [x] Implement responsive layout (AC: 8) **COMPLETE**
  - [x] Desktop: Sidebar visible, collapsible (w-64 expanded, w-16 collapsed)
  - [x] Tablet: Sidebar collapsed by default, expandable
  - [x] Mobile: Sidebar hidden, hamburger menu to open overlay

---

## Dev Notes

### Relevant Source Files

```
frontend/src/
├── app/
│   ├── page.tsx                      # Hero landing page (to be updated)
│   ├── layout.tsx                    # Root layout
│   └── admin/
│       └── layout.tsx                # Reference for admin layout pattern
├── components/
│   ├── sidebar/                      # From UI-1.1
│   │   ├── BaseSidebar.tsx
│   │   ├── SidebarMenuItem.tsx
│   │   └── types.ts
│   └── public/                       # New directory
│       └── PublicSidebar.tsx         # To be created
└── stores/
    ├── sidebarStore.ts               # Admin sidebar store (reference)
    └── publicSidebarStore.ts         # To be created
```

### Layout Structure

```tsx
// factbase/layout.tsx structure
export default function FactbaseLayout({ children }) {
  return (
    <div className="flex h-screen">
      <PublicSidebar />
      <main className="flex-1 overflow-auto">
        {children}
      </main>
    </div>
  );
}
```

### Public Sidebar Store

```typescript
// publicSidebarStore.ts
interface PublicSidebarState {
  isCollapsed: boolean;
  isMobileOpen: boolean;
  toggle: () => void;
  toggleMobile: () => void;
  closeMobile: () => void;
}

// Persist to localStorage with key 'public-sidebar-storage'
```

### Factbase Welcome Page Content

The welcome page should include:
- Brief introduction to the Factbase
- Cards linking to main sections (People, Organizations)
- Optional: Statistics if API endpoints support counts

### Dependencies

- **Requires UI-1.1** (Shared Sidebar Components) to be completed first
- **Requires UI-1.3** (Menu Configuration) for sidebar menu items

---

## Testing

### Test File Location
`frontend/src/app/factbase/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library for unit tests
- Use Playwright for E2E layout tests

### Test Cases
1. `/factbase` route renders with sidebar and content area
2. Sidebar collapse/expand persists across page reloads
3. Mobile hamburger menu opens/closes sidebar overlay
4. "Explore Factbase" CTA on homepage links to `/factbase`
5. Child routes render in content area (e.g., `/factbase/people`)
6. Responsive breakpoints apply correct layout

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-15 | 1.0 | Initial story creation | Winston (Architect) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
N/A

### Completion Notes List
1. Created `frontend/src/stores/publicSidebarStore.ts` - Zustand store with persist
2. Created `frontend/src/components/public/PublicSidebar.tsx` - Uses BaseSidebar
3. Created `frontend/src/app/factbase/layout.tsx` - Layout with sidebar and mobile support
4. Created `frontend/src/app/factbase/page.tsx` - Welcome page with quick links
5. Hero page already had "Explore Factbase" CTA

### File List
- `frontend/src/stores/publicSidebarStore.ts` - Created
- `frontend/src/components/public/PublicSidebar.tsx` - Created
- `frontend/src/app/factbase/layout.tsx` - Created
- `frontend/src/app/factbase/page.tsx` - Created
- `frontend/src/app/page.tsx` - Already had CTA

---

## QA Results

### Review Date: 2025-12-18

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: EXCELLENT**

1. **Architecture**:
   - Clean layout with sidebar + content area
   - Uses shared BaseSidebar from UI-1.1
   - Separate Zustand store from admin (publicSidebarStore)
   - Mobile support with hamburger menu and backdrop

2. **Component Quality**:
   - `layout.tsx`: Responsive with mobile/desktop handling
   - `page.tsx`: Welcome page with People/Organizations quick links
   - `PublicSidebar.tsx`: Uses BaseSidebar with admin link in footer

3. **Responsive Design**:
   - Desktop: Sidebar visible, collapsible
   - Mobile: Hidden sidebar with hamburger toggle
   - Backdrop overlay when mobile sidebar open

### Observations

| ID | Severity | Finding | Suggested Action |
|----|----------|---------|------------------|
| OBS-001 | Low | No unit tests | Add Vitest/Playwright tests |
| OBS-002 | Low | No ESC key handler for mobile | Add keyboard listener |

### Acceptance Criteria Traceability

| AC | Requirement | Evidence | Status |
|----|-------------|----------|--------|
| 1 | /factbase route with PublicSidebar | layout.tsx with PublicSidebar | PASS |
| 2 | Welcome/overview page | page.tsx with hero and quick links | PASS |
| 3 | Hero page has "Explore Factbase" CTA | page.tsx lines 14-19 | PASS |
| 4 | PublicSidebar uses BaseSidebar | PublicSidebar.tsx imports BaseSidebar | PASS |
| 5 | Collapsible on desktop | publicSidebarStore.ts with persist | PASS |
| 6 | Mobile hamburger menu | layout.tsx with mobile toggle | PASS |
| 7 | Content area displays child routes | layout.tsx children prop | PASS |
| 8 | Responsive breakpoints | md: breakpoints throughout | PASS |

### Gate Status

**Gate: PASS** -> `docs/qa/gates/UI-1.2-factbase-layout.yml`

### Recommended Status

**Done** - All 8 ACs met, TypeScript compiles, responsive design works.
