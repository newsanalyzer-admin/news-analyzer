# NewsAnalyzer v2 - Technology Stack

**Last Updated:** 2025-11-27
**Version:** 2.0.0-SNAPSHOT

---

## Overview

NewsAnalyzer v2 is a polyglot application consisting of three main services:
1. **Backend API** - Java/Spring Boot REST service
2. **Frontend** - Next.js React application
3. **Reasoning Service** - Python FastAPI service with NLP and OWL reasoning

---

## Backend Service (Java)

### Runtime & Build

| Component | Version | Purpose |
|-----------|---------|---------|
| Java | 17 (LTS) | Runtime platform |
| Maven | 3.9+ | Build tool, dependency management |
| Spring Boot | 3.2.2 | Application framework |

### Spring Boot Starters

| Starter | Purpose |
|---------|---------|
| `spring-boot-starter-web` | REST API, embedded Tomcat |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM |
| `spring-boot-starter-data-redis` | Redis caching |
| `spring-boot-starter-security` | Authentication, authorization |
| `spring-boot-starter-validation` | Bean validation (JSR-380) |
| `spring-boot-starter-actuator` | Health checks, metrics |

### Database & Persistence

| Component | Version | Purpose |
|-----------|---------|---------|
| PostgreSQL | 15+ | Primary database |
| Flyway | (managed by Spring) | Database migrations |
| Hypersistence Utils | 3.7.0 | Hibernate JSONB support |
| Jackson Hibernate6 | (managed by Spring) | Lazy proxy JSON serialization |
| H2 | (test scope) | In-memory test database |

### Security

| Component | Version | Purpose |
|-----------|---------|---------|
| Spring Security | (managed by Spring Boot) | Authentication framework |
| JJWT | 0.12.5 | JWT token generation/validation |

### API Documentation

| Component | Version | Purpose |
|-----------|---------|---------|
| SpringDoc OpenAPI | 2.3.0 | OpenAPI 3.0 / Swagger UI |

### Development Tools

| Component | Version | Purpose |
|-----------|---------|---------|
| Lombok | (managed by Spring Boot) | Boilerplate reduction |
| JaCoCo | 0.8.11 | Code coverage reporting |

### Testing

| Component | Purpose |
|-----------|---------|
| JUnit 5 | Unit testing framework |
| Mockito | Mocking framework |
| Spring Test | Integration testing |
| Spring Security Test | Security testing utilities |

---

## Frontend Service (TypeScript/React)

### Runtime & Build

| Component | Version | Purpose |
|-----------|---------|---------|
| Node.js | 20+ (LTS) | Runtime platform |
| pnpm | 8+ | Package manager |
| TypeScript | 5.3.3 | Type-safe JavaScript |

### Framework & Rendering

| Component | Version | Purpose |
|-----------|---------|---------|
| Next.js | 14.1.0 | React framework, SSR/SSG |
| React | 18.2.0 | UI component library |
| React DOM | 18.2.0 | DOM rendering |

### State Management & Data Fetching

| Component | Version | Purpose |
|-----------|---------|---------|
| TanStack React Query | 5.17.19 | Server state, caching |
| Zustand | 4.5.0 | Client state management |
| Axios | 1.6.5 | HTTP client |
| Zod | 3.22.4 | Schema validation |

### Styling

| Component | Version | Purpose |
|-----------|---------|---------|
| Tailwind CSS | 3.4.1 | Utility-first CSS |
| PostCSS | 8.4.33 | CSS processing |
| Autoprefixer | 10.4.17 | CSS vendor prefixes |
| clsx | 2.1.0 | Conditional class names |
| tailwind-merge | 2.2.0 | Merge Tailwind classes |
| class-variance-authority | 0.7.0 | Component variants |

### UI Components

| Component | Version | Purpose |
|-----------|---------|---------|
| Lucide React | 0.314.0 | Icon library |
| date-fns | 3.2.0 | Date formatting/manipulation |

### Testing

| Component | Version | Purpose |
|-----------|---------|---------|
| Vitest | 1.2.0 | Unit testing framework |
| Vitest UI | 1.2.0 | Test UI dashboard |
| Playwright | 1.41.0 | E2E testing |

### Code Quality

| Component | Version | Purpose |
|-----------|---------|---------|
| ESLint | 8.56.0 | JavaScript/TypeScript linting |
| eslint-config-next | 14.1.0 | Next.js ESLint rules |

---

## Reasoning Service (Python)

### Runtime

