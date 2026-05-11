#!/usr/bin/env bash
# =============================================================================
# run-eval.sh — Single entry point for running any NewsAnalyzer evaluation
#
# Usage:
#   ./eval/run-eval.sh <eval-type>              Run an evaluation
#   ./eval/run-eval.sh <eval-type> --dry-run    Test infrastructure without running Promptfoo
#   ./eval/run-eval.sh --list                   List available eval types
#
# Examples:
#   ./eval/run-eval.sh baseline
#   ./eval/run-eval.sh bias
#   ./eval/run-eval.sh bias-ungrounded --dry-run
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Convert Git Bash paths (/d/...) to Windows paths (D:/...) for Node.js interop
to_node_path() {
  if [[ "$1" =~ ^/([a-zA-Z])/ ]]; then
    echo "${BASH_REMATCH[1]}:/${1:3}"
  else
    echo "$1"
  fi
}

REPORTS_DIR="${SCRIPT_DIR}/reports"
CONFIGS_FILE="$(to_node_path "${SCRIPT_DIR}/eval-configs.json")"
MANIFEST_PATH="$(to_node_path "${REPORTS_DIR}/manifest.json")"
SCRIPTS_DIR="$(to_node_path "${SCRIPT_DIR}/scripts")"

DRY_RUN=false

# --- Helper functions --------------------------------------------------------

die() {
  echo "ERROR: $1" >&2
  exit 1
}

info() {
  echo "==> $1"
}

# --- Argument parsing --------------------------------------------------------

if [[ $# -lt 1 ]]; then
  echo "Usage: ./eval/run-eval.sh <eval-type> [--dry-run]"
  echo "       ./eval/run-eval.sh --list"
  exit 1
fi

if [[ "$1" == "--list" ]]; then
  echo "Available eval types:"
  node -e "
    const fs = require('fs');
    const c = JSON.parse(fs.readFileSync('${CONFIGS_FILE}', 'utf-8'));
    for (const [k, v] of Object.entries(c)) {
      console.log('  ' + k.padEnd(20) + v.description);
    }
  "
  exit 0
fi

EVAL_TYPE="$1"
shift

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run) DRY_RUN=true; shift ;;
    *) die "Unknown argument: $1" ;;
  esac
done

# --- Validate eval type against registry -------------------------------------

[[ -f "$CONFIGS_FILE" ]] || die "Eval configs not found: $CONFIGS_FILE"

EVAL_CONFIG=$(node -e "
  const fs = require('fs');
  const c = JSON.parse(fs.readFileSync('${CONFIGS_FILE}', 'utf-8'));
  if (!c['${EVAL_TYPE}']) { process.exit(1); }
  console.log(JSON.stringify(c['${EVAL_TYPE}']));
" 2>/dev/null) || die "Unknown eval type: '${EVAL_TYPE}'. Run with --list to see available types."

CONFIG_FILE=$(echo "$EVAL_CONFIG" | node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).config))")
TRANSFORM=$(echo "$EVAL_CONFIG" | node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).transform||''))")
SUMMARIZE=$(echo "$EVAL_CONFIG" | node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).summarize||''))")
PROMPTFOO_ARGS=$(echo "$EVAL_CONFIG" | node -e "process.stdin.on('data',d=>console.log(JSON.parse(d).promptfooArgs||''))")

# --- Create timestamped run directory ----------------------------------------

TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
DIR_TIMESTAMP=$(date -u +"%Y-%m-%d_%H-%M")
RUN_DIR="${REPORTS_DIR}/${EVAL_TYPE}/runs/${DIR_TIMESTAMP}"
RUN_ID="${EVAL_TYPE}-${DIR_TIMESTAMP}"

# Handle duplicate timestamps (AC8 — running same eval twice)
if [[ -d "$RUN_DIR" ]]; then
  COUNTER=1
  while [[ -d "${RUN_DIR}-${COUNTER}" ]]; do
    COUNTER=$((COUNTER + 1))
  done
  DIR_TIMESTAMP="${DIR_TIMESTAMP}-${COUNTER}"
  RUN_DIR="${REPORTS_DIR}/${EVAL_TYPE}/runs/${DIR_TIMESTAMP}"
  RUN_ID="${EVAL_TYPE}-${DIR_TIMESTAMP}"
fi

