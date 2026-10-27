# EN.MATH — Student UI completion, 2026-10-25

تنفيذ محلي فقط، دون Emulator أو هاتف أو ADB أو تثبيت أو لقطات شاشة أو معاينة بصرية. القبول البصري بيد المستخدم وحده. لا Commit أو Push أو حذف بيانات أو تغيير Routes. التعديلات الموجودة عند بدء الجولة محفوظة.

## نطاق الحالة

Implemented — Pending My Visual Review تعني أن التنفيذ البرمجي الحالي موجود ومربوط واجتاز التجميع والفحوص المذكورة. لا تعني اختبار العرض أو اللمس أو التنقل على جهاز، ولا اعتماد مطابقة الصور. تعد تبويبات المكتبة والمتجر والقارئ ونتائج النهايات وحالات المراجعة حالات داخل الوجهات القائمة، وليست شاشات مضافة.

| # | الشاشة | Route | الحالة | التنفيذ / التحقق |
|---|---|---|---|---|
| 1 | الرئيسية | home | Implemented — Pending My Visual Review | التصميم السابق المعتمد؛ الشريط السفلي وTheme مشتركان |
| 2 | المنهاج | study | Implemented — Pending My Visual Review | التنفيذ السابق؛ تسميات التنقل المقاسة والقابلة للالتفاف |
| 3 | كتالوج الكتب | study/catalog | Implemented — Pending My Visual Review | رأس موحد وTheme مركزي وبطاقات Premium الحالية |
| 4 | تفاصيل الكتاب | book/{id} | Implemented — Pending My Visual Review | رأس موحد وTheme مركزي، الإكمال الحالي محفوظ |
| 5 | الموضوع ودروسه | topic/{id} | Implemented — Pending My Visual Review | رأس واحد، أسهم اتجاهية، عداد إشعارات حقيقي وألوان مركزية |
| 6 | الدرس والدرس التفاعلي | lesson/{id} | Implemented — Pending My Visual Review | رأس خارجي للتفاعلي وداخلي للعادي؛ رجوع عند فقد الدرس؛ سطح Progress مناسب للثيمين |
| 7 | التدريب | practice/{id} | Implemented — Pending My Visual Review | LessonPage وLessonProgressCard مركزيان؛ القواعد دون تغيير |
| 8 | نتيجة التدريب والنهايات | practice/{id}/result | Implemented — Pending My Visual Review | Hero وStats وأزرار مرجعية، المحاولة والإتقان الحقيقيان |
| 9 | مساحة الحل | solver | Implemented — Pending My Visual Review | ألوان طالب مركزية، عناصر بحد أدنى وارتفاع مرن، سلوك المعلم مشروط مستقل |
| 10 | تفاصيل الحل | solver/solution/{id}, solution | Implemented — Pending My Visual Review | Hero للطالب، الخطوات والطرق والحفظ الحالية |
| 11 | سجل الحلول | solver/history | Implemented — Pending My Visual Review | رجوع موصول، احتمال وبحث مشتركان وآمنان عند فلتر قديم، سجل الحساب الحالي |
| 12 | المختبر | explore | Implemented — Pending My Visual Review | عنوان وإدخال وتحكم متجاوب للطالب، ألوان الرسم من Theme، الصيغ LTR |
| 13 | التقدم | progress | Implemented — Pending My Visual Review | رأس مرجعي وStats؛ مخطط قابل للتمرير مع فصل العدد والعمود والتاريخ |
| 14 | قائمة الواجبات | assignments | Implemented — Pending My Visual Review | التصميم الحالي والعضوية الديناميكية؛ رأس موحد |
| 15 | تفاصيل الواجب | assignments/{id} | Implemented — Pending My Visual Review | بطاقات أسئلة تتبع Theme؛ لا اسم معلمة مختلق؛ لا حقل مؤلف في نموذج Assignment |
| 16 | حل الواجب | assignments/{id}/work | Implemented — Pending My Visual Review | رأس موحد ومكونات التدريب، رفض التسليم وتأكيده الحاليان |
| 17 | نتيجة الواجب | assignments/{id}/result | Implemented — Pending My Visual Review | مكونات Hero وStats؛ الإجابات والنتيجة والتعليق من الحالة الحالية |
| 18 | المكتبة بجميع تبويباتها | library | Implemented — Pending My Visual Review | محفوظات ومصادر حقيقية؛ وصف الدرس من rule، سهم فتح تلقائي الاتجاه، فلاتر الحالة الحالية |
| 19 | البحث ونتائجه | search | Implemented — Pending My Visual Review | سجل state.product.searches الفعلي، إعادة ضبط النص عند تبديل الهوية، نتائج وربط حقيقيان |
| 20 | الإشعارات | notifications | Implemented — Pending My Visual Review | ألوان طالب مركزية مع الحفاظ على لوحة المعلم؛ قراءة وروابط حقيقية |
| 21 | الحساب | profile | Implemented — Pending My Visual Review | لوحة الطالب من Theme؛ نصوص تتمدد وسهم يتبع الاتجاه؛ بيانات Gems المقيدة بمالكها |
| 22 | الإعدادات | settings | Implemented — Pending My Visual Review | رأس موحد وألوان البطاقات المركزية؛ نفس وظائف Theme/لغة/تنبيهات الحالية |
| 23 | المسح التجريبي | solver/scan | Implemented — Pending My Visual Review | الوجهة الحالية ورأسها؛ محاكاة معلنة، لا ادعاء OCR حقيقي |
| 24 | الجواهر والمحفظة والمتجر والقارئ | gems/wallet | Implemented — Pending My Visual Review | Theme مركزي، التفاف إجراءات المتجر، Reader داخل بطاقة؛ عزل المالك الحالي |
| 25 | المراجعة الأسبوعية وجميع حالاتها | gems/review | Implemented — Pending My Visual Review | Completed يمنع إعادة فتح الأسئلة، حالات تحميل وخطأ وعدم أهلية وجاهزية ونتيجة مع Retry |

