# EN.MATH — Student Screen / Route / State Matrix

2026-10-25 · static Working Tree inventory. «موجود» = destination أو Composable مؤكد، لا اكتمال أو نجاح هاتف. جذر الملفات `app/src/main/java/com/ahmadmol/enmath/`. Routes الحالية من core/navigation/ENMathApp.kt وProductGraphs.kt؛ وجود Composable قديم أو TestTag مكرر لا يجعله شاشة نشطة ثانية.

## 1. مفتاح حالات التحقق

كل صف أدناه يحمل state profile يحدد **Loading / Empty / Offline / Error / Permission**، وعمود V لحالات Light/Dark، RTL/LTR، font ×2. `N/A` يعني لا عملية من ذلك النوع حاليًا؛ لا يعني أن عرضها اختُبر. `غياب` = حالة مطلوبة بلا تنفيذ مستقل مثبت. `جزئي` = فرع/validation موجود يحتاج اختبارًا. `محلي` = المصدر bundled/prefs لا شبكة؛ ليس نجاح pack offline أو cold-restart.

| Profile | Loading الحالي | Empty الحالي | Offline الحالي | Error الحالي | Permission الحالي | المطلوب إضافته |
|---|---|---|---|---|---|---|
| ENTRY | Splash/انتقال؛ لا network loading | N/A | UI محلي | validation جزئي حسب الصفحة | N/A | local identity/recovery/cloud loading/error إن اعتمد |
| CATALOG | غياب loading repo | جزئي/fallback demo | bundled محلي | غير مكتمل unknown-id/version | N/A | downloaded/missing/edition mismatch/error retry |
| LESSON | غياب content load | جزئي invalid content | bundled محلي | grading/feedback جزئي | N/A | block unsupported/resume/version pin/storage error |
| ASSESS | غياب data load | unknown/empty جزئي | bundled محلي | answer/rejection feedback جزئي | N/A | durable submission failure/owner changed/retry safely |
| LOCAL_LIST | لا async loading عام | empty/filter موجود أو جزئي حسب الشاشة | prefs/bundled محلي | غياب storage error UI عام | N/A | no-data ≠ corrupt-data، scoped indexes/version |
| SOLVER | لا network load | empty input/unsupported جزئي | exact demos محلية | invalid/unsupported response جزئي | N/A | capability/domain result، honest unsupported |
| LAB | لا network load | input/functions state | grammar محلي | unsupported input جزئي | N/A | math-safe domains/context/accessibility |
| GEMS | busy/error في ui coordinator | account/history/owned جزئي | SQLite/assets محلي | storage/insufficient موجود | N/A | price fix/event consistency/owner migration |
| SCAN_DEMO | N/A | N/A | simulation فقط | لا OCR pipeline | **لا طلب كاميرا حقيقي** | يبقى معلنًا Demo أو يؤجل؛ OCR خارج النطاق |
| FUTURE | مطلوب عند I/O | مطلوب | local/missing حالات مطلوبة | recover/retry مطلوب | فقط حيث يلزم، opt-in | لم تُنشأ الشاشات أو tags بعد |

V=**NV** في جميع الصفوف الحالية: دعم theme/language/بعض adaptive code موجود، لكن Light/Dark × Arabic/English ×1/×2/RTL-LTR وTalkBack/IME غير مقبولة بجهاز في هذه الجولة. لا تضع PASS بناء على اسم test أو تقرير سابق. Permission للدرس/المكتبة بملفات app-private N/A؛ الإشعارات النظامية المستقبلية تحتاج إذنًا عند انطباقه، مع استمرار التعلم عند الرفض.

## 2. كل destinations الطالب والدخول المسجلة فعليًا

