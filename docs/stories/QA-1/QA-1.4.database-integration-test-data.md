# Story QA-1.4: Database Integration & Test Data Management

## Status

Complete

## Story

**As a** QA Engineer / Developer,
**I want** the API test suite to connect to a dedicated PostgreSQL test database with proper test data seeding and cleanup,
**so that** I can run integration tests against real database state and verify data persistence correctly.

## Acceptance Criteria

1. Dedicated test database configuration exists for the `newsanalyzer_test` database
2. Database connection uses JDBC with PostgreSQL driver (consistent with backend)
3. Test data seeding scripts exist for entities and government organizations
4. Test data cleanup strategies ensure test isolation (each test starts with known state)
5. Database utilities can verify data directly (bypassing API) for assertion validation
6. Configuration supports both local PostgreSQL and CI environment database
7. Flyway migrations are applied to test database before test execution
8. Sensitive credentials are externalized to environment variables
9. Connection pooling is configured appropriately for test scenarios

## Tasks / Subtasks

- [x] **Task 1: Add database dependencies to Maven** (AC: 2, 9)
  - [x] Add PostgreSQL JDBC driver dependency
  - [x] Add HikariCP for connection pooling
  - [x] Add Flyway for migration support
  - [x] Add DbUnit or similar for test data management (optional) - N/A, using custom seeder

- [x] **Task 2: Create database configuration** (AC: 1, 6, 8)
  - [x] Create `api-tests/src/main/java/org/newsanalyzer/apitests/config/DatabaseConfig.java` with connection settings
  - [x] Create `api-tests/src/test/resources/application-local.properties` with local DB settings
  - [x] Create `api-tests/src/test/resources/application-ci.properties` with CI DB settings
  - [x] Externalize credentials to environment variables

- [x] **Task 3: Create database connection utility** (AC: 2, 9)
  - [x] Create `api-tests/src/main/java/org/newsanalyzer/apitests/util/DatabaseConnectionManager.java`:
    - [x] HikariCP DataSource configuration
    - [x] Connection acquisition and release
    - [x] Transaction management utilities
  - [x] Create connection pool settings (min=2, max=5 for tests)

- [x] **Task 4: Create Flyway migration runner** (AC: 7)
  - [x] Create `api-tests/src/main/java/org/newsanalyzer/apitests/util/FlywayMigrationRunner.java`:
    - [x] Point to backend's migration scripts (`backend/src/main/resources/db/migration/`)
    - [x] Run migrations before test suite execution
    - [x] Support clean + migrate for fresh state
  - [x] Create JUnit extension `api-tests/src/main/java/org/newsanalyzer/apitests/util/FlywayMigrationExtension.java` for automatic migration on suite start

- [x] **Task 5: Create test data seeding utilities** (AC: 3)
  - [x] Create `api-tests/src/test/java/org/newsanalyzer/apitests/data/TestDataSeeder.java`:
    - [x] `seedEntities()` - Insert sample entities
    - [x] `seedGovernmentOrganizations()` - Insert sample gov orgs
    - [x] `seedFullTestDataset()` - Complete test data setup
  - [x] Create SQL seed scripts:
    - [x] `src/test/resources/seed/entities.sql`
    - [x] `src/test/resources/seed/government_organizations.sql`
    - [x] `src/test/resources/seed/cleanup.sql`

- [x] **Task 6: Create test data cleanup utilities** (AC: 4)
  - [x] Create `api-tests/src/test/java/org/newsanalyzer/apitests/data/TestDataCleaner.java`:
    - [x] `cleanAllTables()` - Truncate all tables (with CASCADE)
    - [x] `cleanEntities()` - Clean only entities
    - [x] `cleanGovernmentOrganizations()` - Clean only gov orgs
    - [x] `resetSequences()` - Reset ID sequences
  - [x] Create JUnit extension `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseCleanupExtension.java` for automatic cleanup:
    - [x] `@BeforeEach` - Clean and reseed
    - [x] `@AfterAll` - Final cleanup

