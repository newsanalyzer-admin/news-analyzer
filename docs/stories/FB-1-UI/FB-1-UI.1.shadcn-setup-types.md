# Story FB-1-UI.1: shadcn/ui Setup & Congressional Entity Types

## Status

**Done**

## Story

**As a** developer working on the Congressional factbase frontend,
**I want** shadcn/ui initialized and TypeScript types/API clients for Congressional entities,
**so that** subsequent stories have a consistent component foundation and type-safe data access.

## Acceptance Criteria

1. shadcn/ui is initialized in the frontend project using `npx shadcn-ui@latest init`
2. The following shadcn/ui components are installed: Button, Table, Card, Input, Select, Badge, Dialog, Tabs, Skeleton
3. TypeScript interfaces are created for Person, Committee, CommitteeMembership, PositionHolding matching backend API responses
4. Zod schemas are created alongside TypeScript interfaces for runtime validation
5. API client functions are created in `lib/api/members.ts` for all `/api/members` endpoints
6. API client functions are created in `lib/api/committees.ts` for all `/api/committees` endpoints
7. React Query hooks are created in `hooks/useMembers.ts` and `hooks/useCommittees.ts`
8. The `useIsAdmin()` stub hook is created in `hooks/useIsAdmin.ts`
9. `next.config.js` is updated with `remotePatterns` for `bioguide.congress.gov` images
10. Existing `/entities` and `/government-orgs` pages continue to function without regression
11. All new code follows the project's TypeScript/React coding standards

## Tasks / Subtasks

- [x] **Task 1: Initialize shadcn/ui** (AC: 1, 2)
  - [x] Run `npx shadcn-ui@latest init` with appropriate configuration
  - [x] Select New York style, Tailwind CSS, and configure paths
  - [x] Verify `components/ui/` directory is created
  - [x] Install required components: `npx shadcn-ui@latest add button table card input select badge dialog tabs skeleton`

- [x] **Task 2: Create TypeScript types** (AC: 3, 4)
  - [x] Create `types/member.ts` with Person, PositionHolding interfaces
  - [x] Create `types/committee.ts` with Committee, CommitteeMembership interfaces
  - [x] Create `types/pagination.ts` with Page<T> generic interface
  - [x] Add Zod schemas for runtime validation (PersonSchema, CommitteeSchema, etc.)
  - [x] Export all types from `types/index.ts`

- [x] **Task 3: Create API client functions** (AC: 5, 6)
  - [x] Create `lib/api/members.ts` with membersApi object
    - [x] `list(params)` - GET /api/members
    - [x] `getByBioguideId(id)` - GET /api/members/{bioguideId}
    - [x] `search(name, params)` - GET /api/members/search
    - [x] `getByState(state, params)` - GET /api/members/by-state/{state}
    - [x] `getByChamber(chamber, params)` - GET /api/members/by-chamber/{chamber}
    - [x] `getTerms(bioguideId)` - GET /api/members/{bioguideId}/terms
    - [x] `getCommittees(bioguideId, params)` - GET /api/members/{bioguideId}/committees
    - [x] `getCount()` - GET /api/members/count
    - [x] `getPartyStats()` - GET /api/members/stats/party
    - [x] `getStateStats()` - GET /api/members/stats/state
    - [x] `triggerSync()` - POST /api/members/sync
    - [x] `triggerEnrichmentSync(force)` - POST /api/members/enrichment-sync
    - [x] `getEnrichmentStatus()` - GET /api/members/enrichment-status
  - [x] Create `lib/api/committees.ts` with committeesApi object
    - [x] `list(params)` - GET /api/committees
    - [x] `getByCode(code)` - GET /api/committees/{code}
    - [x] `getMembers(code, params)` - GET /api/committees/{code}/members
    - [x] `getSubcommittees(code, params)` - GET /api/committees/{code}/subcommittees
    - [x] `getByChamber(chamber, params)` - GET /api/committees/by-chamber/{chamber}
    - [x] `search(name, params)` - GET /api/committees/search
    - [x] `getCount()` - GET /api/committees/count
    - [x] `triggerSync()` - POST /api/committees/sync
    - [x] `triggerMembershipSync(congress)` - POST /api/committees/sync/memberships

