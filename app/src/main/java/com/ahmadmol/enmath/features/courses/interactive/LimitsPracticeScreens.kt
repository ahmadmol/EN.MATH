package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Star
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
        LessonPage("screen-practice") {
            if (q == null) {
                EmptyState(
                    tr("Practice unavailable", "التدريب غير متاح"),
                    tr("Return to the topic.", "عد إلى الموضوع."),
                )
                return@LessonPage
            }
            LessonProgressCard(tr("Limits · Practice", "النهايات · تدريب"), index + 1, set.questionIds.size, "learning-progress")
            val value = answers[q.id].orEmpty()
            LessonQuestion(q, value, { vm.answer(q.id, it) }, checked, vm.correct(q), !checked)
            LessonHint(q.hint.text(), q.id)
            if (checked) {
                LessonFeedback(vm.correct(q), limitsExplanation(q).text())
                if (!vm.correct(q))
                    TextButton(vm::retry, Modifier.testTag("practice-retry-answer")) {
                        Text(tr("Try this question again", "أعد محاولة هذا السؤال"))
                    }
            }
            LessonAction(tr("Verify my answer", "تحقّق من إجابتي"), {
                focus.clearFocus()
                keyboard?.hide()
                vm.check(q)
            }, value.isNotBlank() && !checked, "check-answer", verify = true)
            LessonAction(if (index == set.questionIds.lastIndex) tr("See my result", "عرض النتيجة") else tr("Continue", "متابعة"), {
                focus.clearFocus()
                keyboard?.hide()
                if (index == set.questionIds.lastIndex) {
                    if (vm.claimFinish(set.questionIds)) onFinish(answers, vm.seconds())
                } else vm.next(index)
            }, checked, "quiz-next")
        }
    }
}

@Composable
fun LimitsPracticeResult(state: LearningState, retry: () -> Unit, topic: () -> Unit) {
    val attempt = state.product.attempts.lastOrNull { it.setId == LimitsLessonSequence.practiceId }
    val mastery = LearningRules.topic(state, "limits")
    ScreenPage(Modifier.testTag("screen-practice-result")) {
        RefHeroCard(tr("Your practice result", "نتيجة تدريبك"),
            tr("Reflect, then continue", "افهم نتيجتك، ثم تابع"), artwork = { QuestionHeroArtwork() })
        if (attempt == null) {
            EmptyState(
                tr("No attempt yet", "لا توجد محاولة بعد"),
                tr("Complete the practice first.", "أكمل التدريب أولاً."),
            )
            return@ScreenPage
        }
        OutcomeMedallion(attempt.accuracy >= .8f)
        RefStatsRow(listOf(
            RefStatItem(androidx.compose.material.icons.Icons.Outlined.CheckCircle, "${attempt.correct}/${attempt.total}", tr("Correct answers", "الإجابات الصحيحة")),
            RefStatItem(androidx.compose.material.icons.Icons.Outlined.AccessTime, learningNumber(attempt.seconds), tr("Seconds", "ثانية")),
            RefStatItem(androidx.compose.material.icons.Icons.Outlined.Star, learningPercent((attempt.accuracy * 100).toInt()), tr("Accuracy", "الدقة")),
        ))
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
        RefPrimaryButton(
            tr("Continue learning", "متابعة التعلم"),
            topic,
            Modifier.testTag("result-topic"),
        )
        RefSecondaryButton(
            tr("Practice again", "إعادة التدريب"),
            retry,
            Modifier.testTag("result-retry"),
        )
    }
}
