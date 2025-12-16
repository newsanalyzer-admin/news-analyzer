# Architect Handoff: Epic FB-1-UI

## Document Purpose

This handoff provides the technical context needed for the Architect to review and approve Epic FB-1-UI (Frontend Integration for Congressional Factbase Entities).

---

## Epic Summary

| Field | Value |
|-------|-------|
| **Epic ID** | FB-1-UI |
| **Epic Name** | Frontend Integration for Congressional Factbase Entities |
| **Depends On** | FB-1 (Congressional Data - Complete) |
| **Scope** | Frontend-only changes, consuming existing backend APIs |

**Goal:** Enable users to observe, search, and manage Congressional factbase entities through the frontend with full administrative capabilities.

---

## Technical Context

### Current Frontend Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Next.js | 14.1.0 | App Router |
| React | 18.2.0 | |
| TypeScript | 5.3.3 | |
| Tailwind CSS | 3.4.1 | |
| TanStack React Query | 5.17.19 | Server state |
| Zustand | 4.5.0 | Client state |
| Axios | 1.6.5 | HTTP client |
| Zod | 3.22.4 | Schema validation |

### Proposed Addition: shadcn/ui

**Rationale:**
- Already has CVA (class-variance-authority), clsx, tailwind-merge dependencies installed
- Provides accessible, composable components (Table, Button, Card, Dialog, etc.)
- Non-breaking: components are copied into codebase, not installed as dependency
- Maintains Tailwind-first approach

**Components Needed:**
- `Table` - Member/Committee listings with pagination
- `Card` - Member profile display
- `Button` - Actions, filters
- `Input` / `Select` - Search and filter controls
- `Badge` - Party, chamber, role indicators
- `Dialog` - Confirmation for admin sync triggers
- `Tabs` - Member detail sections (profile, terms, committees)
- `Skeleton` - Loading states

---

## Backend API Contracts (Already Implemented)

### Member Endpoints (`/api/members`)

| Endpoint | Method | Response | Notes |
|----------|--------|----------|-------|
| `/api/members` | GET | `Page<Person>` | Paginated list |
| `/api/members/{bioguideId}` | GET | `Person` | Single member |
| `/api/members/search?name=` | GET | `Page<Person>` | Name search |
| `/api/members/by-state/{state}` | GET | `Page<Person>` | Filter by state |
| `/api/members/by-chamber/{chamber}` | GET | `Page<Person>` | SENATE or HOUSE |
| `/api/members/{bioguideId}/terms` | GET | `List<PositionHolding>` | Term history |
| `/api/members/{bioguideId}/committees` | GET | `Page<CommitteeMembership>` | Assignments |
| `/api/members/count` | GET | `Long` | Total count |
| `/api/members/stats/party` | GET | `List<Object[]>` | Party distribution |
| `/api/members/stats/state` | GET | `List<Object[]>` | State distribution |
| `/api/members/sync` | POST | `SyncResult` | Trigger sync (admin) |
| `/api/members/enrichment-sync` | POST | `SyncResult` | Trigger enrichment (admin) |
| `/api/members/enrichment-status` | GET | `SyncStatus` | Scheduler status |

### Committee Endpoints (`/api/committees`)

| Endpoint | Method | Response | Notes |
|----------|--------|----------|-------|
| `/api/committees` | GET | `Page<Committee>` | Paginated list |
| `/api/committees/{code}` | GET | `Committee` | Single committee |
| `/api/committees/{code}/members` | GET | `Page<CommitteeMembership>` | Committee members |
| `/api/committees/{code}/subcommittees` | GET | `Page<Committee>` | Subcommittees |
| `/api/committees/by-chamber/{chamber}` | GET | `Page<Committee>` | SENATE, HOUSE, JOINT |
| `/api/committees/search?name=` | GET | `Page<Committee>` | Name search |
| `/api/committees/count` | GET | `Long` | Total count |
| `/api/committees/sync` | POST | `SyncResult` | Trigger sync (admin) |
| `/api/committees/sync/memberships` | POST | `SyncResult` | Sync memberships (admin) |