| Route الحالي | Composable / ملف | TestTag root فعلي / محددات مهمة | الحالة مقابل الهدف | Profile / V | الاختبارات الموجودة ذات الصلة، غير مشغلة هنا |
|---|---|---|---|---|---|
| splash | SplashScreen؛ onboarding/OnboardingScreens.kt | screen-splash | موجود انتقال؛ جهاز غير مثبت | ENTRY/NV | AppFlowTest، StudentVideoRegressionTest |
| onboarding | OnboardingScreen؛ الملف نفسه | screen-onboarding | جزئي؛ لا اختيار stage مكتمل | ENTRY/NV | AppFlowTest/CleanExperienceTest |
| started | GetStartedScreen؛ الملف نفسه | screen-started | Guest/roles موجودة Demo؛ stage غائب | ENTRY/NV | RoleExperienceTest/AppFlowTest |
| login | LoginScreen في auth/AuthScreens.kt → CleanLoginScreen في auth/CleanLoginScreen.kt | screen-login | Demo local validation | ENTRY/NV | AppFlowTest/ProductRulesTest |
| account-choice | AccountChoiceScreen؛ auth/AuthScreens.kt | screen-account-choice | Demo يتضمن Teacher؛ future isolation | ENTRY/NV | RoleExperienceTest |
| register-student | RegistrationScreen؛ AuthScreens.kt | screen-registration | Demo لا تسجيل حقيقي | ENTRY/NV | AppFlowTest/ProductRulesTest |
| register-teacher | RegistrationScreen؛ AuthScreens.kt | screen-registration نفسه | موجود public؛ خارج نطاق الطالب، يحفظ | ENTRY/NV | RoleExperienceTest |
| verify | VerificationScreen؛ AuthScreens.kt | screen-verification | Demo code 123456 | ENTRY/NV | ProductRulesTest/AppFlowTest |
| forgot | ForgotPasswordScreen؛ AuthScreens.kt | screen-forgot | Demo لا إرسال استعادة | ENTRY/NV | AppFlowTest |
| new-password | NewPasswordScreen؛ AuthScreens.kt | screen-new-password | Demo لا تغيير حساب خادمي | ENTRY/NV | AppFlowTest |
| home | StudentLearningHome؛ student/StudentHomeV2.kt | screen-home؛ home-gems-badge، student-continue، daily-learning-goal، home-practice | جزئي/رحلة Bac ثابتة وصندوق غير durable | CATALOG/NV | StudentHomeV2Test/StudentVideoRegressionTest |
| study | StudentStudy→CurriculumScreen؛ student/CurriculumScreen.kt | screen-study؛ book-$bookId، topic-${data.topicId}، browse-all-lessons | جزئي/Seed وchips ثابتة | CATALOG/NV | CurriculumPresentationTest/StudentVideoRegressionTest |
| study/catalog | StudyScreen؛ courses/CourseScreens.kt | screen-study **مشترك مع study** | كتالوج Demo للكتب، ليس PDF Library | CATALOG/NV | ReferenceScreensTest/FullApplicationUiTest |
| book/{id} | BookDetailsScreen؛ CourseScreens.kt | screen-book | جزئي، محتوى Demo، تقدم completion | CATALOG/NV | CurriculumPresentationTest/FullApplicationUiTest |
| topic/{id} | StudentLimitsTopic؛ student/StudentLearningScreens.kt | screen-topic؛ lesson-${l.id}، topic-practice | يعمم بعض البيانات مع artwork/lesson النهايات؛ fallback إلى limits | CATALOG/NV | LessonSequenceTest/FullApplicationUiTest |
| lesson/{id} (limits-practice) | InteractiveLimitsScreen؛ courses/interactive/InteractiveLimitsScreen.kt | screen-interactive-lesson | تسلسل تفاعلي فعلي خاص بالنهايات، ليس general content engine | LESSON/NV | InteractiveLessonSessionTest/PremiumLessonUiTest |
| lesson/{id} (other ids) | LessonScreen؛ courses/CourseScreens.kt | screen-lesson | شرح/checkpoint/completion، Demo/partial generalization | LESSON/NV | LessonSequenceTest/FullApplicationUiTest |
| practice/{id} (limits-practice-set) | LimitsPracticeScreen؛ interactive/LimitsPracticeScreens.kt | screen-practice | Seed questions مع feedback وحماية finish UI | ASSESS/NV | LessonSequenceTest/PremiumLessonUiTest |
| practice/{id} (other ids / quizzes) | PracticeScreen؛ practice/PracticeScreens.kt | screen-practice نفسه | Quiz/Practice بنفس route، لا route quiz منفصل | ASSESS/NV | ProductRulesTest/StudentFirstFixBatchTest |
| practice/{id}/result | LimitsPracticeResult أو PracticeResultScreen؛ الملفان السابقان | screen-practice-result | نتيجة محلية، لا دليل إتقان مستقل | ASSESS/NV | FullApplicationUiTest/RoleExperienceTest |
| solver | SolverWorkspace؛ solver/WorkspaceScreens.kt | screen-solver | 7 أمثلة exact؛ إدخال ولوحة/Undo | SOLVER/NV | DemoLogicTest/StudentVideoRegressionTest |
| solver/scan | SolverScanScreen؛ WorkspaceScreens.kt | screen-scan | **Demo معلن**؛ لا صورة ولا OCR | SCAN_DEMO/NV | FullApplicationUiTest |
| solver/history | SolverHistoryScreen؛ WorkspaceScreens.kt | screen-history | قائمة محلية/بحث/filter/delete | LOCAL_LIST/NV | StudentCompletionRegressionTest/FullApplicationUiTest |
| solver/solution/{id} | ProductSolutionScreen؛ WorkspaceScreens.kt؛ graph يعيد DemoSolverEngine | screen-solution | خطوات curated، save، share future dialog، similar practice | SOLVER/NV | StudentVideoRegressionTest/FullApplicationUiTest |
| explore | ExploreScreen؛ explore/ExploreScreen.kt | screen-explore | grammar محدود، zoom/pan/grid/axes؛ no durable owner graph history | LAB/NV | StudentVideoRegressionTest/FinalStudentRegressionTest |
| progress | ProductProgressScreen؛ progress/ProductProgressScreen.kt | screen-progress | completion+mastery demo، accuracy seed fallback | LOCAL_LIST/NV | ProductRulesTest/CurriculumPresentationTest |
| assignments | AssignmentListScreen؛ assignments/AssignmentScreens.kt | screen-assignments | جزئي/local Teacher classroom dependency؛ Guest محجوب | LOCAL_LIST/NV | AssignmentSubmissionVerificationTest/RoleExperienceTest |
| assignments/{id} | AssignmentDetailsScreen؛ الملف نفسه | screen-assignment-details | local legacy assignment | ASSESS/NV | FullApplicationUiTest |
| assignments/{id}/work | PracticeScreen + rejection banner؛ ProductGraphs.kt | screen-practice؛ assignment-rejection-banner | owner reward guard، deadline/attempt/Guest rejection؛ no disk ack | ASSESS/NV | AssignmentSubmissionVerificationTest/StudentFirstFixBatchTest |
| assignments/{id}/result | AssignmentResultScreen؛ AssignmentScreens.kt | screen-assignment-result | local legacy submission | LOCAL_LIST/NV | RoleExperienceTest/FullApplicationUiTest |
| library | LibraryScreen؛ library/LibraryScreen.kt | screen-library؛ library-problem-${h.id}، resource-item-${r.id} | محفوظات وموارد صف؛ Guest محجوب، لا PDF/فيزياء عامة | LOCAL_LIST/NV | FullApplicationUiTest/RoleExperienceTest |
| search | SearchScreen؛ search/SearchScreen.kt | screen-search | بحث محلي/حفظ استعلام، no stage/pack index | LOCAL_LIST/NV | FinalStudentRegressionTest/StudentVideoRegressionTest |
| notifications | NotificationsScreen؛ notifications/NotificationsScreen.kt | screen-notifications | inbox Demo؛ mark-read محلي؛ لا push | LOCAL_LIST/NV | FullApplicationUiTest |
| profile | ProductProfileScreen؛ profile/ProductProfileScreens.kt | screen-profile؛ profile-gems، profile-library، open-settings، logout | تفضيلات وعداد/روابط؛ stage غير ممثلة | LOCAL_LIST/NV | RoleExperienceTest/FullApplicationUiTest |
| settings | SettingsScreen؛ الملف نفسه | screen-settings؛ theme-${mode.name}، language-${l.name} | ثيم/لغة محفوظة وتفضيل تنبيه فقط، لا scheduler | LOCAL_LIST/NV | StudentVideoRegressionTest/FullApplicationUiTest |
| gems/wallet | LocalGemsScreen؛ localgems/LocalGemsScreens.kt | screen-local-gems؛ local-gems-confirm، gems-reader-close، gems-original-content | busy/storage errors، history/store/owned/reader فرعية في route نفسه؛ Demo balance defect | GEMS/NV | LocalGemsCoordinatorTest/LocalGemsPolicyTest/GemsIntegrationReviewTest |
| gems/review | LocalWeeklyReviewScreen؛ الملف نفسه؛ PracticeScreen عند Ready | screen-weekly-status / screen-weekly-result / screen-practice | weekly reward source موجود، ينتظر policy قرار | GEMS+ASSESS/NV | WeeklyGemCardPresentationTest/LocalGemsPolicyTest |

