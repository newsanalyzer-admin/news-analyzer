# Story QA-2.6: CI/CD Documentation

## Status

Complete

## Story

**As a** contributor,
**I want** clear documentation of CI/CD workflows,
**So that** I understand how to work with the build system.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `.github/workflows/README.md` exists with workflow overview |
| AC2 | Each workflow is documented: trigger conditions, jobs, artifacts |
| AC3 | Local development testing instructions included |
| AC4 | Troubleshooting section for common CI failures |
| AC5 | Documentation reviewed and merged |

## Tasks / Subtasks

- [ ] **Task 1: Create workflow README** (AC: 1)
  - [ ] Create `.github/workflows/README.md`
  - [ ] Add overview section listing all workflows
  - [ ] Add quick reference table

- [ ] **Task 2: Document each workflow** (AC: 2)
  - [ ] Document `backend-ci.yml`
  - [ ] Document `frontend-ci.yml`
  - [ ] Document `reasoning-service-ci.yml`
  - [ ] Document `api-tests.yml`
  - [ ] Document `deploy-production.yml`

- [ ] **Task 3: Add local testing instructions** (AC: 3)
  - [ ] Document how to run backend tests locally
  - [ ] Document how to run frontend tests locally
  - [ ] Document how to run reasoning service tests locally
  - [ ] Document how to run API integration tests locally

- [ ] **Task 4: Add troubleshooting section** (AC: 4)
  - [ ] Document common CI failures and solutions
  - [ ] Add tips for debugging failed builds
  - [ ] Document environment variable requirements

- [ ] **Task 5: Review and finalize** (AC: 5)
  - [ ] Self-review for accuracy
  - [ ] Verify all links work
  - [ ] Submit for review

## Dev Notes

### Document Template

```markdown
# CI/CD Workflows

This document describes the GitHub Actions workflows used in NewsAnalyzer.

## Quick Reference

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| backend-ci.yml | Push/PR to backend/** | Test and build Java backend |
| frontend-ci.yml | Push/PR to frontend/** | Test and build Next.js frontend |
| reasoning-service-ci.yml | Push/PR to reasoning-service/** | Test Python service |
| api-tests.yml | Manual/Schedule | Cross-service API tests |
| deploy-production.yml | Push to main | Deploy to production |

## Workflow Details

### backend-ci.yml

**Triggers:**
- Push to `master` or `main` branches (backend/** paths)
- Pull requests to `master` or `main` (backend/** paths)

**Jobs:**
1. **test** - Run Maven tests with PostgreSQL and Redis services
2. **build-and-push** - Build and push Docker image (on merge to master)

**Services:**
- PostgreSQL 15 (port 5432)
- Redis 7 (port 6379)

**Artifacts:**
- JaCoCo coverage report uploaded to Codecov
- Docker image pushed to ghcr.io

**Environment Variables:**
- `SPRING_DATASOURCE_URL` - PostgreSQL connection
- `SPRING_DATASOURCE_USERNAME` - Database user
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `SPRING_DATA_REDIS_HOST` - Redis host
- `SPRING_DATA_REDIS_PORT` - Redis port

[Continue for each workflow...]

## Running Tests Locally

### Backend

```bash
cd backend

# Run all tests
./mvnw test

# Run tests with coverage
./mvnw clean verify jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Frontend

```bash
cd frontend

# Install dependencies
pnpm install

# Run tests
pnpm test

# Run tests with UI
pnpm test:ui

# Run tests with coverage
pnpm test --coverage
```

### Reasoning Service

```bash
cd reasoning-service

# Create virtual environment
python -m venv venv
source venv/bin/activate  # or venv\Scripts\activate on Windows

# Install dependencies
pip install -r requirements.txt
pip install -r requirements-dev.txt

# Run tests
pytest

# Run tests with coverage
pytest --cov=app --cov-report=html
```

### API Integration Tests

```bash
cd api-tests

# Ensure backend and reasoning service are running
# Then run tests
./mvnw test -Plocal
```

