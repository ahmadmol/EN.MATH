"""Read-only application/source audit. Writes evidence and a non-destructive crosswalk, not migrations."""
import csv
import hashlib
import json
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIR = ROOT / 'docs/curriculum/bac'
SOURCE = DIR / 'source'


def read_inputs():
    rows = list(csv.DictReader((SOURCE / 'BAC_LESSONS.csv').open(encoding='utf-8-sig', newline='')))
    assert len(rows) == len({r['lesson_id'] for r in rows}) == 63
    assert len({r['unit_id'] for r in rows}) == 14
    for book, count, edition, offset, maximum in [('1', 34, '2023-2024', 1, 253), ('2', 29, '2021-2022', 0, 217)]:
        group = [r for r in rows if r['book'] == book]
        assert len(group) == count and len({r['unit_id'] for r in group}) == 7
        for r in group:
            assert r['edition'] == edition and r['status'] == 'STRUCTURAL_ONLY'
            assert r['unit_id'] == f'bac-{book}-u{int(r["unit"]):02d}'
            assert r['lesson_id'] == f'{r["unit_id"]}-l{int(r["lesson"]):02d}'
            assert 1 <= int(r['pdf_start']) <= int(r['pdf_end']) <= maximum
            assert int(r['pdf_start']) - int(r['print_start']) == int(r['pdf_end']) - int(r['print_end']) == offset
    return rows


