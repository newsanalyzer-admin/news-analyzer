# Story EVAL-4.1: Run Script & Run History Infrastructure

## Status

Done

## Story

**As an** AI evaluation engineer running multiple evaluation experiments over time,
**I want** a single command that runs any evaluation, saves results to a timestamped directory, and maintains a manifest of all runs,
**so that** every evaluation is reproducible, immutable, and addressable — enabling before/after comparison across experiments.

## Acceptance Criteria

| # | Criterion |
|---|-----------|
| AC1 | `./eval/run-eval.sh baseline` runs the NER evaluation across all branches, transforms results, and saves to a timestamped directory |
| AC2 | `./eval/run-eval.sh bias` runs the bias (grounded) evaluation, transforms results, and saves to a timestamped directory |
| AC3 | `./eval/run-eval.sh bias-ungrounded` runs the ungrounded bias evaluation similarly |
| AC4 | Each run creates a directory under `eval/reports/{eval-type}/runs/{YYYY-MM-DD_HH-MM}/` containing raw Promptfoo output, transformed summary, and a run metadata file |
| AC5 | `eval/reports/manifest.json` is created/updated after each run with: eval type, timestamp, directory path, and summary metrics (P/R/F1) |
| AC6 | Existing results (2026-03-25 NER baseline, 2026-04-02 bias grounded, 2026-04-02 bias ungrounded) are migrated into the new directory structure as historical entries in the manifest |
| AC7 | A new eval type can be added by creating a Promptfoo config file and an optional transform script — no changes to run-eval.sh or manifest logic required |
| AC8 | Running the same eval type twice produces two separate timestamped directories and two manifest entries |

## Tasks / Subtasks

- [x] Task 1: Design the run history directory structure and manifest schema (AC4, AC5, AC7)
  - [x] Define directory layout: `eval/reports/{eval-type}/runs/{YYYY-MM-DD_HH-MM}/`
  - [x] Define manifest.json schema:
    ```json
    {
      "version": 1,
      "runs": [
        {
          "id": "baseline-2026-03-25_00-00",
          "evalType": "baseline",
          "timestamp": "2026-03-25T00:00:00Z",
          "directory": "baseline/runs/2026-03-25_00-00",
          "config": "promptfooconfig.yaml",
          "summary": { "precision": 0.xx, "recall": 0.xx, "f1": 0.xx },
          "status": "complete"
        }
      ]
    }
    ```
  - [x] Define per-run metadata file (`run-meta.json`): eval type, timestamp, config used, git commit hash, duration

- [x] Task 2: Create eval type registry (AC7)
  - [x] Create `eval/eval-configs.json` mapping eval type names to their Promptfoo config and transform script:
    ```json
    {
      "baseline": {
        "config": "promptfooconfig.yaml",
        "transform": "reports/build-api-json.js",
        "summarize": null,
        "description": "NER entity extraction — spaCy vs Claude across gov branches"
      },
      "bias": {
        "config": "promptfoo-bias.yaml",
        "transform": null,
        "summarize": "reports/bias/scripts/summarize_results.py",
        "description": "Cognitive bias detection — ontology-grounded"
      },
      "bias-ungrounded": {
        "config": "promptfoo-bias-ungrounded.yaml",
        "transform": null,
        "summarize": "reports/bias/scripts/summarize_results.py",
        "description": "Cognitive bias detection — ungrounded baseline"
      }
    }
    ```

- [x] Task 3: Create `run-eval.sh` (AC1, AC2, AC3, AC4, AC5, AC8)
  - [x] Accept eval type as argument, validate against `eval-configs.json`
  - [x] Create timestamped run directory
  - [x] Run Promptfoo with the mapped config, output to the run directory
  - [x] Run transform/summarize script if configured
  - [x] Generate `run-meta.json` in the run directory (timestamp, git commit, config, duration)
  - [x] Generate or extract summary metrics (P/R/F1) for the manifest entry
  - [x] Append entry to `eval/reports/manifest.json`
  - [x] Print summary to console on completion

- [x] Task 4: Generalize transform/summarize scripts (AC1, AC2, AC7)
  - [x] Refactor `build-api-json.js` to accept an output directory argument (currently hardcoded to `baseline/`)
  - [x] Verify `summarize_results.py` already accepts `--input`/`--output` args (it does — no changes expected)
  - [x] Create a generic summary extractor for manifest entries that works across eval types (extract P/R/F1 from either NER summary.json or bias summary format)

