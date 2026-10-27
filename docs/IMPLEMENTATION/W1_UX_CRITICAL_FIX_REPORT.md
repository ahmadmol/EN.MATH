# W1 UX Critical Fix Batch — 2026-10-26

الفرع: `feature/ui-refinement`. النطاق: F01 وF02 وF04 وF05 فقط، قبل W2.

**الحالة العامة: إصلاحات الشيفرة منفذة؛ قبول الهاتف معلّق. لا DEVICE-ACCEPTED ولا اعتماد بصري للـInsets.**

## F01 — جواهر الملف الشخصي — DONE (الكود والتحقق المضيف)

**السبب الجذري:** كانت `ProductProfileScreen` تمرر `state.user?.email` إلى البطاقة، ثم تقارنه `gemBalanceText` بمالك المحفظة. البريد فارغ في `LocalLearner.user()`، بينما المالك الصحيح UUID للملف الجديد أو `legacyWalletKey` للملف المستورد. لذلك بقيت حالة التحميل رغم جاهزية رصيد المالك الصحيح.

**الملفات المعدلة:**

- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProductProfileScreens.kt`
- `app/src/main/java/com/ahmadmol/enmath/features/profile/ProfileGemsPresentation.kt` (جديد)
- `app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt`
- `app/src/test/java/com/ahmadmol/enmath/ProfileGemsPresentationTest.kt` (جديد)
- `app/src/test/java/com/ahmadmol/enmath/LocalGemsCoordinatorTest.kt`
- `app/src/androidTest/java/com/ahmadmol/enmath/W1UxCriticalUiTest.kt` (جديد، تجهيز اختبارات واجهة)

**الإصلاح:** تستقبل البطاقة المستخدم، وتستخرج دالة عرض مستقلة المالك عبر `LocalGemsPolicy.owner(user)` فقط. تعرض `account.balance` من حالة Ledger المربوطة بالمالك، دون أي زيادة عرضية. تتحقق دالة الرصيد أيضًا من تطابق `account.owner` مع المالك المطلوب، فلا يتسرب رصيد قديم أثناء تبديل الملف حتى لو تطابق غلاف الحالة. حالات التحميل والخطأ و«غير متاح» لا تحمل لاحقة عملة. الزائر والجلسة الغائبة يعرضان «غير متاح / N/A» حتى عند تمرير حالة محفظة قديمة. مسار `gems/wallet` ووسم `profile-gems` محفوظان.

**التحقق:** ستة اختبارات عرض جديدة تغطي الملف المحلي ذي البريد الفارغ، رصيد صفر، مفتاح Legacy المختلف عن UUID والبريد، الزائر، تبديل المالك، حمولة مالك أجنبي، التحميل والخطأ. اختبار Coordinator جديد يربط ملفًا جديدًا ثم Legacy ثم Guest ثم يعيد إنشاء Coordinator ويبدل الملف مرة أخرى، مع بقاء السجل التاريخي كما هو وعدم إنشاء محفظة للزائر. إعادة إنشاء Coordinator هنا تستخدم مخزن fixture في الذاكرة؛ ليست إعادة تشغيل Android. اختبارات W1 القائمة تتحقق من إعادة فتح ملفات preferences الاصطناعية من القرص، واختبارات SQLite المضيفة تتحقق من إعادة فتح قاعدة مؤقتة وحفظ الملكية والأرصدة والمقتنيات. جميع اختبارات JVM وSQLite نجحت كما هو موثق أدناه.

**مراجعة الهاتف:** مقارنة Profile وWallet للملف الجديد وLegacy وGuest بعد العودة من الخلفية والتبديل وإعادة تشغيل التطبيق. التحقق من عدم ظهور رصيد الملف السابق لحظيًا. Android SharedPreferences وSQLiteOpenHelper وprocess death لم تختبر فعليًا.

## F02 — System Insets — PARTIAL (الكود منجز؛ القبول البصري معلّق)

**السبب الجذري:** يعيد `ENMathApp` شاشة الهوية مبكرًا خارج Scaffold. لا يملك `ScreenPage` العام معالجة Insets، فبدأ المحتوى عند حافة النافذة.

**الملف المعدل:** `app/src/main/java/com/ahmadmol/enmath/features/auth/LocalIdentityScreen.kt`.

**الإصلاح:** إضافة `Surface(fillMaxSize())` لخلفية من ألوان الثيم، وجذر داخلي `Box(Modifier.safeDrawingPadding().imePadding())` قبل `ScreenPage`. بذلك تقع منطقة التمرير داخل المساحة الآمنة، وتستهلك معدلات Insets ما طبقته، فتتكامل مساحة لوحة المفاتيح دون جمعها مرتين. لم يتغير `ScreenPage` العام أو Scaffold أو Routes، لذلك لا يضاف padding إلى شاشات التطبيق الأخرى. تبقى اتجاهات RTL والمسافات المنطقية والثيم وأحجام النص الموروثة كما هي، دون ارتفاع ثابت جديد.

**التحقق:** ترجمة وبناء Debug وRelease وW1Qa وفحوص Lint. هذه أدلة صحة بناء فقط؛ لا تثبت موضع العنوان بصريًا أو قابلية الوصول بلوحة المفاتيح على Android.

**مراجعة الهاتف المطلوبة:** العربية RTL، Light/Dark، خط 1× و2×، عرض 360dp وعرض أكبر، التنقل بالإيماءات والأزرار، Xiaomi/Android 15 إن توفر، دوران الشاشة، فتح/إغلاق IME والتركيز في حقل الاسم ثم التمرير إلى آخر زر. يجب أن يبقى العنوان خارج Status Bar وآخر إجراء خارج Navigation Bar/IME، مع فحص عدم تضاعف padding في صفحات Scaffold القائمة. لا اعتماد قبل تنفيذ هذه المصفوفة يدويًا.

## F04 — المرتبة الوهمية — DONE

**السبب الجذري:** تمرير `Rank 25 / المرتبة 25` ثابتًا من الرئيسية إلى شارة Header لكل مستخدم.

**الملف المعدل:** `app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt`.

**الإصلاح:** حذف القيمة ومعامل `rankText` والشارة كاملة. بقي صف الحالة بعنصري رصيد الجواهر والسلسلة وبالمسافات القائمة، دون عنصر بديل أو مكان محجوز لرتبة.

**التحقق:** نجاح بناء النسخ الثلاث وJVM وLint؛ مراجعة الفرق تؤكد إزالة المصدر وموضع العرض. أضيف اختبار واجهة لغياب الرتبة والصندوق وبقاء إجراء المحفظة، دون تشغيله على جهاز.

**مراجعة الهاتف:** اتزان Header بعد الحذف في العربية والإنجليزية والثيمين والخط الكبير. DONE هنا يعني اكتمال الإزالة؛ القبول البصري للهاتف معلّق.

## F05 — صندوق المكافأة الوهمي — DONE

**السبب الجذري:** عقدة `reward-1` وحالاتها وواجهة الصندوق توحي بمنحة؛ callback يفتح المحفظة فقط، و`claimedRewards` حالة `remember` بلا مصدر استحقاق دائم.

**الملف المعدل:** `app/src/main/java/com/ahmadmol/enmath/features/student/StudentHomeV2.kt`.

**الإصلاح:** حذف العقدة وواجهة الصندوق وحالات AVAILABLE_REWARD/CLAIMED_REWARD و`isRewardNode` و`claimedRewards` وcallbacks الخاصة بالمطالبة. يتصل المسار بالعقدة التعليمية التالية مباشرة عبر آلية الربط القائمة. لا منح جديدة ولا أوامر تخزين بديلة. بقيت إجراءات المحفظة التعليمية القائمة وRoutes وTestTags كما هي؛ الصندوق المحذوف لم يحمل TestTag.

**التحقق:** نجاح JVM بما يشمل D06 وLegacy وCoordinator وفحوص SQLite العشرة للحفظ والعزل والقيود والتراجع والتزامن وإعادة الفتح. اختبار واجهة مجهز لغياب الصندوق وبقاء فتح المحفظة. لم تتغير `LocalGemsPolicy` أو `SQLiteLocalGems` أو schema أو مخزن Legacy أو مفاتيحه.

**مراجعة الهاتف:** غياب الصندوق قبل إكمال دروس القسم وبعدها وبعد إعادة التشغيل، واستمرار وصلات المسار وفتح الدروس الموجودة والمحفظة. مطابقة الرصيد والمقتنيات التاريخية قبل ترقية QA وبعدها.

## نتائج التحقق

بيئة التشغيل: Windows، JBR 21.0.11، Gradle Wrapper الخاص بالمشروع، SDK المحلي. لم تعدل إعدادات البناء أو التوقيع أو versionCode الافتراضي؛ استخدم override موجود أصلًا للـQA.

```powershell
$env:JAVA_HOME='<LOCAL_USER_RESOURCE>'
.\gradlew.bat testDebugUnitTest testReleaseUnitTest testW1QaUnitTest assembleDebug assembleRelease assembleW1Qa lintDebug lintRelease lintW1Qa -Pw1QaVersionCode=3 --console=plain
python tools/test_local_gems_sqlite.py
.\gradlew.bat compileDebugAndroidTestKotlin -Pw1QaVersionCode=3 --console=plain
```

- JVM: **188/188 PASS لكل variant**: Debug وRelease وW1Qa؛ صفر failures/errors/skipped (564 تنفيذًا للمجموعة نفسها، وليس 564 اختبارًا مختلفًا).
- Host SQLite: **10/10 PASS** على قواعد tempfile باستخدام schema الفعلية، دون فتح أي قاعدة مستخدم.
- `assembleDebug` و`assembleRelease` و`assembleW1Qa`: **PASS**؛ انتهى التشغيل المجمع بـ`BUILD SUCCESSFUL`.
- `lintDebug` و`lintRelease` و`lintW1Qa`: **PASS**، صفر أخطاء. Debug: 14 تحذيرًا؛ Release: 15؛ W1Qa: 14 (مع ملاحظة Information واحدة لكل نسخة). مواضع الملاحظات في إعدادات Gradle وManifest وموارد الأيقونة وملفات قائمة خارج هذه الإصلاحات؛ لم أخف التحذيرات أو أغيّر إعداد Lint. هذه أعداد شجرة العمل الحالية التي تشمل التعديلات السابقة، وليست مقارنة قبول مع APK السابق.
- اختبارات الحسابات والجواهر والتنقل القائمة ضمن تشغيل JVM الكامل، بما فيها W1IdentityIsolation وW1CompletionIdentity وW1AuthBoundary وStudentVideoRegression وLocalGemsPolicy وLocalGemsCoordinator.
- `compileDebugAndroidTestKotlin`: **PASS / BUILD SUCCESSFUL**، بما فيها اختبارات Compose الجديدة للعرض وحالة الزائر والتنقل وغياب الرتبة والصندوق. ترجمة فقط؛ لا instrumentation execution ولا ادعاء قبول هاتف.
- `git diff --check`: PASS.

الأدلة المحلية: `dist/w1-ux-critical/gradle-validation.log` و`sqlite-validation.log` و`ui-test-compile.log`، XML اختبارات JVM تحت `app/build/test-results/` وتقارير Lint تحت `app/build/reports/`. مخرجات البناء والأدلة داخل مسارات ignored؛ التقرير الحالي هو تقرير التنفيذ الوحيد الجديد. قورنت TestTags في ملفات الإنتاج الأربعة المعدلة بنسخة HEAD، وتطابقت القائمة والترتيب.

## APK QA المعزول

- الملف: `dist/w1-ux-critical/ENMATH-W1-QA-UX-CRITICAL-v3.apk`.
- `applicationId = com.ahmadmol.enmath.w1qa`، معزول عن التطبيق الأصلي `com.ahmadmol.enmath`؛ الاسم `EN.MATH W1 QA`.
- `versionCode = 3` أعلى من نسخة QA السابقة ذات الإصدار 2.
- أعيد فحص **ملف APK السابق نفسه** والجديد عبر `aapt dump badging` و`apksigner verify --print-certs`؛ تطابقت الحزمة وشهادة التوقيع، دون الاعتماد على metadata قديمة وحدها.
- شهادة SHA-256 المشتركة: `870a93498b597d9d32abd030b433f924184d1c384f0e940f6f9da85c20700f63`.
- APK SHA-256: `812cb50696583d8e5eea6bc0c7b6170fa15453051e04649596e9c1c3a1dd9cb3`.
- بيانات المقارنة المحلية في `dist/w1-ux-critical/APK_INFO.json`. لم يحدث تثبيت أو اتصال بجهاز. Release الناتج unsigned بحسب إعداد المشروع الحالي، ولا يمثل APK نشرًا موقعًا.

## حدود النطاق والحفاظ على العمل القائم

بدأت الجولة مع تعديلات محلية سابقة في اختبارات الواجهة وManifest وموارد الأيقونة وشاشات Home/Teacher ووثائق أخرى. لم أسترجعها أو أعدلها في هذه الجولة. APK مبني من شجرة العمل الحالية ويشمل تلك التعديلات السابقة؛ ليس حزمة مبنية من فرق هذه الدفعة وحده. تعديلات TeacherWorkspace الظاهرة في `git status` كانت موجودة قبل البدء.

لم تتغير سياسة D06 أو Routes أو TestTags القائمة أو ملفات التخزين/الترحيل أو Ledger أو مفاتيح Legacy. لم تحذف بيانات أو تجر migration. لم يبدأ W2 أو محتوى التاسع أو إعادة تصميم شاملة. لم يستخدم Emulator أو ADB أو تثبيت آلي؛ لا Commit ولا Push.

**القرار:** F01 DONE، F02 PARTIAL بانتظار قبول الهاتف، F04 DONE، F05 DONE. لا بنود BLOCKED في التنفيذ؛ قبول الهاتف وحده ما زال معلّقًا. التوقف عند هذه الدفعة.
