package com.ahmadmol.enmath.features.courses.interactive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.ExperiencePage
import com.ahmadmol.enmath.core.design.components.student.NumericAnswer
import com.ahmadmol.enmath.core.design.components.student.MathExpressionAnswer
import com.ahmadmol.enmath.core.model.Question
import com.ahmadmol.enmath.core.model.QuestionKind

/** Uses the same premium palette and native equation renderer as the curriculum. */
@Composable
fun LessonPage(tag: String, content: @Composable ColumnScope.() -> Unit) {
    if (LocalStudentExperience.current) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = 640.dp).fillMaxSize()) { ExperiencePage(tag, content = content) }
        }
        return
    }
    PremiumLearningTheme {
        val dark = MaterialTheme.colorScheme.background.luminance() < .5f
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
            primary = if (dark) Color(0xFF85B7FF) else Color(0xFF0868ED),
            onPrimary = if (dark) Color(0xFF08254B) else Color.White,
            surfaceVariant = if (dark) Color(0xFF1A304D) else Color(0xFFEDF4FF),
        )) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = 640.dp).fillMaxSize()) { ExperiencePage(tag, content = content) }
        }
        }
    }
}

@Composable
fun LessonProgressCard(title: String, position: Int, total: Int, tag: String = "lesson-block-progress") {
    if (LocalStudentExperience.current) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag(tag))
    } else RefHeroCard(
        title = title,
        subtitle = tr("Your lesson journey", "رحلتك في الدرس"),
        modifier = Modifier.testTag(tag),
        artwork = { QuestionHeroArtwork() }
    )
    val progress = if (total > 0) (position.toFloat() / total).coerceIn(0f, 1f) else 0f
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, RefColors.CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            Text(
                text = tr("$position of $total", "$position من $total"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = RefColors.PrimaryBlue
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RefColors.CardBorder.copy(alpha = 0.5f))
                    .progressSemantics(progress)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RefColors.PrimaryBlue)
                )
            }
        }
    }
}

@Composable
fun MathematicalQuestionCard(expression: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Box(Modifier.heightIn(min = if (LocalStudentExperience.current) 80.dp else 112.dp).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            MathRender(expression, MaterialTheme.typography.headlineLarge, Color.Transparent)
        }
    }
}

@Composable
fun LessonQuestion(q: Question, value: String, onAnswer: (String) -> Unit,
    checked: Boolean, correct: Boolean, enabled: Boolean, prefix: String = "answer-", answerTag: String = "quiz-answer") {
    Text(if (q.kind == QuestionKind.Choice) tr("Choose the correct result", "اختر النتيجة الصحيحة") else q.prompt.text(),
        style = MaterialTheme.typography.titleLarge)
    // Keep any additional instructions supplied by the question catalog.
    if (q.kind == QuestionKind.Choice && q.prompt.text() != tr("Choose the correct result", "اختر النتيجة الصحيحة"))
        MathProse(q.prompt.text())
    MathematicalQuestionCard(q.expression)
    when (q.kind) {
        QuestionKind.Choice, QuestionKind.TrueFalse -> Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            q.options.forEach { option -> key(q.id, option) {
                AnswerOptionCard(option, value == option, enabled, checked && value == option, correct,
                    "$prefix$option") { onAnswer(option) }
            } }
        }
        QuestionKind.Numeric -> NumericAnswer(value, onAnswer, answerTag, enabled)
        QuestionKind.Expression -> MathExpressionAnswer(value, onAnswer, answerTag, enabled)
    }
}

@Composable
private fun AnswerOptionCard(option: String, selected: Boolean, enabled: Boolean,
    verified: Boolean, correct: Boolean, tag: String, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val accent = if (verified) { if (correct) colors.secondary else colors.error } else colors.primary
    val state = if (verified) { if (correct) tr("Correct answer", "إجابة صحيحة") else tr("Incorrect answer", "إجابة غير صحيحة") }
        else if (selected) tr("Selected", "محدد") else tr("Not selected", "غير محدد")
    Surface(Modifier.fillMaxWidth().testTag(tag)
        .selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
        .semantics { stateDescription = state },
        shape = RoundedCornerShape(22.dp), shadowElevation = if (selected) 0.dp else 2.dp,
        border = if (selected) BorderStroke(1.5.dp, accent) else null,
        color = if (selected) accent.copy(alpha = .10f).compositeOver(colors.surface) else colors.surface) {
        Row(Modifier.heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (verified) Icon(if (correct) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel, null,
                Modifier.size(28.dp), tint = accent)
            else RadioButton(selected, onClick = null, enabled = enabled,
                colors = RadioButtonDefaults.colors(selectedColor = accent))
            Box(Modifier.weight(1f)) {
                if (option == "true" || option == "false") Text(if (option == "true") tr("True", "صحيح") else tr("False", "غير صحيح"))
                else MathRender(option, MaterialTheme.typography.titleLarge, Color.Transparent,
                    if (androidx.compose.ui.platform.LocalLayoutDirection.current == androidx.compose.ui.unit.LayoutDirection.Rtl)
                        Alignment.CenterEnd else Alignment.CenterStart, PaddingValues(0.dp))
            }
        }
    }
}

@Composable
fun LessonHint(hint: String, id: String, tag: String = "hint") {
    var expanded by rememberSaveable(id) { mutableStateOf(false) }
    LessonHint(hint, expanded, { expanded = !expanded }, tag)
}

@Composable
fun LessonHint(hint: String, expanded: Boolean, onToggle: () -> Unit, tag: String = "hint") {
    if (hint.isBlank()) return
    Column(Modifier.fillMaxWidth()) {
        TextButton(onToggle, Modifier.heightIn(min = 48.dp).testTag(tag)
            .semantics { stateDescription = if (expanded) "expanded" else "collapsed" }) {
            Icon(Icons.Outlined.Lightbulb, null)
            Spacer(Modifier.width(8.dp))
            Text(if (expanded) tr("Hide hint", "إخفاء التلميح") else tr("I need a hint", "أحتاج تلميحًا"))
        }
        if (expanded) Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            Box(Modifier.fillMaxWidth().padding(16.dp)) { MathProse(hint) }
        }
    }
}

@Composable
fun LessonFeedback(correct: Boolean, explanation: String) {
    val color = if (correct) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    Surface(Modifier.fillMaxWidth().testTag("answer-feedback").semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(20.dp), color = color.copy(alpha = .10f).compositeOver(MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = .4f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (correct) Icons.Outlined.CheckCircle else Icons.Outlined.Info, null, tint = color)
                Text(if (correct) tr("Correct answer!", "إجابة صحيحة!") else tr("Understand the next step", "حاول فهم الخطوة التالية"),
                    style = MaterialTheme.typography.titleMedium)
            }
            if (explanation.isNotBlank()) MathProse(explanation)
        }
    }
}

@Composable
fun LessonAction(text: String, onClick: () -> Unit, enabled: Boolean, tag: String, verify: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Surface(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().testTag(tag),
        color = if (enabled) colors.primary else colors.onSurface.copy(alpha = .12f),
        contentColor = if (enabled) colors.onPrimary else colors.onSurface.copy(alpha = .5f)) {
        Row(Modifier.background(if (enabled) Brush.horizontalGradient(listOf(colors.primary, colors.primary.copy(alpha = .8f)))
            else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
            .heightIn(min = 58.dp).padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
            Icon(if (verify) Icons.Outlined.CheckCircle else Icons.AutoMirrored.Outlined.ArrowForward, null)
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}
