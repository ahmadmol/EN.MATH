package com.ahmadmol.enmath.features.practice

import androidx.compose.foundation.layout.*
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

@Composable
fun QuizQuestion(q: Question, value: String, onAnswer: (String) -> Unit) {
    Text(q.prompt.text(), style = MaterialTheme.typography.titleLarge)
    EquationCard(q.expression)
    if (q.kind == QuestionKind.Choice || q.kind == QuestionKind.TrueFalse)
        q.options.forEach { option ->
            OutlinedButton(
                { onAnswer(option) },
                Modifier.fillMaxWidth().testTag("answer-$option"),
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
    ScreenPage(Modifier.testTag("screen-practice"), narrow = true) {
        PageHeading(label(TextKey.t_013f0e8043), title)
        if (q == null) {
            EmptyState(label(TextKey.t_4ae0bbc92c), label(TextKey.t_d8b994addd))
            return@ScreenPage
        }
        Text(label(TextKey.t_b16ed530ed, index + 1, questionIds.size))
        LinearProgressIndicator(
            progress = { (index + 1f) / questionIds.size },
            modifier = Modifier.fillMaxWidth(),
        )
        QuizQuestion(q, answers[q.id].orEmpty()) { vm.answer(q.id, it) }
        if (hints)
            TextButton(vm::hint, Modifier.testTag("hint")) { Text(label(TextKey.t_5e8aec3b47)) }
        if (hint && hints) DemoNotice(q.hint.text())
        if (checked && !assignment) {
            AppCard(tonal = true) {
                Text(
                    if (vm.correct(q)) label(TextKey.t_584fa3f349) else label(TextKey.t_3b273d414e)
                )
                Text(q.explanation.text())
            }
            if (!vm.correct(q)) SecondaryButton(label(TextKey.t_3b273d414e), vm::retry)
            TextButton({ reveal = true }) { Text(label(TextKey.t_9e72a9bde6)) }
            if (reveal) EquationCard(q.answer)
        }
        if (!checked && !assignment)
            PrimaryButton(
                label(TextKey.t_6c91f383f6),
                vm::check,
                Modifier.testTag("check-answer"),
                answers[q.id].orEmpty().isNotBlank(),
            )
        if (checked || assignment)
            PrimaryButton(
                if (index == questionIds.lastIndex) label(TextKey.t_85731572ae)
                else label(TextKey.t_704198b924),
                {
                    focus.clearFocus()
                    keyboard?.hide()
                    if (index < questionIds.lastIndex) vm.next()
                    else if (assignment) confirm = true else onFinish(answers, vm.seconds())
                },
                Modifier.testTag("quiz-next"),
                answers[q.id].orEmpty().isNotBlank(),
            )
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
        PageHeading(label(TextKey.t_1ce0bc63cd), label(TextKey.t_1d32c31329))
        if (attempt == null || set == null) {
            EmptyState(label(TextKey.t_9c505baa76), label(TextKey.t_11002dea21))
            return@ScreenPage
        }
        ProgressCard(
            label(TextKey.t_094706a820),
            "${attempt.correct}/${attempt.total}",
            attempt.accuracy,
        )
        Text(label(TextKey.t_03e1b7049a, attempt.seconds))
        MasteryBadge(LearningRules.topic(state, set.topicId))
        DemoNotice(label(TextKey.t_29f6eb0ab8))
        SecondaryButton(label(TextKey.t_b5e692ba92), onRetry)
        PrimaryButton(label(TextKey.t_d475577a1a), onTopic)
    }
}
