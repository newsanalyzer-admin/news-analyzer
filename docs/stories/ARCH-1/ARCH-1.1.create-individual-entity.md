# Story ARCH-1.1: Create Individual Entity and Table

## Status

**Status:** Draft
**Priority:** P0 (Critical Path)
**Estimate:** 3 story points
**Phase:** 1

## Story

**As a** developer,
**I want** an Individual entity representing any person in the system,
**So that** we have a single source of truth for biographical data.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `Individual` JPA entity created with all core biographical fields | |
| AC2 | Flyway migration `V34__create_individuals_table.sql` creates table | |
| AC3 | `IndividualRepository` with standard CRUD + `findByFirstNameAndLastName` | |
| AC4 | Indexes on `(first_name, last_name)` and `birth_date` | |
| AC5 | Entity includes `external_ids` JSONB for cross-referencing | |
| AC6 | Entity includes `party` field for current/primary affiliation (MOD-2) | |
| AC7 | Entity includes `primary_data_source` for provenance tracking (MOD-3) | |
| AC8 | Composite unique constraint on `(first_name, last_name, birth_date)` for deduplication (MOD-1) | |
| AC9 | Unit tests for entity and repository | |

## Tasks / Subtasks

- [ ] **Task 1: Create Individual Entity** (AC1, AC5, AC6, AC7)
  - [ ] Create `backend/src/main/java/org/newsanalyzer/model/Individual.java`
  - [ ] Add core biographical fields: `firstName`, `lastName`, `middleName`, `suffix`
  - [ ] Add date fields: `birthDate`, `deathDate`
  - [ ] Add other fields: `birthPlace`, `gender`, `imageUrl`
  - [ ] Add `party` field (VARCHAR 50, nullable) for current/primary affiliation
  - [ ] Add `externalIds` JSONB field for cross-referencing
  - [ ] Add `socialMedia` JSONB field
  - [ ] Add `primaryDataSource` enum field (DataSource)
  - [ ] Add audit fields: `createdAt`, `updatedAt`
  - [ ] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
  - [ ] Add helper methods: `getFullName()`, `isLiving()`

- [ ] **Task 2: Create Flyway Migration** (AC2, AC4, AC8)
  - [ ] Create `backend/src/main/resources/db/migration/V34__create_individuals_table.sql`
  - [ ] Define table with all columns and proper types
  - [ ] Add index `idx_individuals_name` on `(first_name, last_name)`
  - [ ] Add index `idx_individuals_birth_date` on `birth_date`
  - [ ] Add partial unique index `idx_individuals_unique_person` on `(LOWER(first_name), LOWER(last_name), birth_date)` WHERE `birth_date IS NOT NULL`

- [ ] **Task 3: Create Repository** (AC3)
  - [ ] Create `backend/src/main/java/org/newsanalyzer/repository/IndividualRepository.java`
  - [ ] Extend `JpaRepository<Individual, UUID>`
  - [ ] Add `findByFirstNameAndLastName(String firstName, String lastName)` method
  - [ ] Add `findByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, LocalDate birthDate)` method
  - [ ] Add `findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName)` method

- [ ] **Task 4: Write Tests** (AC9)
  - [ ] Create `IndividualRepositoryTest.java` with Testcontainers
  - [ ] Test entity persistence and retrieval
  - [ ] Test `findByFirstNameAndLastName` query
  - [ ] Test unique constraint violation handling
  - [ ] Test JSONB field storage/retrieval

## Dev Notes

### Source Tree Reference

```
backend/src/main/java/org/newsanalyzer/
├── model/
│   ├── Individual.java          # NEW - Create this
│   ├── Person.java              # Existing - Reference for field patterns
│   └── DataSource.java          # Existing - Reuse enum
├── repository/
│   └── IndividualRepository.java # NEW - Create this
└── ...

backend/src/main/resources/db/migration/
├── V33__*.sql                   # Last existing migration
└── V34__create_individuals_table.sql # NEW - Create this
```

### Entity Pattern Reference

Follow the same pattern as `Person.java`:
- Use Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- Use `@JsonIgnoreProperties(ignoreUnknown = true)`
- Use `@JdbcTypeCode(SqlTypes.JSON)` for JSONB columns
- Include audit fields with lifecycle callbacks

### Migration SQL

```sql
CREATE TABLE individuals (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    suffix VARCHAR(20),
    birth_date DATE,
    death_date DATE,
    birth_place VARCHAR(200),
    gender VARCHAR(10),
    image_url VARCHAR(500),
    party VARCHAR(50),
    external_ids JSONB,
    social_media JSONB,
    primary_data_source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_individuals_name ON individuals(first_name, last_name);
CREATE INDEX idx_individuals_birth_date ON individuals(birth_date);

-- Composite unique constraint for deduplication (MOD-1)
CREATE UNIQUE INDEX idx_individuals_unique_person
ON individuals(LOWER(first_name), LOWER(last_name), birth_date)
WHERE birth_date IS NOT NULL;
```

### Testing

**Test Location:** `backend/src/test/java/org/newsanalyzer/repository/IndividualRepositoryTest.java`

**Testing Framework:**
- JUnit 5 + Testcontainers (PostgreSQL)
- Follow pattern in `EntityRepositoryTest.java` or `StatuteRepositoryTest.java`

**Test Requirements:**
- Use `@Testcontainers` and `@Import(TestcontainersConfiguration.class)`
- Test CRUD operations
- Test custom query methods
- Test unique constraint behavior
- Test JSONB field round-trip

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
