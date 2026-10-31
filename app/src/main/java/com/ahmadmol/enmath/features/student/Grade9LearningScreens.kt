package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.MathLine
import com.ahmadmol.enmath.core.model.Attempt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

fun grade9LatestMistakes(data: G9StudyData): List<Pair<G9FractionQuestion, G9SavedAttempt>> {
    return Grade9Pilot.questions.mapNotNull { question ->
        val latest = data.attempts.asReversed().firstOrNull { !it.record.answers[question.id].isNullOrBlank() }
        latest?.takeUnless { Grade9Pilot.correct(question, it.record.answers[question.id].orEmpty()) }?.let { question to it }
    }
}

@Composable fun rememberG9Study(state: LearningState, repository: Grade9StudyRepository?): G9StudyRead? {
    val scope = StudentCatalog.scope(state)
    val changed = repository?.changes?.collectAsStateWithLifecycle()?.value
    return remember(scope, changed) { scope?.let { repository?.read(it) } }
}

@Composable fun G9StudyError(read: G9StudyRead?) {
    Text(if (read == G9StudyRead.StorageError) tr("Your saved study data could not be read. It has been preserved. Restart before trying again; do not clear app data.",
        "تعذّرت قراءة بيانات دراستك المحفوظة. حُفظت دون استبدال. أعد التشغيل قبل المحاولة؛ لا تمسح بيانات التطبيق.") else
        tr("Choose the current Grade 9 local file to continue.", "اختر ملف التاسع المحلي الحالي للمتابعة."), color = MaterialTheme.colorScheme.error)
}

@Composable fun G9LearningCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    OutlinedCard(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

@Composable fun G9Formula(value: String) {
    MathRender(value, style = MaterialTheme.typography.titleLarge,
        surfaceColor = MaterialTheme.colorScheme.primaryContainer, contentPadding = PaddingValues(12.dp))
    if (value.length > 30) Text(tr("Swipe the formula horizontally if needed", "اسحب الصيغة أفقيًا عند الحاجة"), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable fun G9FractionAnswerField(value: String, edit: (String) -> Unit, locked: Boolean = false,
    tag: String = "g9-answer", submit: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    OutlinedTextField(value, edit, Modifier.fillMaxWidth().testTag(tag), readOnly = locked, singleLine = true,
        label = { Text(tr("My fraction", "إجابتي ككسر")) },
        supportingText = { Text(tr("Numerator/denominator · Arabic or English digits", "بسط/مقام · أرقام عربية أو إنجليزية")) },
        textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); if (!locked && value.isNotBlank()) submit() }),
        trailingIcon = { if (!locked && '/' !in value) TextButton({ edit("$value/") }, Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) { Text("/") } })
}

/** Isolate numeric fractions as well as the operator runs handled by MathProse. */
@Composable private fun G9Prose(value: String) {
    val isolated = remember(value) { value.replace(Regex("""[0-9]+\s*/\s*[0-9]+""")) { "\u2066${it.value}\u2069" } }
    MathProse(isolated)
}

@Composable private fun G9WriteMessage(result: PlanWrite?) {
    if (result != null && result !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED))
        Text(tr("Not saved. Check the current file and storage; restart after a storage error.",
            "لم يُحفظ. تحقّق من الملف الحالي والتخزين؛ أعد التشغيل عند حدوث خطأ تخزين."), color = MaterialTheme.colorScheme.error)
}

