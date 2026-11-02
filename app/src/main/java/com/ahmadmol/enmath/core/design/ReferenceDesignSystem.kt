package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.design.TextKey

@Composable
fun Modifier.studentMinHeight(value: androidx.compose.ui.unit.Dp): Modifier =
    if (LocalStudentExperience.current) heightIn(min = value.coerceAtLeast(48.dp)) else height(value)

/** Reference Design System Colors matching the 6 visual reference screens */
object RefColors {
    val Background: Color
        @Composable get() = MaterialTheme.colorScheme.background
    val SurfaceWhite: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val PrimaryBlue: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val PrimaryBlueHover: Color
        @Composable get() = MaterialTheme.colorScheme.primary
    val LightBlueHeroBgStart: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val LightBlueHeroBgEnd: Color
        @Composable get() = MaterialTheme.colorScheme.surface
    val HeroBorder: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val MintGreenFill: Color
        @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val MintGreenOutline: Color
        @Composable get() = MaterialTheme.colorScheme.secondary
    val MintGreenText: Color
        @Composable get() = MaterialTheme.colorScheme.onSecondaryContainer
    val SoftBlueContainer: Color
        @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val SoftBlueBorder: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val SoftBlueText: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
    val CardBorder: Color
        @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val TextPrimary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface
    val TextSecondary: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val TextMuted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val FormulaBoxBg: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant
}

data class RefTabItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
)

/** Hero / Intro Card component matching the 6 reference screens */
@Composable
fun RefHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    badgeColor: Color = RefColors.MintGreenFill,
    badgeTextColor: Color = RefColors.MintGreenText,
    artwork: (@Composable () -> Unit)? = null,
) {
    if (!LocalStudentExperience.current) {
        Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = RefColors.LightBlueHeroBgStart),
            border = BorderStroke(1.dp, RefColors.HeroBorder)) {
            Row(Modifier.fillMaxWidth().background(Brush.horizontalGradient(listOf(RefColors.LightBlueHeroBgStart, RefColors.LightBlueHeroBgEnd))).padding(Space.lg),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                    if (!badgeText.isNullOrEmpty()) Surface(shape = RoundedCornerShape(12.dp), color = badgeColor, modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(badgeText, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = badgeTextColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                    Text(title, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp), color = RefColors.TextPrimary)
                    if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 20.sp), color = RefColors.TextSecondary)
                }
                if (artwork != null) Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) { artwork() }
            }
        }
        return
    }
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RefColors.LightBlueHeroBgStart),
        border = BorderStroke(1.dp, RefColors.HeroBorder)) {
        Box(Modifier.background(Brush.horizontalGradient(listOf(RefColors.LightBlueHeroBgStart, RefColors.LightBlueHeroBgEnd))).padding(Space.lg)) {
            val body: @Composable ColumnScope.() -> Unit = {
                if (!badgeText.isNullOrBlank()) Surface(color = badgeColor, shape = RoundedCornerShape(12.dp)) {
                    Text(badgeText, Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium, color = badgeTextColor)
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = RefColors.TextPrimary)
                if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = RefColors.TextSecondary)
            }
            if (artwork == null) Column(verticalArrangement = Arrangement.spacedBy(Space.sm), content = body)
            else PremiumHeroLayout(artwork = {
                Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) { artwork() }
            }, content = body)
        }
    }
}
/** Unified Top Bar component matching the reference screens */
@Composable
fun RefTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onNotifications: (() -> Unit)? = null,
    unread: Int = 0,
    backTag: String = "back",
    searchTag: String = "global-search",
    notificationsTag: String = "global-notifications",
) {
    Surface(color = RefColors.Background, modifier = Modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = Space.lg, vertical = Space.sm)) {
            val wrap = maxWidth < 340.dp || androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.3f
            val actions: @Composable RowScope.() -> Unit = {
                if (onSearch != null) {
                    OutlinedIconButton(
                        onClick = onSearch,
                        modifier = Modifier
                            .size(46.dp)
                            .liquidGlass(shape = CircleShape, alpha = 0.82f, elevation = 2.dp)
                            .testTag(searchTag),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                    ) {
                        Icon(Icons.Outlined.Search, tr("Search", "بحث"), tint = RefColors.PrimaryBlue)
                    }
                }
                if (onNotifications != null) {
                    OutlinedIconButton(
                        onClick = onNotifications,
                        modifier = Modifier
                            .size(46.dp)
                            .liquidGlass(shape = CircleShape, alpha = 0.82f, elevation = 2.dp)
                            .testTag(notificationsTag),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                    ) {
                        BadgedBox(badge = { if (unread > 0) Badge { Text(learningNumber(unread)) } }) {
                            Icon(Icons.Outlined.Notifications,
                                tr("Notifications: $unread unread", "الإشعارات: $unread غير مقروءة"), tint = RefColors.PrimaryBlue)
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.sm)) {
                    if (onBack != null) {
                        OutlinedIconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(46.dp)
                                .liquidGlass(shape = CircleShape, alpha = 0.82f, elevation = 2.dp)
                                .testTag(backTag),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f))
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, tr("Back", "رجوع"), tint = RefColors.PrimaryBlue)
                        }
                    } else {
                        // Balance spacing when back button is absent on main screens
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = if (onBack != null) TextAlign.Center else TextAlign.Start,
                        color = RefColors.TextPrimary
                    )
                    if (!wrap) actions()
                }
                if (wrap && (onSearch != null || onNotifications != null)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.End), content = actions)
                }
            }
        }
    }
}
/** Refined Pill Search Bar */
@Composable
fun RefSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "بحث",
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        shape = CircleShape,
        color = RefColors.SurfaceWhite,
        border = BorderStroke(1.dp, RefColors.CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Space.lg, vertical = Space.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = RefColors.TextMuted
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = RefColors.TextMuted
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search-input"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = RefColors.TextPrimary
                    ),
                    singleLine = true
                )
            }
        }
    }
}

