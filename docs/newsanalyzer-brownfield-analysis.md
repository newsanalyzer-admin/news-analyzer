# NewsAnalyzer - Brownfield Architecture Analysis
## Learning from a Failed Architecture for Greenfield Redesign

**Document Version:** 1.0
**Analysis Date:** 2025-11-19
**Analyst:** Winston (Architect Agent)
**Original Project Location:** `D:\VSCProjects\NewsAnalyzer`
**Status:** 🔴 FAILED ARCHITECTURE - DO NOT REPLICATE

---

## Executive Summary

### What Went Wrong

The NewsAnalyzer project represents a **classic case of premature optimization and architectural over-engineering** that failed due to:

1. **Data Model Myopia** - Designed database schema around government entities as "first-class citizens" rather than generalized entity model
2. **Premature Specialization** - Government entities treated differently than other entities (persons, organizations, locations)
3. **Discovery-Driven Complexity** - Business requirement for "reasoning error detection" discovered AFTER building relational schema, leading to late ontology/Prolog adoption
4. **Database Sprawl** - 5 databases (PostgreSQL, Neo4j, MongoDB, Redis, Elasticsearch) with complex synchronization
5. **Integration Brittleness** - Java ↔ Python bridge via filesystem/subprocess, fragile and slow
6. **Architecture Mismatch** - Modular monolith appropriate for early stage, but ontology/reasoning requirements demanded different foundation

### Critical Lesson Learned

> **"Design your data model for the BROADEST set of information sources from day one, not the first source you implement."**

The fatal flaw: Focusing on WHERE data comes from (government APIs) rather than WHAT KINDS of entities and relationships the system needs to reason about.

### Salvageable Components

**Keep These Patterns:**
- ✅ Modular monolith architecture (appropriate for small projects)
- ✅ Python entity tagger concept (SQLite + pattern matching)
- ✅ Dual-write coordination pattern (PostgreSQL + Neo4j sync)
- ✅ Flyway database migrations
- ✅ Spring Boot service layer patterns

**Discard These Patterns:**
- ❌ Government-entity-first data model
- ❌ Five-database architecture
- ❌ Java → Python subprocess integration
- ❌ Separate entity types (GovernmentEntity, Person, Organization)
- ❌ Late-stage ontology retrofit attempt

---

## Document Scope

This document captures the **ACTUAL STATE** of the failed NewsAnalyzer codebase to inform a greenfield rewrite. It focuses on:

1. **Architectural Decisions** - Why they were made, why they failed
2. **Data Model Problems** - Government-entity-first vs. generalized entity model
3. **Integration Challenges** - Java/Python bridge brittleness
4. **What to Salvage** - Patterns that worked despite the failure

**Audience:** Architects designing the greenfield replacement
**Purpose:** Learn from mistakes, avoid repeating them

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-11-19 | 1.0 | Initial brownfield analysis | Winston (Architect) |

---

## Quick Reference - Critical Files

### Java Backend Entry Points

**Main Application:**
- `backend/src/main/java/com/newsanalyzer/NewsAnalyzerApplication.java` - Spring Boot entry point with FOUR database configurations

**Key Services (The Problem):**
- `service/GovernmentEntityService.java` - Government entities (specialized)
- `service/PersonService.java` - People (separate!)
- `service/OrganizationService.java` - Organizations (also separate!)
- `service/PythonNlpService.java` - Java→Python bridge (subprocess hell)
- `service/EntitySyncService.java` - PostgreSQL→Neo4j dual-write sync
- `service/Neo4jEntitySyncService.java` - More sync complexity

**Models (The Fatal Flaw):**
- `model/GovernmentEntity.java` - Specialized government entity model
- `model/Person.java` - Separate person model
- `model/Organization.java` - Separate organization model
- `model/graph/GovernmentEntityNode.java` - Neo4j version of government entities
- `model/graph/PersonNode.java` - Neo4j version of persons

### Python NLP Tools

**Entity Tagger (The One Thing That Works):**
- `python-tools/entity_tagger.py` - Pattern-based entity extraction using SQLite
- `python-tools/entity_reference_builder.py` - Downloads entities from Federal Register API
- `python-tools/entity_reference.db` - SQLite database with 469 government entities

**The Integration Problem:**
- Java calls Python via `ProcessBuilder`
- Writes article text to temp file
- Python reads temp file, writes JSON output
- Java parses JSON output
- **Latency:** ~500ms per article (unacceptable)

### Configuration

**Multi-Database Nightmare:**
- `docker-compose.yml` - 5 databases + Ollama + Elasticsearch
- `backend/src/main/resources/application.yml` - Database configs for PostgreSQL, Neo4j, MongoDB, Redis, Elasticsearch

---

## High-Level Architecture

