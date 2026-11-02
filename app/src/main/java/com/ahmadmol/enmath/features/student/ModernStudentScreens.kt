package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.planDate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable private fun StudyAction(title: String, detail: String, icon: ImageVector, tag: String, action: () -> Unit) {
    OutlinedCard(onClick = action, modifier = Modifier.fillMaxWidth().testTag(tag), shape = StudentExperienceTokens.compact) {
        Row(Modifier.padding(16.dp).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (detail.isNotBlank()) Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable fun ModernToolsScreen(state: LearningState, open: (String) -> Unit) {
    Grade9Scaffold("screen-tools") {
        item("title") { Text(tr("My study space", "مركز دراستي"), style = MaterialTheme.typography.headlineMedium) }
        item { StudyAction(tr("My study plan", "خطتي الدراسية"), tr("Goals at your own pace", "أهداف تناسب وتيرتك"), Icons.AutoMirrored.Rounded.EventNote, "tools-planner") { open("planner") } }
        item { StudyAction(tr("Library", "المكتبة"), tr("Your files and saved references", "ملفاتك ومراجعك المحفوظة"), Icons.Rounded.LocalLibrary, "tools-library") { open("library") } }
        item { StudyAction(tr("Math solver", "الحل الرياضي"), tr("Supported expressions with steps", "مسائل مدعومة مع خطوات الحل"), Icons.Rounded.Calculate, "tools-solver") { open(Routes.Solver) } }
        item { StudyAction(tr("Graph lab", "مختبر الرسم"), tr("Explore functions", "استكشف التوابع"), Icons.Rounded.AutoGraph, "tools-graph") { open("explore") } }
        item { StudyAction(tr("Practice", "التدريبات"), tr("Available activities and saved sessions", "الأنشطة المتاحة والجلسات المحفوظة"), Icons.Rounded.EditNote, "tools-training") { open("training") } }
        item { StudyAction(tr("Content status", "حالة المحتوى"), tr("Available learning and content under review", "التعلّم المتاح والمحتوى قيد المراجعة"), Icons.AutoMirrored.Rounded.FactCheck, "tools-content") { open("content/status") } }
        item { StudyAction(tr("Achievements and activity", "إنجازاتي ونشاطي"), tr("Your recorded work", "أعمالك المسجلة"), Icons.Rounded.WorkspacePremium, "tools-activity") { open(Routes.Activity) } }
        item { StudyAction(tr("My progress", "تقدمي"), tr("Results from real practice", "نتائج التدريب الفعلي"), Icons.Rounded.Insights, "tools-progress") { open(Routes.Progress) } }
        item { StudyAction(tr("My gems", "جواهري"), tr("Wallet and reward history", "المحفظة وسجل المكافآت"), Icons.Rounded.Diamond, "tools-gems") { open("gems/wallet") } }
        item { StudyAction(tr("Review my mistakes", "مراجعة أخطائي"), tr("Try tricky questions again", "حاول مجددًا في الأسئلة الصعبة"), Icons.Rounded.History, "tools-review") { open("review") } }
    }
}

@Composable fun AvatarSelectionScreen(state: LearningState, repository: ModernStudentRepository?, done: () -> Unit) {
    val scope = StudentCatalog.scope(state)
    val reading = rememberModernStudent(state, repository)
    val data = (reading as? ModernRead.Ready)?.data
    var choice by rememberSaveable(scope?.ownerId, data?.selectedAvatarId) { mutableStateOf(data?.selectedAvatarId) }
    var error by remember(scope) { mutableStateOf(false) }
    LearningPage(Modifier.testTag("screen-avatar-selection"), scope?.ownerId) {
        Text(tr("Your learning companion", "رفيق رحلتك التعليمية"), style = MaterialTheme.typography.headlineMedium)
        Text(tr("Step 3 of 3 · Make this space yours", "الخطوة 3 من 3 · اجعل هذه المساحة لك"))
        Text(tr("Choose any character. Individual portraits are being prepared; these neutral symbols are temporary.", "اختر أي شخصية. الصور الفردية قيد التجهيز؛ هذه الرموز محايدة ومؤقتة."))
        StudentAvatars.ids.forEach { id ->
            Surface(shape = StudentExperienceTokens.compact, color = if (choice == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth().selectable(choice == id, onClick = { choice = id }, role = Role.RadioButton).testTag("avatar-choice-$id")) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StudentAvatar(id, size = 72.dp)
                    Text(avatarName(id), Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    RadioButton(choice == id, onClick = null)
                }
            }
        }
        if (reading == ModernRead.StorageError || error) Text(tr("The choice was not saved. Your previous data is preserved; restart before retrying.", "لم يُحفظ الاختيار. بياناتك السابقة محفوظة؛ أعد التشغيل قبل المحاولة."), color = MaterialTheme.colorScheme.error)
        PrimaryButton(tr("Save my companion", "احفظ رفيق الرحلة"), {
            val id = choice; val result = if (scope != null && id != null) repository?.selectAvatar(scope, id) else ModernWrite.INVALID
            if (result in setOf(ModernWrite.SAVED, ModernWrite.ALREADY_SAVED)) done() else error = true
        }, Modifier.testTag("avatar-save"), enabled = choice != null && data != null)
        TextButton(done, Modifier.heightIn(min = 48.dp).testTag("avatar-later")) { Text(tr("Choose later", "سأختار لاحقًا")) }
    }
}

@Composable fun ModernProfileScreen(state: LearningState, repository: LocalDemoRepository?, gems: LocalGemsUiState,
    open: (String) -> Unit, logout: () -> Unit, language: (Language) -> Unit, theme: (ThemeMode) -> Unit) {
    val reading = rememberModernStudent(state, repository?.studentExperience)
    val data = (reading as? ModernRead.Ready)?.data
    val scope = StudentCatalog.scope(state)
    var confirm by remember { mutableStateOf(false) }
    var error by remember(scope) { mutableStateOf(false) }
    Grade9Scaffold("screen-profile") {
        item("identity") { StudentPanel(emphasized = true) {
            StudentAvatar(data?.selectedAvatarId, AvatarPose.WELCOME, 104.dp)
            Text(state.user?.name.orEmpty(), style = MaterialTheme.typography.headlineSmall)
            Text(studentStageLabel(state.user?.studyStage))
            Text(tr("Local profile · saved on this device", "ملف محلي · محفوظ على هذا الجهاز"))
            OutlinedButton({ open(Routes.Avatar) }, Modifier.heightIn(min = 48.dp).testTag("profile-change-avatar")) { Text(tr("Change companion", "تغيير الشخصية")) }
        } }
        item("learning") { StudentPanel {
            Text(tr("Your learning", "تعلّمك"), style = MaterialTheme.typography.titleLarge)
            Text(data?.let { tr("${LearningStreak.current(it.events, System.currentTimeMillis(), it.zone)} consecutive learning days", "${LearningStreak.current(it.events, System.currentTimeMillis(), it.zone)} أيام تعلم متتالية") } ?: "—")
            Text(gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language) + " · " + tr("Gems", "جواهر"))
            Text(tr("Unlimited energy · ∞", "طاقة غير محدودة · ∞"))
            TextButton({ open(Routes.Progress) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Learning statistics", "إحصاءات التعلم")) }
        } }
        item { StudyAction(tr("Learning days", "أيام الاستمرارية"), "", Icons.Rounded.LocalFireDepartment, "profile-streak") { open(Routes.Streak) } }
        item { StudyAction(tr("My gems", "جواهري"), "", Icons.Rounded.Diamond, "profile-gems") { open("gems/wallet") } }
        item { StudyAction(tr("Achievements and activity", "إنجازاتي ونشاطي"), "", Icons.Rounded.WorkspacePremium, "profile-activity") { open(Routes.Activity) } }
        item { StudyAction(tr("My plan", "خطتي"), "", Icons.AutoMirrored.Rounded.EventNote, "profile-planner") { open("planner") } }
        item("preferences") { StudentPanel {
            Text(tr("Preferences", "تفضيلاتي"), style = MaterialTheme.typography.titleLarge)
            OutlinedButton({ language(if (state.language == Language.Arabic) Language.English else Language.Arabic) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile-language")) { Text(tr("العربية", "English")) }
            ThemeMode.entries.forEach { mode -> FilterChip(state.theme == mode, onClick = { theme(mode) }, label = {
                Text(when(mode) { ThemeMode.System -> tr("Device theme", "مظهر الجهاز"); ThemeMode.Light -> tr("Light", "فاتح"); ThemeMode.Dark -> tr("Dark", "داكن") })
            }) }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(tr("Reduce motion", "تقليل الحركة"), Modifier.weight(1f))
                Switch(data?.reducedMotion == true, onCheckedChange = { value -> if (scope != null) {
                    val result = repository?.studentExperience?.reducedMotion(scope, value)
                    error = result !in setOf(ModernWrite.SAVED, ModernWrite.ALREADY_SAVED)
                } }, enabled = data != null, modifier = Modifier.testTag("profile-reduced-motion"))
            }
        } }
        item { StudyAction(tr("Local profiles", "الملفات المحلية"), tr("Switch without deleting saved work", "بدّل الملف مع الاحتفاظ بأعمالك"), Icons.Rounded.ManageAccounts, "profile-switch-file") { confirm = true } }
        item { StudyAction(tr("Privacy", "الخصوصية"), tr("Local storage and cloud status", "التخزين المحلي وحالة السحابة"), Icons.Rounded.PrivacyTip, "profile-privacy") { open("student/privacy") } }
        item { StudyAction(tr("Help", "المساعدة"), "", Icons.AutoMirrored.Rounded.HelpOutline, "profile-help") { open("student/help") } }
        item { OutlinedButton({ confirm = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("profile-logout")) { Text(tr("Sign out", "تسجيل الخروج")) } }
        if (error || reading == ModernRead.StorageError) item { Text(tr("Saved preferences need attention. Restart before retrying.", "تحتاج التفضيلات المحفوظة مراجعة. أعد التشغيل قبل المحاولة."), color = MaterialTheme.colorScheme.error) }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(tr("Switch local profile?", "تبديل الملف المحلي؟")) },
        text = { Text(tr("Your learning and character stay saved for this profile.", "يبقى تعلّمك وشخصيتك محفوظين لهذا الملف.")) },
        confirmButton = { TextButton({ confirm = false; logout() }, Modifier.testTag("profile-confirm-logout")) { Text(tr("Continue", "متابعة")) } },
        dismissButton = { TextButton({ confirm = false }) { Text(tr("Cancel", "إلغاء")) } })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun ModernStreakScreen(state: LearningState, repository: LocalDemoRepository?, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    val reading = rememberModernStudent(state, repository?.studentExperience)
    val data = (reading as? ModernRead.Ready)?.data
    var clockWarning by remember(scope) { mutableStateOf(false) }
    var selectedDay by remember(scope) { mutableStateOf<Long?>(null) }
    LaunchedEffect(scope) { if (scope != null) clockWarning = repository?.studentExperience?.refreshLearningDays(scope) == ModernWrite.CLOCK_REVIEW }
    LearningPage(Modifier.testTag("screen-streak"), scope?.ownerId) {
        Text(tr("A little learning, every day", "قليل من التعلم، كل يوم"), style = MaterialTheme.typography.headlineMedium)
        StudentAvatar(data?.selectedAvatarId, AvatarPose.STREAK, 88.dp)
        if (data == null) { Text(tr("Saved learning days are unavailable. Your records are preserved.", "تعذرت قراءة أيام التعلم. سجلاتك محفوظة.")); return@LearningPage }
        Text("${LearningStreak.current(data.events, System.currentTimeMillis(), data.zone)} " + tr("consecutive days", "أيام متتالية"), style = MaterialTheme.typography.headlineSmall)
        Text(tr("Completed saved practice creates an active day. Browsing, opening the app and internal QA drafts do not.", "إتمام تدريب محفوظ يُسجّل يومًا نشطًا. التصفح وفتح التطبيق ومسودات الاختبار الداخلي لا تُحتسب."))
        Text(tr("Calendar timezone: ${data.zone}", "منطقة التقويم: ${data.zone}"))
        val today = LearningStreak.day(System.currentTimeMillis(), data.zone)
        val active = data.events.map { it.day }.toSet()
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            (today - 6..today).forEach { day ->
                val label = SimpleDateFormat("MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(day * 86_400_000L))
                FilterChip(selected = selectedDay == day, onClick = { selectedDay = day }, label = { Text(label + if (day in active) " ✓" else " ·") }, modifier = Modifier.testTag("streak-day-$day"))
            }
        }
        selectedDay?.let { day -> Text(tr("${data.events.count { it.day == day }} saved practice sessions on this day", "${data.events.count { it.day == day }} جلسات تدريب محفوظة في هذا اليوم")) }
        Text(tr("Every fresh start is welcome. Your previous active days stay saved.", "كل بداية جديدة مرحّب بها. أيام نشاطك السابقة تبقى محفوظة."))
        if (clockWarning) Text(tr("The device clock changed. New days need a clock check.", "تغيّرت ساعة الجهاز. يحتاج تسجيل أيام جديدة مراجعة الوقت."), color = MaterialTheme.colorScheme.error)
        StudentPanel {
            Text(tr("Unlimited energy · ∞", "طاقة غير محدودة · ∞"), style = MaterialTheme.typography.titleLarge)
            Text(tr("Learn and retry as often as you like.", "تعلّم وأعد المحاولة كلما أحببت."))
        }
        active.sortedDescending().forEach { day -> Text(SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date(day * 86_400_000L))) }
        OutlinedButton({ open("training") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Available practice", "التدريبات المتاحة")) }
    }
}

@Composable fun ModernProgressScreen(state: LearningState, repository: LocalDemoRepository?, open: (String) -> Unit, activity: Boolean = false) {
    val reading = rememberModernStudent(state, repository?.studentExperience)
    val data = (reading as? ModernRead.Ready)?.data
    val g9 = rememberG9Study(state, repository?.grade9Study)
    val g9Data = (g9 as? G9StudyRead.Ready)?.data
    val attempts = StudentCatalog.attempts(state)
    Grade9Scaffold(if (activity) "screen-learning-activity" else if (state.user?.studyStage == StudyStage.G9) "screen-progress-g9" else "screen-progress") {
        item { StudentPanel(emphasized = true) {
            StudentAvatar(data?.selectedAvatarId, if (activity) AvatarPose.CELEBRATING else AvatarPose.ENCOURAGING)
            Text(if (activity) tr("Achievements and activity", "إنجازاتي ونشاطي") else tr("My learning progress", "تقدمي في التعلم"), style = MaterialTheme.typography.headlineSmall)
            Text(tr("Evidence from your saved work. No curriculum-wide mastery percentage is inferred.", "أدلة من أعمالك المحفوظة. لا نستنتج منها نسبة إتقان للمنهج كاملًا."))
            TextButton({ open(Routes.Streak) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Learning days", "أيام التعلم")) }
        } }
        if (state.user?.studyStage == StudyStage.G9) {
            item { Grade9EvidenceSection(state, repository?.grade9Study, open) }
            item { Grade9NatureEvidenceSection(state, repository?.naturePractice, open) }
            if (activity) g9Data?.attempts?.asReversed()?.forEach { attempt -> item("pilot:${attempt.record.commandId}") {
                StudyAction(tr("Fractions practice", "تدريب الكسور"), planDate(attempt.record.timestamp) + " · ${attempt.record.correct}/${attempt.record.total}", Icons.Rounded.TaskAlt, "activity-${attempt.record.commandId}") { open("g9/result/${attempt.record.commandId}") }
            } }
        } else {
            item { StudentPanel {
                Text(tr("${attempts.size} saved practice attempts", "${attempts.size} محاولات تدريب محفوظة"))
                Text(tr("${StudentCatalog.completed(state).size} demo lessons completed", "${StudentCatalog.completed(state).size} دروس تجريبية مكتملة"))
            } }
            attempts.asReversed().forEach { attempt -> item("attempt:${attempt.commandId}") {
                StudyAction(state.practiceSets.find { it.id == attempt.setId }?.title?.text().orEmpty(), "${attempt.correct}/${attempt.total} · ${planDate(attempt.timestamp)}", Icons.Rounded.TaskAlt, "activity-${attempt.commandId}") { open("practice/${attempt.setId}/result?attempt=${attempt.commandId}") }
            } }
        }
        data?.results?.asReversed()?.forEach { result -> item("qa:${result.id}") {
            StudyAction(tr("Internal QA session", "جلسة اختبار داخلي"), SourceCurricula.forStage(StudyStage.G9).research(result.lessonId)?.title.orEmpty(), Icons.Rounded.Science, "activity-qa-${result.id}") { open("qa/result/${result.id}") }
        } }
        item { StudyAction(tr("Mistake review", "مراجعة الأخطاء"), "", Icons.Rounded.History, "progress-review") { open("review") } }
        item { StudyAction(tr("Curriculum content coverage", "تغطية محتوى المنهج"), "", Icons.AutoMirrored.Rounded.MenuBook, "progress-coverage") { open("progress/coverage") } }
        item { StudyAction(tr("Wallet and rewards", "المحفظة والمكافآت"), "", Icons.Rounded.Diamond, "progress-gems") { open("gems/wallet") } }
        if (reading == ModernRead.StorageError) item { Text(tr("Activity storage is unavailable. Your previous records are preserved.", "تعذرت قراءة سجل النشاط. سجلاتك السابقة محفوظة."), color = MaterialTheme.colorScheme.error) }
    }
}

@Composable fun ModernTrainingHub(state: LearningState, open: (String) -> Unit) {
    val context = LocalContext.current
    val bank = remember(context) { OriginalDraftBank.loadQa(context) }
    Grade9Scaffold("screen-training") {
        item { Text(tr("Practice and review", "تدريب ومراجعة"), style = MaterialTheme.typography.headlineMedium) }
        if (state.user?.studyStage == StudyStage.G9) {
            item { StudyAction(tr("Reduced fractions", "الكسور المختزلة"), tr("Original learning pilot", "تدريب تعليمي أصلي تجريبي"), Icons.Rounded.EditNote, "training-fractions") { open("g9/practice") } }
            if (bank != null) {
                item { Text(tr("Internal QA · 192 original draft questions · 0 scientifically approved", "اختبار داخلي · 192 سؤالًا أصليًا مسودة · 0 معتمد علميًا")) }
                bank.mappings.forEach { mapping -> item(mapping.sourceId) {
                    StudyAction(SourceCurricula.forStage(StudyStage.G9).research(mapping.sourceId)?.title.orEmpty(), tr("6 questions · teacher review pending", "6 أسئلة · اعتماد المعلم معلّق"), Icons.Rounded.Science, "training-${mapping.sourceId}") {
                        open(if (mapping.sourceId == Grade9NatureLesson.LESSON_ID) NaturePracticeGrader.ROUTE else "qa/practice/${mapping.sourceId}")
                    }
                } }
            }
        } else state.practiceSets.forEach { set -> item(set.id) {
            val questions = BacDemoExerciseSource(state).forSet(set.id)
            StudyAction(set.title.text(), tr("${questions.size} preserved demo questions", "${questions.size} أسئلة تجريبية محفوظة"), Icons.Rounded.EditNote, "training-${set.id}") { open("practice/${set.id}") }
        } }
        item { StudyAction(tr("Review my mistakes", "مراجعة أخطائي"), "", Icons.Rounded.History, "training-review") { open("review") } }
    }
}

@Composable fun StudentInformationScreen(state: LearningState, kind: String, open: (String) -> Unit) {
    LearningPage(Modifier.testTag("screen-student-$kind")) {
        when (kind) {
            "privacy" -> {
                Text(tr("Your privacy", "خصوصيتك"), style = MaterialTheme.typography.headlineMedium)
                Text(tr("Profiles, learning, character choices and rewards are stored on this device. Cloud sync is not connected. No Firebase login or advertising is enabled.", "الملفات والتعلم واختيار الشخصية والمكافآت محفوظة على هذا الجهاز. المزامنة السحابية غير متصلة. تسجيل Firebase والإعلانات غير مفعّلين."))
                Text(tr("Switching profiles preserves each profile's records. Do not clear app data to solve storage errors.", "تبديل الملفات يحفظ سجلات كل ملف. لا تمسح بيانات التطبيق لمعالجة خطأ تخزين."))
            }
            "help" -> {
                Text(tr("How to study", "كيف أتعلم؟"), style = MaterialTheme.typography.headlineMedium)
                Text(tr("Choose a book and unit on Home to jump along your journey. Tap a lesson for its reference and available activities. Use Tools for your plan, files, solver and practice.", "اختر الكتاب والوحدة في الرئيسية للانتقال في رحلتك. اضغط الدرس لعرض مرجعه وأنشطته المتاحة. افتح الأدوات للخطة والملفات والحل والتدريبات."))
                Text(tr("Practice saves your answers before showing success. If a save fails, restart before retrying; your previous records remain preserved.", "يحفظ التدريب إجاباتك قبل تأكيد النجاح. إذا فشل الحفظ فأعد التشغيل قبل المحاولة؛ السجلات السابقة محفوظة."))
            }
            else -> {
                val catalog = SourceCurricula.forState(state)
                Text(tr("Content status", "حالة المحتوى"), style = MaterialTheme.typography.headlineMedium)
                Text(tr("${catalog?.units?.size ?: 0} indexed units · ${catalog?.researches?.size ?: 0} lesson references", "${catalog?.units?.size ?: 0} وحدات مفهرسة · ${catalog?.researches?.size ?: 0} مراجع دروس"))
                Text(tr("Textbook explanations and exercises require mathematical, edition and publication-rights review. Source browsing is not completed learning.", "تحتاج شروحات الكتاب وتدريباته اعتماد الرياضيات والطبعة وحقوق النشر. تصفح المصدر ليس تعلّمًا مكتملًا."))
                if (state.user?.studyStage == StudyStage.G9) Text(tr("192 original questions remain drafts; six were already integrated. Only internal Debug QA includes the draft bank. Release contains no draft bank.", "192 سؤالًا أصليًا ما زالت مسودات؛ ستة منها كانت مدمجة سابقًا. يحتوي Debug الداخلي فقط على البنك التجريبي. لا يتضمن Release بنك المسودات."))
                else Text(tr("2 books · 14 units · 63 researches. The 27 activity/exercise sections are an index, not extracted questions. The 24 existing demo lessons remain separate.", "كتابان · 14 وحدة · 63 درسًا. أقسام الأنشطة والتمارين الـ27 فهرس وليست أسئلة مستخرجة. الدروس التجريبية الـ24 مستقلة."))
            }
        }
        OutlinedButton({ open(Routes.Tools) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("My study tools", "أدوات دراستي")) }
    }
}
