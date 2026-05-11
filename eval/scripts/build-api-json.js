#!/usr/bin/env node
/**
 * Transform raw Promptfoo results into the API format the frontend expects.
 *
 * Reads: {dir}/{branch}_results.json (raw Promptfoo output)
 * Writes: {dir}/{branch}_api.json   (BranchDetailResult shape)
 *         {dir}/summary.json        (aggregate P/R/F1 per branch per extractor)
 *
 * Usage:
 *   node eval/reports/build-api-json.js [output-dir]
 *
 * If output-dir is omitted, defaults to eval/reports/baseline/ (backward compat).
 */

const fs = require('fs');
const path = require('path');

const BRANCHES = ['judicial', 'executive', 'legislative', 'conll'];
const ENTITY_TYPES = [
  'person', 'government_org', 'organization',
  'location', 'event', 'concept', 'legislation',
];

const baselineDir = process.argv[2] || path.join(__dirname, 'baseline');
const summaryData = {};

for (const branch of BRANCHES) {
  const inputPath = path.join(baselineDir, `${branch}_results.json`);
  const outputPath = path.join(baselineDir, `${branch}_api.json`);

  const raw = JSON.parse(fs.readFileSync(inputPath, 'utf-8'));

  if (!raw?.results?.prompts) {
    console.error(`Skipping ${branch}: malformed results file`);
    continue;
  }

  const extractors = {};

  for (const prompt of raw.results.prompts) {
    const provider = prompt.provider;
    const namedScores = prompt.metrics.namedScores;

    const byEntityType = {};
    for (const entityType of ENTITY_TYPES) {
      byEntityType[entityType] = {
        tp: namedScores[`${entityType}_tp`] ?? 0,
        fp: namedScores[`${entityType}_fp`] ?? 0,
        fn: namedScores[`${entityType}_fn`] ?? 0,
      };
    }

    extractors[provider] = {
      overall: {
        precision: namedScores['Precision'] ?? 0,
        recall: namedScores['Recall'] ?? 0,
        f1: namedScores['F1'] ?? 0,
        true_positives: namedScores['true_positives'] ?? 0,
        false_positives: namedScores['false_positives'] ?? 0,
        false_negatives: namedScores['false_negatives'] ?? 0,
      },
      byEntityType,
    };
  }

  const result = { branch, extractors };
  fs.writeFileSync(outputPath, JSON.stringify(result, null, 2));
  console.log(`Written: ${outputPath}`);

  // Collect data for summary
  for (const [provider, data] of Object.entries(extractors)) {
    if (!summaryData[branch]) summaryData[branch] = {};
    summaryData[branch][provider] = {
      precision: data.overall.precision,
      recall: data.overall.recall,
      f1: data.overall.f1,
      true_positives: data.overall.true_positives,
      false_positives: data.overall.false_positives,
      false_negatives: data.overall.false_negatives,
      article_count: raw.results.stats?.successes ?? 0,
    };
  }
}

// Write summary.json
const summaryPath = path.join(baselineDir, 'summary.json');
const summary = {
  generated: new Date().toISOString().split('T')[0],
  branches: summaryData,
};
fs.writeFileSync(summaryPath, JSON.stringify(summary, null, 2));
console.log(`Written: ${summaryPath}`);