mkdir -p "$RUN_DIR"
info "Run directory: ${RUN_DIR}"

# --- Capture git commit ------------------------------------------------------

GIT_COMMIT=$(git -C "$SCRIPT_DIR" rev-parse --short HEAD 2>/dev/null || echo "unknown")

# --- Record start time -------------------------------------------------------

START_TIME=$(date +%s)

# --- Run the evaluation ------------------------------------------------------

if [[ "$DRY_RUN" == true ]]; then
  info "[DRY RUN] Skipping Promptfoo execution"
else
  info "Running evaluation: ${EVAL_TYPE} (config: ${CONFIG_FILE})"

  if [[ "$EVAL_TYPE" == "baseline" ]]; then
    # NER baseline: run all branches, output per-branch results to run dir
    BRANCHES=("judicial" "executive" "legislative" "conll")
    for branch in "${BRANCHES[@]}"; do
      info "  Running branch: ${branch}"
      cd "$SCRIPT_DIR"
      npx promptfoo eval \
        -c "$CONFIG_FILE" \
        -t "datasets/gold/${branch}.yaml" \
        -o "${RUN_DIR}/${branch}_results.json" \
        --no-cache
    done
  else
    # Bias and other types: single Promptfoo run
    cd "$SCRIPT_DIR"
    npx promptfoo eval \
      -c "$CONFIG_FILE" \
      $PROMPTFOO_ARGS \
      -o "${RUN_DIR}/results.json" \
      --no-cache
  fi
fi

# --- Run transform/summarize scripts ----------------------------------------

if [[ -n "$TRANSFORM" && "$DRY_RUN" != true ]]; then
  info "Running transform: ${TRANSFORM}"
  cd "$SCRIPT_DIR"
  node "$TRANSFORM" "$RUN_DIR"
fi

if [[ -n "$SUMMARIZE" && "$DRY_RUN" != true ]]; then
  info "Running summarize: ${SUMMARIZE}"
  cd "$SCRIPT_DIR"
  python "$SUMMARIZE" \
    --input "${RUN_DIR}/results.json" \
    --output "${RUN_DIR}/summary.json"
fi

# --- Record end time and duration -------------------------------------------

END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# --- Extract summary metrics -------------------------------------------------

if [[ "$DRY_RUN" == true ]]; then
  SUMMARY_JSON='{"precision": 0, "recall": 0, "f1": 0}'
else
  SUMMARY_JSON=$(node "${SCRIPTS_DIR}/extract-summary.js" "$EVAL_TYPE" "$RUN_DIR")
fi

# --- Write run-meta.json ----------------------------------------------------

cat > "${RUN_DIR}/run-meta.json" << EOF
{
  "id": "${RUN_ID}",
  "evalType": "${EVAL_TYPE}",
  "timestamp": "${TIMESTAMP}",
  "config": "${CONFIG_FILE}",
  "gitCommit": "${GIT_COMMIT}",
  "durationSeconds": ${DURATION},
  "dryRun": ${DRY_RUN},
  "status": "complete"
}
EOF

info "Run metadata written: ${RUN_DIR}/run-meta.json"

# --- Update manifest ---------------------------------------------------------

RUN_ENTRY=$(cat << EOF
{
  "id": "${RUN_ID}",
  "evalType": "${EVAL_TYPE}",
  "timestamp": "${TIMESTAMP}",
  "directory": "${EVAL_TYPE}/runs/${DIR_TIMESTAMP}",
  "config": "${CONFIG_FILE}",
  "summary": ${SUMMARY_JSON},
  "status": "complete"
}
EOF
)

node "${SCRIPTS_DIR}/update-manifest.js" "$MANIFEST_PATH" "$RUN_ENTRY"

# --- Print summary -----------------------------------------------------------

echo ""
echo "========================================"
echo "  Evaluation Complete: ${EVAL_TYPE}"
echo "========================================"
echo "  Run ID:    ${RUN_ID}"
echo "  Directory: ${RUN_DIR}"
echo "  Duration:  ${DURATION}s"
echo "  Git:       ${GIT_COMMIT}"
if [[ "$DRY_RUN" == true ]]; then
  echo "  Mode:      DRY RUN (no Promptfoo execution)"
else
  echo "  Metrics:   ${SUMMARY_JSON}"
fi
echo "========================================"
