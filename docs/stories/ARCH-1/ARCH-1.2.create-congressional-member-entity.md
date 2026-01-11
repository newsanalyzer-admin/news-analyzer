# Story ARCH-1.2: Create CongressionalMember Entity and Refactor Persons Table

## Status

**Status:** Complete
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
| AC1 | `CongressionalMember` entity created with Congress-specific fields only | ✅ |
| AC2 | `individual_id` FK links to `individuals` table (unique constraint) | ✅ |
| AC3 | Flyway migration renames `persons` → `congressional_members` | ✅ |
| AC4 | Migration adds `individual_id` column | ✅ (via V36) |
| AC5 | Migration removes fields moved to `individuals` | ✅ |
| AC6 | `CongressionalMemberRepository` with `findByBioguideId`, `findByIndividualId` | ✅ |
| AC7 | Unique constraint on `bioguide_id` preserved | ✅ |

## Tasks / Subtasks

- [x] **Task 1: Create CongressionalMember Entity** (AC1, AC2)
  - [x] Create `backend/src/main/java/org/newsanalyzer/model/CongressionalMember.java`
  - [x] Add `id` (UUID, PK)
  - [x] Add `individualId` (UUID, FK to individuals, unique)
  - [x] Add `@OneToOne` relationship to `Individual`
  - [x] Add Congress-specific fields: `bioguideId`, `chamber`, `state`
  - [x] Add sync tracking: `congressLastSync`
  - [x] Add enrichment fields: `enrichmentSource`, `enrichmentVersion`
  - [x] Add `dataSource` (DataSource enum)
  - [x] Add audit fields and lifecycle callbacks

- [x] **Task 2: Create Migration V37** (AC3, AC4, AC5)
  - [x] Create `V37__refactor_persons_to_congressional_members.sql`
  - [x] Step 1: Ensure `individual_id` column exists and is populated (from ARCH-1.3)
  - [x] Step 2: Drop columns moved to individuals: `first_name`, `last_name`, `middle_name`, `suffix`, `birth_date`, `death_date`, `birth_place`, `gender`, `image_url`, `external_ids`, `social_media`
  - [x] Step 3: Rename table `persons` → `congressional_members`
  - [x] Step 4: Add FK constraint on `individual_id`
  - [x] Step 5: Add unique constraint on `individual_id`
  - [x] Step 6: Update indexes

- [x] **Task 3: Create Repository** (AC6)
  - [x] Create `backend/src/main/java/org/newsanalyzer/repository/CongressionalMemberRepository.java`
  - [x] Extend `JpaRepository<CongressionalMember, UUID>`
  - [x] Add `findByBioguideId(String bioguideId)` method
  - [x] Add `findByIndividualId(UUID individualId)` method
  - [x] Add `existsByBioguideId(String bioguideId)` method

- [x] **Task 4: Preserve Bioguide Constraint** (AC7)
  - [x] Ensure unique constraint on `bioguide_id` remains
  - [x] Test constraint violation handling

- [x] **Task 5: Write Tests**
  - [x] Create `CongressionalMemberRepositoryTest.java`
  - [x] Test entity persistence with Individual relationship
  - [x] Test `findByBioguideId` query
  - [x] Test unique constraints

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
├── V36__migrate_persons_to_individuals.sql # From ARCH-1.3
├── V37__refactor_persons_to_congressional_members.sql # THIS STORY
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

### Migration Order (Updated)

```
V34: Create individuals table (ARCH-1.1) ✅
V36: Migrate data to individuals (ARCH-1.3) ✅
V37: Refactor persons → congressional_members (THIS STORY) ✅
V38: Add individual_id to presidencies/position_holdings (ARCH-1.4a)
V39: Finalize FK constraints (ARCH-1.4b)
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
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Used V37 instead of V35 (V36 was used by ARCH-1.3)
- PostgreSQL auto-updates FK references when table is renamed
- Existing FKs from committee_memberships, position_holdings, presidencies will be fixed in ARCH-1.4/ARCH-1.5

### Completion Notes List
- Entity uses `@jakarta.persistence.Entity` to avoid conflict with model.Entity class
- Chamber enum defined within CongressionalMember class
- 27 repository tests pass with Testcontainers PostgreSQL
- Migration includes prerequisite verification (checks individual_id column exists and is populated)
- Biographical columns dropped from persons table before rename
- Indexes renamed to match new table name

### File List
| File | Action |
|------|--------|
| `backend/src/main/java/org/newsanalyzer/model/CongressionalMember.java` | Created |
| `backend/src/main/resources/db/migration/V37__refactor_persons_to_congressional_members.sql` | Created |
| `backend/src/main/java/org/newsanalyzer/repository/CongressionalMemberRepository.java` | Created |
| `backend/src/test/java/org/newsanalyzer/repository/CongressionalMemberRepositoryTest.java` | Created |

## QA Results
*To be populated after QA review*
