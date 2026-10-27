# EN.MATH — Phase 1: عقود الهوية والمرحلة وإصدار المنهج

26 أكتوبر 2026 · v1.0 · **تصميم عقود للمراجعة فقط؛ لا migration أو schema أو Kotlin منفذ هنا**.

مرجع الاعتماد: [القرارات PH1-D](../PRODUCT/ENMATH_PHASE1_REVIEW_AND_DECISIONS_2026-10-26.md). مصادر الحقيقة الحالية: `core/model/Models.kt`، `LocalLearnerStore.kt`، `LocalDemoRepository.kt`، `LearningRepository.kt`، `ProductRepository.kt`، `AppViewModel.kt`، `core/localgems/**` و`assets/local_gems/schema.sql` تحت `app/src/main/java/com/ahmadmol/enmath/` أو assets حسب المسار.

## 1. الثوابت والحالة القائمة

لا استبدال لنظام الهوية أو ledger. الموجود: `StudyStage.G9/BAC`، UUID في `User.localId`، stage ثابتة، guest مستقل لكل مرحلة، registry v1، snapshots مستقلة، owner guard، journal وpendingGemEvents، SQLite Gems v1. النواقص: curriculumEdition موثقة وaccount session حقيقية وحراسة stage شاملة ومراجعة توافق Legacy.

العقد المستهدف إضافي، ولا يعني إعادة كتابة كل سجل قديم. لا تغيير قيم enum المسلسلة `G9/BAC` إلى أسماء الخطة `GRADE_9/BAC_SCIENTIFIC` داخل المخازن؛ يمكن mapping عند حدود API مستقبلية بإصدار صريح. الاسم أو البريد أو الهاتف ليس owner جديدًا. بيانات Teacher ليست Student context.

## 2. قاموس الأنواع وحدود السلطة

| الحقل/النوع المفاهيمي المقترح | القاعدة | ربطه بالحالي |
|---|---|---|
| `ownerId: LocalLearnerId` | UUID ثابت لمتعلم محلي واحد، لا يتغير بتعديل الاسم أو اللغة أو دخول حساب مستقبلي | يساوي `LocalLearner.id` و`User.localId` الحاليين للملفات المحلية |
| `stage: StudyStage` | G9 أو BAC فقط؛ ثابتة لهذا الملف؛ unknown ليست BAC | محفوظة أصلًا في profile؛ لا نكرر اختيارها عند كل دخول |
| `sessionMode` | `GUEST_LOCAL / LOCAL_STUDENT / AUTHENTICATED`؛ توصيف جلسة، ليس AccountType.Teacher | Guest/Student الحاليان يتحولان لتمثيل محلي؛ Student لا يصبح AUTHENTICATED دون مزود |
| `accountPrincipal` | اختياري مستقبلًا: provider namespace + opaque accountId موثّق + accountStage + verification state | غير موجود؛ لا يُستخرج من بريد الديمو أو اختيار دور |
| `accountBinding` | رابط معتمد من principal إلى owner المحلي الصحيح؛ نفس stage فقط، بلا استبدال UUID | عقد مستقبلي؛ لا تعديل تلقائي لدور guest الحالي |
| `curriculumEditionId` | معرّف immutable لإصدار مصدر معتمد؛ stage داخله مُتحقق منها، لا نص «2026» بالتخمين | غير موجود؛ لا ننسب طبعة رسمية إلى Demo |
| `editionStatus` | `VERIFIED / LEGACY_UNVERIFIED / REVIEW_REQUIRED / UNAVAILABLE` | طبقة قراءة مقترحة؛ LEGACY_UNVERIFIED حالة وليست ID طبعة رسمية |
| `activeEdition` | إصدار معتمد مختار لعرض تعلم جديد، مستقل عن بقاء التاريخ بإصداراته | تغييره لا يغير stage أو يمنح إكمالًا/جواهر |
| `walletOwnerKey` | opaque مفتاح legacy حرفي أو UUID وفق resolver القائم؛ **ليس ownerId بالضرورة** | resolver الحالي يفضل legacyWalletKey ثم localId ثم بريد الديمو |
| `contextGeneration` | رقم جلسة ذاكرة يتغير عند تبديل الهوية/مرحلة السياق/الإصدار المعروض؛ يرفض نتائج async القديمة | يوسع نمط generation الحالي في LocalGemsCoordinator دون تغييره ضمن هذه الجولة |

