# EN.MATH — Plan-to-Code Gap Report

2026-10-25 · **STATIC AUDIT OF WORKING TREE / DOCUMENTS ONLY**.

## 1. النتيجة التنفيذية

المشروع نموذج Android محلي غني نسبيًا بالواجهات والمنطق والاختبارات، وليس تطبيق طالب مكتملًا للمنهجين. نقاط البناء الجيدة: Compose/StateFlow/ViewModels وLearningRepository، معادلات native/KaTeX assets، lesson blocks وتفاعل النهايات، تدريب وتصحيح محدود، تاريخ حلول ومحفوظات وتفضيلات، ledger SQLite بمعاملات وdedupe وcaps، وسياسات تنقل ووصولية جزئية. لا يبرر وجودها إعادة كتابة واسعة، ولا يثبت صحة منهج أو رحلة إنتاج مكتملة.

أعلى المخاطر: عدم وجود stage/ID مستقر، حفظ Guest غير دائم عبر المسار الحالي، silent seed fallback، owner-check للجواهر بعد كتابة التعلم، عدم ذرية التعلم والجواهر، استثناء Demo +1000/سعر شراء غير متزن، وغياب دورة محتوى/attempts/version/review/packs إنتاجية. لا Room/Planner كامل أو Backend حقيقي مثبت. القبول العلمي والبصري غير منفذ بهذه الجولة.

## 2. نطاق المصدر والوثائق

- Branch: `feature/ui-refinement`؛ HEAD: `9e47d685085c39d181e495d4e95b6306a39e4570`. لا checkout/commit/push/reset/clean. Working Tree كانت تحتوي تغييرات وملفات غير متعقبة قبل المهمة؛ التدقيق على بايتاتها الحالية.
- الخطة الأصلية المقروءة: docs/PRODUCT/ENMATH_STUDENT_FINAL_MASTER_PLAN_V1_2026-10-25.md. أمر التدقيق موجود باسمه المطلوب وبنسخة `(1)`؛ تعليماتهما متطابقة بحسب المقارنة اللاحقة. `ENMATH_STUDENT_MASTER_PLAN.md` ليس الاسم الفعلي؛ لم نفترض غياب الخطة.
- سجل `ENMATH_FEATURES_TECH_BACKEND_DECISION_REGISTER_2026-10-25.md` لم يظهر بعد جرد ملفات المشروع والبحث عن أسماء بديلة/مراجع. لم يُقرأ ولم يُختلق. المصادر Blueprint/Completion Roadmap/deep-research المشار إليها بالخطة لم تظهر أيضًا. Open Decisions الجديدة توثق حدود هذا النقص؛ لا تُقدَّم كنسخة أصلية من مصدر غائب.
- راجعت الخطة والأمر ومصادر Kotlin/Gradle/Manifest والاختبارات ذات الصلة، واستعنت بالتقارير السابقة، خصوصًا ENMATH_PRODUCT_STRATEGY_RECOMMENDATIONS، STUDENT_UI_COMPLETION، STUDENT_VIDEO_REFINEMENT_RESULT، STUDENT_FIRST_BATCH_VERIFICATION_ADDENDUM، STUDENT_CODE_AUDIT، LOCAL_GEMS، BACKEND، وتقارير الدرس/الاستكشاف/الشاشات الثلاث. التقرير القديم ليس دليل اكتمال حالي؛ كل استنتاج رئيسي أدناه له source anchor.
- لم يظهر PDF/DOCX للكتب الرسمية في جرد المشروع؛ assets الموجودة KaTeX والخطوط ونصوص local_gems/schema. لم تُفحص طبعات أو حقوق أو قائمة أهداف رسمية: **BLOCKED_BY_TEXTBOOK**؛ لا ادعاء أن موضوعًا داخل Demo خارج أو داخل المنهج الرسمي دون الكتاب.

## 3. سجل الفجوات والعيوب

جذر الروابط النسبي من هذه الوثيقة إلى المصدر: `../../app/src/main/java/com/ahmadmol/enmath/`. الأرقام أدلة على النسخة المقروءة وقد تتغير بتنفيذ لاحق. Confirmed static = سبب ومسار واضحان، لا تجربة هاتف. Risk = مسار غير محمي يحتاج إثبات تكامل، لا واقعة تسرب مُشاهدة.

