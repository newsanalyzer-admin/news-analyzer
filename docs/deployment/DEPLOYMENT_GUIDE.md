# NewsAnalyzer v2 - Deployment Guide

**Version:** 2.0.0
**Date:** 2025-11-21
**Status:** Phase 1 Complete

---

## Overview

NewsAnalyzer v2 is a multi-service application with three main components:

1. **Frontend** (Next.js) - Port 3000/3001
2. **Python Reasoning Service** (FastAPI) - Port 8000
3. **Java Backend** (Spring Boot) - Port 8080
4. **PostgreSQL Database** - Port 5432

---

## Prerequisites

### Required Software

**Node.js & npm:**
- Version: 20.x or higher
- Download: https://nodejs.org/

**Python:**
- Version: 3.11 (recommended) or 3.10
- ⚠️ **NOT Python 3.12** (pydantic v1 compatibility issues)
- Download: https://www.python.org/downloads/

**Java:**
- Version: JDK 17
- Download: https://adoptium.net/

**PostgreSQL:**
- Version: 14 or higher
- Download: https://www.postgresql.org/download/

**Maven:**
- Included in project (`mvnw` wrapper)

---

## Quick Start

### 1. Clone Repository

```bash
git clone <repository-url>
cd AIProject2
```

### 2. Database Setup

**Create Database:**
```sql
CREATE DATABASE newsanalyzer;
CREATE USER newsanalyzer WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE newsanalyzer TO newsanalyzer;
```

**Configure Connection:**
```bash
# backend/src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/newsanalyzer
    username: newsanalyzer
    password: your_password
```

### 3. Start Services

**Terminal 1 - Python Reasoning Service:**
```bash
cd reasoning-service

# Create virtual environment (Python 3.11)
python -m venv venv
source venv/bin/activate  # Linux/Mac
# or
.\venv\Scripts\activate   # Windows

# Install dependencies
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm

# Start service
uvicorn app.main:app --reload --port 8000
```

**Terminal 2 - Java Backend:**
```bash
cd backend

# Start Spring Boot (includes database migration)
./mvnw spring-boot:run     # Linux/Mac
.\mvnw.cmd spring-boot:run  # Windows
```

**Terminal 3 - Frontend:**
```bash
cd frontend

# Install dependencies
npm install

# Configure environment
cp .env.local.example .env.local
# Edit .env.local if needed

# Start development server
npm run dev
```

### 4. Access Application

- **Frontend:** http://localhost:3000 (or 3001 if 3000 is in use)
- **Entity Extraction:** http://localhost:3000/entities
- **Backend API:** http://localhost:8080
- **Reasoning Service:** http://localhost:8000
- **API Docs:** http://localhost:8000/docs

---

## Detailed Setup

### Python Reasoning Service

**Step 1: Create Virtual Environment**

```bash
cd reasoning-service

# Use Python 3.11
python3.11 -m venv venv

# Activate
source venv/bin/activate  # Linux/Mac
.\venv\Scripts\activate   # Windows

# Verify Python version
python --version  # Should be 3.11.x
```

**Step 2: Install Dependencies**

```bash
pip install --upgrade pip
pip install -r requirements.txt
```

**Dependencies Include:**
- FastAPI 0.109.0
- Uvicorn 0.27.0
- spaCy 3.8+
- Transformers 4.38.0
- PyTorch 2.2.2
- Pydantic 2.5.3

**Step 3: Download spaCy Model**

```bash
python -m spacy download en_core_web_sm
```

**Step 4: Verify Installation**

```bash
python -c "import spacy, fastapi; print('OK')"
```

**Step 5: Start Service**

```bash
# Development with auto-reload
uvicorn app.main:app --reload --port 8000

# Production
uvicorn app.main:app --host 0.0.0.0 --port 8000 --workers 4
```

**Health Check:**
```bash
curl http://localhost:8000
# {"service":"NewsAnalyzer Reasoning Service","version":"2.0.0","status":"healthy"}
```

---

### Java Backend

**Step 1: Configure Database**

Create `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/newsanalyzer
    username: newsanalyzer
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

**Step 2: Build Application**

```bash
cd backend

# Clean and build
./mvnw clean install

# Skip tests if needed
./mvnw clean install -DskipTests
```

**Step 3: Run Tests**

```bash
./mvnw test
```

**Expected:** 61/65 tests passing (4 H2/PostgreSQL compatibility issues)

**Step 4: Start Application**

```bash
# Development mode
./mvnw spring-boot:run

