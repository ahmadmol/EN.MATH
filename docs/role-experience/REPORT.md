# EN.MATH — Role experience pilot

## Audit and reuse

The pre-change `ReferenceHome`/`ReferenceStudy` emphasize reference fixtures and force a dark palette. `TeacherReferenceHome` delegates to the student reference home. `TeacherReferenceDashboard` contains screenshot fixture statistics; `TeacherWorkspace` reads real local statistics but uses the reference layout. Those implementations are retained for comparison, not deleted.

The existing `ENMathTheme`, fonts, `Space`, `AppCard`, buttons, typography and `AdaptiveGrid` are reused. `LearningState`, `LocalDemoRepository`, catalog/domain models, `LearningRules`, `AppViewModel.complete`, `recordPractice` and `QuizViewModel` remain the sources of truth. Existing class/assignment/resource routes and solver/graph tools are kept.

The old lesson is a long explanatory page with one optional checkpoint. Practice already has choice, numeric, expression and true/false questions, grading and persistence. The pilot replaces only the guided limits lesson and limits practice/result presentation, delegating evaluation and saving to the existing logic.

## Architecture changes

- `core/design/tokens/ExperienceTokens.kt`: student/teacher content constraints and layout tokens derived from existing spacing. No duplicated role themes or brand colors.
- `core/design/components/common/ExperienceComponents.kt`: constrained scrollable pages, panels, section headings, accessible detail rows and native LTR short equations.
- `core/design/components/student/InteractiveComponents.kt`: learning interactions and restrained hint/feedback motion.
- `core/design/components/teacher/ProductivityComponents.kt`: summaries, insight panels, class/student rows, action/filter bars and performance presentation. No streaks, games or decorative animation.
- `features/courses/interactive/LessonBlocks.kt`: presentation sequence referencing existing lesson/question objects. Explanation, Equation, Visual, WorkedExample, MultipleChoice, NumericQuestion, MathExpressionQuestion, TrueFalse, Hint, Checkpoint and Completion types.
- `InteractiveLessonScreen`: reusable renderer receiving a block sequence, visual slot and question-explanation provider. `InteractiveLimitsScreen` is a thin adapter for the first lesson; no lesson-per-composable rewrite.
- `InteractiveLessonViewModel`: SavedStateHandle-backed position, answers and checked state. Domain progress remains in the repository. Advancing past questions requires correct checked feedback; previous ideas can be revisited.
- `SolverExperienceContract.kt`: UI intent boundary for QuickSolve/LearnSolution. Quick solver still opens the existing implementation. Guided solution interactions are intentionally deferred; no engine was rewritten or falsely exposed as available.

Repository, persistence codec, domain models, AppViewModel/business rules, Gradle/Kotlin/AGP/Compose versions and dependencies were not changed.

## Student pilot

Home prioritizes greeting → continue learning → actual daily goal → limits practice recommendation → quick solver → unsubmitted upcoming assignment → progress → actual recent activity. No separate mock XP/metrics dashboard was introduced.

Study → Limits Topic → guided `limits-practice` lesson → `limits-practice-set` → result uses the existing route IDs. Eleven blocks cover a short concept, scrub-able native graph of `f(x)=x+2` away from `x=3`, prediction and feedback, two-sided understanding, polynomial substitution rule, the existing removable-limit example at 1, an expression challenge, true/false verification, hint, numeric checkpoint and completion.

The line's hole at `(3,5)` distinguishes the limit from the function value. The existing worked example factors `x²−1`, cancels only for `x≠1`, then takes the limit `2`. Questions reuse `limits-q1`, `q4`, `q5`, `q6`; practice reuses all six existing limits questions. No general symbolic solver was added.

Student components: `InteractiveChoice`, `NumericAnswer`, `MathExpressionAnswer`, `HintReveal`, `AnswerFeedback`, `LessonCheckpoint`, `WorkedExample`, `ProgressFeedback`. Wrong answers receive a reason and hint, not a bare error. Lesson/practice/short math UI is native Compose; no new WebView is used.

## Teacher pilot

Independent Teacher Home → Class Details → Student Details uses the same classroom/catalog/progress data.

Home shows the signed-in identity and a compact summary, support insight, classes, upcoming assignments, recent submissions and real route actions. Displayed counts are computed, not the illustrative 54/80.5% figures. The average summarizes the current topics of enrolled classes. The support filter explicitly uses mastery below 45%.

Class Details has overview/student/curriculum/assignment filters, search, support filtering, invite-code copying and student navigation. Curriculum rows show class topic mastery, students needing support and assigned practice, derived from the same topics/questions. Student Details reads the same `LearningRules.topic` values as student progress. It opens submitted work for review; when no submission exists the action is disabled and explained.

Teacher components: `TeacherSummary`, `TeacherInsightPanel`, `ClassRow`, `ClassPerformance`, `StudentPerformanceRow`, `AssignmentSummary`, `TeacherActionBar`, `TeacherFilterBar`.

## Navigation and scope

