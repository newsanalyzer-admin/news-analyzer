# Story EVAL-4.4: Publish Pipeline & GitHub Actions Integration

## Status

Approved

## Story

**As an** AI evaluation engineer who has completed an evaluation run,
**I want** a single command that deploys results to noometric.com and a GitHub Actions workflow for CI deployment,
**so that** publishing evaluation results is fast, decoupled from the NewsAnalyzer Docker deployment, and automatable.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `./eval/publish.sh` assembles the static site + latest results data and deploys to noometric.com via wrangler CLI |
| AC2 | A GitHub Actions workflow (`publish-eval.yml`) can be triggered manually via workflow_dispatch to deploy results to Cloudflare Pages |
| AC3 | The `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` are the only secrets required for CI deployment |
| AC4 | `eval/README.md` documents the complete workflow: how to run an eval, review results locally, and publish |
| AC5 | The publish step is completely independent of the NewsAnalyzer Docker deployment pipeline — no Docker rebuild required |
| AC6 | Results are live on noometric.com within 60 seconds of the publish command completing |

## Tasks / Subtasks

- [ ] Task 1: Create `publish.sh` script (AC1, AC5, AC6)
  - [ ] Create `eval/publish.sh`
  - [ ] Resolve repo root via `REPO_ROOT=$(git -C "$(dirname "$0")" rev-parse --show-toplevel)`
  - [ ] Assemble build directory (works identically local and in CI — site files are in repo):
    - Copy site files (HTML, CSS, JS) from `$REPO_ROOT/noometric/` → `build/`
    - Copy `eval/reports/manifest.json` → `build/data/manifest.json`
    - For each eval type in manifest, find latest run and copy summary → `build/data/{eval-type}/summary.json`
    - Copy all run summaries for comparison page → `build/data/{eval-type}/runs/{run-id}/summary.json`
  - [ ] Run `wrangler pages deploy build/ --project-name noometric`
  - [ ] Print the deployed URL from wrangler output on success (wrangler prints this to stdout)
  - [ ] Clean up build directory after deploy
  - [ ] Add `--preview` flag: assembles build directory but serves locally via `npx serve build/` instead of deploying — allows reviewing the site with latest data before publishing

- [ ] Task 2: Create GitHub Actions workflow (AC2, AC3)
  - [ ] Create `.github/workflows/publish-eval.yml`
  - [ ] Trigger: `workflow_dispatch` (manual) with optional `note` input
  - [ ] No `--ci` flag needed — `publish.sh` reads from `$REPO_ROOT/noometric/` which is checked out by the workflow
  - [ ] Steps: checkout repo → setup Node.js 22 → `npm install -g wrangler` → `./eval/publish.sh`
  - [ ] Pass `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` as environment variables from repo secrets (AC3 — only these two secrets required)
  - [ ] Token permissions note: `CLOUDFLARE_API_TOKEN` needs only **Cloudflare Pages: Edit** scope — do not create an account-level token. Configure in Cloudflare dashboard → My Profile → API Tokens → Create Token → use "Edit Cloudflare Pages" template
  - [ ] Secrets setup: add both secrets in GitHub repo → Settings → Secrets and variables → Actions → New repository secret

- [ ] Task 3: Update `eval/README.md` (AC4)
  - [ ] `eval/README.md` already exists — this task updates it (do not create a new file)
  - [ ] Document the complete end-to-end workflow:
    1. Run an evaluation: `./eval/run-eval.sh {type}`
    2. Preview results locally: `./eval/publish.sh --preview`
    3. Publish to noometric.com: `./eval/publish.sh`
    4. Or publish via CI: trigger `publish-eval.yml` workflow in GitHub Actions
  - [ ] Document prerequisites: wrangler CLI installed globally (`npm install -g wrangler`), Cloudflare auth (`wrangler login` for local), Node.js
  - [ ] Document noometric site source location: `noometric/` directory in repo root
  - [ ] Document how to add a new eval type (reference `eval-configs.json` from EVAL-4.1)
  - [ ] Document repository secrets setup (CLOUDFLARE_API_TOKEN with Pages:Edit scope, CLOUDFLARE_ACCOUNT_ID)
  - [ ] Include troubleshooting section (common wrangler errors, auth issues, stale build directory)

- [ ] Task 4: Verify end-to-end flow (AC1, AC5, AC6)
  - [ ] Run `./eval/publish.sh --preview` and verify site renders correctly at localhost
  - [ ] Run `./eval/publish.sh` and verify noometric.com updates — confirm deployed URL in wrangler output
  - [ ] Verify the deploy does NOT trigger any NewsAnalyzer Docker workflows (check `.github/workflows/deploy-production.yml` has no trigger on eval/ or noometric/ paths)
  - [ ] Time the deploy and verify it completes within 60 seconds (AC6)
  - [ ] Trigger `publish-eval.yml` via workflow_dispatch in GitHub and verify deploy succeeds (requires secrets configured)

## Dev Notes

### Source Tree

