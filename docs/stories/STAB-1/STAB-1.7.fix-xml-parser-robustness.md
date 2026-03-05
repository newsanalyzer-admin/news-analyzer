# Story STAB-1.7: Fix XML Parser Robustness and Resource Management

## Status

**Done**

---

## Story

**As a** system,
**I want** XML parsers to have depth limits and API clients to report errors clearly,
**so that** crafted XML cannot crash the server and deserialization failures are distinguishable from "not found" responses.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `UslmXmlParser.readElementText()` enforces a maximum nesting depth (e.g., 100) to prevent StackOverflowError |
| AC2 | `FederalRegisterClient` distinguishes deserialization errors from "not found" — callers receive actionable error context |
| AC3 | `GovmanImportController` wraps `file.getInputStream()` in try-with-resources to prevent resource leaks |
| AC4 | Deeply nested XML (100+ levels) is rejected with a clear error, not a JVM crash |
| AC5 | `FederalRegisterImportService` reports "parse error" vs "document not found" in its result DTO |
| AC6 | Existing tests pass; new tests cover depth limit and error differentiation |

---

## Tasks / Subtasks

- [x] **Task 1: Add depth limit to UslmXmlParser** (AC1, AC4)
  - [x] Add constant: `private static final int MAX_NESTING_DEPTH = 100;`
  - [x] Modify `readElementText()` to accept a depth parameter
  - [x] Throw `XMLStreamException("Maximum XML nesting depth exceeded")` when depth > MAX_NESTING_DEPTH
  - [x] Update all callers of `readElementText()` to pass initial depth of 0

- [x] **Task 2: Improve FederalRegisterClient error reporting** (AC2)
  - [x] In `fetchAllAgencies()` catch block (line 68-70): distinguish `JsonMappingException`/`JsonProcessingException` from other exceptions
  - [x] Log the full exception (not just message) for deserialization errors: `log.error("...", e)` not `log.error("...: {}", e.getMessage())`
  - [x] Consider returning a result wrapper instead of empty list/Optional, OR add a method that throws so callers can distinguish
  - [x] At minimum: log at ERROR level with enough context to diagnose (the field that failed, first 500 chars of response)

- [x] **Task 3: Improve FederalRegisterImportService error context** (AC5)
  - [x] When `federalRegisterClient.fetchDocument()` returns empty, check if it was a parse error or genuinely not found
  - [x] If parse error: return result with error "Failed to parse document from Federal Register API"
  - [x] If not found: return result with error "Document not found on Federal Register"
  - [x] Currently both cases return "Document not found" which is misleading

- [x] **Task 4: Fix resource leak in GovmanImportController** (AC3)
  - [x] Wrap `file.getInputStream()` call in try-with-resources:
    ```java
    try (InputStream stream = file.getInputStream()) {
        GovmanImportResult result = importService.importFromStream(stream);
    }
    ```

- [x] **Task 5: Update tests** (AC6)
  - [x] Add `UslmXmlParserTest` case with deeply nested XML (150 levels) — verify clean rejection
  - [x] Add `FederalRegisterClientTest` case verifying error log includes exception details
  - [x] Verify all existing tests pass

---

## Dev Notes

### UslmXmlParser Recursion
`readElementText()` (line ~269) calls itself recursively with no depth limit:
```java
} else if (event == XMLStreamConstants.START_ELEMENT) {
    sb.append(readElementText(reader)); // Recursive, unbounded
}
```
A crafted XML file with 10,000 nested elements will cause `StackOverflowError`, crashing the JVM thread.

### FederalRegisterClient Silent Errors
Current pattern (line 68-70):
```java
catch (Exception e) {
    log.error("Failed to parse Federal Register API response: {}", e.getMessage());
    return Collections.emptyList();
}
```
Callers see an empty list and cannot distinguish "API returned 0 agencies" from "parse failed on all agencies."

### GovmanImportController Resource Leak
Line 103: `file.getInputStream()` returns an InputStream that is never explicitly closed if `importService.importFromStream()` throws an unexpected exception.

