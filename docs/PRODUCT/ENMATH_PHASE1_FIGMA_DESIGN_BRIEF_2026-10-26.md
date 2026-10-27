# EN.MATH — Phase 1: Figma Design Brief

26 أكتوبر 2026 · v1.0 · **Brief للتنفيذ اللاحق في Figma؛ لم يُنشأ ملف Figma أو prototype في هذه الجولة**.

[UX ومصادر الحالات](ENMATH_PHASE1_DUAL_STAGE_UX_SPEC_2026-10-26.md) · [IDs/Routes](ENMATH_PHASE1_SCREEN_ROUTE_MATRIX_2026-10-26.md) · [القرارات](ENMATH_PHASE1_REVIEW_AND_DECISIONS_2026-10-26.md) · [QA](../QA/ENMATH_PHASE1_DUAL_STAGE_ACCEPTANCE_2026-10-26.md).

## 1. المهمة البصرية

منتج Android عربي أولًا لتعلم الرياضيات. طابع G9 واضح ومشجع ومتدرج، وطابع BAC هادئ ومنظم للتحضير. كلاهما EN.MATH؛ الفرق ترتيب المعلومات وسعة الشرح وطريقة الوصول، وليس palette منفصلة أو تكرار محرك. لا mascot إلزامية أو رسوم طفولية أو محاكاة هوية تطبيق آخر. الحركة تشرح انتقال الحالة، لا تعوض غياب المحتوى.

**الحالة الصادقة للتصميم:** G9 CatalogUnavailable هو frame البداية المرجعي الآن؛ BAC تعرض محتوى محليًا تجريبيًا/جزئيًا إذا استُخدم مصدره الحالي. WithData للمنهج المعتمد لاحقًا تبقى template بخانات مرتبطة بعقد وليست نتائج أو دروس مخترعة. لا أرقام نجاح/إتقان/جواهر اصطناعية، ولا أسماء حسابات وهمية توحي بتوفر الخدمة. placeholder المعنون `{عنوان من catalog}` يستخدم في ورقة annotation فقط؛ لا prototype يدّعي درسًا جاهزًا. Login/Register صفحات Future/Provider-gated في صفحة منفصلة.

## 2. بنية ملف Figma وتسميات Frames

| Page | محتواها |
|---|---|
| `00_Readme_Decisions` | نسخة الحزمة، حالة الاعتماد، روابط PH1-D، نطاق محلي/AuthFuture |
| `01_Foundations` | semantic variables Light/Dark، النصوص، spacing، math direction، grid |
| `02_Components` | auto-layout component sets مع variants أدناه |
| `03_Entry_Local` | E01/E02/E03/E04/E05/E10/E11/E13/E14/E16 والتبديل |
| `04_Grade9` | H-G9/Courses/Unit/Lesson/Practice/Planner/Review/Profile؛ غير المتاح واضح |
| `05_Bac` | H-BAC/BookPair/Unit/Lesson/Practice/Planner/Review/Profile |
| `06_Shared_States` | Search/Library/Tools/Gems/Settings وحالات الأخطاء والخصوصية |
| `07_Auth_Future` | E06/E07/E08/E09/E12/E15؛ banner خارج viewport المنتج يوضح الشرط |
| `08_Prototype_Flows` | مسارات دخول محلي وتبديل وعودة وخطأ حفظ ومحتوى غير متاح |
| `09_Handoff_QA` | specs/redlines/semantics/source bindings ومراجع الأدلة |

التسمية: `ID / Stage(G9|BAC|Shared) / Mode(LocalGuest|LocalStudent|AuthFuture) / Theme(Light|Dark) / Locale(ar|en) / State / Width / FontScale`. مثال بنيوي `H-G9/G9/LocalGuest/Dark/ar/CatalogUnavailable/360/100`؛ لا تستعمل مثال حساب أو نتيجته كبيانات حقيقية. تصدير PNG للمراجعة فقط عند إنجاز Figma لاحقًا، مع نفس ID ونسخة التصميم.

## 3. أحجام الشاشات وAuto Layout

