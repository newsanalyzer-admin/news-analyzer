# Story UI-2.7: Migrate People (Judges, Members, Appointees) to Knowledge Explorer

## Status

**Draft**

---

## Story

**As a** user researching government officials,
**I want** to access Federal Judges, Congressional Members, and Executive Appointees through the unified Knowledge Explorer People section,
**so that** I have a consistent browsing experience with all person types in the knowledge base.

---

## Acceptance Criteria

### General (All Person Types)
1. People entity type configuration with subtype support (judges, members, appointees)
2. `/knowledge-base/people` defaults to first available subtype
3. Subtype selector visible when viewing People section
4. List view uses EntityBrowser pattern component (from UI-2.2)
5. Detail view uses EntityDetail pattern component (from UI-2.3)
6. URL query params work for deep linking (`?type=judges&courtLevel=SUPREME`)

### Federal Judges
7. Federal Judges accessible via `/knowledge-base/people?type=judges`
8. All existing functionality preserved (search, filter by court level, circuit, status)
9. Court Level filter available (Supreme, Appeals, District)
10. Circuit filter available (1st-11th, DC, Federal)
11. Status filter available (Active, Senior)
    - **Note:** Existing UI only has Active/Senior. Verify if DECEASED is valid backend status before adding.
12. Judge stats summary displayed above list (optional enhancement)

### Congressional Members
13. Congressional Members accessible via `/knowledge-base/people?type=members`
14. All existing functionality preserved (search, filter by chamber, party, state)
15. Chamber filter available (House, Senate)
16. Party filter available (Democratic, Republican, Independent)
17. State filter available (all 50 states + territories)

### Executive Appointees
18. Executive Appointees accessible via `/knowledge-base/people?type=appointees`
19. All existing functionality preserved (search, filter by appointment type, agency)
20. Appointment Type filter available (PAS, PA, NA, CA, XS)
21. Agency/Department filter available

### Redirects
22. Old routes redirect correctly:
    - `/factbase/people/federal-judges` → `/knowledge-base/people?type=judges`
    - `/factbase/people/congressional-members` → `/knowledge-base/people?type=members`
    - `/factbase/people/executive-appointees` → `/knowledge-base/people?type=appointees`

---

## Tasks / Subtasks

### Infrastructure (All Person Types)

- [ ] Create People EntityTypeConfig with subtype support (AC: 1, 2, 3)
  - [ ] Add configuration to `frontend/src/lib/config/entityTypes.ts`
  - [ ] Define People type with subtype selector (judges, members, appointees)
  - [ ] Support subtype-specific columns and filters
  - [ ] Set supportedViews: ['list'] (no hierarchy for people)
  - [ ] Implement subtype selector component in header

- [ ] Create People page routes (AC: 4, 5)
  - [ ] Create `frontend/src/app/knowledge-base/people/page.tsx`
  - [ ] Read `type` param to select subtype config
  - [ ] Route to appropriate data fetching hook based on type
  - [ ] Create `frontend/src/app/knowledge-base/people/[id]/page.tsx`
  - [ ] Detect person type from data to select correct detail config

### Federal Judges Migration

- [ ] Create Judges column configuration (AC: 7, 8)
  - [ ] Create `frontend/src/lib/config/peopleConfig.ts`
  - [ ] Define columns: Name, Court, Circuit, Status, Appointing President, Party
  - [ ] Create StatusBadge renderer with color coding
  - [ ] Create PartyBadge renderer (for appointing party)

- [ ] Create Judges filter configuration (AC: 9, 10, 11)
  - [ ] Court Level filter (select: Supreme, Appeals, District)
  - [ ] Circuit filter (select: 1st-11th, DC, Federal)
  - [ ] Status filter (select: Active, Senior)
  - [ ] Search by name
  - [ ] Map to existing API params (courtLevel, circuit, status, search)

- [ ] Create Federal Judges EntityDetail config (AC: 5)
  - [ ] Define detail sections: Court Info, Appointment, Service, Personal, Career
  - [ ] Map existing JudgeDetailPanel fields to config structure
  - [ ] Handle Senate vote display (Ayes/Nays with percentage)
  - [ ] Handle optional fields (birthDate, deathDate, professionalCareer)