- [x] Task 5: Migrate existing results (AC6)
  - [x] Move `eval/reports/baseline/*.json` → `eval/reports/baseline/runs/2026-03-25_00-00/`
  - [x] Move `eval/reports/bias/baseline_*.json` → `eval/reports/bias/runs/2026-04-02_23-09/`
  - [x] Move `eval/reports/bias-ungrounded/baseline_*.json` → `eval/reports/bias-ungrounded/runs/2026-04-02_23-09/`
  - [x] Create `run-meta.json` for each migrated run (timestamp from the results file `generated` field)
  - [x] Seed `eval/reports/manifest.json` with entries for all 3 migrated runs
  - [x] Verify migrated data is still readable by existing `build-api-json.js` (backward compat check)

- [x] Task 6: Tests (all ACs)
  - [x] Test manifest.json schema validation (valid entries, required fields) — `tests/test-manifest.js` (8 tests)
  - [x] Test run-eval.sh with a mock/dry-run mode (verify directory creation, manifest update, without actually running Promptfoo) — `tests/test-run-dryrun.sh` (7 tests)
  - [x] Test eval-configs.json validation (unknown eval type rejected, required fields present) — `tests/test-eval-configs.js` (11 tests)
  - [x] Test summary extraction works for both NER and bias result formats — `tests/test-extract-summary.js` (9 tests)

## Dev Notes

### Source Tree

Key files to understand before implementing:

| File | Purpose |
|------|---------|
| `eval/package.json` | npm scripts: `eval`, `eval:all`, `eval:legislative`, etc. |
| `eval/promptfooconfig.yaml` | NER eval config — uses `datasets/gold/{branch}.yaml`, providers: spaCy + Claude |
| `eval/promptfoo-bias.yaml` | Bias eval config — `outputPath: reports/bias/baseline_results.json` |
| `eval/promptfoo-bias-ungrounded.yaml` | Ungrounded bias config — `outputPath: reports/bias-ungrounded/baseline_results.json` |
| `eval/reports/build-api-json.js` | Transforms raw NER Promptfoo output → `{branch}_api.json` + `summary.json`. Hardcoded to `baseline/` dir. |
| `eval/reports/bias/scripts/summarize_results.py` | Transforms raw bias output → `summary.json`. Already accepts `--input`/`--output` args. |
| `eval/reports/baseline/summary.json` | NER summary — keyed by branch, then by extractor, with P/R/F1 |
| `eval/reports/bias/baseline_results.json` | Raw Promptfoo output for grounded bias eval |

### NER Eval Specifics

The NER baseline evaluation runs per-branch (`judicial`, `executive`, `legislative`, `conll`). The `promptfooconfig.yaml` defaults to `legislative` but `npm run eval:all` runs all branches. Each branch produces a separate `{branch}_results.json`. The `build-api-json.js` script then generates `{branch}_api.json` (frontend-ready) and a combined `summary.json`.

For `run-eval.sh`, the NER baseline should run all branches in a single invocation (equivalent to `npm run eval:all`).

### Bias Eval Specifics

The bias evals each produce a single `baseline_results.json` via the Promptfoo `outputPath` directive. The `summarize_results.py` script then generates a `baseline_summary.json`. Note: the `outputPath` in the Promptfoo configs will need to be overridden or parameterized to write to the timestamped run directory instead of the hardcoded path.

### Manifest Design Rationale

The manifest is the single source of truth for the comparison page (EVAL-4.3). It must contain enough summary data to render an index and timeline without loading every individual run's full results. Keep it flat — one array of runs, each with inline summary metrics.

### Testing

| Area | Framework | Location |
|------|-----------|----------|
| Shell script logic | bats or manual validation | `eval/tests/` |
| Manifest schema | pytest or node test | `eval/tests/` |
| Summary extraction | pytest | `eval/tests/` |

Testing the actual Promptfoo evaluation run is out of scope — those are integration tests that require the reasoning service. The unit tests here validate the infrastructure: directory creation, manifest updates, summary extraction, config validation.

## Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2026-04-06 | 1.0 | Initial story creation | Sarah (PO) |

## Dev Agent Record

### Agent Model Used

claude-sonnet-4-6

### Debug Log References

None

### Completion Notes List

