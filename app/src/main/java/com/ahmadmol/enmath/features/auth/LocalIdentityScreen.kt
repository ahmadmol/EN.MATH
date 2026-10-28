package com.ahmadmol.enmath.features.auth

import androidx.activity.compose.BackHandler
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
import com.ahmadmol.enmath.features.student.studentStageLabel

/** Stage intention never mutates a learner. Only explicit submit creates or selects a file. */
@Composable
fun LocalIdentityScreen(state: LearningState, create: (String, StudyStage, Boolean) -> Unit,
    select: (String) -> Unit, importLegacy: (String, StudyStage, String) -> Unit,
    debugEntry: (() -> Unit)? = null, acknowledgeGuest: (StudyStage) -> Unit = {}) {
    var stageName by rememberSaveable { mutableStateOf<String?>(null) }
    var page by rememberSaveable { mutableStateOf("stage") }
    var name by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val stage = stageName?.let(StudyStage::valueOf)
    fun attempt(action: () -> Unit) {
        if (busy) return
        busy = true
        error = runCatching(action).isFailure
        busy = false
    }
    BackHandler(page != "stage") { page = if (page in setOf("notice", "create", "profiles") && stage != null) "entry" else "stage"; error = false }
    EntryBackdrop {
        Box(Modifier.fillMaxSize()) {
            ScreenPage(Modifier.testTag("screen-local-identity"), narrow = true) {
                if (state.storageIssue != null) Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_16), color = MaterialTheme.colorScheme.error)
                when(page) {
                    "stage" -> {
                        StagePicker { value -> stageName = value.name; page = "entry"; name = "" }
                        if (state.localProfiles.isNotEmpty()) OutlinedButton({ page = "profiles" }, Modifier.fillMaxWidth().heightIn(min=48.dp)) { Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_17)) }
                        debugEntry?.let { TextButton(it) { Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_18)) } }
                    }
                    "entry" -> {
                        StageEntryContent(requireNotNull(stage), busy,
                            back = { page="stage" },
                            guest = { if(stage in state.acknowledgedGuestStages) attempt { create("", stage, true) } else page="notice" },
                            create = { page="create" }, saved = { page="profiles" })
                    }
                    "notice" -> {
                        EntryBrand(); EntryTitle(studentStageLabel(stage))
                        Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_30), style=MaterialTheme.typography.titleLarge)
                        Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_31))
                        PrimaryButton(entryText(com.ahmadmol.enmath.R.string.entry_copy_32), { stage?.let { attempt { acknowledgeGuest(it); create("", it, true) } } }, Modifier.testTag("guest-notice-confirm"), !busy)
                    }
                    "create" -> {
                        EntryBrand(); EntryTitle(entryText(com.ahmadmol.enmath.R.string.entry_copy_25))
                        EducationalInfoBanner(entryText(com.ahmadmol.enmath.R.string.entry_copy_33))
                        OutlinedTextField(name, { name = it.take(100) }, label = { Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_34)) }, modifier = Modifier.fillMaxWidth())
                        Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_35))
                        PrimaryButton(entryText(com.ahmadmol.enmath.R.string.entry_copy_36), { stage?.let { attempt { create(name, it, false) } } }, Modifier.testTag("local-create-student"), !busy)
                    }
                    "profiles" -> {
                        EntryBrand(); EntryTitle(entryText(com.ahmadmol.enmath.R.string.entry_copy_27))
                        val profiles = state.localProfiles.filter { stage == null || it.stage == stage }
                        if(profiles.isEmpty()) Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_37))
                        profiles.forEach { profile -> OutlinedButton({ attempt { select(profile.id) } }, Modifier.fillMaxWidth().heightIn(min=56.dp).testTag("local-profile-${profile.id}")) {
                            Text("${profile.name} · ${studentStageLabel(profile.stage)} · ${if(profile.guest) entryText(com.ahmadmol.enmath.R.string.entry_copy_38) else entryText(com.ahmadmol.enmath.R.string.entry_copy_39)}")
                        } }
                    }
                }
                if (state.legacyCandidates.isNotEmpty()) AppCard {
                    Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_40), style=MaterialTheme.typography.titleMedium)
                    Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_41))
                }
                if (error) Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_42), color = MaterialTheme.colorScheme.error)
                if(page != "stage" && page != "entry") TextButton({ page = if(page in setOf("notice", "create", "profiles") && stage != null) "entry" else "stage"; error = false }) { Text(entryText(com.ahmadmol.enmath.R.string.entry_copy_19)) }
            }
        }
    }
}
