"""Test and record the disposition of every previous source finding, without deleting history."""
import csv
import io
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIR = ROOT / 'docs/curriculum'
before = json.loads((DIR / 'unified/BAC_VALIDATION_BEFORE_V1_1.json').read_text(encoding='utf-8'))
after = json.loads((DIR / 'bac/SOURCE_VALIDATION.json').read_text(encoding='utf-8'))
catalog = json.loads((DIR / 'bac/source/BAC_CURRICULUM_CATALOG.json').read_text(encoding='utf-8'))
units = {u['id']: u for u in catalog['units']}
lessons = {l['id']: l for u in units.values() for l in u['lessons']}
pages = list(csv.DictReader(io.StringIO((DIR / 'bac/PAGE_INVENTORY_REGENERATED.csv').read_text(encoding='utf-8'))))
assert before['issueCount'] == len(before['issues']) == 37
assert {i['ids'][0] for i in after['issues']} == {f'bac_sci_math_part1:page:{n}' for n in range(165, 169)}
assert after['regeneratedAnchorCorrections'] == 4
current_keys = {(i['code'], tuple(i['ids'])) for i in after['issues']}
rows = []
for finding in before['issues']:
    code, ids = finding['code'], finding['ids']
    assert (code, tuple(ids)) not in current_keys
    disposition = 'FIXED_AND_TESTED'
    if code == 'INVALID_UNIT_RANGE':
        u = units['bac-1-u07']
        actual = [u[k] for k in ('start_printed_page','end_printed_page','start_pdf_page','end_pdf_page')]
        assert actual == [217, 250, 218, 251]
    elif code == 'INVALID_SECTION_RANGE':
        section = units['bac-1-u07']['exercise_sets'][0]
        actual = [section[k] for k in ('printed_start','printed_end','pdf_start','pdf_end')]
        assert actual == [244, 250, 245, 251]
    elif code == 'OVERLAPPING_LESSON_RANGE':
        assert ids == ['bac-1-u05-l04','bac-1-u05-l05']
        a,b = [lessons[lid] for lid in ids]
        assert a['source_page_start'] == a['source_page_end'] == b['source_page_start'] == 163
        assert a['pdf_page_start'] == a['pdf_page_end'] == b['pdf_page_start'] == 164
        assert b['source_page_end'] == 167
        assert len(after['verifiedSharedPages']) == 1
        disposition = 'RECLASSIFIED_VERIFIED_SHARED_START_NOT_MERGED'
        actual = after['verifiedSharedPages'][0]
    elif code == 'PAGE_INVENTORY_CONFLICT':
        n = int(ids[0].rsplit(':',1)[1]); assert 218 <= n <= 251
        page = next(p for p in pages if p['book'] == '1' and int(p['pdf_page']) == n)
        assert page['unit_id'] == 'bac-1-u07' and page['page_kind_estimated_from_toc'] != 'FRONT_MATTER'
        assert page['manual_page_content_reviewed'] == 'False'
        actual = page
    else:
        raise AssertionError(f'Unreviewed prior finding: {code}')
    rows.append(dict(previousFinding=finding, disposition=disposition, verifiedAfter=actual))
result = dict(previousFindings=37, fixedAndTested=sum(r['disposition']=='FIXED_AND_TESTED' for r in rows),
    verifiedSharedPageReclassifications=1, unresolvedPreviousFindings=0,
    newlyDetectedRawInventoryFindings=after['issues'], regeneratedAnchorsCorrected=4,
    remainingDetailedSourceReviews=after['remainingSourceReviews'], findings=rows,
    manualPageContentReviewed=0, approvedLessons=0)
(DIR / 'unified/SOURCE_CORRECTION_RECONCILIATION.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({k:v for k,v in result.items() if k not in ('findings','newlyDetectedRawInventoryFindings')},ensure_ascii=False))
