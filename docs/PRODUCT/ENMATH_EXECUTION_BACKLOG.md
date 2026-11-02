# EN.MATH — Execution Backlog

2026-10-25 · **PLANNED / NOT STARTED BY THIS AUDIT**. كل مرحلة تحتاج موافقة المالك. الجهد S/M/L/XL نسبي لا موعد، ولا يشمل تقديرًا مختلقًا لعدد صفحات الكتب. Feature IDs من [PRD](ENMATH_STUDENT_FINAL_PRD_DRAFT.md)، QA من [Acceptance Matrix](../QA/ENMATH_STUDENT_RELEASE_ACCEPTANCE_MATRIX.md).

جذر الشيفرة في الجداول `app/src/main/java/com/ahmadmol/enmath/`؛ أسماء ملفات الهدف الجديدة موسومة NEW داخل docs فقط، لم تُنشأ شيفرتها. Unit=JVM، Integration=مخزن فعلي معزول/مهاجرات، Device=المستخدم/مختبر بإذن لاحق وهاتف حقيقي؛ لا emulator/ADB في هذا التكليف.

## 1. التسلسل المصحح وربطه بالخطة الأصلية

| Wave / Epic | مرجع PH/E الأصلي | يعتمد على | Gate / الخطر |
|---|---|---|---|
| W0 اعتماد الوثائق والUX | PH-00/01؛ E01/02 | تقرير المراجعة، D01..D16 بحسب المسار | اعتماد PRD وP0 فقط؛ لا تنفيذ شامل |
| W1 P0 سلامة البيانات والهوية والمدخل | PH-02 + جزء PH-03/06؛ E03/04/11/14 | D01/03/05، owner policy | no loss/leak/reset/false auth؛ مهاجرات fixture أولًا |
| W2 Domain/render/assessment/review | PH-03/04؛ E05..08 | W1، D04/07/13 | عقود مشتركة للمرحلتين، durable learning loop |
| W3 وحدة حقيقية + packaging/pilot | PH-04/05؛ E09/10/15 | W2 + الكتب/الحقوق/المراجع | reviewer+device+offline؛ لا نشر محتوى غير مراجع |
| W4 المخطط والتحفيز والمكتبة والبحث | PH-07/08/09؛ E12..17 | W1/W2؛ المكتبة والتنزيل W3 | جميع الأنماط والحالات، مجانية الفيزياء، no manual rewards |
| W5 إنتاج الكتب | PH-10؛ E18/19/20 | W3 pilot ناجح؛ محتوى الكتب ومراجعيها | B1 كامل ثم B2 ثم G9؛ 100% أهداف الطبعة لكل إعلان كامل |
| CX حسابات سحابية مشروطة | PH-11؛ E21 | D01/02/11/12/15 + W1/W2 | يصبح إلزاميًا قبل إصدار يعد بـAuth/Sync فقط |
| W6 تأهيل إصدار | PH-12؛ E22 | كل ميزة معلنة + device/science/rights | GO/NO-GO موثق؛ لا blockers أو ادعاء نجاح من build |

المدخل وهوية Guest وstage lock يجب أن يسبقا تخزين التقدم الجديد؛ لا يؤجلان لما بعد إنتاج الحزم. المخطط الكامل جزء Student Complete، لكن النسخة الرأسية تبدأ أسبوعية بالإنجاز وتوسّع كل الأنماط في W4 دون ادعاء اكتمال سابق. لا يغير ذلك ترتيب إنتاج الكتب أو يفوض عملًا متوازيًا.

## 2. قصص W1 الضيقة الجاهزة لتفويض منفصل

### P0-01 — Preserve and detect corrupt legacy data (M، F33، QA-27/28/31)

القيمة: منع seed fallback من إخفاء تلف بيانات ثم كتابته فوق الأصل. Depends: اعتماد W1؛ owner هندسي يحدده المالك. Current: core/data/ProductRepository.kt:ProductCodec.load/save، LocalDemoRepository.kt:update. Data: product_v1 في global/personal prefs، بدون تعديل schema Gems.

G/W/T: Given JSON موجود تالف، When القراءة أو أول كتابة لاحقة، Then تعاد CorruptLocalData وتحفظ البايتات الأصلية ويفصل عن حالة تثبيت جديد؛ لا seed overwrite أو reset. التغيير المتوقع مستقبلًا result/error contract وquarantine/recovery UI محدود، لا migration شامل هنا.

