# ADR-QA-002: Test Database Strategy

## Status

**ACCEPTED** - 2025-11-25

## Context

The API integration testing framework needs database access for:
1. **Direct Data Verification** - Bypassing API to verify data persistence
2. **Test Data Seeding** - Inserting known data before tests
3. **Test Cleanup** - Ensuring test isolation between runs
4. **Schema Validation** - Verifying database migrations work correctly

The backend service uses PostgreSQL 15+ with Flyway migrations and JSONB columns. We need to decide how the test framework will interact with the database.

### Options Considered

1. **H2 In-Memory Database** - Lightweight, fast, no setup required
2. **Shared Development Database** - Use existing `newsanalyzer` database
3. **Dedicated Test Database** - Separate `newsanalyzer_test` PostgreSQL instance
4. **Testcontainers** - Docker-managed PostgreSQL per test run

## Decision

**Option 3: Dedicated Test Database (`newsanalyzer_test`)**

The API test framework will connect to a dedicated PostgreSQL database named `newsanalyzer_test`, using the same Flyway migrations as the backend service.

### Configuration

```properties
# Local environment
db.url=jdbc:postgresql://localhost:5432/newsanalyzer_test
db.username=${DB_USERNAME:postgres}
db.password=${DB_PASSWORD:postgres}

# CI environment (GitHub Actions PostgreSQL service)
db.url=jdbc:postgresql://localhost:5432/newsanalyzer_test
db.username=postgres
db.password=postgres
```

### Migration Strategy

```java
// Reuse backend's Flyway migrations
Flyway flyway = Flyway.configure()
    .dataSource(dataSource)
    .locations("filesystem:../backend/src/main/resources/db/migration")
    .load();
flyway.migrate();
```

## Rationale

### Why Not H2 In-Memory?

1. **JSONB Incompatibility** - H2 doesn't support PostgreSQL JSONB columns natively
2. **PostgreSQL Extensions** - Backend uses `pg_trgm` for fuzzy search; H2 can't replicate
3. **Behavioral Differences** - H2's SQL dialect differs from PostgreSQL
4. **False Confidence** - Tests passing on H2 may fail on real PostgreSQL
5. **Existing Backend Issue** - Backend already has 4 failing tests due to H2/PostgreSQL incompatibility

### Why Not Shared Development Database?

1. **Data Pollution** - Tests would corrupt development data
2. **Non-Deterministic** - Test results depend on existing data state
3. **Concurrent Access** - Multiple developers running tests cause conflicts
4. **No Clean State** - Can't truncate tables without losing dev data

### Why Not Testcontainers?

1. **Docker Dependency** - Requires Docker running locally
2. **Startup Overhead** - Container spin-up adds 10-30 seconds per test run
3. **CI Complexity** - GitHub Actions already provides PostgreSQL service
4. **Memory Usage** - Each test run spawns new container

**Note:** Testcontainers remains a valid future enhancement for local development parity.

### Why Dedicated Test Database?

1. **PostgreSQL Fidelity** - Real PostgreSQL behavior, including JSONB and extensions
2. **Migration Verification** - Tests validate Flyway migrations work correctly
3. **Clean Slate** - Can truncate/reset without affecting other environments
4. **CI Ready** - GitHub Actions PostgreSQL service creates fresh database per run
5. **Shared Migrations** - DRY principle; uses backend's migration scripts
6. **Performance** - No container overhead; database already running

## Consequences

### Positive

- Tests run against real PostgreSQL behavior
- Catches PostgreSQL-specific issues early
- Flyway migrations tested automatically
- Fast test execution (no container startup)
- CI/CD integration is straightforward

### Negative

- Requires PostgreSQL running locally for development
- Developers must create `newsanalyzer_test` database manually
- Credentials must be managed (though with sensible defaults)

### Neutral

- Test database is separate from development database
- Cleanup responsibility lies with test framework

## Implementation Details

### Database Setup (One-time)

```bash
# Create test database
psql -U postgres -c "CREATE DATABASE newsanalyzer_test;"

# Grant permissions
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE newsanalyzer_test TO postgres;"
```

### Test Isolation Strategy

```java
@BeforeEach
void cleanDatabase() {
    // Truncate all tables with CASCADE
    jdbcTemplate.execute("TRUNCATE TABLE entities CASCADE");
    jdbcTemplate.execute("TRUNCATE TABLE government_organizations CASCADE");
}

@AfterAll
static void resetSequences() {
    // Reset ID sequences if using serial IDs
}
```

### CI Configuration (GitHub Actions)

```yaml
services:
  postgres:
    image: postgres:15
    env:
      POSTGRES_DB: newsanalyzer_test
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - 5432:5432
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

## Related Decisions

- [ADR-QA-001](ADR-QA-001-api-test-project-location.md) - API Test Project Location
- [ADR-QA-003](ADR-QA-003-mock-vs-live-testing.md) - Mock vs Live Testing

## References

- [Epic QA-1: API Integration Testing Framework](../../stories/QA-1.epic-api-testing-framework.md)
- [Story QA-1.4: Database Integration & Test Data](../../stories/QA-1.4.database-integration-test-data.md)
- [Tech Stack: Database & Persistence](../tech-stack.md)

---

**Decision Made By:** Winston (System Architect)
**Date:** 2025-11-25
