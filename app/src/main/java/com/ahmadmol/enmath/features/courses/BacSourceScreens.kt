package com.ahmadmol.enmath.features.courses

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.StudyStage

@Composable fun BacSourceNotice() {
    EducationalInfoBanner(tr("Source index only · edition comparison and content review pending",
        "فهرس مصدر فقط · مطابقة الطبعة وتدقيق المحتوى معلّقان"))
    Text(tr("Part 1: 2023–2024 · Part 2: 2021–2022. Not verified for 2026–2027.",
        "الجزء الأول: 2023–2024 · الجزء الثاني: 2021–2022. لم تُثبت مطابقة 2026–2027."))
}

@Composable private fun sourceSpan(span: SourceSpan) = if (span.valid)
    tr("Printed ${span.printedFrom}–${span.printedTo} · PDF ${span.pdfFrom}–${span.pdfTo}",
        "المطبوعة ${span.printedFrom}–${span.printedTo} · PDF ${span.pdfFrom}–${span.pdfTo}")
    else tr("Printed start ${span.printedFrom} · PDF start ${span.pdfFrom}; end page needs verification",
        "بداية المطبوعة ${span.printedFrom} · بداية PDF ${span.pdfFrom}؛ نهاية النطاق تحتاج تحققًا")

@Composable fun BacSourceRow(title: String, detail: String, tag: String, open: () -> Unit) {
    CurriculumTile(title, detail, Modifier.testTag(tag), indigo = true, click = open)
}

@Composable fun BacSourceStudyScreen(open: (String) -> Unit) {
    SourceStudyScreen(StudyStage.BAC, open)
}

@Composable fun BacSourceBookScreen(id: String, open: (String) -> Unit) {
    SourceBookScreen(StudyStage.BAC, id, open)
}

@Composable fun BacSourceUnitScreen(id: String, open: (String) -> Unit, back: () -> Unit) {
    SourceUnitScreen(StudyStage.BAC, id, open)
}

@Composable fun BacSourceLessonScreen(id: String, state: LearningState, planner: PlannerRepository?, open: (String) -> Unit, back: () -> Unit) {
    SourceResearchScreen(id, state, planner, null, open)
}

@Composable fun BacSourcePracticeStatus(id: String) {
    SourceExercisePending(StudyStage.BAC, id)
}

@Composable fun BacSourceProgress(state: LearningState) {
    if (state.user?.studyStage != StudyStage.BAC || !state.catalogAvailable) return
    AppCard(Modifier.testTag("bac-source-coverage")) {
        Text(tr("Textbook source index", "فهرس مصدر الكتابين"), style = MaterialTheme.typography.titleLarge)
        Text(tr("14 units · 63 lessons · 27 activity/exercise sections · 470 page records", "14 وحدة · 63 بحثًا · 27 قسم أنشطة وتمارين · 470 سجل صفحة"))
        Text(tr("Reviewed source lessons: 0/63 (0%). Page content reviewed: 0/470.", "أبحاث المصدر المعتمدة: 0/63 (0٪). الصفحات المدققة المحتوى: 0/470."))
        Text(tr("Existing demo progress remains separate.", "تقدم الدروس التجريبية السابقة محفوظ بصورة مستقلة."))
    }
}

@Composable fun BacSourceLibraryLinks(state: LearningState, planner: PlannerRepository?, open: (String) -> Unit) {
    val scope = StudentCatalog.scope(state)
    val changed = planner?.changes?.collectAsState()?.value
    val plan = remember(scope, changed) { scope?.let { (planner?.read(it) as? PlanRead.Ready)?.plan } }
    if (state.user?.studyStage != StudyStage.BAC) return
    plan?.tasks?.mapNotNull { it.lesson }?.filter { it.kind == ContentKind.SOURCE_INDEX && StudentCatalog.acceptsReference(state, it) }
        ?.distinct()?.forEach { ref -> BacSourceRow(BacSourceCatalog.title(ref).orEmpty(), tr("Source reference in your plan · pending review", "مرجع مصدر محفوظ في خطتك · قيد المراجعة"),
            "library-source-${ref.lessonId}") { open("lesson/${ref.lessonId}") } }
}
