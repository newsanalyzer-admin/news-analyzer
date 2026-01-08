# Story ARCH-1.2: Create CongressionalMember Entity and Refactor Persons Table

## Status

**Status:** Draft
**Priority:** P0 (Critical Path)
**Estimate:** 4 story points
**Phase:** 3 (after data migration populates individual_id)

## Story

**As a** developer,
**I want** Congressional-specific data in a separate table linked to Individual,
**So that** Congressional members are a specialized view of individuals.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `CongressionalMember` entity created with Congress-specific fields only | |
| AC2 | `individual_id` FK links to `individuals` table (unique constraint) | |
| AC3 | Flyway migration renames `persons` → `congressional_members` | |
| AC4 | Migration adds `individual_id` column | |
| AC5 | Migration removes fields moved to `individuals` | |
| AC6 | `CongressionalMemberRepository` with `findByBioguideId`, `findByIndividualId` | |
| AC7 | Unique constraint on `bioguide_id` preserved | |

## Tasks / Subtasks

- [ ] **Task 1: Create CongressionalMember Entity** (AC1, AC2)
  - [ ] Create `backend/src/main/java/org/newsanalyzer/model/CongressionalMember.java`
  - [ ] Add `id` (UUID, PK)
  - [ ] Add `individualId` (UUID, FK to individuals, unique)
  - [ ] Add `@OneToOne` relationship to `Individual`
  - [ ] Add Congress-specific fields: `bioguideId`, `chamber`, `state`
  - [ ] Add sync tracking: `congressLastSync`
  - [ ] Add enrichment fields: `enrichmentSource`, `enrichmentVersion`
  - [ ] Add `dataSource` (DataSource enum)
  - [ ] Add audit fields and lifecycle callbacks

- [ ] **Task 2: Create Migration V35** (AC3, AC4, AC5)
  - [ ] Create `V35__refactor_persons_to_congressional_members.sql`
  - [ ] Step 1: Ensure `individual_id` column exists and is populated (from ARCH-1.3)
  - [ ] Step 2: Drop columns moved to individuals: `first_name`, `last_name`, `middle_name`, `suffix`, `birth_date`, `death_date`, `birth_place`, `gender`, `image_url`, `external_ids`, `social_media`
  - [ ] Step 3: Rename table `persons` → `congressional_members`
  - [ ] Step 4: Add FK constraint on `individual_id`
  - [ ] Step 5: Add unique constraint on `individual_id`
  - [ ] Step 6: Update indexes

- [ ] **Task 3: Create Repository** (AC6)
  - [ ] Create `backend/src/main/java/org/newsanalyzer/repository/CongressionalMemberRepository.java`
  - [ ] Extend `JpaRepository<CongressionalMember, UUID>`
  - [ ] Add `findByBioguideId(String bioguideId)` method
  - [ ] Add `findByIndividualId(UUID individualId)` method
  - [ ] Add `existsByBioguideId(String bioguideId)` method

- [ ] **Task 4: Preserve Bioguide Constraint** (AC7)
  - [ ] Ensure unique constraint on `bioguide_id` remains
  - [ ] Test constraint violation handling

- [ ] **Task 5: Write Tests**
  - [ ] Create `CongressionalMemberRepositoryTest.java`
  - [ ] Test entity persistence with Individual relationship
  - [ ] Test `findByBioguideId` query
  - [ ] Test unique constraints

## Dev Notes

### Source Tree Reference

```
backend/src/main/java/org/newsanalyzer/
├── model/
│   ├── CongressionalMember.java  # NEW - Create this
│   ├── Individual.java           # From ARCH-1.1
│   └── Person.java               # DEPRECATED after this story
├── repository/
│   ├── CongressionalMemberRepository.java # NEW - Create this
│   └── PersonRepository.java     # DEPRECATED after this story
└── ...

backend/src/main/resources/db/migration/
├── V34__create_individuals_table.sql  # From ARCH-1.1
├── V35__refactor_persons_to_congressional_members.sql # NEW
└── ...
```

### Entity Structure

```java
@Entity
@Table(name = "congressional_members")
public class CongressionalMember {
    @Id
    private UUID id;

    @Column(name = "individual_id", unique = true)
    private UUID individualId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id", insertable = false, updatable = false)
    private Individual individual;

    // Congress-specific fields only
    private String bioguideId;

    @Enumerated(EnumType.STRING)
    private Chamber chamber;  // Use existing enum from Person.java

    private String state;
    private LocalDateTime congressLastSync;
    private String enrichmentSource;
    private String enrichmentVersion;

    @Enumerated(EnumType.STRING)
    private DataSource dataSource;

    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Migration Dependency

**IMPORTANT:** This migration MUST run AFTER ARCH-1.3 (data migration) has populated `individual_id` for all rows in `persons` table.

### Migration Order (MOD-4)

```
V34: Create individuals table (ARCH-1.1)
V36: Migrate data to individuals (ARCH-1.3)
V37: Add individual_id to presidencies/position_holdings (ARCH-1.4a)
V35: Refactor persons → congressional_members (THIS STORY)
V38: Finalize FK constraints (ARCH-1.4b)
```

### Testing

**Test Location:** `backend/src/test/java/org/newsanalyzer/repository/CongressionalMemberRepositoryTest.java`

**Test Requirements:**
- Use Testcontainers (PostgreSQL)
- Test relationship with Individual
- Test bioguide_id uniqueness
- Test individual_id uniqueness

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
