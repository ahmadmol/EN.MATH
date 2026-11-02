# Grade 9 Phase 3 delivery — 2026-10-30

Implemented in the existing project at `D:\ENMATH\EN.MATH`. This phase improves existing screens and persistence; it does not declare the curriculum complete. Phase 1/2 delivery reports, the screen reference specification and reference boards were reviewed before implementation. The workspace already contained substantial local changes; those changes were preserved.

## Screen audit and changes

| Existing screen | Phase 3 implementation |
|---|---|
| Home / long learning path | Compact student header and search/planner actions; first learning stations visible sooner at normal text size; planner summary follows the curriculum path; saveable scrolling retained; separate Geometry-path render fixture |
| Algebra / Geometry | Grade 9 turquoise source actions and surfaces; indexed units/topics retained; unavailable content remains explicit; no duplicate subject screens |
| Exercises | Consistent Grade 9 surfaces and actions; existing original pilot remains the only enabled practice; no generated assessment bank |
| Profile | Grade 9 surface palette applies to existing profile/actions; existing profile, wallet and learning-evidence contracts retained |
| Lesson / examples / summary | Reusable lesson template, educational cards, heading semantics, native LTR formula panels, spaced navigation, shorter metadata, explicit reading-position distinction; Arabic mathematical prose isolated; checkpoint fraction gets its own formula panel |
| Practice / hint / correction | LTR answer entry within RTL, slash shortcut, keyboard Done/check handling, IME padding, idempotent hints, locked checked answers, polite correction announcements, Arabic “question 1 of 4” wording, autosaved unchecked drafts |
| Saved results | Real immutable attempt receipts remain authoritative; correct/needs-review/skipped distinctions and retry/mistake navigation retained; mathematical correction prose isolated |
| Planner | Grade 9 compact heading and turquoise controls; theme-aware selected chips and button content; controls grow with large text; date text no longer displaces arrow buttons; live repository behavior retained |
| Files / reader | Arabic-normalized multiword search, PDF/Word filters, stable alphabetical ordering and readable sizes; explicit provider-dependent offline messaging; categorized read failures, retry and reselect guidance |

Grade 9 theme tokens now cover elevated/container surfaces in both light and dark modes. Shared components choose these changes by the Grade 9 student stage; Baccalaureate and Teacher keep their existing branches. Existing artwork/icons are reused. English Grade 9 navigation labels use paragraph wrapping/hyphenation at large type sizes.

## Drafts, attempts and data preservation

Unchecked answer edits now autosave after a 350 ms debounce. Navigation/disposal flushes the latest draft. Check, hint and next actions cancel a pending autosave and persist a newer sequence. The repository rejects stale snapshots and different active draft IDs, including stale finalization that could otherwise erase newer answers. Binding a different profile resets session state and cancels the old pending job. Completed attempts keep their existing IDs and idempotent receipt behavior; starting again creates a fresh draft.

The draft JSON adds an optional `sequence` field, defaulting to zero for existing Phase 2 records. No UUIDs, wallet schemas, source IDs, source editions, old attempt records or user-owned files are rewritten. No Gems were added. Student document access continues through read-only SAF URIs; no student files were copied, renamed, converted, uploaded or removed.

## Rendered visual QA

Actual Compose/Layoutlib PNGs were generated, rather than HTML mockups or compilation-only previews. The opt-in screenshot engine is separate from the installed application and uses the existing AGP/Kotlin versions.

* 15 screens/states: Home, Home Geometry path, Algebra, Geometry, Exercises, Profile, Lesson, Example, Summary, Practice, Hint, Correction, Result, Planner and Files.
* 24 configurations: 360/393 dp, Arabic/English, light/dark and 100/150/200% text size.
* 360 distinct PNGs; the gallery tool checks configuration uniqueness and nonblank pixels and records image hashes.
* All 24 contact sheets were visually inspected. Selected original PNGs were also inspected at higher detail. Visual findings drove additional fixes to the Home fold, example navigation spacing, Planner contrast/control height, Arabic question counter and mathematical prose.
* Preview names were changed from decimal scale labels to `100pct`/`150pct`/`200pct`: the renderer otherwise collapsed several names onto the same output filename. The final gallery requires all 360 unique images.

