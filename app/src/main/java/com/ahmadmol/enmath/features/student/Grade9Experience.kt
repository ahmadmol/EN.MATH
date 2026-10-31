package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import com.ahmadmol.enmath.R
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.courses.CatalogUnavailable
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.PlanSummary
import com.ahmadmol.enmath.features.reference.CleanBottomBar
import com.ahmadmol.enmath.features.reference.CleanNavItem

/** Grade 9 consumes source references; browsing is never evidence of learning. */
fun grade9Resume(state: LearningState, visitedId: String?): SourceResearch? =
    SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
        ?.research(visitedId.orEmpty())?.takeIf { StudentCatalog.acceptsReference(state, it.ref) }

fun grade9SubjectUnits(geometry: Boolean, query: String = ""): List<SourceUnit> {
    val catalog = SourceCurricula.forStage(StudyStage.G9)
    val book = catalog.books.single { it.id == if (geometry) "G9-GEO" else "G9-ALG" }
    if (query.isBlank()) return book.units
    val routes = catalog.search(query).map { it.second }.toSet()
    return book.units.filter { it.route in routes || it.researches.any { r -> r.route in routes } || book.route in routes }
}

@Composable
fun Grade9BottomNav(selected: String?, open: (String) -> Unit) {
    // Compose mirrors this logical order in Arabic: Algebra starts at the right; Home stays central.
    CleanBottomBar(listOf(
        CleanNavItem(Routes.Grade9Algebra, tr("Algebra", "الجبر"), Icons.Outlined.Calculate),
        CleanNavItem(Routes.Grade9Geometry, tr("Geometry", "الهندسة"), Icons.Outlined.SquareFoot),
        CleanNavItem(Routes.Home, tr("Home", "الرئيسية"), Icons.Outlined.Home),
        CleanNavItem(Routes.Grade9Exercises, tr("Exercises", "التمارين"), Icons.Outlined.EditNote),
        CleanNavItem(Routes.Profile, tr("Profile", "حسابي"), Icons.Outlined.PersonOutline),
    ), selected, "nav-", dark = false, accessibleCenter = true, open = open)
}

