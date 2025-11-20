# NewsAnalyzer v2

**An independent, open-source platform for news analysis, fact-checking, and bias detection.**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-Documented-green.svg)](docs/architecture.md)

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

NewsAnalyzer analyzes news articles, blog posts, and social media to detect:

- **✅ Factual Accuracy** - Cross-reference claims against authoritative sources
- **🧠 Logical Fallacies** - Identify errors in reasoning using Prolog
- **🎭 Cognitive Biases** - Detect emotional manipulation and framing
- **📊 Source Reliability** - Track historical accuracy of news outlets

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
git clone https://github.com/yourusername/newsanalyzer-v2.git
cd newsanalyzer-v2
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

## 🌐 Source Control & Mirrors

- **Primary Repository:** [GitHub](https://github.com/yourusername/newsanalyzer-v2) (you are here)
- **Mirror Repository:** [Codeberg](https://codeberg.org/yourusername/newsanalyzer-v2) (European non-profit)

All commits are automatically mirrored to Codeberg for transparency and independence. Clone from either source - they are identical.

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

**Current Phase:** Project Scaffolding ✅

- ✅ Architecture document completed
- ✅ V1 brownfield analysis completed
- ✅ Git repository initialized
- ✅ Backend scaffolding (Spring Boot)
- ✅ Frontend scaffolding (Next.js)
- ✅ Python service scaffolding (FastAPI)
- ✅ Docker Compose configurations
- ✅ Nginx reverse proxy configuration
- ✅ GitHub Actions CI/CD pipelines
- ⏳ Implementation (next)

---

## 📝 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Open source for transparency and community benefit.

---

## 🙋 Support & Contact

- **Documentation:** [docs/](docs/)
- **Issues:** [GitHub Issues](https://github.com/yourusername/newsanalyzer-v2/issues)
- **Discussions:** [GitHub Discussions](https://github.com/yourusername/newsanalyzer-v2/discussions)

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
