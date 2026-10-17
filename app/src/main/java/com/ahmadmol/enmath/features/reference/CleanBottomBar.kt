package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.design.tokens.LocalResourceBaseline
import com.ahmadmol.enmath.core.design.tokens.ResourceDimensions as D

data class CleanNavItem(val route: String, val title: String, val icon: ImageVector)

/** Native Material 3 touch targets and selection semantics, with no density/font scaling. */
@Composable
fun CleanBottomBar(
    items: List<CleanNavItem>,
    selected: String?,
    tagPrefix: String,
    dark: Boolean = true,
    open: (String) -> Unit,
) {
    val homeBaseline = LocalResourceBaseline.current
    val scheme =
        if (dark && !homeBaseline) {
            darkColorScheme(
                surface = RefPalette.card,
                primary = RefPalette.violet,
                onSurface = RefPalette.white,
                onSurfaceVariant = RefPalette.muted,
                secondaryContainer = RefPalette.purple.copy(alpha = .22f),
                onSecondaryContainer = RefPalette.violet,
                outlineVariant = RefPalette.border,
            )
        } else MaterialTheme.colorScheme
    MaterialTheme(colorScheme = scheme) {
        Surface(
            modifier =
                Modifier.fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            color = scheme.surface,
            shape = RoundedCornerShape(D.navRadius),
            border = BorderStroke(1.dp, scheme.outlineVariant),
            shadowElevation = D.navElevation,
        ) {
            NavigationBar(
                containerColor = scheme.surface,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.heightIn(min = D.navHeight),
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = selected == item.route,
                        onClick = { open(item.route) },
                        modifier = Modifier.testTag("$tagPrefix${item.route}"),
                        colors =
                            NavigationBarItemDefaults.colors(
                                selectedIconColor = scheme.primary,
                                selectedTextColor = scheme.onSurface,
                                indicatorColor = scheme.primary.copy(alpha = .18f),
                                unselectedIconColor = scheme.onSurfaceVariant,
                                unselectedTextColor = scheme.onSurfaceVariant,
                            ),
                        icon = { Icon(item.icon, null, Modifier.size(22.dp)) },
                        label = {
                            Text(
                                item.title,
                                fontSize = 11.sp,
                                lineHeight = if (homeBaseline) 16.sp else 15.sp,
                                maxLines = if (homeBaseline) 2 else 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight =
                                    if (selected == item.route) FontWeight.SemiBold
                                    else FontWeight.Normal,
                            )
                        },
                    )
                }
            }
        }
    }
}