# With specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**Step 5: Verify**

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}

curl http://localhost:8080/api/entities
# []
```

---

### Frontend

**Step 1: Install Dependencies**

```bash
cd frontend
npm install
```

**Step 2: Configure Environment**

```bash
cp .env.local.example .env.local
```

**Edit `.env.local`:**
```bash
# Python Reasoning Service (FastAPI)
NEXT_PUBLIC_REASONING_SERVICE_URL=http://localhost:8000

# Java Backend (Spring Boot)
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

**Step 3: Start Development Server**

```bash
npm run dev
```

**Step 4: Build for Production**

```bash
npm run build
npm start
```

**Step 5: Verify**

- Navigate to http://localhost:3000
- Should see "NewsAnalyzer v2" homepage
- Click "Try Entity Extraction →"
- Test entity extraction

---

## Configuration

### Environment Variables

**Frontend (`.env.local`):**
```bash
NEXT_PUBLIC_REASONING_SERVICE_URL=http://localhost:8000
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

**Backend (`application.yml`):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/newsanalyzer
    username: newsanalyzer
    password: ${DB_PASSWORD:newsanalyzer}

server:
  port: 8080

cors:
  allowed-origins: http://localhost:3000,http://localhost:3001
```

**Python Service:**
```python
# app/main.py - Line 64
uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
```

---

## Troubleshooting

### Python Service Issues

**Issue: Module not found errors**
```bash
# Verify virtual environment is activated
which python  # Should show venv path

# Reinstall dependencies
pip install -r requirements.txt --force-reinstall
```

**Issue: spaCy model not found**
```bash
# Download model
python -m spacy download en_core_web_sm

# Verify installation
python -c "import spacy; nlp = spacy.load('en_core_web_sm'); print('OK')"
```

**Issue: Port 8000 already in use**
```bash
# Find process
netstat -ano | findstr :8000  # Windows
lsof -i :8000                 # Linux/Mac

# Kill process or use different port
uvicorn app.main:app --reload --port 8001
```

**Issue: Python 3.12 compatibility**
```
TypeError: ForwardRef._evaluate() missing 1 required keyword-only argument
```

**Solution:** Use Python 3.11 (see `reasoning-service/PYTHON_311_SETUP.md`)

---

### Frontend Issues

**Issue: Network Error / Can't connect to backend**

Check service URLs in `.env.local`:
```bash
# Correct ports:
NEXT_PUBLIC_REASONING_SERVICE_URL=http://localhost:8000  # NOT 8001
NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
```

**Issue: Port 3000 in use**

Next.js will automatically use 3001. Update any hardcoded URLs.

**Issue: Module not found**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Issue: Build errors**
```bash
# Clear Next.js cache
rm -rf .next
npm run dev
```

---

### Backend Issues

**Issue: Database connection failed**

Check PostgreSQL is running:
```bash
# Windows
net start postgresql

# Linux/Mac
sudo systemctl start postgresql
```

Verify credentials in `application.yml`

**Issue: Port 8080 in use**
```bash
# Find process
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Change port in application.yml
server:
  port: 8081
```

**Issue: Flyway migration errors**

```bash
# Drop and recreate database
psql -U postgres
DROP DATABASE newsanalyzer;
CREATE DATABASE newsanalyzer;
```

---

## Testing

### End-to-End Test

**1. Start all services** (Frontend, Backend, Python)

**2. Navigate to:** http://localhost:3000/entities

**3. Enter test text:**
```
Senator Elizabeth Warren criticized the EPA's new regulations
during a hearing in Washington, D.C. The Democratic Party
leader called for stronger environmental protections.
```

**4. Click "Extract Entities"**

**5. Verify results:**
- ✅ 4-5 entities extracted
- ✅ Elizabeth Warren (Person) with 👤 icon
- ✅ EPA (Government Organization) with 🏛️ icon
- ✅ Washington, D.C. (Location) with 📍 icon
- ✅ Democratic Party (Political Party) with 🎭 icon
- ✅ Each entity shows confidence score
- ✅ Schema.org JSON-LD expandable

**6. Test filtering:**
- Click entity type badges
- Verify filtering works

**7. Test confidence threshold:**
- Adjust slider
- Click "Extract Entities" again
- Verify results change

---

## Production Deployment