### Intended Architecture (What Was Planned)

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Backend                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Government   │  │   Person     │  │ Organization │      │
│  │   Entity     │  │   Service    │  │   Service    │      │
│  │   Service    │  │              │  │              │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                 │                 │              │
│         └─────────────────┴─────────────────┘              │
│                           │                                │
│                  ┌────────▼─────────┐                      │
│                  │  Dual-Write      │                      │
│                  │  Coordinator     │                      │
│                  └────────┬─────────┘                      │
│         ┌─────────────────┴─────────────────┐              │
│         │                                   │              │
│    ┌────▼────┐                        ┌────▼────┐         │
│    │PostgreSQL│                        │  Neo4j  │         │
│    └─────────┘                        └─────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### Actual Architecture (What Exists)

```
┌───────────────────────────────────────────────────────────────────────┐
│                      Spring Boot Backend                              │
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │GovernmentEntity│  │   Person     │  │ Organization │              │
│  │   Service    │  │   Service    │  │   Service    │              │
│  │(246 entities)│  │  (separate!) │  │  (separate!) │              │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │
│         │                 │                 │                       │
│  ┌──────▼──────────────────▼─────────────────▼────┐                │
│  │       PythonNlpService (subprocess)            │                │
│  │  - Writes temp files                           │                │
│  │  - Calls Python via ProcessBuilder             │                │
│  │  - Parses JSON from stdout                     │                │
│  │  - 500ms latency per call                      │                │
│  └──────────────────┬─────────────────────────────┘                │
│                     │                                               │
└─────────────────────┼───────────────────────────────────────────────┘
                      │
         ┌────────────▼────────────┐
         │  Python entity_tagger.py│
         │  - SQLite with 469 govt │
         │    entities             │
         │  - Pattern matching     │
         │  - Works well!          │
         └────────────┬────────────┘
                      │
      ┌───────────────┴───────────────────┬──────────────┬───────────┐
      │                                   │              │           │
  ┌───▼──────┐  ┌──────────┐  ┌─────────▼──┐  ┌────────▼──┐  ┌─────▼─────┐
  │PostgreSQL│  │  Neo4j   │  │  MongoDB   │  │   Redis   │  │Elasticsearch│
  │(master)  │  │(relationships│ │ (articles) │  │  (cache)  │  │  (search)  │
  └──────────┘  └──────────┘  └────────────┘  └───────────┘  └────────────┘
       │              │
       │  Dual-write  │
       │  sync via    │
       │  EntitySync  │
       │  Service     │
       └──────────────┘
```

### The Ontology Retrofit That Never Happened

The research report (`docs/previousProject/ontology-standards-research-report.md`) shows the team discovered they needed:

1. **Schema.org ontology** - For generalized entity representation
2. **Prolog reasoning** - For detecting logical fallacies
3. **JSON-LD format** - For LLM integration

But this was discovered AFTER building:
- Specialized database schema for government entities
- Separate models for Person, Organization
- PostgreSQL-first architecture

**Result:** Impossible to retrofit without complete rewrite

---

## Actual Tech Stack

### Backend (Java/Spring Boot)

| Component | Technology | Version | Notes |
|-----------|-----------|---------|-------|
| **Language** | Java | 17 | ✅ Modern, fine |
| **Framework** | Spring Boot | 3.2.0 | ✅ Good choice |
| **Build Tool** | Maven | 3.x | ✅ Standard |
| **Primary DB** | PostgreSQL | 15 | ✅ Solid |
| **Graph DB** | Neo4j | 5.15 + APOC | ⚠️ Overkill for use case |
| **Document DB** | MongoDB | 7 | ❌ Unnecessary, articles could be in PostgreSQL JSONB |
| **Cache** | Redis | 7 | ✅ Appropriate |
| **Search** | Elasticsearch | 8.11 | ❌ Premature, PostgreSQL full-text would suffice |
| **NLP** | Stanford CoreNLP | 4.5.1 | ⚠️ Heavy, unused in Python tagger |
| **Web Scraping** | Jsoup + Selenium | Latest | ✅ Appropriate |
| **LLM** | Anthropic Claude | 2.10.0 | ✅ Good choice, underutilized |
| **LLM (Local)** | Ollama | Latest | ⚠️ Docker service, rarely used |
| **Migrations** | Flyway | 9.22.3 | ✅ Excellent |

**Architecture Pattern:** Modular Monolith
**Verdict:** ✅ Appropriate for small project, but database sprawl is insane

### Frontend (React/TypeScript)

| Component | Technology | Version | Notes |
|-----------|-----------|---------|-------|
| **Framework** | React | 18.2.0 | ✅ Standard |
| **Language** | TypeScript | 5.3.3 | ✅ Good |
| **Routing** | React Router | 6.20.0 | ✅ Standard |
| **HTTP Client** | Axios | 1.6.2 | ✅ Fine |
| **Build Tool** | Create React App | 5.0.1 | ⚠️ Deprecated, should use Vite |

