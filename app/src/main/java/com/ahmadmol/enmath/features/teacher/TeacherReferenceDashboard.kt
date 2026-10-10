package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.features.reference.*

/** UI-only fixture data from the teacher screenshot; never writes to the repository. */
private data class ClassPreview(
    val code: String,
    val name: String,
    val students: Int,
    val completion: Int,
    val unit: String,
)

private val previewClasses =
    listOf(
        ClassPreview(
            "BAC-SCI-2026A",
            "الثالث الثانوي العلمي - شعبة أ (متفوقين)",
            28,
            84,
            "التكامل بالتجزئة وحساب المساحات",
        ),
        ClassPreview(
            "BAC-SCI-2026B",
            "الثالث الثانوي العلمي - شعبة ب",
            26,
            76,
            "دراسة التابع اللوغاريتمي",
        ),
    )

@Composable
fun TeacherReferenceDashboard(onSwitchRole: () -> Unit) {
    var assignments by rememberSaveable { mutableStateOf(false) }
    var create by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(-1) }
    var support by rememberSaveable { mutableStateOf(false) }
    var title by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableIntStateOf(0) }
    var saved by rememberSaveable { mutableStateOf("") }
    ReferencePage("screen-teacher-reference", top = 12.dp) {
        RefCard(
            accent = Color(0xFF302665),
            background = Brush.verticalGradient(listOf(Color(0xFF17173B), Color(0xFF141E33))),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        RefIcon(Icons.Outlined.School, size = 11)
                        RefLabel("بوابة المعلم - المنهاج السوري", RefPalette.violet, 9)
                    }
                    RefLabel("الأستاذة سارة الأحمد", RefPalette.white, 12, true)
                    RefLabel("مدرسة الرياضيات للبكالوريا العلمي | إدارة المتفوقين", size = 8)
                }
                Box(
                    Modifier.size(29.dp)
                        .background(Color(0xFF25205D), CircleShape)
                        .border(.7.dp, Color(0xFF4934A8), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    RefLabel("SA", RefPalette.violet, 10, true)
                }
            }
            HorizontalDivider(
                Modifier.padding(vertical = 10.dp),
                thickness = .6.dp,
                color = RefPalette.border,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RefCard(Modifier.weight(1f), padding = 7.dp) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        RefLabel("إجمالي الطلاب", size = 8)
                        RefLabel("54 طالباً", RefPalette.white, 11, true)
                    }
                }
                RefCard(Modifier.weight(1f), padding = 7.dp) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        RefLabel("متوسط إتقان العلم", size = 8)
                        RefLabel("80.5%", RefPalette.green, 11, true)
                    }
                }
            }
        }
        RefCard(padding = 3.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                TeacherTab(
                    "الشعب الدراسية",
                    !assignments,
                    Modifier.weight(1f).testTag("teacher-tab-classes"),
                ) {
                    assignments = false
                }
                TeacherTab(
                    "الواجبات والاختبارات",
                    assignments,
                    Modifier.weight(1f).testTag("teacher-tab-assignments"),
                ) {
                    assignments = true
                }
            }
        }
        if (!assignments) {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RefLabel("قائمة الشعب المسجلة", size = 9)
                    Row(
                        Modifier.clickable {
                                title = ""
                                saved = ""
                                create = true
                            }
                            .testTag("teacher-create-assignment"),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RefIcon(Icons.Outlined.Add, size = 12)
                        RefLabel("إنشاء واجب", RefPalette.violet, 8)
                    }
                }
                previewClasses.forEachIndexed { index, c ->
                    RefCard(
                        Modifier.clickable { selected = index }.testTag("teacher-class-$index"),
                        padding = 11.dp,
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            RefLabel(c.name, RefPalette.white, 9, true, Modifier.weight(1f))
                            Box(
                                Modifier.background(Color(0xFF25205D), RoundedCornerShape(3.dp))
                                    .padding(horizontal = 5.dp, vertical = 3.dp)
                            ) {
                                RefLabel(c.code, RefPalette.violet, 7, true)
                            }
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                RefIcon(Icons.Outlined.Groups, RefPalette.muted, 11)
                                RefLabel("${c.students} طالباً", size = 8)
                            }
                            RefLabel("الإتقان: ${c.completion}%", RefPalette.green, 8, true)
                        }
                        Row(
                            Modifier.fillMaxWidth()
                                .background(RefPalette.equation, RoundedCornerShape(5.dp))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            RefLabel("الوحدة الجارية حالياً:", size = 7)
                            RefLabel(c.unit, RefPalette.white, 8, true, Modifier.weight(1f))
                        }
                    }
                }
            }
            RefCard(
                Modifier.clickable { support = true }.testTag("teacher-support"),
                accent = Color(0xFF54310E),
                background = androidx.compose.ui.graphics.SolidColor(Color(0xFF151317)),
                padding = 10.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RefIcon(Icons.Outlined.WarningAmber, Color(0xFFFFAA00), 11)
                    RefLabel("تنبيه تحليلي اصطناعي", Color(0xFFFFAA00), 9, true)
                }
                RefLabel(
                    "6 طلاب في شعبة ب يحتاجون إلى دعم إضافي في إزالة حالات عدم التعيين باستخدام المطابقات المكعبية.",
                    RefPalette.white,
                    8,
                )
            }
        } else {
            // Only a local demo state: no invented assignment detail screen or business mutation.
            RefCard(Modifier.testTag("teacher-assignments-preview")) {
                RefLabel("الواجبات والاختبارات — تجريبي", RefPalette.white, 10, true)
                RefLabel(
                    if (saved.isEmpty())
                        "لا توجد واجبات في هذه المعاينة المحلية. إنشاء واجب يعرض نموذجاً تجريبياً فقط."
                    else saved
                )
                RefButton(
                    "إنشاء واجب",
                    {
                        title = ""
                        create = true
                    },
                    Modifier.testTag("teacher-preview-create"),
                )
            }
        }
        DemoRoleSwitch(teacher = true, onSwitch = onSwitchRole)
    }
    if (selected >= 0) {
        val c = previewClasses[selected]
        TeacherPreviewDialog(
            c.name,
            "معاينة تجريبية: ${c.students} طالباً · الإتقان ${c.completion}%\n${c.unit}",
        ) {
            selected = -1
        }
    }
    if (support)
        TeacherPreviewDialog(
            "تنبيه تحليلي اصطناعي",
            "تجريبي: 6 طلاب من شعبة ب يحتاجون إلى دعم إضافي. هذه بيانات عرض محلية، ولم يُرسل أي إشعار.",
        ) {
            support = false
        }
    if (create)
        AlertDialog(
            onDismissRequest = { create = false },
            title = { Text("إنشاء واجب تجريبي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("معاينة محلية فقط؛ لا يتم حفظ أو نشر واجب.")
                    OutlinedTextField(
                        title,
                        { title = it },
                        label = { Text("عنوان الواجب") },
                        modifier = Modifier.fillMaxWidth().testTag("teacher-assignment-title"),
                        singleLine = true,
                    )
                    previewClasses.forEachIndexed { i, c ->
                        Row(
                            Modifier.fillMaxWidth()
                                .clickable { target = i }
                                .testTag("teacher-assignment-class-$i"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(target == i, { target = i })
                            Text(c.code)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        saved = "معاينة تجريبية: $title · ${previewClasses[target].code} (لم تُنشر)"
                        assignments = true
                        create = false
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.testTag("teacher-assignment-preview"),
                ) {
                    Text("معاينة")
                }
            },
            dismissButton = { TextButton(onClick = { create = false }) { Text("إلغاء") } },
        )
}

@Composable
private fun TeacherTab(title: String, selected: Boolean, modifier: Modifier, click: () -> Unit) {
    Box(
        modifier
            .heightIn(min = 24.dp)
            .background(
                if (selected) RefPalette.purple else Color.Transparent,
                RoundedCornerShape(6.dp),
            )
            .clickable(onClick = click)
            .padding(horizontal = 6.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        RefLabel(title, if (selected) Color.White else RefPalette.muted, 8, selected)
    }
}

@Composable
fun TeacherPreviewDialog(title: String, body: String, dismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = dismiss, modifier = Modifier.testTag("teacher-preview-dismiss")) {
                Text("حسناً")
            }
        },
    )
}
