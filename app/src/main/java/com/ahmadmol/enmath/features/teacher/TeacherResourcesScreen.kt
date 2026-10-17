package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.library.resourceKindLabel

@Composable
fun TeacherResourcesScreen(
    state: LearningState,
    onCreate: (String, String, String, ResourceKind) -> Unit,
    onShare: (String, String) -> Unit,
    open: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    var create by remember { mutableStateOf(false) }
    var share by remember { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableIntStateOf(0) }
    var kind by rememberSaveable { mutableIntStateOf(0) }
    val topics = state.books.flatMap { it.topics }
    ScreenPage(Modifier.testTag("screen-resources")) {
        PageHeading(label(TextKey.t_6127a49906), label(TextKey.t_4214608de0))
        AppCard {
            DestinationRow(
                tr("Curriculum & lessons", "المنهاج والدروس"),
                Icons.Outlined.MenuBook,
                { open(TeacherReferenceRoutes.Study) },
                Modifier.testTag("teacher-resource-study"),
            )
            DestinationRow(
                tr("Detailed solver", "الحل المفصل"),
                Icons.Outlined.Calculate,
                { open(TeacherReferenceRoutes.Solver) },
                Modifier.testTag("teacher-resource-solver"),
            )
            DestinationRow(
                tr("Function lab", "مختبر الدوال"),
                Icons.Outlined.Science,
                { open(TeacherReferenceRoutes.Lab) },
                Modifier.testTag("teacher-resource-lab"),
            )
        }
        PrimaryButton(label(TextKey.t_0f8f757d87), { create = true })
        if (state.product.resources.isEmpty())
            EmptyState(label(TextKey.t_262b54dcff), label(TextKey.t_237968f024))
        SearchField(query, { query = it })
        ChipRow(
            listOf(tr("All", "الكل")) + ResourceKind.entries.map { resourceKindLabel(it) },
            filter,
            { filter = it },
        )
        val resources =
            state.product.resources.filter {
                LearningRules.search(it.title.en + it.title.ar)
                    .contains(LearningRules.search(query)) &&
                    (filter == 0 || it.kind == ResourceKind.entries[filter - 1])
            }
        if (resources.isEmpty() && state.product.resources.isNotEmpty())
            EmptyState(
                tr("No results", "لا توجد نتائج"),
                tr("Try another filter or search term.", "جرّب تصنيفاً أو كلمة بحث أخرى."),
            )
        resources.forEach { r ->
            AppCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Space.md),
                ) {
                    Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(r.title.text(), style = MaterialTheme.typography.titleMedium)
                        Text(
                            resourceKindLabel(r.kind),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(r.body.text())
                Text(label(TextKey.t_6422ef9e47, r.classIds.size))
                SecondaryButton(
                    label(TextKey.t_7c795eb518),
                    { share = r.id },
                    Modifier.testTag("share-${r.id}"),
                )
            }
        }
    }
    if (create)
        AlertDialog(
            onDismissRequest = { create = false },
            title = { Text(label(TextKey.t_51189c9009)) },
            text = {
                DialogColumn {
                    AppTextField(
                        title,
                        { title = it },
                        label(TextKey.t_111b3c1828),
                        Modifier.testTag("resource-title"),
                    )
                    AppTextField(
                        body,
                        { body = it },
                        label(TextKey.t_6eadb04284),
                        Modifier.testTag("resource-body"),
                        singleLine = false,
                    )
                    ChipRow(ResourceKind.entries.map { resourceKindLabel(it) }, kind, { kind = it })
                    ChipRow(topics.map { it.title.text() }, topic, { topic = it })
                }
            },
            confirmButton = {
                TextButton(
                    {
                        onCreate(title, body, topics[topic].id, ResourceKind.entries[kind])
                        create = false
                        title = ""
                        body = ""
                    },
                    enabled = title.isNotBlank() && body.isNotBlank(),
                ) {
                    Text(label(TextKey.t_ab3669edfa))
                }
            },
        )
    if (share.isNotBlank())
        AlertDialog(
            onDismissRequest = { share = "" },
            title = { Text(label(TextKey.t_7c795eb518)) },
            text = {
                DialogColumn {
                    state.product.classes.forEach { c ->
                        TextButton({
                            onShare(share, c.id)
                            share = ""
                        }) {
                            Text(c.name.text())
                        }
                    }
                }
            },
            confirmButton = { TextButton({ share = "" }) { Text(label(TextKey.t_823a3ad20c)) } },
        )
}
