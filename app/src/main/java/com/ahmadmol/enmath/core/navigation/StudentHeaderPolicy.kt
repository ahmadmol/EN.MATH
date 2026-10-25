package com.ahmadmol.enmath.core.navigation

import com.ahmadmol.enmath.features.courses.interactive.LimitsLessonSequence

/** Exactly one header owns back/search actions for each existing student destination. */
fun studentOwnsHeader(route: String?, lessonId: String?): Boolean =
    route == Routes.Topic || route?.startsWith("topic/") == true ||
        (route == Routes.Lesson && lessonId != LimitsLessonSequence.lessonId) ||
        route == Routes.Progress || route == "solver/history"
