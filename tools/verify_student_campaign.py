"""Read-only release evidence: current test XML, lint, APK contents and publication candidates.

Does not install APKs, invoke ADB, approve curriculum, or print secret values.
"""
import argparse
import collections
import hashlib
import json
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT).decode('utf-8').strip()


def digest(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def suites(folder):
    result = []
    for file in sorted(folder.glob('TEST-*.xml')):
        root = ET.parse(file).getroot()
        result.append(dict(name=root.get('name'), **{
            key: int(root.get(key, 0)) for key in ('tests', 'failures', 'errors', 'skipped')}))
    totals = {key: sum(row[key] for row in result) for key in ('tests', 'failures', 'errors', 'skipped')}
    return dict(totals=totals, suites=result,
                status='PASS' if result and not any(totals[k] for k in ('failures', 'errors', 'skipped')) else 'NOT_PASS')


def audit(build):
    tests = {v: suites(build / 'test-results' / f'test{v}UnitTest') for v in ('Debug', 'Release')}
    lint = {}
    for variant in ('debug', 'release'):
        path = build / 'reports' / f'lint-results-{variant}.xml'
        if path.exists():
            counts = collections.Counter(issue.get('severity') for issue in ET.parse(path).getroot().findall('issue'))
            lint[variant] = dict(counts=counts, status='PASS' if not counts['Error'] and not counts['Fatal'] else 'FAIL')
        else:
            lint[variant] = dict(status='NOT_RUN')
    apks = []
    for variant in ('debug', 'release'):
        for path in sorted((build / 'outputs' / 'apk' / variant).glob('*.apk')):
            with zipfile.ZipFile(path) as archive:
                names = archive.namelist()
                pilot = 'assets/content/g9/nature_numbers_pilot.json' in names
                forbidden = [n for n in names if n.lower().endswith(('.pdf', '.db', '.sqlite', '.jks', '.keystore', '.pem', '.key'))
                             or '/authoring/' in n or '/question_bank/' in n]
                bad_zip = archive.testzip()
            apks.append(dict(variant=variant, file=str(path), bytes=path.stat().st_size, sha256=digest(path),
                             crc='PASS' if bad_zip is None else 'FAIL', demo_bank=pilot,
                             demo_gate='PASS' if pilot == (variant == 'debug') else 'FAIL',
                             forbidden_entries=forbidden))
    tracked = git('ls-files', '-z').split('\0')
    new = git('ls-files', '--others', '--exclude-standard', '-z').split('\0')
    candidates = sorted(set(tracked + new) - {''})
    forbidden_files = []
    findings = []
    patterns = {
        'private_key': re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),
        'github_token': re.compile(r'\b(?:gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{40,})\b'),
        'google_api_key': re.compile(r'\bAIza[0-9A-Za-z_-]{35}\b'),
        'aws_access_key': re.compile(r'\bAKIA[0-9A-Z]{16}\b'),
        'openai_key': re.compile(r'\bsk-(?:proj-)?[A-Za-z0-9_-]{40,}\b'),
    }
    for name in candidates:
        path = ROOT / name
        if path.suffix.lower() in {'.apk', '.aab', '.db', '.sqlite', '.sqlite3', '.pdf', '.jks', '.keystore', '.pem', '.p12', '.pfx'} or path.name in {
                'local.properties', 'google-services.json', '.env', 'signing.properties', 'keystore.properties', 'service-account.json'}:
            forbidden_files.append(name)
        if not path.is_file() or path.stat().st_size > 2_000_000:
            continue
        try:
            source = path.read_text(encoding='utf-8')
        except (UnicodeError, OSError):
            continue
        for kind, pattern in patterns.items():
            for match in pattern.finditer(source):
                findings.append(dict(file=name, line=source.count('\n', 0, match.start()) + 1, kind=kind))
    return dict(date='2026-11-02', head=git('rev-parse', 'HEAD'), branch=git('branch', '--show-current'),
                tests=tests, lint=lint, apks=apks,
                publication_scan=dict(files=len(candidates), forbidden_files=forbidden_files, findings=findings,
                                      scope='Known key patterns and prohibited file types; not a proof that all possible secrets are absent.'),
                phone_QA='NOT_RUN', emulator=False, adb=False)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--build-dir', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    evidence = audit(args.build_dir)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in evidence.items() if k not in ('tests', 'apks')}, ensure_ascii=False, indent=2))
    print(json.dumps({v: row['totals'] for v, row in evidence['tests'].items()}))


if __name__ == '__main__':
    main()
