# Story UI-1.1: Shared Sidebar Components

## Status

**Complete**

---

## Story

**As a** developer,
**I want** shared sidebar components extracted from the admin implementation,
**so that** both admin and public sidebars can reuse the same base logic without code duplication.

---

## Acceptance Criteria

1. `SidebarMenuItem.tsx` is moved from `components/admin/` to `components/sidebar/`
2. A new `BaseSidebar.tsx` wrapper component is created in `components/sidebar/`
3. `MenuItemData` interface is exported from `components/sidebar/types.ts`
4. `AdminSidebar.tsx` is refactored to use the shared components
5. All existing admin sidebar functionality continues to work after refactoring
6. Shared components support max 3 levels of nesting
7. Shared components have full TypeScript types
8. Keyboard navigation (Arrow keys, Enter, Space) works correctly

---

## Tasks / Subtasks

- [ ] Create shared sidebar directory structure (AC: 1, 2, 3)
  - [ ] Create `frontend/src/components/sidebar/` directory
  - [ ] Create `frontend/src/components/sidebar/types.ts` with `MenuItemData` interface
  - [ ] Move `SidebarMenuItem.tsx` from `admin/` to `sidebar/`
  - [ ] Update imports in moved file

- [ ] Create BaseSidebar wrapper component (AC: 2, 6)
  - [ ] Create `frontend/src/components/sidebar/BaseSidebar.tsx`
  - [ ] Accept `menuItems`, `isCollapsed`, `header`, `footer` as props
  - [ ] Support collapsible behavior with width transition
  - [ ] Support mobile responsive behavior
  - [ ] Ensure max 3 levels nesting is enforced

- [ ] Refactor AdminSidebar to use shared components (AC: 4, 5)
  - [ ] Update `AdminSidebar.tsx` imports to use `sidebar/SidebarMenuItem`
  - [ ] Update `AdminSidebar.tsx` to optionally use `BaseSidebar` or keep current structure
  - [ ] Verify all admin navigation still works

- [ ] Add TypeScript types and exports (AC: 7)
  - [ ] Export all types from `sidebar/types.ts`
  - [ ] Create `sidebar/index.ts` barrel export
  - [ ] Ensure strict TypeScript compliance

- [ ] Verify keyboard navigation (AC: 8)
  - [ ] Test Arrow Right expands collapsed menu
  - [ ] Test Arrow Left collapses expanded menu
  - [ ] Test Enter/Space toggles menu items
  - [ ] Test Tab navigation through items

---

## Dev Notes

### Relevant Source Files

```
frontend/src/
├── components/
│   └── admin/
│       ├── AdminSidebar.tsx          # Current sidebar (139 lines)
│       └── SidebarMenuItem.tsx       # To be moved (194 lines, already generic)
├── stores/
│   └── sidebarStore.ts               # Zustand store for sidebar state
└── lib/
    └── utils/
        └── cn.ts                     # Tailwind class merge utility
```

### Key Implementation Details

**SidebarMenuItem.tsx** is already generic and supports:
- Recursive rendering for nested menus
- `MenuItemData` interface with `label`, `href?`, `icon?`, `children?`
- Active state detection via `usePathname()`
- Keyboard navigation (Arrow keys, Enter, Space)
- Collapsed state with tooltips
- Depth-based padding calculation

**BaseSidebar.tsx** should provide:
```typescript
interface BaseSidebarProps {
  menuItems: MenuItemData[];
  isCollapsed: boolean;
  onToggle: () => void;
  header?: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
}
```

### Architecture Reference

From `docs/architecture/architecture.md`:
- Frontend uses Next.js 14 App Router
- State management: Zustand with localStorage persistence
- UI components: Shadcn/UI + Tailwind CSS
- Icons: Lucide React

---

## Testing

### Test File Location
`frontend/src/components/sidebar/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Test keyboard navigation accessibility
- Test collapsed/expanded state transitions
- Test active state highlighting

### Test Cases
1. SidebarMenuItem renders correctly at each nesting level (0, 1, 2)
2. Clicking parent item expands/collapses children
3. Arrow keys navigate and expand/collapse correctly
4. Active state is applied to current route item
5. Collapsed mode shows only icons with tooltips
6. Max 3 levels enforced (level 3 items render, level 4+ ignored)

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
1. Created `frontend/src/components/sidebar/` directory
2. Created `types.ts` with MenuItemData, BaseSidebarProps, SidebarMenuItemProps interfaces
3. Created `SidebarMenuItem.tsx` with max depth enforcement (3 levels)
4. Created `BaseSidebar.tsx` wrapper component
5. Created `index.ts` barrel export
6. Refactored AdminSidebar to use shared BaseSidebar component
7. Updated old SidebarMenuItem.tsx to re-export from shared location for backwards compatibility

### File List
- `frontend/src/components/sidebar/types.ts` - Created
- `frontend/src/components/sidebar/SidebarMenuItem.tsx` - Created (moved from admin/)
- `frontend/src/components/sidebar/BaseSidebar.tsx` - Created
- `frontend/src/components/sidebar/index.ts` - Created
- `frontend/src/components/admin/AdminSidebar.tsx` - Modified (uses shared components)
- `frontend/src/components/admin/SidebarMenuItem.tsx` - Modified (re-exports from shared)

---

## QA Results
*To be filled after QA review*
