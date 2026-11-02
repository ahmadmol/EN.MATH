package com.ahmadmol.enmath.features.planner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable fun SourceReferencePicker(state: LearningState, selected: ContentRef?, select: (ContentRef?) -> Unit) {
    val catalog = SourceCurricula.forState(state)
    var query by rememberSaveable { mutableStateOf("") }
    var expandedBook by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedUnit by rememberSaveable { mutableStateOf<String?>(null) }
    var demo by rememberSaveable { mutableStateOf(false) }
    Text(tr("Optional source reference", "مرجع مصدر اختياري"), style = MaterialTheme.typography.titleMedium)
    selected?.let { ref ->
        Text(if (ref.kind == ContentKind.SOURCE_INDEX) StudentCatalog.sourceTitle(ref).orEmpty() else state.lesson(ref.lessonId)?.title?.text().orEmpty())
        TextButton({ select(null) }, Modifier.heightIn(min = 48.dp)) { Text(tr("Remove link", "إلغاء الربط")) }
    }
    OutlinedTextField(query, { query = it }, label = { Text(tr("Find a book, unit or research", "ابحث عن كتاب أو وحدة أو بحث")) },
        modifier = Modifier.fillMaxWidth().testTag("plan-source-search"))
    if (catalog != null && query.isNotBlank()) {
        val matches = catalog.search(query).map { it.second }.toSet()
        val researches = catalog.researches.filter { it.route in matches || catalog.unit(it.unitId)?.route in matches ||
            catalog.unit(it.unitId)?.let { u -> catalog.book(u.bookId)?.route in matches } == true }
        Text(tr("${researches.size} source references", "${researches.size} مراجع مصدرية"))
        researches.take(12).forEach { research -> FilterChip(selected == research.ref, { select(research.ref) },
            label = { Text(research.title) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) }
        if (researches.size > 12) Text(tr("Refine your search to see the remaining references.", "حدّد عبارة البحث أكثر لعرض بقية المراجع."))
        if (researches.isEmpty()) Text(tr("No matching source reference", "لا يوجد مرجع مصدر مطابق"))
    } else catalog?.books?.forEach { book ->
        OutlinedButton({ expandedBook = if (expandedBook == book.id) null else book.id; expandedUnit = null },
            Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plan-book-${book.id}")) { Text(book.title) }
        if (expandedBook == book.id) book.units.forEach { unit ->
            TextButton({ expandedUnit = if (expandedUnit == unit.id) null else unit.id }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text("${unit.title} · ${unit.researches.size}")
            }
            if (expandedUnit == unit.id) unit.researches.forEach { research ->
                FilterChip(selected == research.ref, { select(research.ref) }, label = { Text(research.title) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("plan-research-${research.id}"))
            }
        }
    }
    if (state.lessons.isNotEmpty()) {
        TextButton({ demo = !demo }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Demo lessons · separate learning", "الدروس التجريبية · تعلّم مستقل")) }
        if (demo) state.lessons.filter { query.isBlank() || LearningRules.search(it.title.en + it.title.ar).contains(LearningRules.search(query)) }.forEach { lesson ->
            FilterChip(selected?.lessonId == lesson.id, { select(StudentCatalog.ref(state, lesson.id)) },
                label = { Text(lesson.title.text()) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
        }
    }
}