@Composable fun Grade9LessonScreen(state: LearningState, repository: Grade9StudyRepository?, open: (String) -> Unit) {
    val read = rememberG9Study(state, repository)
    val data = (read as? G9StudyRead.Ready)?.data
    val scope = StudentCatalog.scope(state)
    val lesson = Grade9Pilot.content
    val total = lesson.sectionCount
    var section by rememberSaveable(scope?.ownerId, scope?.generation) { mutableIntStateOf((data?.section ?: 0).coerceAtMost(total - 1)) }
    var reveal by rememberSaveable(section) { mutableIntStateOf(1) }
    var answer by rememberSaveable(scope?.ownerId) { mutableStateOf("") }
    var checked by rememberSaveable(scope?.ownerId) { mutableStateOf(false) }
    var result by remember { mutableStateOf<PlanWrite?>(null) }
    Grade9Scaffold("screen-g9-lesson", section) {
        item("title") {
            Text(Grade9Pilot.title.text(), style = MaterialTheme.typography.headlineSmall)
            Text(tr("Original learning pilot · no gems", "درس أصلي تجريبي · دون جواهر"), style = MaterialTheme.typography.bodySmall)
        }
        if (data == null) { item("error") { G9StudyError(read) }; return@Grade9Scaffold }
        item("position") {
            Text(tr("Section ${section + 1} of $total", "القسم ${section + 1} من $total"))
            LinearProgressIndicator(progress = { (section + 1f) / total }, Modifier.fillMaxWidth())
            Text(tr("Reading position · not mastery", "موضع القراءة · ليس إتقانًا"), style = MaterialTheme.typography.bodySmall)
        }
        if (section < lesson.blocks.size) {
            val block = lesson.blocks[section]
            item("block-$section") { G9LearningCard(block.title.text()) {
                G9Prose(block.body.text()); if (block.formula.isNotBlank()) G9Formula(block.formula)
            } }
        } else if (section < total - 1) {
            val example = lesson.examples[section - lesson.blocks.size]
            item("example") { G9LearningCard(tr("Worked example", "مثال محلول")) { G9Prose(example.prompt.text()) } }
            items(example.steps.take(reveal)) { step -> G9LearningCard(step.title.text()) {
                G9Prose(step.body.text()); if (step.formula.isNotBlank()) G9Formula(step.formula)
            } }
            if (reveal < example.steps.size) item("reveal") {
                OutlinedButton({ reveal++ }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Show next step", "أظهر الخطوة التالية")) }
            }
        } else {
            item("summary") { G9LearningCard(tr("Summary", "الخلاصة")) {
                Text(lesson.summary.text())
            } }
            item("checkpoint") { G9LearningCard(tr("Check my understanding", "تحقّق من فهمي")) {
                Text(tr("Reduce this fraction. Enter a positive fraction in simplest form.", "اختزل هذا الكسر. اكتب كسرًا موجبًا بأبسط صورة."))
                G9Formula("54/90")
                fun verify() {
                    checked = true
                    if (Grade9Pilot.correct(Grade9Pilot.checkpoint, answer) && scope != null) result = repository?.passCheckpoint(scope, answer)
                }
                G9FractionAnswerField(answer, { if (it.length <= 128) { answer = it; checked = false } }, tag = "g9-checkpoint", submit = ::verify)
                Button(::verify, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = answer.isNotBlank()) { Text(tr("Check and save", "تحقّق واحفظ")) }
                if (checked) G9Prose(if (Grade9Pilot.correct(Grade9Pilot.checkpoint, answer)) tr("Correct. ", "صحيح. ") + Grade9Pilot.checkpoint.correction.text()
                    else tr("Review the GCD and try again. An equivalent but unreduced fraction is not enough.", "راجع القاسم المشترك الأكبر وحاول مجددًا. الكسر المكافئ غير المختزل لا يكفي."))
                if (data.checkpoint != null) Text(tr("Your checkpoint pass is saved. This is not certification of mastery.", "اجتيازك لنقطة التحقّق محفوظ. هذا ليس شهادة إتقان."))
                G9WriteMessage(result)
            } }
            item("practice") { Button({ open("g9/practice") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Practice this topic", "تدرّب على هذا البحث")) } }
        }
        item("navigation") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (section > 0) OutlinedButton({
                val next = section - 1
                if (scope != null) result = repository?.section(scope, next)
                if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) section = next
            }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Previous section", "القسم السابق")) }
            if (section < total - 1) Button({
                val next = section + 1
                if (scope != null) result = repository?.section(scope, next)
                if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) section = next
            }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Next section", "القسم التالي")) }
            G9WriteMessage(result)
            TextButton({ open("g9/reference/${Grade9Pilot.lessonId}") }, Modifier.heightIn(min = 48.dp)) { Text(tr("Source, save reference or add to plan", "المصدر وحفظ المرجع أو إضافته للخطة")) }
            }
        }
    }
}

class Grade9PracticeViewModel(private val draftIo: CoroutineDispatcher = Dispatchers.IO,
    private val draftJobs: CoroutineScope? = null,
    private val beforeDraftSave: suspend () -> Unit = { delay(350) }) : ViewModel() {
    val draft = MutableStateFlow<G9PracticeDraft?>(null)
    val error = MutableStateFlow(false)
    val boundOwner = MutableStateFlow<StudentScope?>(null)
    private var scope: StudentScope? = null
    private var repository: Grade9StudyRepository? = null
    private var seconds: Int? = null
    private var autosave: Job? = null
    var finished = false; private set
    fun bind(owner: StudentScope?, repo: Grade9StudyRepository?) {
        if (scope == owner && repository === repo && draft.value != null) return
        boundOwner.value = null
        autosave?.cancel(); seconds = null; finished = false
        scope = owner; repository = repo
        val read = owner?.let { repo?.read(it) }
        error.value = read !is G9StudyRead.Ready
        draft.value = (read as? G9StudyRead.Ready)?.data?.draft ?: G9PracticeDraft(UUID.randomUUID().toString())
        boundOwner.value = owner
    }
    private fun save(next: G9PracticeDraft) {
        if (error.value || finished) return
        autosave?.cancel()
        val versioned = next.copy(sequence = (draft.value?.sequence ?: 0) + 1)
        val result = scope?.let { repository?.saveDraft(it, versioned) }
        if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) draft.value = versioned else error.value = true
    }
    fun edit(answer: String) {
        val d = draft.value ?: return; val question = Grade9Pilot.questions[d.index]
        if (answer.length <= 128 && question.id !in d.checked && !error.value && !finished) {
            val next = d.copy(answers = d.answers + (question.id to answer), sequence = d.sequence + 1)
            draft.value = next
            autosave?.cancel()
            val owner = scope; val repo = repository
            autosave = (draftJobs ?: viewModelScope).launch {
                beforeDraftSave()
                val result = withContext(draftIo) { owner?.let { repo?.saveDraft(it, next) } }
                if (draft.value?.sequence == next.sequence && scope == owner && result !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) error.value = true
            }
        }
    }
    fun flushDraft() { autosave?.cancel(); if (!finished && !error.value) draft.value?.let { d ->
        val result = scope?.let { repository?.saveDraft(it, d) }
        if (result !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) error.value = true
    } }
    fun hint() { val d = draft.value ?: return; val id = Grade9Pilot.questions[d.index].id
        if (id !in d.checked && id !in d.hints) save(d.copy(hints = d.hints + id)) }
    fun check() { val d = draft.value ?: return; val id = Grade9Pilot.questions[d.index].id
        if (id !in d.checked) save(d.copy(answers = d.answers + (id to d.answers[id].orEmpty()), checked = d.checked + id)) }
    fun next() { val d = draft.value ?: return; if (d.index < Grade9Pilot.questions.lastIndex && Grade9Pilot.questions[d.index].id in d.checked) save(d.copy(index = d.index + 1)) }
    fun previous() { val d = draft.value ?: return; if (d.index > 0) save(d.copy(index = d.index - 1)) }
    fun finish(): String? {
        val d = draft.value ?: return null
        if (error.value || finished || d.index != Grade9Pilot.questions.lastIndex || Grade9Pilot.questions.any { it.id !in d.checked }) return null
        autosave?.cancel()
        val duration = seconds ?: ((System.currentTimeMillis() - d.started).coerceIn(0, 86_400_000) / 1000).toInt().also { seconds = it }
        val result = scope?.let { repository?.finish(it, d, duration) }
        if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) { finished = true; return d.id }
        error.value = true; return null
    }
    override fun onCleared() { flushDraft() }
}

