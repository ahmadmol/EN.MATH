# Grade 9 phase 1 — delivery and verification

Date: 2026-10-30. Workspace: `D:\ENMATH\EN.MATH`.

This is a completed first implementation phase, **not completion of the 45-screen product or educational curriculum**. The exact baseline and every proposed screen are mapped in [the 45-screen audit](G9_MASTER_PLAN_AUDIT_2026-10-30.md).

## Delivered behavior

- G9-01: Grade 9 Home uses a long lazy path with all 10 source units and 32 topics in catalog order, reference-based navigation, resume browsing, saved scroll state, real learning evidence/wallet status and plan/library/tool links. No synthetic level, bookmark-based learning completion, chest reward or imaginary exam result.
- G9-02 / G9-03: separate Algebra and Geometry tabs, real 6/4-unit catalogs, subject-scoped title search, and existing book/progress destinations.
- G9-04: Exercises hub with working book/unit/topic selectors, exact selected-reference navigation, honest question-bank availability and existing real-mistakes/plan links. It cannot start an unreviewed scored session.
- G9-05: existing Profile improved with Solver, Graph Lab, Source and Help entry points; all local profile, wallet, settings, progress, plan, library and switch-file actions preserved.
- G9-10 / G9-45: source/availability and student help destinations. Existing book, unit, research and station-detail templates are reused rather than duplicated.
- Grade 9 gets its own five tabs; the shared Baccalaureate/Teacher tab lists remain unchanged. Bottom navigation disappears on detail/focus screens, Home state restores for Grade 9, and the G9 Home button exposes selected-tab accessibility semantics.

## Files changed by this phase

Paths below are relative to the workspace. These are this phase's edits; the much larger pre-existing working-tree diff also contains earlier unpublished changes.

| File | Change |
|---|---|
| `app/src/main/java/com/ahmadmol/enmath/features/student/Grade9Experience.kt` | New reusable G9 list/card/path/subject/filter presentation; Home, Algebra, Geometry, Exercises, Source, Help and bottom navigation. |
| `app/src/main/java/com/ahmadmol/enmath/features/student/StudentRoadmapScreens.kt` | Route only the G9 Home wrapper into the new experience; retain BAC implementation. |
| `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt` | G9-only tool/help links; notifications entry retained on Home. |
| `app/src/main/java/com/ahmadmol/enmath/core/navigation/Routes.kt` | Add G9 destinations and stage-specific tab selector; retain shared routes. |
| `app/src/main/java/com/ahmadmol/enmath/core/navigation/StudentNavigationPolicy.kt` | Stage-aware focus/transition visibility and testable G9 Home-restoration policy. |
| `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt` | Wire stage tabs, bottom bar/rail, restoration, titles and G9 header actions. |
| `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt` | Wire guarded G9 subject/exercise/source/help destinations. |
| `app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt` | Opt-in G9 center-tab semantics; existing other-role behavior remains the default. |
| `app/src/test/java/com/ahmadmol/enmath/Grade9MainExperienceTest.kt` | Five regressions for stage tabs/focus transitions, restoration isolation, curriculum/search, validated resume and all-station non-reward behavior. |
| `app/src/test/java/com/ahmadmol/enmath/StudentVideoRegressionTest.kt` | Update old source-wiring assertion for the extracted restoration policy; actual policy now covered behaviorally. |
| `app/src/androidTest/java/com/ahmadmol/enmath/Grade9MainUiContractTest.kt` | Pure UI fixtures for every station's exact route and five accessible tab selections; compiled, not executed. |
| `docs/IMPLEMENTATION/G9_MASTER_PLAN_AUDIT_2026-10-30.md` | Actual architecture/curriculum findings and 45-row EXISTING/IMPROVE/NEW/DEFER mapping. |
| `docs/IMPLEMENTATION/G9_PHASE1_VALIDATION_2026-10-30.md` | This delivery report. |
| `docs/QA/g9-2026-10-30/` | Baseline JVM log, final Gradle/SQLite/catalog logs and machine-readable summary. |

No database, UUID, profile, wallet, repository schema, curriculum identity or educational content was changed by this phase. New view state uses existing navigation save state; source browsing/bookmarks still use the existing owner/stage/edition/generation checks.

## Validation

The first compile exposed a positional call to the shared bottom bar; compatibility was restored without editing Teacher code. The first extended JVM run found a source-text assertion expecting Home restoration to stay disabled; the new stage-specific policy is now tested directly, retaining BAC/Teacher behavior.

Final results are recorded below after completion of all checks. The host's `python` command was a Microsoft Store alias; the installed bundled Python interpreter was used instead. Java used the existing JDK 21; no runtime or SDK was installed.

```powershell
$env:JAVA_HOME='<JDK_21_PATH>'
.\gradlew.bat :app:test :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain
python tools/test_local_gems_sqlite.py
python tools/import_g9_catalog.py --check
git diff --check
```

Final validation: **PASS**. The final Gradle invocation finished `BUILD SUCCESSFUL` in 2m 2s after the last source change.

| Check | Measured result |
|---|---|
| JVM — Debug | 291 tests, 0 failures/errors/skipped |
| JVM — Release | 291 tests, 0 failures/errors/skipped |
| JVM — W1Qa | 291 tests, 0 failures/errors/skipped |
| Host SQLite | 10 tests passed; actual asset schema exercised in temporary databases |
| Debug APK | `:app:assembleDebug` passed; output `app/build/outputs/apk/debug/app-debug.apk` |
| Android Lint | `:app:lintDebug` passed; **0 errors, 61 warnings**, 1 informational issue |
| Instrumentation source compilation | `:app:compileDebugAndroidTestKotlin` passed; no device test execution |
| G9 catalog import check | Passed: 2 books, 10 units, 32 topics, 200 pages, 0 ready lessons / verified activities |
| Whitespace check | `git diff --check` passed |
| Device/visual/runtime QA | **NOT_RUN** |

These are **291 distinct JVM tests executed in three variants (873 executions)**, not 873 different test cases. Lint passing does not mean warning-free. The XML/HTML reports remain in `app/build/reports/`; [the saved Gradle log](../QA/g9-2026-10-30/gradle.log), [SQLite log](../QA/g9-2026-10-30/sqlite.log), [catalog check](../QA/g9-2026-10-30/catalog.log), and [machine-readable summary](../QA/g9-2026-10-30/summary.json) preserve this run's evidence and APK hash.

## Remaining work and limits

- **Educational readiness remains 0/32.** There are 32 indexed topics, 32 unapproved exercise seeds and zero verified activities. Mathematical/educational/rights review and 2026–2027 alignment are still required. No curriculum-complete claim is made.
- G9 exercises, worked examples, lesson checks, scored results and quick/unit/comprehensive tests must remain unavailable until their content and marking rules pass review.
- The current Library has resource records and saved references, but **no actual SAF PDF/Word file-opening flow**. Attachment metadata, persistent URI access, reader fallbacks, PDF positions and scoped file search remain for the daily-tools phase. No existing file viewer was removed.
- Unified achievements/activity, optional scheduled reminder delivery, richer goal details, in-app font preference, and reviewed interactive geometry remain. See all 45 rows for precise partial/missing capabilities.
- Phone/UI runtime, screenshots, TalkBack, physical font scaling, keyboard/insets, navigation/process recreation and external-file fallback checks are **NOT_RUN**. Instrumentation compilation is not evidence that device tests passed.
- No emulator, ADB, install, uninstall, phone-data clearing, Git commit or push was used.
