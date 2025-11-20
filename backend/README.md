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
