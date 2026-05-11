# Epic EVAL-4: Evaluation Results Pipeline

## Epic Overview

| Field | Value |
|-------|-------|
| **Epic ID** | EVAL-4 |
| **Epic Name** | Evaluation Results Pipeline |
| **Track** | AI Evaluation |
| **Epic Type** | Infrastructure — Evaluation Publishing & Comparison |
| **Priority** | HIGHEST (Unblocks all future evaluation and training work) |
| **Status** | APPROVED |
| **Created** | 2026-04-06 |
| **Owner** | Sarah (PO) |
| **Depends On** | EVAL-2 Complete, EVAL-3 Complete |
| **Blocked By** | None |
| **Estimated Effort** | ~1 week (4 stories, all required) |

## Executive Summary

Build an automated pipeline that runs evaluations, preserves run history, publishes results to noometric.com (Cloudflare Pages), and renders cross-run comparisons. This replaces the current manual workflow where results are committed to git, require a full Docker production deploy, and overwrite previous runs.

### Problem Statement

> The current evaluation publishing workflow has three critical bottlenecks:
>
> 1. **Manual and undocumented** — Running an evaluation requires multiple manual steps (run Promptfoo, run `build-api-json.js`, commit JSON, git tag, wait for Docker rebuild). There is no single command.
> 2. **No run history** — Each evaluation overwrites previous results. When a model is fine-tuned and re-evaluated, the baseline comparison is lost.
> 3. **Publishing requires a full production deploy** — Updating evaluation numbers on newsanalyzer.org requires triggering the entire Docker build pipeline on Hetzner. This is disproportionately heavy for what is fundamentally a static file update.

### Why This Epic Matters

- **Unblocks MT-1 (SLM Fine-tuning)** — The fine-tuning epic requires visible before/after comparison. Without run history and a comparison view, the fine-tuning work "disappears into a Jupyter notebook nobody sees."
- **Operationalizes evaluation** — A reproducible, automated eval pipeline is itself a portfolio-worthy skill. It demonstrates thinking about reproducibility, reporting, and operationalizing evaluation — exactly what employers look for.
- **Content engine for noometric.com** — Every new evaluation becomes a live proof point on the site. The eval pipeline is the content engine for the most important section of the landing page.
- **Publish target is noometric.com** — Results move from newsanalyzer.org to noometric.com (Cloudflare Pages), aligning with the Noometric consulting brand.

## Current State

### Existing Evaluation Results

| Evaluation | Location | Format | Date |
|-----------|----------|--------|------|
| EVAL-2 NER Baseline (4 branches) | `eval/reports/baseline/` | `{branch}_results.json`, `{branch}_api.json`, `summary.json` | 2026-03-25 |
| EVAL-3 Bias Grounded | `eval/reports/bias/` | `baseline_results.json`, `baseline_summary.json` | 2026-04-02 |
| EVAL-3 Bias Ungrounded | `eval/reports/bias-ungrounded/` | `baseline_results.json`, `baseline_summary.json` | 2026-04-02 |

### Current Workflow (Manual)

```
1. Run Promptfoo locally (manual, per-evaluation config)
2. Run `node eval/reports/build-api-json.js` (transforms NER results only)
3. Commit JSON files to git
4. Git tag (v*) → triggers deploy-production.yml
5. Docker rebuilds entire stack → deploys to Hetzner
6. Nginx serves JSON statically at /api/eval/results
7. Frontend fetches via React Query → renders at /evaluation
```

### Current GH Actions Workflow (`eval.yml`)

The existing `eval.yml` runs scorer unit tests, extractor tests, and gold dataset validation on push to `eval/**`. It does **not** run evaluations or publish results.

## Architecture Decisions

