# Story ARCH-1.1: Create Individual Entity and Table

## Status

**Status:** Complete
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
| AC1 | `Individual` JPA entity created with all core biographical fields | ✅ |
| AC2 | Flyway migration `V34__create_individuals_table.sql` creates table | ✅ |
| AC3 | `IndividualRepository` with standard CRUD + `findByFirstNameAndLastName` | ✅ |
| AC4 | Indexes on `(first_name, last_name)` and `birth_date` | ✅ |
| AC5 | Entity includes `external_ids` JSONB for cross-referencing | ✅ |
| AC6 | Entity includes `party` field for current/primary affiliation (MOD-2) | ✅ |
| AC7 | Entity includes `primary_data_source` for provenance tracking (MOD-3) | ✅ |
| AC8 | Composite unique constraint on `(first_name, last_name, birth_date)` for deduplication (MOD-1) | ✅ |
| AC9 | Unit tests for entity and repository | ✅ |

## Tasks / Subtasks

- [x] **Task 1: Create Individual Entity** (AC1, AC5, AC6, AC7)
  - [x] Create `backend/src/main/java/org/newsanalyzer/model/Individual.java`
  - [x] Add core biographical fields: `firstName`, `lastName`, `middleName`, `suffix`
  - [x] Add date fields: `birthDate`, `deathDate`
  - [x] Add other fields: `birthPlace`, `gender`, `imageUrl`
  - [x] Add `party` field (VARCHAR 50, nullable) for current/primary affiliation
  - [x] Add `externalIds` JSONB field for cross-referencing
  - [x] Add `socialMedia` JSONB field
  - [x] Add `primaryDataSource` enum field (DataSource)
  - [x] Add audit fields: `createdAt`, `updatedAt`
  - [x] Add `@PrePersist` and `@PreUpdate` lifecycle callbacks
  - [x] Add helper methods: `getFullName()`, `isLiving()`

- [x] **Task 2: Create Flyway Migration** (AC2, AC4, AC8)
  - [x] Create `backend/src/main/resources/db/migration/V34__create_individuals_table.sql`
  - [x] Define table with all columns and proper types
  - [x] Add index `idx_individuals_name` on `(first_name, last_name)`
  - [x] Add index `idx_individuals_birth_date` on `birth_date`
  - [x] Add partial unique index `idx_individuals_unique_person` on `(LOWER(first_name), LOWER(last_name), birth_date)` WHERE `birth_date IS NOT NULL`

- [x] **Task 3: Create Repository** (AC3)
  - [x] Create `backend/src/main/java/org/newsanalyzer/repository/IndividualRepository.java`
  - [x] Extend `JpaRepository<Individual, UUID>`
  - [x] Add `findByFirstNameAndLastName(String firstName, String lastName)` method
  - [x] Add `findByFirstNameAndLastNameAndBirthDate(String firstName, String lastName, LocalDate birthDate)` method
  - [x] Add `findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName)` method

- [x] **Task 4: Write Tests** (AC9)
  - [x] Create `IndividualRepositoryTest.java` with Testcontainers
  - [x] Test entity persistence and retrieval
  - [x] Test `findByFirstNameAndLastName` query
  - [x] Test unique constraint violation handling
  - [x] Test JSONB field storage/retrieval

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
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Fixed pre-existing Lombok annotation processing issues by adding maven-compiler-plugin config
- Fixed `@Entity` annotation conflict with `org.newsanalyzer.model.Entity` class

### Completion Notes List
- All 27 repository tests pass
- Entity uses `@jakarta.persistence.Entity` to avoid conflict with Entity model class
- Partial unique index uses LOWER() for case-insensitive deduplication
- Added additional repository methods: `searchByName`, `findByBioguideId`, `findByExternalId`

### File List
| File | Action |
|------|--------|
| `backend/src/main/java/org/newsanalyzer/model/Individual.java` | Created |
| `backend/src/main/resources/db/migration/V34__create_individuals_table.sql` | Created |
| `backend/src/main/java/org/newsanalyzer/repository/IndividualRepository.java` | Created |
| `backend/src/test/java/org/newsanalyzer/repository/IndividualRepositoryTest.java` | Created |
| `backend/pom.xml` | Modified (added maven-compiler-plugin for Lombok) |

## QA Results
*To be populated after QA review*
