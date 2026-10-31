# Grade 9 master-plan audit and first implementation phase — 2026-10-30

## Scope and baseline

Compared the user-supplied `ENMATH_GRADE9_PRODUCT_SCREEN_MASTER_PLAN_2026-10-30.md` with the actual working tree in `D:\ENMATH\EN.MATH`. The document is a proposed product scope, not evidence that its features exist. This audit uses its G9-01…G9-45 IDs, which differ from earlier design-board IDs in `SCREEN_IMPLEMENTATION_MATRIX.csv`.

Baseline: branch `codex/final-consolidation`, HEAD `03ded3d`. The working tree already contained numerous modified and untracked files before this work. Existing unpublished source-catalog, planner, design, BAC, and QA changes were retained. No checkout, reset, database migration, UUID change, commit, push, emulator, ADB, device installation, or app-data clearing was performed.

Architecture inspected: `ENMathApp` owns session-keyed navigation and the application scaffold; `ProductGraphs.studentGraph` owns destinations; `AppViewModel` delegates identity and learning commands to `LocalDemoRepository`; `StudentCatalog` enforces stage/edition identity; `SourceCurricula` adapts catalog metadata; `SourceWorkspaceRepository` stores owner-scoped browsing/bookmarks; `PlannerRepository` stores scoped goals with revision/receipt safeguards. Practice and solver retain their existing ViewModels. Gems retain the existing SQLite/coordinator policies.

Catalog evidence: two source books from 2025–2026, 6 Algebra units / 19 topics and 4 Geometry units / 13 topics, 200 PDF reference pages. `tools/import_g9_catalog.py --check` reports **0 ready lessons and 0 verified activities**. The 32 draft exercise seeds are not a published question bank. Alignment with 2026–2027 and publishing permissions remain unverified.

## All 45 proposed destinations

Baseline labels: **EXISTING** = useful functional destination/template already exists; **IMPROVE** = partial or misleading existing experience; **NEW** = absent Grade 9 capability; **DEFER** = cannot publish learning/assessment honestly before review. “Reused” does not mean every proposed acceptance criterion has passed. UI runtime and phone QA remain NOT_RUN throughout.

Baseline totals across the 45 proposed destinations: **15 EXISTING, 11 IMPROVE, 7 NEW, 12 DEFER**. This phase implements five of the seven new capabilities (Algebra, Geometry, Exercises availability hub, Source, Help) and improves Home/Profile/navigation. The new Exercises hub is not a released assessment engine.