- [ ] Integrate Judge Stats component (AC: 12)
  - [ ] Add optional stats slot to EntityBrowser or page layout
  - [ ] Reuse existing JudgeStats component
  - [ ] Display above EntityBrowser when viewing judges
  - [ ] Hide when other person types selected

### Congressional Members Migration

- [ ] Create Members column configuration (AC: 13, 14)
  - [ ] Define columns: Name, Chamber, Party, State, District (House only)
  - [ ] Create ChamberBadge renderer (Senate/House with icons)
  - [ ] Reuse PartyBadge renderer from judges

- [ ] Create Members filter configuration (AC: 15, 16, 17)
  - [ ] Chamber filter (select: House, Senate)
  - [ ] Party filter (select: Democratic, Republican, Independent)
  - [ ] State filter (select: all 50 states + territories)
  - [ ] Search by name
  - [ ] Map to existing API params

- [ ] Create Members EntityDetail config (AC: 5)
  - [ ] Define detail sections: Position, Contact, Biography
  - [ ] Map existing MemberDetailPanel fields to config structure
  - [ ] Handle social media links display
  - [ ] Handle term history display

### Executive Appointees Migration

- [ ] Create Appointees column configuration (AC: 18, 19)
  - [ ] Define columns: Name, Position, Agency, Appointment Type
  - [ ] Create AppointmentTypeBadge renderer (PAS, PA, NA, CA, XS)

- [ ] Create Appointees filter configuration (AC: 20, 21)
  - [ ] Appointment Type filter (select: PAS, PA, NA, CA, XS)
  - [ ] Agency filter (select: populated from API or static list)
  - [ ] Search by name or position
  - [ ] Map to existing API params

- [ ] Create Appointees EntityDetail config (AC: 5)
  - [ ] Define detail sections: Position, Agency, Appointment Details
  - [ ] Map existing AppointeeDetailPanel fields to config structure
  - [ ] Handle vacancy status display

### Redirects and Verification

- [ ] Implement route redirects (AC: 22)
  - [ ] Add redirects to `next.config.js` or middleware
  - [ ] `/factbase/people/federal-judges` → `/knowledge-base/people?type=judges`
  - [ ] `/factbase/people/congressional-members` → `/knowledge-base/people?type=members`
  - [ ] `/factbase/people/executive-appointees` → `/knowledge-base/people?type=appointees`
  - [ ] Preserve any existing query params (page, filters)
  - [ ] Test all redirects return 301 status

- [ ] Verify feature parity - Judges (AC: 8)
  - [ ] Search works (name)
  - [ ] Filter by court level works
  - [ ] Filter by circuit works
  - [ ] Filter by status works
  - [ ] Pagination and sorting work
  - [ ] Click opens detail view

- [ ] Verify feature parity - Members (AC: 14)
  - [ ] Search works (name)
  - [ ] Filter by chamber works
  - [ ] Filter by party works
  - [ ] Filter by state works
  - [ ] Pagination and sorting work
  - [ ] Click opens detail view

- [ ] Verify feature parity - Appointees (AC: 19)
  - [ ] Search works (name, position)
  - [ ] Filter by appointment type works
  - [ ] Filter by agency works
  - [ ] Pagination and sorting work
  - [ ] Click opens detail view

- [ ] Cross-cutting verification
  - [ ] Subtype switching works correctly
  - [ ] Mobile layout responsive for all types
  - [ ] Keyboard navigation functional

---

## Dev Notes

### Relevant Source Tree

