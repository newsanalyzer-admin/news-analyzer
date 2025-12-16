# Phase 1.6: Automated Deployment Scripts

**Created:** 2025-11-24
**Status:** ✅ Ready to Use
**Purpose:** Automate deployment verification for Entity-to-GovernmentOrganization linking

---

## 🚀 Quick Start

### Run Automated Deployment Test

**PowerShell (Recommended):**
```powershell
cd backend
.\deploy-and-test-phase1.6.ps1
```

**Batch (cmd.exe):**
```cmd
cd backend
deploy-and-test-phase1.6.bat
```

**That's it!** The script will:
1. Stop any running backend
2. Start fresh backend in new window
3. Wait for startup (with progress indicator)
4. Test health endpoint
5. Test government organizations endpoint
6. **Test all 4 new validation scenarios**
7. Display results with success/failure indicators

---

## 📦 What Was Created

### Automated Deployment Scripts

| File | Lines | Purpose |
|------|-------|---------|
| `deploy-and-test-phase1.6.ps1` | 250+ | PowerShell automation with color output |
| `deploy-and-test-phase1.6.bat` | 150+ | Batch script for cmd.exe |
| `query-validated-entities.sql` | 100+ | Database verification queries |
| `DEPLOYMENT_SCRIPTS_README.md` | 400+ | Comprehensive usage guide |

**Total:** ~900 lines of deployment automation

---

## 🎯 What Gets Tested

### Test 1: DOD Validation ✅
```json
POST /api/entities/validate
Body: {"entityType":"GOVERNMENT_ORG","name":"DOD","confidenceScore":0.92}
```
**Expected:** Links to "Department of Defense", verified=true

### Test 2: FBI Validation ✅
```json
POST /api/entities/validate
Body: {"entityType":"GOVERNMENT_ORG","name":"FBI","confidenceScore":0.88}
```
**Expected:** Links to "Federal Bureau of Investigation", verified=true

### Test 3: EPA Validation ✅
```json
POST /api/entities/validate
Body: {"entityType":"GOVERNMENT_ORG","name":"EPA","confidenceScore":0.95}
```
**Expected:** Links to "Environmental Protection Agency", verified=true

### Test 4: Unknown Agency ✅
```json
POST /api/entities/validate
Body: {"entityType":"GOVERNMENT_ORG","name":"Unknown Test Agency","confidenceScore":0.7}
```
**Expected:** Creates entity but does NOT link, verified=false

---

## ✅ Success Indicators

### In API Response

Look for these in the JSON output:

```json
{
  "governmentOrganizationId": "6892b661-...",     // ← Entity is linked!
  "governmentOrganizationName": "Department of Defense",  // ← Name enriched!
  "name": "Department of Defense",                // ← Name standardized!
  "verified": true,                               // ← Entity validated!
  "confidenceScore": 1.0,                        // ← Confidence updated!
  "properties": {
    "acronym": "DOD",                            // ← Properties enriched!
    "website": "https://www.defense.gov",
    "orgType": "department",
    "branch": "executive"
  }
}
```

**If you see all of these → Phase 1.6 is FULLY DEPLOYED! 🎉**

### In PowerShell Output

The PowerShell script will show color-coded results:

- 🟢 **[SUCCESS]** Entity linked to government organization!
- 🟡 **[INFO]** Entity created but not linked (expected for unknown)
- 🔴 **[ERROR]** 405 Method Not Allowed - Endpoint not found!

---

## 🔧 Troubleshooting

### Issue: "405 Method Not Allowed"

**Symptom:** All validation tests return 405 error

**Solution:**
```bash
# Stop backend manually (Ctrl+C in backend window)
cd backend
mvnw clean install
# Run script again
.\deploy-and-test-phase1.6.ps1
```

### Issue: "Backend health check timed out"

**Symptom:** Script waits 30 seconds, backend doesn't respond

**Solution:**
1. Check backend window for startup errors
2. Verify PostgreSQL running: `docker ps | findstr postgres`
3. Check port 8080 not in use: `netstat -ano | findstr 8080`

---

## 📊 Database Verification

After successful API tests, verify database records:

### Quick Check
```bash
docker exec newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev -c "SELECT COUNT(*) FROM entities WHERE government_org_id IS NOT NULL;"
```

### Detailed Analysis
```bash
cd backend
docker exec -i newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev < query-validated-entities.sql
```

**Expected Output:**
- Query 1: Shows entities linked to government orgs
- Query 2: Shows all GOVERNMENT_ORG entities (linked/unlinked)
- Query 3: Statistics (validation success rate)
- Query 4: Most referenced government organizations
- Query 5: Recent validations with timestamps
- Query 6: Unvalidated entities (need manual review)

---

## 📝 Manual Testing Alternative

If scripts fail, test manually:

### 1. Start Backend
```bash
cd backend
mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 2. Wait for Startup (~30 seconds)
```bash
# Check health
curl http://localhost:8080/actuator/health
```

### 3. Test Validation
```bash
curl -X POST http://localhost:8080/api/entities/validate \
  -H "Content-Type: application/json" \
  -d '{"entityType":"GOVERNMENT_ORG","name":"DOD","source":"manual_test","confidenceScore":0.92}'
