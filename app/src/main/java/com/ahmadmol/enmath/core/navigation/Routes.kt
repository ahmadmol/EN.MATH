package com.ahmadmol.enmath.core.navigation

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Started = "started"
    const val Login = "login"
    const val AccountChoice = "account-choice"
    const val RegisterStudent = "register-student"
    const val RegisterTeacher = "register-teacher"
    const val Verify = "verify"
    const val Forgot = "forgot"
    const val NewPassword = "new-password"
    const val Main = "main"
    const val Home = "home"
    const val Study = "study"
    const val Solver = "solver"
    const val Progress = "progress"
    const val Profile = "profile"
    const val Book = "book/{id}"
    const val Topic = "topic/{id}"
    const val Lesson = "lesson/{id}"
    const val Solution = "solution"
    val tabs = listOf(Home, Study, Solver, Progress, Profile)
    const val TeacherMain = "teacher-main"
    const val TeacherHome = "teacher"
    const val TeacherClasses = "teacher/classes"
    const val TeacherAssignments = "teacher/assignments"
    const val TeacherResources = "teacher/resources"
    const val TeacherProfile = "teacher/profile"
    val teacherTabs =
        listOf(TeacherHome, TeacherClasses, TeacherAssignments, TeacherResources, TeacherProfile)
    val focus =
        listOf(
            Lesson,
            "practice/{id}",
            "assignments/{id}/work",
            "solver/scan",
            Solution,
            "solver/solution/{id}",
        )

    fun selectedTab(route: String?) =
        when {
            route == Book ||
                route == Topic ||
                route == Lesson ||
                route?.startsWith("practice/") == true -> Study
            route == Solution || route?.startsWith("solver/") == true -> Solver
            route?.startsWith("teacher/classes/") == true -> TeacherClasses
            route?.startsWith("teacher/assignments/") == true -> TeacherAssignments
            route == "teacher/settings" || route == "teacher/notifications" -> TeacherProfile
            route in listOf("settings", "notifications", "library") -> Profile
            route?.startsWith("assignments") == true || route == "search" || route == "explore" ->
                Home
            else -> route
        }
}
