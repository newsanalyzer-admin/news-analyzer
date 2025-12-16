# Story ADMIN-1.4: GOVMAN XML Parser Service

## Status

**Done**

---

## Story

**As a** system,
**I want** to parse Government Manual XML files,
**so that** administrators can import official government organizational structure.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | GovmanXmlImportService parses GOVMAN XML using JAXB with annotated classes for type safety |
| AC2 | Parser extracts: EntityId, ParentId, SortOrder, EntityType, Category, AgencyName |
| AC3 | Parser extracts: MissionStatement (concatenated paragraphs), WebAddress |
| AC4 | Parser builds parent-child relationships from ParentId references |
| AC5 | Service maps Category to Branch enum (Legislative, Executive, Judicial) |
| AC6 | Service validates records before import (required fields, valid parent references) |
| AC7 | Service returns ImportResult with counts: total, imported, skipped, errors |
| AC8 | Import sets import_source='GOVMAN'; existing created_by/created_at fields capture audit info |
| AC9 | Duplicate detection identifies existing records by name or external_id |
| AC10 | Full GOVMAN file (~5MB) parses within 60 seconds |

---

## Integration Verification

| # | Verification |
|---|--------------|
| IV1 | Imported organizations appear in existing Government Organizations API |
| IV2 | Parent-child relationships queryable via existing hierarchy endpoints |
| IV3 | Existing manually-created organizations are not overwritten |
| IV4 | Database constraints are not violated during import |

---

## Tasks / Subtasks

- [x] **Task 1: Database Migration for import_source** (AC8)
  - [x] Create `V21__add_import_source_column.sql` migration
  - [x] Add `import_source VARCHAR(50)` column to `government_organizations` table
  - [x] Column should be nullable (existing records will have NULL)
  - [x] Add index on import_source for filtering

- [x] **Task 2: Create JAXB Model Classes** (AC1, AC2, AC3)
  - [x] Create `dto/govman/GovmanDocument.java` - Root element class
  - [x] Create `dto/govman/GovmanEntity.java` - Entity element class with fields:
    - EntityId (String)
    - ParentId (String, nullable)
    - SortOrder (Integer)
    - EntityType (String)
    - Category (String)
    - AgencyName (String)
    - MissionStatement (List<String> for paragraphs)
    - WebAddress (String, nullable)
  - [x] Add proper JAXB annotations (@XmlRootElement, @XmlElement, @XmlAttribute)
  - [x] Handle MissionStatement paragraph concatenation in getter

- [x] **Task 3: Create GovmanXmlImportService** (AC1, AC4, AC5, AC6, AC7, AC9)
  - [x] Create `service/GovmanXmlImportService.java`
  - [x] Implement `parseXml(InputStream xmlStream)` method using JAXB Unmarshaller
  - [x] Implement `mapCategoryToBranch(String category)` method:
    - "Legislative Branch" → Branch.LEGISLATIVE
    - "Executive Branch" → Branch.EXECUTIVE
    - "Judicial Branch" → Branch.JUDICIAL
    - Unknown → default to EXECUTIVE with warning log
  - [x] Implement `buildParentChildRelationships(List<GovmanEntity>)` method
  - [x] Implement `validateEntity(GovmanEntity)` method checking required fields
  - [x] Implement `detectDuplicate(GovmanEntity)` using name and external_id matching
  - [x] Implement `importEntities(List<GovmanEntity>)` returning ImportResult

- [x] **Task 4: Create ImportResult DTO** (AC7)
  - [x] Create `dto/GovmanImportResult.java` with fields:
    - total (int) - Total entities parsed
    - imported (int) - Successfully imported
    - updated (int) - Updated existing records
    - skipped (int) - Skipped duplicates
    - errors (int) - Failed validations
    - errorDetails (List<String>) - Specific error messages

- [x] **Task 5: Create Admin Import Controller Endpoint** (AC7)
  - [x] Create `controller/GovmanImportController.java`
  - [x] Implement `POST /api/admin/import/govman` endpoint
  - [x] Accept MultipartFile for XML upload
  - [x] Return GovmanImportResult as JSON response
  - [x] Add appropriate error handling and HTTP status codes

