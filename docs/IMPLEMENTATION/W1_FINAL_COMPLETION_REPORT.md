# EN.MATH — W1 Final Completion Report

2026-10-26 · Branch: `feature/ui-refinement` · HEAD: `01782ed96182eca69c0d22fe530b7f18e9f4c30f`

**W1 CODE-COMPLETE ضمن نطاق الأساس المحلي المعتمد لهذا التكليف؛ DEVICE ACCEPTANCE PENDING. ليست RELEASE-ACCEPTED أو Student Complete.** لا Commit أو Push، ولا W2 أو Backend أو Room شامل. نتائج التشغيل أدناه جديدة لهذه الجولة وليست نقلًا لنتائج النشر السابقة.

## حالة P0-01 إلى P0-06

| البند | حالة الشيفرة | ما نُفذ ودليل التحقق | ما لا تعنيه الحالة |
|---|---|---|---|
| P0-01 حماية البيانات | CODE-COMPLETE | إعادة استخدام ProductCodec الصارم والنسخة الإضافية للاستعادة؛ فصل decode/encode دون تغيير توافق product_v1؛ snapshots جديدة تحمل owner/version وتمنع overwrite عند التلف. فساد Legacy لا يمنع ملفًا مستقلًا آمنًا ولا يتحول إلى Seed. فشل commit لا يعلن نجاحًا، ويمنع الاستمرار بالمخزن غير المؤكد إلى إعادة التشغيل | لا إصلاح تلقائي أو استبدال للملف التالف؛ الاستعادة المعقدة تحتاج مراجعة. تنفيذ SharedPreferences على Android لم يُختبر على هاتف |
| P0-02 هوية الطالب والمرحلة | CODE-COMPLETE | UUID لكل ملف محلي؛ G9/BAC ثابتة في registry؛ لا owner من الاسم أو البريد للملفات الجديدة. تغيير الدور/المرحلة بـUser.copy مرفوض. اختيار ملف آخر صريح. Journal إضافي قابل للاستئناف لاستيراد scope قديم مؤكد فقط، مع compatibility wallet key ثابت | لا تحويل تلقائي لملف مجهول؛ لا إثبات هوية سحابية أو اعتماد جميع بيانات المصدر العامة كملكية شخصية |
| P0-03 الزائر والعزل | CODE-COMPLETE | Guest UUID مستقل لكل مرحلة، singleton للمرحلة نفسها على الجهاز؛ تقدم ومحفوظات وسجل ومحاولات منفصلة عن الطلاب وباقي المراحل؛ لا محفظة أو pending grants للزائر؛ المكتبة والمحـفوظات المحلية متاحتان دون gate دخول للملف المحلي | Guest القديم غير المرحلي يبقى محفوظًا للمراجعة؛ لا claim/merge تلقائي أو تصدير/استعادة عبر الأجهزة |
| P0-04 اتساق الجواهر | CODE-COMPLETE | إصلاحات ledger-only وD05 محفوظة؛ Adapter يربط UUID المؤكد بمفتاح SQLite القديم دون rekey/reset. D06 يمنع أي grant WEEKLY جديد، ويبقي التاريخ كما هو. المصادر الجديدة في المسار المحلي تمر عبر receipt/outbox محفوظين ثم ledger dedupe | لا تسوية أرصدة تاريخية أو اعتماد نهائي لقيم caps. SQLiteOpenHelper الفعلي يحتاج هاتفًا معزولًا |
| P0-05 مدخل محلي صادق | CODE-COMPLETE | شاشة اختيار مرحلة/ملف محلي وإنشاء طالب أو زائر، دون كلمة مرور أو وعد Auth/Recovery سحابي؛ ملفات قديمة confirmed/unresolved ظاهرة بوضوح. Demo يبقى تطويريًا ومعلمًا خارج رحلة الطالب المحلية؛ لا طالب عادي بلا اختيار مرحلة | لا مزود Auth أو tokens أو استعادة حساب حقيقي. Teacher code لم يتغير |
| P0-06 ملكية الأوامر والاستئناف | CODE-COMPLETE | completion/practice/assignment + command receipt + pending reward ضمن snapshot واحدة لكل UUID؛ owner قبل الكتابة؛ ack بعد معاملة ledger؛ replay بعد Restart/Retry بلا mint مكرر؛ فتح الدرس والمحـفوظات وسجل الحل والـcallbacks القديمة محروسة. تنظيف cache التنقل عند تبديل الملف دون إعادة تصميمه | لا قبول Back/rotation/deep links فعليًا من JVM، ولا content revision pin/rubric/outbox سحابي عام من W2/DATA-01 |

