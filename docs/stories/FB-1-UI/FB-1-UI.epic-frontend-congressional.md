# Epic FB-1-UI: Frontend Integration for Congressional Factbase Entities

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | FB-1-UI |
| **Epic Name** | Frontend Integration for Congressional Factbase Entities |
| **Epic Type** | Brownfield Enhancement |
| **Priority** | HIGH |
| **Status** | COMPLETE |
| **Created** | 2024-11-29 |
| **Owner** | Sarah (PO) |
| **Depends On** | FB-1 (Congressional Data - Complete) |

## Epic Goal

Enable users to observe, search, and manage Congressional factbase entities (Members, Committees, Term History) through the frontend, providing full visibility and administrative control over the data integrated in FB-1.

## Epic Description

### Existing System Context

- **Current relevant functionality:** Frontend has `/entities` page for NLP extraction and `/government-orgs` for hierarchical org viewing. TypeScript types exist for Entity, GovernmentOrganization.
- **Technology stack:** Next.js 14, React, TypeScript, Tailwind CSS
- **Integration points:** Backend REST APIs at `/api/members`, `/api/committees` with full CRUD + sync endpoints

### Enhancement Details

- **What's being added:**
  1. shadcn/ui component library integration
  2. TypeScript types for Person, Committee, CommitteeMembership, PositionHolding
  3. API client functions for all member/committee endpoints
  4. Members listing page with search, filtering (state, chamber, party)
  5. Committees listing page with chamber filtering and hierarchy
  6. Member detail page with term history, committee assignments, and social media links
  7. Separate `/admin` route for sync operations (future role-based access)
  8. Navigation updates to expose new pages

- **How it integrates:** Consumes existing backend APIs, follows established patterns, introduces shadcn/ui for improved UX
- **Success criteria:**
  - All 535+ members viewable and searchable
  - All committees browsable with member lists
  - Term history and social media accessible per member
  - Admin can trigger sync operations from dedicated `/admin` route

## Stories

| ID | Story | Priority | Estimate | Dependencies | Status |
|----|-------|----------|----------|--------------|--------|
| FB-1-UI.1 | **shadcn/ui Setup & Congressional Entity Types** - Initialize shadcn/ui, add TypeScript types and API client functions for Person, Committee, CommitteeMembership, PositionHolding | P0 | 0.5 sprint | None | **Done** ✅ |
| FB-1-UI.2 | **Members Listing & Search Page** - Create `/members` page with paginated list, search by name, filter by state/chamber/party, statistics display using shadcn/ui components | P0 | 1 sprint | FB-1-UI.1 | **Done** ✅ |
| FB-1-UI.3 | **Committees Listing & Hierarchy Page** - Create `/committees` page with chamber filtering, subcommittee hierarchy, member counts | P0 | 0.5 sprint | FB-1-UI.1 | **Done** ✅ |
| FB-1-UI.4 | **Member Detail Page** - Create `/members/[bioguideId]` page showing full profile, term history timeline, committee assignments, social media links (Twitter, Facebook, YouTube) | P1 | 0.5 sprint | FB-1-UI.2 | **Done** ✅ |
| FB-1-UI.5 | **Admin Sync Dashboard** - Create `/admin` route with sync status display and manual sync triggers for members, committees, enrichment (designed for future role-based access) | P2 | 0.5 sprint | FB-1-UI.2, FB-1-UI.3 | **Done** ✅ |

### Story Validation Summary (2025-11-29)

All stories have been validated by the Product Owner for development readiness:

| Story | Validation Score | Key Fixes Applied |
|-------|-----------------|-------------------|
| FB-1-UI.1 | Done | QA Passed |
| FB-1-UI.2 | 10/10 | Added complete US_STATES list, specified useDebounce.ts location, clarified navigation |
| FB-1-UI.3 | 9/10 | Updated stats to client-side computation, added useDebounce reference, clarified member count strategy |
| FB-1-UI.4 | 9/10 | Added externalIds structure, term/position helpers, social URL builders, clarified MemberPhoto reuse |
| FB-1-UI.5 | 8.5/10 | Aligned EnrichmentStatus with actual type, added Toast install note, AccessDenied component |

### Dependency Graph

```
FB-1-UI.1 (shadcn/ui + Types + API Client)
    │
    ├──────────────┬──────────────┐
    ▼              ▼              │
FB-1-UI.2      FB-1-UI.3         │
(Members)    (Committees)        │
    │              │              │
    ├──────────────┤              │
    ▼              ▼              ▼
FB-1-UI.4      FB-1-UI.5 ◄───────┘
(Detail)     (Admin Sync)
```

## Compatibility Requirements

- [x] Existing APIs remain unchanged (consuming only)
- [x] Database schema changes are backward compatible (no DB changes)
- [x] UI changes follow existing patterns (enhanced with shadcn/ui)
- [x] Performance impact is minimal (paginated data loading)

## Architectural Decisions (Approved)

The following decisions were approved by the Architect on 2024-11-29:

### 1. shadcn/ui Adoption Scope

