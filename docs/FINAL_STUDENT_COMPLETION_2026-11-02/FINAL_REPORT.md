# EN.MATH final student campaign — 2026-11-02

Classification: **STUDENT MVP READY FOR PHONE QA**. This is a bounded MVP testing handoff. Current raster visual acceptance is **BLOCKED** by Windows security policy; physical-phone acceptance is **NOT_RUN**. This is not STUDENT COMPLETE or FULL CURRICULUM COMPLETE.

## 1. Baseline and protection

Official workspace: D:/ENMATH/EN.MATH. Branch: feature/ui-refinement. Initial HEAD: 96cdd3dce57c48a26be58cd61eeb24cc2ee0a9be; origin matched it after fetch. Worktree was clean, including untracked files: no unpublished Gemini changes were found. Prior Phase 7, runtime/Phase 8–9 evidence, host journey tests and the 2026-11-01 Nature report were reviewed. Existing historical passes were not reused as current passes.

A private recovery copy and complete Git bundle were created outside the repository before code changes. No reset --hard, git clean, force push, mass deletion, phone operation or replacement with older GitHub files was performed.

## 2. Problems found before execution

- Release Home advertised a six-question practice destination which Release correctly blocks.
- Choosing geometry still displayed algebra/Nature learning progress and follow-up.
- Progress/review replaced an unreadable saved document with apparent empty successful progress.
- A new unfinished practice after a completed session still appeared complete.
- Hint-only activity was included in first-answer accuracy before an answer existed.
- Saved-session buttons started practice instead of opening the chosen result.
- The Nature mistake notebook lacked question-specific evidence and praised an account with no attempts.
- An educational draft claimed approval although independent approval was not recorded.
- Graph grammar accepted missing multiplication operands and nonfinite coordinates for some functions.
- A failed subject save poisoned storage without notifying Home observers.
- README described an older shared-persona/cloud-like demo entry flow.
- Environment defaults selected JBR 25 and an incomplete alternate Gradle cache; existing JBR 21 and cached dependencies resolved the build environment without upgrades.

These were correctness, availability and storage-feedback risks. No remaining critical crash/data-loss defect is known within the executed test contracts. This does not certify behavior on a phone or independent educational correctness of unapproved drafts.

## 3. Implemented fixes

Home now follows the selected subject and build availability; reference-only routes stay inside the chosen subject. G9 storage read failures fail closed. Failed G9 writes invalidate observers so the preserved-data error becomes visible. Existing schema, owner UUIDs, session generation, migrations and wallet authority are unchanged.

An active Nature draft takes priority while historical sessions remain intact. Hint-only questions retain their hint evidence without inventing an accuracy denominator. Saved sessions open their exact result, and review shows actual per-question retry/hint counts with available existing explanations. Historical wrong-answer text is never fabricated. Draft approval claims and Release practice wording were corrected. Graph validation rejects missing operands, nonfinite coordinates and oversized input.

Version was advanced from code 4 / 1.0.3 to code 5 / 1.0.4; application ID is unchanged. No new curriculum questions, PDFs, backend, Firebase Authentication, teacher features, website or animation system was introduced.

## 4. Grade 9 feature state

The actual catalog has algebra 6 units / 19 researches and geometry 4 / 13, total 10 units / 32 researches. Subject choice persists per owner; independent saveable path scroll states remain. Source references stay visible without sequential locks. Existing reduced-fractions pilot is available independently of textbook publication.

Nature of Numbers explanation is explicitly REVIEW_REQUIRED educational draft. The six-question QA bank supports grading, hints, retry, saved accepted responses, previous/next, result, resume and independent-first-answer metrics in Debug. It remains unapproved and is excluded/blocked in Release. Learning and mistakes update from persisted evidence; no browsing, practice navigation or repeated result opening generates a new reward.

## 5. Baccalaureate feature state

Both source books, all 14 units and all 63 researches were traversed through production Compose navigation. Correct source references were checked for every research. Unsupported source practice remains a pending-availability screen. Cross-stage content is rejected. Existing 24 demo lessons, attempts and older unverified learning records are preserved separately.

