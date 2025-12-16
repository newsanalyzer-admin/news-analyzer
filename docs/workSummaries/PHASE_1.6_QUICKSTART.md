# Phase 1.6 Quick Start Guide

**Entity-to-GovernmentOrganization Linking**

**Date:** 2025-11-23
**Status:** ✅ Ready to Deploy

---

## What Was Added?

**Master Data Management pattern** - Extracted entities (like "EPA") are now automatically validated against authoritative government organization records and enriched with official data.

---

## Quick Deployment

### Step 1: Run Database Migration

```bash
cd backend
./mvnw flyway:migrate
```

**Expected Output:**
```
[INFO] Successfully applied 1 migration to schema "public"
[INFO] Schema version: 4
```

**What It Does:**
- Adds `government_org_id` column to `entities` table
- Creates foreign key to `government_organizations` table
- Adds indexes for query performance

### Step 2: Rebuild Backend

```bash
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Tests run: 65, Failures: 0, Errors: 0, Skipped: 0
```

### Step 3: Restart Backend

```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Step 4: Test New Endpoints

**Test validation endpoint:**
```bash
curl -X POST http://localhost:8080/api/entities/validate \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "GOVERNMENT_ORG",
    "name": "EPA",
    "source": "test",
    "confidenceScore": 0.9
  }'
```

**Expected Response:**
```json
{
  "id": "...",
  "entityType": "GOVERNMENT_ORG",
  "name": "Environmental Protection Agency",
  "verified": true,
  "confidenceScore": 1.0,
  "governmentOrganizationId": "...",
  "governmentOrganizationName": "Environmental Protection Agency",
  "properties": {
    "acronym": "EPA",
    "website": "https://www.epa.gov",
    "orgType": "independent_agency",
    "branch": "executive"
  }
}
```

---

## New API Endpoints

### 1. Create and Validate Entity

```
POST /api/entities/validate
```

**Use:** Create entity with automatic gov org validation and enrichment

**Request Body:**
```json
{
  "entityType": "GOVERNMENT_ORG",
  "name": "EPA",
  "source": "article:123",
  "confidenceScore": 0.92
}
```

**Response:** Enriched entity (if validation successful) or plain entity (if no match)

### 2. Validate Existing Entity

```
POST /api/entities/{id}/validate
```

**Use:** Validate entity created before Phase 1.6 or retry failed validation

**Response:** Enriched entity (if validation successful)

---

## What Gets Enriched?

When an entity like "EPA" is validated:

**Before:**
```json
{
  "name": "EPA",
  "verified": false,
  "properties": {}
}
```

**After:**
```json
{
  "name": "Environmental Protection Agency",  // Standardized
  "verified": true,                           // Marked as verified
  "governmentOrganizationId": "...",          // Linked to master data
  "properties": {
    "acronym": "EPA",
    "website": "https://www.epa.gov",
    "orgType": "independent_agency",
    "branch": "executive",
    "jurisdictionAreas": [...],
    "missionStatement": "..."
  }
}
```

---

## Integration with Article Processing

### Update Python Extraction Pipeline

**Before:**
```python
# reasoning-service/app/api/entities.py
response = requests.post(f"{JAVA_BACKEND}/api/entities", json=entity_data)
```

**After:**
```python
# Use validation endpoint for government orgs
endpoint = "/api/entities/validate" if entity_data["entity_type"] == "government_org" else "/api/entities"
response = requests.post(f"{JAVA_BACKEND}{endpoint}", json=entity_data)
```

**Benefit:** Automatic enrichment during article processing

---

## Backfilling Existing Entities

If you have entities created before Phase 1.6:

```bash
# Get all unvalidated government org entities
curl http://localhost:8080/api/entities/type/GOVERNMENT_ORG | jq -r '.[] | select(.verified == false) | .id'

# Validate each one
for id in $(curl -s http://localhost:8080/api/entities/type/GOVERNMENT_ORG | jq -r '.[] | select(.verified == false) | .id'); do
  curl -X POST http://localhost:8080/api/entities/$id/validate
done
```

---

## Verification

### Check Database

```sql
-- See entities linked to government orgs
SELECT
    e.id,
    e.name AS entity_name,
    e.verified,
    g.official_name AS gov_org_name
FROM entities e
LEFT JOIN government_organizations g ON e.government_org_id = g.id
WHERE e.entity_type = 'GOVERNMENT_ORG';
```

### Check API

```bash
# Get all validated entities
curl "http://localhost:8080/api/entities/type/GOVERNMENT_ORG" | jq '.[] | select(.verified == true)'
```

---

## Troubleshooting

### Migration Fails

**Error:** `Column "government_org_id" already exists`

**Solution:**
```bash
# Check current schema version
./mvnw flyway:info

# If V4 already applied, skip migration
```

### Compilation Fails

**Error:** `Cannot find symbol: class GovernmentOrganizationService`

**Solution:**
```bash
# Clean and rebuild
./mvnw clean compile
```

### Validation Returns No Match

**Reason:** Government organization not in `government_organizations` table

**Solution:**
1. Check available orgs: `GET /api/government-orgs`
2. Add missing org: `POST /api/government-orgs`
3. Retry validation: `POST /api/entities/{id}/validate`

---

## Next Steps

1. ✅ Deploy to dev environment
2. 🎯 Add unit tests for validation workflow
3. 🎯 Update frontend to display government org links
4. 🎯 Integrate validation into Python extraction pipeline
5. 🎯 Create admin UI for manual entity validation
6. 🎯 Start Phase 2 (Wikidata/DBpedia linking)

---

## Documentation

- **Full Implementation Guide:** `docs/PHASE_1.6_ENTITY_GOV_ORG_LINKING.md`
- **Architecture Design:** `docs/architecture/entity-vs-government-org-design.md`
- **API Documentation:** http://localhost:8080/swagger-ui.html

---

## Summary

**What Changed:**
- Database: Added `government_org_id` FK column
- Model: Added `GovernmentOrganization` relationship
- Service: Added validation and enrichment workflow
- API: Added 2 new validation endpoints

**What It Does:**
- Validates extracted entities against official government org records
- Enriches entities with authoritative data (website, mission, etc.)
- Standardizes entity names ("EPA" → "Environmental Protection Agency")
- Links entities to canonical master data for deduplication

**Impact:**
- Better data quality (verified entities with rich metadata)
- Improved analytics (count mentions of official org, not text variants)
- Foundation for Phase 2 (external entity linking)

---

**Status:** ✅ Ready for Production
**Deployment Time:** ~5 minutes (migration + restart)
**Breaking Changes:** None (backward compatible)
