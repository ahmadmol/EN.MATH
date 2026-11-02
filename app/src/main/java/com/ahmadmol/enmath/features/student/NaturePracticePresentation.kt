package com.ahmadmol.enmath.features.student

import com.ahmadmol.enmath.core.design.isolateMathInArabic

/** Presentation only: preserve the bank text while keeping roots/equations and signed numbers LTR. */
internal fun naturePracticeProse(value: String): String = isolateMathInArabic(value)
    .replace("\u2066:", ":\u2066")
    .replace("\u2069…", "…\u2069")
    .replace(Regex("[-−][0-9]+")) { "\u2066${it.value}\u2069" }
