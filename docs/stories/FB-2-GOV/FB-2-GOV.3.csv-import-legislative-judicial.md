# Story FB-2-GOV.3: CSV Import for Legislative/Judicial Branches

## Status

**Done**

## Story

**As a** NewsAnalyzer administrator,
**I want** to import government organizations from a CSV file,
**so that** I can add Legislative and Judicial branch organizations that are not available in the Federal Register API.

## Acceptance Criteria

1. **CSV Parser**: Backend service parses CSV files with the defined schema
2. **Import Endpoint**: `POST /api/government-organizations/import/csv` accepts multipart file upload
3. **Validation**: CSV import validates:
   - Required fields present (officialName, branch, orgType)
   - Branch values are valid (executive, legislative, judicial)
   - OrgType values are valid enum values
   - No duplicate acronyms within import file
4. **Error Response**: Invalid CSV returns detailed error messages with line numbers
5. **Merge Strategy**: Import uses same merge strategy as Federal Register sync (match by acronym, then name)
6. **Import Result**: Response includes counts: added, updated, skipped, errors with details
7. **Admin UI**: File upload component in admin dashboard for CSV import
8. **Sample CSV**: Documentation includes sample CSV format and downloadable template
9. **Branch Support**: Supports all three branches (executive, legislative, judicial)
10. **Audit Trail**: Imported records have `createdBy`/`updatedBy` set to "csv-import"

## Tasks / Subtasks

- [x] **Task 1: CSV Import Service** (AC: 1, 5, 10)
  - [x] Create `GovOrgCsvImportService.java` in backend services
  - [x] Implement CSV parsing using OpenCSV or Apache Commons CSV
  - [x] Implement validation logic for required fields and enum values
  - [x] Reuse merge strategy from GovernmentOrgSyncService
  - [x] Set audit fields (createdBy/updatedBy = "csv-import")
  - [x] Return ImportResult with detailed statistics

- [x] **Task 2: Import Endpoint** (AC: 2, 4, 6)
  - [x] Add `POST /api/government-organizations/import/csv` to controller
  - [x] Accept `@RequestParam("file") MultipartFile file`
  - [x] Add `@PreAuthorize("hasRole('ADMIN')")` (or stub)
  - [x] Return appropriate HTTP status (200 success, 400 validation error, 500 server error)
  - [x] Return detailed error messages with line numbers for validation failures

- [x] **Task 3: CSV Validation** (AC: 3)
  - [x] Validate CSV headers match expected schema
  - [x] Validate required fields: officialName, branch, orgType
  - [x] Validate branch enum values (case-insensitive)
  - [x] Validate orgType enum values (case-insensitive)
  - [x] Check for duplicate acronyms within file
  - [x] Validate date formats (yyyy-MM-dd) for establishedDate/dissolvedDate
  - [x] Validate URLs for websiteUrl field
  - [x] **(Architect Note)** Support parentId by acronym OR UUID for user-friendliness
  - [x] Resolve acronym references to UUIDs after all records parsed

- [x] **Task 4: Admin UI - CSV Upload** (AC: 7)
  - [x] Create `CsvImportButton.tsx` component
  - [x] Implement file input with drag-and-drop support
  - [x] Show upload progress indicator
  - [x] Display import results in dialog/toast
  - [x] Add to admin dashboard

- [x] **Task 5: Documentation & Template** (AC: 8)
  - [x] Create sample CSV file with Legislative/Judicial organizations
  - [x] Add CSV format documentation to architecture doc or README
  - [x] Include downloadable template link in admin UI

- [x] **Task 6: Unit Testing**
  - [x] Test CSV parsing with valid file
  - [x] Test validation error cases
  - [x] Test merge strategy (update existing vs create new)
  - [x] Test with malformed CSV (missing headers, bad data)

## Dev Notes

### CSV Schema

```csv
officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,dissolvedDate,websiteUrl,jurisdictionAreas
```

| Column | Required | Type | Description |
|--------|----------|------|-------------|
| `officialName` | Yes | String | Full official name |
| `acronym` | No | String | Short name/acronym |
| `branch` | Yes | Enum | executive, legislative, judicial |
| `orgType` | Yes | Enum | branch, department, independent_agency, bureau, office, commission, board |
| `orgLevel` | No | Integer | Hierarchy level (1 = top) |
| `parentId` | No | UUID or Acronym | Parent organization ID (can use acronym for convenience, resolved to UUID) |
| `establishedDate` | No | Date | Format: yyyy-MM-dd |
| `dissolvedDate` | No | Date | Format: yyyy-MM-dd |
| `websiteUrl` | No | String | Official website URL |
| `jurisdictionAreas` | No | String | Semicolon-separated list |

