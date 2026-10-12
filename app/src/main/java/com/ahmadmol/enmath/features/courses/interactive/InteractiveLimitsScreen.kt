package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.student.*
import com.ahmadmol.enmath.core.design.tokens.ExperienceTokens
import com.ahmadmol.enmath.core.model.Copy
import com.ahmadmol.enmath.core.model.Question
import com.ahmadmol.enmath.core.model.QuestionKind
import java.util.Locale

@Composable
fun LimitApproachVisual() {
    var x by rememberSaveable { mutableFloatStateOf(2.7f) }
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.outlineVariant
    val surface = MaterialTheme.colorScheme.surface
    Canvas(Modifier.fillMaxWidth().height(ExperienceTokens.visualHeight).testTag("limit-visual")) {
        val left = 24f
        val right = size.width - 24f
        val top = 20f
        val bottom = size.height - 20f
        fun point(v: Float) =
            Offset(left + (v - 2f) / 2f * (right - left), bottom - (v - 2f) / 2f * (bottom - top))
        drawLine(muted, Offset(left, bottom), Offset(right, bottom), 2f)
        drawLine(muted, Offset(left, top), Offset(left, bottom), 2f)
        drawLine(primary, point(2f), point(4f), 4f)
        drawLine(muted, Offset(point(3f).x, top), Offset(point(3f).x, bottom), 1f)
        drawCircle(surface, 7f, point(3f))
        drawCircle(primary, 7f, point(3f), style = Stroke(2f))
        if (kotlin.math.abs(x - 3f) > .005f) drawCircle(primary, 6f, point(x))
    }
    MathLine(
        "f(x) = x + 2, x ≠ 3\nx = ${String.format(Locale.US, "%.2f", x)}    ${if (kotlin.math.abs(x - 3f) < .005f) "f(3) undefined; limit = 5" else "f(x) = ${String.format(Locale.US, "%.2f", x + 2)}"}",
        large = false,
    )
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Slider(x, { x = it }, valueRange = 2.5f..3.5f, modifier = Modifier.testTag("limit-slider"))
    }
    Text(
        tr(
            "The open circle is at (3, 5). The horizontal axis is x from 2 to 4; the vertical axis is y from 4 to 6.",
            "الدائرة المفتوحة عند (3، 5). المحور الأفقي x من 2 إلى 4، والعمودي y من 4 إلى 6.",
        ),
        style = MaterialTheme.typography.bodySmall,
    )
}

@Composable
fun InteractiveLimitsScreen(
    state: LearningState,
    vm: InteractiveLessonViewModel,
    onComplete: () -> Unit,
    onPractice: () -> Unit,
) {
    InteractiveLessonScreen(
        state.lesson(LimitsLessonSequence.lessonId)?.title?.text().orEmpty(),
        LimitsLessonSequence.blocks(state),
        LimitsLessonSequence.lessonId in state.completed,
        vm,
        onComplete,
        onPractice,
        ::limitsExplanation,
        visualContent = { LimitApproachVisual() },
    )
}

