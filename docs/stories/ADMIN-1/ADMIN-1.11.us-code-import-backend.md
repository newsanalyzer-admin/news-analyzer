# Story ADMIN-1.11: US Code Import - Backend Implementation

## Status

**Done**

---

## Story

**As an** administrator,
**I want** to import US Code data from uscode.house.gov,
**so that** the factbase includes federal statutory law references.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Implementation follows spike recommendations from ADMIN-1.10 (bulk XML import, new `Statute` entity) |
| AC2 | New `Statute` JPA entity created with fields: usc_identifier, title_number, title_name, chapter_number, chapter_name, section_number, heading, content_text, source_credit, release_point |
| AC3 | Flyway migration creates `statutes` table with appropriate indexes and full-text search |
| AC4 | USLM XML parser implemented using StAX streaming for memory efficiency |
| AC5 | Import service downloads XML from uscode.house.gov and upserts sections to database |
| AC6 | Admin sync endpoint `POST /api/admin/sync/statutes` triggers import with progress feedback |
| AC7 | REST API endpoints: `GET /api/statutes` (list/search), `GET /api/statutes/{id}` (detail) |
| AC8 | Import sets `import_source='USCODE'` on records |

---

## Integration Verification

| # | Verification |
|---|--------------|
| IV1 | US Code data accessible via `/api/statutes` endpoint |
| IV2 | No impact on existing Federal Register / Regulation functionality |
| IV3 | Import can be re-run safely (upsert behavior on usc_identifier) |

---

## Tasks / Subtasks

- [x] **Task 1: Create Statute Entity and Repository** (AC1, AC2)
  - [x] Create `Statute.java` JPA entity in `backend/src/main/java/org/newsanalyzer/model/`
  - [x] Fields: id (UUID), uscIdentifier, titleNumber, titleName, chapterNumber, chapterName, sectionNumber, heading, contentText, contentXml, sourceCredit, sourceUrl, releasePoint, effectiveDate, createdAt, updatedAt
  - [x] Add `@Table` indexes for title_number, chapter, section, usc_identifier
  - [x] Create `StatuteRepository.java` with custom query methods: findByUscIdentifier, findByTitleNumber, searchByContentText

- [x] **Task 2: Create Database Migration** (AC3)
  - [x] Create `V22__create_statutes_table.sql` in `backend/src/main/resources/db/migration/`
  - [x] Include columns matching Statute entity
  - [x] Add indexes: idx_statutes_title, idx_statutes_chapter, idx_statutes_section, idx_statutes_usc_identifier (unique)
  - [x] Add full-text search index using gin(to_tsvector('english', content_text))

- [x] **Task 3: Implement USLM XML Parser** (AC4)
  - [x] Create `UslmXmlParser.java` in `backend/src/main/java/org/newsanalyzer/service/`
  - [x] Use StAX (XMLStreamReader) for streaming parse of large files
  - [x] Parse `<section>` elements with identifier, num, heading, content, sourceCredit
  - [x] Extract title/chapter context from parent hierarchy
  - [x] Handle edge cases: nested content, cross-references, notes

- [x] **Task 4: Implement Download Service** (AC5)
  - [x] Create `UsCodeDownloadService.java` in `backend/src/main/java/org/newsanalyzer/service/`
  - [x] Download ZIP from https://uscode.house.gov/download/download.shtml
  - [x] Support downloading individual titles or all titles
  - [x] Extract XML files from ZIP
  - [x] Return InputStream for parser consumption

- [x] **Task 5: Implement Import Service** (AC5, AC8)
  - [x] Create `UsCodeImportService.java` in `backend/src/main/java/org/newsanalyzer/service/`
  - [x] Orchestrate download → parse → save workflow
  - [x] Use batch inserts (100 records) for performance
  - [x] Implement upsert logic on usc_identifier
  - [x] Set import_source='USCODE' on all records
  - [x] Track and return import statistics (added, updated, errors)

- [x] **Task 6: Create Admin Sync Controller** (AC6)
  - [x] Add endpoint to `AdminSyncController.java` or create new controller
  - [x] `POST /api/admin/sync/statutes` - triggers full import
  - [x] `POST /api/admin/sync/statutes/{titleNumber}` - import single title
  - [x] Return import result with counts and timing
  - [x] Consider async execution for large imports