1. **Cloudflare Pages as publish target** — Static site on Cloudflare Pages at noometric.com. Results are static JSON/HTML — no server, no Docker, no API framework needed.
2. **Generic run manifest** — A `manifest.json` tracks all runs across all evaluation types (NER, bias, future evals). Each run is timestamped and tagged with its evaluation type.
3. **Wrangler CLI for deployment** — `wrangler pages deploy` pushes the static site content. Can be run locally or from GitHub Actions.
4. **Evaluation-agnostic** — The pipeline handles any Promptfoo-based evaluation. New eval types (e.g., from MT-1 fine-tuning) are added by registering a new eval config, not by modifying pipeline code.
5. **Run history as directories** — Results stored as `eval/reports/{eval-type}/runs/{YYYY-MM-DD_HH-MM}/`. A `latest` symlink or manifest pointer identifies the most recent run per eval type.

## Stories

---

### EVAL-4.1: Run Script & Run History Infrastructure

**Goal:** Create a single entry-point script that runs any evaluation, captures results with a timestamp, and maintains a manifest of all runs.

**Scope:**
- `eval/run-eval.sh` — Accepts eval type as argument (e.g., `baseline`, `bias`, `bias-ungrounded`), runs the corresponding Promptfoo config, transforms results, saves to timestamped directory
- Run history directory structure: `eval/reports/{eval-type}/runs/{YYYY-MM-DD_HH-MM}/`
- `eval/reports/manifest.json` — Central manifest tracking all runs: eval type, timestamp, summary metrics, status
- Migrate existing results into the new directory structure as the initial historical entries
- Transform scripts generalized to handle both NER (`build-api-json.js`) and bias result formats

**Acceptance Criteria:**
1. `./eval/run-eval.sh baseline` runs the NER evaluation across all branches, transforms results, and saves to a timestamped directory
2. `./eval/run-eval.sh bias` runs the bias evaluation, transforms results, and saves to a timestamped directory
3. Each run creates a directory under `eval/reports/{eval-type}/runs/{YYYY-MM-DD_HH-MM}/` containing raw results, transformed results, and a run summary
4. `eval/reports/manifest.json` is updated after each run with the new entry
5. Existing results (2026-03-25 NER, 2026-04-02 bias) are migrated into the new directory structure
6. A new eval type can be added by creating a Promptfoo config and a transform script — no changes to the run infrastructure required

**Story Points:** 5

---

### EVAL-4.2: Noometric Static Site Structure & Results Pages

**Goal:** Build the static site structure on noometric.com that renders evaluation results from the manifest and individual run data.

**Scope:**
- Extend the existing noometric.com landing page into a multi-page static site (still plain HTML/CSS/JS, no framework)
- `/evaluations/` index page — Lists all evaluation types with latest results summary, sourced from `manifest.json`
- `/evaluations/{eval-type}/` — Detail page for an eval type showing the latest run results with charts (P/R/F1 bar charts, entity-type breakdowns)
- Results data loaded from static JSON files deployed alongside the HTML
- Responsive, dark theme consistent with existing landing page design (teal accent, Inter font)
- Navigation between landing page and evaluations

**Acceptance Criteria:**
1. `noometric.com/evaluations/` renders an index of all evaluation types with latest summary metrics
2. `noometric.com/evaluations/ner/` renders NER results with P/R/F1 charts per branch and extractor
3. `noometric.com/evaluations/bias/` renders bias results with grounded vs. ungrounded comparison
4. All pages are responsive and visually consistent with the existing landing page
5. Results are loaded from static JSON — no API calls, no server-side rendering
6. Adding a new eval type requires adding its JSON data and a page template — no changes to existing pages

**Story Points:** 5

---

### EVAL-4.3: Cross-Run Comparison Page

**Goal:** Build a rendered comparison view that shows how metrics change across runs of the same evaluation type.

**Scope:**
- `/evaluations/{eval-type}/compare/` — Comparison page showing metrics across all historical runs
- Timeline chart showing P/R/F1 trends over time
- Run-to-run delta table (e.g., "+0.05 precision", "-0.02 recall") with color coding (green = improvement, red = regression)
- Run selector to compare any two specific runs side-by-side
- Data sourced from manifest + individual run summaries

