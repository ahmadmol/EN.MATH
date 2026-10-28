"""Validate the V1.1 BAC source package and generate blocked metadata, never textbook content.

Invalid source ranges and estimated page-inventory contradictions are retained and quarantined.
"""
import argparse
import csv
import hashlib
import io
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIR = ROOT / 'docs/curriculum/bac'
SRC = DIR / 'source'
OUT = ROOT / 'app/src/main/java/com/ahmadmol/enmath/core/data/BacSourceData.kt'


def q(value):
    return json.dumps(value, ensure_ascii=False).replace('$', '\\$')


def string_list(values):
    return 'listOf(' + ', '.join(q(v) for v in values) + ')'


def csv_text(rows):
    stream = io.StringIO(newline='')
    writer = csv.DictWriter(stream, fieldnames=list(rows[0]))
    writer.writeheader(); writer.writerows(rows)
    return stream.getvalue()


def read_csv(name):
    with (SRC / name).open(encoding='utf-8-sig', newline='') as handle:
        return list(csv.DictReader(handle))


def import_source(check=False):
    files = {p.name: p.read_bytes() for p in SRC.iterdir() if p.is_file()}
    assert len(files) == 11
    # Read supporting prose as evidence too; it cannot override publication/ownership boundaries.
    for name in ('CODEX_BAC_INTEGRATION_INSTRUCTIONS.md', 'BAC_SOURCE_ANALYSIS.md', 'PEDAGOGICAL_MODEL.md', 'BAC_SOURCE_VERIFIED_ERRATA.md'):
        assert files[name].decode('utf-8-sig').strip()
    data = json.loads(files['BAC_CURRICULUM_CATALOG.json'])
    draft_data = json.loads(files['BAC_ORIGINAL_PRACTICE_DRAFTS.json'])
    assert data['schema_version'] == draft_data['schema_version'] == '1.0'
    assert data['grade'] == 'BAC' and data['track'] == 'SCIENTIFIC'
    sources = {s['id']: s for s in data['sources']}
    units = {u['id']: u for u in data['units']}
    lessons = {l['id']: l for u in units.values() for l in u['lessons']}
    sections = {s['id']: (u, s) for u in units.values() for s in u['activities'] + u['exercise_sets']}
    assert len(sources) == 2 and len(units) == 14 and len(lessons) == 63 and len(sections) == 27
    assert sum(len(u['activities']) for u in units.values()) == 13
    assert sum(len(u['exercise_sets']) for u in units.values()) == 14
    for volume, count, edition, offset, pages in [(1, 34, '2023-2024', 1, 253), (2, 29, '2021-2022', 0, 217)]:
        source = next(s for s in sources.values() if s['volume'] == volume)
        assert source['cover_edition_label'] == edition and source['pdf_page_count'] == pages
        assert len(source['sha256']) == 64 and source['current_2026_2027_approved'] is False
        assert source['publish_pdf_or_full_page_images'] is False
        assert source['pdf_page_from_printed'] == f'pdf_page=printed_page+{offset}'
        group = [u for u in units.values() if u['volume'] == volume]
        assert len(group) == 7 and sum(len(u['lessons']) for u in group) == count
        assert [u['order'] for u in group] == list(range(1, 8))
        for u in group:
            assert u['study_stage'] == 'BAC' and u['publication_status'] == 'BLOCKED_EDITION_REVIEW'
            assert u['source_edition_label'] == edition
            assert u['start_pdf_page'] - u['start_printed_page'] == u['end_pdf_page'] - u['end_printed_page'] == offset
            assert [l['order'] for l in u['lessons']] == list(range(1, len(u['lessons']) + 1))
            for l in u['lessons']:
                assert l['source_book_id'] == source['id'] and l['source_unit_id'] == u['id']
                assert l['source_edition_label'] == edition
                assert l['pdf_page_start'] - l['source_page_start'] == l['pdf_page_end'] - l['source_page_end'] == offset
                assert 1 <= l['pdf_page_start'] <= l['pdf_page_end'] <= pages
                assert l['content_text_status'] == 'NOT_TRANSCRIBED'
                assert l['math_and_figure_review_status'] == 'NOT_REVIEWED' and l['answer_key_status'] == 'NOT_VERIFIED'
                assert l['student_visibility'] == 'LOCKED_SOURCE_REVIEW' and l['reward_eligible'] is False
            for s in u['activities'] + u['exercise_sets']:
                assert s['pdf_start'] - s['printed_start'] == s['pdf_end'] - s['printed_end'] == offset
                assert s['individual_items_extracted'] is False and s['student_visibility'] == 'LOCKED_SOURCE_REVIEW'
    # Cross-validate the duplicated tabular representations, including all 28 draft fields.
    lesson_rows = read_csv('BAC_LESSONS.csv')
    unit_rows = read_csv('BAC_UNITS.csv')
    section_rows = read_csv('BAC_ACTIVITY_EXERCISE_SECTIONS.csv')
    page_rows = read_csv('BAC_PAGE_INVENTORY.csv')
    draft_rows = read_csv('BAC_ORIGINAL_PRACTICE_DRAFTS.csv')
    assert len(lesson_rows) == 63 and len(unit_rows) == 14 and len(section_rows) == 27 and len(page_rows) == 470
    for r in lesson_rows:
        l = lessons[r['lesson_id']]; u = units[l['source_unit_id']]
        assert r['unit_id'] == u['id'] and r['unit_title_ar'] == u['title_ar'] and r['lesson_title_ar'] == l['title_ar']
        assert int(r['book']) == u['volume'] and int(r['unit']) == u['order'] and int(r['lesson']) == l['order']
        assert [int(r[k]) for k in ('print_start', 'print_end', 'pdf_start', 'pdf_end')] == [l[k] for k in ('source_page_start', 'source_page_end', 'pdf_page_start', 'pdf_page_end')]
        assert r['edition'] == l['source_edition_label'] and r['status'] == 'STRUCTURAL_ONLY'
    for r in unit_rows:
        u = units[r['unit_id']]
        assert int(r['book']) == u['volume'] and int(r['unit']) == u['order'] and r['title_ar'] == u['title_ar']
        assert int(r['lesson_count']) == len(u['lessons']) and r['edition'] == u['source_edition_label'] and r['status'] == 'TOC_VERIFIED'
        assert [int(r[k]) for k in ('first_print', 'last_print', 'first_pdf', 'last_pdf')] == [u[k] for k in ('start_printed_page', 'end_printed_page', 'start_pdf_page', 'end_pdf_page')]
        assert int(r['exercises_start']) == u['exercise_sets'][0]['printed_start']
        assert (int(r['activity_start']) if r['activity_start'] else None) == (u['activities'][0]['printed_start'] if u['activities'] else None)
    for r in section_rows:
        u, s = sections[r['id']]
        assert int(r['book']) == u['volume'] and int(r['unit']) == u['order']
        for key in ('kind', 'title_ar', 'student_visibility'):
            assert r[key] == s[key]
        for key in ('printed_start', 'printed_end', 'pdf_start', 'pdf_end'):
            assert int(r[key]) == s[key]
        assert r['individual_items_extracted'] == 'False'
    drafts = draft_data['items']
    assert draft_data['item_count'] == len(drafts) == len(draft_rows) == 28
    assert len({d['id'] for d in drafts}) == 28
    for d, r in zip(drafts, draft_rows):
        assert set(d) == set(r)
        for key, value in d.items():
            assert r[key] == ('' if value is None else str(value))
        assert d['source_unit_id'] in units and d['lesson_id'] is None
        assert d['authoring_origin'] == 'NEW_ORIGINAL_NOT_FROM_BOOK'
        assert d['publication_status'] == 'DRAFT_NEEDS_MATH_EDUCATOR_REVIEW'
        assert all(d[k] is False for k in ('official_source_claim', 'grants_gems', 'mastery_countable'))
    issues = []
    issue_ids = {key: [] for key in list(units) + list(lessons) + list(sections)}

    def issue(code, ids, detail, disposition='PRESERVE_RAW_BLOCK_PENDING_SOURCE_REVIEW'):
        issues.append(dict(code=code, ids=ids, detail=detail, disposition=disposition))
        for rid in ids:
            if rid in issue_ids: issue_ids[rid].append(code)

    for uid, u in units.items():
        if u['end_pdf_page'] < u['start_pdf_page']:
            issue('INVALID_UNIT_RANGE', [uid], {'printed': [u['start_printed_page'], u['end_printed_page']], 'pdf': [u['start_pdf_page'], u['end_pdf_page']]})
    for sid, (u, s) in sections.items():
        if s['pdf_end'] < s['pdf_start']:
            issue('INVALID_SECTION_RANGE', [sid], {'printed': [s['printed_start'], s['printed_end']], 'pdf': [s['pdf_start'], s['pdf_end']]})
    # This narrowly verified exception preserves two identities, with one shared page.
    # Any different pair, widened overlap or changed start still raises a source issue.
    shared_pages = []
    lesson_values = list(lessons.values())
    for i, a in enumerate(lesson_values):
        for b in lesson_values[i + 1:]:
            if a['source_book_id'] == b['source_book_id'] and max(a['pdf_page_start'], b['pdf_page_start']) <= min(a['pdf_page_end'], b['pdf_page_end']):
                span = [max(a['pdf_page_start'], b['pdf_page_start']), min(a['pdf_page_end'], b['pdf_page_end'])]
                if [a['id'], b['id']] == ['bac-1-u05-l04', 'bac-1-u05-l05'] and span == [164, 164] and a['pdf_page_start'] == a['pdf_page_end'] == b['pdf_page_start'] == 164:
                    shared_pages.append(dict(lessonIds=[a['id'], b['id']], printedPage=163, pdfPage=164,
                        status='VERIFIED_SHARED_START_PAGE', evidence='BAC_SOURCE_VERIFIED_ERRATA.md',
                        remainingReview='bac-1-u05-l05 end page 167 retained; detailed boundary review pending'))
                else:
                    issue('OVERLAPPING_LESSON_RANGE', [a['id'], b['id']], span)
    pages = []
    for source in sources.values():
        volume = source['volume']; offset = 1 if volume == 1 else 0
        group = [r for r in page_rows if int(r['book']) == volume]
        assert sorted(int(r['pdf_page']) for r in group) == list(range(1, source['pdf_page_count'] + 1))
        for r in group:
            n = int(r['pdf_page']); printed = int(r['printed_page']) if r['printed_page'] else None
            assert printed is None or n - printed == offset
            assert r['manual_page_content_reviewed'] == 'False'
            assert not r['unit_id'] or r['unit_id'] in units
            ids = [v.strip() for v in r['section_ids'].split('|') if v.strip()]
            candidates = [l['id'] for l in lessons.values() if l['source_book_id'] == source['id'] and l['pdf_page_start'] <= n <= l['pdf_page_end']]
            candidates += [sid for sid, (u, s) in sections.items() if u['volume'] == volume and s['pdf_start'] <= n <= s['pdf_end']]
            page_issues = []
            # Reference/test labels are free text, not catalog record IDs.
            known_ids = [rid for rid in ids if rid in lessons or rid in sections]
            if set(candidates) != set(known_ids): page_issues.append('INVENTORY_ANCHOR_MISMATCH')
            expected_unit = next((u['id'] for u in units.values() if u['volume'] == volume and u['start_pdf_page'] <= n <= u['end_pdf_page']), None)
            if (r['unit_id'] or None) != expected_unit: page_issues.append('INVENTORY_UNIT_MISMATCH')
            # The invalid final-unit boundary caused the supplied inventory to label late content as front matter.
            if volume == 1 and 218 <= n <= 251 and not r['unit_id']:
                page_issues.append('INVENTORY_AFTER_INVALID_UNIT_BOUNDARY')
            if page_issues: issue('PAGE_INVENTORY_CONFLICT', [f'{source["id"]}:page:{n}'], {'raw': r, 'candidateIds': candidates, 'issues': page_issues},
                                 'RAW_ANCHORS_STALE_REGENERATED_FROM_CORRECTED_CATALOG' if page_issues == ['INVENTORY_ANCHOR_MISMATCH'] else 'PRESERVE_RAW_BLOCK_PENDING_SOURCE_REVIEW')
            # Regenerate TOC anchors from corrected ranges while retaining raw evidence above.
            regenerated_ids = candidates + [rid for rid in ids if rid not in lessons and rid not in sections]
            pages.append(dict(sourceId=source['id'], pdfPage=n, printedPage=printed, unitId=expected_unit,
                              role=r['page_kind_estimated_from_toc'], ids=regenerated_ids, rawIds=ids, candidates=candidates, issues=page_issues))
    audit = json.loads((DIR / 'AUDIT_AND_CROSSWALK.json').read_text(encoding='utf-8'))
    lines = ['// Generated by tools/import_bac_source.py. Blocked source metadata only.',
             'package com.ahmadmol.enmath.core.data', '', 'internal object BacSourceData {', '    val books = listOf(']
    for s in sources.values():
        lines.append(f'        BacSourceBook({q(s["id"])}, "book-{s["volume"]}", {s["volume"]}, {q(s["title"])}, {q(s["cover_edition_label"])}, {q(s["sha256"])}, {s["pdf_page_count"]}, {1 if s["volume"] == 1 else 0}),')
    lines += ['    )', '    val units = listOf(']
    for u in units.values():
        source = next(s for s in sources.values() if s['volume'] == u['volume'])
        span = f'SourceSpan({u["start_printed_page"]}, {u["end_printed_page"]}, {u["start_pdf_page"]}, {u["end_pdf_page"]})'
        lines.append(f'        BacSourceUnit({q(u["id"])}, {q(source["id"])}, {u["order"]}, {q(u["title_ar"])}, {span}, {string_list(l["id"] for l in u["lessons"])}, {string_list(s["id"] for s in u["activities"] + u["exercise_sets"])}, {string_list(issue_ids[u["id"]])}),')
    lines += ['    )', '    val lessons = listOf(']
    coverage = []
    for l in lessons.values():
        span = f'SourceSpan({l["source_page_start"]}, {l["source_page_end"]}, {l["pdf_page_start"]}, {l["pdf_page_end"]})'
        lines.append(f'        BacSourceLesson({q(l["id"])}, {q(l["source_unit_id"])}, {q(l["source_book_id"])}, {l["order"]}, {q(l["title_ar"])}, {q(l["source_edition_label"])}, {span}, {q(l["notes"])}, {string_list(issue_ids[l["id"]])}),')
        u = units[l['source_unit_id']]
        coverage.append(dict(lessonId=l['id'], unitId=u['id'], sourceId=l['source_book_id'], edition=l['source_edition_label'],
                             title=l['title_ar'], printedFrom=l['source_page_start'], printedTo=l['source_page_end'],
                             pdfFrom=l['pdf_page_start'], pdfTo=l['pdf_page_end'], status='BLOCKED_EDITION_REVIEW',
                             sourceIssues='|'.join(issue_ids[l['id']] + issue_ids[u['id']]),
                             explanation='NOT_TRANSCRIBED', definitionsProofs='NOT_REVIEWED', examples='NOT_REVIEWED',
                             activitySections='|'.join(s['id'] for s in u['activities']),
                             exerciseSections='|'.join(s['id'] for s in u['exercise_sets']),
                             individualExerciseIds='NOT_EXTRACTED', figures='NOT_REVIEWED', answerKey='NOT_VERIFIED',
                             checker='NOT_IMPLEMENTED', mathReview='PENDING', rightsReview='PENDING', ready='false',
                             rangeReview='END_BOUNDARY_PENDING' if l['id'] == 'bac-1-u05-l05' else 'INDEX_ONLY',
                             sharedStartPage=163 if l['id'] in ('bac-1-u05-l04', 'bac-1-u05-l05') else ''))
    lines += ['    )', '    val sections = listOf(']
    for sid, (u, s) in sections.items():
        source = next(v for v in sources.values() if v['volume'] == u['volume'])
        span = f'SourceSpan({s["printed_start"]}, {s["printed_end"]}, {s["pdf_start"]}, {s["pdf_end"]})'
        lines.append(f'        BacSourceSection({q(sid)}, {q(u["id"])}, {q(source["id"])}, {q(s["kind"])}, {q(s["title_ar"])}, {span}, {string_list(issue_ids[sid])}),')
    lines += ['    )', '    val pages = pagesPart1() + pagesPart2()']
    for volume in (1, 2):
        source = next(v for v in sources.values() if v['volume'] == volume)
        lines += [f'    private fun pagesPart{volume}() = listOf(']
        for p in (p for p in pages if p['sourceId'] == source['id']):
            lines.append(f'        BacInventoryPage({q(p["sourceId"])}, {p["pdfPage"]}, {"null" if p["printedPage"] is None else p["printedPage"]}, {"null" if p["unitId"] is None else q(p["unitId"])}, {q(p["role"])}, {string_list(p["ids"])}, {string_list(p["candidates"])}, {string_list(p["issues"])}),')
        lines += ['    )']
    lines += ['    val drafts = listOf(']
    for d in drafts:
        lines.append(f'        BacDraftReview({q(d["id"])}, {q(d["source_unit_id"])}),')
    lines += ['    )', '    val legacyRelations = listOf(']
    for r in audit['legacyCrosswalk']:
        lines.append(f'        LegacySourceRelation({q(r["legacyLessonId"])}, {string_list(r["relatedSourceLessonIds"])}),')
    lines += ['    )', '}', '']
    summary = dict(schemaVersion=1, sourceFileCount=len(files), sourceHashes={n: hashlib.sha256(b).hexdigest() for n, b in sorted(files.items())},
                   books=2, units=14, lessons=63, activitySections=13, exerciseSections=14, pageInventory=470,
                   pageContentReviewed=0, originalDrafts=28, approvedLessons=0, approvedExercises=0,
                   issueCount=len(issues), inventoryPagesFlagged=sum(bool(p['issues']) for p in pages), issues=issues,
                   verifiedSharedPages=shared_pages, regeneratedAnchorCorrections=sum(p['ids'] != p['rawIds'] for p in pages),
                   remainingSourceReviews=[dict(lessonId='bac-1-u05-l05', field='source_page_end', retainedValue=167, status='DETAILED_BOUNDARY_REVIEW_PENDING')])
    queue = []
    for p in pages:
        page_lessons = [rid for rid in p['ids'] if rid in lessons]
        queue.append(dict(id=f'{p["sourceId"]}:page:{p["pdfPage"]}', level='PAGE', sourceId=p['sourceId'], pdfPage=p['pdfPage'],
                          unitId=p['unitId'], lessonId=page_lessons[0] if len(page_lessons) == 1 else None,
                          lessonIds=page_lessons, sourcePrintedPage=p['printedPage'], sourceRecordIds=p['ids'], candidateIds=p['candidates'],
                          rightsReview='PENDING', mathReview='PENDING', figureRequired=None, sourceExerciseNumber=None,
                          publication='BLOCKED_EDITION_REVIEW', issues=p['issues']))
    for l in coverage:
        queue.append(dict(id=l['lessonId'], level='LESSON', sourceId=l['sourceId'], unitId=l['unitId'], lessonId=l['lessonId'],
                          sourceEdition=l['edition'], pdfPage=None, pdfFrom=l['pdfFrom'], pdfTo=l['pdfTo'],
                          rightsReview='PENDING', mathReview='PENDING', figureRequired=None, sourceExerciseNumber=None,
                          publication='BLOCKED_EDITION_REVIEW', missing=['edition_comparison', 'page_content_review', 'authored_explanation',
                              'definitions_proofs_examples', 'exercise_itemization', 'independent_solutions', 'figure_review', 'checker_tests', 'rights_clearance'] +
                              (['detailed_end_boundary_review'] if l['lessonId'] == 'bac-1-u05-l05' else [])))
    for sid, (u, s) in sections.items():
        queue.append(dict(id=sid, level='ACTIVITY_SECTION' if s['kind'] == 'activity' else 'EXERCISE_SECTION', unitId=u['id'], lessonId=None,
                          pdfPage=s['pdf_start'], sourceSpan=s, rightsReview='PENDING', mathReview='PENDING',
                          figureRequired=None, sourceExerciseNumber=None, items=[], publication='BLOCKED_EDITION_REVIEW', issues=issue_ids[sid]))
    for d in drafts:
        queue.append(dict(id=d['id'], level='ORIGINAL_DRAFT_QUESTION', unitId=d['source_unit_id'], lessonId=None, pdfPage=None,
                          rightsReview='PENDING', mathReview='PENDING', figureRequired=None, sourceExerciseNumber=None,
                          publication='DRAFT_NEEDS_MATH_EDUCATOR_REVIEW', grantsGems=False, masteryCountable=False))
    for u in units.values():
        queue.append(dict(id=u['id'], level='UNIT', unitId=u['id'], lessonId=None, pdfPage=u['start_pdf_page'],
                          sourceSpan={k: u[k] for k in ('start_printed_page', 'end_printed_page', 'start_pdf_page', 'end_pdf_page')},
                          rightsReview='PENDING', mathReview='PENDING', figureRequired=None, sourceExerciseNumber=None,
                          publication='BLOCKED_EDITION_REVIEW', issues=issue_ids[u['id']]))
    for review in queue:
        source_id = review.get('sourceId') or next(s['id'] for s in sources.values() if s['volume'] == units[review['unitId']]['volume'])
        source = sources[source_id]
        review.update(stage='BAC', sourceBookId=source_id, sourceEdition=source['cover_edition_label'],
                      sourceSha256=source['sha256'], contentRevision=1)
    summary['reviewRecords'] = len(queue)
    outputs = {OUT: '\n'.join(lines), DIR / 'SOURCE_VALIDATION.json': json.dumps(summary, ensure_ascii=False, indent=2) + '\n',
               DIR / 'PAGE_INVENTORY_REGENERATED.csv': csv_text([dict(book=sources[p['sourceId']]['volume'], pdf_page=p['pdfPage'],
                   printed_page=p['printedPage'], unit_id=p['unitId'], page_kind_estimated_from_toc=p['role'],
                   section_ids=' | '.join(p['ids']), manual_page_content_reviewed=False,
                   raw_section_ids=' | '.join(p['rawIds']), source_issues='|'.join(p['issues'])) for p in pages]),
               DIR / 'PAGE_COVERAGE.csv': csv_text([dict(sourceId=p['sourceId'], pdfPage=p['pdfPage'], printedPage=p['printedPage'],
                   unitId=p['unitId'], role=p['role'], sectionIds='|'.join(p['ids']), candidateIds='|'.join(p['candidates']),
                   manualPageContentReviewed=False, issues='|'.join(p['issues'])) for p in pages]),
               DIR / 'LESSON_COVERAGE.csv': csv_text(coverage), DIR / 'REVIEW_QUEUE.json': json.dumps(dict(contentRevision='bac-source-index-v1', reviews=queue), ensure_ascii=False, indent=2) + '\n',
               DIR / 'UNIT_COVERAGE.csv': csv_text([dict(unitId=u['id'], title=u['title_ar'], lessonCount=len(u['lessons']),
                   printedFrom=u['start_printed_page'], printedTo=u['end_printed_page'], pdfFrom=u['start_pdf_page'], pdfTo=u['end_pdf_page'],
                   activitySectionIds='|'.join(s['id'] for s in u['activities']), exerciseSectionIds='|'.join(s['id'] for s in u['exercise_sets']),
                   status='BLOCKED_EDITION_REVIEW', issues='|'.join(issue_ids[u['id']])) for u in units.values()]),
               DIR / 'SECTION_COVERAGE.csv': csv_text([dict(sectionId=sid, unitId=u['id'], kind=s['kind'], title=s['title_ar'],
                   printedFrom=s['printed_start'], printedTo=s['printed_end'], pdfFrom=s['pdf_start'], pdfTo=s['pdf_end'],
                   individualItems='NOT_EXTRACTED', status='LOCKED_SOURCE_REVIEW', issues='|'.join(issue_ids[sid])) for sid, (u, s) in sections.items()]),
               DIR / 'ITEM_COVERAGE.csv': csv_text([dict(id=d['id'], unitId=d['source_unit_id'], lessonId='', pdfPage='',
                   sourceExerciseNumber='', status=d['publication_status'], originalNotTextbook=True,
                   independentlyReviewed=False, checkerApproved=False, figureRequired='UNKNOWN', grantsGems=False) for d in drafts])}
    for path, value in outputs.items():
        if check: assert path.read_bytes() == value.encode('utf-8'), f'Stale output: {path.name}'
        else: path.write_bytes(value.encode('utf-8'))
    print(json.dumps({k: v for k, v in summary.items() if k not in ('issues', 'sourceHashes')}, ensure_ascii=False))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__); parser.add_argument('--check', action='store_true')
    import_source(parser.parse_args().check)
