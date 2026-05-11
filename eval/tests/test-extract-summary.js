#!/usr/bin/env node
/**
 * Tests for scripts/extract-summary.js
 *
 * Validates that the summary extractor correctly handles both NER (baseline)
 * and bias result formats, and fails gracefully on missing/malformed input.
 *
 * Run: node --test tests/test-extract-summary.js
 */

const { describe, test } = require('node:test');
const assert = require('node:assert/strict');
const { spawnSync } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const EXTRACT_SUMMARY = path.join(__dirname, '..', 'scripts', 'extract-summary.js');

function tmpDir() {
  return fs.mkdtempSync(path.join(os.tmpdir(), 'eval-extract-test-'));
}

// Minimal NER summary with 2 branches — both have a "Claude Sonnet" extractor.
// Expected averages: P=0.7, R=0.8, F1=0.75
const NER_SUMMARY = {
  generated: '2026-01-01',
  branches: {
    judicial: {
      'spaCy en_core_web_sm': { precision: 0.2, recall: 0.85, f1: 0.32 },
      'Claude Sonnet': { precision: 0.8, recall: 0.9, f1: 0.85 },
    },
    executive: {
      'spaCy en_core_web_sm': { precision: 0.22, recall: 0.88, f1: 0.35 },
      'Claude Sonnet': { precision: 0.6, recall: 0.7, f1: 0.65 },
    },
  },
};

// Minimal bias summary with an "aggregate" block.
const BIAS_SUMMARY = {
  generated: '2026-01-01',
  aggregate: {
    precision: 0.7407,
    recall: 0.9756,
    f1: 0.8421,
    total_articles: 42,
  },
};

function extractSummary(evalType, dir) {
  return spawnSync('node', [EXTRACT_SUMMARY, evalType, dir], { encoding: 'utf-8' });
}

describe('extract-summary.js — NER baseline', () => {
  test('returns averaged P/R/F1 across Claude Sonnet branches', () => {
    const dir = tmpDir();
    try {
      fs.writeFileSync(path.join(dir, 'summary.json'), JSON.stringify(NER_SUMMARY));
      const result = extractSummary('baseline', dir);

      assert.equal(result.status, 0, `Script failed:\n${result.stderr}`);
      const summary = JSON.parse(result.stdout);

      // P = (0.8 + 0.6) / 2 = 0.7
      // R = (0.9 + 0.7) / 2 = 0.8
      // F1 = (0.85 + 0.65) / 2 = 0.75
      assert.equal(summary.precision, 0.7);
      assert.equal(summary.recall, 0.8);
      assert.equal(summary.f1, 0.75);
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });

  test('output is a JSON object with precision, recall, f1 keys', () => {
    const dir = tmpDir();
    try {
      fs.writeFileSync(path.join(dir, 'summary.json'), JSON.stringify(NER_SUMMARY));
      const result = extractSummary('baseline', dir);

      assert.equal(result.status, 0);
      const summary = JSON.parse(result.stdout);
      assert.ok('precision' in summary, 'missing precision');
      assert.ok('recall' in summary, 'missing recall');
      assert.ok('f1' in summary, 'missing f1');
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });

  test('exits non-zero if summary.json is missing', () => {
    const dir = tmpDir();
    try {
      const result = extractSummary('baseline', dir);
      assert.notEqual(result.status, 0);
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });

  test('all output values are numbers in [0, 1] range', () => {
    const dir = tmpDir();
    try {
      fs.writeFileSync(path.join(dir, 'summary.json'), JSON.stringify(NER_SUMMARY));
      const result = extractSummary('baseline', dir);

      assert.equal(result.status, 0);
      const summary = JSON.parse(result.stdout);
      for (const key of ['precision', 'recall', 'f1']) {
        assert.ok(typeof summary[key] === 'number', `${key} should be a number`);
        assert.ok(summary[key] >= 0 && summary[key] <= 1, `${key} should be in [0, 1]`);
      }
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });
});

describe('extract-summary.js — bias / bias-ungrounded', () => {
  test('extracts P/R/F1 from aggregate field', () => {
    const dir = tmpDir();
    try {
      fs.writeFileSync(path.join(dir, 'summary.json'), JSON.stringify(BIAS_SUMMARY));
      const result = extractSummary('bias', dir);

      assert.equal(result.status, 0, `Script failed:\n${result.stderr}`);
      const summary = JSON.parse(result.stdout);
      assert.equal(summary.precision, 0.7407);
      assert.equal(summary.recall, 0.9756);
      assert.equal(summary.f1, 0.8421);
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });

  test('bias-ungrounded uses same aggregate extraction as bias', () => {
    const dir = tmpDir();
    try {
      fs.writeFileSync(path.join(dir, 'summary.json'), JSON.stringify(BIAS_SUMMARY));
      const result = extractSummary('bias-ungrounded', dir);

      assert.equal(result.status, 0, `Script failed:\n${result.stderr}`);
      const summary = JSON.parse(result.stdout);
      assert.equal(summary.precision, 0.7407);
      assert.equal(summary.recall, 0.9756);
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });

  test('accepts legacy filename "baseline_summary.json"', () => {
    const dir = tmpDir();
    try {
      // Migrated runs use the original filename
      fs.writeFileSync(path.join(dir, 'baseline_summary.json'), JSON.stringify(BIAS_SUMMARY));
      const result = extractSummary('bias', dir);

      assert.equal(result.status, 0, `Script failed:\n${result.stderr}`);
      const summary = JSON.parse(result.stdout);
      assert.ok(typeof summary.f1 === 'number');
    } finally {
      fs.rmSync(dir, { recursive: true });
    }
  });
});

describe('extract-summary.js — input validation', () => {
  test('exits non-zero when called with no arguments', () => {
    const result = spawnSync('node', [EXTRACT_SUMMARY], { encoding: 'utf-8' });
    assert.notEqual(result.status, 0);
    assert.ok(result.stderr.includes('Usage'), `Expected usage message, got: ${result.stderr}`);
  });

  test('exits non-zero when called with only eval-type (no directory)', () => {
    const result = spawnSync('node', [EXTRACT_SUMMARY, 'baseline'], { encoding: 'utf-8' });
    assert.notEqual(result.status, 0);
  });
});