@Composable fun Grade9PracticeScreen(state: LearningState, repository: Grade9StudyRepository?, vm: Grade9PracticeViewModel, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    LaunchedEffect(scope) { vm.bind(scope, repository) }
    DisposableEffect(scope, vm) { onDispose { vm.flushDraft() } }
    val draft by vm.draft.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val owner by vm.boundOwner.collectAsStateWithLifecycle()
    val d = draft.takeIf { scope != null && owner == scope }
    var confirmSkip by rememberSaveable(scope, d?.index) { mutableStateOf(false) }
    val keyboard = LocalSoftwareKeyboardController.current
    Grade9Scaffold("screen-g9-practice", d?.index) {
        item { Text(tr("Reduced fractions · practice", "كسور مختزلة · تدريب"), style = MaterialTheme.typography.headlineSmall) }
        if (error) { item { G9StudyError(G9StudyRead.StorageError) }; return@Grade9Scaffold }
        if (d == null) { item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CircularProgressIndicator(Modifier.size(32.dp))
            Text(tr("Loading your saved practice…", "جارٍ تحميل تدريبك المحفوظ…"))
        } }; return@Grade9Scaffold }
        val q = Grade9Pilot.questions[d.index]; val checked = q.id in d.checked
        item { Text(tr("Question ${d.index + 1} of ${Grade9Pilot.questions.size}", "السؤال ${d.index + 1} من ${Grade9Pilot.questions.size}")) }
        item { G9LearningCard(if (checked) tr("Review my answer", "راجع إجابتي") else tr("Reduce this fraction", "اختزل هذا الكسر")) {
            if (!checked) Text(tr("Use a positive fraction in simplest form. Check saves and locks your answer.", "اكتب كسرًا موجبًا بأبسط صورة. التحقّق يحفظ إجابتك ويثبّتها."))
            G9Formula("${q.numerator}/${q.denominator}")
            G9FractionAnswerField(d.answers[q.id].orEmpty(), vm::edit, checked, submit = vm::check)
            if (!checked) {
                OutlinedButton(vm::hint, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = q.id !in d.hints) {
                    Text(if (q.id in d.hints) tr("Hint shown", "ظهر التلميح") else tr("Hint", "تلميح")) }
                if (q.id in d.hints) Text(q.hint.text())
                Button({ keyboard?.hide(); if (d.answers[q.id].isNullOrBlank()) confirmSkip = true else vm.check() }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-check")) {
                    Text(if (d.answers[q.id].isNullOrBlank()) tr("Skip this question", "اترك هذا السؤال") else tr("Check answer", "تحقّق من الإجابة"))
                }
            } else {
                Text(if (Grade9Pilot.correct(q, d.answers[q.id].orEmpty())) tr("Correct", "صحيح") else if (d.answers[q.id].isNullOrBlank()) tr("Skipped", "متروك") else tr("Needs review", "تحتاج مراجعة"),
                    Modifier.semantics { liveRegion = LiveRegionMode.Polite }, style = MaterialTheme.typography.titleMedium)
                G9Formula(q.answer); G9Prose(q.correction.text())
                if (d.index < Grade9Pilot.questions.lastIndex) Button(vm::next, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Next question", "السؤال التالي")) }
                else Button({ vm.finish()?.let { open("g9/result/$it") } }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-finish")) { Text(tr("Save actual result", "احفظ النتيجة الفعلية")) }
            }
        } }
        if (d.index > 0) item { OutlinedButton(vm::previous, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-previous")) {
            Text(tr("Previous question · saved answers remain locked", "السؤال السابق · تبقى الإجابات المثبّتة محفوظة"))
        } }
    }
    if (confirmSkip && d != null && !error) G9SkipDialog({ confirmSkip = false; vm.check() }, { confirmSkip = false })
}

