package com.ahmadmol.enmath.features.planner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import java.text.SimpleDateFormat
import java.util.*

@Composable fun periodLabel(p: PlanPeriod) = when(p) { PlanPeriod.DAY -> tr("Daily", "يومي"); PlanPeriod.WEEK -> tr("Weekly", "أسبوعي"); PlanPeriod.MONTH -> tr("Monthly", "شهري") }
@Composable fun modeLabel(m: PlanMode) = when(m) { PlanMode.ACHIEVEMENT -> tr("Achievement", "إنجاز"); PlanMode.TIME -> tr("Time", "وقت"); PlanMode.HYBRID -> tr("Mixed", "مختلط") }
fun planDate(at: Long) = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone(StudyPeriods.ZONE) }.format(Date(at))

@Composable fun PlanSummary(state: LearningState, repository: PlannerRepository?, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    val changed = repository?.changes?.collectAsState()?.value
    val result = remember(scope, state.product, changed) { scope?.let { repository?.read(it) } }
    AppCard(Modifier.testTag("plan-summary")) {
        Text(tr("This week's plan", "خطة هذا الأسبوع"), style = MaterialTheme.typography.titleMedium)
        when (result) {
            is PlanRead.Ready -> {
                val start = StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK)
                val tasks = result.plan.tasks.filter { !it.closed && it.period == PlanPeriod.WEEK && it.start == start }
                if (tasks.isEmpty()) Text(tr("Set goals for any school subject, at your own pace.", "حدّد أهدافًا لأي مادة دراسية حسب وتيرتك."))
                else tasks.take(3).forEach { t -> val measured = repository!!.measured(t, state)
                    Text("${t.title}: ${if(t.mode == PlanMode.TIME) tr("${measured.spent}/${t.minutes} minutes", "${measured.spent}/${t.minutes} دقيقة") else tr("${measured.done}/${t.target} units", "${measured.done}/${t.target} وحدة")}")
                    if (t.mode == PlanMode.HYBRID) Text(tr("${measured.spent}/${t.minutes} minutes", "${measured.spent}/${t.minutes} دقيقة"))
                }
            }
            PlanRead.StorageError -> Text(tr("Unable to read the plan. Previous data is preserved.", "تعذّر قراءة الخطة. البيانات السابقة محفوظة."))
            else -> Text(tr("Choose a local file to use planning.", "اختر ملفًا محليًا لاستخدام الخطة."))
        }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("My study plan", "خطتي الدراسية")) }
    }
}