**Verdict:** ✅ Frontend is fine, minimal technical debt

### Python NLP Tools

| Component | Purpose | Status |
|-----------|---------|--------|
| **entity_tagger.py** | Pattern-based entity extraction | ✅ **Works well!** |
| **entity_reference_builder.py** | Download entities from Federal Register API | ✅ Solid |
| **entity_reference.db** | SQLite with 469 government entities | ✅ Good data source |
| **usc_xml_parser.py** | Parse U.S. Code XML | ⚠️ Single-purpose, limited use |

**Integration Method:** Java subprocess calls
**Latency:** ~500ms per article
**Verdict:** ❌ Integration is terrible, but the Python tools themselves are good

---

## Source Tree and Module Organization

### Repository Structure (Monorepo)

```text
NewsAnalyzer/
├── backend/                      # Spring Boot Java backend
│   ├── src/main/java/com/newsanalyzer/
│   │   ├── config/               # Database configs (4 databases!)
│   │   ├── controller/           # 15+ REST controllers
│   │   ├── service/              # THE PROBLEM ZONE
│   │   │   ├── GovernmentEntityService.java    # ❌ Specialized
│   │   │   ├── PersonService.java              # ❌ Separate
│   │   │   ├── OrganizationService.java        # ❌ Separate
│   │   │   ├── PythonNlpService.java           # ❌ Subprocess hell
│   │   │   ├── EntitySyncService.java          # ❌ Dual-write complexity
│   │   │   └── Neo4jEntitySyncService.java     # ❌ More sync
│   │   ├── model/                # THE DATA MODEL PROBLEM
│   │   │   ├── GovernmentEntity.java           # ❌ Specialized
│   │   │   ├── Person.java                     # ❌ Separate
│   │   │   ├── Organization.java               # ❌ Separate
│   │   │   ├── graph/                          # Neo4j versions
│   │   │   │   ├── GovernmentEntityNode.java   # ❌ Duplicate model
│   │   │   │   └── PersonNode.java             # ❌ Duplicate model
│   │   │   ├── Claim.java                      # ✅ This is good
│   │   │   ├── Article.java                    # ✅ This is good
│   │   │   └── NewsSource.java                 # ✅ This is good
│   │   ├── repository/
│   │   │   ├── jpa/              # PostgreSQL repositories
│   │   │   ├── neo4j/            # Neo4j repositories
│   │   │   ├── mongo/            # MongoDB repositories
│   │   │   └── elasticsearch/    # Elasticsearch repositories
│   │   └── dto/                  # Data transfer objects
│   ├── src/main/resources/
│   │   ├── application.yml       # Multi-database config nightmare
│   │   └── db/migration/         # 40+ Flyway migrations (schema changes)
│   └── pom.xml                   # 250+ lines, many unused dependencies
│
├── frontend/                     # React TypeScript frontend
│   ├── src/
│   │   ├── components/           # ✅ Clean, well-organized
│   │   ├── pages/                # ✅ Standard React structure
│   │   └── services/             # ✅ API client
│   └── package.json              # ✅ Minimal dependencies
│
├── python-tools/                 # THE ONE THING THAT WORKS
│   ├── entity_tagger.py          # ✅ Excellent pattern matching
│   ├── entity_reference_builder.py # ✅ Federal Register API integration
│   ├── entity_reference.db       # ✅ SQLite with 469 entities
│   ├── entity_relationships.py   # ⚠️ Unused
│   └── docs/
│       ├── ENTITY_TAGGING_README.md  # ✅ Well-documented
│       └── RELATIONSHIPS_README.md
│
├── docs/                         # Project documentation
│   ├── prd.md                    # Product Requirements Document
│   ├── architecture.md           # Architecture Document
│   └── previousProject/
│       └── ontology-standards-research-report.md  # 🔴 The Ontology That Never Was
│
├── docker-compose.yml            # ❌ 5 databases + Ollama + Elasticsearch
└── README.md                     # ✅ Good documentation
```

### Key Modules and Their Purpose

| Module | Purpose | Status | Notes |
|--------|---------|--------|-------|
| **GovernmentEntityService** | Manage government entities (FDA, CDC, etc.) | ❌ **Problem** | Should be generalized EntityService |
| **PersonService** | Manage people | ❌ **Problem** | Should be same as entities |
| **OrganizationService** | Manage organizations | ❌ **Problem** | Should be same as entities |
| **PythonNlpService** | Java→Python bridge | ❌ **Problem** | Subprocess calls, 500ms latency |
| **EntitySyncService** | Sync PostgreSQL→Neo4j | ⚠️ **Fragile** | Dual-write pattern works, but overkill |
| **ArticleAnalysisService** | Analyze articles with LLM | ✅ **Good** | Claude integration works |
| **ClaimService** | Manage fact-check claims | ✅ **Good** | Core domain logic |

---

## The Data Model Problem (Root Cause of Failure)

