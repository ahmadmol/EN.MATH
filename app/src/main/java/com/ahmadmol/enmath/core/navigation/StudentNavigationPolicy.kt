package com.ahmadmol.enmath.core.navigation

/** Incoming route can change while Splash or a subpage is still fading out. */
internal fun showStudentNavigation(
    route: String?,
    visibleRoutes: List<String?>,
    hasStudentSession: Boolean,
): Boolean = hasStudentSession && route in Routes.tabs && visibleRoutes.isNotEmpty() &&
    visibleRoutes.all { it in Routes.tabs }
