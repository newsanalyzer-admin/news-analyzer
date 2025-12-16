# Story UI-1.3: Menu Configuration System

## Status

**Ready**

---

## Story

**As a** developer,
**I want** a static menu configuration system for the public sidebar,
**so that** menu items can be easily defined and maintained in one place.

---

## Acceptance Criteria

1. Menu configuration is defined in `frontend/src/lib/menu-config.ts`
2. Configuration supports 3-level hierarchy (Factbase → Category → Subcategory → Item)
3. Each menu item can have: label, href, icon, children
4. Public sidebar consumes the menu configuration
5. Menu structure matches the Navigation Structure defined in the epic
6. Configuration is type-safe with TypeScript
7. Icons use Lucide React components

---

## Tasks / Subtasks

- [ ] Create menu configuration file (AC: 1, 2, 3, 6)
  - [ ] Create `frontend/src/lib/menu-config.ts`
  - [ ] Define `PublicMenuConfig` using `MenuItemData[]` type
  - [ ] Export configuration for use in `PublicSidebar`

- [ ] Define menu structure per epic (AC: 5)
  - [ ] Level 1: Factbase (root)
  - [ ] Level 2: People, Organizations
  - [ ] Level 3: Current Government Officials, Federal Government
  - [ ] Leaf items with hrefs: Congressional Members, Executive Appointees, Federal Judges, Executive/Legislative/Judicial Branch

- [ ] Add icons to menu items (AC: 7)
  - [ ] Factbase: `Database` icon
  - [ ] People: `Users` icon
  - [ ] Organizations: `Building2` icon
  - [ ] Government Officials: `UserCheck` icon
  - [ ] Federal Government: `Landmark` icon
  - [ ] Branch items: appropriate icons (Landmark, Scale, Gavel)

- [ ] Integrate with PublicSidebar (AC: 4)
  - [ ] Import `publicMenuConfig` in `PublicSidebar.tsx`
  - [ ] Pass to `BaseSidebar` as `menuItems` prop

---

## Dev Notes

### Menu Structure

```typescript
// frontend/src/lib/menu-config.ts

import {
  Database,
  Users,
  Building2,
  UserCheck,
  Landmark,
  Scale,
  Gavel,
} from 'lucide-react';
import { MenuItemData } from '@/components/sidebar/types';

export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Factbase',
    icon: Database,
    children: [
      {
        label: 'People',
        icon: Users,
        children: [
          {
            label: 'Current Government Officials',
            icon: UserCheck,
            children: [
              { label: 'Congressional Members', href: '/factbase/people/congressional-members' },
              { label: 'Executive Appointees', href: '/factbase/people/executive-appointees' },
              { label: 'Federal Judges & Justices', href: '/factbase/people/federal-judges' },
            ],
          },
        ],
      },
      {
        label: 'Organizations',
        icon: Building2,
        children: [
          {
            label: 'Federal Government',
            icon: Landmark,
            children: [
              { label: 'Executive Branch', href: '/factbase/organizations/executive', icon: Landmark },
              { label: 'Legislative Branch', href: '/factbase/organizations/legislative', icon: Scale },
              { label: 'Judicial Branch', href: '/factbase/organizations/judicial', icon: Gavel },
            ],
          },
        ],
      },
    ],
  },
];
```

### Type Definition Reference

From `components/sidebar/types.ts` (created in UI-1.1):
```typescript
export interface MenuItemData {
  label: string;
  href?: string;
  icon?: React.ComponentType<{ className?: string }>;
  children?: MenuItemData[];
}
```

### Dependencies

- **Requires UI-1.1** (Shared Sidebar Components) for `MenuItemData` type
- **Used by UI-1.2** (Factbase Layout) for `PublicSidebar`

---

## Testing

### Test File Location
`frontend/src/lib/__tests__/menu-config.test.ts`

### Testing Standards
- Use Vitest for unit tests
- Validate menu structure type-safety

### Test Cases
1. Menu config exports a valid `MenuItemData[]` array
2. All leaf items have `href` property
3. No menu item exceeds 3 levels of nesting
4. All icons are valid Lucide React components
5. All hrefs start with `/factbase/`

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