CODE-COMPLETE هنا تعني تنفيذ سلوك W1 المحدد ونجاح اختبارات المضيف المناسبة، مع إبقاء الحالات غير القابلة للإثبات محفوظة وفق قرار المالك. لا تعني إنجاز كل مزايا PRD أو استكمال المحتوى أو اجتياز قبول الإصدار.

## القرارات المطبقة وحدودها

### D01 — Local First

- هوية محلية مستقلة عن جهة اتصال أو مستقبل AccountLink. UUID هو مفتاح التعلم، ولا يصبح البريد هو owner لملف جديد.
- شاشة الدخول المحلي تصرح أن الحفظ على هذا الجهاز فقط وأن تسجيل الدخول والاستعادة السحابيين غير متصلين. لا كلمة مرور محلية أو وعد بحساب موثق.
- AuthViewModel التجريبي السابق باقٍ خلف debuggable gate؛ طالب بلا localId لا يدخل رحلة التعلم العادية. كود المعلم ودخوله التطويري محفوظان.
- future Auth يمكنه الربط بملف UUID ثابت دون تغيير مرحلة الملف أو إعادة تسمية Ledger؛ لم يُنشأ provider أو Backend أو sync في هذه الجولة.

### D03 — Student Identity / Stage / Legacy

- `LocalLearnerStore` مخزن إضافي، registry versioned في preferences مستقلة، وsnapshot مستقلة لكل UUID. المرحلة جزء من هوية الملف ولا يوجد setter لتعديلها.
- لا انتقال مرحلة لحساب قائم، ولا دمج بين ملفي G9/BAC أو Guest/Student، حتى عند تشابه الاسم. الطالب الجديد يملك wallet key يساوي UUID؛ الزائر لا يملك مفتاح جواهر.
- المحتوى المرفق الحالي BAC؛ ملف G9 لا يرى BAC عبر lesson/topic/book/practice deep links، وتظهر حالة عدم توفر محتوى المرحلة بدل fallback. لم يُخترع منهج G9 أو يبدأ إنتاج W2.
- استيراد Legacy يتطلب scope مسجلًا في `student_snapshot_files`، و`record_owner` صريحًا متسقًا مع اسم مخزنه، وموافقة محلية واختيار مرحلة صريحين. هذه قرائن ملكية ملف محلي، وليست تحققًا سحابيًا من شخص.
- `enmath_demo` العام، وGuest القديم غير المرحلي، والـaliases الناقصة/المتناقضة/غير المفهرسة لا تُنسب إلى UUID جديد. الملفات المجهولة أو التالفة تبقى في مكانها للمراجعة؛ لا deletion أو reset أو claim بالاسم/البريد المتشابه.
- Journal: PREPARED يسجل المصدر وبصمته وUUID والمرحلة المختارين؛ تُنسخ البيانات الشخصية إلى snapshot مؤكدة ثم DONE وينشر الملف. Restart بعد PREPARED أو بعد copy يستأنف UUID نفسه ولا يعيد العد أو النقل. مصدر تغيّر أثناء الترحيل يتحول إلى SUSPENDED؛ target مختلف يُحفظ للمراجعة بدل الكتابة فوقه.
- بصمة المصدر ومطابقة اسم preferences القديم تستخدمان SHA-256 للتكامل والتوافق، وليستا ادعاءً بإخفاء هوية البريد. لا hash جديد لمفتاح محفظة Legacy ولا تغيير لقيمته.
- يبقى `legacyWalletKey` في metadata المحلية المؤكدة فقط. `LocalGemsPolicy.owner` يعمل كـCompatibility Adapter إلى مفتاح Ledger الأصلي؛ `commandOwner` يظل UUID. لا تحويل أرصدة ولا جمع محافظ.
- المحاولات والتقدم والمحـفوظات والسجل والـreceipts الشخصية المؤكدة تُحفظ في الملف المنقول. صفوف الفصول/الواجبات/التسليمات/الموارد والإشعارات المشتركة التي لا تملك provenance خاصة بالـUUID لا تُنسخ كملكية شخصية؛ أصلها محفوظ دون تعديل للمراجعة. لا ندعي أن هذه الصفوف العامة أصبحت بيانات شخصية مؤكدة أو متاحة كلها في الملف الجديد.
- لا يوجد Planner إنتاجي أو بيانات خطط منفذة في المصدر الحالي؛ لم أضف مخطط W2 ولم أدّع اختبار عزل كيان غير موجود. مخزن الملف وواجهة الأوامر مقيدان بالمالك لبيانات التعلم المحلية الموجودة، وأي كيان خطط لاحق يجب أن يستعمل المالك نفسه.

