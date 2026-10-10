package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.reference.*

/** Teacher-only destinations; student routing and screens are unchanged. */
object TeacherReferenceRoutes {
    const val Home = "teacher/reference/home"
    const val Study = "teacher/reference/study"
    val screens = listOf(Routes.TeacherHome, Home, Study)
}

@Composable
fun TeacherReferenceHome(state: LearningState, open: (String) -> Unit) {
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    ReferenceHome(state) { path ->
        when (path) {
            Routes.Study -> open(TeacherReferenceRoutes.Study)
            else -> preview = if (path.startsWith("lesson/")) "متابعة الدرس" else "أدوات الرياضيات"
        }
    }
    preview?.let { title ->
        TeacherPreviewDialog(
            title,
            "تجريبي: تفاصيل هذه الواجهة غير مشمولة في الصور المرفقة. لم يتم فتح شاشة إضافية.",
        ) {
            preview = null
        }
    }
}

@Composable
fun TeacherReferenceStudy() {
    var preview by rememberSaveable { mutableStateOf(false) }
    ReferenceStudy { preview = true }
    if (preview)
        TeacherPreviewDialog(
            "معاينة الدرس",
            "تجريبي: تم اختيار الدرس. محتوى الدرس التفصيلي خارج نطاق التصاميم المرفقة.",
        ) {
            preview = false
        }
}

@Composable
fun TeacherReferenceBottomNav(route: String?, open: (String) -> Unit) {
    var preview by rememberSaveable { mutableStateOf<String?>(null) }
    BoxWithConstraints(Modifier.fillMaxWidth().background(Color(0xFF030717))) {
        val density = LocalDensity.current
        val scale = (maxWidth.value.coerceAtMost(416f) / 277f).coerceAtLeast(.85f)
        CompositionLocalProvider(
            LocalDensity provides Density(density.density * scale, density.fontScale)
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .border(.6.dp, RefPalette.border)
                    .navigationBarsPadding()
                    .padding(top = 7.dp, bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                val tabs =
                    listOf(
                        Triple(TeacherReferenceRoutes.Home, "الرئيسية", Icons.Outlined.Home),
                        Triple(
                            TeacherReferenceRoutes.Study,
                            "المنهج",
                            Icons.AutoMirrored.Outlined.MenuBook,
                        ),
                        Triple("teacher/reference/solver", "الحل المفصل", Icons.Outlined.Calculate),
                        Triple(
                            "teacher/reference/lab",
                            "المختبر",
                            Icons.AutoMirrored.Outlined.ShowChart,
                        ),
                        Triple(Routes.TeacherHome, "المعلم", Icons.Outlined.School),
                    )
                tabs.forEach { (path, title, icon) ->
                    val selected = path == route
                    val color = if (selected) RefPalette.violet else RefPalette.muted
                    Column(
                        Modifier.weight(1f)
                            .clickable {
                                if (path in TeacherReferenceRoutes.screens) open(path)
                                else preview = title
                            }
                            .testTag("teacher-nav-$path")
                            .padding(horizontal = 1.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        RefIcon(icon, color, 12)
                        Text(
                            title,
                            color = color,
                            fontSize = 7.sp,
                            lineHeight = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
    preview?.let { title ->
        TeacherPreviewDialog(title, "تجريبي: هذا التبويب خارج نطاق الصور المرفقة لدور المعلم.") {
            preview = null
        }
    }
}
