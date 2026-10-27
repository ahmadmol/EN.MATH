# EN.MATH — Phase 1: معايير قبول المرحلتين والبيانات والهاتف

26 أكتوبر 2026 · v1.0 · **خطة قبول، وليست تقرير نجاح اختبارات**. كل السيناريوهات أدناه `NOT_RUN` في مرحلة المواصفات، أو `BLOCKED` بالتبعية المذكورة. لا جهاز أو Emulator/ADB استُخدم، ولا Gradle أو instrumentation أُعيد تشغيلها لهذه الحزمة.

[UX](../PRODUCT/ENMATH_PHASE1_DUAL_STAGE_UX_SPEC_2026-10-26.md) · [Screen Matrix](../PRODUCT/ENMATH_PHASE1_SCREEN_ROUTE_MATRIX_2026-10-26.md) · [Data Contracts](../ARCHITECTURE/ENMATH_PHASE1_IDENTITY_STAGE_DATA_CONTRACTS_2026-10-26.md) · [Figma Brief](../PRODUCT/ENMATH_PHASE1_FIGMA_DESIGN_BRIEF_2026-10-26.md) · [القرارات](../PRODUCT/ENMATH_PHASE1_REVIEW_AND_DECISIONS_2026-10-26.md).

## 1. قواعد الإثبات والبيئة

`PASS` بدليل reproducible، `FAIL` مع خطوات ونتيجة، `BLOCKED_CONTENT/PROVIDER/IMPLEMENTATION/DEVICE` لا يعتبر نجاحًا، `NOT_RUN` لا يختصر إلى «جاهز». نتيجة التدقيق السابق **188 اختبار JVM ناجحة** دليل تاريخي موثق في تقريره، لا تثبت العقود والـ Composables المقترحة هنا. Host SQLite لم ينجح تشغيله في التدقيق؛ لا نسجل نجاحه ضمنيًا.

التغطية اللاحقة ثلاث طبقات: عقود وfixtures معزولة JVM/HostSQLite، ثم مراجعة Figma، ثم قبول جهاز فعلي يديره المستخدم. fixtures مصطنعة لأخطاء التخزين تبقى في بيئة الاختبار فقط ولا تدخل التطبيق كبيانات تعلم أو نسب إنجاز. G9 التعليمي الكامل يظل BLOCKED_CONTENT حتى مادة مراجعة حقيقية؛ لا نسخ BAC لجعل الاختبار أخضر.

لا تختبر على بيانات طالب فعلية بمسح المخزن أو uninstall. اختبارات Android الحالية مثل `StudentHomeV2Test` و`FullApplicationUiTest` تحتوي شرط emulator وclear وexecuteShellCommand، و`DataPersistenceTest` تمسح enmath_demo؛ **لا تُشغّل على هاتف المستخدم ضمن هذه الخطة**. يعاد تصميم أي harness لاحقًا فقط بإذن ونطاق مستقل. لا Emulator أو ADB أو shell device commands في بروتوكول الهاتف.

## 2. قبول المدخل والملفات المحلية

