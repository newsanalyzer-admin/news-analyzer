# ============================================================================
# Phase 1.6 Deployment Verification Script (PowerShell)
# Automates: Stop backend, Start backend, Wait for startup, Test endpoints
# ============================================================================

$ErrorActionPreference = "Continue"

Write-Host ""
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "Phase 1.6 - Entity-to-GovernmentOrganization Linking" -ForegroundColor Cyan
Write-Host "Automated Deployment Verification" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""

# Configuration
$BackendPort = 8080
$WaitTime = 30
$HealthUrl = "http://localhost:$BackendPort/actuator/health"
$ValidateUrl = "http://localhost:$BackendPort/api/entities/validate"
$EntitiesUrl = "http://localhost:$BackendPort/api/entities"
$GovOrgsUrl = "http://localhost:$BackendPort/api/government-organizations"

# Step 1: Stop existing backend
Write-Host "Step 1/6: Stopping existing backend server..." -ForegroundColor Yellow
Write-Host "============================================================================"
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    $javaProcesses | Stop-Process -Force
    Write-Host "[OK] Backend server stopped" -ForegroundColor Green
} else {
    Write-Host "[INFO] No running backend server found" -ForegroundColor Gray
}
Start-Sleep -Seconds 2
Write-Host ""

# Step 2: Start backend server
Write-Host "Step 2/6: Starting backend server in new window..." -ForegroundColor Yellow
Write-Host "============================================================================"
Write-Host "Starting: mvnw spring-boot:run -Dspring.profiles.active=dev"
Write-Host "Window will open separately. Check it for startup logs."

$processInfo = New-Object System.Diagnostics.ProcessStartInfo
$processInfo.FileName = "cmd.exe"
$processInfo.Arguments = "/k `"mvnw spring-boot:run -Dspring.profiles.active=dev`""
$processInfo.WorkingDirectory = Get-Location
$processInfo.UseShellExecute = $true
$processInfo.CreateNoWindow = $false
$process = [System.Diagnostics.Process]::Start($processInfo)

Write-Host "[OK] Backend server starting (PID: $($process.Id))" -ForegroundColor Green
Write-Host ""

# Step 3: Wait for backend to start
Write-Host "Step 3/6: Waiting for backend to start ($WaitTime seconds)..." -ForegroundColor Yellow
Write-Host "============================================================================"
Write-Host "Waiting for health endpoint to respond..."

$backendReady = $false
for ($i = 1; $i -le $WaitTime; $i++) {
    Write-Host "Checking health... (attempt $i/$WaitTime)" -NoNewline

    try {
        $response = Invoke-WebRequest -Uri $HealthUrl -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host " [OK]" -ForegroundColor Green
            Write-Host "[OK] Backend is UP!" -ForegroundColor Green
            $backendReady = $true
            break
        }
    } catch {
        Write-Host " ." -NoNewline
    }

    Start-Sleep -Seconds 1
}

Write-Host ""
if (-not $backendReady) {
    Write-Host "[WARNING] Backend health check timed out after $WaitTime seconds" -ForegroundColor Yellow
    Write-Host "Please check the backend window for errors"
    Write-Host ""
    Write-Host "Press any key to continue anyway, or Ctrl+C to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
}
Write-Host ""

# Step 4: Verify health endpoint
Write-Host "Step 4/6: Verifying health endpoint..." -ForegroundColor Yellow
Write-Host "============================================================================"
try {
    $health = Invoke-RestMethod -Uri $HealthUrl -Method Get
    Write-Host ($health | ConvertTo-Json -Depth 10) -ForegroundColor Green
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# Step 5: Test government organizations endpoint
Write-Host "Step 5/6: Testing government organizations endpoint..." -ForegroundColor Yellow
Write-Host "============================================================================"
Write-Host "GET $GovOrgsUrl?page=0&size=3"
try {
    $govOrgs = Invoke-RestMethod -Uri "$GovOrgsUrl?page=0&size=3" -Method Get
    Write-Host ($govOrgs | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# Step 6: Test NEW validation endpoints
Write-Host "Step 6/6: Testing NEW validation endpoints..." -ForegroundColor Yellow
Write-Host "============================================================================"
Write-Host ""

function Test-ValidationEndpoint {
    param(
        [string]$TestName,
        [string]$EntityName,
        [double]$ConfidenceScore
    )

    Write-Host "Test: $TestName" -ForegroundColor Cyan
    Write-Host "----------------------------------------------------------------------------"

    $body = @{
        entityType = "GOVERNMENT_ORG"
        name = $EntityName
        source = "automated_test"
        confidenceScore = $ConfidenceScore
    } | ConvertTo-Json

    Write-Host "Request Body:"
    Write-Host $body -ForegroundColor Gray
    Write-Host ""
    Write-Host "Response:"

    try {
        $response = Invoke-RestMethod -Uri $ValidateUrl -Method Post `
            -ContentType "application/json" -Body $body

        Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor Green

        # Validation checks
        if ($response.governmentOrganizationId) {
            Write-Host "[SUCCESS] Entity linked to government organization!" -ForegroundColor Green
            Write-Host "  - Linked to: $($response.governmentOrganizationName)" -ForegroundColor Green
            Write-Host "  - Verified: $($response.verified)" -ForegroundColor Green
            Write-Host "  - Confidence: $($response.confidenceScore)" -ForegroundColor Green
        } else {
            Write-Host "[INFO] Entity created but not linked (no match found)" -ForegroundColor Yellow
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 405) {
            Write-Host "[ERROR] 405 Method Not Allowed - Endpoint not found!" -ForegroundColor Red
            Write-Host "  Backend may not have latest code. Try restarting." -ForegroundColor Red
        } else {
            Write-Host "[ERROR] $statusCode - $($_.Exception.Message)" -ForegroundColor Red
        }
    }

    Write-Host ""
    Write-Host ""
}