| Plan ID | Proposed screen | Baseline | Current mapping / phase result / remaining work |
|---|---|---|---|
| G9-01 | Home / long path | IMPROVE | `home` → `Grade9LearningPath`; replaced G9 presentation with all 10 units / 32 topics, exact source IDs, real wallet status, validated resume browsing, plan/progress links, saveable lazy scroll. Removed G9 synthetic level/bookmark completion and claimed +25 reward UI. |
| G9-02 | Algebra | NEW | `g9/algebra` → `Grade9SubjectScreen`; 6 source units / 19 topics, subject-scoped title search, book details and separate progress links. |
| G9-03 | Geometry | NEW | `g9/geometry` → same subject template; 4 source units / 13 topics. No unreviewed mathematical drawings. |
| G9-04 | Exercises | NEW | `g9/exercises` → `Grade9ExercisesScreen`; working book/unit/topic selectors, selected-reference navigation, honest unpublished-bank state, real mistakes and plan entry. Scored practice remains deferred. |
| G9-05 | My account | IMPROVE | `profile` → existing `ProductProfileScreen`; preserved local identity, wallet, plan, progress, library, settings and switch-file flow; added Solver/Graph/Source/Help links. G9 notification entry is Home-only. |
| G9-06 | Book details | EXISTING | `book/{id}` → `SourceBookScreen`; reused two-book template with edition/pages, units and per-book last reference. |
| G9-07 | Unit details | IMPROVE | `topic/{id}` → `SourceUnitScreen`; existing ordered source topics and exercise-page availability. Direct unit-level plan action remains; topic-level pinning already works. |
| G9-08 | Research details | EXISTING | `lesson/{id}` → `SourceResearchScreen`; source pages/edition/summary, saved reference, plan pin, browsing history; remains metadata, not an approved lesson. |
| G9-09 | Path station details | EXISTING | Reuse `SourceResearchScreen` via the station's `ContentRef`-derived route. No duplicate station-detail screen. |
| G9-10 | Source and availability | NEW | `g9/source` → `Grade9SourceScreen`; book edition/pages/counts, pending math/education/rights review, year-alignment uncertainty, no mastery for indexing. |
| G9-11 | Lesson | DEFER | G9 currently opens reference metadata; publish an actual teaching experience only after approved source content exists. BAC demo lesson is not copied into G9. |
| G9-12 | Worked example | DEFER | No reviewed G9 worked-example material or progressive-step lesson supplied. |
| G9-13 | Geometry explanation | DEFER | Needs verified mathematical drawings, interactions and accessible descriptions for the selected G9 topic. |
| G9-14 | Rules and definitions | DEFER | `study/laws` has a review-status hub; no approved G9 rule-card corpus. |
| G9-15 | Understanding check | DEFER | No approved G9 formative questions; no completion/reward action exposed. |
| G9-16 | Topic/unit exercise list | DEFER | New hub can select exact topic/unit and open its reference. Difficulty/question-type filters and start/resume must wait for a reviewed bank. |
| G9-17 | Answer exercise | DEFER | Shared practice engine exists, but Grade 9 source routes remain `SourceExercisePending`; no seed questions released. |
| G9-18 | Correction explanation | DEFER | Shared answer explanation is not evidence of approved G9 corrections. Requires verified questions/solutions and diagram/proof rubrics. |
| G9-19 | Session result | DEFER | Shared `SavedPracticeResultScreen` resolves saved attempts; no G9 source assessment sessions exist to report. |
| G9-20 | Mistake notebook | EXISTING | `review` → `RecordedProgressScreen(review=true)`; errors from stage/edition-scoped saved attempts only; empty for unpublished G9 bank. |
| G9-21 | Quick test | DEFER | Availability explained in Exercises; requires 3–5 reviewed, concept-linked questions. |
| G9-22 | Unit test | DEFER | Requires reviewed coverage and grading rubric; no fabricated start or score. |
| G9-23 | Comprehensive exam | DEFER | Requires approved source/year/rights/marking; no fake exam or trophy action in the new G9 Home. |
| G9-24 | Study-plan dashboard | EXISTING | `planner` → `PlannerScreen`; weekly default, day/month views, subject goals, clearly manual achievements, no gems. |
| G9-25 | Create/edit goal | EXISTING | `GoalEditor` dialog within Planner; source picker, counts/time/period and scope/revision validation. Reused, not a new route. |
| G9-26 | Goal details | IMPROVE | Existing goal rows expose progress/edit/move/delete confirmation; dedicated detail/history presentation can be improved without duplicating data. |
| G9-27 | Plan history | EXISTING | Previous periods and expandable edit history in Planner; carry remaining work and undo use repository receipts. No duplicate history store. |
| G9-28 | Progress dashboard | IMPROVE | `progress` → `SourceProgressScreen`; separates indexing, publication and plan progress. Future real accuracy/skills metrics depend on the G9 bank. |
| G9-29 | Book/unit progress | EXISTING | `progress/book/{id}`, `progress/unit/{id}` → shared source-progress template, same `StudentScope`. |
| G9-30 | Achievements/activity | NEW | Older learning records and gem ledger exist separately; unified real educational activity/achievement view is not implemented. |
| G9-31 | My library | IMPROVE | `library` → `LibraryScreen` + `SourceSavedLibrary`; local source bookmarks, saved solver work and resource records preserved. File attachment and source-search integration remain. |
| G9-32 | Educational resource list | IMPROVE | Library resource category exists; full book/topic/type/size/owner/rights metadata for local PDF/Word resources is not implemented. |
| G9-33 | PDF/Word viewer | NEW | No `OpenDocument` / PDF / Word external-opening flow found in current app source. Needs SAF URI grants, owner-scoped metadata, missing-reader/error handling and PDF position support. No existing viewer was removed. |
| G9-34 | Favorites | EXISTING | Source bookmarks and saved solver problems live in existing Library; no duplicate favorites store. Rules/questions await content. |
| G9-35 | Math solver | EXISTING | `solver` → `SolverWorkspace` / `SolverViewModel`; G9 entry retained in Home and Profile, no learning rewards. |
| G9-36 | Solution/history | EXISTING | `solver/history`, `solver/solution/{id}`; scoped history and saved solutions retained. |
| G9-37 | Graph/geometry lab | IMPROVE | `explore` → `ExploreScreen`; existing function graph engine and saved graphs retained. Reviewed G9-specific geometry activities remain. |
| G9-38 | Student profile | EXISTING | Reuse Profile hero/local identity; no separate redundant profile-detail screen. Guest/local/no-cloud status retained. |
| G9-39 | Gems and ledger | EXISTING | `gems/wallet` → `LocalGemsScreen`; real owner-checked balance and ledger retained unchanged. |
| G9-40 | Unlockable content | IMPROVE | Existing wallet store/transaction policies retained. No reviewed G9 source content available to offer; do not gate free supplementary files. |
| G9-41 | Settings | IMPROVE | `settings` → `SettingsScreen`; existing language/theme/reminder preferences retained. In-app font-size preference and scheduled notification delivery remain. |
| G9-42 | Manage student files | EXISTING | Existing switch-file confirmation → `LocalIdentityScreen`; UUIDs, profile separation, wallets and local storage contracts unchanged. |
| G9-43 | Independent search | EXISTING | `search` → `SearchScreen` / `SourceSearchScreen`; G9 scoped source results retained. Home entry added; repeated G9 detail-header search actions removed. |
| G9-44 | Notifications | IMPROVE | `notifications` → existing list/read state; actual unread badge on Home only. Real optional scheduled reminders/download events remain. |
| G9-45 | Help/about | NEW | `g9/help` → `Grade9HelpScreen`; simple Arabic/English guidance on source availability, local files and preserved work. No invented contact channel. |

