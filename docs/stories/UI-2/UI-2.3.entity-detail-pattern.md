# Story UI-2.3: EntityDetail Pattern Component

## Status

**Draft**

---

## Story

**As a** developer building Knowledge Base views,
**I want** a reusable EntityDetail component that renders any entity's details based on configuration,
**so that** I can display consistent detail pages across entity types with expandable source citations.

---

## Acceptance Criteria

1. EntityDetail accepts an `EntityDetailConfig` and renders appropriate sections
2. Header displays entity name, type badge, and key metadata consistently
3. Sections render dynamically based on configuration (attributes, relationships, etc.)
4. Source citations display as expandable icons next to facts (per design decision)
5. Clicking source icon reveals source details (name, URL, date retrieved)
6. Related entities display as clickable links to their detail pages
7. Component handles loading and error states gracefully
8. Component is fully typed with TypeScript
9. Responsive layout: single column on mobile, multi-column on desktop
10. Back navigation returns to EntityBrowser with preserved filters/page

---

## Tasks / Subtasks

- [ ] Define EntityDetailConfig interface (AC: 1, 8)
  - [ ] Extend `frontend/src/lib/config/entityTypes.ts`
  - [ ] Define DetailSectionConfig interface (id, label, fields, layout)
  - [ ] Define FieldConfig interface (id, label, render, sourceField)
  - [ ] Define RelatedEntityConfig interface (type, field, label)
  - [ ] Export TypeScript types

- [ ] Create EntityDetail component (AC: 1, 9, 10)
  - [ ] Create `frontend/src/components/knowledge-base/EntityDetail.tsx`
  - [ ] Accept entity data and config as props
  - [ ] Implement responsive layout (grid on desktop, stack on mobile)
  - [ ] Add back button with preserved navigation state
  - [ ] Handle loading and error states (AC: 7)

- [ ] Create EntityDetailHeader subcomponent (AC: 2)
  - [ ] Create `frontend/src/components/knowledge-base/EntityDetailHeader.tsx`
  - [ ] Display entity name prominently
  - [ ] Display entity type badge
  - [ ] Display key metadata (configurable per entity type)
  - [ ] Consistent styling across entity types

- [ ] Create DetailSection subcomponent (AC: 3)
  - [ ] Create `frontend/src/components/knowledge-base/DetailSection.tsx`
  - [ ] Accept section config and data
  - [ ] Render fields based on configuration
  - [ ] Support different layouts (list, grid, key-value pairs)
  - [ ] Collapsible sections (optional)

- [ ] Create SourceCitation component (AC: 4, 5)
  - [ ] Create `frontend/src/components/knowledge-base/SourceCitation.tsx`
  - [ ] Small icon button (info or external-link icon)
  - [ ] Expandable popover/tooltip on click
  - [ ] Display: source name, URL (clickable), retrieval date
  - [ ] Accessible: keyboard operable, proper ARIA

- [ ] Create RelatedEntities subcomponent (AC: 6)
  - [ ] Create `frontend/src/components/knowledge-base/RelatedEntities.tsx`
  - [ ] Accept list of related entities and their type
  - [ ] Render as clickable links/chips
  - [ ] Navigate to `/knowledge-base/:type/:id` on click
  - [ ] Handle empty state gracefully

- [ ] Integrate into route structure (AC: 10)
  - [ ] Create `frontend/src/app/knowledge-base/[entityType]/[id]/page.tsx`
  - [ ] Fetch entity data based on type and id
  - [ ] Pass data and config to EntityDetail component
  - [ ] Handle 404 for invalid entity id

- [ ] Create barrel export (AC: 8)
  - [ ] Update `frontend/src/components/knowledge-base/index.ts`
  - [ ] Export EntityDetail and all subcomponents

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── app/
│   └── knowledge-base/
│       └── [entityType]/
│           ├── page.tsx                  # EntityBrowser (from UI-2.2)
│           └── [id]/
│               └── page.tsx              # NEW - EntityDetail page
├── components/
│   ├── knowledge-base/
│   │   ├── EntityDetail.tsx              # NEW - main component
│   │   ├── EntityDetailHeader.tsx        # NEW - header section
│   │   ├── DetailSection.tsx             # NEW - configurable section
│   │   ├── SourceCitation.tsx            # NEW - expandable source
│   │   ├── RelatedEntities.tsx           # NEW - linked entities
│   │   ├── EntityBrowser.tsx             # From UI-2.2
│   │   └── index.ts                      # Barrel export
│   └── ui/
│       ├── popover.tsx                   # Shadcn popover for source citation
│       ├── badge.tsx                     # For entity type badge
│       └── card.tsx                      # For sections
├── lib/
│   └── config/
│       └── entityTypes.ts                # Extended with detail config
```

### Key Implementation Details

**EntityDetailConfig Structure:**
```typescript
interface EntityDetailConfig {
  header: {
    titleField: string;                  // Field to use as title
    subtitleField?: string;              // Optional subtitle
    badgeField?: string;                 // Field for type badge
    metaFields: string[];                // Key metadata to show in header
  };
  sections: DetailSectionConfig[];
  relatedEntities?: RelatedEntityConfig[];
}

