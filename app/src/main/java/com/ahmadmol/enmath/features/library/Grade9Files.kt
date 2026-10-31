package com.ahmadmol.enmath.features.library

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.features.student.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.util.UUID

@Composable fun Grade9FilesEntry(open: (String) -> Unit) {
    OutlinedButton({ open("g9/files") }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-open-files")) {
        Text(tr("My PDF / Word files", "ملفاتي PDF / Word"))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun Grade9FilesScreen(state: LearningState, repository: Grade9StudyRepository?, open: (String) -> Unit) {
    val context = LocalContext.current
    val scope = StudentCatalog.scope(state)
    val read = rememberG9Study(state, repository)
    val data = (read as? G9StudyRead.Ready)?.data
    var classification by rememberSaveable(scope, stateSaver = G9ResourceSelectionSaver) { mutableStateOf(G9ResourceSelection()) }
    var favoritesOnly by rememberSaveable(scope) { mutableStateOf(false) }
    var showClassification by rememberSaveable(scope) { mutableStateOf(false) }
    var editing by remember(scope) { mutableStateOf<StudentFile?>(null) }
    var fileWriteFailure by remember(scope) { mutableStateOf(false) }
    var pending by remember(scope) { mutableStateOf<StudentScope?>(null) }
    var error by remember(scope) { mutableStateOf(false) }
    var removing by remember(scope) { mutableStateOf<StudentFile?>(null) }
    var query by rememberSaveable(scope?.ownerId, scope?.generation) { mutableStateOf("") }
    var filter by rememberSaveable(scope?.ownerId, scope?.generation) { mutableStateOf(G9FileFilter.ALL) }
    var importing by remember(scope) { mutableStateOf(false) }
    val jobs = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val owner = pending; pending = null
        if (uri != null && owner != null && repository?.read(owner) is G9StudyRead.Ready) {
            importing = true
            jobs.launch {
                error = withContext(Dispatchers.IO) { runCatching {
                require(uri.scheme == "content")
                val mime = requireNotNull(context.contentResolver.getType(uri))
                require(mime in Grade9StudyRepository.allowedMimes)
                var name: String? = null; var bytes: Long? = null
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val ni = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val si = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (ni >= 0 && !cursor.isNull(ni)) name = cursor.getString(ni)
                        if (si >= 0 && !cursor.isNull(si)) bytes = cursor.getLong(si)
                    }
                }
                val clean = name?.filterNot { it.isISOControl() }?.take(256)?.takeIf(String::isNotBlank) ?: "Document"
                val file = StudentFile(UUID.randomUUID().toString(), uri.toString(), clean, mime, bytes)
                require(Grade9StudyRepository.validFile(file))
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                repository.addFile(owner, file) in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)
                }.getOrDefault(false).not() }
                importing = false
            }
        }
    }
    Grade9Scaffold("screen-g9-files") {
        item { Text(tr("My study files", "ملفات دراستي"), style = MaterialTheme.typography.headlineSmall)
            Text(tr("Keep your PDF and Word links in this profile. Local or downloaded documents can work offline; cloud providers may need a connection.",
                "احتفظ بروابط PDF وWord في هذا الملف الشخصي. يمكن قراءة المستندات المحلية أو المنزّلة دون اتصال؛ وقد يحتاج المزوّد السحابي إلى الإنترنت.")) }
        if (data == null) { item { G9StudyError(read) }; return@Grade9Scaffold }
        item { Button({ pending = scope; error = false
            try { picker.launch(Grade9StudyRepository.allowedMimes.toTypedArray()) }
            catch (_: ActivityNotFoundException) { pending = null; error = true }
        }, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !importing) { Text(tr("Add a PDF / Word file", "أضف ملف PDF / Word")) }
            if (importing) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error) Text(tr("Could not save access to this document. Choose a supported file from a provider that permits lasting read access.",
                "تعذّر حفظ الوصول إلى المستند. اختر ملفًا مدعومًا من مزوّد يسمح بوصول قراءة مستمر."), color = MaterialTheme.colorScheme.error)
            OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), label = { Text(tr("Search my files", "ابحث في ملفاتي")) })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                G9FileFilter.entries.forEach { kind -> FilterChip(filter == kind, { filter = kind }, label = {
                    Text(when(kind) { G9FileFilter.ALL -> tr("All", "الكل"); G9FileFilter.PDF -> "PDF"; G9FileFilter.WORD -> "Word" })
                }) }
            }
        }
        item {
            TextButton({showClassification = !showClassification}) {Text(tr("Filter book, unit and topic", "تصفية الكتاب والوحدة والبحث"))}
            if (showClassification) G9ReferenceFilters(classification) {classification=it}
            FilterChip(favoritesOnly, {favoritesOnly=!favoritesOnly}, label={Text(tr("Favorites only", "المفضلة فقط"))})
            if (fileWriteFailure) Text(tr("Changes were not saved for the current profile.", "لم تُحفظ التعديلات للملف الشخصي الحالي."),color=MaterialTheme.colorScheme.error)
        }
        val files = grade9Files(data.files, query, filter).filter {classification.matches(it) && (!favoritesOnly || it.favorite)}
        if (files.isEmpty()) item {
            Text(if (data.files.isEmpty()) tr("Your files will appear here. Add a PDF or Word document you may use.", "ستظهر ملفاتك هنا. أضف مستند PDF أو Word يُسمح لك باستخدامه.")
                else tr("No files match this search or type.", "لا ملفات تطابق البحث أو النوع المحدد."))
            if (data.files.isNotEmpty()) TextButton({ query = ""; filter = G9FileFilter.ALL; classification=G9ResourceSelection(); favoritesOnly=false }, Modifier.heightIn(min = 48.dp)) { Text(tr("Clear search and filters", "مسح البحث والتصفية")) }
        }
        items(files, key = { it.id }) { file -> G9LearningCard(file.name) {
            Text((if (file.mime == "application/pdf") "PDF" else "Word") + " · " +
                grade9FileSize(file.bytes))
            Text(tr("Saved link · offline access depends on the document provider", "رابط محفوظ · الوصول دون اتصال يعتمد على مزوّد المستند"), style = MaterialTheme.typography.bodySmall)
            OutlinedButton({ open("g9/file/${file.id}") }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Open", "افتح")) }
            Text(SourceCurricula.forStage(com.ahmadmol.enmath.core.model.StudyStage.G9).let { catalog ->
                listOfNotNull(file.bookId?.let {catalog.book(it)?.title},file.unitId?.let {catalog.unit(it)?.title},file.researchId?.let {catalog.research(it)?.title}).joinToString(" · ").ifEmpty {tr("Unclassified", "غير مصنف")}
            },style=MaterialTheme.typography.bodySmall)
            TextButton({editing=file},Modifier.heightIn(min=48.dp)){Text(tr("Organize", "تنظيم"))}
            TextButton({fileWriteFailure = scope?.let {repository?.organizeFile(it,file.id,file.bookId,file.unitId,file.researchId,!file.favorite)} !in setOf(PlanWrite.SAVED,PlanWrite.ALREADY_SAVED)},Modifier.heightIn(min=48.dp)){Text(if(file.favorite) tr("Remove favorite", "إزالة من المفضلة") else tr("Add favorite", "إضافة للمفضلة"))}
            TextButton({ removing = file }, Modifier.heightIn(min = 48.dp)) { Text(tr("Remove from this list", "أزل من هذه القائمة")) }
        } }
    }
    editing?.let { G9OrganizeFileDialog(it,scope,repository) {editing=null} }
    removing?.let { file -> AlertDialog(onDismissRequest = { removing = null }, title = { Text(tr("Remove saved link?", "إزالة الرابط المحفوظ؟")) },
        text = { Text(tr("The original document will remain untouched.", "لن يُحذف المستند الأصلي.")) },
        confirmButton = { TextButton({
            val result = scope?.let { repository?.removeFile(it, file.id) }
            error = result !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED); removing = null
        }) { Text(tr("Remove link", "أزل الرابط")) } },
        dismissButton = { TextButton({ removing = null }) { Text(tr("Cancel", "إلغاء")) } }) }
}