| ID / أولوية | الدليل الحالي | النتيجة/الشرط والأثر | التصنيف / الإجراء المقترح لا المنفذ |
|---|---|---|---|
| G01 P0 | [User](../../app/src/main/java/com/ahmadmol/enmath/core/model/Models.kt:22)، LearningState | User(name,email,type) بلا learnerId/stage/edition؛ لا ضمان حساب تاسع مستقل ثابت المرحلة | Missing capability؛ P0-02/DATA-01 |
| G02 P0 | [personalPrefs](../../app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt:67)، [update](../../app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt:105) | Guest يستبعد من scoped prefs؛ update لا يحفظ completed/product/last له، ويكتب metadata فقط؛ restart ليس استمرار سجله الحالي | Confirmed static gap؛ P0-03؛ legacy guest لا ينسب تلقائيًا |
| G03 P0 | [ProductCodec.load](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:307)، [fallback](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:424) | parsing exception→ProductSeed.seed بدل corrupt state؛ write لاحق قد يستبدل source بالseed | Confirmed fallback + overwrite risk؛ P0-01، لا مسح بيانات |
| G04 P0 | [owner identity](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:24)، LocalDemoRepository.kt:67، LocalGemsPolicy.owner | التعلم role:normalizedEmail مع alias خاص، Gems normalizedEmail؛ تغيير البريد يعيد مفتاح المخزن؛ لا ID مستقل | Confirmed architecture gap؛ aliases migration موثقة؛ P0-02/04 |
| G05 P0 | [Auth demo](../../app/src/main/java/com/ahmadmol/enmath/features/auth/AuthViewModel.kt:10)، ENMathApp.kt:signIn | validate محلي/code123456 بلا خادم/session/recovery؛ لا يثبت هوية أو تسليم رسائل | Demo؛ P0-05 وCLOUD-01 إن اختير؛ لا مزود مركب هنا |
| G06 P0 | [completeRecorded](../../app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt:52)، [recordPractice](../../app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt:92) | repository write قبل attributable؛ startedBy يحمي award فقط، لا يسد كتابة تعلم قديم للحساب الحالي إذا استدعي بعد switch | Confirmed missing guard / cross-owner risk؛ P0-06، اختبار sequence معزول |
| G07 P0 | LocalDemoRepository.update/ProductCodec.save:apply، AppViewModel.submitAssignment، SQLiteLocalGems.transaction | عدة writes prefs وDB مستقل؛ لا disk ack أو transaction للتعلم+الجواهر؛ UI Submitted ليس durable grant | Architecture risk؛ DATA-01/outbox؛ no exactly-once claim |
| G08 P0 | [display bonus](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt:44)، [purchase](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt:90)، LocalGemsPolicy.validate/SQLiteLocalGems.change | special owner يستعمل bonus1000، actualPrice=min(balance,price) لكن يسجل item.price؛ إذا balance<price يخالف invariant ويرفضه التخزين | Confirmed algebraic defect، لم نجرب شراء/نغير رصيدًا؛ P0-04/D05 |
| G09 P1 | [Home claim](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt:351)، claimedRewards:120 | callback يغير remember set ويهمل نتيجة pure award؛ لا Coordinator/storage call؛ حالة claim تزول بخروج composition | Confirmed UI-only grant defect؛ P0-04/MOT-02 |
| G10 P1 | LocalGemsPolicy GemReward.WEEKLY/LocalLearningGems.finishWeekly وLocalGemsScreens | مكافأة أسبوعية +15 موجودة؛ الخطة تطلب عدم إضافتها دون اعتماد وتشير لرفض سابق | Product conflict D06؛ لا نحذف أو نضيف المكافأة هنا |
| G11 P0 science | [mastery](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:60)، [stats fallback](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:116)، topic() | 40/40/20، best quiz وآخر تدريب وبعض seed fallbacks؛ no attempts قد يعرض seeded accuracy | Demo model لا إتقان معاير؛ LEARN-03؛ لا false percentage |
| G12 P0 science | [correct](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt:42)، ProductModels.Question/Attempt | كسور/عدد بتسامح1e-6 وإلا نص normalized؛ QuestionKind لا يحدد rubric/domain/units؛ Attempt بلا id/owner/revision/assistance | Partial deterministic grading؛ LEARN-02، domain-limited capabilities |
| G13 P0 release | LearningState.books/questions/practiceSets، ProductGraphs practice/solution، AppViewModel.recordPractice | Seed reads مباشرة وتوليد تسلسل خاص بالنهايات؛ تغيير repository وحده لا يبدل كل المحتوى | Coupling gap؛ DATA-01/LEARN-01/02 |
| G14 P1 | [Curriculum chip](../../app/src/main/java/com/ahmadmol/enmath/features/student/CurriculumScreen.kt:241)، ProductSeed.rules/checkpoints | عنوان «الأعداد المركبة» يفتح topicId logarithms الذي يحتوي ln؛ عدم اتساق UI ومحتوى مؤكد | Confirmed label→content mismatch؛ UX/CONTENT؛ لا يحكم على الفهرس الرسمي |
| G15 P1 | StudentHomeV2 journeyNodes/عنوان Bac؛ CurriculumScreen book1/book2/chips؛ StudentLimitsTopic | رحلة وعناوين موضوعات ثابتة، renderer overview خاص بالنهايات مع fallback؛ reuse جزئي وليس stage-dynamic كاملًا | Partial UI/data boundary؛ UX/DATA/LEARN |
| G16 P0 product | لا StudyPlan/Goal/Planner route/models في app/src | daily/weekly/monthly، achievement/time/hybrid، partial/carry/history غير منفذة | Not started؛ PLAN-01..03 |
| G17 P1 product | لا persistent MistakeReview/SkillEvidence/due model أو route | feedback موجود لا يعادل دفتر أخطاء ومراجعة جديدة وتاريخ مهارة | Not started domain؛ LEARN-03 |
| G18 P1 product | نصوص تشجيعية وreward nodes فقط؛ لا catalog/cadence/earned achievement repo | لا مساحة تحفيز وإعداد وتيرة وشارات موثقة كاملة | Partial visuals / missing system؛ MOT-01 |
| G19 P0 offline | app/build.gradle.kts/Manifest، assets، لا ContentPack/DownloadJob/WorkManager/client | bundled content محلي، لا downloads/signature/verify/pause/resume/atomic install/delete | Not started packs؛ PACK-01..03، لا internet permission claim من merged build |
| G20 P0 access | ProductGraphs.kt:331 ProtectedContent، LibraryScreen تبويب resources classIds/membership | Guest محجوب من library، وموارد تعتمد صفًا؛ لا public licensed physics/PDF/last page | Current/target conflict؛ LIB-01/02، إلغاء الاعتماد مستقبلاً بلا حذف teacher data |
| G21 P1 tools | SolverEngine.kt:18 when exact inputs؛ result null else؛ WorkspaceScreens trial/share | 7 worked examples لا parser/CAS عام؛ scan معلن محاكاة وshare حوار مستقبل؛ لا تزعم fake OCR قائمًا | Honest demo partial؛ TOOLS-01، مراجعة ملاءمة لوبيتال بعد الكتب |
| G22 P1 tools | GraphEngine.kt:11 bounded grammar؛ ExploreScreen.kt:736 gap heuristic؛ GraphViewport.kt | 1/x غير مدعومة، tan يعتمد null قرب poles وpixel jump؛ لا ضمان كل الدوال/الانقطاعات؛ context الدرس يحتاج عقد | Partial؛ TOOLS-02، QA-17 يرفض unsupported حتى دعم موثوق |
| G23 P1 time | ProductRepository.stats/Progress today تقسم timestamp على86400000؛ GemsPolicy Calendar Asia/Damascus | يوم الإحصاء UTC مختلف عن يوم مكافآت دمشق قرب منتصف الليل؛ planner لا timezone model | Confirmed differing definitions، أثر المستخدم يحتاج اختبار حدود؛ PLAN-02/MOT-02 |
| G24 P1 auxiliary | SearchScreen normalized blank guard؛ Settings تصف preference؛ Notifications local seeded | الإصلاحات السابقة موجودة ساكنًا؛ لا stage index/resources packs/notification scheduling/report queue/privacy flow كاملة | Partial؛ AUX-01، لا نعيد وصف المشكلة السابقة بعد إصلاح النص |
| G25 P0 release | لا كتب أو rights/reviewer/coverage chain معتمد | لا دليل اكتمال B1/B2/G9 أو قائمة مواد رسمية، ولا ملفات فيزياء حقوقها مثبتة | BLOCKED_BY_TEXTBOOK/RIGHTS؛ CONTENT/LIB |
| G26 P0 release | docs/BACKEND.md future contract، Gradle/Manifest/models | لا Backend إنتاجي مثبت؛ auth/phone recovery/hosting Syrian access/ops غير متحقق | Pending D01/D02/D11؛ CLOUD-01/REL-01، لا إنشاء خدمة |
| G27 P1 acceptance | Instrumentation sources وQA docs السابقة | لا هاتف/visual/TalkBack/large font/real storage/network evidence في الجولة؛ بعض suites تمس data أو تفترض emulator | DEVICE_PENDING؛ REL-01، لا تشغيل هذه suites على هاتف المستخدم |

