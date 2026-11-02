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
    const val Tools = "tools"
    const val Avatar = "profile/avatar"
    const val Streak = "progress/streak"
    const val Activity = "progress/activity"
    const val Book = "book/{id}"
    const val Topic = "topic/{id}"
    const val Lesson = "lesson/{id}"
    const val Solution = "solution"
    val tabs = listOf(Study, Solver, Home, "explore", Profile)
    const val Grade9Algebra = "g9/algebra"
    const val Grade9Geometry = "g9/geometry"
    const val Grade9Exercises = "g9/exercises"
    const val Grade9Source = "g9/source"
    const val Grade9Help = "g9/help"
    const val Grade9Activity = "g9/activity"
    const val Grade9Goal = "g9/goal/{id}"
    val modernStudentTabs = listOf(Study, Tools, Home, Progress, Profile)
    val grade9Tabs = modernStudentTabs
    fun studentTabs(stage: com.ahmadmol.enmath.core.model.StudyStage?) =
        if (stage != null) modernStudentTabs else tabs
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
            route == Progress || route == Streak || route == Activity || route == "review" || route?.startsWith("progress/") == true -> Progress
            route == Tools || route == Grade9Exercises || route in listOf("planner", "library", "explore", "gems/wallet", "gems/review") || route == Solver || route?.startsWith("solver/") == true -> Tools
            route == Grade9Algebra || route == Grade9Geometry || route == Grade9Source -> Study
            route == Avatar -> Profile
            route == Book ||
                route == Topic ||
                route == Lesson ||
                route?.startsWith("practice/") == true ||
                route == "study/catalog" || route == "study/demo" || route == "demo/book/{id}" || route?.startsWith("study/review") == true || route == "study/laws" -> Study
            route == Solution || route?.startsWith("solver/") == true -> Solver
            route?.startsWith("teacher/classes/") == true -> TeacherClasses
            route?.startsWith("teacher/assignments/") == true -> TeacherAssignments
            route == "teacher/settings" || route == "teacher/notifications" -> TeacherProfile
            route in listOf("settings", "notifications", "library", Progress, "gems/wallet", "gems/review", "planner", "review") || route?.startsWith("progress/") == true -> Profile
            route == "explore" -> "explore"
            route?.startsWith("assignments") == true || route == "search" -> Home
            else -> route
        }
}