| ID / أولوية | الشرط والخطوات | النتيجة المطلوبة | الدليل/ربط الخطة |
|---|---|---|---|
| Q-E01 P0 | بيئة QA مستقلة أول تشغيل، offline؛ افتح التطبيق | Splash آمنة ثم onboarding قابل للتخطي ثم chooser؛ لا Auth حاجز | فيديو E01–E03؛ AU-01/17 |
| Q-E02 P0 | اختر G9 ثم راجع Entry دون ضغط إنشاء؛ ارجع واختر BAC | لا profile/active جديد حتى فعل الإنشاء، ولا stage mutation | مقارنة registry fixtures + فيديو؛ AU-17 |
| Q-E03 P0 | إنشاء زائر G9، notice، Home، خروج وإعادة الدخول لنفس الزائر | نفس UUID، notice لا يعاد بعد إقراره، لا Gems | registry/لقطات مخفية الهوية؛ AU-01/02 |
| Q-E04 P0 | زائر BAC يسجل تعلمًا حقيقيًا، ثم افتح زائر G9 بعد تأكيد مساحة مستقلة، ثم عد BAC | هويتان منفصلتان، تقدم BAC باقٍ وغير ظاهر في G9؛ اختيار زائر BAC المتكرر لا ينشئ ثالثًا | fixtures + سجل إجراء يدوي؛ AU-05 |
| Q-E05 P0 | ملفان بنفس الاسم، مرحلتان أو نفس مرحلة؛ افتح بالتناوب | تعريف النوع/المرحلة/شارة الملف يزيل اللبس، لا دمج بالأسماء | E11 + owner checks |
| Q-E06 P0 | أثناء تمرين افتح switch ثم cancel، ثم switch مؤكد | cancel يبقي الحالي؛ المؤكد يحذر من draft غير محفوظة وينظف إجابات/VM/back stack القديمة | فيديو + stale-owner assertions؛ AU-07 |
| Q-E07 P0 | فشل commit عند إنشاء/اختيار/إقرار notice، أعد بنفس العملية | لا نجاح/انتقال/duplicate؛ الأصل محفوظ ويُختار السياق المحفوظ فقط بعد restart | failure injection JVM + فحص UI لاحق |
| Q-E08 P0 | snapshot تالف لملف، وملف مستقل سليم | لا overwrite أو fallback seed؛ شاشة recovery؛ السليم لا يفتح تلقائيًا | hash invariants + E16 |
| Q-E09 P1 | registry version أعلى/تالف | حفظ الأصل، لا listing مخمنة أو claim استعادة؛ لا clear CTA | fixture + نص recovery |
| Q-E10 P0 | إنشاء طالب محلي من Entry بعد وجود guest | UUID مستقل، guest بقي ببياناته، لا صفة authenticated ولا merge | registry/snapshots + E13 |
| Q-E11 P1 | logout ثم launch | لا إعادة فتح ملف خرج منه، اختيار stage/profile؛ لا رصيد في قائمة الجهاز المشترك | فيديو privacy؛ AU-17 |

E03 noticeVersion وE11 switch flow مقترحان؛ لا يعلن PASS الحالي لمجرد أن registry موجودة. أي missing feature تحسب BLOCKED_IMPLEMENTATION حتى تنفذ وتختبر.

## 3. قبول G9 المستقل

| ID / أولوية | الشرط والخطوات | النتيجة المطلوبة |
|---|---|---|
| Q-G901 P0 | G9 دون catalog | حالة CatalogUnavailable صريحة، لا كتب/عقد/تمارين BAC، ولا نسبة إتقان أو درس وهمي |
| Q-G902 P0 | بعد اعتماد catalog G9: افتح درسًا، أجب وأكمل، أغلق وأعد الفتح | next step/path يعكسان source IDs والـ receipts؛ completion محفوظة للمالك، لا إتقان بلا evidence؛ AU-02 |
| Q-G903 P0 | اطلب book/topic/lesson/practice/resource BAC من سياق G9، وعبر raw repository fixture | رفض read/write، لا تغيير stage ولا ترحيل owner، لا fallback limits؛ AU-04 |
| Q-G904 P1 | مسار به available/completed/unavailable وprerequisite معتمد | labels وأفعال صحيحة؛ المكتمل يعاد فتحه؛ لا قفل بحسب index أو completed.size فقط |
| Q-G905 P1 | لا محاولات ثم محاولة خطأ ثم مراجعتها | لا «ضعيف» من absence؛ review reason مشتق من الإجابة، ويحدث فقط بعد حفظ صحيح |
| Q-G906 P1 | خطة G9 بنفس أنماط domain، ثم تعديل/carry وإغلاق | أهداف مبسطة بصريًا، كل قياس ومصدر محفوظان، لا إنجاز أو Gems من check يدوي؛ AU-14 |
| Q-G907 P1 | G9 Solver/Graph/Library/Search/Gems store | مصادر G9 فقط، Unsupported عند غياب قدرة، unlock سابق لا يحذف ولا يعاد شراؤه |

