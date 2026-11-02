package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.core.navigation.Routes

/** All attempts/hints/wrong text are saved; deferred teacher judgment never becomes a correct mark. */
@Composable fun OriginalDraftPracticeScreen(state: LearningState, repository: ModernStudentRepository?, lesson: String,
    open: (String) -> Unit, result: Boolean = false) {
    val context = LocalContext.current
    val bank = remember(context) { OriginalDraftBank.loadQa(context) }
    val scope = StudentCatalog.scope(state)
    val reading = rememberModernStudent(state, repository)
    val data = (reading as? ModernRead.Ready)?.data
    val session = if (result) data?.results?.find { it.id == lesson } else data?.drafts?.find { it.lessonId == lesson }
    var failure by remember(scope, lesson) { mutableStateOf(false) }
    fun checked(status: ModernWrite?) { failure = status !in setOf(ModernWrite.SAVED, ModernWrite.ALREADY_SAVED) }
    LaunchedEffect(scope, lesson, result, bank) {
        if (!result && scope?.stage == StudyStage.G9 && bank != null && lesson != Grade9NatureLesson.LESSON_ID)
            checked(repository?.startQa(scope, lesson, bank))
    }
    LearningPage(Modifier.testTag(if (result) "screen-original-qa-result" else "screen-original-qa-practice"), "${scope?.ownerId}:$lesson:${session?.position}") {
        if (scope?.stage != StudyStage.G9 || bank == null) {
            Text(tr("Internal QA content is unavailable in this build.", "محتوى الاختبار الداخلي غير متاح في هذه النسخة."))
            OutlinedButton({ open(Routes.Home) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Return to journey", "العودة إلى الرحلة")) }
            return@LearningPage
        }
        if (lesson == Grade9NatureLesson.LESSON_ID) {
            PrimaryButton(tr("Open the existing Nature of Numbers pilot", "افتح تدريب طبيعة الأعداد الموجود"), { open(NaturePracticeGrader.ROUTE) })
            return@LearningPage
        }
        Text(tr("Internal QA · unreviewed draft", "اختبار داخلي · مسودة غير معتمدة"), style = MaterialTheme.typography.labelLarge)
        Text(tr("No gems, learning-day credit or official mastery are awarded for QA sessions.", "لا تمنح جلسات الاختبار الداخلي جواهر أو أيام تعلم أو إتقانًا رسميًا."))
        StudentAvatar(data?.selectedAvatarId, if (result) AvatarPose.COMPLETED else AvatarPose.THINKING, 64.dp)
        if (reading == ModernRead.StorageError || failure) {
            Text(tr("The save could not be confirmed. Your previous records are preserved; restart before retrying.", "لم يُؤكّد الحفظ. سجلاتك السابقة محفوظة؛ أعد التشغيل قبل المحاولة."), color = MaterialTheme.colorScheme.error)
            return@LearningPage
        }
        if (session == null) { Text(tr("Opening saved session…", "جارٍ فتح الجلسة المحفوظة…")); return@LearningPage }
        val source = SourceCurricula.forStage(StudyStage.G9).research(session.lessonId)
        Text(source?.title.orEmpty(), style = MaterialTheme.typography.headlineSmall)
        val questions = bank.forLesson(session.lessonId)
        if (result) {
            Text(tr("Your saved practice", "تدريبك المحفوظ"), style = MaterialTheme.typography.titleLarge)
            val last = questions.mapNotNull { session.last(it.id) }
            Text(tr("${last.count { it.grade == DraftGrade.CORRECT }} automatically checked answers", "${last.count { it.grade == DraftGrade.CORRECT }} إجابات صُحّحت آليًا"))
            Text(tr("${last.count { it.grade == DraftGrade.NEEDS_TEACHER_REVIEW }} answers need teacher review", "${last.count { it.grade == DraftGrade.NEEDS_TEACHER_REVIEW }} إجابات تحتاج مراجعة معلم"))
            Text(tr("${session.independentCorrect} correct on the first try without hints", "${session.independentCorrect} صحيحة من أول محاولة دون تلميح"))
            Text(tr("${session.submissions.count { it.grade == DraftGrade.INCORRECT }} saved incorrect attempts · ${session.hints.size} hints", "${session.submissions.count { it.grade == DraftGrade.INCORRECT }} محاولات خاطئة محفوظة · ${session.hints.size} تلميحات"))
            questions.forEach { q -> StudentPanel {
                MathProse(q.prompt)
                session.last(q.id)?.let { saved ->
                    if (q.interaction == DraftInteraction.WORKED_PROBLEM) WorkedDraftResponse.decode(saved.response).forEach { MathProse(it) }
                    else MathProse(saved.response)
                }
                Text(if (session.last(q.id)?.grade == DraftGrade.CORRECT) tr("Checked by the draft rule", "تم التصحيح بقاعدة المسودة") else tr("Teacher review pending", "مراجعة المعلم معلّقة"))
                MathProse(q.explanation)
            } }
            PrimaryButton(tr("Practice again", "تدرّب مجددًا"), { open("qa/practice/${session.lessonId}") }, Modifier.testTag("qa-retry-session"))
            OutlinedButton({ open(Routes.Home) }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Return to journey", "العودة إلى الرحلة")) }
            return@LearningPage
        }
        val q = questions.getOrNull(session.position) ?: return@LearningPage
        val response = session.answers[q.id].orEmpty()
        fun answer(value: String) { checked(repository?.answerQa(scope, lesson, bank, value)) }
        Text(tr("Question ${session.position + 1} of 6 · difficulty ${q.difficulty}", "السؤال ${session.position + 1} من 6 · الصعوبة ${q.difficulty}"))
        LinearProgressIndicator(progress = { session.position / 6f }, modifier = Modifier.fillMaxWidth())
        Text(when(q.interaction) {
            DraftInteraction.SINGLE_CHOICE -> tr("Choose an answer", "اختر إجابة")
            DraftInteraction.SHORT_INPUT -> tr("Short answer", "إجابة قصيرة")
            DraftInteraction.TRUE_FALSE -> tr("True or false", "صح أم خطأ")
            DraftInteraction.FILL_BLANK -> tr("Complete the blank", "أكمل الفراغ")
            DraftInteraction.ERROR_CORRECTION -> tr("Find and correct the error", "اكتشف الخطأ وصححه")
            DraftInteraction.WORKED_PROBLEM -> tr("Work through the problem", "حل المسألة بخطوات")
        }, style = MaterialTheme.typography.titleMedium)
        MathProse(q.prompt)
        when (q.interaction) {
            DraftInteraction.SINGLE_CHOICE -> q.choices.forEachIndexed { index, choice ->
                FilterChip(response == choice, onClick = { answer(choice) }, label = { MathProse(choice) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qa-choice-$index"))
            }
            DraftInteraction.TRUE_FALSE -> listOf("true" to tr("True", "صحيح"), "false" to tr("False", "خطأ")).forEach { (value, label) ->
                FilterChip(response == value, onClick = { answer(value) }, label = { Text(label) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qa-$value"))
            }
            DraftInteraction.WORKED_PROBLEM -> {
                val steps = WorkedDraftResponse.decode(response)
                listOf(tr("Given information", "المعطيات"), tr("Reasoning and steps", "التفكير والخطوات"), tr("Result and check", "النتيجة والتحقق")).forEachIndexed { index, title ->
                    OutlinedTextField(steps.getOrElse(index) { "" }, { value ->
                        val updated = steps.take(3).toMutableList(); updated[index] = value.take(600); answer(WorkedDraftResponse.encode(updated))
                    }, label = { Text(title) }, modifier = Modifier.fillMaxWidth().testTag("qa-step-$index"), minLines = 2)
                }
                Text(tr("Your reasoning will be saved for teacher review.", "سيُحفظ تفكيرك لمراجعة المعلم."))
            }
            else -> OutlinedTextField(response, { answer(it.take(2048)) }, label = { Text(tr("Your answer", "إجابتك")) }, modifier = Modifier.fillMaxWidth().testTag("qa-answer"), minLines = if (q.interaction == DraftInteraction.ERROR_CORRECTION) 3 else 1)
        }
        OutlinedButton({ checked(repository?.hintQa(scope, lesson, bank)) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qa-hint")) { Text(tr("Hint", "تلميح")) }
        if (q.id in session.hints) MathProse(q.hint)
        val last = session.last(q.id)?.takeIf { it.response == response }
        last?.let { submission ->
            StudentPanel(Modifier.semantics { liveRegion = LiveRegionMode.Polite }.testTag("qa-feedback")) {
                StudentAvatar(data?.selectedAvatarId, when(submission.grade) { DraftGrade.CORRECT -> AvatarPose.CORRECT; DraftGrade.INCORRECT -> AvatarPose.TRY_AGAIN; else -> AvatarPose.ENCOURAGING }, 48.dp)
                Text(when (submission.grade) {
                    DraftGrade.CORRECT -> tr("Correct according to the draft rule", "صحيح بحسب قاعدة المسودة")
                    DraftGrade.INCORRECT -> tr("Try again. Use the hint to take a smaller step.", "حاول مجددًا. استفد من التلميح وخذ خطوة أصغر.")
                    DraftGrade.NEEDS_TEACHER_REVIEW -> tr("Saved for teacher review · no automatic mark", "حُفظت لمراجعة المعلم · دون علامة آلية")
                })
                MathProse(q.explanation)
                if (submission.grade == DraftGrade.NEEDS_TEACHER_REVIEW) { Text(tr("Draft reference answer", "إجابة المسودة المرجعية")); MathProse(q.answer) }
            }
        }
        PrimaryButton(tr("Check / save response", "تحقق / احفظ الإجابة"), { checked(repository?.submitQa(scope, lesson, bank)) }, Modifier.testTag("qa-check"), response.isNotBlank())
        if (session.position > 0) OutlinedButton({ checked(repository?.moveQa(scope, lesson, bank, session.position - 1)) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("qa-previous")) { Text(tr("Previous question", "السؤال السابق")) }
        if (last != null && last.grade != DraftGrade.INCORRECT) {
            PrimaryButton(if (session.position == 5) tr("Save session result", "احفظ نتيجة الجلسة") else tr("Next question", "السؤال التالي"), {
                if (session.position == 5) {
                    val status = repository?.finishQa(scope, lesson, bank); checked(status)
                    if (status in setOf(ModernWrite.SAVED, ModernWrite.ALREADY_SAVED)) open("qa/result/${session.id}")
                } else checked(repository?.moveQa(scope, lesson, bank, session.position + 1))
            }, Modifier.testTag("qa-next"))
        }
    }
}

@Composable fun OriginalDraftReviewScreen(state: LearningState, repository: ModernStudentRepository?, open: (String) -> Unit) {
    val context = LocalContext.current
    val bank = remember(context) { OriginalDraftBank.loadQa(context) }
    val read = rememberModernStudent(state, repository)
    val data = (read as? ModernRead.Ready)?.data
    val mistakes = remember(data) { (data?.results.orEmpty() + data?.drafts.orEmpty()).flatMap { it.submissions }
        .filter { it.grade == DraftGrade.INCORRECT }.sortedByDescending { it.at }.distinctBy { it.questionId } }
    Grade9Scaffold("screen-qa-mistakes") {
        item { Text(tr("Internal QA mistake notebook", "دفتر أخطاء الاختبار الداخلي"), style = MaterialTheme.typography.headlineSmall) }
        if (bank == null || state.user?.studyStage != StudyStage.G9) {
            item { Text(tr("Internal draft content is unavailable in this build.", "محتوى المسودات الداخلي غير متاح في هذه النسخة.")) }
        } else if (data == null) {
            item { Text(tr("Saved review data is unavailable. Your records are preserved.", "تعذرت قراءة بيانات المراجعة. سجلاتك محفوظة.")) }
        } else {
            item { StudentAvatar(data.selectedAvatarId, AvatarPose.ENCOURAGING) }
            if (mistakes.isEmpty()) item { Text(tr("No saved incorrect QA answers yet.", "لا إجابات خاطئة محفوظة في الاختبار الداخلي بعد.")) }
            mistakes.forEach { wrong ->
                bank.questions.find { it.id == wrong.questionId }?.let { question ->
                    item(question.id) { StudentPanel(Modifier.testTag("qa-mistake-${question.id}")) {
                        MathProse(question.prompt)
                        Text(tr("Your saved incorrect response", "إجابتك الخاطئة المحفوظة"))
                        MathProse(wrong.response)
                        MathProse(question.hint)
                        MathProse(question.explanation)
                        Text(tr("The source question remains an unreviewed draft.", "السؤال الأصلي ما زال مسودة غير معتمدة."))
                        OutlinedButton({ open("qa/practice/${question.sourceLessonId}") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Practice this lesson again", "تدرّب على هذا الدرس مجددًا")) }
                    } }
                }
            }
        }
    }
}
