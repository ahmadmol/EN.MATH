package com.ahmadmol.enmath.core.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SourceWorkspace(val visitedId: String? = null, val savedIds: Set<String> = emptySet(),
    val lastVisitedByBook: Map<String, String> = emptyMap())
sealed interface SourceWorkspaceRead {
    data class Ready(val workspace: SourceWorkspace) : SourceWorkspaceRead
    data object OwnerChanged : SourceWorkspaceRead
    data object StorageError : SourceWorkspaceRead
}

/** Additive browsing/bookmark store; never touches learner, planner, teacher or wallet documents. */
class SourceWorkspaceRepository internal constructor(private val preferences: (String) -> SharedPreferences,
    private val current: () -> LearningState) {
    private val changed = MutableStateFlow(0L)
    val changes: StateFlow<Long> = changed
    private val poisoned = mutableSetOf<String>()
    private fun key(scope: StudentScope): String {
        UUID.fromString(scope.ownerId)
        return "enmath_source_workspace_v1_${scope.ownerId}_${scope.stage.name}"
    }
    private fun load(scope: StudentScope): SourceWorkspace {
        val key = key(scope); check(key !in poisoned)
        val raw = preferences(key).getString("workspace", null) ?: return SourceWorkspace()
        val doc = JSONObject(raw)
        require(doc.getInt("version") == 1 && doc.getString("owner") == scope.ownerId &&
            doc.getString("stage") == scope.stage.name && doc.getString("edition") == scope.edition)
        val catalog = SourceCurricula.forStage(scope.stage)
        val entries = doc.getJSONArray("saved")
        fun decode(entry: JSONObject): String {
            val id = entry.getString("id")
            require(entry.getString("ref") == catalog.research(id)?.ref?.let(StudentCatalog::referenceKey))
            return id
        }
        val byBook = doc.optJSONObject("visitedByBook")
        val visited = byBook?.keys()?.asSequence()?.associateWith { book ->
            val id = decode(byBook.getJSONObject(book))
            require(catalog.research(id)?.let { catalog.unit(it.unitId)?.bookId } == book)
            id
        }.orEmpty()
        return SourceWorkspace(doc.optJSONObject("visited")?.let(::decode),
            (0 until entries.length()).map { decode(entries.getJSONObject(it)) }.toSet(), visited)
    }
    @Synchronized fun read(scope: StudentScope): SourceWorkspaceRead {
        if (!StudentCatalog.accepts(current(), scope)) return SourceWorkspaceRead.OwnerChanged
        return try { SourceWorkspaceRead.Ready(load(scope)) } catch (_: Exception) { SourceWorkspaceRead.StorageError }
    }
    @Synchronized fun visit(scope: StudentScope, ref: ContentRef) = update(scope, ref, null)
    @Synchronized fun bookmark(scope: StudentScope, ref: ContentRef, saved: Boolean) = update(scope, ref, saved)
    private fun update(scope: StudentScope, ref: ContentRef, saved: Boolean?): PlanWrite {
        if (!StudentCatalog.accepts(current(), scope)) return PlanWrite.OWNER_CHANGED
        if (ref.kind != ContentKind.SOURCE_INDEX || !StudentCatalog.acceptsReference(current(), ref)) return PlanWrite.INVALID
        return try {
            val old = load(scope)
            val catalog = SourceCurricula.forStage(scope.stage)
            val bookId = requireNotNull(catalog.unit(requireNotNull(catalog.research(ref.lessonId)).unitId)).bookId
            val next = if (saved == null) old.copy(visitedId = ref.lessonId, lastVisitedByBook = old.lastVisitedByBook + (bookId to ref.lessonId))
                else old.copy(savedIds = if (saved) old.savedIds + ref.lessonId else old.savedIds - ref.lessonId)
            if (old == next) return PlanWrite.ALREADY_SAVED
            fun encode(id: String) = JSONObject().put("id", id).put("ref",
                StudentCatalog.referenceKey(requireNotNull(catalog.research(id)).ref))
            val doc = JSONObject().put("version", 1).put("owner", scope.ownerId).put("stage", scope.stage.name)
                .put("edition", scope.edition).put("saved", JSONArray().apply { next.savedIds.forEach { put(encode(it)) } })
            next.visitedId?.let { doc.put("visited", encode(it)) }
            doc.put("visitedByBook", JSONObject().apply { next.lastVisitedByBook.forEach { (book, id) -> put(book, encode(id)) } })
            if (!StudentCatalog.accepts(current(), scope)) return PlanWrite.OWNER_CHANGED
            val key = key(scope)
            if (!preferences(key).edit().putString("workspace", doc.toString()).commit()) {
                poisoned += key; PlanWrite.STORAGE_ERROR
            } else { changed.value += 1; PlanWrite.SAVED }
        } catch (_: Exception) { PlanWrite.STORAGE_ERROR }
    }
}
