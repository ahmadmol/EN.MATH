# EN.MATH — Phase 1: مصفوفة الشاشات وRoutes وComposables

26 أكتوبر 2026 · v1.0 · **مصفوفة مواصفات وليست Navigation مُنفّذة**. [UX](ENMATH_PHASE1_DUAL_STAGE_UX_SPEC_2026-10-26.md) · [العقود](../ARCHITECTURE/ENMATH_PHASE1_IDENTITY_STAGE_DATA_CONTRACTS_2026-10-26.md) · [القرارات](ENMATH_PHASE1_REVIEW_AND_DECISIONS_2026-10-26.md).

## 1. مفاتيح القراءة

`C` = route/destination حالي؛ `P` = مقترح غير موجود؛ `F` = مشروط بخدمة فعلية؛ `S` = حالتان/مكوّن داخل شاشة، ليس route جديدًا. رموز E مأخوذة من خطة المدخل، ثم توسعت E13–E16؛ H/C/L/P/R وما بعدها IDs مواصفة ثابتة للحزمة. اسم بعد سهم في عمود الهدف **Composable مقترح**، لا تصريح بوجوده في الكود.

الحالي مأخوذ من `core/navigation/Routes.kt` و`ENMathApp.kt` و`ProductGraphs.kt` تحت `app/src/main/java/com/ahmadmol/enmath/`. المحلي خارج NavHost حاليًا، ووجود Routes.Login لا يعني Auth جاهزًا. Routes لا تحمل owner موثوقًا؛ كل شاشة/أمر يعيد scope من active context والعقود. لا external deep link مُعلن لهذه الشاشات في MainActivity manifest؛ اختبارات روابط IDs هنا تعني طلب ملاحة داخلي/مدخل اختبار آمن، لا ادعاء App Links موجودة.

## 2. المدخل والهوية

| ID | route الحالي / الهدف | C/P/F | Composable الحالي → المستهدف | stage/حراسة/الرجوع |
|---|---|---|---|---|
| E01 | `splash` محفوظ | C | SplashScreen → استعادة عبر EntryCoordinator مقترح | لا بيانات شخصية قبل validation؛ الرجوع يغلق البداية |
| E02 | `onboarding` محفوظ | C | OnboardingScreen → نسخة رسائل محلية صادقة | أول مرة فقط؛ skip يحفظ onboarded ثم E03 |
| E03 | لا route: LocalIdentityScreen؛ هدف `entry/stage` | P | LocalIdentityScreen جزئيًا → StageSelectionScreen | اختيار نية G9/BAC، لا إنشاء/تغيير active؛ رجوع يغلق/يلغي رحلة التبديل |
| E04 | لا route؛ هدف `entry/stage/{stage}` | P | لا StageEntry مستقلة → Grade9EntryScreen | stage parameter نية فقط؛ back E03 |
| E05 | نفس template السابق وقيمة BAC | P | لا مستقل → BacEntryScreen | لا duplicate graph للمرحلة؛ back E03 |
| E10 | شرح مدمج حاليًا؛ هدف `entry/guest-notice/{stage}` | P | LocalIdentityScreen نص → GuestNoticeScreen | owner/noticeVersion من عملية إنشاء/اختيار مؤهلة، لا UUID من route؛ back Entry |
| E11 | قائمة مدمجة بلا route؛ هدف `entry/profiles?stage={stage}` | P | LocalIdentityScreen list → LocalProfilePickerScreen | stage filter اختياري، null يعني الكل؛ cancel إلى المصدر، لا logout أثناء المعاينة |
| E13 | إنشاء مدمج؛ هدف `entry/local-create/{stage}` | P | LocalIdentityScreen create → CreateLocalLearnerScreen | stage ثابتة من النية، مستقل عن guest؛ back بلا كتابة قبل submit |
| E14 | استيراد مدمج؛ هدف `entry/legacy-review` | P | LocalIdentityScreen candidates → LegacyDataReviewScreen | المصدر من registry/candidate موثوق، لا عرض scope آخر؛ back يحفظ الأصل |
| E16 | لا route: recovery/AlertDialog في الجذر | S/P | ENMathApp recovery + storage issue → LocalRecoveryState | حالة جذرية تقطع التسرب؛ لا فرض route إذا registry/Nav غير موثوقين؛ back/اختيار آمن حسب المتاح |
| E06 | `login` محفوظ | C/F | LoginScreen → CleanLoginScreen حاليًا؛ StageAwareLoginScreen مقترح | لا تفعيل إنتاجي قبل AuthReady، stage intent فقط؛ back Entry الأصلي |
| E07 | `register-student` محفوظ | C/F | RegistrationScreen(vm,false) → StageAwareRegisterScreen | لا تغيير `register-teacher`؛ account.stage تثبتها الخدمة؛ back Login/Entry حسب المصدر |
| E08 | `verify` محفوظ | C/F | VerificationScreen → ProviderVerificationScreen | provider request id مخزّن بأمان لا OTP في route؛ back إلى سياق الطلب |
| E09a | `forgot` محفوظ | C/F | ForgotPasswordScreen → StageAwareRecoveryScreen | لا إرسال Demo؛ back Login |
| E09b | `new-password` محفوظ | C/F | NewPasswordScreen → ProviderNewPasswordScreen | verified recovery session فقط؛ back Recovery/Login آمن، لا نجاح كاذب |
| E12 | لا حالي؛ هدف `account/claim-guest` | P/F | ClaimGuestDataScreen مقترح | authenticated target + same stage + consent + dry-run؛ back يبقي المساحتين |
| E15 | لا حالي؛ هدف حالة قبل تفعيل الحساب | S/F | AccountStageMismatchDialog مقترح | المقارنة من خدمة موثوقة، قبول stage الحساب أو cancel؛ لا تعديل مرحلة |
| E-LEGACY | `started`, `account-choice` | C | GetStartedScreen, AccountChoiceScreen | تبقيان لمسار التطوير/المعلم القائم؛ لا تعادان كواجهة الطالب الإنتاجية الجديدة |

