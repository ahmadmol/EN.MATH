package com.ahmadmol.enmath.core.design.components.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.*
import com.ahmadmol.enmath.core.model.Question

@Composable
fun InteractiveChoice(
    options: List<String>,
    value: String,
    onAnswer: (String) -> Unit,
    tagPrefix: String = "answer-",
    enabled: Boolean = true,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            val selected = value == option
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = if (selected) RefColors.LightBlueHeroBgStart else androidx.compose.ui.graphics.Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    if (selected) 2.dp else 1.dp,
                    if (selected) RefColors.PrimaryBlue else RefColors.CardBorder
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("$tagPrefix$option")
                    .selectable(selected, enabled = enabled, role = Role.RadioButton) {
                        onAnswer(option)
                    },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (option == "true") tr("True", "صحيح")
                        else if (option == "false") tr("False", "غير صحيح") else option,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                            textDirection = TextDirection.ContentOrLtr
                        ),
                        color = if (selected) RefColors.PrimaryBlue else RefColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        enabled = enabled,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = RefColors.PrimaryBlue,
                            unselectedColor = RefColors.TextMuted
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NumericAnswer(
    value: String,
    onAnswer: (String) -> Unit,
    tag: String = "quiz-answer",
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        OutlinedTextField(
            value,
            onAnswer,
            Modifier.fillMaxWidth().testTag(tag),
            enabled = enabled,
            label = { Text(tr("Your numerical answer", "إجابتك العددية")) },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = MaterialTheme.shapes.medium,
        )
    }
}

@Composable
fun MathExpressionAnswer(
    value: String,
    onAnswer: (String) -> Unit,
    tag: String = "quiz-answer",
    enabled: Boolean = true,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value,
                onAnswer,
                Modifier.fillMaxWidth().testTag(tag),
                enabled = enabled,
                label = { Text(tr("Your expression", "تعبيرك الرياضي")) },
                singleLine = true,
                textStyle =
                    MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Ltr),
                shape = MaterialTheme.shapes.medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("x", "^", "/", "(", ")").forEach { key ->
                    OutlinedButton(
                        { onAnswer(value + key) },
                        Modifier.weight(1f),
                        enabled = enabled,
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        Text(key)
                    }
                }
            }
        }
    }
}

@Composable
fun HintReveal(hint: String, key: String, tag: String = "hint") {
    var expanded by rememberSaveable(key) { mutableStateOf(false) }
    Column(Modifier.animateContentSize()) {
        TextButton({ expanded = !expanded }, Modifier.testTag(tag)) {
            Text(
                if (expanded) tr("Hide hint", "إخفاء التلميح")
                else tr("A hint, please", "أحتاج تلميحاً")
            )
        }
        AnimatedVisibility(expanded) {
            Text(
                hint,
                Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun AnswerFeedback(correct: Boolean, explanation: String, modifier: Modifier = Modifier) {
    Surface(
        modifier.fillMaxWidth().animateContentSize().testTag("answer-feedback"),
        shape = MaterialTheme.shapes.medium,
        color =
            if (correct) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.errorContainer,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (correct) tr("That reasoning works", "إجابة صحيحة، تابع بهذا الفهم")
                else tr("Let's revisit the idea", "لنراجع الفكرة معاً"),
                style = MaterialTheme.typography.titleMedium,
                color =
                    if (correct) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                explanation,
                style = MaterialTheme.typography.bodyMedium,
                color =
                    if (correct) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
fun LessonCheckpoint(
    question: Question,
    value: String,
    onAnswer: (String) -> Unit,
    checked: Boolean,
    correct: Boolean,
) {
    Text(
        tr("Checkpoint · Can you apply the idea?", "نقطة تحقق · هل تستطيع تطبيق الفكرة؟"),
        style = MaterialTheme.typography.titleMedium,
    )
    MathLine(question.expression)
    NumericAnswer(value, onAnswer, "lesson-answer", !(checked && correct))
}

@Composable
fun WorkedExample(title: String, steps: List<Pair<String, String>>) {
    ExperiencePanel {
        ExperienceSection(title)
        steps.forEachIndexed { index, (equation, reason) ->
            Text(
                tr("Step ${index + 1}", "الخطوة ${index + 1}"),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            MathLine(equation, large = false)
            Text(reason, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ProgressFeedback(
    title: String,
    detail: String,
    fraction: Float,
    tag: String = "learning-progress",
) {
    Column(Modifier.testTag(tag), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailRow(title, detail)
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

