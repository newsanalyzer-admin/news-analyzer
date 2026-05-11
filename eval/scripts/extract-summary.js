#!/usr/bin/env node
/**
 * Extract unified summary metrics (P/R/F1) from any eval result format.
 *
 * Handles two shapes:
 *   1. NER baseline summary.json — multi-branch, multi-extractor
 *      Returns average P/R/F1 across branches for "Claude Sonnet" extractor.
 *   2. Bias Promptfoo output — single provider with namedScores
 *      Returns aggregate P/R/F1 from namedScores.
 *
 * Usage:
 *   node extract-summary.js <eval-type> <results-dir>
 *
 * Output: JSON to stdout: { "precision": 0.xx, "recall": 0.xx, "f1": 0.xx }
 */

const fs = require('fs');
const path = require('path');

const evalType = process.argv[2];
const resultsDir = process.argv[3];

if (!evalType || !resultsDir) {
  console.error('Usage: node extract-summary.js <eval-type> <results-dir>');
  process.exit(1);
}

function extractNerSummary(dir) {
  const summaryPath = path.join(dir, 'summary.json');
  if (!fs.existsSync(summaryPath)) {
    console.error(`NER summary not found: ${summaryPath}`);
    process.exit(1);
  }

  const data = JSON.parse(fs.readFileSync(summaryPath, 'utf-8'));
  const branches = data.branches;

  // Average P/R/F1 across branches for Claude Sonnet
  let totalP = 0, totalR = 0, totalF1 = 0;
  let count = 0;

  for (const branch of Object.keys(branches)) {
    const extractor = branches[branch]['Claude Sonnet'];
    if (extractor) {
      totalP += extractor.precision;
      totalR += extractor.recall;
      totalF1 += extractor.f1;
      count++;
    }
  }

  if (count === 0) {
    console.error('No Claude Sonnet results found in summary');
    process.exit(1);
  }

  return {
    precision: Math.round((totalP / count) * 10000) / 10000,
    recall: Math.round((totalR / count) * 10000) / 10000,
    f1: Math.round((totalF1 / count) * 10000) / 10000,
  };
}

function extractBiasSummary(dir) {
  // Try summary files in order of preference
  for (const name of ['summary.json', 'baseline_summary.json']) {
    const summaryPath = path.join(dir, name);
    if (fs.existsSync(summaryPath)) {
      const data = JSON.parse(fs.readFileSync(summaryPath, 'utf-8'));
      if (data.aggregate) {
        return {
          precision: data.aggregate.precision,
          recall: data.aggregate.recall,
          f1: data.aggregate.f1,
        };
      }
    }
  }

  // Fall back to raw Promptfoo output
  const resultsPath = fs.existsSync(path.join(dir, 'results.json'))
    ? path.join(dir, 'results.json')
    : path.join(dir, 'baseline_results.json');
  if (!fs.existsSync(resultsPath)) {
    console.error(`No summary or results file found in: ${dir}`);
    process.exit(1);
  }

  const raw = JSON.parse(fs.readFileSync(resultsPath, 'utf-8'));
  const prompts = raw?.results?.prompts;
  if (!prompts || prompts.length === 0) {
    console.error('No prompts found in raw Promptfoo output');
    process.exit(1);
  }

  const ns = prompts[0].metrics.namedScores;
  // Bias namedScores store P/R/F1 as percentages (0-100 scale)
  // Normalize to 0-1 if > 1
  const normalize = (v) => v > 1 ? Math.round((v / 100) * 10000) / 10000 : Math.round(v * 10000) / 10000;

  return {
    precision: normalize(ns['Precision'] || 0),
    recall: normalize(ns['Recall'] || 0),
    f1: normalize(ns['F1'] || 0),
  };
}

let summary;
if (evalType === 'baseline') {
  summary = extractNerSummary(resultsDir);
} else {
  // bias, bias-ungrounded, or any future type — try generic bias extraction
  summary = extractBiasSummary(resultsDir);
}

console.log(JSON.stringify(summary));