### What Was Built: Government-Entity-First Schema

The original schema treats government entities as special:

**PostgreSQL Tables:**
```sql
-- PROBLEM: Specialized table for government entities
CREATE TABLE government_entities (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    abbreviation VARCHAR(100),
    entity_type VARCHAR(50),  -- 'department', 'agency', 'office'
    branch VARCHAR(50),       -- 'executive', 'legislative', 'judicial'
    parent_entity_id UUID REFERENCES government_entities(id),
    description TEXT,
    website_url VARCHAR(500),
    -- PostgreSQL-specific fields
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- PROBLEM: Separate table for persons
CREATE TABLE persons (
    id UUID PRIMARY KEY,
    full_name VARCHAR(500),
    -- Different structure!
);

-- PROBLEM: Separate table for organizations
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(500),
    -- Yet another structure!
);
```

**Neo4j Nodes (Duplicate):**
```cypher
// PROBLEM: Duplicate government entity model in Neo4j
CREATE (e:GovernmentEntity {
    id: "uuid",
    name: "FDA",
    entityType: "agency",
    // Same data, different database
})

// PROBLEM: Separate person nodes
CREATE (p:Person {
    id: "uuid",
    fullName: "John Doe"
})
```

### What Should Have Been Built: Generalized Entity Model

**Unified Entity Table (PostgreSQL):**
```sql
CREATE TABLE entities (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50),  -- 'government_org', 'person', 'organization', 'location'
    name VARCHAR(500) NOT NULL,
    properties JSONB,         -- Flexible properties per type
    source VARCHAR(100),      -- 'federal_register', 'wikidata', 'custom'
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Relationships are separate
CREATE TABLE entity_relationships (
    id UUID PRIMARY KEY,
    source_entity_id UUID REFERENCES entities(id),
    target_entity_id UUID REFERENCES entities(id),
    relationship_type VARCHAR(50),  -- 'parent_of', 'works_for', 'located_in'
    properties JSONB
);
```

**Why This Is Better:**
1. ✅ All entities in one table (government, person, org, location)
2. ✅ JSONB for type-specific properties
3. ✅ Easy to add new entity types (events, claims, etc.)
4. ✅ Relationships are explicit, not buried in foreign keys
5. ✅ Easier to migrate to Schema.org/ontology later

### Migration Path That Was Never Taken

The team researched Schema.org + Prolog (see ontology-standards-research-report.md) but couldn't migrate because:

**Blockers:**
1. Existing code assumed `GovernmentEntity.java` as distinct class
2. 40+ Flyway migrations hardcoded government_entities table
3. Services tightly coupled to specialized models
4. Neo4j sync logic assumed separate node types

**Estimated Effort to Retrofit:** 8-12 weeks (too expensive)
**Decision:** Greenfield rewrite instead

---

## Technical Debt and Known Issues

### Critical Technical Debt

#### 1. **Database Sprawl (5 Databases)**

**Problem:** PostgreSQL + Neo4j + MongoDB + Redis + Elasticsearch

**Why It Happened:**
- PostgreSQL: Primary relational data
- Neo4j: "We need graph relationships for entity hierarchies" (actually, recursive queries in PostgreSQL would work)
- MongoDB: "Articles are documents, need document database" (actually, PostgreSQL JSONB would work)
- Redis: Caching (legitimate use)
- Elasticsearch: "We need full-text search" (actually, PostgreSQL full-text search sufficient for MVP)

**Impact:**
- 5x deployment complexity
- Synchronization bugs between PostgreSQL and Neo4j
- MongoDB rarely used (articles could be in PostgreSQL)
- Elasticsearch empty (Phase 6 never reached)

**Cost:**
- Development time: ~4 weeks wasted
- Operational overhead: ~20 hours/month
- Docker Compose startup: ~2 minutes

**Fix:** Use only PostgreSQL + Redis

#### 2. **Java→Python Subprocess Integration**

**Problem:** PythonNlpService calls Python via ProcessBuilder

**Implementation:**
```java
// PythonNlpService.java
ProcessBuilder processBuilder = new ProcessBuilder(
    pythonExecutable,
    entityTaggerScript,
    "--text-file", tempFile.toAbsolutePath().toString(),
    "--db-path", entityDatabasePath,
    "--output-format", "json"
);

Process process = processBuilder.start();
// Reads JSON from stdout
```

**Issues:**
- Latency: ~500ms per article (unacceptable)
- Brittle: Temp file I/O, JSON parsing errors
- Hard to debug: Cross-process errors
- No connection pooling: Spawns new Python process every time

**Better Approaches:**
1. **HTTP API:** Python FastAPI service
2. **gRPC:** High-performance RPC
3. **Native Java NLP:** Ditch Python entirely

**Verdict:** ❌ Terrible integration pattern

#### 3. **Specialized Entity Models**

