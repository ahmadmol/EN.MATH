package com.ahmadmol.enmath.features.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*

@Composable private fun sourceAccent() = if (LocalStudyStage.current == StudyStage.G9) MaterialTheme.colorScheme.primary else Color(0xFF0066FF)

@Composable fun SourceStudyScreen(stage: StudyStage, open: (String) -> Unit) {
    val catalog = SourceCurricula.forStage(stage)
    ScreenPage(Modifier.testTag(if (stage == StudyStage.G9) "screen-study-g9" else "screen-study")) {
        StageHero(stage, tr("Choose your path", "اختر مسارك"),
            if (stage == StudyStage.G9) tr("Algebra and geometry", "رحلة الجبر والهندسة") else tr("Two books, one curriculum", "كتابان ومسار دراسي واحد"))
        catalog.books.forEach { book ->
            SourceItemCard(book.title, tr("${book.units.size} units · ${book.researches.size} indexed researches · ${book.edition}",
                "${book.units.size} وحدات · ${book.researches.size} بحثًا مفهرسًا · ${book.edition}"),
                Modifier.testTag("catalog-book-${if (stage == StudyStage.BAC) BacSourceCatalog.book(book.id)?.existingBookId else book.id}")) { open(book.route) }
        }
        SourceMetrics(listOf("${catalog.units.size}" to tr("Units", "وحدات"), "${catalog.researches.size}" to tr("Indexed researches", "أبحاث مفهرسة"),
            "${catalog.reviewed}" to tr("Reviewed lessons", "دروس معتمدة")))
        SourceStatusNotice(stage)
        Button(
            onClick = { open("planner") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("g9-open-plan"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = sourceAccent())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(20.dp))
                Text(tr("My study plan", "خطتي الدراسية"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        OutlinedButton(
            onClick = { open("study/review/all") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.FactCheck, null, modifier = Modifier.size(20.dp))
                Text(tr("Review and exam preparation", "المراجعة والاستعداد للامتحان"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        if (stage == StudyStage.BAC) SourceItemCard(tr("Demo learning · separate progress", "التجربة التعليمية · تقدم مستقل"),
            tr("24 preserved demo lessons; not the 63 source researches", "24 درسًا تجريبيًا محفوظًا؛ مستقلة عن أبحاث المصدر الـ63"), Modifier.testTag("bac-legacy-lessons"), Icons.Outlined.Science) { open("study/demo") }
    }
}

@Composable fun SourceBookScreen(stage: StudyStage, id: String, open: (String) -> Unit, state: LearningState? = null, workspace: SourceWorkspaceRepository? = null) {
    val catalog = SourceCurricula.forStage(stage)
    val book = catalog.book(id) ?: if (stage == StudyStage.BAC) BacSourceCatalog.book(id)?.let { catalog.book(it.id) } else null
    val scope = state?.let(StudentCatalog::scope)
    val change = workspace?.changes?.collectAsState()?.value
    val saved = remember(scope, change) { scope?.let { (workspace?.read(it) as? SourceWorkspaceRead.Ready)?.workspace } }
    ScreenPage(Modifier.testTag(if (stage == StudyStage.G9) "screen-book-g9" else "screen-book")) {
        if (book == null) { CatalogUnavailable(); return@ScreenPage }
        StageHero(stage, book.title, tr("Source edition ${book.edition} · ${book.pages} PDF pages", "طبعة المصدر ${book.edition} · ${book.pages} صفحة PDF"))
        SourceMetrics(listOf("${book.units.size}" to tr("Units", "وحدات"), "${book.researches.size}" to tr("Indexed researches", "أبحاث مفهرسة")))
        SourceStatusNotice(stage)
        saved?.lastVisitedByBook?.get(book.id)?.let(catalog::research)?.let { last ->
            PrimaryButton(tr("Continue browsing: ${last.title}", "تابع التصفح: ${last.title}"), { open(last.route) })
        }
        SectionTitle(tr("Book units", "وحدات الكتاب"))
        book.units.forEach { unit ->
            SourceItemCard("${unit.order}. ${unit.title}", sourcePageSpan(unit.pages, stage == StudyStage.G9) + " · " +
                tr("${unit.researches.size} researches", "${unit.researches.size} أبحاث"), Modifier.testTag("book-topic-${unit.id}")) { open(unit.route) }
        }
        OutlinedButton(
            onClick = { open("study/review/${book.id}") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Outlined.FactCheck, null, modifier = Modifier.size(20.dp))
                Text(tr("Book review availability", "حالة مراجعة الكتاب"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        OutlinedButton(
            onClick = { open("progress/book/${book.id}") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Insights, null, modifier = Modifier.size(20.dp))
                Text(tr("Book progress", "تقدم الكتاب"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable fun SourceUnitScreen(stage: StudyStage, id: String, open: (String) -> Unit, unitAction: @Composable () -> Unit = {}) {
    val catalog = SourceCurricula.forStage(stage); val unit = catalog.unit(id)
    ScreenPage(Modifier.testTag(if (stage == StudyStage.G9) "screen-topic-g9" else "screen-topic")) {
        if (unit == null) { CatalogUnavailable(); return@ScreenPage }
        StageHero(stage, unit.title, catalog.book(unit.bookId)?.title.orEmpty())
        Text(sourcePageSpan(unit.pages, stage == StudyStage.G9))
        SourceStatusNotice(stage)
        if (stage == StudyStage.G9) unitAction()
        SectionTitle(tr("${unit.researches.size} source researches", "${unit.researches.size} أبحاث من المصدر"))
        unit.researches.forEach { research ->
            SourceItemCard("${research.order}. ${research.title}", sourcePageSpan(research.pages, stage == StudyStage.G9) + " · " +
                if (stage == StudyStage.G9 && research.id == Grade9Pilot.lessonId) tr("Original learning pilot available", "درس أصلي تجريبي متاح") else tr("Pending content review", "قيد مراجعة المحتوى"),
                Modifier.testTag("topic-lesson-${research.id}")) { open(research.route) }
        }
        if (stage == StudyStage.G9) {
            val original = requireNotNull(G9SourceCatalog.unit(id))
            SourceItemCard(tr("Unit exercises and problems", "تمارين ومسائل الوحدة"),
                tr("PDF ${original.reviewPages.from}–${original.reviewPages.to} · pending review", "PDF ${original.reviewPages.from}–${original.reviewPages.to} · قيد المراجعة"),
                Modifier.testTag("g9-unit-review-$id"), Icons.AutoMirrored.Outlined.Assignment) { open("practice/$id-review") }
        } else BacSourceCatalog.unit(id)?.sectionIds.orEmpty().mapNotNull(BacSourceCatalog::section).forEach { section ->
            SourceItemCard(section.title, sourcePageSpan(section.span) + " · " + tr("Question extraction pending", "الأسئلة الفردية غير مستخرجة"),
                Modifier.testTag("bac-section-${section.id}"), Icons.AutoMirrored.Outlined.Assignment) { open("practice/${section.id}") }
        }
        OutlinedButton(
            onClick = { open("study/review/$id") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.AutoMirrored.Outlined.FactCheck, null, modifier = Modifier.size(20.dp))
                Text(tr("Review this unit", "مراجعة هذه الوحدة"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun SourceResearchScreen(id: String, state: LearningState, planner: PlannerRepository?,
    workspace: SourceWorkspaceRepository?, open: (String) -> Unit) {
    val catalog = SourceCurricula.forState(state); val research = catalog?.research(id)
    val scope = StudentCatalog.scope(state)
    val change = workspace?.changes?.collectAsState()?.value
    val reading = remember(scope, change) { scope?.let { workspace?.read(it) } }
    val saved = (reading as? SourceWorkspaceRead.Ready)?.workspace?.savedIds?.contains(id) == true
    var result by remember(id, scope) { mutableStateOf<PlanWrite?>(null) }
    LaunchedEffect(scope, id) { if (scope != null && research != null) result = workspace?.visit(scope, research.ref)?.takeIf { it !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED) } }
    val dark = isDarkTheme()
    ScreenPage(Modifier.testTag(if (catalog?.stage == StudyStage.G9) "screen-lesson-g9" else "screen-lesson")) {
        if (catalog == null || research == null) { CatalogUnavailable(); return@ScreenPage }
        val unit = requireNotNull(catalog.unit(research.unitId)); val book = requireNotNull(catalog.book(unit.bookId))
        StageHero(catalog.stage, research.title, "${book.title} · ${unit.title}")

        // Metadata badges row matching Image 8
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (dark) Color(0xFF1E2D4E) else Color(0xFFEBF3FF),
                border = BorderStroke(1.dp, if (dark) Color(0xFF2E416B) else Color(0xFFD2E3FC)),
                modifier = Modifier.testTag(if (catalog.stage == StudyStage.G9) "g9-source-pages" else "bac-source-pages")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.Description, null, Modifier.size(16.dp), tint = sourceAccent())
                    Text(
                        text = sourcePageSpan(research.pages, catalog.stage == StudyStage.G9),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = if (dark) Color(0xFF1E2D4E) else Color(0xFFEBF3FF),
                border = BorderStroke(1.dp, if (dark) Color(0xFF2E416B) else Color(0xFFD2E3FC))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Outlined.CheckCircle, null, Modifier.size(16.dp), tint = sourceAccent())
                    Text(
                        text = tr("Source edition ${research.ref.edition}", "طبعة المصدر ${research.ref.edition}"),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = if (dark) Color.White else Color(0xFF0F172A)
                    )
                }
            }
        }

        SourceStatusNotice(catalog.stage)
        if (research.summary.isNotBlank()) AppCard {
            Text(tr("Index description", "وصف الفهرس"), style = MaterialTheme.typography.titleMedium)
            Text(research.summary)
        }
        if (research.needsBoundaryReview) EducationalInfoBanner(tr("The source page boundary still requires manual review.", "حدود صفحات المصدر ما زالت تحتاج تحققًا يدويًا."))
        if (id in listOf("bac-1-u05-l04", "bac-1-u05-l05")) EducationalInfoBanner(tr("Two distinct researches share the verified start: printed 163 / PDF 164.", "بحثان منفصلان لهما بداية مشتركة موثقة: المطبوع 163 / PDF 164."))
        if (catalog.stage == StudyStage.G9 && id == Grade9Pilot.lessonId) {
            SourceItemCard(tr("Original EN.MATH learning pilot", "درس EN.MATH أصلي تجريبي"),
                tr("Source checked on pages 14–15. Not official 2026–2027 certification.", "تم التحقّق من المصدر في ص 14–15. ليس اعتمادًا رسميًا لسنة 2026–2027.")) { open("lesson/$id") }
        } else SourcePendingCard(tr("Learning content under review", "المحتوى التعليمي قيد المراجعة"),
            tr("Explanations, worked examples, proofs and scored exercises are not published for this reference yet.", "لم تُنشر شروحات وأمثلة محلولة وبراهين وتمارين معتمدة لهذا المرجع بعد."))
        if (catalog.stage == StudyStage.G9) G9SourceCatalog.lesson(id)?.let { lesson -> G9SourceCatalog.visibleElements(lesson.ref).forEach { ReviewedBookElementCard(it) } }

        Button(
            onClick = {
                if (scope != null && planner != null) result = planner.pinSource(scope, research.ref)
            },
            enabled = scope != null && planner != null,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .testTag(if (catalog.stage == StudyStage.G9) "g9-plan-lesson" else "bac-plan-source"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Add, null, modifier = Modifier.size(20.dp))
                Text(tr("Add reference to my plan", "أضف المرجع إلى خطتي"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        if (workspace != null) {
            OutlinedButton(
                onClick = { if (scope != null) result = workspace.bookmark(scope, research.ref, !saved) },
                enabled = scope != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("source-bookmark"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, sourceAccent()),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (saved) Icons.Outlined.BookmarkRemove else Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(20.dp))
                    Text(if (saved) tr("Remove from my library", "إزالة من مكتبتي") else tr("Save in my library", "حفظ في مكتبتي"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        result?.let { Text(if (it in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)) tr("Saved on this device", "حُفظ على هذا الجهاز")
            else tr("Save unconfirmed. Previous data is preserved; reopen before retrying.", "لم يُؤكّد الحفظ. البيانات السابقة محفوظة؛ أعد الفتح قبل المحاولة.")) }
        OutlinedButton({ open(if (catalog.stage == StudyStage.G9 && id == Grade9Pilot.lessonId) "g9/practice" else "practice/$id") }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-practice-status")) { Text(tr("Exercise availability", "حالة التدريبات")) }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Open my plan", "افتح خطتي")) }
        if (catalog.stage == StudyStage.BAC) BacSourceCatalog.relatedLegacy(id).forEach { demo ->
            SourceItemCard(demo.title.text(), tr("Related demo · separate completion", "تجربة ذات صلة · إكمال مستقل"), Modifier.testTag("bac-related-${demo.id}"), Icons.Outlined.Science) { open("lesson/${demo.id}") }
        }
    }
}

@Composable fun SourceExercisePending(stage: StudyStage, id: String, open: ((String) -> Unit)? = null) {
    val catalog = SourceCurricula.forStage(stage); val research = catalog.research(id)
    val g9Unit = if (stage == StudyStage.G9 && id.endsWith("-review")) G9SourceCatalog.unit(id.removeSuffix("-review")) else null
    val section = if (stage == StudyStage.BAC) BacSourceCatalog.section(id) else null
    ScreenPage(Modifier.testTag(if (stage == StudyStage.G9) "screen-practice-g9-pending" else "screen-bac-source-pending")) {
        if (research == null && g9Unit == null && section == null) { CatalogUnavailable(); return@ScreenPage }
        StageHero(stage, tr("Exercises under review", "التمارين قيد المراجعة"), research?.title ?: g9Unit?.title ?: section?.title.orEmpty())
        SourceStatusNotice(stage)
        SourcePendingCard(tr("No approved exercises published yet", "لم تُجهّز التمارين المعتمدة بعد"),
            tr("The index describes source pages. It does not contain graded questions, attempts or results.", "الفهرس يصف صفحات المصدر. لا يتضمن أسئلة جاهزة للتقييم أو محاولات أو نتائج."))
        if (open != null) {
            OutlinedButton({ open(research?.route ?: "study") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Browse the source", "تصفّح المصدر")) }
            OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Plan my next step", "خطّط لخطوتي التالية")) }
        }
    }
}

@Composable fun SourceProgressScreen(state: LearningState, open: (String) -> Unit, planner: PlannerRepository? = null,
    bookId: String? = null, unitId: String? = null, grade9Study: Grade9StudyRepository? = null) {
    val catalog = SourceCurricula.forState(state)
    ScreenPage(Modifier.testTag(if (catalog?.stage == StudyStage.G9) "screen-progress-g9" else "screen-progress")) {
        if (catalog == null || (bookId != null && catalog.book(bookId) == null) || (unitId != null && catalog.unit(unitId) == null)) { CatalogUnavailable(); return@ScreenPage }
        if (catalog.stage == StudyStage.G9 && (bookId == null || bookId == Grade9Pilot.ref.sourceId) &&
            (unitId == null || unitId == "G9-ALG-U01"))
            com.ahmadmol.enmath.features.student.Grade9EvidenceSection(state, grade9Study, open)
        StageHero(catalog.stage, tr("My learning evidence", "أدلة تعلّمي"), tr("Indexing, reviewed content and learning are measured separately", "الفهرسة واعتماد المحتوى والتعلّم مؤشرات مستقلة"))
        SourceMetrics(listOf("${catalog.units.size}" to tr("Indexed units", "وحدات مفهرسة"), "${catalog.researches.size}" to tr("Indexed researches", "أبحاث مفهرسة"),
            "${catalog.reviewed}/${catalog.researches.size}" to tr("Published learning", "محتوى معتمد")))
        if (catalog.stage == StudyStage.G9) Text(tr("One original pilot is available separately. The source catalog's reviewed-textbook coverage is still 0/32.",
            "يتوفر درس أصلي تجريبي بصورة مستقلة. تغطية الكتاب المعتمدة في فهرس المصدر ما زالت 0/32."))
        SourcePendingCard(if (catalog.stage == StudyStage.G9) tr("Textbook corpus under preparation", "محتوى الكتاب قيد الإعداد") else tr("Learning evidence is not available yet", "لم تتوفر أدلة تعلّم للمصدر بعد"),
            tr("Browsing all pages is not mastery. Your source references and goals stay saved independently.", "تصفح الصفحات لا يثبت الإتقان. تبقى مراجع المصدر والأهداف محفوظة بصورة مستقلة."))
        catalog.books.filter { bookId == null || it.id == bookId }.forEach { book ->
            SectionTitle(book.title)
            book.units.filter { unitId == null || it.id == unitId }.forEach { unit ->
                SourceItemCard(unit.title, tr("${unit.researches.size} indexed researches · learning pending", "${unit.researches.size} أبحاث مفهرسة · التعلّم قيد الإعداد"),
                    Modifier.testTag("g9-progress-${unit.id}"), Icons.Outlined.Insights) { open(if (unitId == null) "progress/unit/${unit.id}" else unit.route) }
            }
        }
        com.ahmadmol.enmath.features.planner.PlanSummary(state, planner, open)
        if (catalog.stage == StudyStage.BAC) {
            SectionTitle(tr("Demo learning · independent evidence", "التعلّم التجريبي · أدلة مستقلة"))
            state.books.forEach { book -> SourceItemCard(book.title.text(),
                tr("${book.lessons.count { it.id in StudentCatalog.completed(state) }}/${book.lessons.size} demo lessons completed", "${book.lessons.count { it.id in StudentCatalog.completed(state) }}/${book.lessons.size} درسًا تجريبيًا مكتملًا"),
                icon = Icons.Outlined.Science) { open("demo/book/${book.id}") } }
            StudentCatalog.attempts(state).asReversed().forEach { attempt -> SourceItemCard(
                state.practiceSets.find { it.id == attempt.setId }?.title?.text().orEmpty(),
                tr("${attempt.correct}/${attempt.total} correct · ${attempt.seconds}s", "${attempt.correct}/${attempt.total} صحيحة · ${attempt.seconds} ثانية"),
                icon = Icons.AutoMirrored.Outlined.FactCheck) { open("practice/${attempt.setId}/result?attempt=${attempt.commandId}") } }
        }
        Button(
            onClick = { open("planner") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(20.dp))
                Text(tr("My study plan", "خطتي الدراسية"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        OutlinedButton(
            onClick = { open("review") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(20.dp))
                Text(tr("Mistake review", "مراجعة الأخطاء"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable fun SourceReviewHub(state: LearningState, id: String, open: (String) -> Unit, laws: Boolean = false) {
    val catalog = SourceCurricula.forState(state)
    val dark = isDarkTheme()
    ScreenPage(Modifier.testTag("screen-source-review")) {
        if (catalog == null || (id != "all" && catalog.book(id) == null && catalog.unit(id) == null)) { CatalogUnavailable(); return@ScreenPage }
        StageHero(catalog.stage, if (laws) tr("Laws and proofs", "القوانين والبراهين") else tr("Review and exam preparation", "المراجعة والاستعداد للامتحان"),
            catalog.book(id)?.title ?: catalog.unit(id)?.title ?: tr("Your stage's source index", "فهرس مصدر مرحلتك"))
        SourcePendingCard(tr("Reviewed material is required", "بانتظار المحتوى المعتمد"),
            if (laws) tr("Verified laws, conditions and proofs will appear with their source references after review.", "تظهر القوانين وشروطها والبراهين بمراجعها المصدرية بعد تدقيقها.")
            else tr("No approved source exam is published. You can browse references, set review goals and revisit actual saved mistakes.", "لم يُنشر نموذج امتحاني معتمد للمصدر بعد. يمكنك تصفح المراجع ووضع أهداف مراجعة والرجوع إلى الأخطاء المحفوظة فعليًا."))

        if (!laws && catalog.stage == StudyStage.BAC) {
            Card(
                onClick = { open("study/laws") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, if (dark) Color(0xFF2A4374) else Color(0xFFCCE1FF)),
                colors = CardDefaults.cardColors(containerColor = if (dark) Color(0xFF14203D) else Color(0xFFF3F8FF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = tr("Laws and proofs status", "حالة القوانين والبراهين"),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = sourceAccent()
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = sourceAccent(),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Section header with "عرض الكل >" on the left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tr("Review references", "مراجع المراجعة"),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = if (dark) Color.White else Color(0xFF0F172A)
            )
            if (id != "all") {
                TextButton(onClick = { open("study/review/all") }) {
                    Text(
                        text = tr("View all >", "عرض الكل >"),
                        color = sourceAccent(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        catalog.units.filter { id == "all" || it.id == id || it.bookId == id }.forEach { unit ->
            SourceItemCard(
                title = unit.title,
                subtitle = tr("${unit.researches.size} references · pending content review", "${unit.researches.size} مراجع · المحتوى قيد المراجعة"),
                icon = Icons.AutoMirrored.Outlined.TrendingUp
            ) { open(unit.route) }
        }

        Button(
            onClick = { open("planner") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(20.dp))
                Text(tr("Plan my review", "خطّط لمراجعتي"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        OutlinedButton(
            onClick = { open("review") },
            modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, sourceAccent()),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = sourceAccent())
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.BookmarkBorder, null, modifier = Modifier.size(20.dp))
                Text(tr("Saved mistakes", "الأخطاء المحفوظة"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable fun SourceSavedLibrary(state: LearningState, workspace: SourceWorkspaceRepository?, open: (String) -> Unit) {
    if (state.user?.studyStage == StudyStage.G9 && state.user?.type != com.ahmadmol.enmath.core.model.AccountType.Teacher) {
        com.ahmadmol.enmath.features.library.Grade9SavedReferences(state, workspace, open); return
    }
    val scope = StudentCatalog.scope(state)
    val change = workspace?.changes?.collectAsState()?.value
    val read = remember(scope, change) { scope?.let { workspace?.read(it) } }
    val catalog = SourceCurricula.forState(state)
    SectionTitle(tr("Saved source references", "مراجع المصدر المحفوظة"))
    val ids = (read as? SourceWorkspaceRead.Ready)?.workspace?.savedIds.orEmpty()
    if (read == SourceWorkspaceRead.StorageError) Text(tr("Saved references could not be read. Previous data is preserved.", "تعذّر قراءة المراجع المحفوظة. البيانات السابقة محفوظة."))
    else if (ids.isEmpty()) Text(tr("Save a research from its details to find it here.", "احفظ بحثًا من صفحة تفاصيله ليظهر هنا."))
    ids.mapNotNull { catalog?.research(it) }.forEach { research -> SourceItemCard(research.title,
        if (catalog?.stage == StudyStage.G9 && research.id == Grade9Pilot.lessonId) tr("Saved reference · original pilot available", "مرجع محفوظ · درس أصلي تجريبي متاح") else
            tr("Saved reference · content pending review", "مرجع محفوظ · المحتوى قيد المراجعة"), Modifier.testTag("source-saved-${research.id}")) { open(research.route) } }
}