35 destination registrations ذات صلة بالطالب/الدخول (10 عامة +23 studentGraph +2 Gems)، وعدد صفوف الجدول أكبر لتوضيح variants. `main` graph container و`teacher-main` graph container ليستا شاشتين ولا root tags. `Routes.Solution="solution"` ثابت/عنوان/select policy قديم، **غير مسجل بـcomposable** في ENMathApp/ProductGraphs؛ المسار الفعلي solver/solution/{id}. لا نحسبه شاشة عاملة.

### عدد ملفات العرض وإعادة الاستخدام

هناك 91 ملف Kotlin main في الجرد الحالي. تستند شاشات الطالب والدخول النشطة في الجدول إلى **19 ملف UI موجودًا**: OnboardingScreens، AuthScreens، CleanLoginScreen، StudentHomeV2، CurriculumScreen، StudentLearningScreens، CourseScreens، InteractiveLimitsScreen، LimitsPracticeScreens، PracticeScreens، WorkspaceScreens، ExploreScreen، ProductProgressScreen، AssignmentScreens، LibraryScreen، SearchScreen، NotificationsScreen، ProductProfileScreens، LocalGemsScreens. هذا عدد ملفات عرض، لا عدد Routes ولا مكونات Compose ولا تقديرًا بأن تحويلها تلقائي.

الـ19 مرشحة للحفظ وإعادة الاستخدام مع اختلاف نطاق التعديل: legacy assignments تعزل من رحلة الطالب المستقلة؛ auth يعاد تحديد صدقه؛ Home/Curriculum/topic/lesson/practice/progress/library تحتاج ربطًا ديناميكيًا/بيانات جديدًا؛ Solver/Lab/Gems تتطلب حواف منطق وسلامة؛ settings/search/inbox تحتاج توسيع حالات. لا ملف منها معتمد release-ready هنا. توجد 7 عائلات عرض جديدة أو توسعات جوهرية لا يغطيها route حالي: اختيار الهوية/المرحلة، المراجعة والمهارات، المخطط، التحفيز والشارات، القارئ/تفاصيل المورد، إدارة الحزم/المساحة، والمساعدة/الخصوصية/الإبلاغ. عدد ملفاتها النهائي يثبت بعد UX، ولا نفرض ملفًا أو Route لكل حالة.

## 3. overlays والمكونات المشتركة

AccountPrompt للزائر modal داخل ENMathApp؛ logout confirmation داخل ProductProfileScreen (dialog-confirm)؛ purchase confirmation داخل LocalGemsScreens (local-gems-confirm)؛ reader داخل wallet عند contentId؛ weekly busy/ready/unavailable/result داخل gems routes. Theme/language وfilter chips وkeypad وحوار share المستقبلي حالات مكوّنات، ليست Routes إضافية. المرفق الآلي آخر الملف يفهرس tag declarations مع موضعها، بما فيها interpolation.

RefBottomNav/CleanBottomBar tags `nav-*` على التبويبات؛ NavigationRail في ENMathApp يكوّن nav-$destination. studentOwnsHeader يحدد headers للـtopic/regular lesson/progress/history؛ showStudentNavigation يطلب visible entries كلها main tabs. لا تُعاد تسمية tags أو تغيير backstack ضمن مهمة التوثيق.

## 4. شاشات/حالات الهدف غير المنفذة

كل route هنا **PROPOSED/غير معتمد**؛ root test tags **TO_BE_DEFINED**؛ لا يُفهم أن المسار موجود. المجموعات تحافظ على IDs الوصفية في الخطة الأصلية وتتيح modal بدل route لكل حالة.