لا تصنيفات Visually Approved. لا وجهة UI محجوبة في نطاق التنفيذ الحالي، ولا خدمات Backend أو OCR جديدة.

## إصلاحات الوظائف وحدود الادعاء

- سجل البحث الحقيقي وفلتر Probability وغياب اسم المعلمة الثابت كانت موجودة بالفعل في نسخة بدء الجولة. راجعتها وحافظت عليها؛ لم أستبدلها ببيانات ثابتة. اختبار FinalStudentRegressionTest يغطي تسجيل البحث الفعلي والتكرار والحد الأعلى والفراغات، والاختبار الجديد يغطي فلتر Probability مع البحث.
- ربط onBack للطالب في ProductGraphs، واستخدام رأس واحد للسجل. مسار TeacherSolverWorkspace وcallback الخاص به دون تعديل.
- دالة studentOwnsHeader تفصل الدرس التفاعلي عن العادي، وتمنع الرأس المكرر. الدرس المفقود يحتفظ بزر الرجوع.
- استبدال شارات الإشعارات «3» الثابتة في الموضوع والدرس بعدد غير المقروء الحقيقي.
- activeStudentId لا يقبل studentRecordId إلا بملكية مسجلة. توقف استنتاج alias جديد من اسم الطالب أو بادئة البريد. alias محفوظ يظل للمالك التاريخي فقط؛ إذا كان ahmed محجوزًا لمالك تاريخي آخر، يستخدم الحساب التجريبي الجديد مفتاح بريده الكامل بدل تصادم البيانات.
- personal_legacy_record_id المخزن سابقًا لا يحذف ولا يعاد تخمينه. لم أغيّر مفاتيح ملفات الحسابات أو IDs التسليمات أو الصفوف، ولم أجر ترحيلًا للبيانات. البيانات القديمة غير المنسوبة بوضوح تبقى محفوظة دون ربط تخميني؛ استعادتها إلى حساب محدد تحتاج دليل ملكية وخطة منفصلة.
- نتائج المراجعة الأسبوعية وحالة Completed تستخدم بيانات المالك الحالي. إعادة فتح وجهة مكتملة لا تعيد الاختبار لمجرد بقاء review في الذاكرة.
- تغييرات العرض لا تعدل قواعد التصحيح أو أوزان الإتقان أو الجوائز أو سقوفها أو الصلاحيات.

## المكونات والتصميم

RefColors وstudentColorScheme من Theme المركزي، RefTopBar، RefHeroCard مع PremiumHeroLayout، RefStatsRow، RefFilterChipRow، RefCategoryTabs، RefPrimaryButton، RefSecondaryButton، RefEmptyState، PremiumHeroCard، LessonPage وLessonProgressCard.

أضيف LocalStudentExperience لعزل تحسينات المكونات المشتركة. PremiumLearningTheme لا يعيد تلوين صفحات الطالب فوق Theme المركزي. الأزرار تتمدد رأسيًا، وتلتف إجراءات المختبر والمتجر. تسميات التنقل تقاس بواسطة TextMeasurer بالخط والقياس الحاليين، ويزداد ارتفاع الشريط بدل قصها. تبقى القياسات الأساسية للمعلم كما كانت. حساب التباين النصي الأساسي اجتاز اختبار JVM في الثيمين؛ ليس ذلك إثباتًا لتباين كل رسم زخرفي أو كل حالة مرئية.

## التحقق

الأمر: `gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain`

JDK: <LOCAL_USER_RESOURCE>

