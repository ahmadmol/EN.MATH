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
    SourceStudyScreen(StudyStage.G9, open)
}

@Composable
fun Grade9BookScreen(id: String, open: (String) -> Unit) {
    SourceBookScreen(StudyStage.G9, id, open)
}

@Composable
fun Grade9UnitScreen(id: String, open: (String) -> Unit) {
    SourceUnitScreen(StudyStage.G9, id, open)
}

@Composable
fun Grade9LessonScreen(id: String, state: LearningState, open: (String) -> Unit, planner: PlannerRepository?) {
    SourceResearchScreen(id, state, planner, null, open)
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
    SourceExercisePending(StudyStage.G9, id)
}

@Composable
fun Grade9ProgressScreen(state: LearningState, open: (String) -> Unit) {
    SourceProgressScreen(state, open)
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
