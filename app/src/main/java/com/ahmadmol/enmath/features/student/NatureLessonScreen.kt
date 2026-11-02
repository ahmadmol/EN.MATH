package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.navigation.Routes

/**
 * Educational Lesson Screen for Syrian Grade 9 Algebra: Unit 1, Lesson 1 (Nature of Numbers).
 *
 * Provides:
 * 1. Clear conceptual exposition of number sets (N, Z, D, Q, and irrational numbers).
 * 2. Pedagogical golden rule emphasizing actual numerical value over outward appearance (e.g. √25 = 5).
 * 3. Step-by-step worked example with progressive step reveal.
 * 4. Interactive self-check with immediate feedback and explanation.
 * 5. Direct, seamless integration to the 6-question interactive practice session and student progress.
 */
@Composable
fun NatureLessonScreen(
    state: LearningState,
    repository: NaturePracticeRepository?,
    open: (String) -> Unit,
    goBack: () -> Unit = { open(Routes.Home) }
) {
    val context = LocalContext.current
    val debuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val scope = StudentCatalog.scope(state)
    val natureChange = repository?.changes?.collectAsState()?.value
    val natureRead = remember(scope, natureChange) { scope?.let { repository?.read(it) } }
    val natureData = (natureRead as? NatureRead.Ready)?.data ?: NaturePracticeData()
    val progress = remember(natureData) {
        StudentLearningProgressEngine.computeGrade9Progress(natureData)
    }

    var revealedSteps by rememberSaveable { mutableIntStateOf(1) }
    var selectedOptionIndex by rememberSaveable { mutableIntStateOf(-1) }
    var selfCheckVerified by rememberSaveable { mutableStateOf(false) }

    val tealAccent = EntryTeal
    val dark = isDarkTheme()
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen-g9-nature-lesson")
            .verticalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Navigation & Stage Breadcrumb
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = goBack,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("g9-nature-lesson-back"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(tr("Learning path", "المسار الدراسي"))
                }
            }

            Surface(
                shape = CircleShape,
                color = tealAccent.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.35f))
            ) {
                Text(
                    text = tr("Grade 9 · Algebra", "الصف التاسع · الجبر"),
                    color = tealAccent,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        // 2. Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.35f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(tealAccent.copy(alpha = 0.12f), MaterialTheme.colorScheme.surface)
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = Grade9NatureLesson.title.text(),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = tealAccent,
                        modifier = Modifier.testTag("g9-nature-lesson-title")
                    )
                    Text(
                        text = tr("Unit 1: Numbers and Fractions · Lesson 1", "الوحدة الأولى: الأعداد والكسور · الدرس 1"),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = Grade9NatureLesson.sourceEdition.text(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 3. Clear Educational Status Notice
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = tealAccent, modifier = Modifier.size(22.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = tr("Educational draft · review required", "مسودة تعليمية · تحتاج مراجعة"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = tr(
                            "Linked to the 2025–2026 source index. Independent educational approval has not been recorded.",
                            "مرتبطة بفهرس المصدر لطبعة 2025–2026. لم يُسجّل اعتماد تربوي مستقل لهذا الشرح."
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Learning Goal / What will I learn?
        val firstBlock = Grade9NatureLesson.blocks.first()
        G9LearningCard(firstBlock.title.text()) {
            MathProse(firstBlock.body.text())
        }

        // 5. Concept Blocks: Number Sets
        Text(
            text = tr("Core Concepts & Number Sets", "المفاهيم الأساسية ومجموعات الأعداد"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        for (i in 1..4) {
            val block = Grade9NatureLesson.blocks[i]
            G9LearningCard(block.title.text()) {
                MathProse(block.body.text())
                if (block.formula.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    G9Formula(block.formula)
                }
            }
        }

        // 6. Pedagogical Golden Rule: Value over appearance
        val goldenRule = Grade9NatureLesson.blocks[5]
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = if (dark) Color(0xFF1E2838) else Color(0xFFF0FDF4)
            ),
            border = BorderStroke(1.5.dp, if (dark) Color(0xFF34D399) else Color(0xFF059669))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = if (dark) Color(0xFF34D399) else Color(0xFF059669),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = goldenRule.title.text(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (dark) Color(0xFF34D399) else Color(0xFF059669)
                    )
                }
                MathProse(goldenRule.body.text())
                if (goldenRule.formula.isNotBlank()) {
                    G9Formula(goldenRule.formula)
                }
            }
        }

        // 7. Step-by-Step Worked Example
        val example = Grade9NatureLesson.workedExample
        Text(
            text = tr("Worked Example Step-by-Step", "مثال محلول خطوة بخطوة"),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        G9LearningCard(tr("Problem Prompt", "نص المسألة")) {
            MathProse(example.prompt.text())
        }

        for (stepIdx in 0 until revealedSteps.coerceAtMost(example.steps.size)) {
            val step = example.steps[stepIdx]
            G9LearningCard(step.title.text()) {
                MathProse(step.body.text())
                if (step.formula.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    G9Formula(step.formula)
                }
            }
        }

        if (revealedSteps < example.steps.size) {
            OutlinedButton(
                onClick = { revealedSteps++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("g9-nature-reveal-step"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(tr("Show next step (${revealedSteps + 1} of ${example.steps.size})", "أظهر الخطوة التالية (${revealedSteps + 1} من ${example.steps.size})"))
                }
            }
        }

        // 8. Self-Check (Check your understanding)
        val check = Grade9NatureLesson.selfCheck
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, tealAccent.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.Quiz, contentDescription = null, tint = tealAccent, modifier = Modifier.size(22.dp))
                    Text(
                        text = tr("Check your understanding", "تحقّق من فهمك"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tealAccent
                    )
                }
                Text(
                    text = check.prompt.text(),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    check.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOptionIndex == idx
                        val isCorrect = idx == check.correctIndex
                        OutlinedButton(
                            onClick = {
                                selectedOptionIndex = idx
                                selfCheckVerified = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("g9-nature-selfcheck-opt-$idx"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = when {
                                    selfCheckVerified && isCorrect -> Color(0xFF10B981).copy(alpha = 0.18f)
                                    selfCheckVerified && isSelected -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = when {
                                    selfCheckVerified && isCorrect -> Color(0xFF10B981)
                                    selfCheckVerified && isSelected -> MaterialTheme.colorScheme.error
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Text(
                                text = opt.text(),
                                style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr)
                            )
                        }
                    }
                }

                if (!selfCheckVerified) {
                    Button(
                        onClick = { selfCheckVerified = true },
                        enabled = selectedOptionIndex >= 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("g9-nature-check-btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = tealAccent)
                    ) {
                        Text(tr("Check answer", "تحقق من الإجابة"))
                    }
                } else {
                    val correct = selectedOptionIndex == check.correctIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (correct) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth().testTag("g9-nature-selfcheck-feedback")
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (correct) tr("Correct!", "أحسنت! إجابة صحيحة.") else tr("Not quite.", "تحتاج إلى مراجعة."),
                                fontWeight = FontWeight.Bold,
                                color = if (correct) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = check.explanation.text(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        // 9. Summary Card
        G9LearningCard(tr("Summary & Takeaways", "خلاصة الدرس")) {
            MathProse(Grade9NatureLesson.summary.text())
        }

        // 10. Interactive Practice Connection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, tealAccent.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.FitnessCenter, contentDescription = null, tint = tealAccent, modifier = Modifier.size(24.dp))
                    Text(
                        text = tr("Interactive Training & Practice", "التدريب التفاعلي والتطبيق"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tealAccent
                    )
                }

                Text(
                    text = if (!debuggable) tr("The six-question demo is unavailable in this build. Check exercise availability for this reference.",
                        "التدريب التجريبي ذو الأسئلة الستة غير متاح في هذه النسخة. يمكنك مراجعة حالة تدريبات هذا المرجع.")
                    else if (natureRead !is NatureRead.Ready) tr("Saved practice progress could not be read. Restart before retrying; your data is preserved.",
                        "تعذّرت قراءة تقدم التدريب المحفوظ. أعد التشغيل قبل المحاولة؛ بياناتك محفوظة.") else when (progress.natureLessonStatus) {
                        LessonProgressStatus.PRACTICE_COMPLETED -> tr(
                            "You have completed the 6 practice activities! You can re-practice to sharpen your skills.",
                            "أكملت الأنشطة الـ6 بنجاح! يمكنك إعادة التدريب في أي وقت لتثبيت المهارات."
                        )
                        LessonProgressStatus.NEEDS_REVIEW -> tr(
                            "Practice completed with some retries. Reviewing explanations and re-practicing is recommended.",
                            "أكملت التدريب مع بعض الأخطاء. يُنصح بمراجعة التفسيرات وإعادة التدريب لترسيخ الفهم."
                        )
                        LessonProgressStatus.STARTED -> tr(
                            "Your practice draft is in progress (${progress.availablePracticeProgressText.text()}). You can continue right where you left off.",
                            "لديك تدريب قيد التقدم (${progress.availablePracticeProgressText.text()}). يمكنك المتابعة من حيث توقفت."
                        )
                        else -> tr(
                            "6 original graded activities designed for Grade 9 algebra (single choice, numeric input, true/false, fill-in, error correction, group classification).",
                            "6 أنشطة تدريبية متدرجة مصممة لجبر التاسع (اختيار، إدخال كسر، صح أو خطأ، إكمال فراغ، تصحيح خطأ، تصنيف مجموعات)."
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (progress.completedSessionsCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${progress.completedSessionsCount}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tealAccent
                                )
                                Text(tr("Completed sessions", "جلسات مكتملة"), style = MaterialTheme.typography.labelSmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = progress.averageFirstAttemptAccuracy?.let { "${it.toInt()}%" } ?: "—",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(tr("First-try accuracy", "دقة أول محاولة"), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        open(
                            if (debuggable) NaturePracticeGrader.ROUTE
                            else "practice/${Grade9NatureLesson.LESSON_ID}"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("g9-nature-start-practice"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = tealAccent),
                    enabled = !debuggable || natureRead is NatureRead.Ready
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (!debuggable) tr("Exercise availability", "حالة التدريبات")
                            else when (progress.natureLessonStatus) {
                                LessonProgressStatus.PRACTICE_COMPLETED -> tr("Practice again (6 activities)", "تدرّب من جديد (6 أنشطة)")
                                LessonProgressStatus.STARTED -> tr("Resume practice session", "أكمل جلسة التدريب")
                                else -> tr("Start interactive practice (6 activities)", "ابدأ التدريب التفاعلي (6 أنشطة)")
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}
