package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.model.Language

@Composable
fun ReferenceProfile(state: LearningState, onLanguage: (Language) -> Unit, onSwitchRole: () -> Unit) {
    ReferencePage("screen-profile") {
        RefCard {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    Modifier.size(35.dp)
                        .background(
                            Brush.linearGradient(listOf(RefPalette.cyan, RefPalette.purple)),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    RefLabel("AM", Color.White, 12, true)
                }
                Column(Modifier.weight(1f)) {
                    RefLabel(refText("أحمد محمد", "Ahmed Mohammad"), RefPalette.white, 12, true)
                    RefLabel(
                        refText(
                            "الثالث الثانوي العلمي | دمشق، سوريا",
                            "Science Baccalaureate | Damascus, Syria",
                        )
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        RefIcon(Icons.Outlined.CheckCircleOutline, RefPalette.green, 10)
                        RefLabel(
                            refText("حساب طالب نشط ومفعل", "Active student account"),
                            RefPalette.green,
                            7,
                        )
                    }
                }
            }
        }
        RefCard(accent = Color(0xFF302665), background = SolidColor(Color(0xFF13132E))) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                RefIcon(Icons.Outlined.CalendarMonth, RefPalette.violet, 20)
                Column(Modifier.weight(1f)) {
                    RefLabel(
                        refText(
                            "الامتحان الوطني للشهادة الثانوية",
                            "National Baccalaureate examination",
                        ),
                        RefPalette.white,
                        10,
                        true,
                    )
                    RefLabel(
                        refText("الدورة الامتحانية لعام 2026", "2026 examination session"),
                        RefPalette.violet,
                        7,
                    )
                }
                Column {
                    RefLabel("234", RefPalette.violet, 12, true)
                    RefLabel(refText("يوماً متبقياً", "days remaining"), size = 7)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            RefLabel(
                refText(
                    "إعدادات التطبيق وتفضيلات العرض",
                    "Application settings and display preferences",
                )
            )
            RefCard {
                ProfileSetting(
                    Icons.Outlined.Language,
                    refText("لغة الواجهة", "Interface language"),
                    refText(
                        "العربية (RTL مع تثبيت المعادلات LTR)",
                        "English (LTR, with isolated math direction)",
                    ),
                    if (state.language == Language.Arabic) "English" else "العربية",
                    RefPalette.white,
                ) {
                    onLanguage(
                        if (state.language == Language.Arabic) Language.English else Language.Arabic
                    )
                }
                HorizontalDivider(
                    Modifier.padding(vertical = 5.dp),
                    thickness = .6.dp,
                    color = RefPalette.border,
                )
                ProfileSetting(
                    Icons.Outlined.DarkMode,
                    refText("المظهر الليلي (Midnight Navy)", "Midnight Navy appearance"),
                    refText(
                        "مفعّل ومريح للعين أثناء المذاكرة",
                        "Comfortable for studying at night",
                    ),
                    refText("مفعّل", "Active"),
                    RefPalette.violet,
                )
                HorizontalDivider(
                    Modifier.padding(vertical = 5.dp),
                    thickness = .6.dp,
                    color = RefPalette.border,
                )
                ProfileSetting(
                    Icons.Outlined.Download,
                    refText("الحفظ للعمل بدون إنترنت", "Offline learning"),
                    refText(
                        "تم تحميل الجزء الأول والثاني محلياً",
                        "Both demo books are available locally",
                    ),
                    refText("جاهز (MB 14)", "Ready (14 MB)"),
                    RefPalette.green,
                )
            }
        }
        DemoRoleSwitch(teacher = false, onSwitch = onSwitchRole)
        RefCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                RefLabel("EN.MATH v1.0.0 (Native Android)", RefPalette.violet, 9, true)
                RefLabel(
                    "Jetpack Compose · Material Design 3 · Syrian Scientific Baccalaureate",
                    size = 7,
                )
            }
        }
    }
}

@Composable
private fun ProfileSetting(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    status: String,
    statusColor: Color,
    onClick: (() -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RefIcon(
            icon,
            if (icon == Icons.Outlined.Download) RefPalette.green else RefPalette.violet,
            12,
        )
        Column(Modifier.weight(1f)) {
            RefLabel(title, RefPalette.white, 8, true)
            RefLabel(subtitle, size = 7)
        }
        RefLabel(status, statusColor, 8, true)
    }
}
