# Epic QA-1: API Integration Testing Framework

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | QA-1 |
| **Epic Name** | API Integration Testing Framework |
| **Epic Type** | Quality Assurance / Infrastructure |
| **Priority** | HIGH |
| **Status** | Done |
| **Created** | 2025-11-25 |
| **Owner** | Sarah (PO) |

## Executive Summary

Establish a dedicated, comprehensive API testing framework using Java REST Assured to ensure quality and reliability across the NewsAnalyzer polyglot architecture. This framework will enable automated testing of both the Java Spring Boot backend and Python FastAPI reasoning service, with full CI/CD integration via GitHub Actions.

## Business Value

### Why This Epic Matters

1. **Quality Assurance** - Catch API regressions before they reach production
2. **Developer Confidence** - Enable refactoring with automated safety net
3. **Documentation** - Tests serve as living API documentation
4. **CI/CD Integration** - Block broken code from merging via automated gates
5. **Cross-Service Validation** - Ensure services work correctly together

### Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Endpoint Coverage | >80% | JaCoCo report |
| Test Pass Rate | 100% | CI/CD pipeline |
| CI Pipeline Duration | <10 min | GitHub Actions timing |
| Regression Detection | 100% | No production API bugs |

## Scope

### In Scope

- REST Assured test project setup at `api-tests/`
- Backend API tests (Entity, Government Organization endpoints)
- Reasoning Service API tests (extraction, reasoning, linking)
- Database integration for test data management
- GitHub Actions CI/CD pipeline
- Cross-service integration tests

### Out of Scope

- Frontend E2E tests (handled by Playwright in frontend/)
- Performance/load testing (future epic)
- Security penetration testing (future epic)
- Unit tests within individual services (already exist)

## Architecture

### Project Structure

```
AIProject2/
├── backend/                    # Java Spring Boot (existing)
├── frontend/                   # Next.js (existing)
├── reasoning-service/          # Python FastAPI (existing)
├── api-tests/                  # NEW - REST Assured API Tests
│   ├── pom.xml
│   ├── README.md
│   └── src/
│       ├── main/java/org/newsanalyzer/apitests/
│       │   ├── config/         # Configuration classes
│       │   └── util/           # Test utilities
│       └── test/
│           ├── java/org/newsanalyzer/apitests/
│           │   ├── backend/    # Backend API tests
│           │   ├── reasoning/  # Reasoning service tests
│           │   ├── integration/# Cross-service tests
│           │   └── data/       # Database utilities
│           └── resources/
│               ├── application-local.properties
│               ├── application-ci.properties
│               └── seed/       # Test data SQL
└── .github/workflows/
    └── api-tests.yml           # NEW - CI/CD pipeline
```

### Technology Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Test Framework | JUnit 5 | 5.10.x |
| API Testing | REST Assured | 5.4.x |
| Assertions | AssertJ | 3.25.x |
| Mocking | WireMock | 3.x |
| Database | PostgreSQL | 15+ |
| Connection Pool | HikariCP | 5.x |
| Migrations | Flyway | 10.x |
| CI/CD | GitHub Actions | - |
| Build Tool | Maven | 3.9+ |
| Java Version | 17 (LTS) | - |

### Service Dependencies

```
┌─────────────────┐     ┌─────────────────┐
│   api-tests     │────▶│    Backend      │
│  (REST Assured) │     │  (Spring Boot)  │
└────────┬────────┘     └────────┬────────┘
         │                       │
         │              ┌────────▼────────┐
         │              │   PostgreSQL    │
         │              │  (Test DB)      │
         │              └─────────────────┘
         │
         │              ┌─────────────────┐
         └─────────────▶│ Reasoning Svc   │
                        │   (FastAPI)     │
                        └─────────────────┘
```

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Dependencies |
|----|-------|----------|----------|--------------|
| QA-1.1 | [Project Setup & Maven Configuration](QA-1.1.api-test-project-setup.md) | P0 | 1 sprint | None |
| QA-1.2 | [Backend API Test Suite](QA-1.2.backend-api-test-suite.md) | P0 | 2 sprints | QA-1.1 |
| QA-1.3 | [Reasoning Service Test Suite](QA-1.3.reasoning-service-test-suite.md) | P0 | 2 sprints | QA-1.1 |
| QA-1.4 | [Database Integration & Test Data](QA-1.4.database-integration-test-data.md) | P1 | 1 sprint | QA-1.1 |
| QA-1.5 | [CI/CD Pipeline Integration](QA-1.5.cicd-pipeline-integration.md) | P1 | 1 sprint | QA-1.1, QA-1.2, QA-1.3 |
| QA-1.6 | [Cross-Service Integration Tests](QA-1.6.cross-service-integration-tests.md) | P2 | 1.5 sprints | QA-1.2, QA-1.3, QA-1.4 |

