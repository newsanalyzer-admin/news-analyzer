# Story STAB-1.5: Harden Congress.gov Import Services

## Status

**Done**

---

## Story

**As a** system,
**I want** Congress.gov import services to handle API response variations gracefully,
**so that** member imports, term syncs, and committee syncs do not break when the API evolves.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `CongressMemberDetailDTO` and `CongressMemberSearchDTO` have `@JsonIgnoreProperties(ignoreUnknown = true)` |
| AC2 | Terms array handling is unified — all services handle both `terms.item` array and direct `terms` array formats |
| AC3 | `CommitteeSyncService` uses granular exception handling instead of blanket `entityManager.clear()` on any failure |
| AC4 | `MemberSyncService`, `TermSyncService`, and `CongressSearchService` all use the same utility method for terms normalization |
| AC5 | Null/missing fields in Congress.gov responses do not cause NullPointerException — graceful defaults are applied |
| AC6 | Existing tests pass; new tests cover terms format variations and missing field scenarios |

---

## Tasks / Subtasks

- [ ] **Task 1: Add @JsonIgnoreProperties to Congress DTOs** (AC1)
  - [ ] Add `@JsonIgnoreProperties(ignoreUnknown = true)` to `CongressMemberDetailDTO`
  - [ ] Add `@JsonIgnoreProperties(ignoreUnknown = true)` to `CongressMemberSearchDTO`
  - [ ] Add to `CongressImportResult` if it is ever used for deserialization (verify usage first)

- [ ] **Task 2: Unify terms array handling** (AC2, AC4)
  - [ ] Create a utility method (e.g., in a `CongressApiUtils` class or as a static method in `CongressApiClient`):
    ```java
    public static JsonNode normalizeTermsArray(JsonNode termsNode) {
        if (termsNode == null || termsNode.isMissingNode()) return EMPTY_ARRAY;
        if (termsNode.isArray()) return termsNode;
        if (termsNode.has("item")) return termsNode.path("item");
        return EMPTY_ARRAY;
    }
    ```
  - [ ] Update `MemberSyncService` (line ~248) to use the utility method
  - [ ] Update `TermSyncService` (line ~104-116) to use the utility method
  - [ ] Update `CongressSearchService` (line ~216) to use the utility method

- [ ] **Task 3: Fix CommitteeSyncService entityManager.clear()** (AC3)
  - [ ] Replace blanket `entityManager.clear()` with targeted error recovery
  - [ ] Use `entityManager.detach(failedEntity)` for the specific failed entity instead of clearing entire context
  - [ ] If detach is insufficient (e.g., constraint violation), use a new transaction for each committee via `TransactionTemplate`
  - [ ] Log the specific committee that failed, not just the exception message

- [ ] **Task 4: Add null safety for Congress.gov responses** (AC5)
  - [ ] Audit `MemberSyncService.syncMember()` for null-unsafe field access
  - [ ] Ensure `mapChamber()` logs unknown values at WARN level instead of silently returning null
  - [ ] Add null checks on `partyName`, `state`, and other fields used without guards
  - [ ] Ensure name parsing handles null/empty firstName gracefully (currently produces "Unknown")

- [ ] **Task 5: Update tests** (AC6)
  - [ ] Add test for terms as direct array format
  - [ ] Add test for terms as `terms.item` array format
  - [ ] Add test for missing terms node entirely
  - [ ] Add test for committee sync with one failed record (verify others still succeed)
  - [ ] Verify existing tests pass

---

## Dev Notes

### Terms Array Inconsistency
Three services handle `terms` differently:
- `MemberSyncService` (line ~248): `terms.path("item")` — expects nested
- `TermSyncService` (line ~104-116): Checks both `terms.item` and direct array — most robust
- `CongressSearchService` (line ~216): `terms.isArray()` — expects direct array only

The Congress.gov API has been observed to return both formats depending on the endpoint and number of results.

### entityManager.clear() Risk
In `CommitteeSyncService` (line ~168), when one committee fails to save:
```java
entityManager.clear(); // Discards ALL pending changes, not just the failed one
```
This can silently lose data from previously successful operations that haven't been flushed.

### Key Files
| File | Path | Change |
|------|------|--------|
| CongressMemberDetailDTO | `backend/src/main/java/org/newsanalyzer/dto/CongressMemberDetailDTO.java` | Add annotation |
| CongressMemberSearchDTO | `backend/src/main/java/org/newsanalyzer/dto/CongressMemberSearchDTO.java` | Add annotation |
| MemberSyncService | `backend/src/main/java/org/newsanalyzer/service/MemberSyncService.java` | Unify terms, null safety |
| TermSyncService | `backend/src/main/java/org/newsanalyzer/service/TermSyncService.java` | Unify terms |
| CongressSearchService | `backend/src/main/java/org/newsanalyzer/service/CongressSearchService.java` | Unify terms |
| CommitteeSyncService | `backend/src/main/java/org/newsanalyzer/service/CommitteeSyncService.java` | Fix entityManager.clear() |

### Pattern Reference
`LegislatorYamlRecord` already correctly uses `@JsonIgnoreProperties(ignoreUnknown = true)` on all 7 inner classes.

### Testing

- **Unit tests**: `MemberSyncServiceTest`, `CommitteeSyncServiceTest`, `TermSyncServiceTest`
- **Framework**: JUnit 5 + Mockito
- **Run**: `cd backend && ./mvnw test -Dtest="MemberSyncServiceTest,CommitteeSyncServiceTest,TermSyncServiceTest"`
- **Coverage**: JaCoCo 70% enforced

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (H1, H2, H3) | Sarah (PO) |