- assembleDebug: PASS.
- testDebugUnitTest: PASS، 119 اختبارًا، صفر Failures/Errors/Skipped.
- lintDebug: PASS، صفر Errors/Fatal، 14 Warning و1 Information. تشمل GradleDependency وOldTargetApi وUseKtx وModifierParameter وUnusedResources وDataExtractionRules؛ Information من AutoboxingStateCreation. لا ادعاء Lint بلا تحذيرات.
- git diff --check: PASS، مع تنبيهات Git عن LF/CRLF فقط، دون أخطاء whitespace.
- أضيف StudentCompletionRegressionTest: 7 اختبارات للهوية القديمة ومالكها والحساب التجريبي والأدوار، رأس الدرس، السجل وProbability، وتباين أزواج نصوص Theme.
- تشمل المجموعة الناجحة أيضًا FinalStudentRegressionTest وWeeklyGemCardPresentationTest وAssignmentSubmissionVerificationTest واختبارات Gems وQuiz وInteractiveLesson القائمة.
- هذه اختبارات JVM وسياسات وربط كود، وليست اختبارات Compose rendering أو تشغيل NavController على جهاز. لم تشغل instrumentation.

مخرجات التحقق:
- app/build/outputs/apk/debug/app-debug.apk
- app/build/reports/tests/testDebugUnitTest/index.html
- app/build/reports/lint-results-debug.html
- app/build/reports/student-completion-verification.log
- app/build/reports/student-completion-summary.json

## حماية التعديلات الحالية

أخذت بصمات ملفات app/src قبل الجولة وقارنتها بعدها. ملفات المعلم الـ12 تحت features/teacher وcomponents/teacher لم تتغير. AppViewModel وRoutes وQuizViewModel وInteractiveLessonViewModel وLocalGemsPolicy وLocalGemsCoordinator وSQLiteLocalGems لم تتغير. لا ملف مصدر محذوف. تغييرات المكونات المشتركة للعرض الجديد مقيدة بـLocalStudentExperience عند استخدامها في واجهات المعلم؛ يبقى اختبار العرض الفعلي للمعلم خارج التحقق المحلي.

## مراجعة المستخدم

1. جميع الوجهات الـ25 بالعربية والإنجليزية وLight/Dark، مع font ×1 و×2، وخاصة تسميات الشريط السفلي والرؤوس والأزرار.
2. تفاعلي → تدريب → نتيجة → إعادة/عودة، والدرس العادي والمفقود دون رؤوس مكررة أو زر رجوع مفقود.
3. السجل بفلتر Probability مع بحث، فارغ/ممتلئ، وفتح التفاصيل؛ رجوع الطالب ورجوع المعلم من السجل الداخلي.
4. المكتبة بتبويباتها والفلاتر، والبحث الفارغ وسجله ونتائجه، والإشعارات قبل/بعد القراءة.
5. الواجب المفتوح والمنتهي والمستنفد، تأكيد التسليم ورفضه، والنتيجة والتعليق.
6. تبديل الحسابات والأدوار، وخاصة من يمتلك alias تاريخيًا؛ لا تمنح ثقة ربط حسابات من الاسم وحده.
7. Gems: تحميل/خطأ/رصيد ناقص/تأكيد فتح/مقتنيات/قارئ، وجميع حالات المراجعة الأسبوعية بما فيها إعادة فتح Completed والنتيجة بعد التخزين.

## القيود المتبقية

- لا ادعاء مطابقة بصرية نهائية أو قياس TalkBack أو insets أو لوحة المفاتيح على جهاز.
- لا بيانات حقيقية لهوية معلم الواجب في النموذج الحالي؛ لا اسم أو صورة بديلة مختلقة.
- هوية وملكية alias القديم المثبتة تحافظ على البيانات؛ السجلات التاريخية الملتبسة لا ترحل تلقائيًا. المصادقة الحالية محلية تجريبية وليست إثبات هوية بخادم.
- عقد SharedPreferences الحالي لا يضمن دوام كل كتابة عند فشل القرص، ولا توجد معاملة واحدة للتعلم وSQLite الخاص بالجواهر. لم أعِد كتابة التخزين أو أدّع نجاحه الدائم؛ اختبارات التسليم القائمة توضح هذا الحد.
- OCR محاكاة حالية، والمحتوى الإثرائي المحلي يحتاج مراجعته العلمية القائمة. لا تغيير لقواعده أو ادعاء خدمة مفقودة.

## الملفات المعدلة في هذه الجولة

- `app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/design/LearningArtwork.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/design/PremiumLearning.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/design/ReferenceDesignSystem.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/design/Theme.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/ProductGraphs.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/assignments/AssignmentScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/courses/CourseScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LessonPresentation.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/explore/ExploreScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/library/LibraryScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/notifications/NotificationsScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/progress/ProductProgressScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/reference/CleanBottomBar.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/search/SearchScreen.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/solver/WorkspaceScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/student/StudentLearningScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/core/navigation/StudentHeaderPolicy.kt`
- `app/src/test/java/com/ahmadmol/enmath/StudentCompletionRegressionTest.kt`

APK SHA-256: `0BA33CDF2DE284C65390F95008069467B70D9F71E44F2D31B2F1BC3A29607D6E`.