Book editions remain 2023–2024 and 2021–2022. No 2026–2027 approval is claimed. Matrix/limit/derivative/integral/probability curated solver cases and unsupported variants were tested; this is not a general symbolic engine.

## 6. Content and supporting features

Approved textbook coverage is still 0/32 G9 and 0/63 BAC in the source catalogs. INDEX_ONLY / REVIEW_REQUIRED metadata is separate from DEMO_LEARNING. No new VERIFIED_LEARNING status or independent teacher/rights approval was granted. The source index itself is not a finished lesson.

Planner retains weekly achievement defaults, editing, confirmed deletion, progress, duplicate-command receipts and existing daily/monthly/time/hybrid records. Other-subject goals do not imply courses. Source bookmarks/search/library remain stage/owner scoped. G9 private PDF/Word links retain Android document-provider behavior; no private or unlicensed PDFs were added. No physics helper file was supplied/published or assigned a Gems price. Existing Gems ledger, unlocked content, guest restrictions and no-retroactive/duplicate reward contracts were retained and tested. Teacher source code was not modified.

See STUDENT_FEATURE_MATRIX.md for every core feature and availability boundary.

## 7. Modified/new implementation files

- README.md
- app/build.gradle.kts
- app/src/hostQa/kotlin/com/ahmadmol/enmath/BaccalaureateJourneyHostTest.kt
- app/src/hostQa/kotlin/com/ahmadmol/enmath/StudentLearningProgressHostTest.kt
- app/src/main/java/com/ahmadmol/enmath/core/data/Grade9NatureLesson.kt
- app/src/main/java/com/ahmadmol/enmath/core/data/Grade9StudyRepository.kt
- app/src/main/java/com/ahmadmol/enmath/core/data/StudentLearningProgress.kt
- app/src/main/java/com/ahmadmol/enmath/features/explore/GraphEngine.kt
- app/src/main/java/com/ahmadmol/enmath/features/student/Grade9Experience.kt
- app/src/main/java/com/ahmadmol/enmath/features/student/Grade9LearningScreens.kt
- app/src/main/java/com/ahmadmol/enmath/features/student/NatureLessonScreen.kt
- app/src/screenshotTest/kotlin/com/ahmadmol/enmath/StudentCampaignRenderedPreviews.kt
- app/src/test/java/com/ahmadmol/enmath/StudentCampaignRegressionTest.kt
- tools/verify_student_campaign.py

Campaign documentation: EXECUTION.md, FINAL_REPORT.md, QA_RESULTS.md, STUDENT_FEATURE_MATRIX.md, KNOWN_LIMITATIONS.md, RELEASE_NOTES.md, SUMMARY_AR.md and EVIDENCE.json in this directory. Local logs, generated APKs and private recovery material are not publication candidates.

## 8. Executed validation

Debug: 444 tests (412 core JVM + 32 Compose/runtime host). Release: 413 (412 core JVM + 1 actual Release-resource host). Python: 34. **891 successful executions**, zero failures/errors/skips in these acceptance suites. Android UI tests compiled; no device execution.

Lint Debug/Release passed with zero Error/Fatal and 72/73 warnings respectively, plus one information each. Both APK builds passed. Detailed scope and earlier failed/superseded attempts are in QA_RESULTS.md. Verification tool: tools/verify_student_campaign.py.

## 9. Blocked / unexecuted validation

Current Layoutlib raster task attempted 36 cases across six production UI scenes, selected 360/393/800dp widths, Arabic/English, Light/Dark, 100/150/200% scale. Every case was blocked at native initialization because Windows Application Control rejected libandroid_runtime.dll. Zero current screenshots were created or visually approved. No policy bypass was attempted. Existing historical renders remain historical only.

Phone install/upgrade/retention, process death and Activity recreation, real document-provider opening/revocation/corruption, IME/insets, system Back gestures, TalkBack, hardware failure and performance remain NOT_RUN. Fixture restoration/storage failures are distinct from these unexecuted tests.

## 10. Data/security review

