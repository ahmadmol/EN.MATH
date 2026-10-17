# Supporting states — 36 entries

These are variants/flows, not 36 additional primary screens. PASS is limited to the existing local capability; no remote service is implied.

| ID | State | Status | Evidence / limit |
| --- | --- | --- | --- |
| X01 | Solver Scan / التصوير بالكاميرا | PARTIAL | Existing explicit scan simulation only; real OCR unavailable. AppFlow scan/method test. |
| X02 | Handwritten Math / كتابة المسألة يدويًا | NOT IMPLEMENTED | No handwriting recognition capability; no new route or pretend handwriting solver. |
| X03 | Practice Sets / قائمة التمارين | PARTIAL | Existing topic practice/quiz entry points; no duplicate standalone practice catalog. |
| X04 | Exam Intro / بدء اختبار | NOT IMPLEMENTED | No separate exam-intro route; existing practice flow retained. |
| X05 | Achievements / الإنجازات | PARTIAL | Existing streak/milestones in Progress; no fabricated achievement catalog. |
| X06 | Favorites / المفضلة | PASS | Existing library saved lessons/problems and solver bookmark; local only. |
| X07 | Notes / ملاحظاتي | NOT IMPLEMENTED | No notes repository/flow exists; optional feature not invented. |
| X08 | Assignment Work / أسئلة الواجب | PASS | Existing assignment work route uses actual question types/answers and submit confirmation. |
| X09 | Assignment Attachments / إرفاق ملفات | BLOCKED | No actual file attachment/upload service. No misleading upload button added. |
| X10 | Submission Success / تأكيد التسليم | PARTIAL | Actual submission confirmation navigates to real result; no separate decorative success screen. |
| X11 | Teacher Feedback / ملاحظات المعلم | PASS | Existing teacher feedback persisted locally and shown with assignment result. |
| X12 | Create Class / إنشاء شعبة | PASS | Existing create-class dialog validation and local class persistence. |
| X13 | Assignment Preview / معاينة واجب | PASS | Existing four-stage wizard and preview use actual selected questions. |
| X14 | Publish Success / نجاح النشر | PARTIAL | Successful local publish opens actual results; no separate celebration page. |
| X15 | Review Student Answers / مراجعة الإجابات | PASS | Teacher response route displays actual student answers and computed scores. |
| X16 | Grade Student Response / تصحيح إجابة | BLOCKED | Existing automatic score plus textual feedback only. Manual grade editing requires prohibited business changes. |
| X17 | Share Resource / مشاركة مورد | PASS | Actual local resource creation and class sharing; teacher-to-student library tested. |
| X18 | Teacher Settings / إعدادات المعلم | PARTIAL | Teacher settings alias reuses SettingsScreen. Shared controls tested through student settings; teacher alias not independently exercised. |
| X19 | Teacher Notifications / إشعارات المعلم | PARTIAL | Teacher notification alias reuses native notifications and role filtering; not independently exercised by the final flow suite. |
| X20 | Guest Exploration / تصفح كضيف | PASS | Actual guest exploration and current guarded features retained. |
| X21 | Guest Access Gate / مطالبة الحساب | PASS | Account prompt and registration redirect; guest restriction tested. |
| X22 | Change Language / اختيار اللغة | PASS | Arabic/English changes persist; RTL/LTR tested in real app configurations. |
| X23 | Font Size / حجم الخط | NOT IMPLEMENTED | No separate font-size preference exists. System 1.3× accessibility font scaling is tested. |
| X24 | Change Password / تغيير كلمة المرور | BLOCKED | Existing reset demo only; no authenticated remote password-change capability. |
| X25 | Delete Account / حذف الحساب | BLOCKED | No account-deletion service; no fake destructive action introduced. |
| X26 | Logout Confirmation / تأكيد الخروج | PASS | Native confirmation and actual logout/session cleanup tested. |
| X27 | Help & Support / المساعدة | PARTIAL | Existing help dialog retained; no fake support messaging service. |
| X28 | About EN.MATH / عن التطبيق | PARTIAL | Existing about/privacy dialogs retained; not duplicated as new routes. |
| X29 | Onboarding Slides / شرائح البداية | PASS | Existing three-slide onboarding, skip/finish/session persistence tested. |
| X30 | Loading / تحميل | PARTIAL | Real existing startup transition; no fake network loading injected into synchronous local data. |
| X31 | Empty Lessons / لا توجد دروس | PARTIAL | Native shared missing-content state retained; not a fabricated empty repository. |
| X32 | Empty Assignments / لا واجبات | PARTIAL | Existing role-appropriate empty assignment lists; gallery is not an exhaustive empty-fixture suite. |
| X33 | Empty Favorites / لا مفضلات | PASS | Empty saved library state rendered in baseline; existing filters retained. |
| X34 | No Search Results / بلا نتائج | PARTIAL | Existing no-result presentation is retained. Positive search was tested; a dedicated no-match fixture was not exercised. |
| X35 | Offline / انقطاع الاتصال | BLOCKED | Local offline notice/error component only; no live network service to disconnect/retry. |
| X36 | Server Error / خطأ الخادم | BLOCKED | ErrorState presentation reusable, but no server exists; no fake request/retry wiring. |
