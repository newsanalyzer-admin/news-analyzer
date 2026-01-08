# Story ARCH-1.5: Update CommitteeMembership Reference

## Status

**Status:** Draft
**Priority:** P1
**Estimate:** 2 story points
**Phase:** 4

## Story

**As a** developer,
**I want** CommitteeMembership to reference CongressionalMember,
**So that** committee membership is properly scoped to Congressional context.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `CommitteeMembership.personId` renamed to `congressionalMemberId` | |
| AC2 | FK updated to reference `congressional_members` table | |
| AC3 | Repository query methods updated | |
| AC4 | Unique constraint updated | |

## Tasks / Subtasks

- [ ] **Task 1: Create Migration V39** (AC2, AC4)
  - [ ] Create `V39__update_committee_membership_fk.sql`
  - [ ] Rename column `person_id` → `congressional_member_id`
  - [ ] Update FK constraint to reference `congressional_members`
  - [ ] Update unique constraint to use new column name

- [ ] **Task 2: Update CommitteeMembership Entity** (AC1)
  - [ ] Rename `person` field to `congressionalMember`
  - [ ] Update `@ManyToOne` relationship type from `Person` to `CongressionalMember`
  - [ ] Update `@JoinColumn` annotation
  - [ ] Update helper method `getPersonBioguideId()` → `getMemberBioguideId()`

- [ ] **Task 3: Update Repository** (AC3)
  - [ ] Update `CommitteeMembershipRepository` query methods
  - [ ] Rename any `findByPerson*` methods to `findByCongressionalMember*`
  - [ ] Update any custom queries

- [ ] **Task 4: Write Tests**
  - [ ] Update existing CommitteeMembership tests
  - [ ] Test new FK constraint
  - [ ] Test unique constraint

## Dev Notes

### Source Tree Reference

```
backend/src/main/java/org/newsanalyzer/
├── model/
│   └── CommitteeMembership.java  # UPDATE - person → congressionalMember
├── repository/
│   └── CommitteeMembershipRepository.java # UPDATE query methods
└── ...

backend/src/main/resources/db/migration/
└── V39__update_committee_membership_fk.sql # NEW
```

### Architectural Decision (MOD-5)

> CommitteeMembership references CongressionalMember (not Individual) because committee membership is **semantically Congressional** - only Congressional members can serve on Congressional committees. This constraint is properly modeled at the FK level, ensuring data integrity.

### Migration SQL

```sql
-- Rename column
ALTER TABLE committee_memberships
RENAME COLUMN person_id TO congressional_member_id;

-- Drop old FK constraint
ALTER TABLE committee_memberships
DROP CONSTRAINT IF EXISTS committee_memberships_person_id_fkey;

-- Add new FK constraint
ALTER TABLE committee_memberships
ADD CONSTRAINT fk_membership_congressional_member
    FOREIGN KEY (congressional_member_id)
    REFERENCES congressional_members(id);

-- Update unique constraint
ALTER TABLE committee_memberships
DROP CONSTRAINT IF EXISTS uq_person_committee_congress;

ALTER TABLE committee_memberships
ADD CONSTRAINT uq_member_committee_congress
    UNIQUE (congressional_member_id, committee_code, congress);
```

### Entity Changes

**CommitteeMembership.java:**
```java
// Before
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "person_id", nullable = false)
@NotNull(message = "Person is required")
private Person person;

// After
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "congressional_member_id", nullable = false)
@NotNull(message = "Congressional member is required")
private CongressionalMember congressionalMember;
```

### Repository Changes

```java
// Before
List<CommitteeMembership> findByPersonId(UUID personId);

// After
List<CommitteeMembership> findByCongressionalMemberId(UUID congressionalMemberId);
```

### Testing

**Test Location:** `backend/src/test/java/org/newsanalyzer/repository/CommitteeMembershipRepositoryTest.java`

**Test Requirements:**
- Test relationship with CongressionalMember
- Test unique constraint (member + committee + congress)
- Test FK constraint violation

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
