package com.ahmadmol.enmath.features.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import java.text.SimpleDateFormat
import java.util.*

@Composable private fun plannerAccent() = if (LocalStudyStage.current == com.ahmadmol.enmath.core.model.StudyStage.G9)
    MaterialTheme.colorScheme.primary else Color(0xFF0066FF)

@OptIn(ExperimentalLayoutApi::class)
@Composable private fun PlannerPills(g9: Boolean, gap: androidx.compose.ui.unit.Dp, content: @Composable RowScope.() -> Unit) {
    if (g9 && LocalDensity.current.fontScale > 1.4f) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(8.dp), maxItemsInEachRow = 2) { content() }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(gap), content = content)
}

@Composable fun periodLabel(p: PlanPeriod) = when(p) { PlanPeriod.DAY -> tr("Daily", "يومي"); PlanPeriod.WEEK -> tr("Weekly", "أسبوعي"); PlanPeriod.MONTH -> tr("Monthly", "شهري") }
@Composable fun modeLabel(m: PlanMode) = when(m) { PlanMode.ACHIEVEMENT -> tr("Achievement", "إنجاز"); PlanMode.TIME -> tr("Time", "وقت"); PlanMode.HYBRID -> tr("Mixed", "مختلط") }
fun planDate(at: Long) = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { timeZone = TimeZone.getTimeZone(StudyPeriods.ZONE) }.format(Date(at))

@Composable fun PlanSummary(state: LearningState, repository: PlannerRepository?, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    val changed = repository?.changes?.collectAsState()?.value
    val result = remember(scope, state.product, changed) { scope?.let { repository?.read(it) } }
    AppCard(Modifier.testTag("plan-summary")) {
        Text(tr("This week's plan", "خطة هذا الأسبوع"), style = MaterialTheme.typography.titleMedium)
        when (result) {
            is PlanRead.Ready -> {
                val start = StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK)
                val tasks = planTasksInView(result.plan, PlanPeriod.WEEK, start).filterNot { it.closed }
                val achieved = tasks.count { repository!!.measured(it, state).isAchieved() }
                if (tasks.isNotEmpty()) {
                    Text(tr("$achieved/${tasks.size} goals achieved", "$achieved/${tasks.size} أهداف منجزة"))
                    LinearProgressIndicator(progress = { achieved.toFloat() / tasks.size }, modifier = Modifier.fillMaxWidth())
                }
                if (tasks.isEmpty()) Text(tr("Set goals for any school subject, at your own pace.", "حدّد أهدافًا لأي مادة دراسية حسب وتيرتك."))
                else tasks.take(3).forEach { t -> val measured = repository!!.measured(t, state)
                    Text("${t.title}: ${if(t.mode == PlanMode.TIME) tr("${measured.spent}/${t.minutes} minutes", "${measured.spent}/${t.minutes} دقيقة") else tr("${measured.done}/${t.target} units", "${measured.done}/${t.target} وحدة")}")
                    if (t.mode == PlanMode.HYBRID) Text(tr("${measured.spent}/${t.minutes} minutes", "${measured.spent}/${t.minutes} دقيقة"))
                }
            }
            PlanRead.StorageError -> Text(tr("Unable to read the plan. Previous data is preserved.", "تعذّر قراءة الخطة. البيانات السابقة محفوظة."))
            else -> Text(tr("Choose a local file to use planning.", "اختر ملفًا محليًا لاستخدام الخطة."))
        }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("My study plan", "خطتي الدراسية")) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
