# Current QA results — 2026-11-02

Tested implementation: `1ff95eb4877fc910ea5ee07fe8933dc3c567c76e`, version 1.0.5 / 6. JBR 21.0.11, existing Gradle 8.11.1 / AGP 8.9.2 / Kotlin 2.1.20 and cached dependencies were retained. Generated outputs are isolated in ignored app/build-polish-qa; logs are outside Git in C:/ENMATH_QA/STUDENT_POLISH_FINAL_2026-11-02/.

| Check | Actual result |
|---|---|
| Baseline Debug / Release | 444 / 413 successful executions before application edits |
| Before-fix new logic/storage regressions | 10 executed, 9 failed, one passing control; failure evidence preserved |
| Final Debug core JVM | 427 passed |
| Final Debug Compose/Navigation host | 41 passed |
| Final Release core JVM | 427 passed |
| Actual Release resource/flag host | 1 passed |
| Python content/source/SQLite/Gems | 34 passed |
| Total final successful test executions | **930**, zero failures/errors/skips in these suites |
| New unique regression cases | **24**: 15 core, 8 student host, 1 graph preparation host; the 15 core tests execute in both variants |
| Android UI test compilation | PASS; compileDebugAndroidTestKotlin |
| Instrumented device execution | NOT_RUN; no phone/emulator/ADB |
| Lint Debug | PASS; 0 Error/Fatal, 72 warnings, 1 information |
| Lint Release | PASS; 0 Error/Fatal, 73 warnings, 1 information |
| Debug and Release APK builds | PASS; final-acceptance.log ends BUILD SUCCESSFUL, 7m 46s |
| Current preview rendering | **BLOCKED**; 108 failures initializing Layoutlib under Windows Application Control; zero current images verified |
| Package ZIP CRC / forbidden payload / demo asset gates | PASS for both APKs |
| Debug signature | PASS, APK v2; matches the preserved v5 debug certificate |
| Release signature | UNSIGNED; apksigner exit 1 is expected, no approved production key supplied |
| Backup restoration | PASS; clone from before-bundle recovers 2b86f00 and fsck exits 0; preserved Codex snapshot trees can be reported as dangling, not corrupt |
| Publication scan | No prohibited files or known credential-pattern findings; bounded scan only |

Final Gradle command (no device task):

```powershell
./gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:compileDebugAndroidTestKotlin :app:lintDebug :app:lintRelease :app:assembleDebug :app:assembleRelease '-PnatureHostQa=true' '-PqaBuildDir=D:/ENMATH/EN.MATH/app/build-polish-qa' --max-workers=2 --offline --console=plain
```

Python: `python -m unittest discover -s tools -p 'test_*.py' -v` using the bundled Python executable. Read-only evidence: tools/verify_student_polish.py imports the existing artifact/publication audit and binds current XML, lint, CRC, source protection and failed-before regressions to EVIDENCE.json.

Lint warning categories: UnusedResources 55, UseKtx 12, IconDuplicates 2, OldTargetApi 1, ModifierParameter 1, DataExtractionRules 1; Release additionally MonochromeLauncherIcon 1. These are the same warning counts as the previous final handoff. Existing unused/reference assets were not destructively removed. Builds also report that libandroidx.graphics.path.so could not be stripped and is packaged unchanged.

Performance evidence: production canvas host test observes exactly two preparations for two curves and no further parsing when zoom/visibility change. For 4,000 equivalent samples, direct/prepared timings were 32.4757 / 1.205 ms in Debug host tests, 55.3136 / 1.1792 ms in Release host tests. These one-run host measurements and equal checksums support removing repeated parsing; they do not establish device FPS, cold start or memory savings.

Earlier attempts are retained and excluded from final success counts: a direct C-drive build path failed AGP's different-root path contract; using a same-drive output resolved it. An unquoted dotted PowerShell property prevented screenshot plugin configuration; quoted properties resolved that setup. The first targeted repair run still had two graph-description failures before that edit completed. A host run found the genuine 200% G9 width/overflow problem; the focused fixed run passed. A full verification attempt caught the new graph test injecting an interface into a concrete-typed canvas parameter; the canvas now accepts GraphEngine and the complete final run passed. Only final-acceptance.log and its corresponding XML are final acceptance evidence.

Host fixtures and Compose semantics do not prove Android power-loss durability, physical upgrade retention, actual activity/process death, IME, gestures, providers, TalkBack or visual perfection. No current screenshots were viewed or approved. No policy bypass or phone testing occurred.
