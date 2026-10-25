package com.ahmadmol.enmath.features.practice

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.interactive.*

@Composable
fun QuizQuestion(q: Question, value: String, onAnswer: (String) -> Unit) {
    Text(q.prompt.text(), style = MaterialTheme.typography.titleLarge)
    EquationCard(q.expression)
    if (q.kind == QuestionKind.Choice || q.kind == QuestionKind.TrueFalse)
        q.options.forEach { option ->
            OutlinedButton(
                { onAnswer(option) },
                Modifier.fillMaxWidth().heightIn(min = Space.touch).testTag("answer-$option"),
                shape = MaterialTheme.shapes.small,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor =
                            if (value == option) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                    ),
            ) {
                Text(
                    if (option == "true") label(TextKey.t_5558807e14)
                    else if (option == "false") label(TextKey.t_4605df753a) else option
                )
            }
        }
    else
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            AppTextField(
                value,
                onAnswer,
                label(TextKey.t_8c507ee733),
                Modifier.testTag("quiz-answer"),
            )
            if (q.kind == QuestionKind.Expression)
                Row(horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    listOf("x", "^", "/", "(", ")").forEach { key ->
                        FilledTonalButton({ onAnswer(value + key) }) { Text(key) }
                    }
                }
        }
}

@Composable
fun PracticeScreen(
    questionIds: List<String>,
    title: String,
    vm: QuizViewModel,
    hints: Boolean = true,
    assignment: Boolean = false,
    product: ProductState = ProductState(),
    onFinish: (Map<String, String>, Int) -> Unit,
) {
    val index by vm.index.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val answers by vm.answers.collectAsStateWithLifecycle()
    val checked by vm.checked.collectAsStateWithLifecycle()
    val hint by vm.hint.collectAsStateWithLifecycle()
    var confirm by remember { mutableStateOf(false) }
    var reveal by remember(index) { mutableStateOf(false) }
    val q = questionIds.getOrNull(index)?.let(product::question)
    key(index) {
        LessonPage("screen-practice") {
            if (q == null) {
                EmptyState(label(TextKey.t_4ae0bbc92c), label(TextKey.t_d8b994addd))
                return@LessonPage
            }
            LessonProgressCard(title, index + 1, questionIds.size, "learning-progress")
            LessonQuestion(q, answers[q.id].orEmpty(), { vm.answer(q.id, it) }, checked && !assignment,
                vm.correct(q), !checked || assignment)
            if (hints) LessonHint(q.hint.text(), hint, vm::hint)
            if (checked && !assignment) {
                LessonFeedback(vm.correct(q), q.explanation.text())
                if (!vm.correct(q)) SecondaryButton(label(TextKey.t_3b273d414e), vm::retry)
                TextButton({ reveal = true }) { Text(label(TextKey.t_9e72a9bde6)) }
                if (reveal) EquationCard(q.answer)
            }
            if (!assignment)
                LessonAction(tr("Verify my answer", "تحقّق من إجابتي"), {
                    focus.clearFocus()
                    keyboard?.hide()
                    vm.check(q)
                },
                    answers[q.id].orEmpty().isNotBlank() && !checked, "check-answer", verify = true)
            LessonAction(
                if (index == questionIds.lastIndex) label(TextKey.t_85731572ae) else tr("Continue", "متابعة"),
                {
                    focus.clearFocus()
                    keyboard?.hide()
                    if (index < questionIds.lastIndex) {
                        vm.next(index, requireChecked = !assignment)
                    } else if (assignment) confirm = true
                    else if (vm.claimFinish(questionIds)) onFinish(answers, vm.seconds())
                },
                (checked || assignment) && answers[q.id].orEmpty().isNotBlank(), "quiz-next",
            )
        }
    }
    if (confirm)
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(label(TextKey.t_13321b65ec)) },
            text = { Text(label(TextKey.t_55998b187e)) },
            confirmButton = {
                TextButton(
                    {
                        confirm = false
                        onFinish(answers, vm.seconds())
                    },
                    Modifier.testTag("submit-confirm"),
                ) {
                    Text(label(TextKey.t_85731572ae))
                }
            },
            dismissButton = {
                TextButton({ confirm = false }) { Text(label(TextKey.t_823a3ad20c)) }
            },
        )
}

@Composable
fun PracticeResultScreen(
    setId: String,
    state: LearningState,
    onRetry: () -> Unit,
    onTopic: () -> Unit,
) {
    val attempt = state.product.attempts.lastOrNull { it.setId == setId }
    val set = state.practiceSets.find { it.id == setId }
    ScreenPage(Modifier.testTag("screen-practice-result")) {
        RefHeroCard(
            title = label(TextKey.t_1d32c31329),
            subtitle = label(TextKey.t_1ce0bc63cd),
            badgeText = tr("Result", "النتيجة"),
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { QuestionHeroArtwork() }
        )

        if (attempt == null || set == null) {
            RefEmptyState(
                title = label(TextKey.t_9c505baa76),
                subtitle = label(TextKey.t_11002dea21),
                artwork = { SearchHeroArtwork() }
            )
            return@ScreenPage
        }

        OutcomeMedallion(attempt.accuracy >= .8f)

        RefStatsRow(
            stats = listOf(
                RefStatItem(
                    icon = androidx.compose.material.icons.Icons.Outlined.CheckCircle,
                    value = "${attempt.correct}/${attempt.total}",
                    label = label(TextKey.t_094706a820)
                ),
                RefStatItem(
                    icon = androidx.compose.material.icons.Icons.Outlined.AccessTime,
                    value = "${attempt.seconds}s",
                    label = tr("Time", "الوقت")
                ),
                RefStatItem(
                    icon = androidx.compose.material.icons.Icons.Outlined.Star,
                    value = "${(attempt.accuracy * 100).toInt()}%",
                    label = tr("Accuracy", "الدقة")
                )
            )
        )

        MasteryBadge(LearningRules.topic(state, set.topicId))

        RefSecondaryButton(label(TextKey.t_b5e692ba92), onRetry)
        RefPrimaryButton(label(TextKey.t_d475577a1a), onTopic)
    }
}
