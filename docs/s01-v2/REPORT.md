# EN.MATH — S01 resource baseline v2 review

Status: Student Home candidate implemented; human visual approval pending. No other canonical screen migrated. No commit or push.

## Audit and source protection

The starting Git tree was clean at `e08acb3`. The archive was extracted locally under ignored `docs/ui-resource-pack/ENMATH_Handoff_v2`; all 81 listed SHA-256 hashes matched. Read README, inventory/dedup/pairing, canonical manifests, design/measurement/component/contrast specifications, Android integration/math boundaries, screen recipes and QA plan. The official inventory has 39 unique core screens and 36 supporting states/flows. Supporting entries are not additional automatic screen implementations.

[Actual 39-screen route map](route_map_before_after.csv) and [36 supporting bindings/gaps](supporting_inventory.csv) record the existing implementation. Every canonical function has an existing destination; this does not mean every old screen implements the new design. Extra reference/catalog routes are retained. Legacy ReferenceHome/Study/Solver/Profile/Graph remain; TeacherHome/Class/Student and interactive limits retain the preceding role pilot. Several references still have fixed dark presentation. Standalone handwriting, account deletion, backend offline/server errors and manual grade editing are not implemented; no fake capability was added.

## Files and components

- `core/design/tokens/ResourceBaseline.kt`: official paired ColorSchemes, typography, spacing/radius/control dimensions. It inherits the existing Arabic IBM Plex Sans Arabic/English Inter fonts and language. `ResourcePackPresentation` always keeps the same composition structure; only token selection changes. It applies only when the existing student route equals `home`.
- `features/student/StudentHomeV2.kt`: actual native S01 composition. HomePanel, HomeAction and HomeSection organize the leading learning action, daily goal ring, two quick actions, upcoming assignment, mastery and recent activity. Existing ProgressFeedback and DetailRow are reused. Action tiles use their taller intrinsic content height equally; no fixed font-dependent height.
- `features/student/StudentLearningScreens.kt`: previous Home retained as `LegacyStudentLearningHome` for comparison; Study and Limits Topic unchanged.
- `core/navigation/ENMathApp.kt`: adds a presentation-only scope around the existing Scaffold/NavHost. No route definition, navigation callback, role/session guard, Activity or lifecycle handling is edited.
- `features/reference/CleanBottomBar.kt`: only while S01's baseline is active, labels use the official 16sp line height and may wrap onto two lines. This fixes the observed large-font truncation of the existing Arabic Solver label. Destinations, order, callbacks, icons and navigation geometry are unchanged; all other screens retain their prior label presentation.
- `StudentHomeV2Test.kt`: emulator-only seeded functional/layout assertions and screenshots.
- `.gitignore`: excludes the extracted handoff and local QA logs. Existing screenshot/build/package ignores remain.
- This report, route/support/contrast CSVs and reproducible emulator QA script document the review.

Repositories, domain models, LearningRules, solver/practice/progress/assignment logic, DemoData, dependencies and all Gradle/Kotlin/AGP/Compose versions are unchanged. Existing selection of last lesson and eligible upcoming assignment, progression values, activity ordering and CTA destinations are preserved. There are no new network requests or UI-background screenshots.

## Layout and themes

Primary composition guide: B03 in Light/B_main_student_10_screens.png. A01 is secondary inspiration, not a second Home or a source of statistics. Dark follows exactly the same composition using the JSON roles; DS1 supplies mood only. Normative S01 hierarchy places the next lesson ahead of the goal, despite the opposite order in one generated illustration.

Phone horizontal inset 16dp; tablet 24dp with capped 720dp reading/content width. Gutter 12dp, panel padding 16dp, section spacing 24dp, shapes 12/16/20dp; prominent CTA minimum 52dp and touch targets minimum 48dp. Typography uses documented sp/line-height roles without density scaling. Arabic is RTL, English is LTR; the goal fraction is explicitly LTR.

Light: background `#F7F9FC`, surface `#FFFFFF`, primary `#175CD3`, primary container `#DCEBFF`, foreground `#0F1F38`.
Dark: background `#0B1322`, surface `#121E30`, primary `#8CB9FF`, primary container `#173E76`, foreground `#F2F6FF`. Primary button foreground uses `onPrimary`, including dark `#092140`; no neon/glow recipe.

[Used contrast pairs](contrast_used_pairs.csv): all audited text pairs meet AA, ranging 5.29–17.16:1. This is token-pair verification plus visual review, not a claim of automated pixel-level contrast for every alpha-blended legacy navigation element.

