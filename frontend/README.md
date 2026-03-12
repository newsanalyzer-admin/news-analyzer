# NewsAnalyzer Frontend

Next.js 14 frontend with TypeScript and Tailwind CSS for NewsAnalyzer v2.

## Tech Stack

- **Next.js 14** (App Router)
- **TypeScript 5.3**
- **Tailwind CSS 3.4**
- **Shadcn/UI** (component library)
- **TanStack Query** (data fetching)
- **Zustand** (state management)
- **Vitest** (unit testing)
- **Playwright** (E2E testing)

## Project Structure

```
frontend/
├── src/
│   ├── app/              # Next.js App Router pages
│   │   ├── layout.tsx    # Root layout
│   │   ├── page.tsx      # Home page
│   │   ├── globals.css   # Global styles
│   │   ├── members/      # Congressional members pages
│   │   ├── committees/   # Committees pages
│   │   └── admin/        # Admin dashboard
│   ├── components/       # React components
│   │   ├── ui/           # Shadcn UI components
│   │   ├── congressional/ # Member/committee components
│   │   └── admin/        # Admin sync components
│   ├── lib/              # Utilities and helpers
│   │   ├── api/          # API client functions
│   │   ├── utils/        # Helper utilities
│   │   └── constants/    # Constants (states, etc.)
│   ├── hooks/            # Custom React hooks
│   └── types/            # TypeScript types
├── public/               # Static assets
├── package.json
├── tsconfig.json
├── tailwind.config.ts
└── next.config.js
```

## Setup

### Prerequisites

- Node.js 20+
- pnpm 8+ (recommended) or npm

### 1. Install Dependencies

```bash
pnpm install
```

### 2. Configure Environment

Copy the example environment file:
```bash
cp .env.local.example .env.local
```

Edit `.env.local` to point to your backend API.

### 3. Run Development Server

```bash
pnpm dev
```

The app will be available at http://localhost:3000

## Development

### Adding Components

Using Shadcn/UI components:
```bash
npx shadcn-ui@latest add button
npx shadcn-ui@latest add card
```

### API Integration

API calls are proxied through Next.js:
- Frontend: `http://localhost:3000/api/*`
- Backend: `http://localhost:8080/*`

### Code Style

```bash
# Lint
pnpm lint

# Type check
pnpm type-check
```

## Testing

```bash
# Unit tests (Vitest)
pnpm test

# Unit tests with UI
pnpm test:ui

# E2E tests (Playwright)
pnpm test:e2e
```

## Building

```bash
# Production build
pnpm build

# Start production server
pnpm start
```

## Docker Build

```bash
docker build -t newsanalyzer-frontend:latest .
```

## Running with Observability (Outside Docker)

When running the frontend on your host machine, OpenTelemetry auto-instruments server-side routes and exports traces/metrics via OTLP. Client-side Core Web Vitals (LCP, CLS, INP) are collected via the `WebVitalsReporter` component and proxied through `/api/vitals`.

1. The observability stack must be running in Docker (from the root of the repo):
   ```bash
   docker compose -f docker-compose.dev.yml up -d
   ```

2. Start the dev server with OTel env vars:

   **Linux / macOS:**
   ```bash
   OTEL_SERVICE_NAME=newsanalyzer-frontend \
   OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317 \
   OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
   OTEL_TRACES_SAMPLER=always_on \
   OTEL_RESOURCE_ATTRIBUTES=deployment.environment=dev \
   pnpm dev
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:OTEL_SERVICE_NAME="newsanalyzer-frontend"
   $env:OTEL_EXPORTER_OTLP_ENDPOINT="http://localhost:4317"
   $env:OTEL_EXPORTER_OTLP_PROTOCOL="grpc"
   $env:OTEL_TRACES_SAMPLER="always_on"
   $env:OTEL_RESOURCE_ATTRIBUTES="deployment.environment=dev"
   pnpm dev
   ```

   **Windows (Command Prompt):**
   ```cmd
   set OTEL_SERVICE_NAME=newsanalyzer-frontend
   set OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
   set OTEL_EXPORTER_OTLP_PROTOCOL=grpc
   set OTEL_TRACES_SAMPLER=always_on
   set OTEL_RESOURCE_ATTRIBUTES=deployment.environment=dev
   pnpm dev
   ```

Once running, server-side traces and client-side Web Vitals will appear in Grafana at http://localhost:3001.

> **Note:** The Web Vitals proxy (`/api/vitals`) auto-derives the HTTP endpoint (`localhost:4318`) from `OTEL_EXPORTER_OTLP_ENDPOINT` by replacing `:4317` with `:4318`. No additional configuration needed.

## Environment Variables

- `NEXT_PUBLIC_BACKEND_URL` - Java Spring Boot backend URL (default: `http://localhost:8080`)
- `NEXT_PUBLIC_REASONING_SERVICE_URL` - Python FastAPI reasoning service URL (default: `http://localhost:8000`)
- `OTEL_SERVICE_NAME` - OpenTelemetry service name (default: `newsanalyzer-frontend`)
- `OTEL_EXPORTER_OTLP_ENDPOINT` - OTel Collector endpoint (default: `http://otel-collector:4317`, use `http://localhost:4317` outside Docker)

## License

MIT License - See [LICENSE](../LICENSE)