```
frontend/src/
├── app/
│   ├── factbase/people/                   # EXISTING - to be deprecated after migration
│   │   ├── page.tsx                       # People landing page
│   │   ├── federal-judges/
│   │   │   ├── page.tsx                   # Judges list page → DELETE
│   │   │   └── JudgeDetailPanel.tsx       # Slide-out detail → DELETE
│   │   ├── congressional-members/
│   │   │   ├── page.tsx                   # Members list page → DELETE
│   │   │   └── MemberDetailPanel.tsx      # Detail panel → DELETE
│   │   └── executive-appointees/
│   │       ├── page.tsx                   # Appointees list page → DELETE
│   │       └── AppointeeDetailPanel.tsx   # Detail panel → DELETE
│   └── knowledge-base/
│       └── people/                        # NEW - migrate here
│           ├── page.tsx                   # EntityBrowser with people config + subtype
│           └── [id]/
│               └── page.tsx               # EntityDetail (auto-detect person type)
├── components/
│   ├── knowledge-base/
│   │   ├── EntityBrowser.tsx              # From UI-2.2
│   │   ├── EntityDetail.tsx               # From UI-2.3
│   │   └── index.ts
│   ├── judicial/                          # EXISTING - may deprecate
│   │   ├── JudgeFilters.tsx               # Reference for filter patterns
│   │   ├── JudgeTable.tsx                 # Reference for column patterns
│   │   └── JudgeStats.tsx                 # KEEP - reused in Knowledge Explorer
│   ├── members/                           # EXISTING - reference for migration
│   │   ├── MemberFilters.tsx              # Reference for filter patterns
│   │   └── MemberTable.tsx                # Reference for column patterns
│   └── appointees/                        # EXISTING - reference for migration
│       └── AppointeeTable.tsx             # Reference for column patterns
├── lib/
│   ├── config/
│   │   ├── entityTypes.ts                 # Entity type registry
│   │   └── peopleConfig.ts                # NEW - all person subtype configs
│   └── api/
│       ├── judges.ts                      # EXISTING - Judges API client
│       ├── members.ts                     # EXISTING - Members API client
│       └── appointees.ts                  # EXISTING - Appointees API client
├── hooks/
│   ├── useJudges.ts                       # EXISTING - Judges hooks
│   ├── useMembers.ts                      # EXISTING - Members hooks
│   └── useAppointees.ts                   # EXISTING - Appointees hooks
└── types/
    ├── judge.ts                           # EXISTING - Judge types & helpers
    ├── member.ts                          # EXISTING - Member types
    └── appointee.ts                       # EXISTING - Appointee types
```

### Key Implementation Details

**People Entity Type Configuration:**
```typescript
// In entityTypes.ts
export const peopleConfig: EntityTypeConfig = {
  id: 'people',
  label: 'People',
  icon: Users,
  apiEndpoint: '/api/judges',  // Default to judges, varies by subtype
  supportedViews: ['list'],
  subtypes: [
    { id: 'judges', label: 'Federal Judges', apiEndpoint: '/api/judges' },
    { id: 'members', label: 'Congressional Members', apiEndpoint: '/api/members' },
    { id: 'appointees', label: 'Executive Appointees', apiEndpoint: '/api/appointees' },
  ],
  defaultSubtype: 'judges',
  // Columns and filters vary by subtype - see subtypeConfigs
};
```

**Judges Subtype Configuration:**
```typescript
// In peopleConfig.ts
export const judgesSubtypeConfig: SubtypeConfig = {
  id: 'judges',
  columns: [
    { id: 'fullName', label: 'Name', sortable: true },
    { id: 'courtName', label: 'Court', sortable: true },
    { id: 'circuit', label: 'Circuit', sortable: true, render: renderCircuit },
    { id: 'judicialStatus', label: 'Status', sortable: true, render: renderStatusBadge },
    { id: 'appointingPresident', label: 'Appointed By', sortable: true, hideOnMobile: true },
    { id: 'partyOfAppointingPresident', label: 'Party', render: renderPartyBadge, hideOnMobile: true },
  ],
  filters: [
    {
      id: 'courtLevel',
      label: 'Court Level',
      type: 'select',
      options: [
        { value: 'SUPREME', label: 'Supreme Court' },
        { value: 'APPEALS', label: 'Court of Appeals' },
        { value: 'DISTRICT', label: 'District Court' },
      ],
      apiParam: 'courtLevel',
    },
    {
      id: 'circuit',
      label: 'Circuit',
      type: 'select',
      options: [
        { value: '1', label: '1st Circuit' },
        { value: '2', label: '2nd Circuit' },
        { value: '3', label: '3rd Circuit' },
        { value: '4', label: '4th Circuit' },
        { value: '5', label: '5th Circuit' },
        { value: '6', label: '6th Circuit' },
        { value: '7', label: '7th Circuit' },
        { value: '8', label: '8th Circuit' },
        { value: '9', label: '9th Circuit' },
        { value: '10', label: '10th Circuit' },
        { value: '11', label: '11th Circuit' },
        { value: 'DC', label: 'D.C. Circuit' },
        { value: 'FEDERAL', label: 'Federal Circuit' },
      ],
      apiParam: 'circuit',
    },
    {
      id: 'status',
      label: 'Status',
      type: 'select',
      options: [
        { value: 'ACTIVE', label: 'Active' },
        { value: 'SENIOR', label: 'Senior Status' },
        // { value: 'DECEASED', label: 'Deceased' },  // Verify backend support before adding
      ],
      apiParam: 'status',
    },
  ],
  defaultSort: { column: 'lastName', direction: 'asc' },
  statsComponent: JudgeStats,  // Optional stats above list
};
```

