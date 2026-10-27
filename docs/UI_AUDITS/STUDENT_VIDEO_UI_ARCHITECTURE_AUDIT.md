# Student video / architecture audit — 2026-10-25

Status: implementation audit, pending user visual review. No emulator, ADB, installation, physical-device run, commit or push is authorized or used.

## Evidence and preservation

- Read the full supplied request and `STUDENT_UI_COMPLETION_2026-10-25.md` (119 passing JVM tests reported there).
- Preserved every tracked modification and untracked nonignored file at `<PROJECT>\student-video-20261025-112037`, including a binary Git patch. Existing changes are the working baseline, not changes made by this audit.
- Inspected extracted frames of the supplied 179.21-second, 864×1920 recording, including 00:24, 01:48, 01:58, 02:09, 02:16, 02:18 and 02:20. These are evidence from the user's recording, not execution or acceptance of the modified app.
- ReferenceDesignSystem, PremiumLearning, Theme, MathRender and the previous implementation/reference reports are available. The six original reference images were not supplied in this chat or located in the project; no claim of image-by-image conformity.

## Findings and planned fixes (before implementation)

| Priority / classification | Evidence / source | Cause and targeted action |
|---|---|---|
| P0 confirmed recorded symptom; transition explanation inferred from code | Splash and student bar coexist at 02:18; ENMathApp bottomBar follows currentBackStackEntry, while NavHost retains outgoing entries during fades | Existing exact-tab membership already hides a settled Splash. Gate student chrome on signed-in state and *all visible transition entries*, not selectedTab alone; test null, public, nested, restoration and transition cases. Keep teacher policy intact. |
| P1 confirmed duplicate presentation | 00:24 shows two identical derivative expressions; ProductSeed generates Choice and Numeric variants with identical expressions; ProductSolutionScreen filters then takes two | Dedupe suggestion IDs and normalized displayed mathematical expression before limiting; exclude current expression. Preserve distinct expressions, evaluation points, signs and variable case. Do not modify question catalog, scores or solver algorithms. Explicit empty state. |
| P1 confirmed graph defect | ExploreScreen Y tick range uses screen-Y bounds with the opposite sign from its draw formula | Extract pure coordinate/tick helpers, use the correct inverse Y mapping for panned bounds and test round trips, pan/zoom and function sample positions. |
| P1 confirmed label-density weakness | Tick step depends only on zoom, labels measured in dp, axis names share tick lane | Choose independent X/Y steps from measured text extents, font scale and actual pixel spacing; reserve origin/axis-name space and clip edge labels. Keep DemoGraphEngine grammar. |
| P1 confirmed layout issue | At 02:09 minus and plus wrap separately inside one FlowRow | Keep zoom pair in one LTR row with percentage; separate wrapping Reset/Axes/Grid row; place controls next to graph. Minimum 48dp controls. |
| P1 confirmed UI state issue | SearchScreen passes an always-enabled RefSecondaryButton for blank query; AppViewModel already rejects blank writes | Add enabled/disabled support to shared secondary button and use normalized nonblank search policy in UI and action. Retain owner-scoped history. |
| P1 confirmed misleading settings label | Settings says Local notifications, but only writes notificationsEnabled/reminder preferences; inbox does not consume flag; existing notice says demo | Student-only labels explicitly describe saved preferences and absence of scheduling. Preserve stored values and teacher wording. |
| P2 scoped solver refinements | SolverWorkspace renders 6–7 keypad rows before Solve, three fixed-width tabs, small category chips | Put Solve immediately below expression/tools, responsive student tabs, 48dp categories; keep the existing numerical keys and clear focus/hide IME when using math keyboard/solving. Preserve undo/delete/clear and all supported math. |
| Verified existing behavior | ProductGraphs history back uses popBackStack; question/practice claimFinish, interactive claimCompletion, assignment isSubmitting already guard repeated dispatch | Retain and extend regression tests instead of adding speculative business rewrites. JVM navigation policy/source checks do not prove NavController behavior on a device. |
| P2 existing working curriculum | CourseScreens / ChapterProgressCard use AutoMirrored arrows, weighted wrapping text and central theme | Preserve approved Home, Learning Path, book progress and chapter cards; no reason for wholesale redesign or duplicate arrow removal in already correct cards. |
| Verified with limits | LearningRepository / LocalDemoRepository isolate records by identity; language/theme setters separate from learning records; notification badges count actual unread IDs | Add regression checks, retain ownership mapping and historical records. No guessed migration, new backend, mock filler or reward-rule changes. |

