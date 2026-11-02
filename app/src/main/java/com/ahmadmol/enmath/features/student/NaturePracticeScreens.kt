package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.LearningPage
import com.ahmadmol.enmath.core.design.LearningFeedback
import com.ahmadmol.enmath.core.design.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A new sub-screen only. Does not replace or redraw the Grade 9 Home path. */
@Composable
fun NaturePracticeScreen(
    state: LearningState,
    repository: NaturePracticeRepository?,
    openResult: (String) -> Unit,
    goBack: () -> Unit,
    openLesson: () -> Unit = {}
) {
    val context = LocalContext.current
    val bank = remember(context) { runCatching { NaturePracticeBank.load(context) }.getOrNull() }
    NaturePracticeContent(state, repository, bank, openResult, goBack, openLesson = openLesson)
}

/** Production layout, also rendered by host previews with an isolated repository snapshot. */
@Composable
internal fun NaturePracticeContent(
    state: LearningState,
    repository: NaturePracticeRepository?,
    bank: List<NaturePracticeQuestion>?,
    openResult: (String) -> Unit,
    goBack: () -> Unit,
    initialRead: NatureRead? = null,
    openLesson: () -> Unit = {}
) {
    val scope = StudentCatalog.scope(state)
    var read by remember(scope) { mutableStateOf<NatureRead?>(initialRead) }
    var busy by remember(scope) { mutableStateOf(false) }
    val jobs = rememberCoroutineScope()

    LaunchedEffect(scope, repository, bank) {
        read = if (scope == null || repository == null || bank == null) NatureRead.OwnerChanged
        else withContext(Dispatchers.IO) { repository.start(scope) }
    }
    val data = (read as? NatureRead.Ready)?.data
    val draft = data?.draft
    val question = draft?.let { bank?.getOrNull(it.position) }
    val savedResponse = question?.let { draft.answers[it.id] }.orEmpty()
    var evaluation by remember(scope, draft?.id, draft?.position) {
        mutableStateOf<Boolean?>(savedResponse.takeIf { it.isNotBlank() }?.let {
            NaturePracticeGrader.correct(question!!.id, it)
        })
    }
    var input by rememberSaveable(scope?.ownerId, draft?.id, draft?.position) { mutableStateOf(savedResponse) }
    var chosenParts by remember(scope?.ownerId, draft?.id, draft?.position) { mutableStateOf(savedResponse.split('|').filter { it.isNotBlank() }) }
    var rootClass by rememberSaveable(scope?.ownerId, draft?.id, draft?.position) { mutableStateOf(savedResponse.substringAfter('|', "")) }
    var rootValue by rememberSaveable(scope?.ownerId, draft?.id, draft?.position) { mutableStateOf(savedResponse.substringBefore('|')) }
    var chosenGroups by remember(scope?.ownerId, draft?.id, draft?.position) { mutableStateOf(NaturePracticeGrader.restoreCategories(savedResponse)) }

    fun perform(block: suspend () -> Unit) {
        if (busy) return
        busy = true
        jobs.launch { try { block() } finally { busy = false } }
    }

    val response = when (question?.interaction) {
        "fill_blank" -> chosenParts.joinToString("|")
        "error_correction" -> "$rootValue|$rootClass"
        "worked_problem" -> NaturePracticeGrader.serializeCategories(chosenGroups)
        else -> input
    }
    val readyResponse = when (question?.interaction) {
        "error_correction" -> rootValue.isNotBlank() && rootClass.isNotBlank()
        "worked_problem" -> chosenGroups.size == (question.classificationTokens.size)
        else -> response.isNotBlank()
    }
    val safeAction = scope != null && repository != null && bank != null && question != null && !busy
    val isPassed = question?.id in (draft?.passed ?: emptySet())

    LearningPage(Modifier.testTag("screen-g9-nature-practice"), stateKey = draft?.position) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = goBack,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(tr("Return to learning path", "العودة إلى المسار"))
            }
            OutlinedButton(
                onClick = openLesson,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("g9-nature-open-lesson"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(tr("Lesson explanation", "شرح الدرس"))
            }
        }

        Text(
            tr("Nature of numbers · pilot", "طبيعة الأعداد · تدريب تجريبي"),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            tr("Original draft exercises — not a mastery certificate or official exam", "تمارين أصلية قيد المراجعة — ليست شهادة إتقان ولا أسئلة وزارية"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (bank == null) {
            Text(
                tr("Practice content failed validation; no data was changed.", "تعذر التحقق من ملف الأسئلة؛ لم تتغير بياناتك."),
                color = MaterialTheme.colorScheme.error
            )
            return@LearningPage
        }
        if (read == NatureRead.OwnerChanged || read == NatureRead.StorageError) {
            Text(
                tr(
                    "Student file or local storage cannot be read. Your previous data is preserved.",
                    "تعذر قراءة ملف الطالب أو التخزين المحلي. بياناتك السابقة محفوظة دون استبدال."
                ),
                color = MaterialTheme.colorScheme.error
            )
            return@LearningPage
        }
        if (question == null) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            return@LearningPage
        }

        Text(
            tr("Question ${draft.position + 1} of 6", "السؤال ${draft.position + 1} من 6"),
            modifier = Modifier.testTag("g9-nature-counter"),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
        LinearProgressIndicator(
            progress = { draft.passed.size / 6f },
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .testTag("g9-nature-progress")
        )
        Text(
            when (question.difficulty) {
                1 -> tr("Level 1 · foundation", "المستوى الأول · التأسيس")
                2 -> tr("Level 2 · application", "المستوى الثاني · التطبيق")
                else -> tr("Level 3 · challenge", "المستوى الثالث · التحدي")
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedCard(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    naturePracticeProse(question.prompt),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.testTag("g9-nature-prompt")
                )
                when (question.interaction) {
                    "single_choice" -> question.choices.forEachIndexed { index, option ->
                        OutlinedButton(
                            onClick = { input = index.toString(); evaluation = null },
                            enabled = !isPassed && !busy,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("g9-nature-option-$index")
                                .semantics { selected = input == index.toString() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (input == index.toString())
                                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                disabledContainerColor = if (input == index.toString())
                                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                disabledContentColor = if (input == index.toString())
                                    MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr))
                        }
                    }
                    "short_input" -> OutlinedTextField(
                        value = input,
                        onValueChange = { if (it.length <= 100) { input = it; evaluation = null } },
                        enabled = !isPassed && !busy,
                        label = { Text(tr("Simplified fraction", "الكسر المختزل")) },
                        supportingText = {
                            Text(
                                tr(
                                    "Use numerator/denominator. Arabic and Persian digits accepted.",
                                    "اكتب بسط/مقام. يمكن استخدام الأرقام العربية أو الفارسية."
                                )
                            )
                        },
                        textStyle = TextStyle(textDirection = TextDirection.Ltr),
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("g9-nature-fraction")
                    )
                    "true_false" -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("true" to tr("True", "صحيح"), "false" to tr("False", "خطأ")).forEach { (value, title) ->
                            OutlinedButton(
                                onClick = { input = value; evaluation = null },
                                enabled = !isPassed && !busy,
                                modifier = Modifier.weight(1f).heightIn(min = 54.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (input == value)
                                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    disabledContainerColor = if (input == value)
                                        MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    disabledContentColor = if (input == value)
                                        MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) { Text(title, style = MaterialTheme.typography.titleMedium) }
                        }
                    }
                    "fill_blank" -> {
                        Text(
                            tr("Choose all parts that complete the statement", "اختر الأجزاء التي تُكمل العبارة"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            question.guidedParts.forEach { value ->
                                FilterChip(
                                    selected = value in chosenParts,
                                    enabled = !isPassed && !busy,
                                    onClick = {
                                        chosenParts = if (value in chosenParts) chosenParts - value else chosenParts + value
                                        evaluation = null
                                    },
                                    label = { Text(value, modifier = Modifier.padding(vertical = 4.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        disabledSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                )
                            }
                        }
                    }
                    "error_correction" -> {
                        Text(
                            tr("Correct the value and classify it", "صحّح قيمة الجذر ثم صنّفها"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = rootValue,
                            onValueChange = { if (it.length <= 20) { rootValue = it; evaluation = null } },
                            enabled = !isPassed && !busy,
                            label = { Text("√25 =", style = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr)) },
                            textStyle = TextStyle(textDirection = TextDirection.Ltr),
                            colors = OutlinedTextFieldDefaults.colors(disabledTextColor = MaterialTheme.colorScheme.onSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("g9-nature-root-value")
                        )
                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            question.classOptions.forEach { value ->
                                FilterChip(
                                    selected = rootClass == value || (rootClass == "integer" && value == "صحيح") ||
                                        (rootClass == "irrational" && value == "غير عادي"),
                                    enabled = !isPassed && !busy,
                                    onClick = {
                                        rootClass = if (value == "صحيح") "integer" else "irrational"
                                        evaluation = null
                                    },
                                    label = { Text(value, modifier = Modifier.padding(vertical = 4.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        disabledSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                )
                            }
                        }
                    }
                    "worked_problem" -> {
                        Text(
                            tr("Choose the smallest number group for each expression", "اختر أصغر مجموعة لكل عدد"),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        question.classificationTokens.forEach { token ->
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    token,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        textDirection = TextDirection.Ltr,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    question.groups.forEach { (key, title) ->
                                        FilterChip(
                                            selected = chosenGroups[token] == key,
                                            enabled = !isPassed && !busy,
                                            onClick = {
                                                chosenGroups = chosenGroups + (token to key)
                                                evaluation = null
                                            },
                                            label = { Text(title, modifier = Modifier.padding(vertical = 4.dp)) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                disabledSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (question.id in draft.hints) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Text(
                    text = question.hint,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(14.dp).testTag("g9-nature-hint")
                )
            }
        }

        if (isPassed || evaluation == false) {
            LearningFeedback(isPassed,
                if (isPassed) tr("Correct. Here is why:", "إجابة صحيحة. إليك التفسير:")
                else tr("Not yet. Try again; no points or gems are lost.", "ليست صحيحة بعد. حاول مرة أخرى؛ لن تخسر أي نقاط أو جواهر."),
                naturePracticeProse(question.explanation), Modifier.testTag("g9-nature-feedback"),
                explanationTag = "g9-nature-explanation")
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (draft.position > 0) {
                OutlinedButton(
                    onClick = {
                        if (scope != null && repository != null) perform {
                            val status = withContext(Dispatchers.IO) { repository.move(scope, draft.position - 1) }
                            if (status in listOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) {
                                read = withContext(Dispatchers.IO) { repository.read(scope) }
                            } else read = if (status == PlanWrite.OWNER_CHANGED) NatureRead.OwnerChanged else NatureRead.StorageError
                        }
                    },
                    enabled = safeAction,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(tr("Previous", "السابق")) }
            }
            if (!isPassed && question.id !in draft.hints) {
                OutlinedButton(
                    onClick = {
                        if (scope != null && repository != null) perform {
                            val status = withContext(Dispatchers.IO) { repository.hint(scope) }
                            if (status in listOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) {
                                read = withContext(Dispatchers.IO) { repository.read(scope) }
                            } else read = if (status == PlanWrite.OWNER_CHANGED) NatureRead.OwnerChanged else NatureRead.StorageError
                        }
                    },
                    enabled = safeAction,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                ) { Text(tr("Hint", "تلميح")) }
            }
        }

        if (!isPassed) {
            Button(
                onClick = {
                    if (scope != null && repository != null) perform {
                        val res = withContext(Dispatchers.IO) { repository.submit(scope, question.id, response) }
                        if (res.status in listOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) {
                            read = withContext(Dispatchers.IO) { repository.read(scope) }
                            evaluation = res.correct
                        } else read = NatureRead.StorageError
                    }
                },
                enabled = safeAction && readyResponse,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("g9-nature-check")
            ) {
                Text(tr("Check answer", "تحقّق من الإجابة"), style = MaterialTheme.typography.titleMedium)
            }
        }

        if (isPassed) {
            Button(
                onClick = {
                    if (scope != null && repository != null) perform {
                        if (draft.position == 5) {
                            val result = withContext(Dispatchers.IO) { repository.finish(scope) }
                            if (result.resultId != null) withContext(Dispatchers.Main.immediate) {
                                openResult(result.resultId)
                            }
                            else read = NatureRead.StorageError
                        } else {
                            val status = withContext(Dispatchers.IO) { repository.move(scope, draft.position + 1) }
                            if (status in listOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) {
                                evaluation = null
                                read = withContext(Dispatchers.IO) { repository.read(scope) }
                            } else read = NatureRead.StorageError
                        }
                    }
                },
                enabled = safeAction,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("g9-nature-next")
            ) {
                Text(
                    if (draft.position == 5) tr("View session result", "عرض نتيجة الجلسة")
                    else tr("Next question", "السؤال التالي"),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun NaturePracticeResultScreen(
    state: LearningState,
    repository: NaturePracticeRepository?,
    id: String,
    backToPath: () -> Unit,
    retry: () -> Unit,
    openLesson: () -> Unit = {}
) {
    val scope = StudentCatalog.scope(state)
    val read = remember(scope, id, repository) { scope?.let { repository?.read(it) } }
    val attempt = (read as? NatureRead.Ready)?.data?.attempts?.firstOrNull { it.id == id }

    LearningPage(Modifier.testTag("screen-g9-nature-result")) {
        Text(
            tr("Session complete", "اكتملت الجلسة"),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        if (attempt == null) {
            Text(
                tr("Result unavailable in this student file.", "النتيجة غير متاحة في ملف الطالب الحالي."),
                color = MaterialTheme.colorScheme.error
            )
            OutlinedButton(
                onClick = backToPath,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(tr("Learning path", "المسار الدراسي"))
            }
            return@LearningPage
        }

        val metrics = StudentLearningProgressEngine.computeAttemptMetrics(attempt)
        Text(tr("First-try independent answers: ", "إجابات مستقلة من أول محاولة: ") + metrics.firstTryCorrectCount + "/" + metrics.totalQuestions,
            modifier = Modifier.testTag("nature-result-first-try"), style = MaterialTheme.typography.titleMedium)
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    tr("You finished 6 original practice activities.", "أنهيت 6 أنشطة تدريبية أصلية."),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    tr(
                        "Attempts needing another try: ${attempt.missed.values.sum()}. Hints: ${attempt.hints.size}.",
                        "المحاولات التي احتاجت إعادة: ${attempt.missed.values.sum()}. التلميحات: ${attempt.hints.size}."
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    tr(
                        "This is practice, not certified mastery. No gems were awarded.",
                        "هذه جلسة تدريب وليست إثباتًا للإتقان. لم تُمنح أي جواهر."
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        val reviewIds = StudentLearningProgressEngine.ALL_NATURE_IDS.filter { (attempt.missed[it] ?: 0) > 0 || it in attempt.hints }
        if (reviewIds.isNotEmpty()) {
            Text(tr("Your next step", "خطوتك التالية"), style = MaterialTheme.typography.titleLarge)
            Text(tr("Review these questions in the lesson, then try again independently.", "راجع هذه الأسئلة في شرح الدرس، ثم جرّب مجددًا دون مساعدة."))
            reviewIds.forEach { questionId ->
                val number = StudentLearningProgressEngine.ALL_NATURE_IDS.indexOf(questionId) + 1
                Text(tr("Question ", "السؤال ") + number + " · " + tr("Retries: ", "إعادات: ") + (attempt.missed[questionId] ?: 0) +
                    if (questionId in attempt.hints) tr(" · hint used", " · استُخدم تلميح") else "")
            }
        }

        Button(
            onClick = backToPath,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(tr("Back to learning path", "العودة إلى المسار الدراسي"), style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = retry,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(tr("Practice again", "التدرّب من جديد"), style = MaterialTheme.typography.titleMedium)
        }

        OutlinedButton(
            onClick = openLesson,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("g9-nature-review-lesson"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(tr("Review lesson explanation", "مراجعة شرح الدرس"), style = MaterialTheme.typography.titleMedium)
        }
    }
}