- [x] **Task 7: Create database assertion utilities** (AC: 5)
  - [x] Create `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseAssertions.java`:
    - [x] `assertEntityExists(UUID id)` - Verify entity in DB
    - [x] `assertEntityCount(int expected)` - Count verification
    - [x] `assertGovOrgExists(UUID id)` - Verify gov org in DB
    - [x] Additional assertions for name, type, verified status, and gov org links
  - [x] Create helper methods to compare API response with DB state

- [x] **Task 8: Extend existing test data builders with DB persistence** (AC: 3)
  - [x] Extend existing `api-tests/src/test/java/org/newsanalyzer/apitests/backend/EntityTestDataBuilder.java`:
    - [x] Add `persistToDatabase()` - Save to DB
    - [x] Add `buildAndPersist()` - Build + save
  - [x] Extend existing `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgTestDataBuilder.java`:
    - [x] Add `persistToDatabase()` - Save to DB
    - [x] Add `buildAndPersist()` - Build + save

- [x] **Task 9: Create sample test data** (AC: 3)
  - [x] Define realistic test entities:
    - [x] 5 PERSON entities (politicians, officials)
    - [x] 5 GOVERNMENT_ORG entities (agencies, departments)
    - [x] 3 ORGANIZATION entities (companies, NGOs)
    - [x] 3 LOCATION entities (cities, states)
  - [x] Define realistic government organizations:
    - [x] 3 Cabinet departments (DOJ, DOD, State)
    - [x] 3 Independent agencies (EPA, NASA, CIA)
    - [x] 2 Bureaus with parent relationships (FBI, ATF)

- [x] **Task 10: Create database test base class** (AC: 4, 7)
  - [x] Create `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseIntegrationTest.java` base class:
    - [x] `@RegisterExtension` FlywayMigrationExtension - Run migrations once per test run
    - [x] `@RegisterExtension` DatabaseCleanupExtension - Clean and reseed before each test
    - [x] Provide SeedEntities and SeedGovOrgs constants for known test data IDs
    - [x] Provide access to connection utilities

- [x] **Task 11: Document database setup** (AC: 1, 6)
  - [x] Update README with database setup instructions
  - [x] Document how to create `newsanalyzer_test` database
  - [x] Document environment variable configuration
  - [x] Document CI database setup

## Dev Notes

### Database Configuration (from tech-stack.md)

| Component | Version | Purpose |
|-----------|---------|---------|
| PostgreSQL | 15+ | Primary database |
| Flyway | (managed) | Database migrations |
| JDBC | 42.x | Database connectivity |

### Connection Details

| Environment | Database | Host | Port |
|-------------|----------|------|------|
| Local | `newsanalyzer_test` | localhost | 5432 |
| CI | `newsanalyzer_test` | postgres (Docker) | 5432 |

### Flyway Migration Location

```
backend/src/main/resources/db/migration/
├── V1__initial_schema.sql
├── V2.9__enable_pg_extensions.sql
├── V3__create_government_organizations.sql
└── V4__add_entity_gov_org_link.sql
```

### Database Schema (Key Tables)

