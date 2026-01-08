# Story ARCH-1.8: Update Frontend Types and Components

## Status

**Status:** Draft
**Priority:** P1
**Estimate:** 3 story points
**Phase:** 5

## Story

**As a** frontend developer,
**I want** TypeScript types and components updated for the new model,
**So that** the UI works correctly with the refactored backend.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `Individual` TypeScript interface created | |
| AC2 | `CongressionalMember` TypeScript interface created | |
| AC3 | Existing `Person` type mapped to backward-compatible DTO | |
| AC4 | API client functions updated if endpoint paths changed | |
| AC5 | Components using person data continue to work | |
| AC6 | All frontend tests pass | |

## Tasks / Subtasks

- [ ] **Task 1: Create Individual Interface** (AC1)
  - [ ] Create `frontend/src/types/individual.ts`
  - [ ] Define `Individual` interface matching IndividualDTO
  - [ ] Export from types index

- [ ] **Task 2: Create CongressionalMember Interface** (AC2)
  - [ ] Create `frontend/src/types/congressionalMember.ts`
  - [ ] Define `CongressionalMember` interface
  - [ ] Include relationship to Individual
  - [ ] Export from types index

- [ ] **Task 3: Update Person Type** (AC3)
  - [ ] Update or alias `Person` type to match backward-compatible MemberDTO
  - [ ] Ensure existing code using Person type continues to work
  - [ ] Add deprecation comment if appropriate

- [ ] **Task 4: Review API Client Functions** (AC4)
  - [ ] Check all API client functions that fetch person data
  - [ ] Update return types if needed
  - [ ] No endpoint path changes expected (backend maintains compatibility)

- [ ] **Task 5: Review Components** (AC5)
  - [ ] Review components using person/member data
  - [ ] Verify prop types are compatible
  - [ ] Update any direct Person type references

- [ ] **Task 6: Run All Frontend Tests** (AC6)
  - [ ] Run `pnpm test`
  - [ ] Fix any failing tests
  - [ ] Add tests for new types if needed

## Dev Notes

### Source Tree Reference

```
frontend/src/
├── types/
│   ├── individual.ts          # NEW
│   ├── congressionalMember.ts # NEW
│   ├── person.ts              # UPDATE (or alias)
│   └── index.ts               # UPDATE exports
├── hooks/
│   └── useMembers.ts          # REVIEW - may need type updates
├── components/
│   ├── knowledge-base/
│   │   └── MemberCard.tsx     # REVIEW
│   └── ...
└── ...
```

### Minimal Changes Expected

Since the backend provides **backward-compatible DTOs**, the frontend changes should be minimal:

1. **Backend flattens data** - Frontend receives same JSON structure
2. **No new required fields** - Existing components continue to work
3. **Types are documentation** - Add new types for clarity, alias existing

### Individual Interface

```typescript
// frontend/src/types/individual.ts
export interface Individual {
  id: string;
  firstName: string;
  lastName: string;
  middleName?: string;
  suffix?: string;
  fullName: string;
  birthDate?: string;
  deathDate?: string;
  birthPlace?: string;
  gender?: string;
  imageUrl?: string;
  party?: string;
  isLiving: boolean;
  externalIds?: Record<string, unknown>;
  socialMedia?: Record<string, unknown>;
}
```

### CongressionalMember Interface

```typescript
// frontend/src/types/congressionalMember.ts
export interface CongressionalMember {
  id: string;
  individualId: string;
  bioguideId: string;
  chamber?: 'SENATE' | 'HOUSE';
  state?: string;
  congressLastSync?: string;
  // Individual data is typically flattened in API responses
}
```

### Member Type (Backward Compatible)

```typescript
// frontend/src/types/member.ts
// This matches the flattened MemberDTO from backend
export interface Member {
  id: string;
  // Biographical (from Individual)
  firstName: string;
  lastName: string;
  middleName?: string;
  suffix?: string;
  fullName: string;
  birthDate?: string;
  deathDate?: string;
  birthPlace?: string;
  imageUrl?: string;
  isLiving: boolean;
  // Congressional (from CongressionalMember)
  bioguideId: string;
  chamber?: 'SENATE' | 'HOUSE';
  state?: string;
  party?: string;
  congressLastSync?: string;
}

// Alias for backward compatibility
export type Person = Member;
```

### Components to Review

| Component | File | Notes |
|-----------|------|-------|
| MemberCard | `components/knowledge-base/MemberCard.tsx` | Uses person data |
| PresidentCard | `components/knowledge-base/PresidentCard.tsx` | Uses presidency data |
| JudgeList | `components/knowledge-base/JudgeList.tsx` | Uses judge data |

### Testing

**Test Command:** `pnpm test`

**Test Requirements:**
- All existing tests pass
- Type compilation succeeds (`pnpm exec tsc --noEmit`)
- No runtime errors in components

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-08 | 1.0 | Initial story creation from epic | Sarah (PO) |

## Dev Agent Record

### Agent Model Used
*To be populated during implementation*

### Debug Log References
*To be populated during implementation*

### Completion Notes List
*To be populated during implementation*

### File List
*To be populated during implementation*

## QA Results
*To be populated after QA review*