### تحليل G08 دون لمس أي حساب

إذا ledger رصيده B=0، السعر P=40، special UI يعرض B+1000. purchase يسمح بالمرور ويخصم min(B,P)=0، لكنه يسجل unlock.price=40. validate ينتظر balance=rewards−prices، فتكون0≠−40 ويرمي استثناء؛ SQLite transaction ترفض التغيير ويعرض coordinator STORAGE_UNAVAILABLE. هذا برهان من المسار والحساب، **ليس تقرير تلف سجل فعلي أو اختبار runtime**. عند B≥P لا ينشأ هذا الفرق تحديدًا، لكن display bonus يبقى غير موثق بالledger. لا تُصفّر الأرصدة لمعالجة الخلل.

## 4. ما يحافظ عليه التنفيذ اللاحق

السجل ذو القيود ومنع تكرار المكافآت وcaps، استبعاد baseline legacy، owner tickets/Mutex وإخفاء wallet السابق، aliases المؤكدة وsnapshot الحسابات، password in-memory وعدم حفظها في prefs، Solver.reset الذي يمسح Undo، SimilarPractice dedupe، NavigationPolicy الذي يحجب chrome وقت transitions، HeaderPolicy، completion helper المستقل، Theme/semantics/native math LTR. هذه أصول لا تعاد كتابتها أو تحذف لتبسيط التنفيذ.

