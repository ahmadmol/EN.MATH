# S01 — approved L06/D06 visual refinement

Implemented for visual review only; no S02 work, commit or push. The dirty tree from the previous work is preserved. ZIP README and manifest read; all 12 hashes verified. The existing 39-function mapping and S01 comparison were updated in place to official L/D filenames; no duplicate route added.

## Changes in this refinement

- `StudentHomeV2.kt`: compact personal header, goal before continuation, blue/teal gradient learning action and circular play, three icon wells, slim mastery, preserved upcoming assignment and recent learning.
- `ResourceBaseline.kt`: centralized approved Home color/gradient roles and 48dp compact goal. Existing staged theme applies only to Home; other screens retain their presentation.
- `StudentHomeBeforeApproved.kt`: retained preceding S01 implementation; the older LegacyStudentLearningHome remains too.
- `StudentHomeV2Test.kt`: screenshot capture explicitly resets scroll to zero for paired theme comparisons.
- `.gitignore`: locally extracted pack/logs excluded. Approved registry, comparison, this report, contrast and matrix document the change.

The third icon action is the existing View All Progress control relocated from the mastery card. Practice, Solver, Continue and assignment actions retain their exact destinations and state selection. No new Lab action or fabricated image statistics. Repositories, LearningRules, AppViewModel, domain models, route graph, navigation callbacks/session/roles and build versions are untouched by this refinement.

Dimensions remain native dp/sp: 16dp phone/24dp tablet insets, 12dp gaps, 16dp panels, 48dp interactive minimum, 52dp continuation minimum, 720dp content cap. Fonts remain installed IBM Plex Arabic/Inter. The goal fraction is LTR inside the RTL page. Same component tree and dimensions in both modes; native brushes/surfaces rather than PNG backgrounds.

## Validation

Build, unit tests, lint and two UI tests run on ENMathReview, explicitly addressed as emulator-5554; the connected phone and other emulator are not seeded or updated. UI tests exercise current lesson/practice/solver/progress destinations, Back, Activity recreation and unchanged user/completion/attempt data.

Final source: assembleDebug, assembleDebugAndroidTest, testDebugUnitTest and lintDebug succeeded (16 seconds on the final screenshot-test adjustment; full initial build 1m 30s). All 14 unit tests passed. Lint: zero errors, ten existing warnings, one informational finding; no version upgrades made. Both UI tests passed in 138.858 seconds after resetting paired captures to identical zero scroll position.

Twenty responsive configurations passed: 360×800, 393×865, 412×915, 800×1280 in Arabic/English × Light/Dark at fontScale 1.0; 360×800 and 320×720 Arabic × Light/Dark at fontScale 1.3. Text line/height constraints, action targets and equal shortcut heights checked. [Actual matrix](ui-matrix.csv). Sixty actual viewport captures verified (top/progress/recent for each configuration). Scroll/font scale restored to the review baseline; Git HEAD remains e08acb3. The unrelated emulator-5570 and connected physical phone were not used.

[Contrast checks](contrast.csv): continuation foreground across gradient endpoints is 6.09–8.16:1; dark body 14.96:1 and primary-on-surface 7.22:1. All tested pairs meet AA. Visual review checks rendered text/spacing; no universal every-device assertion.

## Before / after — actual 393×865 RTL captures

| Mode | Before | After | Lower content |
| --- | --- | --- | --- |
| Light | [Before](screenshots/before-393-Light.png) | [After](screenshots/393-Light-Arabic-1.0-home.png) | [Recent / below fold](screenshots/393-Light-Arabic-1.0-recent.png) |
| Dark | [Before](screenshots/before-393-Dark.png) | [After](screenshots/393-Dark-Arabic-1.0-home.png) | [Recent / below fold](screenshots/393-Dark-Arabic-1.0-recent.png) |

These are actual viewport images; longer content remains scrollable and is documented by additional captures, not presented as a fabricated tall render. New comparisons use matched zero scroll position, data, locale, size and font scale.

## Remaining differences

Existing EN.MATH/search/notification top bar and mobile/tab-rail destinations are preserved rather than removed to mimic the board. Existing Progress action occupies the third shortcut instead of introducing the pictured laboratory shortcut. Stock avatar icon remains; generated artwork, iOS chrome, erroneous text/numbers/formulas and decorative collage backgrounds are not copied. Upcoming assignment/recent sections remain functional below the reference's cropped content. Pixel-identical parity is not claimed for conceptual collage artwork; this candidate awaits human visual acceptance.

Stop: S01 only. No physical-phone deployment, commit or push in this request.