## Implementation boundaries

- The new routes are guarded by `StudyStage.G9` and `catalogAvailable`. Existing BAC/Teacher tabs and screens are retained.
- Navigation chooses Grade 9 tabs from stage, hides them during details/focus transitions, supports the existing wide-screen rail and restores G9 tab state, including Home. Arabic Compose layout mirrors the logical tab order; Home remains central.
- Reusable `Grade9Scaffold`, unit card, path station, card/actions and reference selectors use Material theme colors, adjustable-height text, 48dp minimum targets and saveable list/filter state. The G9 center Home button has selectable tab semantics. No new dependency was added.
- Reused catalog/topic/research routes keep source identity and existing storage safeguards. The new implementation does not change persistent schemas or reward policies.
- Content gating is intentional and incomplete educational work is not counted as a completed feature. Rendering a disabled/unavailable state is not an implemented learning assessment.

## Next implementation phases

1. Daily tools: SAF PDF/Word attachments and viewer, scoped resource metadata/search, source favorite filters, optional local reminder delivery, goal details and verified activity timeline.
2. Educational content: verify edition alignment/rights and each of the 32 topics' original explanations/examples/drawings/questions/solutions, with independent educational and mathematical approval before release.
3. Practice/review: publish the reviewed question bank, answer flows, true session results, quick/unit/comprehensive tests and rubric support for proofs/diagrams.
4. Device QA: real navigation/back/scroll restoration, light/dark, Arabic RTL/English, 100%/150%/200% font scale, TalkBack, keyboard/insets, process recreation and file-reader fallbacks. No device run is claimed here.

Validation commands and final results are recorded in `G9_PHASE1_VALIDATION_2026-10-30.md`.
