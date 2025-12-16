# NewsAnalyzer v2 - Source Tree

**Last Updated:** 2025-11-27
**Version:** 2.0.0-SNAPSHOT

---

## Overview

NewsAnalyzer is a polyglot monorepo containing four main services and supporting documentation/tooling.

```
AIProject2/
├── backend/                    # Java Spring Boot REST API
├── frontend/                   # Next.js React Application
├── reasoning-service/          # Python FastAPI + OWL Reasoning
├── api-tests/                  # REST Assured API Integration Tests (planned)
├── docs/                       # Project documentation
├── .bmad-core/                 # BMAD methodology framework
├── .bmad-infrastructure-devops/# Infrastructure templates
├── .claude/                    # Claude Code agent commands
├── .github/workflows/          # CI/CD pipelines
├── api_keys/                   # API keys (gitignored)
└── .vscode/                    # VS Code workspace settings
```

---

## Backend Service (Java/Spring Boot)

```
backend/
├── pom.xml                                    # Maven build config
├── src/
│   ├── main/
│   │   ├── java/org/newsanalyzer/
│   │   │   ├── NewsAnalyzerApplication.java  # Spring Boot entry point
│   │   │   │
│   │   │   ├── config/                       # Configuration classes
│   │   │   │   ├── JacksonConfig.java        # Jackson/Hibernate6 proxy config
│   │   │   │   ├── JpaConfig.java            # JPA/Hibernate config
│   │   │   │   ├── OpenApiConfig.java        # Swagger/OpenAPI config
│   │   │   │   └── SecurityConfig.java       # Spring Security config
│   │   │   │
│   │   │   ├── controller/                   # REST endpoints
│   │   │   │   ├── EntityController.java     # /api/entities
│   │   │   │   └── GovernmentOrganizationController.java  # /api/government-orgs
│   │   │   │
│   │   │   ├── dto/                          # Data Transfer Objects
│   │   │   │   ├── EntityDTO.java            # Entity response DTO
│   │   │   │   └── CreateEntityRequest.java  # Entity creation request
│   │   │   │
│   │   │   ├── model/                        # JPA Entities
│   │   │   │   ├── Entity.java               # Core entity model
│   │   │   │   ├── EntityType.java           # Entity type enum
│   │   │   │   ├── GovernmentOrganization.java  # Gov org model
│   │   │   │   └── converter/                # JPA attribute converters
│   │   │   │       ├── OrganizationTypeConverter.java
│   │   │   │       └── GovernmentBranchConverter.java
│   │   │   │
│   │   │   ├── repository/                   # Spring Data repositories
│   │   │   │   ├── EntityRepository.java     # Entity CRUD
│   │   │   │   └── GovernmentOrganizationRepository.java
│   │   │   │
│   │   │   └── service/                      # Business logic
│   │   │       ├── EntityService.java        # Entity operations
│   │   │       ├── GovernmentOrganizationService.java
│   │   │       └── SchemaOrgMapper.java      # Schema.org JSON-LD mapping
│   │   │
│   │   └── resources/
│   │       ├── application.yml               # Main config
│   │       ├── application-dev.yml           # Dev profile config
│   │       └── db/migration/                 # Flyway migrations
│   │           ├── V1__initial_schema.sql    # Initial tables
│   │           ├── V2.9__enable_pg_extensions.sql
│   │           ├── V3__create_government_organizations.sql
│   │           └── V4__add_entity_gov_org_link.sql
│   │
│   └── test/
│       ├── java/org/newsanalyzer/
│       │   ├── controller/
│       │   │   └── EntityControllerTest.java
│       │   ├── model/
│       │   │   └── EntityTest.java
│       │   ├── repository/
│       │   │   └── EntityRepositoryTest.java
│       │   └── service/
│       │       └── EntityServiceTest.java
│       │
│       └── resources/
│           └── application-test.yml          # Test profile config
│
└── target/                                   # Maven build output (gitignored)
```

### Key Backend Files