«مكتمل محليًا» يمكن وصفه لوظائف محدودة مثل حفظ اختيار لغة/ثيم أو تطبيع البحث ومنع blank أو قاعدة dedupe؛ **لا توجد feature release-accepted** هنا للمنتج النهائي. Screen count وsuccessful JVM artifacts لا يغيران ذلك.

جرد الحجم وإعادة الاستخدام: 91 ملف Kotlin main؛ 35 registrations للدخول/الطالب/Gems و20 للمعلم، مع graph containers منفصلة. هناك19 ملف UI تستدعيها رحلة الطالب/الدخول، وليست35 نسخة شاشة منفصلة؛ فروع lesson/practice/modal تفسر الفرق. تصنيف إعادة الاستخدام والعائلات الجديدة مفصل في Screen Matrix؛ لا ادعاء تحويل جميعها بتبديل repository واحد.

## 5. تعارضات الوثائق وحلها

| المصدر القديم/الادعاء | الدليل الأحدث/الحكم |
|---|---|
| استراتيجية حساب واحد مع stage context ومحفظة عبر المراحل | مرفوضة كأساس حالي؛ الخطة وأمر المالك أحدث ويثبتان حسابًا مستقلًا ثابت المرحلة |
| وصف تعليمات قديمة بأنها all curriculum complete | لا كتب/coverage علمية؛ أسماء Demo/تعليقات Syrian لا تكفي؛ BLOCKED_BY_TEXTBOOK |
| docs/BACKEND: prototype across student persona ومقترح server-only answers | snapshots/aliases الحالية تغيرت جزئيًا؛ offline self-study يحتاج answer rubrics محلية؛ مفاتيح امتحان cloud الآمن مسار آخر |
| تقارير 92/95/119/139 JVM أو8 SQLite PASS | تقارير تاريخية لإصدارات مختلفة؛ الموجود الآن139 declarations وartifact139، لا إعادة تشغيل هنا |
| QA سابقة تسمي visual complete | visual approval الحقيقي DEVICE_PENDING؛ تقرير static ليس screenshot runtime |
| Gems/weekly/streak ضمن سرد الخطة | التنفيذ الحالي WEEKLY+15 موجود، streak إحصاء؛ لا reward streak مستقل موثق؛ D06 يحسم ولا ينفذ هنا |
| «تبديل repository يكفي» | ProductGraphs/AppViewModel/Rules/Home/LessonBlocks تقرأ Seed مباشرة؛ يتطلب adapters وفك coupling |

## 6. الاختبارات والتحقق المنفذ في هذه المهمة

