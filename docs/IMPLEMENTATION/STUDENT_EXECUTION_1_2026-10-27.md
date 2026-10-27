# EN.MATH — Student execution 1 of 2

27 أكتوبر 2026 · التنفيذ داخل النسخة المنظفة · لا Commit ولا Push.

المرجع: `feature/ui-refinement`، HEAD `8175570f064eee5fc53bf36e244e6cb2d939fcf4`. المشروع الأصلي لم يستبدل. احتُفظ بالتعديلات المحلية المنقحة الموجودة قبل التنفيذ، ولم يُستورد Git قديم أو Stash أو Cherry-pick.

## المنفذ

- Splash محلي، استعادة الملف الصالح، تعريف مختصر قابل للتخطي، اختيار المرحلة، مدخل مخصص لكل مرحلة، إشعار تخزين الزائر، إنشاء ملف باسم اختياري، قائمة الملفات والتبديل المؤكد من Profile. الملف المحلي موصوف بوضوح دون ادعاء حساب سحابي، ومدخل التطوير للمعلم محفوظ.
- Home للتاسع: الخطوة التالية، هدف الأسبوع، المراجعة، مسار الوحدات والأدوات؛ غياب المحتوى معلن. Home للبكالوريا: الاستمرار، الكتابان من الكتالوج الفعلي الجزئي، الخطة، أولوية المراجعة والأدوات.
- Study/Books/Units/Lessons/Practice/Results/Progress/Review/Planner وContinue مترابطة. إعادة استخدام محرك الدرس والتدريب والتصحيح الحالي. النتائج تعرض محاولة محفوظة، والإكمال منفصل عن الإتقان. أحدث إجابة صحيحة تزيل الخطأ من المراجعة الحالية.
- Planner دائم لكل UUID ومرحلة: يوم/أسبوع/شهر، إنجاز/وقت/مختلط، مادة حرة، إضافة/تعديل/حذف، قياس جزئي يدوي، ربط درس صالح اختياري، نقل المتبقي للفترة التالية بتأكيد، وسجل نسخ قديمة كامل. الأسبوع الافتراضي وبدون ساعات إلزامية؛ توقيت `Asia/Damascus` وأسبوع يبدأ الاثنين.
- ملخص حقيقي للخطة في Home وProfile. الوقت والإنجاز منفصلان. الإنجاز اليدوي لا يكتب إكمال درس أو مكافأة. ربط درس أُكمل سابقًا أو ترحيل مهمة لا يعيد احتساب الإكمال القديم.
- Search/Library/Settings قائمة ومتصلة؛ أدوات Solver/Explore وموارد Gems تحجب محتوى BAC عن G9، مع بقاء سجل الفتوحات والمحفظة القديمة محفوظًا. بقيت التبويبات الخمسة وزر Home المركزي، ولا شريط سفلي في Planner/Review والشاشات الفرعية.
- حراسة البيانات داخل المستودعات: owner/stage/edition/generation، فحص مراجع المحتوى والمحاولات الجديدة، رفض جلسة قديمة بعد A→B→A، revision وcommand receipts للخطة، ورفض إعادة نفس أمر التدريب بمحتوى مختلف. المحاولات الجديدة تحفظ stage/edition/commandId بصورة إضافية متوافقة مع القديم.
- استيراد Legacy جديد غير متحقق من المنهج مرفوض عند حد المستودع. الملفات المستوردة سابقًا تحتفظ UUID/stage/legacyWalletKey وتاريخها الخام، مع ReviewRequired وفصلها عن مؤشرات المنهج. قراءة التاريخ القديم متاحة لصاحب الملف محليًا. لا إعادة تصنيف تلقائي أو مسح.

## الملفات الأساسية

تحت `app/src/main/java/com/ahmadmol/enmath/`:

- جديد: `core/data/StudentCatalog.kt`, `core/data/PlannerRepository.kt`, `features/planner/PlannerScreen.kt`, `features/student/DualStageExperience.kt`.
- تعديل: `core/data/{LearningRepository,LocalDemoRepository,ProductRepository}.kt`, `core/model/ProductModels.kt`, `core/AppViewModel.kt`.
- ربط: `core/navigation/{ENMathApp,ProductGraphs,Routes}.kt`, `features/auth/LocalIdentityScreen.kt`, `features/profile/ProductProfileScreens.kt`, `features/courses/CourseScreens.kt`.
- حراسة الموارد: `core/localgems/LocalGemsCoordinator.kt`, `features/localgems/LocalGemsScreens.kt`.
- اختبارات جديدة: `StudentBoundaryRegressionTest`, `StudentPlannerTest`, `StudentLearningCycleTest`. جرى تحديث fixtures لاختبار ملفات مستوردة بالإصدار القديم مع رفض الاستيراد الجديد غير المراجع.

