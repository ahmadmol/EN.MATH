package com.ahmadmol.enmath.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.AppNotification

@Composable
fun NotificationsScreen(
    state: LearningState,
    teacher: Boolean,
    onRead: (String) -> Unit,
    onAll: () -> Unit,
    onRoute: (String) -> Unit,
) {
    val notes = state.product.notifications.filter { it.teacher == teacher }

    ScreenPage(Modifier.testTag("screen-notifications")) {
        // Mark All as Read Primary Blue Button
        RefPrimaryButton(
            text = label(TextKey.t_338c877d38),
            onClick = onAll,
            icon = Icons.Outlined.CheckCircle,
            modifier = Modifier.testTag("mark-all-read")
        )

        // Offline / Changes Saved Info Banner
        RefInfoBanner(
            text = tr(
                "Offline experience • Changes are saved on this device",
                "تجربة دون اتصال • تُحفظ التغييرات على هذا الجهاز"
            )
        )

        // Empty state or list of notifications
        val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
        if (notes.isEmpty()) {
            RefEmptyState(
                title = label(TextKey.t_557d76d5d1),
                subtitle = label(TextKey.t_4850f26893),
                artwork = { SearchHeroArtwork() }
            )
        } else {
            notes
                .sortedByDescending { it.timestamp }
                .forEach { note ->
                    val isRead = note.id in state.product.readNotifications
                    NotificationCard(
                        note = note,
                        isRead = isRead,
                        isDark = isDark,
                        onClick = {
                            onRead(note.id)
                            if (note.target.isNotBlank()) {
                                onRoute(note.target)
                            }
                        }
                    )
                }
        }
    }
}

@Composable
private fun NotificationCard(
    note: AppNotification,
    isRead: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFF10243C)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else Color(0xFFFFFFFF))
    val borderColor = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFF1E3A60)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else Color(0xFFE2E8F0))

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification-item-${note.id}"),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isDark) 0.dp else 2.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Red Unread Dot (Top Start)
            if (!isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background((if (LocalStudentExperience.current) MaterialTheme.colorScheme.error else Color(0xFFEF4444)))
                        .align(Alignment.TopStart)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.md),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(Space.md)
                ) {
                    // Right Bell Icon Container Badge
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFF1E3A60)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE0F2FE))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF)),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Content Title and Body
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = note.title.text(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFFF8FAFC)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else Color(0xFF0F172A))
                        )
                        Text(
                            text = note.body.text(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B))
                        )
                    }
                }

                // Bottom Status Pill: "غير مقروء" (Unread) or "مقروء" (Read)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isRead) {
                            if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFF1E3A60)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE0F2FE))
                        } else {
                            if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFF1A293D)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFF1F5F9))
                        }
                    ) {
                        Text(
                            text = if (!isRead) tr("Unread", "غير مقروء") else tr("Read", "مقروء"),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            ),
                            color = if (!isRead) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF)) else (if (isDark) (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF64748B)) else (if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF94A3B8))),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
