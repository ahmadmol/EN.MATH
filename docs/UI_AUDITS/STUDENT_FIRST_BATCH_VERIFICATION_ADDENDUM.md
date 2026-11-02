# EN.MATH — First Batch Verification Addendum

التاريخ: 2026-10-22 — Asia/Damascus\
المشروع: <PROJECT>\
النطاق: تصحيح عرض AUD-12 واختباراته، ومراجعة واختبار AUD-05 دون تغيير إنتاجي له. هذا الملحق يصحح حدود الاستنتاجات الواردة في STUDENT_FIRST_FIX_BATCH_REPORT.md، ولا يستبدل التقرير التاريخي أو يعدله.

## 1. نطاق التغييرات وحماية العمل السابق

كانت ملفات الدفعة السابقة والتعديلات المحلية موجودة عند البدء، وحُفظت. بالمقارنة مع بصمات المصدر الملتقطة في بداية هذه الجولة، اقتصر التغيير الإنتاجي على:

- [LocalGemsScreens.kt](../../app/src/main/java/com/ahmadmol/enmath/features/localgems/LocalGemsScreens.kt#L90): نموذج عرض حالات البطاقة ونصوصها وربطها بالواجهة.
- أُضيف [WeeklyGemCardPresentationTest.kt](../../app/src/test/java/com/ahmadmol/enmath/WeeklyGemCardPresentationTest.kt): 18 اختبار JVM لمنطق العرض والنصوص.
- أُضيف [AssignmentSubmissionVerificationTest.kt](../../app/src/test/java/com/ahmadmol/enmath/AssignmentSubmissionVerificationTest.kt): 9 اختبارات JVM ببيانات ومستودع وهمي معزولين.
- أُنشئ هذا الملحق فقط ضمن الوثائق.

لم تُعدّل AppViewModel أو LearningRepository أو LocalDemoRepository أو ProductGraphs أو ENMathApp أو StudentHomeV2 أو Bottom Navigation في هذه الجولة. لم تتغير LocalGemsPolicy أو LocalLearningGems أو قاعدة البيانات أو نظام الحسابات أو أوزان Mastery أو عدد أسئلة المراجعة أو المكافآت وسقوفها. أكدت مقارنة SHA-256 ذلك لجميع ملفات المصدر السابقة المفحوصة؛ ملفا الاختبار هما الإضافتان الوحيدتان في app/src.

لم يُستخدم Emulator أو adb، ولم يُثبّت APK أو تُمسح بيانات هاتف. اقتصر Git على قراءة status؛ لا Commit/Push/Merge/Reset/Clean. بناء assembleDebug ينتج APK محليًا ولا يعني تثبيته.

## 2. AUD-12: السبب والتصحيح

### السبب المثبت

كانت الصيغة (5 - qualifyingCount).coerceAtLeast(1) تحول الصفر والأعداد السالبة إلى درس واحد؛ لذلك يمكن إظهار متطلب إضافي رغم اكتمال الدروس المؤهلة عندما ui.review غير جاهزة.

كان الفرع !available يشارك الإتمام وعدم الأهلية في هيكل واحد، ولا يمثل التحميل والخطأ أو تبديل الملكية بوضوح. **كان النص الداخلي السابق يحتوي بالفعل على رسالة للإتمام عند done=true**؛ لا يدعي الملحق أن كل حالة completed كانت تعرض نصًا خاطئًا. التصحيح يجعل الحالات مستقلة في نموذج العرض ويثبت أولوية الإتمام بالاختبارات.

### السلوك النهائي

أُضيفت WeeklyGemCardStatus وWeeklyGemCardPresentation ودالة weeklyGemCardPresentation النقية في ملف البطاقة نفسه. تستخدم الواجهة الحالة والنص الناتجين مباشرة دون remember، ويُمرر وقت ثابت للدالة في الاختبارات فقط.

| الحالة | الشرط والنتيجة |
|---|---|
| Completed | بعد مطابقة ملكية المستخدم وui والحساب، وجود مفتاح WEEKLY للأسبوع الحالي يعرض «أنجزت مراجعة هذا الأسبوع. أحسنت!» وأيقونة إتمام. لا متطلبات أو زر بدء؛ حتى إن كانت review قديمة أو null أو كانت busy/error موجودة مع سجل الإتمام الموثوق للحساب نفسه. |
| Loading | اختلاف ui.owner أو account.owner عن هوية الطالب، أو غياب account، أو busy، أو اكتمال المتطلبات مع غياب review الحالية. نص تحميل ومؤشر تقدم، دون تفاصيل حساب سابق أو درس إضافي مختلق. |
| Error | خطأ التخزين للحساب الحالي قبل الإتمام، أو غياب هوية طالب يمكن التحقق منها. رسالة خطأ مستقلة وأيقونة خطأ؛ لا ادعاء نقص دروس. عدم توفر LearningState لا يجعل بيانات ui وحدها موثوقة. |
| NotEligible | بيانات مملوكة للحساب الحالي ومتاحة دون خطأ، ولا review للأسبوع الحالي، وعدد المتبقي موجب. يعرض العدد الحقيقي فقط. |
| Ready | بيانات الحساب مطابقة، دون busy/error، وreview للأسبوع الحالي، ولا إتمام مسجل. زر البدء هو الإجراء المتاح. review من coordinator هي دليل الجاهزية؛ البطاقة لا تنشئ مراجعة. |

العد مشتق من تقاطع account.lessons وstate.completed، مع checkpoint/checkpointAnswer غير فارغين، ثم distinctBy لزوج نص نقطة التحقق وإجابتها بعد trim، مثل شرط العرض في الدفعة السابقة. يُحسب المتبقي بـcoerceAtLeast(0).

عند خمسة دروس مؤهلة أو أكثر مع review=null: remainingLessons=0 وحالة Loading. لا تعرض البطاقة «درس واحد متبقٍ» أو «متطلبات ناقصة: صفر». إذا استمرت حالة غياب review، فهذا يحتاج تشخيص تحديث coordinator لاحقًا، ولا يبرر اختلاق متطلب أو تغيير LocalLearningGems.weekly().

مطابقة الملكية تعتمد LocalGemsPolicy.owner(state.user)، ثم مطابقة ui.owner وaccount.owner قبل قراءة الإتمام أو إظهار الجاهزية. لا تُعرض مراجعة من أسبوع سابق بوصفها جاهزة للأسبوع الحالي.

حُفظ الوسمان gems-weekly-card وgems-weekly-open. الثاني موجود في فرع Ready فقط؛ حالات الإتمام والتحميل والخطأ وعدم الأهلية لا تعرض فعل البدء.

### اختبارات AUD-12: 18 ناجحة في كل variant

| الاختبار الفعلي | ما يثبته |
|---|---|
| completedWithNoReviewShowsCompletionInsteadOfRequirements | completed مع review=null تعرض رسالة الإتمام في العربية والإنجليزية، بلا متطلبات |
| completedOverridesStaleReadyBusyAndErrorForTheSameOwner | سجل إتمام الحساب الحالي له الأولوية على review متبقية وbusy/error |
| priorWeekRewardDoesNotMarkThisWeekCompleted | مكافأة أسبوع سابق لا تعني إتمام الأسبوع الحالي |
| missingAccountIsLoading | غياب الحساب يمثل تحميلًا |
| busyAccountDoesNotExposeReadyAction | busy لا تعني Ready |
| storageErrorIsDistinctFromMissingLessonsEvenWithoutAccount | خطأ التخزين حالة مستقلة، حتى عند غياب account |
| zeroVerifiedLessonsRequiresFive | صفر دروس موثقة يعطي خمسة متبقية |
| fourQualifyingLessonsRequiresExactlyOne | أربعة دروس تعطي درسًا واحدًا متبقيًا |
| fiveQualifyingLessonsWithoutReviewShowsLoadingNotAnotherLesson | الحالة المبلغ عنها: خمسة دروس وreview غير جاهزة، دون متطلب مختلق |
| moreThanFiveQualifyingLessonsDoesNotProduceNegativeRequirements | ستة دروس لا تعطي عددًا سالبًا أو متطلبات ناقصة |
| verifiedButUncompletedLessonsDoNotQualify | التوثيق وحده لا يكفي دون completed |
| otherUiOwnerCannotExposeReadyOrCompletedData | اختلاف ui.owner يحجب الجاهزية والإتمام |
| otherAccountOwnerCannotExposeReadyOrCompletedData | اختلاف account.owner يحجب الجاهزية والإتمام |
| missingStudentIdentityDoesNotTrustUiOwnership | null state/user والضيف والمعلم والبريد الفارغ لا تثق ببيانات ui |
| ownershipUsesNormalizedCurrentStudentEmail | مطابقة بريد الطالب بعد التطبيع الحالي |
| currentWeekReviewIsReadyAndUsesReadyCopy | review الحالية تعرض نص الجاهزية دون متطلبات |
| staleReviewIsNotReadyForCurrentWeek | مراجعة أسبوع قديم لا تعرض Ready |
| projectionsFollowLoadingReadyAndCompletionWithoutRememberedState | انتقال البيانات Loading→Ready→Completed يعيد اشتقاق الحالة |

هذه اختبارات JVM للدالة والنصوص المستخدمين مباشرة بالبطاقة. **لا تعني اختبارات Compose rendering أو قياس touch/RTL/TalkBack على جهاز.** عدد الاختبارات هو عدد test methods؛ السيناريوهات الفرعية داخل test واحدة لا تُعد اختبارات إضافية.

## 3. AUD-05: حقيقة الحفظ وحدود Submitted

### مسار الإنتاج المقروء دون تعديل

1. [AppViewModel.submitAssignment](../../app/src/main/java/com/ahmadmol/enmath/core/AppViewModel.kt#L124) يتحقق من AssignmentNotFound وGuestUser وAttemptsExhausted وDeadlinePassed.
2. عند السماح، يستدعي repository.changeProduct لإضافة Submission وإشعار، ثم يطلب معالجة Gems إن كان النشاط منسوبًا لصاحبه، ويرجع Submitted.
3. [LearningRepository.changeProduct](../../app/src/main/java/com/ahmadmol/enmath/core/data/LearningRepository.kt#L79) ترجع Unit؛ لا نتيجة حفظ دائم ولا acknowledgment.
4. [LocalDemoRepository.update](../../app/src/main/java/com/ahmadmol/enmath/core/data/LocalDemoRepository.kt#L53) تنشر state في الذاكرة أولًا، ثم ProductCodec.save، ثم بقية تفضيلات الجلسة.
5. [ProductCodec.save](../../app/src/main/java/com/ahmadmol/enmath/core/data/ProductRepository.kt#L131) تستعمل SharedPreferences.Editor.apply()؛ لا تحقق من نجاح الكتابة على القرص قبل إرجاع Submitted.

**الاستنتاج:** Submitted تعني انتهاء مسار القبول وإرجاع changeProduct بصورة طبيعية، وتحديث الذاكرة في تنفيذ LocalDemoRepository المعتاد. لا تعني إقرارًا بأن البيانات أصبحت دائمة على القرص، ولا نجاح SQLite الخاص بـGems؛ لا توجد معاملة واحدة تربط التخزينين. كذلك واجهة LearningRepository الحالية لا تفرض حتى إقرار كتابة على أي adapter آخر.

الاختبار القديم الذي يستعمل TestLearningRepository يؤكد تحديث الذاكرة فقط. نجاحه لا يثبت سلامة SharedPreferences عند تعطل الكتابة أو قتل العملية، ولا يصح استعمال «SavesOnlyOnSuccess» اسمًا كدليل على ضمان القرص.

### اختبارات AUD-05: 9 ناجحة في كل variant

| الاختبار الفعلي | النتيجة المتحققة |
|---|---|
| submittedPublishesExactlyItsPayloadAndOneNotificationInMemory | payload والدرجة والإشعار تطابق النتيجة؛ كتابة واحدة في المستودع الوهمي، دون ادعاء تخزين دائم |
| secondSubmissionAtAttemptLimitDoesNotWriteOrDuplicateNotifications | بعد محاولة مسموحة واحدة، ثلاث إعادات مرفوضة لا تزيد التسليم أو الإشعارات أو عدد الكتابات |
| guestRejectionDoesNotWrite | GuestUser دون تغيير أي state أو استدعاء كتابة |
| deadlineRejectionDoesNotWrite | DeadlinePassed دون تغيير أو كتابة |
| assignmentNotFoundDoesNotWrite | AssignmentNotFound دون تغيير أو كتابة |
| allowedLateSubmissionStillUsesExistingPolicy | السماح بالتأخر يظل مقبولًا وفق القاعدة القائمة |
| failureBeforeUpdatePropagatesWithoutReturningSubmittedOrChangingState | استثناء قبل التحديث ينتشر دون Submitted أو تغيير state |
| failureAfterPublicationCanLeaveMemoryChangedWithoutDurableSave | استثناء بعد النشر قد يترك Submission في الذاكرة مع غيابها من durable snapshot |
| silentDiskFailureCannotBeDistinguishedFromSubmittedByCurrentContract | عند محاكاة فشل قرص صامت بعد تحديث الذاكرة، ترجع Submitted لكن قارئ snapshot الدائم لا يجد تسليمًا |

StorageFixture بيانات اختبار مستقلة، تفوض العمليات العادية إلى TestLearningRepository وتتحكم بترتيب الفشل. durableSnapshot تمثل حالة دائمة مفترضة في الاختبار؛ **ليست قاعدة Android أو اختبارًا فعليًا لفشل SharedPreferences أو إعادة تشغيل الهاتف**. الغرض إثبات ما يسمح به العقد الحالي وما لا يضمنه، دون تلويث بيانات المستخدم.

عدم التكرار المثبت يخص محاولات متتابعة عند حد attempts=1. لا إثبات idempotency عامة أو تزامن عدة submit threads أو عدم تكرار محاولة مسموحة عندما attempts أكبر من واحد؛ لم تتغير هذه القواعد.

### المخاطر المتبقية وخطة AUD-03

- submitAssignment لا يحول استثناء التخزين إلى StorageFailure؛ الاستثناء قد يصل للواجهة. ProductGraphs تستعمل isSubmitting ولا يوجد try/finally حول الاستدعاء الحالي؛ مسار الاستثناء لا يحصل على معالجة الرفض العادية. لم يُغيّر هذا الإنتاج ضمن النطاق.
- نشر state يسبق الحفظ؛ لا rollback عند استثناء لاحق. retry قد يصطدم بمحاولة ظاهرة في الذاكرة لكنها غير دائمة.
- فشل apply الصامت لا يمكن معرفته عبر النتيجة الحالية؛ نجاح الاختبارات لا يصلح هذا القيد.
- ضمن AUD-03 يُقترح عقد حفظ صريح MemoryAccepted/Persisted/StorageFailure، وإرجاع النجاح الدائم بعد تأكيد الكتابة فقط. تحديث state يجب أن يتفق مع commit أو يميز pending بوضوح.
- يلزم تصميم كتابة ذرية للتسليم والإشعار مع هوية/معرف ثابت للمحاولة وقيد idempotency، وخطة ترحيل ونسخة استرداد قبل استبدال البيانات القائمة. لا يُعامل مجرد استبدال apply بcommit دون فحص نتيجته وترتيب نشر state حلًا كاملًا.
- الحفاظ على JSON المتعثر وعدم استبداله بSeed بصمت جزء من AUD-03، مع اختبارات قرص/إعادة فتح على fixtures مستقلة لاحقًا.
- ترابط Gems بعد حفظ التعلم يحتاج عقد إعادة محاولة آمن لا يصرف مكافأة مرتين؛ لا تغيير اقتصاد Gems أو schema في هذه الجولة.

**لم تُنفذ إعادة كتابة للتخزين، وما زال ضمان الحفظ الدائم غير متوفر.**

## 4. سلامة UTF-8 وتمييز Terminal عن الملف

فُحصت bytes الملفات الثلاثة، ثم decode صارم بـUTF-8 وround-trip مطابق للبايتات، مع فحص U+FFFD وعلامات mojibake المعتادة والتحقق من نصوص عربية متوقعة فعلًا. النتيجة: سليمة، ولا replacement characters أو علامات الفساد المفحوصة.

| الملف | عينة نص عربية متحققة | SHA-256 بعد التصحيح |
|---|---|---|
| ProductGraphs.kt | انتهى موعد تسليم الواجب؛ استنفدت جميع المحاولات | 580cfb26397b1fce67cb7b6eac4299856f1620f814cb76d42faa654d4ae733a7 |
| ENMathApp.kt | جواهري؛ مراجعة الأسبوع؛ كتالوج الكتب | 625c39194d7737dbcf49a6ab52afc5b4862108de18331a32741a7f6cbb6464de |
| LocalGemsScreens.kt | أنجزت مراجعة هذا الأسبوع؛ جارٍ تحميل المراجعة الأسبوعية؛ تعذر تحميل مراجعة الحساب الحالي | 212493f16bb3b9ce33c065fad9640a2f574708ae14c154e42f0b060e03e473d8 |

بصمتا ProductGraphs وENMathApp متطابقتان مع بداية هذه الجولة؛ لم تُعد ترجمة نصوص سليمة أو تغيير encoding لهما. التغييرات النصية في LocalGemsScreens هي تصحيح AUD-12 المقصود.

سلامة decode وحدها لا تستبعد كل mojibake صالح UTF-8؛ لذلك شمل الفحص أيضًا الكلمات العربية الأصلية المتوقعة ونقاط Unicode، وليس شكل Terminal فقط. أُخرجت بيانات التحقق بصيغة JSON مع unicode escapes لتجنب أثر code page. أي عرض Terminal مشوه لهذه النصوص لا يثبت فساد الملف؛ نتائج الفحص البايتية هنا تؤكد سلامة النصوص المفحوصة.

## 5. نتائج Gradle الفعلية

البيئة الناجحة: JAVA_HOME=<LOCAL_USER_RESOURCE> Java21.0.11، دون تغيير Gradle أو dependencies أو ملفات إعداد المشروع.

الأمر المنفذ بنجاح: gradlew.bat :app:testDebugUnitTest :app:testReleaseUnitTest :app:assembleDebug :app:lintDebug --console=plain

| المهمة | النتيجة الفعلية |
|---|---|
| :app:testDebugUnitTest | PASS؛ 92 tests، صفر failures/errors/skipped |
| :app:testReleaseUnitTest | PASS؛ 92 tests، صفر failures/errors/skipped |
| :app:assembleDebug | PASS؛ APK بُني محليًا ولم يُثبّت |
| :app:lintDebug | PASS؛ صفر Errors/Fatal، و10 Warnings و1 Information |

العملية الجامعة انتهت BUILD SUCCESSFUL في3m1s؛ 74 actionable tasks، منها23 executed و51 up-to-date. مهام الاختبار الأربع المطلوبة لم تُستبدل بمحاكي أو اختبار instrumentation.

92 اختبارًا مميزًا، نُفذت فيvariantين =184 تنفيذ اختبار ناجح، **وليس184 اختبارًا مميزًا**. الإضافة الجديدة27 اختبارًا:18 للبطاقة و9 للتسليم، فوق65 اختبارًا موجودًا. لم يُضف عدد السيناريوهات الفرعية إلى عدد JUnit.

| Suite | Debug | Release |
|---|---:|---:|
| AssignmentSubmissionVerificationTest | 9 | 9 |
| WeeklyGemCardPresentationTest | 18 | 18 |
| StudentFirstFixBatchTest | 3 | 3 |
| BottomNavigationLayoutTest | 3 | 3 |
| DemoLogicTest | 4 | 4 |
| GemsIntegrationReviewTest | 9 | 9 |
| LessonSequenceTest | 2 | 2 |
| LocalGemsCoordinatorTest | 6 | 6 |
| LocalGemsPolicyTest | 27 | 27 |
| NativeEquationFormattingTest | 3 | 3 |
| ProductRulesTest | 8 | 8 |
| المجموع | 92 | 92 |

مصدر الأعداد ملفات TEST-*.xml في:
- <PROJECT>\app\build\test-results\testDebugUnitTest.
- <PROJECT>\app\build\test-results\testReleaseUnitTest.

نتيجة Lint من <PROJECT>\app\build\reports\lint-results-debug.xml، والتقرير المرئي في lint-results-debug.html. التحذيرات: OldTargetApi مرة، GradleDependency ست مرات، DataExtractionRules مرة، UseKtx مرتان؛ Information: AutoboxingStateCreation مرة. لا ادعاء بأن Lint بلا تحذيرات؛ لا تحذير مسجل على منطق البطاقة الجديد.

محاولة سابقة استعملت JBR المرفق مع Android Studio، واتضح أنه Java25.0.3؛ فشلت تهيئة Gradle برسالة25.0.3 قبل تشغيل المهام المطلوبة. لم تُحتسب نجاحًا أو اختبارات. أعيد الأمر بـJDK21 المتوفر فنجحت المهام كلها. ظهر أيضًا تحذير SDK XML v4 مقابل أداة تقرأ حتىv3، ولم يمنع البناء النهائي؛ لم تُرقّ toolchain.

## 6. ما يحتاج هاتفًا حقيقيًا لاحقًا

لم تُنفذ هذه البنود؛ تتطلب موافقة المستخدم وهاتفًا حقيقيًا، دون Emulator:

- ظهور بطاقة Completed وعدم عرض متطلبات أو زر بدء بعد مراجعة الأسبوع.
- حالات تحميل وخطأ وNotEligible وReady بالعربية والإنجليزية وLight/Dark، وfont1/1.5/2 وRTL/TalkBack.
- تبديل الحساب أثناء تحميل بيانات Gems، ومنع ظهور مراجعة أو إتمام الحساب السابق.
- خمسة دروس مؤهلة مع تأخر وصول review، ثم تحديث الحالة إلىReady دون إعادة فتح الشاشة؛ واختبار حد الأسبوع.
- وجود test tags وبقاء زر البدء عاملًا عندReady، دون تأثير على Home layout أو Bottom Navigation.
- تسليم واجب مرفوض أو مفقود، وعدم الانتقال لنتيجة قديمة أو تكرار الإشعار.
- إعادة فتح التطبيق والتحقق من التسليم والرصيد. اختبار التخزين غير الآمن على fixtures/حساب اختبار فقط؛ لا فساد متعمد لبيانات طالب.
- ضمان durability عند فشل التخزين أو قتل العملية مؤجل لحل AUD-03، ولا يمكن اعتماده من اختبارات هذه الدفعة.

## 7. الحكم النهائي

AUD-12 مصححة في منطق العرض ومغطاة بـ18 اختبار JVM في Debug وRelease. حالة خمسة دروس دون review لا تفرض درسًا إضافيًا، والإتمام حالة صريحة لها الأولوية بعد التحقق من الملكية. سياسة Gems ومحرك الأهلية لم يتغيرا.

AUD-05 اجتازت اختبارات الرفض والتكرار وAssignmentNotFound والفشل المحاكى، لكن **Submitted ليست ضمان حفظ دائم**. مخاطر ترتيب النشر والكتابة ومعالجة فشل التخزين ما زالت ضمن AUD-03، ووُثقت دون تعديل إنتاجي إضافي.

سلامة UTF-8 مؤكدة للملفات والنصوص المفحوصة. توقفت هذه الجولة بعد التصحيح والتحقق والتوثيق؛ لم تبدأ مراحل تطوير أخرى.
