# Story FB-1.1: Member Lookup API Integration

## Status

**Done**

## Story

**As a** fact-checker,
**I want** to look up current Members of Congress by name, state, or BioGuide ID,
**so that** I can verify claims about who currently holds office in the Senate and House.

## Acceptance Criteria

1. **API Client**: Congress.gov API client is implemented with proper authentication (api.data.gov key)
2. **Member Sync**: All 535+ current Members of Congress are synced to the factbase
3. **Person Entity**: Person entity is created with fields: bioguideId, firstName, lastName, middleName, suffix, party, state, birthDate, gender, imageUrl
4. **BioGuide Index**: BioGuide ID is unique and indexed for fast lookups
5. **Lookup Endpoints**: REST endpoints exist for:
   - `GET /api/members` - List all current members (paginated)
   - `GET /api/members/{bioguideId}` - Get member by BioGuide ID
   - `GET /api/members/search?name={name}` - Search by name
   - `GET /api/members/by-state/{state}` - List by state (2-letter code)
   - `GET /api/members/by-chamber/{chamber}` - List by SENATE or HOUSE
6. **Response Time**: All endpoints return within 500ms
7. **Error Handling**: Graceful handling of API failures with appropriate error responses
8. **Rate Limiting**: Client respects Congress.gov 5,000 requests/hour limit

## Tasks / Subtasks

- [x] **Task 1: Database Schema** (AC: 3, 4)
  - [x] Create Person entity class with all required fields
  - [x] Include `externalIds JSONB` column (for future enrichment - FB-1.4)
  - [x] Create Flyway migration for `persons` table (V6__create_persons_table.sql)
  - [x] Add unique index on `bioguide_id`
  - [x] Create PersonRepository with Spring Data JPA

- [x] **Task 2: Congress.gov API Client** (AC: 1, 8)
  - [x] Create CongressApiClient service class
  - [x] Implement API key configuration via environment variable
  - [x] Implement `/v3/member` endpoint call with pagination
  - [x] Implement `/v3/member/{bioguideId}` endpoint call
  - [x] Add rate limiting logic (max 5000/hr, ~1.4/sec average)
  - [x] Add retry logic with exponential backoff

- [x] **Task 3: Member Sync Service** (AC: 2)
  - [x] Create MemberSyncService class
  - [x] Implement full sync of all current members
  - [x] Map Congress.gov API response to Person entity
  - [x] Handle incremental updates (upsert by bioguideId)
  - [x] Log sync statistics (added, updated, errors)

- [x] **Task 4: REST API Endpoints** (AC: 5, 6, 7)
  - [x] Create MemberController class
  - [x] Implement `GET /api/members` with pagination
  - [x] Implement `GET /api/members/{bioguideId}`
  - [x] Implement `GET /api/members/search` with name parameter
  - [x] Implement `GET /api/members/by-state/{state}`
  - [x] Implement `GET /api/members/by-chamber/{chamber}`
  - [x] Add proper error handling and HTTP status codes
  - [x] Add OpenAPI/Swagger documentation

- [x] **Task 5: Testing** (AC: all)
  - [x] Unit tests for CongressApiClient (mock HTTP responses)
  - [x] Unit tests for MemberSyncService
  - [x] Integration tests for MemberController endpoints
  - [x] Verify response time <500ms

## Dev Notes

### Congress.gov API Details

**Base URL**: `https://api.congress.gov/v3`

**Authentication**: API key passed as query parameter `?api_key=YOUR_KEY`

**Get API Key**: Register at https://api.data.gov/signup/

**Key Endpoints**:
```
GET /v3/member
    ?currentMember=true
    &limit=250  (max per request)
    &offset=0
    &api_key=...

GET /v3/member/{bioguideId}
    ?api_key=...
```

**Sample Response** (`/v3/member/{bioguideId}`):
```json
{
  "member": {
    "bioguideId": "S000033",
    "firstName": "Bernard",
    "lastName": "Sanders",
    "party": "Independent",
    "state": "VT",
    "district": null,
    "depiction": {
      "imageUrl": "https://...",
      "attribution": "..."
    },
    "birthYear": "1941",
    "terms": [...]
  }
}
```

