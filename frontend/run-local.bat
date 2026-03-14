@echo off
REM ============================================================
REM  NewsAnalyzer Frontend - Local Development Launcher
REM  Requires: Backend running on localhost:8080
REM  Usage:    run-local.bat          (without observability)
REM            run-local.bat --otel   (with observability)
REM ============================================================

cd /d "%~dp0"

if "%1"=="--otel" (
    echo Starting frontend WITH OpenTelemetry instrumentation...
    set OTEL_SERVICE_NAME=newsanalyzer-frontend
    set OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
    set OTEL_EXPORTER_OTLP_PROTOCOL=grpc
    set OTEL_TRACES_SAMPLER=always_on
    set OTEL_RESOURCE_ATTRIBUTES=deployment.environment=dev
) else (
    echo Starting frontend without observability...
)

REM .env.local provides NEXT_PUBLIC_BACKEND_URL and NEXT_PUBLIC_REASONING_SERVICE_URL
set OTEL_SDK_DISABLED=true
call npm run dev
