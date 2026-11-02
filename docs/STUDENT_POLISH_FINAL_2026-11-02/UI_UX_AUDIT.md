# UI/UX audit and implemented refinement

The established Indigo / Teal identity, native Material 3 components and existing art remain. No new character, sound, website or teacher feature was introduced.

## Concrete changes

- Baccalaureate Home now has a wrapping greeting, a 48dp notification action and an authoritative wallet button. The crowded synthetic level/streak bar was removed. A single next-reference card explains exactly what opening a source means.
- Both Baccalaureate books retain all units/research routes. Reference stations have readable titles, explicit saved/visited/review states, stable keys, deterministic alternating insets and quiet connectors. Bookmarks never display completion checkmarks. Fictitious reward chests and the fixed 2/4 / 50% hero were removed.
- Home tools expose solver, graph, library and actual progress. The planner remains reachable in its summary. The footer names limited demo learning and unpublished curriculum/exam availability accurately.
- Lesson, training and results share a bounded reading column, consistent spacing, rounded shapes and keyboard-aware scrolling. Question changes have independent scroll state. The existing Grade 9 subject paths and saved scroll positions remain.
- Nature feedback uses a short state-based fade, semantic success/error colors and polite accessibility announcements; no repeated animation. Choices expose selection state. Results show independent first-try answers and the exact question numbers with persisted retries/hints.
- Grade 9 learning instructions and encouragement wrap fully. Explicit widths and a weighted heading prevent a 44px text constraint found at 200% font scale. Subject buttons meet the 48dp minimum.
- Arabic/English typography now uses the selected bundled font for all Material headline/title/display styles. Student lesson/training/progress colors use the stage's Light/Dark tokens. Planner pills wrap at large fonts for either student stage, and planner/review actions use the selected stage accent.
- Graph input labels now follow the selected language. Expression normalization and supported analysis agree. Curves compile outside the sampling loop and remain compiled during viewport/visibility changes.

## Scope reviewed without speculative redesign

Navigation and bottom-bar policy, local identity, G9 algebra/geometry, BAC books, planner, library/private files, source search, profile/settings, progress/review, Gems and solver were inspected with their existing tests and presentation helpers. Shared typography and existing theme-aware components unify these screens. No claim is made that every legacy/dead/demo screen was visually redesigned or all student text was independently proofread.

## Visual evidence boundary

108 current production preview cases were attempted: three scenes (BAC Home, Nature lesson, Nature training) × 360/393/800dp × Arabic/English × Light/Dark × 1.0/1.5/2.0 font scale. Layoutlib failed initialization because Windows Application Control blocked libandroid_runtime.dll. **BLOCKED; zero current screenshots verified.** No policy bypass, substitute browser mockup, or historical render was treated as visual acceptance.

Compose host tests separately check actual layouts, scrolling, selected reference identity, touch heights, tablet reading width, error feedback and full Arabic instructions at 200%. Host tests do not establish pixel-perfect visual acceptance, physical TalkBack, IME or device performance.
