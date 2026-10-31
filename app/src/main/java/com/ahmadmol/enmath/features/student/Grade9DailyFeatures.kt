package com.ahmadmol.enmath.features.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.components.common.MathLine
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.planner.*

data class G9Activity(val key: String, val at: Long, val title: Copy, val detail: String, val route: String)
fun grade9PlanAction(action:String):Copy = when(action.substringBefore(':')) {
    "SAVE" -> Copy("Goal saved or edited","حفظ أو تعديل هدف")
    "MANUAL" -> Copy("Manual goal progress recorded","تسجيل إنجاز يدوي لهدف")
    "CARRY" -> Copy("Remaining goal work moved","ترحيل المتبقي من هدف")
    "DELETE" -> Copy("Goal removed","إزالة هدف")
    "UNDO" -> Copy("Plan change undone","تراجع عن تغيير الخطة")
    "PIN_UNIT" -> Copy("Unit references added to plan","إضافة مراجع وحدة للخطة")
    else -> Copy("Recorded plan change","تغيير مسجل بالخطة")
}
fun grade9Activity(data: G9StudyData, plan: StudyPlan, account: GemAccount?, owner: String?): List<G9Activity> = buildList {
    data.attempts.forEach { a -> add(G9Activity("attempt:${a.record.commandId}",a.record.timestamp,
        Copy("Saved practice attempt","محاولة تدريب محفوظة"),"${a.record.correct}/${a.record.total}","g9/result/${a.record.commandId}")) }
    plan.history.forEachIndexed { i,h ->
        val id=h.action.substringAfter(':',"")
        val after=(plan.history.getOrNull(i+1)?.tasks ?: plan.tasks).find {it.id==id}
        val before=h.tasks.find {it.id==id}
        val detail=after?.title ?: before?.title ?: if(h.action.startsWith("PIN_UNIT:")) SourceCurricula.forStage(StudyStage.G9).unit(id)?.title.orEmpty() else ""
        add(G9Activity("plan:$i",h.at,grade9PlanAction(h.action),detail,if(after!=null || before!=null) g9GoalRoute(id) else "planner"))
    }
    account?.takeIf { owner != null && it.owner == owner }?.let { wallet ->
        wallet.rewards.forEach { add(G9Activity("reward:${it.key}",it.at,Copy("Recorded earned reward","مكافأة مكتسبة مسجلة"),"+${it.units}","gems/wallet")) }
        wallet.unlocks.forEach { add(G9Activity("unlock:${it.itemId}",it.at,Copy("Recorded unlock","فتح مسجل بالمحفظة"),"−${it.price}","gems/wallet")) }
    }
}.sortedWith(compareByDescending<G9Activity>{it.at}.thenBy{it.key})

data class G9NextStudy(val title: Copy, val reason: Copy, val route: String)
fun grade9NextStudy(data: G9StudyData, plan: StudyPlan, visited: String?, now: Long): G9NextStudy {
    if (data.draft != null) return G9NextStudy(Copy("Resume saved practice","أكمل تدريبك المحفوظ"),Copy("Your unfinished answers are saved.","لديك إجابات محفوظة في جلسة غير منتهية."),"g9/practice")
    if (grade9LatestMistakes(data).isNotEmpty()) return G9NextStudy(Copy("Review your mistakes","راجع أخطاءك"),Copy("Based on your latest answered questions.","بحسب آخر إجاباتك الفعلية."),"review")
    val catalog=SourceCurricula.forStage(StudyStage.G9)
    val ref = planTasksInView(plan,PlanPeriod.WEEK,now).firstOrNull { !it.closed && !it.isAchieved() &&
        it.lesson?.let { r -> r.stage == StudyStage.G9 && catalog.research(r.lessonId)?.ref == r } == true }?.lesson
    val research = catalog.research(ref?.lessonId ?: visited.orEmpty())
    if (research != null) return G9NextStudy(Copy(research.title,research.title),
        if (ref != null) Copy("An unfinished goal in your current plan. Unreviewed topics open source references only.","هدف غير منجز في خطتك الحالية. الموضوع غير المراجع يفتح مرجع المصدر فقط.")
        else Copy("Your last valid source reference. Browsing is not mastery.","آخر مرجع مصدر صالح زرته. التصفح ليس إتقانًا."),
        if (research.id == Grade9Pilot.lessonId) research.route else "g9/reference/${research.id}")
    return G9NextStudy(Grade9Pilot.title,Copy("The available original pilot. Other lessons remain under review.","الدرس الأصلي التجريبي المتاح؛ بقية الدروس قيد المراجعة."),"lesson/${Grade9Pilot.lessonId}")
}

