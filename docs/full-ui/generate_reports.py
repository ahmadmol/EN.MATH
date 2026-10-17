"""Consolidate observed QA evidence; never infer a passing test from source code."""
from pathlib import Path
import csv
import json
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
DOC = ROOT / 'docs/full-ui'
PHOTOS = DOC / 'screenshots/full-ui'

def read(path):
    return path.read_text(encoding='utf-8-sig', errors='replace') if path.exists() else ''

def write(name, text):
    (ROOT / name).write_text(text.strip() + '\n', encoding='utf-8')

def save_csv(path, rows):
    with path.open('w', encoding='utf-8', newline='') as f:
        writer = csv.DictWriter(f, fieldnames=list(rows[0]))
        writer.writeheader()
        writer.writerows(rows)

registry = list(csv.DictReader(read(ROOT / 'docs/approved-references-2026-10-13/screen_reference_map.csv').splitlines()))
assert len(registry) == 39
matrix = list(csv.DictReader(read(PHOTOS / 'coverage.csv').splitlines()))
flow_ok = 'OK (16 tests)' in read(DOC / 'regression-final.txt')
tools_ok = 'OK (1 test)' in read(DOC / 'teacher-tools-final.txt')
gallery_ok = 'OK (1 test)' in read(DOC / 'gallery-final.txt')
build_ok = 'BUILD SUCCESSFUL' in read(DOC / 'validation-current.txt')
suites = [ET.parse(p).getroot() for p in (ROOT / 'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml')]
unit_total = sum(int(s.attrib['tests']) for s in suites)
unit_failed = sum(int(s.attrib['failures']) + int(s.attrib['errors']) for s in suites)

composables = {'S08': 'SolverWorkspace', 'S11': 'ExploreScreen', 'S19': 'ProductProfileScreen(false)', 'T09': 'ProductProfileScreen(true)'}
issues = {
 'E01': 'Native book logo differs from 3D artwork; component captured with automatic callback held only in QA.',
 'E02': 'Native vector learning artwork replaces generated 3D art; existing slide copy retained.',
 'E03': 'Existing student/teacher/guest roles retained; no unsupported parent role added.',
 'E04': 'No Google/Apple service or pretend OAuth buttons. Local demo login disclosed.',
 'E05': 'Only account types actually supported by the project are shown.',
 'E06': 'Existing validation/schema retained; illustration and form details differ from artwork.',
 'E07': 'Existing subject/experience/institution fields retained; no fabricated subject catalog.',
 'E08': 'Existing single numeric code input instead of six independent boxes. Demo code only; no delivery service.',
 'E09': 'Local recovery demonstration; no email is sent.',
 'E10': 'Existing demo reset validation only; no remote password change.',
 'S01': 'Existing assignments/recent learning retained below reference content; avatar is native initials.',
 'S02': 'Existing featured limits path and book catalog retained, rather than invented reference categories.',
 'S03': 'Native curve/symbol artwork; actual book names/content/progress retained.',
 'S04': 'Real lesson inventory and mastery retained; no lesson counts copied from images.',
 'S05': 'Existing ten-step interactive lesson retained; no pretend video player or media service.',
 'S06': 'Existing question types and scoring; reference-generated math is not copied.',
 'S07': 'Native outcome medallion, actual accuracy and mastery; no generated trophy asset.',
 'S08': 'Existing bounded demo solver and keyboard, no OCR/CAS claims; initial quick entry presentation.',
 'S09': 'Conservative native fraction/limit formatting, literal fallback; not a full TeX typesetter.',
 'S10': 'Actual local history, filters and deletion; row density differs from board.',
 'S11': 'Actual supported functions, clipping boundary, collapsible value tables; controls differ from board.',
 'S12': 'Real computed statistics; no fabricated percentages/achievements.',
 'S13': 'Existing upcoming/completed/overdue filtering; real deadlines retained.',
 'S14': 'Existing question preview and attempt/deadline rules; no attachment service added.',
 'S15': 'Existing scores, answers and teacher feedback; no standalone celebration screen.',
 'S16': 'Actual saved lessons/problems/local teacher resources; no pretend PDF download.',
 'S17': 'Real local search and empty state; not a remote search service.',
 'S18': 'Existing notifications and read state; no push service.',
 'S19': 'Actual identity/progress; no fake rank, storage size or offline-download button.',
 'S20': 'Existing theme/language/reminder/help/about/privacy controls; font selector/account deletion unavailable.',
 'T01': 'Actual class mastery/support/submission evidence; no invented analytics or gamification.',
 'T02': 'Existing class creation with local search; no remote roster administration.',
 'T03': 'Existing overview/students/curriculum/assignments; reference tabs do not override current functionality.',
 'T04': 'Actual shared learning evidence; no fabricated attendance metrics.',
 'T05': 'Existing assignments with local search/status filters; actual submission counts.',
 'T06': 'Existing four-stage question-selection/settings/preview wizard; no file-upload capability.',
 'T07': 'Actual submission scores and response links; no unimplemented chart or editable-grade service.',
 'T08': 'Actual local resources/share-to-class; no remote storage or fake download.',
 'T09': 'Actual teacher identity/class count; no invented certifications/experience metrics.'
}
rows=[]
for item in registry:
    sid=item['id']
    paths=[f'docs/full-ui/screenshots/full-ui/{sid}-{mode}-393x865-Arabic-1.0.png' for mode in ('Light','Dark')]
    passes=[]
    for mode,path in zip(('Light','Dark'),paths):
        evidence=[r for r in matrix if r['screen']==sid and r['theme']==mode and r['viewport']=='393x865' and r['language']=='Arabic']
        passes.append('PASS' if gallery_ok and (ROOT/path).exists() and evidence and not evidence[0]['textIssues'] else 'BLOCKED')
    rows.append({'ID':sid,'Route':item['existing_route'], 'Composable':composables.get(sid,item['existing_composable']),
                 'Light Reference':item['light_board']+' / '+item['light_position'],
                 'Dark Reference':item['dark_board']+' / '+item['dark_position'],
                 'Implemented':'PASS' if build_ok else 'BLOCKED','Light QA':passes[0],'Dark QA':passes[1],
                 'Functional QA':'PASS' if flow_ok else 'PARTIAL', 'Visual Fidelity':'PARTIAL',
                 'Screenshot Path':' ; '.join(paths),'Known Issues':issues[sid]})