Q-G902/904/905/906 تكون BLOCKED_CONTENT أو IMPLEMENTATION حتى وجود مادة وخطة/مراجعة فعلية؛ لا استكمالها بأمثلة مؤقتة تعرض على أنها المنهج.

## 4. قبول BAC المستقل

| ID / أولوية | الشرط والخطوات | النتيجة المطلوبة |
|---|---|---|
| Q-B01 P1 | افتح BAC Home مع partial catalog | resume ثم book pair ثم خطة/مراجعة وفق UX؛ لا journey G9 مكررة؛ label «جزئي/تجريبي» حسب المصدر |
| Q-B02 P0 | افتح بطاقتي كتاب ثم topic وlesson، بما فيها أي عنوان complex/logarithms | العنوان والمحتوى والـ ContentRef متطابقة ومراجعة علميًا؛ لا chip يشير لموضوع مختلف |
| Q-B03 P0 | درس فعلي/تدريب موثق ثم restart | last/completed/attempts/answers والنتيجة صحيحة، book denominator هو المتاح وليس ادعاء تغطية رسمي |
| Q-B04 P1 | حساب بلا محاولات، ثم evidence لمراجعة | لا ضعف أو accuracy seed؛ priority reason يمكن تتبعه لمحاولة محفوظة |
| Q-B05 P1 | خطة متعددة المواد، TIME/ACHIEVEMENT/HYBRID وDAY/WEEK/MONTH | وحدات قياس واضحة، manual source موسوم؛ لا جمع دقائق ودروس في نسبة غير معتمدة |
| Q-B06 P1 | لا exam date ثم date مستخدم أو معتمد | لا عداد افتراضي، مصدر التاريخ واضح، timezone صحيحة |
| Q-B07 P0 | resource فيزياء مؤهل، Guest ثم LocalStudent، offline بعد حفظ فعلي | مجاني بلا جواهر أو تسجيل، integrity/rights من المصدر؛ غياب resource=empty لا success؛ AU-12/18 |
| Q-B08 P1 | كل المحتوى المتاح مكتمل ثم Continue | يقترح مراجعة/درس متاح صحيح دون درس مفقود أو completion مصطنع |

## 5. عقود البيانات والترقية وledger

| ID / أولوية | fixture/تسلسل | invariant القبول والدليل |
|---|---|---|
| Q-D01 P0 | command ملتقط لـowner A ثم switch B وdelivery متأخرة | لا كتابة في B ولا notice/answer/رصيد A على واجهته؛ source A باقٍ |
| Q-D02 P0 | نفس commandId يعاد بعد restart؛ ثم payload مختلف بنفس ID | إعادة مطابقة بلا محاولة/منحة ثانية؛ payload مختلف Conflict لا success |
| Q-D03 P0 | stage مختلفة، edition مجهولة أو revision غير مناسب | WrongStage/UnknownEdition/ReviewRequired، لا BAC default أو إتقان خاطئ |
| Q-D04 P0 | Legacy owner غير مثبت، source مختلط، edition unknown، source تغير بعد preview | لا apply تلقائي؛ dry-run counts متطابقة والأصل byte-preserved؛ journal يتوقف عند تغير fingerprint |
| Q-D05 P0 | UUID imported سابقًا إلى G9 مع بيانات limits BAC | stage/UUID/wallet ثابتة، overlay ReviewRequired؛ التاريخ محفوظ منفصل عن تقدم G9 الحالي |
| Q-D06 P0 | restart عند prepare/copy/publish/ack باستخدام failure injection | استئناف واحد، لا overwrite target مختلف، لا profile/reward duplication، source لا يحذف |
| Q-D07 P0 | ملف legacyWalletKey موجود + عدة محافظ وأسماء متشابهة | resolver نفس المفتاح الحرفي؛ owner/activated/balance/rewards/unlocks/exclusions/lessons/assessments محفوظة قبل/بعد |
| Q-D08 P0 | نجاح ledger commit وفشل ACK ثم replay | نفس ledger entries/balance، event يقر بأمان، لا popup مكافأة لمالك آخر |
| Q-D09 P0 | guest يتعلم/يعيد المحاولة/claim مستقبلًا | لا Gems owner أو pending reward رجعية؛ لا mint عند إعادة تسلسل السجل |
| Q-D10 P0 | تغيير edition/content alias لإنجاز مكافئ | reward key التاريخي محفوظ، لا مكافأة جديدة لنفس الإنجاز؛ unknown equivalence يعلّق المنح المعني فقط |
| Q-D11 P0 | item مفتوح سابقًا، insufficient/error/owner mismatch، source stage conflict | لا خصم مرتين، لا balance zero بديل عند الخطأ، الاستحقاق محفوظ؛ listing جديد scoped |
| Q-D12 P0 | إكمال manual أو imported، وطلب WEEKLY/MASTERY جديد | لا mint وفق السياسة القائمة؛ التاريخ القديم يبقى مقروءًا، لا تعديل أسعار/limits |
| Q-D13 P0 | corrupt snapshot/DB أو schema غير مدعوم | لا fallback destructive أو source replacement، تعليمات مراجعة لا clear |
| Q-D14 P0 | baseline Teacher data قبل/بعد فتح/تبديل/مراجعة طالب | نفس classes/assignments/submissions/resources/questions/notifications وIDs، لا نسخ ملكية إلى UUID |
| Q-D15 P1 | فترة خطة حول منتصف الليل/بداية الاثنين/شهر قصير وتغيير timezone | قياسات وفق timezone المخزنة، history محفوظ، لا إعادة كتابة day/week ledger القديمة؛ clock injected لا تغيير ساعة هاتف المستخدم |
| Q-D16 P1 | تعديل target بعد progress ثم carry المتبقي مرتين | revisions محفوظة، لا حذف القياس أو مضاعفة مهمة/إنجاز، idempotency للترحيل |

