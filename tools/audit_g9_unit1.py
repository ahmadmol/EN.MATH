"""Read-only mathematical audit and teacher handoff. Never publish or write review approvals."""
import hashlib
import json
from pathlib import Path
from g9_content_workflow import validate
from g9_unit1_math import validate_item

ROOT=Path(__file__).resolve().parents[1]
def audit(output):
    rows=[]
    for path in sorted((ROOT/'docs/curriculum/g9/authoring/unit1').glob('*.json')):
        d=json.loads(path.read_bytes());errors=validate(d)
        items=[]
        for kind,values in [('example',d['examples']),('practice',d['questions']),('checkpoint',[d['checkpoint']])]:
            for item in values:
                failures=validate_item(item)
                items.append(dict(id=item['id'],kind=kind,exactMath='PASS' if not failures else 'FAIL',errors=failures))
        rows.append(dict(lessonId=d['source']['lessonId'],title=d['source']['title'],source=d['source'],
            file=str(path.relative_to(ROOT)),sha256=hashlib.sha256(path.read_bytes()).hexdigest(),
            engineeringMath='PASS' if not errors else 'FAIL',schemaErrors=errors,items=items,
            releaseGate='BLOCKED' if validate(d,True) else 'CANDIDATE_REQUIRES_EVIDENCE_CHECK',
            independentEducatorReview='NOT_PERFORMED',studentAvailability='EXISTING_PILOT_ONLY' if d['source']['lessonId'].endswith('L03') else 'UNAVAILABLE'))
    assert len(rows)==4 and all(r['engineeringMath']=='PASS' and r['releaseGate']=='BLOCKED' for r in rows)
    output.mkdir(parents=True,exist_ok=True)
    (output/'unit1-audit.json').write_text(json.dumps(rows,ensure_ascii=False,indent=2),encoding='utf-8')
    lines=['# تسليم حزمة الوحدة الأولى للمعلم المختص','',
      'هذه مراجعة حسابية برمجية، وليست اعتمادًا تربويًا مستقلًا. لا نشر تلقائي ولا تعديل لملفات الطالب.',
      'يُعاد تشغيل tools/audit_g9_unit1.py بعد أي تعديل. تعتمد المراجعة على بصمة الملف المحددة، وأي تعديل لاحق يحتاج إعادة مراجعة.', '',
      'يسجل المعلم اسمه وصفته وتاريخ المراجعة والدليل خارج هذا السجل المولّد، مع ذكر معرف كل مثال وسؤال وملاحظاته. لا تُملأ موافقات افتراضية.', '',
      'لكل بحث: تحقق من صفحات المصدر وطبعة السنة وحقوق الاستخدام، ثم اللغة والأهداف والمتطلبات، وشروط الصيغ والمجال، وتسلسل خطوات الحل، وصحة جميع الخيارات والمشتتات، والتلميحات والتصحيح، والصعوبة لعمر 14–15. افحص استقلالية أسئلة التحقق عن أمثلة التدريب.', '',
      'قبل الاعتماد: جميع مراجعات المصدر والحقوق والرياضيات والتربية ومطابقة الطبعة PASS بدليل قابل للفحص؛ المراجع التربوي مستقل؛ لا تُستنتج الموافقة من نجاح الاختبارات. بعد ذلك فقط قرار هندسي منفصل لإضافة نسخة محتوى واختبار حفظها وعرضها، مع فحص هاتف مصرح. لا تتحول المسودة إلى درس بمجرد تعديل JSON.', '']
    for row in rows:
        lines += ['## '+row['title'], '', 'الملف: `'+row['file']+'`', 'البصمة: `'+row['sha256']+'`',
            'صفحات المصدر: '+str(row['source']['pdfFrom'])+'–'+str(row['source']['pdfTo'])+'؛ طبعة '+row['source']['edition'],
            'الجاهزية: الحسابات اجتازت الفحص؛ اعتماد المعلم ومطابقة السنة والحقوق بانتظار الدليل. بوابة النشر مغلقة.', '',
            '| المعرف | النوع | تحقق حسابي | قرار المعلم |','|---|---|---|---|']
        lines += ['| '+x['id']+' | '+x['kind']+' | '+x['exactMath']+' | لم يراجع بعد |' for x in row['items']]
        lines += ['']
    (output/'TEACHER_REVIEW_HANDOFF.md').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    print(json.dumps(dict(topics=len(rows),examples=sum(x['kind']=='example' for r in rows for x in r['items']),questions=sum(x['kind']!='example' for r in rows for x in r['items']),release='BLOCKED')))
if __name__=='__main__':audit(ROOT/'docs/QA/g9-phase5-2026-10-30')
