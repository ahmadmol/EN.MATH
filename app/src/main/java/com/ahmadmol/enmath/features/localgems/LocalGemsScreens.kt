package com.ahmadmol.enmath.features.localgems

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

fun gemBalanceText(ui: LocalGemsUiState, currentOwner: String?, language: Language): String {
    val ar = language == Language.Arabic
    if (currentOwner == null) return if (ar) "غير متاح" else "N/A"
    if (ui.owner != currentOwner) return if (ar) "تحميل…" else "Loading…"
    if (ui.error != null) return if (ar) "خطأ" else "Error"
    if (ui.account == null) return if (ar) "تحميل…" else "Loading…"
    return units(ui.account.balance, language)
}

@Composable
fun GemCounter(ui: LocalGemsUiState, language: Language, onClick: () -> Unit, tag: String, currentOwner: String? = ui.owner) {
    val ar = language == Language.Arabic
    val count = gemBalanceText(ui, currentOwner, language)
    val title = if (ar) "جواهري" else "My gems"
    val isStatus = currentOwner == null || ui.error != null || ui.owner != currentOwner || ui.account == null

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(tag)
            .semantics { contentDescription = "$title: $count" }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Diamond,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = if (isStatus) count else "$count ${if (ar) "جوهرة" else "gems"}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

internal enum class WeeklyGemCardStatus { Completed, Loading, Error, NotEligible, Ready }

internal data class WeeklyGemCardPresentation(
    val status: WeeklyGemCardStatus,
    val remainingLessons: Int = 0,
) {
    fun detail(ar: Boolean): String = when (status) {
        WeeklyGemCardStatus.Completed ->
            if (ar) "أنجزت مراجعة هذا الأسبوع. أحسنت!" else "This week's review is complete. Well done!"
        WeeklyGemCardStatus.Loading ->
            if (ar) "جارٍ تحميل المراجعة الأسبوعية…" else "Loading this week's review…"
        WeeklyGemCardStatus.Error ->
            if (ar) "تعذر تحميل مراجعة الحساب الحالي. حاول مجددًا لاحقًا." else "Could not load this account's review. Try again later."
        WeeklyGemCardStatus.NotEligible ->
            if (ar) "تحتاج إلى ٥ دروس مكتملة بنقاط تحقق موثقة ومختلفة؛ المتبقي: ${units(remainingLessons, Language.Arabic)}."
            else "Requires 5 completed lessons with distinct verified checkpoints ($remainingLessons remaining)."
        WeeklyGemCardStatus.Ready ->
            if (ar) "خمسة أسئلة ممتازة · أكملها واحصل على حتى ١٥ جوهرة" else "Five questions · earn up to 15 gems"
    }
}

/** Presentation only: never generates reviews or awards; hides data from another account. */
internal fun weeklyGemCardPresentation(
    ui: LocalGemsUiState,
    state: LearningState?,
    now: Long = System.currentTimeMillis(),
): WeeklyGemCardPresentation {
    if (state == null) return WeeklyGemCardPresentation(WeeklyGemCardStatus.Error)
    val owner = LocalGemsPolicy.owner(state.user)
        ?: return WeeklyGemCardPresentation(WeeklyGemCardStatus.Error)
    val account = ui.account
    if (ui.owner != owner || (account != null && account.owner != owner))
        return WeeklyGemCardPresentation(WeeklyGemCardStatus.Loading)
    val week = LocalGemsPolicy.week(now)
    if (account?.rewards?.any { it.key == "WEEKLY:$week" } == true)
        return WeeklyGemCardPresentation(WeeklyGemCardStatus.Completed)
    if (ui.error != null) return WeeklyGemCardPresentation(WeeklyGemCardStatus.Error)
    if (ui.busy || account == null)
        return WeeklyGemCardPresentation(WeeklyGemCardStatus.Loading)
    if (ui.review?.week == week)
        return WeeklyGemCardPresentation(WeeklyGemCardStatus.Ready)
    val qualifyingCount = state.lessons.filter {
        it.id in account.lessons && it.id in state.completed &&
            it.checkpoint.isNotBlank() && it.checkpointAnswer.isNotBlank()
    }.distinctBy { it.checkpoint.trim() to it.checkpointAnswer.trim() }.size
    val remaining = (5 - qualifyingCount).coerceAtLeast(0)
    return if (remaining == 0) WeeklyGemCardPresentation(WeeklyGemCardStatus.Loading)
    else WeeklyGemCardPresentation(WeeklyGemCardStatus.NotEligible, remaining)
}

@Composable
fun WeeklyGemCard(
    ui: LocalGemsUiState,
    language: Language,
    state: LearningState? = null,
    onOpen: () -> Unit
) {
    val ar = language == Language.Arabic
    val presentation = weeklyGemCardPresentation(ui, state)
    val available = presentation.status == WeeklyGemCardStatus.Ready

    if (!available) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gems-weekly-card")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = when (presentation.status) {
                        WeeklyGemCardStatus.Completed -> Icons.Outlined.CheckCircle
                        WeeklyGemCardStatus.Loading -> Icons.Outlined.HourglassEmpty
                        WeeklyGemCardStatus.Error -> Icons.Outlined.ErrorOutline
                        else -> Icons.Outlined.Lock
                    },
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = when (presentation.status) {
                        WeeklyGemCardStatus.Error -> MaterialTheme.colorScheme.error
                        WeeklyGemCardStatus.Completed -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (ar) "المراجعة الأسبوعية" else "Weekly Review",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (presentation.status == WeeklyGemCardStatus.Loading)
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(
                        text = presentation.detail(ar),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        AppCard(
            modifier = Modifier.testTag("gems-weekly-card"),
            tonal = true
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Diamond,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (ar) "مراجعة الأسبوع جاهزة" else "Weekly Review Ready",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = presentation.detail(ar),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onOpen,
                enabled = !ui.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("gems-weekly-open")
            ) {
                Text(if (ar) "ابدأ المراجعة الآن" else "Start Review Now")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
    PremiumPage(Modifier.testTag("screen-local-gems")) {
        if(owner==null) {
            Text(text("جواهري", "My gems"), style=MaterialTheme.typography.headlineMedium)
            Text(if(state.user?.type==com.ahmadmol.enmath.core.model.AccountType.Teacher)
                text("أنت في واجهة المعلم. مكافآت الجواهر تخص حساب الطالب.","You are in the teacher workspace. Gem rewards belong to a student account.")
                else text("سجّل الدخول بحساب الطالب داخل EN.MATH وبريد إلكتروني لبدء رصيد خاص بك. لا حساب إضافي للجواهر، ولا مكافآت رجعية.","Sign in to EN.MATH as a student with an email to start your own balance. No separate Gems login or retroactive rewards."))
            Button(onClick=onStudentSignIn,modifier=Modifier.heightIn(min=48.dp).testTag("gems-use-student-account")) { Text(text("دخول حساب الطالب", "Student sign in")) }
            return@PremiumPage
        }
        if(ui.content!=null) {
            RefHeroCard(LocalGemCatalog.item(ui.contentId.orEmpty())?.title?.let { if(ar) it.ar else it.en }.orEmpty(),
                text("محتوى من مقتنياتك", "From your unlocked content"))
            OutlinedButton(onClick=gems::closeContent,modifier=Modifier.heightIn(min=48.dp).testTag("gems-reader-close")) { Text(text("العودة إلى مقتنياتي","Back to my content")) }
            Text(text("محتوى إثرائي تجريبي أصلي؛ يحتاج مراجعة علمية واعتمادًا قبل النشر النهائي.","Original enrichment draft for this pilot; scientific and publication review remain required."),style=MaterialTheme.typography.bodySmall)
            PremiumHeroCard {
                SelectionContainer { Text(ui.content.orEmpty(),style=MaterialTheme.typography.bodyLarge,modifier=Modifier.testTag("gems-original-content")) }
            }
            return@PremiumPage
        }
        // Refined Hero Card matching Image 4
        RefHeroCard(
            title = text("تعلم، واجمع، وافتح محتوى إضافيًا.", "Learn, collect and unlock something extra."),
            subtitle = text("تجربة محلية على هذا الجهاز؛ لا تتزامن بين الأجهزة ولا تمنع التلاعب بالبيانات المحلية.", "A local pilot on this device, without cross-device sync or protection against local data tampering."),
            badgeText = "💎 ${gemBalanceText(ui, owner, state.language)}",
            badgeColor = RefColors.LightBlueHeroBgStart,
            badgeTextColor = RefColors.PrimaryBlue,
            artwork = { GemsHeroArtwork() }
        )

        // Refined Callout Banner matching Image 4
        RefInfoBanner(
            text = text("نظام تجريبي محلي — الجواهر تُحفظ على هذا الجهاز فقط.", "Local pilot — gems are saved on this device only.")
        )

        // Refined Category Tabs matching Image 4
        val tabs = listOf(
            RefTabItem("store", text("متجر الجواهر", "Gem store"), Icons.Outlined.Storefront),
            RefTabItem("wallet", text("محفظتي", "Wallet"), Icons.Outlined.AccountBalanceWallet)
        )
        RefCategoryTabs(
            items = tabs,
            selectedId = if (section == "store") "store" else "wallet",
            onSelect = { section = if (it == "store") "store" else "wallet" }
        )
        if(ui.busy || (ui.account==null && ui.error==null)) LinearProgressIndicator(Modifier.fillMaxWidth())
        if(ui.error!=null) {
            Text(if(ui.error=="INSUFFICIENT_GEMS") text("رصيدك لا يكفي لهذا المحتوى. تابع التعلم لجمع المزيد.","You need more gems for this content. Keep learning to collect more.")
                else text("تعذر حفظ بيانات الجواهر أو قراءتها. رصيدك السابق محفوظ؛ حاول مجددًا.","Gems could not be saved or read. Your existing data is preserved; try again."),color=MaterialTheme.colorScheme.error)
            OutlinedButton(onClick={gems.refresh(state)},modifier=Modifier.heightIn(min=48.dp)) { Text(text("حاول مجددًا","Try again")) }
        }
        if(section=="store") {
            Text(
                text = text("إضافات للتعلّم، والمنهاج الأساسي متاح كالمعتاد.","Extra learning material; the core curriculum remains available as usual."),
                style = MaterialTheme.typography.bodyMedium,
                color = RefColors.TextSecondary
            )
            LocalGemCatalog.items.forEach { item ->
                val owned = ui.account?.unlocks?.any { it.itemId == item.id } == true
                val enough = (ui.account?.balance ?: -1) >= item.price

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("local-gems-item-card-${item.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, RefColors.CardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Space.lg),
                        verticalArrangement = Arrangement.spacedBy(Space.md)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Artwork matching Image 4
                            Surface(
                                modifier = Modifier.size(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = RefColors.LightBlueHeroBgStart,
                                border = BorderStroke(1.dp, RefColors.HeroBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (owned) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = RefColors.PrimaryBlue
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(Space.xs)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (ar) item.title.ar else item.title.en,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = RefColors.TextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (owned) Icons.Outlined.CheckCircle else Icons.Outlined.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (owned) RefColors.MintGreenOutline else RefColors.PrimaryBlue
                                    )
                                }

                                Text(
                                    text = if (ar) item.summary.ar else item.summary.en,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RefColors.TextSecondary
                                )

                                Text(
                                    text = text("محتوى أصلي للتجربة، وليس مادة نهائية معتمدة.", "Original pilot content, not final approved material."),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = RefColors.TextMuted
                                )
                            }
                        }

                        HorizontalDivider(color = RefColors.CardBorder.copy(alpha = 0.5f))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Space.md),
                            verticalArrangement = Arrangement.spacedBy(Space.sm)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Diamond,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = RefColors.PrimaryBlue
                                )
                                Text(
                                    text = if (owned) text("ضمن مقتنياتك", "Already yours") else "${units(item.price, state.language)} ${text("جوهرة", "gems")}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RefColors.PrimaryBlue
                                )
                            }

                            Button(
                                onClick = { if (owned) gems.openContent(state, owner, item.id) else if (enough) confirmation = item.id },
                                enabled = ui.account != null && (owned || enough) && !ui.busy && ui.error == null,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (owned) RefColors.MintGreenFill else RefColors.LightBlueHeroBgStart,
                                    contentColor = if (owned) RefColors.MintGreenText else RefColors.PrimaryBlue
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("local-gems-item-${item.id}")
                            ) {
                                Text(
                                    text = if (owned) text("افتح المحتوى", "Open content") else text("عرض تأكيد الفتح", "Review unlock"),
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(text("مقتنياتي","My unlocked content"),style=MaterialTheme.typography.titleLarge)
            val owned=ui.account?.unlocks.orEmpty()
            if(owned.isEmpty() && ui.account!=null && !ui.busy && ui.error==null) PremiumHeroCard {
                RewardChestArtwork()
                Text(text("محتواك الإضافي سيظهر هنا بعد فتحه بالجواهر.","Your extra content will appear here after you unlock it."))
            }
            owned.forEach { grant -> LocalGemCatalog.item(grant.itemId)?.let { item ->
                OutlinedButton(onClick={gems.openContent(state,owner,item.id)},enabled=!ui.busy,
                    modifier=Modifier.fillMaxWidth().heightIn(min=48.dp).testTag("local-gems-owned-${item.id}")) { Text(if(ar) item.title.ar else item.title.en) }
            } }
            Text(text("سجل الجواهر","Gem history"),style=MaterialTheme.typography.titleLarge)
            if(ui.account!=null && ui.account.rewards.isEmpty() && owned.isEmpty() && !ui.busy && ui.error==null) PremiumHeroCard {
                Box(Modifier.fillMaxWidth(), contentAlignment=Alignment.Center) {
                    Icon(Icons.Outlined.History, null, Modifier.padding(16.dp).size(56.dp), tint=MaterialTheme.colorScheme.primary)
                }
                Text(text("أكمل نشاطًا مؤهلًا لتبدأ جمع الجواهر.","Complete an eligible activity to start collecting gems."))
            }
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
            history.sortedByDescending { it.first }.forEach { (at,title,amount) ->
                PremiumHeroCard {
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(title)
                            Text(java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT,
                                if(ar) Locale.forLanguageTag("ar") else Locale.ENGLISH).apply {
                                timeZone=java.util.TimeZone.getTimeZone("Asia/Damascus")
                            }.format(java.util.Date(at)), style=MaterialTheme.typography.bodySmall, color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(amount,Modifier.widthIn(max=112.dp),color=MaterialTheme.colorScheme.primary)
                    }
                }
            }
            EducationalInfoBanner(text("حدود التجربة: ${units(LocalGemsPolicy.DAILY_CAP,state.language)} جوهرة يوميًا و${units(LocalGemsPolicy.WEEKLY_CAP,state.language)} أسبوعيًا بتوقيت دمشق.","Pilot caps: ${units(LocalGemsPolicy.DAILY_CAP,state.language)} gems daily and ${units(LocalGemsPolicy.WEEKLY_CAP,state.language)} weekly, Damascus time."))
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
    val review=ui.review.takeIf { ui.owner==owner && it?.week == LocalGemsPolicy.week(System.currentTimeMillis()) }
    val ar=state.language==Language.Arabic
    val presentation = weeklyGemCardPresentation(ui, state)
    var score by rememberSaveable(owner) { mutableStateOf<Int?>(null) }
    var attempt by rememberSaveable(owner) { mutableIntStateOf(0) }
    if(score!=null && owner!=null && ui.owner==owner) {
        ScreenPage(Modifier.testTag("screen-weekly-result")) {
            RefHeroCard(tr("Review result", "نتيجة المراجعة"), tr("Reflect on what you learned this week.", "راجع ما تعلمته هذا الأسبوع."), artwork = { RewardChestArtwork() })
            RefStatsRow(listOf(RefStatItem(Icons.Outlined.CheckCircle, "${score}/5", tr("Correct answers", "الإجابات الصحيحة"))))
            if (ui.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(if(score!!>=4) { if(ar) "أحسنت! يُحفظ استحقاق المراجعة ضمن حدود التجربة، دون تغيير درجاتك." else "Well done! Review eligibility is saved within the pilot caps, without changing your grades." }
                else { if(ar) "تحتاج أربع إجابات صحيحة على الأقل. يمكنك المحاولة مجددًا." else "You need at least four correct answers. You can try again." })
            if(ui.error!=null) Text(if(ar) "تعذر حفظ الجواهر. أعد المحاولة لاحقًا؛ لم يتغير رصيدك." else "Gems could not be saved. Try later; your balance is unchanged.",color=MaterialTheme.colorScheme.error)
            if(score!!<4 || ui.error!=null) OutlinedButton(onClick={attempt++;score=null},modifier=Modifier.heightIn(min=48.dp)) {Text(if(ar) "حاول مجددًا" else "Try again")}
            Button(onBack,Modifier.heightIn(min=48.dp)) { Text(if(ar) "العودة" else "Back") }
        }
    } else if (presentation.status != WeeklyGemCardStatus.Ready || review == null) {
        ScreenPage(Modifier.testTag("screen-weekly-status")) {
            RefHeroCard(tr("Weekly review", "مراجعة الأسبوع"), presentation.detail(ar), artwork = { RewardChestArtwork() })
            if (presentation.status == WeeklyGemCardStatus.Loading) CircularProgressIndicator()
            if (presentation.status == WeeklyGemCardStatus.Error) RefSecondaryButton(tr("Try again", "حاول مجددًا"), { gems.refresh(state) })
            OutlinedButton(onBack, Modifier.heightIn(min=48.dp)) { Text(tr("Back", "رجوع")) }
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