جرد ملفات JVM الـ18 وinstrumentation الـ9 وقراءة منطق الاختبارات ذي الصلة بمخاطر المراجعة؛ report artifacts السابقة قرئت فقط. لا Gradle build/tests/lint جديد؛ الهدف وثائق فقط وعدم توليد ملفات خارج docs. لم تُشغّل tools/test_local_gems_sqlite.py؛ يمكن تشغيلها في مهمة اختبار لاحقة على temp fixtures المعزولة، وهي لا تختبر مسار شراء Coordinator الخاص. لم نحتج تشغيلها لإثبات فجوة الوثائق أو الحساب الساكن.

`git diff --check` على Working Tree أعاد exit1 بسبب blank line at EOF في ملفات سابقة: AuthScreens.kt:507، CleanLoginScreen.kt:123، OnboardingScreens.kt:403، مع تنبيهات LF/CRLF. لم تُصلح لأنها خارج حدود docs. لا نزعم أن diff --check للمشروع الحالي PASS. فحص وثائقنا مستقلاً مذكور في سجل التسليم أدناه.

مخاطر test safety: DataPersistenceTest يبدأ بمسح enmath_demo ويعكس افتراضات قديمة عن sharing؛ StudentHomeV2Test يفحص sdk/generic ثم clear وshell config. القيود تمنع تشغيلهما، ووجودهما لا يعني اختبار phone ready. إعادة تجهيز اختبارات لاحقة تكون على fixture/app storage معزول لا على بيانات المالك.

## 7. الأولوية التالية وحدود ما ينتظر

اعتماد PRD + D01/D03/D05 → P0-01..06 سلامة/هوية/Guest/Gems/مدخل صادق/owner guards → DATA-01 → وحدة حقيقية بعد الكتب + renderer/rubrics/review → packs/pilot → planner/motivation/library/aux → B1→B2→G9 → release gates؛ cloud فقط عند اعتماده. [Backlog](../PRODUCT/ENMATH_EXECUTION_BACKLOG.md) يربط القصص بالملفات والكيانات والاختبارات والمخاطر.

يحتاج كتبًا: فهرس ومهارات وcoverage وأنواع مسائل وملاءمة طرق الحل وحقوق المصدر. يحتاج جهازًا حقيقيًا: عرض/Back/IME/TalkBack/×2/themes وprocess/storage/network/performance. يحتاج Backend decision/benchmark: cloud identity/recovery/two-stage accounts/sync/retention/host/payment/legal availability. لا يُعوّض أي منها ببيانات مختلقة أو mock success.

## 8. جرد ملفات الاختبار الحالية

هذا تعداد declarations لا تقرير تشغيل؛ أسماء test methods موثقة لتحديد التغطية الفعلية وحدودها عند تفويض التنفيذ. Instrumentation parameterization قد تضاعف الحالات.

