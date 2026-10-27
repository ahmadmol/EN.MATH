package com.ahmadmol.enmath.features.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.features.student.studentStageLabel

/** Stage intention never mutates a learner. Only explicit submit creates or selects a file. */
@Composable
fun LocalIdentityScreen(state: LearningState, create: (String, StudyStage, Boolean) -> Unit,
    select: (String) -> Unit, importLegacy: (String, StudyStage, String) -> Unit,
    debugEntry: (() -> Unit)? = null, acknowledgeGuest: (StudyStage) -> Unit = {}) {
    var stageName by rememberSaveable { mutableStateOf<String?>(null) }
    var page by rememberSaveable { mutableStateOf("stage") }
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val stage = stageName?.let(StudyStage::valueOf)
    fun attempt(action: () -> Unit) {
        if (busy) return
        busy = true
        error = runCatching(action).isFailure
        busy = false
    }
    BackHandler(page != "stage") { page = if (page in setOf("notice", "create")) "entry" else "stage"; error = false }
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.safeDrawingPadding().imePadding()) {
            ScreenPage(Modifier.testTag("screen-local-identity"), narrow = true) {
                PageHeading("EN.MATH", if (page == "stage") tr("Choose your study stage", "اختر مرحلتك الدراسية") else studentStageLabel(stage))
                Text(tr("Saved on this device only. No cloud sign-in or cloud recovery. Local files on a shared device can be opened by its users.",
                    "الحفظ على هذا الجهاز فقط، دون تسجيل دخول أو استعادة سحابية. يمكن لمستخدمي الجهاز المشترك فتح الملفات المحلية."))
                if (state.storageIssue != null) Text(tr("An older file needs recovery and was preserved. You can choose another independent local file.",
                    "أحد الملفات يحتاج استعادة وقد حُفظ دون استبدال. يمكنك اختيار ملف محلي مستقل آخر."), color = MaterialTheme.colorScheme.error)
                when(page) {
                    "stage" -> {
                        StudyStage.entries.forEach { value -> OutlinedButton({ stageName = value.name; page = "entry"; name = "" },
                            Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("local-stage-${value.name}")) {
                            Column { Text(studentStageLabel(value), style=MaterialTheme.typography.titleLarge)
                                Text(if(value == StudyStage.G9) tr("Learn step by step", "تعلّم خطوة خطوة") else tr("Organize your preparation and continue learning", "نظّم تحضيرك وواصل التعلم")) }
                        } }
                        if (state.localProfiles.isNotEmpty()) OutlinedButton({ page = "profiles" }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Choose a saved local file", "اختيار ملف محلي محفوظ")) }
                        debugEntry?.let { TextButton(it) { Text(tr("Development demo entry", "الدخول التجريبي للتطوير")) } }
                    }
                    "entry" -> {
                        Text(if(stage == StudyStage.G9) tr("Make mathematics clearer, one step at a time.", "خلّي الرياضيات أوضح، خطوة خطوة.") else tr("Continue from your last lesson and plan across subjects.", "تابع من آخر درس وخطّط لجميع المواد."), style=MaterialTheme.typography.titleLarge)
                        PrimaryButton(tr("Continue as guest", "متابعة كزائر"), { stage?.let { selected -> if (selected in state.acknowledgedGuestStages) attempt { create("", selected, true) } else page = "notice" } }, Modifier.testTag("local-open-guest"), !busy)
                        OutlinedButton({ page = "create" }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Create a local student file", "إنشاء ملف طالب محلي")) }
                        OutlinedButton({ page = "profiles" }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Return to a local file", "العودة إلى ملف محلي")) }
                        Text(tr("Cloud accounts are not connected. Local student files are not authenticated accounts.", "الحسابات السحابية غير متصلة. ملف الطالب المحلي ليس حسابًا موثّقًا."))
                    }
                    "notice" -> {
                        Text(tr("Guest data stays on this device", "بيانات الزائر محفوظة على هذا الجهاز"), style=MaterialTheme.typography.titleLarge)
                        Text(tr("Learning and plans are saved in a separate guest file for this stage. Guests receive no gems. Clearing app storage or losing the device may lose this data. Existing guest data is reused.",
                            "يُحفظ التعلم والخطة في ملف زائر مستقل لهذه المرحلة. لا يحصل الزائر على جواهر. قد تفقد البيانات عند مسح تخزين التطبيق أو فقد الجهاز. يُستعاد ملف الزائر السابق إن وُجد."))
                        PrimaryButton(tr("Understand and continue", "فهمت، متابعة"), { stage?.let { attempt { acknowledgeGuest(it); create("", it, true) } } }, Modifier.testTag("guest-notice-confirm"), !busy)
                    }
                    "create" -> {
                        OutlinedTextField(name, { name = it.take(100) }, label = { Text(tr("Local file name (optional)", "اسم الملف المحلي (اختياري)")) }, modifier = Modifier.fillMaxWidth())
                        Text(tr("Stage is fixed for this file. To study another stage, create a separate file.", "مرحلة هذا الملف ثابتة. لدراسة مرحلة أخرى أنشئ ملفًا مستقلًا."))
                        PrimaryButton(tr("Create local student file", "إنشاء ملف طالب محلي"), { stage?.let { attempt { create(name, it, false) } } }, Modifier.testTag("local-create-student"), !busy)
                    }
                    "profiles" -> {
                        val profiles = state.localProfiles.filter { stage == null || it.stage == stage }
                        if(profiles.isEmpty()) Text(tr("No saved files for this stage.", "لا ملفات محفوظة لهذه المرحلة."))
                        profiles.forEach { profile -> OutlinedButton({ attempt { select(profile.id) } }, Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("local-profile-${profile.id}")) {
                            Text("${profile.name} · ${studentStageLabel(profile.stage)} · ${if(profile.guest) tr("Guest", "زائر") else tr("Local student", "طالب محلي")}")
                        } }
                    }
                }
                if (state.legacyCandidates.isNotEmpty()) AppCard {
                    Text(tr("ReviewRequired · older data preserved", "تحتاج مراجعة · البيانات القديمة محفوظة"), style=MaterialTheme.typography.titleMedium)
                    Text(tr("Ownership alone does not verify curriculum stage or edition. No automatic import is performed. Existing profiles and wallet bindings are kept.", "إثبات الملكية وحده لا يثبت مرحلة المنهج أو إصداره. لا استيراد تلقائي. الملفات السابقة وارتباطات محافظها محفوظة."))
                }
                if (error) Text(tr("The operation was not saved. Data is preserved; restart before retrying.", "لم تُحفظ العملية. البيانات محفوظة؛ أعد التشغيل قبل المحاولة."), color = MaterialTheme.colorScheme.error)
                if(page != "stage") TextButton({ page = if(page in setOf("notice", "create")) "entry" else "stage"; error = false }) { Text(tr("Back", "رجوع")) }
            }
        }
    }
}

@Composable fun LocalOnboarding(onDone: () -> Unit) {
    var error by remember { mutableStateOf(false) }
    Surface(Modifier.fillMaxSize()) { Box(Modifier.safeDrawingPadding()) { ScreenPage(narrow = true) {
        PageHeading("EN.MATH", tr("Learn locally, at your own pace", "تعلّم محليًا حسب وتيرتك"))
        Text(tr("Choose Grade 9 or Scientific Baccalaureate. Each stage has separate guest and student files.", "اختر التاسع أو البكالوريا العلمي. لكل مرحلة زائر وملفات طلاب مستقلة."))
        Text(tr("Keep lessons, practice, and study plans on this device. Available curriculum content is clearly labelled.", "احتفظ بالدروس والتدريب والخطط على هذا الجهاز. يُوضَّح نطاق المحتوى المتاح."))
        PrimaryButton(tr("Continue", "متابعة"), { error = runCatching(onDone).isFailure }, Modifier.testTag("onboarding-continue"))
        TextButton({ error = runCatching(onDone).isFailure }, Modifier.testTag("onboarding-skip")) { Text(tr("Skip", "تخطي")) }
        if(error) Text(tr("Could not save. Restart before retrying.", "تعذّر الحفظ. أعد التشغيل قبل المحاولة."))
    } } }
}