Unit: absent/valid/invalid/unknown-version/partial arrays. Integration: fake storage failures ثم actual isolated prefs restart، hashes قبل/بعد؛ لا fixtures من جهاز المستخدم. Device: رسالة الاستعادة وإغلاق/عودة بإذن لاحق. DoD: الأصل لم يتغير، لا success دائم قبل acknowledgment، والأخطاء موثقة. Risk: اعتماد UI على fallback؛ لا يعالج بمسح التخزين.

### P0-02 — Stable learner identity + stage lock + explicit aliases (L، F01/02/33، QA-05/06/28/31)

Depends: P0-01، D03 وتحديد UX لحساب المرحلة؛ Current: core/model/Models.kt:User، LearningRepository.kt:LearningState، ProductRepository.kt:activeStudentId، LocalDemoRepository.kt، LocalGemsPolicy.kt. Data target: LearnerIdentity/IdentityAlias/MigrationJournal (NEW). مرحلة legacy مجهولة تبقى unresolved حتى اختيار موثق، لا تُستنتج من Demo.

G/W/T: Given متعلم تاسع وحساب بكالوريا آخر أو بريد متغير، When الدخول/فتح رابط/تحديث، Then owner ثابت والمراحل معزولة، aliases المؤكدة محفوظة ولا إعادة إسناد لاسم مشابه. Unit: same-name/different-email/changed-contact/unknown-stage/alias conflict. Integration: additive import counts+ledger owner reconciliation وcrash resume. Device: حسابان/Back/deep links/guest scopes. DoD: كل قراءة وكتابة مالكها واضح، لا تعديل stage لحساب قائم ولا جمع أرصدة.

### P0-03 — Durable guest + isolated account switch (L، F03/05/06، QA-02/03/06/29)

Depends: P0-01/02؛ Current: LocalDemoRepository.kt:personalPrefs/update/signIn/logout، ENMathApp.kt:guest/guarded، ProductGraphs.kt:ProtectedContent. Data: guest progress/bookmarks/history، UUID ثابت؛ claim journal مقترح، لا cloud claim نشط قبل Auth.

G/W/T: Given Guest أنجز درسًا وحفظ درسًا، When إغلاق/عودة ثم دخول حساب مختلف، Then يبقى Guest مستقلًا بلا نقل تلقائي، ويعمل حفظه وفتح الموارد العامة بلا gate. Unit/Integration: reload guest A/B، account switch، orphan legacy claim، duplicate claim rejection، no Gems. Device: cold restart/rotation/ترقية guest باختيار واعٍ مستقبلًا. DoD: محلي دائم واضح الحدود؛ لا يذكر النسخ السحابي؛ لا seed reset.

### P0-04 — Gems invariants and special demo compatibility (M، F22/33، QA-08/41/42)

Depends: P0-01/02 + D05/06؛ Current: LocalGemsCoordinator.kt:publish/purchase، LocalGemsPolicy.kt:validate، SQLiteLocalGems.kt، features/student/StudentHomeV2.kt:onClaimReward. Data: الستة gem_*؛ لا balance reset ولا حذف. نطاق خاص يراجع استثناء ahmed@en.math وأثره فقط، مع بقاء الملاك الآخرين.

G/W/T: Given earned balance أقل من item.price والحساب الخاص يحمل display bonus، When purchase، Then السعر المسجل يساوي الخصم الفعلي وفق السياسة المعتمدة أو ترفض العملية بوضوح مع بقاء السجل. Given صندوق Home، When claim، Then لا success مرئيًا بلا نتيجة محفوظة ومصدر إنجاز مؤهل.

Unit: price 40/120 وحدود الرصيد 0/39/40/119/120، normal/special owner، repeated purchase، cap/replay، no weekly إضافة دون قرار. Integration: SQLite rollback/concurrent spend، event delivery crash before/after ack، حفظ historical grants. Device: Home/Profile/wallet اتساق ورسالة الفشل. DoD: validate invariant لكل سجل، السعر=الخصم، no reset، لا مكافأة UI فقط.

### P0-05 — Honest entry and production/demo boundary (M، F01/04، QA-01/07/35)

Depends: D01، P0-02/03؛ Current: AuthViewModel.kt/AuthScreens.kt/CleanLoginScreen.kt، OnboardingScreens.kt، ENMathApp.kt. Data: LocalIdentity مقابل AccountLink؛ لا مزود يركب الآن. G/W/T: Given build إنتاجي محلي، When دخول/استعادة، Then لا يُقدّم 123456/نجاح validation كحساب موثق، ويظل guest learning متاحًا. إذا D01 اختار cloud تصبح القصة معتمدة على CLOUD-01 قبل قبول الدخول.