## Actual QA

`assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest` and `lintDebug` succeeded on the final implementation (1m 2s). Fourteen unit tests passed (DemoLogic 4, ProductRules 8, LessonSequence 2). Lint: zero errors, ten warnings and one informational finding; existing target/dependency versions, data-extraction and KTX recommendations remain unchanged. An SDK XML tool-version warning was observed during an earlier build; it did not prevent the final build.

`responsiveMatrix` passed in 114.294 seconds: twenty layout configurations checked in one instrumentation process. [Actual configuration results](ui-matrix.csv) cover 360×800, 393×865, 412×915 and 800×1280, each Arabic/English × Light/Dark at fontScale 1.0 (16 combinations); 360×800 and 320×720 Arabic × Light/Dark at fontScale 1.3 (four stress combinations). Assertions cover text line/height constraints, no ellipsized text, minimum 48dp targets and 52dp CTA, and equal action-tile heights. Top/lower-content screenshots were captured for each configuration. This does not claim testing every Android device or every screen.

`homeLayoutAndActions` separately passed on the final APK in 44.722 seconds. It exercises continue → existing lesson, practice → existing practice, quick solver → existing solver, view progress → existing progress, Back and Activity recreation, with unchanged completion/attempt/user data. The upcoming assignment action's target and data filtering were preserved by source comparison; its touch target was tested, not a new assignment-submission workflow. In total two distinct UI tests passed, with twenty configurations inside the matrix test.

Screenshots are from the real Compose application on the dedicated emulator at density 160, not conceptual renders. The phone is not seeded, cleared or updated in this task. The emulator stopped responding during an early run; a system Digital Wellbeing ANR appeared. That emulator-only package was temporarily disabled and system animations temporarily reduced for repeatable checks. Earlier interrupted runs are not counted as successful. Checks are grouped into one instrumentation process to avoid repeated slow cold starts. The 1.3x Solver label clipping found during QA was corrected only in S01; the final stress cases passed.

Debug artifact: `dist/EN.MATH-S01-v2-debug.apk`, SHA-256 `F99B71FDB55B821AF67D8A79EEBA52B86AEA8ED2F56C40CD9846F752A1C5725E`. Build packages, logs, screenshots and the extracted ZIP contents remain ignored by Git. No authentication/backend/offline feature was added; the historical full reference-fixture instrumentation suite was not run.

After validation, Digital Wellbeing was re-enabled, window/transition animation scales returned to 1.0 and the temporary animator override removed. Font scale returned to 1.0; the dedicated review emulator is left at 393×865 and density 160. Git HEAD remains `e08acb3` with the implementation uncommitted.

## 393 × 865 screenshots

| Capture | Arabic RTL | English LTR |
| --- | --- | --- |
| Light | [Home](screenshots/393-Light-Arabic-1.0-home.png) | [Home](screenshots/393-Light-English-1.0-home.png) |
| Dark | [Home](screenshots/393-Dark-Arabic-1.0-home.png) | [Home](screenshots/393-Dark-English-1.0-home.png) |
| Lower content, Light | [Mastery](screenshots/393-Light-Arabic-1.0-progress.png), [Recent activity](screenshots/393-Light-Arabic-1.0-recent.png) | [Mastery](screenshots/393-Light-English-1.0-progress.png) |
| Lower content, Dark | [Mastery](screenshots/393-Dark-Arabic-1.0-progress.png), [Recent activity](screenshots/393-Dark-Arabic-1.0-recent.png) | [Mastery](screenshots/393-Dark-English-1.0-progress.png) |

## Deliberate differences / remaining review

The pack explicitly describes generated illustrations as conceptual, with incorrect text/figures and no production layers. This candidate follows canonical numeric tokens rather than claiming pixel-perfect identity. It uses installed Material icons and a simple person icon, not an invented licensed avatar or generated artwork. It does not reproduce iPhone notches, fake time/status controls, graph decorations or image statistics. The current last lesson, mastery and goal/assignment/activity data replace example values.

Existing search/notification controls and the original five mobile destinations (Home, Study, Solver, Lab, Profile), tablet rail threshold and navigation geometry are preserved. In particular the reference's Progress tab is not substituted for the app's Lab; the existing View All Progress action remains. This navigation styling is not a global migration or certification of unrelated screens.

Review the candidate before proceeding. Implementation stops at S01; Study, Solver and Teacher visual work is deferred until explicit approval.
