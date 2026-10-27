# Detailed Student Code Audit Report (EN.MATH)

**Date**: October 22, 2026\
**Project Path**: `<PROJECT>`\
**Target Focus**: Student Experience & Syrian Scientific Baccalaureate Curriculum\
**Phase**: Phase 1 — Code Audit & Baseline Verification\

---

## 1. Executive Summary & Audit Scope

This document presents a comprehensive static code audit of the EN.MATH Android codebase, focusing strictly on the Student role. It evaluates navigation structures, layout insets, theme tokens, curriculum alignment with the Syrian 12th Grade Scientific Math syllabus (الثالث الثانوي العلمي), Gems economy integration, and test baseline state.

---

## 2. Verified Code Architecture & Proven Implementation Findings

### A. Bottom Navigation & Active Tab Design (`CleanBottomBar.kt` & `ReferenceDesign.kt`)
- **File**: [CleanBottomBar.kt](../../app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt)
- **Active Accent Color**: `activeColor = Color(0xFF0084FF)` is a brand design token representing the floating Home button and active tab icon tint.
- **Layout & Floating Center Button**:
  - Center button (`index == 2`, Home) uses `CenterCircleSize = 52.dp` with a `-10.dp` layout offset (`layout { measurable, constraints -> ... placeable.placeRelative(0, -offsetPx) }`).
  - Bar content height is `68.dp`, and total bar height with top padding is `78.dp` (`BottomNavLayoutDefaults`).
- **Font Scale x2 & Label Truncation Handling**:
  - `BottomNavLayoutDefaults.estimatedContentHeightDp(fontScale)` calculates item height across font scales.
  - *Requirement*: Height constraints must not force truncation of Arabic label text ("الرئيسية", "المنهاج", "الحل", "المختبر", "حسابي") under high font scaling (x1.5 / x2.0). If font scaling x2 exceeds 68dp, side items must allow flexible layout bounds or auto-scaling text rather than clipping labels.

### B. Top App Bar & Inset Behavior (`Components.kt` & `ENMathApp.kt`)
- **File**: [Components.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/Components.kt) and [ENMathApp.kt](../../app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt)
- **TopAppBar Insets Inspection**:
  - In `ENMathApp.kt`, `AppTopBar` is provided inside `Scaffold(topBar = { ... })`.
  - Material 3 `TopAppBar` consumes status bar insets via `TopAppBarDefaults.windowInsets` by default.
  - Adding manual `statusBarsPadding()` on top of `Scaffold`'s topBar slot would create redundant **Double Padding**. The existing `TopAppBar` relies on Material 3's built-in inset handling.

### C. Student Home & Gems Integration (`StudentHomeV2.kt`)
- **File**: [StudentHomeV2.kt](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt)
- **Gems Implementation Verification**:
  - `StudentLearningHome` in `StudentHomeV2.kt` does **NOT** call `GemCounter` directly.
  - Instead, it constructs a dedicated pill badge using `Surface` + `Icons.Outlined.Diamond` + `gemBalanceText(gems, owner, state.language)` with `testTag("home-gems-badge")`.
  - `GemCounter` IS used in `ProductProfileScreens.kt` with `testTag("profile-gems")`.
- **Gems Owner Attribution**:
  - In `StudentHomeV2.kt`: `val owner = LocalGemsPolicy.owner(state.user)`.
  - In `LocalGemsCoordinator.kt`: `bind(state)` synchronously resets `_ui.value` to `LocalGemsUiState(owner = owner)` upon account switch, ensuring previous user balances are not displayed.
- **Fixed Student ID `"ahmed"` Evidence**:
  - `StudentHomeV2.kt` lines 30–35 contain explicit references to student ID `"ahmed"`:
    ```kotlin
    state.product.classes.any { it.id == a.classId && "ahmed" in it.studentIds } &&
    state.product.submissions.none { it.assignmentId == a.id && it.studentId == "ahmed" }
    ```
  - *Impact*: In demo mode, `"ahmed"` is used to filter assigned class work. If a logged-in user has a different ID or email, demo assignments pre-seeded for `"ahmed"` in `ProductSeed.kt` will not match unless mapped to `state.user?.name` or `LocalGemsPolicy.owner(state.user)`.