/** Reusable block renderer; lesson content, visuals and question explanations are supplied. */
@Composable
fun InteractiveLessonScreen(
    title: String,
    blocks: List<LessonBlock>,
    completed: Boolean,
    vm: InteractiveLessonViewModel,
    onComplete: () -> Unit,
    onPractice: () -> Unit,
    explain: (Question) -> Copy,
    visualContent: @Composable (LessonBlock.Visual) -> Unit,
) {
    val index by vm.step.collectAsStateWithLifecycle()
    val answers by vm.answers.collectAsStateWithLifecycle()
    val checked by vm.checked.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val block = blocks.getOrNull(index)
    key(index) {
        ExperiencePage("screen-interactive-lesson") {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            ProgressFeedback(
                tr("Your lesson", "رحلتك في الدرس"),
                tr("${index + 1} of ${blocks.size}", "${index + 1} من ${blocks.size}"),
                (index + 1f) / blocks.size.coerceAtLeast(1),
                "lesson-block-progress",
            )
            if (block == null) {
                EmptyState(
                    tr("Lesson unavailable", "الدرس غير متاح"),
                    tr("Return to the curriculum.", "عد إلى المنهج."),
                )
                return@ExperiencePage
            }
            val q = block.question()
            val value = q?.let { answers[it.id] }.orEmpty()
            val correct = q?.let { LearningRules.correct(it, value) } == true
            Column(
                Modifier.testTag("block-${block.id}"),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (block) {
                    is LessonBlock.Explanation -> {
                        ExperienceSection(block.title.text())
                        Text(block.body.text(), style = MaterialTheme.typography.bodyLarge)
                    }
                    is LessonBlock.Visual -> {
                        ExperienceSection(
                            tr("Explore the idea", "استكشف الفكرة"),
                            block.caption.text(),
                        )
                        visualContent(block)
                    }
                    is LessonBlock.Equation -> {
                        ExperienceSection(tr("A rule you can use", "قاعدة يمكنك استخدامها"))
                        MathLine(block.expression)
                        Text(block.reason.text())
                    }
                    is LessonBlock.WorkedExample -> {
                        MathLine(block.expression, large = false)
                        WorkedExample(
                            tr("Worked example", "مثال محلول"),
                            block.steps.map { it.first to it.second.text() },
                        )
                    }
                    is LessonBlock.Hint -> {
                        ExperienceSection(tr("Before the checkpoint", "قبل نقطة التحقق"))
                        HintReveal(block.body.text(), block.id, "lesson-hint")
                        Text(
                            tr(
                                "Use this reminder before the final checkpoint.",
                                "استعن بهذا التذكير قبل نقطة التحقق الأخيرة.",
                            )
                        )
                    }
                    is LessonBlock.Completion -> {
                        ExperienceSection(block.title.text(), block.body.text())
                        if (!completed)
                            PrimaryButton(
                                tr("Complete lesson", "إكمال الدرس"),
                                onComplete,
                                Modifier.testTag("complete-lesson"),
                            )
                        else {
                            CompletedLabel()
                            PrimaryButton(
                                tr("Practice the idea", "تدرّب على الفكرة"),
                                onPractice,
                                Modifier.testTag("lesson-practice"),
                            )
                        }
                    }
                    else ->
                        if (q != null) {
                            ExperienceSection(
                                if (block is LessonBlock.Checkpoint) tr("Checkpoint", "نقطة التحقق")
                                else q.prompt.text()
                            )
                            if (block is LessonBlock.Checkpoint)
                                LessonCheckpoint(
                                    q,
                                    value,
                                    { vm.answer(q.id, it) },
                                    checked,
                                    correct,
                                )
                            else {
                                MathLine(q.expression)
                                when (q.kind) {
                                    QuestionKind.Choice,
                                    QuestionKind.TrueFalse ->
                                        InteractiveChoice(
                                            q.options,
                                            value,
                                            { vm.answer(q.id, it) },
                                            "lesson-option-",
                                            !(checked && correct),
                                        )
                                    QuestionKind.Numeric ->
                                        NumericAnswer(
                                            value,
                                            { vm.answer(q.id, it) },
                                            "lesson-answer",
                                            !(checked && correct),
                                        )
                                    QuestionKind.Expression ->
                                        MathExpressionAnswer(
                                            value,
                                            { vm.answer(q.id, it) },
                                            "lesson-answer",
                                            !(checked && correct),
                                        )
                                }
                            }
                            HintReveal(q.hint.text(), block.id, "lesson-hint")
                            if (checked) AnswerFeedback(correct, explain(q).text())
                            if (!(checked && correct))
                                PrimaryButton(
                                    tr("Check my reasoning", "تحقّق من إجابتي"),
                                    {
                                        focus.clearFocus()
                                        keyboard?.hide()
                                        vm.check()
                                    },
                                    Modifier.testTag("lesson-check"),
                                    value.isNotBlank(),
                                )
                        }
                }
            }
            if (block !is LessonBlock.Completion)
                PrimaryButton(
                    tr("Continue", "متابعة"),
                    {
                        focus.clearFocus()
                        keyboard?.hide()
                        vm.move(index + 1)
                    },
                    Modifier.testTag("lesson-continue"),
                    q == null || (checked && correct),
                )
            if (index > 0)
                TextButton({ vm.move(index - 1) }, Modifier.testTag("lesson-previous")) {
                    Text(tr("Previous idea", "الفكرة السابقة"))
                }
        }
    }
}
