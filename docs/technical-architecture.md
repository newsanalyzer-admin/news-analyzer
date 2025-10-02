# Technical Architecture Documentation
## News Analysis Platform

**Document Version:** 1.0
**Date:** 2025-09-29
**Architect:** Business Analyst Mary

---

## Executive Summary

This document outlines the technical architecture for an open-source news analysis platform that validates factual claims, detects bias, and identifies logical fallacies in news articles, blogs, and social media posts. The architecture is designed for scalability, performance, and maintains strict open-source requirements to prevent bias introduction.

### Key Design Principles
- **Open-source only** - No proprietary dependencies that could introduce bias
- **Microservices architecture** - Enables independent scaling and deployment
- **Real-time processing** - Sub-5-second analysis for immediate user feedback
- **Data transparency** - Full audit trail of analysis methodology
- **Horizontal scalability** - Support for millions of concurrent users

---

## System Overview

### High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway    │    │  Microservices  │
│                 │    │                  │    │                 │
│ • React Web App │────│ Spring Cloud     │────│ • Fact Validation│
│ • Browser Ext   │    │ Gateway          │    │ • Bias Detection │
│ • Mobile Apps   │    │ • Load Balancing │    │ • Source Scoring │
└─────────────────┘    │ • Rate Limiting  │    │ • Web Scraping   │
                       └──────────────────┘    └─────────────────┘
                                │
                       ┌──────────────────┐
                       │  Event Streaming │
                       │                  │
                       │ Apache Kafka     │
                       │ • Analysis Events│
                       │ • Result Updates │
                       └──────────────────┘
                                │
        ┌─────────────────────────────────────────────────────────────┐
        │                    Data Layer                               │
        │                                                             │
        │ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ │
        │ │ PostgreSQL  │ │ MongoDB     │ │ Elasticsearch│ │ Redis       │ │
        │ │ • Sources   │ │ • Articles  │ │ • Search    │ │ • Cache     │ │
        │ │ • Scores    │ │ • Analysis  │ │ • Analytics │ │ • Sessions  │ │
        │ │ • Users     │ │ • References│ │ • Claims    │ │ • Rate Limit│ │
        │ └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘ │
        └─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Backend Services
- **Framework:** Spring Boot 3.x with Java 21
- **API Gateway:** Spring Cloud Gateway
- **Service Discovery:** Eureka Server
- **Event Streaming:** Apache Kafka
- **Web Scraping:** JSoup + Selenium WebDriver
- **NLP Processing:** Stanford CoreNLP + Apache OpenNLP
- **Machine Learning:** Weka + DL4J (DeepLearning4J)

### Data Storage
- **Relational Data:** PostgreSQL 15
- **Document Storage:** MongoDB 7.x
- **Search Engine:** Elasticsearch 8.x
- **Caching:** Redis 7.x
- **Time-Series Data:** Apache Cassandra

### Infrastructure
- **Containerization:** Docker + Docker Compose
- **Orchestration:** Kubernetes
- **Monitoring:** Prometheus + Grafana
- **Logging:** ELK Stack (Elasticsearch, Logstash, Kibana)
- **CI/CD:** GitHub Actions + ArgoCD

### Frontend
- **Web Application:** React 18 + TypeScript
- **Browser Extensions:** Chrome Extension APIs + Firefox WebExtensions
- **Mobile Apps:** React Native
- **State Management:** Redux Toolkit

---

## Core Microservices

### 1. Fact Validation Service
**Purpose:** Cross-reference claims against authoritative sources

**Key Responsibilities:**
- Extract factual claims from text content
- Query government APIs (Congress.gov, Federal Register, CDC)
- Search academic databases (PubMed, arXiv, CrossRef)
- Generate validation confidence scores

**Technology Stack:**
- Spring Boot with WebFlux (reactive programming)
- Stanford CoreNLP for claim extraction
- RestTemplate/WebClient for external API calls
- Redis for caching validation results

