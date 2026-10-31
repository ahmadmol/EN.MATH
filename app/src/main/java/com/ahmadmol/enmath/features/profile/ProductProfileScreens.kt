package com.ahmadmol.enmath.features.profile

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.ahmadmol.enmath.core.design.isDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*

private object ProfilePalette {
    @Composable
    fun heroGradient(dark: Boolean) = if (LocalStudentExperience.current) {
        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.surface))
    } else if (dark) {
        Brush.linearGradient(listOf(Color(0xFF10264A), Color(0xFF0C182F)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFEBF3FE), Color(0xFFDCE9F9)))
    }

    @Composable
    fun heroBorder(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF24446E) else Color(0xFFB6D4FA)

    @Composable
    fun cardBackground(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.surface else if (dark) Color(0xFF102137) else Color(0xFFFFFFFF)

    @Composable
    fun cardBorder(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.outlineVariant else if (dark) Color(0xFF1F3759) else Color(0xFFE2E8F0)

    @Composable
    fun progressCardBg(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else if (dark) Color(0xFF0A182E) else Color(0xFFE3EFFC)

    @Composable
    fun textPrimary(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurface else if (dark) Color(0xFFF5F8FF) else Color(0xFF111827)

    @Composable
    fun textSecondary(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onSurfaceVariant else if (dark) Color(0xFFA6B7D2) else Color(0xFF64748B)

    val primaryBlue: Color
        @Composable get() = if (LocalStudentExperience.current) MaterialTheme.colorScheme.primary else Color(0xFF168BFF)
    val cyanAccent: Color
        @Composable get() = if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondary else Color(0xFF24C6F4)
    val purpleAccent: Color
        @Composable get() = if (LocalStudentExperience.current) MaterialTheme.colorScheme.tertiary else Color(0xFF9865F4)

    @Composable
    fun settingsBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else if (dark) Color(0xFF1B2F4C) else Color(0xFFE6F0FF)
    @Composable
    fun notificationsBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.tertiaryContainer else if (dark) Color(0xFF281C44) else Color(0xFFF3E8FF)
    @Composable
    fun libraryBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.secondaryContainer else if (dark) Color(0xFF123344) else Color(0xFFE0F7FA)
    @Composable
    fun assignmentsBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.tertiaryContainer else if (dark) Color(0xFF2B1D45) else Color(0xFFEDE7F6)
    @Composable
    fun progressBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else if (dark) Color(0xFF172E4D) else Color(0xFFE3F2FD)
    @Composable
    fun languageBox(dark: Boolean) = if (LocalStudentExperience.current) MaterialTheme.colorScheme.primaryContainer else if (dark) Color(0xFF152D48) else Color(0xFFE8F1FF)
}

@Composable
fun ProductProfileScreen(
    state: LearningState,
    teacher: Boolean,
    onRoute: (String) -> Unit,
    onLogout: () -> Unit,
    onSwitchRole: (() -> Unit)? = null,
    onLanguage: ((Language) -> Unit)? = null,
    gems: LocalGemsUiState = LocalGemsUiState(),
    planner: com.ahmadmol.enmath.core.data.PlannerRepository? = null,
) {
    if (!teacher && state.user?.studyStage == StudyStage.G9) {
        Grade9ProfileScreen(state, onRoute, onLogout, onSwitchRole, onLanguage, gems, planner)
        return
    }
    var confirm by remember { mutableStateOf(false) }
    val dark = isDarkTheme()
    val ar = state.language == Language.Arabic

    ScreenPage(Modifier.testTag("screen-profile")) {
        // 1. Premium Profile Hero Header Card
        ProfileHeroCard(state = state, teacher = teacher, dark = dark, ar = ar)

        if (!teacher) {
            Text(com.ahmadmol.enmath.features.student.studentStageLabel(state.user?.studyStage))
            Text(if (state.user?.type == AccountType.Guest) tr("Guest · saved locally · no gems", "زائر · حفظ محلي · دون جواهر") else tr("Local student file · no cloud account", "ملف طالب محلي · دون حساب سحابي"))
            com.ahmadmol.enmath.features.planner.PlanSummary(state, planner, onRoute)
            TextButton({ onRoute("review") }) { Text(tr("Mistake review", "مراجعة الأخطاء")) }
            if(com.ahmadmol.enmath.core.data.StudentCatalog.hasUnverifiedHistory(state)) {
                com.ahmadmol.enmath.features.student.ReviewRequiredNotice()
                TextButton({ onRoute("review/history") }) { Text(tr("Read preserved older records", "قراءة السجلات القديمة المحفوظة")) }
            }
        }
        // 2. Gems Card (Student Only)
        if (!teacher) {
            ProfileGemsCard(
                gems = gems,
                user = state.user,
                language = state.language,
                dark = dark,
                onClick = { onRoute("gems/wallet") }
            )
        }

        // 3. Settings & Navigation List
        val prefix = if (teacher) "teacher/" else ""

        ProfileMenuItemCard(
            title = if (ar) "الإعدادات" else "Settings",
            icon = Icons.Outlined.Settings,
            boxColor = ProfilePalette.settingsBox(dark),
            iconColor = ProfilePalette.primaryBlue,
            dark = dark,
            testTag = "open-settings",
            onClick = { onRoute(prefix + "settings") }
        )

        if (teacher || state.user?.studyStage != StudyStage.G9) ProfileMenuItemCard(
            title = if (ar) "الإشعارات" else "Notifications",
            icon = Icons.Outlined.Notifications,
            boxColor = ProfilePalette.notificationsBox(dark),
            iconColor = ProfilePalette.purpleAccent,
            dark = dark,
            onClick = { onRoute(prefix + "notifications") }
        )

        if (!teacher) {
            if (state.user?.studyStage == StudyStage.G9) {
                listOf(
                    Triple(tr("Math solver", "الحل الرياضي"), "solver", Icons.Outlined.Calculate),
                    Triple(tr("Graph lab", "مختبر الرسم"), "explore", Icons.Outlined.Science),
                    Triple(tr("Source and availability", "المصدر والتوفر"), "g9/source", Icons.Outlined.Info),
                    Triple(tr("Help and local data", "المساعدة والبيانات المحلية"), "g9/help", Icons.AutoMirrored.Outlined.HelpOutline),
                ).forEach { (title, route, icon) ->
                    ProfileMenuItemCard(title = title, icon = icon,
                        boxColor = ProfilePalette.settingsBox(dark), iconColor = ProfilePalette.primaryBlue,
                        dark = dark, testTag = "profile-$route", onClick = { onRoute(route) })
                }
            }
            ProfileMenuItemCard(
                title = if (ar) "المكتبة" else "Library",
                icon = Icons.Outlined.LocalLibrary,
                boxColor = ProfilePalette.libraryBox(dark),
                iconColor = ProfilePalette.cyanAccent,
                dark = dark,
                testTag = "profile-library",
                onClick = { onRoute("library") }
            )

            ProfileMenuItemCard(
                title = if (ar) "الواجبات" else "Assignments",
                icon = Icons.Outlined.Assignment,
                boxColor = ProfilePalette.assignmentsBox(dark),
                iconColor = Color(0xFFB072FF),
                dark = dark,
                onClick = { onRoute("assignments") }
            )

            ProfileMenuItemCard(
                title = if (ar) "التقدم" else "Progress",
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                boxColor = ProfilePalette.progressBox(dark),
                iconColor = ProfilePalette.primaryBlue,
                dark = dark,
                onClick = { onRoute("progress") }
            )
        }

        if (onLanguage != null) {
            ProfileMenuItemCard(
                title = if (state.language == Language.Arabic) "English" else "العربية",
                icon = Icons.Outlined.Language,
                boxColor = ProfilePalette.languageBox(dark),
                iconColor = ProfilePalette.primaryBlue,
                dark = dark,
                testTag = "language-toggle",
                onClick = {
                    onLanguage(
                        if (state.language == Language.Arabic) Language.English else Language.Arabic
                    )
                }
            )
        }

        // 4. Experimental Role Switcher
        if (onSwitchRole != null) {
            ExperimentalRoleCard(
                teacher = teacher,
                dark = dark,
                ar = ar,
                onSwitchRole = onSwitchRole
            )
        }

        // 5. Logout Button
        ProfileMenuItemCard(
            title = if (!teacher) tr("Switch local file", "تبديل الملف المحلي") else if (ar) "تسجيل الخروج" else "Logout",
            icon = Icons.Outlined.Logout,
            boxColor = if (dark) Color(0xFF3B1C22) else Color(0xFFFFEBEE),
            iconColor = MaterialTheme.colorScheme.error,
            dark = dark,
            testTag = "logout",
            onClick = { confirm = true }
        )
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(if (!teacher) tr("Switch local file?", "تبديل الملف المحلي؟") else label(TextKey.t_7ac13fdc1e)) },
            text = { Text(if (!teacher) tr("Recorded learning and plans stay in this file. Unsubmitted answers will be discarded. You will choose a separate local file.", "يبقى التعلم والخطط المحفوظة في هذا الملف. ستُترك الإجابات التي لم تُسلّم. ستختار ملفًا محليًا مستقلًا.") else label(TextKey.t_acd69bb6a8)) },
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
}

@Composable
private fun ProfileHeroCard(
    state: LearningState,
    teacher: Boolean,
    dark: Boolean,
    ar: Boolean,
) {
    if (!teacher && state.user?.studyStage != null) {
        val catalog = SourceCurricula.forState(state)
        val stage = state.user?.studyStage
        if (catalog != null) {
            StageHero(stage, state.user?.name.orEmpty(), if (state.user?.type == AccountType.Guest)
                tr("Guest · saved locally", "زائر · محفوظ محليًا") else tr("Local student file · no cloud account", "ملف طالب محلي · دون حساب سحابي"))
            SourceMetrics(listOf("${catalog.units.size}" to tr("Indexed units", "وحدات مفهرسة"),
                "${catalog.researches.size}" to tr("Indexed researches", "أبحاث مفهرسة"),
                "${catalog.reviewed}" to tr("Reviewed lessons", "دروس معتمدة")))
            Text(tr("Source learning evidence is not available yet. Demo evidence is shown separately in Progress.",
                "أدلة تعلّم المصدر غير متاحة بعد. تُعرض أدلة التجربة بصورة مستقلة في التقدم."))
        }
        return
    }

    val user = state.user
    val name = user?.name.orEmpty().ifBlank { if (ar) "طالب" else "Student" }
    val email = if (user?.localId != null) tr("Local file on this device", "ملف محلي على هذا الجهاز")
        else user?.email.orEmpty().ifBlank { "student@example.com" }
    val initial = name.trim().take(1).uppercase()
    val roleText = if (user?.type == AccountType.Guest) tr("Local guest", "زائر محلي") else if (teacher) {
        label(TextKey.t_e6145a851c)
    } else {
        if (state.user?.studyStage == StudyStage.G9) { if (ar) "طالب • التاسع" else "Student • Grade 9" }
        else if (ar) "طالب • الثالث الثانوي العلمي" else "Student • 3rd Scientific Secondary"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ProfilePalette.heroGradient(dark))
            .border(1.dp, ProfilePalette.heroBorder(dark), RoundedCornerShape(24.dp))
    ) {
        ProfileHeroBackground(dark = dark)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    modifier = Modifier
                        .size(76.dp)
                        .border(2.5.dp, ProfilePalette.primaryBlue, CircleShape),
                    shape = CircleShape,
                    color = Color.White
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10264A)
                            )
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(ProfilePalette.primaryBlue, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WorkspacePremium,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textDirection = TextDirection.ContentOrRtl
                ),
                color = ProfilePalette.textPrimary(dark),
                maxLines = if (LocalStudentExperience.current) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = roleText,
                style = MaterialTheme.typography.bodyMedium,
                color = ProfilePalette.textSecondary(dark)
            )

            Text(
                text = email,
                style = MaterialTheme.typography.bodySmall,
                color = ProfilePalette.textSecondary(dark),
                maxLines = if (LocalStudentExperience.current) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            if (!teacher) {
                val progress = com.ahmadmol.enmath.core.data.StudentCatalog.completed(state).size.toFloat() / state.lessons.size.coerceAtLeast(1)
                val percentage = (progress * 100).toInt()

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ProfilePalette.progressCardBg(dark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                                    contentDescription = null,
                                    tint = ProfilePalette.primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (ar) "إكمال المحتوى المتاح فقط" else "Available content completion only",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ProfilePalette.textSecondary(dark)
                                )
                            }
                            Text(
                                text = "$percentage%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = ProfilePalette.cyanAccent
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = ProfilePalette.primaryBlue,
                            trackColor = if (dark) Color(0xFF172A46) else Color(0xFFCBDDF5)
                        )
                    }
                }
            } else {
                Text(
                    text = label(TextKey.t_c55bf2e6c2, state.product.classes.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfilePalette.textSecondary(dark)
                )
            }
        }
    }
}

