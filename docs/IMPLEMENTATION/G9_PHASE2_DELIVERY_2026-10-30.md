# Grade 9 phase 2 — original learning pilot, practice and files

Workspace: `D:\ENMATH\EN.MATH`. Date: 2026-10-30. The Phase 1 audit/validation and the actual working tree were inspected first. Existing unpublished changes were retained; no commit, push, checkout, device installation, emulator, ADB or phone-data operation was used.

## Implemented behavior

| Area | Phase 2 delivery |
|---|---|
| Home | Smaller 48dp character, consistent 16dp card padding / 20dp corners, narrower readable content width, compact greeting, direct original-pilot entry, real saved-attempt count, existing plan/path/notifications/tools. All 32 source topics remain in their original order. |
| Algebra | Source units retained, search retained, explicit entry to the reduced-fractions pilot; other Algebra content remains under preparation. |
| Geometry | Real four-unit index retained, clear preparation status; no invented explanations or figures. |
| Exercises | Book/unit/topic filters retained; only the matching Algebra pilot offers start/resume. No quick/unit/comprehensive exam is fabricated. |
| Profile | Replaced only the G9 presentation on the same route with compact identity, actual wallet, plan, study links and tools/preferences. Existing assignments, older-record access, language/settings, local-file switch confirmation remain accessible. BAC/Teacher profile presentation is retained. |
| Lesson | Reusable block/example/card presentation: objective, intuition, definition, formula with conditions, reasoning checks, two progressively revealed examples, summary, understanding checkpoint and practice entry. Reading section is saved; its position indicator is explicitly not mastery. |
| Practice | Four original questions, exact fraction input, Arabic/Persian/English digits, optional hints, explicit skip, locked checked answers, correction, next question, durable result and fresh retry. Section/question changes return the list to the top. Finishing replaces the active practice destination so Back cannot reopen a finished session. |
| Evidence | Actual checkpoint evidence, saved attempts, answer counts, hints and historical results appear in existing Progress routes; actual errors/skips from the latest pilot session appear in the existing `review` route. Nothing is credited for merely browsing. |
| Source/library | Existing source bookmarks and planner pinning are retained. The pilot links to the existing source-detail template through `g9/reference/{id}`. Saved pilot references open the lesson. |
| PDF/Word | SAF document picker, persistent read URI grant, validated MIME/URI/name/size metadata, owner-scoped file list/search, PDF page preview and saved page position, external PDF/DOC/DOCX reader opening, confirmed link removal and explicit error states. |

The five existing main tab routes stay intact. Added focused routes: `g9/practice`, `g9/result/{id}`, `g9/reference/{id}`, `g9/files`, `g9/file/{id}`. These are guarded by the current G9 stage/catalog; navigation chrome remains hidden outside the main tabs. The original `lesson/G9-ALG-U01-L03` route now opens the original pilot; all other G9 source lesson routes retain their unavailable/reference state. Opening the pilot also records a visit through the existing source workspace so Home can resume it.

## Content verification and honest readiness

Pilot: **كسور مختزلة / Reduced fractions**, existing source topic `G9-ALG-U01-L03`, source book `G9-ALG`, 2025–2026, PDF/printed pages 14–15.

The local PDF was hashed again and its 116-page count checked. SHA-256:

`b6a2d13da67f90afbc6df0ffde60de2827d9359021f2ce1924d2961fa1aed59f`

Pages 14–15 were freshly rendered with the existing Poppler runtime and visually inspected against the pilot scope. The book describes positive integer fractions, irreducibility and GCD simplification. The pilot's explanations, examples, questions, hints and corrections are newly authored EN.MATH material. No scanned textbook page, copied textbook paragraph or source exercise is bundled in this change. Source images remain in the private cache outside the repository.

Mathematical review:

- Scope explicitly requires positive integer numerator and denominator. The GCD is nonzero; division of both by it preserves their quotient. If the resulting integers shared a divisor greater than 1, multiplying that divisor by the original GCD would contradict its maximality.
- Equality and irreducibility are separate requirements. The grader uses exact bounded `BigInteger` arithmetic; it rejects unreduced equivalents, zero denominators, decimals, signed inputs outside this pilot's scope and malformed/oversized input. It accepts normalized Arabic/Persian/English digits and supported fraction slashes.
- Worked examples were reviewed step by step and independently checked: `42/70 → 3/5` with GCD 14, and `84/126 → 2/3` with GCD 42.

