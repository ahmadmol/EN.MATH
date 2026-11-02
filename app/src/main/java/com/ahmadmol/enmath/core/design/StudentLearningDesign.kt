package com.ahmadmol.enmath.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/** Reading width shared by lesson, practice and results; height always follows scaled text. */
@Composable
fun LearningPage(modifier: Modifier = Modifier, stateKey: Any? = null, content: @Composable ColumnScope.() -> Unit) {
    val scroll = key(stateKey) { rememberScrollState() }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(modifier.widthIn(max = Space.readingMax).fillMaxSize()
            .imePadding().verticalScroll(scroll).padding(Space.lg),
            verticalArrangement = Arrangement.spacedBy(Space.lg), content = content)
    }
}

/** A single short fade, with no looping movement. Compose observes the system duration scale. */
@Composable
fun LearningFeedback(correct: Boolean, title: String, explanation: String, modifier: Modifier = Modifier,
    explanationTag: String = "learning-explanation") {
    AnimatedContent(Triple(correct, title, explanation), transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "answer-feedback") { (good, feedbackTitle, feedbackExplanation) ->
        Surface(modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
            shape = MaterialTheme.shapes.medium,
            color = if (good) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
            contentColor = if (good) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
            border = BorderStroke(Space.xs / 4, if (good) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error)) {
            Column(Modifier.padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                Text(feedbackTitle, style = MaterialTheme.typography.titleMedium)
                Box(Modifier.testTag(explanationTag)) { MathProse(feedbackExplanation) }
            }
        }
    }
}
