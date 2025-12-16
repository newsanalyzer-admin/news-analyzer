# NewsAnalyzer v2 - Quick Start Guide

Get NewsAnalyzer v2 running in **5 minutes**!

---

## Prerequisites

- Node.js 20+
- Python 3.11 (NOT 3.12)
- Java 17
- PostgreSQL 14+

---

## 1. Database Setup (2 minutes)

```bash
# Create database
psql -U postgres
CREATE DATABASE newsanalyzer;
CREATE USER newsanalyzer WITH PASSWORD 'newsanalyzer';
GRANT ALL PRIVILEGES ON DATABASE newsanalyzer TO newsanalyzer;
\q
```

---

## 2. Start Python Service (2 minutes)

```bash
cd reasoning-service

# Create & activate virtual environment
python3.11 -m venv venv
source venv/bin/activate  # or .\venv\Scripts\activate on Windows

# Install dependencies (takes ~1 minute)
pip install -r requirements.txt

# Download spaCy model
python -m spacy download en_core_web_sm

# Start service
uvicorn app.main:app --reload --port 8000
```

**Leave this running** ✅

---

## 3. Start Java Backend (1 minute)

```bash
# New terminal
cd backend

# Start Spring Boot
./mvnw spring-boot:run  # or .\mvnw.cmd spring-boot:run on Windows
```

**Leave this running** ✅

---

## 4. Start Frontend (1 minute)

```bash
# New terminal
cd frontend

# Install dependencies
npm install

# Create config
cp .env.local.example .env.local

# Start server
npm run dev
```

**Leave this running** ✅

---

## 5. Test It! (30 seconds)

**Open browser:** http://localhost:3000

**Click:** "Try Entity Extraction →"

**Paste this text:**
```
Senator Elizabeth Warren criticized the EPA's new regulations
during a hearing in Washington, D.C.
```

**Click:** "Extract Entities"

**See results:**
- 👤 Elizabeth Warren (Person)
- 🏛️ EPA (Government Organization)
- 📍 Washington, D.C. (Location)

**Expand JSON-LD** to see Schema.org structured data!

---

## That's It! 🎉

**Services Running:**
- Frontend: http://localhost:3000
- Python API: http://localhost:8000
- Java API: http://localhost:8080
- API Docs: http://localhost:8000/docs

---

## Troubleshooting

**Network Error?**
- Check `.env.local` has `NEXT_PUBLIC_REASONING_SERVICE_URL=http://localhost:8000`
- Refresh browser (Ctrl+Shift+R)

**Python errors?**
- Use Python 3.11, not 3.12
- See `reasoning-service/PYTHON_311_SETUP.md`

**Port conflicts?**
- Frontend auto-uses 3001 if 3000 is busy
- Change ports in config files if needed

**More help:**
- See `docs/DEPLOYMENT_GUIDE.md`
- Check service logs in terminals

---

**Happy entity extracting!** 🚀
