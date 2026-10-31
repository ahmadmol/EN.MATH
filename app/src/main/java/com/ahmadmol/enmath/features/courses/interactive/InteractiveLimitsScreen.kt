package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.design.components.student.*
import com.ahmadmol.enmath.core.model.Copy
import com.ahmadmol.enmath.core.model.Question

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
        visualContent = {
            val x by vm.explorationX.collectAsStateWithLifecycle()
            LimitApproachVisual(x, vm::exploreX)
        },
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
    LaunchedEffect(vm) { vm.restoreLegacyReview(blocks) }
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val block = blocks.getOrNull(index)
    key(index) {
        LessonPage("screen-interactive-lesson") {
            EducationalInfoBanner(tr("Demo lesson · independent learning evidence", "درس تجريبي · أدلة تعلّم مستقلة"))
            LessonProgressCard(
                "$title · ${tr("Guided practice", "تدريب موجّه")}",
                if (blocks.isEmpty()) 0 else index + 1, blocks.size,
            )
            if (block == null) {
                EmptyState(
                    tr("Lesson unavailable", "الدرس غير متاح"),
                    tr("Return to the curriculum.", "عد إلى المنهج."),
                )
                return@LessonPage
            }
            val q = block.question()
            val value = q?.let { answers[it.id] }.orEmpty()
            val correct = q?.let(vm::correct) == true
            Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
                color = if (q == null) MaterialTheme.colorScheme.surface else androidx.compose.ui.graphics.Color.Transparent) {
                Column(Modifier.padding(if (q == null) 16.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(tr("Explore the idea", "استكشف الفكرة"), Modifier.weight(1f),
                                        style = MaterialTheme.typography.headlineSmall)
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .08f)) {
                                        Icon(Icons.Outlined.Lightbulb, null, Modifier.padding(12.dp).size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                MathProse(block.caption.text(), MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant))
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
                                LessonHint(block.body.text(), block.id, "lesson-hint")
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
                                        { if (vm.claimCompletion(blocks)) onComplete() },
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
                                    LessonQuestion(q, value, { vm.answer(q.id, it) }, checked, correct,
                                        !(checked && correct), "lesson-option-", "lesson-answer")
                                    LessonHint(q.hint.text(), block.id, "lesson-hint")
                                    if (checked) {
                                        LessonFeedback(correct, explain(q).text())
                                    }
                                    LessonAction(
                                        tr("Verify my answer", "تحقّق من إجابتي"),
                                        {
                                            focus.clearFocus()
                                            keyboard?.hide()
                                            vm.check(q)
                                        },
                                        value.isNotBlank() && !checked, "lesson-check", verify = true,
                                    )
                                }
                        }
                    }
                    if (block !is LessonBlock.Completion)
                        LessonAction(
                            tr("Continue", "متابعة"),
                            {
                                focus.clearFocus()
                                keyboard?.hide()
                                vm.move(index + 1, blocks)
                            },
                            q == null || (checked && correct), "lesson-continue",
                        )
                    if (index > 0) {
                        TextButton({ vm.move(index - 1, blocks) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("lesson-previous")) {
                            Text(tr("Previous idea", "الفكرة السابقة"))
                        }
                    }
                }
            }
        }
    }
}