- [x] **Task 7: Create Statute REST API** (AC7)
  - [x] Create `StatuteController.java` in `backend/src/main/java/org/newsanalyzer/controller/`
  - [x] `GET /api/statutes` - paginated list with filters (titleNumber, search query)
  - [x] `GET /api/statutes/{id}` - single statute by UUID
  - [x] `GET /api/statutes/by-citation/{uscIdentifier}` - lookup by USC citation
  - [x] Create `StatuteDTO.java` for API responses
  - [x] Add Swagger/OpenAPI documentation

- [x] **Task 8: Create Unit Tests** (AC1-AC8)
  - [x] Create `UslmXmlParserTest.java` with sample XML parsing tests
  - [x] Create `UsCodeImportServiceTest.java` with mock download/parse tests
  - [x] Create `StatuteControllerTest.java` with endpoint tests
  - [x] Create `StatuteRepositoryTest.java` with repository query tests
  - [x] Use sample XML snippets from spike findings

---

## Dev Notes

### Spike Reference

**CRITICAL:** All implementation decisions are documented in `docs/research/US_CODE_SPIKE_FINDINGS.md` from ADMIN-1.10. Key findings:

1. **Data Source:** uscode.house.gov (Office of Law Revision Counsel)
   - URL: https://uscode.house.gov/download/download.shtml
   - Format: USLM XML (United States Legislative Markup)
   - No authentication required
   - Public domain data

2. **Data Model:** New `Statute` entity (NOT extending Regulation)
   - US Code is statutory law, fundamentally different from regulations
   - Hierarchical: Title → Chapter → Section

3. **Parser Strategy:** StAX streaming for memory efficiency
   - Large files (~50-100MB per title)
   - Batch inserts (100 records)

### Entity Design (from spike)

```java
@Entity
@Table(name = "statutes")
public class Statute {
    @Id
    private UUID id;

    @Column(name = "usc_identifier", unique = true, nullable = false)
    private String uscIdentifier;  // e.g., "/us/usc/t5/s101"

    @Column(name = "title_number", nullable = false)
    private Integer titleNumber;

    @Column(name = "title_name")
    private String titleName;

    @Column(name = "chapter_number")
    private String chapterNumber;

    @Column(name = "chapter_name")
    private String chapterName;

    @Column(name = "section_number", nullable = false)
    private String sectionNumber;

    @Column(name = "heading")
    private String heading;

    @Column(name = "content_text", columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "content_xml", columnDefinition = "TEXT")
    private String contentXml;

    @Column(name = "source_credit")
    private String sourceCredit;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "release_point")
    private String releasePoint;

    @Column(name = "import_source")
    private String importSource;  // 'USCODE'

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### Migration SQL (from spike)

```sql
CREATE TABLE statutes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usc_identifier VARCHAR(100) UNIQUE NOT NULL,
    title_number INTEGER NOT NULL,
    title_name VARCHAR(500),
    chapter_number VARCHAR(20),
    chapter_name VARCHAR(500),
    section_number VARCHAR(50) NOT NULL,
    heading VARCHAR(1000),
    content_text TEXT,
    content_xml TEXT,
    source_credit VARCHAR(500),
    source_url VARCHAR(500),
    release_point VARCHAR(20),
    import_source VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_statutes_title ON statutes(title_number);
CREATE INDEX idx_statutes_chapter ON statutes(title_number, chapter_number);
CREATE INDEX idx_statutes_section ON statutes(section_number);
CREATE UNIQUE INDEX idx_statutes_usc_identifier ON statutes(usc_identifier);
CREATE INDEX idx_statutes_content_fts ON statutes USING gin(to_tsvector('english', content_text));
```

### Sample USLM XML Structure

```xml
<section identifier="/us/usc/t5/s101" temporalId="s101">
  <num>§ 101</num>
  <heading>Executive departments</heading>
  <content>
    <p>The Executive departments are:</p>
    <list>
      <item><num>(1)</num><p>The Department of State.</p></item>
      <item><num>(2)</num><p>The Department of the Treasury.</p></item>
    </list>
  </content>
  <sourceCredit>(Pub. L. 89–554, Sept. 6, 1966, 80 Stat. 378.)</sourceCredit>