جميع routes الجديدة المقترحة `entry/*` تستلزم إضافة تعريفات وتوافق tests مستقبلًا، لا تغيير أي كود الآن. لا ترسل source path أو wallet key أو password أو answers في route أو analytics. route query المرشح غير سلطة؛ فتح ملف من مرحلة أخرى يوضح/يؤكد تبديل السياق الفعلي.

## 3. الشاشات التعليمية — نفس Routes، presentation مختلفة

| ID | route الحالي | الحالة الفعلية / Composable الحالي | الهدف G9 | الهدف BAC | سياق/Tab |
|---|---|---|---|---|---|
| APP | `main` | nested graph يبدأ `home` | StageStudentScaffold مقترح | نفسه كـ scaffold مشترك | لا شاشة مستقلة؛ يختار stage من الهوية |
| H-G9/H-BAC | `home` | G9 inline unavailable؛ BAC StudentLearningHome من StudentHomeV2.kt | Grade9HomeScreen | BacHomeScreen | Home؛ مصدران stage-scoped، لا نسخ journey BAC |
| C01 | `study` | G9 StageContentUnavailable؛ BAC StudentStudy → CurriculumScreen | Grade9CoursesScreen | BacCoursesScreen | Study؛ units مقابل book pair |
| C02 | `study/catalog` | G9 محجوب؛ StudyScreen | Grade9CatalogPresentation | BacCatalogPresentation | Study؛ catalog فعلي/حالة غير متاح |
| C03 | `book/{id}` | G9 محجوب؛ BookDetailsScreen | StageBookDetailsScreen إن catalog G9 يحوي كتابًا فعلًا | StageBookDetailsScreen مع book1/book2 metadata | Study؛ ليس كل G9 يفترض كتابين |
| C04 | `topic/{id}` | G9 محجوب؛ StudentLimitsTopic | Grade9UnitPresentation | BacUnitPresentation | Study؛ renderer مشترك، لا fallback إلى limits |
| L01 | `lesson/{id}` | G9 محجوب؛ InteractiveLimitsScreen للدرس المحدد وإلا LessonScreen | Grade9LessonPresentation | BacLessonPresentation | Focus؛ TypedBlocks مشتركة + source content |
| P01 | `practice/{id}` | G9 محجوب؛ LimitsPracticeScreen أو PracticeScreen | Grade9PracticePresentation | BacPracticePresentation | Focus؛ assessment/QuizViewModel مشتركان |
| P02 | `practice/{id}/result` | LimitsPracticeResult أو PracticeResultScreen، G9 محجوب | Grade9PracticeResult | BacPracticeResult | Study؛ receipt لمحاولة المالك، لا last result عالمي |
| R01 | `progress` | ProductProgressScreen | Grade9ProgressPresentation | BacProgressPresentation | Profile كما الحالي؛ إكمال/أدلة منفصلان |
| PLAN01 | مقترح `planner` | غير موجود | Grade9PlannerPresentation | BacPlannerPresentation | Home/Study entry؛ app header+back، ليست tab جديدة |
| PLAN02 | مقترح `planner/task/{id}` | غير موجود | PlanTaskEditor / G9 variant | PlanTaskEditor / BAC variant | نفس plan domain؛ cancel دون commit |
| PLAN03 | مقترح `planner/history` | غير موجود | PlanHistoryScreen مشترك | نفسه مع stage/مواد BAC | تاريخ revisions للمالك فقط |
| REV01 | مقترح `review` | لا Mistakes domain كاملة | Grade9ReviewQueue | BacReviewQueue | Home entry؛ empty من absence evidence |
| REV02 | مقترح `review/{id}` | غير موجود | Grade9ReviewPresentation | BacReviewPresentation | Focus؛ evidence scoped لا إنشاء محاولة من نتيجة قديمة |

