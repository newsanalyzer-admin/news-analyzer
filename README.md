# News Analyzer

An open-source platform for analyzing news articles, blogs, and social media posts to detect:
- **Factual accuracy** against authoritative sources
- **Logical fallacies** in reasoning
- **Cognitive biases** in writing

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker and Docker Compose
- Git

### 1. Start Development Environment

First, ensure Docker Desktop is running, then start the database services:

```bash
docker-compose up -d postgres mongodb redis elasticsearch
```

Wait for all services to be healthy (you can check with `docker-compose ps`).

### 2. Run the Application

Use the Maven wrapper to build and run:

```bash
# Build the application
./mvnw clean compile

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 3. Test the API

Once running, you can test the news source endpoints:

```bash
# Get all news sources
curl http://localhost:8080/api/v1/sources

# Get a specific source
curl http://localhost:8080/api/v1/sources/search?name=Reuters

# Check application health
curl http://localhost:8080/api/v1/sources/health
```

## 📊 Services Overview

| Service | Port | Purpose |
|---------|------|---------|
| **News Analyzer API** | 8080 | Main application API |
| **PostgreSQL** | 5432 | Relational data (sources, users, claims) |
| **MongoDB** | 27017 | Document storage (articles, analysis) |
| **Redis** | 6379 | Caching and sessions |
| **Elasticsearch** | 9200 | Search and analytics |
| **Kibana** | 5601 | Elasticsearch visualization |
| **Adminer** | 8080 | Database admin interface |

## 🏗️ Architecture

### Core Components

- **Fact Validation Service** - Cross-references claims with authoritative sources
- **Source Reliability Service** - Tracks historical accuracy of news sources
- **Bias Detection Service** - Analyzes text for emotional manipulation
- **Web Scraping Service** - Automated content collection
- **User Workbench Service** - Personal claim organization and analysis

### Technology Stack

- **Backend**: Spring Boot 3.2, Java 17
- **Databases**: PostgreSQL, MongoDB, Redis, Elasticsearch
- **NLP**: Stanford CoreNLP
- **Web Scraping**: JSoup, Selenium
- **Caching**: Redis with Spring Cache
- **Monitoring**: Micrometer, Prometheus

## 📦 Project Structure

```
news_analyzer/
├── src/main/java/com/newsanalyzer/
│   ├── controller/          # REST API controllers
│   ├── service/            # Business logic services
│   ├── repository/         # Data access layer
│   ├── model/              # JPA entities
│   ├── dto/                # Data transfer objects
│   └── config/             # Spring configuration
├── src/main/resources/
│   └── application.yml     # Application configuration
├── database/
│   ├── init/               # PostgreSQL initialization
│   └── mongo-init/         # MongoDB initialization
├── docs/                   # Documentation
├── docker-compose.yml      # Development environment
└── pom.xml                # Maven dependencies
```

## 🧪 Testing

Run the test suite:

```bash
# Unit tests
./mvnw test

# Integration tests
./mvnw verify

# With coverage report
./mvnw clean test jacoco:report
```

## 🔧 Development

### Adding New Features

1. **Create entity/model** in `src/main/java/com/newsanalyzer/model/`
2. **Add repository** in `src/main/java/com/newsanalyzer/repository/`
3. **Implement service** in `src/main/java/com/newsanalyzer/service/`
4. **Create controller** in `src/main/java/com/newsanalyzer/controller/`
5. **Add tests** in `src/test/java/com/newsanalyzer/`

### Database Changes

1. **Update SQL scripts** in `database/init/`
2. **Restart PostgreSQL container** to apply changes
3. **Update JPA entities** to match schema

### Configuration

Environment-specific settings are in `application.yml`:
- **dev**: Development (default)
- **test**: Testing with in-memory databases
- **prod**: Production with external services

## 📈 Roadmap

### Phase 1: MVP (Months 1-3) ✅
- [x] Basic Spring Boot setup
- [x] Database schema and models
- [x] News source management API
- [ ] Fact validation against government APIs
- [ ] Basic web interface

### Phase 2: Enhanced Analysis (Months 4-6)
- [ ] Bias detection using NLP
- [ ] Cross-source validation
- [ ] Browser extension
- [ ] Performance optimizations

### Phase 3: User Features (Months 7-9)
- [ ] Personal workbench
- [ ] User authentication
- [ ] Report generation
- [ ] Mobile applications

### Phase 4: Scale & Polish (Months 10-12)
- [ ] Kubernetes deployment
- [ ] Advanced monitoring
- [ ] Security hardening
- [ ] Documentation completion

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

- **Documentation**: See `/docs` folder for detailed architecture and API docs
- **Issues**: Report bugs and request features via GitHub Issues
- **Development**: Check the project board for current development status

---

*Built with ❤️ for transparent, unbiased news analysis*