**API Endpoints:**
```
POST /api/v1/validation/validate
GET  /api/v1/validation/claim/{claimId}
POST /api/v1/validation/batch
```

### 2. Source Reliability Service
**Purpose:** Track and score news source accuracy over time

**Key Responsibilities:**
- Maintain historical accuracy records
- Calculate dynamic reliability scores
- Track political bias indicators
- Identify source conflicts of interest

**Scoring Algorithm:**
```
Reliability Score = (Accurate Claims / Total Claims) * Recency Weight * Volume Weight
Where:
- Recency Weight: More recent accuracy weighted higher
- Volume Weight: Sources with more claims get higher confidence
```

**API Endpoints:**
```
GET  /api/v1/sources/{sourceId}/score
POST /api/v1/sources/{sourceId}/accuracy-record
GET  /api/v1/sources/search?domain={domain}
```

### 3. Bias Detection Service
**Purpose:** Analyze text for emotional manipulation and subjective language

**Key Responsibilities:**
- Detect loaded language and emotional triggers
- Identify opinion presented as fact
- Flag missing context or selective reporting
- Generate bias confidence scores

**Analysis Categories:**
- **Emotional Language:** Highly charged words, superlatives
- **Opinion as Fact:** Subjective statements without attribution
- **Loaded Terms:** Words with strong positive/negative connotations
- **Missing Context:** Selective fact presentation

**API Endpoints:**
```
POST /api/v1/bias/analyze
GET  /api/v1/bias/patterns/{sourceId}
POST /api/v1/bias/batch-analyze
```

### 4. Web Scraping Service
**Purpose:** Automated content collection from news sources

**Key Responsibilities:**
- Scrape articles from major news sites
- Extract structured data (title, content, author, date)
- Handle dynamic content (JavaScript rendering)
- Respect robots.txt and rate limiting

**Technology Implementation:**
```java
@Component
public class NewsScrapingService {

    @Autowired
    private WebDriver webDriver;

    @Async
    public CompletableFuture<ScrapedArticle> scrapeArticle(String url) {
        try {
            webDriver.get(url);
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));

            // Wait for content to load
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.tagName("article")));

            return CompletableFuture.completedFuture(
                extractArticleContent(webDriver.getPageSource()));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}
```

### 5. User Workbench Service
**Purpose:** Personal knowledge management and claim visualization

**Key Responsibilities:**
- Store user's personal claim collections
- Manage visual relationship mapping
- Support collaborative workbenches
- Export analysis reports

**Features:**
- Drag-and-drop claim organization
- Visual connection mapping (supports, contradicts, related)
- Collaborative sharing with permission controls
- Export to various formats (PDF, JSON, CSV)

---

## Data Architecture

### PostgreSQL Schema