### Entity Mapping

| Congress.gov Field | Person Entity Field |
|--------------------|---------------------|
| `bioguideId` | `bioguideId` (PK candidate) |
| `firstName` | `firstName` |
| `lastName` | `lastName` |
| `middleName` | `middleName` |
| `party` | `party` |
| `state` | `state` |
| `birthYear` | `birthDate` (convert to date) |
| `depiction.imageUrl` | `imageUrl` |
| _(enrichment)_ | `externalIds` (JSONB - for FEC, GovTrack, etc.) |

**Note**: Include `externalIds JSONB` column from initial migration to avoid future schema changes. This field will be populated by FB-1.4 (Legislators Enrichment Sync).

### Configuration

Add to `application.yml`:
```yaml
congress:
  api:
    base-url: https://api.congress.gov/v3
    key: ${CONGRESS_API_KEY}
    rate-limit: 5000  # per hour
```

### Source Tree Reference

```
backend/
├── src/main/java/org/newsanalyzer/
│   ├── config/
│   │   └── CongressApiConfig.java (NEW)
│   ├── model/
│   │   └── Person.java (NEW)
│   ├── repository/
│   │   └── PersonRepository.java (NEW)
│   ├── service/
│   │   ├── CongressApiClient.java (NEW)
│   │   └── MemberSyncService.java (NEW)
│   └── controller/
│       └── MemberController.java (NEW)
├── src/main/resources/
│   └── db/migration/
│       └── V6__create_persons_table.sql (NEW)  # Next available after V5
└── src/test/java/org/newsanalyzer/
    ├── service/
    │   ├── CongressApiClientTest.java (NEW)
    │   └── MemberSyncServiceTest.java (NEW)
    └── controller/
        └── MemberControllerTest.java (NEW)
```

### Testing

**Test Location**: `backend/src/test/java/org/newsanalyzer/`

**Testing Standards**:
- JUnit 5 for unit tests
- MockMvc for controller integration tests
- WireMock for mocking Congress.gov API responses
- Test database using H2 or Testcontainers

**Test Data**:
- Create mock responses for at least 5 members (mix of Senate/House)
- Include edge cases: members with no middle name, independent party

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2024-11-27 | 1.0 | Initial story creation | Sarah (PO) |
| 2024-11-27 | 1.1 | Architect review: Added externalIds JSONB, fixed migration naming | Winston (Architect) |
| 2024-11-28 | 1.2 | QA review: PASS gate, all ACs verified | Quinn (QA) |
| 2024-11-28 | 1.3 | SM review: Concur with QA, status → Done | Bob (SM) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References
- All compilation checks passed
- All unit and integration tests pass

### Completion Notes List
1. **Task 1 (Database Schema)**: Created Person entity with all required fields including externalIds JSONB for future enrichment. Flyway migration V6 creates persons table with proper indexes.
2. **Task 2 (Congress API Client)**: Implemented CongressApiClient with rate limiting (750ms between requests), exponential backoff retry (3 attempts), and proper error handling.
3. **Task 3 (Member Sync Service)**: Created MemberSyncService that syncs all current members, maps API response to Person entity, handles upserts, and tracks sync statistics.
4. **Task 4 (REST API Endpoints)**: Implemented MemberController with all required endpoints plus additional stats endpoints. Added OpenAPI annotations for documentation.
5. **Task 5 (Testing)**: Created comprehensive unit tests for CongressApiClient and MemberSyncService, and integration tests for MemberController using MockMvc. Response time tests verify <500ms requirement.