- [x] **Task 6: Update GovernmentOrganization Entity** (AC8)
  - [x] Add `importSource` field to `GovernmentOrganization.java` model
  - [x] Map to `import_source` column
  - [x] Update repository if needed for import_source queries

- [x] **Task 7: Unit Tests** (AC1-AC10)
  - [x] Create `test/service/GovmanXmlImportServiceTest.java`
  - [x] Test XML parsing with sample GOVMAN excerpt
  - [x] Test category to branch mapping
  - [x] Test parent-child relationship building
  - [x] Test validation (missing required fields)
  - [x] Test duplicate detection logic
  - [x] Test import result counting

- [x] **Task 8: Integration Tests** (IV1-IV4)
  - [x] Create `test/controller/GovmanImportControllerTest.java`
  - [x] Test endpoint accepts XML file upload
  - [x] Test imported records appear in GovernmentOrganization repository
  - [x] Test parent-child relationships are correctly stored
  - [x] Test existing records are not overwritten

- [ ] **Task 9: Performance Verification** (AC10)
  - [ ] Test with full GOVMAN file (~5MB)
  - [ ] Verify parsing completes within 60 seconds
  - [ ] Document actual performance metrics

---

## Dev Notes

### Source Tree - Relevant Files

**Existing Backend Files:**
```
backend/src/main/java/org/newsanalyzer/
├── controller/
│   └── GovernmentOrganizationController.java  # Existing gov org endpoints
├── dto/
│   ├── CsvImportResult.java                   # Pattern for ImportResult
│   └── PlumImportResult.java                  # Another ImportResult pattern
├── model/
│   ├── GovernmentOrganization.java            # Entity to update
│   └── Branch.java                            # Existing enum (LEGISLATIVE, EXECUTIVE, JUDICIAL)
├── repository/
│   └── GovernmentOrganizationRepository.java  # Existing repository
├── service/
│   ├── GovOrgCsvImportService.java            # Pattern for import service
│   └── PlumCsvImportService.java              # Another import pattern
└── resources/db/migration/
    └── V20__add_federal_register_agency_id.sql  # Last migration
```

**Files to Create:**
```
backend/src/main/java/org/newsanalyzer/
├── controller/
│   └── GovmanImportController.java            # NEW: Import endpoint
├── dto/
│   ├── GovmanImportResult.java                # NEW: Import result DTO
│   └── govman/
│       ├── GovmanDocument.java                # NEW: JAXB root element
│       └── GovmanEntity.java                  # NEW: JAXB entity element
├── service/
│   └── GovmanXmlImportService.java            # NEW: XML parser + importer
└── resources/db/migration/
    └── V21__add_import_source_column.sql      # NEW: Migration

backend/src/test/java/org/newsanalyzer/
├── controller/
│   └── GovmanImportControllerTest.java        # NEW: Integration tests
└── service/
    └── GovmanXmlImportServiceTest.java        # NEW: Unit tests
```

### Tech Stack Reference

| Technology | Version | Usage |
|------------|---------|-------|
| Java | 17 LTS | Backend language |
| Spring Boot | 3.2.2 | REST API framework |
| Spring Data JPA | (managed) | Database access |
| JAXB | (Java SE) | XML parsing with annotations |
| PostgreSQL | 15+ | Database |
| Flyway | (managed) | Schema migrations |
| JUnit 5 | (managed) | Testing framework |
| Mockito | (managed) | Mocking framework |

### GOVMAN XML Structure

The Government Manual XML follows this structure:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<GovernmentManual>
  <Entity>
    <EntityId>1</EntityId>
    <ParentId></ParentId>
    <SortOrder>1</SortOrder>
    <EntityType>Branch</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>United States Congress</AgencyName>
    <MissionStatement>
      <Para>The Congress of the United States...</Para>
      <Para>Additional paragraph...</Para>
    </MissionStatement>
    <WebAddress>https://www.congress.gov</WebAddress>
  </Entity>
  <Entity>
    <EntityId>2</EntityId>
    <ParentId>1</ParentId>
    <SortOrder>1</SortOrder>
    <EntityType>Agency</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>Senate</AgencyName>
    <!-- ... -->
  </Entity>
  <!-- More entities... -->
