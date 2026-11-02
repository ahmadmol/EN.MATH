package com.ahmadmol.enmath.features.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*

@Composable
fun Grade9SearchScreen(state: LearningState, onSearch: (String) -> Unit, open: (String) -> Unit) {
    var query by rememberSaveable(state.activeStudentId) { mutableStateOf("") }
    ScreenPage(Modifier.testTag("screen-search-g9")) {
        PageHeading(tr("Grade 9 mathematics", "رياضيات التاسع"), tr("Search the index", "البحث في الفهرس"))
        RefSearchBar(query, { query = it }, tr("Book, unit or lesson", "كتاب أو وحدة أو بحث"))
        val results = G9SourceCatalog.search(query)
        if (query.isBlank()) {
            state.product.searches.forEach { saved -> TextButton({ query = saved }) { Text(saved) } }
        } else {
            if (results.isEmpty()) Text(tr("No matching references", "لا توجد مراجع مطابقة"))
            results.forEach { result ->
                AppCard(Modifier.fillMaxWidth().testTag("g9-search-${result.route}"), onClick = {
                    onSearch(query); open(result.route)
                }) {
                    Text(result.title, style = MaterialTheme.typography.titleMedium)
                    Text(tr("Index reference · content in preparation", "مرجع في الفهرس · المحتوى قيد الإعداد"))
                }
            }
        }
    }
}