Existing Home/Study/Topic/Lesson/Practice and teacher class/student routes are retained. Main teacher tabs remain Home, Classes, Assignments, Resources, Profile. Mobile navigation uses the existing clean bar; tablet management screens retain a themed rail. Reference Solver/Lab/curriculum routes are not removed. Their later migration should keep aliases/deep links and expose tools through Resources, after review.

Only these first slices were changed. Other topics/lessons, assignments, resources, authentication and reference profile/solver/graph layouts keep their existing implementations. Those reference pages still use their fixed dark presentation; Light/Dark support in this pilot is through the one existing app theme, not a blanket redesign of legacy pages.

## Actual validation and screenshots

`assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest` and `lintDebug` completed successfully on the final source (1m 18s). Fourteen unit tests passed: four demo logic, eight product rules and two lesson sequence tests. Lint reported zero errors, ten warnings and one informational finding. Warnings cover the existing target/dependency versions, data extraction configuration and KTX suggestions; versions were intentionally retained.

`RoleExperienceTest` runs only on the emulator with seeded local data. It exercises wrong-answer feedback and progression gating, hint reveal, the graph slider, all question types, checkpoint state after activity recreation, practice grading/persistence, role switching, identical student/teacher mastery, class tabs, search/support filters, invitation copying and disabled review when no submission exists. A first run exposed a restored child route when selecting Home; the navigation fix passed the subsequent run. Visual review also corrected RTL step labels and the direction of negative numeric choices.

The emulator uses density 160 so the requested dimensions equal dp dimensions. The tested first slices support Light/Dark; legacy reference pages were not included in that theme claim. Tests are local demo integrations, not backend or physical-phone tests. The full historical reference-fixture instrumentation suite was not run.

| Emulator configuration | Integration cases | Result |
| --- | --- | --- |
| 360 × 800, Dark | 2 | Passed |
| 393 × 865, Dark | 2 | Passed |
| 393 × 865, Light | 2 | Passed |
| 800 × 1280, Dark | 2 | Passed |

These are two distinct end-to-end cases across four configurations (eight successful case executions), all verified with the final APK. The 393 Dark/Light images were refreshed after the numeric-direction correction. Compact teacher filters scroll horizontally; tablet topic mastery uses the existing adaptive grid. No overlap or inaccessible pilot controls was found in the reviewed states; this is not a claim about every font scale or device.

Changed integration files: `core/navigation/ProductGraphs.kt`, `core/navigation/ENMathApp.kt`, `AppFlowTest.kt`, `CleanExperienceTest.kt`, `README.md` and `.gitignore`. New files are the design/presentation files described above, `RoleExperienceTest.kt`, `LessonSequenceTest.kt` and this report. No Git commit was made; the original HEAD remains `004e2f8`.

Local debug package: `dist/EN.MATH-role-ux-debug.apk`, copied from the successful current build. SHA-256: `43CE21F7424EA05354908D1D61C412BE6ECFFCD9168C857D173E5BEC46372F33`. It is ignored by Git. The real phone was not updated as part of this visual-review pilot.

### 393 × 865 gallery

| Screen | Dark | Light |
| --- | --- | --- |
| Student Home | [Screenshot](screenshots/393-Dark-student-home.png) | [Screenshot](screenshots/393-Light-student-home.png) |
| Interactive Limits Lesson | [Screenshot](screenshots/393-Dark-interactive-limits-visual.png) | [Screenshot](screenshots/393-Light-interactive-limits-visual.png) |
| Student Practice | [Screenshot](screenshots/393-Dark-student-practice.png) | [Screenshot](screenshots/393-Light-student-practice.png) |
| Practice Result | [Screenshot](screenshots/393-Dark-practice-result.png) | [Screenshot](screenshots/393-Light-practice-result.png) |
| Teacher Home | [Screenshot](screenshots/393-Dark-teacher-home-sara.png) | [Screenshot](screenshots/393-Light-teacher-home-sara.png) |
| Class Details | [Screenshot](screenshots/393-Dark-class-details-sara.png) | [Screenshot](screenshots/393-Light-class-details-sara.png) |
| Student Details | [Screenshot](screenshots/393-Dark-student-details-sara.png) | [Screenshot](screenshots/393-Light-student-details-sara.png) |

Screenshots and logs are local evidence ignored by Git. Screens capture scroll positions; below-fold sections remain scrollable. Layout review checks the seven screens and additional lesson/feedback/curriculum states. There is no claim of completing all topics or teacher workflows.

## Suggested next phase — after visual approval

Generalize the validated block renderer to one additional topic before migrating the full curriculum. Separate block content from visual providers as new visuals are needed. Extend the teacher presentation to assignment review only after class/student evidence is accepted. Replace remaining fixed-dark reference presentations gradually using the shared theme, preserving route aliases. Implement LearnSolution against the existing Solution/steps contract before making that mode available.

This is a local pilot; no commit or push is performed. No new backend, authentication service, remote messages or database was added. Burhan was not modified.