Tests: Unit role/auth capability gates؛ Integration session restore لا secrets؛ Device entry/return destination/error/offline. DoD: copy صادق، teacher entry خارج رحلة الطالب دون حذف الكود، محلي لا يوعد باستعادة من جهاز آخر.

### P0-06 — Owner-bound learning commands and regression baseline (M، F06/09/12، QA-29/32/37)

Depends: P0-02؛ Current: AppViewModel.kt:completeRecorded/recordPractice، QuizViewModel.kt، InteractiveLessonViewModel.kt، ProductGraphs.kt، StudentNavigationPolicy.kt/StudentHeaderPolicy.kt، SolverViewModel.kt. G/W/T: Given تدريب بدأه A، When يختلف صاحب الجلسة قبل submit، Then ترفض كتابة التعلم إلى B، لا الاكتفاء بمنع Gems. command IDs تمنع إعادة التسجيل بعد crash/retry.

Tests: Unit owner change/replay/undo/reset/navigation headers؛ Integration durable attempt + outbox/reload؛ Device switch أثناء lesson/practice + Splash/Back/IME. DoD: جميع مسارات الكتابة تتحقق من المالك، ويحافظ regression على tags/navigation والبيانات القديمة. يرتبط الحد المعاملي النهائي بـDATA-01، ولا يدعي توفيره بواسطة boolean UI فقط.

## 3. بقية الـEpics → Stories → Traceability

الاختبارات التالية **مطلوبة** وليست نتائج ناجحة. DoD العام آخر الوثيقة بالإضافة للنتيجة المحددة في كل صف.