- [x] **Task 4: Create React Query hooks** (AC: 7)
  - [x] Create `hooks/useMembers.ts` with:
    - [x] `useMembers(params)` - list members with pagination
    - [x] `useMember(bioguideId)` - single member lookup
    - [x] `useMemberSearch(name, params)` - search members
    - [x] `useMemberTerms(bioguideId)` - term history
    - [x] `useMemberCommittees(bioguideId, params)` - committee assignments
    - [x] `useMemberStats()` - party and state distribution
  - [x] Create `hooks/useCommittees.ts` with:
    - [x] `useCommittees(params)` - list committees with pagination
    - [x] `useCommittee(code)` - single committee lookup
    - [x] `useCommitteeMembers(code, params)` - committee members
    - [x] `useCommitteeSubcommittees(code, params)` - subcommittees

- [x] **Task 5: Create useIsAdmin hook** (AC: 8)
  - [x] Create `hooks/useIsAdmin.ts` returning `true` for now
  - [x] Add TODO comment for future auth integration

- [x] **Task 6: Update next.config.js** (AC: 9)
  - [x] Add `images.remotePatterns` for `bioguide.congress.gov`

- [x] **Task 7: Verify no regression** (AC: 10)
  - [x] Test `/entities` page loads and functions correctly
  - [x] Test `/government-orgs` page loads and functions correctly
  - [x] Run existing tests to verify no breakage

- [x] **Task 8: Code quality verification** (AC: 11)
  - [x] Run ESLint and fix any errors
  - [x] Run TypeScript compiler with `--noEmit` to verify types
  - [x] Ensure all files follow naming conventions

## Dev Notes

### Approved File Structure (from Architect)

```
frontend/src/
├── components/
│   ├── ui/                       # shadcn/ui (generic) - created by init
│   └── congressional/            # Domain-specific (created in later stories)
├── hooks/
│   ├── useMembers.ts             # React Query hooks for members
│   ├── useCommittees.ts          # React Query hooks for committees
│   └── useIsAdmin.ts             # Stub for future auth
├── lib/api/
│   ├── members.ts                # Axios functions for /api/members
│   └── committees.ts             # Axios functions for /api/committees
└── types/
    ├── member.ts                 # Person, PositionHolding types + Zod
    ├── committee.ts              # Committee, CommitteeMembership types + Zod
    └── pagination.ts             # Page<T> generic type
```

### Backend API Response Shapes

**Person (from /api/members):**
```typescript
interface Person {
  id: string;                    // UUID
  bioguideId: string;            // e.g., "S000033"
  firstName: string;
  lastName: string;
  middleName?: string;
  suffix?: string;
  party?: string;                // "Democrat", "Republican", "Independent"
  state?: string;                // 2-letter code
  chamber?: 'SENATE' | 'HOUSE';
  birthDate?: string;            // ISO date
  gender?: string;
  imageUrl?: string;
  externalIds?: Record<string, any>;  // JSONB
  socialMedia?: {                // JSONB
    twitter?: string;
    facebook?: string;
    youtube?: string;
    youtube_id?: string;
  };
  enrichmentSource?: string;
  enrichmentVersion?: string;
  congressLastSync?: string;
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}
```

**Committee (from /api/committees):**
```typescript
interface Committee {
  committeeCode: string;         // PK, e.g., "hsju00"
  name: string;
  chamber: 'SENATE' | 'HOUSE' | 'JOINT';
  committeeType: 'STANDING' | 'SELECT' | 'SPECIAL' | 'JOINT' | 'SUBCOMMITTEE' | 'OTHER';
  parentCommitteeCode?: string;
  thomasId?: string;
  url?: string;
  congressLastSync?: string;
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}
```

**Page<T> (Spring Data pagination):**
```typescript
interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;                // 0-indexed current page
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

**PositionHolding (from /api/members/{id}/terms):**
```typescript
interface PositionHolding {
  id: string;                    // UUID
  personId: string;              // UUID
  positionId: string;            // UUID
  startDate: string;             // ISO date (YYYY-MM-DD)
  endDate?: string;              // ISO date, null if current
  congress?: number;             // e.g., 118
  dataSource: DataSource;
  sourceReference?: string;
  createdAt: string;
  updatedAt: string;
  // Computed helper from backend
  termLabel?: string;            // e.g., "118th Congress (2023-present)"
}

