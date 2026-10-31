package com.ahmadmol.enmath.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.GemCounter
import com.ahmadmol.enmath.features.planner.PlanSummary
import com.ahmadmol.enmath.features.student.*

/** Replacement presentation on the same Profile route; no new identity or progress store. */
@Composable
fun Grade9ProfileScreen(
    state: LearningState,
    open: (String) -> Unit,
    switchFile: () -> Unit,
    switchRole: (() -> Unit)?,
    language: ((Language) -> Unit)?,
    gems: LocalGemsUiState,
    planner: PlannerRepository?,
) {
    var confirm by remember { mutableStateOf(false) }
    val tealAccent = EntryTeal

    Grade9Scaffold("screen-profile") {
        item("identity") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(tealAccent.copy(alpha = 0.14f), MaterialTheme.colorScheme.surface)
                            )
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = tealAccent.copy(alpha = 0.16f),
                            border = BorderStroke(2.dp, tealAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.student_g9_transparent),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = state.user?.name.orEmpty().ifBlank { tr("Grade 9 Student", "طالب الصف التاسع") },
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = tealAccent.copy(alpha = 0.18f),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = tr("Grade 9", "الصف التاسع"),
                                        color = tealAccent,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (state.user?.type == AccountType.Guest)
                                    tr("Guest file · saved on this device · no gems", "ملف زائر · محفوظ على هذا الجهاز · دون جواهر")
                                else
                                    tr("Local student file · no cloud sync", "ملف طالب محلي · دون مزامنة سحابية"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item("gems") {
            GemCounter(gems, state.language, { open("gems/wallet") }, "profile-gems", LocalGemsPolicy.owner(state.user))
        }

        item("plan") {
            PlanSummary(state, planner, open)
        }

        item("study") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tr("My study", "دراستي"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tealAccent,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ProfileRow(tr("My achievements and activity", "إنجازاتي ونشاطي"), "g9/activity", Icons.Outlined.EmojiEvents, open)
                    ProfileRow(tr("Progress and saved attempts", "التقدم والمحاولات المحفوظة"), "progress", Icons.AutoMirrored.Outlined.TrendingUp, open)
                    ProfileRow(tr("My mistakes", "دفتر أخطائي"), "review", Icons.AutoMirrored.Outlined.FactCheck, open)
                    ProfileRow(tr("Library and saved references", "المكتبة والمراجع المحفوظة"), "library", Icons.Outlined.LocalLibrary, open, "profile-library")
                    ProfileRow(tr("My PDF / Word files", "ملفاتي PDF / Word"), "g9/files", Icons.Outlined.FolderShared, open)
                    ProfileRow(tr("Assignments", "الواجبات"), "assignments", Icons.AutoMirrored.Outlined.Assignment, open)
                    if (StudentCatalog.hasUnverifiedHistory(state)) {
                        ProfileRow(tr("Preserved older records", "السجلات القديمة المحفوظة"), "review/history", Icons.Outlined.History, open)
                    }
                }
            }
        }

        item("tools") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = tr("Tools and preferences", "الأدوات والتفضيلات"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tealAccent,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ProfileRow(tr("Math solver", "الحل الرياضي"), "solver", Icons.Outlined.Calculate, open)
                    ProfileRow(tr("Graph lab", "مختبر الرسم"), "explore", Icons.AutoMirrored.Outlined.ShowChart, open)
                    ProfileRow(tr("Settings", "الإعدادات"), "settings", Icons.Outlined.Settings, open, "open-settings")
                    ProfileRow(tr("Source and availability", "المصدر والتوفر"), "g9/source", Icons.Outlined.Info, open)
                    ProfileRow(tr("Help and local data", "المساعدة والبيانات المحلية"), "g9/help", Icons.AutoMirrored.Outlined.HelpOutline, open)
                    if (language != null) {
                        TextButton(
                            onClick = { language(if (state.language == Language.Arabic) Language.English else Language.Arabic) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("language-toggle")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Outlined.Language, contentDescription = null, tint = tealAccent, modifier = Modifier.size(20.dp))
                                Text(
                                    if (state.language == Language.Arabic) "English (تبديل إلى الإنجليزية)" else "العربية (Switch to Arabic)",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    if (switchRole != null) {
                        TextButton(
                            onClick = switchRole,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Outlined.SwapHoriz, contentDescription = null, tint = tealAccent, modifier = Modifier.size(20.dp))
                                Text(tr("Switch demo role", "تبديل الدور التجريبي"), modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        item("switch") {
            OutlinedButton(
                onClick = { confirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("logout"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(tr("Switch local file", "تبديل الملف المحلي"))
                }
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(tr("Switch local file?", "تبديل الملف المحلي؟")) },
            text = {
                Text(
                    tr(
                        "Saved learning, files and plans stay with this file. Unsubmitted work may be left behind. You will choose a separate local file.",
                        "يبقى التعلّم والملفات والخطط المحفوظة في هذا الملف. قد تُترك الأعمال غير المسلّمة. ستختار ملفًا محليًا مستقلًا."
                    )
                )
            },
            confirmButton = {
                TextButton(switchFile, Modifier.testTag("dialog-confirm")) {
                    Text(tr("Choose file", "اختر ملفًا"))
                }
            },
            dismissButton = {
                TextButton({ confirm = false }) {
                    Text(tr("Cancel", "إلغاء"))
                }
            }
        )
    }
}

@Composable
private fun ProfileRow(
    title: String,
    route: String,
    icon: ImageVector,
    open: (String) -> Unit,
    tag: String = "profile-$route",
) {
    Surface(
        onClick = { open(route) },
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = EntryTeal.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = EntryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