@Composable
private fun ProfileHeroBackground(
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val capColor = if (dark) Color(0x1A24C6F4) else Color(0x20168BFF)
    val bookColor = if (dark) Color(0x1A9865F4) else Color(0x209865F4)
    val starColor = if (dark) Color(0x4024C6F4) else Color(0x40168BFF)

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val capPath = Path().apply {
            moveTo(w * 0.15f, h * 0.12f)
            lineTo(w * 0.28f, h * 0.06f)
            lineTo(w * 0.40f, h * 0.12f)
            lineTo(w * 0.28f, h * 0.18f)
            close()
        }
        drawPath(capPath, color = capColor, style = Stroke(width = 2.dp.toPx()))

        val capBase = Path().apply {
            moveTo(w * 0.20f, h * 0.14f)
            quadraticTo(w * 0.28f, h * 0.22f, w * 0.36f, h * 0.14f)
            lineTo(w * 0.36f, h * 0.20f)
            quadraticTo(w * 0.28f, h * 0.26f, w * 0.20f, h * 0.20f)
            close()
        }
        drawPath(capBase, color = capColor, style = Stroke(width = 1.5.dp.toPx()))

        val bookPath1 = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.72f,
                    top = h * 0.08f,
                    right = w * 0.92f,
                    bottom = h * 0.14f,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            )
        }
        val bookPath2 = Path().apply {
            addRoundRect(
                RoundRect(
                    left = w * 0.70f,
                    top = h * 0.15f,
                    right = w * 0.94f,
                    bottom = h * 0.21f,
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            )
        }
        drawPath(bookPath1, color = bookColor, style = Stroke(width = 1.5.dp.toPx()))
        drawPath(bookPath2, color = bookColor, style = Stroke(width = 1.5.dp.toPx()))

        val stars = listOf(
            Offset(w * 0.10f, h * 0.30f),
            Offset(w * 0.48f, h * 0.08f),
            Offset(w * 0.88f, h * 0.28f),
            Offset(w * 0.52f, h * 0.32f),
            Offset(w * 0.22f, h * 0.04f),
        )
        stars.forEach { pos ->
            drawCircle(color = starColor, radius = 2.dp.toPx(), center = pos)
        }
    }
}