| File | Purpose |
|------|---------|
| `Entity.java` | Core domain model with Schema.org JSONB, external IDs |
| `EntityService.java` | Business logic, enrichment orchestration |
| `EntityDTO.java` | API response with JSON-LD transformation |
| `GovernmentOrganization.java` | MDM master record for gov entities |
| `application.yml` | Database, Redis, service configuration |

---

## Frontend Service (Next.js/React)

```
frontend/
├── package.json                    # Dependencies & scripts
├── pnpm-lock.yaml                  # Lockfile
├── next.config.js                  # Next.js configuration
├── tsconfig.json                   # TypeScript config
├── tailwind.config.ts              # Tailwind CSS config
├── postcss.config.js               # PostCSS config
├── .env.local.example              # Environment template
│
├── public/                         # Static assets
│
├── src/
│   ├── app/                        # Next.js App Router pages
│   │   ├── layout.tsx              # Root layout
│   │   ├── page.tsx                # Home page
│   │   ├── entities/
│   │   │   └── page.tsx            # Entity list page
│   │   └── government-orgs/
│   │       └── page.tsx            # Gov org list page
│   │
│   ├── components/                 # React components
│   │   └── EntityCard.tsx          # Entity display card
│   │
│   ├── lib/                        # Utilities
│   │   ├── utils.ts                # General utilities
│   │   └── api/
│   │       └── entities.ts         # Entity API client
│   │
│   └── types/                      # TypeScript types
│       └── entity.ts               # Entity type definitions
│
└── node_modules/                   # Dependencies (gitignored)
```

### Key Frontend Files

| File | Purpose |
|------|---------|
| `layout.tsx` | App shell, global providers |
| `entities/page.tsx` | Entity listing with filters |
| `api/entities.ts` | React Query hooks for entity API |
| `types/entity.ts` | TypeScript interfaces matching backend DTOs |

---

## Reasoning Service (Python/FastAPI)

```
reasoning-service/
├── requirements.txt                # Python dependencies
├── README.md                       # Service documentation
├── .env.example                    # Environment template
│
├── app/
│   ├── __init__.py
│   ├── main.py                     # FastAPI application entry
│   │
│   ├── api/                        # REST endpoints
│   │   ├── __init__.py
│   │   ├── entities.py             # /api/entities (extraction, validation)
│   │   ├── government_orgs.py      # /api/government-orgs
│   │   ├── reasoning.py            # /api/reasoning (OWL inference)
│   │   └── fallacies.py            # /api/fallacies (logic analysis)
│   │
│   └── services/                   # Business logic
│       ├── __init__.py
│       ├── entity_extractor.py     # NLP entity extraction (spaCy)
│       ├── schema_mapper.py        # Schema.org type mapping
│       ├── owl_reasoner.py         # OWL 2 RL reasoning (rdflib/owlrl)
│       ├── gov_org_ingestion.py    # Gov org data ingestion
│       └── gov_org_validator.py    # Gov org validation rules
│
├── ontology/                       # OWL ontology files
│   └── newsanalyzer.ttl            # NewsAnalyzer ontology (Turtle)
│
├── tests/                          # Test suite
│   ├── __init__.py
│   ├── test_owl_reasoner.py        # Reasoner tests
│   └── services/
│       ├── __init__.py
│       ├── test_schema_mapper.py
│       └── test_entity_extractor.py
│
└── data/                           # Data files (Phase 2)
    └── schema-org-vocab.json       # Cached Schema.org vocab (planned)
```

### Key Reasoning Service Files

| File | Purpose |
|------|---------|
| `main.py` | FastAPI app, router registration |
| `entity_extractor.py` | spaCy NER, entity extraction |
| `owl_reasoner.py` | RDFLib + OWL-RL inference engine |
| `schema_mapper.py` | Entity → Schema.org type mapping |
| `newsanalyzer.ttl` | Custom OWL ontology |

---

## API Tests (Java/REST Assured) - Planned