الأرقام logical px في Figma عند 1× تقابل dp للتخطيط؛ النصوص تقابل sp عند الخط الافتراضي. ليست device screenshots ولا مقاسات physical pixels. insets ديناميكية في التطبيق؛ طبقات safe area منفصلة وقابلة للتغيير، لا status bar مزيفة داخل scroll.

| Frame | الاستخدام المطلوب |
|---|---|
| 360×800 | الأساس compact phone، نسختا ar/en |
| 390×844 | فحص متوسط، نص طويل وحقل/IME |
| 412×915 | فحص هاتف أعرض؛ لا تضخيم الزخرفة لملء الفراغ |
| 320×640 | stress عرض/ارتفاع، scroll بلا horizontal page overflow |
| عرض 600 | فحص انتقال adaptive؛ ما زال ترتيب القراءة والهامش ثابتين دلاليًا |
| عرض 840 | handoff اختياري للـ tablet، contentMax 720؛ لا شرط لقبول الهاتف الأول |

phoneInset 16، tabletInset 24، gutter 12، sectionGap 24، cardInset 16؛ المحتوى التعليمي max 720، النماذج max 520 من النظام الحالي. عند عرض <360 أو font 200% تصبح المجموعات متعددة الأعمدة عمودًا واحدًا. بطاقتا BAC جنبًا إلى جنب فقط إذا لكل منهما ≥160dp مع عنوان وCTA بلا قص؛ وإلا stack. 4-column phone grid بفواصل 12 وouter16 مرجع محاذاة، وليس فرض تقسيم كل بطاقة إلى أعمدة.

زر 52dp حد تصميم أساسي، target تفاعلي ≥48dp؛ input min56dp وينمو للنص والخطأ؛ icon24dp وtouch أكبر، card radius16، hero20، button12، sheet28. لا height ثابت للنص. entry artwork حتى160dp كمقترح (القائم ResourceDimensions.entryArtwork=200) ويختفي/يصغر عند IME أو ارتفاع ضيق؛ يحسم مع PH1-D02 دون تعديل التوكن الحالي الآن.

الشريط السفلي القائم CleanBottomBar له `FloatingTopPadding=10`, `BarContentHeight=68`, المجموع78 قبل insets، دائرة Home52 وأيقونة22 ونص11sp. ResourceDimensions.navHeight=80 token آخر؛ لا توحيدهما رقميًا في الكود ضمن التصميم. يرسم Figma الشريط الفعلي كbaseline78 مع نمو مقاس النص المحسوب وsafe area منفصلة، ويختبر 200% دون قص. تغيير المقاسات/الترتيب يتطلب قبول PH1-D02 وحماية tags/navigation.

## 4. Tokens الألوان المستمدة من الكود

المصدر: `core/design/tokens/ResourceBaseline.kt` داخل `ResourcePackPresentation(enabled=true)`؛ **Dark effective** يتضمن ApprovedHomeVisuals overrides. المدخل المحلي الحالي خارج wrapper ويستخدم studentColorScheme في Theme.kt؛ **نوصي** بتوحيد مدخل الطالب إلى هذه semantic palette مستقبلًا بعد PH1-D03، لا تغيير Theme المعلم أو افتراض أن القيم موحدة الآن.

| Figma semantic token | Light | Dark effective | استعمال |
|---|---|---|---|
| `color/bg` | #F7F9FC | #0B1322 | الخلفية |
| `color/surface` | #FFFFFF | #0D2235 | سطح بطاقة؛ Dark من override |
| `color/surfaceVariant` | #EDF2F8 | #1B2B42 | ثانوي/مدخل |
| `color/textPrimary` | #0F1F38 | #F2F6FF | النص الأساسي |
| `color/textSecondary` | #526178 | #ABBAD0 | نص مساعد |
| `color/primary` | #175CD3 | #56B4FF | CTA/اختيار؛ Dark override |
| `color/onPrimary` | #FFFFFF | #092140 | نص فوق CTA |
| `color/primaryContainer` | #DCEBFF | #173E76 | stage badge/عنصر مختار |
| `color/onPrimaryContainer` | #123368 | #E0ECFF | نصه |
| `color/secondary` | #047A64 | #61D7BB | إبراز تقدم دون ادعاء نجاح |
| `color/secondaryContainer` | #D7F6EC | #164B40 | سطح حالة إيجابية |
| `color/onSecondaryContainer` | #0B4D3F | #D8F8EF | نص الحالة |
| `color/tertiary` | #6941C6 | #C4B5FD | accent محدود للعرض |
| `color/outline` | #6D7D94 | #91A2BB | حدود تفاعلية/أيقونات مهمة |
| `color/outlineDecorative` | #D9E3F0 | #205374 | فواصل زخرفية؛ لا دليل اختيار وحيد |
| `color/error` | #C02D3A | #FFB4B9 | أيقونة/نص خطأ |
| `color/errorContainer` | #FFE8E9 | #722834 | خلفية خطأ |
| `color/onErrorContainer` | #7D1424 | #FFE7EA | نص الخطأ |

