# EN.MATH — أمر Codex لتدقيق الخطة الرئيسية وتثبيتها مقابل الشيفرة

**التشغيل المقترح:** Codex Agent Mode مع السماح بكتابة **وثائق فقط** تحت `docs/`، دون أي تعديل لشيفرة التطبيق/الموارد/Gradle، أو Commit/Push. قبل ذلك انسخ الخطة الرئيسية المرفقة إلى `docs/PRODUCT/ENMATH_STUDENT_MASTER_PLAN.md` في نسخة المستودع الحالية. لا تحذف أي تقرير قائم.

---

اعمل كفريق مراجعة تقني يتكون من:
**Senior Kotlin Developer + Jetpack Compose Architect + Software Architect + EdTech Product Manager + Learning Experience Designer + QA Lead + Security & Privacy Reviewer + Low-Cost Cloud Architect.**

## الهدف

**المهمة مراجعة وتصحيح وإكمال وثائق خطة EN.MATH، وليست تنفيذ التطبيق.** اقرأ `docs/PRODUCT/ENMATH_STUDENT_MASTER_PLAN.md` كاملة؛ تعامل معها كـProduct Intent يجب تدقيقه لا كحقائق عن الموجود. لا تكتف بالموافقة، واعترض بدليل عندما تكون الخطة غير مناسبة للشيفرة أو لواقع الاستخدام السوري.

## المصادر

1. افحص Git Working Tree الحالية وHEAD/Branch والملفات غير الملتزم بها مع المحافظة على كل عمل سابق.
2. اقرأ تقارير `docs/PRODUCT/`, `docs/UI_AUDITS/`, `docs/` المتعلقة بالمناهج والطالب والجواهر والمصادقة والمحتوى، ولا تتعامل مع تقرير قديم كحالة حالية ما لم تؤكده الشيفرة.
3. اقرأ ملفات Kotlin/Compose/Repositories/ViewModels/Routes/Tests/Gradle/Manifest الفعلية حسب صلتها بالمرحلة، مع تقرير كامل لما لا يمكن التحقق منه محليًا.
4. لا تخمّن فهرس أو محتوى الكتب إن لم تكن كتب التاسع والبكالوريا الرسمية وطبعاتها في المشروع. علِّمها `BLOCKED_BY_TEXTBOOK`.
5. لا تفترض أن الاستضافة أو SMS/Auth تعمل من سوريا؛ هذه **قرارات اختبار وتشغيل لاحقة**، لا نتيجة تحليل ساكن.

## المخرجات المطلوبة — docs فقط

- `docs/PRODUCT/ENMATH_STUDENT_MASTER_PLAN_VALIDATED.md`: إصدار منقح من الخطة مع علامات `[VERIFIED] / [ASSUMPTION] / [BLOCKED] / [PRODUCT_DECISION]`.
- `docs/PRODUCT/ENMATH_STUDENT_FINAL_PRD_DRAFT.md`: PRD يغطي الطالب التاسع والبكالوريا وكل ميزات الخطة، وقصص مستخدمين بمعايير قبول Given/When/Then؛ `DRAFT` إلى حين الكتب والقرارات الناقصة.
- `docs/PRODUCT/ENMATH_STUDENT_SCREEN_MATRIX.md`: **كل شاشة ومسار/Route/TestTag الحالي** مع مطلوب/موجود/جزئي/ديمو/غير مثبت، وحالات Loading/Empty/Offline/Error/Permission/Light-Dark/RTL-LTR/×2.
- `docs/PRODUCT/ENMATH_EXECUTION_BACKLOG.md`: Epics→Stories→Dependencies→Affected Code→Unit/Integration/Device tests→Definition of Done؛ تسلسل واقعي يحافظ على تعديلات المشروع.
- `docs/ARCHITECTURE/ENMATH_TARGET_ARCHITECTURE_DRAFT.md`: العقود، البيانات، هوية زائر ومرحلة الحساب الثابتة، Room/migrations، محتوى Pack، R2/Bunny، Auth/Sync المشروط، سياسة Secrets والخصوصية والويب المستقبلي.
- `docs/QA/ENMATH_STUDENT_RELEASE_ACCEPTANCE_MATRIX.md`: تغطية حالات إكمال/إتقان/دفتر أخطاء/مخطط/تحفيز/Gems/offline/حسابات/Grade9/Bac/QA محتوى.
- `docs/PRODUCT/ENMATH_OPEN_DECISIONS.md`: القرارات التي يحتاجها صاحب المشروع، مع توصية ومخاطر وبديل ولا تختر نيابة عنه.
- `docs/AUDITS/ENMATH_PLAN_TO_CODE_GAP_REPORT.md`: أخطر فجوات الإنتاج وفهرس مصادرها وأدلة ملفاتها، وما الذي يجب إصلاحه فورًا دون تنفيذ الآن.

