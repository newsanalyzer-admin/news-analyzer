# Story UI-5.1: Reorganize KB Sidebar Navigation

## Status

**Ready for Review**

## Story

**As a** user browsing the Knowledge Base,
**I want** the sidebar to show "U.S. Federal Government" with organized submenus,
**So that** I can understand the navigation structure and find U.S. Code content.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | "Government" is renamed to "U.S. Federal Government" in sidebar |
| AC2 | "Branches" appears as a non-clickable grouping label under U.S. Federal Government |
| AC3 | Executive Branch, Legislative Branch, Judicial Branch are nested under "Branches" |
| AC4 | "U.S. Code (Federal Laws)" appears as a sibling to "Branches" under U.S. Federal Government |
| AC5 | Existing routes (`/knowledge-base/government/*`) continue to work |
| AC6 | Breadcrumbs update to show new hierarchy labels |
| AC7 | In collapsed sidebar, U.S. Federal Government shows tooltip with full path |

## Tasks / Subtasks

- [x] **Task 1: Update menu configuration** (AC: 1, 2, 3, 4)
  - [x] Open `frontend/src/lib/menu-config.ts`
  - [x] Rename `label: 'Government'` to `label: 'U.S. Federal Government'`
  - [x] Create new `Branches` grouping object with `children` containing the 3 branch items
  - [x] Add `U.S. Code (Federal Laws)` menu item with `href: '/knowledge-base/government/us-code'`
  - [x] Verify `Branches` has no `href` property (makes it non-clickable)

- [x] **Task 2: Update breadcrumb label mappings** (AC: 6)
  - [x] Open `frontend/src/components/knowledge-base/KBBreadcrumbs.tsx`
  - [x] Update label mapping for `government` segment to show "U.S. Federal Government"
  - [x] Add mapping for `us-code` segment to show "U.S. Code"

- [x] **Task 3: Verify BaseSidebar supports non-clickable groups** (AC: 2)
  - [x] Review `frontend/src/components/sidebar/SidebarMenuItem.tsx`
  - [x] Confirm items without `href` render as non-clickable labels with expand/collapse
  - [x] If not supported, modify component to handle this case

- [x] **Task 4: Verify existing routes still work** (AC: 5)
  - [x] Manually test `/knowledge-base/government`
  - [x] Manually test `/knowledge-base/government/executive`
  - [x] Manually test `/knowledge-base/government/legislative`
  - [x] Manually test `/knowledge-base/government/judicial`

- [x] **Task 5: Update tests** (AC: 1-6)
  - [x] Update `frontend/src/lib/__tests__/menu-config.test.ts` for new structure
  - [x] Update breadcrumb tests if label mappings changed
  - [x] Add test for non-clickable "Branches" grouping behavior

## Dev Notes

### Technical Verification (Architect)

**CONFIRMED:** `SidebarMenuItem.tsx` (lines 155-186) already handles items without `href`:
- Items with `children` but no `href` render as `<div role="button">` with expand/collapse
- No component modifications required for "Branches" grouping
- Tooltip behavior for collapsed state is already implemented (line 165)

### Current Menu Structure (menu-config.ts lines 24-50)

```typescript
export const publicMenuConfig: MenuItemData[] = [
  {
    label: 'Knowledge Base',
    icon: Database,
    children: [
      {
        label: 'Government',  // ← rename to 'U.S. Federal Government'
        icon: Building,
        href: '/knowledge-base/government',
        children: [
          {
            label: 'Executive Branch',
            icon: Building,
            href: '/knowledge-base/government/executive',
          },
          {
            label: 'Legislative Branch',
            icon: Landmark,
            href: '/knowledge-base/government/legislative',
          },
          {
            label: 'Judicial Branch',
            icon: Scale,
            href: '/knowledge-base/government/judicial',
          },
        ],
      },
      // ... People, Committees, Organizations
    ],
  },
];
```

### Target Menu Structure

