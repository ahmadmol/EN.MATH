# EN.MATH — Student Experience UI/UX & QA Audit

> **Audit فقط؛ لا إصلاحات منفذة.** اسم الملف مؤرخ حسب الطلب بـ 2026-10-21؛ استُكمل التدقيق في 2026-10-22 بتوقيت Asia/Damascus. النطاق: Student والواجهات المشتركة اللازمة لرحلته.

## 1. Executive Summary

التطبيق يملك رحلة تعلم محلية فعلية: تنقل Compose، منهاج وكتابان، درس نهايات تفاعلي، أسئلة وتغذية راجعة، نتائج وإتقان، محلل محدود، رسم دوال، واجبات ومكتبة وإعدادات، ومحفظة Gems مستقلة. الواجهات النشطة تستخدم الثيم المشترك؛ وجود ملفات Reference قديمة لا يعني أنها الشاشات المعروضة حاليًا.

**التقييم: مناسب لمراجعة نموذج محلي، وغير جاهز لاعتماد تجربة طالب بحساب فعلي أو لقبول شامل على هاتف حقيقي.** أهم الموانع مشاركة بيانات التعلم بين الهويات، المصادقة التجريبية، ومسارات فشل التحميل/التسليم التي لا تعيد نتيجة واضحة للواجهة. توجد مخاطر لتكبير خط التنقل، وعيوب محددة في الهيدر و Navigation Rail وشرح الرسوم. لا دليل في التسجيل على crash أو فقد فعلي لـ Gems، ولا مبرر لتسجيل P0 مؤكد.

المقارنة البصرية المباشرة شملت **12 شاشة ذات مرجع ظهرت في الفيديو**، إضافة إلى **محفظة Gems واحدة بلا مرجع مطابق**. فُحصت 34 وجهة/نوع شاشة مكتبيًا في جداول النطاق؛ هذه ليست 34 شاشة متحققة على الهاتف. الكتابان حالتان من شاشة واحدة، و Light/Dark والتبويبات وحالات الإجابة لا تضاعف العدد.

| الأولوية | العدد | التفسير |
|---|---:|---|
| P0 | 0 | لا عطل حرج مثبت بالأدلة المتاحة |
| P1 | 5 | موانع اعتماد ومخاطر عالية؛ بينها سيناريوهات لم تُستنسخ ميدانيًا |
| P2 | 14 | عيوب وظيفية/إيضاحية/وصول وتحسينات لازمة |
| P3 | 4 | اتساق وتوثيق وصقل |
| المجموع | 23 | AUD-01 إلى AUD-23؛ كل سبب مسجل مرة واحدة |

الأعداد تشمل عيوب كود مؤكدة، ومخاطر مشروطة، وفجوة إنتاج معلنة؛ **لا تعني 23 عطلًا شوهد في الفيديو**. يحدد Backlog حالة تحقق كل بند.

نقاط القوة من المصدر: StateFlow مع collectAsStateWithLifecycle؛ LearningRules مشتركة بدل درجات تصطنعها الواجهة؛ SavedStateHandle لإجابات التمارين والدرس التفاعلي؛ LTR مستقل للرياضيات؛ محتوى قابل للتمرير؛ ومعاملات SQLite لمحفظة Gems مع owner واضح ومنع تكرار المكافآت. التسجيل يثبت ظهور الثيمين والتنقل الأساسي، ولا يثبت جميع هذه الضمانات التشغيلية.

## 2. Scope & Evidence

### 2.1 المشروع وحماية التعديلات الموجودة

- المسار: <PROJECT>.
- الفرع المقروء: feature/ui-refinement.
- HEAD المقروء: 9e47d685085c39d181e495d4e95b6306a39e4570.
- لم يظهر AGENTS.md في جذر المشروع/قائمة ملفاته أو المجلدات الأب المفحوصة.
- البداية كانت dirty: تسعة ملفات معدلة وملف اختبار غير متتبع؛ ليست من أعمال التدقيق.

الملفات المعدلة سابقًا، تحت app/src/main/java/com/ahmadmol/enmath/:

1. core/design/Components.kt.
2. core/design/components/teacher/ProductivityComponents.kt.
3. features/localgems/LocalGemsScreens.kt.
4. features/profile/ProductProfileScreens.kt.
5. features/reference/CleanBottomBar.kt.
6. features/student/StudentHomeBeforeApproved.kt.
7. features/student/StudentHomeV2.kt.
8. features/student/StudentLearningScreens.kt.
9. features/teacher/TeacherWorkspace.kt.

الملف غير المتتبع: app/src/test/java/com/ahmadmol/enmath/BottomNavigationLayoutTest.kt.

لم يُشغّل Gradle أو lint أو اختبار JVM/Instrumentation في هذا التدقيق، ولم يُستخدم Emulator أو adb أو تثبيت APK أو تغيير إعداد هاتف. قراءات Git اقتصرت على status/branch/rev-parse؛ لا commit/push/merge/reset/clean/checkout/stash. لا تعديل لـ Burhan؛ الفيديو قُرئ فقط.

### 2.2 الصور الاثنتا عشرة

كل الصور تحت <LOCAL_USER_RESOURCE> رموز R01–R12 لهذا التقرير، وليست Screen IDs الأصلية. الموضع «أعلى/وسط/أسفل، عمود» يُعد من يسار الصورة بصريًا لتجنب أرقام اللوحات المشوهة.

| الرمز | اسم الملف الفعلي | ما فُحص وكيف استُخدم |
|---|---|---|
| R01 | ChatGPT Image Oct 13, 2026, 12_08_40 PM-5.png | Dark مشترك: الحساب والإعدادات واللغة والإشعارات والمظهر والخروج وحالات النظام. 1672×941؛ الصف الثالث مقصوص في الأصل، فلا تُستنتج منه شاشة كاملة. |
| R02 | ChatGPT Image Oct 13, 2026, 12_08_39 PM-4.png | Dark المعلم؛ فحص المكونات المشتركة فقط. الإنتاجية والتصحيح خارج نطاق الطالب. |
| R03 | ChatGPT Image Oct 13, 2026, 12_08_37 PM-3.png | Dark أدوات الطالب: الحل والسجل والرسم والتقدم والواجبات ونتائجها والبحث والمحفوظات والإشعارات. |
| R04 | ChatGPT Image Oct 13, 2026, 12_08_36 PM-2.png | Dark الكتب والموضوعات والدروس والتدريب والتلميح والصحيح/الخطأ والنتيجة والمتابعة. |
| R05 | ChatGPT Image Oct 13, 2026, 12_08_34 PM-1.png | Dark البداية والتعريف واختيار الدور والدخول والتسجيل و OTP والاستعادة وحدود الضيف. |
| R06 | EN.MATH Arabic Education App UI Showcase.png | Dark المرجع الأساسي للرئيسية: أعلى عمود 3؛ منهج وموضوع ودرس وتمرين وتقدم وحساب، ومعلم/حالات مشتركة في الأسفل. |
| R07 | Arabic Math App UI Screenboard.png | Light البداية والضيف والحالات والإعدادات والخروج؛ إنشاء الشعبة ونشر الواجب ومشاركة المورد للمعلم. |
| R08 | Arabic Student and Teacher App UI Showcase.png | Light واجب الطالب وإرفاقه والتسليم والنتيجة وملاحظات المعلم والإشعارات أعلى؛ المعلم أسفل. |
| R09 | EN.MATH Arabic Educational App Screens.png | Light المسح والكتابة اليدوية والسجل وتفاصيل الحل والاستكشاف والإنجازات والدعم؛ المعلم أسفل. |
| R10 | EN.MATH Arabic App UI Flow Showcase.png | Light المشترك والحل والسجل والرسم والمكتبة والبحث والمفضلة والملاحظات والتمارين والتقدم والإعدادات. |
| R11 | EN.MATH Arabic Math App UI Collage.png | Light بدائل الدخول والتسجيل والكتاب والدرس والنتيجة والحل والواجبات؛ المعلم في أجزاء الصف السفلي. |
| R12 | Arabic Math Learning App UI Showcase.png | Light المرجع الأساسي للرئيسية أعلى 3؛ للمنهج أعلى 4؛ للموضوع أعلى 5؛ درس وتمرين وحل وتقدم وحساب أسفلًا. |

R01–R05 ذات 1672×941، R06/R11 ذات 1448×1086، والبقية 1536×1024. فحص الصور نوعي للتكوين والهرمية، وليس قياس dp/sp أو contrast من montage. لا ملف خط أصلي أو تصميم أبعاد مطلقة بالمرفقات. النصوص والأرقام والصيغ داخل الصور أمثلة وليست بيانات منتج أو ضمان صحة علمية.

### 2.3 الفيديو وطريقة فحصه

- الأصل: <LOCAL_VIDEO_REFERENCE>
- الحجم: 43,846,719 بايت.
- SHA-256: b1d058bb22151a1404bfb1c07ba870d437c06bff793fea5448c4d00bbd0a08d9.
- المدة المقروءة: **95.2563 ثانية**؛ الإطارات المفكوكة **864×1920 px**.
- استُخدم استخراج إطارات بدل تشغيل مباشر داخل أداة العرض: مكتبات FFmpeg المحلية الموجودة مع OBS، عبر Python/ctypes، دون تثبيت حزمة. نجحت أيضًا قراءة المدة عبر Windows Media؛ محاولات thumbnail غير الناجحة لم تستخدم دليلًا.
- فُحصت **96 عينة مرتبة**: أول إطار عند/بعد كل ثانية من 00:00 إلى 01:35، في أربع لوحات اتصال. وكُبّرت عينات 00:00 و 00:12 و 00:44 و 01:27 لقراءة التفاصيل.
- الإطارات من التسجيل الحقيقي، وعُرضت من الذاكرة دون إنشاء ملفات صور أو تعديل الأصل؛ ليست screenshots اصطناعية.
- timestamps إزاحة عن بداية الفيديو، وليست ساعة الهاتف. التصغير قد يخفي النص؛ لا يثبت قصًا في التطبيق وحده.
- يغطي الفحص امتداد التسجيل كاملًا بعينات تمثيلية؛ ليس مشاهدة كل إطار أو تحليل صوت. لا قياس FPS/jank أو latency من عينات ثانية واحدة؛ التفاعلات الأقصر قد تفوت.
- لم يُثبت طراز الهاتف أو density أو fontScale أو hash الـ APK؛ لا ضمان أن binary التسجيل يساوي كل التعديلات المحلية الحالية.