| Item | Original input | Exact reduced answer | GCD |
|---|---|---|---|
| Practice 1 | 42/56 | 3/4 | 14 |
| Practice 2 | 48/180 | 4/15 | 12 |
| Practice 3 | 91/143 | 7/11 | 13 |
| Practice 4 | 31/42 | 31/42 | 1 |
| Understanding checkpoint | 54/90 | 3/5 | 18 |

The JVM suite verifies the published question objects using independent cross products and GCD checks. `tools/verify_g9_pilot.py` checks the actual Kotlin question values using Python `Fraction`, validates the original source PDF hash/page count, and checks both worked examples. The earlier 32 exercise seeds and older lesson drafts are not released.

**Readiness:** one bounded original EN.MATH pilot is available, with source inspection and mathematical/software checks. The other **31 topics remain unavailable for learning/practice**. The existing source catalog still reports **0/32 textbook lessons READY**; it has not been relabeled to imply that a complete textbook topic corpus is approved. No independent human educator/Ministry review was performed, and official 2026–2027 alignment remains unverified. No Gems or mastery certificates are awarded by the pilot.

## Persistence, files and isolation

`LocalDemoRepository.grade9Study` is an additive repository using existing `StudentScope`, `ContentRef`, `Attempt`, `PlanWrite` and preference contracts. The edition used by existing student data is unchanged. A separate versioned `enmath_g9_study_v1_<UUID>` document binds owner, stage, edition, pilot revision and full source-reference identity. No existing learner, planner, source bookmark, teacher or wallet document is migrated or rewritten.

Attempts contain actual submitted answers, hint IDs, elapsed seconds and an immutable command ID. Correctness is recomputed from the trusted pilot questions; a UI-supplied score is never accepted. Repeated identical submissions are idempotent; changed payloads with the same ID are rejected. Corrupt, future, wrong-owner or wrong-revision documents fail closed without replacement. Failed commits block subsequent writes until restart. Checked answers/hints/next-question state are saved as a draft; unchecked input is not guaranteed to survive abrupt process death. Saved results are durable.

Document handling uses read-only `content://` access from the system picker, not raw filesystem paths or arbitrary URLs. MIME allowlist: PDF, DOC and DOCX. Provider metadata/access checks and PDF rendering run off the UI thread. PDF bitmap allocation is bounded to 1200×1600; descriptors, pages and renderers are closed. External opening grants read access only and handles an absent reader or inaccessible document. Local/downloaded providers can work offline; cloud-only documents may need connectivity. Protected/nonseekable/malformed PDFs may require an external reader. Internal PDF preview is raster-based; accessible text/zoom is available through a suitable external reader. Removing a saved link does not delete the original or revoke a URI another profile may still use.

## UI/UX audit and preview evidence

The audit inspected the actual Compose source and navigation wiring for Home, Algebra, Geometry, Exercises and Profile. It found excessive pre-path content, oversized Home/Profile artwork, broad content widths, source badges in a nonwrapping row, stale all-content-unavailable labels, and a finished-practice Back-stack trap. These were addressed in the current implementations. Source badges now wrap; the shared source-detail layout otherwise retains its behavior.

`app/src/debug/java/com/ahmadmol/enmath/Grade9Phase2Previews.kt` adds **11 debug preview surfaces × 24 configurations** using the existing `StudentSurfaceMatrix`: 360/393dp, Arabic/English, Light/Dark, font scales 100%/150%/200%. Surfaces: Home, Algebra, Geometry, Exercises, Profile, lesson, worked example, summary/checkpoint, practice, result and files. Fixtures use memory only; no real preferences, files or wallets are opened. Preview records are visibly named PREVIEW and excluded from release builds.

The preview definitions compile. Android Studio was found installed but not running, so no rendered preview screenshots are claimed. This is **not** a pixel-level or physical-device visual signoff. Instrumentation UI tests also compile but are NOT_RUN. Required remaining runtime checks include clipping at 200%, TalkBack order, actual back/scroll restoration, process recreation, provider permission restoration, offline/cloud providers, protected PDFs, Word reader absence and keyboard/insets. No emulator or ADB was used to substitute for those checks.

