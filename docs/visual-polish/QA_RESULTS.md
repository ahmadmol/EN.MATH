# Visual-polish QA — 2026-10-15

All results below were executed on ENMathReview / emulator-5554, density 160. No physical-phone installation is claimed in this round.

| Validation | Actual result |
|---|---|
| assembleDebug | PASS |
| assembleDebugAndroidTest | PASS |
| testDebugUnitTest | 17 tests, 0 failures |
| lintDebug | 0 errors, 10 warnings |
| Final UI regression | 17 tests, 0 failures |
| Final gallery test | 1 test, 0 failures |
| New baseline photos | 78, 39 routes × Light/Dark, Arabic, actual 393×865 PNGs |
| Final configuration matrix | 238 captures: 78 baseline + 160 representative cases |
| Recorded text overflow | 0; final gallery asserts this |
| Home matrix | Additional 20 configurations inside StudentHomeV2Test |

Representative matrix: E07/S03/S06/S08/S11/S20/T03/T06 × Arabic/English × Light/Dark × 320×720 at 1.3 font scale, 360×800 at 1.0, 393×865 at 1.3, 412×915 at 1.0, 800×1280 at 1.0. This is risk-based coverage, not every screen at every size. Scrollable content was not classified as clipping. Tall content remains reachable through existing scrolling. Long native equations scroll horizontally and remain LTR.

Stages completed before continuing: student build/unit + 3 flows + gallery (88 configurations); remaining-student build/unit + 12 flows + gallery (32); teacher build/unit + 4 flows + gallery (50); shared build/unit + 4 flows + gallery (36). Final full-app run supersedes those snapshots. An early student book image captured an outgoing transition frame; it is excluded from the final delivery. Final capture advances the Compose animation clock and waits for the Android compositor. Splash is the production component with its automatic callback held by the capture fixture; startup behavior is exercised in regression.

Contrast: 14 sampled central text/button/container/gradient-endpoint pairs, minimum 5.99:1. See contrast.json. This is a token-pair check, not an exhaustive accessibility certification. Prominent buttons use 52dp minimum, text inputs 56dp, destination actions 48dp minimum; Material touch expansion remains in effect. Screen-reader and switch-access audits were not run.

Lint warnings: existing target/dependency currency suggestions, backup/data-extraction configuration, and repository KTX suggestions. Versions and repository implementations were deliberately preserved. No disabled checks or new lint suppression were added.

The tests exercise lesson progression, answer validation/retry/results, shared student/teacher mastery, class and assignment creation, submission, resource sharing, solver/history, graph, role/session switching, auth validation/recovery demonstration, themes/languages, search/notifications and guest gates. No crash occurred in these executed flows. This is not proof that every possible interaction is defect-free.