| Timestamp مثبت | المشهد |
|---|---|
| 00:00،00:01 | Home Light ثم أسفلها: الهدف والمتابعة والأدوات والإتقان والواجب والنشاط والمراجعة المقفلة |
| 00:04 | الفكرة الأولى من درس النهايات التفاعلي؛ لا إتمام |
| 00:08 | سؤال خيارات في تدريب النهايات، وزر تحقق معطل قبل الإدخال |
| 00:10،00:21،00:24 | المحلل، حقل الإدخال والأمثلة ثم نهاية القائمة |
| 00:12 | التقدم:3/24 و 12%، صفر تمارين، دقة 65%،315 دقيقة، أعمدة أسبوع |
| 00:16 | تفاصيل واجب مراجعة الاشتقاق وزر البدء والأسئلة |
| 00:26،00:40 | الدراسة وموضوع النهايات المميز والكتابان |
| 00:30 | الكتالوج الكامل study/catalog |
| 00:32،00:37 | تفاصيل الكتاب الأول ثم الثاني |
| 00:44–00:47 | المختبر Light، الرسم والمنحنيات والتحكم |
| 00:48–00:50 | الحساب Light وروابطه، وتبديل الدور التجريبي عند التمرير |
| 00:54،00:59،01:01 | المحفظة الفارغة ثم المتجر المقفل |
| 01:06،01:08،01:10 | إعدادات Light ثم اختيار Dark |
| 01:12،01:13،01:16 | الدراسة والرئيسية والمختبر Dark |
| 01:18–01:24 | إعدادات Dark وأسفلها |
| 01:25،01:27 | الحساب ثم المحفظة Dark |
| 01:30–01:35 | عودة إلى Home Dark؛ آخر عينة 01:35 |

غير ظاهر ميدانيًا: Splash/Onboarding/Auth، Guest، تفاصيل الموضوع، إكمال درس، إجابة حتى النتيجة، الحل المفصل والسجل، إرسال واجب ونتيجته، البحث والمكتبة والإشعارات المفتوحة، ربح/صرف Gems، مراجعة مؤهلة، إعادة تشغيل العملية، English، IME ظاهر، TalkBack، وأحجام خط متعددة. عدم الظهور لا يعني غياب القدرة من الكود.

### 2.4 الوثائق وترتيب المراجع

قُرئت README و DESIGN_SYSTEM_REPORT و LEGACY_UI_AUDIT و SUPPORTING_STATES_REPORT و QA_RESULTS، وتقارير docs/reference و docs/clean-ui و docs/role-experience و docs/s01-v2 و docs/approved-references-2026-10-13 و docs/visual-polish، و docs/LOCAL_GEMS.md و docs/ROUTES.md وخرائط CSV.

1. الكود النشط دليل التنفيذ الحالي؛ الفيديو دليل الـ APK المسجل. لا تُنسب فروق نسخ غير مثبتة إلى مصدر محدد يقينًا.
2. R12/R06 زوج Home الأساسي كما يحدد S01_COMPARISON.md؛ Light للتكوين و Dark للأسطح والألوان في الشجرة نفسها.
3. تقرير visual-polish الأحدث يصف توحيد الثيم، ويؤكده ResourcePackPresentation(enabled=true). قول تقرير قديم إن Home وحدها مهاجرة لا يصف الوضع الحالي.
4. الوثائق تسجل PARTIAL و awaiting approval؛ لا إثبات اعتماد بشري نهائي لكل الشاشات.
5. نتائج QA السابقة تاريخية ولم تُعد هنا؛ محاكي سابق ليس قبول هاتف حقيقي.
6. bar الحالي 78dp+insets، بينما بعض الوثائق تذكر 80 أو 76dp؛ خرائط تسمي ReferenceSolver/Graph/Profile رغم presenters الحالية؛ AUD-20.
7. حزمة الموارد المستخرجة الأصلية لم تظهر في المسار المفحوص؛ لم يُدّع إعادة فحص ZIP غير متاح. الاعتماد على الصور والوثائق والكود المتاح.

### 2.5 سجل أدلة المصدر

كل Cxx اختصار للملف الحقيقي أدناه، وتستخدمه الجداول والـ Backlog. الأسطر تخص حالة العمل عند التدقيق.

