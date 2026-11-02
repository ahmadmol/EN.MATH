"""Author one source-aligned unit and validate it before proceeding to the next unit.

This is an authoring DSL, not a generator of educational prose from lesson titles.
All explanations, examples, questions and rubrics are supplied explicitly by an author.
"""
import hashlib
import json
from pathlib import Path
import sys
from datetime import datetime, timezone

ROOT = Path(__file__).resolve().parents[1]
DOC = ROOT / 'docs/curriculum/education'
sys.path.insert(0, str(ROOT.parent/'curriculum-tooling'))
import sympy as sp


def E(prompt, steps, answer, computed, expected=None):
    return dict(prompt=prompt, steps=steps.split('|'), answer=answer,
        check=dict(computed=computed, expected=expected if expected is not None else answer))


def Q(prompt, expression, answer, reasoning, computed, options=None):
    return dict(prompt=prompt, expression=expression, answer=str(answer), explanation=reasoning,
        options=options or [], check=dict(computed=computed,expected=str(answer)),
        rubric=['تحديد المعطيات ومجال صحة القاعدة.', 'تطبيق القاعدة مع تبرير العملية.', 'التحقق من النتيجة ووحدتها أو احتمالها.'])


def R(prompt, solution, rubric, figure=False):
    return dict(prompt=prompt, solution=solution, rubric=rubric.split('|'),
        automated=False, independentReview='HUMAN_REVIEW_REQUIRED',figureRequired=figure)


def L(lid, objectives, prerequisites, intuition, rules, conditions, mistake, examples, questions, reasoning, diagram=None):
    return dict(lessonId=lid,objectives=objectives.split('|'),prerequisites=prerequisites.split('|'),
        intuition=intuition,rules=rules.split('|'),conditions=conditions.split('|'),commonMistake=mistake,
        examples=examples,questions=questions,reasoning=reasoning,diagram=diagram,
        status='REVIEW_PENDING', scientificReview='PENDING_INDEPENDENT_REVIEW',rightsReview='PENDING',
        editionReview='PENDING_2026_2027_COMPARISON',authoringOrigin='ORIGINAL_ENMATH_EXPLANATION_AND_EXERCISES',
        textbookTextCopied=False,grantsGems=False,masteryEligible=False)


def source_lessons():
    g=json.loads((ROOT/'docs/curriculum/g9/source/G9_CURRICULUM_CATALOG_INDEX.json').read_bytes())
    b=json.loads((ROOT/'docs/curriculum/bac/source/BAC_CURRICULUM_CATALOG.json').read_bytes())
    entries={}
    for u in g['units']:
        source=next(s for s in g['sources'] if s['bookId']==u['sourceBookId'])
        for l in u['lessons']:
            entries[l['lessonId']]=dict(stage='G9',unitId=u['unitId'],title=l['title'],sourceId=u['sourceBookId'],
                sourceSha256=source['sha256'],edition=g['editionLabelOnSource'],pdfFrom=l['sourcePages']['from'],pdfTo=l['sourcePages']['to'],
                printedFrom=l['sourcePages']['from'],printedTo=l['sourcePages']['to'])
    for u in b['units']:
        source=next(s for s in b['sources'] if s['volume']==u['volume'])
        for l in u['lessons']:
            entries[l['id']]=dict(stage='BAC',unitId=u['id'],title=l['title_ar'],sourceId=source['id'],
                sourceSha256=source['sha256'],edition=l['source_edition_label'],pdfFrom=l['pdf_page_start'],pdfTo=l['pdf_page_end'],
                printedFrom=l['source_page_start'],printedTo=l['source_page_end'])
    return entries


def validate_document(d):
    assert d['status']=='REVIEW_PENDING' and not d['grantsGems'] and not d['masteryEligible']
    assert len(d['objectives'])>=2 and len(d['prerequisites'])>=1 and len(d['intuition'])>=100
    assert len(d['rules'])>=1 and len(d['conditions'])>=1 and len(d['commonMistake'])>=20
    assert len(d['examples'])>=2 and len(d['questions'])>=2 and len(d['reasoning'])>=1
    checks=0
    for item in d['examples']+d['questions']:
        if 'steps' in item: assert len(item['steps'])>=2
        else:
            assert len(item['explanation'])>=25 and len(item['rubric'])>=3
            assert sp.sympify(item['answer']).is_number and sp.sympify(item['answer']).is_finite
            if item['options']: assert item['answer'] in item['options'] and len(set(item['options']))==len(item['options'])
        a=sp.sympify(item['check']['computed'])
        b=sp.sympify(item['check']['expected'])
        difference=sp.simplify(a-b)
        assert difference==0, (d['lessonId'],item['prompt'],str(a),str(b),str(difference))
        checks+=1
    for item in d['reasoning']:
        assert len(item['rubric'])>=3 and len(item['solution'])>=80 and item['independentReview']=='HUMAN_REVIEW_REQUIRED'
    return checks


def save_unit(uid, lessons):
    entries=source_lessons()
    expected={lid for lid,s in entries.items() if s['unitId']==uid}
    assert {l['lessonId'] for l in lessons}==expected, (uid,expected-{l['lessonId'] for l in lessons})
    count=0
    for d in lessons:
        d.update(entries[d['lessonId']], revision=1)
        count+=validate_document(d)
    payload=dict(schemaVersion=1,unitId=uid,lessons=lessons)
    directory=DOC/'units';directory.mkdir(parents=True,exist_ok=True)
    text=json.dumps(payload,ensure_ascii=False,indent=2)+'\n'
    (directory/f'{uid}.json').write_text(text,encoding='utf-8')
    # A separate math engine checks all worked and practice answers before the next unit.
    evidence=DOC/'batches';evidence.mkdir(parents=True,exist_ok=True)
    previous=sorted((p.name for p in evidence.glob('*.json')))
    result=dict(unitId=uid,lessonCount=len(lessons),symbolicChecks=count,failures=0,
        checkEngine='SymPy '+sp.__version__,unitSha256=hashlib.sha256(text.encode()).hexdigest(),
        checkedAtUtc=datetime.now(timezone.utc).isoformat(),previousValidatedBatches=previous,
        scientificApproval=False,rightsApproval=False,status='AUTHORED_AND_MACHINE_CHECKED_REVIEW_PENDING')
    (evidence/f'{uid}.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False),flush=True)
