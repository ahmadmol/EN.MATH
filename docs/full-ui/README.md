# Current UI migration snapshot — 2026-10-14

This snapshot preserves the earlier S01 work and extends its presentation baseline to the existing student, teacher and shared destinations. The reports under `docs/s01-v2` and `docs/approved-references-2026-10-13` describe earlier S01-only checkpoints; their scope statements are historical.

## Included

- Shared Light/Dark Material 3 colors, typography, shapes, cards, buttons and responsive spacing.
- Existing Student Home refinement; course artwork, progress and profile presentation updates.
- Native Compose equation rendering with conservative fraction/limit formatting. The solver engine and scoring rules are unchanged.
- Student Solver and Graph destinations use the existing functional tools instead of the fixed reference previews. Teacher resource tools use the same engines with teacher-safe navigation callbacks.
- Local search/filter presentation for teacher classes, assignments and resources.
- Existing route identifiers, domain models, repositories and build/dependency versions are retained. The optional NavHostController parameter enables UI tests; MainActivity keeps the default controller.
- Debug-only Compose test host configuration; the production Activity lifecycle is unchanged.

## Actual validation and limits

`FullApplicationUiTest` completed successfully in 326.55 seconds: 78 Arabic Light/Dark destination captures at the 393×865 device viewport, plus 128 representative responsive cases. The recorded text-layout checks reported no clipping. See [coverage](all-config-coverage.csv). This is route/render coverage, not proof that every interaction or visual detail matches the reference boards.

The earlier captures use the Compose root (393×841 pixels at the 393×865 viewport), excluding the system status bar. Screenshots, extracted reference archives, local backups and logs remain local and are deliberately excluded from this source upload. Splash captures use an isolated production component to prevent its normal timed navigation; complete startup behavior requires the app-flow tests.

The combined AppFlow/CleanExperience/RoleExperience/StudentHome regression invocation was interrupted before a verdict was recorded. It must not be reported as passing. Earlier S01 test evidence remains documented separately. A graph drawing boundary defect found during visual review has been corrected; its earlier screenshot evidence predates that correction.

Pre-upload verification on 2026-10-14: `assembleDebug`, `assembleDebugAndroidTest`, `testDebugUnitTest` and `lintDebug` completed successfully in 1m 54s. All 17 unit tests passed. Lint reports zero errors and ten warnings; dependency and SDK versions were deliberately retained. This build verification includes the graph clipping correction and does not substitute for the interrupted interaction regression suite.

The complete pixel-level migration and final per-screen acceptance are **still in progress**. Native vector illustrations differ from the generated 3D reference artwork. No backend, OAuth, real OTP delivery, OCR, remote file upload, account deletion or manual grading service has been added. This snapshot does not claim production readiness or completion of all supporting flows.

No force push or history rewrite is required for this checkpoint.
