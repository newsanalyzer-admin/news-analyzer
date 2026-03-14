# First Production Deployment - Lessons Learned

**Date:** 2026-03-13
**Context:** First deployment of NewsAnalyzer V2 to Hetzner Cloud production server behind Cloudflare

---

## Issue 1: All API Calls Going to `localhost:8080`

### Symptom
Browser DevTools showed CORS errors on every API call. All requests were targeting `http://localhost:8080` instead of the production domain.

### Root Cause
Frontend code had hardcoded `localhost:8080` fallbacks throughout:
```typescript
// In hooks and components
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
```

### Key Lesson: `NEXT_PUBLIC_*` Env Vars Are Baked at Build Time

This is the single most important Next.js deployment concept:

- **`NEXT_PUBLIC_*` variables** are inlined into the JavaScript bundle during `next build`. Setting them in `docker-compose.yml` at runtime has **zero effect** on client-side code. The value must be present when `docker build` runs.
- **Non-prefixed variables** (e.g., `BACKEND_INTERNAL_URL`) are read at runtime on the server via `process.env`. These work in Docker Compose environment sections.

### Fix
Changed all client-side fallbacks from `'http://localhost:8080'` to `''` (empty string). An empty string means "same origin" — the browser makes requests to `https://newsanalyzer.org/api/...` which nginx proxies to the backend.

```typescript
// BEFORE (broken in production)
const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

// AFTER (works everywhere)
const API_BASE = process.env.NEXT_PUBLIC_API_URL || '';
```

For server-side Next.js rewrites, used a non-prefixed env var that Docker reads at runtime:
```javascript
// next.config.js — runs on the server, so non-NEXT_PUBLIC_ vars work
async rewrites() {
    const backendUrl = process.env.BACKEND_INTERNAL_URL || 'http://localhost:8080';
    return [{ source: '/api/:path*', destination: `${backendUrl}/api/:path*` }];
}
```

### Takeaway
> When deploying Next.js in Docker, never rely on `NEXT_PUBLIC_*` vars set at runtime.
> Either bake them at build time with `--build-arg`, or use relative URLs (empty string)
> and let your reverse proxy handle routing.

---

## Issue 2: Nginx `proxy_pass` Trailing Slash Strips the Path Prefix

### Symptom
API requests reached the backend but with the `/api/` prefix stripped (e.g., `/api/presidencies` arrived as `/presidencies`), causing 404s.

### Root Cause
```nginx
# WRONG — trailing slash strips the location prefix
location /api/ {
    proxy_pass http://backend/;
}

# RIGHT — no trailing slash preserves the full URI
location /api/ {
    proxy_pass http://backend;
}
```

### Key Lesson: Nginx URI Rewriting Rules
- `proxy_pass http://backend;` (no trailing slash) — forwards the **full original URI** (`/api/presidencies`)
- `proxy_pass http://backend/;` (with trailing slash) — **replaces** the matched `location` prefix with the proxy_pass path, so `/api/presidencies` becomes `/presidencies`

This is a subtle nginx behavior that catches many people. The trailing slash acts as a path replacement, not just a base URL.

### Takeaway
> Only add a trailing slash to `proxy_pass` when you intentionally want to strip the
> location prefix. For pass-through proxying, omit it.

---

## Issue 3: Browser Native Login Popup (HTTP Basic Auth)

### Symptom
Every page load triggered the browser's native username/password dialog.

### Root Cause
Spring Security's production config required authentication on all requests and used HTTP Basic auth:
```java
.anyRequest().authenticated()
.httpBasic(basic -> basic.realmName("NewsAnalyzer"))
```

When the backend returns HTTP 401 with a `WWW-Authenticate: Basic` header, the browser is **required by the HTTP spec** to show a native login dialog. This isn't a bug — it's the protocol working as designed.

### Key Lesson: HTTP Basic Auth and Browsers
- HTTP Basic auth is a protocol-level mechanism, not an application feature
- The `WWW-Authenticate: Basic` response header triggers browser UI you can't style or suppress
- For web apps, prefer token-based auth (JWT) or session cookies — these give you control over the UX
- If you don't have auth implemented yet, use `permitAll()` rather than a locked-down placeholder

### Fix
Changed production SecurityFilterChain to `permitAll()` until JWT auth is implemented:
```java
@Profile("!dev & !test")
public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
}
```

---

## Issue 4: `@CrossOrigin` Annotations Blocking Production POST Requests