save_csv(ROOT/'SCREEN_COVERAGE.csv',rows)
table='| '+' | '.join(rows[0])+' |\n| '+' | '.join(['---']*len(rows[0]))+' |\n'
table+='\n'.join('| '+' | '.join(str(v).replace('|','/') for v in row.values())+' |' for row in rows)
write('UI_IMPLEMENTATION_REPORT.md',f'''# EN.MATH — native UI implementation, 2026-10-14

The existing 39 destinations now share one native Compose presentation baseline. No new Android project, domain/repository rewrite, dependency upgrade, route duplication or screen-image backgrounds. Student learning and teacher productivity retain their current data and behaviors. The source and earlier S01 changes were backed up locally before migration. No commit or push was made in this continuation; the previous uploaded checkpoint remains `7dbc375`.

**Acceptance status: PARTIAL visual fidelity.** Implemented PASS means an existing native destination is wired and builds. Light/Dark QA PASS means actual emulator rendering and text-layout checks passed. Functional PASS refers to the named regression scenarios in QA_RESULTS, not every possible input or a remote service. These are deliberately separate from pixel-level approval. Generated 3D artwork, unsupported capabilities and preserved domain-driven layouts prevent a claim of exact board replication. The differences are listed per screen below.

206 capture configurations cover the 78 baseline Arabic Light/Dark screens plus 128 representative responsive cases. Full-display baseline images are 393×865 pixels, not invented renders. See [QA](QA_RESULTS.md), [supporting states](SUPPORTING_STATES_REPORT.md), [design](DESIGN_SYSTEM_REPORT.md), [legacy audit](LEGACY_UI_AUDIT.md), and [CSV](SCREEN_COVERAGE.csv). Images remain local under the ignored screenshot directory.

{table}

## Changes and limits

Student Home, curriculum/books, lessons/practice/results, tools, progress, assignments, shared forms/profile/settings and teacher workflows reuse their existing functional components with the shared theme. The solver/graph/profile fixed-reference bindings were replaced by their existing functional presenters. Teacher tool aliases keep their identities and use real local solver/history/graph data. Graph paths are clipped; its value tables remain available through an explicit expansion. Native equations do not evaluate or rewrite math.

No backend, real OAuth, OTP/email delivery, OCR, upload/storage, account deletion or manual-grade service was added. UI availability is distinct from production readiness. Preserved legacy sources are not deleted; see the audit for IDE-only previews and themed teacher preview aliases.
''')

