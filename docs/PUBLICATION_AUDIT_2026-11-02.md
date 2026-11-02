# Reviewed GitHub publication — 2026-11-02

## Scope and preservation

The original branch `codex/student-polish-final-2026-11-02` was clean and already synchronized with GitHub at `54ce7597e86acf8e9c67a44c0a4985663798f1bc`.
The user selected a new branch containing four consolidated commits. The publication branch is `codex/reviewed-publication-2026-11-02`, based on published `main` at `6a7f1ef8f85f04e3315935283a6eded55fafd72b`.
All 27 original commits after main were inventoried, and their latest combined changes were grouped into data/build, application UI, QA, and documentation commits. Existing published branches are retained; publication uses an ordinary push.

A complete, verified Git bundle of all original refs and private audit logs is retained outside the repository. Preparation and tests used an isolated clone to preserve the original working files and ignored data and to avoid the nearly full workspace drive.

## Exclusions

- Existing rules retain local SDK settings, signing keys, credentials, environment files, learner databases, packages, generated build outputs, caches, and local QA captures outside version control.
- Additional rules exclude recovery bundles, archives, temporary files, editor state, private working documents (PDF, Word, Excel), and private/backup/audit directories.
- `docs/QA/ENMATH_PHASE1_DUAL_STAGE_ACCEPTANCE_2026-10-26.md` was tracked in the original snapshot despite the existing `docs/qa/` ignore rule. It is excluded from the consolidated branch and retained on disk and in the original history/backup.
- All other original tracked files have identical Git content. Deliberate final differences are the ignore policy, `.gitleaks.toml`, this report, and the excluded local QA report.

## Secret and repository checks

Gitleaks 8.30.1 scanned the original reachable history and the consolidated branch. Three generic-key findings were manually reviewed: prose describing missing/revoked/corrupt source documents and two SHA-256 test-file checksums. `.gitleaks.toml` retains default rules and scopes these exceptions to the specific documentation paths and reviewed text/checksum values. No credential findings remain after these precise exceptions.

A separate filename audit found no credential, signing, private-document, database, or application-package filenames in the consolidated reachable history. Git object integrity passed. Secret detection is pattern based and does not prove the absence of every possible secret.

## Validation of the preserved application snapshot

- Gradle Debug JVM and Compose/Navigation host tests: 468 passed.
- Gradle Release JVM and release-flag host tests: 428 passed.
- Python source/content/SQLite tests: 34 passed.
- Total: 930 successful executions, no failures/errors/skips.
- Gradle used the existing JBR 21 and cached dependencies: `:app:testDebugUnitTest :app:testReleaseUnitTest -PnatureHostQa=true --max-workers=2 --offline --console=plain`.
- A first launcher attempt encountered the installed Android Studio JBR 25 incompatibility; rerunning with the project's JBR 21 succeeded.
- Existing whitespace checks report six extra blank lines at EOF. These pre-existing content details were preserved; no application source changes were introduced during consolidation.
- No fresh device run, lint run, or APK assembly is claimed by this publication audit. Earlier campaign reports remain historical evidence for their stated builds.

The branch is prepared for normal publication. The final delivery message records the remote push result after verification.
