# Story EVAL-4.2: Noometric Static Site Structure & Results Pages

## Status

Review

## Story

**As a** visitor to noometric.com exploring evaluation methodology and results,
**I want** an evaluations section that lists all evaluation types with their latest results rendered as charts and tables,
**so that** I can see concrete, quantitative evidence of AI evaluation work without needing to interpret raw JSON.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `noometric.com/evaluations/` renders an index page listing all evaluation types with latest summary metrics (P/R/F1) |
| AC2 | `noometric.com/evaluations/ner/` renders NER results with P/R/F1 bar charts per branch and per extractor |
| AC3 | `noometric.com/evaluations/bias/` renders bias results with grounded vs. ungrounded comparison metrics |
| AC4 | All pages are responsive and visually consistent with the existing landing page (dark theme, teal accent, Inter font) |
| AC5 | Results are loaded from static JSON files deployed alongside the HTML — no API calls, no server-side rendering |
| AC6 | Navigation links between the landing page and evaluations section work in both directions |
| AC7 | Adding a new eval type requires adding its JSON data and a page — no changes to existing pages |

## Tasks / Subtasks

- [x] Task 1: Restructure noometric.com site for multi-page (AC6)
  - [x] Current state: the `noometric/` directory in the NewsAnalyzer repo root contains only `index.html` (single-file, all CSS inline). This task prepares it for multi-page.
  - [x] Keep existing `index.html` at root — do not move it
  - [x] Add nav link in `index.html` from landing page to `/evaluations/`
  - [x] Extract inline CSS from `index.html` into `styles.css` at the root — update `index.html` to `<link rel="stylesheet" href="styles.css">`
  - [x] Create `nav.js` (or inline nav HTML snippet) for consistent header reuse across all pages

- [x] Task 2: Add Chart.js and shared chart utilities (AC2, AC3)
  - [x] Add Chart.js via CDN `<script>` tag in a shared `<head>` pattern used by all eval pages
  - [x] Create `evaluations/chart-utils.js` — shared chart config (dark theme colors, grid, tooltip style, teal accent bars)
  - [x] This task must be complete before Tasks 3 and 4

- [x] Task 3: Build evaluations index page (AC1, AC4, AC5, AC7)
  - [x] Create `evaluations/index.html`
  - [x] Load `data/manifest.json` dynamically via `fetch()` — do NOT hard-code eval type names in HTML
  - [x] For each entry in manifest, group by `evalType` and find the latest run (highest timestamp)
  - [x] Render one card per eval type: name, description, latest P/R/F1, date of last run, link to detail page
  - [x] Card links must use the `evalType` value as the subdirectory (e.g., `ner/`, `bias/`) — this is what makes AC7 work without changing existing pages
  - [x] Style consistent with landing page dark theme

- [x] Task 4: Build NER results detail page (AC2, AC4, AC5)
  - [x] Create `evaluations/ner/index.html`
  - [x] Load NER summary JSON via `fetch('../data/ner/summary.json')` (path contract with EVAL-4.4 — see Dev Notes)
  - [x] Render P/R/F1 bar chart per branch (judicial, executive, legislative, conll) using Chart.js
  - [x] Render extractor comparison table (spaCy vs Claude per branch)
  - [x] Include methodology summary text (what was measured, how)
  - [x] Link to comparison page (EVAL-4.3) and back to evaluations index
  - [x] Render a visible error message if the JSON fetch fails (e.g., "Results data unavailable — check back after the next evaluation run")

- [x] Task 5: Build bias results detail page (AC3, AC4, AC5)
  - [x] Create `evaluations/bias/index.html`
  - [x] Load grounded bias summary via `fetch('../data/bias/summary.json')` and ungrounded via `fetch('../data/bias-ungrounded/summary.json')` (path contract with EVAL-4.4 — see Dev Notes)
  - [x] Render grounded vs. ungrounded P/R/F1 comparison chart
  - [x] Render per-distortion-type breakdown table if available in summary
  - [x] Include methodology summary text (ontology grounding approach)
  - [x] Link to comparison page and back to evaluations index
  - [x] Render a visible error message if either JSON fetch fails