## Student route and ownership inventory

`ENMathApp` owns Scaffold, theme, public/auth routes and the single shared subheader. `studentGraph` in ProductGraphs owns student destination wiring. `StudentHeaderPolicy` delegates Topic/regular Lesson/Progress/history headers and keeps interactive Lesson shared-header behavior.

| Routes | Presentation / state / data |
|---|---|
| home, study, study/catalog, book/{id}, topic/{id} | StudentLearningHome, StudentStudy, CurriculumScreen, StudyScreen, BookDetailsScreen, StudentLimitsTopic; AppViewModel; LearningState/books/completed and curriculum presentation |
| lesson/{id} | LessonScreen or InteractiveLimitsScreen; InteractiveLessonViewModel/SavedStateHandle; LearningRepository and LocalGemsCoordinator |
| practice/{id}, practice/{id}/result | PracticeScreen/LimitsPracticeScreen and results; QuizViewModel/SavedStateHandle; AppViewModel.recordPractice/ProductState attempts |
| solver, solver/scan, solver/history, solver/solution/{id}, legacy solution constant | WorkspaceScreens; SolverViewModel/SavedStateHandle, DemoSolverEngine; owner-scoped history/savedProblems; legacy constant is not an active studentGraph destination |
| explore | ExploreScreen, DemoGraphEngine; local saveable viewport/function state; no repository changes needed |
| progress | ProductProgressScreen; LearningRules/topic/account records |
| assignments, assignments/{id}, assignments/{id}/work, assignments/{id}/result | AssignmentScreens and PracticeScreen; QuizViewModel; AppViewModel submission validation and owner-bound rewards |
| library, search, notifications | LibraryScreen, SearchScreen, NotificationsScreen; AppViewModel; owner-scoped ProductState |
| profile, settings | ProductProfileScreens; AppViewModel / LocalDemoRepository theme-language preferences; LocalGemsCoordinator |
| gems/wallet, gems/review (root graph) | LocalGemsScreens; coordinator, SQLite/policy, saved owner/session and existing award caps |
| splash, onboarding, started, login, account-choice, register-student, register-teacher, verify, forgot, new-password | Root public graph; OnboardingScreens/AuthViewModel; never student tab chrome |

## Validation plan

Add JVM coverage for production navigation transition policy, similar-practice identity/content, viewport transforms/ticks, solver examples and unsupported inputs, history/detail return wiring, one practice result dispatch, search blank handling, account ownership and language/theme data preservation. Re-run existing reward/mastery/submission tests. Execute the requested assembleDebug/testDebugUnitTest/lintDebug command and git diff --check. Report exact counts and limits; user retains all visual acceptance.

## Additional verified findings during detailed review

- `SolverViewModel.reset()` clears expression/recent but leaves undoStack intact. Since logout calls reset, Undo can restore a departed account's input. Clear the transient stack on reset and test it directly.
- The earlier curriculum finding applies to CourseScreens/ChapterProgressCard. The active `study` route also uses **CurriculumScreen**, where two ChevronLeft icons are fixed-direction, the book-open button is 40dp, topic chips are fixed 36dp with one-line ellipsis, and long book summaries are clipped. These are confirmed code issues, not evidence for redesigning the Learning Path. Fix arrows/touch sizing and allow large-font chips to wrap while preserving the normal layout and all progress data.
- Recording frames at 02:30, 02:40, 02:50 and 02:58 were also inspected. At 02:50 Settings repeats its title beneath the shared root header. SettingsScreen renders its own decorative heading as well; omit that student heading and retain the existing root navigation header.
- Search normalization can reduce Arabic diacritics/tatweel-only input to empty. Use the same normalization at the UI enabled check and AppViewModel action so an invisible input cannot become a saved query.
- Existing solution suggestions can include curated practice expressions unsupported by DemoSolverEngine (for example a derivative at a specified point). Preserve the explicit unsupported state and never fabricate a computed answer. This audit does not expand solver grammar.