| Source | @Test declarations | Test methods |
|---|---:|---|
| app/src/test/java/com/ahmadmol/enmath/AssignmentSubmissionVerificationTest.kt | 9 | submittedPublishesExactlyItsPayloadAndOneNotificationInMemory, secondSubmissionAtAttemptLimitDoesNotWriteOrDuplicateNotifications, guestRejectionDoesNotWrite, deadlineRejectionDoesNotWrite, assignmentNotFoundDoesNotWrite, allowedLateSubmissionStillUsesExistingPolicy, failureBeforeUpdatePropagatesWithoutReturningSubmittedOrChangingState, failureAfterPublicationCanLeaveMemoryChangedWithoutDurableSave, silentDiskFailureCannotBeDistinguishedFromSubmittedByCurrentContract |
| app/src/test/java/com/ahmadmol/enmath/BottomNavigationLayoutTest.kt | 3 | bottomNavRowHeightIsStrictlyBounded, itemContentFitsInsideBarAcrossFontScales, centerFloatingButtonMetricsAreValid |
| app/src/test/java/com/ahmadmol/enmath/CurriculumPresentationTest.kt | 3 | completionUsesOnlyLessonsInTheSelectedBook, continuationResumesUnfinishedLessonAndStaysInBook, completedBookOffersReviewAndEmptyBookHasNoDestination |
| app/src/test/java/com/ahmadmol/enmath/DemoLogicTest.kt | 4 | solverNeverInventsAnswersForUnknownExpressions, curatedExamplesHaveCorrectAnswersAndReasoning, catalogAndProgressReferencesAreConsistent, teacherRegistrationUsesEducationFieldsAndValidation |
| app/src/test/java/com/ahmadmol/enmath/FinalStudentRegressionTest.kt | 4 | domainsPunctuationAndAhmedNamesCannotAliasAnotherStudent, anotherStudentCannotReadCurrentStudentsMasteryThroughAhmedAlias, searchSavesActualQueriesDeduplicatesBoundsAndIgnoresBlanks, labDescriptionsFollowTheSelectedLanguage |
| app/src/test/java/com/ahmadmol/enmath/GemsIntegrationReviewTest.kt | 9 | productionCannotEscalateStudentTeacherOrGuestThroughDemoSwitch, debugSwitchNeedsAnExistingBoundLocalAccount, teacherOrBlankEmailExplainsMissingStudentWalletWithoutSeeding, forcedViewModelSwitchIsDeniedWithoutDebugContextAndKeepsProgress, caretPowersAreRaisedWithoutEvaluatingOrChangingTheirSigns, complexOrChainedPowersRemainLiteral, existingUnicodePowersAndFractionOperandsArePreserved, intervalsAndInequalitiesAreIsolatedInArabicProse, plainEnglishMathNeedsNoBidiControls |
| app/src/test/java/com/ahmadmol/enmath/InteractiveLessonSessionTest.kt | 6 | selectionSwitchesByValueAndCannotAdvanceBeforeSuccessfulVerification, reviewedAnswersSurviveBackNavigationAndRestoration, editedWrongAnswerDoesNotRestoreStaleFeedback, legacySessionCanReviewPassedQuestionsWithoutRecheckingOrChangingOwner, completionRequiresAllQuestionsAndDispatchesOnlyOnce, practiceGuardsCheckNextFinishAndRestoresHintAndAnswer |
| app/src/test/java/com/ahmadmol/enmath/LessonSequenceTest.kt | 2 | sequenceReferencesExistingQuestionsAndRequiresCheckpointBeforeCompletion, presentationDoesNotCreateAnIndependentProgressRecord |
| app/src/test/java/com/ahmadmol/enmath/LimitExplorationTest.kt | 4 | engineValuesApproachFiveFromBothSidesButHoleIsUndefined, plottedCoordinatesUseIndependentAxesAndCorrectIntercept, sliderRoundsAndClampsAndFormatsWithLatinDecimal, explorationSurvivesNavigationAndRestorationWithoutGrantingProgress |
| app/src/test/java/com/ahmadmol/enmath/LocalGemsCoordinatorTest.kt | 6 | queuedOldOwnerEventCannotLeakIntoNewOwnerUi, coordinatorRestartAndEmailCasePreserveTheSameWallet, failedSaveCannotPublishCurrencyOrSeedBalance, duplicateSpendAndSwitchKeepContentPrivate, failedInitialActivationCanRetryWithoutTouchingLearningData, teacherAndGuestHaveNoGemsIdentity |
| app/src/test/java/com/ahmadmol/enmath/LocalGemsPolicyTest.kt | 27 | identityIsCanonicalStudentEmail, missingOrForeignActivityOwnerIsRejected, activationNeverSeedsCurrency, excludesLegacyAchievementsAndOldEvents, rewardReplayCannotMintAgain, dailyCapPartiallyGrantsAndConsumesRemainder, weeklyCapPersistsAcrossDays, damascusDayAndWeekBoundary, weekKeyDoesNotResetAtYearBoundary, qualificationRequiresEveryAnswerAndEightyPercent, unlockDeductsOnceAndReopeningIsFree, insufficientBalanceDoesNotUnlock, inconsistentCachedBalanceFailsClosed, noCurrencyMovesBetweenOwners, actualLessonCheckpointIsRequired, interactiveLessonNeedsAllExistingChecks, practiceUsesRealGraderAndAllowsImprovementOnce, practiceThresholdUsesQuestionCountNotRoundedPercentage, baselineBlocksImportedSharedAttemptsAndCompletions, assignmentAnswersAreGradedAndDeduplicated, foreignOrSeedMasteryCannotRewardThisOwner, masteryRequiresNewOwnerEvidenceAndOnlyAwardsOnce, weeklyIsUnavailableWithoutRealLessonProvenance, weeklyUsesFiveExistingDistinctLessonCheckpoints, weeklyAwardsFifteenOnlyAndNeverNormalPractice, staleWeeklyReviewCannotMintNextWeeksReward, rewardAdaptersDoNotMutateLearningDataOrMastery |
| app/src/test/java/com/ahmadmol/enmath/NativeEquationFormattingTest.kt | 3 | limitKeepsItsOriginalApproachAndFractionOperands, ambiguousOperatorPrecedenceRemainsLiteral, nestedDivisionIsNotReinterpreted |
| app/src/test/java/com/ahmadmol/enmath/Phase1ArchitectureFixesTest.kt | 3 | activeStudentIdIsDerivedFromUserIdentity, learningRulesTopicMasteryRespectsStudentIdentity, assignmentFilteringUsesActiveStudentId |
| app/src/test/java/com/ahmadmol/enmath/ProductRulesTest.kt | 8 | allSeedRelationshipsResolve, curriculumOrderAndQuestionKindsMatchSpecification, masteryUsesDocumentedWeightsAndThresholds, teacherAndStudentReadSameAhmedMastery, gradingAcceptsNumericallyEquivalentAnswersWithoutEvaluatingCode, graphSupportsCuratedFunctionsAndBoundedPolynomials, limitMethodsHaveSameCorrectFinalAnswer, arabicSearchIgnoresDiacriticsAndAlefVariants |
| app/src/test/java/com/ahmadmol/enmath/StudentCompletionRegressionTest.kt | 7 | unownedLegacyAliasCannotOverrideFullEmailIdentity, recordedLegacyOwnerKeepsItsExistingClassroomIdWithoutRekeyingData, unrelatedAccountsCannotReuseARecordedAlias, aliasesDoNotAffectGuestTeacherOrSignedOutIdentities, interactiveLessonHasAnOuterHeaderAndRegularLessonOwnsItsHeader, probabilityFilterAndQueryAreAppliedTogetherAndUnknownFilterIsSafe, centralStudentTextPairsMeetNormalTextContrastInBothThemes |
| app/src/test/java/com/ahmadmol/enmath/StudentFirstFixBatchTest.kt | 3 | assignmentSubmissionReturnsExplicitResultAndSavesOnlyOnSuccess, routesSelectedTabMapsDeepDestinationsToParentTabs, weeklyGemsRequiresFiveCompletedVerifiedLessons |
| app/src/test/java/com/ahmadmol/enmath/StudentVideoRegressionTest.kt | 20 | onlyExactStudentTabsCanShowStudentNavigation, splashAndSubpageFadeMustFinishBeforeChromeAppears, missingSessionAndUnresolvedRestorationCannotShowOldChrome, recordedChoiceAndNumericDuplicatesCollapseBeforeTakingTwo, duplicateIdentityAndNotationVariantsDoNotConsumeSuggestionSlots, signsPowersCaseAndEvaluationPointsRemainDifferentMath, noAdditionalSuggestionProducesAnEmptyListWithoutFallbackTopic, viewportRoundTripsAtEverySupportedZoomAndPanDirection, pannedYTicksUseInverseYBoundsInsteadOfScreenYSign, panTranslationAndZoomScalingHaveKnownNumericalResults, tickSeparationAdaptsToPixelDensityTextWidthAndDoubleFontScale, actualGraphExamplesKeepCorrectSamplesAndCanvasCoordinates, supportedSolverExamplesKeepTheirAnswersAndUnsupportedInputsStayUnsolved, rapidFinishAndRestoredFinishDispatchOnlyOnePracticeResult, resettingSolverCannotUndoIntoPreviousAccountsInput, solverEditDeleteClearAndUndoStillWork, blankAndInvisibleSearchInputCannotWriteHistory, languageAndThemeChangesPreservePracticeSearchSavedAndMasteryData, anotherAccountCannotInheritHistoricalAliasOrItsMastery, historyDetailReturnAndTabRestorationKeepExistingNavigationWiring |
| app/src/test/java/com/ahmadmol/enmath/WeeklyGemCardPresentationTest.kt | 18 | completedWithNoReviewShowsCompletionInsteadOfRequirements, completedOverridesStaleReadyBusyAndErrorForTheSameOwner, priorWeekRewardDoesNotMarkThisWeekCompleted, missingAccountIsLoading, busyAccountDoesNotExposeReadyAction, storageErrorIsDistinctFromMissingLessonsEvenWithoutAccount, zeroVerifiedLessonsRequiresFive, fourQualifyingLessonsRequiresExactlyOne, fiveQualifyingLessonsWithoutReviewShowsLoadingNotAnotherLesson, moreThanFiveQualifyingLessonsDoesNotProduceNegativeRequirements, verifiedButUncompletedLessonsDoNotQualify, otherUiOwnerCannotExposeReadyOrCompletedData, otherAccountOwnerCannotExposeReadyOrCompletedData, missingStudentIdentityDoesNotTrustUiOwnership, ownershipUsesNormalizedCurrentStudentEmail, currentWeekReviewIsReadyAndUsesReadyCopy, staleReviewIsNotReadyForCurrentWeek, projectionsFollowLoadingReadyAndCompletionWithoutRememberedState |
| app/src/androidTest/java/com/ahmadmol/enmath/AppFlowTest.kt | 11 | studentRegistrationAndVerification, teacherRegistrationAndRecovery, studyPracticeResultAndPersistence, solverUnknownHistoryAndGraph, guestProtectionAndAdaptiveTabs, teacherCreatesClassAndAssignment, studentAssignmentSubmissionVisibleInRepository, languageThemeSearchNotificationsAndLongExpression, onboardingAndTabState, teacherResourceSharingReachesStudentLibrary, scanLimitMethodsAndTeacherResponse |
| app/src/androidTest/java/com/ahmadmol/enmath/CleanExperienceTest.kt | 1 | loginValidationRolesAndSignOut |
| app/src/androidTest/java/com/ahmadmol/enmath/DataPersistenceTest.kt | 1 | allUserGeneratedDemoDataSurvivesRepositoryReloadAndRoleSwitch |
| app/src/androidTest/java/com/ahmadmol/enmath/FullApplicationUiTest.kt | 1 | captureAllDestinationsAndResponsivePatterns |
| app/src/androidTest/java/com/ahmadmol/enmath/PremiumLessonUiTest.kt | 3 | choiceHintVerificationAndBackReview, explorationSliderHoleAndNavigationRetainValue, longMathRetainsNativeRendererAndChoiceOrder |
| app/src/androidTest/java/com/ahmadmol/enmath/ReferenceScreensTest.kt | 2 | referenceScreensAndInteractions, alternateSolutionAndMathKeyboard |
| app/src/androidTest/java/com/ahmadmol/enmath/RoleExperienceTest.kt | 3 | studentLearningAndTeacherEvidenceUseOneState, teacherProductivityFiltersAndEvidence, teacherToolsHistoryAndGraphStayInTeacherSession |
| app/src/androidTest/java/com/ahmadmol/enmath/StudentHomeV2Test.kt | 2 | responsiveMatrix, homeLayoutAndActions |
| app/src/androidTest/java/com/ahmadmol/enmath/TeacherReferenceScreensTest.kt | 3 | demoRoleSwitchPreservesDataAndSession, referenceNavigationAndCurriculum, localClassAndAssignmentInteractions |

