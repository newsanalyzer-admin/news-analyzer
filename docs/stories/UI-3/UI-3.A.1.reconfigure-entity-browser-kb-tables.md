# Story UI-3.A.1: Reconfigure EntityBrowser for KB Tables

## Status

**Ready for Review**

## Story

**As a** user browsing the Knowledge Base,
**I want** to see authoritative data (persons, committees, government organizations),
**So that** I can explore verified reference information.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | EntityBrowser component accepts configuration for KB table endpoints |
| AC2 | "People" entity type fetches from `/api/persons` instead of `/api/entities` |
| AC3 | "Organizations" entity type fetches from `/api/government-orgs` |
| AC4 | "Committees" entity type fetches from `/api/committees` |
| AC5 | Column configurations updated to match KB table fields |
| AC6 | All existing UI-2 pattern features work (filtering, sorting, pagination) |

## Tasks / Subtasks

- [x] **Task 1: Add `dataLayer` field to EntityTypeConfig interface** (AC: 1)
  - [x] Add `dataLayer: 'kb' | 'analysis'` field to `EntityTypeConfig` in `entityTypes.ts`
  - [x] Mark existing organization config as `dataLayer: 'kb'`
  - [x] Mark existing people config as `dataLayer: 'kb'`
  - [x] This field will be used to distinguish KB data from Article Analyzer data in future stories

- [x] **Task 2: Add Committees entity type configuration** (AC: 4, 5, 6)
  - [x] Create `frontend/src/lib/config/committeesConfig.ts` with:
    - Column configuration for committees (name, chamber, committeeType, url)
    - Filter configuration (chamber filter using `/api/committees/by-chamber/{chamber}`)
    - Detail configuration for committee pages
    - Default sort by `name` ascending
    - `idField: 'committeeCode'` (not 'id' - committees use committeeCode as PK)
  - [x] Add Committees to `entityTypes` array in `entityTypes.ts`
  - [x] Import and wire up committee config as standalone entity type (not subtype)

- [x] **Task 3: Verify and document KB endpoint mappings** (AC: 2, 3, 4)
  - [x] Verify `/api/government-orgs` correctly maps to `government_organizations` table
  - [x] Verify `/api/judges`, `/api/members`, `/api/appointees` correctly map to `persons` table
  - [x] Verify `/api/committees` endpoint exists and maps to `committees` table
  - [x] Add inline documentation comments in `entityTypes.ts` clarifying data layer purpose

- [x] **Task 4: Verify existing Committee infrastructure** (AC: 4, 5)
  - [x] Verify `frontend/src/types/committee.ts` exists with correct types ✓ EXISTS
  - [x] Verify `frontend/src/lib/api/committees.ts` exists with all endpoints ✓ EXISTS
  - [x] Verify `frontend/src/hooks/useCommittees.ts` exists ✓ EXISTS
  - [x] Import types in `committeesConfig.ts` from existing `@/types/committee`

- [x] **Task 5: Update EntityTypeSelector** (AC: 1)
  - [x] Ensure EntityTypeSelector displays Committees as a selectable entity type
  - [x] Verify icon and label display correctly (use `Building` or `Users2` from lucide-react)

- [x] **Task 6: Write tests for new configurations** (AC: 6)
  - [x] Add tests for committees config in new `committeesConfig.test.ts`
  - [x] Verify existing EntityBrowser tests still pass
  - [x] Add tests verifying `dataLayer` field works correctly

## Dev Notes

### Current Implementation Analysis

The existing `entityTypes.ts` already correctly uses KB table endpoints:
- **Organizations**: `/api/government-organizations` → `government_organizations` table
- **People subtypes**: `/api/judges`, `/api/members`, `/api/appointees` → `persons` table

**Existing Committee Infrastructure (already implemented):**
- `frontend/src/types/committee.ts` - Full TypeScript types with Zod schemas
- `frontend/src/lib/api/committees.ts` - Complete API client with all endpoints
- `frontend/src/hooks/useCommittees.ts` - React Query hooks

The UI-2 implementation was built correctly but needs:
1. Adding `dataLayer` distinction for future Article Analyzer integration
2. Adding Committees **configuration** to `entityTypes.ts` (API/types/hooks exist)
3. Clear documentation of KB vs Analysis layer purpose

### Key Files

| File | Purpose | Action |
|------|---------|--------|
| `frontend/src/lib/config/entityTypes.ts` | Main entity type configuration registry | MODIFY (add dataLayer, committees) |
| `frontend/src/lib/config/peopleConfig.ts` | Subtype configurations for people | VERIFY (already KB layer) |
| `frontend/src/lib/config/committeesConfig.ts` | Committee column/filter configs | CREATE |
| `frontend/src/types/committee.ts` | Committee TypeScript types | EXISTS (use as-is) |
| `frontend/src/lib/api/committees.ts` | Committee API client | EXISTS (use as-is) |
| `frontend/src/hooks/useCommittees.ts` | Committee React Query hooks | EXISTS (use as-is) |
| `frontend/src/components/knowledge-base/EntityBrowser.tsx` | Generic browser component | NO CHANGES |
| `frontend/src/components/knowledge-base/EntityTypeSelector.tsx` | Entity type tabs | AUTO-UPDATES from config |

### Backend Endpoints

| Endpoint | Table | Status |
|----------|-------|--------|
| `GET /api/government-organizations` | `government_organizations` | ✓ Exists |
| `GET /api/judges` | `persons` (filtered) | ✓ Exists |
| `GET /api/members` | `persons` (filtered) | ✓ Exists |
| `GET /api/appointees` | `persons` (filtered) | ✓ Exists |
| `GET /api/committees` | `committees` | ✓ Exists |
| `GET /api/committees/by-chamber/{chamber}` | `committees` (filtered) | ✓ Exists |
| `GET /api/committees/search?name=` | `committees` (filtered) | ✓ Exists |

