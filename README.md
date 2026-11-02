# EN.MATH

Native Android mathematics application built with Kotlin, Jetpack Compose and Material 3. Arabic RTL, English LTR, mathematical LTR, light/dark themes and local offline storage are supported.

## Current student scope

- Separate local student profiles and guests for Grade 9 and scientific Baccalaureate; UUID, stage and session generation guard student data. Returning to a guest preserves that guest's records. Local profiles are not cloud accounts.
- Grade 9 source index: algebra (6 units / 19 researches), geometry (4 units / 13 researches). Counts are derived from the catalog in the application.
- Baccalaureate source index: two books, 14 units, 63 researches. The source editions are 2023–2024 and 2021–2022; no 2026–2027 approval is claimed.
- Source indexes are separate from published lessons. No full official curriculum is certified. An original reduced-fractions pilot, a Nature of Numbers educational draft, and 24 preserved Baccalaureate demo lessons provide limited learning experiences.
- The six-question Nature of Numbers bank is an unapproved QA demo bundled only in Debug / w1Qa. Release blocks its routes and excludes its asset. Opening a source reference does not grant progress, mastery or Gems.
- Weekly achievement goals are the planner default. Existing daily/monthly/time/hybrid goals remain supported; edits, confirmed deletion, progress, history and duplicate-command protection persist per profile.
- Search, bookmarks, learning history, local Gems ledger, solver history and settings persist locally. Grade 9 personal PDF/Word links use Android's document picker; documents remain private and are not copied into the public repository. Missing or revoked files report an error.
- The solver handles seven curated input examples and explicitly rejects unsupported problems. The graph engine supports bounded polynomials and a small set of elementary functions, not arbitrary symbolic mathematics.
- Teacher/demo functionality is retained. Development account tools are restricted to debuggable builds. Firebase Authentication, backend accounts, email/OTP delivery and remote synchronization are not implemented.

## Build and validation

Use JDK 21, Android SDK 35 and the existing Gradle wrapper. AGP 8.9.2, Kotlin 2.1.20 and Gradle 8.11.1 are retained. Bytecode targets Java 17; minSdk is 24. The application ID remains `com.ahmadmol.enmath`.

Configure the ignored `local.properties` for the SDK. Use a Gradle cache containing the dependencies, or allow dependency downloads. Do not commit signing keys or local configuration.

```powershell
./gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:lintDebug :app:lintRelease :app:assembleDebug :app:assembleRelease
```

Optional host Compose/Navigation tests use Robolectric without a device:

```powershell
./gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest -PnatureHostQa=true
python -m unittest discover -s tools -p 'test_*.py' -v
```

`-PqaBuildDir=ABSOLUTE_PATH` can redirect generated outputs to another drive. `-Pg9Render=true -Pandroid.experimental.enableScreenshotTest=true` enables the optional Layoutlib preview tasks; rendering dependencies never enter the installed application. Release is unsigned unless an approved signing configuration is supplied securely.

Current campaign reports: [execution record](docs/FINAL_STUDENT_COMPLETION_2026-11-02/EXECUTION.md), [final report](docs/FINAL_STUDENT_COMPLETION_2026-11-02/FINAL_REPORT.md). Historical reports are evidence for their recorded builds only. Phone installation, actual upgrade, process death, IME and TalkBack require separate device QA; host tests do not establish those results.

## Structure and source rights

Application code lives in `app/src/main/java/com/ahmadmol/enmath/`: `core/data` owns catalogs and persistence, `core/localgems` owns the existing wallet, `core/navigation` owns routing and stage guards, and `features` owns Compose screens. Tests live in `src/test`, optional `src/hostQa` / `src/hostQaRelease`, and `src/androidTest`.

Curriculum source metadata and authoring/review gates live in `docs/curriculum/`. Educational drafts do not become approved content merely by being indexed or passing code tests. Bundled third-party font/KaTeX licenses are in `docs/licenses/`; asset provenance is in `docs/ui/ENTRY_ASSET_PROVENANCE.json` and `docs/vendor-assets.json`. No PDFs or private student databases are published. Physics helper files, if supplied with clear publication rights, must remain free and must not receive a Gems lock.

APKs, build caches, local settings, credentials, signing material, personal documents and student databases are excluded from Git. Recovery archives and delivery packages remain outside the repository.
