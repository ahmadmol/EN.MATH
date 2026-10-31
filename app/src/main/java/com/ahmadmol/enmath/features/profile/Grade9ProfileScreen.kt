package com.ahmadmol.enmath.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.GemCounter
import com.ahmadmol.enmath.features.planner.PlanSummary
import com.ahmadmol.enmath.features.student.*

/** Replacement presentation on the same Profile route; no new identity or progress store. */
@Composable fun Grade9ProfileScreen(state: LearningState, open: (String) -> Unit, switchFile: () -> Unit,
    switchRole: (() -> Unit)?, language: ((Language) -> Unit)?, gems: LocalGemsUiState, planner: PlannerRepository?) {
    var confirm by remember { mutableStateOf(false) }
    Grade9Scaffold("screen-profile") {
        item("identity") { G9LearningCard(state.user?.name.orEmpty()) {
            Text(tr("Grade 9", "الصف التاسع"), style = MaterialTheme.typography.titleSmall)
            Text(if (state.user?.type == AccountType.Guest) tr("Guest file · saved on this device · no gems", "ملف زائر · محفوظ على هذا الجهاز · دون جواهر") else
                tr("Local student file · no cloud sync", "ملف طالب محلي · دون مزامنة سحابية"))
        } }
        item("gems") { GemCounter(gems, state.language, { open("gems/wallet") }, "profile-gems", LocalGemsPolicy.owner(state.user)) }
        item("plan") { PlanSummary(state, planner, open) }
        item("study") { G9LearningCard(tr("My study", "دراستي")) {
            ProfileLink(tr("Progress and saved attempts", "التقدم والمحاولات المحفوظة"), "progress", open)
            ProfileLink(tr("My achievements and activity", "إنجازاتي ونشاطي"), "g9/activity", open)
            ProfileLink(tr("My mistakes", "دفتر أخطائي"), "review", open)
            ProfileLink(tr("Library and saved references", "المكتبة والمراجع المحفوظة"), "library", open, "profile-library")
            ProfileLink(tr("My PDF / Word files", "ملفاتي PDF / Word"), "g9/files", open)
            ProfileLink(tr("Assignments", "الواجبات"), "assignments", open)
            if (StudentCatalog.hasUnverifiedHistory(state)) ProfileLink(tr("Preserved older records", "السجلات القديمة المحفوظة"), "review/history", open)
        } }
        item("tools") { G9LearningCard(tr("Tools and preferences", "الأدوات والتفضيلات")) {
            ProfileLink(tr("Math solver", "الحل الرياضي"), "solver", open)
            ProfileLink(tr("Graph lab", "مختبر الرسم"), "explore", open)
            ProfileLink(tr("Settings", "الإعدادات"), "settings", open, "open-settings")
            ProfileLink(tr("Source and availability", "المصدر والتوفر"), "g9/source", open)
            ProfileLink(tr("Help and local data", "المساعدة والبيانات المحلية"), "g9/help", open)
            if (language != null) TextButton({ language(if (state.language == Language.Arabic) Language.English else Language.Arabic) },
                Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("language-toggle")) { Text(if (state.language == Language.Arabic) "English" else "العربية") }
            if (switchRole != null) TextButton(switchRole, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Switch demo role", "تبديل الدور التجريبي")) }
        } }
        item("switch") { OutlinedButton({ confirm = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("logout")) {
            Text(tr("Switch local file", "تبديل الملف المحلي"))
        } }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(tr("Switch local file?", "تبديل الملف المحلي؟")) },
        text = { Text(tr("Saved learning, files and plans stay with this file. Unsubmitted work may be left behind. You will choose a separate local file.",
            "يبقى التعلّم والملفات والخطط المحفوظة في هذا الملف. قد تُترك الأعمال غير المسلّمة. ستختار ملفًا محليًا مستقلًا.")) },
        confirmButton = { TextButton(switchFile, Modifier.testTag("dialog-confirm")) { Text(tr("Choose file", "اختر ملفًا")) } },
        dismissButton = { TextButton({ confirm = false }) { Text(tr("Cancel", "إلغاء")) } })
}

@Composable private fun ProfileLink(title: String, route: String, open: (String) -> Unit, tag: String = "profile-$route") {
    TextButton({ open(route) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
        Box(Modifier.fillMaxWidth()) { Text(title) }
    }
}
