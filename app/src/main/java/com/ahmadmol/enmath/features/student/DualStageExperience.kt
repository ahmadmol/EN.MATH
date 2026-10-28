package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.features.planner.PlanSummary
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.features.courses.*

@Composable fun studentStageLabel(stage: StudyStage?) = if (stage == StudyStage.G9) tr("Grade 9", "الصف التاسع") else tr("Scientific Baccalaureate", "البكالوريا العلمي")

@Composable fun DualStageHome(state: LearningState, open: (String) -> Unit, gems: LocalGemsUiState, planner: PlannerRepository?) {
    if (state.user?.studyStage == StudyStage.G9) {
        Grade9RoadmapHomeScreen(state, open, gems, planner)
    } else {
        BaccalaureateRoadmapHomeScreen(state, open, gems, planner)
    }
}

@Composable fun ScopedStudyScreen(state: LearningState, open: (String) -> Unit, topicId: String? = null, showDemo: Boolean = false) {
    if (state.user?.studyStage == StudyStage.G9 && state.catalogAvailable) {
        if (topicId == null) Grade9StudyScreen(state, open) else Grade9UnitScreen(topicId, open)
        return
    }
    if (state.user?.studyStage == StudyStage.BAC && state.catalogAvailable && topicId == null && !showDemo) {
        BacSourceStudyScreen(open); return
    }
    ScreenPage(Modifier.testTag(if (topicId == null) "screen-study" else "screen-topic")) {
        PageHeading(studentStageLabel(state.user?.studyStage), tr("Study at your own pace", "تعلّم حسب وتيرتك"))
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("My study plan", "خطتي الدراسية")) }
        if (state.books.isEmpty()) { Text(tr("Content for this stage has not been published in this version.", "لم يُنشر محتوى هذه المرحلة في هذه النسخة.")); return@ScreenPage }
        Text(tr("Partial local demo content; this is not a complete official curriculum.", "محتوى محلي تجريبي جزئي؛ لا يمثل المنهج الرسمي الكامل."))
        if (topicId == null) state.books.forEach { book -> AppCard {
            Text(book.title.text(), style=MaterialTheme.typography.titleLarge)
            OutlinedButton({ open(if (showDemo) "demo/book/${book.id}" else "book/${book.id}") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Book details", "تفاصيل الكتاب")) }
            book.topics.forEach { topic -> OutlinedButton({ open("topic/${topic.id}") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(topic.title.text()) } }
        } } else {
            val topic = state.topic(topicId)
            if (topic == null) Text(tr("This unit is unavailable", "هذه الوحدة غير متاحة"))
            else {
                Text(topic.title.text(), style=MaterialTheme.typography.titleLarge)
                topic.lessons.forEachIndexed { index, lesson -> OutlinedButton({ open("lesson/${lesson.id}") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) {
                    Text("${index+1}. ${lesson.title.text()} · ${if (lesson.id in StudentCatalog.completed(state)) tr("Completed", "مكتمل") else tr("Available", "متاح")}")
                } }
                state.practiceSets.filter { it.topicId == topicId }.forEach { set ->
                    PrimaryButton(set.title.text(), { open("practice/${set.id}") }, Modifier.testTag("home-practice"))
                }
            }
        }
    }
}

@Composable fun ReviewRequiredNotice() { AppCard {
    Text(tr("Review required", "تحتاج مراجعة"), style=MaterialTheme.typography.titleMedium)
    Text(tr("Older records are preserved. Their curriculum edition has not been verified; they do not count toward current curriculum progress.", "السجلات القديمة محفوظة. لم يُتحقق من إصدار منهجها، لذلك لا تُحتسب ضمن تقدم المنهج الحالي."))
} }

data class RecordedMistake(val question: Question, val answer: String, val attempt: Attempt)
fun recordedMistakes(state: LearningState): List<RecordedMistake> = StudentCatalog.attempts(state).flatMap { attempt ->
    val set = state.practiceSets.find { it.id == attempt.setId }
    set?.questionIds.orEmpty().mapNotNull { id -> state.questions.find { it.id == id }?.let { q ->
        RecordedMistake(q, attempt.answers[id].orEmpty(), attempt)
    } }
}.asReversed().distinctBy { it.question.id }.filterNot { LearningRules.correct(it.question, it.answer) }

@Composable private fun ReviewSummary(state: LearningState, open: (String) -> Unit) { AppCard {
    val mistakes = recordedMistakes(state)
    Text(tr("Review priorities", "أولويات المراجعة"), style = MaterialTheme.typography.titleMedium)
    Text(if(mistakes.isEmpty()) tr("After saved practice, your mistakes will appear here.", "بعد التدريب المحفوظ ستظهر أخطاؤك هنا.") else tr("${mistakes.size} questions to revisit from saved attempts", "${mistakes.size} أسئلة للمراجعة من محاولات محفوظة"))
    TextButton({ open("review") }) { Text(tr("Mistake review", "مراجعة الأخطاء")) }
} }

@Composable fun RecordedProgressScreen(state: LearningState, open: (String) -> Unit, review: Boolean = false) {
    if (!review && state.user?.studyStage == StudyStage.G9 && state.catalogAvailable) {
        Grade9ProgressScreen(state, open); return
    }
    ScreenPage(Modifier.testTag(if(review) "screen-review" else "screen-progress")) {
        PageHeading(studentStageLabel(state.user?.studyStage), if(review) tr("Mistake review", "مراجعة الأخطاء") else tr("Recorded progress", "التقدم المحفوظ"))
        if(StudentCatalog.hasUnverifiedHistory(state)) ReviewRequiredNotice()
        if (!review) {
            BacSourceProgress(state)
            if (state.user?.studyStage == StudyStage.BAC) SectionTitle(tr("Existing demo progress", "تقدم الدروس التجريبية السابقة"))
            Text(tr("Completion describes available lessons, not mastery.", "الإكمال يصف الدروس المتاحة ولا يثبت الإتقان."))
            state.books.forEach { book -> AppCard {
                Text(book.title.text(), style=MaterialTheme.typography.titleLarge)
                Text("${book.lessons.count { it.id in StudentCatalog.completed(state) }}/${book.lessons.size}")
                TextButton({ open(if (state.user?.studyStage == StudyStage.BAC) "demo/book/${book.id}" else "book/${book.id}") }) { Text(tr("Continue in this book", "متابعة هذا الكتاب")) }
            } }
            val attempts = StudentCatalog.attempts(state)
            if(attempts.isEmpty()) Text(tr("There is not enough assessment evidence yet.", "لم تتوفر أدلة تقييم كافية بعد."))
            attempts.asReversed().forEach { a -> AppCard {
                Text(state.practiceSets.find { it.id == a.setId }?.title?.text().orEmpty())
                Text(tr("${a.correct}/${a.total} correct · ${a.seconds} seconds", "${a.correct}/${a.total} إجابة صحيحة · ${a.seconds} ثانية"))
                TextButton({ open("practice/${a.setId}/result") }) { Text(tr("Latest result for this practice", "آخر نتيجة لهذا التدريب")) }
            } }
        }
        val mistakes = recordedMistakes(state)
        if(mistakes.isEmpty()) Text(tr("No saved mistakes to review.", "لا أخطاء محفوظة للمراجعة."))
        mistakes.forEach { m -> AppCard {
            Text(m.question.prompt.text()); MathLine(m.question.expression)
            Text(tr("Your saved answer: ${m.answer.ifBlank { "—" }}", "إجابتك المحفوظة: ${m.answer.ifBlank { "—" }}"))
            Text(m.question.explanation.text())
            TextButton({ open("practice/${m.attempt.setId}") }) { Text(tr("Try this practice again", "جرّب هذا التدريب مجددًا")) }
        } }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Plan your next step", "خطّط لخطوتك التالية")) }
        OutlinedButton({ open("study") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Continue learning", "متابعة التعلم")) }
    }
}


@Composable fun SavedPracticeResultScreen(state: LearningState, setId: String, open: (String) -> Unit) {
    val set = state.practiceSets.find { it.id == setId }
    val attempt = StudentCatalog.attempts(state).lastOrNull { it.setId == setId }
    ScreenPage(Modifier.testTag("screen-practice-result")) {
        PageHeading(studentStageLabel(state.user?.studyStage), tr("Practice result", "نتيجة التدريب"))
        if (attempt == null || set == null) {
            Text(tr("No verified saved result is available for this practice. Older records remain preserved for review.", "لا توجد نتيجة محفوظة متحققة لهذا التدريب. السجلات القديمة محفوظة للمراجعة."))
        } else {
            Text(set.title.text(), style=MaterialTheme.typography.titleLarge)
            Text(tr("${attempt.correct}/${attempt.total} correct · ${attempt.seconds} seconds", "${attempt.correct}/${attempt.total} إجابة صحيحة · ${attempt.seconds} ثانية"))
            Text(tr("This result describes this attempt. It does not certify mastery.", "هذه النتيجة تصف هذه المحاولة ولا تثبت الإتقان."))
            set.questionIds.forEach { id -> state.questions.find { it.id == id }?.let { q -> AppCard {
                Text(q.prompt.text()); MathLine(q.expression)
                Text(if(LearningRules.correct(q, attempt.answers[id].orEmpty())) tr("Correct answer", "إجابة صحيحة") else tr("Revisit this answer", "راجع هذه الإجابة"))
                Text(tr("Your answer: ${attempt.answers[id].orEmpty().ifBlank { "—" }}", "إجابتك: ${attempt.answers[id].orEmpty().ifBlank { "—" }}"))
                Text(q.explanation.text())
            } } }
            PrimaryButton(tr("Practice again", "إعادة التدريب"), { open("practice/$setId") }, Modifier.testTag("result-retry"))
            OutlinedButton({ open("topic/${set.topicId}") }, Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("result-topic")) { Text(tr("Continue learning", "متابعة التعلم")) }
        }
        OutlinedButton({ open("review") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Mistake review", "مراجعة الأخطاء")) }
        OutlinedButton({ open("progress") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("View progress", "عرض التقدم")) }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(tr("Plan the next step", "خطة الخطوة التالية")) }
    }
}


@Composable fun PreservedLearningHistory(state: LearningState) {
    ScreenPage(Modifier.testTag("screen-preserved-history")) {
        PageHeading(studentStageLabel(state.user?.studyStage), tr("Preserved older records", "السجلات القديمة المحفوظة"))
        ReviewRequiredNotice()
        Text(tr("These are historical local records. Their stage, edition and assessment rubric are unverified; they do not establish current curriculum mastery.", "هذه سجلات محلية تاريخية. مرحلتها وإصدارها ومعيار تقييمها غير متحقق، ولا تثبت إتقان المنهج الحالي."))
        Text(tr("${state.completed.size} historical lesson completions", "${state.completed.size} إكمالات دروس تاريخية"))
        state.product.attempts.filter { it !in StudentCatalog.attempts(state) }.asReversed().forEachIndexed { index, attempt -> AppCard {
            Text(tr("Older attempt ${index + 1}", "محاولة قديمة ${index + 1}"), style=MaterialTheme.typography.titleMedium)
            Text(com.ahmadmol.enmath.features.planner.planDate(attempt.timestamp))
            Text(tr("Recorded at the time: ${attempt.correct}/${attempt.total}", "النتيجة المسجلة حينها: ${attempt.correct}/${attempt.total}"))
            attempt.answers.values.forEach { answer -> MathLine(answer.ifBlank { "—" }, large=false) }
        } }
        if(state.product.attempts.all { it in StudentCatalog.attempts(state) }) Text(tr("No older attempts in this file.", "لا محاولات قديمة في هذا الملف."))
    }
}
