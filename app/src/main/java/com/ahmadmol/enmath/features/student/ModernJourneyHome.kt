package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.PlanSummary
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@Composable fun rememberModernStudent(state: LearningState, repository: ModernStudentRepository?): ModernRead? {
    val scope = StudentCatalog.scope(state)
    val changed = repository?.changes?.collectAsState()?.value
    return remember(scope, changed, repository) { scope?.let { repository?.read(it) } }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable fun ModernJourneyHome(state: LearningState, open: (String) -> Unit,
    gems: LocalGemsUiState, repository: LocalDemoRepository?) {
    val catalog = SourceCurricula.forState(state) ?: return
    val scope = StudentCatalog.scope(state)
    val store = repository?.studentExperience
    val read = rememberModernStudent(state, store)
    val student = (read as? ModernRead.Ready)?.data
    val g9Change = repository?.grade9Study?.changes?.collectAsState()?.value
    val natureChange = repository?.naturePractice?.changes?.collectAsState()?.value
    val g9Read = remember(scope, g9Change) { scope?.takeIf { it.stage == StudyStage.G9 }?.let { repository?.grade9Study?.read(it) } }
    val natureRead = remember(scope, natureChange) { scope?.takeIf { it.stage == StudyStage.G9 }?.let { repository?.naturePractice?.read(it) } }
    val g9 = (g9Read as? G9StudyRead.Ready)?.data
    val nature = (natureRead as? NatureRead.Ready)?.data
    val workspaceChange = repository?.sourceWorkspace?.changes?.collectAsState()?.value
    val workspaceRead = remember(scope, workspaceChange) { scope?.let { repository?.sourceWorkspace?.read(it) } }
    val workspace = (workspaceRead as? SourceWorkspaceRead.Ready)?.workspace
    val context = LocalContext.current
    val energy = remember { StudentEnergy() }
    val bank = remember(context) { OriginalDraftBank.loadQa(context) }
    val list = key(scope?.ownerId) { rememberLazyListState() }
    val jobs = rememberCoroutineScope()
    var picker by rememberSaveable(scope?.ownerId) { mutableStateOf(false) }
    var selectedBook by rememberSaveable(scope?.ownerId) { mutableStateOf(catalog.books.first().id) }
    var selectedLesson by rememberSaveable(scope?.ownerId) { mutableStateOf<String?>(null) }
    var restored by rememberSaveable(scope?.ownerId) { mutableStateOf(false) }
    var saveError by remember(scope) { mutableStateOf(false) }
    val pickerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lessonSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(scope, state.product.attempts, g9Change) { if (scope != null) store?.refreshLearningDays(scope) }
    LaunchedEffect(scope, student?.resumeLesson) {
        if (!restored && student != null) {
            restored = true
            student.resumeLesson?.let { id ->
                val unit = catalog.research(id)?.unitId
                val base = unit?.let { JourneyLayout.unitIndex(catalog, it) }
                val offset = catalog.unit(unit.orEmpty())?.researches?.indexOfFirst { it.id == id }
                if (base != null && offset != null && offset >= 0) list.scrollToItem(base + 1 + offset)
            }
        }
    }
    LaunchedEffect(scope, list) {
        if (scope != null && store != null) snapshotFlow {
            list.layoutInfo.visibleItemsInfo.firstNotNullOfOrNull { item ->
                (item.key as? String)?.takeIf { it.startsWith("station:") }?.removePrefix("station:")
            }
        }.filterNotNull().distinctUntilChanged().collect { id ->
            if (store.rememberStation(scope, id) !in setOf(ModernWrite.SAVED, ModernWrite.ALREADY_SAVED)) saveError = true
        }
    }
    val completed = if (catalog.stage == StudyStage.G9) g9?.attempts?.size else StudentCatalog.attempts(state).size
    val qaCompleted = student?.results?.size
    val nextRoute = when {
        catalog.stage == StudyStage.G9 && nature?.draft != null && bank != null -> NaturePracticeGrader.ROUTE
        catalog.stage == StudyStage.G9 && g9?.draft != null -> "g9/practice"
        catalog.stage == StudyStage.G9 -> "lesson/${Grade9Pilot.lessonId}"
        else -> catalog.research(workspace?.visitedId.orEmpty())?.route ?: catalog.researches.first().route
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = StudentExperienceTokens.contentWidth).fillMaxSize().testTag("screen-home"),
            state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item("status") {
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    AssistChip(onClick = { open(Routes.Study) }, label = { Text(studentStageLabel(catalog.stage)) })
                    AssistChip(onClick = { open(Routes.Streak) }, leadingIcon = { Icon(Icons.Rounded.LocalFireDepartment, null) },
                        label = { Text(student?.let { "${LearningStreak.current(it.events, System.currentTimeMillis(), it.zone)}" } ?: "—") }, modifier = Modifier.testTag("home-streak"))
                    AssistChip(onClick = { open("gems/wallet") }, leadingIcon = { Icon(Icons.Rounded.Diamond, null) },
                        label = { Text(gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language)) }, modifier = Modifier.testTag("home-gems-badge"))
                    AssistChip(onClick = { open(Routes.Streak) }, leadingIcon = { Icon(Icons.Rounded.Bolt, null) },
                        label = { Text(when (energy.mode) { EnergyMode.UNLIMITED -> tr("∞ Energy", "∞ طاقة") }) }, modifier = Modifier.testTag("home-unlimited-energy"))
                }
            }
            item("greeting") {
                StudentPanel(emphasized = true) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StudentAvatar(student?.selectedAvatarId, AvatarPose.WELCOME, 64.dp)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(tr("Hello, ", "أهلًا، ") + "\u2068${state.user?.name.orEmpty()}\u2069", style = MaterialTheme.typography.titleLarge)
                            Text(studentStageLabel(catalog.stage), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Text(tr("One idea at a time. Every thoughtful attempt counts.", "فكرة واحدة في كل مرة. كل محاولة واعية تقرّبك من الفهم."))
                    PrimaryButton(if (catalog.stage == StudyStage.G9) tr("Your next learning step", "خطوتك التعليمية التالية") else tr("Continue your reference", "تابع مرجعك"),
                        { open(nextRoute) }, Modifier.testTag("student-continue"))
                    if (student?.selectedAvatarId == null) TextButton({ open(Routes.Avatar) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Choose a companion", "اختر رفيق الرحلة")) }
                }
            }
            item("learning-progress") {
                StudentPanel(Modifier.testTag("g9-learning-progress-card")) {
                    Text(tr("Your learning progress", "تقدمك في التعلم"), style = MaterialTheme.typography.titleLarge)
                    Text(if (completed == null) tr("Saved learning is unavailable", "تعذرت قراءة التعلم المحفوظ")
                        else tr("$completed saved practice sessions", "$completed جلسات تدريب محفوظة"), style = MaterialTheme.typography.headlineSmall)
                    if (bank != null && qaCompleted != null) Text(tr("$qaCompleted internal QA sessions · separate from published learning", "$qaCompleted جلسات اختبار داخلي · مستقلة عن التعلم المنشور"))
                    Text(tr("Practice evidence describes available activities. It is not mastery of the whole curriculum.", "تصف أدلة التدريب الأنشطة المتاحة؛ ولا تمثل إتقان المنهج كاملًا."))
                    if (catalog.stage == StudyStage.G9 && bank != null && nature != null) {
                        val progress = StudentLearningProgressEngine.computeGrade9Progress(nature)
                        LinearProgressIndicator(progress = { progress.availablePracticeFraction }, modifier = Modifier.fillMaxWidth().testTag("g9-home-progress-bar"))
                        Text(progress.availablePracticeProgressText.text(), Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, minLines = 2)
                    }
                    if (natureRead == NatureRead.StorageError) NatureProgressReadError()
                    if (read == ModernRead.StorageError || saveError || g9Read == G9StudyRead.StorageError || workspaceRead == SourceWorkspaceRead.StorageError)
                        Text(tr("Saved data needs attention. Restart before retrying; your original records are preserved.", "تحتاج البيانات المحفوظة مراجعة. أعد التشغيل قبل المحاولة؛ السجلات الأصلية محفوظة."), color = MaterialTheme.colorScheme.error)
                    TextButton({ open(Routes.Progress) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Results and review", "النتائج والمراجعة")) }
                }
            }
            item("selector") {
                StudentPanel {
                    Text(tr("Your whole journey", "رحلتك كاملة"), style = MaterialTheme.typography.titleLarge)
                    Text(tr("${catalog.books.size} books · ${catalog.units.size} units · ${catalog.researches.size} lessons", "${catalog.books.size} كتابان · ${catalog.units.size} وحدات · ${catalog.researches.size} درسًا"))
                    catalog.books.forEach { book ->
                        OutlinedButton({ selectedBook = book.id; picker = true }, Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            .testTag("journey-select-${book.id}")) {
                            Icon(Icons.AutoMirrored.Rounded.MenuBook, null); Spacer(Modifier.width(8.dp))
                            Text(book.title, Modifier.weight(1f))
                            Icon(Icons.Rounded.ExpandMore, null)
                        }
                    }
                }
            }
            var stationIndex = 0
            catalog.units.forEach { unit ->
                item("unit:${unit.id}") {
                    StudentPanel(Modifier.testTag("home-unit-${unit.id}"), emphasized = true) {
                        Text(catalog.book(unit.bookId)?.title.orEmpty(), style = MaterialTheme.typography.labelLarge)
                        Text("${unit.order}. ${unit.title}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(tr("${unit.researches.size} lessons", "${unit.researches.size} دروس"))
                    }
                }
                unit.researches.forEach { research ->
                    val index = stationIndex++
                    item("station:${research.id}") {
                        val available = research.id == Grade9Pilot.lessonId || (bank != null && bank.forLesson(research.id).isNotEmpty())
                        val done = when {
                            research.id == Grade9Pilot.lessonId && g9?.attempts?.isNotEmpty() == true -> true
                            research.id == Grade9NatureLesson.LESSON_ID && bank != null && nature?.attempts?.isNotEmpty() == true -> true
                            else -> false
                        }
                        val inProgress = student?.drafts?.any { it.lessonId == research.id } == true ||
                            (research.id == Grade9Pilot.lessonId && g9?.draft != null) ||
                            (research.id == Grade9NatureLesson.LESSON_ID && bank != null && nature?.draft != null)
                        val status = when {
                            done -> tr("Practice completed · review available", "اكتمل التدريب · المراجعة متاحة")
                            inProgress -> tr("Practice in progress", "التدريب قيد التقدم")
                            available -> if (research.id == Grade9Pilot.lessonId) tr("Original pilot available", "درس أصلي تجريبي متاح") else tr("Interactive practice available · internal draft", "تدريب تفاعلي متاح · مسودة داخلية")
                            research.id in workspace?.savedIds.orEmpty() -> tr("Saved source reference", "مرجع مصدر محفوظ")
                            else -> tr("Content in preparation", "محتوى قيد الإعداد")
                        }
                        JourneyStation(research, index, student?.selectedAvatarId, status, done, inProgress) {
                            selectedLesson = research.id
                            if (scope != null) store?.rememberStation(scope, research.id)
                        }
                    }
                }
            }
            item("weekly-plan") { PlanSummary(state, repository?.planner, open) }
        }
    }
    if (picker) ModalBottomSheet(onDismissRequest = { picker = false }, sheetState = pickerSheetState) {
        val book = catalog.book(selectedBook) ?: catalog.books.first()
        LazyColumn(Modifier.fillMaxWidth().testTag("journey-unit-picker"), contentPadding = PaddingValues(20.dp)) {
            item { Text(book.title, style = MaterialTheme.typography.titleLarge) }
            book.units.forEach { unit -> item(unit.id) {
                TextButton({ picker = false
                    JourneyLayout.unitIndex(catalog, unit.id)?.let { index -> jobs.launch {
                        if (student?.reducedMotion == true) list.scrollToItem(index) else list.animateScrollToItem(index)
                    } }
                }, Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("journey-jump-${unit.id}")) { Text("${unit.order}. ${unit.title}") }
            } }
        }
    }
    selectedLesson?.let(catalog::research)?.let { research ->
        ModalBottomSheet(onDismissRequest = { selectedLesson = null }, sheetState = lessonSheetState) {
            LearningPage(Modifier.testTag("journey-lesson-sheet"), stateKey = research.id) {
                Text(research.title, style = MaterialTheme.typography.headlineSmall)
                Text(catalog.unit(research.unitId)?.title.orEmpty())
                StudentAvatar(student?.selectedAvatarId, AvatarPose.ENCOURAGING, 64.dp)
                Text(research.summary.ifBlank { tr("Browse this lesson's reference and available activities.", "تصفّح مرجع هذا الدرس والأنشطة المتاحة له.") })
                val mapping = bank?.mappings?.find { it.sourceId == research.id }
                mapping?.let { Text(tr("Learning goal", "هدف التعلم"), fontWeight = FontWeight.Bold); MathProse(it.goal) }
                Text(tr("Browsing references does not grant completion or gems.", "تصفّح المراجع لا يمنح إكمالًا أو جواهر."))
                PrimaryButton(tr("Open lesson", "افتح الدرس"), { selectedLesson = null; open(research.route) }, Modifier.testTag("journey-open-lesson"))
                if (research.id == Grade9Pilot.lessonId) OutlinedButton({ selectedLesson = null; open("g9/practice") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Original fractions practice", "تدريب الكسور الأصلي")) }
                if (mapping != null) OutlinedButton({ selectedLesson = null
                    open(if (research.id == Grade9NatureLesson.LESSON_ID) NaturePracticeGrader.ROUTE else "qa/practice/${research.id}")
                }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("journey-qa-practice")) {
                    Text(tr("Internal QA: 6 unreviewed questions", "اختبار داخلي: 6 أسئلة غير معتمدة"))
                }
            }
        }
    }
}

@Composable private fun JourneyStation(research: SourceResearch, index: Int, avatar: String?, status: String,
    done: Boolean, started: Boolean, click: () -> Unit) {
    val density = LocalDensity.current
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val colors = MaterialTheme.colorScheme
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("journey-row-${research.id}")) {
        val cardWidth = minOf(240.dp, maxWidth * if (density.fontScale > 1.4f) .76f else .64f)
        val widthPx = with(density) { maxWidth.toPx() }
        val cardPx = with(density) { cardWidth.toPx() }
        val center = JourneyLayout.center(widthPx, cardPx, index, rtl)
        val previous = JourneyLayout.center(widthPx, cardPx, (index - 1).coerceAtLeast(0), rtl)
        Canvas(Modifier.matchParentSize()) {
            val y = 64.dp.toPx()
            val path = Path().apply { moveTo(previous, 0f); cubicTo(previous, y * .52f, center, y * .48f, center, y) }
            drawPath(path, colors.outlineVariant, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
            drawLine(colors.outlineVariant, Offset(center, 128.dp.toPx()), Offset(center, size.height), 4.dp.toPx())
        }
        val opposite = if (center > widthPx / 2f) AbsoluteAlignment.TopLeft else AbsoluteAlignment.TopRight
        StudentAvatar(avatar, if (done) AvatarPose.COMPLETED else AvatarPose.ENCOURAGING, 48.dp,
            Modifier.align(opposite).padding(top = 68.dp))
        Column(Modifier.width(cardWidth).align(Alignment.TopCenter)
            .absoluteOffset(x = with(density) { (center - widthPx / 2f).toDp() }).padding(top = 64.dp)
            .clickable(onClick = click).testTag("home-research-${research.id}").semantics { stateDescription = status },
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(Modifier.size(64.dp).clickable(onClick = click).testTag("station-${research.id}").semantics { contentDescription = research.title + " · " + status }, CircleShape,
                color = if (done) colors.secondaryContainer else if (started) colors.primaryContainer else colors.surfaceContainerHigh,
                contentColor = if (done) colors.onSecondaryContainer else colors.onSurface,
                border = BorderStroke(2.dp, if (done || started) colors.primary else colors.outlineVariant), shadowElevation = 2.dp) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(if (done) Icons.Rounded.Check else if (started) Icons.Rounded.PlayArrow else Icons.AutoMirrored.Rounded.MenuBook, null, Modifier.size(28.dp))
                }
            }
            Surface(Modifier.clickable(onClick = click).testTag("station-label-${research.id}"), shape = StudentExperienceTokens.compact, color = colors.surface) {
                Column(Modifier.fillMaxWidth().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(research.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    Text(status, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = colors.onSurfaceVariant)
                }
            }
        }
    }
}