**Acceptance Criteria:**
1. `/evaluations/{eval-type}/compare/` renders a timeline chart of P/R/F1 across all runs for that eval type
2. A delta table shows metric changes between consecutive runs with color-coded improvement/regression indicators
3. User can select any two runs for side-by-side comparison
4. Comparison works for both NER (multi-branch, multi-extractor) and bias (grounded vs. ungrounded) result formats
5. Page degrades gracefully when only one run exists (shows results, no delta)
6. All data sourced from static JSON — no server-side computation

**Story Points:** 5

---

### EVAL-4.4: Publish Pipeline & GitHub Actions Integration

**Goal:** Automate the publish step so that running an evaluation and deploying results to noometric.com is a single workflow.

**Scope:**
- `eval/publish.sh` — Copies results + static site to a build directory, deploys to Cloudflare Pages via `wrangler pages deploy`
- GitHub Actions workflow (`publish-eval.yml`) — Triggered manually (workflow_dispatch) or after `run-eval.sh` completes. Deploys to Cloudflare Pages using `CLOUDFLARE_API_TOKEN` secret.
- Local workflow: `run-eval.sh` → `publish.sh` (two commands, fully offline capable)
- CI workflow: push results to repo → GH Action deploys to Cloudflare Pages
- Documentation: `eval/README.md` updated with the complete evaluation workflow (run, review, publish)

**Acceptance Criteria:**
1. `./eval/publish.sh` deploys the static site with latest results to noometric.com via wrangler CLI
2. A GitHub Actions workflow (`publish-eval.yml`) can be triggered manually to deploy results to Cloudflare Pages
3. The `CLOUDFLARE_API_TOKEN` is the only secret required for CI deployment
4. `eval/README.md` documents the complete workflow: how to run an eval, review results locally, and publish
5. The publish step is independent of the NewsAnalyzer Docker deployment pipeline — no Docker rebuild required
6. Results are live on noometric.com within 60 seconds of publish command completing

**Story Points:** 3

---

## Story Summary

| Story | Title | Points | Dependencies |
|-------|-------|--------|--------------|
| EVAL-4.1 | Run Script & Run History Infrastructure | 5 | None |
| EVAL-4.2 | Noometric Static Site Structure & Results Pages | 5 | EVAL-4.1 (needs manifest + run data format) |
| EVAL-4.3 | Cross-Run Comparison Page | 5 | EVAL-4.2 (needs site structure), EVAL-4.1 (needs run history) |
| EVAL-4.4 | Publish Pipeline & GitHub Actions Integration | 3 | EVAL-4.2 (needs site to deploy) |

**Total: 18 story points, ~1 week**

**Recommended execution order:** EVAL-4.1 → EVAL-4.2 → EVAL-4.3 and EVAL-4.4 in parallel

## Out of Scope

- **Removing newsanalyzer.org/evaluation** — The existing frontend page stays as-is. It may continue to serve EVAL-2 data. No migration or removal work in this epic.
- **Automated eval runs in CI** — This epic supports manual triggering of evaluations. Automatic re-evaluation on code changes is a future enhancement.
- **User authentication** — noometric.com is public. No login or access control.
- **Database or API backend** — Everything is static files. If the site grows to need dynamic content, that's a future architecture decision.

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Cloudflare Pages free tier limits | Low — free tier allows 500 deploys/month, 1 build at a time | More than sufficient for manual eval publishes |
| Wrangler CLI changes | Low | Pin wrangler version in package.json and GH Actions |
| Chart rendering without a framework | Medium — vanilla JS charting can get complex | Use a lightweight charting library (Chart.js or similar) rather than building from scratch |
| Result format divergence across eval types | Medium | Manifest schema enforces common summary fields; eval-specific data is nested under a known key |

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-04-06 | 1.0 | Initial epic creation | Sarah (PO) |
| 2026-04-06 | 1.1 | Epic APPROVED — ready for story creation | Sarah (PO) |