| Component | Version | Purpose |
|-----------|---------|---------|
| Python | 3.11+ | Runtime platform |
| pip | latest | Package manager |

### Web Framework

| Component | Version | Purpose |
|-----------|---------|---------|
| FastAPI | 0.109.0 | Async REST API framework |
| Uvicorn | 0.27.0 | ASGI server |
| Pydantic | 2.5.3 | Data validation |
| Pydantic Settings | 2.1.0 | Configuration management |

### HTTP & Networking

| Component | Version | Purpose |
|-----------|---------|---------|
| HTTPX | 0.26.0 | Async HTTP client |

### NLP & Entity Extraction

| Component | Version | Purpose |
|-----------|---------|---------|
| spaCy | 3.8.0+ | NLP pipeline, NER |
| Transformers | 4.38.0 | Hugging Face models |
| PyTorch | 2.2.2 | ML framework |

### OWL Reasoning & Semantic Web

| Component | Version | Purpose |
|-----------|---------|---------|
| RDFLib | 7.0.0 | RDF parsing, SPARQL |
| OWL-RL | 6.0.2 | OWL 2 RL reasoning |
| PySWIP | 0.2.10 | SWI-Prolog integration |

### Data Processing

| Component | Version | Purpose |
|-----------|---------|---------|
| NumPy | 1.26.3 | Numerical computing |
| Pandas | 2.1.4 | Data manipulation |

### Utilities

| Component | Version | Purpose |
|-----------|---------|---------|
| python-dotenv | 1.0.0 | Environment variables |
| python-multipart | 0.0.6 | Multipart form handling |

### Testing

| Component | Version | Purpose |
|-----------|---------|---------|
| pytest | 7.4.4 | Testing framework |
| pytest-cov | 4.1.0 | Coverage reporting |
| pytest-asyncio | 0.23.3 | Async test support |

### Code Quality

| Component | Version | Purpose |
|-----------|---------|---------|
| Black | 23.12.1 | Code formatter |
| Ruff | 0.1.11 | Fast linter |
| mypy | 1.8.0 | Static type checking |

---

## Infrastructure & DevOps

### Database

| Component | Version | Purpose |
|-----------|---------|---------|
| PostgreSQL | 15+ | Primary relational database |
| Redis | 7+ | Caching, session storage |

### Containerization

| Component | Version | Purpose |
|-----------|---------|---------|
| Docker | 24+ | Container runtime |
| Docker Compose | 2.20+ | Multi-container orchestration |

### CI/CD (Planned)

| Component | Purpose |
|-----------|---------|
| GitHub Actions | CI/CD workflows |
| SonarQube | Code quality analysis |

---

## External Services & APIs

### Knowledge Bases (Phase 2)

| Service | Purpose |
|---------|---------|
| Wikidata SPARQL | Entity linking, enrichment |
| DBpedia Lookup | Fallback entity resolution |

### Reference Data

| Service | Purpose |
|---------|---------|
| Schema.org | Entity type vocabulary |
| GovInfo API | Government document metadata |

---

## Version Compatibility Matrix

| Component | Minimum | Recommended | Notes |
|-----------|---------|-------------|-------|
| Java | 17 | 17 | LTS, required for Spring Boot 3.x |
| Node.js | 18 | 20 | LTS versions only |
| Python | 3.10 | 3.11 | Type hints, performance |
| PostgreSQL | 14 | 15 | JSONB features |
| Redis | 6 | 7 | Streams, modules |

---

## Dependency Update Policy

### Security Updates
- Apply immediately when CVE published
- Use Dependabot/Renovate for automated PRs

### Major Version Updates
- Evaluate quarterly
- Test in staging environment
- Update documentation

### Version Pinning Strategy
- **Backend (Maven)**: Pin major.minor, allow patch
- **Frontend (pnpm)**: Pin exact versions in lockfile
- **Python (pip)**: Pin exact versions for reproducibility

---

## Port Allocations

| Service | Development Port | Production Port |
|---------|-----------------|-----------------|
| Backend API | 8080 | 8080 |
| Frontend | 3000 | 3000 |
| Reasoning Service | 8000 | 8000 |
| PostgreSQL | 5432 | 5432 |
| Redis | 6379 | 6379 |

---

## Environment Requirements

### Development Machine
- 16GB RAM minimum (32GB recommended for ML models)
- SSD storage
- Docker Desktop or equivalent
- IDE: IntelliJ IDEA (Java), VS Code (TypeScript/Python)

### Production
- See deployment documentation for production requirements

---

*End of Technology Stack Document*