### Dependency Graph

```
QA-1.1 (Project Setup)
    │
    ├──────────────┬──────────────┐
    ▼              ▼              ▼
QA-1.2         QA-1.3         QA-1.4
(Backend)    (Reasoning)    (Database)
    │              │              │
    └──────┬───────┘              │
           ▼                      │
        QA-1.5 ◀──────────────────┘
       (CI/CD)
           │
           ▼
        QA-1.6
    (Integration)
```

### Implementation Order

**Sprint 1:**
- QA-1.1: Project Setup & Maven Configuration

**Sprint 2:**
- QA-1.4: Database Integration & Test Data
- QA-1.2: Backend API Test Suite (start)

**Sprint 3:**
- QA-1.2: Backend API Test Suite (complete)
- QA-1.3: Reasoning Service Test Suite (start)

**Sprint 4:**
- QA-1.3: Reasoning Service Test Suite (complete)
- QA-1.5: CI/CD Pipeline Integration

**Sprint 5:**
- QA-1.6: Cross-Service Integration Tests

## API Endpoints Coverage

### Backend API (34 endpoints)

#### Entity Controller - 13 endpoints
| Endpoint | Test Coverage |
|----------|---------------|
| `POST /api/entities` | QA-1.2 |
| `POST /api/entities/validate` | QA-1.2 |
| `GET /api/entities` | QA-1.2 |
| `GET /api/entities/{id}` | QA-1.2 |
| `PUT /api/entities/{id}` | QA-1.2 |
| `DELETE /api/entities/{id}` | QA-1.2 |
| `POST /api/entities/{id}/validate` | QA-1.2 |
| `POST /api/entities/{id}/verify` | QA-1.2 |
| `GET /api/entities/type/{type}` | QA-1.2 |
| `GET /api/entities/schema-org-type/{type}` | QA-1.2 |
| `GET /api/entities/search` | QA-1.2 |
| `GET /api/entities/search/fulltext` | QA-1.2 |
| `GET /api/entities/recent` | QA-1.2 |

#### Government Organization Controller - 21 endpoints
| Endpoint | Test Coverage |
|----------|---------------|
| `GET /api/government-organizations` | QA-1.2 |
| `GET /api/government-organizations/active` | QA-1.2 |
| `GET /api/government-organizations/{id}` | QA-1.2 |
| `POST /api/government-organizations` | QA-1.2 |
| `PUT /api/government-organizations/{id}` | QA-1.2 |
| `DELETE /api/government-organizations/{id}` | QA-1.2 |
| `GET /api/government-organizations/search` | QA-1.2 |
| `GET /api/government-organizations/search/fuzzy` | QA-1.2 |
| `GET /api/government-organizations/search/fulltext` | QA-1.2 |
| `GET /api/government-organizations/find` | QA-1.2 |
| `GET /api/government-organizations/cabinet-departments` | QA-1.2 |
| `GET /api/government-organizations/independent-agencies` | QA-1.2 |
| `GET /api/government-organizations/by-type` | QA-1.2 |
| `GET /api/government-organizations/by-branch` | QA-1.2 |
| `GET /api/government-organizations/by-jurisdiction` | QA-1.2 |
| `GET /api/government-organizations/{id}/hierarchy` | QA-1.2 |
| `GET /api/government-organizations/{id}/descendants` | QA-1.2 |
| `GET /api/government-organizations/{id}/ancestors` | QA-1.2 |
| `GET /api/government-organizations/top-level` | QA-1.2 |
| `POST /api/government-organizations/validate-entity` | QA-1.2 |
| `GET /api/government-organizations/statistics` | QA-1.2 |

### Reasoning Service API (8 endpoints)