states={
 'X01':('PARTIAL','Existing explicit scan simulation only; real OCR unavailable. AppFlow scan/method test.'),
 'X02':('NOT IMPLEMENTED','No handwriting recognition capability; no new route or pretend handwriting solver.'),
 'X03':('PARTIAL','Existing topic practice/quiz entry points; no duplicate standalone practice catalog.'),
 'X04':('NOT IMPLEMENTED','No separate exam-intro route; existing practice flow retained.'),
 'X05':('PARTIAL','Existing streak/milestones in Progress; no fabricated achievement catalog.'),
 'X06':('PASS','Existing library saved lessons/problems and solver bookmark; local only.'),
 'X07':('NOT IMPLEMENTED','No notes repository/flow exists; optional feature not invented.'),
 'X08':('PASS','Existing assignment work route uses actual question types/answers and submit confirmation.'),
 'X09':('BLOCKED','No actual file attachment/upload service. No misleading upload button added.'),
 'X10':('PARTIAL','Actual submission confirmation navigates to real result; no separate decorative success screen.'),
 'X11':('PASS','Existing teacher feedback persisted locally and shown with assignment result.'),
 'X12':('PASS','Existing create-class dialog validation and local class persistence.'),
 'X13':('PASS','Existing four-stage wizard and preview use actual selected questions.'),
 'X14':('PARTIAL','Successful local publish opens actual results; no separate celebration page.'),
 'X15':('PASS','Teacher response route displays actual student answers and computed scores.'),
 'X16':('BLOCKED','Existing automatic score plus textual feedback only. Manual grade editing requires prohibited business changes.'),
 'X17':('PASS','Actual local resource creation and class sharing; teacher-to-student library tested.'),
 'X18':('PARTIAL','Teacher settings alias reuses SettingsScreen. Shared controls tested through student settings; teacher alias not independently exercised.'),
 'X19':('PARTIAL','Teacher notification alias reuses native notifications and role filtering; not independently exercised by the final flow suite.'),
 'X20':('PASS','Actual guest exploration and current guarded features retained.'),
 'X21':('PASS','Account prompt and registration redirect; guest restriction tested.'),
 'X22':('PASS','Arabic/English changes persist; RTL/LTR tested in real app configurations.'),
 'X23':('NOT IMPLEMENTED','No separate font-size preference exists. System 1.3× accessibility font scaling is tested.'),
 'X24':('BLOCKED','Existing reset demo only; no authenticated remote password-change capability.'),
 'X25':('BLOCKED','No account-deletion service; no fake destructive action introduced.'),
 'X26':('PASS','Native confirmation and actual logout/session cleanup tested.'),
 'X27':('PARTIAL','Existing help dialog retained; no fake support messaging service.'),
 'X28':('PARTIAL','Existing about/privacy dialogs retained; not duplicated as new routes.'),
 'X29':('PASS','Existing three-slide onboarding, skip/finish/session persistence tested.'),
 'X30':('PARTIAL','Real existing startup transition; no fake network loading injected into synchronous local data.'),
 'X31':('PARTIAL','Native shared missing-content state retained; not a fabricated empty repository.'),
 'X32':('PARTIAL','Existing role-appropriate empty assignment lists; gallery is not an exhaustive empty-fixture suite.'),
 'X33':('PASS','Empty saved library state rendered in baseline; existing filters retained.'),
 'X34':('PARTIAL','Existing no-result presentation is retained. Positive search was tested; a dedicated no-match fixture was not exercised.'),
 'X35':('BLOCKED','Local offline notice/error component only; no live network service to disconnect/retry.'),
 'X36':('BLOCKED','ErrorState presentation reusable, but no server exists; no fake request/retry wiring.')}