@Composable
private fun ProfileGemsCard(
    gems: LocalGemsUiState,
    user: User?,
    language: Language,
    dark: Boolean,
    onClick: () -> Unit,
) {
    val ar = language == Language.Arabic
    val balanceText = profileGemBalanceText(gems, user, language)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = ProfilePalette.cardBackground(dark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ProfilePalette.cardBorder(dark), RoundedCornerShape(20.dp))
            .testTag("profile-gems")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ProfilePalette.settingsBox(dark), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Diamond,
                    contentDescription = null,
                    tint = ProfilePalette.primaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ar) "جواهري" else "My Gems",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ProfilePalette.textPrimary(dark)
                )
                Text(
                    text = if (ar) "اجمع الجواهر وحقق المزيد من الإنجازات" else "Collect gems and achieve more",
                    style = MaterialTheme.typography.bodySmall,
                    color = ProfilePalette.textSecondary(dark),
                    maxLines = if (LocalStudentExperience.current) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            if (dark) Color(0xFF1B2A4A) else Color(0xFFE3EFFC),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Diamond,
                            contentDescription = null,
                            tint = ProfilePalette.cyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = balanceText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = ProfilePalette.cyanAccent
                        )
                    }
                }

                Icon(
                    imageVector = if (LocalStudentExperience.current) Icons.AutoMirrored.Outlined.ArrowForward else Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = ProfilePalette.textSecondary(dark),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileMenuItemCard(
    title: String,
    icon: ImageVector,
    boxColor: Color,
    iconColor: Color,
    dark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null,
) {
    val baseMod = modifier
        .fillMaxWidth()
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = ProfilePalette.cardBackground(dark),
        modifier = baseMod.border(1.dp, ProfilePalette.cardBorder(dark), RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(boxColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = ProfilePalette.textPrimary(dark),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (LocalStudentExperience.current) Icons.AutoMirrored.Outlined.ArrowForward else Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = ProfilePalette.textSecondary(dark),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ExperimentalRoleCard(
    teacher: Boolean,
    dark: Boolean,
    ar: Boolean,
    onSwitchRole: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ProfilePalette.cardBackground(dark),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ProfilePalette.cardBorder(dark), RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ProfilePalette.notificationsBox(dark), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Science,
                        contentDescription = null,
                        tint = ProfilePalette.purpleAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (ar) "تبديل الدور — تجريبي" else "Role Switch — Demo",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ProfilePalette.textPrimary(dark)
                    )
                    Text(
                        text = if (teacher) {
                            if (ar) "الدور الحالي: المعلم. بياناتك محفوظة عند التبديل." else "Current role: Teacher. Your data is preserved when switching."
                        } else {
                            if (ar) "الدور الحالي: الطالب. بياناتك محفوظة عند التبديل." else "Current role: Student. Your data is preserved when switching."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ProfilePalette.textSecondary(dark)
                    )
                }

                RoleSwitchGraphic()
            }

            Button(
                onClick = onSwitchRole,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfilePalette.primaryBlue,
                    contentColor = if (LocalStudentExperience.current) MaterialTheme.colorScheme.onPrimary else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("demo-switch-role")
            ) {
                Text(
                    text = if (ar) "الانتقال إلى دور آخر" else "Switch to another role",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun RoleSwitchGraphic(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(56.dp, 44.dp)) {
        val w = size.width
        val h = size.height
        val purple = Color(0xFF9865F4)
        val lightPurple = Color(0x339865F4)

        val leftCenter = Offset(w * 0.28f, h * 0.5f)
        val rightCenter = Offset(w * 0.72f, h * 0.5f)
        val r = 10.dp.toPx()

        drawCircle(color = lightPurple, radius = r, center = leftCenter)
        drawCircle(color = lightPurple, radius = r, center = rightCenter)

        drawCircle(color = purple, radius = 3.5.dp.toPx(), center = Offset(leftCenter.x, leftCenter.y - 2.dp.toPx()))
        val leftBody = Path().apply {
            addArc(
                Rect(leftCenter.x - 6.dp.toPx(), leftCenter.y + 1.dp.toPx(), leftCenter.x + 6.dp.toPx(), leftCenter.y + 9.dp.toPx()),
                180f,
                180f
            )
        }
        drawPath(leftBody, color = purple, style = Stroke(width = 1.5.dp.toPx()))

        drawCircle(color = purple, radius = 3.5.dp.toPx(), center = Offset(rightCenter.x, rightCenter.y - 2.dp.toPx()))
        val rightBody = Path().apply {
            addArc(
                Rect(rightCenter.x - 6.dp.toPx(), rightCenter.y + 1.dp.toPx(), rightCenter.x + 6.dp.toPx(), rightCenter.y + 9.dp.toPx()),
                180f,
                180f
            )
        }
        drawPath(rightBody, color = purple, style = Stroke(width = 1.5.dp.toPx()))

        val topArc = Path().apply {
            moveTo(leftCenter.x + 4.dp.toPx(), h * 0.22f)
            quadraticTo(w * 0.5f, h * 0.08f, rightCenter.x - 4.dp.toPx(), h * 0.22f)
        }
        drawPath(topArc, color = purple, style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))

        val bottomArc = Path().apply {
            moveTo(rightCenter.x - 4.dp.toPx(), h * 0.78f)
            quadraticTo(w * 0.5f, h * 0.92f, leftCenter.x + 4.dp.toPx(), h * 0.78f)
        }
        drawPath(bottomArc, color = purple, style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
    }
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
        // Student settings already has the shared Scaffold header.
        if (!LocalStudentExperience.current) Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Space.xs)
        ) {
            Text(
                text = tr("Preferences", "التفضيلات"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = label(TextKey.t_f3f3f790e0),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section 1: Appearance / Theme matching Image 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.ColorLens,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label(TextKey.t_d1df9e0bcb),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        val themeOptions = listOf(
            Triple(ThemeMode.Dark, label(TextKey.t_b384fbdc7e), Icons.Outlined.DarkMode),
            Triple(ThemeMode.Light, label(TextKey.t_d154c99a2e), Icons.Outlined.LightMode),
            Triple(ThemeMode.System, label(TextKey.t_d3524ba8c3), Icons.Outlined.Phonelink)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            themeOptions.forEach { (mode, title, icon) ->
                val selected = state.theme == mode
                Surface(
                    onClick = { onTheme(mode) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("theme-${mode.name}"),
                    shape = MaterialTheme.shapes.large,
                    color = if (selected)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        if (selected) 2.dp else 1.dp,
                        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = Space.lg, horizontal = Space.xs),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (selected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        RadioButton(
                            selected = selected,
                            onClick = { onTheme(mode) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Section 2: Language matching Image 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.sm)
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label(TextKey.t_a2d9619261),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Language.entries.forEachIndexed { idx, l ->
                    val selected = state.language == l
                    Surface(
                        onClick = { onLanguage(l) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("language-${l.name}"),
                        color = if (selected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Space.lg, vertical = Space.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { onLanguage(l) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text(
                                text = if (l == Language.Arabic) label(TextKey.t_dd385d958b) else label(TextKey.t_eeca8fcbc6),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (idx < Language.entries.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Section 3: Local Notifications matching Image 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Space.lg),
                verticalArrangement = Arrangement.spacedBy(Space.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = if (LocalStudentExperience.current) Modifier.weight(1f) else Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Space.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (LocalStudentExperience.current) tr("Notification preference", "تفضيل الإشعارات") else label(TextKey.t_b70bd0ed57),
                            modifier = if (LocalStudentExperience.current) Modifier.weight(1f) else Modifier,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Switch(
                        checked = state.product.notificationsEnabled,
                        onCheckedChange = { onSettings(it, state.product.reminder) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.md),
                        horizontalArrangement = Arrangement.spacedBy(Space.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (LocalStudentExperience.current)
                                tr("Preferences are saved on this device only. The demo inbox remains available; no system notifications are scheduled.",
                                    "تُحفظ التفضيلات على هذا الجهاز فقط. يبقى الصندوق التجريبي متاحًا، ولا تُجدول إشعارات للنظام.")
                                else label(TextKey.t_903b5a1bb9),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Surface(
                    onClick = {
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
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Space.md, horizontal = Space.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (LocalStudentExperience.current) tr("Preferred study time: ${state.product.reminder}", "وقت الدراسة المفضّل: ${state.product.reminder}") else label(TextKey.t_60ed61ee92, state.product.reminder),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Section 4: Support & Legal Rows matching Image 1
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                val menuItems = listOf(
                    Triple("help", label(TextKey.t_afb12a0f65), Icons.Outlined.HelpOutline),
                    Triple("about", label(TextKey.t_473d186ca4), Icons.Outlined.Info),
                    Triple("privacy", label(TextKey.t_13df32fad2), Icons.Outlined.Shield)
                )

                menuItems.forEachIndexed { idx, (id, itemLabel, icon) ->
                    Surface(
                        onClick = { dialog = id },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Space.lg, vertical = Space.md),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Space.md)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )

                            Text(
                                text = itemLabel,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (idx < menuItems.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Section 5: Logout Button matching Image 1
        Surface(
            onClick = { dialog = "logout" },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Space.md, horizontal = Space.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.sm, Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label(TextKey.t_40a3a75f0a),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (dialog.isNotBlank()) {
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
}
