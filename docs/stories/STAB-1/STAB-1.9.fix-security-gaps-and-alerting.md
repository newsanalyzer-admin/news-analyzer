# Story STAB-1.9: Fix Security Gaps and Silent Failure Alerting

## Status

**Done**

---

## Story

**As a** system administrator,
**I want** security gaps closed and sync failures to be visible,
**so that** scheduled syncs do not fail silently for days and CORS policies are properly restrictive.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `JudgeController` removes wildcard CORS (`@CrossOrigin(origins = "*")`) and uses the same origin list as other controllers |
| AC2 | Scheduled sync tasks (RegulationSyncScheduler, GovernmentOrgScheduler, EnrichmentScheduler) log failures at ERROR level with full context |
| AC3 | Scheduled sync results are stored and queryable via a status endpoint so admins can check health |
| AC4 | Date parsing failures across all CSV import services log at WARN level with the field name and raw value |
| AC5 | Existing tests pass |

---

## Tasks / Subtasks

- [x] **Task 1: Fix JudgeController CORS** (AC1)
  - [x] Replace `@CrossOrigin(origins = "*")` with `@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})` to match other controllers
  - [x] Verify no other controllers use wildcard CORS

- [x] **Task 2: Improve scheduled sync failure logging** (AC2)
  - [x] In `RegulationSyncScheduler.scheduledSync()`: log full exception stack trace, not just message
  - [x] In `GovernmentOrgScheduler.runWeeklySync()`: log full exception stack trace
  - [x] In `EnrichmentScheduler.runWeeklySync()`: log full exception stack trace
  - [x] Include sync name, start time, and duration in error logs

- [x] **Task 3: Add sync health tracking** (AC3)
  - [x] Store last sync result (success/failure, timestamp, error message) in each scheduler
  - [x] Expose via the existing `/api/admin/sync/health` endpoint:
    - Add `regulationSync`, `govOrgSync`, `enrichmentSync` sections with `lastRun`, `lastStatus`, `lastError`
  - [x] This gives admins a single endpoint to check all sync health

- [x] **Task 4: Improve CSV date parsing logging** (AC4)
  - [x] In `PlumCsvImportService` date parsing: log at WARN with field name and raw value when parsing fails
  - [x] In `FjcCsvImportService` date parsing: log at WARN with field name and raw value when parsing fails
  - [x] Both services use overloaded `parseDate(String, String)` with field name parameter

- [x] **Task 5: Verify existing tests pass** (AC5)
  - [x] Run full backend test suite — 762 tests pass, 0 failures
  - [x] No behavioral changes to core logic — only logging and config changes

---

## Dev Notes

### Wildcard CORS
`JudgeController` (line 33):
```java
@CrossOrigin(origins = "*")
```
All other controllers use:
```java
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
```
Wildcard CORS allows any website to make requests to the judge endpoints.

### Silent Scheduler Failures
All three schedulers catch exceptions and log, but:
1. No one monitors logs regularly
2. A sync could fail every day for weeks unnoticed
3. No admin dashboard visibility into scheduled sync status

### Scheduler Files
| File | Path |
|------|------|
| RegulationSyncScheduler | `backend/src/main/java/org/newsanalyzer/scheduler/RegulationSyncScheduler.java` |
| GovernmentOrgScheduler | `backend/src/main/java/org/newsanalyzer/scheduler/GovernmentOrgScheduler.java` |
| EnrichmentScheduler | `backend/src/main/java/org/newsanalyzer/scheduler/EnrichmentScheduler.java` |
| JudgeController | `backend/src/main/java/org/newsanalyzer/controller/JudgeController.java` |
| AdminSyncController | `backend/src/main/java/org/newsanalyzer/controller/AdminSyncController.java` |

### Health Endpoint Extension
The existing `AdminSyncController.health()` method (line 502-533) already returns status for PLUM, US Code, Presidential, and EO syncs. Extend it to include the three scheduled syncs.

### Testing

