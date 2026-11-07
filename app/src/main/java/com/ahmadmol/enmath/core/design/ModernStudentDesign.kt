package com.ahmadmol.enmath.core.design

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.AvatarPose
import com.ahmadmol.enmath.core.data.StudentAvatars
import com.ahmadmol.enmath.features.reference.CleanNavItem

object StudentExperienceTokens {
    val panel = RoundedCornerShape(28.dp)
    val compact = RoundedCornerShape(18.dp)
    val circle = 64.dp
    val floatAboveBar = circle / 2
    val contentWidth = 640.dp
    val touch = 48.dp
}
val LocalStudentAvatarId = staticCompositionLocalOf<String?> { null }
val LocalReducedMotion = staticCompositionLocalOf { false }

@Composable fun avatarName(id: String?) = when (id) {
    "muadh" -> tr("Muadh", "معاذ")
    "waseem" -> tr("Waseem", "وسيم")
    "lama" -> tr("Lama", "لمى")
    else -> tr("Choose your companion", "اختر رفيق رحلتك")
}

/** Same saved identity for every pose. Preview pose sheets are not production portraits. */
@Composable fun StudentAvatar(id: String? = LocalStudentAvatarId.current, pose: AvatarPose = AvatarPose.IDLE,
    size: Dp = 64.dp, modifier: Modifier = Modifier) {
    val owner = StudentAvatars.poseOwner(id, pose)
    val colors = MaterialTheme.colorScheme
    val tint = when (owner) { "waseem" -> colors.secondaryContainer; "lama" -> colors.tertiaryContainer; else -> colors.primaryContainer }
    val ink = when (owner) { "waseem" -> colors.onSecondaryContainer; "lama" -> colors.onTertiaryContainer; else -> colors.onPrimaryContainer }
    val reaction = avatarReaction(pose)
    val reduce = LocalReducedMotion.current
    val scale = remember(owner) { Animatable(1f) }
    LaunchedEffect(owner, pose, reduce) {
        if (reduce) scale.snapTo(1f) else {
            scale.snapTo(0.94f)
            scale.animateTo(1f, tween(160))
        }
    }
    val description = avatarName(owner) + " · " + tr("temporary portrait", "رمز مؤقت")
    Surface(modifier.size(size).graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }.testTag("student-avatar-${owner ?: "neutral"}").semantics { contentDescription = description },
        shape = CircleShape, color = tint, contentColor = ink, border = BorderStroke(1.dp, ink.copy(alpha = .15f))) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Person, null, Modifier.size(size * .58f))
            if (owner != null) Text(when (owner) { "muadh" -> "م"; "waseem" -> "و"; else -> "ل" },
                Modifier.align(Alignment.BottomCenter), style = MaterialTheme.typography.labelSmall)
            AnimatedVisibility(reaction != null, Modifier.align(Alignment.TopEnd), enter = fadeIn(tween(if (reduce) 0 else 120))) {
                Icon(reaction ?: Icons.Rounded.Person, null, Modifier.size(size * .28f).testTag("avatar-pose-${pose.name}"))
            }
        }
    }
}

private fun avatarReaction(pose: AvatarPose) = when (pose) {
    AvatarPose.CORRECT, AvatarPose.COMPLETED, AvatarPose.CELEBRATING -> Icons.Rounded.Check
    AvatarPose.TRY_AGAIN, AvatarPose.ENCOURAGING -> Icons.Rounded.Favorite
    AvatarPose.THINKING -> Icons.Rounded.Psychology
    AvatarPose.WELCOME -> Icons.Rounded.WavingHand
    AvatarPose.STREAK -> Icons.Rounded.LocalFireDepartment
    AvatarPose.IDLE -> null
}

@Composable fun StudentPanel(modifier: Modifier = Modifier, emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier.fillMaxWidth(), shape = StudentExperienceTokens.panel,
        color = colors.surface, border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = .7f)),
        tonalElevation = if (emphasized) 2.dp else 0.dp) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
            if (emphasized) colors.primaryContainer.copy(alpha = .6f) else colors.surface,
            colors.surface))).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

/** Circle center is exactly on the bar's top edge. Its entire touch target is measured. */
@Composable fun ModernStudentBottomBar(items: List<CleanNavItem>, selected: String?, tagPrefix: String, open: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPadding()) {
        val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp, lineBreak = LineBreak.Simple, hyphens = Hyphens.None)
        val bold = labelStyle.copy(fontWeight = FontWeight.Bold)
        val textWidths = items.map { item ->
            val result = measurer.measure(item.title, style = bold, constraints = Constraints())
            with(density) { result.size.width.toDp() }
        }
        val equal = maxWidth / items.size
        val measured = items.maxOf { measurer.measure(it.title, style = bold, constraints = Constraints()).size.height }
        val labelHeight = with(density) { measured.toDp() }
        val barHeight = maxOf(76.dp, labelHeight + 64.dp)
        val total = StudentExperienceTokens.floatAboveBar + barHeight
        Surface(Modifier.padding(top = StudentExperienceTokens.floatAboveBar).fillMaxWidth().height(barHeight).testTag("student-nav-surface"),
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp), color = colors.surfaceContainer,
            tonalElevation = 3.dp, shadowElevation = 3.dp) {}
        Row(Modifier.fillMaxWidth().height(total).horizontalScroll(rememberScrollState()).selectableGroup(), verticalAlignment = Alignment.Top) {
            items.forEachIndexed { index, item ->
                val active = item.route == selected
                val tint by animateColorAsState(if (active) colors.primary else colors.onSurfaceVariant, label = "navigation-tint")
                val slot = Modifier.width(maxOf(equal, textWidths[index] * 2f + 12.dp))
                if (index == 2) {
                    Column(slot, horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(StudentExperienceTokens.circle).shadow(6.dp, CircleShape)
                            .clip(CircleShape).background(Brush.linearGradient(listOf(colors.primary, colors.secondary)))
                            .selectable(active, onClick = { open(item.route) }, role = Role.Tab)
                            .testTag(tagPrefix + item.route).semantics { contentDescription = item.title }, contentAlignment = Alignment.Center) {
                            Icon(item.icon, null, Modifier.size(28.dp), tint = colors.onPrimary)
                        }
                        Text(item.title, Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp, end = 4.dp), color = tint,
                            style = labelStyle, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                    }
                } else {
                    Column(slot.padding(top = StudentExperienceTokens.floatAboveBar)
                        .height(barHeight).clip(StudentExperienceTokens.compact)
                        .selectable(active, onClick = { open(item.route) }, role = Role.Tab,
                            interactionSource = remember { MutableInteractionSource() }, indication = ripple())
                        .testTag(tagPrefix + item.route).padding(horizontal = 4.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.size(40.dp, 28.dp).background(if (active) colors.primaryContainer else colors.surfaceContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(item.icon, null, Modifier.size(24.dp), tint = tint)
                        }
                        Text(item.title, Modifier.fillMaxWidth(), color = tint, style = labelStyle, fontWeight = if (active) FontWeight.Bold else FontWeight.Medium, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }
        }
    }
}
