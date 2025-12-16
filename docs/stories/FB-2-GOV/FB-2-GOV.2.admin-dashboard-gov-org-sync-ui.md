# Story FB-2-GOV.2: Admin Dashboard Gov Org Sync UI

## Status

**Done**

## Story

**As a** NewsAnalyzer administrator,
**I want** to view government organization sync status and trigger syncs from the admin dashboard,
**so that** I can monitor data freshness and manually refresh government organization data when needed.

## Acceptance Criteria

1. **Sync Status Card**: Admin dashboard displays a "Government Organizations" status card showing:
   - Total organization count
   - Count by branch (Executive, Legislative, Judicial)
   - Federal Register API availability indicator
2. **Sync Button**: "Sync Government Orgs" button is available in the Manual Sync section
3. **Confirmation Dialog**: Clicking sync button opens confirmation dialog with:
   - Warning about operation scope (~300 agencies will be synced)
   - Estimated duration notice
   - Cancel and Confirm buttons
4. **Loading State**: During sync, button shows loading spinner and is disabled
5. **Success Toast**: On successful sync, toast displays "Sync triggered successfully" with count summary
6. **Error Toast**: On sync failure, toast displays error message with destructive variant
7. **Status Refresh**: After sync completes, status card automatically refreshes to show updated counts
8. **React Query Integration**: Uses React Query hooks following existing useMembers/useCommittees patterns
9. **Component Pattern**: Follows existing SyncButton and SyncStatusCard component patterns from FB-1-UI.5
10. **Responsive Design**: Layout adapts to mobile/tablet/desktop viewports

## Tasks / Subtasks