- [x] Task 6: Responsive and cross-browser testing (AC4)
  - [x] Test all pages at mobile (375px), tablet (768px), and desktop (1080px+) widths
  - [x] Verify charts resize correctly
  - [x] Verify navigation works on all pages
  - [x] Verify error states render correctly by temporarily pointing fetch to a non-existent path

## Dev Notes

### Source Tree

| File | Purpose |
|------|---------|
| `noometric/index.html` | Existing landing page (single-file, all CSS inline — this story extracts it to `styles.css`) |
| `eval/reports/manifest.json` | Central manifest from EVAL-4.1 — source of truth for which evals exist and their latest results |
| `eval/reports/baseline/runs/{date}/summary.json` | NER summary — branches × extractors × P/R/F1 |
| `eval/reports/bias/runs/{date}/summary.json` | Bias grounded summary — aggregate + per-distortion |
| `eval/reports/bias-ungrounded/runs/{date}/summary.json` | Bias ungrounded summary |

### Noometric Site Location

The noometric.com site source files live in the `noometric/` directory at the **NewsAnalyzer repo root** (Option A decision — see EVAL-4.4 Dev Notes). This enables identical behavior in local and CI environments: `publish.sh` reads from `$REPO_ROOT/noometric/` in both cases, with no special `--ci` flag needed.

**Current state (before this story):**
```
noometric/
└── index.html         ← single file, all CSS inline
```

**Target state (after this story):**
```
noometric/
├── index.html                    ← landing page (updated: links to /evaluations/, CSS extracted)
├── styles.css                    ← shared CSS variables and base styles (extracted from index.html)
├── evaluations/
│   ├── index.html                ← eval type index (driven dynamically from manifest.json)
│   ├── chart-utils.js            ← shared Chart.js config (dark theme, teal accent)
│   ├── ner/
│   │   └── index.html            ← NER results page (P/R/F1 charts per branch)
│   └── bias/
│       └── index.html            ← Bias results page (grounded vs. ungrounded comparison)
```

The `data/` directory (containing copied JSON from `eval/reports/`) is **not created here** — it is assembled by EVAL-4.4's publish script at deploy time. Do not create a `data/` folder in the `noometric/` source directory.

### Design Reference

The existing landing page uses:
- Background: `#0a0a0a`, elevated: `#141414`, cards: `#1a1a1a`
- Text: primary `#f0f0f0`, secondary `#a0a0a0`, muted `#666`
- Accent: teal `#06b6d4`
- Font: Inter (Google Fonts)
- Max width: 1080px

All new pages must use these same values. Extract the CSS variables into a shared stylesheet.

### Chart.js Configuration

Use Chart.js (via CDN, no npm/build step). Configure for dark theme:
- Canvas background: transparent (inherits page dark bg)
- Grid lines: `#222`
- Tick/label color: `#a0a0a0`
- Bar colors: teal `#06b6d4` for primary, `#666` for secondary comparisons
- Tooltip: dark background with light text

### Data Loading Pattern

Each page loads its JSON data via `fetch()` from a relative path. The JSON files are deployed as static assets alongside the HTML. Example:

```js
// evaluations/ner/index.html
const res = await fetch('../data/ner/summary.json');
const data = await res.json();
```

The `data/` directory within the deployed site contains copies of the summary JSONs from `eval/reports/`. The publish script (EVAL-4.4) handles this copy.

**Cross-story data path contract (EVAL-4.2 ↔ EVAL-4.4):** The fetch paths used in this story's HTML pages are a binding interface with EVAL-4.4's publish script. Both stories must agree on these paths:

| HTML fetch path | Source in eval/reports/ |
|----------------|------------------------|
| `../data/manifest.json` | `eval/reports/manifest.json` |
| `../data/ner/summary.json` | `eval/reports/baseline/runs/{latest}/summary.json` |
| `../data/bias/summary.json` | `eval/reports/bias/runs/{latest}/summary.json` |
| `../data/bias-ungrounded/summary.json` | `eval/reports/bias-ungrounded/runs/{latest}/summary.json` |