| File | Purpose |
|------|---------|
| `noometric/` | Noometric site source — HTML, CSS, JS created by EVAL-4.2/4.3. Lives in NewsAnalyzer repo root (Option A decision — see below). |
| `eval/reports/manifest.json` | Central manifest from EVAL-4.1 |
| `eval/reports/{eval-type}/runs/` | Run history directories with summary JSONs |
| `eval/publish.sh` | New publish script (created by this story) |
| `.github/workflows/publish-eval.yml` | New publish workflow (created by this story) |
| `.github/workflows/deploy-production.yml` | Existing NewsAnalyzer deploy — must NOT be triggered by this story's changes |
| `.github/workflows/eval.yml` | Existing eval test workflow — no changes needed |

### Wrangler CLI

Wrangler is already installed globally (`wrangler 4.80.0`). The Cloudflare Pages project `noometric` already exists (created during landing page deployment). The deploy command is:

```bash
wrangler pages deploy {directory} --project-name noometric
```

For CI, authentication uses an API token via environment variable:

```bash
CLOUDFLARE_API_TOKEN=xxx wrangler pages deploy ...
```

### Build Directory Structure

The assembled build directory should mirror the URL structure of noometric.com:

```
build/
├── index.html                          # Landing page
├── styles.css                          # Shared styles
├── evaluations/
│   ├── index.html                      # Eval index page
│   ├── ner/
│   │   ├── index.html                  # NER detail page
│   │   └── compare/
│   │       └── index.html              # NER comparison page
│   └── bias/
│       ├── index.html                  # Bias detail page
│       └── compare/
│           └── index.html              # Bias comparison page
└── data/
    ├── manifest.json                   # Run manifest
    ├── ner/
    │   ├── summary.json                # Latest NER summary
    │   └── runs/
    │       └── 2026-03-25_00-00/
    │           └── summary.json        # Historical NER summary
    └── bias/
        ├── summary.json                # Latest bias summary
        └── runs/
            └── 2026-04-02_23-09/
                └── summary.json        # Historical bias summary
```

### Noometric Site Source Location (Option A)

The noometric.com site source files live in the `noometric/` directory at the NewsAnalyzer repo root. This was a deliberate architectural decision made during EVAL-4 story validation:

- **Why:** The CI environment (GitHub Actions on ubuntu-latest) has no access to `D:/VSCProjects/noometric.com/`. Moving site files into the repo eliminates the need for a separate `--ci` flag or deploy key — `publish.sh` reads from `$REPO_ROOT/noometric/` identically in both local and CI environments.
- **How:** EVAL-4.2 and EVAL-4.3 create their HTML/CSS/JS files under `noometric/` (not the external Windows path). The external `D:/VSCProjects/noometric.com/` directory is no longer the canonical source.
- **Local workflow:** Edit site files in `noometric/` within the repo. Run `./eval/publish.sh --preview` to preview. Commit changes to the repo before publishing.

### GitHub Actions Workflow Pattern

Follow the existing workflow patterns in `.github/workflows/`. The new `publish-eval.yml` should be minimal — no `--ci` flag needed because `publish.sh` reads from `$REPO_ROOT/noometric/` which the checkout step provides:

```yaml
name: Publish Evaluation Results
on:
  workflow_dispatch:
    inputs:
      note:
        description: 'Publish note (optional)'
        required: false
jobs:
  publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '22'
      - run: npm install -g wrangler
      - run: ./eval/publish.sh
        env:
          CLOUDFLARE_API_TOKEN: ${{ secrets.CLOUDFLARE_API_TOKEN }}
          CLOUDFLARE_ACCOUNT_ID: ${{ secrets.CLOUDFLARE_ACCOUNT_ID }}
```

**Wrangler output:** On success, wrangler prints the deployed URL to stdout (e.g., `https://noometric.pages.dev` or the custom domain). Capture and echo this in `publish.sh` for confirmation.

### Testing

- **Preview:** Run `./eval/publish.sh --preview`, verify site renders correctly at localhost with current eval data
- **Deploy:** Run `./eval/publish.sh`, confirm wrangler prints a deployed URL, verify noometric.com updates within 60 seconds (AC6)
- **CI:** Trigger `publish-eval.yml` via GitHub Actions → workflow_dispatch; verify deploy succeeds in the Actions log (requires `CLOUDFLARE_API_TOKEN` and `CLOUDFLARE_ACCOUNT_ID` secrets configured)
- **Independence:** Confirm `deploy-production.yml` is NOT triggered — check GitHub Actions tab after publish to ensure no Docker build starts
- **Data paths:** Verify the deployed site loads data from correct paths — check browser Network tab that `data/manifest.json`, `data/ner/summary.json`, and `data/bias/summary.json` all return 200
- **Data path contract:** Verify the build directory structure matches the fetch paths in EVAL-4.2 (`../data/{eval-type}/summary.json`) and EVAL-4.3 (`../../data/{eval-type}/runs/{run-id}/summary.json`)

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-04-06 | 1.0 | Initial story creation | Sarah (PO) |
| 2026-04-21 | 1.1 | PO validation fixes: resolved CI mode via Option A (noometric site files moved into repo under noometric/); merged --preview into Task 1; removed --ci flag complexity; added token permissions scope note; clarified Task 3 is an update not create; added wrangler URL output note; updated GH Actions YAML; expanded Testing section with data path contract verification | Sarah (PO) |

## Dev Agent Record

### Agent Model Used

_To be filled during implementation_

### Debug Log References

_To be filled during implementation_

### Completion Notes List

_To be filled during implementation_

### File List

_To be filled during implementation_

## QA Results

_To be filled after implementation_
