# Story STAB-1.6: Harden CSV Import Services

## Status

**Done**

---

## Story

**As a** system,
**I want** all CSV import services to validate headers, handle null fields safely, and use configurable URLs,
**so that** imports are resilient to upstream CSV format changes and do not fail on edge-case data.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `FjcCsvImportService` validates CSV headers before processing — rejects files with missing required columns |
| AC2 | `PlumCsvImportService` URL is configurable and points to a current, valid OPM source |
| AC3 | Null field access is safe across all CSV record DTOs — no `NullPointerException` on `.trim()`, `.isBlank()`, or `.contains()` calls |
| AC4 | `GovOrgCsvImportService` uses per-row error recovery instead of rolling back the entire import on one bad row |
| AC5 | `PlumCsvImportService` validates that expected CSV headers are present before parsing |
| AC6 | Date parsing failures are logged at WARN level (not trace/debug) across all CSV services |
| AC7 | Existing tests pass; new tests cover missing headers, null fields, and partial failure scenarios |

---

## Tasks / Subtasks

- [ ] **Task 1: Add header validation to FjcCsvImportService** (AC1)
  - [ ] After reading CSV header row, verify required columns exist: `First Name`, `Last Name`, `Court Type`, `Court Name`, `Appointment Date` (verify actual column names from FJC CSV)
  - [ ] Return descriptive error in `FjcImportResult` if required headers are missing
  - [ ] Do not proceed with import if validation fails

- [ ] **Task 2: Add header validation to PlumCsvImportService** (AC5)
  - [ ] After CSV reader is created, validate that `@CsvBindByName` target columns exist in the CSV header
  - [ ] Return error if expected columns like `Agency Name`, `Position Title`, `Incumbent First Name` are missing

- [ ] **Task 3: Update PLUM CSV URL** (AC2)
  - [ ] Verify current OPM PLUM URL is still valid (the Biden archive URL may be stale)
  - [ ] Update the default URL in `@Value` annotation or `application-dev.yml`
  - [ ] Ensure URL remains configurable via `plum.csv.url` property

- [ ] **Task 4: Fix null safety in PlumCsvRecord** (AC3)
  - [ ] Add null guard before `.trim()` on `incumbentFirstName` (service line ~466)
  - [ ] Review all `.isBlank()` calls in `hasIncumbent()` — add null checks before blank checks
  - [ ] Review `getPayPlanCode()` and `getTenureCode()` for null safety

- [ ] **Task 5: Fix null safety in FjcJudgeCsvRecord** (AC3)
  - [ ] Add null checks in `getFullName()` before `.isBlank()` calls on `firstName`, `middleName`, `lastName`
  - [ ] Add null check in `isArticleIIIJudge()` before `.contains()` on `courtType1`
  - [ ] Fix `parseVoteCount()` — validate string format before splitting on "-"
  - [ ] Add null check in `getBirthDateString()` date field concatenation

- [ ] **Task 6: Add per-row error recovery to GovOrgCsvImportService** (AC4)
  - [ ] Wrap individual row processing in try-catch
  - [ ] On row failure, log the error with row number and continue to next row
  - [ ] Track failed rows in `CsvImportResult`
  - [ ] Do not let one bad row roll back the entire transaction

- [ ] **Task 7: Elevate date parsing log level** (AC6)
  - [ ] In `PlumCsvImportService` date parsing fallback (line ~563): change from returning null silently to logging at WARN
  - [ ] In `FjcCsvImportService` date parsing (line ~469): change trace-level to WARN
  - [ ] In `GovOrgCsvImportService` date parsing: verify log level is adequate

- [ ] **Task 8: Update tests** (AC7)
  - [ ] Add test for FJC import with missing CSV headers
  - [ ] Add test for PLUM import with null name fields in CSV
  - [ ] Add test for GovOrg import where one row has invalid data (verify others still import)
  - [ ] Verify all existing CSV import tests pass

---

## Dev Notes

### PLUM CSV URL
Current default: `https://www.opm.gov/about-us/open-government/plum-reporting/plum-archive/plum-archive-biden-administration.csv`
This is an archived URL. Check OPM website for the current PLUM CSV download URL.

### FJC CSV Structure
FJC CSV has 288 columns — no header validation means any schema change silently produces null values. Required columns must be verified before processing.

### Null Safety Examples
PlumCsvRecord — unsafe:
```java
incumbentFirstName.trim() // NPE if null
```

FjcJudgeCsvRecord — unsafe:
```java
if (!firstName.isBlank()) // NPE if null
courtType1.contains("Article III") // NPE if null
```

### GovOrgCsvImportService Transaction Issue
Currently `@Transactional` on the service class — one bad row rolls back everything. Fix: either remove class-level `@Transactional` and use `TransactionTemplate` per row, or add try-catch within the processing loop.

### Key Files
| File | Path | Change |
|------|------|--------|
| FjcCsvImportService | `backend/src/main/java/org/newsanalyzer/service/FjcCsvImportService.java` | Header validation |
| PlumCsvImportService | `backend/src/main/java/org/newsanalyzer/service/PlumCsvImportService.java` | Header validation, URL update |
| GovOrgCsvImportService | `backend/src/main/java/org/newsanalyzer/service/GovOrgCsvImportService.java` | Per-row error recovery |
| PlumCsvRecord | `backend/src/main/java/org/newsanalyzer/dto/PlumCsvRecord.java` | Null safety |
| FjcJudgeCsvRecord | `backend/src/main/java/org/newsanalyzer/dto/FjcJudgeCsvRecord.java` | Null safety |
| application-dev.yml | `backend/src/main/resources/application-dev.yml` | Update PLUM URL if needed |

### Testing

- **Unit tests**: `PlumCsvImportServiceTest`, `FjcCsvImportServiceTest`, `GovOrgCsvImportServiceTest`
- **Test resources**: `backend/src/test/resources/csv/`
- **Framework**: JUnit 5 + Mockito
- **Run**: `cd backend && ./mvnw test -Dtest="*CsvImportServiceTest"`
- **Coverage**: JaCoCo 70% enforced

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (H4, H5, M4, M5, M6) | Sarah (PO) |
