package com.ahmadmol.enmath.core.navigation

/** Incoming route can change while Splash or a subpage is still fading out. */
internal fun showStudentNavigation(
    route: String?,
    visibleRoutes: List<String?>,
    hasStudentSession: Boolean,
    stage: com.ahmadmol.enmath.core.model.StudyStage? = null,
): Boolean = hasStudentSession && route in Routes.studentTabs(stage) && visibleRoutes.isNotEmpty() &&
    visibleRoutes.all { it in Routes.studentTabs(stage) }

/** G9's long Home path must retain its scroll when a student switches tabs. */
internal fun restoreStudentTabState(
    stage: com.ahmadmol.enmath.core.model.StudyStage?, destination: String, teacher: Boolean,
): Boolean = (stage == com.ahmadmol.enmath.core.model.StudyStage.G9 && !teacher) ||
    (destination != Routes.Home && destination != Routes.TeacherHome)

/** Never restore a learner's NavController/ViewModels into another owner or process session. */
internal fun navigationSessionKey(state: com.ahmadmol.enmath.core.data.LearningState): String =
    com.ahmadmol.enmath.core.data.LearningRules.sessionOwner(state)
        ?: "${state.user?.type}:${state.user?.email.orEmpty()}"