- [x] **Task 1: React Query Hooks** (AC: 8)
  - [x] Create `useGovernmentOrgSyncStatus()` hook to fetch `/api/government-organizations/sync/status`
  - [x] Create `useGovernmentOrgSync()` mutation hook to trigger `/api/government-organizations/sync/federal-register`
  - [x] Add hooks to `frontend/src/hooks/useGovernmentOrgs.ts` (create if doesn't exist)
  - [x] Configure staleTime (60s) and refetchOnWindowFocus following existing patterns

- [x] **Task 2: Gov Org Sync Status Card** (AC: 1, 7)
  - [x] Create `GovOrgSyncStatusCard.tsx` component in `frontend/src/components/admin/`
  - [x] Display total organization count
  - [x] Display breakdown by branch (Executive/Legislative/Judicial)
  - [x] Display Federal Register API status indicator (green/red badge)
  - [x] Add loading skeleton state
  - [x] Add error state with retry button
  - [x] Auto-refresh after sync using query invalidation

- [x] **Task 3: Gov Org Sync Button** (AC: 2, 3, 4, 5, 6)
  - [x] Add "Sync Government Orgs" to SyncButton component usage in admin page
  - [x] Configure confirmation dialog with appropriate warning text
  - [x] Implement loading state during sync
  - [x] Implement success toast with sync result counts
  - [x] Implement error toast on failure

- [x] **Task 4: Admin Page Integration** (AC: 9, 10)
  - [x] Add GovOrgSyncStatusCard to admin dashboard grid
  - [x] Add Gov Org sync button to Manual Sync section
  - [x] Ensure responsive grid layout (sm:grid-cols-2 lg:grid-cols-4)
  - [x] Verify mobile layout stacks properly

- [x] **Task 5: Type Definitions** (AC: all)
  - [x] Add `GovOrgSyncStatus` interface to types
  - [x] Add `GovOrgSyncResult` interface to types
  - [x] Ensure type safety across components

## Dev Notes

### API Endpoints

**Sync Status** (GET):
```
GET /api/government-organizations/sync/status

Response:
{
  "lastSync": "2025-11-30T05:00:00",
  "totalOrganizations": 320,
  "countByBranch": {
    "executive": 300,
    "legislative": 15,
    "judicial": 5
  },
  "federalRegisterAvailable": true
}
```

**Trigger Sync** (POST):
```
POST /api/government-organizations/sync/federal-register

Response:
{
  "added": 15,
  "updated": 285,
  "skipped": 0,
  "errors": 0,
  "errorMessages": []
}
```

### Existing Patterns to Follow

**SyncStatusCard** (`frontend/src/components/admin/SyncStatusCard.tsx`):
- Card layout with count and status badge
- Loading skeleton
- Error state with retry

**SyncButton** (`frontend/src/components/admin/SyncButton.tsx`):
- Confirmation dialog pattern
- Loading spinner during operation
- Toast notifications for success/error

**useMembers hooks** (`frontend/src/hooks/useMembers.ts`):
- React Query pattern for sync operations
- Mutation hook with onSuccess/onError handlers

### Type Definitions

```typescript
// Add to frontend/src/types/government-org.ts or similar

interface GovOrgSyncStatus {
  lastSync: string | null;
  totalOrganizations: number;
  countByBranch: {
    executive: number;
    legislative: number;
    judicial: number;
  };
  federalRegisterAvailable: boolean;
}

interface GovOrgSyncResult {
  added: number;
  updated: number;
  skipped: number;
  errors: number;
  errorMessages: string[];
}
```

### React Query Hooks

```typescript
// frontend/src/hooks/useGovernmentOrgs.ts

export function useGovernmentOrgSyncStatus() {
  return useQuery({
    queryKey: ['gov-org-sync-status'],
    queryFn: async () => {
      const response = await fetch('/api/government-organizations/sync/status');
      if (!response.ok) throw new Error('Failed to fetch sync status');
      return response.json() as Promise<GovOrgSyncStatus>;
    },
    staleTime: 60 * 1000, // 60 seconds
  });
}

export function useGovernmentOrgSync() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      const response = await fetch('/api/government-organizations/sync/federal-register', {
        method: 'POST',
      });
      if (!response.ok) throw new Error('Sync failed');
      return response.json() as Promise<GovOrgSyncResult>;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['gov-org-sync-status'] });
      queryClient.invalidateQueries({ queryKey: ['government-organizations'] });
    },
  });
}
```

### Confirmation Dialog Content

```
Title: Sync Government Organizations?

Description: This will fetch ~300 agencies from the Federal Register API and update the database.

Warnings:
• May take 30-60 seconds to complete
• Existing manually curated data will be preserved
• Only executive branch agencies will be synced

[Cancel] [Confirm Sync]
```

### Source Tree Reference

```
frontend/
├── src/
│   ├── app/admin/
│   │   └── page.tsx (MODIFY - add GovOrgSyncStatusCard and sync button)
│   ├── components/admin/
│   │   ├── GovOrgSyncStatusCard.tsx (NEW)
│   │   └── SyncButton.tsx (existing - reuse)
│   ├── hooks/
│   │   └── useGovernmentOrgs.ts (NEW or MODIFY)
│   └── types/
│       └── government-org.ts (NEW or MODIFY)
```

### Testing

**Manual Testing Checklist:**
- [ ] Status card displays correct counts
- [ ] API status indicator shows green when available
- [ ] Sync button opens confirmation dialog
- [ ] Loading spinner shows during sync
- [ ] Success toast appears with counts
- [ ] Error toast appears on failure
- [ ] Status card refreshes after sync
- [ ] Mobile layout is usable

**Component Testing (optional for this story):**
- Consider adding React Testing Library tests in future iteration

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-30 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-30 | 1.1 | Architect review: Approved, no changes required | Winston (Architect) |
| 2025-11-30 | 1.2 | PO review: Approved for development | Sarah (PO) |
| 2025-11-30 | 1.3 | QA review: PASS gate, all ACs verified | Quinn (QA) |
| 2025-11-30 | 1.4 | PO final review: Approved, status set to Done | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- TypeScript compilation: Passed with no errors
- ESLint: Pre-existing lint errors in entities/page.tsx and government-orgs/page.tsx (not from this story)

### Completion Notes List
- Task 5 (Type Definitions): Created GovOrgSyncStatus and GovOrgSyncResult interfaces
- Task 1 (React Query Hooks): Created useGovernmentOrgSyncStatus and useGovernmentOrgSync hooks with query key factory pattern
- Task 2 (Status Card): Created GovOrgSyncStatusCard with loading/error states, branch breakdown, and API status badge
- Task 3 (Sync Button): Extended existing SyncButton component to support 'gov-orgs' type with result counts in toast
- Task 4 (Admin Integration): Added status card to Data Overview grid and sync button to Manual Sync section

### File List
**New Files:**
- `frontend/src/types/government-org.ts` - Type definitions for sync status and result
- `frontend/src/hooks/useGovernmentOrgs.ts` - React Query hooks for gov org sync operations
- `frontend/src/components/admin/GovOrgSyncStatusCard.tsx` - Sync status card component

**Modified Files:**
- `frontend/src/types/index.ts` - Added export for government-org types
- `frontend/src/components/admin/index.ts` - Added export for GovOrgSyncStatusCard
- `frontend/src/components/admin/SyncButton.tsx` - Extended to support 'gov-orgs' sync type
- `frontend/src/app/admin/page.tsx` - Integrated status card and sync button

---

## QA Results

### Review Date: 2025-11-30

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: Excellent** - Clean, well-structured implementation following established patterns. All acceptance criteria are met. Code is self-documenting with appropriate JSDoc comments.

**Highlights:**
- Extended existing `SyncButton` component rather than duplicating code (DRY principle)
- Query key factory pattern (`govOrgKeys`) matches existing `memberKeys` pattern
- Robust error handling in `triggerGovOrgSync` with error message parsing from API response
- Proper loading/error/success states in status card component

**Minor Observation:**
- `useGovernmentOrgs.ts` uses raw `fetch` while `useMembers.ts` uses an API client abstraction. This is acceptable for the current scope but could be unified in a future refactoring effort.

### Refactoring Performed

None required. Implementation is clean and follows established patterns.

### Compliance Check

- Coding Standards: ✓ Follows TypeScript/React standards (naming, structure, imports)
- Project Structure: ✓ Files placed correctly in types/, hooks/, components/admin/
- Testing Strategy: ✓ Story marks component testing as optional; TypeScript compilation validates types
- All ACs Met: ✓ All 10 acceptance criteria verified

### Acceptance Criteria Verification

| AC | Description | Status | Evidence |
|----|-------------|--------|----------|
| 1 | Sync Status Card | ✓ | `GovOrgSyncStatusCard.tsx` - displays total count (L101-103), branch breakdown (L104-124), API status badge (L95-97) |
| 2 | Sync Button | ✓ | `admin/page.tsx:119-124` - "Sync Government Orgs" button with type="gov-orgs" |
| 3 | Confirmation Dialog | ✓ | `SyncButton.tsx:116-140` - dialog with description, warning bullets, Cancel/Confirm buttons |
| 4 | Loading State | ✓ | `SyncButton.tsx:111-113` - Loader2 spinner, button disabled during sync |
| 5 | Success Toast | ✓ | `SyncButton.tsx:82-96` - "Sync triggered successfully" with added/updated/skipped counts |
| 6 | Error Toast | ✓ | `SyncButton.tsx:98-105` - destructive variant toast with error message |
| 7 | Status Refresh | ✓ | `useGovernmentOrgs.ts:65-69` - invalidates sync-status and gov-org queries on success |
| 8 | React Query Integration | ✓ | `useGovernmentOrgs.ts` - follows useMembers pattern with query key factory, staleTime |
| 9 | Component Pattern | ✓ | Extended SyncButton, created GovOrgSyncStatusCard following existing patterns |
| 10 | Responsive Design | ✓ | `admin/page.tsx:59` - grid uses sm:grid-cols-2 lg:grid-cols-4 |

### Improvements Checklist

- [x] All acceptance criteria implemented
- [x] Type definitions complete and exported
- [x] React Query hooks follow established patterns
- [x] Component states (loading/error/success) properly handled
- [x] Query invalidation configured for data refresh
- [ ] Consider creating API client abstraction for gov-orgs (future refactor, not blocking)
- [ ] Consider adding component unit tests (marked optional in story)

### Security Review

**Status: PASS** - No security concerns identified.
- Admin-only functionality properly gated via `useIsAdmin` hook
- No sensitive data exposure in API responses
- Standard fetch patterns without credential exposure

### Performance Considerations

**Status: PASS** - No performance concerns identified.
- `staleTime: 60s` appropriate for sync status data
- Query invalidation properly scoped to relevant queries only
- No unnecessary re-renders or data fetching

### Files Modified During Review

None - no refactoring performed.

### Gate Status

Gate: **PASS** → docs/qa/gates/FB-2-GOV.2-admin-dashboard-gov-org-sync-ui.yml

### Recommended Status

**✓ Ready for Done** - All acceptance criteria met, code quality is excellent, follows established patterns. No blocking issues found.
