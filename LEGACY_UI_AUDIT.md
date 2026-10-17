# Legacy UI audit

All canonical destinations use the shared ResourcePackPresentation. No source screens were deleted.

| Preserved source | Active status |
| --- | --- |
| StudentHomeV2 / StudentLearningHome | Active Home destination |
| StudentHomeBeforeApproved and LegacyStudentLearningHome | Retained previous implementations, not Home route targets |
| StudentHomeScreen | Legacy IDE previews only; not a runtime Home destination |
| ReferenceHome / ReferenceStudy / ReferenceSolver / ReferenceGraph / ReferenceProfile | Fixed reference presenters retained in source; canonical student routes use functional native presenters |
| TeacherReferenceHome / TeacherReferenceStudy helper functions | Retained source; aliases bind to TeacherHome / actual StudyScreen |
| TeacherAccount / older teacher dashboard/class presenters | Retained source; canonical routes use productivity presenters and ProductProfileScreen |
| Teacher preview topic/lesson aliases | Still use ReferencePage/RefCard helpers with current Material theme and real lesson data; compact preview layout remains a visual difference |

Route strings, arguments, main graphs, tab identities, session guards and deep-link identities are preserved. Limited binding updates: Student Solver→SolverWorkspace, Explore→ExploreScreen, profiles→ProductProfileScreen; teacher tool aliases use the existing engines. Teacher solver history/detail uses local presentation state within its existing route, with explicit back handling. No repository, AppViewModel, model, scoring or engine source was changed.

The remaining reference preview layouts and IDE-only previews are explicitly disclosed. The audit does not claim all source files are new or all reference layouts pixel-identical. Old sources remain available for review as requested. Backup lives locally under ignored docs/qa/full-ui-backup.