**Core Tables:**
```sql
-- News sources and reliability tracking
CREATE TABLE news_sources (
    source_id UUID PRIMARY KEY,
    source_name VARCHAR(255) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    reliability_score DECIMAL(3,2) CHECK (reliability_score >= 0 AND reliability_score <= 1),
    political_bias_score DECIMAL(3,2) CHECK (political_bias_score >= -1 AND political_bias_score <= 1),
    founded_date DATE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Historical accuracy tracking
CREATE TABLE accuracy_records (
    record_id UUID PRIMARY KEY,
    source_id UUID REFERENCES news_sources(source_id),
    claim_text TEXT NOT NULL,
    verification_result VARCHAR(50) CHECK (verification_result IN
        ('accurate', 'inaccurate', 'misleading', 'unverifiable', 'partially_accurate')),
    verification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    authoritative_source_url TEXT,
    confidence_score DECIMAL(3,2) CHECK (confidence_score >= 0 AND confidence_score <= 1),
    verified_by VARCHAR(255), -- system, expert, crowd
    verification_notes TEXT
);

-- User management
CREATE TABLE users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- User workbenches for personal analysis
CREATE TABLE user_workbenches (
    workbench_id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(user_id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Individual claims within workbenches
CREATE TABLE workbench_claims (
    claim_id UUID PRIMARY KEY,
    workbench_id UUID REFERENCES user_workbenches(workbench_id),
    claim_text TEXT NOT NULL,
    verification_status VARCHAR(50),
    source_urls TEXT[],
    user_notes TEXT,
    position_x INTEGER DEFAULT 0,
    position_y INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Relationships between claims
CREATE TABLE claim_connections (
    connection_id UUID PRIMARY KEY,
    from_claim_id UUID REFERENCES workbench_claims(claim_id),
    to_claim_id UUID REFERENCES workbench_claims(claim_id),
    relationship_type VARCHAR(50) CHECK (relationship_type IN
        ('supports', 'contradicts', 'related', 'questions', 'elaborates')),
    strength DECIMAL(2,1) CHECK (strength >= 0 AND strength <= 1),
    user_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Performance Optimizations:**
```sql
-- Indexes for common queries
CREATE INDEX idx_sources_domain ON news_sources(domain);
CREATE INDEX idx_sources_reliability ON news_sources(reliability_score DESC);
CREATE INDEX idx_accuracy_source_date ON accuracy_records(source_id, verification_date DESC);
CREATE INDEX idx_accuracy_result ON accuracy_records(verification_result);
CREATE INDEX idx_claims_workbench ON workbench_claims(workbench_id);
CREATE INDEX idx_claims_verification ON workbench_claims(verification_status);

-- Composite indexes for complex queries
CREATE INDEX idx_accuracy_source_result_date ON accuracy_records(source_id, verification_result, verification_date DESC);

-- Partial indexes for performance
CREATE INDEX idx_public_workbenches ON user_workbenches(created_at DESC) WHERE is_public = TRUE;
```

### MongoDB Collections

**Article Analysis Results:**
```javascript
// Collection: article_analyses
{
  "_id": ObjectId("..."),
  "articleUrl": "https://example.com/article",
  "title": "Article Title",
  "content": "Full article content...",
  "sourceId": "uuid-of-source",
  "author": "Author Name",
  "publishedDate": ISODate("2024-01-15T10:30:00Z"),
  "analyzedAt": ISODate("2024-01-15T11:00:00Z"),
  "contentHash": "sha256-hash-of-content",

  // Analysis results
  "claimValidations": [
    {
      "claimText": "The unemployment rate is 3.5%",
      "validationResult": "accurate",
      "confidenceScore": 0.95,
      "authoritativeSource": "https://bls.gov/data",
      "lastVerified": ISODate("2024-01-15T11:00:00Z")
    }
  ],

  "biasAnalysis": {
    "overallBiasScore": 0.3,
    "biasIndicators": [
      {
        "type": "emotional_language",
        "text": "devastating impact",
        "severity": "moderate",
        "position": 245
      }
    ],
    "languagePatterns": {
      "emotionalWords": 5,
      "factualStatements": 12,
      "opinions": 3
    }
  },

  "sourceReliability": {
    "score": 0.82,
    "historicalAccuracy": 0.79,
    "biasScore": 0.15,
    "lastUpdated": ISODate("2024-01-15T06:00:00Z")
  },

  "logicalFallacies": [
    {
      "type": "ad_hominem",
      "text": "Critics, who are clearly biased...",
      "confidence": 0.75,
      "position": 156
    }
  ],

  "processingMetadata": {
    "processingTimeMs": 2340,
    "version": "1.0",
    "modelVersions": {
      "biasDetection": "v2.1",
      "factValidation": "v1.5"
    }
  }
}
```

**External Reference Sources:**
```javascript
// Collection: external_references
{
  "_id": ObjectId("..."),
  "sourceType": "government", // government, academic, legal, fact_checker
  "name": "Bureau of Labor Statistics",
  "apiEndpoint": "https://api.bls.gov/publicAPI/v2/timeseries/data/",
  "description": "Official US employment statistics",
  "accessMethod": "api", // api, scraping, manual
  "reliability": 0.98,
  "updateFrequency": "monthly",
  "lastUpdated": ISODate("2024-01-15T00:00:00Z"),
  "metadata": {
    "apiKey": false,
    "rateLimit": "25 requests per day",
    "dataTypes": ["employment", "wages", "inflation"]
  }
}
```

### Elasticsearch Indexes

**Claims Search Index:**
```json
{
  "mappings": {
    "properties": {
      "claimText": {
        "type": "text",
        "analyzer": "standard",
        "fields": {
          "keyword": {
            "type": "keyword"
          }
        }
      },
      "verificationStatus": {
        "type": "keyword"
      },
      "sourceId": {
        "type": "keyword"
      },
      "firstSeen": {
        "type": "date"
      },
      "lastVerified": {
        "type": "date"
      },
      "sources": {
        "type": "nested",
        "properties": {
          "url": {
            "type": "keyword"
          },
          "reliability": {
            "type": "float"
          }
        }
      },
      "claimVector": {
        "type": "dense_vector",
        "dims": 768
      }
    }
  }
}
```

---

## External Integrations

### Government APIs

**1. Congress.gov API**
```java
@Component
public class CongressApiClient {

