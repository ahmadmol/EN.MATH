package com.ahmadmol.enmath.features.teacher

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.text
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.reference.*

object TeacherReferenceRoutes {
    const val Home = "teacher/reference/home"
    const val Study = "teacher/reference/study"
    const val Solver = "teacher/reference/solver"
    const val Lab = "teacher/reference/lab"
    const val Lesson = "teacher/reference/lesson/{id}"
    val screens = listOf(Routes.TeacherHome, Home, Study, Solver, Lab, Lesson)
}

@Composable
fun TeacherReferenceHome(state: LearningState, open: (String) -> Unit) {
    ReferenceHome(state) { path ->
        open(
            when (path) {
                Routes.Study -> TeacherReferenceRoutes.Study
                Routes.Solver -> TeacherReferenceRoutes.Solver
                "explore" -> TeacherReferenceRoutes.Lab
                else -> "teacher/reference/$path"
            }
        )
    }
}

@Composable
fun TeacherReferenceStudy(open: (String) -> Unit) {
    ReferenceStudy { open("teacher/reference/$it") }
}

@Composable
fun TeacherReferenceLesson(id: String, state: LearningState, open: (String) -> Unit) {
    ReferencePage("screen-teacher-lesson") {
        val lesson = state.lesson(id)
        if (lesson == null) {
            RefHeading(refText("الدرس غير متاح", "Lesson unavailable"))
            return@ReferencePage
        }
        RefHeading(
            lesson.title.text(),
            refText("معاينة المحتوى التعليمي للمعلم", "Teacher curriculum preview"),
        )
        RefCard {
            RefLabel(refText("أهداف الدرس", "Learning objectives"), RefPalette.white, 11, true)
            RefLabel(lesson.objectives.text(), size = 10)
        }
        RefCard {
            RefLabel(refText("شرح المفهوم", "Concept overview"), RefPalette.white, 11, true)
            RefLabel(lesson.introduction.text(), size = 10)
        }
        RefButton(
            refText("فتح الحل المفصل", "Open detailed solver"),
            { open(TeacherReferenceRoutes.Solver) },
        )
        RefButton(
            refText("إنشاء واجب للطلاب", "Create a class assignment"),
            { open("teacher/assignments/new") },
            selected = false,
        )
    }
}

@Composable
fun TeacherReferenceBottomNav(route: String?, open: (String) -> Unit, dark: Boolean = true) {
    val selected =
        when {
            route?.startsWith("teacher/classes") == true -> Routes.TeacherClasses
            route?.startsWith("teacher/assignments") == true -> Routes.TeacherAssignments
            route in listOf(Routes.TeacherProfile, "teacher/settings", "teacher/notifications") ->
                Routes.TeacherProfile
            route in TeacherReferenceRoutes.screens && route != Routes.TeacherHome ->
                Routes.TeacherResources
            else -> route
        }
    CleanBottomBar(
        listOf(
            CleanNavItem(Routes.TeacherHome, refText("الرئيسية", "Home"), Icons.Outlined.Dashboard),
            CleanNavItem(Routes.TeacherClasses, refText("الشعب", "Classes"), Icons.Outlined.Groups),
            CleanNavItem(
                Routes.TeacherAssignments,
                refText("الواجبات", "Assignments"),
                Icons.AutoMirrored.Outlined.Assignment,
            ),
            CleanNavItem(
                Routes.TeacherResources,
                refText("الموارد", "Resources"),
                Icons.Outlined.FolderOpen,
            ),
            CleanNavItem(
                Routes.TeacherProfile,
                refText("حسابي", "Profile"),
                Icons.Outlined.PersonOutline,
            ),
        ),
        selected,
        "teacher-nav-",
        dark,
        open,
    )
}
