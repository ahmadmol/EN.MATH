package com.ahmadmol.enmath.features.courses

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import com.ahmadmol.enmath.core.design.isDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.design.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyScreen(state: LearningState, onBook: (String) -> Unit, onContinue: (String) -> Unit, onExtra: (String) -> Unit = {}) {
    PremiumPage(Modifier.testTag("screen-study")) {
        PremiumHeroCard {
            PremiumHeroLayout(artwork = { LibraryBooksArtwork() }) {
                Text(tr("THE LIBRARY", "المكتبة"), color = MaterialTheme.colorScheme.primary)
                Text(tr("Give understanding room to grow.", "امنح الفهم مساحته."), style = MaterialTheme.typography.headlineMedium)
                Text(tr("${learningNumber(state.books.size)} books · ${learningNumber(state.topics.size)} topics · a clear path forward.",
                    "${learningNumber(state.books.size)} كتب · ${learningNumber(state.topics.size)} موضوعات · مسار واضح للتقدم."), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                listOf(Icons.Outlined.AutoStories to tr("Books", "كتب تعليمية"), Icons.Outlined.Layers to tr("Topics", "مواضيع مرتبة"),
                    Icons.Outlined.Route to tr("Clear path", "مسار واضح"), Icons.Outlined.FactCheck to tr("Checkpoints", "نقاط تحقق")).forEach { (icon, title) ->
                    Column(Modifier.widthIn(min = 64.dp).weight(1f), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        EducationalInfoBanner(tr("Selected demo lessons — a preview of the learning experience.", "دروس تجريبية منتقاة — معاينة للتجربة التعليمية."))
        state.books.forEachIndexed { index, book ->
            val progress = state.completion(book.lessons)
            ChapterProgressCard(book.title.text(), book.subtitle.text() + " · " + tr("${learningNumber(progress.completed)} of ${learningNumber(progress.total)} lessons completed", "${learningNumber(progress.completed)} من ${learningNumber(progress.total)} درس مكتمل"),
                if (index == 0) "∫" else "π", progress.completed, progress.total,
                if (index == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                { onBook(book.id) }, Modifier.testTag("catalog-book-${book.id}"))
        }
        if (state.books.isEmpty()) EmptyState(tr("No books yet", "لا توجد كتب بعد"), tr("Your curriculum will appear here.", "سيظهر منهاجك هنا."))
        state.continueLesson(state.lessons)?.let { next ->
            PremiumActionButton(tr("Continue learning", "متابعة الدراسة"), { onContinue(next.id) }, Modifier.testTag("catalog-continue"))
        }
        listOf(
            Triple("assignments", tr("Assignments", "الواجبات"), tr("Practise and improve your skills.", "تدرّب وحسّن مستواك.")),
            Triple("library", tr("Saved content", "المحفوظات"), tr("Quick access to your content.", "الوصول السريع لمحتواك.")),
            Triple("explore", tr("Explore graphs", "استكشاف الرسوم"), tr("Visualise mathematical concepts.", "رسوم توضيحية للمفاهيم الرياضية."))
        ).forEachIndexed { index, (route, title, subtitle) ->
            PremiumHeroCard(onClick = { onExtra(route) }) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.lg)) {
                    Icon(listOf(Icons.Outlined.Assignment, Icons.Outlined.BookmarkBorder, Icons.Outlined.Insights)[index], null,
                        tint = if(index == 1) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.titleLarge)
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun BookDetailsScreen(id: String, state: LearningState, onTopic: (String) -> Unit, onLesson: (String) -> Unit) {
    val book = state.books.find { it.id == id }
    PremiumPage(Modifier.testTag("screen-book")) {
        if (book == null) {
            EmptyState(label(TextKey.t_c42ca1990b), label(TextKey.t_107d03c5ec))
            return@PremiumPage
        }
        val progress = state.completion(book.lessons)
        PremiumHeroCard {
            PremiumHeroLayout(artwork = { PremiumBookArtwork(if (book.id == "book-1") "∫" else "π") }) {
                Text(tr("Scientific secondary · year 3", "الثالث الثانوي العلمي"), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                Text(book.title.text(), style = MaterialTheme.typography.headlineMedium)
                Text(book.subtitle.text(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LearningStatisticsCards(listOf(learningPercent(progress.percent) to tr("Lessons complete", "إنجاز الدروس"),
                learningNumber(book.topics.size) to tr("Chapters", "فصول"), learningNumber(progress.total) to tr("Lessons", "دروس")))
            Text(tr("${learningNumber(progress.completed)} of ${learningNumber(progress.total)} lessons completed", "${learningNumber(progress.completed)} من ${learningNumber(progress.total)} درس مكتمل"))
            LearningProgressBar(progress.completed, progress.total)
        }
        state.continueLesson(book.lessons)?.let { next ->
            PremiumActionButton(if(progress.completed == progress.total) tr("Book complete · review", "اكتمل الكتاب · مراجعة")
                else tr("Continue this book", "متابعة هذا الكتاب"), { onLesson(next.id) }, Modifier.testTag("book-continue"), icon = Icons.Outlined.PlayArrow)
        }
        SectionTitle(tr("Chapters & topics", "الفصول والمواضيع"))
        Text(tr("${learningNumber(book.topics.size)} chapters", "${learningNumber(book.topics.size)} فصول"), color = MaterialTheme.colorScheme.onSurfaceVariant)
        book.topics.forEachIndexed { index, topic ->
            val chapter = state.completion(topic.lessons)
            val accent = when(index % 3) { 1 -> MaterialTheme.colorScheme.tertiary; 2 -> MaterialTheme.colorScheme.secondary; else -> MaterialTheme.colorScheme.primary }
            ChapterProgressCard(topic.title.text(), tr("${learningNumber(chapter.completed)} of ${learningNumber(chapter.total)} lessons completed", "${learningNumber(chapter.completed)} من ${learningNumber(chapter.total)} درس مكتمل"),
                topic.symbol, chapter.completed, chapter.total, accent, { onTopic(topic.id) }, Modifier.testTag("book-topic-${topic.id}"))
        }
    }
}

@Composable
fun TopicLessonsScreen(
    id: String,
    state: LearningState,
    onLesson: (String) -> Unit,
    onPractice: (String) -> Unit = {},
) {
    com.ahmadmol.enmath.features.student.StudentLimitsTopic(
        state = state,
        lesson = onLesson,
        practice = { state.practiceSets.firstOrNull { it.topicId == id && !it.quiz }?.let { onPractice(it.id) } },
        topicId = id,
    )
}

@Composable
fun LimitsHeaderGraphic(
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val mainColor = if (isDark) Color(0xFF3892FF) else Color(0xFF168BFF)
    val alpha = if (isDark) 0.20f else 0.32f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val gridStep = 22.dp.toPx()
        var x = 0f
        while (x < w) {
            drawLine(
                color = mainColor.copy(alpha = 0.04f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1.dp.toPx()
            )
            x += gridStep
        }

        val xAxisY = h * 0.72f
        val yAxisX = w * 0.22f
        drawLine(
            color = mainColor.copy(alpha = alpha * 0.6f),
            start = Offset(8.dp.toPx(), xAxisY),
            end = Offset(w - 8.dp.toPx(), xAxisY),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = mainColor.copy(alpha = alpha * 0.6f),
            start = Offset(yAxisX, 8.dp.toPx()),
            end = Offset(yAxisX, h - 8.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )

        val curvePath = Path().apply {
            moveTo(yAxisX * 0.3f, h * 0.88f)
            cubicTo(
                w * 0.28f, h * 0.52f,
                w * 0.50f, h * 0.24f,
                w * 0.90f, h * 0.12f
            )
        }
        drawPath(
            path = curvePath,
            color = mainColor.copy(alpha = alpha),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        val limitPoint = Offset(w * 0.58f, h * 0.32f)

        drawCircle(
            color = mainColor.copy(alpha = alpha * 0.9f),
            radius = 4.5.dp.toPx(),
            center = limitPoint,
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = mainColor.copy(alpha = alpha * 0.35f),
            radius = 2.dp.toPx(),
            center = limitPoint
        )

        val p1 = Offset(w * 0.42f, h * 0.46f)
        val p2 = Offset(w * 0.74f, h * 0.22f)
        drawCircle(color = mainColor.copy(alpha = alpha * 0.7f), radius = 3.dp.toPx(), center = p1)
        drawCircle(color = mainColor.copy(alpha = alpha * 0.7f), radius = 3.dp.toPx(), center = p2)
    }
}

@Composable
fun LessonScreen(
    id: String,
    state: LearningState,
    onComplete: () -> Unit,
    onPractice: (String) -> Unit = {},
    onNext: (String) -> Unit = {},
    onBookmark: () -> Unit = {},
    onVerified: (String, String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    onSearch: () -> Unit = {},
    onNotification: () -> Unit = {},
) {
    val lesson = state.lesson(id)
    val isDark = isDarkTheme()

    // Theme Colors
    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = MaterialTheme.colorScheme.surface
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val borderStrokeColor = MaterialTheme.colorScheme.outlineVariant
    val primaryBlue = MaterialTheme.colorScheme.primary
    val electricBlue = Color(0xFF2563EB)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("screen-lesson")
    ) {
        if (lesson == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RefTopBar(tr("Lesson", "الدرس"), onBack = onBack, backTag = "nav-back")
                EmptyState(tr("Lesson unavailable", "الدرس غير متاح"), tr("Return to curriculum.", "عد إلى المنهج."))
            }
            return@Box
        }

        val topic = state.topicOf(id)
        val book = state.books.find { b -> b.topics.any { it.id == topic?.id } }
        val isSaved = id in state.product.savedLessons
        val isCompleted = id in StudentCatalog.completed(state)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RefTopBar(tr("Lesson", "الدرس"), onBack, onSearch, onNotification,
                state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications },
                backTag = "nav-back", searchTag = "header-search", notificationsTag = "header-notifications")
            // 2. Lesson Overview Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (isDark) 0.dp else 2.dp,
                        shape = RoundedCornerShape(22.dp)
                    ),
                shape = RoundedCornerShape(22.dp),
                color = if (isDark) Color(0xFF102238) else Color(0xFFEBF3FF),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF1E3A5F) else Color(0xFFD6E6FF))
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    LimitsHeaderGraphic(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(22.dp)),
                        isDark = isDark
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = tr("Learning session", "جلسة تعلّم"),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = textSecondary
                        )

                        Text(
                            text = lesson.title.text(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = textPrimary
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = tr("${lesson.minutes} minutes for understanding", "${lesson.minutes} دقائق لبناء الفهم"),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 14.sp
                                ),
                                color = textSecondary
                            )
                        }

                        // Demo Notice Banner Pill
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDark) Color(0xFF162B46) else Color.White.copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF23446E) else Color(0xFFD0E2FF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = primaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = tr("Short demo lesson · Not a full curriculum", "درس تجريبي مختصر · ليس منهجًا كاملًا"),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Breadcrumbs & Bookmark / Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Breadcrumbs Path (RTL start)
                val bookTitle = book?.title?.text() ?: tr("Book One", "الكتاب الأول")
                val topicTitle = topic?.title?.text() ?: tr("Limits", "النهايات")
                val pathText = "$bookTitle > $topicTitle > ${lesson.title.text()}"

                Text(
                    text = pathText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = textSecondary,
                    modifier = Modifier.weight(1f)
                )

                // Save Action Button (RTL end)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBookmark() }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("lesson-bookmark"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isSaved) tr("Saved", "محفوظ") else tr("Save", "حفظ"),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = primaryBlue
                    )

                    Icon(
                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isSaved) tr("Saved", "محفوظ") else tr("Save", "حفظ"),
                        tint = primaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 4. Objectives Card ("الأهداف")
            val objectivesText = lesson.objectives.text().ifEmpty { lesson.introduction.text() }
            if (objectivesText.isNotBlank()) {
                LessonSectionCard(
                    icon = Icons.Outlined.TrackChanges,
                    title = tr("Objectives", "الأهداف"),
                    isDark = isDark
                ) {
                    Text(
                        text = objectivesText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = textSecondary
                    )
                }
            }

            // 5. Solved Example Card ("مثال محلول")
            if (lesson.example.isNotBlank()) {
                LessonSectionCard(
                    icon = Icons.Outlined.FactCheck,
                    title = tr("Worked example", "مثال محلول"),
                    isDark = isDark
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF16263B) else Color(0xFFF0F6FF))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MathRender(
                            value = lesson.example,
                            style = MaterialTheme.typography.titleLarge,
                            surfaceColor = Color.Transparent
                        )
                    }
                }
            }

            // 6. Explanation Card ("لماذا يصح ذلك؟")
            if (lesson.explanation.text().isNotBlank()) {
                LessonSectionCard(
                    icon = Icons.Outlined.Lightbulb,
                    title = tr("Why is this valid?", "لماذا يصح ذلك؟"),
                    isDark = isDark
                ) {
                    MathProse(
                        value = lesson.explanation.text(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = textSecondary
                        )
                    )
                }
            }

            // 7. Important Rules & Common Pitfalls Card ("قاعدة مهمة" & "خطأ شائع")
            if (lesson.rule.text().isNotBlank()) {
                LessonSectionCard(
                    icon = Icons.Outlined.Settings,
                    title = tr("Important rule", "قاعدة مهمة"),
                    isDark = isDark
                ) {
                    MathProse(
                        value = lesson.rule.text(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = textSecondary
                        )
                    )

                    // Common Pitfall / Warning Container ("خطأ شائع")
                    if (lesson.mistake.text().isNotBlank()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = if (isDark) Color(0xFF3B1820) else Color(0xFFFFF0F0),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF5C2430) else Color(0xFFFFD6D6))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFE11D48),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = tr("Common pitfall", "خطأ شائع"),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFFE11D48)
                                    )
                                }

                                MathProse(
                                    value = lesson.mistake.text(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 13.sp,
                                        color = if (isDark) Color(0xFFFECDD3) else Color(0xFF9F1239)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 8. Interactive Quiz / Checkpoint Card ("تحقّق من فهمك")
            if (lesson.checkpoint.isNotBlank()) {
                var answer by rememberSaveable(id) { mutableStateOf("") }
                var checked by rememberSaveable(id) { mutableStateOf(false) }
                val checkpointOwner by rememberSaveable(id, "gems-checkpoint-owner") {
                    mutableStateOf(if (answer.isBlank()) LearningRules.sessionOwner(state) else null)
                }
                val isCorrect = MathAnswer.matches(lesson.checkpointAnswer, answer)

                LessonSectionCard(
                    icon = Icons.Outlined.Quiz,
                    title = tr("Check your understanding", "تحقّق من فهمك"),
                    isDark = isDark
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDark) Color(0xFF16263B) else Color(0xFFF0F6FF))
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        MathRender(
                            value = lesson.checkpoint,
                            style = MaterialTheme.typography.headlineMedium,
                            surfaceColor = Color.Transparent
                        )
                    }

                    OutlinedTextField(
                        value = answer,
                        onValueChange = {
                            answer = it
                            checked = false
                        },
                        label = { Text(tr("Your answer", "إجابتك")) },
                        placeholder = { Text(tr("Your answer", "إجابتك")) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = borderStrokeColor,
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("checkpoint-input")
                    )

                    OutlinedButton(
                        onClick = {
                            checked = true
                            if (isCorrect) {
                                onVerified(answer, checkpointOwner)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, primaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("checkpoint-verify")
                    ) {
                        Text(
                            text = tr("Verify", "تحقّق"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = primaryBlue
                        )
                    }

                    if (checked) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isCorrect) {
                                if (isDark) Color(0xFF064E3B) else Color(0xFFECFDF5)
                            } else {
                                if (isDark) Color(0xFF451A03) else Color(0xFFFFF0F0)
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isCorrect) {
                                    if (isDark) Color(0xFF059669) else Color(0xFFA7F3D0)
                                } else {
                                    if (isDark) Color(0xFFB91C1C) else Color(0xFFFECDD3)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.Cancel,
                                    contentDescription = null,
                                    tint = if (isCorrect) Color(0xFF047857) else Color(0xFFE11D48),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isCorrect) tr("Correct answer!", "إجابة صحيحة!") else tr("Incorrect answer. Try direct substitution.", "إجابة غير صحيحة، حاول مجدداً."),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    ),
                                    color = if (isCorrect) {
                                        if (isDark) Color(0xFFA7F3D0) else Color(0xFF047857)
                                    } else {
                                        if (isDark) Color(0xFFFECDD3) else Color(0xFFB91C1C)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 9. Reflection Card ("توقف وفكّر")
            LessonSectionCard(
                icon = Icons.Outlined.Lightbulb,
                title = tr("Pause and think", "توقف وفكّر"),
                isDark = isDark
            ) {
                Text(
                    text = tr(
                        "Can you explain the rule without looking at the example? Try on paper before continuing.",
                        "هل يمكنك شرح القاعدة دون النظر إلى المثال؟ جرّب على الورق قبل المتابعة."
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    ),
                    color = textSecondary
                )
            }

            // 10. Bottom Action Buttons ("تحديد كمكتمل" + "تدرّب على المهارة" + "الدرس التالي")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isCompleted) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tr("Completed", "مكتمل"),
                            color = Color(0xFF059669),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }

                    // Disabled complete-lesson node for tests
                    Surface(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFF059669).copy(alpha = 0.12f),
                        contentColor = Color(0xFF059669),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .testTag("complete-lesson")
                    ) {}
                } else {
                    // Button 1: Mark as Completed
                    Surface(
                        onClick = { onComplete() },
                        enabled = true,
                        shape = RoundedCornerShape(26.dp),
                        color = primaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("complete-lesson")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = tr("Mark as completed", "تحديد كمكتمل"),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }
                }

                // Button 2: Practice Skill
                Surface(
                    onClick = { StudentCatalog.practiceForLesson(state, id)?.let { onPractice(it.id) } },
                    enabled = StudentCatalog.practiceForLesson(state, id) != null,
                    shape = RoundedCornerShape(26.dp),
                    color = electricBlue,
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("lesson-practice")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BarChart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = tr("Practice skill", "تدرّب على المهارة"),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }

                // Button 3: Optional Next Lesson
                val nextLesson = topic?.lessons?.dropWhile { it.id != id }?.drop(1)?.firstOrNull()
                if (nextLesson != null) {
                    OutlinedButton(
                        onClick = { onNext(nextLesson.id) },
                        shape = RoundedCornerShape(26.dp),
                        border = BorderStroke(1.5.dp, primaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("lesson-next")
                    ) {
                        Text(
                            text = tr("Next lesson", "الدرس التالي"),
                            color = primaryBlue,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/** Helper Card Container with Title on RTL start and Badge Icon on RTL end */
@Composable
private fun LessonSectionCard(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    isDark: Boolean = isDarkTheme(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardBg = MaterialTheme.colorScheme.surface
    val borderStrokeColor = MaterialTheme.colorScheme.outlineVariant
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val badgeBg = if (isDark) Color(0xFF1A304D) else Color(0xFFEDF5FF)
    val badgeTint = if (isDark) Color(0xFF3892FF) else Color(0xFF168BFF)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 1.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderStrokeColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = textPrimary
                )

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            content()
        }
    }
}