</GovernmentManual>
```

### JAXB Annotation Patterns

```java
@XmlRootElement(name = "GovernmentManual")
@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanDocument {
    @XmlElement(name = "Entity")
    private List<GovmanEntity> entities;
}

@XmlAccessorType(XmlAccessType.FIELD)
public class GovmanEntity {
    @XmlElement(name = "EntityId")
    private String entityId;

    @XmlElement(name = "ParentId")
    private String parentId;

    @XmlElement(name = "Category")
    private String category;

    @XmlElement(name = "AgencyName")
    private String agencyName;

    @XmlElementWrapper(name = "MissionStatement")
    @XmlElement(name = "Para")
    private List<String> missionParagraphs;

    // Concatenate paragraphs
    public String getMissionStatement() {
        if (missionParagraphs == null) return null;
        return String.join("\n\n", missionParagraphs);
    }
}
```

### Existing Import Service Patterns

From `PlumCsvImportService.java`:
```java
@Service
@Transactional
public class PlumCsvImportService {
    public PlumImportResult importFromCsv(InputStream csvStream) {
        PlumImportResult result = new PlumImportResult();
        // Parse records
        // Validate each record
        // Check for duplicates
        // Import valid records
        // Track counts in result
        return result;
    }
}
```

### Branch Enum Mapping

The existing `Branch.java` enum:
```java
public enum Branch {
    LEGISLATIVE,
    EXECUTIVE,
    JUDICIAL
}
```

Category mapping logic:
```java
private Branch mapCategoryToBranch(String category) {
    if (category == null) return Branch.EXECUTIVE;
    return switch (category.toLowerCase()) {
        case "legislative branch" -> Branch.LEGISLATIVE;
        case "judicial branch" -> Branch.JUDICIAL;
        default -> Branch.EXECUTIVE;  // Executive is default
    };
}
```

### Duplicate Detection Strategy

Match on either:
1. **external_id** - If entity already has GOVMAN EntityId stored
2. **name** - Fuzzy match on organization name (case-insensitive, trimmed)

```java
private Optional<GovernmentOrganization> findDuplicate(GovmanEntity entity) {
    // First try exact external_id match
    Optional<GovernmentOrganization> byExternalId =
        repository.findByExternalId("GOVMAN:" + entity.getEntityId());
    if (byExternalId.isPresent()) return byExternalId;

    // Fall back to name match
    return repository.findByNameIgnoreCase(entity.getAgencyName().trim());
}
```

### Configuration

Add to `application.yml`:
```yaml
newsanalyzer:
  import:
    govman-path: ${GOVMAN_XML_PATH:data/GOVINFO/GOVMAN-2025-01-13.xml}
    batch-size: 100