**Judges Detail Sections Configuration:**
```typescript
export const judgeDetailConfig: EntityDetailConfig = {
  headerFields: ['fullName', 'judicialStatus', 'fjcNid'],
  sections: [
    {
      id: 'court',
      title: 'Court Information',
      fields: [
        { key: 'courtName', label: 'Court' },
        { key: 'courtType', label: 'Court Type' },
        { key: 'circuit', label: 'Circuit', render: formatCircuit },
      ],
    },
    {
      id: 'appointment',
      title: 'Appointment',
      fields: [
        { key: 'appointingPresident', label: 'Appointing President' },
        { key: 'partyOfAppointingPresident', label: 'Party', render: renderPartyBadge },
        { key: 'nominationDate', label: 'Nomination Date', render: formatDate },
        { key: 'confirmationDate', label: 'Confirmation Date', render: formatDate },
        { key: 'commissionDate', label: 'Commission Date', render: formatDate },
        { key: 'senateVote', label: 'Senate Vote', render: renderSenateVote },
        { key: 'abaRating', label: 'ABA Rating' },
      ],
    },
    {
      id: 'service',
      title: 'Service',
      fields: [
        { key: 'seniorStatusDate', label: 'Senior Status Date', render: formatDate },
        { key: 'terminationDate', label: 'Termination Date', render: formatDate },
        { key: 'terminationReason', label: 'Termination Reason' },
      ],
    },
    {
      id: 'personal',
      title: 'Personal Information',
      condition: (judge) => judge.gender || judge.birthDate || judge.deathDate,
      fields: [
        { key: 'gender', label: 'Gender' },
        { key: 'birthDate', label: 'Birth Date', render: formatDate },
        { key: 'deathDate', label: 'Death Date', render: formatDate },
      ],
    },
    {
      id: 'career',
      title: 'Professional Career',
      condition: (judge) => judge.professionalCareer,
      fields: [
        { key: 'professionalCareer', label: null, render: (v) => <pre>{v}</pre> },
      ],
    },
  ],
};
```

**Existing Helper Functions to Reuse:**
```typescript
// From types/judge.ts - can be imported directly
import { getStatusColor, getPartyColor, getCircuitLabel, getCourtLevelLabel } from '@/types/judge';
```

**Existing Hooks (All Verified):**
```typescript
// frontend/src/hooks/useJudges.ts

// List with filters - use for EntityBrowser
useJudges(params: JudgeListParams)  // courtLevel, circuit, status, search, pagination

// Single judge by ID - use for EntityDetail
useJudge(id: string)

// Search by name
useJudgeSearch(query: string)

// Statistics - use for JudgeStats component
useJudgeStats()
```

**Existing API Client (Verified):**
```typescript
// frontend/src/lib/api/judges.ts
interface JudgeListParams {
  page?: number;
  size?: number;
  courtLevel?: CourtLevel | string;
  circuit?: Circuit | string;
  status?: string;
  search?: string;        // Backend param name is 'search' (consistent)
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
```

**Senate Vote Renderer:**
```typescript
function renderSenateVote(judge: Judge): string | null {
  if (judge.ayesCount === null || judge.ayesCount === undefined) return null;
  const nays = judge.naysCount || 0;
  const total = judge.ayesCount + nays;
  const approval = Math.round((judge.ayesCount / total) * 100);
  return `${judge.ayesCount} - ${nays} (${approval}% approval)`;
}
```