```

### 4. Check Response
Look for `governmentOrganizationId` in response

---

## 🎓 What You Learn

Running these scripts teaches you:

1. **How validation works** - See real-time entity validation and enrichment
2. **What success looks like** - Clear indicators of correct behavior
3. **How to debug issues** - Color-coded error messages
4. **Database verification** - SQL queries to inspect results

---

## 📚 Documentation Reference

**For More Details:**

| Topic | Document |
|-------|----------|
| Quick Start | `backend/DEPLOYMENT_SCRIPTS_README.md` |
| Full Implementation | `docs/PHASE_1.6_ENTITY_GOV_ORG_LINKING.md` |
| Test Results | `docs/PHASE_1.6_TEST_RESULTS.md` |
| Deployment Status | `docs/PHASE_1.6_DEPLOYMENT_STATUS.md` |
| Phase Complete | `docs/PHASE_1.6_COMPLETE.md` |

---

## 🔥 Example Output (Success)

```
============================================================================
Phase 1.6 - Entity-to-GovernmentOrganization Linking
Automated Deployment Verification
============================================================================

Step 1/6: Stopping existing backend server...
[OK] Backend server stopped

Step 2/6: Starting backend server in new window...
[OK] Backend server starting (PID: 12345)

Step 3/6: Waiting for backend to start (30 seconds)...
Checking health... (attempt 15/30) [OK]
[OK] Backend is UP!

Step 4/6: Verifying health endpoint...
{"status":"UP"}

Step 5/6: Testing government organizations endpoint...
{
  "content": [
    {"officialName": "Department of State", "acronym": "DOS"},
    {"officialName": "Department of the Treasury", "acronym": "Treasury"},
    {"officialName": "Department of Defense", "acronym": "DOD"}
  ],
  "totalElements": 20
}

Step 6/6: Testing NEW validation endpoints...

Test: Create and Validate DOD
----------------------------------------------------------------------------
Response:
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "name": "Department of Defense",
  "verified": true,
  "confidenceScore": 1.0,
  "governmentOrganizationId": "6892b661-37ea-4d9c-88a7-e9a5929414ec",
  "governmentOrganizationName": "Department of Defense",
  "properties": {
    "acronym": "DOD",
    "website": "https://www.defense.gov"
  }
}
[SUCCESS] Entity linked to government organization!
  - Linked to: Department of Defense
  - Verified: True
  - Confidence: 1.0

============================================================================
Deployment Verification Complete!
============================================================================

✅ All tests passed! Phase 1.6 is fully deployed!
```

---

## 🚦 Next Steps After Success

### 1. Integrate with Python Service
Update entity extraction to use validation endpoint:
```python
response = requests.post(
    f"{JAVA_BACKEND}/api/entities/validate",
    json=entity_data
)
```

### 2. Update Frontend
Display government org links:
```typescript
if (entity.governmentOrganizationId) {
  return <Link to={`/government-orgs/${entity.governmentOrganizationId}`}>
    {entity.governmentOrganizationName}
  </Link>;
}
```

### 3. Backfill Existing Entities
Validate entities created before Phase 1.6:
```bash
# Get all unvalidated government orgs
curl "http://localhost:8080/api/entities/type/GOVERNMENT_ORG" | jq '.[] | select(.verified == false) | .id'

# Validate each one
for id in $(GET_IDS_HERE); do
  curl -X POST "http://localhost:8080/api/entities/$id/validate"
done
```

---

## 🎯 Deployment Milestones

- [x] Database migration V4 applied
- [x] Backend code compiled
- [x] Unit tests passing (24/24)
- [x] Deployment scripts created
- [ ] **Run deployment script** ← YOU ARE HERE
- [ ] Verify all tests pass
- [ ] Check database records
- [ ] Mark Phase 1.6 as complete

---

## 🏆 Success Criteria Met When

✅ PowerShell/Batch script completes without errors
✅ All 4 validation tests show [SUCCESS] indicators
✅ Database query shows entities with `government_org_id` populated
✅ Verified flag is `true` for validated entities
✅ Properties are enriched with government org data

**When all above are true → Phase 1.6 is PRODUCTION READY! 🚀**

---

## 📞 Support

**If you encounter issues:**

1. **Check script output** - Error messages are color-coded
2. **Check backend window** - Look for Java exceptions
3. **Check database** - Run `query-validated-entities.sql`
4. **Consult docs** - See references above

**Common fixes:**
- Restart backend: Ctrl+C in backend window, run script again
- Rebuild: `mvnw clean install`, run script again
- Check PostgreSQL: `docker ps | findstr postgres`

---

## 🎉 You're Ready!

**Run the script now:**

```powershell
cd backend
.\deploy-and-test-phase1.6.ps1
```

**Watch for:**
- Green [SUCCESS] messages
- `governmentOrganizationId` in responses
- `verified: true` in responses

**When you see these → Congratulations! Phase 1.6 is deployed! 🎊**

---

**Created:** 2025-11-24
**Author:** Winston (System Architect)
**Status:** ✅ Ready for Deployment Testing