Open [the rendered gallery](../QA/g9-phase3-2026-10-30/visual/index.html), [image manifest](../QA/g9-phase3-2026-10-30/visual/manifest.json) and [visual review record](../QA/g9-phase3-2026-10-30/VISUAL_REVIEW.md). Gallery images link to original PNGs under `app/src/screenshotTestDebug/reference/`.

Fixtures are explicitly debug-only, use memory-only preferences, and never read/write real student profiles or wallets. The example result fixture is synthetic QA data; production screens display only repository receipts. Planner previews receive a deterministic empty in-memory plan instead of stopping at the loading frame.

These renders establish visual evidence for the captured states. They do not establish device interaction quality, animation smoothness, TalkBack operation, keyboard behavior on a real IME, or behavior of a student's external document provider.

## Educational content readiness

[CONTENT_READINESS.csv](../curriculum/g9/authoring/CONTENT_READINESS.csv) and [JSON](../curriculum/g9/authoring/CONTENT_READINESS.json) inventory every indexed topic and its actual source edition, hash and page range, with separate objectives, explanation, example, formula, question, answer and review states.

| Content | Readiness |
|---|---|
| Curriculum index | 6 Algebra units / 19 topics; 4 Geometry units / 13 topics; 32 total |
| Reduced fractions `G9-ALG-U01-L03` | Existing bounded original pilot; 2 worked examples, 4 practice questions and 1 checkpoint; source/exact-arithmetic software checks |
| Other 31 topics | Existing drafts indexed where present; unavailable to learners pending review |
| Full textbook lessons READY | **0 / 32** |
| Independent educator review | **NOT_PERFORMED** |
| Official 2026–2027 edition alignment | **NOT_VERIFIED**; indexed source remains 2025–2026 |

`G9EducationalTemplates.kt` defines reusable lesson and question presentation contracts. Only the pilot fraction contract has an implemented runtime grader. Number, choice and manual proof/construction contracts remain authoring templates.

[Authoring workflow](../curriculum/g9/authoring/WORKFLOW.md) and `tools/g9_content_workflow.py` provide exclusive draft creation, schema/source checks, bounded exact arithmetic, formula conditions, solution/hint/correction requirements, geometry rubrics/figure descriptions and review evidence fields. Release checks require evidenced independent educator review; a JSON declaration cannot establish that such review happened. The tool has no publish command, and editorial drafts are not loaded into the app.

## Validation

The final machine-readable results and APK hash are in [summary.json](../QA/g9-phase3-2026-10-30/summary.json). Detailed logs are in the same QA directory.

| Check | Result |
|---|---|
| JVM Debug / Release / W1Qa | Final run: 314 tests per variant; 0 failures/errors/skipped |
| Distinct JVM tests | 314; 942 executions across three variants, including 12 new Phase 3 regressions |
| Host SQLite | 10 passed, temporary databases and actual wallet schema |
| Authoring workflow | 11 passed |
| Pilot/source check | PASS; 2 worked examples and all 5 original question/checkpoint answers |
| Catalog check | PASS; 10 units, 32 topics, 0 READY textbook lessons |
| Debug build | PASS |
| Lint Debug | PASS; 0 errors, 66 warnings, 1 informational issue |
| Instrumentation compilation | PASS; instrumentation execution **NOT_RUN** |
| Compose renders | Full 360-case render PASS; final 144 navigation and 24 Planner rerenders PASS; see visual review for inspection scope/remaining issues |
| Whitespace | `git diff --check` PASS |

The new JVM regressions cover debounced edit/restart, navigation flush, required correction before next/finalization, idempotent hints, fresh retries and immutable receipts, profile-switch isolation, competing drafts, stale finalization, legacy sequence-free drafts, template release gates, Arabic resource search/type filters and read-error categories. Existing navigation, progress, stage isolation and wallet regressions remain in the full suite.

Three new instrumentation tests cover Arabic/LTR answer layout at 200%, polite correction semantics and removal of another profile's document title after switching. They were compiled only. No emulator, ADB, phone installation, phone data operation, Git commit or push was performed.