**UX Patterns from Existing Implementation:**
```typescript
// 1. Circuit filter conditional display (from JudgeFilters.tsx)
// Only show circuit filter for Appeals/District courts (not Supreme)
const showCircuitFilter = courtLevel === 'APPEALS' || courtLevel === 'DISTRICT' || courtLevel === 'ALL';

// 2. Filter "no selection" value pattern
// Use 'ALL' (not empty string) as the "show all" value
// When 'ALL' is selected, remove the param from URL
if (value === 'ALL' || value === '') {
  params.delete(key);
} else {
  params.set(key, value);
}

// 3. Reset pagination when filters change
params.delete('page');
```

**Detail Route Subtype Handling:**
```typescript
// Option 1 (Recommended): Use UUID - IDs are globally unique
// Route: /knowledge-base/people/[id]
// The useJudge(id) hook fetches by ID regardless of type

// Option 2: Explicit subtype in route
// Route: /knowledge-base/people/judges/[id]
// More explicit but requires separate routes per subtype

// For MVP, use Option 1 since UUIDs are unique across all person types.
// The detail page can detect person type from the returned data if needed.
```

---

### Congressional Members Configuration

**Members Subtype Configuration:**
```typescript
// In peopleConfig.ts
export const membersSubtypeConfig: SubtypeConfig = {
  id: 'members',
  columns: [
    { id: 'fullName', label: 'Name', sortable: true },
    { id: 'chamber', label: 'Chamber', sortable: true, render: renderChamberBadge },
    { id: 'state', label: 'State', sortable: true },
    { id: 'district', label: 'District', sortable: true, hideOnMobile: true },
    { id: 'party', label: 'Party', sortable: true, render: renderPartyBadge },
  ],
  filters: [
    {
      id: 'chamber',
      label: 'Chamber',
      type: 'select',
      options: [
        { value: 'SENATE', label: 'Senate' },
        { value: 'HOUSE', label: 'House of Representatives' },
      ],
      apiParam: 'chamber',
    },
    {
      id: 'party',
      label: 'Party',
      type: 'select',
      options: [
        { value: 'DEMOCRATIC', label: 'Democratic' },
        { value: 'REPUBLICAN', label: 'Republican' },
        { value: 'INDEPENDENT', label: 'Independent' },
      ],
      apiParam: 'party',
    },
    {
      id: 'state',
      label: 'State',
      type: 'select',
      options: US_STATES_OPTIONS, // Import from constants
      apiParam: 'state',
    },
  ],
  defaultSort: { column: 'lastName', direction: 'asc' },
};
```

**Existing Members Hooks:**
```typescript
// frontend/src/hooks/useMembers.ts
useMembers(params: MemberListParams)  // chamber, party, state, search, pagination
useMember(bioguideId: string)         // Single member by bioguideId
useMemberSearch(query: string)        // Search by name
```

**Existing Members API:**
```typescript
// frontend/src/lib/api/members.ts
interface MemberListParams {
  page?: number;
  size?: number;
  chamber?: 'SENATE' | 'HOUSE';
  party?: string;
  state?: string;
  search?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
```

---

### Executive Appointees Configuration

**Appointees Subtype Configuration:**
```typescript
// In peopleConfig.ts
export const appointeesSubtypeConfig: SubtypeConfig = {
  id: 'appointees',
  columns: [
    { id: 'name', label: 'Name', sortable: true },
    { id: 'positionTitle', label: 'Position', sortable: true },
    { id: 'agencyName', label: 'Agency', sortable: true },
    { id: 'appointmentType', label: 'Type', sortable: true, render: renderAppointmentTypeBadge },
  ],
  filters: [
    {
      id: 'appointmentType',
      label: 'Appointment Type',
      type: 'select',
      options: [
        { value: 'PAS', label: 'PAS - Presidential with Senate' },
        { value: 'PA', label: 'PA - Presidential' },
        { value: 'NA', label: 'NA - Non-Career SES' },
        { value: 'CA', label: 'CA - Career SES' },
        { value: 'XS', label: 'XS - Schedule C' },
      ],
      apiParam: 'appointmentType',
    },
    {
      id: 'agencyName',
      label: 'Agency',
      type: 'select',
      options: [], // Populate dynamically from API or use predefined list
      apiParam: 'agencyName',
    },
  ],
  defaultSort: { column: 'name', direction: 'asc' },
};
```

**Existing Appointees Hooks:**
```typescript
// frontend/src/hooks/useAppointees.ts (verify exact hook names)
useAppointees(params: AppointeeListParams)  // appointmentType, agencyName, search, pagination
useAppointee(id: string)                     // Single appointee by ID
```