```
api-tests/                                      # Epic QA-1 (planned)
├── pom.xml                                     # Maven build config
├── README.md                                   # Test project documentation
│
├── src/
│   ├── main/java/org/newsanalyzer/apitests/
│   │   ├── config/                             # Configuration classes
│   │   │   ├── TestConfig.java                 # Environment properties loader
│   │   │   ├── RestAssuredConfig.java          # REST Assured setup
│   │   │   ├── DatabaseConfig.java             # Database connection config
│   │   │   └── Endpoints.java                  # API endpoint constants
│   │   │
│   │   └── util/                               # Test utilities
│   │       ├── DatabaseConnectionManager.java  # HikariCP connection pool
│   │       ├── FlywayMigrationRunner.java      # Migration runner
│   │       ├── DatabaseAssertions.java         # DB verification helpers
│   │       └── ServiceOrchestrator.java        # Cross-service workflows
│   │
│   └── test/
│       ├── java/org/newsanalyzer/apitests/
│       │   ├── BaseApiTest.java                # Base test class
│       │   │
│       │   ├── backend/                        # Backend API tests
│       │   │   ├── EntityCrudTest.java         # Entity CRUD operations
│       │   │   ├── EntitySearchTest.java       # Entity search/filter
│       │   │   ├── EntityValidationTest.java   # Entity validation
│       │   │   ├── GovOrgCrudTest.java         # Gov org CRUD
│       │   │   ├── GovOrgSearchTest.java       # Gov org search
│       │   │   ├── GovOrgFilterTest.java       # Gov org filters
│       │   │   ├── GovOrgHierarchyTest.java    # Gov org hierarchy
│       │   │   ├── GovOrgStatisticsTest.java   # Gov org statistics
│       │   │   ├── HealthCheckTest.java        # Backend health check
│       │   │   ├── EntityTestDataBuilder.java  # Test data builder
│       │   │   └── GovOrgTestDataBuilder.java  # Test data builder
│       │   │
│       │   ├── reasoning/                      # Reasoning service tests
│       │   │   ├── EntityExtractionTest.java   # Entity extraction
│       │   │   ├── EntityLinkingTest.java      # KB linking
│       │   │   ├── OwlReasoningTest.java       # OWL reasoning
│       │   │   ├── OntologyStatsTest.java      # Ontology stats
│       │   │   ├── SparqlQueryTest.java        # SPARQL queries
│       │   │   ├── HealthCheckTest.java        # Reasoning health
│       │   │   └── ReasoningTestDataBuilder.java
│       │   │
│       │   ├── integration/                    # Cross-service tests
│       │   │   ├── IntegrationTestBase.java    # Integration base class
│       │   │   ├── EntityExtractionWorkflowTest.java
│       │   │   ├── EntityValidationWorkflowTest.java
│       │   │   ├── OwlReasoningWorkflowTest.java
│       │   │   ├── EntityLinkingWorkflowTest.java
│       │   │   ├── FullPipelineWorkflowTest.java
│       │   │   ├── FailureScenarioTest.java
│       │   │   ├── DataConsistencyTest.java
│       │   │   └── PerformanceTest.java
│       │   │
│       │   └── data/                           # Database utilities
│       │       ├── TestDataSeeder.java         # Data seeding
│       │       ├── TestDataCleaner.java        # Data cleanup
│       │       └── DatabaseIntegrationTest.java
│       │
│       └── resources/
│           ├── application-local.properties    # Local config
│           ├── application-ci.properties       # CI config
│           └── seed/                           # Test data SQL
│               ├── entities.sql
│               ├── government_organizations.sql
│               └── cleanup.sql
│
└── target/                                     # Maven build output (gitignored)
```

### Key API Test Files

| File | Purpose |
|------|---------|
| `BaseApiTest.java` | Common REST Assured setup, logging, timeouts |
| `EntityCrudTest.java` | Tests for all 13 Entity endpoints |
| `GovOrgCrudTest.java` | Tests for all 21 Government Org endpoints |
| `EntityExtractionTest.java` | Tests for reasoning service extraction |
| `IntegrationTestBase.java` | Multi-service test configuration |
| `FullPipelineWorkflowTest.java` | End-to-end extraction → storage → enrichment |

### Test Profiles

| Profile | Command | Purpose |
|---------|---------|---------|
| `local` | `mvn test -Plocal` | Live services, local database |
| `ci` | `mvn test -Pci` | Live internal, mocked external KBs |
| `mock` | `mvn test -Pmock` | All mocked (WireMock) |