### File List
**New Files Created:**
- `backend/src/main/resources/db/migration/V6__create_persons_table.sql`
- `backend/src/main/java/org/newsanalyzer/model/Person.java`
- `backend/src/main/java/org/newsanalyzer/repository/PersonRepository.java`
- `backend/src/main/java/org/newsanalyzer/config/CongressApiConfig.java`
- `backend/src/main/java/org/newsanalyzer/service/CongressApiClient.java`
- `backend/src/main/java/org/newsanalyzer/service/MemberSyncService.java`
- `backend/src/main/java/org/newsanalyzer/service/MemberService.java`
- `backend/src/main/java/org/newsanalyzer/controller/MemberController.java`
- `backend/src/test/java/org/newsanalyzer/service/CongressApiClientTest.java`
- `backend/src/test/java/org/newsanalyzer/service/MemberSyncServiceTest.java`
- `backend/src/test/java/org/newsanalyzer/controller/MemberControllerTest.java`

**Modified Files:**
- `backend/src/main/resources/application-dev.yml` (added congress.api configuration)

---

## QA Results

### Review Date: 2024-11-28

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: GOOD** - The implementation is well-structured and follows established patterns. Code is clean, well-documented, and appropriately organized.

**Strengths:**
- Clean separation of concerns (Controller → Service → Repository)
- Comprehensive validation annotations on Person entity
- Good use of JPA lifecycle callbacks for audit fields
- Rate limiting and retry logic properly implemented in CongressApiClient
- Thorough OpenAPI documentation on controller endpoints

**Minor Observations:**
- SyncResult inner class uses public setters but could use builder pattern for immutability
- MemberControllerTest uses `@MockBean` correctly, tests are well-organized by endpoint category

### Refactoring Performed

None required. Code quality is satisfactory for this iteration.

### Compliance Check

- Coding Standards: ✓ Follows existing patterns (e.g., GovernmentOrganization)
- Project Structure: ✓ Files in correct locations per source-tree.md
- Testing Strategy: ✓ Unit tests with Mockito, integration tests with MockMvc
- All ACs Met: ✓ See traceability matrix below

### Acceptance Criteria Traceability

| AC# | Criteria | Test Coverage | Status |
|-----|----------|---------------|--------|
| 1 | API Client with auth | CongressApiClientTest.isConfigured_* | ✓ PASS |
| 2 | Member Sync | MemberSyncServiceTest.syncAllCurrentMembers_* | ✓ PASS |
| 3 | Person Entity fields | Entity class + V6 migration | ✓ PASS |
| 4 | BioGuide Index | V6 migration (UNIQUE INDEX) | ✓ PASS |
| 5 | Lookup Endpoints | MemberControllerTest (all endpoint tests) | ✓ PASS |
| 6 | Response Time <500ms | MemberControllerTest.*_respondsWithin500ms | ✓ PASS |
| 7 | Error Handling | MemberControllerTest.*_returns400/404 | ✓ PASS |
| 8 | Rate Limiting | CongressApiClient.checkRateLimit() | ✓ PASS |

### Improvements Checklist

- [x] All required endpoints implemented
- [x] Unit tests cover service layer
- [x] Integration tests cover controller layer
- [x] Database migration includes proper indexes
- [x] Error responses return appropriate HTTP status codes
- [ ] Consider adding WireMock tests for actual HTTP mocking (deferred to FB-1.0)
- [ ] Cross-service integration tests should be added in api-tests project (FB-1.0)

### Security Review

**Status: PASS**
- API key stored in environment variable (not hardcoded)
- No SQL injection risk (using JPA parameterized queries)
- Input validation present on entity fields
- CORS properly configured for local development

**Note:** POST /api/members/sync endpoint lacks authentication. Consider adding admin-only access control in future iteration.

### Performance Considerations

**Status: PASS**
- Rate limiting prevents API abuse (750ms delay between requests)
- Database indexes on bioguide_id, last_name, state, chamber, party
- Full-text search index for name queries
- Response time tests verify <500ms requirement

**Potential Improvement:** Consider adding Caffeine caching for frequently accessed members (mentioned in epic architecture).

### Files Modified During Review

None. No refactoring was performed.

### Gate Status

Gate: **PASS** → docs/qa/gates/FB-1.1-member-lookup-integration.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, tests passing, code quality satisfactory.

(Story owner decides final status)
