@echo off
REM ============================================================================
REM Phase 1.6 Deployment Verification Script
REM Automates: Stop backend, Start backend, Wait for startup, Test endpoints
REM ============================================================================

setlocal enabledelayedexpansion

echo.
echo ============================================================================
echo Phase 1.6 - Entity-to-GovernmentOrganization Linking
echo Automated Deployment Verification
echo ============================================================================
echo.

REM Configuration
set BACKEND_PORT=8080
set WAIT_TIME=30
set HEALTH_URL=http://localhost:%BACKEND_PORT%/actuator/health
set VALIDATE_URL=http://localhost:%BACKEND_PORT%/api/entities/validate
set ENTITIES_URL=http://localhost:%BACKEND_PORT%/api/entities
set GOV_ORGS_URL=http://localhost:%BACKEND_PORT%/api/government-organizations

echo Step 1/6: Stopping existing backend server...
echo ============================================================================
taskkill /F /IM java.exe 2>nul
if %errorlevel%==0 (
    echo [OK] Backend server stopped
) else (
    echo [INFO] No running backend server found
)
timeout /t 2 /nobreak >nul
echo.

echo Step 2/6: Starting backend server in new window...
echo ============================================================================
echo Starting: mvnw spring-boot:run -Dspring.profiles.active=dev
echo Window will open separately. Check it for startup logs.
start "NewsAnalyzer Backend - Phase 1.6" cmd /k "mvnw spring-boot:run -Dspring.profiles.active=dev"
echo [OK] Backend server starting in separate window
echo.

echo Step 3/6: Waiting for backend to start (%WAIT_TIME% seconds)...
echo ============================================================================
echo Waiting for health endpoint to respond...
for /L %%i in (1,1,%WAIT_TIME%) do (
    echo Checking health... (attempt %%i/%WAIT_TIME%)
    curl -s -f -m 2 %HEALTH_URL% >nul 2>&1
    if !errorlevel!==0 (
        echo [OK] Backend is UP!
        goto :backend_ready
    )
    timeout /t 1 /nobreak >nul
)

echo [WARNING] Backend health check timed out after %WAIT_TIME% seconds
echo Please check the backend window for errors
echo.
echo Press any key to continue anyway, or Ctrl+C to exit...
pause >nul

:backend_ready
echo.

echo Step 4/6: Verifying health endpoint...
echo ============================================================================
curl -s %HEALTH_URL%
echo.
echo.

echo Step 5/6: Testing government organizations endpoint...
echo ============================================================================
echo GET %GOV_ORGS_URL%?page=0^&size=3
curl -s "%GOV_ORGS_URL%?page=0&size=3"
echo.
echo.

echo Step 6/6: Testing NEW validation endpoints...
echo ============================================================================
echo.

REM Test 1: Create and validate "DOD"
echo Test 1: POST %VALIDATE_URL% (Create and Validate "DOD")
echo ----------------------------------------------------------------------------
echo Request Body:
echo {
echo   "entityType": "GOVERNMENT_ORG",
echo   "name": "DOD",
echo   "source": "automated_test",
echo   "confidenceScore": 0.92
echo }
echo.
echo Response:

curl -X POST %VALIDATE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"entityType\":\"GOVERNMENT_ORG\",\"name\":\"DOD\",\"source\":\"automated_test\",\"confidenceScore\":0.92}" ^
  2>nul

echo.
echo.

REM Test 2: Create and validate "FBI"
echo Test 2: POST %VALIDATE_URL% (Create and Validate "FBI")
echo ----------------------------------------------------------------------------
echo Request Body:
echo {
echo   "entityType": "GOVERNMENT_ORG",
echo   "name": "FBI",
echo   "source": "automated_test",
echo   "confidenceScore": 0.88
echo }
echo.
echo Response:

curl -X POST %VALIDATE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"entityType\":\"GOVERNMENT_ORG\",\"name\":\"FBI\",\"source\":\"automated_test\",\"confidenceScore\":0.88}" ^
  2>nul

echo.
echo.

REM Test 3: Create and validate "EPA"
echo Test 3: POST %VALIDATE_URL% (Create and Validate "EPA")
echo ----------------------------------------------------------------------------
echo Request Body:
echo {
echo   "entityType": "GOVERNMENT_ORG",
echo   "name": "EPA",
echo   "source": "automated_test",
echo   "confidenceScore": 0.95
echo }
echo.
echo Response:

curl -X POST %VALIDATE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"entityType\":\"GOVERNMENT_ORG\",\"name\":\"EPA\",\"source\":\"automated_test\",\"confidenceScore\":0.95}" ^
  2>nul

echo.
echo.

REM Test 4: Create and validate unknown org (should not link)
echo Test 4: POST %VALIDATE_URL% (Unknown Agency - No Match Expected)
echo ----------------------------------------------------------------------------
echo Request Body:
echo {
echo   "entityType": "GOVERNMENT_ORG",
echo   "name": "Unknown Test Agency",
echo   "source": "automated_test",
echo   "confidenceScore": 0.7
echo }
echo.
echo Response:

curl -X POST %VALIDATE_URL% ^
  -H "Content-Type: application/json" ^
  -d "{\"entityType\":\"GOVERNMENT_ORG\",\"name\":\"Unknown Test Agency\",\"source\":\"automated_test\",\"confidenceScore\":0.7}" ^
  2>nul

echo.
echo.

REM Test 5: List all entities to see validated ones
echo Test 5: GET %ENTITIES_URL% (List All Entities)
echo ----------------------------------------------------------------------------
curl -s "%ENTITIES_URL%"
echo.
echo.

echo ============================================================================
echo Deployment Verification Complete!
echo ============================================================================
echo.
echo Expected Results:
echo   - Test 1 (DOD): Should link to "Department of Defense", verified=true
echo   - Test 2 (FBI): Should link to "Federal Bureau of Investigation", verified=true
echo   - Test 3 (EPA): Should link to "Environmental Protection Agency", verified=true
echo   - Test 4 (Unknown): Should create entity but NOT link (verified=false)
echo.
echo Check Results Above:
echo   - If you see "governmentOrganizationId" populated: SUCCESS!
echo   - If you see "verified": true: Entity was validated!
echo   - If you see enriched properties (acronym, website): Enrichment worked!
echo.
echo Next Steps:
echo   1. Review the responses above
echo   2. Check database: Run query-validated-entities.sql
echo   3. If tests passed, Phase 1.6 is fully deployed!
echo.
echo Database Query (optional):
echo   docker exec newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev -f query-validated-entities.sql
echo.
pause