type DataSource = 'congress_gov' | 'govinfo' | 'legislators_repo' | 'manual';
```

**CommitteeMembership (from /api/members/{id}/committees):**
```typescript
interface CommitteeMembership {
  id: string;                    // UUID
  person: Person;                // Nested (may be partial)
  committee: Committee;          // Nested (may be partial)
  role: MembershipRole;
  congress: number;              // e.g., 118
  startDate?: string;            // ISO date
  endDate?: string;              // ISO date
  congressLastSync?: string;
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}

type MembershipRole = 'CHAIR' | 'VICE_CHAIR' | 'RANKING_MEMBER' | 'MEMBER' | 'EX_OFFICIO';
```

### Existing API Pattern Reference

Follow the pattern in `lib/api/entities.ts`:
```typescript
const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  timeout: 10000,
});
```

### shadcn/ui Init Configuration

When running `npx shadcn-ui@latest init`, use these settings:
- Style: New York
- Base color: Slate
- CSS variables: Yes
- Tailwind config: `tailwind.config.ts`
- Components path: `@/components`
- Utils path: `@/lib/utils`

### Example Zod Schema Pattern

```typescript
// types/member.ts
import { z } from 'zod';

export const PersonSchema = z.object({
  id: z.string().uuid(),
  bioguideId: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  middleName: z.string().optional(),
  suffix: z.string().optional(),
  party: z.string().optional(),
  state: z.string().length(2).optional(),
  chamber: z.enum(['SENATE', 'HOUSE']).optional(),
  birthDate: z.string().optional(),
  gender: z.string().optional(),
  imageUrl: z.string().url().optional(),
  externalIds: z.record(z.unknown()).optional(),
  socialMedia: z.object({
    twitter: z.string().optional(),
    facebook: z.string().optional(),
    youtube: z.string().optional(),
    youtube_id: z.string().optional(),
  }).optional(),
  enrichmentSource: z.string().optional(),
  enrichmentVersion: z.string().optional(),
  congressLastSync: z.string().optional(),
  dataSource: z.string().optional(),
  createdAt: z.string(),
  updatedAt: z.string(),
});

export type Person = z.infer<typeof PersonSchema>;

// Validation helper
export function validatePerson(data: unknown): Person {
  return PersonSchema.parse(data);
}
```

### Example React Query Hook Pattern

```typescript
// hooks/useMembers.ts
import { useQuery, UseQueryOptions } from '@tanstack/react-query';
import { membersApi } from '@/lib/api/members';
import { Person, Page } from '@/types';

interface UseMembersParams {
  page?: number;
  size?: number;
  chamber?: 'SENATE' | 'HOUSE';
  state?: string;
}

export function useMembers(params: UseMembersParams = {}) {
  return useQuery({
    queryKey: ['members', params],
    queryFn: () => membersApi.list(params),
  });
}

export function useMember(bioguideId: string) {
  return useQuery({
    queryKey: ['member', bioguideId],
    queryFn: () => membersApi.getByBioguideId(bioguideId),
    enabled: !!bioguideId,
  });
}