```typescript
{
  label: 'U.S. Federal Government',  // renamed
  icon: Building,
  href: '/knowledge-base/government',
  children: [
    {
      label: 'Branches',
      icon: GitBranch,  // import from lucide-react
      // NO href - non-clickable grouping
      children: [
        {
          label: 'Executive Branch',
          icon: Building,
          href: '/knowledge-base/government/executive',
        },
        {
          label: 'Legislative Branch',
          icon: Landmark,
          href: '/knowledge-base/government/legislative',
        },
        {
          label: 'Judicial Branch',
          icon: Scale,
          href: '/knowledge-base/government/judicial',
        },
      ],
    },
    {
      label: 'U.S. Code (Federal Laws)',
      icon: BookOpen,  // import from lucide-react
      href: '/knowledge-base/government/us-code',
    },
  ],
}
```

### SidebarMenuItem Behavior

The `SidebarMenuItem` component in `frontend/src/components/sidebar/SidebarMenuItem.tsx` should handle items without `href`:
- If `item.href` exists → render as `<Link>`
- If `item.href` is undefined but has `children` → render as expandable label (button)
- If both undefined → render as static label

Review the component to confirm this behavior exists.

### Breadcrumb Label Mapping

In `KBBreadcrumbs.tsx`, there's likely a mapping function. Update to include:

```typescript
const labelMap: Record<string, string> = {
  'knowledge-base': 'Knowledge Base',
  'government': 'U.S. Federal Government',  // updated
  'executive': 'Executive Branch',
  'legislative': 'Legislative Branch',
  'judicial': 'Judicial Branch',
  'us-code': 'U.S. Code',  // new
  'people': 'People',
  'committees': 'Committees',
  'organizations': 'Organizations',
};
```

### Lucide Icons to Import

```typescript
import { GitBranch, BookOpen } from 'lucide-react';
```

- `GitBranch` - for "Branches" grouping
- `BookOpen` - for "U.S. Code (Federal Laws)"

## Testing

### Test File Locations

- `frontend/src/lib/__tests__/menu-config.test.ts`
- `frontend/src/components/knowledge-base/__tests__/KBBreadcrumbs.test.tsx`
- `frontend/src/components/sidebar/__tests__/SidebarMenuItem.test.tsx` (if exists)

### Test Cases

1. **Menu Config Tests**
   - U.S. Federal Government label exists
   - Branches grouping exists without href
   - Three branch items nested under Branches
   - U.S. Code item exists with correct href

2. **Breadcrumb Tests**
   - `/knowledge-base/government` shows "Knowledge Base > U.S. Federal Government"
   - `/knowledge-base/government/executive` shows correct breadcrumb trail
   - `/knowledge-base/government/us-code` shows "Knowledge Base > U.S. Federal Government > U.S. Code"

3. **SidebarMenuItem Tests**
   - Item without href renders as non-clickable label
   - Item with children but no href expands/collapses
   - Item with href renders as link

4. **Collapsed Sidebar Tests** (AC7)
   - U.S. Federal Government item shows in collapsed view
   - Tooltip shows full path on hover
   - Nested items hidden when collapsed

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-02 | 1.0 | Initial story creation | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

None - no issues encountered

### Completion Notes List

1. Updated `menu-config.ts` to rename "Government" → "U.S. Federal Government" and restructure with nested "Branches" grouping
2. Added `GitBranch` and `BookOpen` icons from lucide-react
3. Added "U.S. Code (Federal Laws)" menu item with href `/knowledge-base/government/us-code`
4. Updated `KBBreadcrumbs.tsx` to add `us-code` segment mapping (government mapping already existed)
5. Verified `SidebarMenuItem.tsx` already supports non-clickable groups (no changes needed)
6. Updated existing `menu-config.test.ts` for new structure (20 tests passing)
7. Added test for U.S. Code breadcrumbs in `KBBreadcrumbs.test.tsx` (21 tests passing)
8. SidebarMenuItem tests already cover non-clickable group behavior (30 tests passing)

### File List

**Modified:**
- `frontend/src/lib/menu-config.ts` - Sidebar menu restructure
- `frontend/src/components/knowledge-base/KBBreadcrumbs.tsx` - Added us-code segment label
- `frontend/src/lib/__tests__/menu-config.test.ts` - Updated for new structure
- `frontend/src/components/knowledge-base/__tests__/KBBreadcrumbs.test.tsx` - Added U.S. Code test

---

## QA Results

_To be filled by QA agent_
