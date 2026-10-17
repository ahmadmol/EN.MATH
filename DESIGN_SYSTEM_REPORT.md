> Latest visual-polish review (2026-10-15): [report](docs/visual-polish/REPORT.md), [actual QA](docs/visual-polish/QA_RESULTS.md), [gallery](docs/visual-polish/review.html). 78 new baseline photos, 238 configuration captures, 17 unit tests and 18 UI tests passed; lint: 0 errors / 10 warnings. The historical report below remains preserved.

# Shared presentation system

ResourceBaseline.kt owns Material 3 Light/Dark colors, typography, shapes and approved Home gradient roles. Both modes share the same component tree and routes. Light uses #F7F9FC / #FFFFFF / #175CD3; approved Dark uses #0B1322 / #0D2235 / #56B4FF with #20DBC2 accents. Existing installed IBM Plex Arabic and Inter fonts remain; equations use native serif text isolated LTR.

Body sizes are 14/24sp and 16/27sp; supporting text 12/20sp, title 16/26sp. Shared spacing is 4/8/12/16/24/32dp, cards 16dp, primary buttons at least 52dp, other principal actions at least 48dp. Layouts scroll with content and IME; phone pages use 16dp insets, forms retain 520dp cap, student experience 720dp cap, wider existing teacher layouts remain adaptive. Shapes are 8/12/16/20/28dp. The retained floating navigation shell still uses its earlier 22dp radius, 76dp inner minimum and 4dp shadow; this is a documented deviation rather than an invented global token.

Reusable components: ScreenPage/ExperiencePage, PageHeading, AppCard/ExperiencePanel, PrimaryButton/SecondaryButton, AppTextField, ChipRow, MathRender/MathLine, ProgressRing/ProgressFeedback, OutcomeMedallion, BookLogo/LearningArtwork, TeacherSummary/ClassRow/StudentPerformanceRow, EmptyState/ErrorState/LoadingState, native dialogs and shared role navigation. Actual app-role navigation items were not changed by this continuation. Account gates now inherit the same resource theme.

Math formatting only stacks unambiguous operands and limit bounds; ambiguous expressions stay literal. Three unit tests guard precedence and original operands. No full TeX renderer or WebView is used in active presentation. Long equations scroll horizontally within the equation surface.

Existing contrast evidence is retained under docs/s01-v2/contrast_used_pairs.csv and docs/approved-references-2026-10-13/contrast.csv. Tested text/gradient pairs meet AA (Home gradient foreground >=6.09:1). This is not a claim that every rendered pixel/component has been accessibility-audited. Shared action targets and Home typography were measured in UI tests; TalkBack and device-specific display modes were not exhaustively tested.

Native vector art is deliberately real Compose/Canvas, not a screenshot background. It does not replicate the generated 3D illustrations pixel for pixel. Teacher screens emphasize class/submission evidence and omit fabricated gamification.