### Sample CSV Content

```csv
officialName,acronym,branch,orgType,orgLevel,parentId,establishedDate,websiteUrl,jurisdictionAreas
"United States Congress",,legislative,branch,1,,1789-03-04,https://congress.gov,"legislation"
"United States Senate",Senate,legislative,branch,1,,1789-03-04,https://senate.gov,"legislation;confirmation;impeachment"
"United States House of Representatives",House,legislative,branch,1,,1789-03-04,https://house.gov,"legislation;appropriations;impeachment"
"Congressional Budget Office",CBO,legislative,office,2,,1974-07-12,https://cbo.gov,"budget analysis"
"Government Accountability Office",GAO,legislative,independent_agency,2,,1921-06-10,https://gao.gov,"audit;oversight"
"Library of Congress",LOC,legislative,independent_agency,2,,1800-04-24,https://loc.gov,"library services;copyright"
"Supreme Court of the United States",SCOTUS,judicial,branch,1,,1789-03-04,https://supremecourt.gov,"constitutional law;federal appeals"
"United States Courts of Appeals",,judicial,branch,2,,1891-03-03,https://uscourts.gov/courts/court-appeals,"federal appeals"
"United States District Courts",,judicial,branch,2,,1789-09-24,https://uscourts.gov/courts/district-courts,"federal trial court"
"Administrative Office of the US Courts",AOUSC,judicial,office,2,,1939-08-07,https://uscourts.gov/about-federal-courts/administrative-office,"court administration"
```

### Validation Error Response

```json
{
  "success": false,
  "errors": [
    {
      "line": 3,
      "field": "branch",
      "value": "congress",
      "message": "Invalid branch value. Must be one of: executive, legislative, judicial"
    },
    {
      "line": 5,
      "field": "establishedDate",
      "value": "March 4, 1789",
      "message": "Invalid date format. Expected: yyyy-MM-dd"
    }
  ]
}
```

### Success Response

```json
{
  "success": true,
  "added": 8,
  "updated": 2,
  "skipped": 0,
  "errors": 0,
  "errorMessages": []
}
```

### CSV Parsing Library

Use **OpenCSV** (already common in Spring projects):

```xml
<dependency>
    <groupId>com.opencsv</groupId>
    <artifactId>opencsv</artifactId>
    <version>5.9</version>
</dependency>
```

```java
@Service
public class GovOrgCsvImportService {

    public ImportResult importFromCsv(InputStream csvStream) {
        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(csvStream))
                .withSkipLines(1) // Skip header
                .build()) {

            List<String[]> rows = reader.readAll();
            // Process rows...
        }
    }
}
```

### Source Tree Reference

```
backend/
├── src/main/java/org/newsanalyzer/
│   ├── service/
│   │   └── GovOrgCsvImportService.java (NEW)
│   ├── dto/
│   │   ├── CsvImportResult.java (NEW)
│   │   └── CsvValidationError.java (NEW)
│   └── controller/
│       └── GovernmentOrganizationController.java (MODIFY - add import endpoint)
├── pom.xml (MODIFY - add opencsv dependency if needed)
└── src/test/java/org/newsanalyzer/
    └── service/
        └── GovOrgCsvImportServiceTest.java (NEW)

frontend/
├── src/
│   ├── app/admin/
│   │   └── page.tsx (MODIFY - add CSV import section)
│   └── components/admin/
│       └── CsvImportButton.tsx (NEW)

docs/
└── data/
    └── sample-gov-orgs.csv (NEW - sample template)
```

### Testing

**Test Location**: `backend/src/test/java/org/newsanalyzer/service/`

**Test Scenarios**:
1. Valid CSV with all fields - success
2. Valid CSV with only required fields - success
3. Missing required field (officialName) - validation error
4. Invalid branch value - validation error
5. Invalid orgType value - validation error
6. Invalid date format - validation error
7. Duplicate acronym in file - validation error
8. Empty file - appropriate error
9. Malformed CSV (unbalanced quotes) - parse error
10. Update existing organization (match by acronym) - success

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-30 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-30 | 1.1 | Architect review: Added parentId by acronym support for user-friendliness | Winston (Architect) |
| 2025-11-30 | 1.2 | PO review: Approved for development. OpenCSV dependency approved. | Sarah (PO) |
| 2025-11-30 | 1.3 | Development complete. All tasks implemented, 17 unit tests passing. | James (Dev) |
| 2025-11-30 | 1.4 | QA review passed. Gate: PASS. No blocking issues. | Quinn (QA) |
| 2025-11-30 | 1.5 | PO review approved. All ACs verified. Status: Done. | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- Backend compilation: Passed
- Unit tests: 17/17 passed (GovOrgCsvImportServiceTest)
- Frontend TypeScript: Passed

