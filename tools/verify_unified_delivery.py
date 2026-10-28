"""Verify the single G9/BAC project using host evidence. No Android device actions."""
import csv
import hashlib
import io
import json
from pathlib import Path
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / 'docs/curriculum'
OUT = DOC / 'unified'


def run(script, *args):
    result = subprocess.run([sys.executable, str(ROOT / 'tools' / script), *args], cwd=ROOT,
                            capture_output=True, text=True, encoding='utf-8')
    assert result.returncode == 0, result.stdout + result.stderr
    return result


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def unit_count(result):
    return int(re.search(r'Ran (\d+) tests', result.stderr).group(1))


bac_check = json.loads(run('import_bac_source.py', '--check').stdout)
g9_check = json.loads(run('import_g9_catalog.py', '--check').stdout)
run('reconcile_bac_corrections.py')
sqlite = run('test_local_gems_sqlite.py')
source_tests = run('test_bac_source_validation.py')
math = json.loads(run('check_bac_draft_math.py').stdout)
head = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
remote = subprocess.check_output(['git', 'rev-parse', 'origin/feature/ui-refinement'], cwd=ROOT, text=True).strip()
transfer = read(OUT / 'SELECTIVE_TRANSFER.json')
baseline_head = transfer['targetHead']
subprocess.run(['git', 'merge-base', '--is-ancestor', baseline_head, head], cwd=ROOT, check=True)
baseline = read(OUT / 'MODERN_TESTS_BASELINE.json')
assert all(hashlib.sha256((ROOT / name).read_bytes()).hexdigest() == digest for name,digest in baseline.items()), 'Modern tests changed or disappeared'
protected = ['core/AppViewModel.kt', 'core/data/LearningRepository.kt', 'core/data/LocalDemoRepository.kt',
    'core/data/LocalLearnerStore.kt', 'core/data/ProductRepository.kt', 'core/model/Models.kt',
    'core/model/ProductModels.kt', 'core/localgems/LocalLearningGems.kt', 'core/localgems/SQLiteLocalGems.kt']
for name in protected:
    name = 'app/src/main/java/com/ahmadmol/enmath/' + name
    before = subprocess.check_output(['git','show',f'{baseline_head}:{name}'],cwd=ROOT).decode('utf-8').replace('\r\n','\n')
    assert before == (ROOT / name).read_text(encoding='utf-8').replace('\r\n','\n'), name

logfile = ROOT / 'unified-verification.log'
log = logfile.read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log
for task in ('testDebugUnitTest','assembleDebug','lintDebug','compileDebugAndroidTestKotlin'):
    assert f':app:{task}' in log
suites = [ET.parse(p).getroot() for p in (ROOT / 'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml')]
totals = {k: sum(int(s.get(k,0)) for s in suites) for k in ('tests','failures','errors','skipped')}
assert totals['tests'] > 0 and totals['failures'] == totals['errors'] == totals['skipped'] == 0
g9 = next(s for s in suites if s.get('name','').endswith('Grade9CatalogIntegrationTest'))
bac = next(s for s in suites if s.get('name','').endswith('BacSourceIntegrationTest'))
assert int(g9.get('tests')) == 7 and int(bac.get('tests')) == 8
test_cases = {c.get('name') for s in suites for c in s.findall('testcase')}
baseline_method_count = 0
for name in baseline:
    if '/src/test/' not in name: continue
    methods = re.findall(r'@Test(?:\([^)]*\))?\s+fun\s+(\w+)', (ROOT / name).read_text(encoding='utf-8'))
    for method in methods:
        assert any(case == method or case.startswith(method + '[') for case in test_cases), f'Modern test did not run: {method}'
    baseline_method_count += len(methods)
lint = ET.parse(ROOT / 'app/build/reports/lint-results-debug.xml').getroot()
lint_counts = {severity: sum(i.get('severity') == severity for i in lint.findall('issue')) for severity in ('Error','Fatal','Warning')}
assert lint_counts['Error'] == lint_counts['Fatal'] == 0

