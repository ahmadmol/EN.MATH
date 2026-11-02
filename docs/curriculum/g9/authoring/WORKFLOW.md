# Grade 9 authoring and review workflow

This directory contains editorial metadata, not an app question bank. No JSON draft is loaded or published by the app. The existing 32-topic source index remains unchanged.

## Use the real source identity

Start with `CONTENT_READINESS.csv` / `.json`. They list all 32 topics in source order: 19 Algebra topics across six units and 13 Geometry topics across four units. Every row carries the actual source book, unit, edition, SHA-256 and PDF page range. The current source is 2025–2026; 2026–2027 alignment is not verified.

Generate a draft outside this directory, or in an intentionally chosen editorial location:

```powershell
python tools/g9_content_workflow.py new G9-ALG-U01-L02 path/to/new-draft.json
python tools/g9_content_workflow.py validate path/to/new-draft.json
python tools/g9_content_workflow.py validate path/to/new-draft.json --require-release
python tools/g9_content_workflow.py inventory docs/curriculum/g9/authoring
```

`new` uses exclusive file creation and will not overwrite an author's file. The generated empty draft deliberately fails validation. A successful validation says `CHECKS_PASS_NOT_PUBLISHED`; it never enables a lesson.

## Author a coherent learning sequence

1. Confirm source identity, edition and topic boundaries against the actual pages. Do not invent page/figure/exercise numbers. Keep private textbook scans outside the repository.
2. Write original bilingual objectives and prerequisites appropriate to age 14–15. Limit each section to one idea.
3. Fill explanation, definition, formula and summary sections. Keep prose and LTR mathematics in separate fields; specify every formula's conditions/domain.
4. Add worked examples with justified steps, exact results and an independent arithmetic check when supported. Geometry/proof examples need actual manual review evidence and accessible figure descriptions.
5. Add original practice questions and a separate checkpoint. Every item needs a stable ID, prompt, hint, correction and answer contract. No Gems, mastery certification or exam score is inferred from authoring.
6. Check common misconceptions, including equivalent-but-unreduced fractions, decimal versus exact forms, invalid domains, units and proof/diagram requirements.

Question templates supported by the editorial validator:

| Kind | Required evidence | Runtime availability |
|---|---|---|
| `reduced_fraction` | Positive integers, exact cross-product equality and coprime answer | Only the existing four-question original pilot is enabled |
| `exact_number` | Bounded arithmetic check and answer equality | Draft template; no additional grader released |
| `choice` | Unique option IDs, bilingual labels, existing correct option | Draft template; mathematical correctness still needs review |
| `proof` | Manual rubric and verified solution | Never automatically scored by this workflow |
| `construction` | Manual rubric, correct figure and accessible description | Never automatically scored by this workflow |

The arithmetic checker supports bounded literal rational arithmetic; it does not execute Python supplied by an author and is not a symbolic proof engine. Unsupported mathematics requires a documented manual check.

## Review and release separately

Track source, rights, mathematics, pedagogy and edition-alignment reviews independently. `PASS` requires a reviewer, date and evidence reference. A metadata declaration is not proof that independent review actually happened: inspect the cited evidence and reviewer independence. Do not claim an educator/Ministry signoff from a JSON flag.

The current original reduced-fractions pilot has source inspection and software/exact-arithmetic checks, but no independent educator review or official current-year alignment. The other 31 topics stay unavailable. The original textbook corpus still has zero topics marked READY.

After all real reviews, an engineer must explicitly register a reviewed content revision, add domain/grading/persistence regressions, render the actual Compose surfaces and perform authorized device QA. This tool has no publish command. Never change student UUIDs, old progress receipts, wallets or source references to make a draft appear ready.

Kotlin presentation contracts live in `G9EducationalTemplates.kt`; the original pilot now uses `G9LessonTemplate`. Draft-only number/choice/manual-rubric contracts do not become automatically gradable merely by being constructed.
