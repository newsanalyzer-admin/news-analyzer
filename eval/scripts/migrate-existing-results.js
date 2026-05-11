#!/usr/bin/env node
/**
 * Migrate existing eval results into the run-history directory structure.
 *
 * This is a one-time migration script. It:
 * 1. Copies existing results into timestamped run directories
 * 2. Creates run-meta.json for each migrated run
 * 3. Seeds manifest.json with entries for all migrated runs
 *
 * Safe to re-run — skips directories that already exist.
 *
 * Usage: node eval/scripts/migrate-existing-results.js
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const EVAL_DIR = path.resolve(__dirname, '..');
const REPORTS_DIR = path.join(EVAL_DIR, 'reports');
const MANIFEST_PATH = path.join(REPORTS_DIR, 'manifest.json');
const EXTRACT_SUMMARY = path.join(__dirname, 'extract-summary.js');

const MIGRATIONS = [
  {
    evalType: 'baseline',
    timestamp: '2026-03-25T00:00:00Z',
    dirTimestamp: '2026-03-25_00-00',
    config: 'promptfooconfig.yaml',
    sourceDir: path.join(REPORTS_DIR, 'baseline'),
    files: [
      'judicial_results.json', 'judicial_results.html', 'judicial_api.json',
      'executive_results.json', 'executive_results.html', 'executive_api.json',
      'legislative_results.json', 'legislative_results.html', 'legislative_api.json',
      'conll_results.json', 'conll_results.html', 'conll_api.json',
      'summary.json',
    ],
  },
  {
    evalType: 'bias',
    timestamp: '2026-04-02T23:09:00Z',
    dirTimestamp: '2026-04-02_23-09',
    config: 'promptfoo-bias.yaml',
    sourceDir: path.join(REPORTS_DIR, 'bias'),
    files: [
      { from: 'baseline_results.json', to: 'results.json' },
      { from: 'baseline_summary.json', to: 'summary.json' },
    ],
  },
  {
    evalType: 'bias-ungrounded',
    timestamp: '2026-04-02T23:09:00Z',
    dirTimestamp: '2026-04-02_23-09',
    config: 'promptfoo-bias-ungrounded.yaml',
    sourceDir: path.join(REPORTS_DIR, 'bias-ungrounded'),
    files: [
      { from: 'baseline_results.json', to: 'results.json' },
      { from: 'baseline_summary.json', to: 'summary.json' },
    ],
  },
];

// Initialize manifest
let manifest = { version: 1, runs: [] };
if (fs.existsSync(MANIFEST_PATH)) {
  manifest = JSON.parse(fs.readFileSync(MANIFEST_PATH, 'utf-8'));
}

for (const migration of MIGRATIONS) {
  const runDir = path.join(REPORTS_DIR, migration.evalType, 'runs', migration.dirTimestamp);
  const runId = `${migration.evalType}-${migration.dirTimestamp}`;

  // Skip if already migrated
  if (fs.existsSync(runDir)) {
    console.log(`SKIP: ${runId} (already exists)`);
    continue;
  }

  console.log(`Migrating: ${runId}`);
  fs.mkdirSync(runDir, { recursive: true });

  // Copy files
  for (const fileSpec of migration.files) {
    const fromName = typeof fileSpec === 'string' ? fileSpec : fileSpec.from;
    const toName = typeof fileSpec === 'string' ? fileSpec : fileSpec.to;
    const src = path.join(migration.sourceDir, fromName);
    const dst = path.join(runDir, toName);

    if (fs.existsSync(src)) {
      fs.copyFileSync(src, dst);
      console.log(`  Copied: ${fromName} -> ${toName}`);
    } else {
      console.warn(`  WARNING: Source not found: ${src}`);
    }
  }

  // Extract summary metrics
  let summary = { precision: 0, recall: 0, f1: 0 };
  try {
    const output = execSync(
      `node "${EXTRACT_SUMMARY}" "${migration.evalType}" "${runDir}"`,
      { encoding: 'utf-8' }
    );
    summary = JSON.parse(output.trim());
  } catch (e) {
    console.warn(`  WARNING: Could not extract summary: ${e.message}`);
  }

  // Write run-meta.json
  const meta = {
    id: runId,
    evalType: migration.evalType,
    timestamp: migration.timestamp,
    config: migration.config,
    gitCommit: 'migrated',
    durationSeconds: 0,
    dryRun: false,
    migrated: true,
    status: 'complete',
  };
  fs.writeFileSync(path.join(runDir, 'run-meta.json'), JSON.stringify(meta, null, 2));
  console.log(`  Written: run-meta.json`);

  // Add to manifest (skip if already present)
  const existing = manifest.runs.find(r => r.id === runId);
  if (!existing) {
    manifest.runs.push({
      id: runId,
      evalType: migration.evalType,
      timestamp: migration.timestamp,
      directory: `${migration.evalType}/runs/${migration.dirTimestamp}`,
      config: migration.config,
      summary,
      status: 'complete',
    });
    console.log(`  Added to manifest: ${runId}`);
  }
}

// Write manifest
fs.writeFileSync(MANIFEST_PATH, JSON.stringify(manifest, null, 2) + '\n');
console.log(`\nManifest written: ${MANIFEST_PATH} (${manifest.runs.length} runs)`);