```

### Error Handling

Controller should return:
- 200 OK with ImportResult for successful import (even if some records failed validation)
- 400 Bad Request for invalid XML format
- 413 Payload Too Large if file exceeds limit
- 500 Internal Server Error for unexpected failures

---

## Testing

### Testing Standards

| Aspect | Requirement |
|--------|-------------|
| Test Location | `backend/src/test/java/org/newsanalyzer/` |
| Framework | JUnit 5 + Mockito |
| Database Tests | Use @DataJpaTest with H2 in-memory (existing pattern) |
| API Tests | Use @WebMvcTest or @SpringBootTest |
| Coverage | Aim for 80%+ on new service code |

### Unit Test Cases

**GovmanXmlImportServiceTest.java:**

1. `testParseXml_validDocument_returnsEntities()`
   - Given: Valid XML with 3 entities
   - When: parseXml() called
   - Then: Returns list of 3 GovmanEntity objects

2. `testParseXml_emptyDocument_returnsEmptyList()`
   - Given: XML with no entities
   - When: parseXml() called
   - Then: Returns empty list

3. `testMapCategoryToBranch_legislativeBranch_returnsLegislative()`
   - Given: Category = "Legislative Branch"
   - When: mapCategoryToBranch() called
   - Then: Returns Branch.LEGISLATIVE

4. `testMapCategoryToBranch_unknownCategory_returnsExecutive()`
   - Given: Category = "Unknown"
   - When: mapCategoryToBranch() called
   - Then: Returns Branch.EXECUTIVE (default)

5. `testValidateEntity_missingAgencyName_returnsFalse()`
   - Given: Entity with null AgencyName
   - When: validateEntity() called
   - Then: Returns false

6. `testBuildParentChildRelationships_validParentIds_buildsTree()`
   - Given: Entities with valid ParentId references
   - When: buildParentChildRelationships() called
   - Then: Parent references are resolved

7. `testDetectDuplicate_existingExternalId_returnsExisting()`
   - Given: Entity with EntityId matching existing external_id
   - When: detectDuplicate() called
   - Then: Returns existing GovernmentOrganization

8. `testImportEntities_mixedResults_returnsAccurateCounts()`
   - Given: 5 entities (2 valid, 1 duplicate, 2 invalid)
   - When: importEntities() called
   - Then: Result shows imported=2, skipped=1, errors=2

### Integration Test Cases

**GovmanImportControllerTest.java:**

1. `testImportGovman_validXml_returns200WithResult()`
   - POST multipart file with valid XML
   - Expect 200 OK with ImportResult JSON

2. `testImportGovman_invalidXml_returns400()`
   - POST malformed XML
   - Expect 400 Bad Request

3. `testImportGovman_importedRecordsQueryable()`
   - Import valid XML
   - Query GovernmentOrganization API
   - Verify imported records are returned

4. `testImportGovman_existingRecordsNotOverwritten()`
   - Create existing org with same name
   - Import XML with that entity
   - Verify original record preserved

### Sample Test XML

Create `src/test/resources/govman-test.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<GovernmentManual>
  <Entity>
    <EntityId>TEST-1</EntityId>
    <ParentId></ParentId>
    <SortOrder>1</SortOrder>
    <EntityType>Branch</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>Test Congress</AgencyName>
    <MissionStatement>
      <Para>Test mission statement.</Para>
    </MissionStatement>
    <WebAddress>https://test.gov</WebAddress>
  </Entity>
  <Entity>
    <EntityId>TEST-2</EntityId>
    <ParentId>TEST-1</ParentId>
    <SortOrder>1</SortOrder>
    <EntityType>Agency</EntityType>
    <Category>Legislative Branch</Category>
    <AgencyName>Test Senate</AgencyName>
  </Entity>