### D06 — Reward Sources

- لا grant جديد لـWEEKLY من Policy أو Weekly Review؛ المراجعة التعليمية باقية دون وعد جواهر. سجلات WEEKLY القديمة والرصيد الناشئ عنها تبقى قابلة للقراءة والصرف دون خصم أو إعادة كتابة.
- المصادر الجديدة المفعلة في رحلة الطالب المحلية: درس مكتمل بتحقق تعليمي، وتدريب أو واجب مكتمل الإجابات بنتيجة 80% فأكثر. لا Guest أو manual grants.
- MASTERY بقيت كنوع تاريخي مقروء، لكن لا تُصدر grant جديدة خارج قائمة المصادر المعتمدة لهذا التكليف. لا تعديل للأدلة أو درجات الإتقان ضمن W2.
- قيم التنفيذ السابقة LESSON=10 وPRACTICE/ASSIGNMENT=5 وحدود 40 يوميًا و120 أسبوعيًا لم تتغير. هذه قيم توافق موروثة، وليست اعتمادًا نهائيًا جديدًا للحدود. المكافأة اليومية المتتابعة غير معتمدة ولم تُخترع لها قيمة أو مصدر؛ streak يبقى إحصائيًا فقط.
- ترتيب التحقق والإكمال لا يضيع الاستحقاق الجديد: آخر حقيقة مؤهلة تُكتب مع receipt وpending event في snapshot نفسها. إكمال Legacy المستورد لا يتحول بأثر رجعي إلى grant جديدة بمجرد إعادة فتحه أو الضغط على مكتمل.

## الحفظ والـReplay وحالات الفشل

- snapshot الجديدة تحمل owner UUID وschema version، وcompleted/last/product/verified evidence/command receipts/pending grants ضمن commit واحدة. نسخ snapshot ملف إلى ملف آخر يرفضها فحص owner حتى دون pending events.
- لا تظهر حالة تعلم جديدة في StateFlow قبل إقرار الحفظ. إذا أعاد commit false يُمنع استعمال المخزن في العملية نفسها إلى إعادة التشغيل، لأن ذاكرة SharedPreferences قد تختلف عن القرص؛ لا ندعي rollback مضمونًا للذاكرة.
- Ledger ما زال SQLite الحالي ومعاملاته الحالية. التسليم at-least-once مع مفاتيح achievement السابقة: الفشل قبل ledger commit يبقي الحدث، والفشل أو crash بعد المنحة وقبل ack يعيد التسليم بلا منحة ثانية.
- baseline exclusions لا تستهلك أحداث snapshot المعلقة الجديدة، ولا تُحوّل بيانات Legacy القديمة إلى منح. لا عملة قبل دليل محفوظ ومملوك للملف.
- ack لملف A لا يكتب إلى ملف B الحالي. عمليات snapshot وack في المستودع متزامنة لمنع فقد تحديث التعلم عند وصول ack من IO.
- أوامر completion/practice/assignment تعاد بعد فشل الحفظ؛ لا تعتمد الحماية على finished flag في ViewModel وحده. flags الواجهة تتحرر عند الرفض أو الفشل.
- المستودع محفوظ بعمر ViewModel كي لا تنشأ نسخة مستقلة غير المرتبطة بـAppViewModel عند rotation. بيانات الـSolver وحالة التبويبات القديمة تُصفّر عند تبديل الملف؛ rotation لنفس الملف لا يفرض العودة إلى Home.
- اللغة والثيم مشتركان كتفضيلات جهاز، ويُحفظان دون إعادة كتابة product_v1 القديم. البيانات الشخصية تبقى داخل ملف UUID فقط.

## الملفات المعدلة في هذه الجولة