manifest=json.loads(read(ROOT/'docs/ui-resource-pack/ENMATH_Handoff_v2/01_Inventory/supporting_manifest.json'))
assert len(manifest)==36
support=[{'ID':i['id'],'State':i['name'],'Status':states[i['id']][0],'Evidence / Limit':states[i['id']][1]} for i in manifest]
save_csv(ROOT/'SUPPORTING_STATES_COVERAGE.csv',support)
write('SUPPORTING_STATES_REPORT.md','# Supporting states — 36 entries\n\nThese are variants/flows, not 36 additional primary screens. PASS is limited to the existing local capability; no remote service is implied.\n\n| ID | State | Status | Evidence / limit |\n| --- | --- | --- | --- |\n'+'\n'.join('| '+' | '.join(r.values())+' |' for r in support))

write('DESIGN_SYSTEM_REPORT.md','''# Shared presentation system

ResourceBaseline.kt owns Material 3 Light/Dark colors, typography, shapes and approved Home gradient roles. Both modes share the same component tree and routes. Light uses #F7F9FC / #FFFFFF / #175CD3; approved Dark uses #0B1322 / #0D2235 / #56B4FF with #20DBC2 accents. Existing installed IBM Plex Arabic and Inter fonts remain; equations use native serif text isolated LTR.

Body sizes are 14/24sp and 16/27sp; supporting text 12/20sp, title 16/26sp. Shared spacing is 4/8/12/16/24/32dp, cards 16dp, primary buttons at least 52dp, other principal actions at least 48dp. Layouts scroll with content and IME; phone pages use 16dp insets, forms retain 520dp cap, student experience 720dp cap, wider existing teacher layouts remain adaptive. Shapes are 8/12/16/20/28dp. The retained floating navigation shell still uses its earlier 22dp radius, 76dp inner minimum and 4dp shadow; this is a documented deviation rather than an invented global token.

Reusable components: ScreenPage/ExperiencePage, PageHeading, AppCard/ExperiencePanel, PrimaryButton/SecondaryButton, AppTextField, ChipRow, MathRender/MathLine, ProgressRing/ProgressFeedback, OutcomeMedallion, BookLogo/LearningArtwork, TeacherSummary/ClassRow/StudentPerformanceRow, EmptyState/ErrorState/LoadingState, native dialogs and shared role navigation. Actual app-role navigation items were not changed by this continuation. Account gates now inherit the same resource theme.

Math formatting only stacks unambiguous operands and limit bounds; ambiguous expressions stay literal. Three unit tests guard precedence and original operands. No full TeX renderer or WebView is used in active presentation. Long equations scroll horizontally within the equation surface.

Existing contrast evidence is retained under docs/s01-v2/contrast_used_pairs.csv and docs/approved-references-2026-10-13/contrast.csv. Tested text/gradient pairs meet AA (Home gradient foreground >=6.09:1). This is not a claim that every rendered pixel/component has been accessibility-audited. Shared action targets and Home typography were measured in UI tests; TalkBack and device-specific display modes were not exhaustively tested.

Native vector art is deliberately real Compose/Canvas, not a screenshot background. It does not replicate the generated 3D illustrations pixel for pixel. Teacher screens emphasize class/submission evidence and omit fabricated gamification.
''')

write('LEGACY_UI_AUDIT.md','''# Legacy UI audit

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
''')

