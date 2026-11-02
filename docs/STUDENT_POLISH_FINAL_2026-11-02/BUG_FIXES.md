# Confirmed fixes

The initial audit has 13 grouped issues (P01–P13), all addressed in this branch. Counts describe grouped root causes, not every affected line or execution. No new critical defect is known in the covered contracts; this is not proof that the entire product is defect-free.

| IDs | Fix and regression evidence |
|---|---|
| P01 | Reject nonfinite fraction operands and prevent overflow/invalid numeric expectations passing by lexical equality. Tests cover 1/Infinity, 1/1e999, NaN, zero denominators, negative and Arabic fractions. |
| P02 | Share MathInput normalization across answers, the curated solver, graph grammar and graph descriptions. Arabic/Persian/English digits, Arabic decimal point, unicode minus, multiplication and whitespace remain equivalent; changed unsupported problems still reject. Locale control passes. |
| P03 | Graph analysis first checks supported grammar. Unsupported x*x has no asserted properties; 0x has every real root and 0x+2 has none. General computed roots are marked approximate instead of rounded as exact integers. |
| P04 | Nature/Planner/Source failure signals now invalidate observers. All four scoped documents also poison and notify when committing throws. Fixture tests preserve existing bytes/history/bookmarks and restore original acknowledged data after restart; owner switches remain isolated. |
| P05 | Failed Hint/Previous immediately leave the practice's apparent ready state and show preserved-data recovery text. Production host tests check both actions and original file bytes. |
| P06–P08 | BAC Home removes fabricated 2/4 / 50% progress, bookmark completion/levels and fictitious +25-Gems chests. Source references, saved markers and the real wallet stay independent. Production host tests verify next-reference navigation and saved-reference semantics without completions or pending Gems events. |
| P09 | Source station layout derives from immutable item indices and stable IDs. Reverse-scroll production regression verifies stable horizontal placement and exact source navigation. |
| P10 | BAC Home greeting/actions/metrics/tools are responsive rather than one packed row. Host tests cover 360 Arabic Dark/200%, 393 English Light/150%, and 800 English Dark/200%, 48dp main action and 640dp tablet reading width. |
| P11 | Shared student reading layout, full font selection, stage theme colors, wrapping progress text and full-width/weighted content fix large-font clipping and Dark contrast. Host regression exposed and then verified the 200% Arabic text constraint; unit tests check primary text/button contrast for both stages/themes. |
| P12 | DemoGraphEngine prepares bounded expressions once; InteractiveGraphCanvas remembers them across redraws. Host instrumentation proves viewport/visibility changes do not parse again. Unit microbenchmark compares equivalent direct/prepared sampling checksums and records host timings without an unreliable timing threshold. |
| P13 | Wrongly typed registry/snapshot data now becomes CorruptLocalData inside codec boundaries; the app recognizes typed preference / malformed legacy JSON failures as preserved-data recovery. Regression preserves the malformed registry bytes without replacement. |

The first 10 new logic/storage regressions ran before implementation: 9 failures and one passing control. Full current counts and any failed/superseded verification attempts are recorded in QA_RESULTS.md and EVIDENCE.json.

No learner, planner, source, file or wallet schema was migrated. No data was deleted, reassigned, reset or manufactured. Teacher implementation and wallet authority remain unchanged.
