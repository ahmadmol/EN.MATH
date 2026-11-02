# EN.MATH — تقرير واجهة الدرس والاختبار

تاريخ التنفيذ: 24 أكتوبر 2026. المرجع البصري: `Arabic Math Quiz Lesson Interface.png` المرفقة. التنفيذ داخل تطبيق Kotlin / Jetpack Compose / Material 3؛ لا توجد بيانات أسئلة بديلة أو مكتبات جديدة.

## نطاق التغيير

| الملف | التغيير في هذه المهمة |
|---|---|
| `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LessonPresentation.kt` | مكونات مشتركة لصفحة الدرس وبطاقة التقدم والسؤال والخيارات والتلميح والتغذية الراجعة والأزرار |
| `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLimitsScreen.kt` | ربط العرض الجديد بتسلسل الدرس والأسئلة الحالية، مع بقاء الأمثلة والمحتوى التعليمي |
| `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/InteractiveLessonViewModel.kt` | حفظ مراجعة كل سؤال واستعادتها، منع تجاوز خطوة وتصحيح مكرر وإكمال مكرر، وترحيل حالة المراجعة القديمة |
| `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitsPracticeScreens.kt` | تطبيق المكونات على تدريب النهايات دون تغيير شروط النتيجة أو إعادة المحاولة |
| `app/src/main/java/com/ahmadmol/enmath/features/practice/PracticeScreens.kt` | تطبيق العرض على التدريب العام والاختبارات، مع الإبقاء على قواعد الواجبات وتأكيد تسليمها |
| `app/src/main/java/com/ahmadmol/enmath/features/practice/QuizViewModel.kt` | حماية الاختيار بعد التحقق، منع تكرار الانتقال والإنهاء، مع بقاء إعادة المحاولة |
| `app/src/main/java/com/ahmadmol/enmath/core/design/MathRender.kt` | معاملات اختيارية للخلفية والمحاذاة والمسافات؛ الإعدادات الافتراضية لبقية الاستخدامات محفوظة |
| `app/src/main/java/com/ahmadmol/enmath/core/design/PremiumLearning.kt` | دعم الأزرار الدائرية في الشريط العلوي المشترك |
| `app/src/main/java/com/ahmadmol/enmath/core/navigation/ENMathApp.kt` | استخدام الشريط المشترك للدرس والتدريب دون إضافة شريط ثانٍ |
| `app/src/main/java/com/ahmadmol/enmath/features/courses/interactive/LimitExploration.kt` | إصلاح تكاملي لتجميع الملف الذي ظهر أثناء العمل: قراءة الترجمة خارج كتلة semantics |
| `app/src/test/java/com/ahmadmol/enmath/InteractiveLessonSessionTest.kt` | 6 اختبارات لحالة الجلسة والتحقق والمراجعة والاستعادة والإكمال |
| `app/src/androidTest/java/com/ahmadmol/enmath/PremiumLessonUiTest.kt` | 16 حالة واجهة: Light/Dark × Arabic/English × تكبير 1.0/2.0، تشمل الأسئلة الطويلة وحالات الاختيار والتلميح والتصحيح |
| `app/src/androidTest/java/com/ahmadmol/enmath/FullApplicationUiTest.kt` | إصلاح استدعاء factory قديم بتمرير Context المطلوب؛ لم يُحذف اختبار |

المشروع احتوى تعديلات سابقة عند البدء. هذا الجدول يصف عمل هذه المهمة فقط. لم تُعدّل قواعد Teacher أو Burhan أو اقتصاد الجواهر، ولم يُنفذ Git Commit أو Push.

## الواجهة والسلوك

- بطاقات بيضاء مستديرة مع ظلال خفيفة، وأسـطح داكنة من لوحة PremiumLearningTheme المشتركة، وعرض أقصى 640dp للمحتوى.
- بطاقة عنوان الدرس وموضع الطالب وشريط تقدم متدرج. الدرس يحوي 11 **خطوة تعليمية**، تتضمن شرحًا ورسمًا وأمثلة وأسئلة؛ لذلك يحسب المؤشر موضع الخطوة من التسلسل الحقيقي. التدريب يحسب موضع السؤال من PracticeSet الحقيقي.
- بطاقة رياضية مستقلة تستخدم MathRender الحالي باتجاه LTR، مع تمرير أفقي للتعبيرات الطويلة. النصوص التعليمية المختلطة تستخدم MathProse لعزل الرياضيات عن العربية.
- بطاقات اختيار قابلة للنقر بالكامل مع RadioButton وحالة تحديد وحد أزرق وحالة صحيحة/خاطئة وأيقونة. تُستخدم قيمة الخيار مع معرف السؤال كمفتاح ثابت؛ نموذج Question الحالي لا يحتوي معرفات منفصلة للخيارات. لا يتغير ترتيب الخيارات.
- التلميحات من Question.hint أو كتلة الدرس المعتمدة، دون توليد محتوى جديد. يظهر زر المصباح فقط عند وجود تلميح.
- زر التحقق معطل قبل الاختيار وبعد التحقق حتى تتغير الإجابة أو تُطلب إعادة المحاولة وفق سياسة النشاط. زر المتابعة ظاهر ومعطل حتى استيفاء الشرط.
- الدرس يشترط التحقق من إجابة صحيحة للمتابعة، ويسمح بتغيير الإجابة الخاطئة. الإجابة الصحيحة المحققة تُقفل وتُستعاد عند العودة. التدريب يسمح بالمتابعة بعد التحقق ولو كانت الإجابة خاطئة، مع بقاء إعادة المحاولة. التدريب لا يضيف انتقالًا للخلف لأن التدفق السابق لا يتيحه.
- أهداف لمس 48dp على الأقل، ارتفاع مرن للنصوص، تمرير رأسي، ومؤشر Radio على البطاقة كاملة. التغذية الراجعة تعلن عبر live region، والألوان تدعمها أيقونات ونصوص. لا تضيف المكونات حركة إلزامية.

