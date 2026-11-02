package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.StudyStage

@Composable
fun StageHero(
    stage: StudyStage,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    badgeIcon: ImageVector? = null
) {
    val dark = isDarkTheme()
    val bgBrush = if (stage == StudyStage.G9) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface))
    } else if (dark) {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(Color(0xFF131D38), Color(0xFF1E2D56))
        )
    } else {
        androidx.compose.ui.graphics.Brush.horizontalGradient(
            listOf(Color(0xFFE8F1FC), Color(0xFFD6E8FA))
        )
    }
    val borderColor = if (stage == StudyStage.G9) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF263765) else Color(0xFFC8DEFA)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Character Illustration (Boy at desk with laptop & books)
                Image(
                    painter = painterResource(
                        if (stage == StudyStage.G9) R.drawable.student_g9_transparent
                        else R.drawable.student_bac_transparent
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .width(if (stage == StudyStage.G9) 72.dp else 96.dp)
                        .heightIn(max = 130.dp)
                )

                // Text Content
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "EN.MATH • " + if (stage == StudyStage.G9) tr("Grade 9", "التاسع") else tr("Baccalaureate", "البكالوريا"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (stage == StudyStage.G9) MaterialTheme.colorScheme.primary else if (dark) Color(0xFF90B0E6) else Color(0xFF4A6899)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            ),
                            color = if (dark) Color(0xFFB0BDD8) else Color(0xFF475569)
                        )
                    }
                }

                // Optional corner badge icon
                if (badgeIcon != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dark) Color(0xFF223565) else Color(0xFFD6E6FE),
                        modifier = Modifier
                            .align(Alignment.Top)
                            .size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = badgeIcon,
                                contentDescription = null,
                                tint = if (dark) Color(0xFF79A8FF) else Color(0xFF0066FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Notice banner matching the reference design:
 * Light-mint/cyan card with document graphic + clock badge and info text.
 */
@Composable
fun SourceStatusNotice(stage: StudyStage) {
    val dark = isDarkTheme()
    val bgColor = if (dark) Color(0xFF0D2520) else Color(0xFFE2F7ED)
    val borderColor = if (dark) Color(0xFF1E453C) else Color(0xFFB2E8D2)
    val textColor = if (dark) Color(0xFFD1F5E5) else Color(0xFF0A4032)
    val subTextColor = if (dark) Color(0xFFA1D4C0) else Color(0xFF1B6350)

    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Document with clock/hourglass mini-illustration
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 58.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (dark) Color(0xFF163E35) else Color(0xFFCEF0E0),
                    border = BorderStroke(1.dp, if (dark) Color(0xFF285E52) else Color(0xFFA0DEBF)),
                    modifier = Modifier.size(width = 38.dp, height = 48.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(0.8f).height(3.dp).background(if (dark) Color(0xFF438A78) else Color(0xFF6CC59E), CircleShape))
                        Box(Modifier.fillMaxWidth(0.9f).height(3.dp).background(if (dark) Color(0xFF438A78) else Color(0xFF6CC59E), CircleShape))
                        Box(Modifier.fillMaxWidth(0.6f).height(3.dp).background(if (dark) Color(0xFF438A78) else Color(0xFF6CC59E), CircleShape))
                    }
                }
                // Clock badge in bottom corner
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF4F6BF8),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Text info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (stage == StudyStage.G9) tr("Source index · content in preparation", "فهرس المصدر · المحتوى قيد الإعداد")
                        else tr("Edition and content review pending", "مطابقة الطبعة وتدقيق المحتوى معلّقان"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = textColor
                    )
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = tr(
                        "Browse and plan freely. Reading a reference does not complete a lesson or award gems.",
                        "تصفّح وخطّط حسب وتيرتك. فتح المرجع لا يسجل إكمال درس ولا يمنح جواهر."
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = subTextColor
                )
            }
        }
    }
}

/**
 * Clickable item card matching reference screens:
 * Right icon in rounded container, middle title & subtitle, left chevron arrow `<` in RTL.
 */
@Composable
fun SourceItemCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Outlined.MenuBook,
    trailingBadge: (@Composable () -> Unit)? = null,
    action: () -> Unit
) {
    val dark = isDarkTheme()
    val bgColor = if (dark) Color(0xFF121B30) else Color.White
    val borderColor = if (dark) Color(0xFF1E2D4E) else Color(0xFFE2E8F0)

    Card(
        onClick = action,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Forward Chevron in RTL (points left)
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = if (dark) Color(0xFF8C98B6) else Color(0xFF64748B),
                modifier = Modifier.size(18.dp)
            )

            // Middle Content: Title and Subtitle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (dark) Color.White else Color(0xFF0F172A),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (trailingBadge != null) {
                trailingBadge()
            }

            // Right Icon Container (e.g., book, chart, flask)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (dark) Color(0xFF1E2D4E) else Color(0xFFEBF3FF),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (dark) Color(0xFF7CA6FF) else Color(0xFF0066FF),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * 3-Card Metrics Row matching reference screens:
 * Icon in circle, large bold count, clean label.
 */
