#!/usr/bin/env node
/**
 * Tests for eval-configs.json
 *
 * Validates that the eval type registry has the correct structure and that
 * the validation logic used by run-eval.sh correctly rejects unknown types.
 *
 * Run: node --test tests/test-eval-configs.js
 */

const { describe, test } = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const EVAL_DIR = path.join(__dirname, '..');
const CONFIGS_FILE = path.join(EVAL_DIR, 'eval-configs.json');

// Mimic the validation node snippet from run-eval.sh
function lookupEvalType(evalType) {
  const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
  if (!configs[evalType]) return null;
  return configs[evalType];
}

describe('eval-configs.json structure', () => {
  test('file exists and is valid JSON', () => {
    assert.ok(fs.existsSync(CONFIGS_FILE), 'eval-configs.json not found');
    // Throws if invalid JSON
    JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
  });

  test('contains the three required eval types', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const expected of ['baseline', 'bias', 'bias-ungrounded']) {
      assert.ok(expected in configs, `Missing required eval type: '${expected}'`);
    }
  });

  test('all entries have a non-null "config" field', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const [name, entry] of Object.entries(configs)) {
      assert.ok('config' in entry, `Entry '${name}' missing 'config' field`);
      assert.ok(entry.config, `Entry '${name}' has falsy config`);
    }
  });

  test('all entries have a non-empty "description" field', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const [name, entry] of Object.entries(configs)) {
      assert.ok('description' in entry, `Entry '${name}' missing 'description' field`);
      assert.ok(entry.description.length > 0, `Entry '${name}' has empty description`);
    }
  });

  test('config files referenced in entries exist on disk', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const [name, entry] of Object.entries(configs)) {
      const configPath = path.join(EVAL_DIR, entry.config);
      assert.ok(
        fs.existsSync(configPath),
        `Entry '${name}' references non-existent config: ${entry.config}`
      );
    }
  });

  test('transform scripts (if non-null) exist on disk', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const [name, entry] of Object.entries(configs)) {
      if (entry.transform) {
        const transformPath = path.join(EVAL_DIR, entry.transform);
        assert.ok(
          fs.existsSync(transformPath),
          `Entry '${name}' references non-existent transform: ${entry.transform}`
        );
      }
    }
  });

  test('summarize scripts (if non-null) exist on disk', () => {
    const configs = JSON.parse(fs.readFileSync(CONFIGS_FILE, 'utf-8'));
    for (const [name, entry] of Object.entries(configs)) {
      if (entry.summarize) {
        const summarizePath = path.join(EVAL_DIR, entry.summarize);
        assert.ok(
          fs.existsSync(summarizePath),
          `Entry '${name}' references non-existent summarize script: ${entry.summarize}`
        );
      }
    }
  });
});

describe('eval type lookup (mirrors run-eval.sh validation)', () => {
  test('known type "baseline" resolves to its config', () => {
    const entry = lookupEvalType('baseline');
    assert.ok(entry !== null, 'baseline should be found');
    assert.equal(entry.config, 'promptfooconfig.yaml');
  });

  test('known type "bias" resolves to its config', () => {
    const entry = lookupEvalType('bias');
    assert.ok(entry !== null, 'bias should be found');
    assert.ok(entry.config.includes('bias'), 'bias config should reference bias');
  });

  test('unknown type returns null (causes exit 1 in run-eval.sh)', () => {
    const entry = lookupEvalType('nonexistent-type');
    assert.equal(entry, null);
  });

  test('unknown type lookup node snippet exits with code 1', () => {
    // Reproduces the exact validation snippet in run-eval.sh
    const configsPath = CONFIGS_FILE.replace(/\\/g, '\\\\');
    const result = spawnSync('node', ['-e', `
      const fs = require('fs');
      const c = JSON.parse(fs.readFileSync('${configsPath}', 'utf-8'));
      if (!c['unknown-eval-type']) { process.exit(1); }
      console.log(JSON.stringify(c['unknown-eval-type']));
    `], { encoding: 'utf-8' });
    assert.equal(result.status, 1, 'Unknown eval type should cause exit code 1');
  });
});
