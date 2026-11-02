# EN.MATH student polish final — 2026-11-02

Result: **ENGINEERING VERIFIED — RASTER VISUAL QA BLOCKED — DEVICE QA NOT_RUN**. Version **1.0.5 / versionCode 6**, application ID `com.ahmadmol.enmath`. This is a tested improvement to the existing student MVP, not a complete or independently approved curriculum.

## Delivered outcome

13 grouped confirmed application issues were addressed (P01–P13 in LOGIC_AUDIT.md and BUG_FIXES.md). The campaign fixes nonfinite numeric answers, inconsistent mathematical input/analysis, silent storage observer failures and ignored practice save errors. The BAC Home no longer invents learning progress, completion from bookmarks, levels or Gems rewards. It uses responsive source-reference stations and one clear next action. Lesson, training and results share reading layout, stage colors, feedback and readable large-font text; results expose real independent-answer and review evidence.

24 unique regression cases were added. Final acceptance executed **930 successful tests**: Debug 468 (427 core + 41 host), Release 428 (427 core + one actual Release-resource host), Python 34. Zero failures/errors/skips in those suites. Android UI tests compile. Both APKs and both lint tasks pass; lint retains 72/73 warnings and one information each, zero Error/Fatal.

The requested current visual matrix was attempted: 108 production previews across 360/393/800dp, Arabic/English, Light/Dark and 1.0/1.5/2.0 text scale. Windows Application Control blocks Layoutlib's native DLL. **Zero current screenshots were produced or approved.** Security protections were not bypassed. Phone, ADB, emulator and installation operations were not performed or requested.

## Seven passes completed within available environment

1. Architecture/logic: inspected identity, scoped repositories, navigation and ViewModel boundaries, student learning/source paths, math engines, design components and previous QA; recorded real root causes before repairs.
2. Critical repairs: finite math and shared normalization; scoped failed-write invalidations/poisoning, ignored Hint/Previous errors, malformed typed identity recovery. Data schemas and ownership remain unchanged.
3. UI/UX: refined BAC Home/path, next actions, availability, lesson/training/results, stage theme/font consistency and large-font planner/progress layouts. No new characters, sound, teacher product or website.
4. Integration: persisted evidence drives progress/results; source bookmarks/visits never become certified lessons or currency. Debug-only Nature gates remain effective in Release.
5. Performance/accessibility: prepare curves once and preserve them during canvas interactions; measure equivalent host sampling. Verify primary contrast, readable 200% text, 48dp actions and 640dp tablet width. No device performance claims.
6. Regression/release: fresh baseline, failing-before tests, targeted repair runs, complete final Debug/Release/host/Python/lint/build run, asset/signature/CRC/publication checks. Failed and blocked evidence remains explicit.
7. Packaging/reporting: APKs, hashes, reports, raw evidence and source recovery artifacts prepared outside Git; implementation and documentation commits published to a separate safe branch without merging main.

## Data and content integrity

No student records were deleted, reset, rekeyed or migrated. Tests preserve original failed-write bytes, attempts, bookmarks and planner history and restore acknowledged records after restart; stale-owner/session contracts and SQLite replay/duplicate protections remain included. Teacher source, curriculum source records and wallet implementation were unchanged. A complete pre-change Git bundle was verified and cloned/fsck-checked outside the repository.

G9 catalogs remain 32 source references (algebra 19 / geometry 13), BAC 63 across 14 units and two books. Zero source catalog researches are newly certified complete lessons. Nature explanation is a draft; its six-question unapproved bank remains Debug-only and absent/blocked in Release. The reduced-fractions pilot and preserved 24 BAC demo lessons retain separate provenance. No questions, official approval or missing historical answers were fabricated.

## Artifacts

Delivery: `D:/ENMATH/delivery/STUDENT_POLISH_FINAL_2026-11-02/`.

| Artifact | Bytes | SHA-256 |
|---|---:|---|
| ENMATH-student-polish-v6-debug.apk | 34,465,801 | 21bb64791a4c54520efd647d9810a26afce02cf72996c1ece32d95014101c50b |
| ENMATH-student-polish-v6-release-unsigned.apk | 25,036,241 | 6ce423ecfa4d88eea16cf4cc791ab2e4be3f6f10d72482242f295252d42846b5 |

Debug has a valid APK v2 signature; certificate SHA-256 `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`, matching the preceding local debug delivery. Release is unsigned because no approved production signing material was supplied. Package/version/certificate prerequisites are checked; physical installed-app update/data retention is not tested. minSdk 24 / targetSdk 35. allowBackup remains false, Release is non-debuggable and requests no dangerous/Internet permission. Both ZIP/asset boundaries pass.

## Source and follow-up

Baseline/reference branch remains `feature/ui-refinement` at `2b86f000b906c6e61f436fa846c77512bc68f4a9`. Tested implementation is `1ff95eb4877fc910ea5ee07fe8933dc3c567c76e`, saved after validating the identical application source. Working branch: `codex/student-polish-final-2026-11-02`. Final remote equality and documentation SHA are recorded in the delivered GIT_STATUS.md, avoiding a report claiming to contain its own commit hash.

No confirmed critical application issue remains known in the covered contracts. Unresolved acceptance limitations are raster policy, independent content/rights approval, production signing, and the explicitly excluded physical-device checks. Lint warnings remain documented. No high-risk architecture rewrite was attempted. See KNOWN_LIMITATIONS.md and FEATURE_STATUS.md for boundaries; no full-product or full-curriculum completion claim is made.
