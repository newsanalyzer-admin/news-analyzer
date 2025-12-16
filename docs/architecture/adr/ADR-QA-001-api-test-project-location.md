# ADR-QA-001: API Test Project Location

## Status

**ACCEPTED** - 2025-11-25

## Context

The NewsAnalyzer v2 system is a polyglot monorepo containing three services:
- `backend/` - Java Spring Boot REST API
- `frontend/` - Next.js React application
- `reasoning-service/` - Python FastAPI service

We need to establish a comprehensive API integration testing framework using Java REST Assured that can test both the Java backend and Python reasoning service. The question is: where should this test project live?

### Options Considered

1. **Inside `backend/` as a submodule** - Embedded within the Java backend project
2. **Inside each service** - Distributed tests in each service directory
3. **At repository root as `api-tests/`** - Separate sibling project

## Decision

**Option 3: Place API tests at repository root as `api-tests/`**

The API testing framework will be a standalone Maven project located at `api-tests/` as a sibling to the existing service directories.

```
AIProject2/
├── backend/                    # Java Spring Boot (existing)
├── frontend/                   # Next.js (existing)
├── reasoning-service/          # Python FastAPI (existing)
├── api-tests/                  # NEW - REST Assured API Tests
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── main/java/...
│       └── test/java/...
└── .github/workflows/
    └── api-tests.yml
```

## Rationale

### Why Not Inside `backend/`?

1. **Scope Mismatch** - These tests cover BOTH backend AND reasoning service; embedding in backend implies backend-only scope
2. **Build Coupling** - Backend builds would include test framework dependencies unnecessarily
3. **Deployment Artifacts** - Backend JAR should not contain API test classes
4. **Team Ownership** - API tests may be owned by QA team, not backend team

### Why Not Distributed Inside Each Service?

1. **Cross-Service Tests** - Integration tests span multiple services; no natural home
2. **Technology Mismatch** - REST Assured (Java) doesn't belong in `reasoning-service/` (Python)
3. **Duplication** - Would require duplicating test utilities and configuration
4. **CI Complexity** - Multiple test projects to orchestrate

### Why Separate `api-tests/` at Root?

1. **Clean Separation** - Test code doesn't pollute service codebases
2. **Single Responsibility** - One project for all API testing concerns
3. **Independent Lifecycle** - Can version, build, and deploy independently
4. **Technology Freedom** - Can use Java regardless of target service language
5. **CI Flexibility** - Can run tests without rebuilding services
6. **Cross-Service Testing** - Natural home for integration tests spanning services
7. **Team Ownership** - Clear ownership by QA/testing team
8. **Monorepo Pattern** - Follows established monorepo best practices

## Consequences

### Positive

- Clear architectural boundaries
- Simplified CI/CD configuration
- Easy to add tests for new services
- No impact on service build times
- Tests serve as living API documentation

### Negative

- Another project to maintain
- Requires separate Maven build step in CI
- Must keep test project in sync with API changes

### Neutral

- Requires developers to navigate to separate directory for tests
- Test reports generated in separate location

## Related Decisions

- [ADR-QA-002](ADR-QA-002-test-database-strategy.md) - Test Database Strategy
- [ADR-QA-003](ADR-QA-003-mock-vs-live-testing.md) - Mock vs Live Testing

## References

- [Epic QA-1: API Integration Testing Framework](../../stories/QA-1.epic-api-testing-framework.md)
- [Story QA-1.1: Project Setup & Maven Configuration](../../stories/QA-1.1.api-test-project-setup.md)

---

**Decision Made By:** Winston (System Architect)
**Date:** 2025-11-25
