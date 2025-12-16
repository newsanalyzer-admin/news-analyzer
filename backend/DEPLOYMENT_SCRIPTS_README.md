# Phase 1.6 Deployment Scripts

**Automated deployment verification for Entity-to-GovernmentOrganization linking**

---

## Quick Start

### Option 1: PowerShell (Recommended for Windows)
```powershell
cd backend
.\deploy-and-test-phase1.6.ps1
```

### Option 2: Batch Script
```cmd
cd backend
deploy-and-test-phase1.6.bat
```

---

## What These Scripts Do

### Automated Steps

1. **Stop Backend** - Kills any running Java processes
2. **Start Backend** - Launches backend in new window with dev profile
3. **Wait for Startup** - Polls health endpoint until backend is ready
4. **Verify Health** - Confirms backend is responding
5. **Test Gov Orgs** - Verifies government organizations are available
6. **Test Validation** - Tests new validation endpoints with 4 test cases:
   - DOD → Should link to "Department of Defense"
   - FBI → Should link to "Federal Bureau of Investigation"
   - EPA → Should link to "Environmental Protection Agency"
   - Unknown Agency → Should create but NOT link

### Test Coverage

**What Gets Tested:**
- ✅ Backend health check
- ✅ Government organizations endpoint
- ✅ Entity validation with exact match
- ✅ Entity validation with acronym match
- ✅ Entity validation with no match
- ✅ Entity enrichment (properties, Schema.org data)
- ✅ Verified flag setting
- ✅ Confidence score updating

---

## Expected Output

### Successful Validation Response

```json
{
  "id": "c202c413-6b66-4068-b318-76cbe12a1ba2",
  "entityType": "GOVERNMENT_ORG",
  "name": "Department of Defense",
  "verified": true,
  "confidenceScore": 1.0,
  "governmentOrganizationId": "6892b661-37ea-4d9c-88a7-e9a5929414ec",
  "governmentOrganizationName": "Department of Defense",
  "properties": {
    "acronym": "DOD",
    "website": "https://www.defense.gov",
    "orgType": "department",
    "branch": "executive",
    "jurisdictionAreas": ["national_defense", "military_operations"]
  },
  "schemaOrgType": "GovernmentOrganization",
  "schemaOrgData": {
    "@context": "https://schema.org",
    "@type": "GovernmentOrganization",
    "name": "Department of Defense",
    "url": "https://www.defense.gov"
  }
}
```

**Key Indicators of Success:**
- `governmentOrganizationId` is NOT null (entity is linked)
- `verified` is `true` (entity was validated)
- `confidenceScore` is `1.0` (validation gave full confidence)
- `name` is standardized (e.g., "DOD" → "Department of Defense")
- `properties` are enriched (acronym, website, org type, branch)

### Failed Validation Response (Unknown Entity)

```json
{
  "id": "d303d514-7c77-4179-c429-87dcce13a1c3",
  "entityType": "GOVERNMENT_ORG",
  "name": "Unknown Test Agency",
  "verified": false,
  "confidenceScore": 0.7,
  "governmentOrganizationId": null,
  "governmentOrganizationName": null,
  "properties": {}
}
```

**Key Indicators:**
- `governmentOrganizationId` is `null` (no match found)
- `verified` is `false` (not validated)
- `confidenceScore` unchanged (original value)
- `properties` not enriched

---

## Troubleshooting

### Error: "405 Method Not Allowed"

**Symptom:**
```json
{
  "status": 405,
  "error": "Method Not Allowed",
  "message": "Method 'POST' is not supported.",
  "path": "/api/entities/validate"
}
```

**Cause:** Backend didn't pick up new endpoints

**Fix:**
1. Stop the backend window manually (Ctrl+C)
2. Rebuild: `mvnw clean install`
3. Run script again

### Error: "Backend health check timed out"

**Symptom:** Script waits 30 seconds, then times out

**Cause:** Backend failed to start or taking longer than expected

**Fix:**
1. Check the backend window for errors
2. Verify PostgreSQL is running: `docker ps | findstr postgres`
3. Check database connection in `application-dev.yml`
4. Increase wait time in script (edit `$WaitTime` or `%WAIT_TIME%`)

### Error: "Connection refused"

**Symptom:** `curl` or `Invoke-WebRequest` fails immediately

**Cause:** Backend not running or wrong port

**Fix:**
1. Verify backend is running: `curl http://localhost:8080/actuator/health`
2. Check port 8080 is not in use: `netstat -ano | findstr 8080`
3. Check firewall settings

---

## Database Verification

After running the deployment script, verify database records:

### Quick Check (PowerShell)
```powershell
docker exec newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev -c "SELECT COUNT(*) as linked_entities FROM entities WHERE government_org_id IS NOT NULL;"
```

### Detailed Query
```bash
cd backend
docker exec -i newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev < query-validated-entities.sql
```

### Manual SQL Query
```sql
-- Show validated entities with government org links
SELECT
    e.name AS entity_name,
    e.verified,
    e.confidence_score,
    g.official_name AS gov_org_name,
    g.acronym,
    e.created_at
FROM entities e
INNER JOIN government_organizations g ON e.government_org_id = g.id
ORDER BY e.created_at DESC
LIMIT 10;
```

---

## Files Included

### Deployment Scripts

| File | Type | Description |
|------|------|-------------|
| `deploy-and-test-phase1.6.ps1` | PowerShell | Automated deployment with color output |
| `deploy-and-test-phase1.6.bat` | Batch | Automated deployment for cmd.exe |
| `query-validated-entities.sql` | SQL | Database verification queries |
| `DEPLOYMENT_SCRIPTS_README.md` | Documentation | This file |

### Usage Recommendations

- **Windows 10+:** Use PowerShell script (better error handling, color output)
- **Older Windows:** Use Batch script (cmd.exe compatible)
- **CI/CD:** Use PowerShell script (can parse JSON responses)

---

## Manual Testing (Alternative)

If scripts don't work, test manually:

### Step 1: Start Backend
```bash
cd backend
mvnw spring-boot:run -Dspring.profiles.active=dev
```

### Step 2: Wait for Startup
Wait ~30 seconds, then check health:
```bash
curl http://localhost:8080/actuator/health
```

### Step 3: Test Validation
```bash
curl -X POST http://localhost:8080/api/entities/validate \
  -H "Content-Type: application/json" \
  -d '{
    "entityType": "GOVERNMENT_ORG",
    "name": "DOD",
    "source": "manual_test",
    "confidenceScore": 0.92
  }'
```

### Step 4: Verify Response
Look for:
- `governmentOrganizationId` populated
- `verified: true`
- `name` standardized to "Department of Defense"
- `properties` enriched with acronym, website, etc.

---

## Success Criteria

### ✅ Deployment Successful If:

1. **Health Check Passes**
   - `GET /actuator/health` returns `{"status":"UP"}`

2. **Government Organizations Available**
   - `GET /api/government-organizations` returns 20 organizations

3. **Validation Endpoints Work**
   - `POST /api/entities/validate` returns 201 Created
   - Response includes `governmentOrganizationId` (if match found)

4. **Entity Enrichment Works**
   - Matched entities have `verified: true`
   - Matched entities have `confidenceScore: 1.0`
   - Properties include: acronym, website, orgType, branch

5. **Database Records Correct**
   - `entities.government_org_id` populated for validated entities
   - Foreign key constraint exists
   - Indexes exist

---

## Performance Benchmarks

**Expected Timing:**

| Step | Expected Time |
|------|---------------|
| Backend Startup | 15-30 seconds |
| Health Check | <100ms |
| Entity Validation | 50-150ms |
| Total Script Runtime | ~1-2 minutes |

**If slower:**
- Check database connection latency
- Verify no resource constraints (CPU/Memory)
- Check antivirus/firewall not blocking

---

## Next Steps After Successful Deployment

1. **Integrate with Python Service**
   - Update `reasoning-service/app/api/entities.py`
   - Use `/api/entities/validate` instead of `/api/entities`

2. **Update Frontend**
   - Display `governmentOrganizationName` in entity views
   - Show verified badge for validated entities
   - Link to government org detail page via `governmentOrganizationId`

3. **Backfill Existing Entities**
   - Run validation on entities created before Phase 1.6
   - Use `POST /api/entities/{id}/validate`

4. **Production Deployment**
   - Run migration on production database
   - Deploy updated backend
   - Monitor validation success rate

---

## Support

**For Issues:**
- Check backend logs in the window that opened
- Review database logs: `docker logs newsanalyzer-postgres-dev`
- Consult documentation: `docs/PHASE_1.6_QUICKSTART.md`

**For Questions:**
- Implementation guide: `docs/PHASE_1.6_ENTITY_GOV_ORG_LINKING.md`
- Test results: `docs/PHASE_1.6_TEST_RESULTS.md`
- Architectural assessment: `docs/ARCHITECTURAL_ASSESSMENT_2025-11-23.md`

---

**Created:** 2025-11-24
**Phase:** 1.6 - Entity-to-GovernmentOrganization Linking
**Status:** ✅ Ready for deployment
