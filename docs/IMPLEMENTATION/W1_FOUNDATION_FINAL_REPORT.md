# W1 Foundation — Final Implementation Report

2026-10-25 — تنفيذ تسلسلي A → اختبارات A → B → اختبارات B → C → اختبارات C، ثم تحقق شامل. النتيجة **PARTIAL / NO-GO لقبول W1 كاملة**. تكليف W1 معتمد في رسالة المالك، لكنه لا يغلق D01–D16 تلقائيًا. لم تبدأ DATA-01 أو أي مرحلة لاحقة.

هذا تقرير تنفيذ واحد، وليس إعادة كتابة للـPRD أو Backlog. المراجع المقروءة: Execution Backlog، Student Final PRD Draft، Open Decisions، Plan-to-Code Gap Report، Student Release Acceptance Matrix.

## الحزمة A — P0-01 — PARTIAL

**المنفذ:**

- فصل المخزن الغائب (Seed للتثبيت الجديد) عن JSON موجود غير قابل للقراءة: `CorruptLocalData` بدل fallback إلى Seed. رفض الإصدار غير المدعوم، المصفوفات الناقصة أو الخاطئة والعناصر الجزئية والحقول المركبة غير الصالحة.
- كل حفظ يعيد فحص المخزن قبل الكتابة. المستودع يفحص المخزنين العام والشخصي قبل أول كتابة؛ فساد أحدهما يمنع تحديثًا شخصيًا جديدًا. الأصل التالف لا يُستبدل ولا يُحذف.
- حفظ أول نسخة JSON سابقة سليمة في مفتاح إضافي `product_v1_recovery` قبل أول تحديث، وعدم استبدال تلك النسخة لاحقًا. لا استعادة تلقائية قد تخفي التلف.
- استخدام `commit()` والتحقق من النتيجة بدل إعلان نجاح بعد `apply()` في مسار تحديث المستودع. لا نشر لحالة التعلم الجديدة قبل اكتمال إقرارات الحفظ؛ فشل الإقرار يرفع `LocalDataWriteFailed`.
- عند تعذّر إنشاء المستودع تظهر رسالة عربية/إنكليزية لحفظ الأصل وطلب الاستعادة دون مسح بيانات التطبيق؛ المسارات العادية والتصميم والتنقل لم يعاد بناؤها.

**الملفات:**

- `app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt`
- `app/build.gradle.kts` — مكتبة JSON لاختبارات JVM فقط، دون تغيير dependency الإنتاج.
- `app/src/test/java/com/ahmadmol/enmath/W1ProductRecoveryTest.kt`

**الاختبارات:** خمسة اختبارات JVM على ملفات مؤقتة مستقلة: absent/valid/restart، malformed/truncated/unknown-version، partial arrays، ثبات نسخة الاستعادة والاستعادة الصريحة داخل fixture، وفشل إقرار الكتابة مع بقاء الملف السابق. نجحت قبل بدء B. أضيف سادس في التحقق النهائي يثبت أن فساد المخزن العام بعد فتحه يمنع الكتابة الشخصية ويحفظ بايتات ملفها والحالة السابقة. فشل التشغيل الأول في مقارنة Seed المتغيرة بتوقيت الإنشاء؛ صُحح الاختبار لمقارنة الحقول المستقرة ثم نجحت الإعادة.

**المتبقي:** fixture الملفات يحاكي واجهة SharedPreferences ولا يشغّل تنفيذ Android الفعلي. فشل كتابة أثناء جلسة مفتوحة يمنع النجاح باستثناء؛ واجهة استعادة كاملة لهذه الحالة وإصلاح/تصدير مدروس غير منجزين. `commit=false` في Android قد يترك ذاكرة SharedPreferences مختلفة عن القرص؛ لا ندعي rollback مضمونًا. لا توجد معاملة ذرية بين ملفات prefs أو التعلم وSQLite. لا اختبار device/process-kill حقيقي ضمن هذا التكليف. لذلك لا تُعد QA-27/28/31 مقبولة بالكامل.

## الحزمة B — P0-02 / P0-03 / P0-06 — PARTIAL

**المنفذ:**

