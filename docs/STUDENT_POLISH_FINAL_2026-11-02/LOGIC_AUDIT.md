# Student polish campaign — initial architecture and logic audit

Baseline: 2b86f00, 1.0.4 / 5; clean feature/ui-refinement. Working branch: codex/student-polish-final-2026-11-02. Recovery bundle outside Git: D:/ENMATH/delivery/ENMATH-pre-polish-2026-11-02.bundle.

Architecture: MainActivity hosts ENMathApp; lifecycle ViewModels retain the local repository and isolate navigation by UUID/stage/session generation. ProductGraphs enforces stage/build availability. LocalLearnerStore owns additive identity/snapshot documents; Grade9Study, NaturePractice, SourceWorkspace and Planner have independent scoped documents. SQLiteLocalGems remains the wallet authority. StudentLearningProgress derives formative metrics from saved practice. Source catalog references remain distinct from lessons and certified learning. Teacher, backend and curriculum publication are outside this campaign.

Prior final acceptance and limitations were read; historical tests/screenshots are not current evidence. Current tests must be executed again. No phone, ADB or emulator operations are allowed.

| ID | Priority | Confirmed cause and effect before changes | Repair / verification plan |
|---|---|---|---|
| P01 | P1 | MathAnswer divides by a nonfinite denominator before validating the result: 1/Infinity becomes 0 | Reject nonfinite operands and overflow numeric fallbacks; numeric regressions |
| P02 | P1 | Solver, graph and answer normalization disagree on Arabic/Persian digits, decimal separators and multiplication | Shared bounded input normalization; supported/unsupported and locale regressions |
| P03 | P1 | Graph description accepts unsupported x*x and claims no roots for identically zero 0x | Use graph grammar first; distinguish zero/nonzero constants, avoid rounded roots appearing exact |
| P04 | P1 | Nature, planner and source workspace poison failed writes without notifying changes observers | Notify on failed commits, retain disk documents; restart/isolation regressions |
| P05 | P1 | Nature Previous/Hint ignore non-success status, leaving a stale apparently usable question | Immediately show preserved-data error; host UI failure regressions |
| P06 | P1 | BAC hero hardcodes 2/4 and 50% regardless of saved learning | Replace with actual reference identity and explicit index availability; host regression |
| P07 | P1 | BAC bookmarks count as completed source stations and increase a synthetic level | Show bookmark/visit/reference status independently; remove invented level |
| P08 | P1 | BAC path chests claim +25 gems without a ledger transaction | Remove fictitious reward interaction; actual wallet remains authoritative |
| P09 | P2 | Lazy station content increments an external counter on composition, changing offsets on recomposition | Use deterministic indices; regression for stable path identity |
| P10 | P2 | BAC header packs avatar/name/logo/three metrics in one unweighted row; several actions under 48dp | Responsive header and wrapping metrics/tools; 360/393/tablet and large-font host checks |
| P11 | P2 | G9 progress/encouragement are restricted to two lines; fixed teal is low contrast in Dark; headings miss selected font | Allow wrapping, semantic theme colors, complete typography and accessibility checks |
| P12 | P2 | Graph parsing builds regex/terms per sample pixel | Compile once per expression and reuse during drawing; instrument compile/evaluation counts |
| P13 | P1 | Wrong SharedPreferences types escape the identity codec as ClassCastException, bypassing the app's storage recovery screen | Wrap typed registry/snapshot reads; recognize malformed legacy preference/JSON data at the application boundary; preserve original bytes |

Before-fix proof: the first ten new logic/storage tests executed against the baseline implementation; nine failed (the locale stability control passed). Results are preserved in the delivery evidence. During current host QA, the G9 progress card also exposed a width constraint that reduced its text to 44px at 200% font size; explicit full-width content and a weighted heading fixed the runtime regression without truncating the copy.

Identity/migrations, duplicate commands, old attempts, planner history, file classification, wallet replay, source routing, release gates and mathematical rendering are also reviewed and covered by the final regression run. No data reset, rekeying or schema migration is planned. Additional findings will be recorded with evidence, not inferred from test counts.