الهوية الأساسية ثابتة، بينما sessionMode يمكن أن يصف ملف طالب محلي مربوطًا مستقبلًا بحساب مثبت. **Guest نفسه لا يتحول إلى Student بتعديل bool**: claim إلى هدف مستقل معلوم الملكية مع بقاء مصدر الضيف، وفق القسم 9. لا نخلق principal لسد غياب Auth.

مستخدم Legacy بلا UUID يمثل `LegacyScopeRef` منفصلًا حتى تثبت المراجعة، ولا يلف بقيمة UUID مصطنعة مشتقة من email. إذا كان ملف UUID مستوردًا سابقًا بمرحلة غير مناسبة، لا نغير stage فيه؛ نبقيه والتاريخ والمحفظة ونفتح ReviewRequired لحل معتمد مستقل.

## 3. سياق القراءة والأوامر

هذه حقول تعاقدية وليست classes مضافة:

| العقد | الحقول المطلوبة | التحقق/النتيجة |
|---|---|---|
| `StudentContext` | ownerId, stage, mode, editionStatus, activeEdition?, generation | owner موجود وsnapshot صالح؛ edition المعتمدة تطابق stage؛ غير ذلك رفض/مراجعة دون fallback |
| `ContentRef` | editionId, stage, contentId, contentRevision, kind, sourceRef, availability | manifest موثوق وid/revision فعليان؛ لا يكتفى بـ startsWith أو عنوان عربي |
| `LearningCommand` | commandId, contextOwner, stage, editionId, contentRef, capturedGeneration, kind, payload, occurredAt | context مطابق وقت التنفيذ، command id ثابت عند retry، content متاح، payload مطابق rubric |
| `LearningReceipt` | commandId, ownerId, recordedAt, outcome, provenance | ACK بعد commit فقط؛ نفس command مع payload مختلف Conflict لا silent success |
| `LearningEvent` | eventId, ownerId, stage, editionId, revision, type, evidenceRef, assistance, source, time | الحدث الجديد في نطاقه؛ لا تحويل ضغط يدوي إلى evidence تعلم مستقل |
| `StudentUiState` | verified context + catalog view + progress + plan/review capability + wallet state | خالٍ من بيانات مالك آخر؛ لا fallback Demo عند empty أو error |
| `DeferredAction` | originOwner, intendedStage, edition, safe action descriptor, requestId, createdAt | مستقبل Auth؛ لا جواب/سر داخل route؛ بعد الدخول تعاد صلاحية الهدف وملكية التنفيذ قبل عرضه |

الـ Route قد يحمل `id` أو `stage` كنية عرض فقط؛ لا يثبت owner أو صلاحية. Query على catalog، fetch درس، read attempt/history، bookmark، open/complete lesson، record practice، submit، search، planner، list/purchase/open gem resource: كلها تمر بعقد scope، بما فيها الاستدعاء المباشر للمستودع. `guarded` UI ليس boundary أمنية كافية.

نتائج موحدة: `Recorded / AlreadyRecorded / OwnerChanged / WrongStage / UnknownEdition / ContentUnavailable / InvalidEvidence / StorageUnconfirmed / ReviewRequired`. لا تستعمل Error عامة لإكمال المعاملة ببيانات وهمية. إكمال درس لا يساوي verified evidence؛ تحديث projection لا يصك مكافأة.

### تسلسل عملية تعلم مقترح يحافظ على القائم

1. التقاط owner/stage/edition/generation وcommandId عند بدء الجلسة؛ لا إعادة ربط إجابات SavedStateHandle بمالك جديد.
2. إعادة التحقق داخل طبقة الأمر قبل القراءة والكتابة؛ رفض command قديم بعد التبديل.
3. تسجيل نتيجة التعلم وcommand receipt وpending event المؤهل في commit الشخصي الواحد كما يدعم مسار UUID الحالي.
4. إعلان نجاح التعلم بعد الحفظ. عرض Gems منفصل loading/ledger ACK؛ لا توهم أن نجاح التعلم يعني وصول مكافأة.
5. outbox يرسل at-least-once إلى ledger بمفتاحه الحالي؛ بعد commit DB يُقر الحدث؛ تكراره لا يمنح ثانية.
6. خطأ ACK أو انقطاع بين المخازن يحتفظ بالأصل/الحدث؛ retry بنفس ID؛ لا rollback يدوي بتصفير المحفظة. async من generation قديم لا يعرض إشعارًا أو رصيدًا للحالي.