Q-D07/10 لا تُختزلان إلى مقارنة الرصيد فقط: يجب مقارنة صفوف الجداول ومعانيها وربطها. invariant «المصدر بلا تغيير» لا يعني target بلا تعلم جديد مشروع؛ سيناريو الترقية نفسه لا يصك أحداث تعلم أو مكافآت. أدلة fixtures تستعمل مخازن معزولة، لا extraction تلقائيًا من هاتف طالب.

## 6. AuthFuture — محجوب حتى المزود

| ID | الشرط | النتيجة المطلوبة / التبعية |
|---|---|---|
| Q-A01 P0 | لا provider معتمد | لا form إنتاجي ينجح بالديمو أو OTP123456، المحلي مستمر؛ AU-19 |
| Q-A02 P0 | provider جاهز وفشل شبكة/انتهاء طلب | لا «أُرسل/تم الدخول» قبل ACK، retry حسب حدود المزود، لا حفظ أسرار؛ AU-11 |
| Q-A03 P0 | account.stage تختلف عن selectedStage | E15، قبول مرحلة الحساب أو رجوع؛ لا تغيير Account.stage أو guest؛ AU-06 |
| Q-A04 P0 | expired session بعد درس وإجراء مؤجل | لا تحويل بيانات الحساب إلى guest؛ بعد التحقق يعاد تقييم action وسياقه، لا ترسل إجابة guest باسم الحساب |
| Q-A05 P0 | claim نفس stage مع تقدم متعارض وإعادة المحاولة/انقطاع | preview واضح، source باقٍ وdedup lineage؛ لا Gems رجعية ولا دمج wallet؛ AU-08 |
| Q-A06 P0 | claim stage مختلفة أو حسابان برقم واحد | رفض claim المختلفة؛ سياسة المزود للحسابين والإثبات والاستعادة مختبرة لا مفترضة |

Q-A02..06 `BLOCKED_PROVIDER` حتى PH1-D10/11؛ قبولها لاحقًا يحتاج خدمة حقيقية، لا MockAuthFlow كدليل إنتاج. Q-A01 يمكن اختباره برمجيًا لموجود Demo gate، لكن كامل Entry الجديد يبقى IMPLEMENTATION_PENDING.

## 7. مصفوفة التصميم واللغات والهاتف

