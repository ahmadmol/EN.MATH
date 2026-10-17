package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.student.*
import com.ahmadmol.enmath.core.model.QuestionKind
import com.ahmadmol.enmath.features.practice.QuizViewModel

@Composable
fun LimitsPracticeScreen(
    state: LearningState,
    vm: QuizViewModel,
    onFinish: (Map<String, String>, Int) -> Unit,
) {
    val index by vm.index.collectAsStateWithLifecycle()
    val answers by vm.answers.collectAsStateWithLifecycle()
    val checked by vm.checked.collectAsStateWithLifecycle()
    val set = state.practiceSets.find { it.id == LimitsLessonSequence.practiceId }
    val q = set?.questionIds?.getOrNull(index)?.let { id -> state.questions.find { it.id == id } }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    key(index) {
        ExperiencePage("screen-practice") {
            PageHeading(
                tr("Apply the idea", "طبّق الفكرة"),
                tr("Practice · Limits", "تدريب النهايات"),
            )
            if (q == null) {
                EmptyState(
                    tr("Practice unavailable", "التدريب غير متاح"),
                    tr("Return to the topic.", "عد إلى الموضوع."),
                )
                return@ExperiencePage
            }
            ProgressFeedback(
                tr("One idea at a time", "فكرة واحدة في كل مرة"),
                tr(
                    "${index + 1} of ${set.questionIds.size}",
                    "${index + 1} من ${set.questionIds.size}",
                ),
                (index + 1f) / set.questionIds.size,
            )
            val value = answers[q.id].orEmpty()
            ExperiencePanel {
                Text(q.prompt.text(), style = MaterialTheme.typography.titleLarge)
                MathLine(q.expression)
                when (q.kind) {
                    QuestionKind.Choice,
                    QuestionKind.TrueFalse ->
                        InteractiveChoice(
                            q.options,
                            value,
                            { vm.answer(q.id, it) },
                            enabled = !checked,
                        )
                    QuestionKind.Numeric ->
                        NumericAnswer(value, { vm.answer(q.id, it) }, enabled = !checked)
                    QuestionKind.Expression ->
                        MathExpressionAnswer(value, { vm.answer(q.id, it) }, enabled = !checked)
                }
            }
            HintReveal(q.hint.text(), q.id)
            if (checked) {
                AnswerFeedback(vm.correct(q), limitsExplanation(q).text())
                if (!vm.correct(q))
                    TextButton(vm::retry, Modifier.testTag("practice-retry-answer")) {
                        Text(tr("Try this question again", "أعد محاولة هذا السؤال"))
                    }
                PrimaryButton(
                    if (index == set.questionIds.lastIndex) tr("See my result", "عرض النتيجة")
                    else tr("Next idea", "الفكرة التالية"),
                    {
                        focus.clearFocus()
                        keyboard?.hide()
                        if (index == set.questionIds.lastIndex) onFinish(answers, vm.seconds())
                        else vm.next()
                    },
                    Modifier.testTag("quiz-next"),
                )
            } else
                PrimaryButton(
                    tr("Check answer", "تحقّق من الإجابة"),
                    {
                        focus.clearFocus()
                        keyboard?.hide()
                        vm.check()
                    },
                    Modifier.testTag("check-answer"),
                    value.isNotBlank(),
                )
        }
    }
}

@Composable
fun LimitsPracticeResult(state: LearningState, retry: () -> Unit, topic: () -> Unit) {
    val attempt = state.product.attempts.lastOrNull { it.setId == LimitsLessonSequence.practiceId }
    val mastery = LearningRules.topic(state, "limits")
    ExperiencePage("screen-practice-result") {
        PageHeading(
            tr("Reflect, then continue", "افهم نتيجتك، ثم تابع"),
            tr("Your practice result", "نتيجة تدريبك"),
        )
        if (attempt == null) {
            EmptyState(
                tr("No attempt yet", "لا توجد محاولة بعد"),
                tr("Complete the practice first.", "أكمل التدريب أولاً."),
            )
            return@ExperiencePage
        }
        OutcomeMedallion(attempt.accuracy >= .8f)
        ExperiencePanel(emphasized = true) {
            Text(
                tr(
                    "${attempt.correct} of ${attempt.total} understood",
                    "${attempt.correct} من ${attempt.total} إجابات صحيحة",
                ),
                style = MaterialTheme.typography.headlineMedium,
            )
            ProgressFeedback(
                tr("Practice accuracy", "دقة التدريب"),
                "${(attempt.accuracy * 100).toInt()}%",
                attempt.accuracy,
            )
            Text(
                if (attempt.accuracy >= .8f)
                    tr(
                        "You can apply the limit rule. Keep explaining why each step is valid.",
                        "تستطيع تطبيق قاعدة النهاية. تابع تبرير صحة كل خطوة.",
                    )
                else
                    tr(
                        "Review substitution and the domain, then try again. Your explanation matters as much as the result.",
                        "راجع التعويض والمجال، ثم أعد المحاولة. فهم السبب مهم بقدر النتيجة.",
                    )
            )
        }
        ExperiencePanel {
            DetailRow(
                tr("Limits mastery", "إتقان النهايات"),
                "${mastery.score}%",
                Modifier.testTag("student-mastery-limits"),
            )
            Text(
                tr(
                    "Completion and practice feed the same progress shown to your teacher.",
                    "الإكمال والتدريب يحدّثان التقدم نفسه الذي يراه معلمك.",
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            state.practiceSets
                .find { it.id == LimitsLessonSequence.practiceId }
                ?.questionIds
                ?.forEach { id ->
                    val q = state.questions.find { it.id == id }
                    if (q != null && !LearningRules.correct(q, attempt.answers[id].orEmpty())) {
                        MathLine(q.expression, large = false)
                        Text(
                            limitsExplanation(q).text(),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
        }
        PrimaryButton(
            tr("Continue learning", "متابعة التعلم"),
            topic,
            Modifier.testTag("result-topic"),
        )
        SecondaryButton(
            tr("Practice again", "إعادة التدريب"),
            retry,
            Modifier.testTag("result-retry"),
        )
    }
}
