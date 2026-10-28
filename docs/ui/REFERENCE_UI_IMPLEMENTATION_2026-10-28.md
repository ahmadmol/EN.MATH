# EN.MATH reference UI implementation — 2026-10-28

Applied directly to `[local-private-path]`.
HEAD: `c8548955751cbfeeb26f08105c2a35a45ccf0703`, detached HEAD. Existing curriculum, planner, navigation, library and search edits were already present. They were retained; the UI work also edits the existing local `DualStageExperience.kt`, `Grade9CatalogScreens.kt`, and `BacSourceScreens.kt` in place. No commit/push, emulator, ADB, installation or data reset was performed.

## Reference and asset checks

Opened all three original panels found in Downloads (their actual filenames omit the numeric suffixes in the brief):
- `Arabic EN.MATH Onboarding App Concept.png`
- `ChatGPT Image Oct 27, 2026, 04_31_17 PM.png`
- `EN.MATH Educational UI Asset Sheet.png`

Read `README_FOR_CODEX.md` and `ASSETS_MANIFEST.json` inside `ENMATH_ALL_IMAGES_SEPARATE_FOR_CODEX(1) (1).zip`. The optional `ENMATH_GEMINI_PHONE_SCREEN_REFERENCES_2026-10-28.zip` was not found. Created and opened individual reference-only phone crops at `[local-private-path]` through `S6.png`. These crops are outside Android resources and the APK. No videos were used.

## Screen mapping

Paths below are relative to `app/src/main/java/com/ahmadmol/enmath/`. All rows are implemented, compiled, and **VISUAL_QA_NOT_RUN** (no rendered app screenshot comparison).

| Screen | Reference phone | ZIP assets (original paths) | Code |
|---|---|---|---|
| S0 | Entry panel phone 1 | `01_Backgrounds/bg_splash.png`, `02_Illustrations/student_grade9_cutout.png`; existing official `ic_enmath` | `features/onboarding/OnboardingScreens.kt`: `SplashScreen` |
| S1 | Onboarding panel phone 2; available artwork follows asset sheet phone 2 | `01_Backgrounds/bg_onboarding_01.png`, `02_Illustrations/hero_onboarding_01_learn.png` | `features/auth/LocalOnboarding.kt` |
| S2 | Onboarding panel phone 3; available artwork follows asset sheet phone 3 | `01_Backgrounds/bg_onboarding_02.png`, `02_Illustrations/hero_onboarding_02_study.png` | `features/auth/LocalOnboarding.kt` |
| S3 | Asset sheet phone 4 | `01_Backgrounds/bg_onboarding_03.png`, `02_Illustrations/hero_onboarding_03_progress.png` | `features/auth/LocalOnboarding.kt` |
| S4 | Entry panel phone 2 | `01_Backgrounds/bg_stage_picker.png`, both `student_*_cutout.png` | `core/design/EntryDesign.kt`: `StagePicker`; `features/auth/LocalIdentityScreen.kt` |
| S5 | Entry panel phone 3 | `bg_stage_picker.png`, `bg_onboarding_01.png`, `student_grade9_cutout.png`, optional pack play/user/folder icons | `core/design/EntryDesign.kt`: `StageEntryContent`, `EntryHero`, `EntryAction`; `features/auth/LocalIdentityScreen.kt` |
| S6 | Entry panel phone 4 | `bg_stage_picker.png`, `bg_onboarding_02.png`, `student_baccalaureate_cutout.png`, optional pack play/user/folder icons | Same shared entry geometry, with BAC callbacks and indigo accent |
| S7 | Operational states, no supplied screen reference | Existing official logo; Compose form/notice/profile controls | `features/auth/LocalIdentityScreen.kt` |
| G9 Home / Study | Written requirements; no supplied pixel reference | Optional pack `ic_book_open.png` | `features/student/DualStageExperience.kt`, `SourcePathStation.kt`, `features/courses/Grade9CatalogScreens.kt` |
| BAC Home / Study | Written requirements; no supplied pixel reference | Optional pack `ic_book_open.png` | `features/student/DualStageExperience.kt`, `SourcePathStation.kt`, `features/courses/BacSourceScreens.kt` |

## Changes and resource IDs

