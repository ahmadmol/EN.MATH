package com.ahmadmol.enmath.features.courses

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable
private fun CatalogRow(title: String, detail: String, tag: String, click: () -> Unit) {
    CurriculumTile(title, detail, Modifier.testTag(tag), click = click)
}

@Composable
private fun PendingContent() {
    EducationalInfoBanner(tr("Content is being prepared / reviewed", "قيد إعداد المحتوى/المراجعة"))
    Text(tr("Only the source index is available. Reviewed explanations and exercises will appear here.",
        "المتاح حاليًا هو فهرس المصدر. ستظهر الشروحات والتمارين بعد إعدادها واعتمادها."))
}

@Composable
private fun pages(p: SourcePages) = tr("PDF pages ${p.from}–${p.to}", "صفحات PDF ${p.from}–${p.to}")

@Composable
fun Grade9StudyScreen(state: LearningState, open: (String) -> Unit) {
    ScreenPage(Modifier.testTag("screen-study-g9")) {
        PageHeading(tr("Grade 9", "التاسع"), tr("Mathematics", "الرياضيات"))
        Text(tr("Algebra and geometry · 10 units · 32 indexed lessons", "الجبر والهندسة · 10 وحدات · 32 بحثًا مفهرسًا"))
        PendingContent()
        G9SourceCatalog.sources.forEach { source ->
            val units = G9SourceCatalog.units.filter { it.ref.sourceBookId == source.ref.sourceBookId }
            CatalogRow(source.title, tr("${units.size} units · ${units.sumOf { it.lessons.size }} lessons · ${source.pages} reference pages",
                "${units.size} وحدات · ${units.sumOf { it.lessons.size }} بحثًا · ${source.pages} صفحة مرجعية"),
                "catalog-book-${source.ref.sourceBookId}") { open("book/${source.ref.sourceBookId}") }
        }
        Text(tr("Source edition: 2025–2026. Edition 2026–2027 is unverified.", "طبعة المصدر: 2025–2026. لم يُتحقق من مطابقتها لطبعة 2026–2027."))
        OutlinedButton({ open("planner") }, Modifier.testTag("g9-open-plan")) { Text(tr("Study planner", "مخطط الدراسة")) }
        OutlinedButton({ open("progress") }) { Text(tr("My progress", "تقدمي")) }
        OutlinedButton({ open("search") }) { Text(tr("Search the index", "البحث في الفهرس")) }
        Text(tr("Educational content ready: ${G9SourceCatalog.readyLessonCount}/32 (${G9SourceCatalog.educationalCoveragePercent}%)",
            "المحتوى التعليمي المعتمد: ${G9SourceCatalog.readyLessonCount}/32 (${G9SourceCatalog.educationalCoveragePercent}٪)"),
            Modifier.testTag("g9-content-coverage"))
    }
}

@Composable
fun Grade9BookScreen(id: String, open: (String) -> Unit) {
    val source = G9SourceCatalog.sources.find { it.ref.sourceBookId == id }
    ScreenPage(Modifier.testTag("screen-book-g9")) {
        if (source == null) { CatalogUnavailable(); return@ScreenPage }
        PageHeading(tr("Mathematics", "الرياضيات"), source.title)
        Text(tr("Source edition ${source.ref.edition} · ${source.pages} PDF pages", "طبعة المصدر ${source.ref.edition} · ${source.pages} صفحة PDF"))
        G9SourceCatalog.units.filter { it.ref.sourceBookId == id }.forEach { unit ->
            CatalogRow("${unit.order}. ${unit.title}", pages(unit.pages) + " · " + tr("${unit.lessons.size} indexed lessons", "${unit.lessons.size} أبحاث مفهرسة"),
                "book-topic-${unit.ref.unitId}") { open("topic/${unit.ref.unitId}") }
        }
    }
}

@Composable
fun Grade9UnitScreen(id: String, open: (String) -> Unit) {
    val unit = G9SourceCatalog.unit(id)
    ScreenPage(Modifier.testTag("screen-topic-g9")) {
        if (unit == null) { CatalogUnavailable(); return@ScreenPage }
        PageHeading(tr("Unit ${unit.order}", "الوحدة ${unit.order}"), unit.title)
        Text(pages(unit.pages))
        Text(tr("Unit introduction: ", "مقدمة الوحدة: ") + pages(unit.introductionPages))
        unit.lessons.forEach { lesson ->
            CatalogRow(lesson.title, pages(lesson.pages) + " · " + tr("Content in preparation", "قيد إعداد المحتوى"),
                "topic-lesson-${lesson.ref.lessonId}") { open("lesson/${lesson.ref.lessonId}") }
        }
        CatalogRow(tr("Unit exercises and problems", "تمارين ومسائل الوحدة"),
            pages(unit.reviewPages) + " · " + tr("Pending review", "قيد المراجعة"), "g9-unit-review-$id") { open("practice/$id-review") }
    }
}

