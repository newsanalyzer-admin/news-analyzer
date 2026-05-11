#!/usr/bin/env bash
# =============================================================================
# test-run-dryrun.sh — Shell tests for run-eval.sh --dry-run mode
#
# Verifies that --dry-run creates the run directory, writes run-meta.json with
# dryRun: true, and updates manifest.json — without executing Promptfoo.
#
# Cleans up all created artifacts and restores manifest on exit.
#
# Run: bash eval/tests/test-run-dryrun.sh
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EVAL_DIR="${SCRIPT_DIR}/.."
MANIFEST="${EVAL_DIR}/reports/manifest.json"

PASS=0
FAIL=0
RUN_DIR=""

# Convert Git Bash paths (/d/...) to Windows paths (D:/...) for Node.js
to_node_path() {
  if [[ "$1" =~ ^/([a-zA-Z])/ ]]; then
    echo "${BASH_REMATCH[1]}:/${1:3}"
  else
    echo "$1"
  fi
}

pass() { echo "  PASS: $1"; PASS=$((PASS + 1)); }
fail() { echo "  FAIL: $1"; FAIL=$((FAIL + 1)); }

cleanup() {
  # Remove dry-run directory if it was created
  if [[ -n "$RUN_DIR" && -d "$RUN_DIR" ]]; then
    rm -rf "$RUN_DIR"
  fi
  # Restore original manifest
  if [[ -f "${MANIFEST}.bak" ]]; then
    cp "${MANIFEST}.bak" "$MANIFEST"
    rm "${MANIFEST}.bak"
  fi
}
trap cleanup EXIT

echo "=== test-run-dryrun.sh ==="
echo ""

# --- Setup ---
[[ -f "$MANIFEST" ]] || { echo "ERROR: manifest.json not found at $MANIFEST"; exit 1; }

# Back up manifest before the test run
cp "$MANIFEST" "${MANIFEST}.bak"

# Record initial run count
MANIFEST_NODE=$(to_node_path "$MANIFEST")
INITIAL_COUNT=$(node -e "
  const fs = require('fs');
  const m = JSON.parse(fs.readFileSync('${MANIFEST_NODE}', 'utf-8'));
  process.stdout.write(String(m.runs.length));
")

# --- Execute dry-run ---
cd "$EVAL_DIR"
OUTPUT=$(./run-eval.sh baseline --dry-run 2>&1)

# Parse the run directory from script output (line: "==> Run directory: <path>")
RUN_DIR=$(echo "$OUTPUT" | grep "^==> Run directory:" | sed 's/^==> Run directory: //')

# --- Tests ---

# Test 1: A new run directory was created
if [[ -n "$RUN_DIR" && -d "$RUN_DIR" ]]; then
  pass "New run directory created: $(basename "$RUN_DIR")"
else
  fail "No run directory found in output (got: '$RUN_DIR')"
fi

# Test 2: run-meta.json exists in the run directory
if [[ -f "${RUN_DIR}/run-meta.json" ]]; then
  pass "run-meta.json present in run directory"
else
  fail "run-meta.json not found in: $RUN_DIR"
fi

# Test 3: run-meta.json has dryRun: true
if [[ -f "${RUN_DIR}/run-meta.json" ]]; then
  RUN_META_NODE=$(to_node_path "${RUN_DIR}/run-meta.json")
  DRY_RUN_VAL=$(node -e "
    const fs = require('fs');
    const m = JSON.parse(fs.readFileSync('${RUN_META_NODE}', 'utf-8'));
    process.stdout.write(String(m.dryRun));
  ")
  if [[ "$DRY_RUN_VAL" == "true" ]]; then
    pass "run-meta.json.dryRun is true"
  else
    fail "run-meta.json.dryRun is '$DRY_RUN_VAL' (expected 'true')"
  fi
fi

# Test 4: run-meta.json has evalType 'baseline'
if [[ -f "${RUN_DIR}/run-meta.json" ]]; then
  RUN_META_NODE=$(to_node_path "${RUN_DIR}/run-meta.json")
  EVAL_TYPE_VAL=$(node -e "
    const fs = require('fs');
    const m = JSON.parse(fs.readFileSync('${RUN_META_NODE}', 'utf-8'));
    process.stdout.write(m.evalType);
  ")
  if [[ "$EVAL_TYPE_VAL" == "baseline" ]]; then
    pass "run-meta.json.evalType is 'baseline'"
  else
    fail "run-meta.json.evalType is '$EVAL_TYPE_VAL' (expected 'baseline')"
  fi
fi

# Test 5: manifest was updated with exactly one new entry
NEW_COUNT=$(node -e "
  const fs = require('fs');
  const m = JSON.parse(fs.readFileSync('${MANIFEST_NODE}', 'utf-8'));
  process.stdout.write(String(m.runs.length));
")
EXPECTED_COUNT=$((INITIAL_COUNT + 1))
if [[ "$NEW_COUNT" -eq "$EXPECTED_COUNT" ]]; then
  pass "manifest updated with new entry (total: $NEW_COUNT)"
else
  fail "manifest run count: expected $EXPECTED_COUNT, got $NEW_COUNT"
fi

# Test 6: New manifest entry has status 'complete'
LAST_STATUS=$(node -e "
  const fs = require('fs');
  const m = JSON.parse(fs.readFileSync('${MANIFEST_NODE}', 'utf-8'));
  process.stdout.write(m.runs[m.runs.length - 1].status);
")
if [[ "$LAST_STATUS" == "complete" ]]; then
  pass "new manifest entry has status 'complete'"
else
  fail "new manifest entry status is '$LAST_STATUS' (expected 'complete')"
fi

# Test 7: Script output contains the run summary block
if echo "$OUTPUT" | grep -q "Evaluation Complete"; then
  pass "script printed completion summary"
else
  fail "script did not print completion summary"
fi

# --- Results ---
echo ""
echo "========================================"
echo "  Results: $PASS passed, $FAIL failed"
echo "========================================"

if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi
