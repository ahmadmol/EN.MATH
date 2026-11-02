# Final acceptance results — 2026-11-02

Tested application code: 91a6f13682be836682d0b82487607f9f7c170c88. Final Gradle command completed successfully using JBR 21.0.11 and the existing C Gradle cache; the C-backed app/build-nature-qa directory holds outputs. Toolchain versions were retained.

| Check | Result | Executed evidence |
|---|---|---|
| Debug core JVM | PASS | 412 tests |
| Debug production Compose/Navigation host | PASS | 32 tests; Robolectric SDK 34 |
| Release core JVM | PASS | 412 tests |
| Actual Release resource/flag host | PASS | 1 test |
| Python source/content/SQLite/Gems | PASS | 34 tests |
| Total successful executions above | PASS | 891; zero failures, errors or skips |
| Android instrumentation compilation | PASS | compileDebugAndroidTestKotlin |
| Android instrumentation execution | NOT_RUN | No device/emulator authorization |
| Lint Debug | PASS | 0 Error/Fatal; 72 warnings, 1 information |
| Lint Release | PASS | 0 Error/Fatal; 73 warnings, 1 information |
| assembleDebug / assembleRelease | PASS | Fresh versionCode 5 outputs |
| Debug signature | PASS | apksigner v2; same certificate as preserved local v4 APK |
| Release signature | UNSIGNED | Expected apksigner exit 1; no approved production signing configuration |
| ZIP integrity / asset boundary | PASS | Debug demo bank present; Release bank absent; no PDF/database/key/authoring payload |
| Current raster screenshots | BLOCKED | 36 attempts failed Layoutlib initialization because Windows Application Control blocked its native DLL; 0 current images |
| Phone install / actual upgrade / retention | NOT_RUN | No ADB, emulator, installation, uninstall or phone state access |
| Activity/process death, IME/insets, gestures, TalkBack, hardware failures | NOT_RUN | Host fixtures do not prove these device properties |

Debug host tests traverse the production navigation graph. Coverage includes Grade 9 first subject choice, independent subject paths, practice/hints/wrong and correct answers, results, resume, saved-session identity, instantaneous progress, Release availability, corrupt storage and failed subject-save feedback. Baccalaureate host tests click through both books, all 14 units and all 63 researches at 393dp / English / Dark / 200%, verify source identity, and keep unavailable practice pending.

Existing unit/fixture suites cover local-profile/guest restore and isolation, old sessions, invalid IDs, duplicate commands, legacy records, planner edit/progress/undo/carry semantics, library/search/bookmark contracts, bounded solver/graphs and Gems policy/coordinator behavior. Python includes 10 SQLite/Gems tests plus source/content authoring gates. Fixtures do not imply a complete physical end-to-end acceptance pass.

Earlier attempts retained locally: unsupported default JBR 25; alternate cache missing AGP offline; one baseline 200% host timeout (the same complete class passed subsequent targeted and full runs); a settling run caught the new hint-only accuracy test against an older compiled implementation; two superseded runs were interrupted. Only final-acceptance.log and its final XML are delivery acceptance evidence. The separate screenshot task remains BLOCKED and is not counted as a successful test.

Local evidence: C:/ENMATH_QA/STUDENT_FINAL_2026-11-02/ (final-acceptance.log, python-regression.log, artifact-audit.json, render-current.log, signature/badging/permissions records, backup restore evidence). Selected reports and XML are copied into the delivery evidence directory. Historical screenshots are not current visual approval.
