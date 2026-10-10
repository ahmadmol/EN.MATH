package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.navigation.Routes

@Composable
fun ReferenceHome(state: LearningState, open: (String) -> Unit) {
    ReferencePage("screen-home", top = 19.dp) {
        RefCard(
            accent = Color(0xFF302665),
            background = Brush.verticalGradient(listOf(Color(0xFF17173B), Color(0xFF141E33))),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    RefLabel(
                        refText(
                            "الثالث الثانوي العلمي - بكالوريا سوريا",
                            "Science Baccalaureate · Syria",
                        ),
                        RefPalette.violet,
                    )
                    RefLabel(refText("أحمد محمد", "Ahmed Mohammad"), RefPalette.white, 12, true)
                }
                Box(
                    Modifier.size(29.dp)
                        .background(Color(0xFF25205D), CircleShape)
                        .border(.7.dp, Color(0xFF4934A8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RefLabel("AM", RefPalette.violet, 10, true)
                }
            }
            HorizontalDivider(
                Modifier.padding(vertical = 8.dp),
                thickness = .6.dp,
                color = RefPalette.border,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RefCard(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RefIcon(Icons.Outlined.LocalFireDepartment, Color(0xFFFFAA00))
                        Column {
                            RefLabel(refText("أيام متتالية", "Daily streak"), size = 7)
                            RefLabel(refText("14 يوماً", "14 days"), RefPalette.white, 11, true)
                        }
                    }
                }
                RefCard(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RefIcon(Icons.Outlined.WorkspacePremium, RefPalette.green)
                        Column {
                            RefLabel(refText("نقاط الخبرة", "Experience"), size = 7)
                            RefLabel("XP 1,850", RefPalette.white, 11, true)
                        }
                    }
                }
            }
        }
        RefCard(accent = Color(0xFF3C337F)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RefLabel(
                    refText("متابعة التعلم من حيث توقفت...", "Continue where you left off…"),
                    RefPalette.violet,
                )
                RefLabel("70%", RefPalette.violet, 9, true)
            }
            RefLabel(
                refText(
                    "إزالة حالات عدم التعيين 0/0 و ∞/∞",
                    "Removing indeterminate forms 0/0 and ∞/∞",
                ),
                RefPalette.white,
                10,
                true,
            )
            RefLabel(
                refText(
                    "التحليل الرياضي، النهايات، الاشتقاق والتكامل، وقاعدة لوبيتال.",
                    "Limits, differentiation, integration and L'Hôpital's rule.",
                )
            )
            RefButton(
                refText("متابعة الدرس وحل التمارين ←", "Continue lesson and exercises →"),
                { open("lesson/limits-practice") },
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            RefLabel(refText("أدوات ومختبرات الرياضيات", "Mathematics tools and labs"))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ToolCard(
                    refText("الحل المفصّل", "Detailed solution"),
                    refText("حل الرياضيات العلمية", "Step-by-step mathematics"),
                    Icons.Outlined.Calculate,
                    RefPalette.violet,
                    Modifier.weight(1f),
                ) {
                    open(Routes.Solver)
                }
                ToolCard(
                    refText("مختبر الدوال", "Function lab"),
                    refText("رسم تفاعلي تحليلي", "Interactive analytical graphs"),
                    Icons.AutoMirrored.Outlined.ShowChart,
                    RefPalette.cyan,
                    Modifier.weight(1f),
                ) {
                    open("explore")
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                RefLabel(refText("الكتب المقررة للبكالوريا", "Baccalaureate textbooks"))
                RefLabel(
                    refText("عرض المنهج", "View curriculum"),
                    RefPalette.violet,
                    8,
                    modifier = Modifier.clickable { open(Routes.Study) },
                )
            }
            BookPreview(0, 67, 16, 24) { open(Routes.Study) }
            BookPreview(1, 45, 10, 22) { open(Routes.Study) }
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    RefCard(modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier.size(27.dp)
                    .background(
                        color.copy(alpha = .15f),
                        androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                RefIcon(icon, color, 14)
            }
            Column {
                RefLabel(title, RefPalette.white, 9, true)
                RefLabel(subtitle, size = 7)
            }
        }
    }
}

@Composable
fun BookPreview(index: Int, percent: Int, done: Int, total: Int, onClick: () -> Unit) {
    RefCard(Modifier.clickable(onClick = onClick)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    Modifier.size(24.dp)
                        .background(
                            RefPalette.violet.copy(alpha = .13f),
                            androidx.compose.foundation.shape.RoundedCornerShape(5.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    RefIcon(Icons.AutoMirrored.Outlined.MenuBook, size = 12)
                }
                Column {
                    RefLabel(
                        if (index == 0)
                            refText(
                                "الجزء الأول: التحليل والتوابع",
                                "Part I: Analysis and functions",
                            )
                        else
                            refText(
                                "الجزء الثاني: الجبر والهندسة الفضائية والاحتمالات",
                                "Part II: Algebra, geometry and probability",
                            ),
                        RefPalette.white,
                        8,
                        true,
                    )
                    RefLabel(
                        refText(
                            "$done من أصل $total درساً مكتمل",
                            "$done of $total lessons completed",
                        ),
                        size = 7,
                    )
                }
            }
            RefLabel("$percent%", RefPalette.violet, 9, true)
        }
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(5.dp),
            color = RefPalette.violet,
            trackColor = RefPalette.border,
            gapSize = 0.dp,
            drawStopIndicator = {},
        )
    }
}
