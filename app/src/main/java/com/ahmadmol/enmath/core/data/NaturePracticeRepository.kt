package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.AccountType
import com.ahmadmol.enmath.core.model.StudyStage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Additive, isolated store: never reads or rewrites legacy Grade9Study, planner or Gems. */
data class NatureDraft(
    val id: String,
    val position: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val passed: Set<String> = emptySet(),
    val missed: Map<String, Int> = emptyMap(),
    val hints: Set<String> = emptySet(),
    val startedAt: Long = System.currentTimeMillis()
)
data class NatureAttempt(val id: String, val answers: Map<String, String>, val missed: Map<String, Int>,
    val hints: Set<String>, val startedAt: Long, val finishedAt: Long)
data class NaturePracticeData(val draft: NatureDraft? = null, val attempts: List<NatureAttempt> = emptyList())
sealed interface NatureRead {
    data class Ready(val data: NaturePracticeData) : NatureRead
    data object OwnerChanged : NatureRead
    data object StorageError : NatureRead
}
data class NatureOperation(val status: PlanWrite, val correct: Boolean? = null, val resultId: String? = null)

class NaturePracticeRepository internal constructor(
    private val preferences: (String) -> SharedPreferences,
    private val current: () -> LearningState
) {
    private val changed = MutableStateFlow(0L)
    val changes: StateFlow<Long> = changed.asStateFlow()
    private val poisoned = mutableSetOf<String>()
    private val allIds = (1..6).map { "${NaturePracticeGrader.SOURCE_LESSON}-Q${it.toString().padStart(2, '0')}" }
    private fun accepts(scope: StudentScope) = scope.stage == StudyStage.G9 &&
        current().user?.type != AccountType.Teacher && current().catalogAvailable &&
        StudentCatalog.accepts(current(), scope)
    private fun key(scope: StudentScope): String {
        UUID.fromString(scope.ownerId)
        return "enmath_g9_nature_pilot_v1_${scope.ownerId}"
    }
    private fun toMap(obj: JSONObject): Map<String, String> = obj.keys().asSequence().associateWith { obj.getString(it) }
    private fun counts(obj: JSONObject): Map<String, Int> = obj.keys().asSequence().associateWith { obj.getInt(it) }
    private fun toSet(a: JSONArray): Set<String> = (0 until a.length()).map { a.getString(it) }.also {
        require(it.size == it.distinct().size)
    }.toSet()
    private fun array(a: Set<String>) = JSONArray(a.toList())
    private fun validAnswers(map: Map<String, String>) = map.keys.all { it in allIds } && map.values.all { it.length <= 512 }
    private fun validCounts(map: Map<String, Int>) = map.keys.all { it in allIds } && map.values.all { it in 1..1000000 }
    private fun validDraft(d: NatureDraft) = runCatching {
        UUID.fromString(d.id); d.position in 0..5 && d.startedAt >= 0 && validAnswers(d.answers) &&
            d.passed.all { it in d.answers } && d.passed.all { NaturePracticeGrader.correct(it, d.answers.getValue(it)) } &&
            d.hints.all { it in allIds } && validCounts(d.missed) &&
            allIds.take(d.position).all { it in d.passed }
    }.getOrDefault(false)
    private fun validAttempt(a: NatureAttempt) = runCatching {
        UUID.fromString(a.id); a.startedAt >= 0 && a.finishedAt >= a.startedAt && validAnswers(a.answers) &&
            allIds.all { it in a.answers && NaturePracticeGrader.correct(it, a.answers.getValue(it)) } &&
            a.hints.all { it in allIds } && validCounts(a.missed)
    }.getOrDefault(false)

    private fun readStored(scope: StudentScope): NaturePracticeData {
        check(key(scope) !in poisoned)
        val raw = preferences(key(scope)).getString("practice", null) ?: return NaturePracticeData()
        val o = JSONObject(raw)
        require(o.getInt("version") == 1 && o.getString("owner") == scope.ownerId &&
            o.getString("stage") == scope.stage.name && o.getString("edition") == scope.edition &&
            o.getString("lesson") == NaturePracticeGrader.SOURCE_LESSON)
        val d = o.optJSONObject("draft")?.let { obj ->
            NatureDraft(obj.getString("id"), obj.getInt("position"), toMap(obj.getJSONObject("answers")),
                toSet(obj.getJSONArray("passed")), counts(obj.getJSONObject("missed")),
                toSet(obj.getJSONArray("hints")), obj.getLong("startedAt")).also { require(validDraft(it)) }
        }
        val arr = o.getJSONArray("attempts")
        val attempts = (0 until arr.length()).map { i -> arr.getJSONObject(i).let { obj ->
            NatureAttempt(obj.getString("id"), toMap(obj.getJSONObject("answers")),
                counts(obj.getJSONObject("missed")), toSet(obj.getJSONArray("hints")),
                obj.getLong("startedAt"), obj.getLong("finishedAt")).also { require(validAttempt(it)) }
        } }
        require(attempts.map { it.id }.distinct().size == attempts.size)
        require(d == null || attempts.none { it.id == d.id })
        return NaturePracticeData(d, attempts)
    }

    private fun store(scope: StudentScope, data: NaturePracticeData): Boolean {
        if (!accepts(scope)) return false // prevent writes from an out-of-date student session
        val doc = JSONObject().put("version", 1).put("owner", scope.ownerId).put("stage", scope.stage.name)
            .put("edition", scope.edition).put("lesson", NaturePracticeGrader.SOURCE_LESSON)
        data.draft?.let { d -> doc.put("draft", JSONObject().put("id", d.id).put("position", d.position)
            .put("answers", JSONObject(d.answers)).put("passed", array(d.passed)).put("missed", JSONObject(d.missed))
            .put("hints", array(d.hints)).put("startedAt", d.startedAt)) }
        doc.put("attempts", JSONArray().apply { data.attempts.forEach { a -> put(JSONObject()
            .put("id", a.id).put("answers", JSONObject(a.answers)).put("missed", JSONObject(a.missed))
            .put("hints", array(a.hints)).put("startedAt", a.startedAt).put("finishedAt", a.finishedAt)) } })
        val key = key(scope)
        val committed = runCatching { preferences(key).edit().putString("practice", doc.toString()).commit() }.getOrDefault(false)
        if (!committed) poisoned += key
        changed.value += 1 // Also notify observers to read the fail-closed storage state.
        return committed
    }

    @Synchronized fun read(scope: StudentScope): NatureRead = if (!accepts(scope)) NatureRead.OwnerChanged else
        try { NatureRead.Ready(readStored(scope)) } catch (_: Exception) { NatureRead.StorageError }

    @Synchronized fun start(scope: StudentScope): NatureRead {
        if (!accepts(scope)) return NatureRead.OwnerChanged
        return try {
            val old = readStored(scope)
            if (old.draft != null) NatureRead.Ready(old) else {
                val next = old.copy(draft = NatureDraft(UUID.randomUUID().toString()))
                if (store(scope, next)) NatureRead.Ready(next) else NatureRead.StorageError
            }
        } catch (_: Exception) { NatureRead.StorageError }
    }

    /** Every submit persists locally before UI claims success. Wrong answers remain retryable. */
    @Synchronized fun submit(scope: StudentScope, id: String, response: String): NatureOperation {
        if (!accepts(scope)) return NatureOperation(PlanWrite.OWNER_CHANGED)
        return try {
            val old = readStored(scope)
            val d = old.draft ?: return NatureOperation(PlanWrite.INVALID)
            if (id != allIds[d.position] || response.isBlank() || response.length > 512) return NatureOperation(PlanWrite.INVALID)
            if (id in d.passed) return NatureOperation(PlanWrite.ALREADY_SAVED, correct = true)
            val good = NaturePracticeGrader.correct(id, response)
            val next = d.copy(answers = d.answers + (id to response),
                passed = if (good) d.passed + id else d.passed,
                missed = if (!good) d.missed + (id to ((d.missed[id] ?: 0) + 1).coerceAtMost(1000000)) else d.missed)
            if (store(scope, old.copy(draft = next))) NatureOperation(PlanWrite.SAVED, correct = good)
            else NatureOperation(PlanWrite.STORAGE_ERROR)
        } catch (_: Exception) { NatureOperation(PlanWrite.STORAGE_ERROR) }
    }

    @Synchronized fun hint(scope: StudentScope): PlanWrite {
        if (!accepts(scope)) return PlanWrite.OWNER_CHANGED
        return try {
            val old = readStored(scope); val d = old.draft ?: return PlanWrite.INVALID
            val id = allIds[d.position]
            if (id in d.hints) return PlanWrite.ALREADY_SAVED
            if (store(scope, old.copy(draft = d.copy(hints = d.hints + id)))) PlanWrite.SAVED else PlanWrite.STORAGE_ERROR
        } catch (_: Exception) { PlanWrite.STORAGE_ERROR }
    }

    @Synchronized fun move(scope: StudentScope, nextPosition: Int): PlanWrite {
        if (!accepts(scope)) return PlanWrite.OWNER_CHANGED
        return try {
            val old = readStored(scope); val d = old.draft ?: return PlanWrite.INVALID
            if (nextPosition !in 0..5 || (nextPosition > d.position && allIds[d.position] !in d.passed) ||
                nextPosition > d.position + 1) return PlanWrite.INVALID
            if (nextPosition == d.position) return PlanWrite.ALREADY_SAVED
            if (store(scope, old.copy(draft = d.copy(position = nextPosition)))) PlanWrite.SAVED else PlanWrite.STORAGE_ERROR
        } catch (_: Exception) { PlanWrite.STORAGE_ERROR }
    }

    @Synchronized fun finish(scope: StudentScope, finishedAt: Long = System.currentTimeMillis()): NatureOperation {
        if (!accepts(scope)) return NatureOperation(PlanWrite.OWNER_CHANGED)
        return try {
            val old = readStored(scope); val d = old.draft ?: return NatureOperation(PlanWrite.INVALID)
            if (allIds.any { it !in d.passed } || finishedAt < d.startedAt) return NatureOperation(PlanWrite.INVALID)
            val previous = old.attempts.firstOrNull { it.id == d.id }
            if (previous != null) return NatureOperation(PlanWrite.ALREADY_SAVED, resultId = previous.id)
            val record = NatureAttempt(d.id, d.answers, d.missed, d.hints, d.startedAt, finishedAt)
            if (store(scope, old.copy(draft = null, attempts = old.attempts + record)))
                NatureOperation(PlanWrite.SAVED, resultId = record.id)
            else NatureOperation(PlanWrite.STORAGE_ERROR)
        } catch (_: Exception) { NatureOperation(PlanWrite.STORAGE_ERROR) }
    }
}