- UUID محلي دائم **للزائر الجديد فقط**، محفوظ إضافيًا في `w1_guest_uuid`، ومخزن شخصي منفصل له. تبقى إنجازاته ومحفوظاته بعد إعادة إنشاء المستودع، والخروج ودخول حساب آخر لا ينقلانها. لا Gems للزائر.
- إيقاف إنشاء `personal_legacy_owner` باستنتاج المستخدم الحالي أو الافتراض `ahmed`. إيقاف استيراد التقدم العام تلقائيًا، حتى عند وجود marker قديم قد يكون ناتجًا عن الاستنتاج السابق. تبقى JSON ومفاتيح completed القديمة في مكانها للاستعادة المعتمدة؛ لا دمج ولا حذف.
- الإبقاء على snapshots الشخصية الموجودة ومعرفات الفصل والـaliases المسجلة دون إعادة مفاتيح الطلاب أو الجواهر. الحساب ذو بريد مختلف لا يرث بيانات اسم مشابه، وتغيير وسيلة الاتصال لا يطالب تلقائيًا بملكية البيانات السابقة.
- فحص مالك الجلسة **قبل** كتابة completion/activity/practice/assignment، بما فيه رفض provenance مفقودة قادمة من واجهة مستعادة. `OwnerChanged` نتيجة صريحة للتسليم. واجهات الدرس والتدريب تلتقط مالك البداية، مع استعمال UUID للزائر ومفتاح الطالب الحالي مؤقتًا.
- `commandId` محفوظ في Quiz SavedState، وإيصال `learningCommands` إضافي اختياري في JSON v1. المحاولة والنشاط والإيصال تُكتب داخل ProductState نفسها؛ إعادة إرسال أمر التدريب بعد إعادة الفتح لا تضيف محاولة أخرى. السجلات القديمة التي لا تحمل الحقل الجديد تُقرأ دون حذف.
- مدخل تخزين قابل للحقن لاختبار المستودع الفعلي على ملفات fixtures، دون Context مستخدم أو قاعدة تطبيق حقيقية.

**الملفات:**

- `app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/model/ProductModels.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt` — مصدر provenance فقط، دون تغيير routes أو TestTags.
- `app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt`
- `app/src/test/java/com/ahmadmol/enmath/W1IdentityIsolationTest.kt`

**الاختبارات:** ستة اختبارات جديدة: UUID/guest restart/bookmark/account switch، نفس الاسم وبريد مختلف وتغيير البريد، Legacy مجهولة محفوظة دون تبنٍّ، رفض مالك قديم/مفقود قبل كل مسارات التعلم المختبرة، زائر بلا Gems وخروج، وإعادة أمر تدريب بعد restart. نجحت مع اختبارات A وAssignmentSubmissionVerification وInteractiveLessonSession وStudentVideoRegression قبل بدء C. أضيف سابع أثناء المراجعة النهائية لإغلاق fallback من metadata شخصية ناقصة إلى completed/last عامين، ونجح ضمن المجموعة الكاملة.

**المحجوب والمخاطر:**

- **P0-02 BLOCKED بواسطة D03:** لم تُفعّل UUID للطلاب أو stage immutable أو mapping/journal لترحيل الطلاب والـledger. لا نستنتج مرحلة من Demo، ولا نغير مالك محفظة أو نفتح محفظة UUID فارغة بدل القديمة. اختيار المرحلة وUX استعادة الملكية والموافقة على aliases تحتاج المالك.
- UUID الزائر الحالي مساحة واحدة غير مرحلية؛ تقسيم زائر تاسع/بكالوريا واختيار المرحلة وقفلها لم ينفذ. بعض حواجز Guest في واجهات المحفوظات والمكتبة باقية؛ إثبات التخزين لا يعني اكتمال رحلة الوصول العامة.
- قد يظهر التقدم غير المنسوب فارغًا في المساحة الجديدة؛ الأصل محفوظ خارجها وليس محذوفًا. يلزم استرجاع بملكية مؤكدة بدل نسبته تلقائيًا. الحسابات القديمة المؤكدة بـsnapshots تبقى على مفاتيحها الحالية.
- حراسة الملكية تعتمد مؤقتًا بريد الطالب الحالي؛ لا تحقق سحابي أو alias لوسيلة اتصال متغيرة. لا ضمان شامل لتزامن threads أو عزل كل presentation state بمجرد هذه الحراسة.
- receipts الحالية للتدريب، وليست transactional outbox عامًا. إكمال الدرس والنشاط والتعلم والجواهر تبقى عمليات منفصلة؛ crash قد يترك حدث جواهر غير مسلّم. لا ندعي حل QA-50 ولا DATA-01، ولا idempotency شاملًا لكل أمر واجب.

## الحزمة C — P0-04 / P0-05 — PARTIAL

**المنفذ:**

