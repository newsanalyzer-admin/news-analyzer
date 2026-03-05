# Story STAB-1.1: Fix Federal Register DTO Deserialization

## Status

**Done**

---

## Story

**As a** system administrator,
**I want** the Federal Register agency sync to handle the live API response correctly,
**so that** government agency data can be imported without deserialization failures.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `FederalRegisterAgency` DTO has `@JsonIgnoreProperties(ignoreUnknown = true)` annotation |
| AC2 | `logo` field handles both `null` and nested JSON object responses (not just String) |
| AC3 | Unknown API fields (`agency_url`, `child_ids`, `child_slugs`, `json_url`, `recent_articles_url`) are silently ignored |
| AC4 | `FederalRegisterDocument.agencies` list deserializes correctly when agency objects contain unknown fields |
| AC5 | Federal Register agency sync endpoint (`POST /api/government-organizations/sync/federal-register`) completes successfully against the live API |
| AC6 | Federal Register document import (`POST /api/admin/import/federal-register/document`) still works correctly |
| AC7 | Existing unit tests pass; new tests cover the logo object and unknown field scenarios |

---

## Tasks / Subtasks

- [ ] **Task 1: Fix FederalRegisterAgency DTO** (AC1, AC2, AC3)
  - [ ] Add `@JsonIgnoreProperties(ignoreUnknown = true)` to class
  - [ ] Change `logo` field from `String` to `Object` (Jackson will deserialize as null, String, or LinkedHashMap)
  - [ ] Add import for `com.fasterxml.jackson.annotation.JsonIgnoreProperties`

- [ ] **Task 2: Verify cascading fix in FederalRegisterDocument** (AC4)
  - [ ] Confirm `FederalRegisterDocument` already has `@JsonIgnoreProperties(ignoreUnknown = true)` (it does)
  - [ ] Verify that the `agencies` field (`List<FederalRegisterAgency>`) now deserializes correctly since the nested DTO is fixed
  - [ ] No changes needed to `FederalRegisterDocument.java` itself

- [ ] **Task 3: Update tests** (AC7)
  - [ ] Update `FederalRegisterClientTest` to include test data with logo as nested object: `{"thumb_url": "...", "small_url": "...", "medium_url": "..."}`
  - [ ] Add test case with unknown fields in agency JSON (`recent_articles_url`, `child_ids`, etc.)
  - [ ] Update WireMock test data in `api-tests/src/test/resources/wiremock/federal-register/__files/federal-register-agencies.json` to match live API structure
  - [ ] Verify existing GovOrgSyncTest still passes

- [ ] **Task 4: Manual verification** (AC5, AC6)
  - [ ] Start backend locally and trigger Federal Register agency sync
  - [ ] Confirm sync completes without deserialization errors
  - [ ] Verify imported agencies appear in the database

---

## Dev Notes

### Root Cause
The live Federal Register API at `https://www.federalregister.gov/api/v1/agencies` returns `logo` as either:
- `null` (most agencies)
- A nested JSON object: `{"thumb_url": "...", "small_url": "...", "medium_url": "..."}` (agencies with logos)

The current DTO defines `logo` as `String`, causing Jackson to fail with:
```
Cannot deserialize value of type `java.lang.String` from Object value (token `JsonToken.START_OBJECT`)
```

### Key Files
| File | Path | Change |
|------|------|--------|
| FederalRegisterAgency DTO | `backend/src/main/java/org/newsanalyzer/dto/FederalRegisterAgency.java` | Add annotation, change logo type |
| FederalRegisterDocument DTO | `backend/src/main/java/org/newsanalyzer/dto/FederalRegisterDocument.java` | Verify only (has annotation already) |
| FR Client | `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | No changes (deserialization at line 62-64) |
| GovOrgSyncService | `backend/src/main/java/org/newsanalyzer/service/GovernmentOrgSyncService.java` | No changes (calls client at line 116) |
| FR Client Test | `backend/src/test/java/org/newsanalyzer/service/FederalRegisterClientTest.java` | Add test cases |
| WireMock data | `api-tests/src/test/resources/wiremock/federal-register/__files/federal-register-agencies.json` | Update test data |

### Live API Response Structure (per agency)
```json
{
  "id": 1,
  "name": "Department of Agriculture",
  "short_name": "USDA",
  "description": "...",
  "url": "https://www.usda.gov",
  "parent_id": null,
  "slug": "agriculture-department",
  "logo": null,
  "agency_url": "",
  "child_ids": [],
  "child_slugs": [],
  "recent_articles_url": "https://...",
  "json_url": "https://..."
}
```

Some agencies have populated logo:
```json
"logo": {
  "thumb_url": "https://...",
  "small_url": "https://...",
  "medium_url": "https://..."
}
```

### Pattern Reference
`FederalRegisterDocumentPage` already correctly uses `@JsonIgnoreProperties(ignoreUnknown = true)` — follow the same pattern.

### Testing

- **Unit tests**: `backend/src/test/java/org/newsanalyzer/service/FederalRegisterClientTest.java`
- **Integration tests**: `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgSyncTest.java`
- **Framework**: JUnit 5 + Mockito (backend), REST Assured (api-tests)
- **Run**: `cd backend && ./mvnw test -pl . -Dtest=FederalRegisterClientTest`
- **Coverage**: JaCoCo enforced at 70% for backend

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (C1, C2, C3) | Sarah (PO) |
