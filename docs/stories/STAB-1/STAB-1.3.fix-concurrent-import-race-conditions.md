# Story STAB-1.3: Fix Concurrent Import Race Conditions

## Status

**Done**

---

## Story

**As a** system,
**I want** concurrent import prevention to be thread-safe,
**so that** two simultaneous HTTP requests cannot both start the same import operation, which could corrupt data.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | All `volatile boolean` concurrency flags are replaced with `AtomicBoolean` using `compareAndSet()` |
| AC2 | `AdminSyncController` — all 4 import endpoints (PLUM, US Code, Presidential, EO) use atomic concurrency guards |
| AC3 | `GovmanImportController` — import endpoint uses atomic concurrency guard |
| AC4 | `StatuteImportController` — upload endpoint uses atomic concurrency guard |
| AC5 | The `finally` block correctly resets the flag even on exceptions |
| AC6 | Existing controller tests pass; new tests verify concurrent request rejection |

---

## Tasks / Subtasks

- [ ] **Task 1: Fix AdminSyncController** (AC1, AC2, AC5)
  - [ ] Replace 4 `volatile boolean` fields with `AtomicBoolean` initialized to `false`
  - [ ] Replace `if (flag) { return 409 }; flag = true;` pattern with:
    ```java
    if (!flag.compareAndSet(false, true)) { return 409; }
    ```
  - [ ] Keep `flag.set(false)` in existing `finally` blocks
  - [ ] Apply to: `plumImportInProgress`, `usCodeImportInProgress`, `presidentialSyncInProgress`, `eoSyncInProgress`

- [ ] **Task 2: Fix GovmanImportController** (AC1, AC3, AC5)
  - [ ] Replace `volatile boolean importInProgress` with `AtomicBoolean`
  - [ ] Apply same `compareAndSet` pattern

- [ ] **Task 3: Fix StatuteImportController** (AC1, AC4, AC5)
  - [ ] Replace `volatile boolean importInProgress` with `AtomicBoolean`
  - [ ] Apply same `compareAndSet` pattern

- [ ] **Task 4: Update tests** (AC6)
  - [ ] Verify existing controller tests still pass
  - [ ] Add test that simulates concurrent requests (verify second request gets 409)

---

## Dev Notes

### The Race Condition
Current code:
```java
if (plumImportInProgress) {        // Thread A reads false
    return ResponseEntity.status(409); // Thread B also reads false (race window)
}
plumImportInProgress = true;         // Both threads set true, both proceed
```

`volatile` guarantees visibility but NOT atomicity of the check-then-set operation.

### Fix Pattern
```java
private final AtomicBoolean plumImportInProgress = new AtomicBoolean(false);

// In endpoint:
if (!plumImportInProgress.compareAndSet(false, true)) {
    return ResponseEntity.status(409).build();
}
try {
    // ... import logic
} finally {
    plumImportInProgress.set(false);
}
```

`compareAndSet` is a single atomic operation — no race window.

### Key Files
| File | Path | Lines | Change |
|------|------|-------|--------|
| AdminSyncController | `backend/src/main/java/org/newsanalyzer/controller/AdminSyncController.java` | 50, 53, 56, 59, 89-95, 175-181, 305-311, 388-394 | Replace volatile with AtomicBoolean |
| GovmanImportController | `backend/src/main/java/org/newsanalyzer/controller/GovmanImportController.java` | 50, 73, 100 | Replace volatile with AtomicBoolean |
| StatuteImportController | `backend/src/main/java/org/newsanalyzer/controller/StatuteImportController.java` | 63, 88, 126 | Replace volatile with AtomicBoolean |

### Import Required
```java
import java.util.concurrent.atomic.AtomicBoolean;
```

### Testing

- **Unit tests**: Controller test classes in `backend/src/test/java/org/newsanalyzer/controller/`
- **Framework**: JUnit 5 + MockMvc
- **Run**: `cd backend && ./mvnw test -Dtest="AdminSyncControllerTest,GovmanImportControllerTest,StatuteImportControllerTest"`

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (C5) | Sarah (PO) |