**Decision:** Option C - Incremental Migration
- New pages use shadcn/ui (FB-1-UI scope)
- Existing pages remain unchanged in this epic
- Migration of `/government-orgs` is a follow-up story after FB-1-UI completes
- Initialize with `npx shadcn-ui@latest init`
- Use `components/ui/` for shadcn components

### 2. State Management

**Decision:** React Query + URL Params
- URL params for shareable, bookmarkable filter states (e.g., `/members?chamber=SENATE&state=CA`)
- React Query for server state and caching
- Use `useSearchParams()` from Next.js for reading
- Debounce search input (300ms)
- No Zustand needed for this use case

### 3. Admin Route Protection

**Decision:** Stub `useIsAdmin()` hook
```typescript
// hooks/useIsAdmin.ts
export function useIsAdmin(): boolean {
  // TODO: Integrate with auth system when available
  return process.env.NODE_ENV === 'development' || true;
}
```
- `/admin` route hidden from main nav but accessible by URL
- Confirmation dialogs required for all sync triggers

### 4. API Client Pattern

**Decision:** Axios functions + React Query hooks
- Axios functions in `lib/api/` (consistent with existing `reasoningApi` pattern)
- React Query hooks in `hooks/` for caching, loading states, error handling
- Manual TypeScript types sufficient (no OpenAPI generation required)

### 5. File Structure

**Decision:** Approved with `congressional/` grouping
```
frontend/src/
├── components/
│   ├── ui/                       # shadcn/ui (generic)
│   └── congressional/            # Domain-specific components
│       ├── MemberCard.tsx
│       ├── MemberTable.tsx
│       ├── MemberFilters.tsx
│       ├── TermTimeline.tsx
│       ├── CommitteeTable.tsx
│       └── CommitteeHierarchy.tsx
├── hooks/
│   ├── useMembers.ts
│   ├── useCommittees.ts
│   └── useIsAdmin.ts
├── lib/api/
│   ├── members.ts
│   └── committees.ts
└── types/
    ├── member.ts
    └── committee.ts
```

### Additional Technical Requirements

1. **Image Handling:**
   - Use Next.js `<Image>` with `domains` config for Congress.gov
   - Fallback to initials avatar for missing images
   - Add to `next.config.js`:
     ```javascript
     images: {
       remotePatterns: [{ protocol: 'https', hostname: 'bioguide.congress.gov' }],
     },
     ```

2. **Type Validation:**
   - Add Zod schemas alongside TypeScript interfaces for runtime validation
   - Catches API mismatches early

3. **Error Boundaries:**
   - Wrap new pages in error boundaries (`app/members/error.tsx`, etc.)

---

## Risk Mitigation

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| **API response shape mismatch** | Medium | Low | Zod runtime validation in FB-1-UI.1 |
| **Large dataset performance** | Medium | Medium | Implement pagination, lazy loading, virtual scrolling for 535+ members |
| **shadcn/ui integration conflicts** | Low | Low | Scoped to new pages only |
| **Admin sync triggers abuse** | Low | Low | Confirmation dialogs, `useIsAdmin()` stub for future auth |

**Rollback Plan:** Frontend changes are isolated; can revert to previous build without backend impact.

## Definition of Done

- [x] All 5 stories completed with acceptance criteria met
- [x] shadcn/ui integrated and working with existing styles
- [x] All current Congress members viewable in frontend
- [x] Search and filter functionality working correctly
- [x] Committee hierarchy displays correctly
- [x] Term history timeline displays for members
- [x] Social media links displayed on member profiles
- [x] Admin sync dashboard at `/admin` route operational
- [x] Navigation updated with new pages
- [x] No regression in existing `/entities` or `/government-orgs` pages

## Story Manager Handoff

> Please develop detailed user stories for this brownfield epic. Key considerations:
>
> - This is an enhancement to an existing system running **Next.js 14, React, TypeScript, Tailwind CSS**
> - **New addition:** shadcn/ui component library to be integrated in FB-1-UI.1
> - Integration points: Backend REST APIs at `/api/members`, `/api/committees`
> - Existing patterns to follow: `/government-orgs` page structure, `/entities` page for component patterns
> - Critical compatibility requirements: No backend changes, pagination required for large datasets
> - Each story must include verification that existing functionality remains intact
> - **FB-1-UI.4** must include social media links (Twitter, Facebook, YouTube) from enrichment data
> - **FB-1-UI.5** must use separate `/admin` route designed for future role-based access
>
> The epic should maintain system integrity while delivering **full observability and management of Congressional factbase entities**.

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2024-11-29 | **DRAFTED** |
| Architect | Winston | 2024-11-29 | **APPROVED** |
| Product Owner | Sarah (PO) | 2025-11-29 | **STORIES VALIDATED** |
| Product Owner | Sarah (PO) | 2025-12-01 | **EPIC COMPLETE** |

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-29 | 1.0 | Initial epic creation | Sarah (PO) |
| 2024-11-29 | 1.1 | Architect review: Added architectural decisions, approved | Winston (Architect) |
| 2025-11-29 | 1.2 | Story validation: All 5 stories validated and approved for development with fixes applied | Sarah (PO) |
| 2025-12-01 | 1.3 | Epic marked COMPLETE: All 5 stories done and QA passed | Sarah (PO) |

---

*End of Epic Document*