## البيانات والتصحيح والتقدم والجواهر

مصادر البيانات بقيت `LimitsLessonSequence.blocks(state)` و`state.practiceSets` و`ProductState.question`. التصحيح يستدعي `LearningRules.correct` من ViewModel؛ لم تتغير المعادلات أو الإجابات أو قواعد التصحيح. شروحات النهايات تستخدم `limitsExplanation` القائمة، والتدريب العام يستخدم `Question.explanation`.

حالة الواجهة محفوظة في SavedStateHandle/StateFlow، والتلميح المحلي في rememberSaveable. مراجعة الأسئلة لا تستدعي التسجيل أو المكافآت. إكمال الدرس يمر عبر `app.completeInteractive` الحالي، والنتيجة عبر `app.recordPractice` ثم شاشة النتيجة الحالية. لا يُزاد LearningState بمجرد عرض سؤال أو الانتقال إليه.

يظل استحقاق الجواهر والدقة والحدود ومنع الازدواج وعزل المالك والضيف داخل نظام LocalGems الحالي. لم تُضف أي عملية منح داخل Composable. حواجز الجلسة تمنع تكرار إرسال الإكمال، وتبقى حماية السجل الموجود هي المسؤولة عن منع تكرار المكافأة بين الجلسات. واجهة تسليم الواجب تبقى تحت حماية التحقق الموجودة في ProductGraphs وتسمح بإظهار رفض التسليم كما سابقًا.

## التحقق والتسليم

اكتملت جولة التحقق النهائية بنجاح (`BUILD SUCCESSFUL`). استُخدمت Java 21 الموجودة في `<LOCAL_USER_RESOURCE>` دون تعديل Gradle أو الاعتماديات:

```powershell
$env:JAVA_HOME='<LOCAL_USER_RESOURCE>'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest --console=plain
```

| الفحص | النتيجة |
|---|---|
| Assemble Debug | ناجح، تم إنتاج APK |
| Unit Tests | 105 اختبارات، 0 فشل، 0 أخطاء، 0 اختبارات متجاوزة؛ تشمل 6 اختبارات الجلسة الجديدة واختبارات التقدم والجواهر الحالية |
| Lint Debug | ناجح، 0 أخطاء و11 تحذيرًا؛ تخص target SDK والاعتماديات وموارد/إعدادات قائمة، ولم تُعطل قواعد Lint |
| Assemble Debug AndroidTest | ناجح؛ تم تجميع حالات واجهة PremiumLessonUiTest ضمن حزمة الاختبارات |
| Git diff whitespace check | ناجح |
| Physical UI / TalkBack / screenshot comparison | معلق لعدم وجود هاتف متصل |

سجل الفحص: `<PROJECT>\lesson-premium-verified-build.log`. تقارير الوحدة: `app/build/reports/tests/testDebugUnitTest/index.html`. تقرير Lint: `app/build/reports/lint-results-debug.html`.

الهاتف: أظهر `<ANDROID_SDK>\platform-tools\adb.exe devices -l` قائمة فارغة. لم يُستخدم Emulator. لا توجد لقطات شاشة هاتف جديدة ولا ادعاء بإتمام QA بصري أو TalkBack على جهاز. اعتماد المطابقة البصرية، الأجهزة الصغيرة/المتوسطة/الكبيرة، لوحة المفاتيح، Safe Insets وTalkBack معلق حتى توصيل هاتف حقيقي.

حزمة اختبارات الواجهة معدة لحفظ لقطات الحالات في مجلد التطبيق الخارجي `premium-lesson-qa` عند تنفيذها على هاتف. تتضمن حالات قبل الاختيار وبعده والتلميح والتصحيح، والوضعين واللغتين وتكبير الخط والتعبيرات الطويلة. يلزم مقارنة هذه اللقطات بالمرجع عند توفر الجهاز.

APK التطبيق الناتج: `<PROJECT>\app\build\outputs\apk\debug\app-debug.apk`.

APK اختبارات Android الناتج: `<PROJECT>\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk`.
