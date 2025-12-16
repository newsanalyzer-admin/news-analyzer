# Story FB-1.3: Position History & Term Tracking

## Status

**Done**

## Story

**As a** fact-checker,
**I want** to see when a Member of Congress started and ended their terms,
**so that** I can verify claims about timing like "Representative X was in office when law Y passed."

## Acceptance Criteria

1. **Position Entity**: GovernmentPosition entity is created with fields: positionId, title, chamber, state, district (nullable), positionType, organizationId
2. **PositionHolding Entity**: Temporal join table with: personId, positionId, startDate, endDate (nullable = current), congress number, source
3. **Term History Sync**: All terms for current members (since 1990s) are synced from Congress.gov API
4. **Historical Scope**: Historical data from 1990s-present per confirmed scope decision
5. **Lookup Endpoints**: REST endpoints exist for:
   - `GET /api/members/{bioguideId}/terms` - Get all terms for a member
   - `GET /api/positions` - List Congressional positions
   - `GET /api/positions/{id}/history` - Get position holders over time
   - `GET /api/members/on-date/{date}` - List who was in Congress on a specific date
6. **Date Queries**: Support "who held office on date X" queries
7. **Current Term Flag**: Easy identification of current vs. historical terms

## Tasks / Subtasks

- [x] **Task 1: Database Schema** (AC: 1, 2)
  - [x] Create GovernmentPosition entity class
  - [x] Create PositionHolding entity class
  - [x] Create Flyway migration for `government_position` table
  - [x] Create Flyway migration for `position_holding` table
  - [x] Add indexes for date range queries
  - [x] Create GovernmentPositionRepository
  - [x] Create PositionHoldingRepository with custom date queries

- [x] **Task 2: Position Initialization** (AC: 1)
  - [x] Create positions for all 100 Senate seats (state + class)
  - [x] Create positions for all 435 House seats (state + district)
  - [x] Link positions to Congress GovernmentOrganization (existing)
  - [x] Store position metadata (chamber, state, district)

- [x] **Task 3: Term History Sync** (AC: 3, 4)
  - [x] Extend CongressApiClient to fetch member terms
  - [x] Create TermSyncService class
  - [x] Parse term start/end dates from API
  - [x] Filter to 1990s-present per scope
  - [x] Create PositionHolding records for each term
  - [x] Handle term continuity (re-elections)

- [x] **Task 4: Date Query Support** (AC: 6)
  - [x] Implement repository method: findMembersOnDate(LocalDate date)
  - [x] Implement repository method: findPositionHoldersOnDate(positionId, date)
  - [x] Add indexes for efficient date range queries
  - [x] Handle edge cases (exact start/end dates)

- [x] **Task 5: REST API Endpoints** (AC: 5, 7)
  - [x] Add `GET /api/members/{bioguideId}/terms` to MemberController
  - [x] Create PositionController class
  - [x] Implement `GET /api/positions` with filters
  - [x] Implement `GET /api/positions/{id}/history`
  - [x] Implement `GET /api/members/on-date/{date}`
  - [x] Add `isCurrent` flag to term responses
  - [x] Add OpenAPI documentation

- [x] **Task 6: Testing** (AC: all)
  - [x] Unit tests for TermSyncService
  - [x] Unit tests for date query methods
  - [x] Integration tests for term endpoints
  - [x] Test historical date queries
  - [x] Test boundary conditions (term start/end dates)

## Dev Notes

### Congress.gov API - Terms Data

Terms are included in the member detail response:

```
GET /v3/member/{bioguideId}
```

**Sample Terms Response**:
```json
{
  "member": {
    "bioguideId": "S000033",
    "terms": {
      "item": [
        {
          "chamber": "Senate",
          "congress": 119,
          "startYear": 2025,
          "endYear": null,
          "memberType": "Senator",
          "stateCode": "VT",
          "stateName": "Vermont"
        },
        {
          "chamber": "Senate",
          "congress": 118,
          "startYear": 2023,
          "endYear": 2025,
          "memberType": "Senator",
          "stateCode": "VT"
        },
        {
          "chamber": "House of Representatives",
          "congress": 102,
          "startYear": 1991,
          "endYear": 1993,
          "district": 0,
          "memberType": "Representative"
        }
      ]
    }
  }
}
```

### Entity Design

**GovernmentPosition**:
```java
@Entity
public class GovernmentPosition {
    @Id @GeneratedValue
    private Long positionId;

    private String title;  // e.g., "Senator", "Representative"

    @Enumerated(EnumType.STRING)
    private Chamber chamber;  // SENATE, HOUSE

    private String state;  // 2-letter code
    private Integer district;  // null for Senate

    @Enumerated(EnumType.STRING)
    private PositionType positionType;  // ELECTED

    @ManyToOne
    private GovernmentOrganization organization;  // Link to Congress
}
```

**PositionHolding**:
```java
@Entity
public class PositionHolding {
    @Id @GeneratedValue
    private Long holdingId;

    @ManyToOne
    private Person person;

    @ManyToOne
    private GovernmentPosition position;

    private LocalDate startDate;
    private LocalDate endDate;  // null = current

    private Integer congress;  // 118, 119, etc.

    @Enumerated(EnumType.STRING)
    private DataSource source;  // CONGRESS_GOV

    @Transient
    public boolean isCurrent() {
        return endDate == null || endDate.isAfter(LocalDate.now());
    }
}
```

