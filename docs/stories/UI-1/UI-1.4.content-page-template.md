# Story UI-1.4: Content Page Template

## Status

**Ready**

---

## Story

**As a** user,
**I want** each factbase page to have a consistent header with title and educational description,
**so that** I can understand what information is available on each page.

---

## Acceptance Criteria

1. `ContentPageHeader` component is created in `components/public/`
2. Component displays page title prominently
3. Component displays educational description below title
4. Component optionally displays breadcrumb navigation
5. Component is reusable across all factbase content pages
6. Descriptions are stored in a central configuration file
7. Component has TypeScript props interface
8. Styling is consistent with existing design system (Shadcn/Tailwind)

---

## Tasks / Subtasks

- [ ] Create ContentPageHeader component (AC: 1, 2, 3, 7, 8)
  - [ ] Create `frontend/src/components/public/ContentPageHeader.tsx`
  - [ ] Define props interface: `title`, `description`, `breadcrumbs?`
  - [ ] Style title as prominent heading (h1, text-3xl)
  - [ ] Style description as muted text block with good readability
  - [ ] Use Tailwind classes consistent with existing components

- [ ] Add breadcrumb support (AC: 4)
  - [ ] Accept optional `breadcrumbs` prop as array of `{ label, href }`
  - [ ] Render breadcrumb trail if provided
  - [ ] Style breadcrumbs with separator and active state

- [ ] Create page descriptions configuration (AC: 6)
  - [ ] Create `frontend/src/lib/page-descriptions.ts`
  - [ ] Export descriptions object keyed by route path
  - [ ] Include all descriptions from epic document

- [ ] Ensure reusability (AC: 5)
  - [ ] Component should work standalone (no context dependencies)
  - [ ] Export from `components/public/index.ts`

---

## Dev Notes

### Component Interface

```typescript
// ContentPageHeader.tsx

interface BreadcrumbItem {
  label: string;
  href?: string; // Optional - last item typically has no href
}

interface ContentPageHeaderProps {
  title: string;
  description: string;
  breadcrumbs?: BreadcrumbItem[];
}

export function ContentPageHeader({ title, description, breadcrumbs }: ContentPageHeaderProps) {
  return (
    <div className="mb-8">
      {breadcrumbs && (
        <nav className="mb-4 text-sm text-muted-foreground">
          {/* Breadcrumb rendering */}
        </nav>
      )}
      <h1 className="text-3xl font-bold tracking-tight mb-4">{title}</h1>
      <p className="text-muted-foreground text-lg leading-relaxed max-w-3xl">
        {description}
      </p>
    </div>
  );
}
```

### Page Descriptions Configuration

```typescript
// frontend/src/lib/page-descriptions.ts

export const pageDescriptions: Record<string, { title: string; description: string }> = {
  '/factbase/people/congressional-members': {
    title: 'Congressional Members',
    description: 'Congressional Members are the 535 elected officials who serve in the United States Congress—100 Senators (2 per state) and 435 Representatives (apportioned by population). Congress is the legislative branch of government, responsible for writing and passing federal laws, controlling the federal budget, and providing oversight of the executive branch. Members serve terms of 6 years (Senate) or 2 years (House).',
  },
  '/factbase/people/executive-appointees': {
    title: 'Executive Appointees',
    description: 'Executive Appointees are individuals appointed by the President to serve in leadership positions across the executive branch. This includes Cabinet secretaries, agency heads, ambassadors, and thousands of other positions that help run the federal government. Some positions require Senate confirmation (PAS - Presidential Appointment with Senate Confirmation), while others do not. The Plum Book, published after each presidential election, catalogs these positions.',
  },
  // ... additional descriptions from epic
};
```

### Usage Example

```tsx
// In a factbase page component
import { ContentPageHeader } from '@/components/public';
import { pageDescriptions } from '@/lib/page-descriptions';

export default function CongressionalMembersPage() {
  const { title, description } = pageDescriptions['/factbase/people/congressional-members'];

  return (
    <div className="container py-8">
      <ContentPageHeader
        title={title}
        description={description}
        breadcrumbs={[
          { label: 'Factbase', href: '/factbase' },
          { label: 'People', href: '/factbase/people' },
          { label: 'Congressional Members' },
        ]}
      />
      {/* Page content */}
    </div>
  );
}
```

### All Descriptions (from Epic)

Include these in `page-descriptions.ts`:
- People > Current Government Officials
- People > Congressional Members
- People > Executive Appointees
- People > Federal Judges & Justices
- Organizations > Federal Government
- Organizations > Executive Branch
- Organizations > Legislative Branch
- Organizations > Judicial Branch

---

## Testing

### Test File Location
`frontend/src/components/public/__tests__/ContentPageHeader.test.tsx`

### Testing Standards
- Use Vitest + React Testing Library
- Test accessibility (heading levels, landmarks)

### Test Cases
1. Renders title as h1 element
2. Renders description text
3. Renders breadcrumbs when provided
4. Does not render breadcrumbs when not provided
5. Breadcrumb links are clickable (except last item)
6. Component matches snapshot

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