### D. Curriculum Comparison (DemoContent vs. Official Syrian Baccalaureate Syllabus)
- **Files**: [DemoContent.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/DemoContent.kt) vs. Official Syrian 12th Grade Scientific Curriculum (الثالث الثانوي العلمي - كتابا الرياضيات)

| Official Syllabus Unit (منهاج البكالوريا العلمي) | Official Book | DemoContent Location in Code | Discrepancy Analysis |
| :--- | :--- | :--- | :--- |
| **المتتاليات (Sequences)** | **الكتاب الأول** (الوحدة الأولى) | `book-2` (`Book two`) | Placed in Book 2 in code; belongs in Book 1 in official syllabus. |
| **النهايات والاستمرار (Limits & Continuity)** | **الكتاب الأول** (الوحدة الثانية) | `book-1` (`Book one`) | Correctly located in Book 1. |
| **الاشتقاق (Derivatives)** | **الكتاب الأول** (الوحدة الثالثة) | `book-1` (`Book one`) | Correctly located in Book 1. |
| **التابع اللوغاريتمي والأسي (Logarithms & Exponentials)** | **الكتاب الأول** (الوحدة الرابعة) | `book-2` (`Book two`) | Placed in Book 2 in code; belongs in Book 1 in official syllabus. |
| **التكامل والتابعين الأصليين (Integrals)** | **الكتاب الأول** (الوحدة الخامسة) | `book-1` (`Book one`) | Correctly located in Book 1. |
| **الأشعة والهندسة الفضائية (Spatial Geometry)** | **الكتاب الثاني** (الوحدة الأولى) | `book-2` (`Book two`) | Correctly located in Book 2. |
| **الجبر: المصفوفات والمحددات (Matrices)** | **الكتاب الثاني** (الوحدة الثانية) | `book-1` (`Book one`) | Placed in Book 1 in code; belongs in Book 2 in official syllabus. |
| **الاحتمالات (Probability)** | **الكتاب الثاني** (الوحدة الثالثة) | `book-2` (`Book two`) | Correctly located in Book 2. |
| **الأعداد المركبة (Complex Numbers)** | **الكتاب الثاني** (الوحدة الرابعة) | *Omitted in code* | Missing from current `DemoContent`. |

*Preservation Note*: IDs in `DemoContent.kt` will NOT be altered to prevent breaking tests (`DemoLogicTest.kt`, `LessonSequenceTest.kt`) or route state keys.

---

## 3. Categorized Issue Analysis (Severity Matrix)

