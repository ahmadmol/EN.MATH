# W1 — Secure Publication and Dependency Audit

2026-10-25 · **INDEPENDENT BUILD VERIFIED / READY FOR COMMIT REVIEW**. W1 نفسها **PARTIAL**. لا اعتماد لـD03/D06 أو W2.

## أسباب التوقف ومعالجتها

- سابقًا تعذّر حفظ W1 وحدها على HEAD دون اعتماديات غير ملتزم بها؛ اعتمد المالك الآن نشر التبعيات السابقة الضرورية بعد فحصها.
- صُنّف المعرف السابق Potential PII / Legacy Wallet Identifier ولم يُعتمد نشره صريحًا. وافق المالك على إزالة قيمته وإزالة عرض ‎+1000 فقط ضمن D05. لا تتضمن هذه الوثيقة العنوان أو hash له.
- أزيل الاستثناء بالكامل؛ Coordinator ينشر GemAccount الفعلي. لا حاجة إلى معرفة العنوان القديم لقراءة المحفظة؛ تطبيع owner والاستعلامات نفسها يعملان لأي معرف محفوظ.
- SQLiteLocalGems.kt وschema ودالة owner لم تتغير عن نسخها المحلية الأصلية. لم يُفتح مخزن مستخدم، ولم تنفذ migration أو rekey/reset أو تسوية تاريخية.
- استُبدلت عناوين اختبار غير محجوزة بمعرفات اصطناعية ضمن example.test، مع الحفاظ على اختلاف الحسابات والـaliases المقصود في الاختبارات.

## النسخة المستقلة ومجموعة النشر

- المستودع `ahmadmol/EN.MATH`؛ الفرع `feature/ui-refinement`.
- worktree مستقل detached من HEAD البعيد `9e47d685085c39d181e495d4e95b6306a39e4570` بعد fetch والتحقق من مطابقته للمحلي. لم يبدأ من كامل Working Tree الأصلية.
- نُسخت المسارات المحددة فقط. local.properties مستقل يشير إلى SDK المثبت، وهو ignored وغير ملتزم به؛ يستخدم بيئة Android المعتادة ولا يحتوي سرًا. لا اعتماد على ملف مصدر محلي مستبعد.
- نسخ الملفات الأصلية وبصماتها محفوظة خارج المستودع. يُحافَظ على الملفات المحلية السابقة خارج نطاق النشر؛ لا hard reset أو clean أو force push.

## Dependency Closure المثبتة

| الجذر | التبعيات السابقة المنشورة وسببها |
|---|---|
| حماية JSON والزائر | account snapshots وaliases وactiveStudentId وحقول LearningState داخل ملفات البيانات المشتركة؛ لازمة لتجنب الرجوع إلى تخزين عام يخلط الحسابات |
| اختبارات W1 | StudentFirstFixBatchTest.kt لتعريف TestLearningRepository، واختبارات regression للهوية والتسليم والاستعادة والتعلم السابقة |
| ENMathApp/ProductGraphs | StudentNavigationPolicy.kt وStudentHeaderPolicy.kt وRoutes الحالية وPremiumLearning/ReferenceDesignSystem؛ استدعاءات الواجهات الحالية تحتاجها |
| الدرس والتدريب وHome | LessonPresentation وLimitExploration وCurriculumPresentation وInteractiveLessonViewModel/LimitsPracticeScreens والمكونات المشتركة؛ الحفاظ على العرض وTestTags الحالية ومنع fake claim |
| التنقل إلى الشاشات القائمة | شاشات الحساب والمكتبة والبحث والتقدم والحالّ والرسم المرتبطة، ومكونات العرض التابعة؛ لا إعادة تنفيذ للميزات |
| تكامل المعلم | التعديلات السابقة المرتبطة بهوية الطالب في ClassScreens وTeacherProductivityScreens والمكونات المشتركة، دون تعديل بيانات المعلم أو إعادة تصميمه في هذه الجولة |

إغلاق Kotlin الساكن الأولي كان محافظًا؛ مجموعة النشر النهائية هي المسارات المحددة أدناه. أثبت البناء المستقل اكتمال الاعتماديات دون نسخ تغييرات launcher وManifest وinstrumentation أو مستندات المنتج السابقة.

## الاختبارات المستقلة

```powershell
./gradlew.bat testDebugUnitTest assembleDebug assembleRelease lintDebug lintRelease --console=plain
python tools/test_local_gems_sqlite.py
```

- 157 JVM tests، صفر failures/errors/skipped.
- 9 Host SQLite tests على tempfile، تشمل التزامن وrollback وثبات الجداول الستة بعد إعادة تفعيل مالك legacy وفتح شراء قديم.
- اختبار Coordinator جديد: محفظة تاريخية بمعرف اصطناعي، رصيد العرض يساوي Ledger، grants/exclusions/assessments/unlocks والمالك ثابتة، والمحتوى المشترى متاح بعد إعادة الإنشاء وتطبيع حالة الأحرف.
- assembleDebug وassembleRelease وlintDebug وlintRelease ناجحة؛ صفر lint errors و11 warnings لكل variant. Release APK unsigned فقط، ولا تثبيت أو توزيع APK.
- لا Emulator/ADB/هاتف أو instrumentation؛ هذه الأدلة لا تدّعي Android SQLiteOpenHelper/device acceptance.

