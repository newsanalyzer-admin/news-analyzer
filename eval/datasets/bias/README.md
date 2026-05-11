# Bias Gold Datasets — Mirror Copy

The YAML files in this directory (`synthetic_biased.yaml`, `curated_biased.yaml`) are a mirror of the authoritative copies in:

**`noometric-intelligence/eval/datasets/bias/`**

## Why there are two copies

The Promptfoo evaluation harness (`eval/promptfoo-bias.yaml`) runs in this repo and calls the reasoning service over HTTP. It reads gold annotations from this directory at evaluation time. Promptfoo requires local access to the dataset files — it cannot resolve cross-repo paths.

The `noometric-intelligence` repo is the authoritative source because:
- EVAL-5.1 (IRR), EVAL-5.2 (Demographic Stratification), and EVAL-5.3 (DIF Analysis) all run inside `noometric-intelligence` and treat that copy as ground truth
- The bias detector and OWL ontology that generated these annotations live in `noometric-intelligence`

## When updating gold datasets

1. Make changes in `noometric-intelligence/eval/datasets/bias/` first
2. Copy the updated files here to keep the Promptfoo harness in sync
3. Do not make gold data corrections in this copy only — they will be overwritten

## Contents

| File | Description |
|------|-------------|
| `synthetic_biased.yaml` | 42 synthetic articles with injected cognitive distortions (EVAL-1 neutral articles rewritten by LLM) |
| `curated_biased.yaml` | 1 curated real-world excerpt with human annotation |
| `curation/` | Curation scripts and notes (sync from `noometric-intelligence`) |
| `scripts/` | Article generation scripts (sync from `noometric-intelligence`) |