لا نغير stage من لون: الشارة والنص دائمًا. G9 يوسع استخدام secondaryContainer لتوجيه الخطوة التالية، وBAC يكتفي بإبراز primary والكتابين؛ كلاهما نفس variable collection. الجوهر البنفسجي/الأزرق المعتمد والتركوازي يظل في tokens لا ألوان مشبعة مستقلة للمدرسة. الألوان الخاصة برسوم الكتب زخرفية لا مؤشر منهج أو وصول.

Hero مسموح مشتق من ApprovedHomeVisuals: Light #245B98→#145763 مع نص أبيض، Dark #005BD4→#065C86 مع نص أبيض؛ يُعاد قياس النص فوق كل موضع خصوصًا بعد overlay، ولا نستخدم gradient جديدًا للـ Dark. الواجهات البسيطة surfaces أولًا، لا gradient وراء فقرات تعليمية طويلة.

**هدف قبول داخلي للتباين:** نص عادي ≥4.5:1، نص كبير ورموز/حدود وظيفية ≥3:1؛ ليست شهادة امتثال تلقائية. حُسبت هذه الأزواج opaque محليًا بمعادلة sRGB relative luminance، بلا اختبار جهاز: Light body/surface 16.49، secondary text/surface 6.29، onPrimary/primary 5.99؛ Dark body/surface14.96، secondary text8.22، onPrimary/primary7.19؛ Light hero الأبيض عند الطرفين6.94/8.16. هذه قياسات الأزواج المحددة فقط؛ أي opacity/disabled/gradient/chart يحتاج قياسًا جديدًا، والفواصل الزخرفية لا تحل محل focus/selection contrast.

## 5. Typography واللغة والمعادلات

الخطوط المحلية: `app/src/main/res/font/ibm_plex_arabic.ttf` للعربية و`inter.ttf` للإنجليزية؛ لا استبدال font غير مرخص أو اعتماد وزن غير متوفر دون QA rendering. القياسات التالية من ResourcePackPresentation؛ max line count للتعليم غير ثابت.

| style | size/line height sp | weight | الاستخدام |
|---|---|---|---|
| headlineLarge | 28/38 | 700 | عنوان Entry/Home، مع التفاف |
| headlineMedium | 24/34 | 700 | عنوان درس/صفحة |
| headlineSmall | 22/32 | 700 | عنوان رئيسي صغير |
| titleLarge | 20/30 | 600 | قسم/اسم وحدة |
| titleMedium | 16/26 | 600 | عنوان بطاقة/CTA مناسب |
| bodyLarge | 16/27 | 400 | الشرح والحقول المهمة |
| bodyMedium | 14/24 | 400 | وصف مساعد |
| labelLarge | 14/22 | 600 | زر/شارة |
| bodySmall | 12/20 | 400 | metadata فقط؛ ليس بديل شرح الدخول |

تصميم 100% و200%؛ 150% فحص وسيط. يزداد الخط والـ line height وتتمدد البطاقة/الزر؛ لا screenshot scaling يصغر الأزرار. لا قص اسم المرحلة أو رسائل الحفظ أو شروط الزائر. أعداد صحيحة من formatter اللغة، القيم كما في المصدر؛ لا تغير عرض الأرقام معنى معادلة أو source ID.