كل G9/BAC على ArabicRTL/EnglishLTR × Light/Dark × font100/200 = **16 configuration** لكل شاشة stage-sensitive أساسية؛ 150% فحص وسيط. Entry المشترك يمكن مشاركته حيث لا فرق مع تسجيل N/A. core screens: E03/E04/E05/E10/E11/E13، H، C01/C03/C04، L01، P01/P02، PLAN01، PROF01، GEM01، E16. حتى قبل مادة G9 تختبر Empty/Unavailable بجميع التركيبات؛ لا يُحتسب ذلك نجاح درس G9.

| ID | قبول بصري/تفاعلي |
|---|---|
| Q-V01 | 320/360/390/412 logical widths: لا قص CTA/حقول/اسم مرحلة أو صفحة horizontal scroll؛ Math وحدها يمكن أن تسحب داخليًا |
| Q-V02 | keyboard على إنشاء ملف وAuthFuture: الحقل والخطأ والزر مرئية بscroll؛ insets/status/navigation safe؛ لا header مزدوج |
| Q-V03 | TalkBack: labels/direction/order والـ tab المختارة والأيقونات والحالة، ترتيب Path خطي، Math alternative مقروء |
| Q-V04 | تباين الأزواج الفعلية فوق surfaces/gradients/disabled/focus يحقق أهداف Brief؛ لون وحده لا يشرح lock/error |
| Q-V05 | العربية المحيطة بكسور وأسّ وlim ورموز لاتينية: ترتيب المعادلة LTR لا mirror؛ محاور Graph/play لا تنقلب |
| Q-V06 | Home G9 مقابل BAC في نفس الثيم/العرض: فرق بنيوي مثبت في hierarchy والمسار/الكتابين والخطة، لا شارة فقط |
| Q-V07 | bottom tabs الخمسة وHome المركزية: labels كاملة في 200%، لا overlap مع CTA؛ subpages/Focus لا تعرض شريطًا متسربًا أثناء transitions |
| Q-V08 | loading/error/owner-change: لا flash اسم/إجابة/رصيد السابق؛ loading ≠ صفر؛ الحركة مختصرة ومخفضة عند reduced motion |
| Q-V09 | empty/offline/error حالات مختلفة ومصدر كل CTA حقيقي؛ Retry لا يصنع نتائج؛ اكتمال كل المتاح لا يعني المنهج الرسمي كله |

## 8. بروتوكول الهاتف الحقيقي — لاحقًا بإدارة المستخدم

1. تحديد الجهاز/Android/app build/hash/theme/locale/font، وبيئة QA منفصلة عن بيانات المستخدم. variant `w1Qa` الحالي مثال متاح لعزل package عند تجهيزه لاحقًا؛ لا تعديل Gradle أو تثبيت أو إنشاء حزمة ضمن هذه الجولة. لا افتراض أن تغيير suffix وحده يحقق اختبار ترقية بيانات الإنتاج.
2. تجهيز baseline QA معزول بشكل مشروع، باستخدام أفعال UI حقيقية على مادة مراجعة أو fixtures ترقية في بيئة مختبر معلنة؛ لا تحميل بيانات طالب فعلية دون نطاق صريح. لا uninstall أو Clear storage. تكون نسخة الترقية بنفس package/signature للبيئة المعزولة وبـversion مناسب عند توفيرها؛ المستخدم يثبت فوقها ويحافظ على الأصل.
3. إنشاء/اختيار زائر G9 وBAC وملفين محليين من الواجهة. G9 غير متاح حاليًا يوثق كذلك؛ عند توفر المادة ينفذ درس/تدريب فعلي لكل مرحلة. تسجيل refs منزوعة الهوية للنتائج والتاريخ والرصيد/الفتح قبل وبعد إن كانت متاحة عبر تشخيص QA مأذون، دون كشف أسرار أو بيانات الطلاب.
4. تغيير اللغة والثيم والخط من واجهة التطبيق/إعدادات Android يدويًا، ثم اختبار Back وIME وTalkBack والـ screenshots. Offline عبر إعدادات الاتصال يدويًا، لا ADB. يعاد الوضع السابق للمستخدم بعد الجلسة حسب اختياره.
5. إعادة تشغيل عادية، background/return، وإيقاف من إعدادات Android يدويًا عند الموافقة. **Force stop أو swipe-away ليس دليلًا كافيًا على كل حالات process death/state restore**؛ يسجل نوع الإنهاء بدقة. الحفظ بعد kill/انقطاع commit يفحص أساسًا بحقن الفشل في fixtures؛ low-memory restore على الجهاز إن لوحظ يوثق منفصلًا بلا ادعاء محاكاة لم تحصل.
6. ترقية package QA فوق baseline دون حذف، إعادة فتح كل ملف ومقارنة سجله ورصيده وفتح موارده، ومراجعة بيانات المعلم المعزولة؛ أي اختلاف غير متوقع يوقف القبول ولا «يصلح» بمسح البيانات.
7. فيديو قصير لكل رحلة وscreenshots لـG9/BAC في الثيمين، وملف نتائج به Expected/Actual/Status/DefectID. لا screenshots فيها هاتف/بريد/اسم طالب حقيقي، ولا رفع أدلة خارجية تلقائيًا.

