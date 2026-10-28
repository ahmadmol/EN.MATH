package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import com.ahmadmol.enmath.core.model.StudyStage
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

enum class PlanPeriod { DAY, WEEK, MONTH }
enum class PlanMode { ACHIEVEMENT, TIME, HYBRID }
data class PlanTask(val id: String, val title: String, val subject: String, val period: PlanPeriod,
    val start: Long, val mode: PlanMode, val target: Int, val minutes: Int = 0,
    val done: Int = 0, val spent: Int = 0, val lesson: ContentRef? = null,
    val source: String = "MANUAL", val parent: String? = null, val closed: Boolean = false,
    val linkedBaseline: Boolean = false)
data class PlanRevision(val at: Long, val action: String, val tasks: List<PlanTask>)
data class StudyPlan(val revision: Int = 0, val tasks: List<PlanTask> = emptyList(),
    val history: List<PlanRevision> = emptyList(), val receipts: Map<String, String> = emptyMap())
sealed interface PlanRead {
    data class Ready(val plan: StudyPlan) : PlanRead
    data object OwnerChanged : PlanRead
    data object StorageError : PlanRead
}
enum class PlanWrite { SAVED, ALREADY_SAVED, OWNER_CHANGED, CONFLICT, INVALID, STORAGE_ERROR }

object StudyPeriods {
    const val ZONE = "Asia/Damascus"
    private fun calendar(at: Long) = GregorianCalendar(TimeZone.getTimeZone(ZONE), Locale.ROOT).apply { timeInMillis = at }
    fun start(at: Long, period: PlanPeriod): Long = calendar(at).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        when (period) { PlanPeriod.WEEK -> add(Calendar.DAY_OF_MONTH, -((get(Calendar.DAY_OF_WEEK) + 5) % 7))
            PlanPeriod.MONTH -> set(Calendar.DAY_OF_MONTH, 1); else -> Unit }
    }.timeInMillis
    fun next(start: Long, period: PlanPeriod) = calendar(start).apply {
        add(when (period) { PlanPeriod.DAY -> Calendar.DAY_OF_MONTH; PlanPeriod.WEEK -> Calendar.WEEK_OF_YEAR
            PlanPeriod.MONTH -> Calendar.MONTH }, 1)
    }.timeInMillis
}

