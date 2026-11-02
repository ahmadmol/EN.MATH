"""Host-only final UI repair evidence. No device, account, or user database access."""
from pathlib import Path
import collections
import csv
import hashlib
import json
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
QA = ROOT / 'docs/QA/final-2026-10-28'
OUT = ROOT / 'docs/IMPLEMENTATION'
BASE = '03ded3debe3930a272b5399142723111e362250e'

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT)

def digest(data):
    return hashlib.sha256(data).hexdigest()

def counts(folder):
    suites = [ET.parse(p).getroot() for p in folder.glob('TEST-*.xml')]
    return {key: sum(int(s.get(key, 0)) for s in suites) for key in ('tests', 'failures', 'errors', 'skipped')}

assert git('rev-parse', 'HEAD').decode().strip() == BASE, 'HEAD changed without authorization'
assert not git('diff', '--cached', '--name-only').strip(), 'Unexpected staged files'
with zipfile.ZipFile(QA / 'source-before.zip') as archive:
    baseline_tests = [name for name in archive.namelist() if name.startswith(('app/src/test/', 'app/src/androidTest/')) and name.endswith('.kt')]
    assert all((ROOT / name).read_bytes().replace(b'\r\n', b'\n') == archive.read(name).replace(b'\r\n', b'\n') for name in baseline_tests)
    protected = [name for name in archive.namelist() if not name.endswith('/') and name.startswith(('app/src/main/java/com/ahmadmol/enmath/core/localgems/',
        'app/src/main/java/com/ahmadmol/enmath/features/teacher/', 'app/src/main/assets/local_gems/'))]
    protected += ['app/src/main/java/com/ahmadmol/enmath/' + name for name in ('core/data/LocalLearnerStore.kt',
        'core/data/ProductRepository.kt', 'core/data/LearningRepository.kt', 'core/model/Models.kt', 'core/model/ProductModels.kt')]
    protected += [name for name in archive.namelist() if not name.endswith('/') and name.startswith('docs/curriculum/') and '/source/' in name]
    assert all((ROOT / name).read_bytes().replace(b'\r\n', b'\n') == archive.read(name).replace(b'\r\n', b'\n') for name in protected)
    # Repository change is additive registration only; no learner persistence or identity code changed.
    repo = 'app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt'
    current = (ROOT/repo).read_text(encoding='utf-8').replace('    val sourceWorkspace = SourceWorkspaceRepository(preferences) { state.value }\n', '')
    assert current == archive.read(repo).decode('utf-8').replace('\r\n', '\n')

baseline = counts(QA / 'baseline-results')
variants = {variant: counts(ROOT / f'app/build/test-results/test{variant}UnitTest') for variant in ('Debug', 'Release', 'W1Qa')}
assert 'Ran 10 tests' in (QA/'sqlite.log').read_text(encoding='utf-8-sig')
assert 'Ran 6 tests' in (QA/'source-regression.log').read_text(encoding='utf-8-sig')
assert all((QA/name).read_text(encoding='utf-8-sig').strip().endswith('OK') for name in ('sqlite.log','source-regression.log'))
assert baseline == dict(tests=264, failures=0, errors=0, skipped=0)
for result in variants.values():
    assert result['tests'] >= baseline['tests'] + 22
    assert result['failures'] == result['errors'] == result['skipped'] == 0

log = (QA / 'final-gradle.log').read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log
assert all(f':app:{task}' in log for task in ('testDebugUnitTest', 'testReleaseUnitTest', 'testW1QaUnitTest',
    'assembleDebug', 'lintDebug', 'compileDebugAndroidTestKotlin'))
lint = ET.parse(ROOT/'app/build/reports/lint-results-debug.xml').getroot()
lint_counts = collections.Counter(x.get('severity') for x in lint.findall('issue'))
assert not lint_counts['Error'] and not lint_counts['Fatal']
warnings = collections.Counter(x.get('id') for x in lint.findall('issue') if x.get('severity') == 'Warning')

matrix = list(csv.DictReader((OUT/'SCREEN_IMPLEMENTATION_MATRIX.csv').open(encoding='utf-8-sig')))
expected = {f'E{i:02}' for i in range(1,10)} | {f'G{i:02}' for i in range(1,40)} | {f'B{i:02}' for i in range(1,45)}
assert len(matrix) == 92 and {r['id'] for r in matrix} == expected
assert all('VISUAL_NOT_VERIFIED' in r['visual_evidence'] for r in matrix)
for entry in matrix:
    assert digest((QA/'reference'/entry['reference_PNG']).read_bytes()) == entry['reference_sha256']

apk = ROOT/'app/build/outputs/apk/debug/app-debug.apk'
with zipfile.ZipFile(apk) as archive:
    assert not any(name.lower().endswith(('.pdf', '.db', '.sqlite', '.jks', '.pem')) for name in archive.namelist())
    assert not any('ORIGINAL_PRACTICE_DRAFTS' in name or 'ORIGINAL_EXERCISE_SEEDS' in name for name in archive.namelist())
    binary = b'\n'.join(archive.read(name) for name in archive.namelist() if name.endswith('.dex') or name.startswith('assets/'))
    drafts = [x['prompt_ar'] for x in json.loads((ROOT/'docs/curriculum/bac/source/BAC_ORIGINAL_PRACTICE_DRAFTS.json').read_bytes())['items']]
    drafts += [x['promptArabic'] for x in json.loads((ROOT/'docs/curriculum/g9/source/G9_ORIGINAL_EXERCISE_SEEDS_32.json').read_bytes())['items']]
    assert not any(prompt.encode('utf-8') in binary for prompt in drafts)

subprocess.run(['git', 'diff', '--check'], cwd=ROOT, check=True, capture_output=True)
files = sorted(set(git('diff', '--name-only').decode().splitlines() + git('ls-files', '--others', '--exclude-standard').decode().splitlines()))
evidence = dict(date='2026-10-28', path=str(ROOT), head=BASE, baseline=baseline, JVM=variants,
    preserved_original_test_files=len(baseline_tests), preserved_storage_teacher_source_files=len(set(protected)),
    SQLite=dict(tests=10,result='PASS'),source_validation=dict(tests=6,result='PASS'),
    lint=dict(Error=lint_counts['Error'],Fatal=lint_counts['Fatal'],Warning=lint_counts['Warning'],Information=lint_counts['Information']), lint_warning_categories=dict(warnings), reference_screens=92,
    screen_states=dict(collections.Counter(r['implementation_status'] for r in matrix)),
    phone_visual_QA='NOT_RUN', compose_runtime_UI='NOT_RUN', visual_comparison='VISUAL_NOT_VERIFIED',
    performance_on_device='NOT_MEASURED', apk_sha256=digest(apk.read_bytes()),
    snapshot_sha256=digest((QA/'source-before.zip').read_bytes()), changed_files=files,
    file_sha256={name:digest((ROOT/name).read_bytes()) for name in files if (ROOT/name).is_file() and name != 'docs/IMPLEMENTATION/ENMATH_FINAL_G9_BAC_VERIFICATION_2026-10-28.json'})
(OUT/'ENMATH_FINAL_G9_BAC_VERIFICATION_2026-10-28.json').write_text(json.dumps(evidence, ensure_ascii=False, indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in evidence.items() if k not in ('changed_files','file_sha256')},ensure_ascii=False,indent=2))