**Problem:** `GovernmentEntity.java`, `Person.java`, `Organization.java` are separate classes

**Why It's Bad:**
- Cannot easily add new entity types (events, claims, locations)
- Triple the code for similar functionality
- Makes ontology migration impossible

**Example of Duplication:**
```java
// GovernmentEntityService.java
public GovernmentEntity save(GovernmentEntity entity) {
    // Save to PostgreSQL
    GovernmentEntity saved = governmentEntityRepository.save(entity);
    // Sync to Neo4j
    entitySyncService.syncToNeo4j(saved);
    return saved;
}

// PersonService.java (DUPLICATE LOGIC)
public Person save(Person person) {
    Person saved = personRepository.save(person);
    personSyncService.syncToNeo4j(saved);  // Different sync service!
    return saved;
}
```

**Fix:** Single `EntityService` with polymorphic entity type

#### 4. **PostgreSQL↔Neo4j Dual-Write Sync**

**Pattern:**
```java
@Transactional
public void syncToNeo4j(GovernmentEntity entity) {
    // 1. Save to PostgreSQL (primary)
    governmentEntityRepository.save(entity);

    // 2. Convert to Neo4j node
    GovernmentEntityNode node = convertToNode(entity);

    // 3. Save to Neo4j (secondary)
    neo4jRepository.save(node);

    // PROBLEM: If Neo4j fails, PostgreSQL already committed
}
```

**Issues:**
- Not atomic (PostgreSQL succeeds, Neo4j fails → data inconsistency)
- No rollback mechanism
- Manual sync required after Neo4j downtime

**Why It Exists:**
- Team wanted "best of both worlds" (relational + graph)
- PostgreSQL for transactional integrity
- Neo4j for relationship queries

**Reality:** Neo4j barely used, complex queries (entity hierarchies) could use PostgreSQL recursive CTEs

**Verdict:** ⚠️ Pattern works but unnecessary complexity

#### 5. **Unused Dependencies**

**Stanford CoreNLP** (4.5.1) - 500MB library, never used
**Selenium** - WebDriver for scraping, used in 1 controller
**Elasticsearch** - Deployed but empty
**MongoDB** - 20 articles stored, could be PostgreSQL JSONB

**Impact:** Docker Compose uses 6GB RAM for unused services

---

## Integration Points and External Dependencies

### External Services

| Service | Purpose | Integration Type | Status |
|---------|---------|------------------|--------|
| **Federal Register API** | Download government entities | Python `requests` | ✅ Works |
| **Anthropic Claude API** | Article analysis, claim extraction | Java SDK | ✅ Works |
| **Ollama (Local LLM)** | Local LLM runtime | Docker service | ⚠️ Rarely used |
| **GovInfo.gov API** | Download federal laws | Python | ✅ Works |

### Internal Integration Points

**Java ↔ Python Bridge:**
```
┌──────────────┐         Temp File          ┌─────────────────┐
│ Java Spring  ├──────────────────────────>│ Python          │
│ Boot         │    article_123.txt         │ entity_tagger.py│
│              │                            │                 │
│ PythonNlp    │<──────────────────────────┤                 │
│ Service      │    JSON via stdout         │                 │
└──────────────┘                            └─────────────────┘
```

**Latency:** 500ms (unacceptable)

**PostgreSQL ↔ Neo4j Sync:**
```
┌──────────────┐                            ┌─────────────────┐
│ PostgreSQL   │         Manual Sync        │ Neo4j           │
│ (Primary)    ├───────────────────────────>│ (Secondary)     │
│              │   EntitySyncService        │                 │
└──────────────┘                            └─────────────────┘
```

**Consistency:** ⚠️ Eventually consistent (not atomic)

---

## Development and Deployment

### Local Development Setup

**Prerequisites:**
- Java 17+
- Node.js 16+
- Python 3.11+
- Docker Desktop

**Steps:**
```bash
# 1. Start databases (2 minutes)
docker-compose up -d

# Wait for health checks
docker-compose ps

# 2. Run backend
cd backend
./mvnw spring-boot:run

# 3. Run frontend (optional)
cd frontend
npm install --legacy-peer-deps
npm start
```

**Known Issues:**
- Docker Compose takes 2 minutes to start (5 databases + Ollama + Elasticsearch)
- First backend startup takes 1 minute (Flyway migrations)
- Elasticsearch health check fails randomly (ignore it, unused anyway)

### Build Process

**Backend:**
```bash
cd backend
./mvnw clean package
# Creates: target/news-analyzer-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Creates: build/ directory
```

**Python Tools:**
```bash
cd python-tools
pip install -r requirements.txt
# No build step
```

### Deployment

**Docker Compose (Current):**
- `docker-compose.yml` - 5 databases + backend + frontend
- **Problem:** Not production-ready, no secrets management

**Kubernetes (Planned, Never Implemented):**
- `backend/k8s/` - Empty directory with placeholder files
- Research done, but never deployed

