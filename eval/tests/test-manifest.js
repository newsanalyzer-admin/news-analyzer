#!/usr/bin/env node
/**
 * Tests for update-manifest.js
 *
 * Validates that the manifest writer correctly creates/appends manifests,
 * enforces the required field schema, and rejects malformed input.
 *
 * Run: node --test tests/test-manifest.js
 */

const { describe, test } = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const UPDATE_MANIFEST = path.join(__dirname, '..', 'scripts', 'update-manifest.js');

function tmpManifestPath() {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), 'eval-manifest-test-'));
  return path.join(dir, 'manifest.json');
}

function validRun(overrides = {}) {
  return {
    id: 'baseline-2026-01-01_00-00',
    evalType: 'baseline',
    timestamp: '2026-01-01T00:00:00Z',
    directory: 'baseline/runs/2026-01-01_00-00',
    config: 'promptfooconfig.yaml',
    summary: { precision: 0.8, recall: 0.9, f1: 0.85 },
    status: 'complete',
    ...overrides,
  };
}

function runScript(manifestPath, runObj) {
  return spawnSync('node', [UPDATE_MANIFEST, manifestPath, JSON.stringify(runObj)], {
    encoding: 'utf-8',
  });
}

describe('update-manifest.js', () => {
  test('creates manifest with version and runs array if file does not exist', () => {
    const manifestPath = tmpManifestPath();
    const result = runScript(manifestPath, validRun());

    assert.equal(result.status, 0, `Script failed: ${result.stderr}`);

    const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
    assert.equal(manifest.version, 1);
    assert.ok(Array.isArray(manifest.runs));
    assert.equal(manifest.runs.length, 1);
    assert.equal(manifest.runs[0].id, 'baseline-2026-01-01_00-00');
  });

  test('appends to existing manifest, preserving prior runs', () => {
    const manifestPath = tmpManifestPath();
    runScript(manifestPath, validRun({ id: 'run-1' }));
    runScript(manifestPath, validRun({ id: 'run-2' }));

    const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
    assert.equal(manifest.runs.length, 2);
    assert.equal(manifest.runs[0].id, 'run-1');
    assert.equal(manifest.runs[1].id, 'run-2');
  });

  test('preserves different evalType entries across appends', () => {
    const manifestPath = tmpManifestPath();
    runScript(manifestPath, validRun({ id: 'bias-run', evalType: 'bias' }));
    runScript(manifestPath, validRun({ id: 'ner-run', evalType: 'baseline' }));

    const manifest = JSON.parse(fs.readFileSync(manifestPath, 'utf-8'));
    assert.equal(manifest.runs[0].evalType, 'bias');
    assert.equal(manifest.runs[1].evalType, 'baseline');
  });

  test('rejects invalid JSON run entry', () => {
    const manifestPath = tmpManifestPath();
    const result = spawnSync('node', [UPDATE_MANIFEST, manifestPath, 'not-valid-json'], {
      encoding: 'utf-8',
    });
    assert.notEqual(result.status, 0);
    assert.ok(result.stderr.includes('Invalid JSON'), `Expected 'Invalid JSON' in stderr, got: ${result.stderr}`);
  });

  test('rejects run entry missing "timestamp" field', () => {
    const manifestPath = tmpManifestPath();
    const run = validRun();
    delete run.timestamp;
    const result = runScript(manifestPath, run);
    assert.notEqual(result.status, 0);
    assert.ok(result.stderr.includes('timestamp'), `Expected 'timestamp' in error, got: ${result.stderr}`);
  });

  test('rejects run entry missing "summary" field', () => {
    const manifestPath = tmpManifestPath();
    const run = validRun();
    delete run.summary;
    const result = runScript(manifestPath, run);
    assert.notEqual(result.status, 0);
    assert.ok(result.stderr.includes('summary'), `Expected 'summary' in error, got: ${result.stderr}`);
  });

  test('rejects run entry missing "directory" field', () => {
    const manifestPath = tmpManifestPath();
    const run = validRun();
    delete run.directory;
    const result = runScript(manifestPath, run);
    assert.notEqual(result.status, 0);
  });

  test('written manifest is valid JSON with trailing newline', () => {
    const manifestPath = tmpManifestPath();
    runScript(manifestPath, validRun());

    const raw = fs.readFileSync(manifestPath, 'utf-8');
    assert.ok(raw.endsWith('\n'), 'Manifest file should end with a newline');
    // If this throws, it's not valid JSON
    JSON.parse(raw);
  });
});