| الرمز | الملف |
|---|---|
| C01 | [ENMathApp.kt](../../app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt#L41) — shell والعناوين والـ guards والتنقل |
| C02 | [Routes.kt](../../app/src/main/java/com/ahmadmol/enmath/core/navigation/Routes.kt#L3) |
| C03 | [ProductGraphs.kt](../../app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt#L33) |
| C04 | [StudentHomeV2.kt](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt#L37) |
| C05 | [StudentLearningScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/student/StudentLearningScreens.kt#L119) |
| C06 | [CleanBottomBar.kt](../../app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt#L24) |
| C07 | [Components.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/Components.kt#L34) |
| C08 | [ResourceBaseline.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/tokens/ResourceBaseline.kt#L15)؛ [Theme.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/Theme.kt#L166) |
| C09 | [CourseScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt#L15) |
| C10 | [InteractiveLimitsScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt#L104)؛ [LessonBlocks.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LessonBlocks.kt#L58) |
| C11 | [LimitsPracticeScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt#L22)؛ [PracticeScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/practice/PracticeScreens.kt#L58) |
| C12 | [WorkspaceScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/solver/WorkspaceScreens.kt#L74)؛ [SolverViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/features/solver/SolverViewModel.kt#L7) |
| C13 | [ExploreScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/explore/ExploreScreen.kt#L24)؛ [GraphEngine.kt](../../app/src/main/java/com/ahmadmol/enmath/features/explore/GraphEngine.kt#L10) |
| C14 | [ProductProgressScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/progress/ProductProgressScreen.kt#L13) |
| C15 | [ProductProfileScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt#L24) |
| C16 | [LocalGemsScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt#L35) |
| C17 | [AppViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt#L16) |
| C18 | [LearningRepository.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt#L7)؛ [LocalDemoRepository.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt#L11) |
| C19 | [ProductRepository.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt#L12) |
| C20 | [ProductSeed.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductSeed.kt#L278)؛ [DemoContent.kt](../../app/src/main/java/com/ahmadmol/enmath/core/data/DemoContent.kt#L148) |
| C21 | [AuthViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/features/auth/AuthViewModel.kt#L9)؛ [CleanLoginScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/auth/CleanLoginScreen.kt#L18)؛ [AuthScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/auth/AuthScreens.kt#L17) |
| C22 | [AssignmentScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/assignments/AssignmentScreens.kt#L24) |
| C23 | [LibraryScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/library/LibraryScreen.kt#L17)؛ [SearchScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/search/SearchScreen.kt#L13)؛ [NotificationsScreen.kt](../../app/src/main/java/com/ahmadmol/enmath/features/notifications/NotificationsScreen.kt#L9) |
| C24 | [LocalGemsPolicy.kt](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsPolicy.kt#L26)؛ [LocalLearningGems.kt](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalLearningGems.kt#L10) |
| C25 | [LocalGemsCoordinator.kt](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/LocalGemsCoordinator.kt#L15)؛ [SQLiteLocalGems.kt](../../app/src/main/java/com/ahmadmol/enmath/core/localgems/SQLiteLocalGems.kt#L12) |
| C26 | [MathRender.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/MathRender.kt#L30)؛ [ProductComponents.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/ProductComponents.kt#L36) |
| C27 | [BottomNavigationLayoutTest.kt](../../app/src/test/java/com/ahmadmol/enmath/BottomNavigationLayoutTest.kt#L9)؛ [QuizViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt#L8)؛ [InteractiveLessonViewModel.kt](../../app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLessonViewModel.kt#L7) |
| C28 | [ExperienceComponents.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/components/common/ExperienceComponents.kt#L17)؛ [InteractiveComponents.kt](../../app/src/main/java/com/ahmadmol/enmath/core/design/components/student/InteractiveComponents.kt#L24) |
| C29 | [OnboardingScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/onboarding/OnboardingScreens.kt#L39)؛ [MainActivity.kt](../../app/src/main/java/com/ahmadmol/enmath/MainActivity.kt#L9) |

## 3. Screen-by-Screen Comparison

الحالات: MATCHED لجزء مثبت مطابق، PARTIAL لتنفيذ موجود بفروق، MISSING لعنصر أو قدرة غير منفذة، BROKEN لسلوك خاطئ مثبت، NOT VERIFIED عند نقص الأدلة، و NOT APPLICABLE لما يقع خارج نطاق الطالب أو الخدمة الحالية. PARTIAL لا تعني قبولًا وظيفيًا كاملًا.

### 3.1 الشاشات الاثنتا عشرة المقارنة بصريًا

| Screen ID / Route | المرجع و Timestamp | الحالة والفروق البصرية | الربط الوظيفي والتوصية والأولوية |
|---|---|---|---|
| S01 / home | R12 أعلى 3 و R06 أعلى 3؛ 00:00،01:13 | PARTIAL: ترتيب الهدف ثم المتابعة ثم ثلاث أدوات ثم التقدم موجود. هيدر عام إضافي، و Gems بدل avatar، وواجب ونشاط أسفل المرجع. | C04/C17/C19؛ المتابعة من lastLessonId والتدريب من practiceId. أدوات Practice/Solver/Progress قرار سابق موثق؛ عدم استبدال Progress بـ Lab ليس Bug. AUD-01/06/16/17/21؛ P1 للبيانات و P3 للمسافات. |
| S02 / study | R12 أعلى 4، و R04 أعلى 1 بديل؛ 00:26،01:12 | PARTIAL: موضوع نهايات مميز وكتابان بدل قائمة موضوعات وتبويبات منهج/مساري/مفضلة. | C05/C03؛ topic/limits و book/id وجهات حقيقية. البحث العام في الهيدر. «مساري» وظيفة مقترحة، لا وظيفة مكسورة. وضّح الفرق عن catalog؛ P2. |
| X-CAT / study/catalog | R04 أعلى 1؛ 00:30 | PARTIAL: كتابان وزر متابعة وروابط واجب/مكتبة/Lab؛ هيدر EN.MATH وشريط سفلي مخفي. | C09:15/C03:71؛ وجهة فعلية إضافية على S02. AUD-07/20؛ P2. إخفاء الشريط في العمق مقبول إن اتسق؛ لا تضف فلاتر بلا بيانات. |
| S03 / book/{id} | R11 أعلى 3 و R04 أعلى 2–3؛ 00:32،00:37 | PARTIAL: artwork واسم ونسبة وزر متابعة وموضوعات؛ لا تبويبات نبذة/موضوعات، وزخرفة أقل. | C09:40؛ التقدم من completed والمتابعة لأول درس غير مكتمل. الكتابان حالتان لشاشة واحدة. عدم نسخ 3D أو نسب 86/40 مقبول؛ صقل الهرمية دون أرقام مختلقة. |
| S05 / lesson/{id}: limits-practice | R12 أسفل 1 و R04 أعلى 6؛ 00:04 | PARTIAL: فكرة أولى وعداد وزر متابعة؛ المرجع يبرز رسمًا أو مثالًا كبيرًا. | C10/C27؛ بلوكات وأسئلة و checkpoint. الرسم في خطوة لاحقة، وليس MISSING لأنه غائب عن أول إطار. الإتمام والحفظ والمكافأة غير متحققة ميدانيًا. |
| S06 / practice/{id} | R12 أسفل 2 و R04 أسفل 1؛ 00:08 | PARTIAL: مسألة وخيارات وتلميح وتحقق؛ لا مؤقت ظاهر في العينة. | C11/C27؛ إدخال وتفسير ونتيجة في الكود، لا إجابة مكتملة في التسجيل. مؤقت اللوحة ليس عقد امتحان. AUD-18؛ اختبر خطأ→إعادة→نتيجة. |
| S08 / solver | R12 أسفل 3 و R10 وسط 1 و R06 وسط 2؛ 00:10،00:24 | PARTIAL: modes وحقل وأفعال مسح/حذف/تراجع وحل وأمثلة. السجل و Lab أسفل قائمة طويلة، وصيغ حرفية. | C12/C26؛ DemoSolverEngine محدود، لا AI/CAS عام. OCR محاكاة معلنة؛ الحل والسجل لم يُفتحا. AUD-14؛ الميكروفون والكتابة الحرة اقتراحان لا عطلان. |
| S11 / explore | R10 وسط 4 و R03 أعلى 3؛ 00:44،01:16 | PARTIAL: رسم وثلاث منحنيات وإظهار/حذف وتكبير وجدول؛ لا x/y أو تدريج رقمي أو palette المرجع. | C13؛ منحنيات محسوبة ضمن grammar محدود. AUD-10/11/18؛ P2. لا تُنسخ تسميات رياضية خاطئة من الصور. |
| S12 / progress | R12 أسفل 4 و R10 أسفل 5 و R03 أعلى 4؛ 00:12 | PARTIAL: إكمال وحلقة ومؤشرات وأسبوع وإتقان؛ لا تبويبات أو catalog إنجازات. صفر تمارين مع 65% و 315 دقيقة. | C14/C19/C20؛ بيانات محلية مع seed/fallback، وليست دليل نشاط الحساب الفعلي. AUD-01/06/09/15. completion مختلف عن mastery؛ P1 للهوية و P2 للإيضاح. |
| S14 / assignments/{id} | R08 أعلى 1 و R03 أسفل 1؛ 00:16 | PARTIAL: عنوان ووصف وموعد وبدء وأسئلة؛ لا بطاقة معلم أو PDF أو مرفقات كالمرجع. | C22؛ قواعد محلية للمحاولات والموعد. Work لم يظهر. AUD-01/05/14. المرفقات قدرة مستقبلية، لا زر وهمي مطلوب. |
| S19 / profile | R12 أسفل 5 و R10 أعلى 8 و R01 أعلى 1؛ 00:48،01:25 | PARTIAL: initials وهوية وبريد ونسبة و Gems وروابط؛ لا rank/crown أو تحرير ملف وملاحظات. بنية الثيمين واحدة. | C15؛ user متغير والتعلم مشترك. اسم student لا يثبت عزل الحساب. تبديل الدور ظاهر في Debug، وحصره بـ Debug في C17. AUD-01/06/22. |
| S20 / settings | R10 أسفل 7 و R07 أسفل 3 و R01 أعلى 3–6؛ 01:06،01:10،01:18 | PARTIAL؛ التبديل إلى Dark مرئي. ثلاثة مظاهر ولغتان وتنبيه وتذكير ودعم وخصوصية؛ لا صفحة لكل اختيار. | C15:143/C18؛ الحفظ من الكود، وإعادة التشغيل غير متحققة. حجم خط/accent picker/حذف حساب ليست قدرات قائمة؛ لا إنشاء وهمي. AUD-11/22. |

MATCHED على مستوى أجزاء محددة: وجهات الهاتف الخمس وترتيبها الحالي وربطها في C02 و RefBottomNav، اتجاه الواجهة العربية، حقل الرياضيات LTR في العينة المقروءة، وبنية الإعدادات بين الثيمين. Home العائم تغيير محلي حالي؛ لا تفترض أنه معيار جميع اللوحات.

### 3.2 وجهات ذات مرجع لم تظهر في التسجيل

كل صف هنا NOT VERIFIED بصريًا؛ المقارنة مكتبية، وليست اختبار هاتف.

| ID / Route | المرجع والتنفيذ من الكود | الفروق والقرار |
|---|---|---|
| E01 / splash | R12 أعلى 1 و R05 أعلى 1؛ C29 | logo وموجات وتأخير 900ms؛ الرسم مختلف. تحقق من cold start و insets؛ لا قياس latency هنا. |
| E02 / onboarding | R07 أعلى 1–3 و R05 أعلى 2–4؛ C29 | ثلاث صفحات pager و skip و next؛ لا ست صفحات من دمج البدائل. font2 و RTL مطلوبان. |
| E03 / started | R07 أعلى 4–5 و R05 أعلى 5؛ C29/C01 | Student/Teacher/Guest؛ شاشة الضيف والـ gate حالتان، ولا دور ولي أمر جديد. |
| E04 / login | R10 أعلى 3 و R05 أعلى 6؛ C21 | PARTIAL للنموذج، MISSING للمصادقة الإنتاجية. إفصاح Demo موجود؛ AUD-02 P1 للحساب الفعلي. |
| E05 / account-choice | R10 أعلى 2 و R05 أسفل 1؛ C21 | دوران فقط؛ Parent غير مطلوب حاليًا. |
| E06 / register-student | R10 أعلى 4 و R05 أسفل 2؛ C21 | نموذج وتأكيد وشروط بلا خادم؛ لا social buttons وهمية. AUD-02. |
| E08 / verify | R10 أعلى 5 و R05 أسفل 4؛ C21 | حقل ستة أرقام LTR بدل ست خلايا اختلاف مقبول؛ OTP123456، لا تسليم بريد. AUD-02. |
| E09 / forgot | R10 أعلى 6 و R05 أسفل 5؛ C21 | تحقق بريد ثم Demo verify، دون إرسال رابط فعلي. AUD-02. |
| E10 / new-password | R10 أعلى 7 و R05 أسفل 6؛ C21 | validate/match/clearSecrets، لا password store. AUD-02. |
| S04 / topic/{id} | R12 أعلى 5 و R04 أعلى 4–5؛ C05:165/C09:100 | limits pilot مقابل قائمة عامة؛ لا إثبات تكافؤ بصري للنوعين. توحيد تدريجي، P2. |
| S07 / practice/{id}/result | R11 أعلى 5 و R04 أسفل 5؛ C11 | lastAttempt و accuracy و mastery و retry؛ قبول ربطها بالحساب بعد AUD-01. |
| S09 / solver/solution/{id} | R11 أسفل 1 و R03 أعلى 1؛ C12/C26 | إجابة وطرق وخطوات وحفظ وتعديل؛ fallback أمين للصيغ الغامضة. AUD-14؛ لا CAS كامل. |
| S10 / solver/history | R10 وسط 3 و R03 أعلى 2؛ C12 | بحث وتصنيف وحذف ومسح محلي؛ عزل الحساب AUD-01. |
| S13 / assignments | R11 أسفل 2 و R03 أعلى 5–7؛ C22 | القادمة/المكتملة/المتأخرة filters، وليست ثلاث شاشات؛ عضوية ahmed ثابتة. AUD-01. |
| S15 / assignments/{id}/result | R08 أعلى 5–6 و R03 أسفل 5؛ C22 | درجات وإجابات و feedback محلية؛ لا manual grading أو server. AUD-01/05. |
| S16 / library | R10 وسط 5 و R03 أسفل 7؛ C23 | saved lessons/problems و teacher resources؛ لا PDF download service. AUD-23. |
| S17 / search | R10 وسط 6 و R03 أسفل 6؛ C23 | بحث عربي مطبع محلي في topic/lesson/rule/question؛ لا remote search. تبويبات التصنيف تحسين اختياري. |
| S18 / notifications | R08 أعلى 7 و R03 أسفل 8؛ C23 | local/read/links؛ لا push. ظهور badge لا يثبت فتح الشاشة. |

### 3.3 الإضافات والحالات

| ID / Route أو حالة | الدليل | الحكم |
|---|---|---|
| G01 / gems/wallet | 00:54،00:59،01:27؛ C16/C24/C25 | PARTIAL للواجهة؛ NOT APPLICABLE لمطابقة صورة: لا wallet/store مرجعية مطابقة. الرصيد والمقتنيات والسجل والمتجر والثيمان مرئية؛ earn/debit/restart غير متحققة. |
| G02 / gems/review | C16:277/C24:59 | NOT VERIFIED؛ أسئلة ونتيجة موجودة، ولم تُفتح مراجعة مؤهلة. AUD-12/13. |
| X-SCAN / solver/scan | R09 أعلى 1؛ C12 | NOT VERIFIED بصريًا؛ PARTIAL لمحاكاة 2x+4=10. real camera/OCR: MISSING ومفصح عنه. لا BROKEN لغياب قدرة غير مدعومة أصلًا. |
| X-WORK / assignments/{id}/work | R08 أعلى 2–4؛ C03/C11 | NOT VERIFIED؛ أسئلة وتأكيد إرسال، upload غير منفذ، ولا success route منفصلة. AUD-05/18. |
| نجاح/خطأ/تلميح | R04 أسفل 2–4؛ C10/C11/C28 | حالات داخلية لا تضاعف عدد الشاشات؛ معظمها لم يظهر. |
| مفضلة/إنجازات/دعم | R09/R10؛ C14/C15/C23 | PARTIAL بالكود: محفوظات في Library، milestones في Progress، و help dialog؛ لا صفحات وهمية. |
| ملاحظات/كتابة حرة/إرفاق/حذف حساب/تغيير كلمة مرور موثّق | R07–R10 والكود وتقارير Supporting | MISSING كقدرات مستقلة؛ اقتراحات مستقبلية، لا عيوب عرض مؤكدة. |
| network/server errors | R01/R07؛ تطبيق محلي | NOT APPLICABLE كفشل backend حقيقي؛ لا API قائم. لا retry وهمي بدل خطأ SQLite. |
| teacher-only panels | R02 وأجزاء R06–R11 | NOT APPLICABLE؛ لا اعتماد UI/QA للمعلم هنا. |

العد المكتبي:12+18+4 وجهات إضافية=34. بقية الصفوف حالات وقدرات وليست وجهات إضافية. register-teacher خارج العد.

## 4. Design System Audit

### 4.1 تعارض البدائل والمرجع المقترح

| المجال | بدائل الصور | القرار المقترح وسببه |
|---|---|---|
| Home | هدف ثم CTA وأدوات في R12/R06، مقابل crown/XP وكثافة أعلى في غيرها | زوج R12/R06 مرجع الترتيب المعتمد في intake؛ Gems إضافة حقيقية، والواجب والنشاط أسفلًا. |
| Navigation | Home/Learn/Solve/Progress/Profile أو Books/Practice/Library | وجهات المنتج الحالية هي عقد التنقل؛ لا تستبدل Lab بـ Progress لتقليد لوحة. أصلح الهندسة ثم أقر الشكل. |
| Study | موضوعات R12 وكتب R04 وتفاصيل كتاب R11 | study مدخل، و catalog عرض كامل بتسميات واضحة؛ لا شاشتان متنافستان. |
| Lesson | فيديو R11 واختيار تفاعلي R12 و graph/tabsR04 | استخدم native interactive القائم؛ لا play يوحي بفيديو غير موجود. |
| Dark | glow/cyan كثيف R01–R05، مقابل R06 أهدأ | token roles الحالية أساس، مع composition واحدة؛ لا خلفية collage. |
| Profile/Gems | crown/rank/points في الصور | لا Gems=XP=Mastery؛ وضح مصدر ومعنى كل قيمة. |

### 4.2 الألوان والخط والمسافات

- C08 Light: background #F7F9FC، surface #FFFFFF، primary #175CD3، onSurface #0F1F38. Dark الفعال: background #0B1322، surface #0D2235، primary #56B4FF، secondary #61D7BB، outlineVariant #205374. بعض الوثائق تذكر accent آخر؛ الكود الحالي هو الدليل.
- C06 يستثني bar بألوان #111221 و#0084FF وألوان inactive ثابتة خارج بقية tokens. يفسر ذلك اختلاف سطح Dark، ولا يثبت فشل contrast.
- IBM Plex Arabic و Inter محليان. typography الفعال: bodyLarge16/27sp، bodyMedium14/24، bodySmall12/20، titleMedium16/26، headlineSmall22/32، labelSmall11/16. لا خط أصلي موثق في المونتاج.
- bar يستخدم 11sp و maxLines1 بلا lineHeight محلي؛ تقدير 1.25× ليس قياس Compose؛ AUD-04.
- phoneInset16dp، gutter12، card16، sectionGap24، touch48، CTA52، input56، radius8/12/16/20/28. لا تُقاس هذه القيم من pixels اللوحات.
- Home و ExperiencePage حتى 720dp، ScreenPage العام حتى 1040، و forms520. اختلاف العرض على الأجهزة الكبيرة يحتاج قرارًا بحسب القراءة؛ لا توحيد اعتباطي.
- Home يجمع gutter و padding للعناوين، فينتج إيقاع متفاوت قبل الأقسام؛ AUD-21.
- لم يُنفذ قياس contrast لكل بكسل. أرقام QA القديمة لا تغطي bar الحالي أو TalkBack.

### 4.3 المكونات والهرمية والوصول

| المكون | الأساسي والزخرفي | التوصية الدقيقة |
|---|---|---|
| TopAppBar/PageHeading | المكان والإجراءات أساسية؛ logo مساعد | metadata للعنوان والرجوع والإجراءات؛ لا تكرار brand في صفحة عميقة. AUD-07. |
| Goal/Continue | فعل التعلم التالي؛ حلقة الهدف مساندة | هرمية Home جيدة؛ نسبة مقروءة ووصف موحد، لا استبدالها بـ trophy. |
| Action wells | ثلاث وظائف؛ pastel يميزها | اختبر ثلاثة أعمدة عند font2؛ لا تثبت ارتفاع نص متغير. |
| Book/Topic/Lesson rows | عنوان وحالة وتقدم وفعل | أعد استخدام CourseCard/TopicCard/LessonCard؛ completion مميز عن mastery. |
| Math surface | المعادلة أساسية | LTR و scroll موجودان؛ fallback أمين دون تغيير صيغة أو حساب. AUD-14. |
| Graph | دليل كمي، لا زخرفة | تدريج x/y وملخص وجدول ودقة density؛ AUD-10/11. |
| Tabs/chips | اختيار حالة | ChipRow بحد 48dp و scroll أفقي؛ افحص selected semantics وبداية RTL. |
| Empty/Error | سبب وفعل تعافٍ | variant سياقي و action slot؛ AUD-23. |
| Gems | رصيد وفتح إثراء | unavailable/zero/error متميزة؛ badge الصغير يحتاج وصفًا؛ AUD-11/17. |
| Bottom nav | تنقل متكرر وحالة تحديد | Tab semantics موجودة؛ افحص حدود الدائرة والتسميات الطويلة؛ AUD-04/19. |

معظم icons24dp؛ أزرار الهيدر 48dp ودوائرها 40dp وأيقوناتها 20dp. حجم الأيقونة ليس حجم هدف اللمس. badge Home لا يحدد حد 48dp صريحًا؛ يعتمد على القياس وتوسعة Material، فلا يُثبت نجاحه أو فشله من الفيديو وحده. الأيقونة الزخرفية بوصف null مقبولة إذا يشرح عنوان الهدف وظيفته؛ Diamond ورقم وحدهما يحتاجان AUD-11.

العربية تستعمل LocalLayoutDirection=Rtl، والبريد و OTP و MathRender باتجاه LTR. DestinationRow يستخدم ChevronRight ثابتًا دون AutoMirrored؛ AUD-22. كسور النتائج و Progress ليست كلها داخل LTR صريح؛ اختبر 3/24 و 17/20 مع TalkBack دون ادعاء انعكاسها في التسجيل.

## 5. Student User Journey Audit

| المرحلة | الدليل | التقييم والخطوة |
|---|---|---|
| تشغيل/تعريف/دور | C01/C29؛ غير مسجل | لا قياس cold start؛ اختبر أول مرة وجلسة سابقة دون مسح البيانات. |
| دخول/إنشاء/استعادة | C21؛ غير مسجل | Demo صريح، دون account server أو mail؛ حسم حدود المنتج AUD-02. |
| Home→استئناف | 00:00→00:04؛ C04 | CTA تفتح lastLessonId الموجود، وليست recommendation engine. |
| Study→Book→Topic→Lesson | Study/Book ظاهران، Topic لا | الربط في C03؛ limits مختلفة عن باقي الموضوعات؛ قبول النوعين. |
| تعلم/تفاعل/تحقق/إتمام | بداية فقط؛ C10/C27 | interactive يشترط جوابًا صحيحًا. generic Lesson يسمح بالإتمام دون اجتياز checkpoint؛ سلوك قديم، وليس تغييرًا مأذونًا لقواعد Mastery. وضح completion مقابل verification. |
| تدريب/تلميح/إعادة/نتيجة | سؤال 00:08؛ C11 | formative practice، وليست امتحان أول محاولة. إعادة الإجابة بعد feedback جزء من السلوك؛ لا تصنفها خللًا بلا عقد امتحان. |
| نتيجة→تقدم | C17/C19؛ غير مكتمل بالفيديو | آخر تدريب وأفضل quiz حسب القواعد؛ الحساب الفعلي غير مثبت بسبب AUD-01/06. |
| محلل→خطوات/سجل/حفظ | input/examples؛ C12 | القدرة المحدودة معلنة؛ لا إثبات نتيجة أو حفظ بالتسجيل. |
| مختبر→تحكم/جدول | رسم وتحكم؛ C13 | فعلي لكنه بلا تدريج كمي؛ لا قرار بإضافة engine جديد. |
| واجب→تسليم/نتيجة | تفاصيل فقط؛ C22/C03 | return/result والهوية AUD-01/05؛ المرفقات غير مدعومة. |
| تقدم→Gems→متجر | wallet/store مرئيان | Gems تتبع البريد والتعلم مشترك. رصيد 0 مع تقدم ليس Bug لأن المكافآت غير رجعية؛ اشرح AUD-17. |
| إعدادات→Dark→رجوع | 01:06–01:35 | التبديل مرئي؛ restart و English و IME غير متحققة. |
| تبويبات/Back/Home | عودة متكررة؛ C01 | يثبت العودة لصفحات مألوفة، لا استعادة كل scroll/answer؛ AUD-18. |

## 6. Functional & Technical Findings

### 6.1 الحالة والبيانات

تدفق المصدر الحقيقي: LocalDemoRepository(enmath_demo) → LearningState/StateFlow → AppViewModel → ProductGraphs → Composables.

completeLesson يحدّث completed/last؛ recordPractice يضيف Attempt/Activity؛ LearningRules تشتق stats/mastery. إحصاءات Home ليست عدادات مستقلة، لكن مصادرها مشتركة وتجريبية:

- user يتغير، و completed/product_v1/last بلا namespace للحساب. Home/Assignments/Library تستعمل ahmed؛ AUD-01 مؤكد.
- fallback accuracy=.65 عند صفر محاولات، ونشاط seed وأسماء صف/مستوى ثابتة ليست سجلات مدرسة أو حساب؛ AUD-06.
- إتمام الدرس يضيف عشر دقائق ثابتة؛ stats.minutes ليس وقت تعلم مقاسًا، وهو جزء من AUD-06.
- JSON في SharedPreferences عبر apply، وقاعدة Gems منفصلة؛ لا transaction واحدة عبرهما. لا دليل على فقد حدث، لكن اختبار الانقطاع بينهما مطلوب.
- SavedStateHandle يساعد recreation واستعادة Android، ولا يضمن draft بعد إزالة الوجهة أو force-stop. المختبر يحفظ functions/zoom بـ rememberSaveable و pan بـ remember فقط؛ ليس حفظ جلسة دائمًا.

### 6.2 Compose و WindowInsets والقياسات

- MainActivity تستعمل enableEdgeToEdge، و Material TopAppBar. Scaffold يمرر padding إلى Row ثم imePadding، و bar تضيف navigationBarsPadding. ترتيب معقول؛ لا status overlap أو محتوى مخفي خلف bar مثبت في المشاهد المقروءة.
- لا consumeWindowInsets(padding) صريح؛ تراكب IME/system padding احتمال اختبار على بعض الأجهزة، لا Bug مؤكد. لا تضف padding تعويضيًا ثابتًا لكل شاشة دون قياس.
- Surface و Row في C06 بارتفاع 68dp مع top10، فيصبح shell78dp+system inset؛ fillMaxHeight داخل item محكوم. **لا دليل على bar تملأ الشاشة كلها**.
- center.layout يخفض ارتفاعه 10dp ويضع child عند y=-10؛ يلزم تطابق hit/semantics مع الرسم؛ AUD-19.
- Home actions و theme choices تستخدم IntrinsicSize.Min/fillMaxHeight لتساوي الصف؛ لا حاجة مثبتة لإزالتها، بل لاختبار font2.
- ScreenPage/ExperiencePage قابلتان للتمرير والقوائم صغيرة حاليًا. لا nested vertical scroll غير محدود مثبت. التحويل الشامل إلى LazyColumn ليس إصلاحًا ثبتت لزومه.
- Graph بارتفاع 260dp لكن scale35f و stroke3f بالـ px؛ المجال وحجم العلامات يختلفان مع density؛ AUD-10.
- لا profiling أو traces؛ لا تشخيص recomposition مفرط أو jank من StateFlow أو لقطة انتقال.
- rail تبدأ عند 768dp و grid عند 600dp. explore يسقط على default profile label/icon في rail؛ AUD-08.

### 6.3 Local Gems

C24/C25: owner هو بريد Student بعد trim/lowercase، و null للضيف والمعلم والفارغ. attribution لمن بدأ النشاط، و baseline يستبعد السابق، ودليل الإتقان للحساب يأتي من lessons/assessments الخاصة به. generation/ticket يمنع عرض بيانات owner سابق، وهناك Mutex و Dispatchers.IO ومعاملة SQLite للخصم والفتح.

السياسة المقروءة دون تعديل: lesson10، mastery20، practice/assignment5، weekly15؛ سقف 40 يوميًا و 120 أسبوعيًا بتوقيتدمشق؛ المكرر لا يدفع، وإعادة فتح المقتنى مجانية. هذه قراءة كود، وليست اختبار ربح وصرف على الهاتف.

الأساس أقوى من mock رقمي، لكن البريد هوية محلية غير موثقة بسبب AUD-02. لم تُقرأ قاعدة الهاتف أو تُنفذ عملية شراء واستعادة. **اختلاط التعلم لا يثبت اختلاط أرصدة Gems**؛ التخزين الأخير owner-scoped في المصدر.

weekly تشترط خمسة checkpoints مميزة من دروس verified ومكتملة؛ لكل درس هنا checkpoint واحد، فلا ينتج درسان خمسة أسئلة. نص الأهلية و remember(owner) يحتاجان AUD-12/13؛ لا تغيّر threshold أو مكافأة لمعالجة النص.

### 6.4 القديم مقابل النشط

الشاشات النشطة: StudentLearningHome في StudentHomeV2، StudentStudy، BookDetailsScreen، StudentLimitsTopic/TopicLessonsScreen، InteractiveLimitsScreen/LessonScreen، LimitsPracticeScreen/PracticeScreen، SolverWorkspace، ExploreScreen، ProductProgressScreen، ProductProfileScreen.

StudentHomeBeforeApproved و LegacyStudentLearningHome و StudentHomeScreen و ReferenceHome/Study/Solver/Graph/Profile محفوظة وليست canonical student targets. RefBottomNav ما زال نشطًا ويستدعي CleanBottomBar؛ RefPalette أصبحت getters للثيم. لا تحذف ملفًا لمجرد اسمه Reference، ولا تفترض Dark إجباريًا.

الاختلافات النشطة الفعلية: ExperiencePage pilot لـ limits مقابل ScreenPage العام، و Home خاصة، وثوابت bar خارج بعض tokens. هذه أدق من وصف كل تجربة الطالب بأنها legacy.

## 7. Issues Backlog

التصنيف: V مرئي بالفيديو، C ثابت من الكود، R أثر أو سيناريو يحتاج استنساخًا، G فجوة قدرة موثقة وليست regression. S/M/L تعقيد نسبي وليس وعدًا زمنيًا. جميع معايير القبول أدناه عمل لاحق غير منفذ.

### AUD-01 — بيانات التعلم والواجبات لا تتبع الحساب — P1

- **Screen/Component:** Home/Progress/Assignments/Library/History؛ C.
- **Evidence/Root Cause:** C18.signIn:72 و logout:74 يبدلان user فقط؛ completed/product_v1 مشتركان. C19:50 default ahmed؛ C17:116/139 يسلم باسم ahmed؛ C04/C22/C23 تستعمل عضوية ثابتة.
- **Recommended Fix:** هوية طالب مستقرة وتخزين وعضوية وتسليم scoped؛ خطة ترحيل تحافظ على Demo ولا تنسخها لكل حساب؛ فصل Guest قبل الترحيل. لا تغيير أوزان Mastery أو سياسة Gems.
- **Files Likely Affected:** C17/C18/C19/C22/C23/C04/C05 و models/codec.
- **Acceptance Criteria:** A/B منفصلان في completed/attempts/submissions/saved؛ logout لا يحذف؛ Guest لا يعدل A؛ عودة A تستعيد تعلمه وجواهره بلا مكافأة رجعية.
- **Complexity/Verification:** L؛ مؤكد كود، ولم تُجرب مبادلة حسابين على الهاتف.

### AUD-02 — المصادقة Demo وليست حسابًا فعليًا — P1

- **Screen/Component:** login/register/verify/recovery؛ C/G.
- **Evidence/Root Cause:** C21:10 OTP123456؛ password بطول ثمانية فقط؛ login لا يتحقق من سجل حساب؛ newPassword يمسح السر. CleanLoginScreen تفصح عن Demo، ولا خادم حسابات.
- **Recommended Fix:** حسم نموذج محلي معلن أو مصادقة حقيقية بعقد وترحيل منفصلين. حتى الحسم لا وعد بإرسال email أو استعادة password حقيقية، ولا OAuth تجميلي.
- **Files Likely Affected:** C21/C01/C18 و identity adapter لاحق إذا أُقر.
- **Acceptance Criteria:** للنموذج إفصاح صحيح؛ للإنتاج credential/OTP/recovery/session متحققة قبل وصف الحساب بالفعلي.
- **Complexity/Verification:** L للإنتاج و S للإيضاح؛ فجوة معلنة مؤكدة، غير مرئية بالفيديو وليست regression.

### AUD-03 — JSON غير المقروء يتحول إلى Seed بصمت — P1

- **Screen/Component:** persistence؛ C/R.
- **Evidence/Root Cause:** C19.load:273–390 catch عام يعيد ProductSeed.seed؛ C18.update يحفظ المنتج الناتج عند تحديث لاحق؛ لا مسار خطأ أو استرداد raw.
- **Recommended Fix:** LoadResult للغياب والنجاح والخطأ؛ الاحتفاظ بالأصل المتعثر ومنع overwrite تلقائي، مع version migration وتعافٍ غير هدّام؛ لا مساس بقاعدة Gems.
- **Files Likely Affected:** C19/C18/C17 ومكون خطأ محلي.
- **Acceptance Criteria:** invalidJSON أو unsupported version لا يصير Demo صامتًا ولا يُستبدل بتغيير الثيم؛ استعادة نسخة سليمة ممكنة. الاختبار على fixture معزول فقط.
- **Complexity/Verification:** M؛ fallback مؤكد؛ فساد أو فقد فعلي بالتسجيل NOT VERIFIED.

### AUD-04 — bar ثابتة وتسميات سطر واحد؛ الاختبار لا يقيس Compose — P1

- **Screen/Component:** bottom navigation/font scale؛ C/R.
- **Evidence/Root Cause:** C06:96 height68dp؛ Text11sp/maxLines1/ellipsis:155/179؛ circle52dp. C27 يختبر 22+4+11×scale×1.25، لا خط Compose أو عرض label أو دائرة Home.
- **Recommended Fix:** ارتفاع محكوم مشتق من المحتوى و lineHeight؛ سطران أو بديل تسميات معتمد؛ فحص bar مع insets، دون اعتماد المعادلة التقريبية للقبول.
- **Files Likely Affected:** C06/C08/C27 واختبار layout مستهدف لاحقًا.
- **Acceptance Criteria:** 360/393dp و Arabic/English و font1/1.5/2 والثيمان؛ لا قص يفقد المعنى أو تداخل، target≥48dp، ولا تستهلك bar المحتوى؛ selected semantics صحيحة.
- **Complexity/Verification:** M؛ القيود مؤكدة؛ قص x1.5/x2 لم يُشاهد أو يُختبر هنا.

### AUD-05 — الانتقال للنتيجة حتى عند رفض تسليم الواجب — P1

- **Screen/Component:** assignment work→result؛ C/R.
- **Evidence/Root Cause:** C17:116 يرجع عند deadline/attempts/Guest؛ C03 callback ينتقل للنتيجة بعد submitAssignment دون success result.
- **Recommended Fix:** نتيجة typed مثل Submitted/DeadlinePassed/AttemptsExhausted؛ الانتقال بعد حفظ التسليم الجديد فقط، وعرض سبب الرفض دون تغيير قواعد الدرجات أو Gems.
- **Files Likely Affected:** C17/C03/C11/C22.
- **Acceptance Criteria:** تجاوز الموعد أثناء الحل لا يعرض نجاحًا أو نتيجة قديمة؛ المسودة لا تضيع صامتًا؛ الضغط المتكرر لا يكرر التسليم.
- **Complexity/Verification:** M؛ callback مؤكد؛ سيناريو عبور الموعد غير منفذ، والفيديو تفاصيل فقط 00:16.

### AUD-06 — إحصاءات Demo تبدو نشاطًا شخصيًا — P2

- **Screen/Component:** Home/Progress/Profile؛ V/C/G.
- **Evidence/Root Cause:** 00:12 صفر تمرين مع 65% و 315 دقيقة و 3/24. C20:278/415 seed accuracy/activity؛ C19.stats fallback عند total0؛ C17 يضيف عشر دقائق ثابتة للإتمام.
- **Recommended Fix:** توضيح Demo ومصدر المؤشر؛ «لا محاولات» بدل دقة شخصية بديلة؛ وصف الزمن التقديري؛ فصل seed عن الفعلي ضمن AUD-01 دون تغيير أوزان Mastery.
- **Files Likely Affected:** C14/C04/C15/C19/C20/C17.
- **Acceptance Criteria:** لا دقة seed تُنسب لمن لم يتدرب؛ completion/accuracy/mastery/time متميزة، دون أرقام من الصور.
- **Complexity/Verification:** M؛ القيم مرئية ومصدرها ثابت؛ لم يُدّع أن الحساب المسجل جديد.

### AUD-07 — هيدر EN.MATH لوجهات عميقة بلا metadata — P2

- **Screen/Component:** wallet/catalog/review؛ V/C.
- **Evidence/Root Cause:** 00:30 و 01:27؛ C01.topTitle:84 لا يعرف gems/wallet أو study/catalog أو gems/review فيعود EN.MATH؛ عنوان داخلي آخر.
- **Recommended Fix:** تعريف title/up/parent/actions/showBottom لكل route؛ «جواهري» و«الكتب/الكتالوج» و«مراجعة الأسبوع» بدل default brand.
- **Files Likely Affected:** C01/C02/C03/C16.
- **Acceptance Criteria:** العنوان يشرح المكان، والرجوع للمصدر، دون brand مكرر؛ الدخول من Home/Profile متسق.
- **Complexity/Verification:** S؛ wallet/catalog مرئيان؛ review كود فقط.

### AUD-08 — المختبر يأخذ تسمية وأيقونة الحساب في Rail — P2

- **Screen/Component:** wide student navigation؛ C.
- **Evidence/Root Cause:** C02.tabs فيها explore؛ C01.tabLabel:402 و tabIcon:416 بلا branch لها فتأخذ default Profile/Person.
- **Recommended Fix:** مشاركة route/label/icon بين bar و rail، وإضافة Lab mapping صريح.
- **Files Likely Affected:** C01/C02 و ReferenceDesign.RefBottomNav.
- **Acceptance Criteria:** ≥768dp تعرض خمس وجهات مميزة، والمختبر باسمه وأيقونته والتحديد المطابق.
- **Complexity/Verification:** S؛ مؤكد كود، ولم يُعرض wide بالفيديو.

### AUD-09 — يوم التعلم UTC مقابل سقوف Gems بتوقيت دمشق — P2

- **Screen/Component:** goal/streak/week؛ C/R.
- **Evidence/Root Cause:** C18.LearningState:56 و C19.stats:70 يقسمان epoch على 86400000؛ C24.day/week تستعمل Asia/Damascus.
- **Recommended Fix:** date provider وتعريف توقيت لعرض النشاط يتفق مع توقع المستخدم؛ لا تغيير توقيت أو caps Gems.
- **Files Likely Affected:** C18/C19/C14/C04.
- **Acceptance Criteria:** اليوم والسلسلة صحيحان قبل وبعد منتصف ليلدمشق؛ تغيير timezone له نتيجة موثقة؛ لا دفع مكرر.
- **Complexity/Verification:** M؛ اختلاف bucket مؤكد، وأثر midnight غير مرئي.

### AUD-10 — graph بلا تدريج وتسميات و scale بالـ px — P2

- **Screen/Component:** explore Canvas؛ V/C.
- **Evidence/Root Cause:** 00:44 المكبر بلا x/y أو أرقام؛ C13:148 scale35f×zoom و stroke بالـ px مقابل height260dp.
- **Recommended Fix:** محوران وتدريج و domain مقروء ودقة density بالمحرك الحالي؛ الإبقاء على القص وفواصل الدالة.
- **Files Likely Affected:** C13 و visual helpers إن فُصلت.
- **Acceptance Criteria:** قراءة نقطة أو جذر ومجال الرؤية على densities مختلفة، دون انعكاس رياضي في RTL أو خروج curve.
- **Complexity/Verification:** M؛ غياب labels مرئي و scale مؤكد؛ لا قياس خطأ منحنى أُجري.

### AUD-11 — أوصاف وصول ناقصة في badge والتحكم — P2

- **Screen/Component:** Home Gems/graph checkbox/Canvas/settings switch؛ C/R.
- **Evidence/Root Cause:** C04:95 تعرض Diamond ورقمًا دون وصف الوظيفة؛ C13 checkbox منفصلة عن اسم curve و Canvas بلا ملخص؛ C15 switch/text منفصلان. GemCounter الأكبر له وصف بالفعل.
- **Recommended Fix:** badge بوصف «جواهري، الرصيد…، فتح المحفظة»، و merged/toggleable rows للمنحنى والتنبيه، ووصف وجدول للرسم؛ دون تكرار الأيقونات الزخرفية.
- **Files Likely Affected:** C04/C13/C15/C16/C28.
- **Acceptance Criteria:** TalkBack يعلن الهدف والحالة واسم المنحنى؛ switch access يعمل؛ لا «محدد» مجهولة السياق.
- **Complexity/Verification:** M؛ نقص semantics مؤكد، ونطق TalkBack غير متحقق.

### AUD-12 — نص المراجعة لا يشرح خمسة دروس موثقة — P2

- **Screen/Component:** WeeklyGemCard؛ V/C.
- **Evidence/Root Cause:** أسفل Home00:01؛ C16:124 «درسين و٥ أسئلة مؤهلة»؛ C24.weekly:59 يأخذ checkpoint واحدًا لكل درس مميز ويطلب خمسة؛ البيانات الحالية تستلزم خمسة دروس.
- **Recommended Fix:** اشتقاق نص وعدّاد الأهلية من builder، مع شرح verified وعدم retroactive؛ لا خفض الشرط لمطابقة النص.
- **Files Likely Affected:** C16 و read model في C24.
- **Acceptance Criteria:** remaining count صحيح؛ حالة ready المعروضة تساوي ui.review؛ السياسة والمكافأة ثابتتان.
- **Complexity/Verification:** S؛ نص وكود مؤكدان، وحالة eligible لم تُعرض.

### AUD-13 — review يتجمد عبر remember(owner) — P2

- **Screen/Component:** gems/review؛ C/R.
- **Evidence/Root Cause:** C16:280 تلتقط ui.review مرة للـ owner؛ refresh لنفس الهوية لا يعيد remember. الدخول أثناء null أو تغير الأسبوع قد يُبقي واجهة غير متاحة أو قديمة.
- **Recommended Fix:** state حية أو snapshot بعد ready بمفتاح owner/week/review id؛ تمييز loading/unavailable/ready.
- **Files Likely Affected:** C16/C25.
- **Acceptance Criteria:** null→ready دون خروج؛ تغيير owner/week لا يعرض snapshot خاطئة؛ retry لا يكرر reward.
- **Complexity/Verification:** M؛ dependency الناقصة مؤكدة، والسيناريو غير منفذ.

### AUD-14 — تنسيق الرياضيات متفاوت و fallback حرفي — P2

- **Screen/Component:** solver examples/assignment preview/solution؛ V/C.
- **Evidence/Root Cause:** 00:16 و 00:24 صيغ d/dx و at وأقواس literal؛ C26.nativeEquationParts متحفظة ولا تحول جميع التراكيب؛ ليس دليل خطأ حساب.
- **Recommended Fix:** renderer contract موحد للأنواع المدعومة و fallback أمين؛ فصل at بوصف مناسب، و scroll للصيغ الطويلة و semantics كاملة.
- **Files Likely Affected:** C26/C12/C09/C22.
- **Acceptance Criteria:** حدود وكسور ومشتقات ومصفوفات متسقة عند RTL/font2؛ دون تغيير أولوية أو صيغة غامضة أو escape ظاهر.
- **Complexity/Verification:** M؛ literal مرئي؛ الحل التفصيلي/font2 غير متحققين. لا تعهد TeX/CAS كامل.

### AUD-15 — أعمدة الأسبوع بلا أيام أو وحدة ملاصقة — P2

- **Screen/Component:** Progress chart؛ V/C.
- **Evidence/Root Cause:** 00:12؛ C14 ترسم i+1 و n؛ C19.week تُرجع دقائق آخر سبعة أيام؛ 1..7 لا تعرّف التاريخ، و 20 لا تشرح الوحدة.
- **Recommended Fix:** يوم أو تاريخ محلي، ودقائق و today indicator و semantic group؛ القيم كما هي بعد AUD-09.
- **Files Likely Affected:** C14/C19.
- **Acceptance Criteria:** اليوم والترتيب والدقائق واضحة بالثيمين واللغتين و TalkBack.
- **Complexity/Verification:** S؛ مرئي وكود مؤكدان.

### AUD-16 — دعوة واجب شخصية تظهر للضيف ثم يُمنع — P2

- **Screen/Component:** Guest Home؛ C/R.
- **Evidence/Root Cause:** C04 assignment filter لا يفحص Guest؛ C03.ProtectedContent يحجب الوجهة؛ C18 تبقي product، فيظل واجب ahmed مؤهلًا حسب التاريخ.
- **Recommended Fix:** Guest selector يعرض استكشافًا ودعوة تسجيل بدل «واجبك القادم» لشخص آخر؛ الاحتفاظ بالـ guard.
- **Files Likely Affected:** C04/C03 و selectors في C18.
- **Acceptance Criteria:** لا يرى Guest واجبًا شخصيًا من A، ولا يعدل سجله أو يكسب على حسابه؛ دعوة التسجيل متسقة.
- **Complexity/Verification:** M ضمن AUD-01؛ مسار مشروط من الكود، و Guest غير مسجل.

### AUD-17 — المحفظة الفارغة لا تقود لنشاط مؤهل — P2

- **Screen/Component:** Gems wallet/store empty؛ V/C.
- **Evidence/Root Cause:** 00:54 و 01:27 «أكمل نشاطًا مؤهلًا» بلا CTA؛ 00:59 متجر مقفل؛ C16 لا تفصل للمسجل عدم retroactive وشروط 80%/verified في C24.
- **Recommended Fix:** «كيف أجمع الجواهر؟» من policy read model، ورابط نشاط موجود، وشرح new/verified/capped/already rewarded؛ دون وعد بمكافأة غير مستحقة.
- **Files Likely Affected:** C16/C24/C03.
- **Acceptance Criteria:** طالب برصيد 0 وتقدم قديم يفهم السبب ويصل لنشاط حقيقي؛ رسائل الأهلية واضحة دون تعديل المكافأة أو threshold.
- **Complexity/Verification:** M؛ الفراغ والمتجر مرئيان، و earn لم يُجرب.

### AUD-18 — استئناف المسودات خارج back stack غير مضمون — P2

- **Screen/Component:** practice/work/lesson/lab؛ C/R.
- **Evidence/Root Cause:** C27 state خاصة بالوجهة؛ C01 Up=popBackStack دون عقد draft؛ C13 pan=remember؛ لا persist draft في C18/C19.
- **Recommended Fix:** عقد واضح: exit confirmation أو draft scoped للحساب والمهمة مع save/restore؛ حفظ pan مع viewport إن اعتُمد استئناف Lab.
- **Files Likely Affected:** C01/C03/C27/C18/C13.
- **Acceptance Criteria:** input→Up/tab/recreation/reopen لها نتيجة واضحة؛ لا فقد صامت أو انتقال مسودة إلى B.
- **Complexity/Verification:** M؛ حدود التخزين مؤكدة، والفقد الفعلي في الفيديو غير مثبت.

### AUD-19 — أعلى دائرة Home خارج مستطيل العنصر المعلن للمس — P2

- **Screen/Component:** floating Home hit region؛ C/R.
- **Evidence/Root Cause:** C06:96 تبدأ Row بعد 10dp؛ selectable على item:108؛ child placement:129 عند-10dp. الرسم يمتد أعلى المستطيل المعلن، ولا clickable مستقل للدائرة.
- **Recommended Fix:** جعل حيز الرسم واللمس واحدًا في layout، أو target للدائرة بـ semantics واحدة؛ لا click handlers متداخلين.
- **Files Likely Affected:** C06/C27.
- **Acceptance Criteria:** لمس أعلى ووسط وحواف الدائرة و label يفتح Home مرة، و TalkBack bounds تشملها دون التقاط محتوى فوقها.
- **Complexity/Verification:** M؛ الهندسة مؤكدة؛ missed tap على الهاتف NOT VERIFIED، ولا قياس hit testing من الفيديو.

### AUD-20 — خرائط التصميم متقادمة عن الربط الحالي — P3

- **Screen/Component:** docs/reference inventory؛ C.
- **Evidence/Root Cause:** DESIGN_SYSTEM_REPORT تصف 22dp/76dp؛ C06 الحالية 68+10؛ navHeight80 غير مستخدمة هنا؛ docs/ROUTES تفتقد Gems/catalog، وبعض CSV تسمي Reference بدل presenter الحالية.
- **Recommended Fix:** بعد اعتماد الجولة اللاحقة تحديث current mapping و HEAD و deviations، وفصل QA التاريخية. لا تعديل لهذه الوثائق في Audit المقيد.
- **Files Likely Affected:** DESIGN_SYSTEM_REPORT.md؛ docs/ROUTES.md؛ docs/approved-references-2026-10-13/screen_reference_map.csv؛ docs/visual-polish/SCREEN_REVIEW.csv.
- **Acceptance Criteria:** كل route/presenter/current token موثق؛ لا PASS قديم كقبول هاتف حالي.
- **Complexity/Verification:** S؛ مؤكد وثائقيًا.

### AUD-21 — تفاوت إيقاع مسافات Home — P3

- **Screen/Component:** Home sections؛ V/C.
- **Evidence/Root Cause:** 00:00 قبل «تقدم فهمك» و«واجبك القادم»؛ C04 تجمع spacedBy(gutter) و padding(top=gutter)، ويتغير ارتفاع الأدوات بالنص.
- **Recommended Fix:** Section wrapper بـ sectionGap بين المجموعات و gutter داخلها؛ لا spacer لتعويض label طويل.
- **Files Likely Affected:** C04/C08.
- **Acceptance Criteria:** ترتيب الهدف و CTA والأدوات والتقدم واضح عند 360/393/font2؛ مسافات متسقة مع بقاء البيانات.
- **Complexity/Verification:** S؛ تحسين بصري وليس blocker وظيفيًا.

### AUD-22 — Chevron ثابت الاتجاه — P3

- **Screen/Component:** DestinationRow في profile/settings؛ V/C.
- **Evidence/Root Cause:** 00:48 و 01:18؛ C26:56 تستخدم Icons.Outlined.ChevronRight دون AutoMirrored، خلاف Back/Book.
- **Recommended Fix:** directional chevron حسب LayoutDirection و convention واحدة؛ لا تعكس math أو icons غير الاتجاهية.
- **Files Likely Affected:** C26.
- **Acceptance Criteria:** سهم فتح الوجهة يتبع RTL/LTR المعتمد بكل صف دون تغيير معنى اللمس.
- **Complexity/Verification:** S؛ ثابت ظاهر ومن الكود؛ convention المنتج تحتاج اعتمادًا.

### AUD-23 — EmptyState عامة بلا فعل سياقي — P3

- **Screen/Component:** library/search/missing content؛ C/G.
- **Evidence/Root Cause:** C07:373 أيقونة Search وعنوان ووصف بلا action slot؛ تستعملها C23/C09 للمحفوظات والبحث والمفقود. wallet منفصلة يغطيها AUD-17.
- **Recommended Fix:** semantic variant و action slot: استكشاف دروس للمحفوظات، ومسح filter للبحث، ورجوع للمنهج للمحتوى المفقود؛ لا retry شبكة وهمي.
- **Files Likely Affected:** C07/C23/C09.
- **Acceptance Criteria:** السبب واضح وفعل موجود يعمل، مقروء بالثيمين و TalkBack.
- **Complexity/Verification:** S؛ فجوة component مؤكدة، والحالات لم تُفتح بالفيديو.

## 8. Proposed Improvements

1. بيانات صادقة: Demo معلن أولًا، ثم حساب معزول عند إقراره. لا «دقتك» لقيمة fallback، أو «وقت دراسة» لرقم ثابت دون وصف؛ لا تغيير أوزان Mastery/Gems.
2. Shell بعقد وجهات: تعريف واحد للعنوان والأيقونة وال parent والإجراءات و bottom/rail؛ يعالج AUD-07/08 ويمنع mapping المكرر.
3. Navigation بقياس فعلي: إصلاح font/hit bounds قبل قرار الشكل. Home الأزرق الدائم يعطي بروزًا أقوى من التبويب المحدد، لكنه فرق حالي لا يثبت selected state خاطئة؛ اعرض البديل للمراجعة.
4. تعلم متدرج: تعميم block renderer على topic إضافي بعد اعتماد limits، مع status rows موحدة؛ لا composable منفصلة لكل درس، ولا video player وهمي.
5. رسم تعليمي: تدريج و domain وقيم و accessible text من المحرك الحالي؛ لا curve زخرفية لمطابقة لوحة.
6. Gems مفهومة: badge موصوف وسبب رصيد 0 وعدّاد أهلية؛ فصل المحفظة والمتجر والمقتنيات مع بقاء السياسة.
7. مكونات مستخرجة من الموجود: ScreenShell metadata، Section، LearningStatusRow، MathSurface، EligibilityNotice، ContextualEmptyState؛ لا Design System موازية.

لا يُوصى بدمج glowR01 و pastelR12 كمتطلب واحد، أو إضافة crown/XP/rank وملاحظات/PDF/OAuth ومراسلة غير عاملة لاستكمال montage.

## 9. Implementation Roadmap

كل التالي توصية لجولة مأذونة لاحقًا؛ لم يبدأ تنفيذه.

| المرحلة | الاعتماديات والنطاق | المخرج ومعيار الخروج |
|---|---|---|
| 1: سلامة الحالة و Shell/navigation | AUD-03/04/05/07/08/19؛ تحديد نسخة المصدر والـ APK؛ دون تغيير Mastery/Gems | typed load/submit results، عناوين و rail صحيحة، قياس ولمس bar. diff قابل للمراجعة و host checks ملائمة، ثم موافقة اختبار هاتف لنسخة محددة. هي الأولى لأنها البنية المشتركة قبل تعميم التصميم. |
| 2: هوية الطالب وحدود المنتج | قرار AUD-02 ثم AUD-01؛ ترحيل واسترجاع قبل الكتابة لبيانات موجودة | Demo معلن أو حساب فعلي بعزل وعقد هوية؛ AUD-06/16 بحسب القرار؛ لا وصف Student account ready قبلها. |
| 3: تفسير البيانات والوصول | AUD-09/10/11/12/13/14/15/17/18 بعد الهوية | تقويم متسق، رسم مقروء، semantics، أهلية Gems وعقد استئناف؛ القواعد ثابتة. |
| 4: صقل P3 | AUD-20/21/22/23 بعد اعتماد shell | mapping حديث، spacing واتجاه صفوف و empty actions؛ paired phone captures. |
| 5: قبول رحلة الطالب | Matrix أدناه بعد الموافقة وعلى هاتف حقيقي | دليل version/device/dp/density/font/language/theme؛ لا PASS من montage أو محاكي قديم. |

لا P0 مسجلة. هوية الطالب والمصادقة لا تُحل بتجميل Home؛ المرحلة 1 ترفع سلامة النموذج، والمرحلة 2 شرط حساب فعلي. فصل المرحلتين يمنع توسع إصلاح spacing إلى backend غير محدد.

## 10. Physical Device Acceptance Checklist

**حالة كل البنود: NOT RUN. التنفيذ بعد موافقة المستخدم وعلى هاتف Android حقيقي فقط.** لا تثبيت أو clear data أو تعديل font/density الآن. لا تشغّل instrumentation تاريخية تزرع seed أو تعيد تهيئة prefs على هاتف الطالب.

### 10.1 QA Matrix

| البعد | القيم المطلوبة لاحقًا |
|---|---|
| العرض | 360dp و 393dp وأكبر/نافذة حقيقية≥768dp إن أمكن؛ pixels الفيديو ليست dp |
| الخط | ×1،×1.5،×2، مع fontScale فعلية مسجلة |
| اللغة والاتجاه | Arabic RTL و English LTR |
| الثيم | Light و Dark |
| المستخدم | Student فعلي عند توفر المصادقة، وإلا Student Demo صريح؛ Guest؛ وحسابان A/B للعزل |
| system UI | gestures و 3-button إن أمكن؛ cutout/status/IME؛ portrait و landscape للرياضيات الطويلة |

ثلاثة عروض×ثلاثة خطوط×لغتين×ثيمين=36 إعدادًا لكل نوع مستخدم؛ للطالب والضيف 72 حالة shell. هذا حجم خطة وليس عدد اختبارات منفذة، ولا يعني تكرار كل flow كاملة 72 مرة. نفذ journeys كاملة في Arabic Light/Dark و English baseline، ثم المكونات المعرضة للخطر في البقية. أي عرض أو هاتف غير متاح يبقى NOT VERIFIED.

### 10.2 الأدلة وحفظ البيانات

- [ ] تحديد APK/hash/commit وحالة التعديلات، وتوثيق model/API/density/dp/fontScale/theme/language/navigation.
- [ ] حساب اختبار وإجراء استرجاع قبل persistence؛ لا corruption على بيانات حقيقية.
- [ ] تسجيل completed/attempts/submissions و Gems ledger/unlocks قبل وبعد، بإذن وصول مناسب ودون أسرار.
- [ ] لا fixtures تزرع seed أو clear prefs؛ أي تغيير جهاز يُحدد في طلب الموافقة.
- [ ] screenshots حقيقية paired بنفس viewport/scroll/data؛ لا iOS chrome كقياس Android.
- [ ] سجل steps/expected/actual/timestamp/build، مع فصل unsupported عن failure.

### 10.3 Shell والعرض واللمس

- [ ] status/topbar دون overlap؛ العنوان والإجراءات واضحة عند font2، والرجوع صحيح.
- [ ] كل nav target≥48dp؛ لمس أعلى ووسط وحواف Home و label، مع selected مفهومة واحدة.
- [ ] آخر المحتوى قابل للوصول فوق bar؛ لا IME double padding أو bar ممتدة للشاشة.
- [ ] Curriculum/Math Lab والعربية بلا ellipsis يفقد المعنى عند font1/1.5/2.
- [ ] rail≥768dp تعرض Lab باسمها وأيقونتها، دون حساب مكرر.
- [ ] Home goal/CTA/tools/mastery/assignment/recent/weekly مقروءة وقابلة للتمرير دون حذف بيانات.
- [ ] Topic pilot/general و book artwork/title و choice/numeric/expression بلا overflow عند 360dp/font2.
- [ ] Dark/Light فيهما العناصر والحالات نفسها، مع contrast مقاس للنص والأزرار الحالية.
- [ ] صفوف وأسهم RTL/LTR، و email/OTP/fractions/matrices/long math تحافظ على المعنى والترتيب.
- [ ] TalkBack يقرأ الرصيد والمنحنى و Switch والنتيجة والتحديد دون تكرار زخرفي.

### 10.4 الوظائف والاستئناف

- [ ] cold start/onboarding/skip/عودة جلسة و Student/Guest دون فقد.
- [ ] درس interactive: خطأ→تلميح→صحيح→checkpoint→إتمام؛ انعكاسه في Home/Book/Progress.
- [ ] معنى generic completion و verified واضح دون تغيير Mastery rules.
- [ ] practice بستة أسئلة مختلطة و feedback/retry/result/time؛ لا double Attempt بالضغط.
- [ ] Up/tab/recreation/force-stop/reopen أثناء draft لها نتيجة متوقعة ومعلنة.
- [ ] A→logout→B→Guest→A دون اختلاط تعلم أو واجب أو محفوظات أو مسودات؛Gems مستقلة.
- [ ] solver: دعم ورفض وصيغة طويلة و cursor/undo/keyboard/method/save/history/delete؛ scan محاكاة واضحة.
- [ ] graph: add/hide/delete/zoom/pan/table/domain/x/y؛ رفض syntax برسالة، والإيماءة لا تمنع التمرير.
- [ ] واجب work→confirm→حفظ→result→feedback؛ deadline أو نفاد المحاولات لا يعرض نجاحًا.
- [ ] library/search positive/empty/filter/no result وروابط صحيحة؛ notification read محفوظة.
- [ ] language/theme/reminder بعد restart؛ لا push/mail/OCR ادعاء بلا خدمة.

### 10.5 Gems والوقت والتخزين

- [ ] رصيد جديد 0 مع progress سابق دون مكافأة رجعية؛ loading/error/unavailable مختلفة عن zero.
- [ ] eligible جديد لمن بدأه؛ المكرر و Guest/Teacher وحساب آخر لا يكسب.
- [ ] caps40/120 بتوقيتدمشق و capped achievement محفوظ؛ لا تكرار الدفع غدًا.
- [ ] خمسة checkpoints موثقة مميزة تتيح review؛ loading→ready؛ ≥4/5 حسب القواعد الحالية.
- [ ] تكرار review أو تغيير owner/week لا يعرض snapshot قديمة ولا مكافأة مكررة.
- [ ] تأكيد شراء، و debit+unlock مرة؛ insufficient بلا خصم؛ إعادة الفتح مجانية و restart يحفظ.
- [ ] SQLite/JSON failure على fixtures معزولة فقط؛ لا silent seed أو حذف DB أو رصيد.
- [ ] منتصف ليل التعلم/Gems وتغير timezone والساعة له سلوك موثق دون تعديل caps.

## 11. Final Assessment

يمكن الاحتفاظ بهوية EN.MATH وزوج R12/R06 للرئيسية، والثيم والخطوط والبنية المحلية للتعلم وقواعد LearningRules/LocalGemsPolicy. التسجيل دليل على 13 واجهة مشاهدة، منها 12 مقابلة لمرجع، وعلى الثيمين والتنقل في الحالات الظاهرة فقط.

لا يمكن اعتماد حساب طالب حقيقي أو عزل التعلم، أو حالات فساد التخزين ورفض التسليم، أو bar عند font1.5/2 ولمس حافتها، أو wide Rail، أو earn/spend/restart لـ Gems. غياب production/OCR/PDF/notes ليس إذنًا لتنفيذ هذه القدرات تلقائيًا.

أهم خمس نتائج مؤكدة قابلة للتتبع:

1. AUD-01: تعلم وواجبات ومحفوظات مشتركة مع ahmed ثابت، رغم user متغيرة.
2. AUD-03: JSON غير المقروء يعود seed بصمت، وقد يُكتب فوق الأصل عند تحديث لاحق؛ لا فقد فعلي مدّعى.
3. AUD-07: wallet/catalog بهيدر EN.MATH بدل عنوان واضح؛00:30 و 01:27 والكود.
4. AUD-08: wide Lab تسقط على Profile label/icon من mapping الكود.
5. AUD-10: graph بلا تدريج أو تسميات، و scale بالـ px؛ يضعف الفهم الكمي.

أول مرحلة تنفيذ مقترحة: سلامة الحالة والتسليم و shell/navigation قبل تعميم الصقل؛ ثم هوية الطالب والترحيل شرطًا للحساب الفعلي. **لم يبدأ أي إصلاح.**

### سجل التحقق النهائي

- الملف الوحيد المنشأ لهذه المهمة هو هذا التقرير.
- لا تعديل Kotlin/XML/Gradle/assets أو اختبارات، والتعديلات المحلية السابقة محفوظة.
- التقطت SHA-256 لـ 143 ملفًا من المصدر والأدوات وإعدادات البناء قبل الكتابة؛ أعيدت مقارنتها بعد حفظ التقرير، ولم تتغير أي بصمة ولم يُضف ملف مصدر.
- لا Git مغير للحالة، ولا Emulator أو اختبار هاتف أو تثبيت APK أو حذف بيانات أو تعديل Burhan.
