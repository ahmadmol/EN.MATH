package com.ahmadmol.enmath.features.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.*

@Composable
fun NotificationsScreen(
    state: LearningState,
    teacher: Boolean,
    onRead: (String) -> Unit,
    onAll: () -> Unit,
    onRoute: (String) -> Unit,
) {
    val notes = state.product.notifications.filter { it.teacher == teacher }
    ScreenPage(androidx.compose.ui.Modifier.testTag("screen-notifications")) {
        SecondaryButton(label(TextKey.t_338c877d38), onAll)
        OfflineState()
        if (notes.isEmpty()) EmptyState(label(TextKey.t_557d76d5d1), label(TextKey.t_4850f26893))
        notes
            .sortedByDescending { it.timestamp }
            .forEach { note ->
                NotificationItem(note, note.id in state.product.readNotifications) {
                    onRead(note.id)
                    onRoute(note.target)
                }
            }
    }
}
