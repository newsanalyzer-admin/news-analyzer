# Epic OBS-1: Full-Stack Observability with OpenTelemetry & Grafana

## Status

**Done**

---

## Epic Summary

Add comprehensive observability to NewsAnalyzer v2 by instrumenting all three services (Spring Boot backend, Next.js frontend, FastAPI reasoning service) with OpenTelemetry and deploying the Grafana LGTM stack (Loki, Grafana, Tempo, Mimir/Prometheus) for metrics, logs, and distributed traces. This epic delivers a production-grade observability platform usable in both development and production environments.

---

## Business Value

- **Operational Visibility**: Real-time insight into service health, latency, error rates, and throughput across all three services
- **Faster Debugging**: Distributed tracing correlates requests across the Java backend, Python reasoning service, and Next.js frontend — reducing mean time to resolution
- **Production Readiness**: Industry-standard observability is a prerequisite for operating in production with confidence
- **Developer Experience**: Developers can trace requests end-to-end during local development using the same tooling as production
- **Portfolio Value**: OpenTelemetry (CNCF #2 after Kubernetes) + Grafana stack is the current industry gold standard for open-source observability

---

## Scope

### Three Pillars of Observability

| Pillar | Tool | What It Provides |
|--------|------|------------------|
| **Metrics** | Prometheus | Request rates, latency histograms, JVM/Python/Node runtime metrics, custom business metrics |
| **Logs** | Loki | Centralized structured logs from all services, queryable via LogQL, correlated with traces |
| **Traces** | Tempo | Distributed traces across service boundaries, latency breakdowns, dependency mapping |

### Visualization

| Tool | Purpose |
|------|---------|
| **Grafana** | Unified dashboards for all three pillars, log-to-trace correlation, service health overview |

### Instrumentation Standard

| Service | Instrumentation Approach |
|---------|-------------------------|
| **Backend (Spring Boot)** | OTel Java Agent (zero-code auto-instrumentation via JVM flag) |
| **Reasoning Service (FastAPI)** | OTel Python SDK with auto-instrumentation packages |
| **Frontend (Next.js)** | OTel JS SDK — server-side traces + client-side web vitals |

### Out of Scope

- Alerting rules and notification channels (future epic)
- APM profiling (continuous profiling)
- Synthetic monitoring / uptime checks
- SLO/SLA dashboards
- Custom business metric dashboards beyond service health

---

## Architecture Overview

```
                        ┌─────────────────────────────────┐
                        │          Grafana (:3001)         │
                        │   Dashboards, Log Exploration,   │
                        │   Trace Viewer, Metrics Graphs   │
                        └──────┬──────────┬──────────┬─────┘
                               │          │          │
                    ┌──────────┘    ┌─────┘    ┌─────┘
                    ▼              ▼            ▼
              ┌──────────┐  ┌──────────┐  ┌──────────┐
              │Prometheus │  │   Loki   │  │  Tempo   │
              │  (:9090)  │  │ (:3100)  │  │ (:3200)  │
              │  Metrics  │  │   Logs   │  │  Traces  │
              └─────▲─────┘  └────▲─────┘  └────▲─────┘
                    │             │              │
                    └─────────────┼──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │   OpenTelemetry Collector   │
                    │     (:4317 gRPC / :4318     │
                    │          HTTP)              │
                    └──▲──────────▲───────────▲──┘
                       │          │           │
              ┌────────┘   ┌──────┘    ┌──────┘
              ▼            ▼           ▼
     ┌──────────────┐ ┌─────────┐ ┌──────────────┐
     │   Backend    │ │Frontend │ │  Reasoning   │
     │ Spring Boot  │ │ Next.js │ │   FastAPI    │
     │   (:8080)    │ │ (:3000) │ │   (:8000)    │
     │ OTel Java    │ │ OTel JS │ │ OTel Python  │
     │   Agent      │ │   SDK   │ │    SDK       │
     └──────────────┘ └─────────┘ └──────────────┘
```

### Data Flow

1. Each service emits telemetry (metrics, logs, traces) via OTLP protocol to the **OTel Collector**
2. The Collector processes and routes data to the appropriate backend:
   - Metrics → **Prometheus** (via remote write or scrape)
   - Logs → **Loki** (via Loki exporter)
   - Traces → **Tempo** (via OTLP exporter)
3. **Grafana** queries all three backends and provides unified dashboards with cross-signal correlation

### Port Allocations

| Service | Development Port | Purpose |
|---------|-----------------|---------|
| Grafana | 3001 | Dashboards (avoids conflict with frontend :3000) |
| Prometheus | 9090 | Metrics storage & query |
| Loki | 3100 | Log aggregation & query |
| Tempo | 3200 | Trace storage & query |
| OTel Collector (gRPC) | 4317 | Telemetry ingestion (gRPC) |
| OTel Collector (HTTP) | 4318 | Telemetry ingestion (HTTP) |

---

## Stories

| Story | Title | Status | Points |
|-------|-------|--------|--------|
| OBS-1.1 | Observability Infrastructure Stack | Done | 5 |
| OBS-1.2 | Spring Boot Backend Instrumentation | Done | 5 |
| OBS-1.3 | FastAPI Reasoning Service Instrumentation | Done | 5 |
| OBS-1.4 | Next.js Frontend Instrumentation | Done | 3 |
| OBS-1.5 | Grafana Dashboards & Correlation | Done | 5 |
| **Total** | | **5/5 Done** | **23** |

### Story Dependency Chain

```
OBS-1.1 (Infrastructure)
   ├──► OBS-1.2 (Backend Instrumentation)
   ├──► OBS-1.3 (Reasoning Service Instrumentation)
   └──► OBS-1.4 (Frontend Instrumentation)
              │
              ▼
        OBS-1.5 (Dashboards) — requires all instrumentation stories complete
```

- **OBS-1.1** must be completed first (infrastructure foundation)
- **OBS-1.2, OBS-1.3, OBS-1.4** can be parallelized after OBS-1.1
- **OBS-1.5** depends on all instrumentation stories to build meaningful dashboards

---

## Story Summaries

### OBS-1.1: Observability Infrastructure Stack

Deploy the full observability backend via Docker Compose for both dev and production environments.

**Deliverables:**
- OTel Collector configuration (receivers, processors, exporters)
- Prometheus with scrape config for Actuator endpoint + OTel metrics
- Loki for log aggregation
- Tempo for trace storage
- Grafana with pre-configured data sources (Prometheus, Loki, Tempo)
- Docker Compose service definitions for dev (`docker-compose.dev.yml`) and prod (`deploy/`)
- Health checks for all observability services

### OBS-1.2: Spring Boot Backend Instrumentation

Instrument the Java backend using the OTel Java agent for auto-instrumentation.

**Deliverables:**
- OTel Java agent JAR integrated into backend startup (JVM flag `-javaagent`)
- OTLP exporter configured to send to OTel Collector
- Existing Actuator/Prometheus metrics preserved and scraped
- Automatic instrumentation of Spring Web (HTTP), JPA/Hibernate (DB), Redis, and RestTemplate/WebClient calls
- Trace context propagation via W3C Trace Context headers
- Structured log correlation (trace ID + span ID injected into Logback MDC)
- Dockerfile updated with OTel agent
- Application config for dev/prod profiles

### OBS-1.3: FastAPI Reasoning Service Instrumentation

Instrument the Python reasoning service using the OTel Python SDK.

**Deliverables:**
- `opentelemetry-distro` and auto-instrumentation packages added to `requirements.txt`
- OTLP exporter configured to send to OTel Collector
- Automatic instrumentation of FastAPI routes, HTTPX calls
- Trace context propagation (incoming from backend, outgoing to external APIs)
- Structured log correlation (trace ID + span ID in log records)
- Dockerfile updated with OTel bootstrap
- Environment-based config for dev/prod

### OBS-1.4: Next.js Frontend Instrumentation

Instrument the Next.js frontend for server-side traces and client-side web vitals.

**Deliverables:**
- `@opentelemetry/sdk-node` for Next.js server-side instrumentation
- Server-side: automatic tracing of API route handlers and `fetch` calls to backend
- Client-side: Web Vitals (LCP, FID, CLS) exported as metrics
- Trace context propagation on outbound API calls (Axios interceptor)
- `instrumentation.ts` Next.js instrumentation hook
- OTLP exporter configured for OTel Collector

### OBS-1.5: Grafana Dashboards & Correlation

Build pre-provisioned Grafana dashboards for service health and cross-signal correlation.

**Deliverables:**
- **Service Health Dashboard**: Request rate, error rate, latency (RED metrics) per service
- **JVM Dashboard**: Heap usage, GC pauses, thread counts (backend)
- **Python Runtime Dashboard**: Memory, CPU, active requests (reasoning service)
- **Distributed Trace Explorer**: Pre-configured Tempo data source with log-to-trace links
- **Log Explorer**: Pre-configured Loki queries for each service with trace correlation
- All dashboards provisioned as JSON (Grafana provisioning, not manual creation)
- Grafana home dashboard with service overview

---

## Technical Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Instrumentation standard | OpenTelemetry | CNCF standard, vendor-neutral, supports all 3 languages |
| Metrics backend | Prometheus | Already integrated via Actuator; battle-tested |
| Log backend | Loki | Lightweight, label-based (like Prometheus for logs), pairs natively with Grafana |
| Trace backend | Tempo | Minimal dependencies (no Elasticsearch/Cassandra), native Grafana integration |
| Collector deployment | OTel Collector as sidecar container | Decouples services from backend specifics; enables processing/batching |
| Java instrumentation | OTel Java Agent (auto) | Zero-code instrumentation; covers Spring, JPA, JDBC, Redis automatically |
| Python instrumentation | OTel Python SDK + auto packages | Lightweight; auto-instruments FastAPI, HTTPX |
| Dashboard provisioning | Grafana JSON provisioning | Infrastructure-as-code; reproducible across environments |

---

## Compatibility Requirements

- [x] Existing APIs remain unchanged — instrumentation is additive only
- [x] Existing Actuator/Prometheus endpoint preserved — Prometheus scrapes it alongside OTel metrics
- [x] Existing Logback configuration extended (not replaced) — adds OTel appender for export
- [x] Existing Docker Compose services unaffected — observability services are additive
- [x] No database schema changes required
- [x] No frontend UI changes — instrumentation is transparent to users
- [x] Performance impact minimal — OTel agents are designed for production use

---

## Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| OTel Java agent adds startup latency | Low | Low | Agent adds ~1-2s cold start; acceptable for long-running service |
| Memory overhead from telemetry buffering | Low | Medium | OTel Collector handles batching; configure memory limits |
| Port conflicts with existing services | Low | Low | All observability ports are on non-conflicting ranges (3001, 3100, 3200, 4317, 4318, 9090) |
| Docker Compose resource usage in dev | Medium | Low | Observability services are lightweight; Loki/Tempo use local filesystem storage |
| Trace volume in production | Medium | Medium | Configure sampling rate (e.g., 10% in prod, 100% in dev) via Collector config |
| Next.js OTel integration maturity | Medium | Low | Next.js has official `instrumentation.ts` hook since v13; stable in v14 |

---

## Dependencies

### External (New)

| Dependency | Version | Purpose |
|------------|---------|---------|
| OpenTelemetry Collector | latest | Telemetry pipeline |
| Grafana | 11+ | Visualization |
| Prometheus | 2.x | Metrics backend |
| Grafana Loki | 3.x | Log backend |
| Grafana Tempo | 2.x | Trace backend |
| OTel Java Agent | 2.x | Backend auto-instrumentation |
| opentelemetry-distro (Python) | 0.48+ | Reasoning service instrumentation |
| @opentelemetry/sdk-node (JS) | 1.x | Frontend instrumentation |

### Internal

| Dependency | Impact |
|------------|--------|
| Spring Boot Actuator (existing) | Prometheus scrape target — no changes needed |
| Logback (existing) | Extended with OTel log appender for export |
| Docker Compose (existing) | New services added alongside existing Postgres + Redis |

---

## Definition of Done

- [x] All five stories completed with acceptance criteria met
- [x] All three services emit metrics, logs, and traces to OTel Collector
- [x] Grafana dashboards load with live data from all three pillars
- [x] Distributed trace shows request flow from frontend → backend → reasoning service
- [x] Log entries include trace IDs for correlation
- [x] Dev environment works via `docker compose up` with no manual configuration
- [x] Prod environment configuration documented and deployable
- [x] Existing functionality verified — no regressions
- [x] Tech stack documentation updated with observability components
- [x] Source tree documentation updated

---

## Future Considerations

1. **Alerting**: Grafana Alerting with notification channels (Slack, email) — separate epic
2. **SLO Dashboards**: Define and track SLIs/SLOs per service
3. **Continuous Profiling**: Pyroscope integration for CPU/memory profiling
4. **Synthetic Monitoring**: Uptime checks and API probes
5. **Custom Business Metrics**: Entity counts, import rates, sync health
6. **Log-based Alerts**: Alert on error rate spikes via Loki rules

---

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-03-05 | 1.0 | Epic created with 5 stories | Sarah (PO) |
| 2026-03-11 | 1.1 | OBS-1.4 completed — 4/5 stories Done, OBS-1.5 unblocked | Sarah (PO) |
| 2026-03-11 | 1.2 | OBS-1.5 completed — **5/5 stories Done, epic complete** | Sarah (PO) |

---

## References

- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [OpenTelemetry Java Agent](https://opentelemetry.io/docs/zero-code/java/agent/)
- [OpenTelemetry Python](https://opentelemetry.io/docs/languages/python/)
- [OpenTelemetry JavaScript](https://opentelemetry.io/docs/languages/js/)
- [Grafana LGTM Stack](https://grafana.com/oss/)
- [Grafana Loki](https://grafana.com/oss/loki/)
- [Grafana Tempo](https://grafana.com/oss/tempo/)
- [Next.js Instrumentation](https://nextjs.org/docs/app/building-your-application/optimizing/instrumentation)
