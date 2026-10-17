"""Package executed evidence. Run only after the final emulator gallery has completed."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import csv
import hashlib
import html
import json
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
DOC = ROOT / 'docs/visual-polish'
LOG = ROOT / 'docs/qa/visual-polish-before'
PHOTOS = DOC / 'screenshots/visual-polish-all'
BEFORE = LOG / 'screenshots'

def read(p):
    return p.read_text(encoding='utf-8-sig', errors='replace')

def digest(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def write_csv(p, rows):
    with p.open('w', encoding='utf-8', newline='') as f:
        w = csv.DictWriter(f, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)

assert 'BUILD SUCCESSFUL' in read(LOG / 'final-build.txt')
assert 'OK (17 tests)' in read(LOG / 'final-regression.txt')
assert 'OK (1 test)' in read(LOG / 'final-gallery.txt')
matrix = list(csv.DictReader(read(PHOTOS / 'coverage.csv').splitlines()))
assert len(matrix) == 238, len(matrix)
assert not any(r['textIssues'] for r in matrix)
baseline = [r for r in matrix if r['viewport'] == '393x865' and r['language'] == 'Arabic' and r['fontScale'] == '1.0']
assert len(baseline) == 78
assert all(Image.open(PHOTOS / r['screenshot']).size == (393, 865) for r in baseline)
registry = list(csv.DictReader(read(ROOT / 'docs/approved-references-2026-10-13/screen_reference_map.csv').splitlines()))
assert len(registry) == 39

improvements = {
    'S01': 'Softer header, quieter dark accent, consistent 80dp navigation with 48dp touch targets; existing learning/assignment actions retained.',
    'S02': 'Compact featured-topic row and horizontal book cards with real completion bars.',
    'S03': 'Horizontal book/art hero; completion integrated into hero instead of a second large card; compact topic rows.',
    'S04': 'Compact lesson cards with visible completion/play status and reading-time captions.',
    'S05': 'Centered native mathematical surfaces, compact headings and section hierarchy; existing multi-step learning interaction retained.',
    'S06': 'Centered native equation presentation, compact heading and consistent feedback surfaces; answer logic retained.',
    'S07': 'Compact section hierarchy and common result surfaces; actual accuracy/mastery and outcome medallion retained.',
    'S08': 'Mode chips precede entry; generous serif equation input; disclosure moved to example area; existing keyboard/actions retained.',
    'S09': 'Numbered solution steps; centered math; removed nested equation frames; expand/collapse unchanged.',
    'S10': 'Compact section typography and unified equation surfaces; actual local history/filter/delete retained.',
    'S11': 'Input/add row; plot precedes controls; compact curve rows with color, visibility and delete actions; real tables retained.',
    'S12': 'Parallel compact metric tiles instead of stacked full cards; real progress and activity data retained.',
    'S13': 'Assignment rows separate identity, deadline and question/point metadata.',
    'S14': 'Start/result actions precede long question preview; all preview expressions retained.',
    'S15': 'Feedback and elapsed time grouped; cleaner common math/result surfaces; real marks retained.',
    'S16': 'Resource type/icon/title hierarchy; compact lesson cards and real resource bodies retained.',
    'S17': 'Compact common headings, consistent fields and result-card typography; existing search scope retained.',
    'S18': 'Notification icon and title/body in one row, unread badge plus existing unread label.',
    'S19': 'Slim completion strip replaces oversized ring; icon/chevron destination rows replace outlined form-like links.',
    'S20': 'Three equal theme choices and compact language rows; icon destination rows for help/about/privacy.',
    'T01': 'Avatar/identity header; three professional metric tiles; structured insight and class rows.',
    'T02': 'Class icon, compact name/metadata row and real mastery bar; create/search preserved.',
    'T03': 'Clear section hierarchy and compact student evidence rows with avatars; tabs/filters preserved.',
    'T04': 'Structured teaching-insight block and compact topic evidence; existing learning calculations retained.',
    'T05': 'Shared compact assignment cards distinguish title/class/submissions/points.',
    'T06': 'Four visible numbered stages and stage captions; existing wizard validation and callbacks retained.',
    'T07': 'Horizontal student identity, elapsed-time caption and score; real submission totals retained.',
    'T08': 'Icon destination rows for curriculum/solver/lab; compact resource kind/title hierarchy; share flow preserved.',
    'T09': 'Professional identity and compact icon destination rows; actual class count retained.',
    'E01': 'Native subtle wave motif, existing vector logo and timed startup retained.',
    'E02': 'Larger native book/geometry illustration, centered compact title/body; existing pager/actions retained.',
    'E03': 'Student/teacher role cards gain native role icons and clear text hierarchy.',
    'E04': 'Centered logo header, flat form grouping, proper mail/lock/visibility icons; existing local sign-in validation retained.',
    'E05': 'Native student/teacher icons; existing supported account types only.',
    'E06': 'Consistent 56dp fields and lock/mail icons; demo disclosure follows submit instead of dominating form.',
    'E07': 'Same field system and disclosure ordering; subject/experience/institution fields preserved.',
    'E08': 'Native email-verification emblem and centered LTR monospace numeric code input.',
    'E09': 'Native lock-reset emblem and consistent email/submit presentation.',
    'E10': 'Native lock emblem, password visibility controls and consistent recovery form.',
}
old_coverage = {r['ID']: r for r in csv.DictReader(read(ROOT / 'SCREEN_COVERAGE.csv').splitlines())}
rows = []
for r in registry:
    sid = r['id']
    prior = old_coverage.get(sid, {})
    # The previous report contains honest per-destination functional/artwork limitations.
    gap = prior.get('Known Issues', '')
    if sid == 'S03': gap = 'Native book/geometry artwork differs from generated 3D illustration. Actual book names, topics and completion data retained.'
    if sid == 'S11': gap = 'Real supported functions and existing graph controls retained; no reference curves or invented data copied.'
    rows.append(dict(id=sid, route=r['existing_route'], light_reference=r['light_board'], light_position=r['light_position'], dark_reference=r['dark_board'], dark_position=r['dark_position'], improvement=improvements[sid], visual_status='PARTIAL - reviewed, approval pending', remaining_difference=gap, light_screenshot=f'{sid}-Light-393x865-Arabic-1.0.png', dark_screenshot=f'{sid}-Dark-393x865-Arabic-1.0.png'))
write_csv(DOC / 'SCREEN_REVIEW.csv', rows)

backup = ZipFile(LOG / 'source.zip')
changed = []
protected = []
for name in backup.namelist():
    if name.endswith('/'): continue
    p = ROOT / 'app' / name
    if p.exists() and p.read_bytes() != backup.read(name): changed.append(p.relative_to(ROOT).as_posix())
    if '/core/data/' in name or '/core/model/' in name or '/core/navigation/' in name or name.endswith('ViewModel.kt'):
        assert p.read_bytes() == backup.read(name), f'Protected source changed: {name}'
        protected.append(name)
(DOC / 'FILES_CHANGED.md').write_text('# Files changed in the visual-polish round\n\nCompared byte-for-byte with the starting source ZIP (which includes pre-existing local work).\n\n' + '\n'.join('- `' + p + '`' for p in changed) + f'\n\n{len(protected)} existing data/model/navigation/view-model files match the starting backup exactly. Build/dependency files were not edited. Review documents added under docs/visual-polish (REPORT.md, QA_RESULTS.md, SCREEN_REVIEW.csv, FILES_CHANGED.md, contrast.json, review.html, evidence and delivery/checksum indexes). Root UI_IMPLEMENTATION_REPORT.md, QA_RESULTS.md, DESIGN_SYSTEM_REPORT.md, SCREEN_COVERAGE.csv, docs/full-ui/README.md and the approved-reference map now link or point to the new evidence while retaining the earlier report content. No commit, push, reset, file deletion or history rewrite was performed.\n', encoding='utf-8')

review = DOC / 'screenshots/comparisons'
review.mkdir(parents=True, exist_ok=True)
for sid in ['S01','S02','S03','S08','S09','S11','S12','S19','S20','T01','T02','T06','E04','E08']:
    sheet = Image.new('RGB', (4 * 260, 610), '#e4e9f2')
    draw = ImageDraw.Draw(sheet)
    for i, (theme, old) in enumerate([('Light',True),('Light',False),('Dark',True),('Dark',False)]):
        p = (BEFORE if old else PHOTOS) / f'{sid}-{theme}-393x865-Arabic-1.0.png'
        shot = Image.open(p).convert('RGB'); shot.thumbnail((250, 558))
        sheet.paste(shot, (i * 260 + 5, 35))
        draw.text((i * 260 + 8, 10), f'{sid} {theme} {"BEFORE" if old else "AFTER"}', fill='#122440')
    sheet.save(review / f'{sid}-before-after.png')

# Contact sheets are resized indexes of actual photos; originals remain untouched.
for mode in ['Light','Dark']:
    ordered = sorted(baseline, key=lambda r: r['screen'])
    ordered = [r for r in ordered if r['theme'] == mode]
    for start in range(0,len(ordered),8):
        group = ordered[start:start+8]
        sheet = Image.new('RGB',(4*240,2*558),'#e4e9f2'); draw=ImageDraw.Draw(sheet)
        for i,r in enumerate(group):
            shot=Image.open(PHOTOS/r['screenshot']).convert('RGB');shot.thumbnail((232,520))
            x=(i%4)*240;y=(i//4)*558
            draw.text((x+5,y+5),r['screen']+' '+mode,fill='#122440');sheet.paste(shot,(x+4,y+26))
        sheet.save(review / f'contact-{mode}-{start//8+1}.png')

suites=[ET.parse(p).getroot() for p in (ROOT/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml')]
unit=sum(int(s.attrib['tests']) for s in suites)
fail=sum(int(s.attrib['failures'])+int(s.attrib['errors']) for s in suites)
assert unit == 17 and fail == 0
lint=ET.parse(ROOT/'app/build/reports/lint-results-debug.xml').getroot().findall('issue')
errors=[i for i in lint if i.attrib['severity']=='Error'];warnings=[i for i in lint if i.attrib['severity']=='Warning']
assert not errors
contrast=json.loads(read(DOC/'contrast.json'))
assert min(r['ratio'] for r in contrast)>=4.5

qa = f'''# Visual-polish QA — 2026-10-15

All results below were executed on ENMathReview / emulator-5554, density 160. No physical-phone installation is claimed in this round.

| Validation | Actual result |
|---|---|
| assembleDebug | PASS |
| assembleDebugAndroidTest | PASS |
| testDebugUnitTest | {unit} tests, {fail} failures |
| lintDebug | {len(errors)} errors, {len(warnings)} warnings |
| Final UI regression | 17 tests, 0 failures |
| Final gallery test | 1 test, 0 failures |
| New baseline photos | 78, 39 routes × Light/Dark, Arabic, actual 393×865 PNGs |
| Final configuration matrix | 238 captures: 78 baseline + 160 representative cases |
| Recorded text overflow | 0; final gallery asserts this |
| Home matrix | Additional 20 configurations inside StudentHomeV2Test |

Representative matrix: E07/S03/S06/S08/S11/S20/T03/T06 × Arabic/English × Light/Dark × 320×720 at 1.3 font scale, 360×800 at 1.0, 393×865 at 1.3, 412×915 at 1.0, 800×1280 at 1.0. This is risk-based coverage, not every screen at every size. Scrollable content was not classified as clipping. Tall content remains reachable through existing scrolling. Long native equations scroll horizontally and remain LTR.

Stages completed before continuing: student build/unit + 3 flows + gallery (88 configurations); remaining-student build/unit + 12 flows + gallery (32); teacher build/unit + 4 flows + gallery (50); shared build/unit + 4 flows + gallery (36). Final full-app run supersedes those snapshots. An early student book image captured an outgoing transition frame; it is excluded from the final delivery. Final capture advances the Compose animation clock and waits for the Android compositor. Splash is the production component with its automatic callback held by the capture fixture; startup behavior is exercised in regression.

Contrast: 14 sampled central text/button/container/gradient-endpoint pairs, minimum {min(r['ratio'] for r in contrast)}:1. See contrast.json. This is a token-pair check, not an exhaustive accessibility certification. Prominent buttons use 52dp minimum, text inputs 56dp, destination actions 48dp minimum; Material touch expansion remains in effect. Screen-reader and switch-access audits were not run.

Lint warnings: existing target/dependency currency suggestions, backup/data-extraction configuration, and repository KTX suggestions. Versions and repository implementations were deliberately preserved. No disabled checks or new lint suppression were added.

The tests exercise lesson progression, answer validation/retry/results, shared student/teacher mastery, class and assignment creation, submission, resource sharing, solver/history, graph, role/session switching, auth validation/recovery demonstration, themes/languages, search/notifications and guest gates. No crash occurred in these executed flows. This is not proof that every possible interaction is defect-free.
'''
(DOC/'QA_RESULTS.md').write_text(qa,encoding='utf-8')

report='''# EN.MATH — Final visual-fidelity polish review

Presentation polish is complete for this review round across the 39 existing destinations. Visual fidelity remains **PARTIAL**, pending human approval; functional test success is not visual acceptance.

Both resource packs, the 39-screen map and previous review evidence were retained. All 12 approved boards were inspected. Light determines layout and Dark supplies surface/accent treatment; native components share the same structure in both modes. The official data, destinations, roles, view-models and repositories were preserved. No dependency version was changed.

## Visible changes

- Student learning: compact hierarchy; horizontal book hero and integrated completion; lesson status rows; centered mathematical surfaces; numbered expandable solution steps; streamlined graph input and curve list; parallel progress metrics.
- Remaining student pages: structured assignment metadata and earlier primary action; compact notification/resource rows; slimmer profile completion; native icon destination links; three equal appearance choices and compact language selection.
- Teacher: identity header and professional metric tiles; readable class/student evidence rows; four numbered assignment stages; clear submission scores and resource destinations. No new gamification or invented analytics.
- Shared entry/auth: native wave/book/geometry art, centered onboarding copy, role icons, flat login form, email/lock/visibility icons, recovery emblems and centered LTR code entry.
- Shared system: dp/sp dimensions, IBM Plex Arabic / Inter, quieter dark teal, consistent shapes and surfaces, 80dp navigation with unchanged items/actions, 52dp prominent buttons and 56dp inputs.

## Evidence and review

- [Actual QA results](QA_RESULTS.md)
- [All 39 reference mappings, improvements and remaining differences](SCREEN_REVIEW.csv)
- [Files changed versus the task-start backup](FILES_CHANGED.md)
- [Actual Light/Dark gallery](review.html)
- `screenshots/comparisons/`: 14 before/after montages, four columns: Light before, Light after, Dark before, Dark after. These contain resized actual screenshots; unmodified originals are included separately.

## Remaining visual differences

The generated boards are montages rather than dimensioned production designs. Native vector books, initials avatars and outcome icons do not reproduce all 3D illustrations or photographed characters. Existing labels, supported topic inventory and real data produce different wrapping and card counts from the generated art. Existing global app bars/navigation locations and all destination identities are retained. Some screens therefore retain a different section order or information density. No pixel-perfect match is claimed.

The native math renderer supports conservative fractions/limits and literal fallbacks; it is not a full TeX typesetter. Verification remains one accessible numeric field instead of six independently styled cells. Theme and language preferences remain on the existing settings route rather than creating new appearance/language routes. Charts show actual supported functions and existing computed statistics; no fake trend or fabricated value was inserted to resemble an illustration.

Backend capabilities were not expanded: login/OTP/recovery are disclosed local demonstrations; no real OAuth, mail delivery, OCR, remote uploads, production messaging, account deletion or manual grading service was invented. These pre-existing limits are not marked implemented by visual polish.

The source backup and previous local modifications remain intact. No commit, push, reset, destructive cleanup or route removal was performed. Work stops here for review.
'''
(DOC/'REPORT.md').write_text(report,encoding='utf-8')

parts=['<!doctype html><meta charset="utf-8"><title>EN.MATH visual review</title><style>body{font:15px system-ui;background:#e7edf5;color:#122440;margin:24px}section{background:white;padding:20px;margin:20px 0;border-radius:16px}img{width:min(43vw,393px);height:auto;margin:8px}p{max-width:900px}</style><h1>EN.MATH — actual Light/Dark review</h1><p>39 destinations. Actual emulator captures, 393×865. Visual fidelity: PARTIAL, approval pending. References are identified by approved board and position. No generated screen render is used.</p>']
for r in rows:
    parts.append('<section><h2>'+r['id']+' — '+html.escape(r['route'])+'</h2><p>'+html.escape(r['improvement'])+'</p><p>Light: '+html.escape(r['light_reference']+' / '+r['light_position'])+'<br>Dark: '+html.escape(r['dark_reference']+' / '+r['dark_position'])+'</p>')
    for mode in ['light','dark']:
        parts.append('<a href="screenshots/visual-polish-all/'+r[mode+'_screenshot']+'"><img loading="lazy" alt="'+r['id']+' '+mode+'" src="screenshots/visual-polish-all/'+r[mode+'_screenshot']+'"></a>')
    parts.append('</section>')
(DOC/'review.html').write_text('\n'.join(parts),encoding='utf-8')

evidence=DOC/'evidence';evidence.mkdir(exist_ok=True)
for name in ['final-build.txt','final-regression.txt','final-gallery.txt','phase1-build.txt','phase1-flow.txt','phase1-gallery.txt','phase2-build.txt','phase2-flow.txt','phase2-gallery.txt','phase3-build.txt','phase3-flow.txt','phase3-gallery.txt','phase4-build.txt','phase4-flow.txt','phase4-gallery.txt']:
    shutil.copy2(LOG/name,evidence/name)
for src in (ROOT/'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'): shutil.copy2(src,evidence/src.name)
shutil.copy2(ROOT/'app/build/reports/lint-results-debug.xml',evidence/'lint-results-debug.xml')
dist=ROOT/'dist';dist.mkdir(exist_ok=True)
apk=dist/'EN.MATH-visual-polish-debug.apk';shutil.copy2(ROOT/'app/build/outputs/apk/debug/app-debug.apk',apk)
manifest=[]
for r in matrix:
    p=PHOTOS/r['screenshot'];manifest.append(dict(file=r['screenshot'],sha256=digest(p)))
write_csv(DOC/'SCREENSHOT_SHA256.csv',manifest)
archive=dist/'EN.MATH-visual-polish-review.zip'
with ZipFile(archive,'w',ZIP_DEFLATED) as z:
    for p in DOC.rglob('*'):
        if p.is_file() and p.name != 'DELIVERY.json' and (not 'screenshots' in p.parts or 'visual-polish-all' in p.parts or 'comparisons' in p.parts): z.write(p,p.relative_to(DOC))
    for r in baseline: z.write(BEFORE/r['screenshot'],Path('before')/r['screenshot'])
summary=dict(baseline_photos=78,configuration_captures=len(matrix),unit_tests=unit,ui_regression=17,ui_gallery=1,lint_errors=len(errors),lint_warnings=len(warnings),changed_sources=len(changed),apk_sha256=digest(apk),zip_sha256=digest(archive))
(DOC/'DELIVERY.json').write_text(json.dumps(summary,indent=2),encoding='utf-8');print(json.dumps(summary,indent=2))
