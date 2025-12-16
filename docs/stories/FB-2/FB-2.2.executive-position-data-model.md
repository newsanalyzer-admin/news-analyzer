# Story FB-2.2: Executive Position Data Model

## Status

**Ready for Development** (Architect Approved with Required Changes Incorporated)

## Story

**As a** developer,
**I want** the data model extended to support executive branch positions and appointees,
**so that** PLUM data can be stored with proper structure and relationships.

## Acceptance Criteria

1. **NEW:** Branch enum created (LEGISLATIVE, EXECUTIVE, JUDICIAL)
2. **NEW:** GovernmentPosition extended with `branch` field (required)
3. **MODIFIED:** `chamber` and `state` fields made nullable (executive positions don't have these)
4. **NEW:** Unique constraints updated with partial indexes by branch
5. GovernmentPosition entity extended with executive-specific fields (appointmentType, payPlan, payGrade, location, expirationDate)
6. New AppointmentType enum created with PLUM appointment types
7. PositionHolding entity extended with tenure field
8. DataSource enum extended with PLUM_CSV value
9. Database migrations created for schema changes (V12-V15)
10. Existing Congressional data unaffected by changes (backward compatible)
11. Repository methods added for executive position queries
12. All existing tests continue to pass

## Tasks / Subtasks

- [ ] **Task 1: Create Branch enum** *(Architect Required)*
  - [ ] Create `model/Branch.java`
  - [ ] Define values: LEGISLATIVE, EXECUTIVE, JUDICIAL
  - [ ] Add description field for each type

- [ ] **Task 2: Create AppointmentType enum**
  - [ ] Create `model/AppointmentType.java`
  - [ ] Define values: PAS, PA, NA, CA, XS
  - [ ] Add description field for each type
  - [ ] Add static fromCsvValue() method

- [ ] **Task 3: Extend GovernmentPosition entity** *(Architect Required Changes)*
  - [ ] Add `branch` field (Branch enum, required)
  - [ ] Remove `@NotNull` from `chamber` field (make nullable)
  - [ ] Remove `@NotBlank` from `state` field (make nullable)
  - [ ] Add `appointmentType` field (AppointmentType enum)
  - [ ] Add `payPlan` field (String - e.g., "EX", "ES", "GS")
  - [ ] Add `payGrade` field (String - e.g., "I", "II", "III")
  - [ ] Add `location` field (String)
  - [ ] Add `expirationDate` field (LocalDate)
  - [ ] Add JPA converter for AppointmentType

- [ ] **Task 4: Extend PositionHolding entity**
  - [ ] Add `tenure` field (Integer - tenure code from PLUM)
  - [ ] Verify existing fields support PLUM data

- [ ] **Task 5: Extend DataSource enum**
  - [ ] Add `PLUM_CSV` value
  - [ ] Update any switch statements using DataSource

- [ ] **Task 6: Create database migrations** *(Updated Sequence)*
  - [ ] V12: Add `branch` column, make `chamber`/`state` nullable
  - [ ] V13: Add `appointment_type` to government_position
  - [ ] V14: Add `pay_plan`, `pay_grade`, `location`, `expiration_date`
  - [ ] V15: Add `tenure` to position_holding
  - [ ] Update unique constraints with partial indexes
  - [ ] Test migrations on fresh database

- [ ] **Task 7: Add repository methods**
  - [ ] `findByBranch(Branch branch)`
  - [ ] `findByAppointmentType(AppointmentType type)`
  - [ ] `findByPayPlan(String payPlan)`
  - [ ] `findVacantPositions()` (no active PositionHolding)
  - [ ] `findByOrganizationIdAndBranch(UUID orgId, Branch branch)`

- [ ] **Task 8: Update existing tests**
  - [ ] Update GovernmentPositionTest for nullable fields
  - [ ] Verify PositionHoldingTest passes
  - [ ] Add tests for new fields

- [ ] **Task 9: Add new unit tests**
  - [ ] Test Branch enum methods
  - [ ] Test AppointmentType enum methods
  - [ ] Test new repository methods
  - [ ] Test JPA converter

## Dev Notes

### Branch Enum (Architect Required)

```java
package org.newsanalyzer.model;

/**
 * Represents the three branches of the US federal government.
 * Used to distinguish between Congressional, Executive, and Judicial positions.
 */
public enum Branch {
    LEGISLATIVE("Legislative Branch - Congress"),
    EXECUTIVE("Executive Branch - President and Agencies"),
    JUDICIAL("Judicial Branch - Federal Courts");

    private final String description;

    Branch(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
```

### AppointmentType Enum

```java
package org.newsanalyzer.model;

public enum AppointmentType {
    PAS("Presidential Appointment with Senate Confirmation"),
    PA("Presidential Appointment without Senate Confirmation"),
    NA("Noncareer Appointment"),
    CA("Career Appointment"),
    XS("Schedule C - Expected to change with administration");

    private final String description;

    AppointmentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static AppointmentType fromCsvValue(String value) {
        if (value == null || value.isBlank()) return null;
        return switch (value.trim().toUpperCase()) {
            case "PAS" -> PAS;
            case "PA" -> PA;
            case "NA" -> NA;
            case "CA" -> CA;
            case "XS" -> XS;
            default -> null;
        };
    }
}
```

### GovernmentPosition Extensions (Architect Required Changes)

```java
// MODIFY existing fields in GovernmentPosition.java:

// 1. ADD branch field (required for all positions)
@Enumerated(EnumType.STRING)
@Column(name = "branch", nullable = false)
@NotNull(message = "Branch is required")
private Branch branch;

// 2. MODIFY chamber field - REMOVE @NotNull (nullable for executive positions)
@Column(name = "chamber", length = 20)  // Remove nullable = false
@Enumerated(EnumType.STRING)
// @NotNull - REMOVE THIS ANNOTATION
private Person.Chamber chamber;

// 3. MODIFY state field - REMOVE @NotBlank (nullable for executive positions)
@Column(name = "state", length = 2)  // Remove nullable = false
// @NotBlank - REMOVE THIS ANNOTATION
@Size(min = 2, max = 2, message = "State must be 2-letter code if provided")
private String state;

// ADD new executive-specific fields:

@Enumerated(EnumType.STRING)
@Column(name = "appointment_type")
private AppointmentType appointmentType;

@Column(name = "pay_plan", length = 10)
private String payPlan;

@Column(name = "pay_grade", length = 10)
private String payGrade;

@Column(name = "location")
private String location;

@Column(name = "expiration_date")
private LocalDate expirationDate;
```

### PositionHolding Extension

```java
// Add to PositionHolding.java

@Column(name = "tenure")
private Integer tenure;
```

### Database Migration V12 (Architect Required - Branch Support)

```sql
-- V12__add_branch_and_nullable_fields.sql
-- CRITICAL: This migration enables executive branch positions

-- Add branch column with default for existing legislative data
ALTER TABLE government_positions
ADD COLUMN branch VARCHAR(20) DEFAULT 'LEGISLATIVE';

-- Set all existing positions to LEGISLATIVE
UPDATE government_positions SET branch = 'LEGISLATIVE';

-- Make branch required going forward
ALTER TABLE government_positions
ALTER COLUMN branch SET NOT NULL;

-- Make chamber nullable (executive positions don't have chamber)
ALTER TABLE government_positions ALTER COLUMN chamber DROP NOT NULL;

-- Make state nullable (executive positions are federal-level)
ALTER TABLE government_positions ALTER COLUMN state DROP NOT NULL;

-- Drop existing unique constraint
ALTER TABLE government_positions DROP CONSTRAINT IF EXISTS uk_position_seat;

-- Create partial index for legislative positions (chamber + state + district)
CREATE UNIQUE INDEX uk_legislative_seat
ON government_positions (chamber, state, district)
WHERE branch = 'LEGISLATIVE';

-- Create partial index for executive positions (title + organization)
CREATE UNIQUE INDEX uk_executive_position
ON government_positions (title, organization_id)
WHERE branch = 'EXECUTIVE';

COMMENT ON COLUMN government_positions.branch IS
    'Government branch: LEGISLATIVE, EXECUTIVE, or JUDICIAL';
```

### Database Migration V13

```sql
-- V13__add_appointment_type_to_position.sql

ALTER TABLE government_positions
ADD COLUMN appointment_type VARCHAR(10);

COMMENT ON COLUMN government_positions.appointment_type IS
    'PLUM appointment type: PAS, PA, NA, CA, XS';
```

### Database Migration V14

```sql
-- V14__add_executive_position_fields.sql

ALTER TABLE government_positions
ADD COLUMN pay_plan VARCHAR(10),
ADD COLUMN pay_grade VARCHAR(10),
ADD COLUMN location VARCHAR(255),
ADD COLUMN expiration_date DATE;

COMMENT ON COLUMN government_positions.pay_plan IS 'Pay plan code (EX, ES, GS, etc.)';
COMMENT ON COLUMN government_positions.pay_grade IS 'Pay grade/level (I, II, III, etc.)';
COMMENT ON COLUMN government_positions.location IS 'Work location';
COMMENT ON COLUMN government_positions.expiration_date IS 'Term expiration date for appointed positions';
```

### Database Migration V15

```sql
-- V15__add_tenure_to_position_holding.sql

ALTER TABLE position_holding
ADD COLUMN tenure INTEGER;

COMMENT ON COLUMN position_holding.tenure IS 'Tenure code from PLUM data';
```

### File Structure

```
backend/src/main/java/org/newsanalyzer/
├── model/
│   ├── Branch.java                   # NEW (Architect Required)
│   ├── AppointmentType.java          # NEW
│   ├── GovernmentPosition.java       # MODIFIED (branch, nullable chamber/state)
│   ├── PositionHolding.java          # MODIFIED
│   └── DataSource.java               # MODIFIED
└── repository/
    └── GovernmentPositionRepository.java  # MODIFIED

backend/src/main/resources/db/migration/
├── V12__add_branch_and_nullable_fields.sql   # NEW (Architect Required)
├── V13__add_appointment_type_to_position.sql
├── V14__add_executive_position_fields.sql
└── V15__add_tenure_to_position_holding.sql
```

### Backward Compatibility

All new columns are nullable to ensure:
- Existing Congressional positions continue to work
- No data migration required for existing records
- Import can proceed incrementally

## Definition of Done

- [ ] All acceptance criteria verified
- [ ] Database migrations run successfully
- [ ] Existing tests pass without modification
- [ ] New unit tests for AppointmentType enum
- [ ] Repository methods tested
- [ ] Code reviewed

## Architect Review Notes

**Reviewed by:** Winston (Architect)
**Review Date:** 2025-12-01
**Status:** APPROVED WITH REQUIRED CHANGES (incorporated above)

### Required Changes (Now Incorporated)

1. Added Branch enum (LEGISLATIVE, EXECUTIVE, JUDICIAL)
2. Added `branch` field to GovernmentPosition (required)
3. Made `chamber` and `state` nullable for executive positions
4. Updated unique constraints with partial indexes by branch
5. Revised migration sequence to V12-V15

### Rationale

The existing GovernmentPosition entity was Congressional-specific with `@NotNull` on chamber and state. Executive positions don't have chambers or states - they're federal-level appointments. The Branch enum allows the same entity to represent positions in all three branches of government while maintaining data integrity through partial unique indexes.

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-01 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-12-01 | 1.1 | Architect review: Added Branch enum, nullable fields, updated migrations | Winston (Architect) |

---

*End of Story Document*