fun g9GoalRoute(id:String) = "g9/goal/"+java.net.URLEncoder.encode(id,Charsets.UTF_8.name()).replace("+","%20")

data class G9GoalChange(val at: Long, val action: String, val before: PlanTask?, val after: PlanTask?)
fun grade9GoalChanges(plan: StudyPlan, id: String): List<G9GoalChange> {
    val task = plan.tasks.find { it.id == id } ?: plan.history.asReversed().firstNotNullOfOrNull { h -> h.tasks.find { it.id == id } }
    val unitId = task?.lesson?.lessonId?.let { SourceCurricula.forStage(StudyStage.G9).research(it)?.unitId }
    return plan.history.mapIndexedNotNull { index,h ->
        val before=h.tasks.find {it.id==id}
        val after=(plan.history.getOrNull(index+1)?.tasks ?: plan.tasks).find {it.id==id}
        if (h.action.substringAfter(':',"") == id ||
            (before != after && (h.action == "PIN_UNIT:$unitId" || h.action == "UNDO")))
            G9GoalChange(h.at,h.action.substringBefore(':'),before,after) else null
    }.asReversed()
}

@Composable fun rememberG9Plan(state: LearningState, repository: PlannerRepository?): PlanRead? {
    val scope=StudentCatalog.scope(state);val changed=repository?.changes?.collectAsState()?.value
    return remember(scope,changed,repository) {scope?.let {repository?.read(it)}}
}

@Composable fun Grade9ActivityScreen(state: LearningState, study: Grade9StudyRepository?, planner: PlannerRepository?, gems: LocalGemsUiState, open:(String)->Unit) {
    val read=rememberG9Study(state,study);val planRead=rememberG9Plan(state,planner)
    val data=(read as? G9StudyRead.Ready)?.data;val plan=(planRead as? PlanRead.Ready)?.plan
    val owner=LocalGemsPolicy.owner(state.user)
    val account=gems.account?.takeIf {gems.owner==owner && it.owner==owner}
    Grade9Scaffold("screen-g9-activity") {
        item { Text(tr("My achievements and activity","إنجازاتي ونشاطي"),style=MaterialTheme.typography.headlineSmall) }
        if(data==null || plan==null) {item {G9StudyError(read);if(planRead==PlanRead.StorageError) Text(tr("The plan could not be read; its data is preserved.","تعذرت قراءة الخطة؛ بياناتها محفوظة."))};return@Grade9Scaffold}
        item {G9LearningCard(tr("Recorded evidence only","أدلة محفوظة فقط")) {
            Text(tr("${data.attempts.size} saved attempts · ${plan.tasks.count { !it.closed && it.isAchieved() }} goals meet their plan criteria","${data.attempts.size} محاولات محفوظة · ${plan.tasks.count { !it.closed && it.isAchieved() }} أهداف تستوفي معايير الخطة"))
            Text(if(account==null) tr("The reward ledger is unavailable for this profile. Open the wallet to check its status.","سجل المكافآت غير متاح لهذا الملف حاليًا. افتح المحفظة للتحقق من حالته.") else tr("${account.rewards.size} recorded wallet rewards; this is not certification of Grade 9 mastery.","${account.rewards.size} مكافآت محفوظة بالمحفظة؛ ليست شهادة إتقان للتاسع."))
            Text(tr("No new badges or gems are granted here.","لا تُمنح هنا شارات أو جواهر جديدة."))
        }}
        val events=grade9Activity(data,plan,account,owner)
        if(events.isEmpty()) item {Text(tr("No recorded activity yet. Start with an available lesson or a small plan goal.","لا نشاط مسجل بعد. ابدأ بدرس متاح أو هدف صغير للخطة."))}
        items(events,key={it.key}) {event -> G9LearningCard(event.title.text()) {
            Text(planDate(event.at))
            if(event.detail.isNotBlank()) {
                if(event.detail.matches(Regex("[+−0-9/]+"))) MathLine(event.detail)
                else Text(event.detail,style=MaterialTheme.typography.bodyLarge.copy(textDirection=androidx.compose.ui.text.style.TextDirection.Content))
            }
            OutlinedButton({open(event.route)},Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(tr("Open record","افتح السجل"))}
        }}
        item {TextButton({open("gems/wallet")}){Text(tr("Wallet and full ledger","المحفظة والسجل الكامل"))}}
    }
}