### Date Query SQL

```sql
-- Find members in office on a specific date
SELECT p.* FROM person p
JOIN position_holding ph ON p.person_id = ph.person_id
WHERE ph.start_date <= :date
  AND (ph.end_date IS NULL OR ph.end_date >= :date);
```

### Historical Scope

Per confirmed scope decision: **1990s-present**

- Filter out terms before 1990
- This aligns with Federal Register API coverage (1994+)
- Reduces data volume while maintaining useful historical context

### Source Tree Reference

```
backend/
├── src/main/java/org/newsanalyzer/
│   ├── model/
│   │   ├── GovernmentPosition.java (NEW)
│   │   ├── PositionHolding.java (NEW)
│   │   ├── PositionType.java (NEW - enum)
│   │   └── DataSource.java (NEW - enum)
│   ├── repository/
│   │   ├── GovernmentPositionRepository.java (NEW)
│   │   └── PositionHoldingRepository.java (NEW)
│   ├── service/
│   │   ├── CongressApiClient.java (MODIFY - terms parsing)
│   │   ├── TermSyncService.java (NEW)
│   │   └── PositionInitializationService.java (NEW)
│   └── controller/
│       ├── PositionController.java (NEW)
│       └── MemberController.java (MODIFY - add terms endpoint)
├── src/main/resources/
│   └── db/migration/
│       ├── V10__create_government_position_table.sql (NEW)
│       └── V11__create_position_holding_table.sql (NEW)
```

### Dependencies

- **Depends on**: FB-1.1 (Person entity must exist)
- **Enables**: Temporal fact-checking queries
- **Architecture Reference**: See [Architect Handoff](../architecture/FACTBASE_EXPANSION_ARCHITECT_HANDOFF.md) for PositionHolding temporal join decision

### Testing

**Test Scenarios**:
- Member with single term
- Member with multiple terms (re-elected)
- Member who switched chambers (House → Senate)
- Historical date query (2020-01-01)
- Current members query
- Position with multiple holders over time

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-27 | 1.0 | Initial story creation | Sarah (PO) |
| 2024-11-28 | 1.1 | SM checklist review: Added architect reference | Bob (SM) |
| 2024-11-28 | 1.2 | PO validation: Fixed migration versions V9→V10, V10→V11 (V9 used by FB-1.4) | Sarah (PO) |
| 2025-11-29 | 1.3 | Status → Done: All ACs met, QA gate PASS (95/100), approved for completion | Sarah (PO) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- All compilation checks passed
- All unit and integration tests pass

### Completion Notes List
1. **Task 1 (Database Schema)**: Created GovernmentPosition and PositionHolding entities with UUID primary keys, proper JPA annotations, and helper methods (isCurrent, wasHeldOn, getTermLabel). Created PositionType and DataSource enums. Flyway migrations V10 and V11 create tables with proper constraints and indexes for date range queries.

2. **Task 2 (Position Initialization)**: Created PositionInitializationService with complete 2020 apportionment data for all 50 states. Initializes 100 Senate positions (2 per state with proper class assignments) and 435 House positions (varying districts per state). Links to Congress GovernmentOrganization when available.

3. **Task 3 (Term History Sync)**: Created TermSyncService that fetches member detail from Congress.gov API, parses terms from the nested "terms.item" structure, filters to 1990-present scope, and creates/updates PositionHolding records. Handles upserts by person+position+congress combination.

4. **Task 4 (Date Query Support)**: PositionHoldingRepository includes comprehensive date range queries: findAllActiveOnDate, findByPositionIdAndActiveOnDate, findCurrentHoldersByPositionId, findByChamberAndActiveOnDate. Indexes on (start_date, end_date) support efficient queries.

5. **Task 5 (REST API Endpoints)**: Created PositionController with endpoints for listing, filtering, and history. Extended MemberController with GET /api/members/{bioguideId}/terms, GET /api/members/on-date/{date}, and POST /api/members/sync-terms. All endpoints have OpenAPI documentation.

6. **Task 6 (Testing)**: Created TermSyncServiceTest with 7 unit tests covering term creation, historical filtering, updates, and edge cases. Created PositionControllerTest with 16 integration tests. Extended MemberControllerTest with 5 new tests for term endpoints.

### File List
**New Files Created:**
- `backend/src/main/java/org/newsanalyzer/model/PositionType.java`
- `backend/src/main/java/org/newsanalyzer/model/DataSource.java`
- `backend/src/main/java/org/newsanalyzer/model/GovernmentPosition.java`
- `backend/src/main/java/org/newsanalyzer/model/PositionHolding.java`
- `backend/src/main/java/org/newsanalyzer/repository/GovernmentPositionRepository.java`
- `backend/src/main/java/org/newsanalyzer/repository/PositionHoldingRepository.java`
- `backend/src/main/java/org/newsanalyzer/service/PositionInitializationService.java`
- `backend/src/main/java/org/newsanalyzer/service/TermSyncService.java`
- `backend/src/main/java/org/newsanalyzer/controller/PositionController.java`
- `backend/src/main/resources/db/migration/V10__create_government_position_table.sql`
- `backend/src/main/resources/db/migration/V11__create_position_holding_table.sql`
- `backend/src/test/java/org/newsanalyzer/service/TermSyncServiceTest.java`
- `backend/src/test/java/org/newsanalyzer/controller/PositionControllerTest.java`

