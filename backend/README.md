# NewsAnalyzer Backend

Spring Boot 3.2 REST API (Java 17) for NewsAnalyzer v2.

## Tech Stack

- **Java 17** (LTS)
- **Spring Boot 3.2** (Web, Data JPA, Security, Redis)
- **PostgreSQL 15** (primary database with JSONB support)
- **Redis 7** (caching)
- **Flyway** (database migrations)
- **JWT** (authentication)
- **OpenAPI 3.0** (API documentation via Springdoc)

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/org/newsanalyzer/
│   │   │   ├── NewsAnalyzerApplication.java
│   │   │   ├── config/           # Spring configuration
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── service/          # Business logic
│   │   │   ├── repository/       # Data access
│   │   │   ├── model/            # JPA entities
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── security/         # JWT, authentication
│   │   │   └── exception/        # Custom exceptions
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/     # Flyway SQL migrations
│   └── test/                     # Unit & integration tests
└── pom.xml
```

## Setup

### Prerequisites

- Java 17+ (OpenJDK or Eclipse Temurin)
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+

### 1. Configure Database

Copy the example configuration:
```bash
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

Edit `application-dev.yml` with your database credentials.

### 2. Install Dependencies

```bash
./mvnw clean install
```

### 3. Run Database Migrations

```bash
./mvnw flyway:migrate
```

### 4. Run Application

```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

The API will be available at http://localhost:8080

## API Documentation

Once running, access Swagger UI at:
- http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

## Testing

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# Code coverage report
./mvnw jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Database Migrations

Create new migration:
```bash
# Create file: src/main/resources/db/migration/V2__description.sql
```

Run migrations:
```bash
./mvnw flyway:migrate
```

## Building for Production

```bash
./mvnw clean package -DskipTests
# JAR file: target/newsanalyzer-backend-2.0.0-SNAPSHOT.jar
```

## Docker Build

```bash
docker build -t newsanalyzer-backend:latest .
```

## Running with Observability (Outside Docker)

When running the backend on your host machine (not in Docker), the OTel Java Agent provides zero-code auto-instrumentation for HTTP, JPA, JDBC, and Redis. You need:

1. The observability stack running in Docker (from the root of the repo):
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. The OTel Java Agent JAR downloaded locally:
   ```bash
   # Download once (same version pinned in Dockerfiles)
   curl -L -o opentelemetry-javaagent.jar \
     https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.12.0/opentelemetry-javaagent.jar
   ```

3. Start the backend with the agent and OTel env vars:

   **Linux / macOS:**
   ```bash
   OTEL_SERVICE_NAME=newsanalyzer-backend \
   OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
   OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
   OTEL_TRACES_SAMPLER=always_on \
   OTEL_METRICS_EXPORTER=otlp \
   OTEL_LOGS_EXPORTER=otlp \
   OTEL_RESOURCE_ATTRIBUTES=deployment.environment=dev \
   ./mvnw spring-boot:run \
     -Dspring.profiles.active=dev \
     -Dspring-boot.run.jvmArguments="-javaagent:./opentelemetry-javaagent.jar"
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:OTEL_SERVICE_NAME="newsanalyzer-backend"
   $env:OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
   $env:OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
   $env:OTEL_TRACES_SAMPLER="always_on"
   $env:OTEL_METRICS_EXPORTER="otlp"
   $env:OTEL_LOGS_EXPORTER="otlp"
   $env:OTEL_RESOURCE_ATTRIBUTES="deployment.environment=dev"
   ./mvnw spring-boot:run `
     -Dspring.profiles.active=dev `
     '-Dspring-boot.run.jvmArguments="-javaagent:./opentelemetry-javaagent.jar"'
   ```

   **Windows (Command Prompt):**
   ```cmd
   set OTEL_SERVICE_NAME=newsanalyzer-backend
   set OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
   set OTEL_EXPORTER_OTLP_PROTOCOL=grpc
   set OTEL_TRACES_SAMPLER=always_on
   set OTEL_METRICS_EXPORTER=otlp
   set OTEL_LOGS_EXPORTER=otlp
   set OTEL_RESOURCE_ATTRIBUTES=deployment.environment=dev
   mvnw spring-boot:run -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments="-javaagent:./opentelemetry-javaagent.jar"
   ```

Once running, traces, metrics, and logs will appear in Grafana at http://localhost:3001.

> **Note:** The key difference from Docker is the endpoint — `localhost:4317` instead of `otel-collector:4317`. The Collector's ports are exposed to the host by Docker Compose.

## Environment Variables (Production)

- `DB_HOST` - PostgreSQL host
- `DB_NAME` - Database name
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password
- `REDIS_HOST` - Redis host
- `REDIS_PORT` - Redis port
- `REDIS_PASSWORD` - Redis password
- `JWT_SECRET` - JWT signing secret (min 256 bits)
- `REASONING_SERVICE_URL` - Python reasoning service URL

## License

MIT License - See [LICENSE](../LICENSE)