- الشراء في Coordinator يستعمل `LocalGemsPolicy.unlock` نفسها لجميع الملاك. الرصيد المؤهل للصرف هو رصيد ledger الفعلي؛ الخصم يساوي السعر المسجل أو تعاد `INSUFFICIENT_GEMS`. استثناء العرض الخاص لا يجيز شراء دون رصيد مكتسب.
- لا تعديل لجدول/رصيد/منحة/شراء تاريخي. لا reset، لا تعويض، ولا تسوية bonus. تبقى حماية SQLite الحالية والتحقق من invariants والتراجع المعاملي.
- صندوق Home لا يضيف حالة claimed وهمية أو ينشئ مكافأة غير محفوظة؛ يفتح المحفظة الحالية للاستعلام بدل الادعاء بنجاح. لا مصدر مكافأة جديد ولا تغيير weekly.
- المصادقة التجريبية (validation/123456/recovery/new password) معطّلة افتراضيًا ولا تُفعّل إلا صراحة في تطبيق debuggable. الإنتاج لا يستعيد جلسة Demo طالب/معلم ولا يقبل إنشاءها، مع حفظ ملفاتها وإتاحة الزائر المحلي. لا Backend أو tokens أو ادعاء تسجيل دخول سحابي.
- نص الدخول ورسالة الرفض يوضحان حدود المحلي والإنتاج. كود المعلم ومساراته محفوظان؛ تجربة Demo متاحة في Debug، وليست مصادقة إنتاجية.

**الملفات:**

- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/auth/AuthViewModel.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/auth/AuthScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/auth/CleanLoginScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt`
- `app/src/test/java/com/ahmadmol/enmath/LocalGemsCoordinatorTest.kt`
- `app/src/test/java/com/ahmadmol/enmath/W1AuthBoundaryTest.kt`

**الاختبارات:** Coordinator/Policy/WeeklyGemCard/Auth boundary نجحت. مصفوفة شراء جديدة من 20 حالة داخل اختبار واحد: مالكان (عادي/خاص) × أرصدة 0/39/40/119/120 × سعر 40/120، مع رفض آمن أو تطابق الخصم والسعر، ثبات grants، وعدم تكرار الشراء. اختبارات الإنتاج ترفض Demo واستعادته وتحفظ بياناته وتسمح بالزائر. اختبارات SQLite المضيفة الثمانية نجحت على قواعد tempfile، بما فيها rollback والتزامن وعزل المالك وإعادة الفتح؛ لا تمثل Android SQLiteOpenHelper فعليًا.

**المحجوب:** عند إنجاز W1 الأولي كان استثناء العرض الخاص محجوبًا بقرار D05. لاحقًا اعتمد المالك إزالة العرض غير المدعوم بالـLedger وإزالة المعرف الشخصي المحتمل من مرشحي النشر؛ نُفّذ ذلك دون تغيير سجلات SQLite أو مفاتيحها. هذا يغلق استثناء العرض فقط، ولا يعتمد أي تسوية تاريخية أخرى. D06 يحجب اعتماد السياسة النهائية ومصدر WEEKLY+15 الموجود؛ لم تتغير السياسة أو أسعار المتجر. D01 يحجب اعتماد المنتج محليًا أم سحابيًا وصياغة دخوله النهائية؛ فصل Demo أمان تقني وليس اعتمادًا لإطلاق guest-only. لا auth حقيقي أو cloud recovery.

## نتائج التحقق الشامل

الأوامر باستخدام JBR 21 المثبت محليًا، دون تعديل إعدادات Java الدائمة:

```powershell
./gradlew.bat testDebugUnitTest --tests '*W1ProductRecoveryTest' --console=plain
./gradlew.bat testDebugUnitTest --tests '*W1IdentityIsolationTest' --tests '*W1ProductRecoveryTest' --tests '*AssignmentSubmissionVerificationTest' --tests '*InteractiveLessonSessionTest' --tests '*StudentVideoRegressionTest' --console=plain
./gradlew.bat testDebugUnitTest --tests '*LocalGemsCoordinatorTest' --tests '*LocalGemsPolicyTest' --tests '*W1AuthBoundaryTest' --tests '*WeeklyGemCardPresentationTest' --console=plain
python tools/test_local_gems_sqlite.py
./gradlew.bat testDebugUnitTest assembleDebug assembleRelease lintDebug lintRelease --console=plain
```

- المجموعة النهائية: **156 JVM tests، صفر failures/errors/skipped**، وفق XML تحت `app/build/test-results/testDebugUnitTest/`. تضم 17 اختبارًا جديدًا مقارنة بـ139 في baseline، منها مصفوفة الشراء متعددة الحالات داخل اختبار واحد.
- **8/8 Host SQLite PASS**، باستخدام Python المرفق بالبيئة ومسار tempfile الخاص بالاختبار فقط.
- **assembleDebug / assembleRelease / lintDebug / lintRelease: PASS** بعد التصحيح النهائي والاختبارين الإضافيين؛ الأمر الشامل انتهى `BUILD SUCCESSFUL` في 1m 52s. Debug APK مبني، وRelease APK **unsigned** فقط؛ لا توقيع نشر أو تثبيت أو توزيع.
- **Lint: صفر errors، 12 warnings لـDebug و13 لـRelease**. تشمل dependency updates، target SDK، backup rules، Compose boxing/modifier، KTX، unused resource، وmonochrome launcher في Release؛ لم تُخفَ بتحويلها إلى baseline أو تعطيل lint. توجد أيضًا تحذيرات deprecated icons أثناء compilation وتحذير SDK XML/tooling؛ لا تُعد أخطاء بناء.
- الأدلة: `app/build/reports/tests/testDebugUnitTest/index.html`، `app/build/reports/lint-results-debug.html`، `app/build/reports/lint-results-release.html`، وXML المقابلة. هذه مخرجات بناء محلية وليست ملفات تقرير مكررة ضمن docs.
- لا instrumentation أو اختبار process death/Android SharedPreferences/Android SQLiteOpenHelper/واجهة مرئية فعلي. ملفات XML وHTML هي أدلة host build/test وليست قبول هاتف.
- فحص `git diff --check` للنطاق المحدد لملفات الأساس نجح. فحص Working Tree الكاملة ما زال يبلغ blank line at EOF السابقة في AuthScreens/CleanLoginScreen/OnboardingScreens؛ لم أعد تنسيق الملفات السابقة لإخفاءها.

## الحماية وحدود القبول

- كان Working Tree يحتوي تعديلات كثيرة قبل التنفيذ. قورنت SHA-256 لعدد 237 ملفًا ببيان محفوظ خارج المستودع في بداية المهمة لتحديد إضافات هذه الجولة، وليس بـHEAD وحده: 15 ملفًا قائمًا تغيرت، و222 بقيت مطابقة، وثلاثة ملفات اختبارات وهذا التقرير أضيفت؛ لا ملف محذوف. لم تُستبدل التعديلات السابقة.
- لم يُعدل أي ملف Teacher أو مورد تصميم أو Manifest أو instrumentation أو وثيقة مرجعية في هذه الجولة. تغييرات ملفات التنقل محصورة في provenance ورسالة تعذر التخزين وربط قدرة Demo، دون تغيير routes أو TestTags أو الرسم الحالي.
- لا Emulator، لا ADB، لا هاتف، لا تشغيل instrumentation، لا وصول لقواعد مستخدم، لا حذف بيانات أو Reset Gems أو destructive migration، لا Commit/Push. الاختبارات على fixtures وقواعد مؤقتة فقط.
- **NO-GO لقبول W1 كاملة:** إغلاق D03 وتنفيذ هوية الطالب والمرحلة والـmigration المعتمدة، وإغلاق D01/D06 وبقية نطاق D05 بحسب نطاق الإصدار، واكتمال الاستعادة/Guest UX واختبارات Android المعزولة وقبول الهاتف لاحقًا وفق D16، مطلوبة قبل القبول. نجاح البناء لا يغلق هذه البنود.

## قرار المالك اللاحق — D05 Privacy & Legacy Compatibility

2026-10-25: صُنّف المعرف السابق Potential PII / Legacy Wallet Identifier، ولم يُعتمد نشره صريحًا. اعتمد المالك إزالة استثناء العرض ‎+1000، وإزالة القيمة من الشيفرة والاختبارات والوثائق المرشحة للنشر، مع استعمال معرفات example.test للاختبارات. نُفّذ القرار دون hash للمعرف أو migration أو تعديل مالك أو رصيد أو reward أو purchase تاريخي. دالة owner وتخزين SQLite لم يتغيرا. لا يغلق القرار D03 أو D06 أو ترحيل الهوية.

أُعدت نسخة نشر مستقلة من HEAD البعيد، تضم الاعتماديات السابقة اللازمة للشيفرة والواجهات واختبارات regression. نجحت عليها 157 اختبارات JVM و9 اختبارات Host SQLite، وبناء Debug/Release وLint بلا أخطاء، مع 11 تحذيرًا لكل variant. نتائج هذه الفقرة تخص نسخة النشر؛ نتائج الأقسام السابقة تخص تشغيل W1 الأصلي. تفاصيل النطاق والاستبعادات في `W1_PUBLICATION_DEPENDENCY_AUDIT.md`. بقيت W1 PARTIAL.