هذه إضافة عقود حول الآليات القائمة؛ لا دمج DB وPrefs في migration واسعة دون حاجة أو اعتماد. crash atomicity عبر كل المخازن ليست ادعاءً نحققه بمجرد هذه الوثيقة.

## 4. عقد Catalog / curriculumEdition

Manifest معتمد مقترح يتضمن: `editionId`, `stage`, `jurisdiction`, `subjectId`, `sourceTitle`, `sourceEditionLabel`, `sourceHash/ref`, `contentVersion`, `reviewedBy`, `reviewedAt`, `rightsRef`, `books/units/items`, `coverageStatus`, `publishedAt`. لا يملأ أي من هذه بقيمة توحي بمراجعة لم تحصل. إصدار كتابين BAC يمكن أن يكون manifest واحدًا يربط طبعات كل كتاب صراحة أو إصدارين تحت مجموعة معتمدة؛ يحسم PH1-D04 قبل IDs النهائية.

- content availability منفصلة: `BUNDLED / DOWNLOADED_VERIFIED / REMOTE_NOT_AVAILABLE_OFFLINE / NOT_PUBLISHED / REVIEW_BLOCKED`؛ تعادل أسماء الحالة الفعلية لاحقًا، لا نقل حالّ لمحتوى لا يملكه.
- عرض «مكتمل من المتاح» denominator من العناصر المنشورة الفعلية، مع كلمة «حزمة جزئية». نسبة تغطية الكتاب الرسمي لا تظهر دون فهرس كامل معتمد.
- revision تغيير محتوى داخل edition لا يمحو المحاولات السابقة؛ assessment يحتفظ بـ rubricVersion/reference. إذا تغير معنى السؤال أو الإجابة يراجع التكافؤ، ولا تصنف محاولة قديمة صحيحة/خاطئة من answer أحدث بصمت.
- mapper القديم: `(sourceScope, legacyContentId, knownContentHash) → verified stage/edition/contentRef + evidence + reviewer`. الأسماء المتشابهة أو id `limits-practice` وحده ليس إثبات طبعة.
- ids legacy تبقى قابلة للقراءة. لا تغيير مكافأة `LESSON:<id>` لمجرد إضافة edition إلى catalog؛ القسم 7 يحدد حماية dedup.
- الأداة الرياضية تطلب capability/topic templates للمرحلة. غياب template G9 لا يسمح بإظهار template BAC؛ engine مشترك مع Unsupported صريح.

## 5. عقود Progress / Attempts / Planner / Review

| المجال | حقول/قواعد مقترحة |
|---|---|
| Completion | owner+stage+edition+contentRef، completionEvent/وقت ومصدر، منفصل عن verification؛ imported/manual مميزان |
| Attempt | attemptId ثابت + commandId + scoped content/revision/rubric + answers + assistance + outcome/score nullable + time؛ unknown legacy لا يعاد توليد attemptId ثم يمنح مكافأة |
| MasteryEvidence | skillRef موثق بالمرحلة/الطبعة، attemptRef، assisted/independent، تاريخ ونوع إثبات؛ نسب غير معايرة لا تعرض كيقين |
| StudyPlan | planId, ownerId, stage, edition refs اختيارية للمهام غير المرتبطة بمحتوى، timezone, period DAY/WEEK/MONTH, revision, goal mode TIME/ACHIEVEMENT/HYBRID |
| PlanTask | taskId, subjectRef, optional ContentRef, target وunit متجانسان، due/local period، progress source LEARNING_EVENT/TIMER/MANUAL، parent/carry lineage، history |
| ReviewItem | itemId, scope, evidence refs, reason, dueAt?, status, resolver version؛ لا WeakSkill افتراضية لغياب محاولات |
| Resource | resourceId, stageAllowlist, edition/source refs, rights/access/free flags, hash/download availability؛ موارد فيزياء BAC المؤهلة مجانية دائمًا |
| Settings | اللغة والثيم عامان كما في enmath_demo؛ student context لا يضم حسابًا آخر بسبب تغيير theme؛ خصوصية أي إعداد شخصي مستقبلًا واضحة |