### Key Files
| File | Path | Change |
|------|------|--------|
| UslmXmlParser | `backend/src/main/java/org/newsanalyzer/service/UslmXmlParser.java` | Add depth limit to readElementText() |
| FederalRegisterClient | `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | Improve error logging and reporting |
| FederalRegisterImportService | `backend/src/main/java/org/newsanalyzer/service/FederalRegisterImportService.java` | Differentiate parse error vs not found |
| GovmanImportController | `backend/src/main/java/org/newsanalyzer/controller/GovmanImportController.java` | Try-with-resources on line 103 |

### Testing

- **Unit tests**: `UslmXmlParserTest`, `FederalRegisterClientTest`
- **Framework**: JUnit 5 + Mockito
- **Run**: `cd backend && ./mvnw test -Dtest="UslmXmlParserTest,FederalRegisterClientTest"`
- **Coverage**: JaCoCo 70% enforced

---

## File List

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/java/org/newsanalyzer/service/UslmXmlParser.java` | Modified | Added MAX_NESTING_DEPTH constant and depth parameter to readElementText() |
| `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | Modified | Improved error logging with full stack traces, response snippets; fixed infinite loop in executeWithRetry(); fetchDocument() now throws FederalRegisterParseException |
| `backend/src/main/java/org/newsanalyzer/service/FederalRegisterParseException.java` | New | Custom exception to distinguish parse errors from not-found |
| `backend/src/main/java/org/newsanalyzer/service/FederalRegisterImportService.java` | Modified | Catches FederalRegisterParseException to differentiate parse error vs not found |
| `backend/src/main/java/org/newsanalyzer/controller/GovmanImportController.java` | Modified | Wrapped file.getInputStream() in try-with-resources |
| `backend/src/test/java/org/newsanalyzer/service/UslmXmlParserTest.java` | Modified | Added depth limit tests (150-level rejection, 10-level success) |
| `backend/src/test/java/org/newsanalyzer/service/FederalRegisterClientTest.java` | Modified | Added error differentiation tests (parse error, not found, invalid JSON) |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.6

### Debug Log References
- Fixed infinite loop bug in `FederalRegisterClient.executeWithRetry()` — `attempt` counter only incremented inside catch block, causing null responses to spin forever. Converted to `for` loop with proper increment on each iteration.

### Completion Notes
- All 5 tasks implemented and verified
- 29 tests pass (15 FederalRegisterClientTest + 14 UslmXmlParserTest)
- Bonus fix: `executeWithRetry()` infinite loop on null API responses (discovered during test execution)
- Most implementation was already present from prior uncommitted sessions; validated and fixed the retry loop bug

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (H6, H7, H8) | Sarah (PO) |
| 2026-03-05 | 1.1 | Implementation complete — all tasks done, tests passing | James (Dev) |

---

## QA Results

### Review Date: 2026-03-05

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

Solid implementation across all 5 tasks. The infinite loop fix in `executeWithRetry()` (discovered during testing) was a significant bonus catch — a latent production bug. `FederalRegisterParseException` is well-designed for its purpose. `readElementText()` depth limiting is clean and well-tested.

One asymmetry identified: `readElementXml()` does NOT enforce `MAX_NESTING_DEPTH` while `readElementText()` does. `readElementXml()` is iterative (not recursive), so no StackOverflow risk, but extremely deep nesting could accumulate unbounded StringBuilder memory. Low practical risk but inconsistent defense.

### Refactoring Performed

None — code quality is acceptable as-is.

### Compliance Check

- Coding Standards: ✓ K&R braces, proper naming, class organization follows conventions
- Project Structure: ✓ Files in correct packages
- Testing Strategy: ✓ 29 tests covering depth limits, error differentiation, retry behavior
- All ACs Met: ✓ AC1-AC6 all verified

### Improvements Checklist

- [x] Depth limit on readElementText() (AC1, AC4)
- [x] FederalRegisterClient error reporting improved (AC2)
- [x] FederalRegisterImportService parse vs not-found differentiation (AC5)
- [x] GovmanImportController try-with-resources (AC3)
- [x] Tests for depth limit and error differentiation (AC6)
- [x] Bonus: fixed infinite loop in executeWithRetry()
- [ ] Add depth limit to readElementXml() for parity (future improvement)

### Security Review

XXE protection confirmed in UslmXmlParser (external entities disabled, DTD disallowed). Depth limit prevents stack exhaustion attacks on readElementText(). No new security vulnerabilities introduced.

### Performance Considerations

Infinite loop fix eliminates a latent performance catastrophe (null API responses would spin forever). No negative performance impact from depth checking.

### Files Modified During Review

None.

### Gate Status

Gate: CONCERNS → docs/qa/gates/STAB-1.7-fix-xml-parser-robustness.yml

### Recommended Status

✓ Ready for Done — the CONCERNS item (readElementXml depth limit) is a future improvement, not a blocker.