---

## Data Models (Backend → Frontend TypeScript)

### Person

```typescript
interface Person {
  id: string;                    // UUID
  bioguideId: string;            // Unique identifier (e.g., "S000033")
  firstName: string;
  lastName: string;
  middleName?: string;
  suffix?: string;
  party?: string;                // "Democrat", "Republican", "Independent"
  state?: string;                // 2-letter code (e.g., "CA")
  chamber?: 'SENATE' | 'HOUSE';
  birthDate?: string;            // ISO date
  gender?: string;
  imageUrl?: string;
  externalIds?: {                // JSONB
    fec?: string[];
    govtrack?: number;
    opensecrets?: string;
    votesmart?: number;
    wikipedia?: string;
    // ... more
  };
  socialMedia?: {                // JSONB - for FB-1-UI.4
    twitter?: string;
    facebook?: string;
    youtube?: string;
    youtube_id?: string;
  };
  enrichmentSource?: string;
  enrichmentVersion?: string;
  congressLastSync?: string;     // ISO datetime
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}
```

### Committee

```typescript
interface Committee {
  committeeCode: string;         // Primary key (e.g., "hsju00")
  name: string;
  chamber: 'SENATE' | 'HOUSE' | 'JOINT';
  committeeType: 'STANDING' | 'SELECT' | 'SPECIAL' | 'JOINT' | 'SUBCOMMITTEE' | 'OTHER';
  parentCommitteeCode?: string;  // For subcommittees
  thomasId?: string;
  url?: string;
  congressLastSync?: string;
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}
```

### CommitteeMembership

```typescript
interface CommitteeMembership {
  id: string;
  person: Person;                // Nested
  committee: Committee;          // Nested
  role: 'MEMBER' | 'CHAIR' | 'VICE_CHAIR' | 'RANKING_MEMBER' | 'EX_OFFICIO';
  congress: number;              // e.g., 118
  startDate?: string;
  endDate?: string;
  congressLastSync?: string;
  dataSource?: string;
  createdAt: string;
  updatedAt: string;
}
```

### PositionHolding

```typescript
interface PositionHolding {
  id: string;
  personId: string;
  positionId: string;
  startDate: string;             // ISO date
  endDate?: string;              // null = current
  congress?: number;
  dataSource: 'CONGRESS_GOV' | 'LEGISLATORS_REPO' | 'MANUAL';
  sourceReference?: string;
  createdAt: string;
  updatedAt: string;
  // Helper fields from backend
  termLabel?: string;            // "118th Congress (2023-present)"
}
```

### Pagination Response (Spring Data Page)

```typescript
interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;                // Current page (0-indexed)
  first: boolean;
  last: boolean;
  empty: boolean;
}
```

---

## Architecture Decisions Required

### 1. shadcn/ui Integration Strategy

**Options:**

| Option | Pros | Cons |
|--------|------|------|
| A. Full adoption | Consistent UI, faster dev | Existing pages need refactor |
| B. New pages only | No regression risk | Inconsistent UI |
| C. Incremental migration | Best of both | Longer timeline |

**Recommendation:** Option B for FB-1-UI scope, with Option C as follow-up epic.

### 2. State Management for Congressional Data

**Options:**

| Option | Description | Notes |
|--------|-------------|-------|
| A. React Query only | Server state caching | Already in stack, recommended |
| B. Zustand + React Query | Add client state for filters | More complexity |
| C. URL state | Filters in URL params | Shareable links, SSR-friendly |

**Recommendation:** A + C (React Query for data, URL params for filters)

### 3. Admin Route Protection (FB-1-UI.5)

**Current:** No auth in frontend. Backend has Spring Security.

**Options:**