وقت الوقائع epoch milliseconds UTC، وعرض/فترات الخطة والجواهر من timezone صريحة `Asia/Damascus` افتراضيًا، دون ترحيل تواريخ ledger القديمة أو إعادة احتساب حدودها. الأسبوع الاثنين 00:00 إلى الاثنين التالي، متسقًا مع LocalGemsPolicy الحالي، رهن PH1-D09. الشهر تقويمي بتلك المنطقة. تحديث timezone خطة مستقبلية يتطلب تأكيدًا ولا يعيد كتابة فترات منجزة. جلسات الوقت لا تتضاعف بسبب restore؛ manual موسوم ولا يمنح Gems. HYBRID يقيس الزمن والإنجاز كقيمتين منفصلتين، ولا يخترع جمعًا موزونًا لم يعتمد.

تخفيض هدف بعد تقدم لا يمحو القياس الأصلي؛ revision موثقة ونسبة عرض capped مع القيمة الفعلية. carry ينسخ المتبقي فقط بسند للمهمة الأصلية ويغلق/يربط الأصل دون نجاح مصطنع؛ لا ترحيل تلقائي بلا موافقة. لا حاجة لاستيراد بيانات المعلم لتنفيذ الخطة.

## 6. سياسة مراجعة Legacy غير المطابق

### حالات التوافق المقترحة — overlay إضافي

`DISCOVERED → READ_VALIDATED → OWNER_CONFIRMED → CONTENT_REVIEWED → READY_FOR_EXPLICIT_APPLY → APPLIED`؛ بدائل `PRESERVED_UNRESOLVED / STAGE_CONFLICT / EDITION_UNKNOWN / SOURCE_CHANGED / CORRUPT / UNSUPPORTED_VERSION`.

هذه ليست تغييرًا لقيم journal الحالية `PREPARED/DONE/SUSPENDED`. metadata مراجعة إضافية حولها. لا يستخدم نفس الاسم للدلالة على معنيين، ولا يتجاوز القارئ version أحدث بإعادة حفظ v1 فوقه.

| حالة المصدر | المعالجة المطلوبة |
|---|---|
| owner غير مثبت/metadata متناقضة | حفظ الأصل، لا ربط بحساب حالي بسبب الاسم/البريد المدخل؛ review pending |
| owner مثبت، stage المحتوى غير محسومة | لا استخدام اختيار المستخدم وحده؛ طلب source mapping مدقق أو إبقاء معلق |
| محتوى stage أخرى داخل ملف معروف | لا تعديل stage للملف ولا نقل wallet؛ projection يفصل المعلق عن تقدم المنهج الحالي؛ حل نقل/نسخ إلى هدف مستقل يحتاج مراجعة واعتماد |
| نفس stage، edition غير مثبتة | تاريخ Legacy متاح لصاحبه، بلا نسب إتقان/معادلة إكمال رسمي؛ يستمر تعلم جديد بإصدار معتمد منفصل |
| مصدر مختلط stages | تصنيف سجلات refs واحدًا واحدًا؛ لا تقطيع الأصل أو ترحيل كامل إلى مرحلة مختارة؛ قوائم المؤكد والمعلق واضحة |
| ملف UUID مستورد سابقًا خطأ | REVIEW_REQUIRED overlay؛ stage/UUID/legacyWalletKey تبقى كما هي؛ لا reverse migration مدمرة |
| JSON تالف/version غير مدعوم | خطأ قابل للمراجعة، لا empty seed بديل أو clear؛ فتح ملف مستقل إن registry موثوق |
| صفوف/واجبات/إجابات Teacher مشتركة | تبقى في المصدر؛ لا اكتساب ملكية UUID جديدة، ولا تعديل Teacher IDs |

### بروتوكول التطبيق المستقبلي بعد الاعتماد

1. اكتشاف المصدر read-only مع fingerprint وحالة إصدار؛ لا قراءة بيانات مستخدم حقيقي في تطوير عام.
2. إثبات نطاق الملكية من metadata القائمة والتوافق، لا «تأكيد اسمي» فقط؛ التصنيف المحلّي لا يثبت هوية سحابية.
3. بناء تقرير dry-run: source fingerprint، المالك/المرحلة المقترحان، edition evidence، أعداد matched/unresolved/conflicts، wallet binding الحالي، لا تغيير رصيد.
4. E14 تعرض ما سينسب بالضبط؛ ما لم يحسم يبقى محفوظًا. لا checkbox «أوافق على الكل» تتخطى stage conflict.
5. apply token ثابت للعملية وmanifest mapping version؛ journal الحالي يستأنف نسخه المشروعة كما هو. أي تكامل جديد يختبر عدم تفعيل مصدر تغير fingerprint له منذ العرض.
6. كتابة هدف إضافي إذا اعتمد، ثم تحقق وسجل publication؛ لا حذف الأصل بعد DONE. target موجود مختلف → تعارض، لا overwrite.
7. restart يعيد نفس القرار ونفس target/receipt، ولا يكرر profiles أو claims أو rewards. يحافظ على SUSPENDED ويعرض سببه دون استبداله بنجاح.

