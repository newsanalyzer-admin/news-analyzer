# Curator Process Guide
### Steve's reference for running curation sessions

---

## Session Setup (5–10 min)

1. **Select 3–5 article excerpts** (see Article Selection below)
2. For each article, print:
   - The article text (paste into a doc, 12pt font, double-spaced — easy to write on)
   - One annotation worksheet (with Article ID and header filled in)
3. Clip the worksheet to the article. Hand Kate the reference card + the stack.
4. No rush — she can do one, take a break, do another.

---

## Article Selection Guidelines

### Good sources for excerpts
- Opinion / editorial sections of major newspapers
- Political commentary (left and right lean — we want variety)
- Think-tank press releases on policy topics
- Letters to the editor
- Cable news transcripts

### Length
- 1–3 paragraphs is ideal (roughly 150–400 words)
- Long enough to have context, short enough not to be exhausting

### What to look for when selecting
- **For bias articles:** Pick pieces where *you* notice something off — loaded language, a suspicious either/or, an attack on the speaker rather than the argument. If it feels persuasive in a slippery way, it's probably worth annotating.
- **For faithful (clean) articles:** Straight news reporting, press releases with dry factual content. The detector should return empty on these — they're control cases.

### Target mix (for the full 15–20 article set)
| Type | Count |
|---|---|
| Articles with 1 bias | 7–8 |
| Articles with 2 biases | 4–5 |
| Articles with 3+ biases | 2–3 |
| Clean / no bias (control) | 5–6 |

### Distortion types to prioritize
The synthetic dataset (42 articles) already covers all 14 types. For curated data, prioritize types that appear naturally in real writing:
- Framing Effect (most common in journalism)
- Appeal to Authority (very common in opinion pieces)
- False Dilemma (common in political commentary)
- Ad Hominem (common in attack pieces)
- Confirmation Bias (common in think-tank writing)

---

## Article ID Numbering

Continue from `eval-3-curated-001` (the placeholder already in the YAML).

The first real article Kate annotates = `eval-3-curated-002`, then `003`, etc.

---

## Entering Kate's Annotations into YAML

After a session, open `eval/datasets/bias/curated_biased.yaml` and add entries.

### Mapping her worksheet to the schema

| Worksheet field | YAML field | Notes |
|---|---|---|
| Article text | `article_text` | Paste the full excerpt |
| Bias / Fallacy Type (circled) | `biases[].type` | Convert to snake_case — see table below |
| Passage (copied text) | `biases[].excerpt` | Exact quote from article |
| Why does it fit? | `biases[].explanation` | Her words, lightly edited for clarity |
| Article ID | `metadata.id` | e.g., `eval-3-curated-002` |
| Overall difficulty | `metadata.difficulty` | easy / medium / hard |
| Total findings | `metadata.bias_count` | Number of bias entries |
| "No bias found" checked | `biases: []` | Empty list |

### Type name conversion (her label → YAML key)

| Kate writes | YAML value |
|---|---|
| Ad Hominem | `ad_hominem` |
| Straw Man | `straw_man` |
| False Dilemma | `false_dilemma` |
| Slippery Slope | `slippery_slope` |
| Appeal to Authority | `appeal_to_authority` |
| Red Herring | `red_herring` |
| Circular Reasoning | `circular_reasoning` |
| Affirming the Consequent | `affirming_the_consequent` |
| Denying the Antecedent | `denying_the_antecedent` |
| Confirmation Bias | `confirmation_bias` |
| Framing Effect | `framing_effect` |
| Anchoring Bias | `anchoring_bias` |
| Availability Heuristic | `availability_heuristic` |
| Bandwagon Effect | `bandwagon_effect` |

### Academic source to include

Each bias entry needs an `academic_source` field. Use these:

| Type | academic_source value |
|---|---|
| confirmation_bias | `"Nickerson, 1998"` |
| framing_effect | `"Tversky & Kahneman, 1981"` |
| anchoring_bias, availability_heuristic, bandwagon_effect | `"Kahneman, 2011"` |
| All 9 fallacies | `"Walton, 2008"` |

### Example YAML entry

```yaml
- vars:
    article_text: >
      The bill's opponents have consistently argued that any reform
      to the zoning code would inevitably lead to the destruction of
      neighborhood character, rising crime, and an exodus of longtime
      residents — despite offering no evidence that these outcomes
      have occurred in comparable cities.
    biases:
      - type: slippery_slope
        excerpt: "inevitably lead to the destruction of neighborhood character, rising crime, and an exodus of longtime residents"
        explanation: "The author's opponents claim a chain of extreme consequences without providing evidence that each step follows from the last."
        academic_source: "Walton, 2008"
    metadata:
      id: eval-3-curated-002
      source: curated
      difficulty: easy
      bias_count: 1
      injected_types: []
```

### On "debatable" annotations
If Kate marked a finding as "Yes, debatable," still include it in the YAML — just note it in the difficulty as `hard`. Subjective cases are real and valuable. The evaluation scorer gives partial credit for category-level matches anyway.

---

## Tips for Good Sessions

- **Don't coach her** on what to find before she reads. The value is her independent read.
- **After she annotates**, you can discuss — that's when you'll learn if her mental model of a fallacy matches the ontology definition. If there's a gap, note it.
- **Keep sessions short** — 3–5 articles is plenty. This should feel like a puzzle, not a job.
- **Save her comments.** Her "why" explanations are often better than anything you'd write. Use her words.

---

*Process guide — Steve Wooding, April 2026*
