package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.AccountType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.TimeZone
import java.util.UUID

enum class AvatarPose { IDLE, WELCOME, THINKING, CORRECT, TRY_AGAIN, COMPLETED, CELEBRATING, ENCOURAGING, STREAK }
object StudentAvatars {
    val ids = listOf("muadh", "waseem", "lama")
    /**
     * Saved account ids stay muadh, waseem, and lama. Adam, Yousef, Karim, Sara, Lina, and Noor
     * exist only as labeled preview boards and are intentionally not mapped onto these ids.
     * Every pose keeps the saved owner; missing artwork falls back to that same portrait.
     */
    fun poseOwner(id: String?, pose: AvatarPose): String? = id?.takeIf { it in ids }
}
data class LearningDayEvent(val id: String, val at: Long, val day: Long)
data class QaSubmission(val questionId: String, val response: String, val grade: DraftGrade, val at: Long)
data class QaDraft(val id: String, val lessonId: String, val position: Int = 0,
    val answers: Map<String, String> = emptyMap(), val hints: Set<String> = emptySet(),
    val submissions: List<QaSubmission> = emptyList(), val startedAt: Long,
    val finishedAt: Long? = null) {
    fun last(id: String) = submissions.lastOrNull { it.questionId == id }
    val independentCorrect get() = submissions.groupBy { it.questionId }.count { (id, rows) -> rows.size == 1 && rows.single().grade == DraftGrade.CORRECT && id !in hints }
}
data class ModernStudentData(val selectedAvatarId: String? = null, val zone: String = TimeZone.getDefault().id,
    val events: List<LearningDayEvent> = emptyList(), val clockHighWater: Long = 0,
    val resumeLesson: String? = null, val reducedMotion: Boolean = false,
    val drafts: List<QaDraft> = emptyList(), val results: List<QaDraft> = emptyList())
sealed interface ModernRead {
    data class Ready(val data: ModernStudentData) : ModernRead
    data object OwnerChanged : ModernRead
    data object StorageError : ModernRead
}
enum class ModernWrite { SAVED, ALREADY_SAVED, INVALID, OWNER_CHANGED, STORAGE_ERROR, CLOCK_REVIEW }

object LearningStreak {
    private const val DAY = 86_400_000L
    fun day(at: Long, zone: String) = Math.floorDiv(at + TimeZone.getTimeZone(zone).getOffset(at), DAY)
    fun current(events: List<LearningDayEvent>, now: Long, zone: String): Int {
        val days = events.map { it.day }.toSet(); val today = day(now, zone)
        var cursor = if (today in days) today else today - 1
        var count = 0
        while (cursor in days) { count++; cursor-- }
        return count
    }
}