| مجموعة الخطة | surfaces المطلوبة | اقتراح تمثيل | الحالة / حالات يجب تصميمها | PRD/Backlog |
|---|---|---|---|---|
| Entry/identity | اختيار المرحلة، local learner chooser، account ثابت المرحلة، claim comparison، guest limits، account recovery حقيقي | entry/stage، accounts/choose، claim sheet | FUTURE؛ existing auth Demo لا يغطيها؛ loading/error/offline/ownership conflict | F01..06؛ P0/CLOUD |
| Courses/lessons | Subject catalog، book/unit availability، reviewed source، dynamic blocks، resume/download status | reuse study/book/topic/lesson، subject route عند الحاجة | FUTURE+reuse؛ stage9/Bac/version/coming soon/rights blocked | F07..12؛ DATA/LEARN |
| Review/mastery | دفتر أخطاء، تفاصيل خطأ، مراجعة جديدة، due list، skill evidence، completion vs mastery | review، review/{id}، skill/{id} | FUTURE؛ empty/due/deferred/no new question/history | F13/14؛ LEARN-03 |
| P01–P12 | empty welcome، create plan، period/mode، quick goal، goal/tasks، partial update، period goals، calendar/time، edit/defer/carry، period close، history | planner + goal/detail/add sheets | FUTURE؛ كل 9 modes، timezone/partial/undo/cancel/end period | F15..19؛ PLAN-01..03 |
| M01–M06 | motivation space، general/context message، badges list/detail، frequency settings | motivation + achievements/detail + settings | FUTURE؛ opt-out/no data/rest/no interrupt | F20/21؛ MOT-01 |
| T01–T09 | solver capabilities/history/similar، graph ranges/context | existing solver/explore مع states إضافية | PARTIAL؛ unsupported/domain/empty suggestions/owner restore؛ OCR مؤجل | F23/24؛ TOOLS |
| F01–F11 | physics free list، resource detail، PDF reader، last page/bookmarks، pack catalog، download center/progress/pause/resume/failure/repair، space/update/delete | library/resource/{id}/reader + downloads؛ أسماء مقترحة | FUTURE؛ NoSpace/offline missing/rights/version/pin/permission N/A للخاص | F25..29؛ PACK/LIB |
| U01–U10 | stage-aware profile/progress، Gems ledger، notification prefs، language/theme، storage، privacy، data/export/delete حين cloud، about/help | existing profile/settings + privacy/help/storage | PARTIAL/FUTURE؛ no cloud identity/deletion بدون backend | F22/31/32؛ AUX/CLOUD |
| S01–S05 | search types/history، content issue report، FAQ | reuse search + support/report/help | PARTIAL؛ normalized empty، offline draft مقابل sent، edition filters | F30/32؛ AUX-01 |

كل شاشة جديدة أو معاد استخدامها يجب أن ترث [QA-30/49](../QA/ENMATH_STUDENT_RELEASE_ACCEPTANCE_MATRIX.md) لـLight/Dark/RTL-LTR/×2، مع اختبار loading/empty/local offline/missing/error/retry وpermission/auth حيث يلزم. لا نضيف permission من غير حاجة وظيفية.

## 5. المسارات المحفوظة خارج نطاق تطوير الطالب

الجرد التالي يمنع إسقاط الكود القديم أو الخلط بينه وبين واجهات الطالب؛ كلها موجودة في teacherGraph، وليست backlog تطوير معلم. teacher reference route constants من TeacherReferenceNavigation.kt.

| Routes الحالية | الشاشة/tag root | الموقف |
|---|---|---|
| teacher، teacher/reference/home | TeacherHome / screen-teacher-home | محفوظة/legacy |
| teacher/reference/study، teacher/reference/study/catalog | StudyScreen / screen-study | مشترك محفوظ |
| teacher/reference/solver | TeacherSolverWorkspace | محفوظ؛ محدداته تفهرس أدناه |
| teacher/reference/lab | ExploreScreen / screen-explore | مشترك محفوظ |
| teacher/reference/book/{id} | BookDetailsScreen / screen-book | محفوظ |
| teacher/reference/topic/{id} | ReferencePage / screen-teacher-topic | محفوظ |
| teacher/reference/lesson/{id} | TeacherReferenceLesson / screen-teacher-lesson | محفوظ |
| teacher/classes | ClassesScreen / screen-classes | محفوظ |
| teacher/classes/{id} | TeacherClassDetails / screen-class-details | محفوظ |
| teacher/classes/{id}/students/{student} | TeacherStudentDetails / screen-student-details | محفوظ |
| teacher/assignments | TeacherAssignmentsScreen / screen-teacher-assignments | محفوظ |
| teacher/assignments/new | AssignmentBuilderScreen / screen-builder | محفوظ |
| teacher/assignments/{id} | TeacherResultsScreen / screen-teacher-results | محفوظ |
| teacher/assignments/{id}/responses/{student} | AssignmentResultScreen / screen-assignment-result | محفوظ |
| teacher/resources | TeacherResourcesScreen / screen-resources | محفوظ |
| teacher/profile | ProductProfileScreen / screen-profile | محفوظ |
| teacher/settings | SettingsScreen / screen-settings | محفوظ |
| teacher/notifications | NotificationsScreen / screen-notifications | محفوظ |

الملفات ReferenceHome/Study/Profile/Solver/Graph، LegacyStudentLearningHome، StudentHomeBeforeApproved، features/home/StudentHomeScreen وLegacy Teacher UIs فيها tags مكررة؛ لا دليل على ربطها بالرحلة النشطة بمجرد اسم screen-home. الجرد الآلي لا يثبت reachability؛ جدول route→call أعلاه هو المرجع.

## 6. ملحق جرد TestTag declarations من المصدر

جرد آلي بتاريخ 2026-10-25 لجميع ملفات main Kotlin، بما فيها shared/teacher/legacy. السطر نص المصدر نفسه؛ المتغيرات/interpolations ليست قيمًا ثابتة، ووجود التصريح لا يثبت reachability أو ظهور semantics على جهاز. مرجع الشاشة النشطة هو جدول routes أعلاه. تكرار tag على عقدة واحدة يحتاج فحص semantics، ولا يُفترض أن آخر tag يحتفظ بكل السابق.


