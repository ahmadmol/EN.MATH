package com.ahmadmol.enmath.features.localgems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.Language
import com.ahmadmol.enmath.features.practice.PracticeScreen
import com.ahmadmol.enmath.features.practice.QuizViewModel
import java.text.NumberFormat
import java.util.Locale

private fun units(value: Int, language: Language)=NumberFormat.getIntegerInstance(if(language==Language.Arabic) Locale.forLanguageTag("ar") else Locale.ENGLISH).format(value)

@Composable
fun GemCounter(ui: LocalGemsUiState, language: Language, onClick: ()->Unit, tag: String) {
    val ar=language==Language.Arabic
    val count=if(ui.owner==null) "—" else ui.account?.balance?.let { units(it,language) } ?: "…"
    val title=if(ar) "جواهري" else "My gems"
    FilledTonalButton(onClick=onClick, modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag(tag)
        .semantics { contentDescription="$title: $count" }, contentPadding=PaddingValues(16.dp)) {
        Icon(Icons.Outlined.Diamond,null,Modifier.size(28.dp));Spacer(Modifier.width(12.dp))
        Text(title,Modifier.weight(1f),style=MaterialTheme.typography.titleMedium)
        Text(count,style=MaterialTheme.typography.titleLarge)
    }
    if(ui.owner==null) Text(if(ar) "الرصيد متاح لحساب طالب ببريد إلكتروني؛ اضغط للتفاصيل." else "A balance needs a student account with an email. Tap for details.", style=MaterialTheme.typography.bodySmall)
}

@Composable
fun WeeklyGemCard(ui: LocalGemsUiState, language: Language, onOpen: ()->Unit) {
    val ar=language==Language.Arabic
    val done=ui.account?.rewards?.any { it.key=="WEEKLY:${LocalGemsPolicy.week(System.currentTimeMillis())}" }==true
    AppCard(Modifier.testTag("gems-weekly-card"),tonal=true) {
        Text(if(ar) "مراجعة الأسبوع" else "Weekly review",style=MaterialTheme.typography.titleMedium)
        Text(if(done) { if(ar) "أنجزت مراجعة هذا الأسبوع. أحسنت!" else "This week's review is complete. Well done!" }
            else if(ui.review!=null) { if(ar) "خمسة أسئلة من دروس تحققت من فهمها · حتى ١٥ جوهرة" else "Five questions from verified lessons · up to 15 gems" }
            else { if(ar) "تتوفر بعد التحقق من دروسك ووجود خمسة أسئلة مختلفة مرتبطة بدرسين على الأقل." else "Available after verifying your lessons, with five distinct questions linked to at least two completed lessons." })
        OutlinedButton(onClick=onOpen,enabled=ui.review!=null && !ui.busy,
            modifier=Modifier.heightIn(min=48.dp).testTag("gems-weekly-open")) { Text(if(ar) "ابدأ المراجعة" else "Start review") }
    }
}

