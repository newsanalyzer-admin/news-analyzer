# Testing Phase 1.2: Java Entity Model with Schema.org

## Current Status

✅ **Databases Running:**
- PostgreSQL: `newsanalyzer-postgres-dev` (healthy) on port 5432
- Redis: `newsanalyzer-redis-dev` (healthy) on port 6379

✅ **Backend Built:**
- Maven dependencies downloaded
- Project compiled successfully
- JAR ready to run

✅ **Old V1 Containers Removed:**
- Cleaned up all V1 NewsAnalyzer containers
- Jenkins removed (was using port 8080)

---

## How to Start the Backend

### Option 1: Using the Batch File (Easiest)

1. Open a new terminal (PowerShell or CMD)
2. Navigate to backend:
   ```bash
   cd D:\VSCProjects\AIProject2\backend
   ```
3. Run the batch file:
   ```bash
   .\run-dev.bat
   ```

### Option 2: Using Maven Directly

```bash
cd D:\VSCProjects\AIProject2\backend
mvnw.cmd spring-boot:run -Dspring.profiles.active=dev
```

---

## What to Expect

When Spring Boot starts successfully, you'll see:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::  (v3.2.2)

...
Started NewsAnalyzerApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

---

## Testing the API

### 1. Open Swagger UI

Once the backend is running, open your browser to:
```
http://localhost:8080/swagger-ui.html
```

You should see:
- **Title:** NewsAnalyzer API
- **Version:** 2.0.0
- **Entity Controller** with 11 endpoints

### 2. Create a Test Entity (Person)

In Swagger UI:

1. Expand `POST /api/entities`
2. Click "Try it out"
3. Paste this JSON:

```json
{
  "entityType": "PERSON",
  "name": "Elizabeth Warren",
  "properties": {
    "jobTitle": "United States Senator",
    "politicalParty": "Democratic Party",
    "worksFor": "United States Senate",
    "description": "Senior Senator from Massachusetts"
  }
}
```

4. Click "Execute"

**Expected Response (201 Created):**
```json
{
  "id": "uuid-here",
  "entityType": "PERSON",
  "name": "Elizabeth Warren",
  "properties": {
    "jobTitle": "United States Senator",
    "politicalParty": "Democratic Party",
    "worksFor": "United States Senate",
    "description": "Senior Senator from Massachusetts"
  },
  "schemaOrgType": "Person",
  "schemaOrgData": {
    "@context": "https://schema.org",
    "@type": "Person",
    "@id": "https://newsanalyzer.org/entities/uuid-here",
    "name": "Elizabeth Warren",
    "jobTitle": "United States Senator",
    "description": "Senior Senator from Massachusetts",
    "worksFor": {
      "@type": "GovernmentOrganization",
      "name": "United States Senate"
    },
    "memberOf": {
      "@type": "PoliticalParty",
      "name": "Democratic Party"
    }
  },
  "source": null,
  "confidenceScore": 1.0,
  "verified": false,
  "createdAt": "2025-01-20T19:30:00",
  "updatedAt": "2025-01-20T19:30:00"
}
```

**Notice:**
- `schemaOrgType` was auto-generated: "Person"
- `schemaOrgData` was auto-generated with full JSON-LD
- Relationships were mapped (worksFor, memberOf)

### 3. Create a Government Organization

```json
{
  "entityType": "GOVERNMENT_ORG",
  "name": "Environmental Protection Agency",
  "properties": {
    "url": "https://www.epa.gov",
    "description": "Federal agency protecting human health and the environment",
    "parentOrganization": "Executive Branch"
  }
}
```

**Expected schemaOrgType:** "GovernmentOrganization"

### 4. Test Search

1. Expand `GET /api/entities/search`
2. Try it out with `q=warren`
3. Should return the Elizabeth Warren entity

### 5. Test Full-Text Search

1. Expand `GET /api/entities/search/fulltext`
2. Try it out with `q=senator&limit=10`
3. Should return entities matching "senator" in name or description

### 6. Get All Entities

1. Expand `GET /api/entities`
2. Click "Try it out" → "Execute"
3. Should return all created entities

### 7. Filter by Type

1. Expand `GET /api/entities/type/{type}`
2. Try it out with type=`PERSON`
3. Should return only person entities

### 8. Filter by Schema.org Type

1. Expand `GET /api/entities/schema-org-type/{schemaOrgType}`
2. Try it out with schemaOrgType=`Person`
3. Should return the same results as PERSON type

---

## Testing with cURL

If you prefer command-line testing:

### Create Entity
```bash
curl -X POST http://localhost:8080/api/entities \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "PERSON",
    "name": "Joe Biden",
    "properties": {
      "jobTitle": "President of the United States"
    }
  }'
```

### Get All Entities
```bash
curl http://localhost:8080/api/entities
```

### Search
```bash
curl "http://localhost:8080/api/entities/search?q=biden"
```

---

## Verifying Database Changes

You can connect to PostgreSQL and verify the data:

```bash
# Using Docker exec
docker exec -it newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev

# Then run SQL
SELECT id, entity_type, name, schema_org_type FROM entities;
```

You should see your created entities with:
- `entity_type`: Internal type (person, government_org)
- `schema_org_type`: Schema.org type (Person, GovernmentOrganization)
- `schema_org_data`: Full JSON-LD (in JSONB column)

---

## Troubleshooting

### Backend won't start

**Check databases are running:**
```bash
docker ps | findstr newsanalyzer
```

Should show both postgres-dev and redis-dev as healthy.

**If databases aren't running:**
```bash
cd D:\VSCProjects\AIProject2
docker-compose -f docker-compose.dev.yml up -d
```

### Port 8080 already in use

**Find what's using port 8080:**
```bash
netstat -ano | findstr :8080
```

**Kill the process (use the PID from above):**
```bash
taskkill /PID <PID> /F
```

### Database connection error

**Check application-dev.yml:**
```bash
D:\VSCProjects\AIProject2\backend\src\main\resources\application-dev.yml
```

Should have:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/newsanalyzer_dev
    username: newsanalyzer
    password: changeme_dev_password
```

---

## Success Criteria

✅ **Backend starts without errors**
✅ **Swagger UI loads at http://localhost:8080/swagger-ui.html**
✅ **Can create Person entity via POST**
✅ **Schema.org JSON-LD is auto-generated**
✅ **Can retrieve entities via GET**
✅ **Search works (name search and full-text)**
✅ **Filter by entity type works**
✅ **Database contains entities with JSONB Schema.org data**

---

## What This Proves

1. **Dual-layer design works:** Internal `entity_type` + Schema.org `schema_org_type`
2. **Auto-generation works:** Schema.org JSON-LD created automatically
3. **Type-specific mapping works:** Person gets `worksFor`, `memberOf` relationships
4. **JSONB storage works:** PostgreSQL storing flexible JSON data
5. **Repository queries work:** Search, filter, full-text search
6. **REST API works:** All 11 endpoints functional

---

## Next Steps After Successful Testing

Once Phase 1.2 testing passes:

1. **Phase 1.3:** Python entity extraction service
   - NLP entity extraction (spaCy)
   - HTTP endpoint for extraction
   - Integration with Spring Boot backend

2. **Frontend Integration:**
   - Display entities in Next.js
   - Show Schema.org JSON-LD
   - Entity search interface

3. **Extended Testing:**
   - All 6 entity types (PERSON, GOVERNMENT_ORG, ORGANIZATION, LOCATION, EVENT, CONCEPT)
   - Relationship handling
   - Verification workflow
   - Confidence scores