ArabicRTL على layout root، EnglishLTR للإنجليزية. استخدم Start/End بدل Left/Right في annotations. أرقام الهاتف/email/OTP مستقبلًا LTR داخل حقل مسمى عربيًا. المعادلات دائمًا LTR مع منطقة مستقلّة: الكسور/الأسس/lim والأقواس والمحاور لا تعكس. الكتلة الطويلة تسمح scroll أفقيًا داخل منطقة Math فقط مع indication وبديل مقروء؛ لا يجعل ذلك الصفحة كلها scroll أفقيًا. لا عرض صيغ تعليمية كصورة فقط. Math semantic label مطلوب لـ TalkBack، والنص المحيط يُقرأ قبلها وبعدها.

## 6. مكتبة Components وVariants

كل component: Auto Layout، padding متغير من tokens، semantic annotation ودور accessibility، property `stage` فقط إن يغير العرض، لا يقرر reward أو ownership. Mapping أدناه هدف لاحق، لا classes جاهزة.

| Component | Properties/Variants إلزامية | محتوى وربط |
|---|---|---|
| StageBadge / IdentityBadge | G9/BAC؛ Guest/LocalStudent/AuthFuture؛ Light/Dark | من context، لا selectable stage editor داخل الملف |
| StageChoiceCard | normal/focused/pressed، contentUnavailable/partial | stage + وصف؛ لا يخلق UUID |
| EntryActions | LocalOnly/AuthReady، guestNew/guestExisting | تظهر أفعال متاحة فقط، زائر primary |
| LocalProfileRow | current/normal/recovery، sameName | اسم+مرحلة+نوع دون balance/answers |
| Notice / StatePanel | info/offline/readError/writeUnconfirmed/wrongStage/reviewRequired | عنوان+سبب+CTA حقيقي، لا لون فقط |
| NextStepCard | G9 step / BAC resume، loading/available/completeAll/unavailable | ContentRef + next action؛ لا عنوان ثابت |
| UnitPathNode | available/inProgress/completed/reviewDue/unavailable/prerequisite | لكل حالة label وsemantics وترتيب مستقل عن curve |
| BookCard / BookPair | partial/available/unavailable/completed، stacked/row | book manifest ونطاق completion؛ لا 12 درسًا افتراضيًا |
| PlanSummary / TaskRow | unimplemented/noPlan/withPlan/saveError؛ TIME/ACHIEVEMENT/HYBRID | لا نسبة مخترعة؛ المهمة manual موسومة |
| ReviewCard | noEvidence/due/reviewed/unsupported | attempt/skill refs موثقة |
| LessonBlock / MathBlock | text/rule/workedExample/checkpoint/unsupported | same renderer، مادة المرحلة فقط |
| PracticeItem / Result | unanswered/checking/correct/incorrect/ungraded/storageError | rubric result، answer assistance واضحة، خطأ حفظ لا يعرض completion |
| GemBalance / Unlock | loading/known/error/notApplicable/ownerMismatch؛ owned/eligible/insufficient | نفس ledger، لا رصيد صفر عند failure؛ لا سعر UI مستقل |
| TextField / PasswordField | empty/focused/filled/invalid/disabled/loading | label ثابت + supporting text؛ Password فقط AuthFuture |
| Primary/Secondary/TextButton | normal/pressed/focused/loading/disabled | label يتسع، منع double-submit محليًا، target48+ |
| StudentNav / SubHeader | RTL/LTR، selected tab، fontScale | routes القائمة وheader واحد؛ focus screens موثقة |

لا تستخرج component جديدًا إذا كان الحالي يفي بالعقد؛ المخرج Figma يمكن أن يترجم إلى existing Components/Resource tokens وstage-specific presentation. لا setValue لحالة حساب في prototype تدعي تخزين التطبيق الحقيقي.

## 7. Frames وحالات الصفحة المطلوبة

