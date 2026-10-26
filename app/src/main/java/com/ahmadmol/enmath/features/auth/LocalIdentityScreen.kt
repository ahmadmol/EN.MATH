package com.ahmadmol.enmath.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.StudyStage

/** Local files only: choosing a stage creates a separate, immutable learner identity. */
@Composable
fun LocalIdentityScreen(state: LearningState, create: (String, StudyStage, Boolean) -> Unit,
    select: (String) -> Unit, importLegacy: (String, StudyStage, String) -> Unit,
    debugEntry: (() -> Unit)? = null) {
    var stageName by rememberSaveable { mutableStateOf<String?>(null) }
    var name by rememberSaveable { mutableStateOf("") }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    var error by remember { mutableStateOf(false) }
    val stage = stageName?.let(StudyStage::valueOf)
    fun attempt(action: () -> Unit) { error = runCatching(action).isFailure }
    ScreenPage(Modifier.testTag("screen-local-identity"), narrow = true) {
        PageHeading(tr("Local learning files", "ملفات التعلم المحلية"), tr("Choose your stage", "اختر المرحلة الدراسية"))
        Text(tr("Saved on this device only. No cloud sign-in or recovery. Each file has a fixed stage and separate progress and wallet.",
            "الحفظ على هذا الجهاز فقط، دون تسجيل دخول أو استعادة سحابية. لكل ملف مرحلة ثابتة وتقدم ومحفظة مستقلان."))
        if (state.storageIssue != null) Text(tr("An older file needs recovery and was preserved. You can choose another independent local file.",
            "أحد الملفات يحتاج استعادة وقد حُفظ دون استبدال. يمكنك اختيار ملف محلي مستقل آخر."), color = MaterialTheme.colorScheme.error)
        state.localProfiles.forEach { profile ->
            OutlinedButton(onClick = { attempt { select(profile.id) } }, modifier = Modifier.fillMaxWidth()) {
                Text("${profile.name} · ${profile.stage.name} · ${if (profile.guest) tr("Guest", "زائر") else tr("Local student", "طالب محلي")}")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StudyStage.entries.forEach { value ->
                FilterChip(selected = stage == value, onClick = { stageName = value.name },
                    label = { Text(if (value == StudyStage.G9) tr("Grade 9", "التاسع") else tr("Baccalaureate", "البكالوريا")) },
                    modifier = Modifier.testTag("local-stage-${value.name}"))
            }
        }
        OutlinedTextField(name, { name = it }, label = { Text(tr("Local file name (optional)", "اسم الملف المحلي (اختياري)")) }, modifier = Modifier.fillMaxWidth())
        Text(tr("You cannot change this file's stage later. Create another file to study the other stage.",
            "لا يمكن تغيير مرحلة هذا الملف لاحقًا. أنشئ ملفًا مستقلًا لدراسة المرحلة الأخرى."))
        Button(onClick = { stage?.let { attempt { create(name, it, false) } } }, enabled = stage != null,
            modifier = Modifier.fillMaxWidth().testTag("local-create-student")) { Text(tr("Create local student file", "إنشاء ملف طالب محلي")) }
        OutlinedButton(onClick = { stage?.let { attempt { create(name, it, true) } } }, enabled = stage != null,
            modifier = Modifier.fillMaxWidth().testTag("local-open-guest")) { Text(tr("Continue as stage guest — no gems", "متابعة كزائر لهذه المرحلة — دون جواهر")) }
        if (state.legacyCandidates.isNotEmpty()) {
            AppCard {
                Text(tr("Older data is preserved", "البيانات القديمة محفوظة"), style = MaterialTheme.typography.titleMedium)
                Text(tr("Unknown ownership stays pending. A confirmed local scope still needs an explicit stage choice; nothing is assigned automatically.",
                    "الملكية المجهولة تبقى معلّقة. الملف المحلي المؤكد يحتاج اختيار مرحلته صراحة؛ لا تُنسب البيانات تلقائيًا."))
                Row {
                    Checkbox(confirmed, { confirmed = it })
                    Text(tr("I confirm ownership of the selected older local file", "أؤكد ملكيتي للملف المحلي القديم الذي أختاره"))
                }
                state.legacyCandidates.forEachIndexed { index, candidate ->
                    if (candidate.canConfirm) OutlinedButton(onClick = {
                        stage?.let { attempt { importLegacy(candidate.source, it, name) } }
                    }, enabled = stage != null && confirmed) { Text(tr("Import confirmed file ${index + 1}", "إضافة الملف المؤكد ${index + 1}")) }
                    else Text(tr("Older file ${index + 1}: ownership/stage unresolved; preserved for review.",
                        "الملف القديم ${index + 1}: الملكية أو المرحلة غير محسومة؛ محفوظ للمراجعة."))
                }
            }
        }
        if (error) Text(tr("The operation was not saved. Data is preserved; restart and retry or request a storage review.",
            "لم تُحفظ العملية. البيانات محفوظة؛ أعد التشغيل والمحاولة أو اطلب مراجعة التخزين."), color = MaterialTheme.colorScheme.error)
        debugEntry?.let { TextButton(it) { Text(tr("Development demo entry", "الدخول التجريبي للتطوير")) } }
    }
}