@Composable
fun Grade9LessonScreen(id: String, state: LearningState, open: (String) -> Unit, planner: PlannerRepository?) {
    val lesson = G9SourceCatalog.lesson(id)
    val scope = remember(state.user?.localId, state.generation) { StudentCatalog.scope(state) }
    var result by remember(id, scope) { mutableStateOf<PlanWrite?>(null) }
    ScreenPage(Modifier.testTag("screen-lesson-g9")) {
        if (lesson == null) { CatalogUnavailable(); return@ScreenPage }
        PageHeading(tr("Indexed lesson", "بحث مفهرس"), lesson.title)
        Text(pages(lesson.pages), Modifier.testTag("g9-source-pages"))
        Text(tr("Source edition ${lesson.ref.edition}", "طبعة المصدر ${lesson.ref.edition}"))
        PendingContent()
        Text(tr("Index summary: ", "ملخص الفهرس: ") + lesson.summary)
        // A pattern from another page/lesson is not a verified activity for this lesson.
        G9SourceCatalog.visibleElements(lesson.ref).forEach { ReviewedBookElementCard(it) }
        OutlinedButton({ if (scope != null && planner != null) result = planner.pinSource(scope, requireNotNull(G9SourceCatalog.ref(id))) },
            Modifier.testTag("g9-plan-lesson"), enabled = scope != null && planner != null) {
            Text(tr("Keep reference in my plan", "حفظ المرجع في خطتي"))
        }
        result?.let { Text(if (it == PlanWrite.SAVED || it == PlanWrite.ALREADY_SAVED)
            tr("Reference saved; content remains under review.", "حُفظ المرجع؛ المحتوى ما زال قيد المراجعة.")
            else tr("Reference could not be saved.", "تعذّر حفظ المرجع.")) }
        OutlinedButton({ open("practice/$id") }, Modifier.testTag("g9-practice-status")) { Text(tr("Exercise availability", "حالة التدريبات")) }
        OutlinedButton({ open("planner") }) { Text(tr("Study planner", "مخطط الدراسة")) }
    }
}

/** Shared presentation for source-verified, approved book elements. No synthetic lesson blocks. */
@Composable
fun ReviewedBookElementCard(element: ReviewedBookElement) {
    if (!element.sourcePresenceVerified || element.review.pdfPage == null ||
        element.review.mathReview != ReviewStatus.APPROVED || element.review.rightsReview != ReviewStatus.APPROVED) return
    val heading = G9SourceCatalog.patterns.find { it.kind == element.kind }?.heading ?: return
    AppCard(Modifier.fillMaxWidth().testTag("g9-element-${element.id}")) {
        Text(heading, style = MaterialTheme.typography.titleMedium)
        Text(element.body.text())
        Text(tr("PDF page ${element.review.pdfPage}", "صفحة PDF ${element.review.pdfPage}"), style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun Grade9PracticeStatusScreen(id: String) {
    val lesson = G9SourceCatalog.lesson(id)
    val unit = if (id.endsWith("-review")) G9SourceCatalog.unit(id.removeSuffix("-review")) else null
    ScreenPage(Modifier.testTag("screen-practice-g9-pending")) {
        if (lesson == null && unit == null) { CatalogUnavailable(); return@ScreenPage }
        PageHeading(tr("Exercises", "التدريبات"), lesson?.title ?: requireNotNull(unit).title)
        Text(pages(lesson?.pages ?: requireNotNull(unit).reviewPages))
        PendingContent()
        Text(tr("No approved exercises are available yet.", "لا توجد تمارين معتمدة متاحة بعد."))
    }
}

@Composable
fun Grade9ProgressScreen(state: LearningState, open: (String) -> Unit) {
    ScreenPage(Modifier.testTag("screen-progress-g9")) {
        PageHeading(tr("Grade 9", "التاسع"), tr("My progress", "تقدمي"))
        Text(tr("Reviewed educational content: ${G9SourceCatalog.readyLessonCount}/32 (${G9SourceCatalog.educationalCoveragePercent}%)",
            "المحتوى التعليمي المعتمد: ${G9SourceCatalog.readyLessonCount}/32 (${G9SourceCatalog.educationalCoveragePercent}٪)"))
        Text(tr("Completed lessons: ${StudentCatalog.completed(state).size}/32", "الأبحاث المكتملة: ${StudentCatalog.completed(state).size}/32"))
        Text(tr("Source index: 10/10 units · 32/32 lessons · 200/200 pages", "فهرس المصدر: 10/10 وحدات · 32/32 أبحاث · 200/200 صفحات"))
        PendingContent()
        G9SourceCatalog.units.forEach { unit ->
            CatalogRow(unit.title, tr("${unit.lessons.size} indexed lessons · content in preparation", "${unit.lessons.size} أبحاث مفهرسة · قيد إعداد المحتوى"),
                "g9-progress-${unit.ref.unitId}") { open("topic/${unit.ref.unitId}") }
        }
    }
}

@Composable
fun CatalogUnavailable() = EmptyState(tr("Content unavailable", "المحتوى غير متاح"),
    tr("This reference is not in your stage's catalog.", "هذا المرجع غير موجود في فهرس مرحلتك."))

@Composable
fun Grade9LibraryLinks(state: LearningState, planner: PlannerRepository?, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    val changed = planner?.changes?.collectAsState()?.value
    val plan = remember(scope, changed) { scope?.let { (planner?.read(it) as? PlanRead.Ready)?.plan } }
    Text(tr("Grade 9 source index · 10 units · 32 lessons", "فهرس مصدر التاسع · 10 وحدات · 32 بحثًا"))
    OutlinedButton({ open("study") }) { Text(tr("Browse the source index", "تصفح فهرس المصدر")) }
    plan?.tasks?.mapNotNull { it.lesson }?.filter { it.kind == ContentKind.SOURCE_INDEX && StudentCatalog.acceptsReference(state, it) }
        ?.distinct()?.forEach { ref -> CatalogRow(StudentCatalog.sourceTitle(ref).orEmpty(),
            tr("Source reference saved in your plan · pending review", "مرجع مصدر محفوظ في خطتك · قيد المراجعة"),
            "library-source-${ref.lessonId}") { open("lesson/${ref.lessonId}") } }
}
