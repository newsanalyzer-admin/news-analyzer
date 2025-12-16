# ADR-QA-003: Mock vs Live Testing Strategy

## Status

**ACCEPTED** - 2025-11-25

## Context

The API integration testing framework needs to test two services:
1. **Java Backend** (Spring Boot) - Port 8080
2. **Python Reasoning Service** (FastAPI) - Port 8000

Additionally, the reasoning service integrates with external knowledge bases:
- **Wikidata** - SPARQL endpoint for entity linking
- **DBpedia** - RDF lookup for fallback entity resolution

We need to decide whether tests should run against:
- Live, running services (integration testing)
- Mocked responses (unit-style API testing)
- A combination of both

### Options Considered

1. **Live Services Only** - All tests require services running
2. **Mocked Services Only** - All tests use WireMock stubs
3. **Hybrid Approach** - Profile-based switching between mock and live

## Decision

**Option 3: Hybrid Approach with Profile-Based Switching**

The test framework will support both mocked and live testing via Maven profiles:

```bash
# Run with mocked services (fast, deterministic)
mvn test -Pmock

# Run with live services (requires services running)
mvn test -Plocal

# Run in CI (live internal services, mocked external KBs)
mvn test -Pci
```

### Test Categorization

| Test Type | Internal Services | External KBs | Profile |
|-----------|-------------------|--------------|---------|
| Unit-style API | WireMock | WireMock | `mock` |
| Integration | Live | WireMock | `ci` |
| Full E2E | Live | Live | `local` (optional) |

### Default Strategy

- **CI Pipeline:** Live internal services + Mocked external KBs (`-Pci`)
- **Local Development:** Developer choice, typically `-Plocal`
- **Quick Validation:** Mocked everything (`-Pmock`)

## Rationale

### Why Not Live Services Only?

1. **Slow Feedback** - Requires full stack running; startup overhead
2. **Flaky External KBs** - Wikidata/DBpedia can timeout or rate-limit
3. **CI Complexity** - Must orchestrate service startup before tests
4. **Debugging Difficulty** - Hard to isolate failures

### Why Not Mocked Services Only?

1. **False Confidence** - Mocks may not reflect real service behavior
2. **Integration Gaps** - Won't catch actual API contract violations
3. **Drift Risk** - Mocks can become stale as services evolve
4. **Missing Value** - Defeats purpose of "integration" testing

### Why Hybrid Approach?

1. **Best of Both Worlds** - Fast unit tests + thorough integration tests
2. **Developer Flexibility** - Choose speed vs thoroughness as needed
3. **CI Reliability** - Mock external dependencies that cause flakiness
4. **Layered Testing Pyramid** - Supports multiple test granularities

## Implementation Details

### WireMock Configuration

```java
@WireMockTest(httpPort = 8080)
class MockedBackendTest {

    @BeforeEach
    void setupStubs(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(get(urlEqualTo("/api/entities"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("[{\"id\": \"...\", \"name\": \"EPA\"}]")));
    }
}
```

### Profile-Based Configuration

```xml
<!-- pom.xml -->
<profiles>
    <profile>
        <id>local</id>
        <properties>
            <test.backend.url>http://localhost:8080</test.backend.url>
            <test.reasoning.url>http://localhost:8000</test.reasoning.url>
            <test.mock.enabled>false</test.mock.enabled>
        </properties>
    </profile>
    <profile>
        <id>ci</id>
        <properties>
            <test.backend.url>http://localhost:8080</test.backend.url>
            <test.reasoning.url>http://localhost:8000</test.reasoning.url>
            <test.mock.external>true</test.mock.external>
        </properties>
    </profile>
    <profile>
        <id>mock</id>
        <properties>
            <test.mock.enabled>true</test.mock.enabled>
        </properties>
    </profile>
</profiles>
```

### Test Tagging Strategy

```java
@Tag("unit")        // Runs with mocked services
@Tag("integration") // Runs with live internal services
@Tag("e2e")         // Runs with everything live
@Tag("external")    // Requires external KB access (Wikidata/DBpedia)
```

### External KB Mocking (CI Default)

```java
// Mock Wikidata SPARQL responses
stubFor(post(urlPathEqualTo("/sparql"))
    .withHeader("Accept", containing("application/json"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBodyFile("wikidata-epa-response.json")));

// Mock DBpedia lookup
stubFor(get(urlPathMatching("/resource/.*"))
    .willReturn(aResponse()
        .withStatus(200)
        .withBodyFile("dbpedia-entity-response.json")));
```

## Consequences

### Positive

- Fast feedback loop with mocked tests
- Reliable CI with mocked external dependencies
- Thorough validation with live integration tests
- Flexibility for different development scenarios
- Clear test categorization via tags

### Negative

- More configuration complexity (multiple profiles)
- Must maintain mock response files
- Risk of mock/reality drift if not regularly validated
- Developers must understand which profile to use

### Neutral

- Two test execution modes to document
- CI pipeline uses different profile than local development

## Testing Pyramid Alignment

```
        /\
       /  \  E2E (Live everything) - Occasional
      /----\
     /      \  Integration (Live internal, mock external) - CI
    /--------\
   /          \  Unit-style API (All mocked) - Fast, frequent
  /------------\
```

## Related Decisions

- [ADR-QA-001](ADR-QA-001-api-test-project-location.md) - API Test Project Location
- [ADR-QA-002](ADR-QA-002-test-database-strategy.md) - Test Database Strategy

## References

- [Epic QA-1: API Integration Testing Framework](../../stories/QA-1.epic-api-testing-framework.md)
- [Story QA-1.2: Backend API Test Suite](../../stories/QA-1.2.backend-api-test-suite.md)
- [Story QA-1.3: Reasoning Service Test Suite](../../stories/QA-1.3.reasoning-service-test-suite.md)
- [WireMock Documentation](https://wiremock.org/docs/)

---

**Decision Made By:** Winston (System Architect)
**Date:** 2025-11-25