### Completion Notes List
- Task 1: Created GovOrgCsvImportService.java with CSV parsing using OpenCSV, validation, and merge strategy
- Task 2: Added POST /api/government-organizations/import/csv endpoint with multipart file upload support
- Task 3: Implemented comprehensive validation for headers, required fields, enums, dates, URLs, and duplicate acronyms
- Task 4: Created CsvImportButton.tsx with drag-and-drop file input, validation error display, and success results
- Task 5: Created sample CSV template at docs/data/sample-gov-orgs.csv with Legislative and Judicial branch examples
- Task 6: Created 17 unit tests covering valid imports, validation errors, merge strategy, and edge cases

### File List
**New Files:**
- `backend/src/main/java/org/newsanalyzer/dto/CsvValidationError.java` - Validation error DTO
- `backend/src/main/java/org/newsanalyzer/dto/CsvImportResult.java` - Import result DTO
- `backend/src/main/java/org/newsanalyzer/service/GovOrgCsvImportService.java` - CSV import service
- `backend/src/test/java/org/newsanalyzer/service/GovOrgCsvImportServiceTest.java` - Unit tests
- `frontend/src/components/admin/CsvImportButton.tsx` - CSV import UI component
- `docs/data/sample-gov-orgs.csv` - Sample CSV template

**Modified Files:**
- `backend/pom.xml` - Added OpenCSV 5.9 dependency
- `backend/src/main/java/org/newsanalyzer/controller/GovernmentOrganizationController.java` - Added import endpoint
- `frontend/src/types/government-org.ts` - Added CsvValidationError and CsvImportResult types
- `frontend/src/hooks/useGovernmentOrgs.ts` - Added useGovOrgCsvImport hook
- `frontend/src/components/admin/index.ts` - Exported CsvImportButton
- `frontend/src/app/admin/page.tsx` - Added Data Import section with CsvImportButton

---

## Definition of Done Checklist

### 1. Requirements Met
- [x] All functional requirements specified in the story are implemented.
- [x] All acceptance criteria (AC 1-10) defined in the story are met.

### 2. Coding Standards & Project Structure
- [x] All new/modified code adheres to Operational Guidelines.
- [x] All new/modified code aligns with Project Structure (file locations, naming).
- [x] Adherence to Tech Stack - OpenCSV 5.9 for CSV parsing (approved in story).
- [x] Adherence to API Reference and Data Models.
- [x] Basic security best practices applied (input validation, error handling).
- [x] No new linter errors or warnings introduced by this story's code.
- [x] Code is well-commented where necessary.

### 3. Testing
- [x] All required unit tests implemented (17 tests in GovOrgCsvImportServiceTest).
- [N/A] Integration tests - covered by existing API tests when backend runs.
- [x] All tests pass successfully (17/17 passed).
- [x] Test coverage adequate for CSV import scenarios.

### 4. Functionality & Verification
- [x] Functionality verified by running unit tests covering all scenarios.
- [x] Edge cases handled (empty files, malformed CSV, duplicate acronyms, invalid enums/dates/URLs).

### 5. Story Administration
- [x] All tasks within the story file are marked as complete.
- [x] Dev Agent Record section completed with notes, agent model, and file list.
- [x] Changelog updated with development completion entry.

### 6. Dependencies, Build & Configuration
- [x] Project builds successfully without errors (backend compilation passed).
- [x] Frontend linting passes for new CsvImportButton.tsx component.
- [x] OpenCSV 5.9 dependency was pre-approved in story requirements.
- [x] New dependency recorded in backend/pom.xml with version specified.
- [x] No known security vulnerabilities in OpenCSV 5.9.
- [N/A] No new environment variables introduced.

### 7. Documentation
- [x] Sample CSV template created at docs/data/sample-gov-orgs.csv.
- [x] CSV schema documented in story Dev Notes section.
- [x] Import endpoint documented with request/response formats.

