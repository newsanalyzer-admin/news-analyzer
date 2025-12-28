# Story UI-2.3: EntityDetail Pattern Component

## Status

**Done**

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

- [x] Define EntityDetailConfig interface (AC: 1, 8)
  - [x] Extend `frontend/src/lib/config/entityTypes.ts`
  - [x] Define DetailSectionConfig interface (id, label, fields, layout)
  - [x] Define FieldConfig interface (id, label, render, sourceField)
  - [x] Define RelatedEntityConfig interface (type, field, label)
  - [x] Export TypeScript types

- [x] Create EntityDetail component (AC: 1, 9, 10)
  - [x] Create `frontend/src/components/knowledge-base/EntityDetail.tsx`
  - [x] Accept entity data and config as props
  - [x] Implement responsive layout (grid on desktop, stack on mobile)
  - [x] Add back button with preserved navigation state
  - [x] Handle loading and error states (AC: 7)

- [x] Create EntityDetailHeader subcomponent (AC: 2)
  - [x] Create `frontend/src/components/knowledge-base/EntityDetailHeader.tsx`
  - [x] Display entity name prominently
  - [x] Display entity type badge
  - [x] Display key metadata (configurable per entity type)
  - [x] Consistent styling across entity types

- [x] Create DetailSection subcomponent (AC: 3)
  - [x] Create `frontend/src/components/knowledge-base/DetailSection.tsx`
  - [x] Accept section config and data
  - [x] Render fields based on configuration
  - [x] Support different layouts (list, grid, key-value pairs)
  - [x] Collapsible sections (optional)

- [x] Create SourceCitation component (AC: 4, 5)
  - [x] Create `frontend/src/components/knowledge-base/SourceCitation.tsx`
  - [x] Small icon button (info or external-link icon)
  - [x] Expandable popover/tooltip on click
  - [x] Display: source name, URL (clickable), retrieval date
  - [x] Accessible: keyboard operable, proper ARIA

- [x] Create RelatedEntities subcomponent (AC: 6)
  - [x] Create `frontend/src/components/knowledge-base/RelatedEntities.tsx`
  - [x] Accept list of related entities and their type
  - [x] Render as clickable links/chips
  - [x] Navigate to `/knowledge-base/:type/:id` on click
  - [x] Handle empty state gracefully

- [x] Integrate into route structure (AC: 10)
  - [x] Create `frontend/src/app/knowledge-base/[entityType]/[id]/page.tsx`
  - [x] Fetch entity data based on type and id
  - [x] Pass data and config to EntityDetail component
  - [x] Handle 404 for invalid entity id

- [x] Create barrel export (AC: 8)
  - [x] Update `frontend/src/components/knowledge-base/index.ts`
  - [x] Export EntityDetail and all subcomponents

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
| 2025-12-27 | 1.1 | Validation: reviewed existing OrgDetailPanel and JudgeDetailPanel patterns, status → Approved | James (Dev) |
| 2025-12-27 | 1.2 | Implementation complete: EntityDetail pattern with all subcomponents, organization detail config, detail page route | Dev Agent |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- TypeScript compilation passed after fixing ReactNode type issue in EntityDetailHeader.tsx

### Completion Notes List
1. Extended entityTypes.ts with comprehensive EntityDetail configuration types:
   - SourceInfo for citation metadata
   - DetailFieldConfig with dot-notation path support and custom render functions
   - DetailSectionConfig with list/grid/key-value layouts and collapsible support
   - RelatedEntityConfig for cross-entity navigation
   - DetailHeaderConfig with badge rendering support
   - EntityDetailConfig combining all above

2. Created EntityDetail component with:
   - Loading skeleton with responsive layout
   - Error state with retry button
   - Not found state for missing entities
   - Responsive 3-column grid (2 for content, 1 for sidebar)

3. Created EntityDetailHeader with:
   - Back navigation button (router.back() or custom backUrl)
   - Title and subtitle from configured fields
   - Custom badge rendering support
   - Meta fields display with automatic date formatting

4. Created DetailSection with:
   - Three layout modes: list, grid, key-value
   - Collapsible sections with ChevronDown/ChevronRight icons
   - hideIfEmpty field support
   - SourceCitation integration for sourced fields
   - getNestedValue helper for dot-notation field access

5. Created SourceCitation with Radix UI Popover:
   - Info icon button
   - Popover displays source name, URL (clickable), retrieval date, data source
   - Keyboard accessible

6. Created RelatedEntities with:
   - Badge-style clickable links
   - Navigation to /knowledge-base/{entityType}/{id}
   - Handles arrays and single objects
   - Empty state handling

7. Created detail page route with:
   - Dynamic [entityType]/[id] routing
   - useGovernmentOrg hook for data fetching
   - Invalid ID and missing config error states
   - Placeholder for future entity types

8. Added useGovernmentOrg hook to useGovernmentOrgs.ts:
   - Query key with detail ID
   - 404 error handling

9. Created organization detail configuration with:
   - Overview section (description, mission, website)
   - Organization Details grid (type, level, status, branch)
   - Timeline section (collapsible, established/dissolved dates)
   - Jurisdiction section (collapsible, default collapsed)

### File List
**New Files:**
- `frontend/src/components/ui/popover.tsx` - Shadcn-style Popover component using @radix-ui/react-popover
- `frontend/src/components/knowledge-base/SourceCitation.tsx` - Expandable source citation popover
- `frontend/src/components/knowledge-base/DetailSection.tsx` - Configurable section renderer
- `frontend/src/components/knowledge-base/EntityDetailHeader.tsx` - Header with back button, title, badges
- `frontend/src/components/knowledge-base/RelatedEntities.tsx` - Related entity links
- `frontend/src/components/knowledge-base/EntityDetail.tsx` - Main detail component
- `frontend/src/app/knowledge-base/[entityType]/[id]/page.tsx` - Detail page route

**Modified Files:**
- `frontend/src/lib/config/entityTypes.ts` - Extended with EntityDetail configuration types and organization detail config
- `frontend/src/components/knowledge-base/index.ts` - Added exports for new components
- `frontend/src/hooks/useGovernmentOrgs.ts` - Added useGovernmentOrg hook for fetching single organization

---

## QA Results
_To be filled by QA Agent_
