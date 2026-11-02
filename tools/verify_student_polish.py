"""Read-only campaign evidence. Never invokes ADB, an emulator, or learner databases."""
import argparse
import collections
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET
from verify_student_campaign import audit, suites

ROOT = Path(__file__).resolve().parents[1]
BASE = '2b86f000b906c6e61f436fa846c77512bc68f4a9'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--build-dir', type=Path, required=True)
    parser.add_argument('--evidence-dir', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    evidence = audit(args.build_dir)
    evidence['baseline_head'] = BASE
    evidence['baseline_tests'] = {v: suites(args.evidence_dir / 'baseline-results' / f'test{v}UnitTest')
                                  for v in ('Debug', 'Release')}
    names = {'com.ahmadmol.enmath.StudentPolishRegressionTest', 'com.ahmadmol.enmath.StudentPolishHostTest',
             'com.ahmadmol.enmath.GraphPreparationHostTest'}
    evidence['new_regression_tests'] = {v: [s for s in evidence['tests'][v]['suites'] if s['name'] in names]
                                        for v in ('Debug', 'Release')}
    evidence['new_unique_regressions'] = sum(s['tests'] for s in evidence['new_regression_tests']['Debug'])
    before = suites(args.evidence_dir / 'regressions-before-results')
    evidence['before_fix_proof'] = before
    evidence['sampling_microbenchmark'] = []
    for variant in ('Debug', 'Release'):
        path = args.build_dir / 'test-results' / f'test{variant}UnitTest' / 'TEST-com.ahmadmol.enmath.StudentPolishRegressionTest.xml'
        if path.exists():
            root = ET.parse(path).getroot()
            evidence['sampling_microbenchmark'] += [dict(variant=variant, samples=4000, direct_ns=int(a), prepared_ns=int(b))
                for a, b in re.findall(r'GRAPH_SAMPLING_4000 direct_ns=(\d+) prepared_ns=(\d+)', ''.join(root.itertext()))]
    raster = suites(args.build_dir / 'test-results' / 'updateDebugScreenshotTest')
    raster_xml = '\n'.join(p.read_text(encoding='utf-8') for p in
                           (args.build_dir / 'test-results' / 'updateDebugScreenshotTest').glob('TEST-*.xml'))
    evidence['raster'] = dict(totals=raster['totals'], status='BLOCKED' if
        'An Application Control policy has blocked this file' in raster_xml else raster['status'],
        current_images_verified=0, policy_bypass=False)
    evidence['lint_categories'] = {}
    for variant in ('debug', 'release'):
        path = args.build_dir / 'reports' / f'lint-results-{variant}.xml'
        evidence['lint_categories'][variant] = dict(collections.Counter(i.get('id') for i in
            ET.parse(path).getroot().findall('issue') if i.get('severity') == 'Warning'))
    changed = subprocess.check_output(['git', 'diff', '--name-only', BASE], cwd=ROOT, text=True).splitlines()
    protected = ('app/src/main/java/com/ahmadmol/enmath/features/teacher/',
                 'app/src/main/java/com/ahmadmol/enmath/core/localgems/', 'app/src/main/assets/local_gems/',
                 'docs/curriculum/')
    evidence['protected_source_changes'] = [name for name in changed if name.startswith(protected)]
    log = (args.evidence_dir / 'final-acceptance.log').read_text(encoding='utf-8-sig')
    evidence['build_log_success'] = 'BUILD SUCCESSFUL' in log and 'BUILD FAILED' not in log
    evidence['version'] = dict(name='1.0.5', code=6, application_id='com.ahmadmol.enmath')
    assert evidence['build_log_success']
    assert all(v['status'] == 'PASS' for v in evidence['tests'].values())
    assert all(v['status'] == 'PASS' for v in evidence['lint'].values())
    assert len(evidence['apks']) == 2
    assert all(a['crc'] == a['demo_gate'] == 'PASS' and not a['forbidden_entries'] for a in evidence['apks'])
    assert not evidence['protected_source_changes']
    assert not evidence['publication_scan']['forbidden_files'] and not evidence['publication_scan']['findings']
    assert before['totals']['tests'] == 10 and before['totals']['failures'] == 9
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in evidence.items() if k not in ('tests', 'baseline_tests', 'before_fix_proof')},
                     ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
