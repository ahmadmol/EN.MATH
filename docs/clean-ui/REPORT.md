# EN.MATH — Clean UI and local role workflows

## Delivered

- Shared native Material 3 bottom navigation: rounded surface, consistent icons and labels, purple selected indicator and standard touch targets. Student main tabs remain Home, Study, Solver, Lab and Profile; teacher tabs are Dashboard, Classes, Assignments, Resources and Profile.
- Consistent card borders, padding, corner radii, label sizes and button heights; navy/purple identity retained. Email and password fields use LTR text inside Arabic RTL layouts.
- Role-aware student/teacher login, input validation and links to the existing registration, verification and password-recovery screens. The login screen explicitly explains that accounts are simulated locally.
- Teacher workspace uses repository class counts, distinct students and mastery statistics. Its buttons open real local class management, assignment publishing, curriculum, solver and graph screens.
- Teacher resources link to the curriculum, solver and graph tools. Native teacher lesson/topic routes and a full-catalog entry connect the tools to the existing local lessons.
- Student home reads the current name, streak, completed lessons, last lesson and book completion from local state. The lesson count uses words rather than an RTL-reversed fraction.
- Student profile opens assignments, progress, library and settings. Both profiles support confirmed sign out and role switching while retaining learning data.

## Files

New UI files under `app/src/main/java/com/ahmadmol/enmath/`:

- `features/reference/CleanBottomBar.kt`
- `features/reference/SessionExit.kt`
- `features/auth/CleanLoginScreen.kt`
- `features/teacher/TeacherWorkspace.kt`

Modified UI/navigation files under the same source root:

- `core/design/Components.kt`, `core/design/Theme.kt`
- `core/navigation/ENMathApp.kt`, `core/navigation/ProductGraphs.kt`
- `features/auth/AuthScreens.kt`
- `features/reference/ReferenceDesign.kt`, `ReferenceHome.kt`, `ReferenceProfile.kt`, `ReferenceStudy.kt`
- `features/teacher/TeacherReferenceNavigation.kt`, `TeacherResourcesScreen.kt`

Tests: new `CleanExperienceTest.kt`; updated `TeacherReferenceScreensTest.kt` and relevant `AppFlowTest.kt` cases. Documentation: `README.md`, this report; `.gitignore` excludes local QA logs. No toolchain or dependency versions changed.

## Validation

Completed on 2026-10-11 using JDK 17 and the existing Android SDK 35/toolchain.

| Check | Actual result |
| --- | --- |
| `:app:assembleDebug`, `:app:assembleDebugAndroidTest`, `:app:testDebugUnitTest`, `:app:lintDebug` | BUILD SUCCESSFUL; final run 1m 16s |
| DemoLogicTest / ProductRulesTest | 12 tests, zero failures/errors |
| 360 × 800 dp emulator: CleanExperienceTest, TeacherReferenceScreensTest, ReferenceScreensTest | 6 passed; login validation/logout, selected navigation and ≥48dp nav targets, teacher curriculum/lesson/solver/graph/resources, class creation, role switch/session recreation, student reference screens, English/Arabic switch and math controls |
| 360 dp emulator: selected AppFlowTest cases | 4 passed: student registration with invalid/valid verification; teacher registration/recovery; class creation and four-step assignment publication; six-question practice/completion/result persistence |
| Final APK, 800 × 1280 dp emulator | 2 passed: login/logout for both roles and teacher curriculum/tool navigation |
| Lint | Zero errors; 10 warnings and 1 informational item retained (dependency/target SDK recommendations, data-extraction rules, state boxing and KTX suggestions) |
| `git diff --check` | Passed; only existing LF/CRLF Git notices |
| Redmi Note 13 (`23129RAA4G`) | Current Debug APK installed with `adb install -r`; launch returned `Status: ok`; MainActivity verified RESUMED and process running. Phone preferences were not reset. |

This is 10 distinct UI test cases, with two repeated on the wide display (12 successful UI test executions). It is not a claim that every older instrumentation test passed.

Visually reviewed the current 360dp student home/login, teacher dashboard/account/curriculum; 800dp login/dashboard; and the actual phone teacher screen. Inspected spacing, Arabic labels, selected navigation and content bounds. Narrow content scrolls; wide login/content stay constrained instead of stretching cards. A reversed lesson-count fraction and inconsistent selected-tab color found during review were corrected. Reference math remains LTR. Light/System form and management flows were exercised by AppFlowTest; reference pages retain their fixed dark identity.

Local evidence: `build.txt`, `test-build.txt`, `ui-360.txt`, `auth-management.txt`, `ui-800.txt` and `screenshots/` in this folder (ignored by Git). An initial emulator run began before Android services were ready; after boot completed, installation succeeded. One earlier run was interrupted during QA and rerun successfully; neither interrupted run is counted as a passing test. A Lint indentation error was fixed before the successful final build.

Current artifact: `dist/EN.MATH-clean-ui-debug.apk` (ignored), SHA-256 `79bd7228aecbf366d955fc5128c43dcf38dfc12778555d6370f52f908c468d8d`. It matches the APK produced by the final Gradle build and installed on the phone.

## Remaining limits

This remains a local functional prototype. Registration, verification, recovery, classroom membership and cross-role data simulate a shared local dataset; there is no real authentication server, account isolation, email delivery or remote synchronization. No backend was added.

The native reference curriculum retains illustrative percentages and profile exam metadata from the reference screens. Reference screens retain their midnight theme; Light/System themes apply to the existing non-reference forms and management pages. The entire older AppFlowTest suite was not rerun; only relevant cases listed in validation were selected.

New screens and reference solver/math UI use Compose. Existing offline KaTeX rendering in legacy lessons/practice was retained. No general math solver, OCR service or production release is implied by this UI update.

This update is prepared for publication to `ahmadmol/EN.MATH` at the user's request. Generated packages and local QA evidence remain excluded from Git. Burhan was not modified.
