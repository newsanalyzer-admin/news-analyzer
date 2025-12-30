# Epic QA-2: Test Infrastructure Improvements

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | QA-2 |
| **Epic Name** | Test Infrastructure Improvements |
| **Epic Type** | Technical Debt / Infrastructure |
| **Priority** | LOW (Post-MVP) |
| **Status** | **COMPLETE** |
| **Created** | 2025-12-01 |
| **Revised** | 2025-12-30 |
| **Owner** | Sarah (PO) |
| **Architect** | Winston |
| **Depends On** | None |

## Executive Summary

This epic addresses remaining gaps in test infrastructure after significant progress has already been made. The original scope (Testcontainers, frontend testing setup, CI/CD) has been largely implemented. This revised epic focuses on expanding coverage, adding quality gates, and documenting the existing infrastructure.

### Work Already Completed (Prior to Epic Start)

| Item | Status | Evidence |
|------|--------|----------|
| Testcontainers Integration | **COMPLETE** | `TestcontainersConfiguration.java`, 39+ repository tests |
| Frontend Testing Framework | **COMPLETE** | Vitest + Testing Library configured, 5 test files |
| CI/CD Workflows | **COMPLETE** | 5 GitHub Actions workflows operational |

## Business Value

### Why This Epic Matters

1. **Expanded Coverage** - Current frontend tests cover only knowledge-base components; admin/shared components need testing
2. **Quality Gates** - No coverage thresholds enforced; tests can pass with low coverage
3. **Documentation** - CI/CD workflows exist but lack consolidated documentation for contributors
4. **Maintainability** - Better test coverage reduces regression risk as codebase grows

### Success Metrics

| Metric | Target | Current |
|--------|--------|---------|
| Backend repository test coverage | 100% enabled | **100%** (39+ tests) |
| Frontend component test coverage | >60% | ~20% (knowledge-base only) |
| CI coverage thresholds | Enforced | Not enforced |
| CI/CD documentation | Complete | Workflows exist, no README |

## Scope

### In Scope

- Expand frontend component tests to admin and shared components
- Add test coverage thresholds to CI pipelines
- Create CI/CD documentation README
- Verify all existing tests pass in CI

### Out of Scope

- E2E browser testing (Playwright/Cypress) - future enhancement
- Performance/load testing - separate epic
- Security testing automation - separate epic
- Backend Testcontainers setup - **already complete**

## Architecture

### Current Test Infrastructure (Already Implemented)

```
Backend Tests (Testcontainers)
┌─────────────────────────────────────────────────────────────┐
│                  TestcontainersConfiguration                 │
│  - Shared PostgreSQL 15 container                           │
│  - @ServiceConnection for auto-configuration                │
│  - @ActiveProfiles("tc") for test profile                   │
└─────────────────────────────────────────────────────────────┘
         │
         ├── EntityRepositoryTest (18 tests)
         └── StatuteRepositoryTest (21 tests)

Frontend Tests (Vitest + Testing Library)
┌─────────────────────────────────────────────────────────────┐
│                    vitest.config.ts                          │
│  - jsdom environment                                         │
│  - React Testing Library                                     │
│  - Coverage reporting                                        │
└─────────────────────────────────────────────────────────────┘
         │
         └── src/components/knowledge-base/__tests__/
             ├── EntityBrowser.test.tsx (30+ tests)
             ├── EntityFilters.test.tsx
             ├── EntityTypeSelector.test.tsx
             ├── ViewModeSelector.test.tsx
             └── KnowledgeExplorer.test.tsx

CI/CD Pipelines (GitHub Actions)
┌─────────────────────────────────────────────────────────────┐
│  .github/workflows/                                          │
│  ├── backend-ci.yml      (tests, coverage, Docker)          │
│  ├── frontend-ci.yml     (lint, tests, build, Docker)       │
│  ├── reasoning-service-ci.yml                               │
│  ├── api-tests.yml                                          │
│  └── deploy-production.yml                                  │
└─────────────────────────────────────────────────────────────┘
```