### Frontend (Vercel/Netlify)

**Build:**
```bash
cd frontend
npm run build
```

**Environment Variables:**
```
NEXT_PUBLIC_REASONING_SERVICE_URL=https://reasoning-api.yourdomain.com
NEXT_PUBLIC_BACKEND_URL=https://api.yourdomain.com
```

**Deploy:**
- Vercel: `vercel --prod`
- Netlify: `netlify deploy --prod`

---

### Python Service (Docker)

**Dockerfile:**
```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
RUN python -m spacy download en_core_web_sm

COPY app/ ./app/

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
```

**Build and Run:**
```bash
docker build -t newsanalyzer-reasoning .
docker run -p 8000:8000 newsanalyzer-reasoning
```

---

### Java Backend (Docker)

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw package -DskipTests

CMD ["java", "-jar", "target/newsanalyzer-backend-2.0.0-SNAPSHOT.jar"]
```

---

### PostgreSQL (Docker Compose)

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: newsanalyzer
      POSTGRES_USER: newsanalyzer
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/newsanalyzer
      SPRING_DATASOURCE_USERNAME: newsanalyzer
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}

  reasoning:
    build: ./reasoning-service
    ports:
      - "8000:8000"

  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    environment:
      NEXT_PUBLIC_REASONING_SERVICE_URL: http://reasoning:8000
      NEXT_PUBLIC_BACKEND_URL: http://backend:8080

volumes:
  postgres_data:
```

**Start:**
```bash
docker-compose up -d
```

---

## Monitoring

### Health Checks

**Python Service:**
```bash
curl http://localhost:8000/health
```

**Java Backend:**
```bash
curl http://localhost:8080/actuator/health
```

**Frontend:**
```bash
curl http://localhost:3000
```

### Logs

**Python:**
```bash
# Development
tail -f logs/reasoning-service.log

# Docker
docker logs -f newsanalyzer-reasoning
```

**Java:**
```bash
# Development
tail -f backend/logs/application.log

# Docker
docker logs -f newsanalyzer-backend
```

**Frontend:**
```bash
# Next.js logs in terminal
# Browser console for client errors
```

---

## Performance

### Expected Response Times

- **Entity Extraction:** 100-500ms (depends on text length)
- **Backend CRUD:** 10-50ms
- **Frontend Page Load:** < 1s

### Optimization

**Python Service:**
- Use `--workers 4` for production
- Enable caching for repeated extractions
- Consider GPU for large batches

**Java Backend:**
- Enable connection pooling
- Add Redis cache for frequent queries
- Use database indexing

**Frontend:**
- Enable SSR for better SEO
- Use CDN for static assets
- Implement lazy loading

---

## Security

### Production Checklist

**Environment Variables:**
- [ ] No hardcoded passwords
- [ ] Use environment variables
- [ ] Rotate secrets regularly

**CORS:**
- [ ] Restrict allowed origins
- [ ] No `allow_origins=["*"]` in production

**Database:**
- [ ] Use strong passwords
- [ ] Enable SSL connections
- [ ] Regular backups

**API:**
- [ ] Add authentication (JWT)
- [ ] Rate limiting
- [ ] Input validation

---

## Backup & Recovery

### Database Backup

```bash
# Backup
pg_dump -U newsanalyzer newsanalyzer > backup.sql

# Restore
psql -U newsanalyzer newsanalyzer < backup.sql
```

### Application State

- Frontend: Stateless (no backup needed)
- Backend: Database only
- Python: Stateless (models can be re-downloaded)

---

## Support

### Documentation

- **Architecture:** `docs/architecture/`
- **API Docs:** http://localhost:8000/docs
- **Schema.org Guide:** `docs/schema-org-owl-integration.md`
- **Phase Docs:** `docs/PHASE_*.md`

### Troubleshooting

- Check service logs
- Verify all ports are correct
- Ensure databases are running
- Review environment variables

---

## Version History

**v2.0.0 - Phase 1 Complete (2025-11-21)**
- ✅ Database schema with Schema.org support
- ✅ Java backend with Entity CRUD
- ✅ Python entity extraction service
- ✅ Frontend with entity visualization
- ✅ Full Schema.org JSON-LD integration

**Next Release: v2.1.0 - Phase 2**
- Entity library and storage
- External entity linking
- Advanced Schema.org features

---

**For issues or questions, refer to project documentation or create an issue in the repository.**