Reproduce ordinary checks with JBR 21 configured as `JAVA_HOME`:

```powershell
.\gradlew.bat :app:test :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain
python tools/test_local_gems_sqlite.py
python tools/test_g9_content_workflow.py
python tools/verify_g9_pilot.py
python tools/import_g9_catalog.py --check
git diff --check
```

Reproduce host rendering using the optional local Maven mirror prepared on this workstation:

```powershell
.\gradlew.bat :app:updateDebugScreenshotTest '-Pg9Render=true' '-Pg9RenderRepo=D:\ENMATH\g9-render-maven' '-Pandroid.experimental.enableScreenshotTest=true' '-Pandroid.compose.screenshot.maxHeapSize=4g' --offline --console=plain
python tools/catalog_g9_previews.py app/src/screenshotTestDebug/reference docs/QA/g9-phase3-2026-10-30/visual
```

The mirror contains public Google/Maven artifacts outside the project; downloads were checked against public artifact SHA-1 files. It works around this workstation's Java DNS failures and truncated large HTTP downloads. Normal builds do not enable the renderer. See the [official screenshot documentation](https://developer.android.com/studio/preview/compose-screenshot-testing) and [release notes](https://developer.android.com/studio/preview/compose-screenshot-testing-release-notes) for the experimental renderer; generated candidates are not automatically approved baselines.

## Phase 3 source/tool files

Paths below are relative to the official project. Other dirty files predate this phase and must not be attributed to this delivery.

* `app/build.gradle.kts`, `settings.gradle.kts` — opt-in host renderer configuration.
* `app/src/main/java/com/ahmadmol/enmath/core/design/Theme.kt`, `StudentSourceDesign.kt` — Grade 9 theme and hero styling.
* `app/src/main/java/com/ahmadmol/enmath/core/data/Grade9StudyRepository.kt`, `Grade9Pilot.kt`, `G9EducationalTemplates.kt` — versioned drafts and educational contracts.
* `app/src/main/java/com/ahmadmol/enmath/features/student/Grade9Experience.kt`, `Grade9LearningScreens.kt` — Home, lesson/practice/results and draft ViewModel.
* `app/src/main/java/com/ahmadmol/enmath/features/courses/SourceCurriculumScreens.kt` — stage-aware source actions.
* `app/src/main/java/com/ahmadmol/enmath/features/planner/PlannerScreen.kt` — Grade 9 styling, wrapping and preview state input.
* `app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt` — Grade 9 label wrapping.
* `app/src/main/java/com/ahmadmol/enmath/features/library/Grade9Files.kt`, `Grade9FilePresentation.kt` — file filtering/search/failures.
* `app/src/debug/java/com/ahmadmol/enmath/Grade9Phase2Previews.kt`, `StudentSourcePreviews.kt` — actual memory-only preview fixtures and unique configuration labels.
* `app/src/screenshotTest/kotlin/com/ahmadmol/enmath/Grade9RenderedPreviews.kt` — host-rendered wrappers.
* `app/src/test/java/com/ahmadmol/enmath/Grade9Phase3RegressionTest.kt` — 12 regressions.
* `app/src/androidTest/java/com/ahmadmol/enmath/Grade9Phase3AccessibilityTest.kt` — 3 compile-validated UI regressions.
* `tools/g9_content_workflow.py`, `test_g9_content_workflow.py`, `prefetch_g9_render.py`, `catalog_g9_previews.py` — authoring checks and render tooling.
* `docs/curriculum/g9/authoring/`, `docs/QA/g9-phase3-2026-10-30/`, this delivery report — readiness inventory, review instructions and validation evidence.

## Remaining work

Independent educator review, official year alignment and verified educational expansion remain necessary. PDF text accessibility/zoom uses an external reader; offline availability depends on the selected provider. Real keyboard/inset, TalkBack, back-stack interaction, resource-provider and animation QA require authorized device testing. The visual review lists remaining layout limitations and uncaptured states; this delivery does not claim full visual or device approval.