    private static final String BASE_URL = "https://api.congress.gov/v3";

    @Value("${congress.api.key}")
    private String apiKey;

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public CongressionalRecord getBillDetails(String congress, String billType, String billNumber) {
        String url = String.format("%s/bill/%s/%s/%s", BASE_URL, congress, billType, billNumber);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-Key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(url, HttpMethod.GET, entity, CongressionalRecord.class).getBody();
    }

    public List<Vote> getVotingRecord(String memberId, String congress) {
        // Implementation for member voting records
    }
}
```

**2. Federal Register API**
```java
@Component
public class FederalRegisterClient {

    private static final String BASE_URL = "https://www.federalregister.gov/api/v1";

    public FederalRegisterDocument getDocument(String documentId) {
        String url = String.format("%s/documents/%s.json", BASE_URL, documentId);
        return restTemplate.getForObject(url, FederalRegisterDocument.class);
    }

    public List<FederalRegisterDocument> searchDocuments(String query, LocalDate fromDate, LocalDate toDate) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(BASE_URL + "/documents.json")
            .queryParam("conditions[term]", query)
            .queryParam("conditions[publication_date][gte]", fromDate.toString())
            .queryParam("conditions[publication_date][lte]", toDate.toString());

        FederalRegisterResponse response = restTemplate.getForObject(
            builder.toUriString(), FederalRegisterResponse.class);

        return response != null ? response.getResults() : Collections.emptyList();
    }
}
```

### Academic Sources

**3. PubMed Integration**
```java
@Component
public class PubMedClient {

    private static final String ESEARCH_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi";
    private static final String EFETCH_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi";

    public PubMedSearchResult searchArticles(String query, int maxResults) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(ESEARCH_URL)
            .queryParam("db", "pubmed")
            .queryParam("term", query)
            .queryParam("retmax", maxResults)
            .queryParam("retmode", "json");

        return restTemplate.getForObject(builder.toUriString(), PubMedSearchResult.class);
    }

    public PubMedArticle getArticleDetails(String pmid) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(EFETCH_URL)
            .queryParam("db", "pubmed")
            .queryParam("id", pmid)
            .queryParam("retmode", "xml");

        String xmlResponse = restTemplate.getForObject(builder.toUriString(), String.class);
        return parseXmlToArticle(xmlResponse);
    }
}
```

**4. arXiv API Integration**
```java
@Component
public class ArxivClient {

    private static final String BASE_URL = "http://export.arxiv.org/api/query";