@Composable fun PlannerScreen(state: LearningState, repository: PlannerRepository) {
    val scope = remember(state.user?.localId, state.generation) { StudentCatalog.scope(state) }
    var reload by remember { mutableIntStateOf(0) }
    var read by remember(scope) { mutableStateOf<PlanRead?>(null) }
    LaunchedEffect(scope, reload) { read = scope?.let { repository.read(it) } ?: PlanRead.OwnerChanged }
    var period by remember { mutableStateOf(PlanPeriod.WEEK) }
    var at by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var editing by remember(scope) { mutableStateOf<PlanTask?>(null) }
    var editorRevision by remember(scope) { mutableIntStateOf(0) }
    var form by remember(scope) { mutableStateOf(false) }
    var action by remember(scope) { mutableStateOf<Pair<String, PlanTask>?>(null) }
    var message by remember(scope) { mutableStateOf<PlanWrite?>(null) }
    val plan = (read as? PlanRead.Ready)?.plan
    fun save(result: PlanWrite) { message = result; reload++ }
    ScreenPage(Modifier.imePadding().testTag("screen-planner")) {
        PageHeading(tr("Asia/Damascus · saved on this device", "توقيت دمشق · محفوظة على هذا الجهاز"), tr("My study plan", "خطتي الدراسية"))
        Text(tr("Manual achievements do not award gems or establish mastery. Time and achievement are measured separately.", "الإنجاز اليدوي لا يمنح جواهر ولا يثبت الإتقان. يقاس الوقت والإنجاز بصورة منفصلة."))
        PlanPeriod.entries.forEach { p -> FilterChip(period == p, { period = p }, label = { Text(periodLabel(p)) }, modifier = Modifier.heightIn(min = 48.dp)) }
        val start = StudyPeriods.start(at, period)
        Text("${planDate(start)} — ${planDate(StudyPeriods.next(start, period) - 1)}")
        OutlinedButton({ at = start - 1 }, Modifier.heightIn(min = 48.dp)) { Text(tr("Previous period", "الفترة السابقة")) }
        OutlinedButton({ at = StudyPeriods.next(start, period) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Next period", "الفترة التالية")) }
        TextButton({ at = System.currentTimeMillis() }) { Text(tr("Current period", "الفترة الحالية")) }
        when (read) {
            null -> CircularProgressIndicator()
            PlanRead.StorageError -> { Text(tr("Storage error. Nothing was replaced. Restart if saving failed.", "خطأ تخزين. لم تُستبدل البيانات. أعد التشغيل إذا فشل الحفظ."), color = MaterialTheme.colorScheme.error)
                OutlinedButton({ reload++ }) { Text(tr("Retry reading", "إعادة القراءة")) } }
            PlanRead.OwnerChanged -> Text(tr("The active file changed. Reopen planning.", "تغيّر الملف النشط. أعد فتح الخطة."))
            is PlanRead.Ready -> {
                PrimaryButton(tr("Add a goal", "إضافة هدف"), { editing = null; editorRevision = plan!!.revision; form = true }, Modifier.testTag("plan-add"))
                val tasks = plan!!.tasks.filter { it.period == period && it.start == start }
                if (tasks.isEmpty()) Text(tr("No goals in this period. Add a goal for any subject, including subjects without courses.", "لا أهداف لهذه الفترة. أضف هدفًا لأي مادة، بما فيها المواد التي لا تتوفر لها كورسات."))
                tasks.forEach { original -> val t = repository.measured(original, state)
                    AppCard(Modifier.testTag("plan-task-${t.id}")) {
                        Text(t.title, style = MaterialTheme.typography.titleLarge)
                        Text("${t.subject} · ${modeLabel(t.mode)}")
                        if (t.mode != PlanMode.TIME) Text(tr("Achievement: ${t.done}/${t.target}", "الإنجاز: ${t.done}/${t.target}"))
                        if (t.mode != PlanMode.ACHIEVEMENT) Text(tr("Time: ${t.spent}/${t.minutes} minutes", "الوقت: ${t.spent}/${t.minutes} دقيقة"))
                        Text(if (t.source == "LEARNING_EVENT") tr("Source: recorded lesson completion", "المصدر: إكمال درس محفوظ") else tr("Source: manual entry", "المصدر: إدخال يدوي"))
                        t.lesson?.let { ref -> Text(if (ref.kind == ContentKind.SOURCE_INDEX && StudentCatalog.acceptsReference(state, ref))
                            StudentCatalog.sourceTitle(ref).orEmpty() + tr(" · source pending review", " · مرجع قيد المراجعة")
                            else state.lesson(ref.lessonId)?.title?.text() ?: tr("Linked content unavailable", "المحتوى المرتبط غير متاح")) }
                        if (t.closed) Text(tr("Remaining work moved to another period", "نُقل المتبقي إلى فترة أخرى")) else {
                            OutlinedButton({ action = "progress" to original }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Record partial progress", "تسجيل إنجاز جزئي")) }
                            OutlinedButton({ editing = original; editorRevision = plan!!.revision; form = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Edit goal", "تعديل الهدف")) }
                            OutlinedButton({ action = "carry" to original }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Move remainder to next period", "نقل المتبقي للفترة التالية")) }
                        }
                        TextButton({ action = "delete" to original }) { Text(tr("Delete goal", "حذف الهدف")) }
                    }
                }
                Text(tr("Edit history and previous plans", "سجل التعديلات والخطط السابقة"), style = MaterialTheme.typography.titleLarge)
                if (plan.history.isEmpty()) Text(tr("No edits yet", "لا تعديلات بعد"))
                plan.history.asReversed().forEach { r ->
                    Text("${planDate(r.at)} · ${when(r.action.substringBefore(':')) {
                        "SAVE" -> tr("Goal edited", "تعديل هدف"); "DELETE" -> tr("Goal deleted", "حذف هدف")
                        "CARRY" -> tr("Remaining work moved", "نقل المتبقي"); else -> tr("Manual progress", "إنجاز يدوي")
                    }} · ${r.tasks.size}")
                    r.tasks.forEach { t -> Text("${t.subject}: ${t.title} · ${t.done}/${t.target} · ${t.spent}/${t.minutes}", style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        message?.let { Text(when(it) {
            PlanWrite.SAVED, PlanWrite.ALREADY_SAVED -> tr("Saved on this device", "حُفظت على هذا الجهاز")
            PlanWrite.OWNER_CHANGED -> tr("File changed; operation rejected", "تغيّر الملف؛ رُفضت العملية")
            PlanWrite.CONFLICT -> tr("Plan changed. Reload before editing again.", "تغيّرت الخطة. أعد تحميلها قبل التعديل.")
            PlanWrite.INVALID -> tr("Check the goal, quantities, and linked content.", "تحقق من الهدف والكميات والمحتوى المرتبط.")
            else -> tr("Save unconfirmed. Previous data is preserved. Restart before retrying.", "لم يُؤكَّد الحفظ. البيانات السابقة محفوظة. أعد التشغيل قبل المحاولة.")
        }) }
    }
    if (form && scope != null && plan != null) GoalEditor(state, editing, period, StudyPeriods.start(at, period),
        { form = false }, { task, command -> val result = repository.save(scope, editorRevision, command, task); save(result)
            if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) form = false; result })
    action?.let { (kind, task) ->
        var count by remember(task.id, kind) { mutableStateOf("") }
        var minutes by remember(task.id, kind) { mutableStateOf("") }
        val command = remember(task.id, kind) { UUID.randomUUID().toString() }
        AlertDialog(onDismissRequest = { action = null },
            title = { Text(when(kind) { "progress" -> tr("Manual progress", "إنجاز يدوي"); "carry" -> tr("Confirm moving remaining work", "تأكيد نقل المتبقي"); else -> tr("Delete goal?", "حذف الهدف؟") }) },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(task.title)
                if (kind == "progress") {
                    if(task.mode != PlanMode.TIME) OutlinedTextField(count, { count = it }, label = { Text(tr("Additional units", "وحدات إنجاز إضافية")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    if(task.mode != PlanMode.ACHIEVEMENT) OutlinedTextField(minutes, { minutes = it }, label = { Text(tr("Additional minutes", "دقائق إضافية")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                } else Text(if (kind == "carry") tr("Only the remainder moves. The original goal stays in history.", "يُنقل المتبقي فقط. يبقى الهدف الأصلي في السجل.") else tr("The goal is retained in edit history.", "يبقى الهدف محفوظًا في سجل التعديلات."))
            } }, confirmButton = { TextButton({
                if (scope != null && plan != null) save(when(kind) {
                    "progress" -> repository.progress(scope, plan.revision, command, task.id, if (count.isBlank()) 0 else count.toIntOrNull() ?: -1, if (minutes.isBlank()) 0 else minutes.toIntOrNull() ?: -1)
                    "carry" -> repository.carry(scope, plan.revision, command, task.id, StudyPeriods.next(task.start, task.period), true)
                    else -> repository.remove(scope, plan.revision, command, task.id)
                }); action = null
            }) { Text(tr("Confirm", "تأكيد")) } }, dismissButton = { TextButton({ action = null }) { Text(tr("Cancel", "إلغاء")) } })
    }
}

@Composable private fun GoalEditor(state: LearningState, old: PlanTask?, period: PlanPeriod, start: Long,
    close: () -> Unit, save: (PlanTask, String) -> PlanWrite) {
    var failure by remember { mutableStateOf<PlanWrite?>(null) }
    val id = remember { old?.id ?: UUID.randomUUID().toString() }
    var title by remember { mutableStateOf(old?.title.orEmpty()) }
    var subject by remember { mutableStateOf(old?.subject.orEmpty()) }
    var target by remember { mutableStateOf(old?.target?.toString() ?: "1") }
    var minutes by remember { mutableStateOf(old?.minutes?.toString() ?: "30") }
    var mode by remember { mutableStateOf(old?.mode ?: PlanMode.ACHIEVEMENT) }
    var linked by remember { mutableStateOf(old?.lesson) }
    // The same unchanged payload retries with the same command. Edits receive a new identity.
    val command = remember(title, subject, target, minutes, mode, linked) { UUID.randomUUID().toString() }
    androidx.compose.ui.window.Dialog(onDismissRequest = close) {
        Surface(Modifier.fillMaxWidth().imePadding(), shape = MaterialTheme.shapes.large) {
            ScreenPage(narrow = true) {
                Text(tr("Study goal", "هدف الدراسة"), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(title, { title = it.take(200) }, label = { Text(tr("Goal", "الهدف")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(subject, { subject = it.take(100) }, label = { Text(tr("Any school subject", "أي مادة دراسية")) }, modifier = Modifier.fillMaxWidth())
                PlanMode.entries.forEach { m -> FilterChip(mode == m, { mode = m }, label = { Text(modeLabel(m)) }, modifier = Modifier.heightIn(min = 48.dp)) }
                if (mode != PlanMode.TIME) OutlinedTextField(target, { target = it }, label = { Text(tr("Target units", "عدد الوحدات المستهدف")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                if (mode != PlanMode.ACHIEVEMENT) OutlinedTextField(minutes, { minutes = it }, label = { Text(tr("Target minutes", "الدقائق المستهدفة")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text(tr("Optional mathematics lesson link", "ربط اختياري بدرس رياضيات"))
                TextButton({ linked = null }) { Text(tr("No lesson link", "دون ربط بدرس")) }
                state.lessons.forEach { lesson -> FilterChip(linked?.lessonId == lesson.id,
                    { linked = StudentCatalog.ref(state, lesson.id) }, label = { Text(lesson.title.text()) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
                if (state.user?.studyStage == com.ahmadmol.enmath.core.model.StudyStage.BAC && state.catalogAvailable) {
                    Text(tr("Source references · content pending review", "مراجع المصدر · المحتوى قيد المراجعة"))
                    BacSourceCatalog.lessons.forEach { source -> FilterChip(linked?.lessonId == source.id,
                        { linked = BacSourceCatalog.ref(source.id) }, label = { Text(source.title) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
                }
                if (state.user?.studyStage == com.ahmadmol.enmath.core.model.StudyStage.G9 && state.catalogAvailable) {
                    Text(tr("Source references · content pending review", "مراجع المصدر · المحتوى قيد المراجعة"))
                    G9SourceCatalog.indexedLessons.forEach { source -> FilterChip(linked?.lessonId == source.ref.lessonId,
                        { linked = G9SourceCatalog.ref(requireNotNull(source.ref.lessonId)) }, label = { Text(source.title) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
                }
                PrimaryButton(tr("Save goal", "حفظ الهدف"), {
                    failure = save(PlanTask(id, title.trim(), subject.trim(), old?.period ?: period, old?.start ?: start, mode,
                        if (mode == PlanMode.TIME) 0 else target.toIntOrNull() ?: -1,
                        if (mode == PlanMode.ACHIEVEMENT) 0 else minutes.toIntOrNull() ?: -1,
                        old?.done ?: 0, old?.spent ?: 0, linked, "MANUAL", old?.parent, old?.closed ?: false), command)
                }, enabled = title.isNotBlank() && subject.isNotBlank())
                failure?.takeIf { it !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED) }?.let {
                    Text(if(it == PlanWrite.INVALID) tr("Check positive quantities and the lesson link.", "تحقق من الكميات الموجبة وربط الدرس.") else tr("Save unconfirmed. Close this form and reload; restart after a storage error.", "لم يُؤكَّد الحفظ. أغلق النموذج وأعد التحميل؛ أعد التشغيل بعد خطأ التخزين."), color = MaterialTheme.colorScheme.error)
                }
                TextButton(close) { Text(tr("Cancel", "إلغاء")) }
            }
        }
    }
}