</section>
```

### Source Tree - Backend Files

```
backend/src/main/java/org/newsanalyzer/
├── controller/
│   └── StatuteController.java           # NEW: REST API endpoints
├── dto/
│   └── StatuteDTO.java                  # NEW: API response DTO
├── model/
│   └── Statute.java                     # NEW: JPA entity
├── repository/
│   └── StatuteRepository.java           # NEW: Spring Data repository
├── service/
│   ├── UslmXmlParser.java               # NEW: XML streaming parser
│   ├── UsCodeDownloadService.java       # NEW: Download from house.gov
│   └── UsCodeImportService.java         # NEW: Import orchestration

backend/src/main/resources/db/migration/
└── V22__create_statutes_table.sql       # NEW: Database migration

backend/src/test/java/org/newsanalyzer/
├── controller/
│   └── StatuteControllerTest.java       # NEW
├── repository/
│   └── StatuteRepositoryTest.java       # NEW
└── service/
    ├── UslmXmlParserTest.java           # NEW
    └── UsCodeImportServiceTest.java     # NEW
```

### Risks (from spike)

| Risk | Mitigation |
|------|------------|
| Large file parsing performance | StAX streaming, batch inserts |
| Complex section structures | Robust parser with fallbacks |

---

## Testing

### Test Location

`backend/src/test/java/org/newsanalyzer/`

### Test Standards

- Use JUnit 5 with `@ExtendWith(MockitoExtension.class)` for unit tests
- Use `@WebMvcTest` for controller tests
- Use `@DataJpaTest` for repository tests
- Follow project patterns from existing tests (e.g., `RegulationControllerTest`)

### Required Tests

1. **UslmXmlParserTest.java**
   - Parse valid section XML
   - Handle missing optional fields
   - Parse hierarchical context (title/chapter)
   - Handle malformed XML gracefully

2. **UsCodeImportServiceTest.java**
   - Import single title successfully
   - Upsert existing records
   - Handle download failures
   - Report accurate statistics

3. **StatuteControllerTest.java**
   - List statutes with pagination
   - Filter by title number
   - Get statute by ID
   - Get statute by USC citation
   - Handle not found

4. **StatuteRepositoryTest.java**
   - Find by USC identifier
   - Find by title number
   - Full-text search

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-10 | 1.0 | Initial story creation from PRD and ADMIN-1.10 spike findings | Sarah (PO) |
| 2025-12-10 | 1.1 | Story validated and approved for development | Sarah (PO) |
| 2025-12-10 | 1.2 | Implementation complete, QA PASS, status updated to Done | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

N/A - Clean implementation, no significant debugging required.

### Completion Notes List

1. **Task 1: Statute Entity and Repository** - COMPLETE
   - Created `Statute.java` JPA entity with all fields from spike recommendations
   - Created `StatuteRepository.java` with custom query methods for USC identifier, title, chapter, section, and full-text search
   - Followed existing project patterns (Regulation model as reference)

2. **Task 2: Database Migration** - COMPLETE
   - Created `V22__create_statutes_table.sql` with all indexes and full-text search support
   - Included table and column comments for documentation

3. **Task 3: USLM XML Parser** - COMPLETE
   - Created `UslmXmlParser.java` using StAX streaming for memory efficiency
   - Parses section elements with identifier, num, heading, content, sourceCredit
   - Extracts title/chapter context from parent hierarchy
   - Security: Disabled external entities (XXE prevention)

4. **Task 4: Download Service** - COMPLETE
   - Created `UsCodeDownloadService.java` for downloading from uscode.house.gov
   - Handles ZIP file download and XML extraction
   - Supports individual title download with configurable release point

5. **Task 5: Import Service** - COMPLETE
   - Created `UsCodeImportService.java` for orchestrating download → parse → save workflow
   - Implements batch processing (100 records per batch)
   - Upsert logic on usc_identifier for idempotent imports
   - Sets import_source='USCODE' on all records
   - Created `UsCodeImportResult.java` DTO for statistics tracking

6. **Task 6: Admin Sync Controller** - COMPLETE
   - Added endpoints to existing `AdminSyncController.java`:
     - `POST /api/admin/sync/statutes` - full import
     - `POST /api/admin/sync/statutes/{titleNumber}` - single title import
     - `GET /api/admin/sync/statutes/status` - import status
     - `GET /api/admin/sync/statutes/last-result` - last import result
   - Updated health endpoint to include US Code import status

7. **Task 7: Statute REST API** - COMPLETE
   - Created `StatuteController.java` with endpoints:
     - `GET /api/statutes` - paginated list with title filter
     - `GET /api/statutes/{id}` - detail by UUID
     - `GET /api/statutes/by-citation/{uscIdentifier}` - lookup by USC citation
     - `GET /api/statutes/title/{titleNumber}/section/{sectionNumber}` - lookup by title+section
     - `GET /api/statutes/title/{titleNumber}` - list by title with chapter filter
     - `GET /api/statutes/search` - full-text search with optional title filter
     - `GET /api/statutes/titles` - title index
     - `GET /api/statutes/stats` - statistics
   - Created `StatuteDTO.java` with formatted citation helper
   - Created `ParsedStatuteSection.java` DTO for parser output

8. **Task 8: Unit Tests** - COMPLETE
   - Created `UslmXmlParserTest.java` (13 tests) - valid section parsing, missing fields, content extraction
   - Created `UsCodeImportServiceTest.java` (9 tests) - import flow, upsert logic, error handling
   - Created `StatuteControllerTest.java` (15 tests) - all REST endpoints, pagination, response format
   - Created `StatuteRepositoryTest.java` (16 tests) - all query methods, statistics

### File List

**Created:**
- `backend/src/main/java/org/newsanalyzer/model/Statute.java`
- `backend/src/main/java/org/newsanalyzer/repository/StatuteRepository.java`
- `backend/src/main/resources/db/migration/V22__create_statutes_table.sql`
- `backend/src/main/java/org/newsanalyzer/dto/ParsedStatuteSection.java`
- `backend/src/main/java/org/newsanalyzer/dto/StatuteDTO.java`
- `backend/src/main/java/org/newsanalyzer/dto/UsCodeImportResult.java`
- `backend/src/main/java/org/newsanalyzer/service/UslmXmlParser.java`
- `backend/src/main/java/org/newsanalyzer/service/UsCodeDownloadService.java`
- `backend/src/main/java/org/newsanalyzer/service/UsCodeImportService.java`
- `backend/src/main/java/org/newsanalyzer/controller/StatuteController.java`
- `backend/src/test/java/org/newsanalyzer/service/UslmXmlParserTest.java`
- `backend/src/test/java/org/newsanalyzer/service/UsCodeImportServiceTest.java`
- `backend/src/test/java/org/newsanalyzer/controller/StatuteControllerTest.java`
- `backend/src/test/java/org/newsanalyzer/repository/StatuteRepositoryTest.java`

**Modified:**
- `backend/src/main/java/org/newsanalyzer/controller/AdminSyncController.java` - added US Code sync endpoints

---

## QA Results

### Review Date: 2025-12-10

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: EXCELLENT** - The implementation demonstrates high-quality, well-architected code that closely follows the spike recommendations from ADMIN-1.10. The code is well-organized, properly documented, and follows project coding standards consistently.

**Highlights:**
- **Security**: Proper XXE prevention in XML parser (`IS_SUPPORTING_EXTERNAL_ENTITIES=false`, `SUPPORT_DTD=false`)
- **Memory Efficiency**: StAX streaming parser with batch processing (100 records) as recommended by spike
- **Idempotent Design**: Proper upsert logic on `usc_identifier` ensures safe re-runs (IV3)
- **Clean Architecture**: Clear separation of concerns across controller/service/repository layers
- **API Design**: Comprehensive REST endpoints with OpenAPI documentation
- **Test Coverage**: 53 unit tests covering all layers with appropriate test patterns

### Acceptance Criteria Validation

| AC | Status | Evidence |
|----|--------|----------|
| AC1 | ✓ PASS | Follows spike: bulk XML import, new Statute entity, StAX streaming |
| AC2 | ✓ PASS | `Statute.java` has all required fields including usc_identifier, title_number, title_name, chapter_number, chapter_name, section_number, heading, content_text, source_credit, release_point |
| AC3 | ✓ PASS | `V22__create_statutes_table.sql` creates table with idx_statutes_title, idx_statutes_chapter, idx_statutes_section, idx_statutes_usc_identifier (unique), and gin full-text search index |
| AC4 | ✓ PASS | `UslmXmlParser.java` uses XMLStreamReader (StAX) with streaming callback pattern |
| AC5 | ✓ PASS | `UsCodeDownloadService` downloads from uscode.house.gov, `UsCodeImportService` orchestrates upsert flow |
| AC6 | ✓ PASS | `POST /api/admin/sync/statutes` and `POST /api/admin/sync/statutes/{titleNumber}` endpoints with status tracking |
| AC7 | ✓ PASS | `StatuteController` provides `GET /api/statutes` (paginated list with filters), `GET /api/statutes/{id}`, plus additional endpoints for by-citation, by-title-section, search, titles, stats |
| AC8 | ✓ PASS | `UsCodeImportService.IMPORT_SOURCE = "USCODE"` consistently set on all records |

### Integration Verification Assessment

| IV | Status | Notes |
|----|--------|-------|
| IV1 | ✓ PASS | `/api/statutes` endpoints fully implemented with pagination, filtering, and search |
| IV2 | ✓ PASS | New entity/table completely isolated from Regulation functionality |
| IV3 | ✓ PASS | `saveBatch()` method implements proper upsert using `findByUscIdentifier` check |

### Refactoring Performed

None required. The code quality is high and follows established patterns.

### Compliance Check

- Coding Standards: ✓
  - Constructor injection used throughout
  - K&R brace style followed
  - Proper naming conventions (PascalCase classes, camelCase methods, snake_case DB columns)
  - Well-organized class structure with static fields first, then instance fields, constructor, public methods, private methods
- Project Structure: ✓
  - Files placed in correct packages (controller, service, repository, model, dto)
  - Migration follows naming convention `V22__create_statutes_table.sql`
- Testing Strategy: ✓
  - Unit tests use JUnit 5 with MockitoExtension
  - Controller tests use @WebMvcTest
  - Repository tests use @DataJpaTest
  - Given/When/Then pattern followed
- All ACs Met: ✓ (8/8)

### Test Architecture Assessment

**Tests Reviewed: 53**

| Test File | Count | Coverage | Quality |
|-----------|-------|----------|---------|
| UslmXmlParserTest | 13 | Valid parsing, missing fields, edge cases | Excellent |
| UsCodeImportServiceTest | 9 | Import flow, upsert logic, error handling | Excellent |
| StatuteControllerTest | 15 | All endpoints, pagination, response format | Excellent |
| StatuteRepositoryTest | 16 | All query methods, statistics | Good |

**Test Quality Notes:**
- Good use of Given/When/Then structure
- Proper mocking with Mockito
- Edge case coverage (missing fields, empty content, not found scenarios)
- Repository tests require actual database (`@DataJpaTest` with `AutoConfigureTestDatabase.Replace.NONE`)

### Improvements Checklist

[All items are suggestions for future consideration, not blocking issues]

- [ ] Consider adding `@Async` for full import operation to prevent HTTP timeout on 54-title import
- [ ] Consider adding rate limiting on download service to be respectful to uscode.house.gov
- [ ] Consider adding integration test that validates end-to-end with sample XML file
- [ ] Consider caching `listTitles()` response as it performs N+1 queries for section counts
- [ ] Consider making DEFAULT_RELEASE_POINT configurable via application.yml

### Security Review

**Status: PASS**

- XXE prevention properly implemented in `UslmXmlParser`
- No SQL injection risk (using JPA parameterized queries)
- No credential exposure
- Public domain data source (no auth secrets)
- Proper input validation on controller endpoints (@Min, @Max, @NotBlank)

### Performance Considerations

**Status: PASS with NOTES**

**Implemented:**
- StAX streaming for memory-efficient XML parsing
- Batch inserts (100 records) to reduce database roundtrips
- Full-text search uses GIN index for performance
- Pagination on all list endpoints

**Future Considerations:**
- Full import of 54 titles may take 30+ minutes (synchronous execution)
- `listTitles()` performs N+1 queries for section counts - consider caching or restructuring

### Files Modified During Review

None - no modifications made.

### Gate Status

Gate: **PASS** → docs/qa/gates/ADMIN-1.11-us-code-import-backend.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, high code quality, comprehensive test coverage (53 tests), follows spike recommendations exactly.