لكل شاشة أساسية مسجلة في المصفوفة: `State=Loading / WithData(bound) / Empty / OfflineLocal / OfflineMissing / ReadError / WriteUnconfirmed / WrongStage أو ReviewRequired عند صلتها`. إذا حالة لا تنطبق يوثق سبب N/A؛ لا رسم صفحة واحدة لكل خطأ دون تحديد السبب والـ action.

Minimum review set للـ Entry المتخصص وHome/Courses/Lesson/Practice/Planner/Profile: **2 stages ×2 themes ×2 locales =8** variants عند 100%، ويضاف نفس set عند200% للشاشات ذات النص/CTA الحرجة =16 configuration لكل نوع شاشة. بعض entry Shared لا تضاعف stage بلا فرق؛ تسجل قاعدة الاشتراك. Frames فرعية تؤدي إلى نفس component state لا تحتاج تكرارًا يدويًا إذا variable/component يثبت التغطية، مع export أدلة للـ core set.

| الأولوية | Frames |
|---|---|
| F0 | E03/E04/E05/E10/E11/E13، H-G9 CatalogUnavailable، H-BAC partial catalog، E16 Error |
| F1 | H-G9 valid catalog template وH-BAC resume/empty، C01–C04، L01، P01/P02، PLAN01، REV01، PROF01 |
| F2 | Shared library/search/solver/gems/settings، E14 conflicts، planner task/history/review item |
| Future | E06/E07/E08/E09/E12/E15؛ مطبوعة «مشروط بمزود» خارج canvas المنتج، لا توصل إلى success حقيقي |

أفقيًا يختلف التصميم: G9 مسار الوحدة تحت next step وأهداف مبسطة؛ BAC بطاقتا كتابين وخطة/مراجعة، لا نسخ مسیر BAC الحالي وتسميته G9. زخرفة المسار لا تحمل إكمالًا أو قفلًا خارج availability. لا رسوم تشوش المعادلات، ولا حجب محتوى مجاني خلف Gems.

## 8. مسارات الـ Prototype والتفاعلات

المسارات المطلوب رسمها لاحقًا: New→stage→guest notice→HomeUnavailable G9؛ BAC guest→Home partial source؛ اختيار local profile موجود→Home stage الفعلية؛ switch→cancel يبقي الحالي؛ switch→read failure يبقي الحالي؛ ضغط متكرر Create لا يخلق صفين؛ wrong stage content→شرح→catalog؛ عملية حفظ غير مؤكدة→retry بلا نجاح زائف؛ guest wallet→notApplicable؛ reviewed legacy unresolved→leave preserved.

أي WithData flow دراسي يحتاج content مصدره معتمد لهذه المرحلة، أو يوقف عند template annotation بدل إيهام نجاح Lesson. AuthFuture يشرح الحالات ولا يستخدم OTP ثابتًا أو «تم إرسال» تجريبيًا. motion مقترح120–200ms fade/size محدود؛ reduced motion يجعلها فورية، بلا pulsating gems أو auto-scroll للمسار. الخطأ يُعلن بتركيز/semantics ولا يحرك الشاشة فجأة.

## 9. تسليم Figma إلى Compose ومعيار المراجعة

لكل frame: ID+route+current/proposed Composable؛ الـ context required؛ data field bindings؛ layout min/max/stretch؛ paddings/radii؛ variable names؛ state enum؛ CTA target/guard؛ back behavior؛ focus order/TalkBack labels؛ math direction؛ tags الحالية/المقترحة؛ حالة العلم بالمحتوى؛ قرار PH1-D الحاجب إن وجد. كل icon اسم vector قائم أو حاجة أصل موثقة؛ لا صور من مصادر مجهولة الحقوق.

مطلوب صفحة مقارنة G9/BAC جنبًا إلى جنب للثيمين توضح ثلاثة فروق بنيوية على الأقل: order/hierarchy، navigation into content، presentation of plan/review. يراجع المالك كل مرحلة مستقلة. قبول تصميم Figma لا يعني اجتياز التخزين/Auth/QA؛ ولا يعني موافقة برمجة مرحلة ثانية قبل مراجعة الحزمة والدفعة المحددة. لم تُشغل أدوات Figma أو تُرفع ملفات إلى خدمة خارجية هنا.