# Run tests
Test-ValidationEndpoint "Create and Validate DOD" "DOD" 0.92
Test-ValidationEndpoint "Create and Validate FBI" "FBI" 0.88
Test-ValidationEndpoint "Create and Validate EPA" "EPA" 0.95
Test-ValidationEndpoint "Unknown Agency (No Match Expected)" "Unknown Test Agency" 0.7

# Step 7: List all entities
Write-Host "Test 5: GET $EntitiesUrl (List All Entities)" -ForegroundColor Cyan
Write-Host "----------------------------------------------------------------------------"
try {
    $entities = Invoke-RestMethod -Uri $EntitiesUrl -Method Get
    Write-Host ($entities | ConvertTo-Json -Depth 10)
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# Summary
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host "Deployment Verification Complete!" -ForegroundColor Cyan
Write-Host "============================================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Expected Results:" -ForegroundColor Yellow
Write-Host "  - Test 1 (DOD): Should link to 'Department of Defense', verified=true"
Write-Host "  - Test 2 (FBI): Should link to 'Federal Bureau of Investigation', verified=true"
Write-Host "  - Test 3 (EPA): Should link to 'Environmental Protection Agency', verified=true"
Write-Host "  - Test 4 (Unknown): Should create entity but NOT link (verified=false)"
Write-Host ""
Write-Host "Check Results Above:" -ForegroundColor Yellow
Write-Host "  - If you see 'governmentOrganizationId' populated: SUCCESS!"
Write-Host "  - If you see 'verified': true: Entity was validated!"
Write-Host "  - If you see enriched properties (acronym, website): Enrichment worked!"
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Green
Write-Host "  1. Review the responses above"
Write-Host "  2. Check database: Run query-validated-entities.sql"
Write-Host "  3. If tests passed, Phase 1.6 is fully deployed!"
Write-Host ""
Write-Host "Database Query (optional):" -ForegroundColor Gray
Write-Host '  docker exec newsanalyzer-postgres-dev psql -U newsanalyzer -d newsanalyzer_dev -f /query-validated-entities.sql'
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
