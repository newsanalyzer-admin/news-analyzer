# Story UI-1.12: Admin Access Link

## Status

**Ready**

---

## Story

**As a** power user,
**I want** a gear icon in the public sidebar footer that links to the admin section,
**so that** I can quickly access administrative functions when needed.

---

## Acceptance Criteria

1. Gear icon (Settings) displayed in public sidebar footer
2. Icon links to `/admin` route
3. Icon has tooltip showing "Admin" on hover
4. Icon styling matches existing sidebar design
5. Icon is visible in both expanded and collapsed sidebar states
6. Click navigates to admin dashboard

---

## Tasks / Subtasks

- [ ] Add gear icon to PublicSidebar footer (AC: 1, 4, 5)
  - [ ] Import `Settings` icon from Lucide React
  - [ ] Add footer section to `PublicSidebar.tsx`
  - [ ] Style icon consistently with sidebar theme
  - [ ] Ensure visibility in collapsed state

- [ ] Implement navigation link (AC: 2, 6)
  - [ ] Wrap icon in Next.js `Link` component
  - [ ] Set `href="/admin"`
  - [ ] Test navigation works correctly

- [ ] Add tooltip (AC: 3)
  - [ ] Add `title="Admin"` attribute for native tooltip
  - [ ] Or use Shadcn Tooltip component for styled tooltip
  - [ ] Tooltip especially important in collapsed state

---

## Dev Notes

### Implementation Location

```
frontend/src/components/public/PublicSidebar.tsx
```

### Code Example

```tsx
import { Settings } from 'lucide-react';
import Link from 'next/link';

// In PublicSidebar component, add footer:
<div className="border-t border-border p-2 shrink-0">
  <Link
    href="/admin"
    className={cn(
      'flex items-center gap-3 w-full rounded-md py-2 px-3 text-sm font-medium',
      'hover:bg-accent hover:text-accent-foreground transition-colors',
      isCollapsed && 'justify-center'
    )}
    title="Admin"
  >
    <Settings className="h-5 w-5" />
    {!isCollapsed && <span>Admin</span>}
  </Link>
</div>
```

### Admin Sidebar Reference

The admin sidebar already has a similar footer pattern (Dashboard link). Use same styling approach.

### Collapsed State

When sidebar is collapsed (w-16), show only the gear icon centered with tooltip.

### Dependencies

- **Requires UI-1.2** (Factbase Layout) which creates `PublicSidebar`
- Trivial story, can be done last

---

## Testing

### Test File Location
`frontend/src/components/public/__tests__/PublicSidebar.test.tsx`

### Test Cases
1. Gear icon renders in sidebar footer
2. Clicking icon navigates to /admin
3. Tooltip shows "Admin" on hover
4. Icon visible in expanded sidebar
5. Icon visible in collapsed sidebar

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