| Story / Epic / جهد | Depends / PRD | Affected code الحالي → target data/logic | Unit / Integration / Device المطلوبة | DoD الخاص / المخاطر |
|---|---|---|---|---|
| UX-01 IA وstate design، M | W0، D10/16؛ F19/35 | ENMathApp/ProductGraphs، Components/ReferenceDesignSystem/Theme، HomeV2/Curriculum → state contracts shared | U navigation policy؛ I route restoration؛ D كل screen/state، RTL/LTR/×2/Light-Dark/TalkBack/IME | نقاط دخول Planner/Library/Review واضحة، no teacher dependency؛ لا redesign غير معتمد |
| DATA-01 Domain + Room/adapters، L | P0-01..03/06، D04؛ F07/33 | LearningRepository/ProductRepository/ProductModels/ProductSeed → Identity/Edition/Skill/Attempt/Event/Outbox/Journal (NEW) | U schema/IDs؛ I migrations + corruption + disk-full + event replay؛ D upgrade/process death | local source of truth، no Seed reads في slice؛ snapshots محفوظة؛ لا transaction زائفة عبر DB/files |
| CONTENT-01 intake + first-unit specification، XL تحريريًا | الكتب/الطبعات/حقوق/مراجع، DATA-01؛ F34 | DemoContent/ProductSeed كمرجع legacy فقط → Coverage/SourceRef/ReviewLog | U validator fixtures؛ I referential integrity؛ D معاينة صفحة/رسم؛ review مستقل | BLOCKED_BY_TEXTBOOK؛ هدف/صفحة/سؤال لكل عنصر، لا عناوين منهج مختلقة |
| LEARN-01 generic renderer + completion/resume، L | DATA-01، CONTENT-01 للوحدة الحقيقية؛ F08/09 | CourseScreens/LessonBlocks/InteractiveLimitsScreen/LessonPresentation/MathRender → LessonRevision/Block/Resume | U block dispatch/completion؛ I persistence/pins؛ D long math/خط كبير/استئناف | renderer المرحلتين نفسه، قوالب مناسبة للكتاب، completion مستقل |
| LEARN-02 rubric/assessment/attempts، L | DATA-01، D13، CONTENT-01؛ F10..12 | LearningRules.correct، QuizVM، PracticeScreens/LimitsPracticeScreens، AppViewModel → per-question Attempt/Session/Rubric | U fractions/tolerance/domain/unsupported/assistance؛ I duplicate submit+revision pin؛ D hint/retry/end/restore | لا scoring وهمي للتعبير/البرهان، أسئلة مستقلة؛ محتوى مراجع |
| LEARN-03 mastery + mistakes/review، L | LEARN-02، D07؛ F13/14 | LearningRules.mastery/topic/stats، ProductProgressScreen → SkillEvidence/MistakeReview/ReviewDue | U no seed accuracy/no completion=mastery؛ I history retained/review new item؛ D empty/due/defer/plan opt-in | حالات وصفية policy versioned؛ no حذف الخطأ القديم |
| PACK-01 pack/publisher contract & validator، L | DATA-01، CONTENT-01، D11؛ F28/34 | NEW tooling/pack schema لاحقًا؛ local assets retained → Manifest/Signatures/ReviewLog | U schema/path/size/signature/key rotation؛ I reproducible packages؛ D preview approved pack | مفاتيح خارج APK، بيانات فقط، rights/review تمنع draft publish |
| PACK-02 downloads/install/update/delete، XL | PACK-01، W1؛ F27..29 | NEW downloader/install store؛ Library/CourseScreens UI → DownloadJob/Installation/VersionPin | U state machine؛ I crash/incomplete/fs+DB recovery/no-space/ZIP bomb؛ D network switch/offline/delete active | لا Ready قبل verify، progress لا يحذف، retry/resume آمن |
| PACK-03 host benchmark/publishing operations، M | PACK-01/02، D11، تصريح تشغيل لاحق؛ F27 | NEW content catalog/domain config خارج APK، لا تأسيس حالي | U catalog URL؛ I range/ETag/manifest delivery؛ D قياس سوريا فعلي | اختيار موثق بميزانية/أهلية/قياسات؛ no provider claim من docs |
| TOOLS-01 bounded Solver + context/history، M | DATA-01، D13؛ F23 | SolverEngine/ViewModel/WorkspaceScreens/SimilarPractice/SolverExperienceContract → Capability/Result/History | U supported/unknown/domain/exact examples/dedupe؛ I owner reset/history؛ D keypad/share trial/context | لا وعد CAS/OCR/OS-share قبل تنفيذ؛ الطرق مراجعة من الكتب |
| TOOLS-02 Math Lab guardrails/context، M/L | DATA-01، CONTENT-01، D13؛ F24 | GraphEngine/GraphViewport/ExploreScreen/LimitExploration → GraphContext | U inverse coords/domain/discontinuity/finite؛ I lesson→graph؛ D zoom/pan/labels/accessibility | QA-17 يرفض 1/x صراحة حتى يدعمها، لا curve across asymptote |
| PLAN-01 weekly achievement + manual/linked، L | DATA-01، UX-01، D14؛ F15/16/19 | NEW planner feature/models/repos؛ HomeV2 snapshot → Plan/Goal/Event | U quantity/units/idempotent/manual-no-Gems؛ I restart/linked event؛ D 4 علوم→2/4 بلا ساعات | أول خطة قصيرة، محفوظة per-owner؛ core weekly slice |
| PLAN-02 all periods/modes/sessions، L | PLAN-01، D14؛ F17 | NEW planner period/session engine → daily/weekly/monthly × achievement/time/hybrid | U 9 combinations/month-end/leap/timezone/DST/conflicts؛ I mode conversion؛ D calendar+large font | لا كمية ضائعة عند تغيير mode؛ لا وقت إلزامي في achievement |
| PLAN-03 carry/rescope/history/undo، M | PLAN-01/02؛ F18 | NEW GoalRevision/CarryOver/undo → planner history UI | U remainder/target decrease/repeated carry؛ I journal/reopen؛ D نهاية فترة داعمة | لا نقل قسري أو reward أو تاريخ ممحو |
| MOT-01 motivation + achievements، M | LEARN-03، PLAN-01، UX-01؛ F20/21 | HomeV2 ونصوص UX → NEW curated catalog/rules/display/earned records | U cadence/opt-out/empty/no punitive؛ I event provenance/reload؛ D no interrupt/notifications denied | badges دليل، التحكم بالوتيرة، لا data مزروعة |
| MOT-02 final Gems policy integration، M | P0-04، LEARN-03، D06؛ F22 | LocalLearningGems/Policy/Coordinator/LocalGemsScreens → verified event adapter | U exclusions/caps/replay/no weekly بدون قرار؛ I cross-store outbox؛ D wallet/guest/owned reader | لا رصيد مالي أو شراء أساس/فيزياء؛ audit تاريخي بلا reset |
| LIB-01 licensed public catalog/coming soon، M | DATA-01، D09؛ F07/25 | LibraryScreen/ProductGraphs ProtectedContent/ProductModels → ResourceRights/Availability/alwaysFree | U free access/rights/stage؛ I catalog indexing؛ D guest/physics/coming soon | بلا classId/account/Gems للملفات العامة؛ no نشر حقوق مجهولة |
| LIB-02 local reader + saved positions، L | LIB-01، PACK-02؛ F26 | NEW PDF reader + library/download UI → ReadingPosition/Bookmarks/files | U bounds/page/version؛ I restart/delete retains records؛ D PDF big/low memory/offline/RTL | قارئ فعلي لا نص seed، استئناف ومفضلة لكل مالك |
| AUX-01 search/settings/help/privacy/report، M | DATA-01، LIB-01، UX-01؛ F30..32 | SearchScreen/NotificationsScreen/ProductProfileScreens/AppViewModel → scoped index/prefs/support draft | U normalized blank/stage filter؛ I persisted prefs/report queue؛ D empty/unsupported/denied/×2 | لا sent/scheduled/cloud success بلا تنفيذ؛ privacy/help routes معتمدة |
| CONTENT-02 B1 complete، XL | pilot W3، CONTENT-01، D08؛ F34 | content packs/coverage/review pipeline؛ لا أسماء دروس رسمية حتى الكتب | U every rubric؛ I links/schema/version؛ D+علمي كل هدف/عينة واجهة ممثلة | 100% أهداف B1 المنشور، سجل errata وقبول، لا عدد مفترض |
| CONTENT-03 B2 complete، XL | CONTENT-02 accepted + كتاب2؛ F34 | shared content engine/packs؛ templates إضافية عند الحاجة فقط | U/I/D+science كما B1 | 100% B2 قبل G9 production |
| CONTENT-04 Grade9 complete، XL | CONTENT-03 accepted + كتاب9؛ F02/34 | same screens + G9 edition/skills/rubrics | U stage isolation؛ I no cross-account/progress؛ D G9 رحلة كاملة+science | لا نسخ Bac إلى G9 أو تبديل stage |
| CLOUD-01 conditional Auth/Sync، XL | D01/02/11/12/15، W1/W2؛ F04/05/32 | AuthViewModel/ENMathApp + NEW AuthRepository/API/Outbox sync | U/session conflicts؛ I authorization/RLS/recovery/rate-limit/delete/replay؛ D Syria restore/two accounts/two devices | لا provider/service الآن؛ no local balance trusted server-side؛ gate قبل أي وعد cloud |
| REL-01 release/ops/privacy/performance، L | كل ميزة معلنة، D15/16؛ F35 | Manifest/build/signing مستقبلًا + QA runbooks؛ لا تعديل الآن | U+I regression؛ D low RAM/storage/network/TalkBack/themes/×2؛ science/rights review | RC signed/checksum، no P0، budget+distribution+rollback، GO/NO-GO المالك |