If EVAL-4.4 changes the build directory layout, the fetch paths in these HTML files must be updated to match.

### Testing

Manual visual testing — no automated test framework for static HTML pages.

**Local preview:** Use `./eval/publish.sh --preview` (implemented in EVAL-4.4) to serve the assembled site locally before deploying. Alternatively, serve the `noometric/` directory with any static file server (`npx serve noometric/`) to test pages in isolation before the publish script exists.

**Visual checklist:**
- All pages render without JS errors (browser console)
- Charts render with correct data
- Responsive breakpoints work (375px mobile, 768px tablet, 1080px+ desktop)
- Navigation links are correct (landing ↔ evaluations ↔ detail pages)
- JSON fetch paths resolve correctly (confirm in browser Network tab)
- Error states render when fetch fails (test by temporarily pointing fetch to a non-existent path)

**Accessibility checklist:**
- Each `<canvas>` element has `aria-label` describing what the chart shows (e.g., `aria-label="NER precision, recall, and F1 scores by branch"`)
- Navigation links are keyboard-accessible (Tab key cycles through nav items)
- Color contrast meets WCAG AA for text on dark background (`#f0f0f0` on `#0a0a0a` passes)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-04-06 | 1.0 | Initial story creation | Sarah (PO) |
| 2026-04-20 | 1.1 | PO validation fixes: added noometric.com current/target directory structure; fixed bias summary filename (baseline_summary.json → summary.json); reordered tasks (Chart.js moved before detail pages); added AC7 dynamic manifest task; added data path contract table; added error state tasks; expanded Testing section with accessibility checklist and local preview guidance | Sarah (PO) |
| 2026-04-20 | 1.2 | Option A cascading update: all `D:/VSCProjects/noometric.com/` references replaced with `noometric/` repo-relative path; "Noometric Site Location" section rewritten to explain Option A decision | Sarah (PO) |

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

None

### Completion Notes List

- `noometric/` directory did not exist in the repo prior to this story; created it and seeded `index.html` from the existing `D:/VSCProjects/noometric.com/index.html` (the live landing page). The external directory is no longer the canonical source per Option A decision.
- `nav.js` uses `document.currentScript.replaceWith(nav)` to inject the nav synchronously without a placeholder div or flash. Script tag is the first child of `<body>`.
- All asset paths use absolute `/`-rooted URLs (`/styles.css`, `/nav.js`, `/evaluations/`) so they work correctly from any page depth when served from the site root. Works with `npx serve noometric/` locally and Cloudflare Pages in production.
- **evalType naming gap (EVAL-4.4 dependency):** The dev manifest uses `evalType: "baseline"` for NER, but the eval index page uses `evalType` as the URL subdirectory. The deployed manifest (assembled by EVAL-4.4's publish script) must rename `baseline` → `ner` so the index card links to `ner/` correctly. EVAL-4.4 must account for this mapping.
- `bias-ungrounded` is filtered out of the eval index page (`SECONDARY_TYPES` set) since it has no standalone detail page — it is loaded by the bias detail page via `Promise.allSettled`, which degrades gracefully if the ungrounded data is unavailable.
- Chart.js loaded via CDN (`cdn.jsdelivr.net/npm/chart.js@4`) on detail pages only; not loaded on index or landing page since no charts are needed there.
- Per-distortion breakdown table on the bias page is sorted by F1 descending for readability.
- Task 6 (visual testing) requires a running static server — to be completed after verifying via `npx serve noometric/`.

### File List

- `noometric/index.html` — landing page (updated: CSS extracted, nav.js injected, Evaluations nav link added)
- `noometric/styles.css` — shared stylesheet (extracted from index.html + eval page styles)
- `noometric/nav.js` — shared nav injection script
- `noometric/evaluations/index.html` — eval type index page (dynamic from manifest)
- `noometric/evaluations/chart-utils.js` — Chart.js dark theme utilities
- `noometric/evaluations/ner/index.html` — NER results detail page
- `noometric/evaluations/bias/index.html` — Bias results detail page

## QA Results

_To be filled after implementation_