/** Own additive document per UUID/stage. Never writes learner, teacher, or Gems stores. */
class PlannerRepository internal constructor(private val preferences: (String) -> SharedPreferences,
    private val current: () -> LearningState) {
    private val _changes = kotlinx.coroutines.flow.MutableStateFlow(0L)
    val changes: kotlinx.coroutines.flow.StateFlow<Long> = _changes
    private val poisoned = mutableSetOf<String>()
    private fun key(s: StudentScope): String {
        UUID.fromString(s.ownerId)
        return "enmath_plan_v1_${s.ownerId}_${s.stage.name}"
    }
    @Synchronized fun read(scope: StudentScope): PlanRead {
        if (!StudentCatalog.accepts(current(), scope)) return PlanRead.OwnerChanged
        return try { PlanRead.Ready(load(scope)) } catch (_: Exception) { PlanRead.StorageError }
    }
    private fun load(scope: StudentScope): StudyPlan {
        val key = key(scope); check(key !in poisoned)
        val raw = preferences(key).getString("plan", null) ?: return StudyPlan()
        val o = JSONObject(raw)
        require(o.getInt("version") == 1 && o.getString("owner") == scope.ownerId && o.getString("stage") == scope.stage.name)
        require(o.getString("timezone") == StudyPeriods.ZONE)
        fun tasks(a: JSONArray) = (0 until a.length()).map { decode(a.getJSONObject(it), scope) }.also { list ->
            require(list.map { it.id }.distinct().size == list.size) }
        val history = o.getJSONArray("history")
        val receipts = o.getJSONObject("receipts")
        return StudyPlan(o.getInt("revision"), tasks(o.getJSONArray("tasks")), (0 until history.length()).map {
            val r = history.getJSONObject(it); PlanRevision(r.getLong("at"), r.getString("action"), tasks(r.getJSONArray("tasks")))
        }, receipts.keys().asSequence().associateWith { receipts.getString(it) })
    }
    private fun decode(o: JSONObject, scope: StudentScope): PlanTask {
        val ref = o.optJSONObject("lesson")?.let {
            ContentRef(StudyStage.valueOf(it.getString("stage")), it.getString("edition"), it.getString("id"), it.optInt("revision", 1), it.optString("source", "enmath-demo"),
                ContentKind.valueOf(it.optString("kind", "LESSON")), it.optString("sourceSha256").takeIf(String::isNotEmpty))
        }
        require(ref == null || ref.stage == scope.stage)
        return PlanTask(o.getString("id"), o.getString("title"), o.getString("subject"),
            PlanPeriod.valueOf(o.getString("period")), o.getLong("start"), PlanMode.valueOf(o.getString("mode")),
            o.getInt("target"), o.getInt("minutes"), o.getInt("done"), o.getInt("spent"), ref,
            o.getString("source"), o.optString("parent").takeIf { it.isNotEmpty() }, o.getBoolean("closed"), o.optBoolean("linkedBaseline", false)).also {
            require(valid(it, false))
        }
    }
    private fun json(t: PlanTask) = JSONObject().put("id", t.id).put("title", t.title).put("subject", t.subject)
        .put("period", t.period.name).put("start", t.start).put("mode", t.mode.name).put("target", t.target)
        .put("minutes", t.minutes).put("done", t.done).put("spent", t.spent).put("source", t.source)
        .put("parent", t.parent.orEmpty()).put("closed", t.closed).put("linkedBaseline", t.linkedBaseline).apply { t.lesson?.let {
            val link = JSONObject().put("stage", it.stage.name).put("edition", it.edition).put("id", it.lessonId).put("revision", it.revision).put("source", it.sourceId)
            // Keep the exact legacy payload representation so old duplicate-command receipts still match.
            if (it.kind != ContentKind.LESSON || it.sourceSha256 != null)
                link.put("kind", it.kind.name).put("sourceSha256", it.sourceSha256.orEmpty())
            put("lesson", link) } }
    private fun array(tasks: List<PlanTask>) = JSONArray().apply { tasks.forEach { put(json(it)) } }
    private fun valid(t: PlanTask, checkRef: Boolean = true) = t.id.isNotBlank() && t.title.isNotBlank() &&
        t.title.length <= 200 && t.subject.isNotBlank() && t.subject.length <= 100 &&
        t.target in 0..100000 && t.minutes in 0..100000 && t.done in 0..100000 && t.spent in 0..100000 &&
        (t.mode != PlanMode.TIME || t.minutes > 0) && (t.mode == PlanMode.TIME || t.target > 0) &&
        t.start == StudyPeriods.start(t.start, t.period) && t.source in setOf("MANUAL", "LEARNING_EVENT") &&
        (!checkRef || t.lesson == null || StudentCatalog.acceptsReference(current(), t.lesson))

    @Synchronized fun pinSource(scope: StudentScope, ref: ContentRef): PlanWrite {
        if (!StudentCatalog.accepts(current(), scope)) return PlanWrite.OWNER_CHANGED
        if (ref.kind != ContentKind.SOURCE_INDEX || !StudentCatalog.acceptsReference(current(), ref)) return PlanWrite.INVALID
        val plan = (read(scope) as? PlanRead.Ready)?.plan ?: return PlanWrite.STORAGE_ERROR
        val start = StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK)
        val id = UUID.nameUUIDFromBytes("${scope.ownerId}|${StudentCatalog.referenceKey(ref)}|$start".toByteArray(Charsets.UTF_8)).toString()
        plan.tasks.find { it.id == id }?.let { return if (it.lesson == ref) PlanWrite.ALREADY_SAVED else PlanWrite.CONFLICT }
        return save(scope, plan.revision, "source-index:$id", PlanTask(id, requireNotNull(StudentCatalog.sourceTitle(ref)),
            "Mathematics / الرياضيات", PlanPeriod.WEEK, start, PlanMode.ACHIEVEMENT, 1, lesson = ref))
    }

    private fun write(scope: StudentScope, expected: Int, command: String, payload: String, action: String,
        transform: (StudyPlan) -> List<PlanTask>): PlanWrite {
        if (!StudentCatalog.accepts(current(), scope)) return PlanWrite.OWNER_CHANGED
        if (command.isBlank()) return PlanWrite.INVALID
        val old = try { load(scope) } catch (_: Exception) { return PlanWrite.STORAGE_ERROR }
        return try {
            old.receipts[command]?.let { return if (it == payload) PlanWrite.ALREADY_SAVED else PlanWrite.CONFLICT }
            if (old.revision != expected) return PlanWrite.CONFLICT
            val tasks = transform(old)
            if (tasks.any { !valid(it, false) } || tasks.map { it.id }.distinct().size != tasks.size) return PlanWrite.INVALID
            val history = old.history + PlanRevision(System.currentTimeMillis(), action, old.tasks.map { measured(it, current()) })
            val o = JSONObject().put("version", 1).put("owner", scope.ownerId).put("stage", scope.stage.name)
                .put("timezone", StudyPeriods.ZONE).put("revision", old.revision + 1).put("tasks", array(tasks))
                .put("receipts", JSONObject(old.receipts + (command to payload))).put("history", JSONArray().apply {
                    history.forEach { put(JSONObject().put("at", it.at).put("action", it.action).put("tasks", array(it.tasks))) }
                })
            if (!StudentCatalog.accepts(current(), scope)) return PlanWrite.OWNER_CHANGED
            val k = key(scope)
            if (!preferences(k).edit().putString("plan", o.toString()).commit()) {
                poisoned += k; PlanWrite.STORAGE_ERROR
            } else { _changes.value += 1; PlanWrite.SAVED }
        } catch (_: IllegalArgumentException) { PlanWrite.INVALID }
        catch (_: Exception) { PlanWrite.STORAGE_ERROR }
    }
    @Synchronized fun save(scope: StudentScope, revision: Int, command: String, task: PlanTask): PlanWrite {
        if (!valid(task) || task.source != "MANUAL") return PlanWrite.INVALID
        return write(scope, revision, command, "save:${json(task)}", "SAVE:${task.id}") { old ->
            val existing = old.tasks.find { it.id == task.id }
            require(existing == null || (task.done == existing.done && task.spent == existing.spent &&
                task.parent == existing.parent && task.closed == existing.closed))
            require(existing != null || (task.done == 0 && task.spent == 0 && task.parent == null && !task.closed))
            val baseline = if (existing != null && existing.lesson == task.lesson) existing.linkedBaseline
                else task.lesson?.lessonId in StudentCatalog.completed(current())
            val stored = task.copy(linkedBaseline = baseline)
            if (existing == null) old.tasks + stored else old.tasks.map { if (it.id == task.id) stored else it }
        }
    }
    @Synchronized fun remove(scope: StudentScope, revision: Int, command: String, id: String) =
        write(scope, revision, command, "delete:$id", "DELETE:$id") { old ->
            require(old.tasks.any { it.id == id }); old.tasks.filterNot { it.id == id } }
    @Synchronized fun progress(scope: StudentScope, revision: Int, command: String, id: String, amount: Int, minutes: Int): PlanWrite {
        if (amount < 0 || minutes < 0 || (amount == 0 && minutes == 0)) return PlanWrite.INVALID
        return write(scope, revision, command, "manual:$id:$amount:$minutes", "MANUAL:$id") { old -> old.tasks.map {
            if (it.id != id) it else { require(!it.closed && (it.mode != PlanMode.TIME || amount == 0) &&
                (it.mode != PlanMode.ACHIEVEMENT || minutes == 0)); it.copy(done = Math.addExact(it.done, amount),
                spent = Math.addExact(it.spent, minutes), source = "MANUAL") }
        }.also { require(old.tasks.any { it.id == id }) } }
    }
    @Synchronized fun carry(scope: StudentScope, revision: Int, command: String, id: String, destination: Long, consent: Boolean): PlanWrite {
        if (!consent) return PlanWrite.INVALID
        return write(scope, revision, command, "carry:$id:$destination", "CARRY:$id") { old ->
            val t = measured(old.tasks.first { it.id == id }, current()); require(!t.closed && destination > t.start)
            val target = (t.target - t.done).coerceAtLeast(0)
            val minutes = (t.minutes - t.spent).coerceAtLeast(0)
            require((t.mode != PlanMode.TIME && target > 0) || (t.mode != PlanMode.ACHIEVEMENT && minutes > 0))
            // A finished dimension of a hybrid task carries a zero remainder; keep the other dimension.
            val mode = if (target == 0) PlanMode.TIME else if (minutes == 0) PlanMode.ACHIEVEMENT else t.mode
            old.tasks.map { if (it.id == id) it.copy(closed = true) else it } + t.copy(id = UUID.randomUUID().toString(),
                start = destination, target = target, minutes = minutes, done = 0, spent = 0, mode = mode, parent = id, source = "MANUAL", linkedBaseline = t.lesson?.lessonId in StudentCatalog.completed(current()))
        }
    }
    /** Projection only. A recorded lesson can satisfy a linked one-lesson goal, without granting anything. */
    fun measured(task: PlanTask, state: LearningState): PlanTask = if (!task.linkedBaseline && task.lesson?.let {
        StudentCatalog.accepts(state, it) && it.lessonId in StudentCatalog.completed(state) } == true)
        task.copy(done = maxOf(task.done, 1), source = if (task.done == 0) "LEARNING_EVENT" else task.source) else task
}
