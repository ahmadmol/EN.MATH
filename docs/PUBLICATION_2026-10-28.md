# Publication verification — 2026-10-28

Changes are based solely on the cleaned GitHub history at c8548955751cbfeeb26f08105c2a35a45ccf0703. No legacy repository or history was imported.

The four publication groups are source catalogs and importers; scoped navigation and planning; entry and student roadmap UI; regression tests, validation evidence and publication hygiene. Source indexing remains separate from approved lessons and reward eligibility. All 42 existing modern test files are preserved.

Verification runs JVM tests for Debug, Release and W1Qa, host SQLite and source regression tests, both importers, Debug APK, Android UI test compilation and Lint. Device UI acceptance is separate and is not claimed by host tests. Publication-specific results are recorded below after successful execution.

Local configuration, credentials, signing keys, user databases, build outputs, logs, caches, private recovery reports and design-only reference screenshots are excluded. Excluded files are preserved locally; no user data is deleted.

## Explicit local-only files

- `docs/CONSOLIDATION_2026-10-28.md`
- `docs/CONSOLIDATION_RESULTS_2026-10-28.json`
- `app/src/main/res/drawable-nodpi/bac_snake_path_reference_only.png`
- `app/src/main/res/drawable-nodpi/concept_book_logo_transparent_reference.png`
- `app/src/main/res/drawable-nodpi/g9_snake_path_reference_only.png`
- `app/src/main/res/drawable-nodpi/reference_bac_footer.png`
- `app/src/main/res/drawable-nodpi/reference_bac_full.png`
- `app/src/main/res/drawable-nodpi/reference_bac_header.png`
- `app/src/main/res/drawable-nodpi/reference_bac_path_first.png`
- `app/src/main/res/drawable-nodpi/reference_bac_path_last.png`
- `app/src/main/res/drawable-nodpi/reference_bac_path_middle.png`
- `app/src/main/res/drawable-nodpi/reference_g9_footer.png`
- `app/src/main/res/drawable-nodpi/reference_g9_full.png`
- `app/src/main/res/drawable-nodpi/reference_g9_header.png`
- `app/src/main/res/drawable-nodpi/reference_g9_path_first.png`
- `app/src/main/res/drawable-nodpi/reference_g9_path_last.png`
- `app/src/main/res/drawable-nodpi/reference_g9_path_middle.png`

## Reproduction

Use JDK 21 and Android SDK 35; configure the SDK in ignored local.properties. Optional curriculum authoring requires SymPy and pypdf in the Python environment. Host verification tools resolve the repository root from their own path.

```powershell
.\gradlew.bat :app:test :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain
python tools/test_local_gems_sqlite.py
python tools/test_bac_source_validation.py
python tools/import_g9_catalog.py --check
python tools/import_bac_source.py --check
```

## Successful publication checks

- JVM: 264 tests for each of Debug, Release and W1Qa (792 executions), zero failures/errors/skips.
- SQLite: 10 passed; source regression: 6 passed; both source importers passed.
- 42 modern baseline test files unchanged; 232 baseline JVM methods verified.
- Source index: 24 units / 95 researches; 24 legacy demo lessons retained.
- Debug APK and Android UI test compilation passed. Instrumentation execution not run.
- Lint: zero errors/fatals; 53 warnings remain (including local design reference resources excluded from publication).
- Fixed both reactive configuration reads in the new roadmap screen.
- Candidate secret-pattern and credential-assignment scans: no findings. No credentials, personal databases, signing material, packages or archives staged.