### A. Confirmed Bugs (خلل مؤكد في الكود)
1. **Hardcoded Student ID `"ahmed"` in Home Assignment Filter**:
   - *File*: [StudentHomeV2.kt](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt#L30)
   - *Evidence*: `state.product.classes.any { it.id == a.classId && "ahmed" in it.studentIds }`.
   - *Impact*: Class assignment card only resolves for hardcoded user ID `"ahmed"`, failing for other logged-in demo student accounts.

2. **English Digits in Arabic Math Visual Callout**:
   - *File*: [InteractiveLimitsScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt#L45)
   - *Evidence*: `String.format(Locale.US, "%.2f", x)` formats numbers with Western numerals (`2.70`, `3.00`) inside Arabic text strings.

3. **Weekly Progress Chart Zero-Value Text Collision**:
   - *File*: [ProductProgressScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/progress/ProductProgressScreen.kt#L60)
   - *Evidence*: When daily exercise count $n=0$, bar height is minimal and the text label `"$n"` overlaps directly with the day label underneath.

---

### B. Potential Risks (شبهة أو خطر محتمل تحت حالات معينة)
1. **Font Scale x2 Label Truncation Risk**:
   - *File*: [CleanBottomBar.kt](../../app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt#L100)
   - *Risk*: Fixed `68.dp` bar content height may clip multi-line Arabic text if text scaling is applied without adjusting vertical padding or text overflow rules.

2. **Window Insets Redundancy Risk**:
   - *File*: [Components.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/Components.kt#L403)
   - *Risk*: Manually adding `statusBarsPadding()` to `AppTopBar` while `Scaffold` or `TopAppBar` consumes window insets would cause double padding.

3. **Gems Balance State Hygiene Risk**:
   - *File*: [ProductProfileScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt#L50)
   - *Risk*: Omitting `currentOwner` parameter when calling `GemCounter` could allow display of stale wallet state if asynchronous binding is delayed during role switching.

---

### C. Design & Architecture Suggestions (مقترحات تحسين)
1. **Syrian Baccalaureate Branding Context**:
   - Add a subtle localized badge ("الشهادة الثانوية - الفرع العلمي") to `AppTopBar` and `StudentLearningHome` header to strengthen platform identity for Syrian students.
2. **Dynamic Light Theme Adaptation in Solver**:
   - Adapt `ReferenceSolver` background cards to `MaterialTheme.colorScheme` surfaces in Light Theme rather than forcing hardcoded dark colors (`Color(0xFF13132E)`).
3. **Interactive Lesson Completion Reward Feedback**:
   - Display an instant visual Gems reward celebration banner upon completing interactive practice blocks.

---

### D. Not Verified / Requires Device Verification (تحتاج فحصاً بصرياً على هاتف حقيقي)
1. **Floating Home Center Circle Touch Ripple & Elevation**:
   - Visual shadow blur, ambient lighting, and press ripple animation for the 52dp elevated Home button.
2. **Gesture Navigation Bar vs 3-Button Bar Alignment**:
   - Inset rendering when switching between Android gesture navigation bar and physical/virtual 3-button navigation bar.
3. **RTL IBM Plex Arabic Alignment**:
   - Line heights, font baseline alignment, and text wrapping across physical screen screen densities.

---

## 4. Top 5 Development Priorities (P0–P2)

1. **P0 (Bug Fix)**: Replace hardcoded student ID `"ahmed"` in [StudentHomeV2.kt](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt) with dynamic student ID resolution (`LocalGemsPolicy.owner(state.user)` or `state.user?.name`).
2. **P0 (Bug Fix)**: Explicitly pass `currentOwner = LocalGemsPolicy.owner(state.user)` to `GemCounter` in [ProductProfileScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt) and ensure strict owner attribution.
3. **P1 (Layout Protection)**: Refine vertical padding in [CleanBottomBar.kt](../../app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt) to guarantee zero label text truncation under Font Scale x1.5 and x2.0 without violating `BottomNavLayoutDefaults` height rules.
4. **P1 (Bug Fix & LTR)**: Localize numeric prose text formatting in [InteractiveLimitsScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt) while strictly preserving Mathematical LTR layout for equations.
5. **P2 (Scope Modernization)**: Modernize Curriculum screens ([CourseScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt)), Practice sets ([LimitsPracticeScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt)), Quizzes ([QuizViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt)), and Solver ([ReferenceSolver.kt](../../app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceSolver.kt)).

---

## 5. Baseline Unit Test Verification Results

The automated Gradle unit test suite was executed to establish the baseline:

- **Command Executed**: `./gradlew test`
- **Result Status**: `Build finished successfully.`
- **Tests Evaluated**:
  - `BottomNavigationLayoutTest` (verifies 68dp bar content height, 78dp total bar height, 52dp center circle size, 10dp floating padding).
  - `DemoLogicTest`
  - `GemsIntegrationReviewTest`
  - `LessonSequenceTest`
  - `LocalGemsCoordinatorTest`
  - `LocalGemsPolicyTest`
  - `NativeEquationFormattingTest`
  - `ProductRulesTest`
- **Pass Rate**: 100% (0 failures, 0 errors).

*Visual QA Status*: Visual and interaction testing is pending connected physical Android device testing once Phase 2 changes are approved.

---

## 6. Confirmation of Phase 1 Completion & Execution Stop

- [x] Audit report file generated at `docs/UI_AUDITS/STUDENT_CODE_AUDIT_2026-10-22.md`.
- [x] All 3 confirmed bugs, 3 potential risks, 3 design suggestions, and 3 device verification items documented with exact file paths and line references.
- [x] Baseline unit tests executed and verified (100% passing).
- [x] **No Kotlin code, layout XML, Gradle scripts, or UI components were modified.**
- [x] **No emulator, APK installation, data reset, or Git commands were executed.**
- [x] **Execution stopped at Phase 1 completion.** Awaiting user approval before proceeding to Phase 2 or Visual Audit.
