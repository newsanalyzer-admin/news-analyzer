# Story EVAL-4.3: Cross-Run Comparison Page

## Status

Review

## Story

**As an** AI evaluation engineer iterating on model performance,
**I want** a rendered comparison view that shows how metrics change across runs of the same evaluation type,
**so that** I can demonstrate the evaluate-diagnose-fix-re-evaluate loop with visible before/after evidence.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `/evaluations/{eval-type}/compare/` renders a timeline chart of P/R/F1 across all historical runs for that eval type |
| AC2 | A delta table shows metric changes between consecutive runs with color-coded indicators (green = improvement, red = regression) |
| AC3 | User can select any two runs from a dropdown for side-by-side comparison |
| AC4 | Comparison works for NER results (multi-branch, multi-extractor) |
| AC5 | Comparison works for bias results (grounded vs. ungrounded, per-distortion) |
| AC6 | Page degrades gracefully when only one run exists (shows current results, no delta, message indicating more runs needed) |
| AC7 | All data sourced from static JSON (manifest + individual run summaries) — no server-side computation |

## Tasks / Subtasks

- [x] Task 1: Build comparison page structure (AC1, AC7)
  - [x] Create `evaluations/ner/compare/index.html` — NER comparison page
  - [x] Create `evaluations/bias/compare/index.html` — Bias comparison page
  - [x] Each page is self-contained (separate file per eval type — consistent with EVAL-4.2's per-type page pattern; no shared template with URL parsing)
  - [x] Each page loads `../../data/manifest.json` via `fetch()`, filters by its eval type, and sorts runs by `timestamp` ascending before any delta computation
  - [x] Reuse `../../styles.css` and `../../evaluations/chart-utils.js` from EVAL-4.2
  - [x] Consistent dark theme styling with other eval pages

- [x] Task 2: Timeline chart (AC1, AC4, AC5)
  - [x] Chart.js line chart with X-axis = run dates, Y-axis = metric value (0-1)
  - [x] Separate lines for Precision, Recall, F1
  - [x] For NER: show aggregate metrics (across all branches) on the timeline, with a branch selector to drill down
  - [x] For bias: show grounded metrics on the timeline
  - [x] Point markers on each data point, hover tooltip with exact values
  - [x] Handle single-run case: show a single point, no connecting lines

- [x] Task 3: Delta table (AC2, AC4, AC5)
  - [x] Table rows: one per consecutive run pair (run N vs. run N-1)
  - [x] Columns: Run date, P (delta), R (delta), F1 (delta)
  - [x] Color coding: green text/bg for positive deltas (improvement), red for negative (regression), neutral for zero/negligible (<0.005)
  - [x] For NER: show per-branch deltas in an expandable row
  - [x] For bias: show per-distortion-type deltas in an expandable row
  - [x] Single-run case: show "Baseline — no prior run to compare" message

- [x] Task 4: Side-by-side run selector (AC3, AC4, AC5)
  - [x] Two dropdown selectors: "Run A" and "Run B"
  - [x] Populated from manifest entries for this eval type (sorted by timestamp ascending)
  - [x] On selection: load both run summaries on-demand via `fetch('../../data/{eval-type}/runs/{run.id}/summary.json')` (path contract with EVAL-4.4 — see Dev Notes)
  - [x] Render a visible error message if either on-demand fetch fails (e.g., "Run data unavailable for this selection")
  - [x] NER side-by-side: P/R/F1 per branch, per extractor, with delta column
  - [x] Bias side-by-side: aggregate P/R/F1, per-distortion breakdown, with delta column
  - [x] Delta column uses same green/red color coding as the delta table

- [x] Task 5: Graceful degradation (AC6)
  - [x] If only one run exists: show the current results, hide delta table, show message "Run additional evaluations to see comparison data"
  - [x] If no runs exist: show message "No evaluation runs found for this type"
  - [x] Side-by-side defaults to most recent two runs (or disables Run B dropdown if only one)

- [x] Task 6: Navigation integration
  - [x] Link from each eval detail page (EVAL-4.2) to its comparison page
  - [x] Breadcrumb navigation: Evaluations > {Eval Type} > Compare
  - [x] Back links to detail page and evaluations index

## Dev Notes

### Source Tree

Starting state (after EVAL-4.2 completes — this story's direct dependency):

```
noometric/                        ← repo root directory (NewsAnalyzer repo)
├── index.html
├── styles.css                    ← shared CSS variables
└── evaluations/
    ├── index.html                ← eval index page
    ├── chart-utils.js            ← shared Chart.js dark theme config
    ├── ner/
    │   └── index.html            ← NER detail page
    └── bias/
        └── index.html            ← Bias detail page
```

This story adds:

```
noometric/
└── evaluations/
    ├── ner/
    │   └── compare/
    │       └── index.html        ← NEW
    └── bias/
        └── compare/
            └── index.html        ← NEW
```

Asset paths from within `evaluations/{eval-type}/compare/index.html`:
- Shared CSS: `../../../styles.css`
- Chart utils: `../../../evaluations/chart-utils.js` → actually `../../chart-utils.js` (one level up from ner/ or bias/, then evaluations/)
- Manifest: `../../../data/manifest.json`

> **Path note:** From `evaluations/ner/compare/index.html`, the root is three levels up (`../../..`). Use `../../../styles.css` and `../../../data/manifest.json`. chart-utils.js is at `../../chart-utils.js` (in `evaluations/`).

### Data Sources

The comparison page reads from two data sources:

1. **`manifest.json`** — provides the list of runs, their timestamps, and inline summary metrics. Enough to render the timeline chart and delta table without loading individual run files. Filter by `evalType` field matching this page's eval type, then **sort by `timestamp` ascending** before any delta computation. Do not assume insertion order.

2. **Individual run summaries** — loaded on-demand when the user selects specific runs for side-by-side comparison. These contain the detailed breakdowns (per-branch for NER, per-distortion for bias).

**Data path contract (EVAL-4.3 ↔ EVAL-4.4):** The fetch paths are a binding interface with the publish script:

| Fetch path (from compare/ page) | Source in eval/reports/ |
|--------------------------------|------------------------|
| `../../../data/manifest.json` | `eval/reports/manifest.json` |
| `../../../data/{eval-type}/runs/{run.id}/summary.json` | `eval/reports/{eval-type}/runs/{run-dir}/summary.json` |

The `run.id` field in the manifest (e.g., `"baseline-2026-03-25_00-00"`) must match the run directory name used by EVAL-4.4 when copying files.

### NER Summary Shape (per run)

```json
{
  "branches": {
    "judicial": {
      "spaCy en_core_web_sm": { "precision": 0.192, "recall": 0.925, "f1": 0.318 },
      "Claude Sonnet": { "precision": 0.456, "recall": 0.938, "f1": 0.614 }
    }
  }
}
```

**NER aggregate metric for timeline:** Average P/R/F1 across all branches for the **Claude Sonnet extractor only** (not spaCy). This matches the primary metric used in EVAL-2 reporting and the `extract-summary.js` behavior in EVAL-4.1. Use the inline `summary` field from `manifest.json` for the timeline — it already contains the pre-computed Claude Sonnet average. Only load individual run summaries on-demand for the side-by-side branch breakdown.

### Bias Summary Shape (per run)

```json
{
  "aggregate": { "precision": 0.69, "recall": 0.90, "f1": 0.78 },
  "by_distortion_type": {
    "confirmation_bias": { "precision": 0.80, "recall": 1.0, "f1": 0.89 }
  }
}
```

### Chart.js Multi-Line Configuration

Reuse the shared chart utility from EVAL-4.2. For the timeline:
- Line colors: Precision = `#06b6d4` (teal), Recall = `#22d3ee` (light teal), F1 = `#f0f0f0` (white)
- Point radius: 4px, hover radius: 6px
- X-axis: date labels from manifest timestamps
- Y-axis: 0 to 1, step 0.1

### Color Coding for Deltas

| Delta | Color | Meaning |
|-------|-------|---------|
| > +0.005 | `#22c55e` (green) | Improvement |
| < -0.005 | `#ef4444` (red) | Regression |
| -0.005 to +0.005 | `#a0a0a0` (gray) | Negligible change |

Format deltas as "+0.05" or "-0.03" with the sign always shown.

### Testing

Manual visual testing — no automated test framework for static HTML pages.

**Local preview:** Serve the site directory with `npx serve noometric/` or use `./eval/publish.sh --preview` (from EVAL-4.4) once available.

**Functional checklist:**
- Timeline renders correctly with 1 run (single point, no connecting line), 2 runs (one line segment), and 3+ runs (full chart)
- Delta table shows correct color coding: green for >+0.005, red for <-0.005, gray for negligible
- Delta table rows are ordered by timestamp ascending (oldest → newest)
- Side-by-side selector populates from manifest and loads on-demand run summaries via correct fetch path
- Graceful degradation: single-run state shows current results and "Run additional evaluations to see comparison data" message
- No-run state shows appropriate empty message
- Run B dropdown is disabled (or hidden) when only one run exists
- Render a visible error message if an on-demand run summary fetch fails (test by temporarily pointing fetch to a non-existent path)

**NER-specific:**
- Timeline uses Claude Sonnet aggregate (average across branches), not spaCy
- Branch drill-down selector shows per-branch breakdown
- Side-by-side shows P/R/F1 per branch, per extractor (both spaCy and Claude Sonnet)

**Bias-specific:**
- Timeline uses `aggregate.f1` from the grounded bias manifest entry
- Side-by-side shows per-distortion-type breakdown

**Responsive layout:** Test at 375px, 768px, and 1080px+ widths. Verify timeline chart and delta table reflow correctly on mobile.

**Accessibility checklist:**
- Each `<canvas>` (timeline chart) has `aria-label` describing its content
- Run A and Run B `<select>` dropdowns have associated `<label>` elements
- Keyboard navigation: Tab cycles through dropdowns and any expandable rows
- Color is not the only indicator for delta direction — also show +/- sign in the value

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-04-06 | 1.0 | Initial story creation | Sarah (PO) |
| 2026-04-20 | 1.1 | PO validation fixes: resolved design decision (separate files per eval type, not shared template); added on-demand run fetch path and data path contract table; added Source Tree section with post-EVAL-4.2 state and asset paths; added manifest sort-order requirement; clarified NER aggregate uses Claude Sonnet only; expanded Testing section with functional, NER-specific, bias-specific, responsive, and accessibility checklists | Sarah (PO) |
| 2026-04-20 | 1.2 | Option A cascading update: replaced `npx serve D:/VSCProjects/noometric.com` with `npx serve noometric/`; Source Tree directory labels updated to `noometric/` | Sarah (PO) |

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

None

### Completion Notes List

- Task 6 (navigation integration) was already partially satisfied by EVAL-4.2 — the NER and bias detail pages both include `<a href="../compare/">` action links. Compare pages add breadcrumbs and back-links completing the full navigation loop.
- `makeTimelineConfig()` was added to `evaluations/chart-utils.js` (shared with EVAL-4.2 pages) rather than inlining it in each compare page — keeps chart config DRY.
- Compare-page CSS (run selector, delta classes, expandable rows, side-by-side table) added to shared `styles.css` so both pages stay markup-only.
- Expandable breakdown rows use native `<details>/<summary>` — zero JS needed for the toggle, keyboard accessible by default.
- Per-branch and per-distortion breakdown rows in the delta table load individual run summaries **lazily** (only on first expand), avoiding unnecessary fetches.
- **evalType naming dependency (EVAL-4.4):** NER compare page filters for `evalType === 'ner'`. Dev manifest uses `evalType: "baseline"` — NER page shows empty state locally (correct graceful degradation). EVAL-4.4 must rename `baseline` → `ner` in the deployed manifest.
- **run.id as directory name (EVAL-4.4):** Side-by-side fetches `data/{evalType}/runs/{run.id}/summary.json`. EVAL-4.4's publish script must use `run.id` as the directory name when copying run summaries (e.g., `data/ner/runs/baseline-2026-03-25_00-00/summary.json`).
- Bias compare page is fully testable in dev: manifest has 1 `bias` run, so single-run graceful degradation (timeline with 1 point, info banner, Run B disabled) is exercised. NER compare shows empty state (0 `ner` runs in dev manifest).

### File List

- `noometric/evaluations/ner/compare/index.html` — NER run comparison page (NEW)
- `noometric/evaluations/bias/compare/index.html` — Bias run comparison page (NEW)
- `noometric/evaluations/chart-utils.js` — added `makeTimelineConfig()` (MODIFIED)
- `noometric/styles.css` — added compare page styles: run selector, delta colors, expandable rows, side-by-side table (MODIFIED)

## QA Results

_To be filled after implementation_