## التحقق الفعلي

قبل تعديل المسارات الحساسة أُضيفت ثلاثة اختبارات كشفَت الثغرات الثلاث فعليًا وفشلت، ثم نجحت بعد إصلاح حدود المستودع. الاختبارات النهائية تشمل عزل المالك والمرحلة والإصدار، ثبات UUID والمحفظة القديمة، فشل الكتابة وإعادة التشغيل، التكرار، النقل الجزئي، التاريخ، حراسة التنقل، ومطابقة مصدر المعلم قبل/بعد تعلم الطالب وخطته.

الأوامر: `:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:compileDebugAndroidTestKotlin --offline --console=plain`، و`tools/test_local_gems_sqlite.py` باستخدام Python المرافق.

الأعداد والنتيجة النهائية مسجلة بعد اكتمال التحقق في القسم التالي. تجميع androidTest لا يعني تشغيلها. لم تُستخدم Emulator أو ADB أو هاتف أو قاعدة بيانات مستخدم حقيقية.

## حدود المحتوى والمزود والقبول

منهج G9 غير مضمّن، وكتالوج BAC تجريبي جزئي؛ لا ادعاء بمنهج رسمي كامل. لا قوالب تعليمية متحققة لـG9 في Solver/Explore، ولا موارد فيزياء كاملة مرخصة. هذه حالات عدم توفر معلنة. Auth/OTP/استعادة كلمة المرور/Claim السحابي تبقى محجوبة لغياب مزود إنتاجي متحقق. لا تظهر كأفعال إنتاجية ناجحة.

العربية RTL والإنجليزية LTR عبر Theme القائم، والمعادلات عبر Math renderer القائم. استُخدمت قوائم قابلة للتمرير وأزرار بحد لمس 48dp ومراعاة IME/insets. القبول البصري على هاتف بخط 100/150/200% وTalkBack والثيمين لم يُنفذ؛ سيديره المستخدم لاحقًا، ولا يُعلن PASS له.

## سلامة البيانات

لم يتغير `LocalLearnerStore` أو مخطط SQLite أو قواعد مكافآت وأسعار `LocalGemsPolicy`. لم يتغير أي ملف في `features/teacher/`. تطابقت نسخة `TeacherWorkspace.kt` الأصلية مع بصمتها المحفوظة السابقة، وتعلم الطالب وخطته لا يغيران بايتات مصدر المعلم في fixture. اختبارات SQLite تفحص جداول المحفظة الستة، rollback والتكرار والمالك القديم دون إعادة ترقيم أو منح رجعي. هذه أدلة fixtures، وليست ادعاء فحص بيانات هاتف حقيقية.

## نتيجة التحقق النهائية

- JVM: **212 اختبارًا، 0 فشل، 0 أخطاء، 0 تخطي**؛ منها 24 اختبارًا جديدًا في هذه الدفعة.
- SQLite المضيف: **10 اختبارات ناجحة، 0 فشل**، على قواعد مؤقتة فقط.
- Debug Build: **BUILD SUCCESSFUL**؛ `app/build/outputs/apk/debug/app-debug.apk`.
- Lint Debug: **0 errors, 9 warnings**. التحذيرات في target SDK الحالي وbackup metadata وبعض مكونات Compose القائمة؛ لا تعديل SDK لإخفائها.
- `compileDebugAndroidTestKotlin`: **ناجح**؛ الاختبارات لم تُشغّل على جهاز.
- `git diff --check`: ناجح. الفرع والـHEAD لم يتغيرا. لا Commit أو Push.
- مقارنة **168 ملفًا من التطبيق الأصلي** بخط الأساس الخاص المحفوظ: **0 اختلاف**. هذه المقارنة تخص الأصل، بينما جميع إضافات التنفيذ في النسخة المنظفة المستقلة.
- عقد `CurriculumCatalog` يجمع كتب وأسئلة ومجموعات تدريب كل مرحلة وإصدار لتزويد المحتوى لاحقًا. إتاحة الشاشات التعليمية تُفحص من الكتالوج الفعلي. الرجوع الصريح إلى درس مستورد يسجل إكمال الإصدار الحالي بلا مكافآت رجعية.

الأدلة القابلة للقراءة محليًا: `app/build/test-results/testDebugUnitTest/` و`app/build/reports/tests/testDebugUnitTest/index.html` و`app/build/reports/lint-results-debug.html`، إضافة إلى `STUDENT_EXECUTION_1_VERIFICATION_2026-10-27.json` في هذا المجلد.
