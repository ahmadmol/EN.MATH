package com.ahmadmol.enmath.features.student

import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.reference.CleanNavItem

@Composable private fun modernStudentNavItems() = listOf(
        CleanNavItem(Routes.Study, tr("Curriculum", "المنهج"), Icons.AutoMirrored.Rounded.MenuBook),
        CleanNavItem(Routes.Tools, tr("Tools", "الأدوات"), Icons.Rounded.Widgets),
        CleanNavItem(Routes.Home, tr("Home", "الرئيسية"), Icons.Rounded.Home),
        CleanNavItem(Routes.Progress, tr("Progress", "تقدمي"), Icons.Rounded.Insights),
        CleanNavItem(Routes.Profile, tr("Profile", "حسابي"), Icons.Rounded.Person),
    )

@Composable fun ModernStudentNavigation(selected: String?, open: (String) -> Unit) {
    ModernStudentBottomBar(modernStudentNavItems(), selected, "nav-", open)
}

@Composable fun ModernStudentRail(selected: String?, open: (String) -> Unit) {
    val scale = LocalDensity.current.fontScale
    NavigationRail(Modifier.width(maxOf(88.dp, (80f * scale).dp)).fillMaxHeight().verticalScroll(rememberScrollState()).testTag("student-navigation-rail"),
        containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Spacer(Modifier.height(16.dp))
        modernStudentNavItems().forEach { item ->
            NavigationRailItem(selected = selected == item.route, onClick = { open(item.route) },
                icon = { Icon(item.icon, null, Modifier.size(26.dp)) },
                label = { Text(item.title, Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 15.sp), textAlign = TextAlign.Center) },
                modifier = Modifier.heightIn(min = if (scale > 1.4f) 96.dp else 72.dp).testTag("nav-${item.route}"))
        }
    }
}