private data class PdfPreview(val bitmap: Bitmap, val count: Int, val page: Int)

@Composable private fun g9FileFailureMessage(failure: G9FileFailure): String = when (failure) {
    G9FileFailure.PERMISSION -> tr("Read permission expired. Select this file again from Files.", "انتهى إذن القراءة. اختر المستند مجددًا من الملفات.")
    G9FileFailure.MISSING -> tr("The provider cannot find this document. Your saved link is preserved.", "تعذّر العثور على المستند لدى المزوّد. الرابط المحفوظ ما زال موجودًا.")
    G9FileFailure.NO_READER -> tr("No compatible reader is available. Your saved link is preserved.", "لا يوجد قارئ متوافق. الرابط المحفوظ ما زال موجودًا.")
    else -> tr("The document provider may be offline or unable to read this file. Retry when it is available.", "قد يكون مزوّد المستند غير متصل أو تعذّرت قراءة الملف. أعد المحاولة عند توفره.")
}

@Composable fun Grade9FileScreen(state: LearningState, repository: Grade9StudyRepository?, id: String) {
    val context = LocalContext.current
    val scope = StudentCatalog.scope(state)
    val read = rememberG9Study(state, repository)
    val file = (read as? G9StudyRead.Ready)?.data?.files?.find { it.id == id }
    var requested by rememberSaveable(scope?.ownerId, scope?.generation, file?.uri, id) { mutableIntStateOf(file?.page ?: 0) }
    var preview by remember(scope, file?.uri, id, requested) { mutableStateOf<PdfPreview?>(null) }
    var failure by remember(scope, file?.uri, id, requested) { mutableStateOf<G9FileFailure?>(null) }
    var retry by remember(scope, file?.uri, id) { mutableIntStateOf(0) }
    var externalFailure by remember(scope, file?.uri, id) { mutableStateOf<G9FileFailure?>(null) }
    var pageSaveFailure by remember(scope, file?.uri, id) { mutableStateOf(false) }
    var opening by remember(scope, file?.uri, id) { mutableStateOf(false) }
    val jobs = rememberCoroutineScope()
    LaunchedEffect(scope, file?.uri, requested, retry) {
        if (file?.mime != "application/pdf") return@LaunchedEffect
        failure = null; preview = null
        val loaded = withContext(Dispatchers.IO) { runCatching {
            require(file.uri.startsWith("content://"))
            context.contentResolver.openFileDescriptor(Uri.parse(file.uri), "r")?.use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    require(renderer.pageCount > 0)
                    val page = requested.coerceIn(0, renderer.pageCount - 1)
                    renderer.openPage(page).use { rendered ->
                        require(rendered.width > 0 && rendered.height > 0)
                        // Bound allocation even for unusually tall or wide user documents.
                        val scale = minOf(1200f / rendered.width, 1600f / rendered.height)
                        val bitmap = Bitmap.createBitmap((rendered.width * scale).toInt().coerceAtLeast(1),
                            (rendered.height * scale).toInt().coerceAtLeast(1), Bitmap.Config.ARGB_8888)
                        bitmap.eraseColor(android.graphics.Color.WHITE)
                        rendered.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        PdfPreview(bitmap, renderer.pageCount, page)
                    }
                }
            } ?: error("No file descriptor")
        } }
        loaded.onSuccess { preview = it }.onFailure { failure = grade9FileFailure(it) }
    }
    Grade9Scaffold("screen-g9-file") {
        if (file == null) { item { Text(tr("This document is not saved in the current local file.", "هذا المستند غير محفوظ في الملف المحلي الحالي.")); if (read !is G9StudyRead.Ready) G9StudyError(read) }; return@Grade9Scaffold }
        item { Text(file.name, style = MaterialTheme.typography.titleLarge.copy(textDirection = androidx.compose.ui.text.style.TextDirection.Content))
            Text(tr("Read access only. No upload or public sharing.", "وصول للقراءة فقط. لا رفع أو مشاركة عامة.")) }
        if (file.mime == "application/pdf") {
            val rendered = preview
            item {
                when {
                    failure != null -> {
                        Text(when (failure) {
                            G9FileFailure.PERMISSION -> tr("Read permission expired. Return to Files and select the document again.", "انتهى إذن القراءة. ارجع إلى الملفات واختر المستند مجددًا.")
                            G9FileFailure.MISSING -> tr("The document was moved, removed or is unavailable from its provider. Your saved link remains.", "نُقل المستند أو أُزيل أو تعذّر توفيره من المزوّد. الرابط المحفوظ ما زال موجودًا.")
                            else -> tr("The provider may be offline, or this PDF may be protected or unsupported. Try again or use another reader.", "قد يكون المزوّد غير متصل، أو ملف PDF محميًا أو غير مدعوم. حاول مجددًا أو استعمل قارئًا آخر.")
                        }, color = MaterialTheme.colorScheme.error)
                        OutlinedButton({ retry++ }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("g9-file-retry")) { Text(tr("Retry read access", "أعد محاولة القراءة")) }
                    }
                    rendered == null -> CircularProgressIndicator()
                    else -> {
                        Text(tr("Page ${rendered.page + 1} of ${rendered.count}", "الصفحة ${rendered.page + 1} من ${rendered.count}"))
                        Image(rendered.bitmap.asImageBitmap(), tr("PDF page ${rendered.page + 1}. For accessible text and zoom use an external reader.",
                            "صفحة PDF ${rendered.page + 1}. للنص القابل للوصول والتكبير استعمل قارئًا خارجيًا."), Modifier.fillMaxWidth())
                        if (rendered.page > 0) OutlinedButton({
                            val target = rendered.page - 1; val saved = scope?.let { repository?.filePage(it, id, target) }
                            pageSaveFailure = saved !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)
                            if (!pageSaveFailure) requested = target
                        }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Previous page", "الصفحة السابقة")) }
                        if (rendered.page < rendered.count - 1) OutlinedButton({
                            val target = rendered.page + 1; val saved = scope?.let { repository?.filePage(it, id, target) }
                            pageSaveFailure = saved !in setOf(PlanWrite.SAVED, PlanWrite.ALREADY_SAVED)
                            if (!pageSaveFailure) requested = target
                        }, Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(tr("Next page", "الصفحة التالية")) }
                        if (pageSaveFailure) Text(tr("Page position could not be saved.", "تعذّر حفظ موضع الصفحة."), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else item { Text(tr("Word files open in a compatible app. If none is installed, keep the saved link and return later, or export a PDF yourself.",
            "تُفتح ملفات Word بتطبيق متوافق. إن لم يتوفر تطبيق، احتفظ بالرابط وارجع لاحقًا، أو صدّر نسخة PDF بنفسك.")) }
        item { Button({
            opening = true
            jobs.launch {
                val available = withContext(Dispatchers.IO) { runCatching {
                    check(scope != null && (repository?.read(scope) as? G9StudyRead.Ready)?.data?.files?.any { it.id == id && it.uri == file.uri } == true)
                    context.contentResolver.openFileDescriptor(Uri.parse(file.uri), "r")?.use { } ?: error("Unavailable")
                } }
                val launched = available.fold(onSuccess = { runCatching {
                    check(scope != null && (repository?.read(scope) as? G9StudyRead.Ready)?.data?.files?.any { it.id == id && it.uri == file.uri } == true)
                    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(file.uri), file.mime)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION))
                } }, onFailure = { Result.failure(it) })
                externalFailure = launched.exceptionOrNull()?.let(::grade9FileFailure)
                opening = false
            }
        }, Modifier.fillMaxWidth().heightIn(min = 48.dp), enabled = !opening) { Text(tr("Open in another app", "افتح بتطبيق آخر")) }
            externalFailure?.let { Text(g9FileFailureMessage(it), color = MaterialTheme.colorScheme.error) }
        }
    }
}
