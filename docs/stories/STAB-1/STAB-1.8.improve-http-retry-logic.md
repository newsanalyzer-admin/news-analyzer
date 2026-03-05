# Story STAB-1.8: Improve HTTP Retry Logic and Error Differentiation

## Status

**Done**

---

## Story

**As a** system,
**I want** API clients to distinguish between retryable and non-retryable HTTP errors,
**so that** 404s are not wastefully retried and API keys are not exposed in logs.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `FederalRegisterClient.executeWithRetry()` only retries on 5xx and network errors — 4xx errors fail immediately |
| AC2 | `CongressApiClient.executeWithRetry()` only retries on 5xx and network errors — 4xx errors fail immediately |
| AC3 | 429 (rate limit) responses trigger a longer backoff delay, not standard retry |
| AC4 | API keys are not logged in URLs, error messages, or stack traces (Congress.gov `api_key` query param) |
| AC5 | Error logs include HTTP status code when available for diagnostics |
| AC6 | Existing tests pass; retry behavior change does not break expected flows |

---

## Tasks / Subtasks

- [x] **Task 1: Add HTTP status-aware retry to FederalRegisterClient** (AC1, AC3, AC5)
  - [x] In catch block, inspect exception type: HttpClientErrorException (4xx no retry), HttpServerErrorException (5xx retry), ResourceAccessException (retry)
  - [x] For 429 specifically: extract `Retry-After` header if present, use as delay (default 10s)
  - [x] Log HTTP status code in warning messages

- [x] **Task 2: Add HTTP status-aware retry to CongressApiClient** (AC2, AC3, AC5)
  - [x] Apply same pattern as Task 1
  - [x] Fixed infinite loop bug (same as STAB-1.7 fix in FederalRegisterClient)
  - [x] Consistent error handling across both clients

- [x] **Task 3: Sanitize API keys from logs** (AC4)
  - [x] Created `CongressApiUtils.sanitizeUrl()` utility — strips `api_key` query param via regex
  - [x] Applied to all `log.warn()` and `log.error()` calls in CongressApiClient that include request URLs

- [x] **Task 4: Update tests** (AC6)
  - [x] Added test verifying 404 is not retried (1 call only)
  - [x] Added test verifying 500 is retried (2 calls)
  - [x] Added URL sanitization tests (4 tests)
  - [x] All 33 tests pass

---

## Dev Notes

### Current Retry Behavior
Both clients retry ALL exceptions the same way:
```java
catch (RestClientException e) {
    attempt++;
    Thread.sleep(delayMs);
    delayMs *= 2;
}
```
This means a 404 (document genuinely not found) is retried 3 times with exponential backoff — wasting ~7 seconds per failed lookup.

### RestClientException Hierarchy
```
RestClientException
├── HttpStatusCodeException
│   ├── HttpClientErrorException (4xx) — do NOT retry
│   └── HttpServerErrorException (5xx) — retry
└── ResourceAccessException (network/timeout) — retry
```

### API Key Exposure
`CongressApiClient` adds the API key as a query parameter:
```java
.queryParam("api_key", config.getKey())
```
This URL is logged in error messages, making the key visible in log files.

### Key Files
| File | Path | Change |
|------|------|--------|
| FederalRegisterClient | `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | Status-aware retry (line 248-280) |
| CongressApiClient | `backend/src/main/java/org/newsanalyzer/service/CongressApiClient.java` | Status-aware retry, key sanitization |

### Testing

- **Unit tests**: `FederalRegisterClientTest`, `CongressApiClientTest`
- **Framework**: JUnit 5 + Mockito
- **Run**: `cd backend && ./mvnw test -Dtest="FederalRegisterClientTest,CongressApiClientTest"`

---

## File List

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | Modified | HTTP status-aware retry with 4xx/5xx/429/network differentiation |
| `backend/src/main/java/org/newsanalyzer/service/CongressApiClient.java` | Modified | HTTP status-aware retry, URL sanitization in logs, fixed infinite loop |
| `backend/src/main/java/org/newsanalyzer/service/CongressApiUtils.java` | Modified | Added `sanitizeUrl()` method to strip api_key from URLs |
| `backend/src/test/java/org/newsanalyzer/service/FederalRegisterClientTest.java` | Modified | Added 404/500 retry behavior tests |
| `backend/src/test/java/org/newsanalyzer/service/CongressApiUtilsTest.java` | Modified | Added URL sanitization tests |

---

## Dev Agent Record

### Agent Model Used
Claude Opus 4.6

### Debug Log References
- Fixed infinite loop bug in `CongressApiClient.executeWithRetry()` — same pattern as STAB-1.7 FederalRegisterClient fix
- Note: Story subtask suggested using `restTemplate.exchange()` but catching specific exception subclasses achieves the same goal without changing the request method

### Completion Notes
- All 4 tasks implemented
- 33 tests pass (17 FederalRegisterClientTest + 10 CongressApiUtilsTest + 6 CongressApiClientTest)
- Both API clients now share consistent retry semantics: 4xx = fail fast, 5xx/network = retry, 429 = extended backoff

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (M1, M2) | Sarah (PO) |
| 2026-03-05 | 1.1 | Implementation complete — all tasks done, tests passing | James (Dev) |

---

## QA Results

### Review Date: 2026-03-05

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

Strong implementation with consistent retry semantics across both API clients. The HTTP exception hierarchy usage (`HttpClientErrorException` for 4xx, `HttpServerErrorException` for 5xx, `ResourceAccessException` for network) is the correct Spring pattern. URL sanitization regex is well-crafted. `sleepQuietly()` correctly restores interrupt status.

Three concerns identified:
1. `parseRetryAfter()` swallows `NumberFormatException` silently — no log output on malformed Retry-After headers
2. `CongressApiClient.checkRateLimit()` has a TOCTOU race on window reset (AtomicLong check + set not atomic together)
3. No test coverage for 429 with Retry-After header parsing

### Refactoring Performed

None — concerns are non-blocking at current scale.

### Compliance Check

- Coding Standards: ✓ Consistent naming, proper exception hierarchy usage
- Project Structure: ✓ CongressApiUtils in correct package
- Testing Strategy: ✓ 33 tests; 404/500 retry behavior verified; URL sanitization tested
- All ACs Met: ✓ AC1-AC6 all verified

### Improvements Checklist

- [x] FederalRegisterClient status-aware retry (AC1)
- [x] CongressApiClient status-aware retry (AC2)
- [x] 429 rate limit extended backoff (AC3)
- [x] API key sanitization from logs (AC4)
- [x] HTTP status codes in log messages (AC5)
- [x] Tests pass, retry behavior verified (AC6)
- [ ] Add log.debug for malformed Retry-After headers
- [ ] Add test for 429 + Retry-After header parsing
- [ ] Consider extracting retry/backoff helpers to shared utility

### Security Review

API key sanitization is properly implemented. Regex pattern `([?&])api_key=[^&]*` correctly handles both query parameter positions. All log.warn/log.error calls in CongressApiClient use sanitized URLs. No new security vulnerabilities.

### Performance Considerations

`Thread.sleep()` blocks HTTP request handler threads during retry. Acceptable for low-traffic admin/import endpoints but would be problematic at scale. Both clients duplicate `parseRetryAfter()` and `sleepQuietly()` — could be extracted to shared utility if a third client is added.

### Files Modified During Review

None.

### Gate Status

Gate: CONCERNS → docs/qa/gates/STAB-1.8-improve-http-retry-logic.yml

### Recommended Status

✓ Ready for Done — CONCERNS items are minor hardening improvements, not blockers.