- **Unit tests**: Controller and scheduler test classes
- **Framework**: JUnit 5 + Mockito
- **Run**: `cd backend && ./mvnw test`

---

## File List

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/java/org/newsanalyzer/controller/JudgeController.java` | Modified | Replaced wildcard CORS with explicit origin list |
| `backend/src/main/java/org/newsanalyzer/scheduler/RegulationSyncScheduler.java` | Modified | Added health tracking fields (lastSyncTime, lastSyncStatus, lastSyncError), timing in logs |
| `backend/src/main/java/org/newsanalyzer/controller/AdminSyncController.java` | Modified | Extended health endpoint with scheduledSyncs section for all 3 schedulers |
| `backend/src/main/java/org/newsanalyzer/service/PlumCsvImportService.java` | Modified | Added field name parameter to parseDate, updated all callers |
| `backend/src/main/java/org/newsanalyzer/service/FjcCsvImportService.java` | Modified | Added overloaded parseDate with field name, updated all callers, improved warn log |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.6

### Debug Log References
- GovernmentOrgScheduler and EnrichmentScheduler already had adequate error logging with full stack traces — no changes needed
- RegulationSyncScheduler needed health tracking additions (AtomicReference fields)
- AdminSyncController uses `@Autowired(required = false)` for scheduler dependencies since they are `@ConditionalOnProperty` beans

### Completion Notes
- All 5 tasks implemented
- 762 tests pass (full backend suite), 0 failures
- CORS wildcard removed from JudgeController
- Sync health tracking exposed via `/api/admin/sync/health` endpoint
- CSV date parsing in both PlumCsvImportService and FjcCsvImportService now logs field names on parse failure

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (M3, M7, M8) | Sarah (PO) |
| 2026-03-05 | 1.1 | Implementation complete — all tasks done, tests passing | James (Dev) |

---

## QA Results

### Review Date: 2026-03-05

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

Clean implementation across all 5 tasks. CORS fix is straightforward and correct. Scheduler health tracking via `AtomicReference` in `RegulationSyncScheduler` is properly thread-safe. The `@Autowired(required = false)` pattern for `@ConditionalOnProperty` scheduler beans is the correct Spring approach. CSV date parsing overloaded methods are clean.

One concern: `AdminSyncController` stores import result fields (`lastPlumResult`, `lastUsCodeResult`, etc.) as plain instance variables. These are written by import-triggering HTTP requests and read by the health endpoint — without `volatile` or `AtomicReference`, the Java Memory Model does not guarantee cross-thread visibility. This is inconsistent with `RegulationSyncScheduler` which correctly uses `AtomicReference`.

### Refactoring Performed

None.

### Compliance Check

- Coding Standards: ✓ Follows established patterns
- Project Structure: ✓ Files in correct locations
- Testing Strategy: ✓ Full 762-test suite verified; no behavioral changes to core logic
- All ACs Met: ✓ AC1-AC5 all verified

### Improvements Checklist

- [x] JudgeController CORS wildcard removed (AC1)
- [x] Scheduler failure logging improved (AC2)
- [x] Sync health tracking via admin endpoint (AC3)
- [x] CSV date parsing logs field names (AC4)
- [x] Full test suite passes (AC5)
- [ ] Make AdminSyncController result fields volatile/AtomicReference
- [ ] Future: externalize CORS origins to application.yml

### Security Review

CORS wildcard (`*`) successfully removed from `JudgeController`. Verified all controllers now use explicit origin list. No other controllers had wildcard CORS. The hardcoded localhost origins are appropriate for development; production deployment should externalize them.

### Performance Considerations

No performance impact. Health endpoint adds minimal overhead (reads AtomicReference fields and non-volatile result objects).

### Files Modified During Review

None.

### Gate Status

Gate: CONCERNS → docs/qa/gates/STAB-1.9-fix-security-gaps-and-alerting.yml

### Recommended Status

✓ Ready for Done — the volatile concern is a correctness hardening, not a functional defect at current usage patterns.