    public ArxivSearchResult searchPreprints(String query, int maxResults) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(BASE_URL)
            .queryParam("search_query", query)
            .queryParam("max_results", maxResults)
            .queryParam("sortBy", "submittedDate")
            .queryParam("sortOrder", "descending");

        String atomResponse = restTemplate.getForObject(builder.toUriString(), String.class);
        return parseAtomFeed(atomResponse);
    }
}
```

---

## Performance & Scalability

### Caching Strategy

**Multi-Level Caching:**
```java
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager primaryCacheManager() {
        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(getCacheConfiguration())
            .withCacheConfiguration("source-reliability",
                getCacheConfiguration().entryTtl(Duration.ofHours(1)))
            .withCacheConfiguration("claim-validation",
                getCacheConfiguration().entryTtl(Duration.ofHours(24)))
            .withCacheConfiguration("bias-patterns",
                getCacheConfiguration().entryTtl(Duration.ofHours(6)))
            .build();
    }

    @Bean
    public CacheManager localCacheManager() {
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .recordStats());
        return caffeineCacheManager;
    }
}
```

**Cache Performance Optimization:**
```java
@Service
public class CacheOptimizedAnalysisService {

    @Cacheable(value = "quick-analysis", key = "#contentHash", cacheManager = "localCacheManager")
    public QuickAnalysis getCachedQuickAnalysis(String contentHash) {
        // L1: Local in-memory cache (30 minutes)
        return null; // Cache miss, proceed to L2
    }

    @Cacheable(value = "quick-analysis", key = "#contentHash", cacheManager = "primaryCacheManager")
    public QuickAnalysis getQuickAnalysis(String contentHash, String content) {
        // L2: Redis distributed cache (1 hour)
        return performQuickAnalysis(content);
    }

    @CacheEvict(value = "source-reliability", key = "#sourceId")
    public void invalidateSourceReliability(String sourceId) {
        // Invalidate cache when source data updates
    }
}
```

### Database Performance

**Connection Pooling:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      connection-timeout: 20000

  data:
    mongodb:
      uri: mongodb://user:password@mongo1:27017,mongo2:27017,mongo3:27017/newsanalyzer?replicaSet=rs0

  redis:
    lettuce:
      pool:
        max-active: 20
        max-idle: 8
        min-idle: 2
```

**Query Optimization:**
```sql
-- Explain plans for critical queries
EXPLAIN (ANALYZE, BUFFERS)
SELECT s.source_name, AVG(ar.confidence_score) as avg_confidence
FROM news_sources s
JOIN accuracy_records ar ON s.source_id = ar.source_id
WHERE ar.verification_date >= NOW() - INTERVAL '30 days'
GROUP BY s.source_id, s.source_name
ORDER BY avg_confidence DESC;

-- Materialized views for complex analytics
CREATE MATERIALIZED VIEW daily_source_performance AS
SELECT
    source_id,
    DATE(verification_date) as report_date,
    COUNT(*) as total_claims,
    COUNT(CASE WHEN verification_result = 'accurate' THEN 1 END) as accurate_claims,
    AVG(confidence_score) as avg_confidence
FROM accuracy_records
GROUP BY source_id, DATE(verification_date);

-- Refresh schedule
SELECT cron.schedule('refresh-daily-performance', '0 1 * * *',
    'REFRESH MATERIALIZED VIEW daily_source_performance;');
```

### Horizontal Scaling

**Kubernetes Deployment:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fact-validation-service
  labels:
    app: fact-validation
    version: v1
spec:
  replicas: 5
  selector:
    matchLabels:
      app: fact-validation
      version: v1
  template:
    metadata:
      labels:
        app: fact-validation
        version: v1
    spec:
      containers:
      - name: fact-validation
        image: news-analyzer/fact-validation:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
---
apiVersion: v1
kind: Service
metadata:
  name: fact-validation-service
spec:
  selector:
    app: fact-validation
  ports:
  - port: 8080
    targetPort: 8080
  type: ClusterIP