---

## Testing Reality

### Current Test Coverage

**Backend:**
- Unit Tests: ~30% coverage
- Integration Tests: ~10% coverage
- E2E Tests: None

**Frontend:**
- Unit Tests: ~15% coverage
- Integration Tests: None
- E2E Tests: None

**Python Tools:**
- Unit Tests: None
- Integration Tests: Manual verification only

### Running Tests

**Backend:**
```bash
cd backend
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests (Testcontainers)
./mvnw jacoco:report           # Coverage report
```

**Testcontainers Config:**
- Spins up PostgreSQL, MongoDB, Neo4j containers
- Slow: ~2 minutes to run integration tests

**Frontend:**
```bash
cd frontend
npm test
```

### Why Testing Is Inadequate

1. **Complex integration** - 5 databases hard to mock
2. **Python subprocess** - Hard to test Java→Python bridge
3. **Manual testing prioritized** - Small team, fast iteration
4. **Database sync** - PostgreSQL↔Neo4j sync hard to verify

---

## What Went Wrong: Post-Mortem Analysis

### Root Cause: Premature Specialization

**Decision:** Design database schema around government entities (first data source)

**Why It Seemed Right:**
- Federal Register API provided 467 government entities
- "Government entities are core to fact-checking news"
- "Special properties: branch, parent_agency, entity_type"

**Why It Was Wrong:**
- Fact-checking requires ALL entity types (people, orgs, locations, events)
- Government entities are just ONE type, not special
- Led to separate models: `GovernmentEntity`, `Person`, `Organization`
- Made generalization (Schema.org ontology) impossible

### Cascade of Bad Decisions

1. **Government-entity-first schema** → Specialized `government_entities` table
2. **"Persons are different"** → Separate `persons` table
3. **"Organizations are different"** → Separate `organizations` table
4. **"We need graph relationships"** → Add Neo4j
5. **"Articles are documents"** → Add MongoDB
6. **"We need full-text search"** → Add Elasticsearch
7. **"Java can't do NLP"** → Add Python subprocess integration
8. **"We need reasoning logic"** → Research Prolog/ontology (too late!)

**Result:** 5 databases, 3 programming languages, impossible to maintain

### Discovery-Driven Complexity

**Business Requirement Discovered Late:**
> "We need to detect **logical fallacies** and **reasoning errors** in text."

**Implication:** Requires formal logic (Prolog) and ontology (Schema.org)

**Problem:** Discovered AFTER building relational schema

**Retrofit Attempt:**
- Researched Schema.org + Prolog (see ontology-standards-research-report.md)
- Estimated 8 months to retrofit
- Too expensive → Decided on greenfield rewrite

### What Should Have Been Done

**Day 1 Architecture:**
1. ✅ Start with generalized entity model
2. ✅ Use PostgreSQL JSONB for flexibility
3. ✅ Plan for ontology from beginning (Schema.org)
4. ✅ Avoid database sprawl (PostgreSQL + Redis only)
5. ✅ Python as HTTP service, not subprocess

**Lesson Learned:**
> **"Design for the BROADEST set of requirements, not the first data source you implement."**

---

## Salvageable Components for Greenfield Rewrite

### ✅ Keep These Patterns

#### 1. **Modular Monolith Architecture**

The Spring Boot modular monolith was **appropriate** for a small project.

**Structure:**
```
backend/
├── controller/   # REST API layer
├── service/      # Business logic layer
├── repository/   # Data access layer
└── model/        # Domain models
```

**Why It Works:**
- Easy to develop and test
- No microservice overhead
- Can split into microservices later if needed

**Verdict:** ✅ Use modular monolith for greenfield MVP

#### 2. **Python Entity Tagger (The One Thing That Works)**

`entity_tagger.py` is **excellent**:

**Features:**
- SQLite database with 469 government entities
- Pattern-based matching (fast, accurate)
- Federal Register API integration
- Well-documented

**Performance:**
- Processes 100-200 files/minute
- Low memory usage (<100 MB)

**What to Salvage:**
- Keep the SQLite entity database
- Keep the pattern-matching algorithm
- **Change the integration:** HTTP API instead of subprocess

**Greenfield Implementation:**
```python
# FastAPI service
from fastapi import FastAPI

app = FastAPI()

@app.post("/extract-entities")
def extract_entities(text: str):
    tagger = EntityTagger()
    entities = tagger.tag_text(text)
    return {"entities": entities}
```

**Verdict:** ✅ Salvage the algorithm, fix the integration

#### 3. **Dual-Write Coordination Pattern**

The PostgreSQL→Neo4j sync pattern **works** (despite being unnecessary).

**Pattern:**
```java
@Transactional
public Entity save(Entity entity) {
    // 1. Save to primary database
    Entity saved = primaryRepository.save(entity);

    // 2. Sync to secondary database
    secondarySync.sync(saved);

    return saved;
}
```