### app/src/main/java/com/ahmadmol/enmath/core/design/Components.kt

```kotlin
255: AppCard(onClick = onClick, modifier = Modifier.testTag("book-${book.id}")) {
291: AppCard(onClick = onClick, modifier = Modifier.testTag("topic-${topic.id}")) {
458: .testTag("back")
484: .testTag("global-search")
508: .testTag("global-notifications")
```

### app/src/main/java/com/ahmadmol/enmath/core/design/components/common/ExperienceComponents.kt

```kotlin
22: Box(Modifier.fillMaxSize().testTag(tag), contentAlignment = Alignment.TopCenter) {
```

### app/src/main/java/com/ahmadmol/enmath/core/design/components/student/InteractiveComponents.kt

```kotlin
44: .testTag("$tagPrefix$option")
92: Modifier.fillMaxWidth().testTag(tag),
115: Modifier.fillMaxWidth().testTag(tag),
143: TextButton({ expanded = !expanded }, Modifier.testTag(tag)) {
163: modifier.fillMaxWidth().animateContentSize().testTag("answer-feedback"),
228: Column(Modifier.testTag(tag), verticalArrangement = Arrangement.spacedBy(8.dp)) {
```

### app/src/main/java/com/ahmadmol/enmath/core/design/components/teacher/ProductivityComponents.kt

```kotlin
89: ExperiencePanel(Modifier.testTag(tag)) {
110: AppCard(Modifier.testTag(tag), onClick = onOpen) {
151: Column(Modifier.testTag(tag), verticalArrangement = Arrangement.spacedBy(8.dp)) {
173: AppCard(Modifier.testTag(tag), onClick = onOpen) {
200: AppCard(Modifier.testTag(tag), onClick = onOpen) {
218: OutlinedButton(action, Modifier.testTag("teacher-action-$index")) { Text(title) }
239: modifier = Modifier.testTag("$prefix$index"),
```

### app/src/main/java/com/ahmadmol/enmath/core/design/MathRender.kt

```kotlin
39: Modifier.fillMaxWidth().testTag("math-render").semantics {
```

### app/src/main/java/com/ahmadmol/enmath/core/design/PremiumLearning.kt

```kotlin
86: if (onSearch != null) OutlinedIconButton(onSearch, Modifier.size(48.dp).testTag("global-search"), shape = buttonShape) {
89: OutlinedIconButton(onNotifications, Modifier.size(48.dp).testTag("global-notifications"), shape = buttonShape) {
100: OutlinedIconButton(onBack, Modifier.size(48.dp).testTag("back"), shape = buttonShape) {
```

### app/src/main/java/com/ahmadmol/enmath/core/design/ProductComponents.kt

```kotlin
75: AppTextField(value, onChange, label(TextKey.t_67b32d1cb9), Modifier.testTag("search-input"))
183: AppCard(onClick = onClick, modifier = Modifier.testTag("lesson-${lesson.id}")) {
223: AppCard(onClick = onClick, modifier = Modifier.testTag("assignment-${a.id}")) {
340: TextButton(onClick, Modifier.testTag("bookmark")) {
```

### app/src/main/java/com/ahmadmol/enmath/core/design/ReferenceDesignSystem.kt

```kotlin
146: if (onSearch != null) OutlinedIconButton(onSearch, Modifier.size(48.dp).testTag(searchTag)) {
149: if (onNotifications != null) OutlinedIconButton(onNotifications, Modifier.size(48.dp).testTag(notificationsTag)) {
158: if (onBack != null) OutlinedIconButton(onBack, Modifier.size(48.dp).testTag(backTag)) {
214: .testTag("search-input"),
244: .testTag("category-tab-${item.id}"),
310: modifier = Modifier.heightIn(min = if (LocalStudentExperience.current) 48.dp else 0.dp).testTag("chip-${item.id}")
```

### app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt

```kotlin
284: Modifier.testTag(
```

### app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt

```kotlin
290: .testTag("assignment-rejection-banner")
451: ReferencePage("screen-teacher-topic") {
```

### app/src/main/java/com/ahmadmol/enmath/features/assignments/AssignmentScreens.kt

```kotlin
45: ScreenPage(Modifier.testTag("screen-assignments")) {
247: ScreenPage(Modifier.testTag("screen-assignment-details")) {
307: modifier = Modifier.testTag("assignment-work")
321: modifier = Modifier.fillMaxWidth().testTag("assignment-question-$idx"),
368: ScreenPage(Modifier.testTag("screen-assignment-result")) {
422: Modifier.testTag("teacher-feedback"),
```

### app/src/main/java/com/ahmadmol/enmath/features/auth/AuthScreens.kt

```kotlin
48: .testTag("auth-error")
92: .testTag("select-${type.name}")
131: ScreenPage(Modifier.testTag("screen-account-choice"), true) {
153: modifier = Modifier.testTag("register-${role.name}"),
213: ScreenPage(Modifier.testTag("screen-registration"), true) {
245: Modifier.testTag("auth-name"),
251: Modifier.testTag("auth-email"),
259: Modifier.testTag("teacher-subject"),
265: Modifier.testTag("teacher-experience"),
274: Modifier.testTag("auth-password"),
281: Modifier.testTag("auth-confirm"),
294: modifier = Modifier.testTag("auth-terms"),
317: Modifier.testTag("registration-submit"),
347: ScreenPage(Modifier.testTag("screen-verification"), true) {
370: .testTag("auth-code"),
396: Modifier.testTag("verify-submit"),
444: ScreenPage(Modifier.testTag("screen-forgot"), true) {
454: Modifier.testTag("auth-email"),
464: Modifier.testTag("forgot-submit"),
474: ScreenPage(Modifier.testTag("screen-new-password"), true) {
485: Modifier.testTag("auth-password"),
492: Modifier.testTag("auth-confirm"),
503: Modifier.testTag("new-password-submit"),
```