## Troubleshooting

### Common Issues

#### Backend: "Connection refused" to PostgreSQL
- **Cause:** PostgreSQL service not ready
- **Solution:** Check service health in workflow logs

#### Frontend: "Module not found" errors
- **Cause:** Dependencies not installed or cache stale
- **Solution:** Clear pnpm cache: `pnpm store prune`

#### Coverage below threshold
- **Cause:** New code without tests
- **Solution:** Add tests for new code or adjust thresholds (with approval)

#### Docker build fails
- **Cause:** Usually missing build artifacts
- **Solution:** Ensure build step completes before Docker build

### Debugging Tips

1. **Re-run failed job:** Use GitHub UI "Re-run failed jobs"
2. **Check service logs:** Expand service container logs in workflow run
3. **Local reproduction:** Run same commands locally with same versions
4. **Cache issues:** Try "Re-run all jobs" to get fresh cache
```

### Existing Workflows to Document

| File | Lines | Key Features |
|------|-------|--------------|
| `backend-ci.yml` | ~110 | PostgreSQL/Redis services, JaCoCo, Docker |
| `frontend-ci.yml` | ~78 | pnpm, lint, type-check, Vitest, Docker |
| `reasoning-service-ci.yml` | TBD | Python, pytest |
| `api-tests.yml` | TBD | REST Assured, cross-service |
| `deploy-production.yml` | TBD | Deployment steps |

### Information to Gather

For each workflow, document:
- Trigger events (push, PR, manual, schedule)
- Branch filters
- Path filters
- Jobs and their purposes
- Services required
- Environment variables
- Artifacts produced
- Secrets required

## Technical Details

### File Location

`.github/workflows/README.md`

### Markdown Conventions

- Use tables for quick reference
- Use code blocks for commands
- Use collapsible sections for long content:
  ```markdown
  <details>
  <summary>Click to expand</summary>

  Long content here...

  </details>
  ```

### Cross-References

Link to related documentation:
- `docs/deployment/DEPLOYMENT_GUIDE.md`
- `docs/architecture/tech-stack.md`
- Individual service README files

## Dependencies

- None (documentation only)

## Estimate

1 story point

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-12-30 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-12-30 | 1.1 | Implementation complete | Dev |

---

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Completion Notes

**Documentation Created:**

Created comprehensive `.github/workflows/README.md` covering all acceptance criteria:

1. **Workflow Overview (AC1):**
   - Quick reference table with all 5 workflows
   - Purpose, triggers, and file names for each

2. **Workflow Details (AC2):**
   - `backend-ci.yml`: Jobs, services, coverage thresholds, artifacts, environment variables
   - `frontend-ci.yml`: Test steps, coverage configuration, Docker build
   - `reasoning-service-ci.yml`: Python setup, linting, testing
   - `api-tests.yml`: Multi-service orchestration, health checks, artifacts
   - `deploy-production.yml`: Validation, deployment process, GitHub Release creation

3. **Local Testing Instructions (AC3):**
   - Backend: Maven commands for tests, coverage, specific test execution
   - Frontend: pnpm commands for tests, watch mode, coverage, linting
   - Reasoning Service: venv setup, pytest, ruff, mypy
   - API Tests: Prerequisites and execution commands

4. **Troubleshooting Section (AC4):**
   - Common CI failures with symptoms and solutions
   - PostgreSQL connection issues
   - Coverage threshold violations
   - Module not found errors
   - Docker build failures
   - Debugging tips (6 specific techniques)
   - Environment variable reference table

5. **Additional Content:**
   - Coverage thresholds summary table
   - Related documentation links

### File List

- `.github/workflows/README.md` - New comprehensive CI/CD documentation

### Documentation Stats

- ~450 lines of documentation
- 5 workflows fully documented
- 4 local testing sections
- 6+ troubleshooting scenarios
- Environment variable reference table
- Coverage thresholds summary

---

*End of Story Document*