@Composable fun Grade9GoalScreen(state: LearningState, repository: PlannerRepository?, id:String, open:(String)->Unit) {
    val read=rememberG9Plan(state,repository);val plan=(read as? PlanRead.Ready)?.plan
    val task=plan?.tasks?.find {it.id==id}
    val scope=StudentCatalog.scope(state)
    var editing by remember(scope,id){mutableStateOf<PlanTask?>(null)}
    var editRevision by remember(scope,id){mutableIntStateOf(0)}
    Grade9Scaffold("screen-g9-goal") {
        item {Text(tr("Goal details and history","تفاصيل الهدف وسجله"),style=MaterialTheme.typography.headlineSmall)}
        if(plan==null) {item {Text(tr("The current plan could not be read. Existing data is preserved.","تعذرت قراءة الخطة الحالية. بياناتها محفوظة."))};return@Grade9Scaffold}
        if(task!=null) item {G9LearningCard(task.title) {
            val measured=repository!!.measured(task,state)
            Text(task.subject);Text(periodLabel(task.period));MathLine(planDate(task.start))
            if(task.mode!=PlanMode.TIME) Text(tr("Achievement ${measured.done} of ${task.target}","الإنجاز ${measured.done} من ${task.target}"))
            if(task.mode!=PlanMode.ACHIEVEMENT) Text(tr("Time ${measured.spent} of ${task.minutes} minutes","الوقت ${measured.spent} من ${task.minutes} دقيقة"))
            Text(tr("Plan progress is not educational mastery.","إنجاز الخطة ليس إثبات إتقان تعليمي."))
            task.lesson?.let {ref -> TextButton({open("g9/reference/${ref.lessonId}")}){Text(tr("Source reference","مرجع المصدر"))}}
            if(!task.closed) OutlinedButton({editing=task;editRevision=plan.revision},Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(tr("Edit this goal","تعديل هذا الهدف"))}
            OutlinedButton({open("planner")},Modifier.fillMaxWidth().heightIn(min=48.dp)){Text(tr("Record, move or remove in my plan","سجّل الإنجاز أو رحّل أو احذف من خطتي"))}
        }} else item {Text(tr("The goal is not in the current plan. Its recorded history is shown below.","الهدف غير موجود في الخطة الحالية. يظهر سجله المحفوظ أدناه."))}
        val history=grade9GoalChanges(plan,id)
        if(history.isEmpty()) item {Text(tr("No recorded changes for this goal.","لا تغييرات مسجلة لهذا الهدف."))}
        items(history) {change -> G9LearningCard(planDate(change.at)) {
            Text(grade9PlanAction(change.action).text())
            G9GoalSnapshot(tr("Before", "قبل"),change.before)
            G9GoalSnapshot(tr("After", "بعد"),change.after)
        }}
    }
    editing?.let { old -> if(scope!=null && repository!=null) GoalEditor(state,old,old.period,old.start,
        {editing=null},{ next,command ->
            val result=repository.save(scope,editRevision,command,next)
            if(result in setOf(PlanWrite.SAVED,PlanWrite.ALREADY_SAVED)) editing=null
            result
        }) }
}

@Composable fun G9PinUnitAction(state:LearningState, repository:PlannerRepository?, unitId:String) {
    val scope=StudentCatalog.scope(state);var result by remember(scope,unitId){mutableStateOf<PlanWrite?>(null)}
    Button({if(scope!=null) result=repository?.pinGrade9Unit(scope,unitId)},Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=repository!=null) {Text(tr("Add all unit references to this week's plan","أضف مراجع الوحدة كلها إلى خطة هذا الأسبوع"))}
    result?.let {Text(when(it){PlanWrite.SAVED -> tr("Unit references added without duplicating existing goals.","أُضيفت مراجع الوحدة دون تكرار الأهداف الموجودة.");PlanWrite.ALREADY_SAVED -> tr("Already in this week's plan.","موجودة بالفعل في خطة هذا الأسبوع.");else -> tr("Not saved. Reopen the current plan and retry.","لم تُحفظ. أعد فتح الخطة الحالية وحاول مجددًا.")})}
    Text(tr("These are source-reference goals, not approved lessons or completed learning.","هذه أهداف لمراجع المصدر، وليست دروسًا معتمدة أو تعلمًا مكتملًا."),style=MaterialTheme.typography.bodySmall)
}

@Composable private fun G9GoalSnapshot(label:String,task:PlanTask?) {
    Text(label,style=MaterialTheme.typography.titleSmall)
    if(task==null) Text(tr("Not present", "غير موجود")) else {
        Text(task.title)
        if(task.mode!=PlanMode.TIME) Text(tr("Achievement ${task.done} of ${task.target}","الإنجاز ${task.done} من ${task.target}"))
        if(task.mode!=PlanMode.ACHIEVEMENT) Text(tr("Time ${task.spent} of ${task.minutes} minutes","الوقت ${task.spent} من ${task.minutes} دقيقة"))
    }
}
