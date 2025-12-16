# NewsAnalyzer API Tests

[![API Integration Tests](https://github.com/yourusername/AIProject2/actions/workflows/api-tests.yml/badge.svg)](https://github.com/yourusername/AIProject2/actions/workflows/api-tests.yml)

REST Assured-based API integration tests for the NewsAnalyzer platform.

## Prerequisites

- **Java 17** (LTS) - Required for compilation and execution
- **Maven 3.9+** - Build tool
- **Running Services** - Backend and/or Reasoning service must be running

## Project Structure

```
api-tests/
├── pom.xml                                    # Maven build configuration
├── README.md                                  # This file
├── src/
│   ├── main/java/org/newsanalyzer/apitests/
│   │   ├── config/                            # Configuration classes
│   │   │   ├── TestConfig.java                # Environment properties loader
│   │   │   ├── RestAssuredConfig.java         # REST Assured setup
│   │   │   └── Endpoints.java                 # API endpoint constants
│   │   └── util/                              # Test utilities
│   │
│   └── test/
│       ├── java/org/newsanalyzer/apitests/
│       │   ├── BaseApiTest.java               # Base test class
│       │   ├── backend/                       # Backend API tests
│       │   │   └── HealthCheckTest.java       # Backend health checks
│       │   ├── reasoning/                     # Reasoning service tests
│       │   │   └── HealthCheckTest.java       # Reasoning health checks
│       │   └── integration/                   # Cross-service tests
│       │
│       └── resources/
│           ├── application-local.properties   # Local environment config
│           ├── application-ci.properties      # CI environment config
│           └── application-staging.properties # Staging environment config
```

## Quick Start

### 1. Start Required Services

Before running tests, ensure the services are running:

```bash
# Backend (port 8080)
cd ../backend
mvn spring-boot:run

# Reasoning Service (port 8000)
cd ../reasoning-service
uvicorn app.main:app --port 8000
```

### 2. Run Tests

```bash
# Run all tests with local profile (default)
mvn test

# Run with specific profile
mvn test -Plocal
mvn test -Pci
mvn test -Pstaging

# Run only smoke tests
mvn test -Dgroups=smoke

# Run only backend tests
mvn test -Dgroups=backend

# Run only reasoning tests
mvn test -Dgroups=reasoning
```

### 3. Build Without Tests

```bash
mvn clean compile
```

## Configuration

### Environment Profiles

| Profile | Command | Description |
|---------|---------|-------------|
| `local` | `mvn test -Plocal` | Local development (default) |
| `ci` | `mvn test -Pci` | CI/CD pipeline |
| `staging` | `mvn test -Pstaging` | Staging environment |

### Service URLs

| Service | Local | CI |
|---------|-------|-----|
| Backend API | http://localhost:8080 | http://localhost:8080 |
| Reasoning Service | http://localhost:8000 | http://localhost:8000 |

### Configuration Files

- `application-local.properties` - Local development settings
- `application-ci.properties` - CI pipeline settings
- `application-staging.properties` - Staging environment settings

## Test Structure

### Base Classes

- `BaseApiTest` - Extend this class for common setup and utility methods

### Test Categories (Tags)

| Tag | Description |
|-----|-------------|
| `smoke` | Quick health check tests |
| `backend` | Backend API tests |
| `reasoning` | Reasoning service tests |
| `integration` | Cross-service integration tests |

## Integration Tests

Cross-service integration tests verify workflows that span multiple services (backend + reasoning service).

### Prerequisites for Integration Tests

**Both services must be running:**

```bash
# Terminal 1: Start Backend (port 8080)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Terminal 2: Start Reasoning Service (port 8000)
cd reasoning-service
uvicorn app.main:app --port 8000 --reload

# Terminal 3: Run integration tests
cd api-tests
mvn test -Dgroups=integration
```

### Running Integration Tests

```bash
# Run only integration tests
mvn test -Dgroups=integration

# Run all tests EXCEPT integration
mvn test -DexcludedGroups=integration

# Run integration tests with verbose timing
mvn test -Dgroups=integration -Dsurefire.reportFormat=plain

# Run integration tests with specific profile
mvn test -Dgroups=integration -Pci
```

### Integration Test Classes

| Test Class | Description | Acceptance Criteria |
|------------|-------------|---------------------|
| `EntityExtractionWorkflowTest` | Extraction → Storage workflow | AC 1, 5 |
| `EntityValidationWorkflowTest` | Validation → Gov Org linking | AC 2, 4 |
| `OwlReasoningWorkflowTest` | OWL reasoning → Enrichment | AC 3, 5 |
| `EntityLinkingWorkflowTest` | External KB linking | AC 4 |
| `FullPipelineWorkflowTest` | Complete end-to-end pipeline | AC 1-5 |
| `FailureScenarioTest` | Service unavailable, timeouts, invalid data | AC 6 |
| `DataConsistencyTest` | Data integrity across services | AC 4 |
| `PerformanceTest` | Response time measurements | AC 7 |

### Integration Test Structure

```
api-tests/src/test/java/org/newsanalyzer/apitests/
├── integration/
│   ├── IntegrationTestBase.java          # Base class with timing, cleanup
│   ├── EntityExtractionWorkflowTest.java # Extract → Store tests
│   ├── EntityValidationWorkflowTest.java # Validate → Link tests
│   ├── OwlReasoningWorkflowTest.java     # OWL reasoning tests
│   ├── EntityLinkingWorkflowTest.java    # External KB linking tests
│   ├── FullPipelineWorkflowTest.java     # End-to-end pipeline tests
│   ├── FailureScenarioTest.java          # Failure handling tests
│   ├── DataConsistencyTest.java          # Data integrity tests
│   ├── PerformanceTest.java              # Performance measurement tests
│   └── util/
│       ├── ServiceOrchestrator.java      # Cross-service workflow helper
│       └── WorkflowAssertions.java       # Custom assertions
```

### Performance Thresholds

| Workflow | Threshold | Description |
|----------|-----------|-------------|
| Single entity extraction | 2 seconds | Reasoning service call |
| Single entity storage | 500 ms | Backend API call |
| Entity validation | 1 second | Includes gov org lookup |
| OWL reasoning | 3 seconds | Inference can be slow |
| Full pipeline (1 entity) | 5 seconds | End-to-end |
| Full pipeline (10 entities) | 30 seconds | Batch processing |

### Writing Integration Tests

Extend `IntegrationTestBase` for integration test functionality:

```java
@Tag("integration")
@DisplayName("My Integration Test")
class MyIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("Given entity, when workflow executed, then result is correct")
    void shouldProcessEntityWorkflow() {
        // Given - setup using pre-configured clients
        Instant start = startTiming();

        // When - execute cross-service workflow
        Response extractResponse = reasoningClient.extractEntities(text, 0.5);
        endTiming(start, "Extraction");

        // Then - verify results
        assertThat(extractResponse.statusCode()).isEqualTo(200);

        printTimingSummary();
    }

    @AfterEach
    void cleanup() {
        cleanupTestData(); // Deletes tracked entities
    }
}
```

### Using ServiceOrchestrator

The `ServiceOrchestrator` combines multiple service calls into coherent workflows:

```java
// Create orchestrator
ServiceOrchestrator orchestrator = new ServiceOrchestrator(
    entityClient, govOrgClient, reasoningClient);

// Extract and store in one call
WorkflowResult result = orchestrator.extractAndStore(text, confidenceThreshold);

// Validate and link to gov org
ValidationResult validation = orchestrator.validateAndLink(entityId);

// Enrich with OWL reasoning
EnrichmentResult enrichment = orchestrator.enrichWithReasoning(entityId);

// Link to external knowledge bases
LinkingResult linking = orchestrator.linkToExternalKB(entityId);

// Full pipeline
FullPipelineResult fullResult = orchestrator.processArticle(articleText);
```

### Using WorkflowAssertions

Custom assertions for integration test verification:

```java
// Entity consistency
WorkflowAssertions.assertEntityConsistent(extractedEntity, storedEntity);
WorkflowAssertions.assertEntityMatches(response, expectedName, expectedType);

// Schema.org verification
WorkflowAssertions.assertSchemaOrgComplete(response);
WorkflowAssertions.assertSchemaOrgType(response, "GovernmentOrganization");

// External ID verification
WorkflowAssertions.assertExternalIdsPresent(response);
WorkflowAssertions.assertWikidataIdPresent(response);

// Workflow result verification
WorkflowAssertions.assertExtractionSuccessful(result, minExpectedEntities);
WorkflowAssertions.assertFullPipelineSuccessful(result, minExpectedEntities);

// Failure handling
WorkflowAssertions.assertGracefulFailure(response, 404);
```

### Expected Test Duration

| Test Suite | Duration |
|------------|----------|
| Backend tests only | 1-2 min |
| Reasoning tests only | 1-2 min |
| Integration tests only | 3-5 min |
| All tests | 5-8 min |

## Writing Tests

### Example Test

```java
package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.BaseApiTest;
import org.newsanalyzer.apitests.config.Endpoints;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class MyTest extends BaseApiTest {

    @Test
    void shouldReturnEntity_whenGetById() {
        given()
            .spec(getBackendSpec())
            .pathParam("id", "some-uuid")
        .when()
            .get(Endpoints.Backend.ENTITY_BY_ID)
        .then()
            .statusCode(200)
            .body("name", equalTo("Expected Name"));
    }
}
```

### REST Assured BDD Style

```java
given()
    .spec(getBackendSpec())          // Use pre-configured spec
    .contentType(ContentType.JSON)
    .body(requestBody)
.when()
    .post("/api/entities")
.then()
    .statusCode(201)
    .body("id", notNullValue());
```

## Database Integration

### Prerequisites

For database integration tests, you need a PostgreSQL 15+ instance:

```bash
# Using Docker (recommended)
docker run -d --name newsanalyzer-test-db \
  -e POSTGRES_DB=newsanalyzer_test \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

### Configuration

Database settings are in `application-{profile}.properties`:

```properties
# Database connection
db.url=jdbc:postgresql://localhost:5432/newsanalyzer_test
db.username=${DB_USERNAME:postgres}
db.password=${DB_PASSWORD:postgres}

# Connection pool settings
db.pool.minIdle=2
db.pool.maxSize=5
```

Override credentials via environment variables:
```bash
export DB_USERNAME=myuser
export DB_PASSWORD=mypassword
```

### Writing Database Tests

Extend `DatabaseIntegrationTest` for automatic migrations and cleanup:

```java
package org.newsanalyzer.apitests.backend;

import org.junit.jupiter.api.Test;
import org.newsanalyzer.apitests.data.DatabaseIntegrationTest;
import org.newsanalyzer.apitests.data.DatabaseAssertions;

import java.sql.SQLException;
import java.util.UUID;

class MyDatabaseTest extends DatabaseIntegrationTest {

    @Test
    void shouldPersistEntity() throws SQLException {
        // Database is clean and seeded before this runs
        UUID id = EntityTestDataBuilder.aPerson()
            .withName("Test Person")
            .withVerified(true)
            .buildAndPersist();

        DatabaseAssertions.assertEntityExists(id);
        DatabaseAssertions.assertEntityHasName(id.toString(), "Test Person");
        DatabaseAssertions.assertEntityIsVerified(id.toString());
    }

    @Test
    void shouldUseSeededData() {
        // Access known seed data constants
        DatabaseAssertions.assertEntityExists(SeedEntities.ELIZABETH_WARREN_ID);
        DatabaseAssertions.assertGovOrgExists(SeedGovOrgs.DOJ_ID);
        DatabaseAssertions.assertEntityCount(SeedEntities.TOTAL_ENTITY_COUNT);
    }
}
```

### Database Test Components

| Component | Purpose |
|-----------|---------|
| `DatabaseIntegrationTest` | Base class with migrations, cleanup, and seed data constants |
| `DatabaseConnectionManager` | HikariCP connection pool with transaction support |
| `FlywayMigrationExtension` | JUnit extension to run migrations once per test run |
| `DatabaseCleanupExtension` | JUnit extension for cleanup/reseed before each test |
| `TestDataSeeder` | Seed entities and government organizations from SQL |
| `TestDataCleaner` | Truncate tables and reset sequences |
| `DatabaseAssertions` | Fluent assertions for database state verification |

### Test Data Builders with Persistence

Both `EntityTestDataBuilder` and `GovOrgTestDataBuilder` support direct database persistence:

```java
// Build and persist in one step
UUID entityId = EntityTestDataBuilder.aPerson()
    .withName("John Doe")
    .withEntityType("PERSON")
    .withVerified(true)
    .buildAndPersist();

// Persist government organization first (for FK)
UUID deptId = GovOrgTestDataBuilder.aCabinetDepartment()
    .withOfficialName("Department of Test")
    .withAcronym("DOT")
    .buildAndPersist();

// Then entity linked to gov org
UUID govEntityId = EntityTestDataBuilder.aGovernmentOrganization()
    .withName("Department of Test")
    .withGovernmentOrgId(deptId)
    .buildAndPersist();
```

### Seed Data

Pre-seeded test data available via `SeedEntities` and `SeedGovOrgs` constants:

**Entities (16 total):**
- 5 PERSON entities (politicians/officials)
- 5 GOVERNMENT_ORG entities (linked to gov_organizations)
- 3 ORGANIZATION entities (companies, NGOs)
- 3 LOCATION entities (cities, states)

**Government Organizations (8 total):**
- 3 Cabinet Departments (DOJ, DOD, State)
- 3 Independent Agencies (EPA, NASA, CIA)
- 2 Bureaus with parent relationships (FBI, ATF under DOJ)

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| REST Assured | 5.4.0 | API testing DSL |
| JUnit Jupiter | 5.10.1 | Test framework |
| AssertJ | 3.25.1 | Fluent assertions |
| Jackson | 2.16.1 | JSON processing |
| SLF4J Simple | 2.0.9 | Logging |
| PostgreSQL JDBC | 42.7.1 | Database driver |
| HikariCP | 5.1.0 | Connection pooling |
| Flyway | 10.4.1 | Database migrations |

## CI/CD Integration

### GitHub Actions Workflow

API integration tests run automatically via GitHub Actions on:
- Pull requests to `main` or `master` branches
- Push to `main` or `master` branches
- Manual trigger via workflow dispatch

### Workflow Location

`.github/workflows/api-tests.yml`

### What the Pipeline Does

1. **Sets up services**: PostgreSQL 15 service container
2. **Builds backend**: Java 17 Spring Boot application
3. **Installs Python deps**: FastAPI reasoning service + spaCy models
4. **Starts services**: Both backend and reasoning service in background
5. **Health checks**: Waits for services to be healthy (2 min timeout)
6. **Runs tests**: Executes `mvn test -Pci` with full test suite
7. **Reports results**: Uploads artifacts and publishes JUnit summary

### Manual Trigger

To manually run the API tests:

1. Go to **Actions** tab in GitHub
2. Select **API Integration Tests** workflow
3. Click **Run workflow**
4. Optionally enable debug logging
5. Click **Run workflow** button

### Test Artifacts

After each run, these artifacts are available:
- `test-results` - Surefire reports and JaCoCo coverage

### Environment Variables in CI

| Variable | Value | Description |
|----------|-------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/newsanalyzer_test` | Test database |
| `DB_USERNAME` | `postgres` | Database user |
| `DB_PASSWORD` | `postgres` | Database password |
| `BACKEND_URL` | `http://localhost:8080` | Backend API |
| `REASONING_URL` | `http://localhost:8000` | Reasoning service |

### Branch Protection (Optional)

To require tests to pass before merging:

1. Go to **Settings** > **Branches**
2. Add rule for `main` (or `master`)
3. Enable **Require status checks to pass before merging**
4. Select **Run API Integration Tests** as required check

### Estimated Pipeline Duration

| Step | Time |
|------|------|
| Setup & checkout | ~30s |
| Build backend | 2-3 min |
| Install Python deps | 1-2 min |
| Service startup | ~30s |
| Health checks | ~30s |
| Run tests | 2-5 min |
| Upload artifacts | ~10s |
| **Total** | **7-12 min** |

## Troubleshooting

### Services Not Running

```
Connection refused: connect
```

Ensure both services are running before executing tests.

### Wrong Profile

Check active profile in test output:
```
Profile: local
Backend URL: http://localhost:8080
```

### Timeout Issues

Increase timeout in properties file:
```properties
test.timeout.seconds=60
```

## Related Documentation

- [Epic QA-1: API Integration Testing Framework](../docs/stories/QA-1.epic-api-testing-framework.md)
- [Tech Stack](../docs/architecture/tech-stack.md)
- [Coding Standards](../docs/architecture/coding-standards.md)