/** initialRead supplies deterministic memory-only preview state; live callers load the repository. */
@Composable fun PlannerScreen(state: LearningState, repository: PlannerRepository, initialRead: PlanRead? = null, open: (String) -> Unit = {}) {
    val scope = remember(state.user?.localId, state.generation) { StudentCatalog.scope(state) }
    var reload by remember { mutableIntStateOf(0) }
    var read by remember(scope) { mutableStateOf<PlanRead?>(initialRead) }
    LaunchedEffect(scope, reload) { read = scope?.let { repository.read(it) } ?: PlanRead.OwnerChanged }
    var period by rememberSaveable(scope) { mutableStateOf(PlanPeriod.WEEK) }
    var at by rememberSaveable(scope) { mutableLongStateOf(System.currentTimeMillis()) }
    var editing by remember(scope) { mutableStateOf<PlanTask?>(null) }
    var editorRevision by remember(scope) { mutableIntStateOf(0) }
    var form by remember(scope) { mutableStateOf(false) }
    var action by remember(scope) { mutableStateOf<Pair<String, PlanTask>?>(null) }
    var message by remember(scope) { mutableStateOf<PlanWrite?>(null) }
    var undoConfirm by remember(scope) { mutableStateOf(false) }
    var historyCount by rememberSaveable(scope) { mutableIntStateOf(0) }
    val plan = (read as? PlanRead.Ready)?.plan
    fun save(result: PlanWrite) { message = result; reload++ }
    val g9 = LocalStudyStage.current == com.ahmadmol.enmath.core.model.StudyStage.G9
    ScreenPage(Modifier.then(if (g9) Modifier.widthIn(max = 640.dp) else Modifier).imePadding().testTag("screen-planner")) {
        if (g9) {
            Text(tr("My study plan", "خطتي الدراسية"), style = MaterialTheme.typography.headlineSmall)
            Text(tr("Choose a small weekly goal. Manual plan progress is separate from learning results.", "اختر هدفًا أسبوعيًا صغيرًا. تقدم الخطة اليدوي مستقل عن نتائج التعلّم."), color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else StageHero(
            stage = state.user?.studyStage ?: com.ahmadmol.enmath.core.model.StudyStage.BAC,
            title = tr("My study plan", "خطتي الدراسية"),
            subtitle = tr("Achievement at your pace · Damascus time", "إنجاز حسب وتيرتك · توقيت دمشق")
        )

        // Info notice card matching Image 2
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (g9) MaterialTheme.colorScheme.primaryContainer else if (isDarkTheme()) Color(0xFF132238) else Color(0xFFEDF5FD)
            ),
            border = BorderStroke(1.dp, if (isDarkTheme()) Color(0xFF223A5E) else Color(0xFFD0E4FA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = tr(
                        "Manual achievements do not award gems or establish mastery. Time and achievement are measured separately.",
                        "الإنجاز اليدوي لا يمنح جواهر ولا يثبت الإتقان. يقاس الوقت والإنجاز بصورة منفصلة."
                    ),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 18.sp),
                    color = if (g9) MaterialTheme.colorScheme.onPrimaryContainer else if (isDarkTheme()) Color(0xFFB0CDED) else Color(0xFF1E3A8A),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = if (isDarkTheme()) Color(0xFF60A5FA) else plannerAccent(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Period Selection Pills: [شهري, أسبوعي, يومي] (Image 2)
        PlannerPills(g9, 10.dp) {
            listOf(PlanPeriod.MONTH, PlanPeriod.WEEK, PlanPeriod.DAY).forEach { p ->
                val selected = period == p
                Surface(
                    onClick = { period = p },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected && g9) MaterialTheme.colorScheme.primaryContainer else if (selected) Color(0xFFE2EFFF) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) plannerAccent() else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(44.dp))
                        .testTag("plan-period-${p.name}")
                ) {
                    Box(Modifier.then(if (g9) Modifier.padding(8.dp) else Modifier), contentAlignment = Alignment.Center) {
                        Text(
                            text = periodLabel(p),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected && g9) MaterialTheme.colorScheme.onPrimaryContainer else if (selected) plannerAccent() else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        val start = StudyPeriods.start(at, period)

        // Date Range Navigator: [< 2026-10-26 — 2026-11-01 >]
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { at = StudyPeriods.next(start, period) },
                    modifier = Modifier.size(if (g9) 48.dp else 36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = tr("Next", "التالية"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${planDate(start)} — ${planDate(StudyPeriods.next(start, period) - 1)}",
                    modifier = if (g9) Modifier.weight(1f) else Modifier,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { at = start - 1 },
                    modifier = Modifier.size(if (g9) 48.dp else 36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = tr("Previous", "السابقة"),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Period filter pills: [السابقة, الحالية, الفترة القادمة]
        PlannerPills(g9, 8.dp) {
            Surface(
                onClick = { at = StudyPeriods.next(start, period) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(40.dp))
            ) {
                Box(Modifier.then(if (g9) Modifier.padding(8.dp) else Modifier), contentAlignment = Alignment.Center) {
                    Text(tr("Next period", "الفترة القادمة"), style = MaterialTheme.typography.bodySmall)
                }
            }
            Surface(
                onClick = { at = System.currentTimeMillis() },
                shape = RoundedCornerShape(14.dp),
                color = if (g9) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE2EFFF),
                border = BorderStroke(1.dp, plannerAccent()),
                modifier = Modifier.weight(1f).then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(40.dp))
            ) {
                Box(Modifier.then(if (g9) Modifier.padding(8.dp) else Modifier), contentAlignment = Alignment.Center) {
                    Text(tr("Current", "الحالية"), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (g9) MaterialTheme.colorScheme.onPrimaryContainer else plannerAccent())
                }
            }
            Surface(
                onClick = { at = start - 1 },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(40.dp))
            ) {
                Box(Modifier.then(if (g9) Modifier.padding(8.dp) else Modifier), contentAlignment = Alignment.Center) {
                    Text(tr("Previous", "السابقة"), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        when (read) {
            null -> CircularProgressIndicator()
            PlanRead.StorageError -> {
                Text(tr("Storage error. Nothing was replaced. Restart if saving failed.", "خطأ تخزين. لم تُستبدل البيانات. أعد التشغيل إذا فشل الحفظ."), color = MaterialTheme.colorScheme.error)
                OutlinedButton({ reload++ }) { Text(tr("Retry reading", "إعادة القراءة")) }
            }
            PlanRead.OwnerChanged -> Text(tr("The active file changed. Reopen planning.", "تغيّر الملف النشط. أعد فتح الخطة."))
            is PlanRead.Ready -> {
                // Big Blue Button: "+ إضافة هدف" (Image 2)
                Button(
                    onClick = { editing = null; editorRevision = plan!!.revision; form = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = plannerAccent()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (g9) Modifier.heightIn(min = 52.dp) else Modifier.height(52.dp))
                        .testTag("plan-add")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircle,
                            contentDescription = null,
                            tint = if (g9) MaterialTheme.colorScheme.onPrimary else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = tr("Add a goal", "إضافة هدف"),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (g9) MaterialTheme.colorScheme.onPrimary else Color.White
                        )
                    }
                }

                val tasks = planTasksInView(plan!!, period, at).sortedBy { it.subject }
                val active = tasks.filterNot { it.closed }.map { repository.measured(it, state) }
                val completed = active.count { it.isAchieved() }

                // 3 Stat Cards Row: أهداف مستهدفة, أُنجزت, متبقية (Image 2)
                SourceMetrics(listOf(
                    "${active.size}" to tr("Target goals", "أهداف مستهدفة"),
                    "$completed" to tr("Achieved", "أُنجزت"),
                    "${active.size - completed}" to tr("Remaining", "متبقية")
                ))

                if (active.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / active.size },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = plannerAccent(),
                        trackColor = plannerAccent().copy(alpha = 0.15f)
                    )
                }

                if (tasks.isEmpty()) {
                    SourcePendingCard(
                        title = tr("Your next step starts with a goal", "خطوتك التالية تبدأ بهدف"),
                        detail = tr("Add a goal for any school subject. Choose a source reference when useful.", "أضف هدفًا لأي مادة دراسية، واربطه بمرجع من المصدر عند الحاجة.")
                    )
                }

                tasks.forEachIndexed { index, original ->
                    val t = repository.measured(original, state)
                    if (index == 0 || tasks[index - 1].subject != t.subject) {
                        SectionTitle(t.subject)
                    }

                    // Task Card matching Image 1
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("plan-task-${t.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header: Title and subject icon
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = t.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 17.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (g9) MaterialTheme.colorScheme.primaryContainer else Color(0xFFE8F1FC),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoGraph,
                                            contentDescription = null,
                                            tint = plannerAccent(),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            if (g9) TextButton({open(com.ahmadmol.enmath.features.student.g9GoalRoute(t.id))},Modifier.heightIn(min=48.dp)) {Text(tr("Goal details and changes", "تفاصيل الهدف وتغييراته"))}
                            // Meta row: [calendar] أسبوعي | [target] 0/1 | [document] إدخال يدوي
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.CalendarToday, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(periodLabel(t.period), style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.TrackChanges, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text("الإنجاز: ${t.done}/${t.target}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Outlined.Description, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        if (t.source == "LEARNING_EVENT") "المصدر: إكمال درس" else "المصدر: إدخال يدوي",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // Subtitle reference note
                            t.lesson?.let { ref ->
                                val refText = if (ref.kind == ContentKind.SOURCE_INDEX && StudentCatalog.acceptsReference(state, ref))
                                    StudentCatalog.sourceTitle(ref).orEmpty() + " • مرجع قيد المراجعة"
                                else state.lesson(ref.lessonId)?.title?.text() ?: tr("Linked content unavailable", "المحتوى المرتبط غير متاح")
                                Text(
                                    text = refText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                            // Action buttons matching Image 1
                            if (t.closed) {
                                Text(tr("Remaining work moved to another period", "نُقل المتبقي إلى فترة أخرى"))
                            } else {
                                // 1. Solid Blue Button: تسجيل إنجاز الهدف
                                if (!t.isAchieved()) {
                                    Button(
                                        onClick = { action = "complete" to original },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = plannerAccent()),
                                        modifier = Modifier.fillMaxWidth().then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(48.dp))
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Filled.CheckCircle, null, Modifier.size(18.dp), tint = if (g9) MaterialTheme.colorScheme.onPrimary else Color.White)
                                            Text(tr("Mark goal achieved", "تسجيل إنجاز الهدف"), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                // 2. Outlined Button: تسجيل إنجاز جزئي
                                OutlinedButton(
                                    onClick = { action = "progress" to original },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, plannerAccent().copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(48.dp))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Outlined.Schedule, null, Modifier.size(18.dp), tint = plannerAccent())
                                        Text(tr("Record partial progress", "تسجيل إنجاز جزئي"), color = plannerAccent())
                                    }
                                }

                                // 3. Outlined Button: تعديل الهدف
                                OutlinedButton(
                                    onClick = { editing = original; editorRevision = plan!!.revision; form = true },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, plannerAccent().copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(48.dp))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Outlined.Edit, null, Modifier.size(18.dp), tint = plannerAccent())
                                        Text(tr("Edit goal", "تعديل الهدف"), color = plannerAccent())
                                    }
                                }

                                // 4. Outlined Button: نقل المتبقي للفترة التالية
                                OutlinedButton(
                                    onClick = { action = "carry" to original },
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, plannerAccent().copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth().then(if (g9) Modifier.heightIn(min = 48.dp) else Modifier.height(48.dp))
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Outlined.FastForward, null, Modifier.size(18.dp), tint = plannerAccent())
                                        Text(tr("Move remainder to next period", "نقل المتبقي للفترة التالية"), color = plannerAccent())
                                    }
                                }

                                // 5. Red Delete Button: حذف الهدف
                                TextButton(
                                    onClick = { action = "delete" to original },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp), tint = Color(0xFFEF4444))
                                        Text(tr("Delete goal", "حذف الهدف"), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                if (plan.history.isNotEmpty() && plan.history.last().action != "UNDO") OutlinedButton({ undoConfirm = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plan-undo")) { Text(tr("Undo last change", "تراجع عن آخر تغيير")) }
                TextButton({ historyCount = if (historyCount == 0) 10 else 0 }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Edit history and previous plans", "سجل التعديلات والخطط السابقة")) }
                if (plan.history.isEmpty()) Text(tr("No edits yet", "لا تعديلات بعد"))
                plan.history.asReversed().take(historyCount).forEach { r ->
                    Text("${planDate(r.at)} · ${when(r.action.substringBefore(':')) {
                        "SAVE" -> tr("Goal edited", "تعديل هدف"); "DELETE" -> tr("Goal deleted", "حذف هدف")
                        "PIN_UNIT" -> tr("Unit references added", "إضافة مراجع الوحدة"); "CARRY" -> tr("Remaining work moved", "نقل المتبقي"); "UNDO" -> tr("Change undone", "تراجع عن تغيير"); else -> tr("Manual progress", "إنجاز يدوي")
                    }} · ${r.tasks.size}")
                    r.tasks.forEach { t -> Text("${t.subject}: ${t.title} · ${t.done}/${t.target} · ${t.spent}/${t.minutes}", style = MaterialTheme.typography.bodySmall) }
                }
                if (historyCount > 0 && plan.history.size > historyCount) TextButton({ historyCount += 10 }) { Text(tr("Show older changes", "عرض تعديلات أقدم")) }
            }
        }
        message?.let { Text(when(it) {
            PlanWrite.SAVED, PlanWrite.ALREADY_SAVED -> tr("Saved on this device", "حُفظت على هذا الجهاز")
            PlanWrite.OWNER_CHANGED -> tr("File changed; operation rejected", "تغيّر الملف؛ رُفضت العملية")
            PlanWrite.CONFLICT -> tr("Plan changed. Reload before editing again.", "تغيّرت الخطة. أعد تحميلها قبل التعديل.")
            PlanWrite.INVALID -> tr("Check the goal, quantities, and linked content.", "تحقق من الهدف والكميات والمحتوى المرتبط.")
            else -> tr("Save unconfirmed. Previous data is preserved. Restart before retrying.", "لم يُؤكَّد الحفظ. البيانات السابقة محفوظة. أعد التشغيل قبل المحاولة.")
        }) }
    }
    if (form && scope != null && plan != null) GoalEditor(state, editing, PlanPeriod.WEEK, at,
        { form = false }, { task, command -> val result = repository.save(scope, editorRevision, command, task); save(result)
            if (result in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) form = false; result })
    action?.let { (kind, task) ->
        var count by remember(task.id, kind) { mutableStateOf("") }
        var minutes by remember(task.id, kind) { mutableStateOf("") }
        val command = remember(task.id, kind) { UUID.randomUUID().toString() }
        AlertDialog(onDismissRequest = { action = null },
            title = { Text(when(kind) { "progress" -> tr("Manual progress", "إنجاز يدوي"); "complete" -> tr("Record goal achievement?", "تسجيل إنجاز الهدف؟"); "carry" -> tr("Confirm moving remaining work", "تأكيد نقل المتبقي"); else -> tr("Delete goal?", "حذف الهدف؟") }) },
            text = { Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(task.title)
                if (kind == "progress") {
                    if(task.mode != PlanMode.TIME) OutlinedTextField(count, { count = it }, label = { Text(tr("Additional units", "وحدات إنجاز إضافية")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    if(task.mode != PlanMode.ACHIEVEMENT) OutlinedTextField(minutes, { minutes = it }, label = { Text(tr("Additional minutes", "دقائق إضافية")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                } else Text(when(kind) { "carry" -> tr("Only the remainder moves. The original goal stays in history.", "يُنقل المتبقي فقط. يبقى الهدف الأصلي في السجل.");
                    "complete" -> tr("This is your manual plan record. It does not complete a lesson, certify mastery or grant gems.", "هذا سجل يدوي للخطة؛ لا يكمل درسًا ولا يثبت الإتقان ولا يمنح جواهر.");
                    else -> tr("The goal is retained in edit history.", "يبقى الهدف محفوظًا في سجل التعديلات.") })
            } }, confirmButton = { TextButton({
                if (scope != null && plan != null) save(when(kind) {
                    "progress" -> repository.progress(scope, plan.revision, command, task.id, if (count.isBlank()) 0 else count.toIntOrNull() ?: -1, if (minutes.isBlank()) 0 else minutes.toIntOrNull() ?: -1)
                    "complete" -> repository.progress(scope, plan.revision, command, task.id, if (task.mode == PlanMode.TIME) 0 else (task.target-task.done).coerceAtLeast(0), if (task.mode == PlanMode.ACHIEVEMENT) 0 else (task.minutes-task.spent).coerceAtLeast(0))
                    "carry" -> repository.carry(scope, plan.revision, command, task.id, StudyPeriods.next(task.start, task.period), true)
                    else -> repository.remove(scope, plan.revision, command, task.id)
                }); action = null
            }) { Text(tr("Confirm", "تأكيد")) } }, dismissButton = { TextButton({ action = null }) { Text(tr("Cancel", "إلغاء")) } })
    }
    if (undoConfirm && scope != null && plan != null) {
        val command = remember(scope, plan.revision) { UUID.randomUUID().toString() }
        AlertDialog(onDismissRequest = { undoConfirm = false }, title = { Text(tr("Undo last change?", "التراجع عن آخر تغيير؟")) },
            text = { Text(tr("Restore the previous goal state. Both versions remain in history.", "استعادة حالة الأهداف السابقة مع الاحتفاظ بالنسختين في السجل.")) },
            confirmButton = { TextButton({ save(repository.undo(scope, plan.revision, command)); undoConfirm = false }) { Text(tr("Confirm", "تأكيد")) } },
            dismissButton = { TextButton({ undoConfirm = false }) { Text(tr("Cancel", "إلغاء")) } })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable internal fun GoalEditor(state: LearningState, old: PlanTask?, period: PlanPeriod, start: Long,
    close: () -> Unit, save: (PlanTask, String) -> PlanWrite) {
    var failure by remember { mutableStateOf<PlanWrite?>(null) }
    val id = remember { old?.id ?: UUID.randomUUID().toString() }
    var title by remember { mutableStateOf(old?.title.orEmpty()) }
    var subject by remember { mutableStateOf(old?.subject.orEmpty()) }
    var target by remember { mutableStateOf(old?.target?.toString() ?: "1") }
    var minutes by remember { mutableStateOf(old?.minutes?.toString() ?: "30") }
    var mode by remember { mutableStateOf(old?.mode ?: PlanMode.ACHIEVEMENT) }
    var taskPeriod by remember { mutableStateOf(old?.period ?: period) }
    var linked by remember { mutableStateOf(old?.lesson) }
    // The same unchanged payload retries with the same command. Edits receive a new identity.
    val command = remember(title, subject, target, minutes, mode, linked, taskPeriod) { UUID.randomUUID().toString() }
    androidx.compose.ui.window.Dialog(onDismissRequest = close) {
        Surface(Modifier.fillMaxWidth().imePadding(), shape = MaterialTheme.shapes.large) {
            ScreenPage(narrow = true) {
                Text(tr("Study goal", "هدف الدراسة"), style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(title, { title = it.take(200) }, label = { Text(tr("Goal", "الهدف")) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(subject, { subject = it.take(100) }, label = { Text(tr("Any school subject", "أي مادة دراسية")) }, modifier = Modifier.fillMaxWidth())
                Text(tr("Goal period", "فترة الهدف"), style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanPeriod.entries.forEach { p -> FilterChip(taskPeriod == p, { taskPeriod = p }, enabled = old == null, label = { Text(periodLabel(p)) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlanMode.entries.forEach { m -> FilterChip(mode == m, { mode = m }, label = { Text(modeLabel(m)) }, modifier = Modifier.heightIn(min = 48.dp)) }
                }
                if (mode != PlanMode.TIME) OutlinedTextField(target, { target = it }, label = { Text(tr("Target units", "عدد الوحدات المستهدف")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                if (mode != PlanMode.ACHIEVEMENT) OutlinedTextField(minutes, { minutes = it }, label = { Text(tr("Target minutes", "الدقائق المستهدفة")) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text(periodLabel(taskPeriod) + " · " + planDate(old?.start ?: StudyPeriods.start(start, taskPeriod)))
                SourceReferencePicker(state, linked) { ref ->
                    linked = ref
                    if (ref != null) {
                        if (subject.isBlank()) subject = "Mathematics / الرياضيات"
                        if (title.isBlank()) title = if (ref.kind == ContentKind.SOURCE_INDEX) StudentCatalog.sourceTitle(ref).orEmpty()
                            else state.lesson(ref.lessonId)?.title?.let { if (state.language == com.ahmadmol.enmath.core.model.Language.Arabic) it.ar else it.en }.orEmpty()
                    }
                }
                PrimaryButton(tr("Save goal", "حفظ الهدف"), {
                    failure = save(PlanTask(id, title.trim(), subject.trim(), taskPeriod, old?.start ?: StudyPeriods.start(start, taskPeriod), mode,
                        if (mode == PlanMode.TIME) 0 else target.toIntOrNull() ?: -1,
                        if (mode == PlanMode.ACHIEVEMENT) 0 else minutes.toIntOrNull() ?: -1,
                        old?.done ?: 0, old?.spent ?: 0, linked, "MANUAL", old?.parent, old?.closed ?: false), command)
                }, enabled = title.isNotBlank() && subject.isNotBlank())
                failure?.takeIf { it !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED) }?.let {
                    Text(if(it == PlanWrite.INVALID) tr("Check positive quantities and the lesson link.", "تحقق من الكميات الموجبة وربط الدرس.") else tr("Save unconfirmed. Close this form and reload; restart after a storage error.", "لم يُؤكَّد الحفظ. أغلق النموذج وأعد التحميل؛ أعد التشغيل بعد خطأ التخزين."), color = MaterialTheme.colorScheme.error)
                }
                TextButton(close) { Text(tr("Cancel", "إلغاء")) }
            }
        }
    }
}
