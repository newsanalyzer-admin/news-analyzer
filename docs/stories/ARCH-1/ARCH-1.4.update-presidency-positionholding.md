# Story ARCH-1.4: Update Presidency and PositionHolding References

## Status

**Status:** Complete
**Priority:** P0 (Critical Path)
**Estimate:** 5 story points (revised from 4 per MOD-4)
**Phase:** 2 (Phase A), 4 (Phase B)

## Story

**As a** developer,
**I want** Presidency and PositionHolding to reference Individual instead of Person,
**So that** presidents, VPs, and appointees link to the master individual record.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | `Presidency.personId` renamed to `individualId` | ✅ |
| AC2 | `Presidency.person` relationship updated to `individual` | ✅ |
| AC3 | `PositionHolding.personId` renamed to `individualId` | ✅ |
| AC4 | `PositionHolding.person` relationship updated to `individual` | ✅ |
| AC5 | Flyway migration updates FK references | ✅ |
| AC6 | Indexes updated for new column names | ✅ |
| AC7 | All existing FK values point to valid Individual records | ✅ |
| AC8 | Repository query methods updated | ✅ |

## Tasks / Subtasks

- [x] **Task 1: Phase A - Add individual_id Columns (V38)** (AC5, AC7)
  - [x] Create `V38__add_individual_id_to_presidency_holding.sql`
  - [x] Add `individual_id` column to `presidencies` (nullable initially)
  - [x] Add `individual_id` column to `position_holdings` (nullable initially)
  - [x] Populate from mapping via congressional_members.individual_id
  - [x] Verify no NULL values remain

- [x] **Task 2: Phase B - Finalize Constraints (V39)** (AC5, AC6)
  - [x] Create `V39__finalize_individual_fk_constraints.sql`
  - [x] Make `individual_id` NOT NULL on presidencies
  - [x] Make `individual_id` NOT NULL on position_holdings
  - [x] Add FK constraints to individuals table
  - [x] Drop old `person_id` columns
  - [x] Update indexes

- [x] **Task 3: Update Presidency Entity** (AC1, AC2)
  - [x] Rename `personId` field to `individualId`
  - [x] Rename `person` relationship to `individual`
  - [x] Update `@JoinColumn` annotation
  - [x] Update @Table index annotations

- [x] **Task 4: Update PositionHolding Entity** (AC3, AC4)
  - [x] Rename `personId` field to `individualId`
  - [x] Rename `person` relationship to `individual`
  - [x] Update `@JoinColumn` annotation
  - [x] Update @Table index annotations

- [x] **Task 5: Update Repositories** (AC8)
  - [x] Update `PresidencyRepository` query methods
  - [x] Update `PositionHoldingRepository` query methods
  - [x] Rename all `findByPersonId` to `findByIndividualId`
  - [x] Update Chamber import from Person.Chamber to CongressionalMember.Chamber

- [x] **Task 6: Verify Changes**
  - [x] Compile project successfully
  - [x] Run repository tests (54 tests pass)

## Dev Notes

### Source Tree Reference

```
backend/src/main/java/org/newsanalyzer/
├── model/
│   ├── Presidency.java           # UPDATE - personId → individualId
│   └── PositionHolding.java      # UPDATE - personId → individualId
├── repository/
│   ├── PresidencyRepository.java      # UPDATE query methods
│   └── PositionHoldingRepository.java # UPDATE query methods
└── ...

backend/src/main/resources/db/migration/
├── V38__add_individual_id_to_presidency_holding.sql # Phase A
└── V39__finalize_individual_fk_constraints.sql      # Phase B
```

### Two-Phase Migration (MOD-4)