الأجهزة المقترحة للقبول: هاتف فعلي ضيق وهاتف فعلي متوسط/عريض متى توفرا، مع Android ضمن minSdk24/target35 الحاليين. أي نسخة OS لم تختبر تسجل كفجوة؛ لا ادعاء دعم مختبر لكل API لمجرد minSdk. المقاس المقاس بوحدات dp الفعلية يدوّن؛ resolution الفيزيائية وحدها لا تحقق Frame360.

## 9. قالب النتائج وبوابات GO/NO-GO

قالب سجل لكل حالة: `Test ID / spec version / decision IDs / build+source snapshot / device or host / initial context mode+stage (redacted) / theme+locale+font+width / preconditions / steps / expected / actual / status / evidence path / defect severity / reviewer/date`. السجلات الجديدة فقط تغيّر NOT_RUN إلى PASS بدليل؛ جدول مواصفة لا يعتبر test report.

| Gate | دليل الإغلاق | ما لا يكفي |
|---|---|---|
| G1 وثائق Phase1 | كل طلب مربوط بوثيقة، labels current/proposed صحيحة، no-code invariant، القرارات Pending ظاهرة | المواصفات نفسها ليست موافقة تنفيذ |
| G2 UX/Figma | قبول G9 وBAC منفصلين، حالات الثيم/اللغة/الخط، صلاحية ربط CTA/مصدر | screenshot Home واحدة أو badge فقط |
| G3 Data safety | جميع Q-D P0 وQ-E P0، hashes وledger rows وcrash/replay/teacher invariants | نجاح رصيد ظاهري واحد أو اختبارات ذاكرة فقط |
| G4 Local learning | دورة درس/تمرين/خطة حفظ حقيقية لكل stage في النطاق المعتمد، no fake mastery | G9 Empty أو BAC Demo باعتباره منهجًا رسميًا |
| G5 Auth عند دخوله النطاق | provider فعلي وQ-A والقرارات PH1-D10/11 مغلقة | OTP ثابت أو MockProvider أو شكل نموذج |
| G6 Physical acceptance | دليل جهاز فعلي للتصميمين والترقية/العزل والثيمين واللغتين والخط، توقيع المالك وQA | JVM أو Figma وحدهما |

أي فقد بيانات أو اختلاط مالك/مرحلة أو wallet key drift أو مكافأة مضاعفة أو تعديل بيانات معلم = NO-GO. نقص المحتوى/Auth يوقف بوابته ولا يُخفى باختبارات غير ذات صلة. الإطلاق المحلي إن اعتُمد يمكن أن يستبعد Auth صراحة؛ لا يسمح ذلك بادعاء اكتمال تجربتين تعليميتين بينما G9 بلا مادة. التوقيعات المطلوبة لاحقًا: مالك المنتج للـ UX والنطاق، المراجع التعليمي للمنهج والتقييم، المعماري لعقود البيانات، وQA لدليل الهاتف. جميعها Pending الآن.