Known-key-pattern/prohibited-file scan found no matches in current publication candidates; final counts are recorded in EVIDENCE.json. No APKs, PDFs, keys, databases, google-services.json or local.properties are tracked for this delivery. The scan is bounded and is not a proof against every possible secret.

APK ZIP integrity and content gates passed. Release excludes the unapproved six-question asset; no authoring/question-bank directories, PDFs, learner databases or signing keys are bundled. Release is non-debuggable (actual resource host check). allowBackup remains false. Its only requested permission is its own signature-scoped non-exported receiver permission; no dangerous or Internet permission was introduced. No real student data or phone storage was accessed.

## 11. Firebase and offline state

There is no Firebase Authentication implementation/dependency or active Google Services configuration in this project. No production Firebase settings were changed. Identity, catalogs, bundled learning/rendering assets and persistence operate locally. Imported cloud-provider files may require connectivity; the app does not claim to make those remote files offline automatically.

## 12. APK handoff

Delivery: D:/ENMATH/delivery/STUDENT_FINAL_2026-11-02/

Application ID: com.ahmadmol.enmath; versionName 1.0.4; versionCode 5; minSdk 24 / targetSdk 35.

- ENMATH-student-qa-v5-debug.apk: 34,864,547 bytes; SHA-256 8289c108ff1231eccf973e88ced94b7f780e9fc8904e17f11a11bca408941b75
- ENMATH-student-v5-release-unsigned.apk: 25,052,625 bytes; SHA-256 ee75cbf098a00a5547920df4070f998525e63feedffeac540acaf676ad43a43d

Debug signature: valid APK v2; certificate SHA-256 870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63, identical to the preserved local versionCode 4 APK. Package/version/certificate prerequisites permit updating that local build; the installed phone package was not inspected and actual retention is untested.

Release is deliberately unsigned: no approved production signing configuration/key was available to this campaign. It is not directly distributable as a signed production update. Use the fresh delivered Debug APK for authorized future phone QA; the older app/build baseline output was preserved.

## 13. Backup/recovery

Before: C:/ENMATH_BACKUPS/STUDENT_FINAL_2026-11-02_before/ contains the complete verified Git bundle and workspace source copy, including needed ignored reference/configuration files; generated caches/builds and junction traversal were excluded. All 504 baseline tracked files are present with SHA-256 manifest. A clone from the bundle recovered the baseline HEAD and git fsck --full succeeded.

Before-bundle SHA-256: b3983e9566dc11a1006ff4910ed479788e46386b69d0c5559a54c05edd8c533b.

Final source archive, reports, final Git bundle and local evidence are retained outside GitHub. SHA256SUMS.txt binds the local delivered artifacts. The private recovery snapshot must not be published.

## 14. GitHub

Tested application-code commit: 91a6f13682be836682d0b82487607f9f7c170c88.
Normal fast-forward push to origin/feature/ui-refinement succeeded and remote HEAD was verified as exactly that SHA before writing this final report. No force push or main merge occurred.

The subsequent documentation-only completion commit is recorded after its push in the delivery GIT_STATUS.txt, together with final local/remote equality and clean-worktree status. This avoids claiming that a report can contain its own commit hash. Published code: https://github.com/ahmadmol/EN.MATH/commit/91a6f13682be836682d0b82487607f9f7c170c88

## 15. Work requiring external input / later acceptance

Independent scientific/content approval and licensed complete learning material are required for the indexed curriculum. Approved Release signing material is required for production distribution. Windows policy resolution by the device owner/administrator, or an authorized compatible rendering environment, is required for current raster QA. Explicitly authorized phone QA is required for physical acceptance. Historical wrong answers cannot be recovered from data that never stored them.

## 16. Final judgment

The available local student MVP has passed its current code, persistence/SQLite, Compose runtime, lint, build, signature and publication-content checks and is handed off for phone QA. **STUDENT MVP READY FOR PHONE QA**, with current raster QA BLOCKED and phone acceptance NOT_RUN. No full curriculum completion, independent draft approval, signed production release or completed physical acceptance is claimed.