- Tasks 1-5 were already complete from prior session; Task 6 (tests) implemented in this session
- 28 Node.js unit tests (node:test) + 7 shell tests all passing
- `tests/test-run-dryrun.sh` creates/restores manifest backup via trap EXIT — safe to re-run
- Original result files at `eval/reports/baseline/`, `eval/reports/bias/`, and `eval/reports/bias-ungrounded/` are intentionally retained alongside the new `runs/` directories. The existing `newsanalyzer.org/evaluation` frontend page is explicitly out of scope for this epic and continues to read from these top-level files. This is not a migration gap.

### File List

- `eval/run-eval.sh` — main entry-point script (created)
- `eval/eval-configs.json` — eval type registry mapping names to configs and transform scripts (created)
- `eval/scripts/update-manifest.js` — appends a run entry to manifest.json (created)
- `eval/scripts/extract-summary.js` — extracts P/R/F1 summary from NER and bias result formats (created)
- `eval/scripts/migrate-existing-results.js` — seeds manifest with historical runs (created)
- `eval/reports/manifest.json` — central run manifest, seeded with 3 historical entries (created)
- `eval/package.json` — added test, test:shell, test:all scripts (modified)
- `eval/tests/test-manifest.js` — 8 tests for update-manifest.js (created)
- `eval/tests/test-eval-configs.js` — 11 tests for eval-configs.json structure and lookup validation (created)
- `eval/tests/test-extract-summary.js` — 9 tests for extract-summary.js (NER and bias formats) (created)
- `eval/tests/test-run-dryrun.sh` — 7 shell tests for run-eval.sh --dry-run mode (created)

## QA Results

### Review Date: 2026-04-20

### Reviewed By: Quinn (Test Architect)

### Code Quality Assessment

Solid infrastructure implementation. All 35 tests pass (28 Node.js unit + 7 shell). The code is well-structured, defensive, and appropriately scoped. `run-eval.sh` uses `set -euo pipefail` correctly, handles duplicate timestamps, and captures git commit hash. `update-manifest.js` and `extract-summary.js` are clean, single-responsibility scripts with good input validation. Test isolation is excellent — subprocess-based tests, temp directories, manifest backup/restore via `trap EXIT`. The `--dry-run` mode was a smart testability design decision.

### Refactoring Performed

None required. Code is clean and implementation matches story intent.

### Compliance Check

- Coding Standards: ✓ Shell scripts follow defensive practices (set -euo pipefail, die() helper, info() helper)
- Project Structure: ✓ Files placed correctly under eval/scripts/, eval/tests/, eval/reports/
- Testing Strategy: ✓ Appropriate level (unit/CLI subprocess) — integration tests against live Promptfoo explicitly out of scope per story
- All ACs Met: ✓ AC1–AC7 verified; AC8 implemented in code (counter suffix logic) but not covered by an explicit test case

### Improvements Checklist

- [ ] **`run-eval.sh` line 163**: Replace `python` with `python3` for cross-platform portability. On systems where only `python3` is in PATH (Linux, some macOS configurations), the summarize step will fail silently. Low risk on this Windows dev setup but worth fixing before any CI usage.
- [ ] **AC8 test gap**: Add a dry-run test that runs `run-eval.sh` twice in the same minute and asserts the second run creates a `-1` suffixed directory (e.g., `2026-04-20_23-44-1/`). The counter logic exists and works, but is untested.
- [ ] **`extract-summary.js` hardcoded extractor key**: The string `'Claude Sonnet'` (line 43) will break if the extractor name changes in `promptfooconfig.yaml`. Low risk now, but when MT-1 adds new extractors, update this to read the extractor name from eval-configs.json or accept it as a parameter.

### Security Review

No concerns. The eval-type argument is validated against the `eval-configs.json` registry before any shell expansion, preventing injection. All file paths are constructed from validated inputs. No secrets or credentials handled.

### Performance Considerations

Not applicable — this is a developer CLI tool, not a latency-sensitive service. Script overhead is negligible (4 node process spawns for JSON parsing). The 4-subprocess approach for parsing eval config fields (lines 88–91) could be combined into one node call for efficiency, but this is cosmetic for a manual tool.

### Files Modified During Review

None.

### Gate Status

Gate: PASS → docs/qa/gates/EVAL-4.1-run-script-run-history.yml
Risk profile: N/A (low-risk infrastructure story)
NFR assessment: Inline above

### Recommended Status

✓ Ready for Done — three minor improvements logged above for future sprints; none are blocking.