**المعلّق ليس محذوفًا:** الوصول التاريخي المقروء للمالك المثبت يبقى من الملف/المراجعة؛ لا يحسب ضمن المنهج النشط إلا بعد mapping. لا نحجب المحفظة التاريخية كعقوبة لغياب edition. استحقاق محتوى سابق متعارض يحفظ في history؛ سياسة فتح المادة نفسها تحتاج PH1-D12 ولا تسمح بتسريب stage في listing الجديد أو إعادة شراء حق مكتسب.

## 7. صون legacyWalletKey وكل الجواهر

Resolver الحالي هو المرجع: Student → `legacyWalletKey` إن موجود، وإلا `localId`، وإلا normalized email لديمو Legacy فقط؛ Guest/Teacher → لا مالك Gems. لا تطبق normalization جديدًا على legacyWalletKey المحفوظة، ولا تستنتجها من اسم أو بريد شاشة Profile.

| الثابت | ما يحظر |
|---|---|
| UUID learner قد يختلف عن walletOwnerKey | استبدال wallet owner بالـ UUID تلقائيًا أو استخدام UUID للوصول لرصيد لم ينقل |
| `(owner, achievement)` identity التاريخية | نسخ ledger لمالك آخر أو تغيير مفاتيح rewards أو إعادة منح إكمال قديم |
| balance/history/unlocks/exclusions/activated | إعادة حساب/صفر/تسوية عامة/منح +1000 عرضية أو تغيير تاريخ التنشيط |
| أسعار وlimits ومصادر المكافأة الحالية | اعتماد مكافأة أسبوعية/إتقان جديدة من وثيقة UI؛ WEEKLY/MASTERY التاريخية تبقى مقروءة دون صك جديد |
| PendingGemEvent owner تعلم UUID | تبديل event.owner إلى legacyWalletKey؛ deliver يتحقق مالك التعلم ثم storage يستخدم resolver للمحفظة |
| زائر | تفعيل ledger أو دفع reward مستتر أو مكافآت رجعية عند claim |
| إصدار منهج جديد | تغيير achievement key إلى edition-prefixed ثم مكافأة الحدث نفسه ثانية |

عقد `RewardEquivalenceMap` المقترح يربط المحتوى المعتمد بالإنجاز التاريخي نفسه إذا كان مكافئًا، ويحافظ على key القائم. إذا لم تثبت المكافأة/التكافؤ يُعلّق **المنح الجديد للعنصر المعني** للمراجعة، لا يمنح key جديدًا افتراضيًا ولا يصفر القديم. مادة جديدة فعلًا تحتاج قرار هوية إنجاز versioned واختبارات عدم الازدواج قبل إطلاقها؛ ليست مجرد rename/edition rollover.

فحص عدم التغيير للمحافظ المستوردة/الموجودة على fixtures قبل أي migration لاحقة: مقارنة owner/activated/balance والصفوف الكاملة لكل rewards/unlocks/exclusions/lessons/assessments؛ baseline hash لمصدر Legacy؛ ثبات aliases؛ فحص foreign keys وintegrity. الصفوف التاريخية لا يعاد ترتيب معناها أو تحويل timezone/day/week. لا database upgrade هنا؛ `SQLiteLocalGems.onUpgrade` الحالي يرفض غير المدعوم لحفظ الأصل.

حالات عرض المحفظة: Known(balance من DB)، Loading، StorageError، NotApplicable للزائر، OwnerMismatch يخفي الرصيد السابق؛ لا تحويل أي منها إلى صفر، ولا نجاح شراء قبل transaction، ولا خصم ثانٍ لفتح item مملوك. stage gate للكتالوج الجديد لا يمحو unlock قديم.

## 8. تبديل السياق وفشل التخزين