**Existing Appointees API:**
```typescript
// frontend/src/lib/api/appointees.ts
interface AppointeeListParams {
  page?: number;
  size?: number;
  appointmentType?: string;
  agencyName?: string;
  search?: string;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}
```

---

### Architecture Reference

- Frontend: Next.js 14 App Router
- State: URL-based for filters, view mode, subtype
- UI: Shadcn/UI + Tailwind CSS
- Data: React Query with existing hooks

---

## Testing

### Test File Location
`frontend/src/app/knowledge-base/people/__tests__/`

### Testing Standards
- Use Vitest + React Testing Library
- Mock API responses
- Test configuration-driven rendering
- Test subtype switching between all three person types

### Test Cases

#### General / Infrastructure

1. **Route Access**
   - `/knowledge-base/people` defaults to first subtype (judges)
   - Subtype selector visible and functional
   - `/knowledge-base/people/[id]` loads correct detail based on data type

2. **Subtype Switching**
   - Clicking subtype tab updates URL param
   - Columns change when switching types
   - Filters change when switching types
   - Data reloads for new type

#### Federal Judges

3. **Judges Route Access**
   - `/knowledge-base/people?type=judges` shows judges list
   - `?courtLevel=SUPREME` filters to Supreme Court
   - Redirect from `/factbase/people/federal-judges` works (301)

4. **Judges EntityBrowser**
   - Columns render: Name, Court, Circuit, Status, Appointed By, Party
   - Status badges have correct colors
   - Party badges display correctly
   - Sorting works on sortable columns
   - Pagination works

5. **Judges Filtering**
   - Court Level filter shows Supreme/Appeals/District
   - Circuit filter shows all 13 circuits
   - Status filter shows Active/Senior
   - Combined filters work together

6. **Judges EntityDetail**
   - Court information section displays
   - Appointment section displays with vote counts
   - Service section conditionally displays
   - Senate vote percentage calculated correctly

7. **Judge Stats**
   - Stats display above entity browser when viewing judges
   - Shows total, active, senior counts
   - Hidden when viewing other person types

#### Congressional Members

8. **Members Route Access**
   - `/knowledge-base/people?type=members` shows members list
   - `?chamber=SENATE` filters to senators
   - Redirect from `/factbase/people/congressional-members` works (301)

9. **Members EntityBrowser**
   - Columns render: Name, Chamber, State, District, Party
   - Chamber badges display correctly (Senate/House)
   - Party badges display correctly
   - District column hidden for senators

10. **Members Filtering**
    - Chamber filter shows House/Senate
    - Party filter shows Democratic/Republican/Independent
    - State filter shows all 50+ options
    - Combined filters work together

11. **Members EntityDetail**
    - Position information displays
    - Contact information displays
    - Biography section displays
    - Social media links display correctly

#### Executive Appointees

12. **Appointees Route Access**
    - `/knowledge-base/people?type=appointees` shows appointees list
    - `?appointmentType=PAS` filters correctly
    - Redirect from `/factbase/people/executive-appointees` works (301)

13. **Appointees EntityBrowser**
    - Columns render: Name, Position, Agency, Type
    - Appointment type badges display correctly

14. **Appointees Filtering**
    - Appointment Type filter shows PAS/PA/NA/CA/XS
    - Agency filter works (if populated)
    - Search by name or position works

15. **Appointees EntityDetail**
    - Position details display
    - Agency information displays
    - Appointment details display

#### Cross-Cutting

16. **Redirects (All Types)**
    - All three old factbase URLs redirect with 301
    - Query params preserved through redirects

17. **Responsive Design**
    - Mobile layout works for all person types
    - hideOnMobile columns hidden appropriately

18. **Accessibility**
    - Keyboard navigation works for all types
    - Subtype selector keyboard accessible

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-24 | 1.0 | Initial story creation (Federal Judges only) | Sarah (PO) |
| 2025-12-24 | 1.1 | Validation: add hook refs, UX patterns, detail route strategy, verify DECEASED status | Sarah (PO) |
| 2025-12-26 | 2.0 | **SCOPE EXPANDED**: Now includes ALL person types (Judges, Members, Appointees); Updated ACs, tasks, dev notes, and tests | Sarah (PO) |

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