---

## Documentation

```
docs/
├── ROADMAP.md                           # Master project roadmap
│
├── architecture/                        # Technical documentation
│   ├── coding-standards.md              # Coding conventions
│   ├── tech-stack.md                    # Technology stack
│   ├── source-tree.md                   # This file
│   └── adr/                             # Architecture Decision Records
│       ├── ADR-QA-001-api-test-project-location.md
│       ├── ADR-QA-002-test-database-strategy.md
│       └── ADR-QA-003-mock-vs-live-testing.md
│
├── qa/                                  # QA artifacts
│   └── gates/                           # QA gate decisions
│
├── stories/                             # User stories
│   │
│   ├── # Epic QA-1: API Integration Testing Framework
│   ├── QA-1.epic-api-testing-framework.md    # Epic document
│   ├── QA-1.1.api-test-project-setup.md      # Project setup
│   ├── QA-1.2.backend-api-test-suite.md      # Backend tests
│   ├── QA-1.3.reasoning-service-test-suite.md # Reasoning tests
│   ├── QA-1.4.database-integration-test-data.md # Database integration
│   ├── QA-1.5.cicd-pipeline-integration.md   # CI/CD pipeline
│   ├── QA-1.6.cross-service-integration-tests.md # Integration tests
│   │
│   ├── # Phase 2: Schema.org Enrichment
│   ├── PHASE_2_IMPLEMENTATION_PLAN.md   # Phase 2 master plan
│   ├── PHASE_2_EPIC_INDEX.md            # Epic navigation
│   ├── 2.1.1.wikidata-entity-lookup.md
│   ├── 2.1.2.dbpedia-entity-lookup.md
│   ├── 2.1.3.entity-disambiguation.md
│   ├── 2.1.4.external-linking-api.md
│   ├── 2.2.1.wikidata-property-extraction.md
│   ├── 2.2.2.property-merge-strategy.md
│   ├── 2.2.3.enrichment-pipeline.md
│   ├── 2.2.4.java-backend-integration.md
│   ├── 2.3.1.jsonld-generation.md
│   ├── 2.3.2.public-entity-pages.md
│   ├── 2.3.3.entity-sitemap.md
│   ├── 2.4.1.schema-validation-service.md
│   ├── 2.4.2.validation-api-endpoint.md
│   └── 2.4.3.bulk-validation-report.md
│
├── previousProject/                     # Legacy documentation
│   ├── brainstorming-session-results.md
│   ├── business-requirements.md
│   ├── ontology-research-prompt.md
│   └── ontology-standards-research-report.md
│
├── reference/                           # External reference materials
│   └── APIs and Other Sources/
│       ├── congress.gov/
│       │   ├── README.md
│       │   └── Glossary of Legislative Terms.md
│       └── uscode.house.gov/
│           └── USLM-User-Guide.pdf
│
├── analysis/                            # Analysis documents (Phase 1.6)
├── deployment/                          # Deployment guides
└── workSummaries/                       # Work session summaries
```

---

## BMAD Framework

```
.bmad-core/
├── core-config.yaml                     # BMAD configuration
├── user-guide.md                        # BMAD usage guide
├── enhanced-ide-development-workflow.md
├── working-in-the-brownfield.md
│
├── agents/                              # Agent personas
│   ├── po.md                            # Product Owner (Sarah)
│   ├── architect.md                     # System Architect (Winston)
│   ├── dev.md                           # Developer (James)
│   ├── qa.md                            # QA Engineer
│   ├── pm.md                            # Project Manager
│   ├── sm.md                            # Scrum Master
│   ├── analyst.md                       # Business Analyst
│   ├── ux-expert.md                     # UX Designer
│   ├── bmad-master.md                   # BMAD Master
│   └── bmad-orchestrator.md             # BMAD Orchestrator
│
├── tasks/                               # Reusable task templates
├── templates/                           # Document templates
├── checklists/                          # Review checklists
├── data/                                # Reference data
├── workflows/                           # Workflow definitions
├── utils/                               # Utility scripts
└── agent-teams/                         # Team configurations
```