### app/src/main/java/com/ahmadmol/enmath/features/auth/CleanLoginScreen.kt

```kotlin
28: ScreenPage(Modifier.testTag("screen-login"), narrow = true) {
65: Modifier.testTag("auth-email"),
72: Modifier.testTag("auth-password"),
78: modifier = Modifier.align(Alignment.End).testTag("forgot"),
88: Modifier.testTag("login-submit"),
107: TextButton(onClick = register, modifier = Modifier.testTag("register")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt

```kotlin
44: PremiumPage(Modifier.testTag("screen-study")) {
68: { onBook(book.id) }, Modifier.testTag("catalog-book-${book.id}"))
72: PremiumActionButton(tr("Continue learning", "متابعة الدراسة"), { onContinue(next.id) }, Modifier.testTag("catalog-continue"))
97: PremiumPage(Modifier.testTag("screen-book")) {
116: else tr("Continue this book", "متابعة هذا الكتاب"), { onLesson(next.id) }, Modifier.testTag("book-continue"), icon = Icons.Outlined.PlayArrow)
124: topic.symbol, chapter.completed, chapter.total, accent, { onTopic(topic.id) }, Modifier.testTag("book-topic-${topic.id}"))
247: .testTag("screen-lesson")
257: RefTopBar(tr("Lesson", "الدرس"), onBack = onBack, backTag = "nav-back")
277: backTag = "nav-back", searchTag = "header-search", notificationsTag = "header-notifications")
404: .testTag("lesson-bookmark"),
595: .testTag("checkpoint-input")
610: .testTag("checkpoint-verify")
730: .testTag("complete-lesson")
743: .testTag("complete-lesson")
776: .testTag("lesson-practice")
809: .testTag("lesson-next")
```

### app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt

```kotlin
65: LessonPage("screen-interactive-lesson") {
84: Modifier.testTag("block-${block.id}"),
134: Modifier.testTag("complete-lesson"),
141: Modifier.testTag("lesson-practice"),
176: TextButton({ vm.move(index - 1, blocks) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("lesson-previous")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LessonPresentation.kt

```kotlin
59: Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(tag))
63: modifier = Modifier.testTag(tag),
143: Surface(Modifier.fillMaxWidth().testTag(tag)
175: TextButton(onToggle, Modifier.heightIn(min = 48.dp).testTag(tag)
190: Surface(Modifier.fillMaxWidth().testTag("answer-feedback").semantics { liveRegion = LiveRegionMode.Polite },
208: modifier = Modifier.fillMaxWidth().testTag(tag),
```

### app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitExploration.kt

```kotlin
89: modifier = Modifier.testTag("limit-undefined"))
101: modifier = Modifier.weight(1f).testTag("limit-slider").semantics {
136: .testTag("limit-visual").semantics { contentDescription = summary }) {
```

### app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt

```kotlin
35: LessonPage("screen-practice") {
50: TextButton(vm::retry, Modifier.testTag("practice-retry-answer")) {
74: ScreenPage(Modifier.testTag("screen-practice-result")) {
120: Modifier.testTag("student-mastery-limits"),
146: Modifier.testTag("result-topic"),
151: Modifier.testTag("result-retry"),
```

### app/src/main/java/com/ahmadmol/enmath/features/explore/ExploreScreen.kt

```kotlin
141: ScreenPage(Modifier.testTag("screen-explore")) {
228: modifier = Modifier.testTag("graph-values-toggle")
365: AppTextField(input, onInputChange, "y = f(x)", Modifier.fillMaxWidth().testTag("graph-input"))
441: .testTag("graph-input"),
508: .testTag("graph-canvas")
803: .testTag("graph-function-$index")
932: .testTag("graph-control-2")
1016: .testTag("graph-control-0")
1034: .testTag("graph-control-1")
1059: OutlinedIconButton(onZoomOut, Modifier.size(48.dp).testTag("graph-control-0"), enabled = zoom > .25f) {
1063: OutlinedIconButton(onZoomIn, Modifier.size(48.dp).testTag("graph-control-1"), enabled = zoom < 10f) {
1069: OutlinedButton(onReset, Modifier.heightIn(min = 48.dp).testTag("graph-control-2")) { Text(tr("Reset view", "إعادة العرض")) }
```

### app/src/main/java/com/ahmadmol/enmath/features/home/StudentHomeScreen.kt

```kotlin
19: ScreenPage(Modifier.testTag("screen-home")) {
34: Modifier.testTag("continue-lesson"),
```

### app/src/main/java/com/ahmadmol/enmath/features/library/LibraryScreen.kt

```kotlin
38: ScreenPage(Modifier.testTag("screen-library")) {
119: modifier = Modifier.fillMaxWidth().testTag("lesson-${l.id}"),
265: Card(onClick = { onProblem(h.id) }, modifier = Modifier.fillMaxWidth().testTag("library-problem-${h.id}"),
312: modifier = Modifier.fillMaxWidth().testTag("resource-item-${r.id}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt

```kotlin
69: .testTag(tag)
166: .testTag("gems-weekly-card")
207: modifier = Modifier.testTag("gems-weekly-card"),
238: .testTag("gems-weekly-open")
258: PremiumPage(Modifier.testTag("screen-local-gems")) {
264: Button(onClick=onStudentSignIn,modifier=Modifier.heightIn(min=48.dp).testTag("gems-use-student-account")) { Text(text("دخول حساب الطالب", "Student sign in")) }
270: OutlinedButton(onClick=gems::closeContent,modifier=Modifier.heightIn(min=48.dp).testTag("gems-reader-close")) { Text(text("العودة إلى مقتنياتي","Back to my content")) }
273: SelectionContainer { Text(ui.content.orEmpty(),style=MaterialTheme.typography.bodyLarge,modifier=Modifier.testTag("gems-original-content")) }
319: modifier = Modifier.fillMaxWidth().testTag("local-gems-item-card-${item.id}"),
422: .testTag("local-gems-item-${item.id}")
442: modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("local-gems-owned-${item.id}")) { Text(if(ar) item.title.ar else item.title.en) }
483: modifier=Modifier.heightIn(min=48.dp).testTag("local-gems-confirm")) { Text(text("تأكيد الفتح","Confirm unlock")) }},
498: ScreenPage(Modifier.testTag("screen-weekly-result")) {
509: ScreenPage(Modifier.testTag("screen-weekly-status")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/notifications/NotificationsScreen.kt

```kotlin
37: ScreenPage(Modifier.testTag("screen-notifications")) {
43: modifier = Modifier.testTag("mark-all-read")
97: .testTag("notification-item-${note.id}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/onboarding/OnboardingScreens.kt

```kotlin
58: modifier = Modifier.fillMaxSize().testTag("screen-splash"),
120: .testTag("screen-onboarding")
145: modifier = Modifier.testTag("intro-skip"),
238: modifier = Modifier.testTag("intro-next"),
246: ScreenPage(Modifier.testTag("screen-started"), narrow = true) {
271: modifier = Modifier.testTag("role-student"),
324: modifier = Modifier.testTag("role-teacher"),
380: .testTag("guest"),
```

### app/src/main/java/com/ahmadmol/enmath/features/practice/PracticeScreens.kt

```kotlin
30: Modifier.fillMaxWidth().heightIn(min = Space.touch).testTag("answer-$option"),
51: Modifier.testTag("quiz-answer"),
82: LessonPage("screen-practice") {
129: Modifier.testTag("submit-confirm"),
149: ScreenPage(Modifier.testTag("screen-practice-result")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt

```kotlin
107: ScreenPage(Modifier.testTag("screen-profile")) {
131: testTag = "open-settings",
151: testTag = "profile-library",
181: testTag = "language-toggle",
207: testTag = "logout",
218: TextButton(onLogout, Modifier.testTag("dialog-confirm")) {
475: .testTag("profile-gems")
565: .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
675: .testTag("demo-switch-role")
746: ScreenPage(Modifier.testTag("screen-settings")) {
806: .testTag("theme-${mode.name}"),
897: .testTag("language-${l.name}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/progress/ProductProgressScreen.kt

```kotlin
55: ScreenPage(modifier = modifier.testTag("screen-progress")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt

```kotlin
129: .testTag("$tagPrefix${item.route}"),
193: .testTag("$tagPrefix${item.route}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/DemoRoleSwitch.kt

```kotlin
25: Modifier.heightIn(min = 48.dp).testTag("demo-switch-role"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceDesign.kt

```kotlin
82: .testTag(tag),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceGraph.kt

```kotlin
37: ReferencePage("screen-explore") {
66: .testTag("reference-graph")
183: .testTag("graph-control-$index"),
201: .testTag("graph-function-$i"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceHome.kt

```kotlin
21: ReferencePage("screen-home", top = 19.dp) {
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceProfile.kt

```kotlin
25: ReferencePage("screen-profile") {
152: Modifier.testTag("student-assignments"),
168: Modifier.testTag("open-settings"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceSolver.kt

```kotlin
33: ReferencePage("screen-solver") {
104: Modifier.weight(1f).testTag("method-algebra"),
116: Modifier.weight(1f).testTag("method-hopital"),
348: Modifier.fillMaxWidth().testTag("reference-math-input"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/ReferenceStudy.kt

```kotlin
16: ReferencePage("screen-study") {
27: Modifier.testTag("browse-all-lessons"),
38: Modifier.weight(1f).testTag("reference-book-1"),
44: Modifier.weight(1f).testTag("reference-book-2"),
```

### app/src/main/java/com/ahmadmol/enmath/features/reference/SessionExit.kt

```kotlin
15: Modifier.testTag("logout"),
36: modifier = Modifier.testTag("dialog-confirm"),
```

### app/src/main/java/com/ahmadmol/enmath/features/search/SearchScreen.kt

```kotlin
27: ScreenPage(Modifier.testTag("screen-search")) {
51: modifier = Modifier.testTag("save-search"),
78: modifier = Modifier.testTag("recent-search-$savedQuery"))
```

### app/src/main/java/com/ahmadmol/enmath/features/solver/WorkspaceScreens.kt

```kotlin
256: .testTag("solver-tab-$index"),
359: .testTag("solver-input"),
417: .testTag("solver-clear"),
451: .testTag("solver-delete"),
485: .testTag("solver-undo"),
533: .testTag("solve"),
688: .testTag("example-$index"),
814: modifier = Modifier.then(if (LocalStudentExperience.current) Modifier.heightIn(min = 48.dp) else Modifier).testTag("math-group-$index")
924: .testTag("key-$keyLabel"),
1017: ScreenPage(Modifier.testTag("screen-solver")) {
1184: ScreenPage(Modifier.testTag("screen-solution")) {
1307: PrimaryButton(label(TextKey.t_f762e78c7a), onEdit, Modifier.testTag("edit-problem"))
1327: ScreenPage(Modifier.testTag("screen-scan")) {
1490: .testTag("simulate-capture"),
1545: .testTag("simulate-upload"),
1609: ScreenPage(Modifier.testTag("screen-history")) {
1611: if (state.user?.type != AccountType.Teacher) Box(Modifier.testTag("history-back")) {
1613: } else TextButton(onClick = onBack, modifier = Modifier.testTag("history-back")) {
1684: modifier = Modifier.fillMaxWidth().testTag("history-item-${h.id}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/student/CurriculumScreen.kt

```kotlin
141: .testTag("screen-study")
581: .testTag("student-continue")
582: .testTag("study-limits"),
803: .testTag("book-$bookId"),
990: .testTag("topic-${data.topicId}"),
1050: .testTag("browse-all-lessons"),
```

### app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeBeforeApproved.kt

```kotlin
45: Modifier.fillMaxSize().testTag("screen-home"),
88: HomePanel(tinted = true, modifier = Modifier.testTag("home-next-lesson")) {
117: .testTag("student-continue"),
129: HomePanel(modifier = Modifier.testTag("daily-learning-goal")) {
174: Modifier.weight(1f).fillMaxHeight().testTag("home-practice"),
182: Modifier.weight(1f).fillMaxHeight().testTag("home-quick-solver"),
194: .testTag("home-upcoming-assignment"),
237: Modifier.heightIn(min = D.touch).testTag("home-all-progress"),
```

### app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt

```kotlin
285: .testTag("screen-home")
388: .testTag("home-practice")
400: .testTag("home-quick-solver")
412: .testTag("home-all-progress")
435: tag = "student-mastery-limits"
453: .testTag("home-upcoming-assignment"),
587: modifier = Modifier.testTag("home-gems-badge")
682: .testTag("daily-learning-goal"),
1156: modifier = Modifier.testTag("student-continue"),
1327: .testTag("discover-gems-banner"),
1422: .testTag("super-package-teaser"),
```

### app/src/main/java/com/ahmadmol/enmath/features/student/StudentLearningScreens.kt

```kotlin
58: ExperiencePage("screen-home") {
81: Modifier.testTag("student-continue"),
104: Modifier.testTag("home-practice"),
111: Modifier.testTag("home-quick-solver"),
116: Modifier.testTag("home-upcoming-assignment"),
185: .testTag("screen-topic")
196: backTag = "nav-back", searchTag = "header-search", notificationsTag = "header-notifications")
388: .testTag("start-interactive-limits"),
496: .testTag("lesson-${l.id}"),
622: .testTag("topic-practice"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/AssignmentBuilderScreen.kt

```kotlin
48: ScreenPage(Modifier.testTag("screen-builder")) {
91: Modifier.testTag("builder-title"),
97: Modifier.testTag("builder-description"),
125: Modifier.testTag("select-${q.id}"),
186: Modifier.testTag("builder-next"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/ClassScreens.kt

```kotlin
22: ScreenPage(Modifier.testTag("screen-classes")) {
27: Modifier.testTag("create-class"),
90: Modifier.testTag("class-name"),
101: modifier = Modifier.testTag("class-save"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherAssignmentScreens.kt

```kotlin
27: ScreenPage(Modifier.testTag("screen-teacher-assignments")) {
29: PrimaryButton(label(TextKey.t_4c5461762c), onCreate, Modifier.testTag("create-assignment"))
64: ScreenPage(Modifier.testTag("screen-teacher-results")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherDashboard.kt

```kotlin
18: ScreenPage(Modifier.testTag("screen-teacher")) {
42: Modifier.testTag("create-assignment"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherProductivityScreens.kt

```kotlin
50: ExperiencePage("screen-teacher-home", teacher = true) {
146: ExperiencePage("screen-class-details", teacher = true) {
217: TextButton({ tab = 1 }, Modifier.testTag("class-all-students")) {
247: Modifier.testTag("teacher-student-search"),
341: ExperiencePage("screen-student-details", teacher = true) {
409: Modifier.testTag("teacher-mastery-${t.id}"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherReferenceDashboard.kt

```kotlin
56: ReferencePage("screen-teacher-reference", top = 12.dp) {
117: Modifier.weight(1f).testTag("teacher-tab-classes"),
124: Modifier.weight(1f).testTag("teacher-tab-assignments"),
144: .testTag("teacher-create-assignment"),
153: Modifier.clickable { selected = index }.testTag("teacher-class-$index"),
195: Modifier.clickable { support = true }.testTag("teacher-support"),
215: RefCard(Modifier.testTag("teacher-assignments-preview")) {
228: Modifier.testTag("teacher-preview-create"),
261: modifier = Modifier.fillMaxWidth().testTag("teacher-assignment-title"),
268: .testTag("teacher-assignment-class-$i"),
285: modifier = Modifier.testTag("teacher-assignment-preview"),
318: TextButton(onClick = dismiss, modifier = Modifier.testTag("teacher-preview-dismiss")) {
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherReferenceNavigation.kt

```kotlin
42: ReferencePage("screen-teacher-lesson") {
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherResourcesScreen.kt

```kotlin
33: ScreenPage(Modifier.testTag("screen-resources")) {
40: Modifier.testTag("teacher-resource-study"),
46: Modifier.testTag("teacher-resource-solver"),
52: Modifier.testTag("teacher-resource-lab"),
96: Modifier.testTag("share-${r.id}"),
111: Modifier.testTag("resource-title"),
117: Modifier.testTag("resource-body"),
```

### app/src/main/java/com/ahmadmol/enmath/features/teacher/TeacherWorkspace.kt

```kotlin
21: ReferencePage("screen-teacher-reference") {
55: Modifier.testTag("teacher-create-assignment"),
61: Modifier.weight(1f).testTag("teacher-manage-classes"),
84: .testTag("teacher-class-$index")
107: Modifier.testTag("teacher-open-study"),
113: Modifier.testTag("teacher-open-solver"),
119: Modifier.testTag("teacher-open-lab"),
134: ReferencePage("screen-profile") {
149: Modifier.testTag("open-settings"),
```
