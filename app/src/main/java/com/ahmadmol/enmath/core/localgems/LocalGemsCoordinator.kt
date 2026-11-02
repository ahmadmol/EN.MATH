package com.ahmadmol.enmath.core.localgems

import android.content.Context
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.model.PendingGemEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LocalGemsUiState(val owner: String?=null, val account: GemAccount?=null, val busy: Boolean=false,
    val error: String?=null, val review: GemWeeklyReview?=null, val contentId: String?=null, val content: String?=null)
data class GemNotice(val owner: String, val units: Int)

/** Owns an independent local store. No server identity, legacy Gems wallet or imported balances. */
class LocalGemsCoordinator(private val storage: LocalGemsStorage, private val scope: CoroutineScope,
    private val assets: (String)->String, private val clock: ()->Long=System::currentTimeMillis) {
    private data class Ticket(val owner: String, val generation: Long)
    private val lock=Mutex(); private var generation=0L; @Volatile private var active: Ticket?=null
    private val _ui=MutableStateFlow(LocalGemsUiState());val ui: StateFlow<LocalGemsUiState> = _ui.asStateFlow()
    private val _notices=MutableSharedFlow<GemNotice>(extraBufferCapacity=8);val notices = _notices.asSharedFlow()
    fun bind(state: LearningState) {
        val owner=LocalGemsPolicy.owner(state.user)
        if(owner==active?.owner && (owner!=null || _ui.value.owner==null)) return
        generation++;active=owner?.let { Ticket(it,generation) }
        _ui.value=LocalGemsUiState(owner=owner,busy=owner!=null) // hide old owner's data synchronously
        val ticket=active ?: return
        val excluded=LocalLearningGems.baseline(state)
        val now=minOf(clock(), state.product.pendingGemEvents.minOfOrNull { it.at } ?: clock())
        enqueue(ticket) { publish(ticket,storage.activate(ticket.owner,excluded,now),state) }
    }
    private fun ticket(state: LearningState): Ticket? = active?.takeIf { it.owner==LocalGemsPolicy.owner(state.user) }
    private fun enqueue(ticket: Ticket, action: suspend ()->Unit) {
        scope.launch {
            lock.withLock {
                try { withContext(Dispatchers.IO) { action() } }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { if(active==ticket) _ui.value=_ui.value.copy(busy=false,error="STORAGE_UNAVAILABLE",content=null,contentId=null) }
            }
        }
    }
    private fun publish(ticket: Ticket, account: GemAccount, state: LearningState) {
        if(active!=ticket) return
        val previous=_ui.value
        _ui.value=LocalGemsUiState(ticket.owner,account,review=LocalLearningGems.weekly(account,state,clock()),contentId=previous.contentId,content=previous.content)
    }
    private fun change(state: LearningState, transform: (GemAccount,Long)->GemAccount) {
        val ticket=ticket(state) ?: return;val at=clock()
        enqueue(ticket) {
            var added=0
            val account=storage.change(ticket.owner) { old ->
                transform(old,at).also { next -> added=next.rewards.sumOf { it.units }-old.rewards.sumOf { it.units } }
            }
            publish(ticket,account,state)
            if(active==ticket && added>0) _notices.emit(GemNotice(ticket.owner,added))
        }
    }
    fun verifyLesson(state: LearningState, id: String, answer: String) {
        if(!LocalLearningGems.lessonVerified(state,id,answer)) return
        change(state) { account,_ -> account.copy(lessons=account.lessons+id) }
    }
    /** At-least-once delivery from the acknowledged learner snapshot, with ledger dedupe. */
    fun deliver(state: LearningState, event: PendingGemEvent, acknowledge: () -> Unit) {
        if (event.owner != LearningRules.commandOwner(state) || event !in state.product.pendingGemEvents) return
        val ticket = ticket(state) ?: return
        enqueue(ticket) {
            if (active != ticket) return@enqueue
            val account = storage.change(ticket.owner) { old ->
                val at = maxOf(event.at, old.activatedAt)
                when (event.kind) {
                    "LESSON" -> {
                        val verified = event.answers["checkpoint"]?.let {
                            LocalLearningGems.lessonVerified(state, event.contentId, it)
                        } == true || LocalLearningGems.interactiveVerified(state, event.contentId, event.answers)
                        if (event.contentId !in state.completed || !verified) old
                        else LocalGemsPolicy.award(old.copy(lessons = old.lessons + event.contentId), GemReward.LESSON, event.contentId, at)
                    }
                    "PRACTICE" -> LocalLearningGems.practice(old, state, event.contentId, event.answers, at)
                    "ASSIGNMENT" -> LocalLearningGems.assignment(old, state, event.contentId, event.answers, at)
                    else -> error("Unsupported durable learning event")
                }
            }
            // A crash/failure here keeps the pending event; replay cannot mint the grant twice.
            acknowledge()
            publish(ticket, account, state)
        }
    }
    fun completeLesson(state: LearningState, id: String, interactiveAnswers: Map<String,String>?=null) {
        if(id !in state.completed) return
        val interactive=interactiveAnswers?.let { LocalLearningGems.interactiveVerified(state,id,it) }==true
        change(state) { account,at ->
            val verified=if(interactive) account.copy(lessons=account.lessons+id) else account
            if(id !in verified.lessons) verified else LocalLearningGems.mastery(LocalGemsPolicy.award(verified,GemReward.LESSON,id,at),state,at)
        }
    }
    fun practice(state: LearningState, id: String, answers: Map<String,String>) {
        val captured=answers.toMap();change(state) { account,at -> LocalLearningGems.practice(account,state,id,captured,at) }
    }
    fun assignment(state: LearningState, id: String, answers: Map<String,String>) {
        val captured=answers.toMap();change(state) { account,at -> LocalLearningGems.assignment(account,state,id,captured,at) }
    }
    fun finishWeekly(state: LearningState, expectedOwner: String?, review: GemWeeklyReview, answers: Map<String,String>) {
        if(active?.owner!=expectedOwner) return
        val captured=answers.toMap();change(state) { account,at -> LocalLearningGems.finishWeekly(account,state,review,captured,at) }
    }
    fun purchase(state: LearningState, expectedOwner: String?, id: String) {
        if (state.user?.studyStage == com.ahmadmol.enmath.core.model.StudyStage.G9) return
        val ticket=ticket(state)?.takeIf { it.owner==expectedOwner } ?: return
        val item=LocalGemCatalog.item(id) ?: return
        _ui.value=_ui.value.copy(busy=true,error=null)
        enqueue(ticket) {
            try {
                publish(ticket,storage.change(ticket.owner) { acc ->
                    // D05 display compatibility never authorizes spending unearned currency.
                    LocalGemsPolicy.unlock(acc, item, clock())
                }, state)
            } catch (_: InsufficientLocalGems) {
                if(active==ticket) _ui.value=_ui.value.copy(busy=false,error="INSUFFICIENT_GEMS")
            }
        }
    }
    fun openContent(state: LearningState, expectedOwner: String?, id: String) {
        if (state.user?.studyStage == com.ahmadmol.enmath.core.model.StudyStage.G9) return
        val ticket=ticket(state)?.takeIf { it.owner==expectedOwner } ?: return
        val item=LocalGemCatalog.item(id) ?: return
        val language=state.language
        enqueue(ticket) {
            val account=storage.read(ticket.owner)
            check(account.unlocks.any { it.itemId==id })
            val body=assets("local_gems/${item.asset}_${if(language==com.ahmadmol.enmath.core.model.Language.Arabic) "ar" else "en"}.txt")
            if(active==ticket) _ui.value=_ui.value.copy(contentId=id,content=body,error=null)
        }
    }
    fun closeContent() { _ui.value=_ui.value.copy(contentId=null,content=null) }
    fun refresh(state: LearningState) {
        val ticket=ticket(state) ?: return
        // INSERT IGNORE makes this a read for existing accounts and allows retrying a failed first activation.
        enqueue(ticket) { publish(ticket,storage.activate(ticket.owner,LocalLearningGems.baseline(state),clock()),state) }
    }
    companion object {
        fun create(context: Context, scope: CoroutineScope)=LocalGemsCoordinator(SQLiteLocalGems(context),scope,
            { path -> context.applicationContext.assets.open(path).bufferedReader().use { it.readText() } })
    }
}