## Files changed in this phase

New:

- `app/src/main/java/com/ahmadmol/enmath/core/data/Grade9Pilot.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/Grade9StudyRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/student/Grade9LearningScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/profile/Grade9ProfileScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/library/Grade9Files.kt`
- `app/src/debug/java/com/ahmadmol/enmath/Grade9Phase2Previews.kt`
- `app/src/test/java/com/ahmadmol/enmath/Grade9PilotLearningTest.kt`
- `app/src/androidTest/java/com/ahmadmol/enmath/Grade9PilotUiContractTest.kt`
- `tools/verify_g9_pilot.py`
- This report and `docs/QA/g9-phase2-2026-10-30/` evidence.

Updated existing working-tree files:

- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt` — additive repository member only.
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt` — guarded routes, pilot dispatch, existing repository connections, result replacement and resume visit.
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt` — focused-route titles/header ownership.
- `app/src/main/java/com/ahmadmol/enmath/features/student/Grade9Experience.kt` — compact main UI, pilot availability and evidence entry.
- `app/src/main/java/com/ahmadmol/enmath/features/student/DualStageExperience.kt` — pass G9 repository and connect the existing mistakes route.
- `app/src/main/java/com/ahmadmol/enmath/features/courses/SourceCurriculumScreens.kt` — original-pilot links/status, existing scoped Progress, source badge wrapping and saved-reference labels.
- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt` — G9-only dispatch to compact Profile; BAC/Teacher retained.
- `app/src/test/java/com/ahmadmol/enmath/Grade9MainExperienceTest.kt` — verify focused new routes hide the G9 tabs.

## Validation

Final settled-source Gradle run: **BUILD SUCCESSFUL in 3m 1s**. No source changes were made while this final run executed. An intermediate pass had failed Lint; the final pass has no Lint errors.

| Check | Final measured result |
|---|---|
| JVM Debug | 302 tests, 0 failures/errors/skipped |
| JVM Release | 302 tests, 0 failures/errors/skipped |
| JVM W1Qa | 302 tests, 0 failures/errors/skipped |
| Host SQLite | 10 tests passed against temporary databases using the actual wallet schema |
| Debug build | PASS; `app/build/outputs/apk/debug/app-debug.apk` |
| Lint Debug | PASS; **0 errors, 66 warnings, 1 informational issue** |
| Instrumentation compilation | PASS; actual instrumentation execution NOT_RUN |
| Original pilot/source validation | PASS; source SHA/page count, 2 worked examples, 4 practice answers + 1 checkpoint |
| Catalog check | PASS; source metadata still has 10 units, 32 topics and 0 READY textbook lessons |
| Whitespace | `git diff --check` PASS |
| Preview/device visual QA | Preview definitions compile; rendered previews/screenshots and device checks NOT_RUN |

There are **302 distinct JVM tests executed in three variants: 906 executions**, not 906 different tests. The 11 new pilot regressions cover exact grading, source identity, real results/skips/mistakes, persistence/restart, duplicate receipts, stale owner/stage/edition, corrupted/future documents, failed commits and file metadata validation. The five added Lint warnings are advisory KTX conversion suggestions in the new store/file code; no checks were disabled and no Lint baseline was added.

Evidence: [Gradle log](../QA/g9-phase2-2026-10-30/gradle.log), [SQLite log](../QA/g9-phase2-2026-10-30/sqlite.log), [pilot content checks](../QA/g9-phase2-2026-10-30/pilot-content.json), [catalog check](../QA/g9-phase2-2026-10-30/catalog.log), [machine-readable summary and APK hash](../QA/g9-phase2-2026-10-30/summary.json).

Commands:

```powershell
$env:JAVA_HOME='<JDK_21_PATH>'
.\gradlew.bat :app:test :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain
python tools/test_local_gems_sqlite.py
python tools/verify_g9_pilot.py
git diff --check
```

Remaining product work: independently reviewed content for the other topics; educator review of the original pilot; official year alignment; additional question types and unit/exam rubrics; optional scheduled reminders; richer resource classification and accessible PDF text/zoom; full device QA. The whole Grade 9 experience/curriculum is not declared complete.