/** Additive UUID-owned storage. Never migrates or writes learner snapshots, planner or Gems. */
class ModernStudentRepository internal constructor(private val preferences: (String) -> SharedPreferences,
    private val current: () -> LearningState, private val evidence: () -> Map<String, Long>) {
    private val changed = MutableStateFlow(0L)
    val changes: StateFlow<Long> = changed
    private val poisoned = mutableSetOf<String>()
    private fun accepts(scope: StudentScope) = StudentCatalog.accepts(current(), scope) &&
        current().catalogAvailable && current().user?.type != AccountType.Teacher
    private fun key(scope: StudentScope): String {
        UUID.fromString(scope.ownerId)
        return "enmath_student_experience_v1_${scope.ownerId}"
    }
    private fun strings(a: JSONArray) = (0 until a.length()).map(a::getString)
    private fun parseDraft(o: JSONObject): QaDraft {
        val answers = o.getJSONObject("answers").let { a -> a.keys().asSequence().associateWith(a::getString) }
        val submissions = o.getJSONArray("submissions").let { a -> (0 until a.length()).map { i -> a.getJSONObject(i).let { s ->
            QaSubmission(s.getString("question"), s.getString("response"), DraftGrade.valueOf(s.getString("grade")), s.getLong("at"))
        } } }
        return QaDraft(o.getString("id"), o.getString("lesson"), o.getInt("position"), answers,
            strings(o.getJSONArray("hints")).toSet(), submissions, o.getLong("started"),
            if (o.has("finished")) o.getLong("finished") else null).also { d ->
            UUID.fromString(d.id)
            require(SourceCurricula.forStage(com.ahmadmol.enmath.core.model.StudyStage.G9).research(d.lessonId) != null)
            require(d.position in 0..5 && d.startedAt >= 0 && (d.finishedAt == null || d.finishedAt >= d.startedAt))
            val draftPrefix = d.lessonId.replace("G9-ALG", "G9-A").replace("G9-GEO", "G9-H")
            fun validQuestion(id: String) = id.startsWith("$draftPrefix-Q") && id.matches(Regex("G9-[AH]-U[0-9]{2}-L[0-9]{2}-Q0[1-6]"))
            require(answers.all { validQuestion(it.key) && it.value.length <= 2048 })
            require(d.hints.all(::validQuestion) && submissions.all { validQuestion(it.questionId) && it.response.length <= 2048 && it.at >= d.startedAt })
        }
    }
    private fun readStored(scope: StudentScope): ModernStudentData {
        check(key(scope) !in poisoned)
        val raw = preferences(key(scope)).getString("document", null) ?: return ModernStudentData()
        val o = JSONObject(raw)
        require(o.getInt("version") == 1 && o.getString("owner") == scope.ownerId && o.getString("stage") == scope.stage.name)
        val events = o.getJSONArray("events").let { a -> (0 until a.length()).map { i -> a.getJSONObject(i).let { e -> LearningDayEvent(e.getString("id"), e.getLong("at"), e.getLong("day")) } } }
        val drafts = o.getJSONArray("drafts").let { a -> (0 until a.length()).map { parseDraft(a.getJSONObject(it)) } }
        val results = o.getJSONArray("results").let { a -> (0 until a.length()).map { parseDraft(a.getJSONObject(it)) } }
        val zone = o.getString("zone"); require(zone in TimeZone.getAvailableIDs())
        val avatar = o.optString("avatar").takeIf(String::isNotBlank); require(avatar == null || avatar in StudentAvatars.ids)
        require(events.map { it.id }.distinct().size == events.size && events.all { it.id.isNotBlank() && it.at >= 0 && it.day == LearningStreak.day(it.at, zone) })
        require(drafts.map { it.lessonId }.distinct().size == drafts.size && drafts.all { it.finishedAt == null } && results.all { it.finishedAt != null })
        require((drafts + results).map { it.id }.distinct().size == drafts.size + results.size)
        val resume = o.optString("resume").takeIf(String::isNotBlank)
        require(resume == null || SourceCurricula.forStage(scope.stage).research(resume) != null)
        return ModernStudentData(avatar, zone, events, o.getLong("clock"), resume, o.getBoolean("reducedMotion"), drafts, results)
    }
    private fun draftJson(d: QaDraft) = JSONObject().put("id", d.id).put("lesson", d.lessonId).put("position", d.position)
        .put("answers", JSONObject(d.answers)).put("hints", JSONArray(d.hints.toList())).put("started", d.startedAt)
        .put("submissions", JSONArray().apply { d.submissions.forEach { put(JSONObject().put("question", it.questionId).put("response", it.response).put("grade", it.grade.name).put("at", it.at)) } })
        .apply { d.finishedAt?.let { put("finished", it) } }
    private fun store(scope: StudentScope, data: ModernStudentData): ModernWrite {
        if (!accepts(scope)) return ModernWrite.OWNER_CHANGED
        val o = JSONObject().put("version", 1).put("owner", scope.ownerId).put("stage", scope.stage.name)
            .put("avatar", data.selectedAvatarId.orEmpty()).put("zone", data.zone).put("clock", data.clockHighWater)
            .put("resume", data.resumeLesson.orEmpty()).put("reducedMotion", data.reducedMotion)
            .put("events", JSONArray().apply { data.events.forEach { put(JSONObject().put("id", it.id).put("at", it.at).put("day", it.day)) } })
            .put("drafts", JSONArray().apply { data.drafts.forEach { put(draftJson(it)) } })
            .put("results", JSONArray().apply { data.results.forEach { put(draftJson(it)) } })
        val committed = runCatching { preferences(key(scope)).edit().putString("document", o.toString()).commit() }.getOrDefault(false)
        if (!committed) poisoned += key(scope)
        changed.value++
        return when { !committed -> ModernWrite.STORAGE_ERROR; !accepts(scope) -> ModernWrite.OWNER_CHANGED; else -> ModernWrite.SAVED }
    }
    @Synchronized fun read(scope: StudentScope): ModernRead = if (!accepts(scope)) ModernRead.OwnerChanged else
        try { ModernRead.Ready(readStored(scope)) } catch (_: Exception) { ModernRead.StorageError }
    private fun mutate(scope: StudentScope, transform: (ModernStudentData) -> ModernStudentData): ModernWrite {
        if (!accepts(scope)) return ModernWrite.OWNER_CHANGED
        val old = try { readStored(scope) } catch (_: Exception) { return ModernWrite.STORAGE_ERROR }
        return try { val next = transform(old)
            if (!accepts(scope)) return ModernWrite.OWNER_CHANGED
            if (old == next) ModernWrite.ALREADY_SAVED else store(scope, next)
        } catch (_: IllegalArgumentException) { ModernWrite.INVALID }
          catch (_: Exception) { ModernWrite.STORAGE_ERROR }
    }
    @Synchronized fun selectAvatar(scope: StudentScope, id: String) = mutate(scope) { require(id in StudentAvatars.ids); it.copy(selectedAvatarId = id) }
    @Synchronized fun reducedMotion(scope: StudentScope, value: Boolean) = mutate(scope) { it.copy(reducedMotion = value) }
    @Synchronized fun rememberStation(scope: StudentScope, id: String) = mutate(scope) {
        require(SourceCurricula.forStage(scope.stage).research(id) != null); it.copy(resumeLesson = id)
    }
    /** Replayed from actual persisted practice only. Source visits, plans and QA drafts cannot enter. */
    @Synchronized fun refreshLearningDays(scope: StudentScope, now: Long = System.currentTimeMillis()): ModernWrite {
        if (!accepts(scope)) return ModernWrite.OWNER_CHANGED
        return try {
            val old = readStored(scope)
            if (now < 0 || now < old.clockHighWater - 300_000) return ModernWrite.CLOCK_REVIEW
            val recorded = old.events.associateBy { it.id }
            val incoming = evidence().filter { (id, at) -> id !in recorded && at in 0..(now + 300_000) }
                .map { (id, at) -> LearningDayEvent(id, at, LearningStreak.day(at, old.zone)) }
            if (incoming.isEmpty()) ModernWrite.ALREADY_SAVED
            else store(scope, old.copy(events = (old.events + incoming).sortedBy { it.at }, clockHighWater = maxOf(now, old.clockHighWater)))
        } catch (_: Exception) { ModernWrite.STORAGE_ERROR }
    }
    @Synchronized fun startQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, now: Long = System.currentTimeMillis()) = mutate(scope) {
        require(scope.stage == com.ahmadmol.enmath.core.model.StudyStage.G9 && bank.forLesson(lesson).size == 6 && now >= 0)
        if (it.drafts.any { d -> d.lessonId == lesson }) it else it.copy(drafts = it.drafts + QaDraft(UUID.randomUUID().toString(), lesson, startedAt = now))
    }
    private fun editQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, change: (QaDraft, List<DraftExercise>) -> QaDraft) = mutate(scope) { data ->
        val draft = requireNotNull(data.drafts.find { it.lessonId == lesson }); val qs = bank.forLesson(lesson)
        require(qs.size == 6 && scope.stage == com.ahmadmol.enmath.core.model.StudyStage.G9)
        data.copy(drafts = data.drafts.map { if (it.id == draft.id) change(draft, qs) else it })
    }
    @Synchronized fun answerQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, response: String) = editQa(scope, lesson, bank) { d, qs ->
        require(response.length <= 2048)
        d.copy(answers = d.answers + (qs[d.position].id to response))
    }
    @Synchronized fun hintQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank) = editQa(scope, lesson, bank) { d, qs -> d.copy(hints = d.hints + qs[d.position].id) }
    @Synchronized fun submitQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, now: Long = System.currentTimeMillis()) = editQa(scope, lesson, bank) { d, qs ->
        val q = qs[d.position]; val response = d.answers[q.id].orEmpty(); require(response.isNotBlank() && now >= d.startedAt)
        // Repeated taps on an unchanged response are idempotent, including incorrect answers.
        val last = d.last(q.id)
        if (last?.response == response) d else
            d.copy(submissions = d.submissions + QaSubmission(q.id, response, OriginalDraftGrader.grade(q, response), now))
    }
    @Synchronized fun moveQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, next: Int) = editQa(scope, lesson, bank) { d, qs ->
        require(next in 0..5 && next <= d.position + 1)
        if (next > d.position) require(d.last(qs[d.position].id)?.let { it.response == d.answers[qs[d.position].id] && it.grade != DraftGrade.INCORRECT } == true)
        d.copy(position = next)
    }
    @Synchronized fun finishQa(scope: StudentScope, lesson: String, bank: OriginalDraftBank, now: Long = System.currentTimeMillis()) = mutate(scope) { data ->
        val d = requireNotNull(data.drafts.find { it.lessonId == lesson }); val qs = bank.forLesson(lesson)
        require(now >= d.startedAt && qs.size == 6 && qs.all { q -> d.last(q.id)?.let { it.grade != DraftGrade.INCORRECT && it.response == d.answers[q.id] } == true })
        data.copy(drafts = data.drafts.filterNot { it.id == d.id }, results = data.results + d.copy(finishedAt = now))
    }
}