@Composable
fun SourceMetrics(
    values: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme()
    val g9 = LocalStudyStage.current == StudyStage.G9
    val bgColor = if (g9) MaterialTheme.colorScheme.surface else if (dark) Color(0xFF121B30) else Color.White
    val borderColor = if (g9) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF1E2D4E) else Color(0xFFE2E8F0)

    val metricIcons = listOf(
        Icons.AutoMirrored.Outlined.MenuBook,
        Icons.Outlined.Layers,
        Icons.Outlined.Description
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        values.forEachIndexed { index, (value, label) ->
            val icon = metricIcons.getOrElse(index) { Icons.Outlined.CheckCircle }
            val iconTint = if (g9) MaterialTheme.colorScheme.primary else when (index % 3) {
                0 -> if (dark) Color(0xFFB388FF) else Color(0xFF7C3AED)
                1 -> if (dark) Color(0xFF64B5F6) else Color(0xFF0084FF)
                else -> if (dark) Color(0xFF81C784) else Color(0xFF0F9D58)
            }
            val iconBg = iconTint.copy(alpha = if (dark) 0.20f else 0.12f)

            Card(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, borderColor),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Icon in circle
                    Surface(
                        shape = CircleShape,
                        color = iconBg,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Value
                    Text(
                        text = value,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )

                    // Label
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        maxLines = if (g9) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun sourcePageSpan(span: SourceSpan, grade9: Boolean = false) =
    if (grade9) tr("PDF ${span.pdfFrom}–${span.pdfTo}", "صفحات PDF ${span.pdfFrom}–${span.pdfTo}")
    else if (span.valid) tr("Printed ${span.printedFrom}–${span.printedTo} · PDF ${span.pdfFrom}–${span.pdfTo}",
        "المطبوع ${span.printedFrom}–${span.printedTo} · PDF ${span.pdfFrom}–${span.pdfTo}")
    else tr("PDF starts at ${span.pdfFrom}; end boundary under review", "بداية PDF ${span.pdfFrom}؛ نهاية النطاق قيد المراجعة")

/**
 * Pending card matching Image 4 & 8:
 * White/dark card with rounded square blue hourglass container on right, title & description on left.
 */
@Composable
fun SourcePendingCard(
    title: String,
    detail: String,
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme()
    val g9 = LocalStudyStage.current == StudyStage.G9
    val bgColor = if (g9) MaterialTheme.colorScheme.surfaceContainerLow else if (dark) Color(0xFF121B30) else Color.White
    val borderColor = if (g9) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF1E2D4E) else Color(0xFFE2E8F0)

    Card(
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (g9) MaterialTheme.colorScheme.onSurface else if (dark) Color.White else Color(0xFF0F172A)
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = if (g9) MaterialTheme.colorScheme.onSurfaceVariant else if (dark) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (g9) MaterialTheme.colorScheme.primaryContainer else if (dark) Color(0xFF1E2D4E) else Color(0xFFE6EFFF),
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.HourglassEmpty,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (dark) Color(0xFF7CA6FF) else Color(0xFF0066FF)
                    )
                }
            }
        }
    }
}

/**
 * Large Empty State Card matching Image 5:
 * Soft cyan glowing backdrop, standing document with folded corner and lines,
 * leaves accents, prominent blue hourglass in foreground, centered bold title and details.
 */
@Composable
fun EmptyMistakesCard(
    modifier: Modifier = Modifier
) {
    val dark = isDarkTheme()
    val g9 = LocalStudyStage.current == StudyStage.G9
    val bgColor = if (g9) MaterialTheme.colorScheme.surfaceContainerLow else if (dark) Color(0xFF121B30) else Color.White
    val borderColor = if (g9) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF1E2D4E) else Color(0xFFE2E8F0)

    Card(
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 130.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    if (dark) Color(0xFF1A365D).copy(alpha = 0.5f) else Color(0xFFE0F2FE),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, bottomEnd = 16.dp),
                    color = if (dark) Color(0xFF1B4D3E) else Color(0xFF86EFAC).copy(alpha = 0.6f),
                    modifier = Modifier
                        .offset(x = (-35).dp, y = 20.dp)
                        .size(width = 24.dp, height = 36.dp)
                ) {}
                Surface(
                    shape = RoundedCornerShape(topEnd = 16.dp, bottomStart = 16.dp),
                    color = if (dark) Color(0xFF143A2F) else Color(0xFF4ADE80).copy(alpha = 0.5f),
                    modifier = Modifier
                        .offset(x = (-45).dp, y = 28.dp)
                        .size(width = 20.dp, height = 28.dp)
                ) {}

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (dark) Color(0xFF1E293B) else Color(0xFFF8FAFC),
                    border = BorderStroke(1.5.dp, if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .offset(x = (-10).dp, y = (-5).dp)
                        .size(width = 75.dp, height = 95.dp)
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(Modifier.fillMaxWidth(0.6f).height(4.dp).background(if (dark) Color(0xFF38BDF8) else Color(0xFF0284C7), CircleShape))
                        Box(Modifier.fillMaxWidth(0.9f).height(4.dp).background(if (dark) Color(0xFF475569) else Color(0xFFCBD5E1), CircleShape))
                        Box(Modifier.fillMaxWidth(0.85f).height(4.dp).background(if (dark) Color(0xFF475569) else Color(0xFFCBD5E1), CircleShape))
                        Box(Modifier.fillMaxWidth(0.5f).height(4.dp).background(if (dark) Color(0xFF475569) else Color(0xFFCBD5E1), CircleShape))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (dark) Color(0xFF1E3A8A) else Color(0xFFEFF6FF),
                    border = BorderStroke(2.dp, if (dark) Color(0xFF3B82F6) else Color(0xFF2563EB)),
                    modifier = Modifier
                        .offset(x = 25.dp, y = 15.dp)
                        .size(56.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = if (dark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Text(
                text = tr("No saved mistakes yet", "لا أخطاء محفوظة بعد"),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = if (g9) MaterialTheme.colorScheme.onSurface else if (dark) Color.White else Color(0xFF0F172A),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = tr(
                    "A saved practice attempt is the review source.\nSource exercises remain under review.",
                    "محاولة التدريب المحفوظة هي مصدر المراجعة.\nتمارين المصدر ما زالت قيد التدقيق."
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                ),
                color = if (g9) MaterialTheme.colorScheme.onSurfaceVariant else if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