---
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: fact-validation-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: fact-validation-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

---

## Security Architecture

### Authentication & Authorization

**OAuth 2.0 + JWT Implementation:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/v1/analysis/**").authenticated()
                .requestMatchers("/api/v1/workbench/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt()
                .jwtDecoder(jwtDecoder());

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://auth.newsanalyzer.org/.well-known/jwks.json").build();
    }
}
```

### Rate Limiting

**Distributed Rate Limiting:**
```java
@Component
public class DistributedRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean isAllowed(String clientId, int requestsPerMinute) {
        String key = "rate_limit:" + clientId + ":" + getCurrentMinute();

        String currentCount = redisTemplate.opsForValue().get(key);

        if (currentCount == null) {
            redisTemplate.opsForValue().set(key, "1", Duration.ofMinutes(1));
            return true;
        }

        int count = Integer.parseInt(currentCount);
        if (count < requestsPerMinute) {
            redisTemplate.opsForValue().increment(key);
            return true;
        }

        return false;
    }

    private String getCurrentMinute() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }
}
```

---

## Monitoring & Observability

### Metrics Collection

**Custom Metrics:**
```java
@Component
public class AnalysisMetrics {

    private final Counter analysisRequestsTotal;
    private final Timer analysisProcessingTime;
    private final Gauge activeAnalysisRequests;

    public AnalysisMetrics(MeterRegistry meterRegistry) {
        this.analysisRequestsTotal = Counter.builder("analysis_requests_total")
            .description("Total number of analysis requests")
            .tag("service", "fact-validation")
            .register(meterRegistry);

        this.analysisProcessingTime = Timer.builder("analysis_processing_duration")
            .description("Time taken to process analysis requests")
            .register(meterRegistry);

        this.activeAnalysisRequests = Gauge.builder("analysis_requests_active")
            .description("Currently active analysis requests")
            .register(meterRegistry);
    }

    public void recordAnalysisRequest(String analysisType) {
        analysisRequestsTotal.increment(Tags.of("type", analysisType));
    }

    public Timer.Sample startTimer() {
        return Timer.start(analysisProcessingTime);
    }
}
```

### Logging Strategy

**Structured Logging:**
```java
@Component
public class AnalysisLogger {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisLogger.class);

    public void logAnalysisStart(String requestId, String contentType, String sourceUrl) {
        logger.info("Analysis started",
            kv("requestId", requestId),
            kv("contentType", contentType),
            kv("sourceUrl", sourceUrl),
            kv("timestamp", Instant.now())
        );
    }

    public void logAnalysisComplete(String requestId, AnalysisResult result, Duration processingTime) {
        logger.info("Analysis completed",
            kv("requestId", requestId),
            kv("factValidationScore", result.getFactValidation().getScore()),
            kv("biasScore", result.getBiasAnalysis().getScore()),
            kv("processingTimeMs", processingTime.toMillis())
        );
    }
}
```

---

## Deployment Strategy

### Development Environment

**Docker Compose Setup:**
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: newsanalyzer
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: devpass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  mongodb:
    image: mongo:7
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    ports:
      - "9092:9092"

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    ports:
      - "2181:2181"

volumes:
  postgres_data:
  mongodb_data:
  elasticsearch_data:
```

### Production Deployment

**Kubernetes Manifests:**
```yaml
# Namespace
apiVersion: v1
kind: Namespace
metadata:
  name: news-analyzer

---
# ConfigMap for application properties
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: news-analyzer
data:
  application.yml: |
    spring:
      profiles:
        active: production
      datasource:
        url: jdbc:postgresql://postgres-service:5432/newsanalyzer
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
      data:
        mongodb:
          uri: mongodb://mongodb-service:27017/newsanalyzer
      redis:
        host: redis-service
        port: 6379
      kafka:
        bootstrap-servers: kafka-service:9092

---
# Secrets
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: news-analyzer
type: Opaque
data:
  db-username: <base64-encoded-username>
  db-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-jwt-secret>
```

---

## Implementation Roadmap

### Phase 1: MVP (Months 1-3)
**Core Infrastructure:**
- Basic Spring Boot microservices setup
- PostgreSQL database with core schema
- Simple fact validation against government APIs
- Basic source reliability scoring
- Web API for article analysis

**Deliverables:**
- Fact validation service with Congress.gov integration
- Source reliability service with basic scoring
- Simple web interface for testing
- Docker development environment

### Phase 2: Enhanced Analysis (Months 4-6)
**Advanced Features:**
- Bias detection using NLP
- MongoDB integration for article storage
- Elasticsearch for claim search
- Redis caching implementation
- Browser extension development

**Deliverables:**
- Complete bias detection system
- Enhanced fact validation with academic sources
- Browser extension for Chrome/Firefox
- Performance optimizations

### Phase 3: User Features (Months 7-9)
**User-Focused Development:**
- Personal workbench implementation
- User authentication and authorization
- Collaborative features
- Report generation and export
- Mobile application development

**Deliverables:**
- Full user workbench with visual mapping
- User accounts and permissions
- Mobile apps for iOS/Android
- Export capabilities

### Phase 4: Scale & Polish (Months 10-12)
**Production Readiness:**
- Kubernetes deployment
- Monitoring and logging
- Performance testing and optimization
- Security hardening
- Documentation completion

**Deliverables:**
- Production-ready Kubernetes deployment
- Comprehensive monitoring
- Load testing results
- Security audit completion
- User documentation

---

## Technology Justifications

### Why Java/Spring Boot?
- **Mature Ecosystem:** Extensive libraries for NLP, web scraping, and API integration
- **Performance:** JVM optimizations for high-throughput processing
- **Enterprise Features:** Built-in security, monitoring, and scaling capabilities
- **Open Source:** All components are open-source, preventing bias introduction
- **Developer Familiarity:** Aligns with your Java background

### Why PostgreSQL?
- **ACID Compliance:** Critical for maintaining data integrity in fact-checking
- **JSON Support:** Flexible schema for evolving analysis requirements
- **Performance:** Excellent query optimization for complex analytics
- **Extensions:** PostGIS for geospatial data, pg_cron for scheduled tasks

### Why MongoDB?
- **Document Storage:** Natural fit for varied article content and analysis results
- **Flexible Schema:** Easy to evolve analysis result structures
- **Aggregation Framework:** Powerful analytics for pattern recognition
- **Horizontal Scaling:** Sharding support for large-scale deployments

### Why Elasticsearch?
- **Full-Text Search:** Essential for finding similar claims across articles
- **Analytics:** Real-time analytics for bias pattern detection
- **Scalability:** Distributed search across millions of articles
- **Machine Learning:** Built-in ML capabilities for anomaly detection

### Why Redis?
- **Performance:** Sub-millisecond response times for cached data
- **Data Structures:** Rich data types for complex caching scenarios
- **Distributed Locking:** Coordination across microservices
- **Session Management:** Fast user session storage

---

## Conclusion

This technical architecture provides a solid foundation for building a scalable, performant news analysis platform while maintaining strict open-source requirements. The microservices approach enables independent development and scaling of different analysis components, while the chosen technology stack provides the necessary tools for complex NLP processing and large-scale data management.

The architecture is designed to grow from a simple MVP to a comprehensive platform capable of serving millions of users while maintaining the transparency and neutrality essential for a trusted fact-checking system.

**Next Steps:**
1. Set up development environment using provided Docker Compose configuration
2. Implement core fact validation service with government API integration
3. Design and implement database schema in PostgreSQL
4. Begin development of basic web interface for testing
5. Plan and execute first sprint of MVP development

---

*Architecture designed using enterprise-grade open-source technologies to ensure scalability, maintainability, and bias-free operation.*