## 4. Definition of Ready / Done ومخاطر التقسيم

Ready لكل قصة: موافقة مرحلة، owner مسؤول، قرار مرجعي محسوم أو نطاق لا يعتمد عليه، UI states وعقد ملكية/حفظ، current paths، G/W/T، اختبارات معزولة، وخطة ترحيل/توافق. الكتب ليست شرطًا لـP0 أو schema fixtures؛ هي شرط لـCONTENT-01 والمحتوى العلمي وكل ادعاء تغطية.

Done لكل قصة: معايير PRD مطبقة، Unit/Integration الضرورية ناجحة بدليل جديد، خطأ/offline/retry وعزل معروفان، legacy محفوظ، docs updated، لا change مدمر أو reset؛ جهاز حقيقي للعرض والتكامل بعد موافقة؛ مراجعة علمية وحقوق عند المحتوى. قصة يمكن وصفها code-complete قبل جهاز، لكن لا توصف release-accepted.

لا fork واسع للواجهات حسب المرحلة، ولا migration للمحفظة لمجرد استخدام Room، ولا UI inventory يُعد منتجًا مكتملاً. مصادر الخطر الأعلى: ownership/corruption/cross-store consistency؛ عنق زجاجة المحتوى مراجعته وحقوقه؛ عنق زجاجة cloud إثبات الاستعادة والتشغيل في سوريا. لا موعد نهائي موثوق قبل جرد الكتب وتعيين المراجعين.

## 5. أول اعتماد مطلوب

اعتماد PRD واتجاه D01 وسياسة D03/D05 ثم تفويض **P0-01 فقط أو حزمة W1 المحددة**. لا تبدأ DATA-01 أو إنتاج الوحدة أو الحزم أو Backend تلقائيًا بعد ذلك؛ لكل gate مراجعة. إعداد fixtures/spec لوحدة واحدة جاهز للتخطيط؛ نشر وحدة حقيقية `BLOCKED_BY_TEXTBOOK`.
