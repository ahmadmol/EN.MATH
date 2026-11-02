package com.ahmadmol.enmath.features.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.StudyStage

data class G9ResourceSelection(val book: String?=null,val unit: String?=null,val research: String?=null)
val G9ResourceSelectionSaver = listSaver<G9ResourceSelection,String>(save={listOf(it.book.orEmpty(),it.unit.orEmpty(),it.research.orEmpty())},
    restore={G9ResourceSelection(it[0].ifEmpty{null},it[1].ifEmpty{null},it[2].ifEmpty{null})})
fun G9ResourceSelection.matches(file:StudentFile) = (book==null || file.bookId==book) && (unit==null || file.unitId==unit) && (research==null || file.researchId==research)
fun G9ResourceSelection.matches(ref:SourceResearch):Boolean {
    val catalog=SourceCurricula.forStage(StudyStage.G9)
    return (book==null || catalog.unit(ref.unitId)?.bookId==book) && (unit==null || ref.unitId==unit) && (research==null || ref.id==research)
}
@Composable private fun G9ResourceChoice(label:String,selected:String?,options:List<Pair<String,String>>,emptyLabel:String,change:(String?)->Unit) {
    var expanded by remember {mutableStateOf(false)}
    Box {
        OutlinedButton({expanded=true},Modifier.fillMaxWidth().heightIn(min=48.dp)) {Text(label+": "+(options.find {it.first==selected}?.second ?: emptyLabel))}
        DropdownMenu(expanded,{expanded=false}) {
            DropdownMenuItem(text={Text(emptyLabel)},onClick={expanded=false;change(null)})
            options.forEach {(id,title)->DropdownMenuItem(text={Text(title)},onClick={expanded=false;change(id)})}
        }
    }
}
@Composable fun G9ReferenceFilters(value:G9ResourceSelection,unclassified:Boolean=false,change:(G9ResourceSelection)->Unit) {
    val catalog=SourceCurricula.forStage(StudyStage.G9)
    val emptyLabel=if(unclassified) tr("Unclassified","غير مصنف") else tr("All","الكل")
    G9ResourceChoice(tr("Book","الكتاب"),value.book,catalog.books.map {it.id to it.title},emptyLabel) {change(G9ResourceSelection(book=it))}
    G9ResourceChoice(tr("Unit","الوحدة"),value.unit,catalog.units.filter {it.bookId==value.book}.map{it.id to it.title},emptyLabel) {change(value.copy(unit=it,research=null))}
    G9ResourceChoice(tr("Topic","البحث"),value.research,catalog.researches.filter {it.unitId==value.unit}.map{it.id to it.title},emptyLabel) {change(value.copy(research=it))}
}
@Composable fun G9OrganizeFileDialog(file:StudentFile,scope:StudentScope?,repository:Grade9StudyRepository?,dismiss:()->Unit) {
    var selection by rememberSaveable(scope,file.id,stateSaver=G9ResourceSelectionSaver) {mutableStateOf(G9ResourceSelection(file.bookId,file.unitId,file.researchId))}
    var favorite by rememberSaveable(scope,file.id){mutableStateOf(file.favorite)}
    var failure by remember {mutableStateOf(false)}
    AlertDialog(onDismissRequest=dismiss,title={Text(tr("Organize saved link","تنظيم الرابط المحفوظ"))},text={
        Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Text(file.name)
            Text(tr("Classification does not rename or modify the original document, or approve its content.","التصنيف لا يغيّر المستند الأصلي أو اسمه، ولا يعتمد محتواه."))
            G9ReferenceFilters(selection,unclassified=true){selection=it}
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp).toggleable(favorite, role=Role.Checkbox, onValueChange={favorite=it}),verticalAlignment=Alignment.CenterVertically) {Checkbox(favorite,null);Text(tr("Favorite","مفضلة"))}
            if(failure) Text(tr("Changes were not saved. Reopen the current local profile and retry.","لم تُحفظ التعديلات. أعد فتح الملف الشخصي الحالي وحاول مجددًا."),color=MaterialTheme.colorScheme.error)
        }
    },confirmButton={TextButton({val result=scope?.let {repository?.organizeFile(it,file.id,selection.book,selection.unit,selection.research,favorite)}
        if(result in setOf(PlanWrite.SAVED,PlanWrite.ALREADY_SAVED)) dismiss() else failure=true
    },Modifier.heightIn(min=48.dp)){Text(tr("Save classification","حفظ التصنيف"))}},dismissButton={TextButton(dismiss){Text(tr("Cancel","إلغاء"))}})
}

@Composable fun Grade9SavedReferences(state:LearningState,repository:SourceWorkspaceRepository?,open:(String)->Unit) {
    val scope=StudentCatalog.scope(state);val changed=repository?.changes?.collectAsState()?.value
    val read=remember(scope,changed){scope?.let {repository?.read(it)}}
    var selection by rememberSaveable(scope,stateSaver=G9ResourceSelectionSaver){mutableStateOf(G9ResourceSelection())}
    var query by rememberSaveable(scope){mutableStateOf("")}
    var expanded by rememberSaveable(scope){mutableStateOf(false)}
    var failure by remember(scope){mutableStateOf(false)}
    Text(tr("Saved source favorites","مراجع المصدر المفضلة"),style=MaterialTheme.typography.titleMedium)
    if(read !is SourceWorkspaceRead.Ready) {Text(tr("The current saved references could not be read. Existing data is preserved.","تعذرت قراءة المراجع المحفوظة الحالية. بياناتها محفوظة."));return}
    OutlinedTextField(query,{query=it},Modifier.fillMaxWidth(),label={Text(tr("Search favorites","بحث في المفضلة"))})
    TextButton({expanded=!expanded}) {Text(tr("Filter book, unit and topic","تصفية الكتاب والوحدة والبحث"))}
    if(expanded) G9ReferenceFilters(selection){selection=it}
    val words=LearningRules.search(query).split(Regex("\\s+")).filter(String::isNotBlank)
    val refs=SourceCurricula.forStage(StudyStage.G9).researches.filter {it.id in read.workspace.savedIds && selection.matches(it) && words.all {word->LearningRules.search(it.title).contains(word)}}
    if(refs.isEmpty()) Text(tr("No saved references match. Save a source reference or clear the filters.","لا مراجع محفوظة مطابقة. احفظ مرجعًا أو امسح التصفية."))
    TextButton({query="";selection=G9ResourceSelection()}){Text(tr("Clear filters","مسح التصفية"))}
    if(failure) Text(tr("Not saved. Check the active profile.","لم يُحفظ. تحقّق من الملف الشخصي الحالي."),color=MaterialTheme.colorScheme.error)
    refs.forEach {ref ->
        OutlinedCard(Modifier.fillMaxWidth()) {Column(Modifier.padding(12.dp)) {
            Text(ref.title,style=MaterialTheme.typography.titleMedium)
            Text(SourceCurricula.forStage(StudyStage.G9).unit(ref.unitId)?.title.orEmpty())
            Text(tr("Saved source reference; educational review is separate.","مرجع مصدر محفوظ؛ المراجعة التعليمية منفصلة."),style=MaterialTheme.typography.bodySmall)
            TextButton({open("g9/reference/${ref.id}")}){Text(tr("Open reference","افتح المرجع"))}
            TextButton({failure=scope?.let {repository?.bookmark(it,ref.ref,false)} !in setOf(PlanWrite.SAVED,PlanWrite.ALREADY_SAVED)}){Text(tr("Remove favorite","إزالة من المفضلة"))}
        }}
    }
}
