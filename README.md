# NewsAnalyzer v2

**An independent, open-source platform for news analysis, fact-checking, and bias detection.**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-Documented-green.svg)](docs/architecture.md)
[![API Tests](https://github.com/newsanalyzer-admin/news-analyzer/actions/workflows/api-tests.yml/badge.svg)](https://github.com/newsanalyzer-admin/news-analyzer/actions/workflows/api-tests.yml)
[![Production](https://img.shields.io/badge/Production-Live-brightgreen.svg)](http://newsanalyzer.org)

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
- ✅ RDFLib + OWL-RL reasoner integration
- ✅ Automated entity classification via inference rules
- ✅ Consistency validation with cardinality constraints
- ✅ SPARQL query support for complex relationships
- ✅ API endpoints: /entities/reason, /ontology/stats, /query/sparql
- ✅ Comprehensive unit tests

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

| V1 Mistake | V2 Solution | Improvement |
|------------|-------------|-------------|
| 5 databases (PG, Neo4j, Mongo, Redis, ES) | 2 databases (PG, Redis) | ✅ 60% reduction |
| Government-entity-first model | Unified entity model | ✅ Flexible, extensible |
| Java subprocess → Python (500ms) | HTTP API (50ms) | ✅ 10x faster |
| AWS consideration | Hetzner (Germany) | ✅ Independent |

See [brownfield analysis](docs/newsanalyzer-brownfield-analysis.md) for full details.

---

## ⭐ Star History

If you find this project useful, please star it on GitHub!

---

**Built with ❤️ for transparent, unbiased news analysis**

*Hosted independently in Europe 🇪🇺 • Open Source 🔓 • Community Driven 🤝*
