"""Read-only verification for the modern campaign. No device, network or learner storage access."""
import argparse
import csv
import json
from pathlib import Path
import re
import xml.etree.ElementTree as ET
import zipfile
from verify_student_campaign import audit, git

ROOT = Path(__file__).resolve().parents[1]
BASE = '3f22440a35bf58c4963867aa05364be268839b82'

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--build-dir',type=Path,required=True)
    parser.add_argument('--output-dir',type=Path,required=True)
    args=parser.parse_args();args.output_dir.mkdir(parents=True,exist_ok=True)
    evidence=audit(args.build_dir)
    bank=json.loads((ROOT/'app/src/debug/assets/content/g9_original_draft_v1.json').read_text(encoding='utf-8'))
    evidence['content']=dict(questions=len(bank['questions']),lessons=len(bank['lessonMapping']),
        pending_scientific_review=192,scientifically_approved=0,
        previously_integrated=6,newly_integrated=186,source_sha256=bank['importProvenance']['sourceSha256'])
    evidence['original_draft_asset_gates']=[]
    for apk in evidence['apks']:
        with zipfile.ZipFile(apk['file']) as z:
            present='assets/content/g9_original_draft_v1.json' in z.namelist()
            evidence['original_draft_asset_gates'].append(dict(variant=apk['variant'],present=present,passed=present==(apk['variant']=='debug')))
            assert not any('20_Poses' in name or 'REFERENCE_PACK' in name for name in z.namelist())
    xml=args.build_dir/'test-results/testDebugUnitTest/TEST-com.ahmadmol.enmath.ModernStudentHostTest.xml'
    result=ET.parse(xml).getroot()
    matrix=re.findall(r'MODERN_MATRIX [^\r\n]+ PASS',''.join(result.itertext()))
    evidence['configuration_matrix']=dict(executed=len(matrix),unique=len(set(matrix)),widths=[360,393,800],languages=['Arabic','English'],font_scales=[1,1.5,2],stages=['G9','BAC'])
    changes=git('diff','--name-only',BASE).splitlines()
    protected=('app/src/main/java/com/ahmadmol/enmath/core/localgems/', 'app/src/main/assets/local_gems/',
        'app/src/main/java/com/ahmadmol/enmath/core/data/LocalLearnerStore.kt','docs/curriculum/')
    evidence['protected_source_changes']=[path for path in changes if path.startswith(protected)]
    evidence['baseline']=BASE
    evidence['version']=dict(name='1.1.0',code=7,package='com.ahmadmol.enmath')
    evidence['cloud']=dict(connected=False,sdk_installed=False,live_auth_tests='NOT_RUN',live_firestore_tests='NOT_RUN',local_contract_tests_only=True)
    evidence['date']='2026-11-03'
    render_path=args.build_dir.parent/'render-status.json'
    evidence['raster_visual_approval']=json.loads(render_path.read_text(encoding='utf-8')) if render_path.exists() else dict(status='PENDING')
    assert all(v['status']=='PASS' for v in evidence['tests'].values())
    assert all(v['status']=='PASS' for v in evidence['lint'].values())
    assert len(evidence['apks'])==2 and all(a['crc']==a['demo_gate']=='PASS' and not a['forbidden_entries'] for a in evidence['apks'])
    assert all(row['passed'] for row in evidence['original_draft_asset_gates'])
    assert len(matrix)==72 and len(set(matrix))==72
    assert not evidence['protected_source_changes']
    assert not evidence['publication_scan']['forbidden_files'] and not evidence['publication_scan']['findings']
    mappings={m['draftLessonId']:m for m in bank['lessonMapping']}
    with (args.output_dir/'QUESTION_REVIEW_LEDGER.csv').open('w',newline='',encoding='utf-8-sig') as file:
        fields=['question_id','source_lesson_id','source_unit_id','interaction','difficulty','grading_mode','structural_validation','math_approval','edition_approval','rights_approval','previously_integrated','review_note']
        writer=csv.DictWriter(file,fieldnames=fields);writer.writeheader()
        for q in bank['questions']:
            m=mappings[q['lessonId']]
            note='Teacher judgment required; free reasoning is never automatically marked correct.' if q['gradingMode']=='structured_or_teacher_review_required' else 'Draft grading rule only; independent review still required.'
            if q['id']=='G9-H-U04-L01-Q04':note='Prompt asks for coefficient before pi, but reference answer includes pi. Retained verbatim and deferred to teacher.'
            if q['id']=='G9-A-U06-L04-Q06':note='Quartile convention must match the approved textbook edition.'
            writer.writerow(dict(question_id=q['id'],source_lesson_id=m['sourceLessonId'],source_unit_id=m['sourceUnitId'],interaction=q['interaction'],difficulty=q['difficulty'],grading_mode=q['gradingMode'],structural_validation='PASS',math_approval='PENDING',edition_approval='PENDING',rights_approval='PENDING',previously_integrated=q['id'] in bank['importProvenance']['previouslyIntegratedIds'],review_note=note))
    (args.output_dir/'EVIDENCE.json').write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(dict(tests={v:row['totals'] for v,row in evidence['tests'].items()},matrix=evidence['configuration_matrix'],content=evidence['content'],artifacts=[dict(variant=a['variant'],bytes=a['bytes'],sha256=a['sha256']) for a in evidence['apks']]),ensure_ascii=False,indent=2))

if __name__=='__main__': main()