### Symptom
GET requests worked, but POST requests (e.g., triggering a sync) returned `403 Forbidden` in the browser. Curl from the server worked fine.

### Root Cause
Every controller had:
```java
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
```

Modern browsers send an `Origin` header even on **same-origin POST requests** from `fetch()`. Spring's `DefaultCorsProcessor` sees the `Origin: https://newsanalyzer.org` header, checks it against the allowed origins list (`localhost:3000`, `localhost:3001`), doesn't find a match, and returns "Invalid CORS request" (403).

### Key Lesson: How CORS Actually Works

1. **CORS is enforced by the browser**, not the server. That's why `curl` worked — curl doesn't do CORS checks.
2. **Browsers send `Origin` on same-origin POSTs** from `fetch()`/`XMLHttpRequest`. This is a security feature, not a bug.
3. **`@CrossOrigin` on controllers overrides global CORS config.** Even if you disable CORS in `SecurityConfig`, controller-level annotations still trigger Spring's CORS processor.
4. **In a reverse-proxy architecture (nginx → backend), you don't need CORS at all.** The browser talks to nginx (same origin), and nginx talks to the backend (server-to-server, no CORS). Controller-level CORS annotations are only useful when the browser talks directly to the backend (e.g., `localhost:3000` → `localhost:8080` during local dev without a proxy).

### Debugging Technique
Adding the `Origin` header to curl reproduces the browser's behavior:
```bash
# Without Origin header — works (no CORS processing)
curl -X POST http://localhost/api/admin/sync/presidencies -v
# → 202 Accepted

# With Origin header — fails (triggers CORS processing)
curl -X POST http://localhost/api/admin/sync/presidencies \
  -H "Origin: https://newsanalyzer.org" -v
# → 403 "Invalid CORS request"
```

### Fix
Removed all `@CrossOrigin` annotations from controllers. With `cors.disable()` in SecurityConfig and no controller-level annotations, Spring does no CORS processing at all.

---

## Issue 5: Docker Compose `--env-file` and Container Lifecycle

### Symptom
After restarting Docker Compose, Redis went into a crash loop and all services failed. Environment variable warnings appeared for `DB_PASSWORD`, `REDIS_PASSWORD`, `JWT_SECRET`.

### Root Cause
Running `docker compose restart` or `docker compose up -d` without `--env-file .env` when the `.env` file is not in the current working directory. Docker Compose defaults to looking for `.env` in the **current directory**, not the compose file's directory.

### Key Lesson: Docker Compose Environment Loading
```bash
# WRONG — looks for .env in current directory (might not exist)
docker compose -f deploy/production/docker-compose.yml up -d

# RIGHT — explicitly point to the env file
docker compose --env-file .env -f deploy/production/docker-compose.yml up -d
```

Also: `docker compose restart` does NOT re-read environment variables or pull new images. Use `up -d` to apply config changes, and `--force-recreate` when the image has been updated but compose doesn't detect a change.

### Takeaway
> Always use `--env-file` explicitly. Always use `up -d --force-recreate` after
> pulling new images. Never rely on `restart` for applying changes.

---

## Issue 6: Stale Browser Cache Masking Fix

### Symptom
After deploying fixes, the site still showed "Error. Page cannot be displayed" in DuckDuckGo browser, but worked fine in Edge.

### Key Lesson
Browsers (especially privacy-focused ones like DuckDuckGo) may aggressively cache error pages. Always test in a fresh browser/incognito window after deploying fixes before assuming the fix didn't work.

---

## General Deployment Debugging Checklist

1. **Is the right image running?**
   ```bash
   docker image inspect <image>:latest --format '{{.Created}}'
   ```
   Compare the timestamp with when CI completed.

2. **Is the request reaching the backend?**
   Check both nginx and backend logs:
   ```bash
   docker logs newsanalyzer-nginx 2>&1 | tail -20
   docker logs newsanalyzer-backend 2>&1 | tail -20
   ```
   If it's in nginx logs but not backend logs, something is rejecting it before the controller.

3. **Is it a CORS issue?**
   Test with curl, adding the `Origin` header to simulate browser behavior:
   ```bash
   curl -X POST http://localhost/api/endpoint -H "Origin: https://yourdomain.com" -v
   ```

4. **Is the env file loaded?**
   ```bash
   docker compose --env-file .env -f <compose-file> config
   ```
   This shows the resolved config with all variables substituted.

5. **Is the container actually recreated?**
   Look for "Recreated" (not "Running") in the `up -d` output. Use `--force-recreate` if needed.
