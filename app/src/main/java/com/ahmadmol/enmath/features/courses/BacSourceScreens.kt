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
    ScreenPage(Modifier.testTag("screen-study")) {
        PageHeading(tr("Scientific baccalaureate", "الثالث الثانوي العلمي"), tr("Mathematics", "الرياضيات"))
        BacSourceNotice()
        Text(tr("14 units · 63 indexed lessons · 0 reviewed lessons", "14 وحدة · 63 بحثًا مفهرسًا · 0 أبحاث معتمدة"))
        BacSourceCatalog.books.forEach { book ->
            val lessons = BacSourceCatalog.lessons.count { it.sourceId == book.id }
            BacSourceRow(tr("Part ${book.volume}", "الجزء ${book.volume}"),
                tr("7 units · $lessons lessons · ${book.pdfPages} reference pages", "7 وحدات · $lessons بحثًا · ${book.pdfPages} صفحة مرجعية"),
                "catalog-book-${book.existingBookId}") { open("book/${book.existingBookId}") }
        }
        OutlinedButton({ open("study/demo") }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("bac-legacy-lessons")) {
            Text(tr("Existing demo lessons and progress", "الدروس التجريبية السابقة وتقدمك"))
        }
        OutlinedButton({ open("planner") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("My study plan", "خطتي الدراسية")) }
    }
}

@Composable fun BacSourceBookScreen(id: String, open: (String) -> Unit) {
    val book = BacSourceCatalog.book(id)
    ScreenPage(Modifier.testTag("screen-book")) {
        if (book == null) { Text(tr("Source unavailable", "المصدر غير متاح")); return@ScreenPage }
        PageHeading(tr("Baccalaureate mathematics", "رياضيات البكالوريا"), tr("Part ${book.volume}", "الجزء ${book.volume}"))
        BacSourceNotice()
        BacSourceCatalog.units.filter { it.sourceId == book.id }.forEach { unit ->
            BacSourceRow("${unit.order}. ${unit.title}", sourceSpan(unit.span) + " · " +
                tr("${unit.lessonIds.size} indexed lessons", "${unit.lessonIds.size} أبحاث مفهرسة"), "book-topic-${unit.id}") { open("topic/${unit.id}") }
        }
        if (book.volume == 2) Text(tr("The supplied inventory locates general tests at printed/PDF 205–214; individual questions are unreviewed.",
            "يحدد الجرد المرفق اختبارات عامة في المطبوعة/PDF 205–214؛ الأسئلة الفردية غير مدققة."))
    }
}

@Composable fun BacSourceUnitScreen(id: String, open: (String) -> Unit, back: () -> Unit) {
    val unit = BacSourceCatalog.unit(id)
    ScreenPage(Modifier.testTag("screen-topic")) {
        RefTopBar(tr("Source unit", "وحدة من المصدر"), back, { open("search") }, { open("notifications") })
        if (unit == null) { Text(tr("Unit unavailable", "الوحدة غير متاحة")); return@ScreenPage }
        PageHeading(tr("Unit ${unit.order}", "الوحدة ${unit.order}"), unit.title)
        BacSourceNotice()
        Text(sourceSpan(unit.span))
        if (unit.issues.isNotEmpty()) Text(tr("Source page boundary needs review. No end page was invented.", "حدود صفحات المصدر بحاجة إلى مراجعة. لم تُفترض نهاية بديلة."))
        unit.lessonIds.mapNotNull(BacSourceCatalog::lesson).forEach { lesson ->
            BacSourceRow(lesson.title, sourceSpan(lesson.span) + " · " + tr("Pending content review", "قيد تدقيق المحتوى"),
                "topic-lesson-${lesson.id}") { open("lesson/${lesson.id}") }
        }
        unit.sectionIds.mapNotNull(BacSourceCatalog::section).forEach { section ->
            BacSourceRow(section.title, sourceSpan(section.span) + " · " + tr("Items not extracted", "الأسئلة الفردية غير مستخرجة"),
                "bac-section-${section.id}") { open("practice/${section.id}") }
        }
        if (unit.sectionIds.none { BacSourceCatalog.section(it)?.kind == "activity" })
            Text(tr("No separate unit activity section is listed in the supplied index.", "لا يسرد الفهرس المرفق قسم أنشطة وحدة مستقلًا هنا."))
    }
}