/** A saveable lazy list keeps long screens usable at large type sizes and on return from details. */
@Composable
fun Grade9Scaffold(tag: String, stateKey: Any? = null, initialIndex: Int = 0, content: LazyListScope.() -> Unit) {
    val scroll = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    LaunchedEffect(stateKey) { if (stateKey != null) scroll.scrollToItem(0) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(state = scroll, modifier = Modifier.widthIn(max = 640.dp).fillMaxSize().testTag(tag),
            contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun Grade9Card(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
private fun Grade9Heading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Grade9Action(title: String, route: String, open: (String) -> Unit, tag: String = route) {
    OutlinedButton({ open(route) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag(tag)) { Text(title) }
}

@Composable
fun Grade9LearningPath(state: LearningState, open: (String) -> Unit, gems: LocalGemsUiState,
    planner: PlannerRepository?, workspace: SourceWorkspaceRepository?, grade9Study: Grade9StudyRepository? = null, initialPathIndex: Int = 0) {
    val catalog = SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
    if (catalog == null) { CatalogUnavailable(); return }
    val scope = StudentCatalog.scope(state)
    val change = workspace?.changes?.collectAsState()?.value
    val read = remember(scope, change) { scope?.let { workspace?.read(it) } }
    val resumed = grade9Resume(state, (read as? SourceWorkspaceRead.Ready)?.workspace?.visitedId)
    val pilotData = (rememberG9Study(state, grade9Study) as? G9StudyRead.Ready)?.data
    val dailyPlan = (rememberG9Plan(state, planner) as? PlanRead.Ready)?.plan ?: StudyPlan()
    val nextStudy = grade9NextStudy(pilotData ?: G9StudyData(), dailyPlan, (read as? SourceWorkspaceRead.Ready)?.workspace?.visitedId, System.currentTimeMillis())
    val unread = state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications }
    Grade9Scaffold("screen-home", initialIndex = initialPathIndex) {
        item("greeting") {
            Grade9Card {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Image(painterResource(R.drawable.student_g9_transparent), null, Modifier.size(48.dp))
                    Column(Modifier.weight(1f)) {
                        Text((if (LocalDensity.current.fontScale > 1.4f) "" else "EN.MATH · ") + tr("Grade 9", "التاسع"), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary)
                        Text(tr("Hello, ${state.user?.name.orEmpty()}", "أهلًا، ${state.user?.name.orEmpty()}"), style = MaterialTheme.typography.titleMedium,
                            maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton({ open("notifications") }, Modifier.size(48.dp).testTag("g9-notifications")) {
                        BadgedBox(badge = { if (unread > 0) Badge { Text("$unread") } }) {
                            Icon(Icons.Outlined.Notifications, tr("Notifications, $unread unread", "الإشعارات، $unread غير مقروءة"))
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton({ open("search") }, Modifier.weight(1f).heightIn(min = 48.dp).testTag("g9-search")) { Text(tr("Search", "ابحث في التاسع")) }
                    TextButton({ open("planner") }, Modifier.weight(1f).heightIn(min = 48.dp)) { Text(tr("My plan", "خطتي")) }
                }
            }
        }
        item("continue") {
            Grade9Card {
                Text(tr("What should I study now?", "ماذا أدرس الآن؟"), style = MaterialTheme.typography.titleMedium)
                Text(nextStudy.title.text())
                Text(nextStudy.reason.text(),style=MaterialTheme.typography.bodySmall)
                Button({open(nextStudy.route)},Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("g9-continue")){Text(tr("Open suggested next step", "افتح الخطوة المقترحة"))}
                if (read == SourceWorkspaceRead.StorageError) Text(tr("Your browsing history could not be read. Existing data is preserved.",
                    "تعذّر قراءة سجل التصفح. بياناتك الحالية محفوظة."), color = MaterialTheme.colorScheme.error)
            }
        }
        item("source") {
            Column {
                Text(tr("32 topics · one original pilot; other topics under preparation", "32 بحثًا · درس أصلي تجريبي واحد؛ والبقية قيد الإعداد"), style = MaterialTheme.typography.bodySmall)
                TextButton({ open(Routes.Grade9Source) }) { Text(tr("Source and availability", "المصدر والتوفر")) }
            }
        }
        catalog.books.forEach { book ->
            item("book-${book.id}") {
                Grade9Heading(book.title, tr("${book.units.size} units · ${book.researches.size} topics", "${book.units.size} وحدات · ${book.researches.size} بحثًا"))
            }
            book.units.forEach { unit ->
                item("unit-${unit.id}") { CurriculumUnitCard(unit, open) }
                items(unit.researches, key = { it.id }) { research -> PathStation(research, resumed?.id == research.id, open) }
            }
        }
        item("plan") { PlanSummary(state, planner, open) }
        item("tools") {
            Grade9Card {
                Grade9Heading(tr("Your study tools", "أدوات دراستك"), tr("Available locally", "متاحة محليًا"))
                Grade9Action(tr("Library and saved references", "المكتبة والمراجع المحفوظة"), "library", open)
                Grade9Action(tr("Math solver", "الحل الرياضي"), Routes.Solver, open)
                Grade9Action(tr("Graph lab", "مختبر الرسم"), "explore", open)
                Grade9Action(tr("Exercises and availability", "التمارين وحالة التوفر"), Routes.Grade9Exercises, open)
                Grade9Action(tr("My progress", "تقدّمي"), Routes.Progress, open)
                Grade9Action(tr("My achievements and activity", "إنجازاتي ونشاطي"), "g9/activity", open)
                Grade9Action(tr("My gems: ", "جواهري: ") + gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language), "gems/wallet", open)
            }
        }
    }
}

@Composable
fun CurriculumUnitCard(unit: SourceUnit, open: (String) -> Unit) {
    OutlinedCard({ open(unit.route) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-unit-${unit.id}"),
        shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("${unit.order}. ${unit.title}", style = MaterialTheme.typography.titleMedium)
            Text(if (unit.researches.any { it.id == Grade9Pilot.lessonId }) tr("${unit.researches.size} indexed topics · one original pilot available", "${unit.researches.size} أبحاث مفهرسة · درس أصلي تجريبي واحد متاح") else
                tr("${unit.researches.size} indexed topics · content under review", "${unit.researches.size} أبحاث مفهرسة · المحتوى قيد المراجعة"))
        }
    }
}

@Composable
fun PathStation(research: SourceResearch, lastVisited: Boolean, open: (String) -> Unit) {
    val inset = if (LocalDensity.current.fontScale > 1.4f) 8.dp else 28.dp
    val start = research.order % 2 == 0
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(3.dp).height(16.dp).background(MaterialTheme.colorScheme.outlineVariant))
        OutlinedCard({ open(research.route) }, Modifier.fillMaxWidth()
            .padding(start = if (start) inset else 0.dp, end = if (start) 0.dp else inset)
            .heightIn(min = 64.dp).testTag("station-${research.id}"), shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("${research.order}", Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(research.title, style = MaterialTheme.typography.titleMedium)
                    Text(if (research.id == Grade9Pilot.lessonId) tr("Original learning pilot available", "درس أصلي تجريبي متاح") else if (lastVisited) tr("Last browsed · content under review", "آخر مرجع تصفحته · المحتوى قيد المراجعة") else
                        tr("Source reference · content under review", "مرجع مصدر · المحتوى قيد المراجعة"),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun Grade9SubjectScreen(state: LearningState, geometry: Boolean, open: (String) -> Unit) {
    if (SourceCurricula.forState(state)?.stage != StudyStage.G9) { CatalogUnavailable(); return }
    var query by rememberSaveable { mutableStateOf("") }
    val book = SourceCurricula.forStage(StudyStage.G9).books.single { it.id == if (geometry) "G9-GEO" else "G9-ALG" }
    val units = grade9SubjectUnits(geometry, query)
    Grade9Scaffold(if (geometry) "screen-g9-geometry" else "screen-g9-algebra") {
        item("heading") { Grade9Heading(book.title, tr("${book.units.size} units · ${book.researches.size} indexed topics",
            "${book.units.size} وحدات · ${book.researches.size} بحثًا مفهرسًا")) }
        item("search") { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(),
            label = { Text(if (geometry) tr("Search geometry", "ابحث في الهندسة") else tr("Search algebra", "ابحث في الجبر")) }, singleLine = true) }
        item("status") { Grade9Card {
            Text(if (geometry) tr("Geometry content is under preparation. Browsing the index is not completed learning.", "محتوى الهندسة قيد الإعداد. تصفح الفهرس ليس تعلّمًا مكتملًا.") else
                tr("An original reduced-fractions pilot is available. The other Algebra topics remain under preparation.", "يتوفر درس أصلي تجريبي للكسور المختزلة. بقية أبحاث الجبر قيد الإعداد."))
            if (!geometry) Grade9Action(tr("Learn reduced fractions", "تعلّم الكسور المختزلة"), "lesson/${Grade9Pilot.lessonId}", open)
            Grade9Action(tr("Book details and source pages", "تفاصيل الكتاب وصفحات المصدر"), book.route, open)
            Grade9Action(tr("Separate book progress", "تقدم الكتاب المستقل"), "progress/book/${book.id}", open)
        } }
        if (units.isEmpty()) item("empty") { Text(tr("No matching units or topics. Try another title.", "لا وحدات أو أبحاث مطابقة. جرّب عنوانًا آخر.")) }
        items(units, key = { it.id }) { unit -> CurriculumUnitCard(unit, open) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReferenceFilter(label: String, selected: String, options: List<Pair<String, String>>, choose: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(selected, {}, Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), readOnly = true,
            label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded, { expanded = false }) {
            options.forEach { (id, title) -> DropdownMenuItem(text = { Text(title) }, onClick = { choose(id); expanded = false }) }
        }
    }
}

@Composable
fun Grade9ExercisesScreen(state: LearningState, open: (String) -> Unit, grade9Study: Grade9StudyRepository? = null) {
    val catalog = SourceCurricula.forState(state)?.takeIf { it.stage == StudyStage.G9 }
    if (catalog == null) { CatalogUnavailable(); return }
    var bookId by rememberSaveable { mutableStateOf(catalog.books.first().id) }
    var unitId by rememberSaveable { mutableStateOf("") }
    var researchId by rememberSaveable { mutableStateOf("") }
    val book = catalog.book(bookId) ?: catalog.books.first()
    val unit = book.units.find { it.id == unitId }
    val research = unit?.researches?.find { it.id == researchId }
    Grade9Scaffold("screen-g9-exercises") {
        item("title") { Grade9Heading(tr("Exercises", "التمارين"), tr("Choose a source topic to check availability", "اختر موضوعًا من المصدر لمعرفة حالة التوفر")) }
        item("filters") { Grade9Card {
            ReferenceFilter(tr("Book", "الكتاب"), book.title, catalog.books.map { it.id to it.title }) { bookId = it; unitId = ""; researchId = "" }
            ReferenceFilter(tr("Unit", "الوحدة"), unit?.title ?: tr("All units", "كل الوحدات"),
                listOf("" to tr("All units", "كل الوحدات")) + book.units.map { it.id to it.title }) { unitId = it; researchId = "" }
            if (unit != null) ReferenceFilter(tr("Topic", "البحث"), research?.title ?: tr("All topics", "كل الأبحاث"),
                listOf("" to tr("All topics", "كل الأبحاث")) + unit.researches.map { it.id to it.title }) { researchId = it }
        } }
        item("availability") { Grade9Card {
            val pilotMatches = book.id == "G9-ALG" && (unit == null || unit.id == "G9-ALG-U01") && (research == null || research.id == Grade9Pilot.lessonId)
            if (pilotMatches) {
                Text(tr("Reduced fractions · 4 original practice questions", "كسور مختزلة · 4 أسئلة تدريب أصلية"), style = MaterialTheme.typography.titleMedium)
                Grade9Action(tr("Start or resume practice", "ابدأ التدريب أو استكمله"), "g9/practice", open)
                Grade9Action(tr("Review the lesson", "راجع الدرس"), "lesson/${Grade9Pilot.lessonId}", open)
            }
            Text(tr("Other exercises are under preparation", "بقية التمارين قيد الإعداد"), style = MaterialTheme.typography.titleMedium)
            Text(tr("Only the bounded EN.MATH pilot is available. Textbook exercise pages and earlier drafts are not a released question bank.",
                "المتاح هو درس EN.MATH التجريبي المحدد فقط. صفحات تمارين الكتاب والمسودات السابقة ليست بنك أسئلة منشورًا."))
            Text(tr("Quick tests, unit tests and comprehensive exams will become available after content review.",
                "تتوفر الاختبارات السريعة واختبارات الوحدات والنماذج الشاملة بعد مراجعة المحتوى."))
            Grade9Action(tr("Browse selected reference", "تصفح المرجع المحدد"), research?.route ?: unit?.route ?: book.route, open)
            Grade9Action(tr("Source and review status", "المصدر وحالة المراجعة"), Routes.Grade9Source, open)
        } }
        item("mistakes") { Grade9Card {
            Text(tr("My mistakes", "دفتر أخطائي"), style = MaterialTheme.typography.titleMedium)
            Text(tr("Review answers from your actual saved pilot attempts.", "راجع إجابات محاولاتك التجريبية المحفوظة فعليًا."))
            Grade9Action(tr("Open mistake review", "فتح مراجعة الأخطاء"), "review", open)
            Grade9Action(tr("Plan my next step", "خطّط لخطوتي التالية"), "planner", open)
        } }
        item("pilot-evidence") { Grade9EvidenceSection(state, grade9Study, open) }
    }
}

@Composable
fun Grade9SourceScreen() {
    val catalog = SourceCurricula.forStage(StudyStage.G9)
    Grade9Scaffold("screen-g9-source") {
        item { Grade9Heading(tr("Source and availability", "المصدر والتوفر"), tr("An index is a guide, not a published lesson", "الفهرس دليل وليس درسًا منشورًا")) }
        items(catalog.books, key = { it.id }) { book -> Grade9Card {
            Text(book.title, style = MaterialTheme.typography.titleLarge)
            Text(tr("Source edition ${book.edition} · ${book.pages} PDF pages", "طبعة المصدر ${book.edition} · ${book.pages} صفحة PDF"))
            Text(tr("${book.units.size} units · ${book.researches.size} indexed topics", "${book.units.size} وحدات · ${book.researches.size} بحثًا مفهرسًا"))
        } }
        item { Grade9Card {
            Text(tr("One original EN.MATH pilot: reduced fractions, with source and automated mathematical checks. Independent educator review is still recommended. This is not official year-alignment certification.",
                "درس EN.MATH أصلي تجريبي واحد: الكسور المختزلة، مع تحقق المصدر واختبارات رياضية آلية. ما زالت مراجعة تربوية مستقلة مستحسنة. هذا ليس اعتمادًا رسميًا لمطابقة السنة الدراسية."))
            Text(tr("Under review", "قيد المراجعة"), style = MaterialTheme.typography.titleLarge)
            Text(tr("The source analyzed is 2025–2026. Alignment with 2026–2027 has not been verified.", "المصدر المحلل هو 2025–2026. لم تُثبت المطابقة مع 2026–2027."))
            Text(tr("Explanations, worked examples, diagrams, questions and solutions need mathematical and educational review. Source indexing does not grant publishing rights; permissions must be checked before distributing files.",
                "تحتاج الشروحات والأمثلة والرسوم والأسئلة والحلول مراجعة رياضية وتربوية. فهرسة المصدر لا تمنح حق النشر؛ يجب فحص الإذن قبل توزيع الملفات."))
            Text(tr("Browsing, saving references and completing manual plan goals do not award mastery or gems.", "التصفح وحفظ المراجع وإنجاز أهداف الخطة يدويًا لا تمنح إتقانًا أو جواهر."))
        } }
    }
}

@Composable
fun Grade9HelpScreen(open: (String) -> Unit) {
    Grade9Scaffold("screen-g9-help") {
        item { Grade9Heading(tr("Your Grade 9 guide", "دليلك في التاسع"), tr("One step at a time", "خطوة بخطوة")) }
        item { Grade9Card {
            Text(tr("Where do I start?", "من أين أبدأ؟"), style = MaterialTheme.typography.titleLarge)
            Text(tr("Explore the path on Home or choose Algebra or Geometry. Open a topic to see its source pages, save it to your library or add it to your plan.",
                "تصفح المسار في الرئيسية أو اختر الجبر أو الهندسة. افتح البحث لمعرفة صفحات المصدر وحفظه في مكتبتك أو إضافته إلى خطتك."))
            Text(tr("What does under review mean?", "ماذا تعني قيد المراجعة؟"), style = MaterialTheme.typography.titleMedium)
            Text(tr("The title is indexed. A checked explanation and exercise set are not yet available. It is not counted as completed learning.",
                "العنوان مفهرس، لكن الشرح والتمارين المعتمدة غير متاحة بعد. لا يُحسب هذا تعلّمًا مكتملًا."))
            Text(tr("Where are my data?", "أين تُحفظ بياناتي؟"), style = MaterialTheme.typography.titleMedium)
            Text(tr("Your profile, plan and saved work stay on this device in a separate local file. There is no cloud sync. Switch files from Profile; keep app data to retain your work.",
                "ملفك وخطتك وأعمالك المحفوظة تبقى على هذا الجهاز في ملف محلي مستقل. لا توجد مزامنة سحابية. بدّل الملف من حسابي، واحتفظ ببيانات التطبيق لحفظ عملك."))
            Grade9Action(tr("My study plan", "خطتي الدراسية"), "planner", open)
            Grade9Action(tr("Source and availability", "المصدر والتوفر"), Routes.Grade9Source, open)
        } }
    }
}
