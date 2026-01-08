# Story ARCH-1.3: Migrate Existing Data

## Status

**Status:** Draft
**Priority:** P0 (Critical Path)
**Estimate:** 7 story points (revised from 5 per MOD-4)
**Phase:** 1-2

## Story

**As a** developer,
**I want** all existing person data migrated to the new structure,
**So that** no data is lost and all references remain valid.

## Acceptance Criteria

| # | Criterion | Status |
|---|-----------|--------|
| AC1 | All existing persons have corresponding Individual records | |
| AC2 | Duplicate detection: same name + birth date = same individual | |
| AC3 | Congressional members linked to their individual records | |
| AC4 | Non-Congressional persons (presidents, judges, appointees) have individual records | |
| AC5 | `individual_id` populated for all congressional_members | |
| AC6 | Migration is idempotent (can run multiple times safely) | |
| AC7 | Rollback script available | |
| AC8 | Data verification queries confirm no data loss | |

## Tasks / Subtasks

- [ ] **Task 1: Analyze Existing Data** (AC2)
  - [ ] Query to find potential duplicates (same name + birth date)
  - [ ] Document deduplication strategy for edge cases
  - [ ] Identify all data sources in current `persons` table

- [ ] **Task 2: Create Migration V36** (AC1, AC3, AC4, AC5)
  - [ ] Create `V36__migrate_persons_to_individuals.sql`
  - [ ] Step 1: Insert unique individuals from persons (deduplicated)
  - [ ] Step 2: Add `individual_id` column to `persons` table
  - [ ] Step 3: Populate `individual_id` by matching name + birth_date
  - [ ] Step 4: Handle persons without birth_date (match by name only, log warnings)
  - [ ] Step 5: Verify all rows have `individual_id` populated

- [ ] **Task 3: Handle Non-Congressional Persons** (AC4)
  - [ ] Identify persons from Presidency table (presidents)
  - [ ] Identify persons from PositionHolding table (VPs, judges, appointees)
  - [ ] Create Individual records for any not already in persons table
  - [ ] Create mapping table if needed for transition

- [ ] **Task 4: Ensure Idempotency** (AC6)
  - [ ] Use `INSERT ... ON CONFLICT DO NOTHING` or equivalent
  - [ ] Add checks before each step
  - [ ] Handle partial migration state gracefully

- [ ] **Task 5: Create Rollback Script** (AC7)
  - [ ] Create `V36_rollback__undo_migrate_persons_to_individuals.sql`
  - [ ] Document rollback procedure
  - [ ] Test rollback in dev environment

- [ ] **Task 6: Data Verification** (AC8)
  - [ ] Create verification queries
  - [ ] Count comparison: persons vs individuals
  - [ ] Verify all FKs point to valid individuals
  - [ ] Verify no orphaned records
  - [ ] Create verification report

- [ ] **Task 7: Optional Java Service** (complex cases)
  - [ ] Create `DataMigrationService.java` if SQL alone is insufficient
  - [ ] Handle edge cases programmatically
  - [ ] Add logging for audit trail

## Dev Notes

### Source Tree Reference

```
backend/src/main/resources/db/migration/
├── V34__create_individuals_table.sql     # Creates empty table
├── V36__migrate_persons_to_individuals.sql # THIS STORY
└── V36_rollback__*.sql                   # Rollback script

backend/src/main/java/org/newsanalyzer/service/
└── DataMigrationService.java             # Optional, if needed
```

### Migration SQL Approach

```sql
-- Step 1: Insert unique individuals (deduplicated by name + birth_date)
INSERT INTO individuals (
    id, first_name, last_name, middle_name, suffix,
    birth_date, death_date, birth_place, gender, image_url,
    party, external_ids, social_media, primary_data_source,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
    first_name, last_name, middle_name, suffix,
    birth_date, death_date, birth_place, gender, image_url,
    party, external_ids, social_media, data_source,
    NOW(), NOW()
FROM (
    SELECT DISTINCT ON (LOWER(first_name), LOWER(last_name), COALESCE(birth_date, '1900-01-01'))
        *
    FROM persons
    ORDER BY LOWER(first_name), LOWER(last_name), COALESCE(birth_date, '1900-01-01'), created_at
) deduped
ON CONFLICT DO NOTHING;

-- Step 2: Add individual_id column to persons
ALTER TABLE persons ADD COLUMN IF NOT EXISTS individual_id UUID;

-- Step 3: Populate individual_id
UPDATE persons p
SET individual_id = i.id
FROM individuals i
WHERE LOWER(p.first_name) = LOWER(i.first_name)
  AND LOWER(p.last_name) = LOWER(i.last_name)
  AND (p.birth_date = i.birth_date OR (p.birth_date IS NULL AND i.birth_date IS NULL));

-- Step 4: Verify no nulls remain
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM persons WHERE individual_id IS NULL) THEN
        RAISE EXCEPTION 'Migration incomplete: some persons have NULL individual_id';
    END IF;
END $$;
```

### Deduplication Strategy

| Scenario | Strategy |
|----------|----------|
| Same name, same DOB | Single Individual record |
| Same name, different DOB | Separate Individual records |
| Same name, one null DOB | Match to existing if unique, else create separate |
| Truly different people, same name+DOB | Use `external_ids` to distinguish (MOD-7) |

### Data Sources to Handle

| Source | Table | Notes |
|--------|-------|-------|
| Congress.gov | persons (bioguide_id not null) | Congressional members |
| White House Historical | persons (via Presidency) | Presidents |
| FJC | position_holdings | Federal judges |
| PLUM | position_holdings | Executive appointees |

### Verification Queries

```sql
-- Count comparison
SELECT
    (SELECT COUNT(*) FROM persons) as persons_count,
    (SELECT COUNT(*) FROM individuals) as individuals_count,
    (SELECT COUNT(DISTINCT individual_id) FROM persons) as linked_count;

-- Orphan check
SELECT * FROM persons WHERE individual_id IS NULL;

-- Duplicate check in individuals
SELECT first_name, last_name, birth_date, COUNT(*)
FROM individuals
GROUP BY first_name, last_name, birth_date
HAVING COUNT(*) > 1;
```

### Testing

**Test Requirements:**
- Test with sample data before production migration
- Verify deduplication logic
- Test rollback procedure
- Run verification queries

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
