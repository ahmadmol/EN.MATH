package com.ahmadmol.enmath.features.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.reference.DemoRoleSwitch

@Composable
fun ProductProfileScreen(
    state: LearningState,
    teacher: Boolean,
    onRoute: (String) -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: (() -> Unit)? = null,
    onLanguage: ((Language) -> Unit)? = null,
) {
    var confirm by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-profile")) {
        PageHeading(
            label(TextKey.t_2753fda825),
            if (state.user?.type == AccountType.Guest) label(TextKey.t_5aef859af5)
            else label(TextKey.t_e269f67032),
        )
        AppCard {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Space.md),
            ) {
                Avatar(state.user?.name.orEmpty())
                Text(state.user?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(
                    if (teacher) label(TextKey.t_e6145a851c) else label(TextKey.t_d60458396f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(state.user?.email.orEmpty(), style = MaterialTheme.typography.bodySmall)
                if (teacher) Text(label(TextKey.t_c55bf2e6c2, state.product.classes.size))
                else {
                    val progress = LearningRules.stats(state).lessonFraction
                    Text(
                        "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        val prefix = if (teacher) "teacher/" else ""
        DestinationRow(
            label(TextKey.t_f3f3f790e0),
            Icons.Outlined.Settings,
            { onRoute(prefix + "settings") },
            Modifier.testTag("open-settings"),
        )
        DestinationRow(
            label(TextKey.t_bda3e17468),
            Icons.Outlined.Notifications,
            { onRoute(prefix + "notifications") },
        )
        if (!teacher)
            DestinationRow(
                label(TextKey.t_455904a5d7),
                Icons.Outlined.LocalLibrary,
                { onRoute("library") },
                Modifier.testTag("profile-library"),
            )
        if (!teacher) {
            DestinationRow(
                label(TextKey.t_f6202fc5ef),
                Icons.Outlined.Assignment,
                { onRoute("assignments") },
            )
            DestinationRow(
                label(TextKey.t_693fec8aa7),
                Icons.Outlined.Insights,
                { onRoute("progress") },
            )
        }
        if (onLanguage != null)
            DestinationRow(
                if (state.language == Language.Arabic) "English" else "العربية",
                Icons.Outlined.Language,
                {
                    onLanguage(
                        if (state.language == Language.Arabic) Language.English else Language.Arabic
                    )
                },
                Modifier.testTag("language-toggle"),
            )
        if (onSwitchRole != null) DemoRoleSwitch(teacher, onSwitchRole)
        DestinationRow(
            label(TextKey.t_40a3a75f0a),
            Icons.Outlined.Logout,
            { confirm = true },
            Modifier.testTag("logout"),
        )
    }
    if (confirm)
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(label(TextKey.t_7ac13fdc1e)) },
            text = { Text(label(TextKey.t_acd69bb6a8)) },
            confirmButton = {
                TextButton(onLogout, Modifier.testTag("dialog-confirm")) {
                    Text(label(TextKey.t_2948c7a5a8))
                }
            },
            dismissButton = {
                TextButton({ confirm = false }) { Text(label(TextKey.t_823a3ad20c)) }
            },
        )
}

@Composable
fun SettingsScreen(
    state: LearningState,
    onTheme: (ThemeMode) -> Unit,
    onLanguage: (Language) -> Unit,
    onSettings: (Boolean, String) -> Unit,
    onLogout: () -> Unit,
) {
    var dialog by remember { mutableStateOf("") }
    val context = LocalContext.current
    ScreenPage(Modifier.testTag("screen-settings")) {
        PageHeading(label(TextKey.t_2f4cbc4954), label(TextKey.t_f3f3f790e0))
        SectionTitle(label(TextKey.t_d1df9e0bcb))
        Row(
            Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Space.sm),
        ) {
            ThemeMode.entries.forEach { m ->
                val selected = state.theme == m
                Surface(
                    Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    color =
                        if (selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                ) {
                    Column(
                        Modifier.fillMaxHeight()
                            .selectable(selected, role = Role.RadioButton, onClick = { onTheme(m) })
                            .testTag("theme-${m.name}")
                            .padding(Space.md),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Space.sm),
                    ) {
                        Icon(
                            when (m) {
                                ThemeMode.System -> Icons.Outlined.BrightnessAuto
                                ThemeMode.Light -> Icons.Outlined.LightMode
                                ThemeMode.Dark -> Icons.Outlined.DarkMode
                            },
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            when (m) {
                                ThemeMode.System -> label(TextKey.t_d3524ba8c3)
                                ThemeMode.Light -> label(TextKey.t_d154c99a2e)
                                ThemeMode.Dark -> label(TextKey.t_b384fbdc7e)
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                        RadioButton(selected, onClick = null)
                    }
                }
            }
        }
        SectionTitle(label(TextKey.t_a2d9619261))
        AppCard {
            Language.entries.forEach { l ->
                Row(
                    Modifier.fillMaxWidth()
                        .heightIn(min = Space.touch)
                        .selectable(
                            state.language == l,
                            role = Role.RadioButton,
                            onClick = { onLanguage(l) },
                        )
                        .testTag("language-${l.name}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                ) {
                    Icon(Icons.Outlined.Language, null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        if (l == Language.Arabic) label(TextKey.t_dd385d958b)
                        else label(TextKey.t_eeca8fcbc6),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    RadioButton(state.language == l, onClick = null)
                }
            }
        }
        AppCard {
            Row {
                Text(label(TextKey.t_b70bd0ed57), Modifier.weight(1f))
                Switch(
                    state.product.notificationsEnabled,
                    { onSettings(it, state.product.reminder) },
                )
            }
            DemoNotice(label(TextKey.t_903b5a1bb9))
            SecondaryButton(
                label(TextKey.t_60ed61ee92, state.product.reminder),
                {
                    val parts = state.product.reminder.split(":")
                    TimePickerDialog(
                            context,
                            { _, h, m ->
                                onSettings(
                                    state.product.notificationsEnabled,
                                    "%02d:%02d".format(java.util.Locale.US, h, m),
                                )
                            },
                            parts[0].toIntOrNull() ?: 18,
                            parts.getOrNull(1)?.toIntOrNull() ?: 0,
                            true,
                        )
                        .show()
                },
            )
        }
        listOf(
                "help" to label(TextKey.t_afb12a0f65),
                "about" to label(TextKey.t_473d186ca4),
                "privacy" to label(TextKey.t_13df32fad2),
            )
            .forEach { (id, label) ->
                DestinationRow(
                    label,
                    when (id) {
                        "help" -> Icons.Outlined.HelpOutline
                        "privacy" -> Icons.Outlined.Shield
                        else -> Icons.Outlined.Info
                    },
                    { dialog = id },
                )
            }
        SecondaryButton(label(TextKey.t_40a3a75f0a), { dialog = "logout" })
    }
    if (dialog.isNotBlank())
        AlertDialog(
            onDismissRequest = { dialog = "" },
            title = { Text(if (dialog == "logout") label(TextKey.t_9e4e01dc96) else "EN.MATH") },
            text = {
                Text(
                    when (dialog) {
                        "help" -> label(TextKey.t_3dc06c8440)
                        "privacy" -> label(TextKey.t_6be9b96bd7)
                        "logout" -> label(TextKey.t_fb9232756f)
                        else -> label(TextKey.t_59e803052e)
                    }
                )
            },
            confirmButton = {
                TextButton({
                    val logout = dialog == "logout"
                    dialog = ""
                    if (logout) onLogout()
                }) {
                    Text(label(TextKey.t_61bbd2bb4c))
                }
            },
        )
}