package = read(OUT / 'SOURCE_PACKAGE_RECEIPT.json')
for name,digest in package['fileHashes'].items():
    assert hashlib.sha256((DOC / 'bac/source' / name).read_bytes()).hexdigest() == digest
old_catalog_hashes = read(OUT / 'BAC_VALIDATION_BEFORE_V1_1.json')['sourceHashes']
for name in ('BAC_ORIGINAL_PRACTICE_DRAFTS.json','BAC_ORIGINAL_PRACTICE_DRAFTS.csv'):
    assert package['fileHashes'][name] == old_catalog_hashes[name]
catalog = read(DOC / 'bac/source/BAC_CURRICULUM_CATALOG.json')
assert {s['volume']:s['sha256'] for s in catalog['sources']} == {
    1:'68ac74c38efc97abe037df0cf185038fba28162de1f30c94ba12a61844b8867e',
    2:'c43f76f7b8e9cda0a6d615b84dc40500825edc96dfbfb65cb845a037edb3745a'}

apk = ROOT / 'app/build/outputs/apk/debug/app-debug.apk'
drafts = [d['prompt_ar'] for d in read(DOC / 'bac/source/BAC_ORIGINAL_PRACTICE_DRAFTS.json')['items']]
drafts += [d['promptArabic'] for d in read(DOC / 'g9/source/G9_ORIGINAL_EXERCISE_SEEDS_32.json')['items']]
with zipfile.ZipFile(apk) as archive:
    assert not any(n.lower().endswith('.pdf') for n in archive.namelist())
    assert not any('ORIGINAL_PRACTICE_DRAFTS' in n or 'ORIGINAL_EXERCISE_SEEDS' in n for n in archive.namelist())
    data = b'\n'.join(archive.read(n) for n in archive.namelist() if n.endswith('.dex') or n.startswith('assets/'))
    assert not any(prompt.encode('utf-8') in data for prompt in drafts), 'Review-only draft prompt leaked into APK'
subprocess.run(['git','diff','--check'],cwd=ROOT,check=True,capture_output=True)
result = dict(date=__import__('datetime').date.today().isoformat(), head=head, remoteBranch=remote,
    baseline=baseline_head,
    indexed=dict(G9=dict(books=2,units=10,lessons=32,pages=200),BAC=dict(books=2,units=14,lessons=63,pages=470),
                 combined=dict(books=4,units=24,lessons=95,pages=670),legacyDemoLessons=24),
    educationalApproval=dict(G9=0,BAC=0,approvedDraftExercises=0,originalDrafts=60),
    sourceReconciliation={k:v for k,v in read(OUT / 'SOURCE_CORRECTION_RECONCILIATION.json').items() if k not in ('findings','newlyDetectedRawInventoryFindings')},
    rawInventoryFindings=bac_check['issueCount'],
    jvm=dict(**totals,grade9IntegrationTests=7,bacIntegrationTests=8,modernTestFilesUnchanged=len(baseline),modernTestMethodsExecuted=baseline_method_count),
    sqlite=dict(tests=unit_count(sqlite),result='PASS',scope='Temporary host databases and actual schema.sql'),
    sourceRegression=dict(tests=unit_count(source_tests),result='PASS'),
    importers=dict(G9='PASS',BAC='PASS'),draftNumericalQa=math,debugBuild='PASS',lint=dict(result='PASS',**lint_counts),
    androidTestCompilation='PASS',instrumentationExecution='NOT_RUN',visualDeviceAcceptance='NOT_RUN',
    preservedStorageImplementations=protected,
    apk=dict(path=apk.relative_to(ROOT).as_posix(),sha256=hashlib.sha256(apk.read_bytes()).hexdigest(),pdfFiles=0,draftPrompts=0),
    buildLogSha256=hashlib.sha256(logfile.read_bytes()).hexdigest(),
    emulatorUsed=False,adbUsed=False,phoneDataTouched=False,installedApk=False,commitPerformed=False,pushPerformed=False)
for path in (OUT / 'VALIDATION_RESULTS.json',DOC / 'bac/VALIDATION_RESULTS.json', DOC / 'g9/VALIDATION_RESULTS.json'):
    path.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(result,ensure_ascii=False))
