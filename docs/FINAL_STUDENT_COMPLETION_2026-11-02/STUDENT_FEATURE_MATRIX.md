# Student feature matrix — 2026-11-02

Availability is separate from test outcome. See QA_RESULTS.md for executed checks and their limits.

| Feature | Grade 9 | Scientific Baccalaureate | Contract / limitation |
|---|---|---|---|
| Entry / onboarding | Local student + per-stage guest | Local student + per-stage guest | Saved local identity; no cloud account / Firebase Authentication |
| Profile isolation | UUID / stage / session generation | UUID / stage / session generation | Existing stores, migrations and IDs retained |
| Source catalog | Algebra 6 units / 19 researches; geometry 4 / 13 | Two books, 14 units / 63 researches | INDEX_ONLY / REVIEW_REQUIRED; actual catalog traversal tested |
| Source editions | 2025–2026 | Book 1: 2023–2024; Book 2: 2021–2022 | No 2026–2027 approval claimed |
| Approved textbook lessons | 0 / 32 in catalog | 0 / 63 in catalog | No source metadata promoted into approved instruction |
| Learning | Original reduced-fractions pilot; Nature explanation marked educational draft | 24 preserved legacy demo lessons | DEMO_LEARNING, separate from official textbook coverage |
| Nature practice | Six QA demo questions in Debug / w1Qa | Not applicable | REVIEW_REQUIRED; bank absent and destinations blocked in Release |
| Other source exercises | Honest pending availability | Honest pending availability | No training without a valid bank |
| Progress | Saved attempts, first independent answer, retries, hints, draft resume and review evidence | Preserved demo attempts/completions and older unverified history | Source browsing earns no learning credit; no mastery certification |
| History / review | Selected Nature result opens its exact saved session; question-specific retry/hint evidence | Actual saved demo answers and mistake review | Historical Nature wrong-answer text was not stored; never reconstructed |
| Subject choice | Saved algebra/geometry choice and separate saveable scroll states | Two books / units / researches | Geometry follow-up remains within geometry; no sequential locks introduced |
| Planner | Weekly achievement default, edits, confirmed deletion, progress, duplicate receipts, history, links | Same existing planner | Daily/monthly/time/hybrid data retained; manual goals for other subjects do not imply available courses |
| Library / search / bookmarks | Stage-filtered references + private PDF/Word document links | Source references + preserved demo library/search | No bundled PDFs or fake download source; real document-provider interaction needs phone QA |
| Physics helper files | No new files supplied or published | No new files supplied or published | No physics Gems SKU; any future licensed helper files must remain free |
| Solver | Seven curated examples; unsupported inputs explicit | Same bounded engine including matrix, limit, derivative, integral and probability examples | General CAS/AI/OCR not implemented |
| Graph lab | Bounded polynomial/basic trig grammar; axes, viewport, values, history | Same engine | Reject malformed operands and nonfinite coordinates; no automatic learning reward |
| Gems / achievements | Existing local wallet and activity ledger | Existing local wallet / preserved entitlements | No schema rewrite, retroactive award or reward on source browsing/goal creation |
| Profile / settings | Local profile switching, theme/language and related routes retained | Same existing behavior | No teacher development or website changes |
| UI QA | Current production Compose runtime tests | Current full source traversal on Compose host | Current raster renders BLOCKED by Windows Application Control; phone IME, gestures, TalkBack and process death NOT_RUN |

The student build is an MVP with deliberately limited reviewed learning content, not a completed official curriculum.
