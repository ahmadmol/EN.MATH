# Local earned-only Gems

Gems is integrated into the existing EN.MATH Android app. Student Home and Profile
open the local wallet, reward history, store and unlocked enrichment reader. It
requires no server, additional login, payment provider or computer connection.

- Identity: the current local student email, trimmed and lowercased. Guests,
  teachers and blank emails cannot earn or spend. The existing demo sign-in is
  not production authentication; debug role switching is disabled in release.
- Only newly verified, owner-attributed educational activity earns rewards.
  Existing progress is excluded when the wallet activates. Mastery, XP and
  learning scores retain their original behavior.
- Lesson +10, first eligible mastery +20, practice/assignment +5, weekly review
  +15; caps are 40 daily and 120 weekly in Asia/Damascus. Duplicate achievements
  cannot earn again, including capped achievements.
- Weekly review requires five distinct actual checkpoints from this account's
  verified completed lessons; otherwise it stays unavailable.
- An additional SQLite database stores account-scoped ledger entries and unlocks.
  A transaction commits the debit and entitlement together; reopening costs
  nothing. No migration or clearing of the existing learning store is required.
- Original pilot enrichment drafts cost 40/120 gems. Scientific and publication
  rights approval remain pending; these are test materials, not approved courses.
- Local data does not sync between devices and is not tamper-resistant currency.
  Real payments, Premium and Marketplace are not part of this implementation.

## Source and checks

`core/localgems` contains the policy, coordinator and SQLite adapter;
`features/localgems` contains the UI; `assets/local_gems` contains the schema and
draft material. AppViewModel and navigation connect the existing learning flows.

Run the existing Android build, JVM tests and lint without a device:

```text
gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
python tools/test_local_gems_sqlite.py
```

The Python tests exercise real host SQLite constraints and transactions using the
application schema. They do not certify Android SQLiteOpenHelper or device UI.
No emulator, device installation or destructive instrumentation is needed.

This branch excludes the earlier backend, standalone Gems QA application, private
machine reports, APKs, build outputs, local databases, credentials and backups.
