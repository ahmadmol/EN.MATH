package com.ahmadmol.enmath.features.reference

import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable

/** Local demo role selection; this does not grant server permissions or create an account. */
@Composable
fun DemoRoleSwitch(teacher: Boolean, onSwitch: () -> Unit) {
    RefCard {
        RefLabel(refText("تبديل الدور — تجريبي", "Switch role — demo"), RefPalette.white, 10, true)
        RefLabel(
            if (teacher) refText("الدور الحالي: المعلم. بياناتك محفوظة عند التبديل.", "Current role: Teacher. Your data is kept when switching.")
            else refText("الدور الحالي: الطالب. بياناتك محفوظة عند التبديل.", "Current role: Student. Your data is kept when switching.")
        )
        RefButton(
            if (teacher) refText("الانتقال إلى الطالب", "Switch to Student")
            else refText("الانتقال إلى المعلم", "Switch to Teacher"),
            onSwitch,
            Modifier.heightIn(min = 36.dp).testTag("demo-switch-role"),
        )
    }
}
