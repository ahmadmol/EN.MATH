package com.ahmadmol.enmath.features.teacher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
) {
    var create by remember { mutableStateOf(false) }
    var share by remember { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableIntStateOf(0) }
    var kind by rememberSaveable { mutableIntStateOf(0) }
    val topics = state.books.flatMap { it.topics }
    ScreenPage(Modifier.testTag("screen-resources")) {
        PageHeading(label(TextKey.t_6127a49906), label(TextKey.t_4214608de0))
        PrimaryButton(label(TextKey.t_0f8f757d87), { create = true })
        if (state.product.resources.isEmpty())
            EmptyState(label(TextKey.t_262b54dcff), label(TextKey.t_237968f024))
        state.product.resources.forEach { r ->
            AppCard {
                Text(r.title.text(), style = MaterialTheme.typography.titleLarge)
                Text(resourceKindLabel(r.kind))
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
