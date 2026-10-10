package com.ahmadmol.enmath.features.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
fun ProductProfileScreen(
    state: LearningState,
    teacher: Boolean,
    onRoute: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var confirm by remember { mutableStateOf(false) }
    ScreenPage(Modifier.testTag("screen-profile")) {
        PageHeading(
            label(TextKey.t_2753fda825),
            if (state.user?.type == AccountType.Guest) label(TextKey.t_5aef859af5)
            else state.user?.name.orEmpty(),
        )
        AppCard(tonal = true) {
            Avatar(state.user?.name.orEmpty())
            Text(if (teacher) label(TextKey.t_e6145a851c) else label(TextKey.t_d60458396f))
            Text(state.user?.email.orEmpty())
            if (teacher) Text(label(TextKey.t_c55bf2e6c2, state.product.classes.size))
            else ProgressRing(LearningRules.stats(state).lessonFraction)
        }
        val prefix = if (teacher) "teacher/" else ""
        SecondaryButton(
            label(TextKey.t_f3f3f790e0),
            { onRoute(prefix + "settings") },
            Modifier.testTag("open-settings"),
        )
        SecondaryButton(label(TextKey.t_bda3e17468), { onRoute(prefix + "notifications") })
        if (!teacher) SecondaryButton(label(TextKey.t_455904a5d7), { onRoute("library") })
        SecondaryButton(label(TextKey.t_40a3a75f0a), { confirm = true }, Modifier.testTag("logout"))
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
        AppCard {
            ThemeMode.entries.forEach { m ->
                Row {
                    RadioButton(
                        state.theme == m,
                        { onTheme(m) },
                        Modifier.testTag("theme-${m.name}"),
                    )
                    TextButton({ onTheme(m) }) {
                        Text(
                            when (m) {
                                ThemeMode.System -> label(TextKey.t_d3524ba8c3)
                                ThemeMode.Light -> label(TextKey.t_d154c99a2e)
                                ThemeMode.Dark -> label(TextKey.t_b384fbdc7e)
                            }
                        )
                    }
                }
            }
        }
        SectionTitle(label(TextKey.t_a2d9619261))
        AppCard {
            Language.entries.forEach { l ->
                Row {
                    RadioButton(
                        state.language == l,
                        { onLanguage(l) },
                        Modifier.testTag("language-${l.name}"),
                    )
                    TextButton({ onLanguage(l) }) {
                        Text(
                            if (l == Language.Arabic) label(TextKey.t_dd385d958b)
                            else label(TextKey.t_eeca8fcbc6)
                        )
                    }
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
            .forEach { (id, label) -> SecondaryButton(label, { dialog = id }) }
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
