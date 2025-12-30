# CI/CD Workflows

This document describes the GitHub Actions workflows used in NewsAnalyzer v2.

## Quick Reference

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| Backend CI | `backend-ci.yml` | Push/PR to `backend/**` | Test, lint, build Java backend |
| Frontend CI | `frontend-ci.yml` | Push/PR to `frontend/**` | Test, lint, build Next.js frontend |
| Reasoning Service CI | `reasoning-service-ci.yml` | Push/PR to `reasoning-service/**` | Test Python reasoning service |
| API Tests | `api-tests.yml` | Push/PR, Manual | Cross-service integration tests |
| Deploy Production | `deploy-production.yml` | Tag `v*`, Manual | Deploy to production server |

## Workflow Details

### backend-ci.yml

**Purpose:** Build and test the Spring Boot backend application.

**Triggers:**
- Push to `master` or `main` branches (paths: `backend/**`)
- Pull requests to `master` or `main` (paths: `backend/**`)

**Jobs:**

| Job | Description |
|-----|-------------|
| `test` | Run Maven tests with PostgreSQL and Redis services |
| `build-and-push` | Build and push Docker image to GHCR (on merge to master) |

**Services:**
- PostgreSQL 15 (port 5432)
- Redis 7 (port 6379)

**Coverage:**
- JaCoCo coverage report generated
- **Threshold: 70% line coverage** (build fails if below)
- Coverage uploaded to Codecov

**Artifacts:**
- Docker image: `ghcr.io/{repo}-backend:latest`
- Docker image: `ghcr.io/{repo}-backend:{sha}`