`book/{id}` يبقى route عامًا؛ إن لم يكن لمنهج التاسع كتاب مرخّص معرف في manifest فالمسار غير متاح للتاسع، وليس كتاب BAC تحت عنوان G9. كل item ID مجهول/مرحلة غير متوافقة يوقف القراءة والكتابة، ويتيح العودة للكتالوج دون استبدال الهوية.

## 4. الشاشات المشتركة مع مواصفات اختلاف مقبولة

| ID | route | Composable الحالي → المقترح إن احتاج | اختلاف G9/BAC / شرط الظهور |
|---|---|---|---|
| T01 | `solver` | SolverWorkspace → StageSolverWorkspace | أمثلة stage/capability فقط؛ محرك واحد، لا promise CAS عام |
| T02 | `solver/scan` | SolverScanScreen → CapabilityAwareScanScreen | لا يدعي OCR/camera جاهزة؛ أي engine إدخال غير جاهز يعرض حدوده؛ نفس stage |
| T03 | `solver/history` | SolverHistoryScreen → العرض نفسه مع scope | تاريخ وإجابات owner فقط، filters مناسبة للمرحلة |
| T04 | `solver/solution/{id}` | ProductSolutionScreen → StageSolutionPresentation | history id يجب أن يخص المالك؛ شرح مناسب للمادة، لا حل legacy مجهول يوثق كتعلم جديد |
| T05 | `explore` | ExploreScreen → StageExploreScreen | قوالب من stage/capability؛ لا قلب الإحداثيات في RTL |
| LIB01 | `library` | LibraryScreen → StageLibraryScreen | محفوظات G9 مقابل BAC وموارد فيزياء مؤهلة مجانية؛ Empty واضح |
| LIB-FILTER | query مستقبلي اختياري `library?kind={kind}` | لا argument مسجل الآن | filter في state مبدئيًا؛ إضافة query تتطلب تحديث selectedTab/guards/tests لا route آخر للمكتبة |
| SEARCH01 | `search` | SearchScreen → StageSearchScreen | catalog والموارد مقيدان؛ البحث المحفوظ شخصي؛ لا اقتراح BAC لضيف G9 |
| N01 | `notifications` | NotificationsScreen(state,false,...) | نفس layout مقبول؛ stage links تُتحقق، بيانات المالك فقط؛ لا ادعاء push مجدول |
| PROF01 | `profile` | ProductProfileScreen → StageProfileScreen | نص زائر/طالب محلي/حساب، تقدم مسار G9 مقابل كتابي BAC؛ نفس ledger adapter |
| SET01 | `settings` | SettingsScreen → نفس الوظيفة مع سياق واضح | تخطيط موحد؛ theme/language عامان حاليًا؛ لا stage editor |
| GEM01 | `gems/wallet` | LocalGemsScreen → StageLocalGemsScreen | رصيد من نفس resolver؛ كتالوج موارد stage-filtered؛ guest NotApplicable |
| GEM02 | `gems/review` | LocalWeeklyReviewScreen | نفس سياسة الجواهر القائمة؛ أسئلة مؤهلة من المرحلة، no new weekly reward |
| A01 | `assignments` | AssignmentListScreen داخل ProtectedContent | نفس layout مقبول، فارغ إن لا provenance صف مسموح؛ لا بيانات معلم تلقائية |
| A02 | `assignments/{id}` | AssignmentDetailsScreen | metadata مبنية على صلاحية الطالب + المحتوى الموافق للمرحلة |
| A03 | `assignments/{id}/work` | PracticeScreen assignment=true + QuizViewModel | owner/deadline/attempt limits، Guest submit مرفوض كما الحالي؛ Focus |
| A04 | `assignments/{id}/result` | AssignmentResultScreen | نتيجة المالك فقط، لا آخر تسليم لطالب آخر |
| CONST | `solution` | ثابت Routes.Solution وسياسة تحديد tab/focus موجودة؛ لم يرصد composable له في graph الطالب الحالي | ليس وجهة جديدة معتمدة؛ لا زر ينتقل إليه دون destination، الحل الفعلي T04 |