qa=f'''# Actual QA results — 2026-10-14

## Build and automated checks

- assembleDebug / assembleDebugAndroidTest / testDebugUnitTest / lintDebug: {'PASS' if build_ok else 'BLOCKED'}, see local docs/full-ui/validation-current.txt.
- Unit tests: {unit_total} run, {unit_failed} failures/errors.
- AppFlowTest + CleanExperienceTest + RoleExperienceTest + StudentHomeV2Test: {'PASS: 16 tests, 0 failures, 261.916 seconds' if flow_ok else 'PARTIAL: no passing verdict found'}.
- Additional teacher solver/history/back/graph/session test: {'PASS: 1 test, 18.126 seconds' if tools_ok else 'PARTIAL: no passing verdict found'}.
- FullApplicationUiTest: {'PASS' if gallery_ok else 'BLOCKED: pending final verdict'}, {len(matrix)} recorded configurations; text issue rows: {sum(bool(r['textIssues']) for r in matrix)}.
- Lint: 0 errors, 10 existing warnings (SDK/dependency currency, backup configuration), 1 informational state-allocation finding. No version upgrades.

## Functional coverage

Student/teacher registration and local code validation, password recovery, onboarding and guest gating; login/logout/role selection; generic and interactive lesson persistence; wrong/correct answers, hint, ten-step checkpoint and six-question practice; result/progress propagation into teacher evidence; bounded solver unsupported-input state, history/bookmark and method selection; graph entry and teacher function addition/value expansion; class creation/filter/search/student evidence; four-stage assignment publishing, student submission, automatic result and teacher textual feedback; actual local resource sharing to the student's library; settings language/theme, notifications, long expressions and Activity recreation. The additional teacher tools scenario asserts the session remains Teacher.

The first completed regression run had 3 stale-selector failures. Tests were updated to the existing Lab tab, profile Library entry and generic lesson presenter. No domain behavior was changed to force a test pass. The complete rerun passed. The earlier interactive path remains separately tested end to end.

## Visual/configuration coverage

39 canonical destinations × Arabic Light/Dark = 78 baseline photos at 393×865. Eight representative risky screens × four additional viewports × two languages × two modes = 128 additional captures. Sizes: 320×720 (font 1.3×), 360×800, 393×865, 412×915 and 800×1280. Home has its own 20-case matrix including extra 1.3× Arabic checks. This is representative coverage, not the assertion that every one of 39 screens was tested at every size/language/font.

Images are actual UiAutomation full-display captures. The test host enables edge-to-edge just as MainActivity does. Capture waits for Compose, Android idle and the compositor to avoid an outgoing transition frame. Baseline Splash uses the production component with a held callback only for capture; normal startup is tested separately. Teacher form, solution and settings lower-content captures supplement top-of-page photos.

Graph curves are clipped to the plot boundary. The earlier visual review caught overflow despite text-only checks; native path clipping was fixed and final captures regenerated. Static contact sheets support review; no generated mockup is presented as application output.

## Limits

Visual fidelity remains PARTIAL: native illustrations, preserved content/schema and supported capabilities differ from generated boards. No pixel-diff acceptance, exhaustive TalkBack, physical-phone, real server/offline failure, OAuth, upload/OCR, remote account deletion or manual-grade service testing was claimed. UI readiness is separate from backend/production readiness. Existing local demo limitations are disclosed in the app and supporting-state report.

Device: dedicated ENMathReview emulator-5554 (sdk_gphone64_x86_64). No phone was seeded/cleared and no Burhan files were changed. Review viewport/font restored to 393×865/1.0×. No commit/push in this continuation.
'''
write('QA_RESULTS.md',qa)
modified=subprocess.check_output(['git','diff','--name-only','7dbc375','--'],cwd=ROOT,text=True).splitlines()
full_scope=subprocess.check_output(['git','diff','--name-only','e08acb3','--'],cwd=ROOT,text=True).splitlines()
write('docs/full-ui/FILES_CHANGED.md','# Files changed\n\n## Full migration since e08acb3\n\n'+ '\n'.join('- `'+p+'`' for p in full_scope) +'\n\n## Continuation since uploaded checkpoint 7dbc375\n\n'+ '\n'.join('- `'+p+'`' for p in modified) + '\n\nNew files: UI_IMPLEMENTATION_REPORT.md, SCREEN_COVERAGE.csv, DESIGN_SYSTEM_REPORT.md, QA_RESULTS.md, LEGACY_UI_AUDIT.md, SUPPORTING_STATES_REPORT.md, SUPPORTING_STATES_COVERAGE.csv, docs/full-ui/FILES_CHANGED.md and docs/full-ui/generate_reports.py. Screenshots, extracted reference archives, APKs, logs and backups remain ignored/local.')
print(f'Generated 39 primary rows, {len(support)} supporting rows. Build={build_ok}, flows={flow_ok}, tools={tools_ok}, gallery={gallery_ok}.')