#### entities table
```sql
CREATE TABLE entities (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    schema_org_type VARCHAR(100),
    properties JSONB,
    schema_org_data JSONB,
    external_ids JSONB,
    government_org_id UUID REFERENCES government_organizations(id),
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

#### government_organizations table
```sql
CREATE TABLE government_organizations (
    id UUID PRIMARY KEY,
    official_name VARCHAR(500) NOT NULL,
    acronym VARCHAR(50),
    organization_type VARCHAR(50),
    government_branch VARCHAR(50),
    parent_id UUID REFERENCES government_organizations(id),
    jurisdiction VARCHAR(255),
    established_date DATE,
    dissolved_date DATE,
    website VARCHAR(500),
    mission TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### Sample Seed Data SQL

```sql
-- entities.sql
INSERT INTO entities (id, name, entity_type, schema_org_type, verified, created_at, updated_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Elizabeth Warren', 'PERSON', 'Person', true, NOW(), NOW()),
    ('22222222-2222-2222-2222-222222222222', 'Environmental Protection Agency', 'GOVERNMENT_ORG', 'GovernmentOrganization', true, NOW(), NOW());

-- government_organizations.sql
INSERT INTO government_organizations (id, official_name, acronym, organization_type, government_branch, created_at, updated_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Environmental Protection Agency', 'EPA', 'AGENCY', 'INDEPENDENT', NOW(), NOW()),
    ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Department of Justice', 'DOJ', 'DEPARTMENT', 'EXECUTIVE', NOW(), NOW());
```

### Testing

**Test file location:** `api-tests/src/test/java/org/newsanalyzer/apitests/data/`

**Test standards:**
- Use JUnit 5 lifecycle annotations for setup/teardown
- Ensure test isolation - each test should not depend on others
- Use transactions or truncate for cleanup
- Never modify production database

**Testing frameworks:**
- JUnit 5 Extensions for database lifecycle
- HikariCP for connection pooling
- Flyway for migrations
- AssertJ for database assertions

### Maven Dependencies to Add

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.1</version>
</dependency>
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>10.4.1</version>
</dependency>
```

> **Note:** The `flyway-database-postgresql` dependency was removed from backend due to compatibility issues. Use only `flyway-core` which includes PostgreSQL support in Flyway 10.x.

### Database Setup Commands

```bash
# Create test database (run once)
psql -U postgres -c "CREATE DATABASE newsanalyzer_test;"

# Grant permissions
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE newsanalyzer_test TO postgres;"
```

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-25 | 1.0 | Initial story creation | Sarah (PO) |
| 2025-11-26 | 1.1 | Validation fixes: Added explicit file paths for all tasks, clarified Task 8 extends existing builders, removed flyway-database-postgresql dependency, added JUnit extension class names | Sarah (PO) |
| 2025-11-26 | 1.2 | Marked Complete - QA gate PASS, all ACs met, no blocking issues | Sarah (PO) |

## Dev Agent Record

### Agent Model Used

Claude Opus 4.5 (claude-opus-4-5-20251101)

### Debug Log References

No debug logs required - implementation was straightforward with no runtime errors.

### Completion Notes List

1. **Database Dependencies**: Added PostgreSQL 42.7.1, HikariCP 5.1.0, and Flyway 10.4.1 to pom.xml with proper version properties.

2. **Configuration**: Created DatabaseConfig singleton that supports `${ENV_VAR:default}` syntax for environment variable externalization. Both local and CI profiles configured.

3. **Connection Management**: HikariCP pool with min=2, max=5 connections, auto-commit disabled for transaction control. Provides `executeInTransaction()` with functional callbacks.

4. **Flyway Integration**: Points to `filesystem:../backend/src/main/resources/db/migration` to reuse backend migrations. JUnit 5 extension ensures migrations run once per test run using ExtensionContext root store.

5. **Test Data Seeding**: SQL scripts provide 16 entities across 4 types and 8 government organizations. Uses `ON CONFLICT (id) DO NOTHING` for idempotent inserts.

6. **Test Data Cleanup**: Uses `SET session_replication_role = 'replica'` to disable FK checks for faster TRUNCATE. JUnit extension provides builder pattern for configuration.

7. **Database Assertions**: Comprehensive static assertions using PreparedStatements with proper parameter handling.

8. **Builder Extensions**: Both EntityTestDataBuilder and GovOrgTestDataBuilder now support `buildAndPersist()` for direct database insertion with proper JSONB handling for properties.

9. **Base Test Class**: `DatabaseIntegrationTest` provides SeedEntities and SeedGovOrgs inner classes with all known test data IDs and counts.

10. **Documentation**: README updated with complete Database Integration section including Docker setup, configuration, usage examples, and component reference table.

### File List

**New Files Created:**
- `api-tests/src/main/java/org/newsanalyzer/apitests/config/DatabaseConfig.java`
- `api-tests/src/main/java/org/newsanalyzer/apitests/util/DatabaseConnectionManager.java`
- `api-tests/src/main/java/org/newsanalyzer/apitests/util/FlywayMigrationRunner.java`
- `api-tests/src/main/java/org/newsanalyzer/apitests/util/FlywayMigrationExtension.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/data/TestDataSeeder.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/data/TestDataCleaner.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseCleanupExtension.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseAssertions.java`
- `api-tests/src/test/java/org/newsanalyzer/apitests/data/DatabaseIntegrationTest.java`
- `api-tests/src/test/resources/seed/entities.sql`
- `api-tests/src/test/resources/seed/government_organizations.sql`
- `api-tests/src/test/resources/seed/cleanup.sql`

**Modified Files:**
- `api-tests/pom.xml` - Added PostgreSQL, HikariCP, Flyway dependencies
- `api-tests/src/test/resources/application-local.properties` - Added database configuration
- `api-tests/src/test/resources/application-ci.properties` - Added database configuration
- `api-tests/src/test/java/org/newsanalyzer/apitests/backend/EntityTestDataBuilder.java` - Added DB persistence methods
- `api-tests/src/test/java/org/newsanalyzer/apitests/backend/GovOrgTestDataBuilder.java` - Added DB persistence methods
- `api-tests/README.md` - Added Database Integration documentation

## QA Results

### Review Date: 2025-11-26

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

**Overall: Excellent** - The implementation demonstrates high-quality software engineering practices with well-structured code, proper separation of concerns, and comprehensive documentation. The database integration layer is production-ready with appropriate consideration for test isolation, transaction management, and credential security.

**Strengths:**
- Clean singleton patterns with proper reset methods for testability
- Functional programming style with `TransactionCallback` interfaces
- Proper use of PreparedStatements preventing SQL injection
- Comprehensive JUnit 5 extension architecture
- Idempotent seed data with `ON CONFLICT DO NOTHING`
- Good use of builder pattern for flexible test configuration
- Environment variable externalization for sensitive credentials

### Refactoring Performed

No refactoring required - code quality meets standards.

### Compliance Check

- Coding Standards: ✓ Follows Java conventions, proper error handling, clear naming
- Project Structure: ✓ Files in correct locations per story specification
- Testing Strategy: ✓ JUnit 5 extensions, proper lifecycle management
- All ACs Met: ✓ All 9 acceptance criteria fully implemented

### Improvements Checklist

- [x] All 11 tasks completed as specified
- [x] Database dependencies properly versioned (PostgreSQL 42.7.1, HikariCP 5.1.0, Flyway 10.4.1)
- [x] Connection pooling configured appropriately for tests (min=2, max=5)
- [x] Credential externalization via `${ENV_VAR:default}` syntax
- [x] Flyway migrations point to backend scripts correctly
- [x] Test data isolation via TRUNCATE CASCADE
- [x] Comprehensive database assertions
- [x] Builder persistence methods added
- [x] README documentation complete
- [ ] Consider adding `FlywayMigrationExtension.create()` factory method (minor - currently uses constructor)

### Security Review

**Status: PASS**
- Credentials properly externalized to environment variables
- PreparedStatements used throughout preventing SQL injection
- No hardcoded passwords in source code
- Test-only database (`newsanalyzer_test`) clearly separated from production

**Note:** The `TestDataCleaner` methods `deleteEntitiesByType()`, `deleteEntity()`, and `deleteGovOrg()` use string concatenation for SQL. While acceptable for test utilities with controlled input, these are flagged as informational.

### Performance Considerations

**Status: PASS**
- Connection pool sizing (2-5) appropriate for test scenarios
- `SET session_replication_role = 'replica'` for faster TRUNCATE
- Migrations run once per test run via ExtensionContext.Store
- Auto-commit disabled for proper transaction batching

### Files Modified During Review

None - no modifications required.

### Gate Status

Gate: **PASS** → docs/qa/gates/QA-1.4-database-integration-test-data.yml

### Recommended Status

✓ **Ready for Done** - All acceptance criteria met, code quality excellent, no blocking issues.