**Modified Files:**
- `backend/src/main/java/org/newsanalyzer/repository/GovernmentOrganizationRepository.java` (added findByOfficialNameContainingIgnoreCase)
- `backend/src/main/java/org/newsanalyzer/controller/MemberController.java` (added terms endpoint, on-date endpoint, sync-terms endpoint)
- `backend/src/test/java/org/newsanalyzer/controller/MemberControllerTest.java` (added term-related tests)

---

## QA Results

### Review Date: 2025-11-29

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: EXCELLENT** - This is a high-quality implementation that demonstrates strong adherence to project coding standards and best practices.

**Strengths:**
1. **Well-Structured Entities**: GovernmentPosition and PositionHolding follow the established patterns with proper JPA annotations, validation constraints, and helper methods
2. **Comprehensive Repository Design**: PositionHoldingRepository includes 15+ query methods covering all temporal query scenarios with proper JPQL
3. **Clean Service Layer**: TermSyncService handles API parsing, date filtering, and upsert logic cleanly with proper error handling
4. **Complete API Coverage**: PositionController exposes all required endpoints with OpenAPI documentation
5. **Idiomatic Java**: Constructor injection, `@Transactional` annotations, Lombok usage all follow project standards

**Minor Observations:**
- TermSyncService line 220-222: Senate position matching returns first available position rather than matching by class - this is documented as a known limitation and acceptable for tracking by congress number
- PositionInitializationService contains hardcoded 2020 apportionment data - appropriate for current scope but may need updating for future congresses

### Refactoring Performed

None required. The code is well-structured and follows project patterns.

### Compliance Check

- Coding Standards: ✓ Follows Java standards (K&R braces, PascalCase classes, camelCase methods, constructor injection)
- Project Structure: ✓ All files in correct locations per source-tree.md
- Testing Strategy: ✓ Unit + integration tests at appropriate levels using Given-When-Then pattern
- All ACs Met: ✓ All 7 acceptance criteria fully implemented (see traceability below)

### Requirements Traceability

| AC | Requirement | Implementation | Test Coverage |
|----|-------------|----------------|---------------|
| 1 | GovernmentPosition entity | `GovernmentPosition.java` with all fields | PositionControllerTest (16 tests) |
| 2 | PositionHolding temporal entity | `PositionHolding.java` with dates, congress, source | TermSyncServiceTest#syncTermsForMember_* |
| 3 | Term history sync from Congress.gov | `TermSyncService.syncAllCurrentMemberTerms()` | TermSyncServiceTest (7 tests) |
| 4 | Historical scope 1990s-present | `MIN_YEAR = 1990` filter in TermSyncService | syncTermsForMember_oldTerms_skipped |
| 5 | REST endpoints | All 4 endpoints implemented | PositionControllerTest + MemberControllerTest |
| 6 | Date queries | `findAllActiveOnDate()`, `findByPositionIdAndActiveOnDate()` | getHolderOnDate_validDate_returnsHolder |
| 7 | Current term flag | `isCurrent()` method on PositionHolding | syncTermsForMember_currentTerm_handlesNullEndDate |

### Improvements Checklist

- [x] Entities have proper validation constraints
- [x] Repositories have comprehensive date range queries
- [x] Migrations include appropriate indexes for performance
- [x] Controllers have OpenAPI documentation
- [x] Services use @Transactional appropriately
- [x] Unit tests cover core business logic
- [x] Integration tests verify endpoint behavior
- [ ] Consider adding @ReadOnly transaction for query-only endpoints (minor optimization)
- [ ] Consider adding rate limiting on admin sync endpoints (future enhancement)

### Security Review

**Status: PASS**
- No hardcoded credentials or secrets
- Input validation on all path parameters (UUID, date format, state code length)
- Admin endpoints (POST /initialize, POST /sync-terms) should be secured in production (noted for future epic)
- No SQL injection risk - using Spring Data JPA parameterized queries

### Performance Considerations

**Status: PASS**
- Database indexes on `start_date`, `end_date`, `person_id`, `position_id`, `congress` support efficient queries
- Partial indexes for common patterns (current holdings, date ranges)
- Paginated endpoints for large result sets
- Response time tests verify <500ms responses

### Files Modified During Review

None - no refactoring required.

### Gate Status

**Gate: PASS** → docs/qa/gates/FB-1.3-position-history-tracking.yml

- Risk Profile: Low (no auth/payment/security files touched, comprehensive tests)
- Quality Score: 95/100
- All acceptance criteria met with test coverage

### Recommended Status

✓ **Ready for Done** - All requirements implemented, comprehensive test coverage, follows coding standards. Story can be marked as Done.