@Composable fun G9SkipDialog(confirm: () -> Unit, dismiss: () -> Unit) {
    AlertDialog(onDismissRequest = dismiss,
        title = { Text(tr("Leave this question unanswered?", "ترك السؤال دون إجابة؟")) },
        text = { Text(tr("It will be saved as skipped, not incorrect. You can review the correction and retry in a new attempt.", "سيُحفظ متروكًا، وليس إجابة خاطئة. يمكنك مراجعة التصحيح والمحاولة مجددًا في جلسة جديدة.")) },
        confirmButton = { TextButton(confirm, Modifier.heightIn(min = 48.dp)) { Text(tr("Skip and show correction", "اتركه وأظهر التصحيح")) } },
        dismissButton = { TextButton(dismiss, Modifier.heightIn(min = 48.dp)) { Text(tr("Keep answering", "تابع الإجابة")) } })
}

@Composable fun Grade9ResultScreen(state: LearningState, repository: Grade9StudyRepository?, id: String, open: (String) -> Unit) {
    val read = rememberG9Study(state, repository)
    val attempt = (read as? G9StudyRead.Ready)?.data?.attempts?.find { it.record.commandId == id }
    Grade9Scaffold("screen-g9-result") {
        item { Text(tr("Saved practice result", "نتيجة التدريب المحفوظة"), style = MaterialTheme.typography.headlineSmall) }
        if (attempt == null) { item { Text(tr("No saved result for this file and attempt.", "لا توجد نتيجة محفوظة لهذا الملف وهذه المحاولة.")); if (read !is G9StudyRead.Ready) G9StudyError(read) }; return@Grade9Scaffold }
        item { G9LearningCard(tr("This attempt only · not an exam or mastery score", "هذه المحاولة فقط · ليست امتحانًا أو درجة إتقان")) {
            val a = attempt.record
            val skipped = Grade9Pilot.questions.count { a.answers[it.id].isNullOrBlank() }
            Text(tr("${a.correct} correct · ${a.total - a.correct - skipped} incorrect · $skipped skipped", "${a.correct} صحيحة · ${a.total - a.correct - skipped} خاطئة · $skipped متروكة"))
            Text(tr("Hints used: ${attempt.hints.size}. No gems awarded.", "تلميحات مستخدمة: ${attempt.hints.size}. لا تُمنح جواهر."))
        } }
        items(Grade9Pilot.questions, key = { it.id }) { q -> G9LearningCard("${q.numerator}/${q.denominator}") {
            Text(if (attempt.record.answers[q.id].isNullOrBlank()) tr("Skipped", "متروك") else if (Grade9Pilot.correct(q, attempt.record.answers[q.id].orEmpty())) tr("Correct", "صحيح") else tr("Needs review", "تحتاج مراجعة"))
            Text(tr("Your saved answer", "إجابتك المحفوظة")); MathLine(attempt.record.answers[q.id].orEmpty().ifBlank { "—" })
            Text(tr("Reduced answer", "الإجابة المختزلة")); MathLine(q.answer); G9Prose(q.correction.text())
        } }
        item { Button({ open("g9/practice") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Start a new attempt", "ابدأ محاولة جديدة")) }
            TextButton({ open("review") }, Modifier.heightIn(min = 48.dp)) { Text(tr("My mistakes", "دفتر أخطائي")) } }
    }
}

@Composable fun Grade9EvidenceSection(state: LearningState, repository: Grade9StudyRepository?, open: (String) -> Unit, mistakes: Boolean = false) {
    val read = rememberG9Study(state, repository)
    val data = (read as? G9StudyRead.Ready)?.data
    if (data == null) { G9StudyError(read); return }
    G9LearningCard(if (mistakes) tr("Pilot mistake notebook", "دفتر أخطاء الدرس التجريبي") else tr("Pilot learning evidence", "أدلة الدرس التجريبي")) {
        if (!mistakes) {
            Text(if (data.checkpoint != null) tr("Understanding checkpoint passed", "تم اجتياز نقطة التحقّق") else tr("No checkpoint pass saved yet", "لا يوجد اجتياز محفوظ لنقطة التحقّق بعد"))
            val total = data.attempts.sumOf { it.record.total }; val correct = data.attempts.sumOf { it.record.correct }
            Text(if (total == 0) tr("No saved attempts yet", "لا توجد محاولات محفوظة بعد") else
                tr("${data.attempts.size} saved attempts · $correct/$total correct across those attempts", "${data.attempts.size} محاولات محفوظة · $correct/$total صحيحة عبر هذه المحاولات"))
        }
        val errors = grade9LatestMistakes(data)
        if (mistakes) Text(tr("Latest answered response per question. Skips do not erase earlier mistakes; a later correct answer resolves them.", "نعرض آخر إجابة فعلية لكل سؤال. ترك السؤال لا يمحو خطأً سابقًا، والإجابة الصحيحة اللاحقة تزيله من هذا الدفتر."))
        if (mistakes && errors.isEmpty()) Text(tr("No unresolved mistakes in saved pilot attempts.", "لا أخطاء غير محلولة في محاولات الدرس التجريبي المحفوظة."))
        if (mistakes) errors.forEach { (q, a) ->
            MathLine("${q.numerator}/${q.denominator}")
            Text(tr("Your saved answer", "إجابتك المحفوظة")); MathLine(a.record.answers[q.id].orEmpty())
            Text(tr("Reduced answer", "الإجابة المختزلة")); MathLine(q.answer)
            G9Prose(q.correction.text())
            TextButton({ open("g9/result/${a.record.commandId}") }) { Text(tr("Review saved attempt", "راجع المحاولة المحفوظة")) }
        }
        if (!mistakes) data.attempts.asReversed().forEach { saved ->
            OutlinedButton({ open("g9/result/${saved.record.commandId}") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("${saved.record.correct}/${saved.record.total} · " + com.ahmadmol.enmath.features.planner.planDate(saved.record.timestamp))
            }
        }
        TextButton({ open("lesson/${Grade9Pilot.lessonId}") }) { Text(tr("Review reduced fractions", "راجع الكسور المختزلة")) }
        TextButton({ open("g9/practice") }) { Text(tr("Practice again", "تدرّب مجددًا")) }
    }
}
