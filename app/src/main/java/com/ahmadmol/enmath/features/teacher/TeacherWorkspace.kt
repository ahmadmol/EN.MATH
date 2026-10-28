package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.text
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.reference.*

@Composable
fun TeacherWorkspace(state: LearningState, open: (String) -> Unit, switchRole: () -> Unit) {
    val classes = state.product.classes
    val students = classes.flatMap { it.studentIds }.distinct().size
    val mean =
        if (classes.isEmpty()) 0 else classes.map { classAverage(state, it) }.average().toInt()
    ReferencePage("screen-teacher-reference") {
        RefHeading(
            refText(
                "مرحباً، \u2068${state.user?.name.orEmpty()}\u2069",
                "Welcome, \u2068${state.user?.name.orEmpty()}\u2069",
            ),
            refText(
                "مساحة المعلم · إدارة التعليم في مكان واحد",
                "Teacher workspace · Your teaching at a glance",
            ),
        )
        RefCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    RefLabel(refText("الطلاب", "Students"))
                    RefLabel("$students", RefPalette.white, 20, true)
                }
                Column {
                    RefLabel(refText("الشعب", "Classes"))
                    RefLabel("${classes.size}", RefPalette.white, 20, true)
                }
                Column {
                    RefLabel(refText("الإتقان", "Mastery"))
                    RefLabel("$mean%", RefPalette.green, 20, true)
                }
            }
            HorizontalDivider(thickness = .7.dp, color = RefPalette.border)
            RefLabel(
                refText("إحصاءات من بيانات التجربة المحلية", "Statistics from local demo data")
            )
        }
        RefButton(
            refText("إنشاء واجب جديد", "Create assignment"),
            { open("teacher/assignments/new") },
            Modifier.testTag("teacher-create-assignment"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            RefButton(
                refText("إدارة الشعب", "Manage classes"),
                { open(Routes.TeacherClasses) },
                Modifier.weight(1f).testTag("teacher-manage-classes"),
                false,
            )
            RefButton(
                refText("الواجبات والنتائج", "Assignments & results"),
                { open(Routes.TeacherAssignments) },
                Modifier.weight(1f),
                false,
            )
        }
        RefHeading(refText("الشعب الدراسية", "Your classes"))
        if (classes.isEmpty())
            RefCard {
                RefLabel(
                    refText(
                        "أنشئ شعبة للبدء بإضافة الواجبات والموارد.",
                        "Create a class to start assigning work and resources.",
                    )
                )
            }
        classes.forEachIndexed { index, c ->
            RefCard(
                Modifier.clickable { open("teacher/classes/${c.id}") }
                    .testTag("teacher-class-$index")
            ) {
                RefLabel(c.name.text(), RefPalette.white, 11, true)
                RefLabel(
                    refText(
                        "${c.studentIds.size} طالباً · رمز الدعوة ${c.code}",
                        "${c.studentIds.size} students · Invite code ${c.code}",
                    )
                )
                RefLabel(
                    refText(
                        "الإتقان ${classAverage(state, c)}%",
                        "Mastery ${classAverage(state, c)}%",
                    ),
                    RefPalette.green,
                )
            }
        }
        RefHeading(refText("أدوات التدريس", "Teaching tools"))
        RefCard {
            RefButton(
                refText("المنهاج والدروس", "Curriculum & lessons"),
                { open(TeacherReferenceRoutes.Study) },
                Modifier.testTag("teacher-open-study"),
                false,
            )
            RefButton(
                refText("الحل المفصل", "Detailed solver"),
                { open(TeacherReferenceRoutes.Solver) },
                Modifier.testTag("teacher-open-solver"),
                false,
            )
            RefButton(
                refText("مختبر الدوال", "Function lab"),
                { open(TeacherReferenceRoutes.Lab) },
                Modifier.testTag("teacher-open-lab"),
                false,
            )
        }
        DemoRoleSwitch(true, switchRole)
    }
}

@Composable
fun TeacherAccount(
    state: LearningState,
    open: (String) -> Unit,
    switchRole: () -> Unit,
    logout: () -> Unit,
) {
    ReferencePage("screen-profile") {
        RefHeading(refText("حساب المعلم", "Teacher account"))
        RefCard {
            RefLabel(state.user?.name.orEmpty(), RefPalette.white, 16, true)
            RefLabel(state.user?.email.orEmpty(), size = 10)
            RefLabel(
                refText(
                    "حساب تجريبي محلي · لا توجد خدمة حسابات متصلة",
                    "Local demo account · No connected account service",
                )
            )
        }
        RefButton(
            refText("إعدادات التطبيق", "App settings"),
            { open("teacher/settings") },
            Modifier.testTag("open-settings"),
            false,
        )
        RefButton(
            refText("الإشعارات", "Notifications"),
            { open("teacher/notifications") },
            selected = false,
        )
        DemoRoleSwitch(true, switchRole)
        SessionExit(logout)
    }
}
