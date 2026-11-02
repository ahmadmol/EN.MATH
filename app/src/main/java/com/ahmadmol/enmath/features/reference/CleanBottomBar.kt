package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import com.ahmadmol.enmath.core.design.LocalStudyStage
import com.ahmadmol.enmath.core.model.StudyStage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.text.style.TextAlign
import com.ahmadmol.enmath.core.design.LocalStudentExperience

data class CleanNavItem(val route: String, val title: String, val icon: ImageVector)

object BottomNavLayoutDefaults {
    val FloatingTopPadding = 10.dp
    val BarContentHeight = 68.dp
    val TotalBarHeight = FloatingTopPadding + BarContentHeight // 78.dp
    val CenterCircleSize = 52.dp
    val IconSize = 22.dp
    val TextFontSize = 11.sp

    /**
     * Calculates estimated max content height inside a bottom nav item for a given font scale factor.
     */
    fun estimatedContentHeightDp(fontScale: Float): Float {
        val textHeightSp = TextFontSize.value * fontScale * 1.25f
        return IconSize.value + 4f + textHeightSp
    }
}

/**
 * Custom Bottom Navigation Bar matching reference specification:
 * - Full-width rectangular bar with top divider.
 * - Center Home button elevated blue circle (#0084FF) floating ~10dp above bar top edge.
 * - Unbounded clipping allowed for floating circle while bar height remains strictly bounded (78.dp + insets).
 * - Label "الرئيسية" fully visible below circle without bottom cut-off.
 * - Accessible selectable semantics (Role.Tab, selected state, TalkBack support).
 * - Circular ripple visual interaction feedback strictly on floating circle without square highlights.
 * - Flexible for Font Scale x1, x1.5, x2 without stretching full screen.
 */
@Composable
fun CleanBottomBar(
    items: List<CleanNavItem>,
    selected: String?,
    tagPrefix: String,
    dark: Boolean = true,
    open: (String) -> Unit,
    accessibleCenter: Boolean = false,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val student = LocalStudentExperience.current
    val g9 = LocalStudyStage.current == StudyStage.G9
    val labelStyle = if (g9) MaterialTheme.typography.bodyMedium.copy(
        lineBreak = LineBreak.Paragraph, hyphens = Hyphens.Auto, lineHeight = 14.sp) else LocalTextStyle.current
    val navBgColor = if (student) MaterialTheme.colorScheme.surface else if (isDark) Color(0xFF111221) else Color(0xFFFFFFFF)
    val dividerColor = if (student) MaterialTheme.colorScheme.outlineVariant else if (isDark) Color(0xFF1E2038) else Color(0xFFE0E3EF)
    val activeColor = if (student) MaterialTheme.colorScheme.primary else Color(0xFF0084FF)
    val inactiveColor = if (student) MaterialTheme.colorScheme.onSurfaceVariant else if (isDark) Color(0xFF8A8F9E) else Color(0xFF6C727F)
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        val labelWidth = with(density) { (maxWidth / items.size.coerceAtLeast(1) - 8.dp).roundToPx().coerceAtLeast(1) }
        val labelHeight = if (student) items.maxOfOrNull {
            textMeasurer.measure(it.title, style = (if (g9) labelStyle else MaterialTheme.typography.bodyMedium).copy(
                fontSize = BottomNavLayoutDefaults.TextFontSize, fontWeight = FontWeight.Bold),
                constraints = Constraints(maxWidth = labelWidth)).size.height
        } ?: 0 else 0
        val contentHeight = if (student) maxOf(BottomNavLayoutDefaults.BarContentHeight,
            with(density) { labelHeight.toDp() } + 52.dp) else BottomNavLayoutDefaults.BarContentHeight
        // Rectangular Bar Background
        Surface(
            modifier = Modifier
                .padding(top = BottomNavLayoutDefaults.FloatingTopPadding)
                .fillMaxWidth()
                .height(contentHeight),
            color = navBgColor,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Box(Modifier.fillMaxSize()) {
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.TopCenter),
                    thickness = 1.dp,
                    color = dividerColor
                )
            }
        }

        // Navigation Row
        Row(
            modifier = Modifier
                .padding(top = BottomNavLayoutDefaults.FloatingTopPadding)
                .fillMaxWidth()
                .height(contentHeight)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                val isCenter = index == 2
                val isSelected = selected == item.route

                if (isCenter) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(if (accessibleCenter) Modifier.selectable(
                                selected = isSelected, onClick = { open(item.route) }, role = Role.Tab
                            ) else Modifier)
                            .testTag("$tagPrefix${item.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier
                                .fillMaxHeight()
                                .then(if (accessibleCenter) Modifier else Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { open(item.route) })
                        ) {
                            Box(
                                modifier = Modifier
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)
                                        val offsetPx = 10.dp.roundToPx()
                                        val layoutHeight = (placeable.height - offsetPx).coerceAtLeast(0)
                                        layout(placeable.width, layoutHeight) {
                                            placeable.placeRelative(0, -offsetPx)
                                        }
                                    }
                                    .size(BottomNavLayoutDefaults.CenterCircleSize)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        ambientColor = activeColor,
                                        spotColor = activeColor
                                    )
                                    .background(activeColor, CircleShape)
                                    .clip(CircleShape)
                                    .then(if (accessibleCenter) Modifier else Modifier.clickable { open(item.route) }),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = if (accessibleCenter) null else item.title,
                                    tint = if (student) MaterialTheme.colorScheme.onPrimary else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                style = labelStyle,
                                fontSize = BottomNavLayoutDefaults.TextFontSize,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) activeColor else inactiveColor,
                                textAlign = TextAlign.Center,
                                maxLines = if (student) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(
                                selected = isSelected,
                                onClick = { open(item.route) },
                                role = Role.Tab
                            )
                            .testTag("$tagPrefix${item.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = if (g9) Arrangement.Top else Arrangement.Center,
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(top = if (g9) 18.dp else 4.dp, bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(BottomNavLayoutDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                style = labelStyle,
                                fontSize = BottomNavLayoutDefaults.TextFontSize,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else inactiveColor,
                                textAlign = TextAlign.Center,
                                maxLines = if (student) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