## الأمان والاستبعادات

- فُحص كامل محتوى شجرة Git الناتجة: 215 مسارًا، مع فحص مرشحي النشر للعناوين غير المحجوزة. لا أثر للقيمة الأصلية ولا أنماط أسرار أو عناوين غير محققة بعد sanitization. فحص الأنماط ليس ضمانًا مطلقًا للأمان.
- المعرف غير موجود في HEAD أو commits المحلية المفحوصة؛ لا حاجة إلى history rewrite، ولم يُستخدم hash باعتباره إخفاء هوية.
- مستندات PRODUCT السابقة التي تحتوي القيمة بقيت محلية خارج النشر؛ لم تنسخ إلى worktree. لم تُنشر مناهج أو PDF أو ملفات حقوق غير مثبتة.
- مستبعدة: local.properties و.env والتوقيع وSQLite/بيانات المستخدم وAPK/Build/Cache/Logs والنسخ الاحتياطية ومخرجات التدقيق المؤقتة.
- تغييرات AndroidManifest/launcher وinstrumentation والشاشات القديمة غير المستخدمة وTeacherWorkspace غير اللازمة لمجموعة النشر بقيت خارج Commits ومحفوظة محليًا.
- لم تتغير قيم DAILY_CAP/WEEKLY_CAP أو WEEKLY+15؛ استخراج constants السابق يبقي القيم نفسها. D06 باقٍ مفتوحًا.

## الملفات الفعلية في فرق النشر

- `app/build.gradle.kts` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/design/Components.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/LearningArtwork.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/MathRender.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/PremiumLearning.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/ReferenceDesignSystem.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/Theme.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/components/student/InteractiveComponents.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/design/components/teacher/ProductivityComponents.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsPolicy.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/model/ProductModels.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/Routes.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/StudentHeaderPolicy.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/StudentNavigationPolicy.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/assignments/AssignmentScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/auth/AuthScreens.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/features/auth/AuthViewModel.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/features/auth/CleanLoginScreen.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/CurriculumPresentation.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLessonViewModel.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LessonPresentation.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitExploration.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/explore/ExploreScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/explore/GraphEngine.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/explore/GraphViewport.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/library/LibraryScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/notifications/NotificationsScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/onboarding/OnboardingScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/practice/PracticeScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/progress/ProductProgressScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/search/SearchScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/solver/SimilarPractice.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/solver/SolverViewModel.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/solver/WorkspaceScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/student/CurriculumScreen.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt` — W1
- `app/src/main/java/com/ahmadmol/enmath/features/student/StudentLearningScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/teacher/ClassScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherProductivityScreens.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/AssignmentSubmissionVerificationTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/BottomNavigationLayoutTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/CurriculumPresentationTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/FinalStudentRegressionTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/InteractiveLessonSessionTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/LessonSequenceTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/LimitExplorationTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/LocalGemsCoordinatorTest.kt` — W1
- `app/src/test/java/com/ahmadmol/enmath/Phase1ArchitectureFixesTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/ProductRulesTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/StudentCompletionRegressionTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/StudentFirstFixBatchTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/StudentVideoRegressionTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `app/src/test/java/com/ahmadmol/enmath/W1AuthBoundaryTest.kt` — W1
- `app/src/test/java/com/ahmadmol/enmath/W1IdentityIsolationTest.kt` — W1
- `app/src/test/java/com/ahmadmol/enmath/W1ProductRecoveryTest.kt` — W1
- `app/src/test/java/com/ahmadmol/enmath/WeeklyGemCardPresentationTest.kt` — تبعية سابقة / تحقق / تدقيق نشر
- `docs/IMPLEMENTATION/W1_FOUNDATION_FINAL_REPORT.md` — W1
- `docs/IMPLEMENTATION/W1_PUBLICATION_DEPENDENCY_AUDIT.md` — تبعية سابقة / تحقق / تدقيق نشر
- `tools/test_local_gems_sqlite.py` — تبعية سابقة / تحقق / تدقيق نشر

## Commits والمزامنة

مراجعة staging تتم لمسارات صريحة قبل كل Commit. التقسيم: storage/guest، learning والاعتماديات، Gems/Legacy privacy، Auth boundary، وتقرير النشر. تُحفظ SHA النهائية في سجل Git والرد النهائي؛ لا self-hash داخل الوثيقة. يجب إعادة فحص البعيد قبل Push عادي، ثم مطابقة فرع العمل المحلي والبعيد مع إبقاء التعديلات المستبعدة محليًا.

## المتبقي

W1 PARTIAL: UUID الطالب وقفل المرحلة وترحيل الملكية في D03، وسياسة weekly في D06، ومدخل الإصدار في D01، وقبول Android الفعلي لم تُعتمد أو تُنفذ بهذه المهمة. قرار D05 الحالي يغلق استثناء العرض والخصوصية فقط.