**Phase A (V38) - Non-Breaking:**
```sql
-- Add individual_id columns (nullable initially)
ALTER TABLE presidencies ADD COLUMN individual_id UUID;
ALTER TABLE position_holdings ADD COLUMN individual_id UUID;

-- Populate from mapping (person_id → individuals.id)
-- This requires joining through persons to find matching individual
UPDATE presidencies p
SET individual_id = i.id
FROM individuals i
INNER JOIN persons per ON LOWER(i.first_name) = LOWER(per.first_name)
                       AND LOWER(i.last_name) = LOWER(per.last_name)
                       AND (i.birth_date = per.birth_date OR (i.birth_date IS NULL AND per.birth_date IS NULL))
WHERE p.person_id = per.id;

UPDATE position_holdings ph
SET individual_id = i.id
FROM individuals i
INNER JOIN persons per ON LOWER(i.first_name) = LOWER(per.first_name)
                       AND LOWER(i.last_name) = LOWER(per.last_name)
                       AND (i.birth_date = per.birth_date OR (i.birth_date IS NULL AND per.birth_date IS NULL))
WHERE ph.person_id = per.id;

-- Verify no NULLs
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM presidencies WHERE individual_id IS NULL) THEN
        RAISE EXCEPTION 'presidencies has NULL individual_id values';
    END IF;
    IF EXISTS (SELECT 1 FROM position_holdings WHERE individual_id IS NULL) THEN
        RAISE EXCEPTION 'position_holdings has NULL individual_id values';
    END IF;
END $$;
```

**Phase B (V39) - After ARCH-1.2:**
```sql
-- Make NOT NULL and add FK constraints
ALTER TABLE presidencies ALTER COLUMN individual_id SET NOT NULL;
ALTER TABLE presidencies ADD CONSTRAINT fk_presidency_individual
    FOREIGN KEY (individual_id) REFERENCES individuals(id);
ALTER TABLE presidencies DROP COLUMN person_id;

ALTER TABLE position_holdings ALTER COLUMN individual_id SET NOT NULL;
ALTER TABLE position_holdings ADD CONSTRAINT fk_holding_individual
    FOREIGN KEY (individual_id) REFERENCES individuals(id);
ALTER TABLE position_holdings DROP COLUMN person_id;

-- Update indexes
DROP INDEX IF EXISTS idx_presidency_person;
CREATE INDEX idx_presidency_individual ON presidencies(individual_id);

DROP INDEX IF EXISTS idx_holding_person;
CREATE INDEX idx_holding_individual ON position_holdings(individual_id);
```

### Entity Changes

**Presidency.java:**
```java
// Before
@Column(name = "person_id", nullable = false)
private UUID personId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "person_id", insertable = false, updatable = false)
private Person person;

// After
@Column(name = "individual_id", nullable = false)
private UUID individualId;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "individual_id", insertable = false, updatable = false)
private Individual individual;
```

### Testing

**Test Requirements:**
- Test entity relationship loads correctly
- Test FK constraint violations
- Test repository query methods

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-01-08 | 1.0 | Initial story creation from epic | Sarah (PO) |

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Used V38/V39 instead of V37/V38 (V37 was used by ARCH-1.2)
- Mapping uses congressional_members.individual_id since persons table was renamed
- PositionHoldingRepository imports CongressionalMember.Chamber instead of Person.Chamber

### Completion Notes List
- Phase A (V38) adds individual_id columns and populates from congressional_members mapping
- Phase B (V39) makes NOT NULL, adds FK constraints, drops old person_id columns
- Both entities updated with individual relationship to Individual class
- All repository methods renamed from personId to individualId
- 54 repository tests pass

### File List
| File | Action |
|------|--------|
| `backend/src/main/resources/db/migration/V38__add_individual_id_to_presidency_holding.sql` | Created |
| `backend/src/main/resources/db/migration/V39__finalize_individual_fk_constraints.sql` | Created |
| `backend/src/main/java/org/newsanalyzer/model/Presidency.java` | Modified |
| `backend/src/main/java/org/newsanalyzer/model/PositionHolding.java` | Modified |
| `backend/src/main/java/org/newsanalyzer/repository/PresidencyRepository.java` | Modified |
| `backend/src/main/java/org/newsanalyzer/repository/PositionHoldingRepository.java` | Modified |

## QA Results
*To be populated after QA review*
