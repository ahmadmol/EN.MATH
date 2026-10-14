# Approved-reference intake — 2026-10-13

The supplied archive `ENMATH_Approved_Light_Dark_References_2026-10-13 (1).zip` was extracted safely. README_START_HERE.md, ASSET_MANIFEST.csv and AGENT_MESSAGE_AR.txt were read. All twelve image hashes matched. The existing 39-function map is retained and updated to canonical L01–L06/D01–D06 names; its actual route bindings and dedup decisions remain unchanged.

Git is already dirty with the preceding S01 implementation. All those changes are preserved. The refinement changes S01 presentation only. Its preceding source remains as StudentHomeBeforeApproved, alongside LegacyStudentLearningHome, with before screenshots retained. No business logic, route, repository, version, commit or push change.

## Mapping and deduplication

[39-function mapping](screen_reference_map.csv) links the actual current routes/composables to Light/Dark panels in the 12 attached boards. Positions use top/middle/bottom and column numbers rather than distorted printed image IDs. Splash/onboarding/role selection are separate existing functions; onboarding slides stay one flow. Topic and book variants share the corresponding destination. Question feedback, hints, assignment filters, class tabs and wizard stages remain states/steps, not additional main screens. Parent-role imagery and handwritten/file-upload artwork do not authorize new product capabilities.

The current 39-screen map was reused as an inventory of actual implementation, not as a new visual authority. Old reference and role-pilot composables remain in the project. Nonexistent features recorded in the previous supporting inventory are not made functional by the illustrations.

## S01 visual comparison

Selected Light: `Arabic Math Learning App UI Showcase.png`, top third phone. Selected Dark: `EN.MATH Arabic Education App UI Showcase.png`, top third phone. These depict the same student-home structure. The other attached boards concern student learning/tools, teacher productivity, entry and shared/supporting states.

| Element | Current S01 | New approved reference | Presentation change indicated |
| --- | --- | --- | --- |
| Header | Existing EN.MATH/search/notifications bar, greeting, person icon | Personal greeting plus small avatar, minimal surrounding chrome | Tighten visual spacing; preserve search/notification actions and actual identity |
| Daily goal | 96dp ring in a tall panel, below lesson hero | Compact ring beside a short goal, before continue | Move existing goal above continue and reduce its visual footprint using Android dimensions |
| Continue learning | Large tinted panel, description, full-width button | Compact horizontal gradient CTA with lesson caption and circular play affordance | Recompose the same lesson action into one accessible learning CTA; retain lesson selection |
| Quick actions | Two tall text-heavy tiles: practice and solver | Three compact icon wells with captions, including laboratory | Match compact presentation for existing actions; do not add an action or route solely to copy the image |
| Progress | Separate heading and larger progress card | Compact continuing-topic row with icon and progress | Condense the existing limits mastery and keep its existing Progress action/data |
| Upcoming/recent sections | Existing functional scroll content | Limited or absent from the board's cropped viewport | Keep existing data and actions below the primary content; do not delete them |
| Light treatment | Flat cobalt/pale-blue surfaces from v2 tokens | Soft cool gradient background, pastel icon wells, more compact surfaces | Use actual Compose surfaces/brushes; never a board bitmap background |
| Dark treatment | Muted periwinkle and flat navy from v2 tokens | Deeper navy, saturated blue/cyan gradient, thin accent boundaries | New imagery takes visual priority; exact palette/intensity awaits new README guidance and contrast validation |

## Pending input and validation

S01 now uses a compact goal above a gradient continue CTA with a circular play affordance; three compact icon actions and a slimmer mastery row. The third action relocates the existing View All Progress button, not a new Lab capability. Upcoming assignment and recent activity remain. Both modes share the same layout using native Compose brushes, surfaces and icons. QA_REPORT.md records new actual validation; old v2 results remain historical. S01 awaits human visual approval.