القياس مقابل SHA-256 عند بداية التكليف، وليس كامل git diff مع HEAD الذي يتضمن تعديلات محلية أقدم.

- `app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsPolicy.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalLearningGems.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/model/Models.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/model/ProductModels.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLessonViewModel.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt`
- `app/src/test/java/com/ahmadmol/enmath/AssignmentSubmissionVerificationTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/LocalGemsCoordinatorTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/LocalGemsPolicyTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/W1IdentityIsolationTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/W1ProductRecoveryTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/WeeklyGemCardPresentationTest.kt`
- `tools/test_local_gems_sqlite.py`

الملفات الجديدة:

- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalLearnerStore.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/auth/LocalIdentityScreen.kt`
- `app/src/test/java/com/ahmadmol/enmath/W1CompletionIdentityTest.kt`
- `docs/IMPLEMENTATION/W1_FINAL_COMPLETION_REPORT.md` — هذا التقرير الوحيد الجديد ضمن docs.

## نتائج التحقق

```powershell
./gradlew.bat testDebugUnitTest assembleDebug assembleRelease lintDebug lintRelease --console=plain
python tools/test_local_gems_sqlite.py
```

- **JVM: 181 tests، صفر failures/errors/skipped**، وفق XML تحت `app/build/test-results/testDebugUnitTest/`؛ 24 اختبارًا جديدًا مقارنة بـ157 عند بداية الجولة.
- **Host SQLite: 10/10 PASS** على tempfile، دون قاعدة مستخدم. الاختبار الجديد يثبت بقاء WEEKLY تاريخية ورصيدها بعد إعادة فتح قاعدة fixture.
- **Debug Build: PASS. Release Build: PASS**؛ Release APK unsigned فقط. لم يُثبت أو يُشغّل التطبيق على هاتف أو محاكي.
- **Lint Debug: صفر errors و14 warnings. Lint Release: صفر errors و15 warnings.** التحذيرات تشمل إعدادات/اعتماديات/موارد سابقة وUseKtx للأسلوب الصريح الذي يفحص commit؛ لا baseline أو suppression لإخفائها.
- اختبارات الهوية الجديدة تغطي UUID ومرحلة ثابتة، Guest بكل مرحلة، تبديل الملف، رفض مالك قديم، snapshot من مالك آخر، corrupt source/profile، ترحيل مؤكد، marker متناقض، فشل journal، Restart بعد prepare/copy، source change→suspension، receipt/outbox، ترتيب التحقق/الإكمال، عدم منح Legacy بأثر رجعي، واجب مكرر وتفضيلات الجهاز.
- اختبارات Coordinator الجديدة تغطي فشل ledger، grant→ack failure→restart، مفتاح Legacy خلف UUID مع بقاء المنح التاريخية، ورفض Guest/foreign-owner events. اختبارات السياسة القديمة حدثت لتعبّر عن D06 بدل توقع مكافأة أسبوعية جديدة؛ اختبارات caps تستعمل مصادر معتمدة، وتغطية التاريخ محفوظة.
- fixture حقن فشل التسليم أصبح يعترض primitive الأوامر الجديدة صراحةً؛ لم تُحذف اختبارات فشل قبل/بعد publication أو يُخفَ قصور adapters القديمة في الذاكرة.
- تحقق source/footprints: 242 ملفًا في baseline، تغير 23 ملفًا قائمًا وأضيفت 3 ملفات code/test وهذا التقرير؛ لا حذف. بقيت ملفات المعلم وcore/design وBottom Navigation وRoutes وManifest وinstrumentation والوثائق السابقة مطابقة لبصمات البداية، بما فيها تعديلات المستخدم السابقة. كل TestTag قديم في الملفات المعدلة باقٍ.
- فحص الأنماط للمحتوى المعدل والجديد لم يرصد أسرارًا أو بريدًا غير محجوز للاختبارات. لا بيانات حقيقية ضمن fixtures أو التقرير.
- `git diff --check` للنطاق البرمجي المعدل ناجح؛ لا Commit/Push أو staging جديد، وHEAD بقي `01782ed`.

## أدلة المضيف وحدودها

- تقارير JVM وLint تحت `app/build/reports/` وXML تحت `app/build/test-results/` مخرجات ignored، وليست وثائق مكررة أو ملفات معدة للنشر.
- اختبارات preferences تستخدم واجهة SharedPreferences مع ملفات fixture مؤقتة وإعادة إنشاء فعلية من القرص، ولا تشغّل Android SharedPreferences نفسه. اختبارات SQLite المضيفة تستعمل schema الفعلية، لكنها لا تشغّل Android SQLiteOpenHelper أو Activity lifecycle.
- Unknown ownership ليس تصريحًا بإسناد البيانات؛ يبقى مصدرها محفوظًا. البيانات التي تعذرت نسبة ملكيتها تحتاج مراجعة بشرية، لا قرارًا افتراضيًا أو مسحًا.

## قبول الهاتف المختصر — يدوي ومعزول فقط

استعمل Android حقيقيًا API 24 فأعلى. الحزمة المخصصة للاختبار **`com.ahmadmol.enmath.w1qa`**، واسمها الظاهر **EN.MATH W1 QA**؛ تختلف عن `com.ahmadmol.enmath` وتحفظ preferences وSQLite داخل sandbox أخرى. لا تختبر على APK العادي، ولا تلغِ تثبيت التطبيق الموجود أو تمسح بياناته.

ملفا QA محفوظان محليًا فقط تحت `dist/w1-phone-20261026-113816`، ولا يدخلان Git:

- `ENMATH-W1-QA-BASELINE-v1.apk`: من شيفرة HEAD السابق `01782ed`، مع إعداد QA المعزول فقط، versionCode=1.
- `ENMATH-W1-QA-FINAL-v2.apk`: نسخة W1-FINAL، versionCode=2، Application ID والتوقيع نفسيهما، لتحديث QA دون uninstall.
- `APK_INFO.json`: package/versionCode/SHA-256 وبصمة شهادة التوقيع لكلا الملفين. تحققت الأدوات المضيفة من الهوية والاسم والتوقيع؛ لم يحدث تثبيت.

إن كانت حزمة w1qa موجودة ولها بيانات تريد الاحتفاظ بها، لا تستبدلها أو تمسحها؛ استعمل مستخدم Android اختباريًا مستقلًا أو اطلب build بمعرف منفصل آخر. لا تنسخ بيانات التطبيق الأصلي إلى QA.

لتجهيز Legacy اصطناعية: ثبّت Baseline يدويًا، وأنشئ في مسار Demo المحلي حسابًا مثل `qa-legacy@example.test` وكلمة مرور اختبارية فقط. سجّل درسًا/تدريبًا مؤهلًا ورصيد المحفظة، وشراءً تجريبيًا إن توفر رصيد كافٍ، ثم جرّب Guest قديمًا واحفظ نشاطه. دوّن الرصيد والمقتنيات، ثم حدّث **QA فقط** بملف Final v2. لا يستعيد Final أي حساب سحابي. الخيار `-Pw1QaVersionCode=1` وحده لا يحوّل الشيفرة الحالية إلى Baseline؛ الملف المرفق بُني من `01782ed` قبل نسخ W1-FINAL.

سجّل لكل صف PASS/FAIL/BLOCKED مع طراز الهاتف وإصدار Android والتاريخ؛ لقطة دون بيانات شخصية عند الحاجة:

| الاختبار | النتيجة المطلوبة |
|---|---|
| إنشاء BAC وG9 | ملفان مستقلان ومرحلة كل منهما ثابتة؛ G9 لا يفتح محتوى BAC غير التابع له |
| Guest لكل مرحلة | UUID/تقدم/محفوظات مستقلة لكل مرحلة، ولا محفظة أو Gems؛ العودة إلى Guest نفسه تحفظ بياناته |
| التبديل أثناء درس/تدريب/حل | لا إجابات أو سجل أو محفوظات أو تقدم أو Wallet من المالك السابق؛ retry قديم لا يكتب للملف الآخر |
| إغلاق وإعادة فتح | التقدم والمرحلة والمحـفوظات والمقتنيات محفوظة؛ لا seed/reset |
| ترقية Legacy الاصطناعية | اختيار مرحلة صريح للملف المؤكد؛ الرصيد والمشتريات القديمة نفسها، والملكية الملتبسة محفوظة ومعلّقة دون نقل أو merge |
| الجواهر وRetry/Restart | درس مع تحقق وتدريب/واجب 80% فأكثر حسب المحتوى المتاح؛ لا grant مكرر، لا Weekly جديد ولا Guest/manual grants؛ التاريخ يبقى |
| Light/Dark وRTL وBack وRotation | لا قص أو overlap أو تسرب حالة حساب؛ rotation لنفس الملف لا يمسح الإدخال أو يعيد الوجهة قسرًا |
| فشل التخزين/البيانات الملتبسة | لا نجاح غير محفوظ أو حذف أو Reset؛ رسالة واضحة وملف مستقل آمن عند إمكانه؛ مصدر غير مثبت لا يُنسب تلقائيًا |

فشل التخزين/الفساد والقتل عند prepare/copy/ledger/ack يحتاج fixtures اصطناعية مهيأة داخل sandbox QA أو مختبرًا مأذونًا. إن لم تتوفر وسيلة حقن آمنة، سجّل هذه الحالات **BLOCKED**؛ لا تملأ تخزين هاتف المستخدم أو تعدل قواعده أو ساعته لمحاكاة الفشل، ولا تدّعِ أن إغلاقًا عاديًا اختبر نقطة crash محددة. اختبارات المضيف لا تحل محل هذه الحالات على Android.

لا Emulator أو ADB أو Instrumentation أو تثبيت تلقائي في هذا التكليف. تبقى W1 **CODE-COMPLETE / PHONE ACCEPTANCE PENDING** حتى إتمام القائمة وحالات الفشل المطلوبة بأدلة على جهاز حقيقي.

## الخلاصة

**W1 CODE-COMPLETE محليًا ضمن القرارات المعتمدة، وDEVICE/RELEASE ACCEPTANCE PENDING.** لا ادعاء بقبول نهائي أو اكتمال مناهج G9/BAC أو Planner أو Auth/Sync سحابي. D01/D03 مطبقتان في الأساس المحلي، وD06 مطبق لمصادر المنح الجديدة مع حفظ التاريخ؛ تبقى أرقام السياسة النهائية ومكافأة streak غير محسومة كما طلب المالك. لم يبدأ W2 أو DATA-01 الشامل، ولم يحدث Commit أو Push.

## تأهيل النشر النهائي — 2026-10-26

ما سبق يسجل تنفيذ W1 قبل Commit/Push؛ جولة النشر اللاحقة أُعدت من HEAD البعيد `01782ed` داخل worktree مستقل، ونُقلت 27 مسارًا من W1-FINAL فقط، مع ملف Gradle وmanifest خاصين بعزل QA. التعديلات المحلية الأقدم في Teacher/launcher/Manifest/instrumentation والوثائق خارج النطاق لم تُنسخ.

- على نسخة النشر نفسها: **181 JVM tests / 10 Host SQLite PASS**؛ assembleDebug وassembleRelease وassembleW1Qa ناجحة؛ lintDebug وlintRelease وlintW1Qa جميعها **صفر أخطاء و13 تحذيرًا**. فرق عدد التحذيرات عن تشغيل التنفيذ السابق يعود إلى استبعاد موارد/Manifest محلية غير مرتبطة، وليس تعطيل Lint أو إخفاء تحذير.
- QA Baseline v1 وFinal v2 مفصولان عن التطبيق الأصلي بـApplication ID؛ ثبت تطابق توقيعهما وزيادة versionCode بأدوات APK المضيفة. لا APK أو Build أو قاعدة بيانات أو مفاتيح توقيع ضمن مسارات النشر.
- فحص كامل للملفات المرشحة لم يرصد أنماط أسرار أو PII صريحة أو مفتاح Legacy حساس مضمّنًا كقيمة حقيقية. معرفات Legacy تبقى metadata محلية وقت التشغيل، ولا تنشر بيانات المستخدم معها؛ فحص الأنماط ليس ضمانًا مطلقًا.
- أربع مجموعات Commit منطقية: الهوية وJournal؛ ملكية التعلم وتسليم الجواهر؛ QA المعزول؛ والتقرير وقائمة قبول الهاتف. إثبات Push وSHA النهائية في Git والرد النهائي دون self-hash داخل الوثيقة.
- لا تشغيل Emulator/ADB/Instrumentation أو تثبيت تلقائي. **CODE-COMPLETE وليست W1 ACCEPTED**؛ يلزم قبول الهاتف الفعلي، بما فيه fixtures فشل آمنة حيث تتطلبها القائمة.
