package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.Attempt
import com.ahmadmol.enmath.core.model.StudyStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class G9PracticeDraft(val id: String, val answers: Map<String, String> = emptyMap(),
    val checked: Set<String> = emptySet(), val hints: Set<String> = emptySet(), val index: Int = 0,
    val started: Long = System.currentTimeMillis(), val sequence: Long = 0)
data class G9SavedAttempt(val record: Attempt, val hints: Set<String> = emptySet())
data class StudentFile(val id: String, val uri: String, val name: String, val mime: String,
    val bytes: Long? = null, val page: Int = 0, val bookId: String? = null, val unitId: String? = null,
    val researchId: String? = null, val favorite: Boolean = false)
data class G9StudyData(val attempts: List<G9SavedAttempt> = emptyList(), val checkpoint: String? = null,
    val section: Int = 0, val draft: G9PracticeDraft? = null, val files: List<StudentFile> = emptyList())
sealed interface G9StudyRead {
    data class Ready(val data: G9StudyData) : G9StudyRead
    data object OwnerChanged : G9StudyRead
    data object StorageError : G9StudyRead
}

/** Additive pilot/resource document owned by the existing LocalDemoRepository/StudentScope.
 * Existing learner, planner, source-bookmark and wallet documents are never rewritten.
 * Failed commits poison this instance until restart; corrupt/future documents fail closed.
 */