| Option | Description | Notes |
|--------|-------------|-------|
| A. No protection now | Admin-only by convention | Matches current state |
| B. Basic flag/env var | Hide admin link in prod | Quick, not secure |
| C. Stub role check | Placeholder for future auth | Future-ready |

**Recommendation:** Option C - Stub a `useIsAdmin()` hook returning `true`, ready for future role integration.

### 4. API Client Pattern

**Options:**

| Option | Description | Notes |
|--------|-------------|-------|
| A. Axios functions | Match existing `reasoningApi` pattern | Consistent |
| B. Generated from OpenAPI | Type-safe, auto-updated | Requires OpenAPI export |
| C. React Query hooks | Integrated fetching + caching | Modern pattern |

**Recommendation:** Option A + C (Axios functions wrapped in React Query hooks)

---

## Performance Considerations

### Large Dataset Handling (535+ members, 200+ committees)

| Concern | Mitigation |
|---------|------------|
| Initial load time | Server-side pagination (default 20 items) |
| Search latency | Debounced input (300ms) |
| Re-renders | React Query caching, memoization |
| Table performance | Virtual scrolling if >100 visible rows (shadcn/ui Table supports this) |

### Image Loading (Member Photos)

- Backend provides `imageUrl` from Congress.gov
- Use Next.js `<Image>` with lazy loading
- Placeholder/skeleton while loading
- Fallback icon for missing images

---

## Proposed File Structure

```
frontend/src/
├── app/
│   ├── members/
│   │   ├── page.tsx              # FB-1-UI.2: Members listing
│   │   └── [bioguideId]/
│   │       └── page.tsx          # FB-1-UI.4: Member detail
│   ├── committees/
│   │   └── page.tsx              # FB-1-UI.3: Committees listing
│   └── admin/
│       └── page.tsx              # FB-1-UI.5: Admin dashboard
├── components/
│   ├── ui/                       # shadcn/ui components
│   │   ├── button.tsx
│   │   ├── table.tsx
│   │   ├── card.tsx
│   │   └── ...
│   ├── members/
│   │   ├── MemberCard.tsx
│   │   ├── MemberTable.tsx
│   │   ├── MemberFilters.tsx
│   │   └── TermTimeline.tsx
│   └── committees/
│       ├── CommitteeTable.tsx
│       └── CommitteeHierarchy.tsx
├── lib/
│   └── api/
│       ├── members.ts            # FB-1-UI.1: Member API client
│       └── committees.ts         # FB-1-UI.1: Committee API client
├── hooks/
│   ├── useMembers.ts             # React Query hooks
│   ├── useCommittees.ts
│   └── useIsAdmin.ts             # Stub for future auth
└── types/
    ├── member.ts                 # FB-1-UI.1: Person, PositionHolding types
    └── committee.ts              # FB-1-UI.1: Committee, Membership types
```

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| API response mismatch | Low | Medium | Validate types against actual responses in FB-1-UI.1 |
| shadcn/ui conflicts with existing styles | Low | Low | Scoped to new pages only |
| Performance with 535 members | Medium | Medium | Pagination, virtual scrolling |
| Admin sync abuse | Low | Low | Confirmation dialogs, future rate limiting |

---

## Questions for Architect

1. **shadcn/ui adoption scope:** New pages only (Option B) or incremental migration (Option C)?

2. **Filter state management:** URL params for shareable filter states, or client-only?

3. **Admin route stub:** Is `useIsAdmin()` hook returning `true` acceptable as placeholder?

4. **API client generation:** Should we set up OpenAPI type generation for future-proofing, or manual types are sufficient?

5. **Any concerns with the proposed file structure?**

---

## Approval Requested

Please review and provide:

- [ ] Approval of shadcn/ui integration approach
- [ ] Approval of proposed architecture decisions
- [ ] Answers to questions above
- [ ] Any additional technical requirements or constraints

---

*Prepared by: Sarah (PO) | Date: 2024-11-29*