/** Segmented Category Tabs Row matching Image 1 & 4 */
@Composable
fun RefCategoryTabs(
    items: List<RefTabItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        items.forEach { item ->
            val isSelected = selectedId == item.id
            Surface(
                onClick = { onSelect(item.id) },
                modifier = Modifier
                    .widthIn(min = 96.dp)
                    .heightIn(min = 48.dp)
                    .testTag("category-tab-${item.id}"),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) RefColors.LightBlueHeroBgStart else RefColors.SurfaceWhite,
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) RefColors.PrimaryBlue else RefColors.CardBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = Space.md, vertical = Space.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) RefColors.PrimaryBlue else RefColors.TextSecondary
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = if (isSelected) RefColors.PrimaryBlue else RefColors.TextSecondary,
                        maxLines = Int.MAX_VALUE
                    )
                }
            }
        }
    }
}

/** Horizontal Scrollable Filter Chips matching Image 1 & 6 */
@Composable
fun RefFilterChipRow(
    items: List<RefTabItem>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    useMintGreen: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Space.sm)
    ) {
        items.forEach { item ->
            val isSelected = selectedId == item.id
            val activeBg = if (useMintGreen) RefColors.MintGreenFill else RefColors.SoftBlueContainer
            val activeBorder = if (useMintGreen) RefColors.MintGreenOutline else RefColors.PrimaryBlue
            val activeText = if (useMintGreen) RefColors.MintGreenText else RefColors.SoftBlueText

            Surface(
                onClick = { onSelect(item.id) },
                shape = CircleShape,
                color = if (isSelected) activeBg else RefColors.SurfaceWhite,
                border = BorderStroke(
                    1.dp,
                    if (isSelected) activeBorder else RefColors.CardBorder
                ),
                modifier = Modifier.heightIn(min = if (LocalStudentExperience.current) 48.dp else 0.dp).testTag("chip-${item.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.icon != null) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isSelected) activeText else RefColors.TextSecondary
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) activeText else RefColors.TextSecondary
                    )
                }
            }
        }
    }
}

/** Info / Callout Banner Container matching Image 2 & 4 */
@Composable
fun RefInfoBanner(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Info,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = RefColors.SoftBlueContainer,
        border = BorderStroke(1.dp, RefColors.SoftBlueBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = RefColors.SoftBlueText
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = RefColors.SoftBlueText,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Prominent Primary Button matching Image 2 & 5 */
@Composable
fun RefPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = RefColors.PrimaryBlue,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

/** Secondary Outlined Button matching Image 3 & 6 */
@Composable
fun RefSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (enabled) RefColors.PrimaryBlue else MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = RefColors.SurfaceWhite,
            contentColor = RefColors.PrimaryBlue
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = LocalContentColor.current
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LocalContentColor.current
            )
        }
    }
}

/** Reusable Empty State Card Container matching Image 3 & 6 */
@Composable
fun RefEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    artwork: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Space.sm),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = RefColors.SurfaceWhite),
        border = BorderStroke(1.dp, RefColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.xxl, horizontal = Space.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            if (artwork != null) {
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    artwork()
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = RefColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp
                    ),
                    color = RefColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

data class RefStatItem(
    val icon: ImageVector,
    val value: String,
    val label: String,
)

/** Stats Row Card with Dividers matching Image 5 */
@Composable
fun RefStatsRow(
    stats: List<RefStatItem>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RefColors.SurfaceWhite),
        border = BorderStroke(1.dp, RefColors.CardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Space.md, horizontal = Space.sm),
            verticalArrangement = Arrangement.spacedBy(Space.md)
        ) {
            stats.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        color = RefColors.CardBorder
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = RefColors.LightBlueHeroBgStart,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = RefColors.PrimaryBlue
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.value,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = RefColors.TextPrimary
                        )
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp
                            ),
                            color = RefColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