@Composable fun BacSourceLessonScreen(id: String, state: LearningState, planner: PlannerRepository?, open: (String) -> Unit, back: () -> Unit) {
    val lesson = BacSourceCatalog.lesson(id)
    val scope = remember(state.user?.localId, state.generation) { StudentCatalog.scope(state) }
    var result by remember(id, scope) { mutableStateOf<PlanWrite?>(null) }
    ScreenPage(Modifier.testTag("screen-lesson")) {
        RefTopBar(tr("Source reference", "مرجع البحث"), back, { open("search") }, { open("notifications") })
        if (lesson == null) { Text(tr("Lesson unavailable", "البحث غير متاح")); return@ScreenPage }
        PageHeading(tr("Indexed lesson", "بحث مفهرس"), lesson.title)
        BacSourceNotice()
        Text(tr("Source edition ${lesson.edition}", "طبعة المصدر ${lesson.edition}"))
        Text(sourceSpan(lesson.span), Modifier.testTag("bac-source-pages"))
        if (lesson.id in listOf("bac-1-u05-l04", "bac-1-u05-l05")) {
            EducationalInfoBanner(tr("Verified shared start: printed page 163 / PDF 164. These are two separate lessons.",
                "بداية مشتركة موثقة: المطبوع 163 / PDF 164. البحثان منفصلان."))
            if (lesson.id == "bac-1-u05-l05") Text(tr("End page 167 is retained pending detailed boundary review.", "أُبقيت صفحة النهاية 167 بانتظار تدقيق الحدود التفصيلي."))
        }
        if (lesson.issues.isNotEmpty()) EducationalInfoBanner(tr("This page range is shared with another entry and needs manual verification.",
            "نطاق الصفحات مشترك مع بحث آخر ويحتاج تحققًا يدويًا."))
        if (BacSourceCatalog.unit(lesson.unitId)?.issues?.isNotEmpty() == true)
            Text(tr("The containing unit also has an invalid end-page reference.", "مرجع نهاية الوحدة الحاوية غير صالح أيضًا ويحتاج مراجعة."))
        Text(tr("Explanations, definitions, proofs, examples, activities, figures and exercises require review. No completion or gems are available for this reference.",
            "الشرح والتعاريف والبراهين والأمثلة والأنشطة والرسوم والتمارين بحاجة إلى تدقيق. لا يتاح إكمال هذا المرجع أو منح جواهر عليه."))
        OutlinedButton({ if (scope != null && planner != null) result = planner.pinSource(scope, requireNotNull(BacSourceCatalog.ref(id))) },
            Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("bac-plan-source"), enabled = scope != null && planner != null) { Text(tr("Keep reference in my plan", "حفظ المرجع في خطتي")) }
        result?.let { Text(when (it) {
            PlanWrite.SAVED, PlanWrite.ALREADY_SAVED -> tr("Reference saved; content remains under review.", "حُفظ المرجع؛ المحتوى ما زال قيد المراجعة.")
            PlanWrite.OWNER_CHANGED -> tr("File changed; operation rejected.", "تغيّر الملف؛ رُفضت العملية.")
            else -> tr("Reference could not be saved. Previous data was preserved.", "تعذّر حفظ المرجع. حُفظت البيانات السابقة.")
        }) }
        OutlinedButton({ open("practice/$id") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Exercise status", "حالة التمارين")) }
        OutlinedButton({ open("solver") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Independent solver tool", "المحلل كأداة مستقلة")) }
        OutlinedButton({ open("explore") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Independent graph tool", "أداة الرسم المستقلة")) }
        Text(tr("These tools do not provide the textbook's official answer key or complete this lesson.", "الأداتان لا توفران مفتاح حلول الكتاب ولا تسجلان إكمال البحث."))
        val related = BacSourceCatalog.relatedLegacy(id)
        if (related.isNotEmpty()) {
            SectionTitle(tr("Related existing demo lessons", "دروس تجريبية سابقة ذات صلة"))
            Text(tr("Related material does not establish equivalence or transfer completion.", "الصلة لا تثبت التكافؤ ولا تنقل الإكمال إلى بحث المصدر."))
            related.forEach { old -> BacSourceRow(old.title.text(), tr("Existing demo", "درس تجريبي سابق"), "bac-related-${old.id}") { open("lesson/${old.id}") } }
        }
    }
}

@Composable fun BacSourcePracticeStatus(id: String) {
    val lesson = BacSourceCatalog.lesson(id)
    val section = BacSourceCatalog.section(id)
    ScreenPage(Modifier.testTag("screen-bac-source-pending")) {
        PageHeading(tr("Source reference", "مرجع المصدر"), lesson?.title ?: section?.title.orEmpty())
        BacSourceNotice()
        (lesson?.span ?: section?.span)?.let { Text(sourceSpan(it)) }
        Text(tr("Individual question numbers, subparts, diagrams, independent solutions and checkers are unreviewed. No scored test is published.",
            "أرقام الأسئلة الفردية وفروعها ورسومها والحلول المستقلة ومعايير التصحيح غير مدققة. لا يوجد اختبار منشور للتقييم."))
    }
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
