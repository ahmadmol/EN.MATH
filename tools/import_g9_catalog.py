"""Validate all supplied JSON and generate metadata only. No PDFs, draft prompts or device access.

Run again with --check to check determinism and source -> compiled catalog parity.
"""
import argparse
import csv
import io
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'docs/curriculum/g9/source'
OUTPUT = ROOT / 'app/src/main/java/com/ahmadmol/enmath/core/data/G9CatalogData.kt'
REVIEW = ROOT / 'docs/curriculum/g9/REVIEW_QUEUE.json'
REVISION = 'g9-index-2026-10-27-v1'


def quoted(value):
    return json.dumps(value, ensure_ascii=False).replace('$', '\\$')


def nullable(value):
    return 'null' if value is None else quoted(value)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true')
    args = parser.parse_args()
    loaded = {p.name: json.loads(p.read_text(encoding='utf-8-sig')) for p in sorted(SOURCE.glob('*.json'))}
    assert len(loaded) == 4
    catalog = loaded['G9_CURRICULUM_CATALOG_INDEX.json']
    page_map = loaded['G9_SOURCE_PAGE_MAP_200_PAGES.json']
    drafts = loaded['G9_ORIGINAL_EXERCISE_SEEDS_32.json']
    manifest = loaded['MANIFEST.json']
    assert catalog['schemaVersion'] == '0.1.0-index-only' and catalog['stage'] == 'G9'
    assert catalog['editionLabelOnSource'] == '2025-2026'
    assert catalog['publicationReview'] == 'NOT_VERIFIED_FOR_2026_2027'
    assert page_map['schemaVersion'] == drafts['schemaVersion'] == '0.1.0'
    sources = {s['bookId']: s for s in catalog['sources']}
    units = {u['unitId']: u for u in catalog['units']}
    lessons = {l['lessonId']: (u, l) for u in units.values() for l in u['lessons']}
    assert len(sources) == 2 and len(units) == 10 and len(lessons) == 32
    assert len([u for u in units.values() if u['sourceBookId'] == 'G9-ALG']) == 6
    assert len([u for u, _ in lessons.values() if u['sourceBookId'] == 'G9-ALG']) == 19
    assert sum(s['pages'] for s in sources.values()) == 200
    assert manifest['units'] == 10 and manifest['lessons'] == 32 and manifest['pagesMapped'] == 200
    assert manifest['originalDraftExercises'] == 32 and manifest['readyForStudentRelease'] is False
    for sid, source in sources.items():
        assert all(manifest['books'][sid][key] == val for key, val in source.items())
    for uid, unit in units.items():
        assert uid.startswith(unit['sourceBookId'] + '-U')
        assert unit['implementationState'] == 'INDEX_READY_CONTENT_NOT_TRANSCRIBED'
        for lesson in unit['lessons']:
            assert lesson['lessonId'].startswith(uid + '-L')
            assert lesson['lessonOutlineStatus'] == 'INDEX_VERIFIED_BY_PAGE_IMAGE'
            assert lesson['completeBookTextImported'] is False
            assert lesson['exerciseStatementsTranscribed'] is False
            assert lesson['pedagogicalReviewStatus'] == 'PENDING'
            assert lesson['publishingStatus'] == 'BLOCKED_PENDING_MATH_REVIEW_AND_RIGHTS'
    pages = page_map['pageIndex']
    assert len(pages) == 200
    assert len({(p['sourceBookId'], p['pdfPageNumberOneBased']) for p in pages}) == 200
    for sid, source in sources.items():
        assert sorted(p['pdfPageNumberOneBased'] for p in pages if p['sourceBookId'] == sid) == list(range(1, source['pages'] + 1))
        for p in (p for p in pages if p['sourceBookId'] == sid):
            n = p['pdfPageNumberOneBased']
            expected_unit = next((u for u in units.values() if u['sourceBookId'] == sid and u['sourcePages']['from'] <= n <= u['sourcePages']['to']), None)
            assert p['unitId'] == (expected_unit['unitId'] if expected_unit else None)
            expected_lesson = next((l for u, l in lessons.values() if u['sourceBookId'] == sid and l['sourcePages']['from'] <= n <= l['sourcePages']['to']), None)
            assert p['lessonId'] == (expected_lesson['lessonId'] if expected_lesson else None)
            role = 'front_matter' if not expected_unit else 'lesson_material_and_inlesson_activities' if expected_lesson else 'end_of_unit_exercises' if n >= expected_unit['reviewExercisePages']['from'] else 'unit_introduction'
            assert p['pageRole'] == role
            assert not any(p[k] for k in ('fullTextReviewed', 'figuresReviewed', 'exerciseItemsTranscribed'))
    assert len(drafts['items']) == 32 and len({d['itemId'] for d in drafts['items']}) == 32
    assert {d['lessonId'] for d in drafts['items']} == set(lessons)
    for d in drafts['items']:
        assert d['origin'] == 'ORIGINAL_EDUCATIONAL_DRAFT_NOT_FROM_TEXTBOOK'
        assert d['needsHumanMathReview'] is True
        assert all(d[k] is False for k in ('readyForPublishing', 'awardsGems', 'sourceTextCopied'))
        assert all(isinstance(d[k], str) and d[k] for k in ('promptArabic', 'expectedAnswer', 'explanationArabic', 'validatorProposal'))

    def ref(sid, uid=None, lid=None):
        return f'G9SourceRef(StudyStage.G9, {quoted(sid)}, {quoted(catalog["editionLabelOnSource"])}, {quoted(sources[sid]["sha256"])}, REVISION, {nullable(uid)}, {nullable(lid)})'

    def span(p):
        return f'SourcePages({p["from"]}, {p["to"]})'

    hashes = {p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(SOURCE.glob('*.json'))}
    lines = ['// Generated by tools/import_g9_catalog.py. Metadata only; do not edit.',
             'package com.ahmadmol.enmath.core.data', '', 'import com.ahmadmol.enmath.core.model.*', '',
             'internal object G9CatalogData {', f'    const val REVISION = {quoted(REVISION)}',
             f'    const val PUBLICATION_REVIEW = {quoted(catalog["publicationReview"])}',
             '    val sources = listOf(']
    for sid, s in sources.items():
        lines.append(f'        CatalogSource({ref(sid)}, {quoted(s["shortTitle"])}, {quoted(s["bookSubject"])}, {s["pages"]}),')
    lines += ['    )', '    val units = listOf(']
    for uid, u in units.items():
        sid = u['sourceBookId']
        lines.append(f'        CatalogUnit({ref(sid, uid)}, {u["orderInBook"]}, {quoted(u["title"])}, {span(u["sourcePages"])}, {span(u["introductionPages"])}, {span(u["reviewExercisePages"])}, listOf(')
        for l in u['lessons']:
            lines.append(f'            CatalogLesson({ref(sid, uid, l["lessonId"])}, {l["order"]}, {quoted(l["title"])}, {span(l["sourcePages"])}, {quoted(l["topicSummaryOriginal"])}, {quoted(l["draftLearningObjectiveOriginal"])}),')
        lines.append('        )),')
    lines += ['    )', '    val pages = listOf(']
    reviews = []

    def review(rid, sid, uid, lid, level, page):
        return dict(id=rid, stage='G9', sourceBookId=sid, edition=catalog['editionLabelOnSource'],
                    sourceSha256=sources[sid]['sha256'], unitId=uid, lessonId=lid, pdfPage=page,
                    contentRevision=REVISION, level=level, status='PENDING_REVIEW',
                    rightsReview='PENDING', mathReview='PENDING', figureRequired=None, sourceExerciseNumber=None)

    for p in pages:
        sid, uid, lid, n = p['sourceBookId'], p['unitId'], p['lessonId'], p['pdfPageNumberOneBased']
        lines.append(f'        SourcePage({ref(sid, uid, lid)}, {n}, {quoted(p["pageRole"])}, false, false, false),')
        reviews.append(review(f'{sid}:page:{n}', sid, uid, lid, 'PAGE', n))
    lines += ['    )', '    val patterns = listOf(']
    for p in catalog['instructionPatternsFromBooks']:
        lines.append(f'        BookElementPattern(BookElementKind.{p["uiBlock"]}, {quoted(p["bookHeading"])}, {quoted(p["purpose"])}),')
    lines += ['    )', '    val draftReviews = listOf(']
    for d in drafts['items']:
        u, l = lessons[d['lessonId']]
        sid, uid, lid = u['sourceBookId'], u['unitId'], l['lessonId']
        lines.append(f'        ContentReview({quoted(d["itemId"])}, {ref(sid, uid, lid)}, ReviewLevel.QUESTION, null),')
        reviews.append(review(d['itemId'], sid, uid, lid, 'QUESTION', None))
    lines += ['    )', '}', '']
    for u, l in lessons.values():
        reviews.append(review(l['lessonId'], u['sourceBookId'], u['unitId'], l['lessonId'], 'LESSON', None))
    queue = dict(schemaVersion=1, revision=REVISION, sourceHashes=hashes,
                 note='No activity instances verified; question page/figure requirements are unknown. Draft texts are review-only, excluded from APK.',
                 reviews=reviews)
    def csv_text(rows):
        stream = io.StringIO(newline='')
        writer = csv.DictWriter(stream, fieldnames=list(rows[0]))
        writer.writeheader(); writer.writerows(rows)
        return stream.getvalue()

    directory = REVIEW.parent
    outputs = {OUTPUT: '\n'.join(lines), REVIEW: json.dumps(queue, ensure_ascii=False, indent=2) + '\n',
        directory / 'UNIT_COVERAGE.csv': csv_text([dict(unitId=u['unitId'], sourceId=u['sourceBookId'], title=u['title'],
            pdfFrom=u['sourcePages']['from'], pdfTo=u['sourcePages']['to'], lessonCount=len(u['lessons']),
            reviewFrom=u['reviewExercisePages']['from'], reviewTo=u['reviewExercisePages']['to'],
            status='INDEX_READY_CONTENT_NOT_TRANSCRIBED', ready=False) for u in units.values()]),
        directory / 'LESSON_COVERAGE.csv': csv_text([dict(lessonId=l['lessonId'], unitId=u['unitId'], sourceId=u['sourceBookId'],
            edition=catalog['editionLabelOnSource'], title=l['title'], pdfFrom=l['sourcePages']['from'], pdfTo=l['sourcePages']['to'],
            text='NOT_TRANSCRIBED', exercises='NOT_EXTRACTED', figures='NOT_REVIEWED', mathReview='PENDING',
            rightsReview='PENDING', editionReview='NOT_VERIFIED_FOR_2026_2027', ready=False) for u,l in lessons.values()]),
        directory / 'PAGE_COVERAGE.csv': csv_text([dict(sourceId=p['sourceBookId'], pdfPage=p['pdfPageNumberOneBased'],
            unitId=p['unitId'], lessonId=p['lessonId'], role=p['pageRole'], fullTextReviewed=p['fullTextReviewed'],
            figuresReviewed=p['figuresReviewed'], exerciseItemsTranscribed=p['exerciseItemsTranscribed']) for p in pages]),
        directory / 'ITEM_COVERAGE.csv': csv_text([dict(id=d['itemId'], lessonId=d['lessonId'], origin=d['origin'],
            mathReview='PENDING', rightsReview='PENDING', ready=False, awardsGems=False) for d in drafts['items']])}
    for path, value in outputs.items():
        if args.check:
            assert path.read_bytes() == value.encode('utf-8'), f'Stale generated file: {path}'
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(value, encoding='utf-8', newline='\n')
    print(json.dumps(dict(books=2, units=10, lessons=32, pages=200, draftExercises=32,
                         verifiedActivities=0, readyLessons=0, educationalCoveragePercent=0,
                         reviewRecords=len(reviews), check=args.check)))


if __name__ == '__main__':
    main()