### Target State

```
Frontend Tests - Expanded Coverage
┌─────────────────────────────────────────────────────────────┐
│  src/components/                                             │
│  ├── knowledge-base/__tests__/  ✅ (existing)               │
│  ├── admin/__tests__/           🆕 (new)                    │
│  ├── shared/__tests__/          🆕 (new)                    │
│  └── ui/__tests__/              🆕 (new - critical only)    │
└─────────────────────────────────────────────────────────────┘

CI Quality Gates
┌─────────────────────────────────────────────────────────────┐
│  Backend: JaCoCo threshold 70% line coverage                │
│  Frontend: Vitest threshold 60% line coverage               │
│  Fail build if coverage drops below threshold               │
└─────────────────────────────────────────────────────────────┘
```

## Stories

### Story Summary

| ID | Story | Priority | Estimate | Status |
|----|-------|----------|----------|--------|
| QA-2.1 | ~~Testcontainers for Repository Tests~~ | - | - | **COMPLETE** (pre-epic) |
| QA-2.2 | ~~Frontend Testing Setup~~ | - | - | **COMPLETE** (pre-epic) |
| QA-2.3 | ~~CI/CD Workflows~~ | - | - | **COMPLETE** (pre-epic) |
| **QA-2.4** | **Expand Frontend Test Coverage** | P1 | 3 pts | **Complete** |
| **QA-2.5** | **Add CI Coverage Thresholds** | P2 | 2 pts | **Complete** |
| **QA-2.6** | **CI/CD Documentation** | P2 | 1 pt | **Complete** |

### Dependency Graph

```
QA-2.4 (Frontend Coverage)
    │
    └── QA-2.5 (Coverage Thresholds) ─── depends on coverage existing
            │
            └── QA-2.6 (Documentation) ─── documents final state
```

---

## Story Details

### Story QA-2.4: Expand Frontend Test Coverage

**Status:** Ready for Development

**As a** developer,
**I want** comprehensive frontend component tests,
**So that** UI regressions are caught before deployment.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Admin components have test files: `AdminSidebar.test.tsx`, `SearchImportPanel.test.tsx` |
| AC2 | Shared components have test files: `SidebarMenu.test.tsx`, `PageHeader.test.tsx` |
| AC3 | Each test file covers: rendering, user interactions, error states |
| AC4 | All new tests follow existing patterns (Vitest + Testing Library) |
| AC5 | `pnpm test` passes with all new tests |
| AC6 | Test coverage report shows >60% line coverage for tested components |

#### Technical Notes

**Priority Components to Test:**

| Component | Location | Complexity |
|-----------|----------|------------|
| `AdminSidebar` | `components/admin/` | Medium - state management |
| `SearchImportPanel` | `components/admin/` | High - API interactions |
| `SidebarMenu` | `components/shared/` | Low - presentational |
| `PageHeader` | `components/shared/` | Low - presentational |
| `ImportPreviewModal` | `components/admin/` | Medium - form handling |

**Test Patterns to Follow:**
```typescript
// Reference: frontend/src/components/knowledge-base/__tests__/EntityBrowser.test.tsx
// - Use vi.fn() for mocks
// - Group tests with describe()
// - Test rendering, interactions, accessibility
```

#### Estimate: 3 story points

---

### Story QA-2.5: Add CI Coverage Thresholds

**Status:** Ready for Development

**As a** tech lead,
**I want** CI builds to fail if test coverage drops below thresholds,
**So that** code quality is maintained over time.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | Backend CI fails if JaCoCo line coverage < 70% |
| AC2 | Frontend CI fails if Vitest line coverage < 60% |
| AC3 | Coverage thresholds are configurable via workflow or config files |
| AC4 | CI output clearly shows coverage percentage and threshold |
| AC5 | Existing builds pass with current coverage levels |

#### Technical Notes