- `EntryBackdrop`, `EntryBrand`, `EntryTitle`, `StagePicker`, `EntryHero`, `EntryAction`, `StageEntryContent`, `CurriculumTile`, `entryText`, `SourcePathStation` added.
- `LocalIdentityScreen`, `LocalOnboarding`, `SplashScreen`, legacy `OnboardingScreen`, `DualStageHome`, `CatalogRow`, and `BacSourceRow` updated. The legacy onboarding route delegates to the same three-page implementation.
- Three swipeable pages, next/back/skip and one final stage picker. Existing onboarding persistence callback remains the only completion write. Scrolling pages use immediate pager transitions without added motion.
- Returning onboarded users skip the additional Compose splash. Fresh-start splash advances without artificial timer or animation. Android system splash was not replaced.
- Stage intent does not create a learner. Guest acknowledgement, local create and saved-file selection retain the original repository callbacks. Both Android Back and visible Back return from stage-specific saved-file lists to that stage's entry. Teacher development entry is retained.
- Original tags retained: `screen-local-identity`, `local-stage-G9`, `local-stage-BAC`, `local-open-guest`, `guest-notice-confirm`, `local-create-student`, `local-profile-*`, `onboarding-continue`, `onboarding-skip`, `screen-onboarding`, `screen-splash`, source/catalog tags.
- Entry copy is in `res/values/entry_strings.xml` and `res/values-ar/entry_strings.xml`: `R.string.entry_copy_0` through `entry_copy_47`, selected using the app's language setting.
- Only 14 used PNG resources are packaged under `drawable-nodpi`, prefixed `entry_`: five `bg_*`, three `hero_onboarding_*`, two `student_*_cutout`, and four `ic_*`.
- Only extracted artwork is used in `Image`; buttons, titles, descriptions, dots, cards, notices and source paths are Compose.
- Home source stations list the real 10 G9 / 14 BAC units and their indexed research titles, explicitly pending preparation. They have no completion/test/reward action. Book/unit/research screens keep existing real routes and source page/status information. Existing BAC demo progress remains separate and reachable.
- `EntryPreviews.kt` adds S0–S6, 393x865, 360dp 1.5/2.0 font, dark entry and English entry preview configurations. These are preview definitions, **not exported or visually verified renders**.

## Asset defects and remaining visual gaps

The supplied ZIP is not a layered export of the direct reference panels. In particular S1/S2 artwork depicts different poses from the direct onboarding panel, and the G9 entry cutout holds a tablet rather than the reference's writing pose. Existing official app logo is intentionally retained and differs from the concept logo. Exact matching therefore remains unresolved with the allowed assets.

`hero_grade9_card.png` and `hero_bac_card.png` include baked Arabic text/buttons and are not shipped. Their cards are reconstructed with native text and complete character cutouts. `hero_splash.png` is also not shipped; the separate character is used over the supplied splash background.

The three onboarding heroes have opaque backgrounds and source clipping at the top. Removed only edge strips (12 px left / 24 px right) containing phone borders; no segmentation, redraw or AI art was used. Their composition differs from the direct onboarding panel, and the source's top clipping cannot be restored without a different authorized original. Dark mode suppresses full-screen pale bitmap backgrounds; the opaque onboarding illustrations themselves still need visual dark-theme review.

The `03_Icons` raster crops contain printed filenames and truncated icon silhouettes. Inspecting their artwork-only crops confirmed the play icon was incomplete. The four shipped icons consequently use complete transparent originals from `04_Optional_Vector_Icons/{ic_play,ic_user,ic_folder,ic_book_open}.png`. This is the brief's broken-original fallback; they are simpler outlines rather than identical reference raster icons. No cropped broken icons are shipped.

Asset provenance, output hashes and sampled alpha bounds are recorded in `docs/ui/ENTRY_ASSET_PROVENANCE.json` (sampling is not a complete pixel histogram).

All character images use Fit; source art is not stretched or used as a complete screen. Insets, IME and vertical scrolling are provided. On-device text wrapping, exact spacing, contrast, swipes, back stack and 1.5/2.0 font sizing still require execution.

## Validation

Command (offline, JDK 21 at `[local-private-path]`):

```
gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain
```

- Debug build: PASS.
- JVM: 264 tests / 33 suites, 0 failures, 0 errors.
- Lint: 0 errors / 8 warnings; see `app/build/reports/lint-results-debug.html`.
- Compose instrumentation sources: compilation PASS; execution NOT_RUN.
- Render/screenshot comparison: NOT_RUN; preview sources supplied, no render driver used.
- PHONE QA: **NOT_RUN**. No DEVICE_PASS or 100% visual matching claim.
- Full output: `entry-verification.log`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

The first build attempt selected Android Studio's JDK 25 and failed during Gradle startup; switching to the existing JDK 21 resolved it. No Gradle dependencies, SDK versions or global Java settings were changed.

## Content blockers retained

`ProductGraphs` routes `study`, `book/{id}`, `topic/{id}`, and `lesson/{id}` already dispatch source catalogs from `ScopedStudyScreen` / the existing `AppViewModel` LearningState. No new routing/repository repair was necessary. Actual source lessons remain G9 INDEX_ONLY and BAC BLOCKED_EDITION_REVIEW; source approval, edition verification, extracted exercises and their learning/completion path are content-contract work. The UI does not fabricate ready lessons, progress, tests or gems to fill this gap. SQLite, IDs, wallet ownership/generation, reward rules, teacher data and curriculum sources were not edited by this UI task.

