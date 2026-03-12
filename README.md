# NewsAnalyzer v2

**An independent, open-source platform for news analysis, fact-checking, and bias detection.**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-Documented-green.svg)](docs/architecture.md)
[![API Tests](https://github.com/newsanalyzer-admin/news-analyzer/actions/workflows/api-tests.yml/badge.svg)](https://github.com/newsanalyzer-admin/news-analyzer/actions/workflows/api-tests.yml)
[![Production](https://img.shields.io/badge/Production-Live-brightgreen.svg)](http://newsanalyzer.org)

---

## About This Project

NewsAnalyzer is both a genuine civic technology project and a 
demonstration of quality engineering practices across a polyglot 
AI application stack.

The engineering decisions documented here — structured data modeling 
for bias mitigation, OWL-based semantic reasoning, modular monolith 
architecture, production observability — reflect real tradeoffs made 
for real reasons, documented in the 
[brownfield analysis](docs/newsanalyzer-brownfield-analysis.md).

Built by [Steve Kosuth-Wood](https://www.linkedin.com/in/steve-kosuth-wood-532a909/)

---

## 🌍 Mission: Transparency & Independence

NewsAnalyzer v2 is built with **transparency** and **independence** as core principles:

- **🇪🇺 European Hosting:** Hetzner Cloud (Germany) - no US tech giants
- **🔓 Open Source:** 100% open-source stack (PostgreSQL, Redis, Nginx, Docker)
- **📖 Public Code:** Auditable on GitHub, mirrored to Codeberg (European non-profit)
- **🛡️ Data Sovereignty:** All data hosted in EU, GDPR-compliant

For a news analysis platform, independence from major tech companies ensures unbiased operation and protects against conflicts of interest.

---


## 🎯 What It Does

NewsAnalyzer extracts and analyzes entities from news articles with Schema.org support:

- **👤 Entity Extraction** - People, organizations, locations, events from text (Phase 1 ✅)
- **🏛️ Schema.org Integration** - Full JSON-LD structured data for all entities (Phase 1 ✅)
- **📊 Smart Classification** - 9 entity types with automatic government detection (Phase 1 ✅)
- **🎨 Interactive Visualization** - Real-time entity display with filtering (Phase 1 ✅)
- **🧠 OWL Reasoning** - Semantic inference and entity classification (Phase 3 ✅)
- **🔗 External Linking** - Wikidata, DBpedia integration (Phase 2 - Coming Soon)

---


## Why Structured Data — The Bias Problem

Most AI fact-checking approaches make the same architectural mistake: 
ingest reference documents as vectors and let the LLM reason over 
retrieved text chunks.

The problem: LLMs don't neutrally extract facts from documents. They 
pattern-match on text, inheriting whatever framing and implicit bias 
exists in how source material was written. An AI reasoning over a 
government document written by a policy supporter will "see" that 
policy differently than if the same facts had been written by a critic.

NewsAnalyzer addresses this by forcing all reference data through 
explicit structured data models before the AI layer touches it. A 
senator's voting record isn't stored as a document — it's structured 
objects with defined fields. The AI works with facts, not text.

This design decision came directly from applying experimental psychology 
methodology to AI system design: controlled measurement requires 
isolating variables. Unstructured text is an uncontrolled variable.

The tradeoff: every new data source requires explicit data modeling. 
There are no shortcuts that maintain evaluation integrity. This is a 
deliberate choice of reliability over development velocity.

---

## Data Source Hierarchy

NewsAnalyzer distinguishes between two categories of reference data, 
and treats them differently in analysis:

### Tier 1: Official Sources (Factual Ground Truth)
Primary government sources used as the authoritative basis for 
factual accuracy verdicts. When NewsAnalyzer flags a claim as 
inaccurate, it is because an official source directly contradicts it.

| Source | Data | Authority |
|--------|------|-----------|
| congress.gov | Bills, votes, legislative activity | U.S. Congress |
| house.gov / senate.gov | Member records, committees | U.S. Legislature |
| govinfo.gov | Authenticated federal documents | GPO |
| BLS | Employment, inflation, wage data | Dept. of Labor |
| BEA | GDP, economic indicators | Dept. of Commerce |
| FEC | Campaign finance records | Fed. Election Commission |


### Tier 2: Enrichment Sources (Planned — Phase 2)
Designed but not yet implemented. Will provide entity enrichment 
and contextual relationships — useful for understanding connections 
between entities, but explicitly not used as the basis for factual 
accuracy verdicts.

When implemented, analysis outputs drawing on Tier 2 sources will 
be flagged differently from Tier 1-grounded verdicts, preserving 
the distinction between authoritative fact and contextual inference.

| Source | Data | Limitation |
|--------|------|------------|
| Wikidata | Entity relationships, identifiers | Community-maintained |
| DBpedia | Structured Wikipedia data | Derivative of Wikipedia |

### Why This Matters

A system that treats Wikidata and an official congressional 
voting record as equivalent reference sources will produce 
analysis that sounds authoritative but isn't. The accuracy 
of a factual verdict is only as good as the authority of 
its source.

This hierarchy is enforced architecturally — Tier 1 and Tier 2 
data are designed to flow through separate pathways so the analysis 
layer always knows the authority level of any fact before 
constructing a verdict. The Tier 2 pathway is planned for Phase 2.

---

## 🏗️ Architecture Overview

**Greenfield rewrite** that learns from V1's mistakes (see [brownfield analysis](docs/newsanalyzer-brownfield-analysis.md)):

- **Frontend:** Next.js 14 + TypeScript + Tailwind CSS
- **Backend:** Spring Boot 3.2 (Java 17) REST API
- **Reasoning Service:** Python FastAPI + SWI-Prolog
- **Databases:** PostgreSQL + Redis (only 2 databases - V1 had 5!)
- **Infrastructure:** Docker Compose on Hetzner Cloud
- **CI/CD:** GitHub Actions

**Key Innovation:** Unified entity model (all entities in one table with JSONB) - fixes V1's government-entity-first mistake.

See full [Architecture Document](docs/architecture.md) for details.

---

## 📁 Repository Structure

```
newsanalyzer-v2/
├── backend/              # Spring Boot Java backend (REST API)
├── frontend/             # Next.js TypeScript frontend
├── reasoning-service/    # Python FastAPI (entity extraction, Prolog)
├── docs/                 # Architecture & documentation
│   ├── architecture.md                      # Complete architecture
│   └── newsanalyzer-brownfield-analysis.md  # V1 failure analysis
├── nginx/                # Nginx reverse proxy config
├── .github/workflows/    # CI/CD pipelines
├── docker-compose.yml    # Local development
└── docker-compose.prod.yml  # Production deployment
```

---

## 🚀 Quick Start

> **Live Demo:** [newsanalyzer.org](http://newsanalyzer.org) — 
> running on Hetzner Cloud, Germany
- Note: Live Demo may be behind current code boase.

### Prerequisites

- **Java 17+** (OpenJDK or Temurin)
- **Node.js 20+** and pnpm
- **Python 3.11+**
- **Docker Desktop**
- **Git**

### 1. Clone Repository

```bash
git clone https://github.com/newsanalyzer-admin/news-analyzer.git
cd news-analyzer
```

### 2. Start Databases

```bash
docker-compose -f docker-compose.dev.yml up -d postgres redis
```

### 3. Run Backend

```bash
cd backend
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
./mvnw clean install
./mvnw flyway:migrate  # Run database migrations
./mvnw spring-boot:run -Dspring.profiles.active=dev
# Backend runs on http://localhost:8080
```

### 4. Run Frontend

```bash
cd frontend
cp .env.local.example .env.local
pnpm install
pnpm dev
# Frontend runs on http://localhost:3000
```

### 5. Run Python Service

```bash
cd reasoning-service
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
# Python service runs on http://localhost:8000
```

---

## 🧪 Testing

```bash
# Backend tests
cd backend
./mvnw test                 # Unit tests
./mvnw verify               # Integration tests
./mvnw jacoco:report        # Coverage report

# Frontend tests
cd frontend
pnpm test                   # Unit tests (Vitest)
pnpm test:e2e               # E2E tests (Playwright)

# Python tests
cd reasoning-service
pytest
pytest --cov=app tests/     # With coverage
```

---

## 📚 Documentation

- **[Architecture Document](docs/architecture.md)** - Complete fullstack architecture
- **[Brownfield Analysis](docs/newsanalyzer-brownfield-analysis.md)** - V1 failure analysis and lessons learned
- **[API Documentation](http://localhost:8080/swagger-ui.html)** - OpenAPI/Swagger UI (when backend running)

---

## 🔐 Security & Privacy

- **HTTPS Only:** Let's Encrypt SSL certificates in production
- **JWT Authentication:** Stateless, secure token-based auth
- **Rate Limiting:** Nginx rate limiting (10 req/s API, 5 req/m login)
- **Security Headers:** HSTS, X-Frame-Options, CSP, XSS protection
- **GDPR Compliant:** EU hosting, data sovereignty

---

## 🌐 Source Control & Production

- **Primary Repository:** [GitHub](https://github.com/newsanalyzer-admin/news-analyzer)
- **Production Site:** [newsanalyzer.org](http://newsanalyzer.org)
- **Production Docs:** [docs/deployment/PRODUCTION_ENVIRONMENT.md](docs/deployment/PRODUCTION_ENVIRONMENT.md)

Production is hosted on Hetzner Cloud (Germany) for data sovereignty and independence.

---

## 🤝 Contributing

We welcome contributions! Here's how:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

**Development Guidelines:**
- Follow existing code style and patterns
- Add tests for new features
- Update documentation
- Ensure all tests pass before submitting PR

---

## 📊 Project Status

> **Live Demo:** [newsanalyzer.org](http://newsanalyzer.org) — 
> running on Hetzner Cloud, Germany

**Development is sequenced by source reliability:** Tier 1 official 
sources (Phase 1, Phase 3) before Tier 2 enrichment sources (Phase 2) 
— ensuring the factual foundation is solid before adding contextual 
enrichment.

**Current Phase:** Phase 3 Complete - OWL Reasoning ✅

### ✅ Phase 1: Schema.org Foundation (COMPLETE)
- ✅ PostgreSQL schema with JSONB support
- ✅ Java backend with Entity CRUD (61/65 tests passing)
- ✅ Python entity extraction service (spaCy + Schema.org)
- ✅ Frontend with entity visualization
- ✅ Full Schema.org JSON-LD integration
- ✅ 9 entity types supported
- ✅ Interactive UI with type filtering

### ✅ Phase 3: OWL Reasoning (COMPLETE)
- ✅ Custom NewsAnalyzer ontology (7 classes, 10 properties)
- ✅ SWI-Prolog integration for formal logical inference
- ✅ Automated entity classification via ontology-based inference rules
  — entities classified by what can be *inferred* about them, not just 
  what is explicitly stated
- ✅ Consistency validation with cardinality constraints
- ✅ SPARQL query support for complex entity relationships

### 🚧 Phase 2: Schema.org Enrichment (NEXT)
- Entity library and persistence
- External entity linking (Wikidata, DBpedia)
- Property expansion and enrichment
- Entity relationships
- Export functionality

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Open source for transparency and community benefit.

---

## 🙋 Support & Contact

- **Documentation:** [docs/](docs/)
- **Issues:** [GitHub Issues](https://github.com/newsanalyzer-admin/news-analyzer/issues)
- **Discussions:** [GitHub Discussions](https://github.com/newsanalyzer-admin/news-analyzer/discussions)

---

## 🔗 Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Frontend** | Next.js + TypeScript | 14.x |
| **Backend** | Spring Boot (Java) | 3.2.x |
| **Python Service** | FastAPI + SWI-Prolog | 0.109+ |
| **Database** | PostgreSQL | 15+ |
| **Cache** | Redis | 7+ |
| **Hosting** | Hetzner Cloud (Germany) | - |
| **CI/CD** | GitHub Actions | - |

---

## 🎓 Learning from V1

NewsAnalyzer v2 is a **greenfield rewrite** that fixes critical V1 mistakes:
## Learning From V1: The Brownfield Analysis

NewsAnalyzer v2 is a complete greenfield rewrite of a failed 
first attempt. Rather than quietly discarding v1, the failure 
was systematically documented in a 
[full brownfield analysis](docs/analysis/newsanalyzer-brownfield-analysis.md )
that drove every significant architectural decision in v2.

The core lesson:

> *"Design your data model for the BROADEST set of information 
> sources from day one, not the first source you implement."*

The fatal flaw in v1 was treating government entities as 
architecturally special — building separate database schemas, 
service layers, and models for each entity type — rather than 
recognizing that a senator, a corporation, and a geographic 
location are all just *entities with different properties*. 
This decision made the system increasingly rigid as requirements 
evolved, eventually making the ontology and reasoning 
requirements impossible to retrofit without a complete rewrite.

| V1 Mistake | Root Cause | V2 Solution |
|---|---|---|
| 5 databases (PG, Neo4j, Mongo, Redis, ES) | Premature optimization for use cases that never materialized | PostgreSQL + Redis only |
| Separate models per entity type | Government-entity-first thinking | Unified JSONB entity model |
| Java → Python via subprocess (500ms) | Integration designed as afterthought | HTTP API (50ms, 10x faster) |
| Late ontology discovery | Requirements discovered after architecture locked | Schema.org designed in from day one |
| Unstructured document ingestion | Standard RAG approach | Structured data models — prevents LLM bias contamination |

The last row is the one not present in most architectural 
post-mortems: discovering that the standard approach to AI 
knowledge bases produces evaluations contaminated by source 
document framing — and that structured data modeling is the 
correct mitigation.


---

## ⭐ Star History

If you find this project useful, please star it on GitHub!

---

**Built with ❤️ for transparent, unbiased news analysis**

*Hosted independently in Europe 🇪🇺 • Open Source 🔓 • Community Driven 🤝*