## قواعد المنتج التي لا تغيّرها من نفسك

- الحساب الطالب مرحلته **ثابتة** ويفصل تاسع عن بكالوريا، وفق رغبة المالك؛ اعرض بدائل هندسية كـADR لا تغير القرار بصمت.
- الشاشات مكونات مشتركة reusable/dynamic للمرحلتين، والمعلم خارج رحلة الطالب مع حفظ الكود/البيانات القديمة.
- خطتي: DAILY/WEEKLY/MONTHLY وACHIEVEMENT/TIME/HYBRID؛ الافتراضي WEEKLY + ACHIEVEMENT، ويشمل كل المواد، دون منح Gems لإنجاز يدوي.
- التحفيز غير العقابي؛ الفيزياء المجانية فعلًا بعد التحقق من الحقوق؛ كورسات بقية المواد «قريبًا» دون إيهام بكونها مدفوعة/منشورة.
- عدم فقد تقدم الزائر والحساب؛ فصل تقدم الطالب عن حذف الحزمة أو التحديث؛ الإتقان لا يساوي إنهاء الشاشة؛ أخطاء حساب الجواهر التجريبية يجب توثيقها.
- المصادقة الإنتاجية ليست AuthViewModel التجريبي؛ هاتف وكلمة مرور قرار مشروط بتسليم واستعادة في سوريا؛ لا تُضف مزودًا أو تغيّر قاعدة بيانات في هذه المهمة.
- الكتب: كتاب أول بكالوريا، ثم ثانٍ، ثم التاسع؛ تمهيد معماري للمرحلتين من البداية؛ محتوى مطابق للطبعة بعد استلامه فقط.
- App Android أولًا؛ المعلم والويب وAI/OCR العام مؤجلون.

## كيفية التسليم

1. `EXECUTIVE DECISIONS`: ما الذي تقبله، وتختلف معه، وما الذي ينقصه دليل.
2. `CURRENT vs TARGET`: لكل فئة Feature وجود فعلي وملفات/Routes أدلة.
3. `DEPENDENCY GRAPH`: ترتيب المراحل والمخاطر؛ لا تسمِّها Final إذا الكتب/الـBackend/الاعتماد البصري لم يحسموا.
4. `LOW-COST STRATEGY`: ثلاثة سيناريوهات محلي مع Content CDN، API خفيف، Auth+Sync لاحقًا مع حقائق تكلفتها ومخاطر سوريا.
5. `READY TO EXECUTE FIRST PHASE`: تذاكر ضيقة لمرحلة P0 بيانات/هويات ووحدة تعليمية؛ **لا تنفذها الآن**.
6. `NO FALSE COMPLETION`: قل صراحة ما لم تختبره على هاتف أو علميًا.

**محظور:** تعديل أي ملف `.kt`, `.kts`, Gradle, resources, Manifest, databases، تشغيل emulator أو ADB أو تثبيت على هاتف، تعديل Gem balances، حذف بيانات أو عمل Commit/Push أو رفع مستودع. حافظ على كل تغييرات Working Tree. إذا تعارضت وثائق مع شيفرة أو قرار مستخدم، وثّق الاختلاف بوضوح، ولا تعوّضه ببيانات وهمية.

ابدأ بجرد المراجع والشيفرة ثم أنشئ الوثائق المطلوبة فقط، وبعدها قدّم التقرير النهائي واطلب اعتماد PRD قبل التنفيذ.
