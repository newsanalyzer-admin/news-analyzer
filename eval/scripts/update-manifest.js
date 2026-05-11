#!/usr/bin/env node
/**
 * Update the eval manifest with a new run entry.
 *
 * Usage:
 *   node update-manifest.js <manifest-path> <run-json>
 *
 * <run-json> is a JSON string with the run entry to append:
 *   { "id": "...", "evalType": "...", "timestamp": "...", ... }
 *
 * Creates the manifest file if it doesn't exist.
 */

const fs = require('fs');
const path = require('path');

const manifestPath = process.argv[2];
const runJson = process.argv[3];

if (!manifestPath || !runJson) {
  console.error('Usage: node update-manifest.js <manifest-path> <run-json>');
  process.exit(1);
}

let run;
try {
  run = JSON.parse(runJson);
} catch (e) {
  console.error(`Invalid JSON for run entry: ${e.message}`);
  process.exit(1);
}

// Required fields validation
const required = ['id', 'evalType', 'timestamp', 'directory', 'config', 'summary', 'status'];
for (const field of required) {
  if (!(field in run)) {
    console.error(`Missing required field in run entry: ${field}`);
    process.exit(1);
  }
}

// Load or create manifest
let manifest;
if (fs.existsSync(manifestPath)) {
  manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
} else {
  manifest = { version: 1, runs: [] };
}

// Append run (newest last)
manifest.runs.push(run);

// Write back
fs.mkdirSync(path.dirname(manifestPath), { recursive: true });
fs.writeFileSync(manifestPath, JSON.stringify(manifest, null, 2) + '\n');

console.log(`Manifest updated: ${run.id} (${manifest.runs.length} total runs)`);
