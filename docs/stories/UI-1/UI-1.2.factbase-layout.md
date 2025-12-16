# Story UI-1.2: Factbase Layout & Landing Update

## Status

**Ready**

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

- [ ] Create Factbase route structure (AC: 1, 7)
  - [ ] Create `frontend/src/app/factbase/` directory
  - [ ] Create `frontend/src/app/factbase/layout.tsx` with sidebar + content area
  - [ ] Create `frontend/src/app/factbase/page.tsx` as welcome/overview page

- [ ] Create PublicSidebar component (AC: 4, 5, 6)
  - [ ] Create `frontend/src/components/public/PublicSidebar.tsx`
  - [ ] Import and use `BaseSidebar` from `components/sidebar/`
  - [ ] Create `frontend/src/stores/publicSidebarStore.ts` (separate from admin)
  - [ ] Implement desktop collapse toggle
  - [ ] Implement mobile hamburger menu with backdrop

- [ ] Create Factbase welcome page (AC: 2)
  - [ ] Design welcome content explaining the Factbase
  - [ ] Include quick links to People and Organizations sections
  - [ ] Add statistics cards (total members, appointees, orgs) if data available

- [ ] Update hero landing page (AC: 3)
  - [ ] Add "Explore Factbase" button/CTA to `frontend/src/app/page.tsx`
  - [ ] Link CTA to `/factbase`
  - [ ] Style CTA prominently (primary button style)

- [ ] Implement responsive layout (AC: 8)
  - [ ] Desktop: Sidebar visible, collapsible (w-64 expanded, w-16 collapsed)
  - [ ] Tablet: Sidebar collapsed by default, expandable
  - [ ] Mobile: Sidebar hidden, hamburger menu to open overlay

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