class Grade9StudyRepository internal constructor(private val preferences: (String) -> SharedPreferences,
    private val current: () -> LearningState) {
    private val changed = MutableStateFlow(0L)
    val changes: StateFlow<Long> = changed
    private val poisoned = mutableSetOf<String>()
    private fun accepts(scope: StudentScope) = current().user?.type != com.ahmadmol.enmath.core.model.AccountType.Teacher && scope.stage == StudyStage.G9 &&
        StudentCatalog.accepts(current(), scope) && current().catalogAvailable &&
        StudentCatalog.acceptsReference(current(), Grade9Pilot.ref)
    private fun key(scope: StudentScope): String {
        UUID.fromString(scope.ownerId)
        return "enmath_g9_study_v1_${scope.ownerId}"
    }
    private fun strings(values: Set<String>) = JSONArray(values.toList())
    private fun set(doc: JSONObject, key: String): Set<String> = doc.getJSONArray(key).let { a ->
        (0 until a.length()).map { a.getString(it) }.also { require(it.distinct().size == it.size) }.toSet()
    }
    private fun answers(doc: JSONObject): Map<String, String> = doc.keys().asSequence().associateWith { doc.getString(it) }
    private fun validAnswers(values: Map<String, String>) = values.keys.all { id -> Grade9Pilot.questions.any { it.id == id } } && values.values.all { it.length <= 128 }
    private fun validDraft(d: G9PracticeDraft) = d.id.isNotBlank() && d.id.length <= 80 && validAnswers(d.answers) &&
        d.checked.all { it in d.answers } && d.hints.all { id -> Grade9Pilot.questions.any { it.id == id } } &&
        d.index in Grade9Pilot.questions.indices && d.started >= 0 && d.sequence >= 0
    private fun load(scope: StudentScope): G9StudyData {
        val key = key(scope); check(key !in poisoned)
        val raw = preferences(key).getString("study", null) ?: return G9StudyData()
        val doc = JSONObject(raw)
        require(doc.getInt("version") == 1 && doc.getString("owner") == scope.ownerId &&
            doc.getString("edition") == scope.edition && doc.getString("stage") == scope.stage.name &&
            doc.getString("sourceRef") == StudentCatalog.referenceKey(Grade9Pilot.ref) &&
            doc.getString("revision") == Grade9Pilot.revision)
        val attempts = doc.getJSONArray("attempts").let { a -> (0 until a.length()).map { i ->
            val item = a.getJSONObject(i); val values = answers(item.getJSONObject("answers"))
            require(validAnswers(values) && item.getInt("seconds") in 0..86400 && item.getLong("at") >= 0)
            val command = item.getString("id"); require(command.isNotBlank() && command.length <= 80)
            val hints = set(item, "hints"); require(hints.all { id -> Grade9Pilot.questions.any { it.id == id } })
            G9SavedAttempt(Attempt(Grade9Pilot.practiceId, values,
                Grade9Pilot.questions.count { Grade9Pilot.correct(it, values[it.id].orEmpty()) },
                Grade9Pilot.questions.size, item.getInt("seconds"), item.getLong("at"),
                Grade9Pilot.revision, StudyStage.G9, command), hints)
        } }
        require(attempts.map { it.record.commandId }.distinct().size == attempts.size)
        val draft = doc.optJSONObject("draft")?.let {
            G9PracticeDraft(it.getString("id"), answers(it.getJSONObject("answers")), set(it, "checked"),
                set(it, "hints"), it.getInt("index"), it.getLong("started"), it.optLong("sequence", 0)).also { d ->
                require(validDraft(d) && attempts.none { a -> a.record.commandId == d.id })
            }
        }
        val files = doc.getJSONArray("files").let { a -> (0 until a.length()).map { i ->
            val f = a.getJSONObject(i)
            StudentFile(f.getString("id"), f.getString("uri"), f.getString("name"), f.getString("mime"),
                if (f.has("bytes")) f.getLong("bytes") else null, f.getInt("page"), f.optString("bookId").takeIf(String::isNotBlank),
                f.optString("unitId").takeIf(String::isNotBlank), f.optString("researchId").takeIf(String::isNotBlank), f.optBoolean("favorite", false)).also { require(validFile(it)) }
        } }
        require(files.map { it.id }.distinct().size == files.size && files.map { it.uri }.distinct().size == files.size)
        val checkpoint = if (doc.has("checkpoint")) doc.getString("checkpoint") else null
        require(checkpoint == null || Grade9Pilot.correct(Grade9Pilot.checkpoint, checkpoint))
        val section = doc.getInt("section"); require(section in 0..30)
        return G9StudyData(attempts, checkpoint, section, draft, files)
    }
    @Synchronized fun read(scope: StudentScope): G9StudyRead = if (!accepts(scope)) G9StudyRead.OwnerChanged else
        try { G9StudyRead.Ready(load(scope)) } catch (_: Exception) { G9StudyRead.StorageError }

    private fun encode(scope: StudentScope, data: G9StudyData): String = JSONObject()
        .put("version", 1).put("owner", scope.ownerId).put("stage", scope.stage.name).put("edition", scope.edition)
        .put("sourceRef", StudentCatalog.referenceKey(Grade9Pilot.ref)).put("revision", Grade9Pilot.revision)
        .put("section", data.section).apply {
            data.checkpoint?.let { put("checkpoint", it) }
            data.draft?.let { d -> put("draft", JSONObject().put("id", d.id).put("answers", JSONObject(d.answers))
                .put("checked", strings(d.checked)).put("hints", strings(d.hints)).put("index", d.index).put("started", d.started).put("sequence", d.sequence)) }
            put("attempts", JSONArray().apply { data.attempts.forEach { a -> put(JSONObject()
                .put("id", a.record.commandId).put("answers", JSONObject(a.record.answers))
                .put("seconds", a.record.seconds).put("at", a.record.timestamp).put("hints", strings(a.hints))) } })
            put("files", JSONArray().apply { data.files.forEach { f -> put(JSONObject().put("id", f.id)
                .put("uri", f.uri).put("name", f.name).put("mime", f.mime).put("page", f.page).put("bookId", f.bookId).put("unitId", f.unitId)
                .put("researchId", f.researchId).put("favorite", f.favorite)
                .apply { f.bytes?.let { put("bytes", it) } }) } })
        }.toString()
    @Synchronized private fun update(scope: StudentScope, transform: (G9StudyData) -> G9StudyData?): PlanWrite {
        if (!accepts(scope)) return PlanWrite.OWNER_CHANGED
        return try {
            val old = load(scope); val next = transform(old) ?: return PlanWrite.INVALID
            if (old == next) return PlanWrite.ALREADY_SAVED
            if (!accepts(scope)) return PlanWrite.OWNER_CHANGED
            val key = key(scope)
            if (!preferences(key).edit().putString("study", encode(scope, next)).commit()) {
                poisoned += key; PlanWrite.STORAGE_ERROR
            } else { changed.value += 1; PlanWrite.SAVED }
        } catch (_: Exception) { PlanWrite.STORAGE_ERROR }
    }
    fun section(scope: StudentScope, section: Int) = update(scope) { if (section in 0..30) it.copy(section = section) else null }
    fun passCheckpoint(scope: StudentScope, answer: String) = update(scope) {
        if (answer.length <= 128 && Grade9Pilot.correct(Grade9Pilot.checkpoint, answer)) it.copy(checkpoint = answer) else null
    }
    fun saveDraft(scope: StudentScope, draft: G9PracticeDraft) = update(scope) {
        val previous = it.draft
        if (validDraft(draft) && it.attempts.none { a -> a.record.commandId == draft.id } &&
            (previous == null || previous == draft || (previous.id == draft.id && draft.sequence > previous.sequence))) it.copy(draft = draft) else null
    }
    fun finish(scope: StudentScope, draft: G9PracticeDraft, seconds: Int, now: Long = System.currentTimeMillis()) = update(scope) { old ->
        if (!validDraft(draft) || seconds !in 0..86400 || now < 0) return@update null
        val previous = old.attempts.find { it.record.commandId == draft.id }
        if (previous != null) return@update old.takeIf { previous.record.answers == draft.answers && previous.record.seconds == seconds && previous.hints == draft.hints }
        if (old.draft?.let { active -> active.id != draft.id || active.sequence > draft.sequence ||
                (active.sequence == draft.sequence && active != draft) } == true) return@update null
        val record = Attempt(Grade9Pilot.practiceId, draft.answers.toMap(), Grade9Pilot.questions.count {
            Grade9Pilot.correct(it, draft.answers[it.id].orEmpty()) }, Grade9Pilot.questions.size, seconds, now,
            Grade9Pilot.revision, StudyStage.G9, draft.id)
        old.copy(attempts = old.attempts + G9SavedAttempt(record, draft.hints.toSet()), draft = null)
    }
    fun addFile(scope: StudentScope, file: StudentFile) = update(scope) { old ->
        when {
            !validFile(file) -> null
            old.files.any { it.uri == file.uri } -> old
            old.files.any { it.id == file.id } -> null
            else -> old.copy(files = old.files + file)
        }
    }
    fun organizeFile(scope: StudentScope, id: String, bookId: String?, unitId: String?, researchId: String?, favorite: Boolean) = update(scope) { old ->
        val file = old.files.find { it.id == id } ?: return@update null
        val next = file.copy(bookId = bookId, unitId = unitId, researchId = researchId, favorite = favorite)
        if (!validFile(next)) null else old.copy(files = old.files.map { if (it.id == id) next else it })
    }
    fun removeFile(scope: StudentScope, id: String) = update(scope) { it.copy(files = it.files.filterNot { f -> f.id == id }) }
    fun filePage(scope: StudentScope, id: String, page: Int) = update(scope) { old ->
        if (page !in 0..100000 || old.files.none { it.id == id }) null else old.copy(files = old.files.map { if (it.id == id) it.copy(page = page) else it })
    }
    companion object {
        val allowedMimes = setOf("application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        private fun validClassification(file: StudentFile): Boolean {
            val catalog = SourceCurricula.forStage(com.ahmadmol.enmath.core.model.StudyStage.G9)
            return (file.bookId == null || catalog.book(file.bookId) != null) &&
                (file.unitId == null || catalog.unit(file.unitId)?.let { it.bookId == file.bookId } == true) &&
                (file.researchId == null || catalog.research(file.researchId)?.let { it.unitId == file.unitId } == true)
        }
        fun validFile(file: StudentFile) = file.id.isNotBlank() && file.id.length <= 80 && file.uri.startsWith("content://") &&
            runCatching { java.net.URI(file.uri).let { it.scheme == "content" && !it.rawAuthority.isNullOrBlank() && it.rawUserInfo == null } }.getOrDefault(false) &&
            file.uri.length <= 4096 && file.name.isNotBlank() && file.name.length <= 256 &&
            file.name.none { it.isISOControl() } && file.mime in allowedMimes &&
            (file.bytes == null || file.bytes >= 0) && file.page in 0..100000 && validClassification(file)
    }
}
