package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.gemBalanceText
import com.ahmadmol.enmath.features.planner.PlanSummary
import kotlinx.coroutines.launch

@Composable
fun Grade9RoadmapHomeScreen(state: LearningState, open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(), planner: PlannerRepository? = null,
    workspace: SourceWorkspaceRepository? = null) = Grade9LearningPath(state, open, gems, planner, workspace)

/** Source browsing, bookmarks, learning evidence and the real wallet remain separate. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaccalaureateRoadmapHomeScreen(state: LearningState, open: (String) -> Unit,
    gems: LocalGemsUiState = LocalGemsUiState(), planner: PlannerRepository? = null,
    workspace: SourceWorkspaceRepository? = null) {
    val catalog = SourceCurricula.forStage(StudyStage.BAC)
    val scope = StudentCatalog.scope(state)
    val changed = workspace?.changes?.collectAsStateWithLifecycle()?.value
    val read = remember(scope, workspace, changed) { scope?.let { workspace?.read(it) } }
    val saved = (read as? SourceWorkspaceRead.Ready)?.workspace
    val visited = saved?.visitedId?.let(catalog::research)
    val active = visited ?: catalog.researches.firstOrNull()
    val list = rememberLazyListState()
    val jobs = rememberCoroutineScope()
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 640.dp).fillMaxSize().testTag("screen-home"), state = list,
            contentPadding = PaddingValues(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.lg)) {
            item("header") {
                Column(verticalArrangement = Arrangement.spacedBy(Space.md)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("Welcome, ", "أهلًا، ") + "\u2068" + state.user?.name.orEmpty() + "\u2069",
                                style = MaterialTheme.typography.titleLarge)
                            Text(studentStageLabel(StudyStage.BAC), style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton({ open("notifications") }, Modifier.size(Space.touch).testTag("home-notifications")) {
                            Icon(Icons.Outlined.Notifications, tr("Notifications", "الإشعارات"))
                        }
                    }
                    OutlinedButton({ open("gems/wallet") }, Modifier.heightIn(min = Space.touch).testTag("home-gems-badge")) {
                        Icon(Icons.Outlined.Diamond, null); Spacer(Modifier.width(Space.sm))
                        Text(gemBalanceText(gems, LocalGemsPolicy.owner(state.user), state.language) + " · " + tr("Gems wallet", "محفظة الجواهر"))
                    }
                }
            }
            if (read == SourceWorkspaceRead.StorageError) item("storage-error") {
                EducationalInfoBanner(tr("Your source history could not be read. Existing data is preserved; restart before retrying.",
                    "تعذّرت قراءة سجل المراجع. بياناتك محفوظة؛ أعد التشغيل قبل المحاولة."))
            }
            item("next") {
                AppCard(tonal = true) {
                    Text(if (visited == null) tr("Start with a source reference", "ابدأ بمرجع من الكتاب") else tr("Return to your last reference", "ارجع إلى آخر مرجع"),
                        style = MaterialTheme.typography.titleLarge)
                    Text(active?.title.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    Text(tr("This path indexes the two books. Opening a reference does not count as lesson completion.",
                        "يفهرس هذا المسار الكتابين. فتح المرجع لا يُحتسب إكمالًا لدرس."), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PrimaryButton(tr("Open source reference", "فتح مرجع المصدر"), { open(active?.route ?: "study") }, Modifier.testTag("student-continue"))
                }
            }
            item("tools") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(Space.sm), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
                    listOf(Triple("solver", tr("Solver", "المحلل"), "home-quick-solver"),
                        Triple("explore", tr("Graph lab", "مختبر الرسم"), "home-graph"),
                        Triple("library", tr("My library", "مكتبتي"), "home-library"),
                        Triple("progress", tr("My progress", "تقدمي"), "home-all-progress")).forEach { (route, title, tag) ->
                        OutlinedButton({ open(route) }, Modifier.heightIn(min = Space.touch).testTag(tag)) { Text(title) }
                    }
                }
            }
            item("plan") { PlanSummary(state, planner, open) }
            catalog.books.forEach { book ->
                item("book-" + book.id) {
                    AppCard(Modifier.testTag("home-book-" + book.id), onClick = { open(book.route) }, tonal = true) {
                        Text(book.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(tr("Source index · ", "فهرس المصدر · ") + book.units.size + tr(" units", " وحدات"))
                    }
                }
                book.units.forEach { unit ->
                    item("unit-" + unit.id) {
                        AppCard(Modifier.testTag("home-unit-" + unit.id), onClick = { open(unit.route) }) {
                            Text(tr("Unit ", "الوحدة ") + unit.order + " · " + unit.title, style = MaterialTheme.typography.titleMedium)
                            Text(tr("Source references under review", "مراجع مصدر قيد المراجعة"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    itemsIndexed(unit.researches, key = { _, research -> "source-" + research.id }) { index, research ->
                        val status = when {
                            research.id in saved?.savedIds.orEmpty() -> tr("Saved source reference", "مرجع محفوظ")
                            research.id == visited?.id -> tr("Last visited source reference", "آخر مرجع تمت زيارته")
                            else -> tr("Source reference · under review", "مرجع مصدر · قيد المراجعة")
                        }
                        Column(Modifier.padding(start = if (index % 2 == 0) 0.dp else Space.lg)) {
                            OutlinedCard(onClick = { open(research.route) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = Space.touch).testTag("home-research-" + research.id)
                                    .semantics { stateDescription = status },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.outlinedCardColors(containerColor = if (research.id == active?.id)
                                    MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                                Row(Modifier.padding(Space.lg), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Space.md)) {
                                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        modifier = Modifier.size(Space.touch)) {
                                        Box(contentAlignment = Alignment.Center) { Icon(if (research.id in saved?.savedIds.orEmpty())
                                            Icons.Outlined.BookmarkBorder else Icons.AutoMirrored.Outlined.MenuBook, null) }
                                    }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.xs)) {
                                        Text(research.title, style = MaterialTheme.typography.titleSmall)
                                        Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (index < unit.researches.lastIndex) Box(Modifier.padding(start = Space.xl).width(2.dp).height(Space.md)
                                .background(MaterialTheme.colorScheme.outlineVariant))
                        }
                    }
                }
            }
            item("availability") {
                AppCard {
                    Text(tr("Learning availability", "حالة المحتوى التعليمي"), style = MaterialTheme.typography.titleLarge)
                    Text(tr("The full curriculum and official final exam are not published in this version. Limited demo learning has separate progress.",
                        "لم يُنشر المنهج الكامل والامتحان النهائي الرسمي في هذه النسخة. للتجربة التعليمية المحدودة تقدم مستقل."))
                    OutlinedButton({ open("study/demo") }, Modifier.fillMaxWidth().heightIn(min = Space.touch).testTag("home-practice")) {
                        Text(tr("Explore demo learning", "استكشاف التجربة التعليمية"))
                    }
                    TextButton({ jobs.launch { list.animateScrollToItem(0) } }, Modifier.heightIn(min = Space.touch)) {
                        Text(tr("Back to top of path", "العودة إلى بداية المسار"))
                    }
                }
            }
        }
    }
}

data class SourcePathEntry(
    val key: String,
    val research: SourceResearch?,
    val station: Int = 0,
    val hasNext: Boolean = false,
)

fun sourcePathEntries(catalog: SourceCurriculum): List<SourcePathEntry> {
    return catalog.units.flatMap { unit ->
        unit.researches.mapIndexed { index, research ->
            SourcePathEntry(
                key = "station-${research.id}",
                research = research,
                station = index,
                hasNext = index < unit.researches.size - 1
            )
        }
    }
}
