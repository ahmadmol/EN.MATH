package com.ahmadmol.enmath.features.reference

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

@Composable
fun SessionExit(logout: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    RefButton(
        refText("تسجيل الخروج", "Sign out"),
        { confirm = true },
        Modifier.testTag("logout"),
        false,
    )
    if (confirm)
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(refText("تسجيل الخروج؟", "Sign out?")) },
            text = {
                Text(
                    refText(
                        "يمكنك العودة واختيار دور آخر. بيانات التعلم محفوظة على الجهاز.",
                        "You can sign in with another role. Learning data stays on this device.",
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirm = false
                        logout()
                    },
                    modifier = Modifier.testTag("dialog-confirm"),
                ) {
                    Text(refText("تسجيل الخروج", "Sign out"))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(refText("إلغاء", "Cancel")) }
            },
        )
}