### EntityTypeConfig Interface

```typescript
export interface EntityTypeConfig<T = unknown> {
  id: string;
  label: string;
  icon: LucideIcon;
  apiEndpoint: string;
  dataLayer: 'kb' | 'analysis';  // NEW FIELD
  supportedViews: ViewMode[];
  defaultView: ViewMode;
  // ... rest of existing fields
}
```

### Committees Configuration Pattern

Follow the existing organization/people patterns:
- Create dedicated config file (`committeesConfig.ts`)
- Define columns matching Committee DTO fields
- Define filters for chamber, type, etc.
- Add detail sections for committee info

### Committee Type Reference (from backend `Committee.java`)

**Primary Fields:**
- `committeeCode`: string (PK, e.g., 'hsju00' for House Judiciary)
- `name`: string (committee name)
- `chamber`: CommitteeChamber enum ('SENATE' | 'HOUSE' | 'JOINT')
- `committeeType`: CommitteeType enum ('STANDING' | 'SELECT' | 'JOINT' | 'SUBCOMMITTEE' | 'OTHER')

**Relationship Fields:**
- `parentCommittee`: Committee (for subcommittees, lazy-loaded)
- `subcommittees`: List<Committee> (lazy-loaded)
- `memberships`: List<CommitteeMembership> (lazy-loaded)

**External IDs:**
- `thomasId`: string
- `url`: string (Congress.gov URL)

**Audit Fields:**
- `createdAt`, `updatedAt`: LocalDateTime
- `dataSource`: string (default: 'CONGRESS_GOV')

**API Endpoints (from `CommitteeController.java`):**
- `GET /api/committees` - Paginated list
- `GET /api/committees/{code}` - By committee code
- `GET /api/committees/by-chamber/{chamber}` - Filter by chamber
- `GET /api/committees/search?name=` - Search by name
- `GET /api/committees/{code}/subcommittees` - Get subcommittees
- `GET /api/committees/{code}/members` - Get members
- `GET /api/committees/count` - Total count
- `GET /api/committees/stats/type` - Type distribution
- `GET /api/committees/stats/chamber` - Chamber distribution

## Testing

### Test File Locations
- `frontend/src/lib/config/__tests__/committeesConfig.test.ts` (new)
- `frontend/src/lib/config/__tests__/entityTypes.test.ts` (update if exists, or create)

### Testing Standards
- Use Vitest framework
- Mock API calls with vi.mock()
- Test configuration exports and helper functions
- Verify column render functions work with mock data
- Test filter options are correctly defined

### Test Cases

1. **committeesConfig.test.ts** (NEW)
   - Committees config has required fields (id, label, apiEndpoint, dataLayer)
   - Column configurations render correctly with mock Committee data
   - Filter options match CommitteeChamber enum values
   - Detail config sections are defined
   - `idField` is set to 'committeeCode'

2. **entityTypes.test.ts** (UPDATE)
   - `getEntityTypeConfig('committees')` returns valid config
   - All entity types have `dataLayer` field
   - KB entity types have `dataLayer: 'kb'`
   - `entityTypes` array includes organizations, people, and committees

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-30 | 1.0 | Initial story creation | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

- ESLint plugin warning for @typescript-eslint/eslint-plugin (pre-existing project issue, TypeScript compilation passed)
- EntityTypeSelector tests updated for 3 entity types (was 2)

### Completion Notes List

1. **Task 1**: Added `DataLayer` type (`'kb' | 'analysis'`) and `dataLayer` field to `EntityTypeConfig` interface. Added documentation comments explaining KB vs Analysis layer distinction per architecture v2.5.

2. **Task 2**: Created `committeesConfig.ts` with full configuration:
   - 5 columns (name, chamber, committeeType, committeeCode, url)
   - 2 filters (chamber, type)
   - Detail config with 3 sections (overview, links, metadata)
   - Hierarchy config for subcommittee relationships
   - Card config for grid view
   - `idField: 'committeeCode'` (committees use committeeCode as PK)

3. **Task 3**: Verified all KB endpoints exist via backend controller inspection:
   - GovernmentOrganizationController.java
   - JudgeController.java, MemberController.java, AppointeeController.java
   - CommitteeController.java

4. **Task 4**: Verified existing Committee infrastructure (types, API client, hooks) already existed from prior work.

5. **Task 5**: EntityTypeSelector auto-updates from `entityTypes` array - no code changes needed. Committees now appears as third tab.

6. **Task 6**: Created test files:
   - `committeesConfig.test.ts` (17 tests)
   - `entityTypes.test.ts` (26 tests)
   - Updated `EntityTypeSelector.test.tsx` for 3 entity types (3 tests fixed)
   - All 104 frontend knowledge-base tests passing

### File List

| File | Action | Description |
|------|--------|-------------|
| `frontend/src/lib/config/entityTypes.ts` | MODIFIED | Added DataLayer type, dataLayer field to interface, added committees entity config |
| `frontend/src/lib/config/committeesConfig.ts` | CREATED | Full committees configuration (columns, filters, detail, hierarchy, card) |
| `frontend/src/lib/config/__tests__/committeesConfig.test.ts` | CREATED | 17 tests for committees config exports |
| `frontend/src/lib/config/__tests__/entityTypes.test.ts` | CREATED | 26 tests for entityTypes, dataLayer, and helper functions |
| `frontend/src/components/knowledge-base/__tests__/EntityTypeSelector.test.tsx` | MODIFIED | Updated 3 tests for 3 entity types (was 2) |

---

## QA Results

_To be filled by QA agent_