**Environment Variables:**
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/newsanalyzer_test
SPRING_DATASOURCE_USERNAME=newsanalyzer
SPRING_DATASOURCE_PASSWORD=test_password
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379
```

---

### frontend-ci.yml

**Purpose:** Build and test the Next.js frontend application.

**Triggers:**
- Push to `master` or `main` branches (paths: `frontend/**`)
- Pull requests to `master` or `main` (paths: `frontend/**`)

**Jobs:**

| Job | Description |
|-----|-------------|
| `test` | Lint, type-check, run Vitest tests with coverage |
| `build-and-push` | Build and push Docker image to GHCR (on merge to master) |

**Steps in Test Job:**
1. Install pnpm dependencies
2. Run ESLint (`pnpm lint`)
3. Run TypeScript type checking (`pnpm type-check`)
4. Run tests with coverage (`pnpm test --coverage`)
5. Upload coverage to Codecov
6. Build production bundle (`pnpm build`)

**Coverage:**
- Vitest coverage with v8 provider
- **Thresholds:** Lines 30%, Functions 20%, Branches 50%, Statements 30%
- Coverage uploaded to Codecov

**Artifacts:**
- Docker image: `ghcr.io/{repo}-frontend:latest`
- Docker image: `ghcr.io/{repo}-frontend:{sha}`

---

### reasoning-service-ci.yml

**Purpose:** Build and test the Python FastAPI reasoning service.

**Triggers:**
- Push to `master` or `main` branches (paths: `reasoning-service/**`)
- Pull requests to `master` or `main` (paths: `reasoning-service/**`)

**Jobs:**

| Job | Description |
|-----|-------------|
| `test` | Lint with ruff, type-check with mypy, run pytest |
| `build-and-push` | Build and push Docker image to GHCR (on merge to master) |

**Steps in Test Job:**
1. Set up Python 3.11
2. Install dependencies from `requirements.txt`
3. Download spaCy model (`en_core_web_lg`)
4. Lint with ruff (`ruff check app/`)
5. Type check with mypy (`mypy app/`)
6. Run tests with coverage (`pytest --cov=app tests/`)
7. Upload coverage to Codecov

**Artifacts:**
- Docker image: `ghcr.io/{repo}-reasoning-service:latest`
- Docker image: `ghcr.io/{repo}-reasoning-service:{sha}`

---

### api-tests.yml

**Purpose:** Run cross-service API integration tests against running backend and reasoning services.

**Triggers:**
- Push to `main`, `master`, `feature/**`, `fix/**`, `hotfix/**` branches
- Pull requests to `main` or `master`
- Version tags (`v*`)
- Manual trigger via `workflow_dispatch`
- Can be called by other workflows (`workflow_call`)

**Concurrency:** Cancels in-progress runs for same branch.

**Services:**
- PostgreSQL 15 (port 5432)
- Redis 7 (port 6379)

**Process:**
1. Build backend JAR (`mvn clean package -DskipTests`)
2. Install Python dependencies for reasoning service
3. Start backend service (port 8080)
4. Start reasoning service (port 8000)
5. Wait for both services to be healthy
6. Run API integration tests (`mvn test -Pci`)
7. Upload test results as artifacts
8. Publish JUnit test summary

**Timeout:** 20 minutes

**Artifacts:**
- Surefire test reports (`api-tests/target/surefire-reports/`)
- JaCoCo coverage report (`api-tests/target/site/jacoco/`)
- Retention: 14 days

**Manual Trigger Options:**
- `debug_enabled`: Enable debug logging (default: false)

---

### deploy-production.yml

**Purpose:** Deploy application to production Hetzner server.

**Triggers:**
- Push of version tags (`v*`)
- Manual trigger via `workflow_dispatch` (requires typing "deploy" to confirm)

**Concurrency:** Only one production deployment at a time (never cancels in-progress).

**Jobs:**

| Job | Description |
|-----|-------------|
| `validate` | Extract version, validate manual confirmation |
| `test` | Run full API integration tests before deploying |
| `deploy` | SSH to production server and deploy via Docker Compose |
| `release` | Create GitHub Release (for version tags only) |

**Deployment Process:**
1. Validate release tag or manual confirmation
2. Run full API integration test suite
3. SSH to production server
4. Pull latest code
5. Build Docker images
6. Deploy with `docker compose up -d`
7. Verify health endpoint responds
8. Create GitHub Release (if version tag)

**Required Secrets:**
- `DEPLOY_SSH_KEY`: SSH private key for production server

**Production Environment:**
- Host: `5.78.71.195`
- User: `root`
- Deploy Path: `/opt/newsanalyzer`
- URL: `http://newsanalyzer.org`

---

## Running Tests Locally

### Backend (Java/Spring Boot)

```bash
cd backend

# Run all tests
./mvnw test

# Run tests with coverage check (fails if <70%)
./mvnw clean verify

# Generate coverage report only
./mvnw clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html

# Run specific test class
./mvnw test -Dtest=EntityRepositoryTest

# Run with specific profile
./mvnw test -Dspring.profiles.active=tc
```

### Frontend (Next.js/React)

```bash
cd frontend

# Install dependencies
pnpm install

# Run all tests
pnpm test

# Run tests in watch mode
pnpm test --watch

# Run tests with coverage (fails if below thresholds)
pnpm test --coverage

# Run tests with UI
pnpm test:ui

# Run specific test file
pnpm test AdminSidebar

# Run linting
pnpm lint

# Run type checking
pnpm type-check
```

### Reasoning Service (Python/FastAPI)

```bash
cd reasoning-service

# Create virtual environment
python -m venv venv

# Activate (Linux/Mac)
source venv/bin/activate

# Activate (Windows)
venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt
python -m spacy download en_core_web_lg

# Run tests
pytest

# Run tests with coverage
pytest --cov=app --cov-report=html tests/

# View coverage report
# Open: htmlcov/index.html

# Run linting
ruff check app/

# Run type checking
mypy app/ --ignore-missing-imports
```

### API Integration Tests

```bash
cd api-tests

# Prerequisites: Backend and Reasoning Service must be running
# Start backend on port 8080
# Start reasoning service on port 8000

# Run tests against local services
./mvnw test -Plocal

# Run tests against CI environment
./mvnw test -Pci \
  -Dbackend.baseUrl=http://localhost:8080 \
  -Dreasoning.baseUrl=http://localhost:8000
```

---

## Troubleshooting

### Common CI Failures

#### Backend: "Connection refused" to PostgreSQL

**Symptoms:**
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```

**Causes & Solutions:**
- PostgreSQL service container not ready yet - check service health in workflow logs
- Database credentials mismatch - verify environment variables match service config
- Port conflict - ensure no other services on port 5432

#### Backend: JaCoCo coverage below threshold

**Symptoms:**
```
Rule violated for bundle: line coverage is 0.65, but expected minimum is 0.70
```

**Solutions:**
- Add tests for uncovered code
- Check which files lack coverage in `target/site/jacoco/index.html`
- If temporary, adjust threshold in `pom.xml` (requires approval)

#### Frontend: "Module not found" errors

**Symptoms:**
```
Error: Cannot find module '@/components/...'
```

**Solutions:**
- Clear pnpm cache: `pnpm store prune`
- Delete `node_modules` and reinstall: `rm -rf node_modules && pnpm install`
- Verify path aliases in `tsconfig.json`

#### Frontend: Coverage below threshold

**Symptoms:**
```
ERROR: Coverage for lines (25%) does not meet global threshold (30%)
```

**Solutions:**
- Add tests for uncovered components
- Run `pnpm test --coverage` locally to see detailed report
- Check `frontend/vitest.config.ts` for excluded directories

#### API Tests: Services not starting

**Symptoms:**
```
Backend failed to start within 2 minutes
```

**Solutions:**
- Check service logs in workflow output (look for "BACKEND LOGS" section)
- Verify JAR was built successfully
- Check for port conflicts
- Ensure database migrations completed

#### Docker build fails

**Symptoms:**
```
COPY failed: file not found in build context
```

**Solutions:**
- Ensure build step completed before Docker build
- Check Dockerfile COPY paths match actual file locations
- Verify `.dockerignore` isn't excluding required files

### Debugging Tips

1. **Re-run failed job:** Use GitHub UI "Re-run failed jobs" button

2. **Check service logs:** Expand service container logs in workflow run output

3. **Local reproduction:** Run same commands locally with matching versions:
   - Java 17 (Temurin)
   - Node.js 20
   - Python 3.11
   - PostgreSQL 15
   - Redis 7

4. **Cache issues:** Try "Re-run all jobs" to get fresh cache

5. **Debug mode:** For API tests, trigger workflow manually with `debug_enabled: true`

6. **View test reports:** Download artifacts from workflow run for detailed test results

### Environment Variable Reference

| Variable | Service | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | Backend | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Backend | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Backend | Database password |
| `SPRING_DATA_REDIS_HOST` | Backend | Redis hostname |
| `SPRING_DATA_REDIS_PORT` | Backend | Redis port |
| `BACKEND_URL` | API Tests | Backend service URL |
| `REASONING_URL` | API Tests | Reasoning service URL |
| `DB_URL` | API Tests | Database connection URL |

---

## Coverage Thresholds

| Service | Metric | Threshold | Enforced In |
|---------|--------|-----------|-------------|
| Backend | Line Coverage | 70% | `pom.xml` (JaCoCo) |
| Frontend | Lines | 30% | `vitest.config.ts` |
| Frontend | Functions | 20% | `vitest.config.ts` |
| Frontend | Branches | 50% | `vitest.config.ts` |
| Frontend | Statements | 30% | `vitest.config.ts` |

---

## Related Documentation

- [Deployment Guide](../../docs/deployment/DEPLOYMENT_GUIDE.md)
- [Tech Stack](../../docs/architecture/tech-stack.md)
- [Backend README](../../backend/README.md)
- [Frontend README](../../frontend/README.md)
- [Reasoning Service README](../../reasoning-service/README.md)

---

*Last updated: 2025-12-30*