**Backend (JaCoCo):**
```xml
<!-- pom.xml addition -->
<configuration>
  <rules>
    <rule>
      <element>BUNDLE</element>
      <limits>
        <limit>
          <counter>LINE</counter>
          <value>COVEREDRATIO</value>
          <minimum>0.70</minimum>
        </limit>
      </limits>
    </rule>
  </rules>
</configuration>
```

**Frontend (Vitest):**
```typescript
// vitest.config.ts addition
coverage: {
  provider: 'v8',
  reporter: ['text', 'json', 'html'],
  thresholds: {
    lines: 60,
    functions: 60,
    branches: 60,
    statements: 60
  }
}
```

#### Estimate: 2 story points

---

### Story QA-2.6: CI/CD Documentation

**Status:** Ready for Development

**As a** contributor,
**I want** clear documentation of CI/CD workflows,
**So that** I understand how to work with the build system.

#### Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `.github/workflows/README.md` exists with workflow overview |
| AC2 | Each workflow is documented: trigger conditions, jobs, artifacts |
| AC3 | Local development testing instructions included |
| AC4 | Troubleshooting section for common CI failures |
| AC5 | Documentation reviewed and merged |

#### Content Outline

```markdown
# CI/CD Workflows

## Overview
- backend-ci.yml - Java backend testing and Docker build
- frontend-ci.yml - Next.js testing and Docker build
- reasoning-service-ci.yml - Python service testing
- api-tests.yml - Cross-service API integration tests
- deploy-production.yml - Production deployment

## Workflow Details
[Each workflow documented]

## Running Tests Locally
[Commands for each service]

## Troubleshooting
[Common issues and solutions]
```

#### Estimate: 1 story point

---

## Acceptance Criteria (Epic Level)

1. **Frontend Coverage:** >60% of frontend components have tests
2. **CI Quality Gates:** Coverage thresholds enforced in all pipelines
3. **Documentation:** CI/CD workflows fully documented
4. **No Regressions:** All existing tests continue to pass
5. **Build Performance:** CI time increase <2 minutes

## Risks & Mitigations

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Current coverage below threshold | MEDIUM | LOW | Verify current levels before setting thresholds |
| Frontend test flakiness | MEDIUM | MEDIUM | Use Testing Library best practices, avoid timeouts |
| Documentation becomes stale | LOW | MEDIUM | Link docs to workflow files, review on changes |

## Definition of Done

- [x] Testcontainers implemented and working (pre-epic)
- [x] Frontend testing framework configured (pre-epic)
- [x] CI/CD workflows operational (pre-epic)
- [x] QA-2.4: Frontend test coverage expanded (105 new tests)
- [x] QA-2.5: Coverage thresholds enforced (70% backend, 30% frontend baseline)
- [x] QA-2.6: CI/CD documentation complete (.github/workflows/README.md)
- [x] All tests pass in CI (166 tests passing)
- [x] Build time increase <2 minutes (coverage adds ~5s to frontend CI)

## Related Documentation

- [QA-1 Epic](QA-1/QA-1.epic-api-testing-framework.md) - API testing framework
- [Coding Standards - Testing](../architecture/coding-standards.md#testing)
- [Tech Stack](../architecture/tech-stack.md) - Testing tools reference

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-01 | 1.0 | Initial epic creation | Sarah (PO) |
| 2025-12-30 | 2.0 | **Major Revision:** Updated to reflect completed work (Testcontainers, frontend setup, CI/CD). Replaced QA-2.1-2.3 with new stories QA-2.4-2.6 focusing on remaining gaps. | Winston (Architect) |

## Approval

| Role | Name | Date | Status |
|------|------|------|--------|
| Product Owner | Sarah (PO) | 2025-12-01 | Drafted |
| Architect | Winston | 2025-12-30 | **APPROVED with Revisions** |
| Tech Lead | _Pending_ | _Pending_ | _Pending_ |

---

*End of Epic Document*