### Final Confirmation
- [x] I, the Developer Agent, confirm that all applicable items above have been addressed.

**Summary:** Story FB-2-GOV.3 implementation complete. Created full CSV import functionality for government organizations including backend service with OpenCSV parsing, REST endpoint, comprehensive validation, frontend upload component, and 17 unit tests. All acceptance criteria met.

---

## QA Results

### Review Date: 2025-11-30

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

The implementation demonstrates solid software engineering practices:

- **Backend Service** (`GovOrgCsvImportService.java`): Well-structured with clear separation of concerns. The CSV parsing, validation, and import logic are properly organized. Good use of internal classes (`CsvRow`, `ImportAction`) to manage state.
- **DTOs**: Clean and minimal with appropriate Lombok annotations. `CsvImportResult` includes helpful utility methods (`hasValidationErrors()`, `addError()`).
- **Controller**: Proper HTTP status code handling (200, 400, 500). File validation at the boundary is good practice.
- **Frontend**: React Query mutation pattern correctly implemented with cache invalidation. Drag-and-drop UX is user-friendly.
- **Tests**: Comprehensive coverage with 17 tests organized into logical nested classes.

### Refactoring Performed

No refactoring performed. The code quality is satisfactory for acceptance.

### Compliance Check

- Coding Standards: ✓ Follows project patterns (Lombok, Spring conventions)
- Project Structure: ✓ Files in correct locations per source-tree.md
- Testing Strategy: ✓ Unit tests with Mockito, proper Given-When-Then structure
- All ACs Met: ✓ All 10 acceptance criteria implemented and verified

### Improvements Checklist

- [x] CSV parsing and validation fully implemented
- [x] Merge strategy (acronym first, then name) working correctly
- [x] Parent ID resolution by acronym implemented
- [x] Audit trail with "csv-import" source
- [x] Frontend drag-and-drop file upload
- [x] Validation error display with line numbers
- [x] 17 unit tests covering all scenarios
- [ ] **FUTURE**: Add file size limit to prevent large file uploads (recommend 5MB max)
- [ ] **FUTURE**: Implement @PreAuthorize for admin endpoint (noted as stub in AC 2)
- [ ] **FUTURE**: Add integration test for end-to-end CSV import flow

### Security Review

| Finding | Severity | Status |
|---------|----------|--------|
| No file size limit on upload | Medium | Documented for future - Spring defaults provide some protection |
| @PreAuthorize not implemented | Low | Explicitly noted as "(or stub)" in AC 2 - acceptable for current phase |
| Content-type validation present | N/A | Good - validates CSV/text content types |
| No path traversal risk | N/A | Good - file content only, no file path handling |

**Conclusion**: No blocking security issues. The missing @PreAuthorize was explicitly allowed in the story AC as a stub. File size limit should be added in a future story when production auth is implemented.

### Performance Considerations

- CSV parsed in memory - acceptable for expected file sizes (<1000 rows)
- Single transaction for entire import - appropriate for data consistency
- `findAll()` called for acronym map building could be optimized for large datasets (future consideration)

### Files Modified During Review

None - no modifications required.

### Requirements Traceability

| AC | Description | Test Coverage | Status |
|----|-------------|---------------|--------|
| 1 | CSV Parser | `ValidCsvTests.importValidCsvWithAllFields` | ✓ |
| 2 | Import Endpoint | Controller multipart handling verified | ✓ |
| 3 | Validation | `ValidationErrorTests.*` (7 tests) | ✓ |
| 4 | Error Response | Validation errors include line numbers | ✓ |
| 5 | Merge Strategy | `updateExistingOrganizationByAcronym`, `skipOrganizationWhenNoChanges` | ✓ |
| 6 | Import Result | All tests verify CsvImportResult counts | ✓ |
| 7 | Admin UI | CsvImportButton.tsx with dialog/toast | ✓ |
| 8 | Sample CSV | docs/data/sample-gov-orgs.csv created | ✓ |
| 9 | Branch Support | Legislative/Judicial in sample, tests use legislative | ✓ |
| 10 | Audit Trail | Tests verify createdBy/updatedBy = "csv-import" | ✓ |

### Gate Status

Gate: **PASS** → docs/qa/gates/FB-2-GOV.3-csv-import-legislative-judicial.yml

### Recommended Status

✓ **Ready for Done**

The implementation is complete and meets all acceptance criteria. The security items noted are acceptable given the explicit stub allowance in AC 2. No blocking issues found.
