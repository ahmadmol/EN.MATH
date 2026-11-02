# EN.MATH — Codex Integration Plan — Baccalaureate books 1 & 2

ROLE: Senior Android Kotlin/Compose Architect + Curriculum Data Engineer + Math QA + Student UX.

## Source of truth and input
Read `BAC_SOURCE_ANALYSIS.md`, `BAC_CURRICULUM_CATALOG.json`, `BAC_LESSONS.csv`, `BAC_ACTIVITY_EXERCISE_SECTIONS.csv`, `BAC_PAGE_INVENTORY.csv`, `PEDAGOGICAL_MODEL.md`, and `BAC_ORIGINAL_PRACTICE_DRAFTS.json`. The two large PDFs are user-provided local reference files and are **not included in this ZIP**. Do not infer the original PDFs exist in the repository. Ask the user to attach them locally to Codex only if page-by-page source inspection is required.

## Critical blockers
1. Part 1 is labelled 2023–2024; Part 2 2021–2022. This package **DOES NOT** certify 2026–2027 edition compliance. Verify official/current edition before declaring textbook content live. Maintain `BLOCKED_EDITION_REVIEW` until verified.
2. No full text/math extraction and no textbook exercise answer key in the package. Do NOT fabricate textbook problems, diagrams, or official answer keys. The 28 original math questions are DRAFT / not official.
3. Copyright: do not commit full PDFs, scans/screenshots of textbook pages, or large verbatim chapters/figures unless permission is explicitly verified. Author your own original explanations with page citations and original figures.

## Target architecture
- `BAC -> Book1/Book2 -> Unit (7+7) -> Lesson (34+29) -> source-aware content blocks -> activity / practice / unit exercises`.
- Keep existing lesson IDs/gems ledger aliases stable; use an explicit migration/crosswalk rather than renumbering existing IDs. If a catalog ID already exists, compare semantic identity and never repurpose it.
- Extend existing `CurriculumCatalog`/`ContentRef` compatibly; avoid rebuilding stores. Use immutable ownership context `{owner, stage, sourceEdition, generation}` in data write boundaries; do not regress duplicate-command receipts.
- UI: Grade9 remains separate, no grade mixing; enable only reviewed content; clearly distinguish unreviewed source and original drafts.
- No Gems from an unreviewed exercise; dedupe per validated source ID / learner.
- Preserve existing Android screens/route tags and central Home button. Support AR/EN, RTL/Math LTR, dark/light and 100/150/200% font size.

## Implementation milestones
A. READ-ONLY audit repo and check overlap with existing 24 demo Bac lessons and their reward keys. Produce mapping and migration plan before code changes.
B. Import 14 units and 63 lesson shells with correct book, edition, source printed page and PDF page; pass schema validation; blocked units must be visually distinguishable and not empty misreported as completed.
C. For every lesson, review relevant original pages, enter original *authored* explanation, formula rendering, definitions, proof steps, worked examples and source page references. Mark content incomplete when not checked.
D. For every exercise set, index each exercise ID + page + subparts + figure dependency; solve independently and test checker. If copyright or verification is unresolved, do not publish original text/image; create a licensable or newly-authored alternative with a provenance flag.
E. Link activities, practice, solver, graph, search, library, planner, progress and gems to source-aware refs; avoid stale cross-stage progress.
F. Automated tests: catalog counts 7/7, lessons 34/29, source page offset 1/0, source edition, unique IDs, guard on unavailable, answer normalization and ownership stage, curriculum search deep-link to correct lesson.
G. JVM, SQLite, Debug Build, Lint, compile Android tests; physical phone manual QA remains NOT_RUN until user performs it. Do NOT use emulator, ADB, install/uninstall, clear app data or change student/teacher history.
H. Security and Git: exclude private QA, credentials, PDFs/copyright assets, local caches and backups. Do not commit/push/force-push without separate explicit permission.

## Deliverables
- Real before/after repo evidence: which of 63 lessons are only indexed, fully reviewed, available, blocked; which exercises are source-indexed, solved, tested; no invented percentages.
- Migration plan for old demos and Gems.
- Unit, lesson, item coverage matrix with precise printed/PDF pages and editorial statuses.
- Report with tests executed, device QA NOT_RUN, blockers, changed files.