---

## Claude Code Commands

```
.claude/
├── settings.json                        # Claude Code settings
└── commands/
    └── BMad/
        ├── agents/                      # Agent activation commands
        │   ├── po.md                    # /BMad:agents:po
        │   ├── architect.md             # /BMad:agents:architect
        │   ├── dev.md                   # /BMad:agents:dev
        │   └── ...
        └── tasks/                       # Task execution commands
            ├── create-next-story.md     # /BMad:tasks:create-next-story
            ├── qa-gate.md               # /BMad:tasks:qa-gate
            └── ...
```

---

## Root Files

```
AIProject2/
├── README.md                            # Project overview
├── .gitignore                           # Git ignore rules
├── docker-compose.yml                   # Local development stack (planned)
├── PHASE_1.6_RUN_THIS_NOW.md           # Phase 1.6 quick start
├── PHASE_1.6_AUTOMATED_DEPLOYMENT.md   # Deployment automation
└── test_deployment.md                   # Deployment test results
```

---

## Directory Purposes

| Directory | Purpose | Primary Language |
|-----------|---------|------------------|
| `backend/` | REST API, database access, business logic | Java 17 |
| `frontend/` | User interface, client-side state | TypeScript |
| `reasoning-service/` | NLP, OWL reasoning, enrichment | Python 3.11 |
| `api-tests/` | API integration tests (REST Assured) | Java 17 |
| `docs/` | All project documentation | Markdown |
| `.bmad-core/` | BMAD methodology framework | YAML/Markdown |
| `.claude/` | Claude Code agent commands | Markdown |
| `.github/workflows/` | CI/CD pipeline definitions | YAML |

---

## Naming Conventions

### Files

| Type | Convention | Example |
|------|------------|---------|
| Java classes | PascalCase | `EntityService.java` |
| TypeScript/React | PascalCase for components | `EntityCard.tsx` |
| Python modules | snake_case | `entity_extractor.py` |
| SQL migrations | `V{version}__{description}.sql` | `V3__create_government_organizations.sql` |
| Story files | `{epic}.{story}.{slug}.md` | `2.1.1.wikidata-entity-lookup.md` |

### Directories

| Type | Convention | Example |
|------|------------|---------|
| Java packages | lowercase, dot-separated | `org.newsanalyzer.service` |
| TypeScript | kebab-case | `src/lib/api/` |
| Python packages | snake_case | `app/services/` |

---

## Import Organization

### Java (IntelliJ style)
```java
import java.* // Standard library
import javax.*
import org.* // Third party
import com.*
import org.newsanalyzer.* // Project imports
```

### TypeScript
```typescript
// React/Next.js
import { useState } from 'react'
import Link from 'next/link'

// Third party
import { useQuery } from '@tanstack/react-query'

// Internal - absolute
import { cn } from '@/lib/utils'

// Internal - relative
import { EntityCard } from './EntityCard'

// Types
import type { Entity } from '@/types/entity'
```

### Python
```python
# Standard library
import json
from typing import List, Optional

# Third party
from fastapi import APIRouter
from pydantic import BaseModel

# Internal
from app.services.entity_extractor import EntityExtractor
```

---

## Build Outputs (Gitignored)

| Directory | Contents |
|-----------|----------|
| `backend/target/` | Maven build artifacts, JARs |
| `frontend/node_modules/` | npm dependencies |
| `frontend/.next/` | Next.js build cache |
| `reasoning-service/__pycache__/` | Python bytecode |
| `reasoning-service/.venv/` | Python virtual environment |
| `api-tests/target/` | Maven test artifacts, reports |

---

## Related Architecture Decision Records

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-QA-001](adr/ADR-QA-001-api-test-project-location.md) | API Test Project Location | Accepted |
| [ADR-QA-002](adr/ADR-QA-002-test-database-strategy.md) | Test Database Strategy | Accepted |
| [ADR-QA-003](adr/ADR-QA-003-mock-vs-live-testing.md) | Mock vs Live Testing | Accepted |

---

*End of Source Tree Document*
