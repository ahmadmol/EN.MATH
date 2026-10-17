> Latest visual-polish review (2026-10-15): [report](docs/visual-polish/REPORT.md), [actual QA](docs/visual-polish/QA_RESULTS.md), [gallery](docs/visual-polish/review.html). 78 new baseline photos, 238 configuration captures, 17 unit tests and 18 UI tests passed; lint: 0 errors / 10 warnings. The historical report below remains preserved.

# Actual QA results — 2026-10-14

## Build and automated checks

- assembleDebug / assembleDebugAndroidTest / testDebugUnitTest / lintDebug: PASS, see local docs/full-ui/validation-current.txt.
- Unit tests: 17 run, 0 failures/errors.
- AppFlowTest + CleanExperienceTest + RoleExperienceTest + StudentHomeV2Test: PASS: 16 tests, 0 failures, 261.916 seconds.
- Additional teacher solver/history/back/graph/session test: PASS: 1 test, 18.126 seconds.
- FullApplicationUiTest: PASS, 206 recorded configurations; text issue rows: 0.
- Lint: 0 errors, 10 existing warnings (SDK/dependency currency, backup configuration), 1 informational state-allocation finding. No version upgrades.

## Functional coverage

Student/teacher registration and local code validation, password recovery, onboarding and guest gating; login/logout/role selection; generic and interactive lesson persistence; wrong/correct answers, hint, ten-step checkpoint and six-question practice; result/progress propagation into teacher evidence; bounded solver unsupported-input state, history/bookmark and method selection; graph entry and teacher function addition/value expansion; class creation/filter/search/student evidence; four-stage assignment publishing, student submission, automatic result and teacher textual feedback; actual local resource sharing to the student's library; settings language/theme, notifications, long expressions and Activity recreation. The additional teacher tools scenario asserts the session remains Teacher.

The first completed regression run had 3 stale-selector failures. Tests were updated to the existing Lab tab, profile Library entry and generic lesson presenter. No domain behavior was changed to force a test pass. The complete rerun passed. The earlier interactive path remains separately tested end to end.

## Visual/configuration coverage

39 canonical destinations × Arabic Light/Dark = 78 baseline photos at 393×865. Eight representative risky screens × four additional viewports × two languages × two modes = 128 additional captures. Sizes: 320×720 (font 1.3×), 360×800, 393×865, 412×915 and 800×1280. Home has its own 20-case matrix including extra 1.3× Arabic checks. This is representative coverage, not the assertion that every one of 39 screens was tested at every size/language/font.

Images are actual UiAutomation full-display captures. The test host enables edge-to-edge just as MainActivity does. Capture waits for Compose, Android idle and the compositor to avoid an outgoing transition frame. Baseline Splash uses the production component with a held callback only for capture; normal startup is tested separately. Teacher form, solution and settings lower-content captures supplement top-of-page photos.

Graph curves are clipped to the plot boundary. The earlier visual review caught overflow despite text-only checks; native path clipping was fixed and final captures regenerated. Static contact sheets support review; no generated mockup is presented as application output.

## Limits

Visual fidelity remains PARTIAL: native illustrations, preserved content/schema and supported capabilities differ from generated boards. No pixel-diff acceptance, exhaustive TalkBack, physical-phone, real server/offline failure, OAuth, upload/OCR, remote account deletion or manual-grade service testing was claimed. UI readiness is separate from backend/production readiness. Existing local demo limitations are disclosed in the app and supporting-state report.

Device: dedicated ENMathReview emulator-5554 (sdk_gphone64_x86_64). No phone was seeded/cleared and no Burhan files were changed. Review viewport/font restored to 393×865/1.0×. No commit/push in this continuation.
