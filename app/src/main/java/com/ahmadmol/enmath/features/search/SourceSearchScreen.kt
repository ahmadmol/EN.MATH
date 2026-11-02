package com.ahmadmol.enmath.features.search

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable fun SourceSearchScreen(state: LearningState, onSearch: (String) -> Unit, open: (String) -> Unit) {
    var query by rememberSaveable(state.user?.localId, state.generation) { mutableStateOf("") }
    val catalog = SourceCurricula.forState(state)
    ScreenPage(Modifier.imePadding().testTag("screen-search")) {
        if (catalog == null) {
            SourcePendingCard(tr("The source index is unavailable", "فهرس المصدر غير متاح"),
                tr("Choose an active stage file or resolve the storage issue before searching.", "اختر ملف مرحلة نشطًا أو عالج مشكلة التخزين قبل البحث."))
            return@ScreenPage
        }
        StageHero(catalog.stage, tr("Search my curriculum", "ابحث في منهجي"),
            tr("Books, units and exact research references", "الكتب والوحدات ومراجع الأبحاث الفعلية"))
        RefSearchBar(query, { query = it }, tr("Book, unit or research", "كتاب أو وحدة أو بحث"))
        OutlinedButton({ onSearch(query) }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("save-search"), enabled = query.isNotBlank()) { Text(tr("Save this search", "حفظ البحث")) }
        if (query.isBlank()) {
            if (state.product.searches.isNotEmpty()) {
                SectionTitle(tr("Recent searches", "عمليات البحث الأخيرة"))
                state.product.searches.forEach { saved -> TextButton({ query = saved }, Modifier.heightIn(min = 48.dp)) { Text(saved) } }
            }
            SectionTitle(tr("Browse the source", "تصفّح المصدر"))
            catalog.books.forEach { book -> SourceItemCard(book.title,
                tr("${book.units.size} units · ${book.researches.size} indexed researches", "${book.units.size} وحدات · ${book.researches.size} أبحاث مفهرسة")) { open(book.route) } }
        } else {
            val results = catalog.search(query)
            SectionTitle(tr("Source index · ${results.size} matches", "فهرس المصدر · ${results.size} نتائج"))
            if (results.isEmpty()) SourcePendingCard(tr("No matching reference", "لا توجد مراجع مطابقة"),
                tr("The index is available. Try a shorter title or remove extra words.", "الفهرس متاح. جرّب جزءًا من العنوان أو قلّل كلمات البحث."))
            results.forEach { (title, route) ->
                val research = catalog.research(route.removePrefix("lesson/"))
                val breadcrumb = research?.let { r -> catalog.unit(r.unitId)?.let { u -> "${catalog.book(u.bookId)?.title} · ${u.title}" } }
                SourceItemCard(title, breadcrumb ?: tr("Source reference · content under review", "مرجع مصدر · المحتوى قيد المراجعة"),
                    Modifier.testTag("source-search-$route"), Icons.Outlined.Search) { onSearch(query); open(route) }
            }
            val term = LearningRules.search(query)
            val demo = state.lessons.filter { LearningRules.search(it.title.en + " " + it.title.ar).contains(term) }
            if (demo.isNotEmpty()) {
                SectionTitle(tr("Demo lessons · separate learning", "الدروس التجريبية · تعلّم مستقل"))
                demo.forEach { lesson -> SourceItemCard(lesson.title.text(), tr("Preserved demo · not a textbook source research", "تجربة محفوظة · ليست بحثًا من فهرس الكتاب"),
                    icon = Icons.Outlined.Science) { onSearch(query); open("lesson/${lesson.id}") } }
            }
            val questions = state.questions.filter { StudentCatalog.practiceForQuestion(state, it.id) != null &&
                LearningRules.search(it.prompt.en + " " + it.prompt.ar + " " + it.expression).contains(term) }
            if (questions.isNotEmpty()) {
                SectionTitle(tr("Demo practice questions", "أسئلة التدريب التجريبي"))
                questions.take(12).forEach { question -> SourceItemCard(question.prompt.text(), "\u2066${question.expression}\u2069",
                    icon = Icons.Outlined.Science) { onSearch(query); open("practice/${StudentCatalog.practiceForQuestion(state, question.id)?.id}") } }
            }
            val concepts = state.lessons.filter { LearningRules.search(it.rule.en + " " + it.rule.ar).contains(term) && it !in demo }
            if (concepts.isNotEmpty()) {
                SectionTitle(tr("Demo concepts", "مفاهيم التجربة التعليمية"))
                concepts.take(12).forEach { lesson -> SourceItemCard(lesson.title.text(), lesson.rule.text(),
                    icon = Icons.Outlined.Science) { onSearch(query); open("lesson/${lesson.id}") } }
            }
        }
    }
}
