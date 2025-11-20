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
│   │   └── globals.css   # Global styles
│   ├── components/       # React components
│   │   ├── ui/          # Shadcn UI components
│   │   └── ...          # Feature components
│   ├── lib/             # Utilities and helpers
│   ├── hooks/           # Custom React hooks
│   ├── stores/          # Zustand stores
│   └── types/           # TypeScript types
├── public/              # Static assets
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

## Environment Variables

- `NEXT_PUBLIC_API_URL` - Backend API URL
- `NEXT_PUBLIC_APP_URL` - Frontend application URL
- `NEXT_PUBLIC_ENABLE_ANALYTICS` - Enable analytics (true/false)

## License

MIT License - See [LICENSE](../LICENSE)