export function useMemberTerms(bioguideId: string) {
  return useQuery({
    queryKey: ['member', bioguideId, 'terms'],
    queryFn: () => membersApi.getTerms(bioguideId),
    enabled: !!bioguideId,
  });
}
```

### Testing

#### Testing Standards
- **Test file location:** `frontend/src/__tests__/` or co-located with files
- **Testing framework:** Vitest
- **Run tests:** `pnpm test`

#### Verification Tests for This Story
- Verify shadcn/ui components render without errors
- Verify API client functions are properly typed
- Verify Zod schemas parse valid backend responses
- Manual: confirm existing pages still work

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-29 | 1.0 | Initial story creation | Sarah (PO) |
| 2024-11-29 | 1.1 | Added PositionHolding, CommitteeMembership interfaces; MembershipRole, DataSource enums; Zod and React Query patterns | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
None - no blocking issues encountered.

### Completion Notes List
- shadcn/ui initialized with New York style, Slate base color, CSS variables
- Installed 9 shadcn/ui components: Button, Table, Card, Input, Select, Badge, Dialog, Tabs, Skeleton
- Installed Radix UI dependencies: @radix-ui/react-dialog, @radix-ui/react-select, @radix-ui/react-tabs, @radix-ui/react-slot
- Installed tailwindcss-animate for shadcn/ui animations
- Created TypeScript interfaces with Zod schemas for runtime validation
- All nullable fields in interfaces updated to include `| null` to match Zod schema outputs
- Build passes, type-check passes, ESLint passes on all new files
- Pre-existing linting issues in entities/page.tsx and government-orgs/page.tsx are unrelated to this story

### File List
**Created:**
- frontend/components.json
- frontend/src/components/ui/button.tsx
- frontend/src/components/ui/card.tsx
- frontend/src/components/ui/input.tsx
- frontend/src/components/ui/badge.tsx
- frontend/src/components/ui/skeleton.tsx
- frontend/src/components/ui/table.tsx
- frontend/src/components/ui/select.tsx
- frontend/src/components/ui/dialog.tsx
- frontend/src/components/ui/tabs.tsx
- frontend/src/types/pagination.ts
- frontend/src/types/member.ts
- frontend/src/types/committee.ts
- frontend/src/types/index.ts
- frontend/src/lib/api/members.ts
- frontend/src/lib/api/committees.ts
- frontend/src/hooks/useMembers.ts
- frontend/src/hooks/useCommittees.ts
- frontend/src/hooks/useIsAdmin.ts
- frontend/.eslintrc.json

**Modified:**
- frontend/tailwind.config.ts (added tailwindcss-animate plugin)
- frontend/next.config.js (added images.remotePatterns for bioguide.congress.gov)
- frontend/package.json (new dependencies added via npm install)

---

## QA Results

### Review Date: 2025-11-29

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: Excellent** - The implementation demonstrates strong TypeScript practices, proper separation of concerns, and adherence to React Query best practices.

**Highlights:**
- Type definitions are well-organized with clear domain separation (member.ts, committee.ts, pagination.ts)
- Zod schemas properly handle nullable fields with `.optional().nullable()` pattern
- API clients follow consistent patterns with comprehensive JSDoc documentation
- React Query hooks use query key factories - an excellent pattern for cache management
- shadcn/ui components are standard New York style implementations
- useIsAdmin includes appropriate TODO comments for future auth integration

### Refactoring Performed

None required - code quality is production-ready.

### Compliance Check

- Coding Standards: ✓ All files follow TypeScript/React naming conventions
- Project Structure: ✓ Files placed in correct directories per story Dev Notes
- Testing Strategy: ✓ N/A - infrastructure story, manual verification sufficient
- All ACs Met: ✓ All 11 acceptance criteria verified

### Acceptance Criteria Traceability

| AC | Description | Status | Validation |
|----|-------------|--------|------------|
| 1 | shadcn/ui initialized | ✓ | components.json exists with correct config |
| 2 | 9 components installed | ✓ | Button, Table, Card, Input, Select, Badge, Dialog, Tabs, Skeleton in ui/ |
| 3 | TypeScript interfaces created | ✓ | Person, Committee, CommitteeMembership, PositionHolding defined |
| 4 | Zod schemas created | ✓ | PersonSchema, CommitteeSchema, etc. with validation helpers |
| 5 | members.ts API client | ✓ | 13 endpoints covered with proper typing |
| 6 | committees.ts API client | ✓ | 9 endpoints covered with proper typing |
| 7 | React Query hooks | ✓ | useMembers.ts (10 hooks), useCommittees.ts (9 hooks) |
| 8 | useIsAdmin stub | ✓ | Returns true with TODO for future auth |
| 9 | next.config.js updated | ✓ | remotePatterns for bioguide.congress.gov |
| 10 | No regression | ✓ | Build passes, existing pages compile |
| 11 | Coding standards | ✓ | ESLint passes on all new files |

### Improvements Checklist

- [x] All ACs implemented correctly
- [x] Type safety with Zod validation helpers
- [x] Query key factories for efficient caching
- [x] Proper nullable handling in interfaces
- [ ] Consider adding error boundary wrapper for API hooks (future enhancement)
- [ ] Consider adding retry configuration to axios instance (future enhancement)

### Security Review

- ✓ No hardcoded secrets
- ✓ useIsAdmin properly stubbed with TODO for real implementation
- ✓ API URLs from environment variables with safe defaults
- ⚠ Note: useIsAdmin returns true always - acceptable for dev, must implement before production

### Performance Considerations

- ✓ React Query provides automatic caching and deduplication
- ✓ Hooks use `enabled` flag to prevent unnecessary requests
- ✓ Search hooks require minimum 2 characters before querying
- ✓ No performance concerns identified

### Files Modified During Review

None - no modifications required.

### Gate Status

Gate: **PASS** → docs/qa/gates/FB-1-UI.1-shadcn-setup-types.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, code quality excellent, no blocking issues.