**Why It Works:**
- Simple, predictable
- Easy to debug
- Can add retry logic

**When to Use in Greenfield:**
- If you REALLY need multiple databases (you probably don't)
- Read replicas for analytics
- Eventual consistency acceptable

**Verdict:** ✅ Pattern is solid, but avoid multiple databases

#### 4. **Flyway Database Migrations**

Flyway is **excellent** for schema versioning.

**Example:**
```sql
-- V1__create_entities_table.sql
CREATE TABLE entities (
    id UUID PRIMARY KEY,
    name VARCHAR(500) NOT NULL
);

-- V2__add_entity_type.sql
ALTER TABLE entities ADD COLUMN entity_type VARCHAR(50);
```

**Benefits:**
- Version-controlled schema changes
- Repeatable migrations
- Rollback support

**Verdict:** ✅ Use Flyway in greenfield

#### 5. **Claude API Integration**

The Anthropic Claude integration **works well**.

**Example:**
```java
@Service
public class ArticleAnalysisService {
    private final ClaudeClient claudeClient;

    public ClaimExtractionResponse extractClaims(String articleText) {
        String prompt = "Extract factual claims from this article:\n" + articleText;
        String response = claudeClient.complete(prompt);
        return parseResponse(response);
    }
}
```

**Verdict:** ✅ Keep Claude API, expand usage

---

### ❌ Discard These Patterns

#### 1. **Government-Entity-First Data Model**

**DO NOT** replicate specialized entity tables.

**Instead:** Unified entity table with JSONB properties

#### 2. **Five-Database Architecture**

**DO NOT** use PostgreSQL + Neo4j + MongoDB + Redis + Elasticsearch.

**Instead:** PostgreSQL + Redis (cache) only

#### 3. **Java→Python Subprocess Integration**

**DO NOT** call Python via `ProcessBuilder`.

**Instead:** Python FastAPI HTTP service

#### 4. **Separate Entity Services**

**DO NOT** create `GovernmentEntityService`, `PersonService`, `OrganizationService`.

**Instead:** Single `EntityService` with polymorphic types

#### 5. **Late-Stage Ontology Retrofit**

**DO NOT** build relational schema first, then try to add ontology.

**Instead:** Design with Schema.org from day 1

---

## Recommendations for Greenfield Rewrite

### Architecture Principles

1. **✅ Generalized Entity Model** - All entities in one table (government, person, org, location, event)
2. **✅ Schema.org from Day 1** - Design for ontology, not against it
3. **✅ Minimal Databases** - PostgreSQL + Redis only
4. **✅ HTTP Services** - Python as FastAPI, not subprocess
5. **✅ Modular Monolith** - Start simple, split later if needed

### Greenfield Data Model

**Unified Entity Table:**
```sql
CREATE TABLE entities (
    id UUID PRIMARY KEY,
    entity_type VARCHAR(50),  -- 'government_org', 'person', 'organization', 'location', 'event'
    name VARCHAR(500) NOT NULL,
    properties JSONB,         -- Type-specific properties
    ontology_class_uri VARCHAR(500),  -- Schema.org class URI
    source VARCHAR(100),      -- 'federal_register', 'wikidata', 'custom'
    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    -- Full-text search
    search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(properties->>'description', '')), 'B')
    ) STORED
);

CREATE INDEX idx_entities_search ON entities USING GIN(search_vector);
CREATE INDEX idx_entities_type ON entities(entity_type);
CREATE INDEX idx_entities_properties ON entities USING GIN(properties);
```

**Entity Relationships:**
```sql
CREATE TABLE entity_relationships (
    id UUID PRIMARY KEY,
    source_entity_id UUID REFERENCES entities(id),
    target_entity_id UUID REFERENCES entities(id),
    relationship_type VARCHAR(50),  -- 'parent_of', 'works_for', 'located_in', 'mentioned_with'
    properties JSONB,
    created_at TIMESTAMP
);

CREATE INDEX idx_relationships_source ON entity_relationships(source_entity_id);
CREATE INDEX idx_relationships_target ON entity_relationships(target_entity_id);
CREATE INDEX idx_relationships_type ON entity_relationships(relationship_type);
```

**Claims (Keep This Model):**
```sql
CREATE TABLE claims (
    id UUID PRIMARY KEY,
    article_id UUID REFERENCES articles(id),
    claim_text TEXT NOT NULL,
    claim_type VARCHAR(50),
    verification_status VARCHAR(50),
    verifiability_status VARCHAR(50),
    extraction_method VARCHAR(50),
    properties JSONB,
    created_at TIMESTAMP
);
```

### Greenfield Service Architecture

**Simplified Service Layer:**
```java
@Service
public class EntityService {

    // Single service for ALL entity types
    public Entity save(Entity entity) {
        // Validate based on entity_type
        validateEntity(entity);

        // Save to PostgreSQL
        Entity saved = entityRepository.save(entity);

        // Cache in Redis
        cacheService.cache(saved);

        return saved;
    }

    public List<Entity> findByType(String entityType) {
        return entityRepository.findByEntityType(entityType);
    }

    public List<Entity> search(String query) {
        // PostgreSQL full-text search
        return entityRepository.fullTextSearch(query);
    }
}
```

### Greenfield Integration Architecture

**Python as FastAPI Service:**
```python
# entity_service.py
from fastapi import FastAPI
from entity_tagger import EntityTagger

app = FastAPI()
tagger = EntityTagger()

@app.post("/api/entities/extract")
def extract_entities(text: str):
    entities = tagger.tag_text(text)
    return {"entities": entities}

@app.get("/api/entities/{entity_id}")
def get_entity(entity_id: int):
    entity = tagger.get_entity(entity_id)
    return entity
```

**Java HTTP Client:**
```java
@Service
public class EntityExtractionService {

    private final WebClient pythonServiceClient;

    public EntityExtractionResult extractEntities(String text) {
        return pythonServiceClient.post()
            .uri("/api/entities/extract")
            .bodyValue(Map.of("text", text))
            .retrieve()
            .bodyToMono(EntityExtractionResult.class)
            .block();
    }
}
```

**Benefits:**
- HTTP protocol (standard, debuggable)
- Connection pooling
- Circuit breaker support
- Latency: ~50ms (10x faster than subprocess)

---

## Appendix - Useful Commands

### Development Commands

**Start databases:**
```bash
docker-compose up -d postgres redis
# Skip Neo4j, MongoDB, Elasticsearch
```

**Run backend:**
```bash
cd backend
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

**Run backend tests:**
```bash
./mvnw test                    # Unit tests
./mvnw verify                  # Integration tests (Testcontainers)
./mvnw jacoco:report           # Coverage report
```

**Run Python entity tagger:**
```bash
cd python-tools
python entity_tagger.py --text-file article.txt --output entities.json
```

**Database migrations:**
```bash
cd backend
./mvnw flyway:migrate          # Apply migrations
./mvnw flyway:info             # Show migration status
```

### Docker Commands

**View logs:**
```bash
docker-compose logs -f postgres
docker-compose logs -f neo4j
```

**Database shells:**
```bash
# PostgreSQL
docker exec -it newsanalyzer-postgres psql -U newsanalyzer -d newsanalyzer

# Neo4j Cypher
docker exec -it newsanalyzer-neo4j cypher-shell -u neo4j -p devpassword

# MongoDB
docker exec -it newsanalyzer-mongodb mongosh -u admin -p devpassword
```

### Debugging

**Enable debug logging:**
```yaml
# application.yml
logging:
  level:
    com.newsanalyzer: DEBUG
    org.springframework.data: DEBUG
```

**JVM debug mode:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

---

## Conclusion

### Summary of Failures

1. ❌ **Government-entity-first data model** → Should be generalized entities
2. ❌ **Database sprawl** (5 databases) → Should be PostgreSQL + Redis only
3. ❌ **Java→Python subprocess** → Should be HTTP API
4. ❌ **Separate entity services** → Should be single EntityService
5. ❌ **Late ontology discovery** → Should design with Schema.org from day 1

### Summary of Successes

1. ✅ **Modular monolith architecture** → Keep for greenfield
2. ✅ **Python entity tagger algorithm** → Salvage, fix integration
3. ✅ **Flyway migrations** → Keep for greenfield
4. ✅ **Claude API integration** → Keep for greenfield
5. ✅ **Dual-write pattern** → Keep pattern (but avoid needing it)

### Key Lessons for Greenfield

> **"Design your data model for the BROADEST set of information sources, not the first source you implement."**

**Before writing any code:**
1. ✅ Identify ALL entity types the system will handle (not just government entities)
2. ✅ Design a generalized entity model (one table, JSONB properties)
3. ✅ Plan for ontology from day 1 (Schema.org)
4. ✅ Minimize databases (PostgreSQL + Redis only)
5. ✅ Use HTTP for inter-service communication (not subprocesses)

**Remember:**
- Government entities are NOT special
- Persons, organizations, locations, events are ALL entities
- Use JSONB for type-specific properties
- Full-text search: PostgreSQL is sufficient
- Graph queries: PostgreSQL recursive CTEs work fine
- Ontology: Design for it from day 1, or don't bother

---

**END OF BROWNFIELD ANALYSIS**

**Next Steps:**
1. Review this document with team
2. Create greenfield architecture design
3. Build MVP with unified entity model
4. Test with diverse entity types (not just government)
5. Add Prolog/ontology when reasoning requirements are clear

---

**Document Status:** ✅ Complete - Ready for Greenfield Architecture Design
**Recommendation:** DO NOT replicate this architecture. Learn from mistakes.
