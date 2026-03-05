# Story STAB-1.4: Add HTTP Timeouts to API Clients

## Status

**Done**

---

## Story

**As a** system,
**I want** all external API clients to have configured connection and read timeouts,
**so that** HTTP requests to external services do not hang indefinitely and block server threads.

---

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `FederalRegisterClient` RestTemplate uses the existing `timeout` config value from `FederalRegisterConfig` (30s) |
| AC2 | `CongressApiClient` RestTemplate uses the existing `timeout` config value from `CongressApiConfig` (30s) |
| AC3 | Both clients configure both connect timeout and read timeout |
| AC4 | Timeout values are sourced from the config classes, not hardcoded in clients |
| AC5 | A timed-out request throws a clear exception that is caught by existing retry logic |
| AC6 | Existing tests pass; no behavioral changes beyond timeout enforcement |

---

## Tasks / Subtasks

- [ ] **Task 1: Fix FederalRegisterClient RestTemplate** (AC1, AC3, AC4)
  - [ ] Inject `RestTemplateBuilder` into constructor (Spring Boot provides this bean)
  - [ ] Build RestTemplate with timeouts from config:
    ```java
    this.restTemplate = restTemplateBuilder
        .setConnectTimeout(Duration.ofMillis(config.getTimeout()))
        .setReadTimeout(Duration.ofMillis(config.getTimeout()))
        .build();
    ```
  - [ ] Remove bare `new RestTemplate()` constructor call

- [ ] **Task 2: Fix CongressApiClient RestTemplate** (AC2, AC3, AC4)
  - [ ] Inject `RestTemplateBuilder` into constructor
  - [ ] Build RestTemplate with timeouts from config
  - [ ] Remove bare `new RestTemplate()` constructor call

- [ ] **Task 3: Verify timeout exceptions are handled** (AC5)
  - [ ] Confirm `RestClientException` (thrown on timeout) is caught by existing `executeWithRetry()` in both clients
  - [ ] Confirm `ResourceAccessException` (a subclass for timeout) triggers retry as expected

- [ ] **Task 4: Update tests** (AC6)
  - [ ] Verify existing tests compile and pass (constructor signature changed)
  - [ ] Update any test mocks that construct the clients directly

---

## Dev Notes

### Current State
Both clients create `new RestTemplate()` with zero timeout configuration. This means:
- **Connect timeout**: Infinite (OS default, typically 60-120s)
- **Read timeout**: Infinite (will hang forever waiting for response)

The timeout config values already exist but are never used:
- `FederalRegisterConfig.timeout` = 30000ms (line 26)
- `CongressApiConfig.timeout` = 30000ms (line 36)

### Pattern Reference
`PlumConfig` already does this correctly:
```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(30))
        .setReadTimeout(Duration.ofMinutes(5))
        .build();
}
```

Also `LegislatorsRepoClient` correctly configures timeouts via `RestTemplateBuilder`.

### Key Files
| File | Path | Change |
|------|------|--------|
| FederalRegisterClient | `backend/src/main/java/org/newsanalyzer/service/FederalRegisterClient.java` | Inject RestTemplateBuilder, configure timeouts (line 42-46) |
| CongressApiClient | `backend/src/main/java/org/newsanalyzer/service/CongressApiClient.java` | Inject RestTemplateBuilder, configure timeouts |
| FederalRegisterConfig | `backend/src/main/java/org/newsanalyzer/config/FederalRegisterConfig.java` | No changes (timeout already defined) |
| CongressApiConfig | `backend/src/main/java/org/newsanalyzer/config/CongressApiConfig.java` | No changes (timeout already defined) |

### Constructor Change
Current:
```java
public FederalRegisterClient(FederalRegisterConfig config, ObjectMapper objectMapper) {
    this.restTemplate = new RestTemplate();
```

New:
```java
public FederalRegisterClient(FederalRegisterConfig config, ObjectMapper objectMapper, RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder
        .setConnectTimeout(Duration.ofMillis(config.getTimeout()))
        .setReadTimeout(Duration.ofMillis(config.getTimeout()))
        .build();
```

### Testing

- **Unit tests**: `FederalRegisterClientTest.java`, `CongressApiClientTest.java`
- **Framework**: JUnit 5 + Mockito
- **Note**: Tests that mock or construct clients directly will need constructor updates
- **Run**: `cd backend && ./mvnw test -Dtest="FederalRegisterClientTest,CongressApiClientTest"`

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-02 | 1.0 | Story created from STAB-1 audit (C6) | Sarah (PO) |