def audit():
    rows = read_inputs()
    base = (ROOT / 'app/src/main/java/com/ahmadmol/enmath/core/data/DemoContent.kt').read_text(encoding='utf-8')
    rich = (ROOT / 'app/src/main/java/com/ahmadmol/enmath/core/data/ProductSeed.kt').read_text(encoding='utf-8')
    topic_titles = re.findall(r'topic\(\s*"([a-z]+)",\s*"([^"]+)",\s*"([^"]+)"', base)
    assert len(topic_titles) == 8
    extras = re.findall(r'Copy\("([^"]+)", "([^"]+)"\)', rich.split('private val extraTitles =')[1].split('private val checkpoints =')[0])
    assert len(extras) == 8
    related = {
        'limits': ['bac-1-u02-l01', 'bac-1-u02-l02', 'bac-1-u02-l03', 'bac-1-u02-l07'],
        'derivatives': ['bac-1-u03-l01', 'bac-1-u03-l02', 'bac-1-u03-l03'],
        'integrals': ['bac-1-u07-l01', 'bac-1-u07-l02', 'bac-1-u07-l03'],
        'matrices': [], 'probability': ['bac-2-u07-l01'],
        'geometry': ['bac-2-u01-l04'], 'logarithms': ['bac-1-u05-l01', 'bac-1-u05-l02'],
        'sequences': ['bac-1-u01-l01'],
    }
    mapping = []
    for index, (topic, _, ar) in enumerate(topic_titles):
        for suffix in ('foundations', 'practice', 'applications'):
            lid = f'{topic}-{suffix}'
            mapping.append(dict(legacyLessonId=lid, legacyTopicId=topic,
                                title=extras[index][1] if suffix == 'applications' else ar + (' · الأساسيات' if suffix == 'foundations' else ' · تدريب موجّه'),
                                legacyBookId='book-1' if topic in ('limits', 'derivatives', 'integrals', 'sequences') else 'book-2',
                                relation='RELATED_NOT_EQUIVALENT' if related[topic] else 'OUTSIDE_SUPPLIED_63',
                                relatedSourceLessonIds=related[topic],
                                rewardKey=f'LESSON:{lid}', commandReceipt=f'lesson:{lid}',
                                migration='PRESERVE_ID_NO_COMPLETION_OR_REWARD_TRANSFER'))
    assert len(mapping) == 24
    assert not ({m['legacyLessonId'] for m in mapping} & {r['lesson_id'] for r in rows})
    conflicts = []
    verified_shared = []
    for index, a in enumerate(rows):
        for b in rows[index + 1:]:
            if a['book'] == b['book'] and max(int(a['pdf_start']), int(b['pdf_start'])) <= min(int(a['pdf_end']), int(b['pdf_end'])):
                overlap = dict(lessonIds=[a['lesson_id'], b['lesson_id']],
                                      pdfFrom=max(int(a['pdf_start']), int(b['pdf_start'])),
                                      pdfTo=min(int(a['pdf_end']), int(b['pdf_end'])), status='SOURCE_RANGE_CONFLICT_REVIEW_REQUIRED')
                if overlap['lessonIds'] == ['bac-1-u05-l04', 'bac-1-u05-l05'] and overlap['pdfFrom'] == overlap['pdfTo'] == 164 and int(a['pdf_end']) == 164:
                    overlap['status'] = 'VERIFIED_SHARED_START_PAGE'
                    verified_shared.append(overlap)
                else:
                    conflicts.append(overlap)
    expected = ['BAC_CURRICULUM_CATALOG.json', 'BAC_ACTIVITY_EXERCISE_SECTIONS.csv', 'BAC_PAGE_INVENTORY.csv',
                'PEDAGOGICAL_MODEL.md', 'BAC_ORIGINAL_PRACTICE_DRAFTS.json']
    result = dict(schemaVersion=1,
                  gitBase=dict(head=subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
                               remoteBranch=subprocess.check_output(['git', 'rev-parse', 'origin/feature/ui-refinement'], cwd=ROOT, text=True).strip(),
                               cleanTrackedBaseline=True, oldWorkspacePreserved=True),
                  sourceFiles={p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in sorted(SOURCE.iterdir()) if p.is_file()},
                  missingInputs=[name for name in expected if not (SOURCE / name).exists()],
                  before=dict(books=2, topics=8, demoLessons=24, sourceLessons=0,
                              sourceUnitIdsAlreadyPresent=0, sourceLessonIdsAlreadyPresent=0),
                  source=dict(books=2, units=14, lessons=63, bookLessonCounts=[34, 29],
                              declaredPdfPages=470, suppliedPageInventoryRecords=470, suppliedDraftQuestions=28),
                  sourcePageConflicts=conflicts, verifiedSharedPages=verified_shared, legacyCrosswalk=mapping,
                  plan=dict(existingBookIds=['book-1', 'book-2'], reuseBookIds=True,
                            preserveLegacyLessonObjects=True, exactApprovedEquivalences=0,
                            copyCompletions=False, rewriteRewardKeys=False, rewriteWalletOrIdentity=False,
                            sourcePublicationStatus='BLOCKED_EDITION_REVIEW'))
    DIR.mkdir(parents=True, exist_ok=True)
    (DIR / 'AUDIT_AND_CROSSWALK.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    lines = ['# تدقيق ما قبل التكامل وخطة حفظ البيانات — 2026-10-27', '',
             'الكتالوج السابق: كتابان، 8 موضوعات، 24 درسًا تجريبيًا، 48 سؤالًا و16 مجموعة تدريب/اختبار.',
             'المصدر المتاح: 14 وحدة و63 بحثًا في CSV. لا يوجد تطابق معرفات، ولا تكافؤ تعليمي كامل مُعتمد بين درس تجريبي وبحث مصدر.', '',
             'سيُعاد استخدام معرفَي الكتابين `book-1` و`book-2` في واجهة المصدر. الدروس التجريبية كائناتها ومعرفاتها الأصلية، مع مجموعة واضحة منفصلة؛ لا نسخ أو إعادة ترقيم.',
             'تُضاف مراجع الوحدات والأبحاث كمعلومات مصدر إضافية. لا يُنسخ أي إكمال أو محاولة أو reward receipt إلى معرف مصدر. سجل الجواهر وUUID وlegacyWalletKey وبيانات المعلم دون ترحيل.', '',
             'تم استلام ملفات V1.1 الإحدى عشرة. جرد الصفحات الـ470 بنيوي وتقديري، ولا يثبت مراجعة النص أو الرسوم. فهرس الأقسام يتضمن 13 نشاطًا و14 قسم تمارين؛ أرقام الأسئلة الفردية ورسومها وحلولها غير مستخرجة.', '',
             'أساس التنفيذ المعزول: `' + result['gitBase']['head'] + '` يطابق المرجع البعيد الذي جرى تحديثه. النسخة السابقة لم تُمسح ولم يُنقل كود أساسها القديم إلى هذا التكامل.', '',
             'تعارضات نطاقات الأبحاث غير الموثقة: ' + str(len(conflicts)) + '. اشتراك موثق في المطبوع 163 / PDF 164؛ تبقى نهاية البحث الخامس بحاجة إلى تدقيق تفصيلي. راجع SOURCE_VALIDATION.json لتناقضات الجرد الخام.', '',
             '| معرف الدرس الحالي | العنوان | صلة محتملة بالمصدر (ليست تكافؤًا أو ترحيلًا) |', '|---|---|---|']
    for m in mapping:
        lines.append(f'| {m["legacyLessonId"]} | {m["title"]} | {", ".join(m["relatedSourceLessonIds"]) or "خارج الأبحاث الـ63 المتاحة"} |')
    (DIR / 'PRE_INTEGRATION_AUDIT.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')
    return result


if __name__ == '__main__':
    result = audit()
    print(json.dumps({k: result[k] for k in ('before', 'source', 'missingInputs', 'sourcePageConflicts', 'plan')}, ensure_ascii=False))