## 9. سجل التحقق النهائي وحماية Working Tree

- قورنت بصمات SHA256 لعدد 180 ملفًا tracked أو untracked غير ignored خارج docs بين بداية التدقيق ونهايته: العدد نفسه، صفر تغيير/حذف/إضافة في هذه المجموعة. يشمل ذلك Kotlin/Compose/Gradle/Manifest/resources/tests/tools المقروءة. لا يُقدَّم هذا كفحص لكل ملف ignored أو قاعدة على هاتف؛ لم يُستدعَ أصلًا ما يعدلها.
- قورنت 50 وثيقة سابقة تحت docs: صفر تغيير أو حذف. أضيفت الوثائق الثمانية المطلوبة فقط، مع ملاحق جرد tags والاختبارات داخلها؛ لا نسخ مكررة أو ملفات فارغة أو سكربتات جديدة.
- تحقق نصوص الوثائق الجديدة: روابطها المحلية موجودة، fenced code blocks متوازنة، لا UTF8 replacement characters ولا trailing whitespace. هذا فحص وثائق، لا اختبار تطبيق.
- فحص التسجيلات من source: ENMathApp يحتوي 12 destination (10 public و2 Gems)، studentGraph يحتوي 23، teacherGraph يحتوي 20. root variants وgraph containers لا تعد تلقائيًا Routes إضافية.
- نسختا أمر المراجعة لهما SHA256 متطابق: 333042259FBC88F8D1A9B3739FE7494C10A3BD3070B38166C599137C33CCD845.
- لم يُعدل أي كود تطبيق أو اختبار أو مورد أو Gradle أو Manifest. لا تشغيل تطبيق/هاتف/Emulator/ADB، لا DB أو Gems reset أو حذف بيانات، لا Backend/service مدفوعة، لا Commit/Push. كل التعديلات السابقة محفوظة وفق نطاق المقارنة.
- المطلوب التالي مراجعة PRD والقرارات واعتماد حزمة P0 محددة؛ لا يُنفذ أي بند بهذه الوثائق تلقائيًا.