| Endpoint | Test Coverage |
|----------|---------------|
| `GET /` | QA-1.3 |
| `GET /health` | QA-1.3 |
| `POST /entities/extract` | QA-1.3 |
| `POST /entities/link` | QA-1.3 |
| `POST /entities/link/single` | QA-1.3 |
| `POST /entities/reason` | QA-1.3 |
| `GET /entities/ontology/stats` | QA-1.3 |
| `POST /entities/query/sparql` | QA-1.3 |

## Acceptance Criteria (Epic Level)

1. **Project Structure**: `api-tests/` directory exists at repository root with complete Maven project
2. **Backend Coverage**: All 34 backend API endpoints have test coverage
3. **Reasoning Coverage**: All 8 reasoning service endpoints have test coverage
4. **Database Integration**: Tests can seed, query, and cleanup test data
5. **CI/CD Pipeline**: GitHub Actions workflow runs tests on every PR
6. **Test Types**: Both unit-style (mocked) and integration tests exist
7. **Documentation**: README explains setup, execution, and configuration
8. **Pass Rate**: All tests pass in CI before merge is allowed

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Services not running in CI | High | Medium | Use Docker services in GitHub Actions |
| Flaky tests due to timing | Medium | Medium | Add retry logic, increase timeouts |
| Test data pollution | Medium | Low | Use transaction rollback or truncate |
| External KB rate limiting | Low | Medium | Mock Wikidata/DBpedia in CI |
| Long CI pipeline duration | Low | Medium | Parallelize test execution |

## Definition of Done

- [x] All stories completed and merged
- [x] Test coverage >80% for API endpoints
- [x] CI pipeline runs successfully on PR
- [x] All tests pass (100% pass rate)
- [x] README documentation complete
- [x] No critical or high severity bugs
- [x] Code reviewed and approved
- [x] QA sign-off on test quality

## Related Documentation

- [Tech Stack](../architecture/tech-stack.md) - Technology versions
- [Source Tree](../architecture/source-tree.md) - Project structure
- [ROADMAP](../ROADMAP.md) - Project roadmap
- [Entity Controller](../../backend/src/main/java/org/newsanalyzer/controller/EntityController.java)
- [Gov Org Controller](../../backend/src/main/java/org/newsanalyzer/controller/GovernmentOrganizationController.java)
- [Reasoning Service](../../reasoning-service/app/main.py)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-25 | 1.0 | Initial epic creation | Sarah (PO) |
| 2025-11-30 | 1.1 | Epic status → Done: All 6 stories completed, DoD fulfilled | Sarah (PO) |

## Architectural Review Summary

**Review Date:** 2025-11-25
**Reviewer:** Winston (System Architect)
**Verdict:** APPROVED

### Strengths Identified
- Correct architectural decision to place `api-tests/` as separate project at repo root
- Technology stack aligns perfectly with existing infrastructure (Java 17, Maven, JUnit 5, PostgreSQL 15+)
- Dual testing strategy (WireMock + Live) via Maven profiles is architecturally sound
- Dedicated test database with shared Flyway migrations follows DRY principle
- Cross-service workflow testing demonstrates deep system understanding

### Recommendations (Non-Blocking)
1. Consider contract testing (Pact) in future enhancement
2. Clarify reasoning service `/government-orgs/*` endpoint coverage
3. Consider parallel CI jobs for faster pipeline (2-3 min improvement)
4. Ensure external KBs (Wikidata/DBpedia) are mocked in CI to avoid flaky tests
5. Consider Testcontainers as alternative for local development consistency

### Related ADRs
- [ADR-QA-001](../architecture/adr/ADR-QA-001-api-test-project-location.md) - API Test Project Location
- [ADR-QA-002](../architecture/adr/ADR-QA-002-test-database-strategy.md) - Test Database Strategy
- [ADR-QA-003](../architecture/adr/ADR-QA-003-mock-vs-live-testing.md) - Mock vs Live Testing

## Approval

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | Sarah (PO) | 2025-11-25 | Drafted |
| Architect | Winston (SA) | 2025-11-25 | **APPROVED** |
| Tech Lead | _TBD_ | _Pending_ | _Pending_ |
| QA Lead | Quinn (QA) | _Pending_ | _Pending_ |

---

*End of Epic Document*