@Composable
fun LocalGemsScreen(state: LearningState, gems: LocalGemsCoordinator, onStudentSignIn: () -> Unit = {}) {
    val observed by gems.ui.collectAsStateWithLifecycle()
    val owner=LocalGemsPolicy.owner(state.user)
    val ui=observed.takeIf { it.owner==owner } ?: LocalGemsUiState()
    val ar=state.language==Language.Arabic
    fun text(arabic:String,english:String)=if(ar) arabic else english
    var section by rememberSaveable(owner) { mutableStateOf("wallet") }
    var confirmation by rememberSaveable(owner) { mutableStateOf<String?>(null) }
    LaunchedEffect(owner) { gems.refresh(state) }
    BackHandler(enabled=ui.content!=null) { gems.closeContent() }
    ScreenPage(Modifier.testTag("screen-local-gems")) {
        if(owner==null) {
            Text(text("جواهري", "My gems"), style=MaterialTheme.typography.headlineMedium)
            Text(if(state.user?.type==com.ahmadmol.enmath.core.model.AccountType.Teacher)
                text("أنت في واجهة المعلم. مكافآت الجواهر تخص حساب الطالب.","You are in the teacher workspace. Gem rewards belong to a student account.")
                else text("سجّل الدخول بحساب الطالب داخل EN.MATH وبريد إلكتروني لبدء رصيد خاص بك. لا حساب إضافي للجواهر، ولا مكافآت رجعية.","Sign in to EN.MATH as a student with an email to start your own balance. No separate Gems login or retroactive rewards."))
            Button(onClick=onStudentSignIn,modifier=Modifier.heightIn(min=48.dp).testTag("gems-use-student-account")) { Text(text("دخول حساب الطالب", "Student sign in")) }
            return@ScreenPage
        }
        if(ui.content!=null) {
            Text(LocalGemCatalog.item(ui.contentId.orEmpty())?.title?.let { if(ar) it.ar else it.en }.orEmpty(),style=MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick=gems::closeContent,modifier=Modifier.heightIn(min=48.dp).testTag("gems-reader-close")) { Text(text("العودة إلى مقتنياتي","Back to my content")) }
            Text(text("محتوى إثرائي تجريبي أصلي؛ يحتاج مراجعة علمية واعتمادًا قبل النشر النهائي.","Original enrichment draft for this pilot; scientific and publication review remain required."),style=MaterialTheme.typography.bodySmall)
            SelectionContainer { Text(ui.content.orEmpty(),style=MaterialTheme.typography.bodyLarge,modifier=Modifier.testTag("gems-original-content")) }
            return@ScreenPage
        }
        Text(text("جواهري","My gems"),style=MaterialTheme.typography.headlineMedium)
        AppCard(tonal=true) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.Diamond,null,Modifier.size(36.dp),tint=MaterialTheme.colorScheme.primary)
                Text(ui.account?.balance?.let { units(it,state.language) } ?: "…",style=MaterialTheme.typography.displaySmall,modifier=Modifier.testTag("local-gems-balance"))
            }
            Text(text("تعلّم، واجمع، وافتح محتوى إضافيًا.","Learn, collect and unlock something extra."))
            Text(text("تجربة محلية على هذا الجهاز؛ لا تتزامن بين الأجهزة ولا تمنع التلاعب بالبيانات المحلية.","A local pilot on this device, without cross-device sync or protection against local data tampering."),style=MaterialTheme.typography.bodySmall)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilterChip(selected=section=="wallet",onClick={section="wallet"},label={Text(text("محفظتي","Wallet"))},modifier=Modifier.heightIn(min=48.dp).testTag("local-gems-wallet"))
            FilterChip(selected=section=="store",onClick={section="store"},label={Text(text("متجر الجواهر","Gem store"))},modifier=Modifier.heightIn(min=48.dp).testTag("local-gems-store"))
        }
        if(ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        if(ui.error!=null) {
            Text(if(ui.error=="INSUFFICIENT_GEMS") text("رصيدك لا يكفي لهذا المحتوى. تابع التعلم لجمع المزيد.","You need more gems for this content. Keep learning to collect more.")
                else text("تعذر حفظ بيانات الجواهر أو قراءتها. رصيدك السابق محفوظ؛ حاول مجددًا.","Gems could not be saved or read. Your existing data is preserved; try again."),color=MaterialTheme.colorScheme.error)
            OutlinedButton(onClick={gems.refresh(state)},modifier=Modifier.heightIn(min=48.dp)) { Text(text("حاول مجددًا","Try again")) }
        }
        if(section=="store") {
            Text(text("إضافات للتعلّم، والمنهاج الأساسي متاح كالمعتاد.","Extra learning material; the core curriculum remains available as usual."))
            LocalGemCatalog.items.forEach { item ->
                val owned=ui.account?.unlocks?.any { it.itemId==item.id }==true
                val enough=(ui.account?.balance ?: -1)>=item.price
                AppCard {
                    Row(horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.CenterVertically) {
                        Icon(if(owned) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,null)
                        Text(if(ar) item.title.ar else item.title.en,Modifier.weight(1f),style=MaterialTheme.typography.titleLarge)
                    }
                    Text(if(ar) item.summary.ar else item.summary.en)
                    Text(text("مسودة أصلية للتجربة، وليست مادة نهائية معتمدة.","An original pilot draft, not final approved educational material."),style=MaterialTheme.typography.bodySmall)
                    Text(if(owned) text("ضمن مقتنياتك","Already yours") else "${units(item.price,state.language)} ${text("جوهرة","gems")}",style=MaterialTheme.typography.titleMedium)
                    if(!owned && !enough && ui.account!=null) Text(text("تابع التعلم لجمع الجواهر اللازمة.","Keep learning to collect the gems you need."),style=MaterialTheme.typography.bodySmall)
                    Button(onClick={if(owned) gems.openContent(state,owner,item.id) else if(enough) confirmation=item.id},
                        enabled=ui.account!=null && (owned || enough) && !ui.busy && ui.error==null,
                        modifier=Modifier.heightIn(min=48.dp).testTag("local-gems-item-${item.id}")) { Text(if(owned) text("افتح المحتوى","Open content") else text("عرض تأكيد الفتح","Review unlock")) }
                }
            }
        } else {
            Text(text("مقتنياتي","My unlocked content"),style=MaterialTheme.typography.titleLarge)
            val owned=ui.account?.unlocks.orEmpty()
            if(owned.isEmpty()) Text(text("محتواك الإضافي سيظهر هنا بعد فتحه بالجواهر.","Your extra content will appear here after you unlock it."))
            owned.forEach { grant -> LocalGemCatalog.item(grant.itemId)?.let { item ->
                OutlinedButton(onClick={gems.openContent(state,owner,item.id)},enabled=!ui.busy,
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("local-gems-owned-${item.id}")) { Text(if(ar) item.title.ar else item.title.en) }
            } }
            Text(text("سجل الجواهر","Gem history"),style=MaterialTheme.typography.titleLarge)
            if(ui.account?.rewards.isNullOrEmpty()) Text(text("أكمل نشاطًا مؤهلًا لتبدأ جمع الجواهر.","Complete an eligible activity to start collecting gems."))
            val history=ui.account?.rewards.orEmpty().map { entry ->
                val title=when(entry.kind) {
                    GemReward.LESSON -> text("تحقق وإتمام درس","Verified lesson completion")
                    GemReward.PRACTICE -> text("تدريب مؤهل","Qualified practice")
                    GemReward.ASSIGNMENT -> text("واجب مؤهل","Qualified assignment")
                    GemReward.MASTERY -> text("إتقان موضوع لأول مرة","First topic mastery")
                    GemReward.WEEKLY -> text("مراجعة الأسبوع","Weekly review")
                }
                Triple(entry.at,title,if(entry.units>0) "+${units(entry.units,state.language)}" else text("بلغت الحد التجريبي","Pilot cap reached"))
            }+owned.map { Triple(it.at,text("فتح محتوى إضافي","Extra content unlocked"),"−${units(it.price,state.language)}") }
            history.sortedByDescending { it.first }.forEach { (_,title,amount) ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(title,Modifier.weight(1f));Text(amount,color=MaterialTheme.colorScheme.primary)
                }
            }
            Text(text("حدود التجربة: ٤٠ جوهرة يوميًا و١٢٠ أسبوعيًا بتوقيت دمشق.","Pilot caps: 40 gems daily and 120 weekly, Damascus time."),style=MaterialTheme.typography.bodySmall)
        }
    }
    confirmation?.let { id -> LocalGemCatalog.item(id)?.let { item ->
        AlertDialog(onDismissRequest={confirmation=null},
            title={Text(text("فتح هذا المحتوى؟","Unlock this content?"))},
            text={Text(text("سيُخصم ${units(item.price,state.language)} جوهرة مرة واحدة، ويظل المحتوى ضمن مقتنيات هذا الحساب على الجهاز.","Spend ${item.price} gems once and keep this content for this account on the device."))},
            confirmButton={TextButton(onClick={confirmation=null;gems.purchase(state,owner,id)},enabled=!ui.busy,
                modifier=Modifier.heightIn(min=48.dp).testTag("local-gems-confirm")) { Text(text("تأكيد الفتح","Confirm unlock")) }},
            dismissButton={TextButton(onClick={confirmation=null},modifier=Modifier.heightIn(min=48.dp)) { Text(text("إلغاء","Cancel")) }})
    } }
}