</GovernmentManual>
```

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-04 | 1.0 | Initial story creation from PRD | Sarah (PO) |
| 2025-12-04 | 1.1 | Story approved for development | Sarah (PO) |
| 2025-12-04 | 2.0 | Implementation complete (Tasks 1-8), ready for review | James (Dev Agent) |

---

## Dev Agent Record

### Agent Model Used

claude-opus-4-5-20251101

### Debug Log References

No debug log entries required - all tests pass.

### Completion Notes List

- Tasks 1-8 completed successfully
- Task 9 (Performance Verification) pending - requires actual GOVMAN XML file (~5MB) for testing
- All 28 unit and integration tests pass
- Implementation follows existing patterns from PlumCsvImportService
- Uses JAXB for type-safe XML parsing
- Supports parent-child relationship resolution in two passes
- Duplicate detection by external ID and name matching
- Preserves manually-created records (different import_source)

### File List

**New Files Created:**
- `backend/src/main/resources/db/migration/V21__add_import_source_column.sql`
- `backend/src/main/java/org/newsanalyzer/dto/govman/GovmanDocument.java`
- `backend/src/main/java/org/newsanalyzer/dto/govman/GovmanEntity.java`
- `backend/src/main/java/org/newsanalyzer/dto/GovmanImportResult.java`
- `backend/src/main/java/org/newsanalyzer/service/GovmanXmlImportService.java`
- `backend/src/main/java/org/newsanalyzer/controller/GovmanImportController.java`
- `backend/src/test/java/org/newsanalyzer/service/GovmanXmlImportServiceTest.java`
- `backend/src/test/java/org/newsanalyzer/controller/GovmanImportControllerTest.java`
- `backend/src/test/resources/govman-test.xml`

**Modified Files:**
- `backend/src/main/java/org/newsanalyzer/model/GovernmentOrganization.java` - Added importSource field
- `backend/src/main/java/org/newsanalyzer/repository/GovernmentOrganizationRepository.java` - Added findByImportSource and findByGovinfoExternalId methods

---

## QA Results

### Review Date: 2025-12-05

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: GOOD** - The implementation is well-structured, follows existing patterns in the codebase, and demonstrates solid software engineering practices. The code is clean, readable, and properly documented with Javadoc.

**Strengths:**
- Follows established import service patterns (PlumCsvImportService)
- Clean separation of concerns (DTO, Service, Controller, Repository)
- Comprehensive test coverage with 28 tests (19 unit, 9 integration)
- Good use of JAXB annotations for type-safe XML parsing
- Two-pass import algorithm handles parent-child relationships correctly
- Proper duplicate detection with external ID and name fallback
- OpenAPI/Swagger documentation on controller endpoints
- Defensive programming with null checks and validation

**Minor Observations:**
- Controller uses in-memory state (`importInProgress`, `lastResult`) which won't survive restarts - acceptable for MVP but should be noted
- JAXBContext is lazily initialized but not thread-safe in the null check pattern (line 50-52) - low risk since context is thread-safe once created
- Task 9 (Performance Verification) remains incomplete pending actual GOVMAN file

### Refactoring Performed

None - code quality is sufficient for this iteration.

### Compliance Check

- Coding Standards: ✓ Follows Java/Spring Boot conventions
- Project Structure: ✓ Files in correct locations under dto/, service/, controller/
- Testing Strategy: ✓ Unit + integration tests with Given-When-Then pattern
- All ACs Met: ✓ AC1-AC9 fully implemented; AC10 pending verification

### Requirements Traceability

| AC | Criterion | Test Coverage |
|----|-----------|---------------|
| AC1 | JAXB parsing with annotated classes | `XmlParsingTests` (3 tests) ✓ |
| AC2 | Extract EntityId, ParentId, SortOrder, EntityType, Category, AgencyName | `testParseXml_validDocument_returnsEntities` ✓ |
| AC3 | Extract MissionStatement (concatenated), WebAddress | `testParseXml_multipleParagraphs_concatenatesMissionStatement` ✓ |
| AC4 | Build parent-child relationships | `ParentChildTests`, `resolveParentReferences()` ✓ |
| AC5 | Map Category to Branch enum | `CategoryMappingTests` (6 tests) ✓ |
| AC6 | Validate records before import | `ValidationTests` (3 tests) ✓ |
| AC7 | Return ImportResult with counts | `ImportResultTests` (2 tests) ✓ |
| AC8 | Set import_source='GOVMAN' | `testImport_validEntity_createsOrganization` ✓ |
| AC9 | Duplicate detection by name/external_id | `DuplicateDetectionTests` (3 tests) ✓ |
| AC10 | Parse ~5MB file within 60s | **Not verified** - requires actual file |

### Improvements Checklist

- [x] Database migration with proper indexing
- [x] JAXB model classes with correct annotations
- [x] Import service with two-pass algorithm
- [x] Controller with file validation and status endpoints
- [x] Unit tests covering all mapping and validation logic
- [x] Integration tests for controller endpoints
- [ ] Thread-safe JAXBContext initialization (minor - consider using @PostConstruct)
- [ ] Persistent import status storage (future improvement)
- [ ] AC10 Performance verification with real GOVMAN file

### Security Review

- ✓ File size validation (10MB limit)
- ✓ File type validation (.xml extension)
- ✓ No SQL injection risk (uses JPA parameterized queries)
- ✓ No XXE risk - JAXB is not vulnerable by default to external entity expansion
- Note: Admin endpoint should be secured with role-based access (documented in controller comments)

### Performance Considerations

- Import runs in single transaction - may need batching for very large files
- Pre-loading existing organizations by source reduces N+1 queries
- Parent resolution in second pass is efficient (single lookup per entity with parent)
- AC10 (60-second performance target) not verified - awaiting actual file

### Files Modified During Review

None - no refactoring performed.

### Gate Status

**Gate: PASS** → `docs/qa/gates/ADMIN-1.4-govman-xml-parser-service.yml`

### Recommended Status

✓ **Ready for Done** - All implemented acceptance criteria verified. Task 9 (Performance) can be tracked separately or completed before release with actual GOVMAN file.