```text
طلب تبديل → افحص حالة الكتابة والمسودة → تأكيد المستخدم عند الحاجة
→ تحقق snapshot الهدف → ثبت اختيار active
→ generation جديدة وإخفاء بيانات المالك السابق فورًا
→ تنظيف VM/answers/solver/back stacks/snackbars والمرشحات الشخصية
→ bind wallet الجديد → عرض Home المرحلة الفعلية
```

فشل validation أو commit قبل نشر active: يبقى الحالي، ولا يعلن نجاح. إذا انقطعت العملية بعد commit وقبل UI: إعادة التشغيل تستعيد active المحفوظة ثم تبني state جديدًا، لا تمزج الشاشة السابقة. لا يحتفظ saved tab stack بمالك آخر؛ إن استعيد route يفحص scope قبل القراءة. ack حدث قديم المالك يمكن أن يحدث في ملفه فقط وفق الآلية القائمة، ولا يغير state الظاهرة للمالك الجديد.

توصيف الحفظ للمستخدم دقيق: يوجد حفظ للأفعال المسجلة، وليس وعدًا بأن كل draft exercise محفوظ تلقائيًا. أي إضافة حفظ مسودة تعتمد عقد draft owner/stage/edition/revision وتختبر restore؛ حتى ذلك لا يُخبر المستخدم بأنها محفوظة.

## 9. حسابات وClaim مستقبليان، دون إعادة تعريف الموجود

AuthReady ليس networkAvailable فقط: مزود معتمد، enrollment/recovery/session حقيقية، privacy/terms، دعم البلد، وقرار رقم واحد لحسابين. Account.stage تأتي من provider/service policy الموثوقة؛ user input يحدد نية فقط. `AccountType.Student` الحالي لا يحقق هذا الشرط. tokens وأسرار المزود خارج ProductState/Prefs/السجلات/route وبخزن مناسب يحدد مع المزود لاحقًا.

Account.stage مختلفة → E15 وموافقة واضحة على stage الحساب، لا تعديلها ولا تعديل guest. session expired لا تفتح بياناتها باسم guest؛ يستطيع المستخدم فتح مساحته المحلية الأخرى صراحة. تعاد صلاحية DeferredAction بعد نجاح التحقق: نفس stage وcontent متاح؛ لا إرسال محاولة أجابها guest باسم الحساب تلقائيًا.

Claim نفس المرحلة فقط: يحتفظ source guest UUID بكل بياناته وهدف account owner معلومًا. لا mutation guest→student، لا إعادة ترقيم Wallet، لا جمع محافظ. dry-run يرصد تعارض completion/attempt/plan/history وrevision، ويختار المستخدم نقلًا محددًا أو الاحتفاظ بالمساحتين. provenance `sourceOwner/sourceRecord/sourceCommand/claimId` ثابت؛ التكرار لا يزيد المحاولات. لا dedup بالاسم أو إجابة متشابهة أو timestamp فقط.

المستورد من guest يبقى imported/assisted provenance حسب الأصل؛ لا pending reward events جديدة، ولا تحويل old command IDs إلى أوامر تعلم جديدة تمنح Gems. الخطة المتعارضة لا تدمج بمجرد union؛ نسخة مراجعة/اختيار ومهمة lineage. لا يعتمد claim على نجاح شاشة OTP Demo. تعقيد المزود والسياسة يحسم PH1-D10/11، والإبقاء على المساحتين بديل آمن قابل للاستعمال.

## 10. حدود التنفيذ واختبارات العقد المطلوبة

تطبيق تدريجي مستقبلًا: adapters حول LocalLearnerStore/LocalDemoRepository وledger القائمين، ثم catalog scope وcommands، ثم read models وstage presentation. لا تبديل Room/DataStore/DB أو دمج domains للمعلم لمجرد هذا التصميم. نسخة schema جديدة لا تكتب قبل عقد backward/forward compatibility واستئناف وتعليق للنسخ الأعلى.

الاختبارات المطلوبة مسجلة في [QA](../QA/ENMATH_PHASE1_DUAL_STAGE_ACCEPTANCE_2026-10-26.md): owner stale، payload conflict، stage/edition mismatch، alias preservation، reward equivalence، mixed Legacy، crash لكل حد commit، existing wrong-stage import، source changed، unknown version، guest no reward، teacher data invariant. لا ادعاء بأن overlay والعقود المذكورة موجودة بالفعل، ولا تشغيل migration على أجهزة طلاب في مرحلة المواصفات.