interface DetailSectionConfig {
  id: string;
  label: string;
  fields: FieldConfig[];
  layout: 'list' | 'grid' | 'key-value';
  collapsible?: boolean;
  defaultCollapsed?: boolean;
}

interface FieldConfig {
  id: string;                            // Field path in data (supports dot notation)
  label: string;
  render?: (value: any, entity: any) => React.ReactNode;
  sourceField?: string;                  // Field containing source info
  hideIfEmpty?: boolean;
}

interface RelatedEntityConfig {
  entityType: string;                    // 'organizations', 'people'
  field: string;                         // Field containing related entity ids
  label: string;                         // Section label
  displayField: string;                  // Field to show as link text
}
```

**SourceCitation Props:**
```typescript
interface SourceCitationProps {
  source: {
    name: string;                        // "Federal Judicial Center"
    url?: string;                        // "https://fjc.gov/..."
    retrievedAt?: string;                // ISO date string
    dataSource?: string;                 // "FJC", "CONGRESS_GOV", etc.
  };
}
```

**Example Usage:**
```tsx
// In /knowledge-base/organizations/[id]/page.tsx
const orgDetailConfig: EntityDetailConfig = {
  header: {
    titleField: 'officialName',
    subtitleField: 'acronym',
    badgeField: 'branch',
    metaFields: ['establishedDate', 'parentOrganization'],
  },
  sections: [
    {
      id: 'overview',
      label: 'Overview',
      layout: 'key-value',
      fields: [
        { id: 'description', label: 'Description' },
        { id: 'website', label: 'Website', render: (v) => <a href={v}>{v}</a> },
        { id: 'jurisdiction', label: 'Jurisdiction', sourceField: 'sources.jurisdiction' },
      ],
    },
  ],
  relatedEntities: [
    { entityType: 'organizations', field: 'childOrganizations', label: 'Sub-organizations', displayField: 'officialName' },
    { entityType: 'people', field: 'currentOfficials', label: 'Current Officials', displayField: 'fullName' },
  ],
};
```

**Existing Detail Panels to Reference:**
- `factbase/organizations/OrgDetailPanel.tsx` - current org detail implementation
- `factbase/people/federal-judges/JudgeDetailPanel.tsx` - judge detail implementation

**Back Navigation:**
- Use `useRouter()` and `useSearchParams()` to preserve list state
- Store scroll position in sessionStorage if needed
- Back button: `router.back()` or explicit link to list with params

### Architecture Reference

- UI Components: Shadcn/UI (Card, Badge, Popover, Button)
- Styling: Tailwind CSS
- Icons: Lucide React (Info, ExternalLink, ChevronLeft for back)
- Data: Props-driven, fetched by parent page

---

## Testing

### Test File Location
`frontend/src/components/knowledge-base/__tests__/EntityDetail.test.tsx`

### Testing Standards
- Use Vitest + React Testing Library
- Mock entity data for various types
- Test expandable source citation behavior
- Test accessibility

### Test Cases

1. **EntityDetail Rendering**
   - Renders header with entity name and type badge
   - Renders all configured sections
   - Renders loading skeleton when loading
   - Renders error state when error prop provided
   - Renders 404 message for missing entity

2. **EntityDetailHeader**
   - Displays title from configured field
   - Displays subtitle when configured
   - Displays type badge with correct styling
   - Displays meta fields in header area

3. **DetailSection**
   - Renders section label
   - Renders all configured fields
   - Respects layout prop (list, grid, key-value)
   - Collapsible sections toggle correctly
   - Hides empty fields when hideIfEmpty=true

4. **SourceCitation**
   - Renders icon button next to sourced field
   - Click expands popover with source details
   - Source URL is clickable link (opens new tab)
   - Retrieval date formatted correctly
   - Keyboard: Enter/Space opens popover
   - Escape closes popover

5. **RelatedEntities**
   - Renders section with related entity links
   - Links navigate to correct detail URL
   - Handles empty related entities gracefully
   - Displays correct entity name as link text

6. **Navigation**
   - Back button visible and functional
   - Back preserves previous list state (filters, page)
   - Related entity links navigate correctly

7. **Responsive**
   - Single column layout on mobile
   - Multi-column layout on desktop
   - Sections stack appropriately

8. **Accessibility**
   - Proper heading hierarchy (h1, h2, etc.)
   - Source citation popover has ARIA attributes
   - Focus trap in popover when open
   - All interactive elements keyboard accessible

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