الـ Guest المحلي يملك UUID؛ ProtectedContent الحالية لا تمنع العرض إذا localId موجودة، بينما AppViewModel.submitAssignment يرفض Guest فعلًا. لا تبدل هذه السياسة في حزمة التصميم إلى سماح تسليم أو إلى ادعاء حساب. أي تعديل صلاحيات الصفوف لاحقًا يحتاج عقد الطالب/المعلم ولا يأتي من UI وحدها.

## 5. سياسة الشريط والـ Back وHeaders

الخمس الحالية `Routes.tabs = [study, solver, home, explore, profile]` تبقى كمسارات semantic؛ التصميم يراجع ترتيبها المرئي RTL/LTR حسب layout الفعلي وRefBottomNav/CleanBottomBar، ويحافظ Home المركزية. لا تبديل النص/الأيقونة بوجهة مختلفة. كل tab اسم قابل للقراءة و48dp وحد أدنى للنص؛ إذا ضاق الخط الكبير يزداد ارتفاعه ولا تُسقط التسميات.

| المجموعة | Tab الأب | شريط سفلي / Header | Back المستهدف |
|---|---|---|---|
| Entry/Auth | لا tab | لا شريط طالب، stage/context في header حسب الشاشة | إلى مصدر المرحلة، أو إلغاء التبديل/الخروج من البداية |
| Home/Study/Solver/Explore/Profile | tab نفسها | الشريط الحالي، header واحد فقط | Android back وفق stack؛ لا حلقة إعادة دخول |
| Book/Topic/Catalog | Study دلاليًا | لا bottom bar، اتساقًا مع showStudentNavigation الحالية التي تعرضه على الخمس فقط؛ subheader إن لا own-header | نفس موضع الكتالوج للمالك نفسه |
| Lesson/Practice/Review item/Assignment work/Scan/Solution | Study/Solver/Home دلاليًا وفق الوجهة | Focus بلا bottom bar، لا ازدواج headers أو تداخل مع CTA/IME | يحفظ المرسل فقط؛ ينبه عن draft غير محفوظة، لا claim حفظ شامل |
| Progress/Library/Settings/Gems | Profile دلاليًا | لا bottom bar؛ header/back واحد؛ selectedTab الحالية metadata وليست دليل شريط ظاهر | الملف/المصدر الفعلي |
| Planner/Review queue الجديدتان | مصدر Home أو Study محفوظ في state | subheader + back بلا bottom bar؛ المصدر يحفظ للعودة لا لإضافة tab | العودة للمصدر والـ scroll لنفس owner |

عند owner change لا restoreState لstack شخص آخر. الرجوع من E11 قبل التأكيد لا logout؛ بعد التبديل الجذري لا Back إلى شاشة إجابات المالك السابق. unknown content يبقى خطأ داخل سياق المرحلة الفعلي، لا انتقالًا للـ BAC كافتراض. navigation idempotent للضغط المكرر.

## 6. توافق Test Tags وحالة التصميم

Tags الحالية المرجعية: `screen-local-identity`, `local-stage-G9`, `local-stage-BAC`, `local-create-student`, `local-open-guest`, `screen-home`, `student-continue`, `home-practice`, `home-quick-solver`, `home-all-progress`, `home-gems-badge`, `screen-study`, `study-limits`, `book-{id}`, `topic-{id}`, `browse-all-lessons`, `complete-lesson`, `lesson-practice`, وعائلة `nav-*`؛ auth tags مثل `auth-email/auth-password/registration-submit/verify-submit` تخص العقود الحالية.

يحفظ tag القائم إذا بقي نفس المعنى، ويمكن إضافة stage semantics/اختبار إضافي؛ لا tag مضللة كـ `study-limits` لزر وحدة G9 أخرى. قبل أي تغيير معنوي توثق mapping `old→new / سبب / test files impacted` ويعتمد ضمن دفعة التنفيذ، مع بقاء اختبارات المعلم. لا نفترض أن شاشات E الجديدة لها هذه tags الآن.

Figma frame IDs تطابق أعمدة IDs. الحالة الحالية لكل P/F غير منفذة، وكل C يشير لوجود الكود فقط. Teacher graph ومسارات `teacher-*`, `teacher/*`, `teacher/reference/*`, `register-teacher` وشاشات Legacy/BeforeApproved/Reference محفوظة خارج إعادة التصميم؛ لا حذف أو إعادة توجيه. Shared tokens extraction لا يبرر تغيير المعلم.