@Composable
fun LocalWeeklyReviewScreen(state: LearningState, gems: LocalGemsCoordinator, onBack: ()->Unit) {
    val ui by gems.ui.collectAsStateWithLifecycle()
    val owner=LocalGemsPolicy.owner(state.user)
    val review=remember(owner) { ui.review.takeIf { ui.owner==owner } }
    val ar=state.language==Language.Arabic
    var score by rememberSaveable(owner) { mutableStateOf<Int?>(null) }
    var attempt by rememberSaveable(owner) { mutableIntStateOf(0) }
    if(owner==null || review==null || ui.owner!=owner) {
        ScreenPage { Text(if(ar) "لا تتوفر مراجعة مؤهلة الآن." else "No eligible review is available yet.");OutlinedButton(onBack,Modifier.heightIn(min=48.dp)) {Text(if(ar) "رجوع" else "Back")} }
    } else if(score!=null) {
        ScreenPage {
            Text(if(ar) "نتيجة المراجعة" else "Review result",style=MaterialTheme.typography.headlineSmall)
            Text("${score}/5",style=MaterialTheme.typography.displaySmall)
            Text(if(score!!>=4) { if(ar) "أحسنت! يُحفظ استحقاق المراجعة ضمن حدود التجربة، دون تغيير درجاتك." else "Well done! Review eligibility is saved within the pilot caps, without changing your grades." }
                else { if(ar) "تحتاج أربع إجابات صحيحة على الأقل. يمكنك المحاولة مجددًا." else "You need at least four correct answers. You can try again." })
            if(ui.error!=null) Text(if(ar) "تعذر حفظ الجواهر. أعد المحاولة لاحقًا؛ لم يتغير رصيدك." else "Gems could not be saved. Try later; your balance is unchanged.",color=MaterialTheme.colorScheme.error)
            if(score!!<4 || ui.error!=null) OutlinedButton(onClick={attempt++;score=null},modifier=Modifier.heightIn(min=48.dp)) {Text(if(ar) "حاول مجددًا" else "Try again")}
            Button(onBack,Modifier.heightIn(min=48.dp)) { Text(if(ar) "العودة" else "Back") }
        }
    } else {
        val quiz: QuizViewModel=viewModel(key="local-gems-review-$owner-${review.week}-$attempt")
        val questions=review.questions.map { it.question }
        PracticeScreen(questions.map { it.id },if(ar) "مراجعة الأسبوع" else "Weekly review",quiz,
            product=state.product.copy(questions=state.product.questions+questions),onFinish={answers,_ ->
                score=questions.count { LearningRules.correct(it,answers[it.id].orEmpty()) }
                gems.finishWeekly(state,owner,review,answers)
            })
    }
}
