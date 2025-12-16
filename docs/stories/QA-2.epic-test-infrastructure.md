# Epic QA-2: Test Infrastructure Improvements

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | QA-2 |
| **Epic Name** | Test Infrastructure Improvements |
| **Epic Type** | Technical Debt / Infrastructure |
| **Priority** | LOW (Post-MVP) |
| **Status** | Draft |
| **Created** | 2025-12-01 |
| **Owner** | Sarah (PO) |
| **Depends On** | None |

## Executive Summary

Address technical debt in the test infrastructure by implementing Testcontainers for real PostgreSQL testing, adding frontend component tests, and improving CI/CD documentation. This epic resolves the current workaround of disabled repository tests due to H2/PostgreSQL JSONB incompatibility.

## Business Value

### Why This Epic Matters

1. **Test Confidence** - Repository tests currently disabled; real PostgreSQL testing ensures JSONB queries work correctly
2. **Frontend Quality** - No frontend tests exist; component tests prevent UI regressions
3. **Developer Experience** - Better test infrastructure reduces debugging time
4. **CI/CD Reliability** - Improved documentation ensures consistent deployments

### Success Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Repository test coverage | 100% enabled | 17 tests disabled |
| Frontend test coverage | >60% components | 0% |
| CI/CD documentation | Complete | Partial |
| Test suite reliability | >99% pass rate | N/A (tests disabled) |

## Scope

### In Scope

- Testcontainers integration for backend repository tests
- Re-enable disabled EntityRepositoryTest tests
- Frontend component testing with React Testing Library
- Frontend integration tests for key user flows
- CI/CD pipeline documentation
- Test environment setup guide

### Out of Scope

- E2E browser testing (Playwright/Cypress) - future enhancement
- Performance/load testing - separate epic
- Security testing automation - separate epic
- Mobile testing - not applicable

## Architecture

### Testcontainers Integration

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Execution                            │
│                   (Maven Surefire)                           │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                  Testcontainers                              │
│  - PostgreSQL 15 container                                   │
│  - Auto-start/stop per test class                            │
│  - Flyway migrations applied                                 │
└─────────────────────┬───────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────────┐
│              Real PostgreSQL Instance                        │
│  - JSONB support ✅                                          │
│  - Full-text search ✅                                       │
│  - All extensions ✅                                         │
└─────────────────────────────────────────────────────────────┘
```

### Frontend Testing Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Runner                               │
│                   (Jest / Vitest)                            │
└─────────────────────┬───────────────────────────────────────┘
                      │
          ┌───────────┼───────────┐
          ▼           ▼           ▼
    ┌──────────┐ ┌──────────┐ ┌──────────┐
    │  Unit    │ │Component │ │Integration│
    │  Tests   │ │  Tests   │ │  Tests   │
    │(utils)   │ │(RTL)     │ │(MSW)     │
    └──────────┘ └──────────┘ └──────────┘
```

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| QA-2.1 | **Testcontainers for Repository Tests** - Add Testcontainers dependency, configure PostgreSQL container, re-enable and verify EntityRepositoryTest | P0 | 0.5 sprint | None |
| QA-2.2 | **Frontend Component Testing Setup** - Add Jest/Vitest + React Testing Library, create test utilities, add tests for key components | P1 | 1 sprint | None |
| QA-2.3 | **CI/CD Documentation** - Document GitHub Actions workflows, test environment setup, deployment procedures | P2 | 0.25 sprint | None |

### Dependency Graph

```
QA-2.1 (Testcontainers)     QA-2.2 (Frontend Tests)
    │                            │
    └────────────┬───────────────┘
                 │
                 ▼
           QA-2.3 (CI/CD Docs)
```

### Implementation Order

**Sprint 1:**
- QA-2.1: Testcontainers for Repository Tests (parallel with other work)
- QA-2.2: Frontend Component Testing Setup (if capacity)

**Sprint 2:**
- QA-2.3: CI/CD Documentation

## Technical Details

### QA-2.1: Testcontainers Setup

**Dependencies to Add (pom.xml):**
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.3</version>
    <scope>test</scope>
</dependency>
```

**Test Configuration:**
```java
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EntityRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### QA-2.2: Frontend Testing Setup

**Dependencies to Add (package.json):**
```json
{
  "devDependencies": {
    "@testing-library/react": "^14.1.0",
    "@testing-library/jest-dom": "^6.1.0",
    "@testing-library/user-event": "^14.5.0",
    "vitest": "^1.0.0",
    "jsdom": "^23.0.0",
    "msw": "^2.0.0"
  }
}
```

**Priority Components to Test:**
1. `MemberTable` - Core listing functionality
2. `MemberFilters` - Filter state management
3. `CommitteeHierarchy` - Nested data display
4. `SyncStatusCard` - Admin dashboard

## Acceptance Criteria (Epic Level)

1. **Repository Tests:** All 17 EntityRepositoryTest tests passing with Testcontainers
2. **Frontend Coverage:** >60% of congressional components have tests
3. **CI Integration:** Tests run in GitHub Actions pipeline
4. **Documentation:** CI/CD guide covers all workflows
5. **No Regressions:** All existing tests continue to pass

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Docker not available in CI | HIGH | LOW | GitHub Actions has Docker; document setup |
| Testcontainers slow | MEDIUM | MEDIUM | Use reusable containers, parallel tests |
| Frontend test flakiness | MEDIUM | MEDIUM | Use Testing Library best practices |
| Breaking existing tests | LOW | LOW | Run full suite before/after changes |

## Definition of Done

- [ ] All 3 stories completed and merged
- [ ] EntityRepositoryTest fully enabled and passing
- [ ] Frontend test suite runs in CI
- [ ] CI/CD documentation complete
- [ ] No increase in disabled tests
- [ ] Build time increase <2 minutes

## Related Documentation

- [ADR-QA-002: Test Database Strategy](../architecture/adr/ADR-QA-002-test-database-strategy.md)
- [QA-1 Epic](QA-1/QA-1.epic-api-testing-framework.md)
- [Coding Standards - Testing](../architecture/coding-standards.md#testing)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-01 | 1.0 | Initial epic creation | Sarah (PO) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2025-12-01 | **DRAFTED** |
| Architect | _Pending_ | _Pending_ | _Pending_ |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
