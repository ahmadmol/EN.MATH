package com.ahmadmol.enmath.core

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.ahmadmol.enmath.core.localgems.LocalGemsCoordinator
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmadmol.enmath.core.data.LearningRepository
import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.data.ProductSeed
import com.ahmadmol.enmath.core.data.activeStudentId
import com.ahmadmol.enmath.core.data.question
import com.ahmadmol.enmath.core.model.*
import java.util.UUID

sealed interface AssignmentSubmissionResult {
    data class Submitted(val submission: Submission) : AssignmentSubmissionResult
    data object DeadlinePassed : AssignmentSubmissionResult
    data object AttemptsExhausted : AssignmentSubmissionResult
    data object GuestUser : AssignmentSubmissionResult
    data object AssignmentNotFound : AssignmentSubmissionResult
    data object OwnerChanged : AssignmentSubmissionResult
    data object CommandConflict : AssignmentSubmissionResult
    data object StorageUnavailable : AssignmentSubmissionResult
}

class AppViewModel(val repository: LearningRepository, context: Context? = null) : ViewModel() {
    val gems = context?.let { LocalGemsCoordinator.create(it, viewModelScope) }
    val state = repository.state
    init { gems?.bind(repository.state.value); replayPendingGems() }
    private val demoSwitchAllowed = context?.applicationInfo?.flags?.let {
        it and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    } == true
    fun canSwitchDemoRole() = DemoRolePolicy.target(state.value.user, demoSwitchAllowed) != null
    fun switchDemoRole(): AccountType? {
        val current=state.value.user ?: return null
        val next=DemoRolePolicy.target(current, demoSwitchAllowed) ?: return null
        if (!signIn(current.copy(type=next))) return null
        return next
    }

    fun signIn(user: User): Boolean = safely { repository.signIn(user); refreshIdentity(); true }

    fun refreshIdentity() { gems?.bind(state.value); replayPendingGems() }

    fun replayPendingGems() {
        state.value.product.pendingGemEvents.forEach { event ->
            gems?.deliver(state.value, event) { repository.acknowledgeGem(event.owner, event.id) }
        }
    }

    private fun safely(action: () -> Boolean): Boolean = try { action() }
        catch (_: com.ahmadmol.enmath.core.data.LocalDataWriteFailed) {
            (repository as? com.ahmadmol.enmath.core.data.LocalDemoRepository)?.reportStorageIssue(); false
        } catch (_: com.ahmadmol.enmath.core.data.CorruptLocalData) {
            (repository as? com.ahmadmol.enmath.core.data.LocalDemoRepository)?.reportStorageIssue(); false
        }

    private fun pending(p: ProductState, command: String, kind: String, content: String, answers: Map<String,String>): ProductState {
        val user = state.value.user
        return if (user?.localId != null && user.type == AccountType.Student)
            p.copy(pendingGemEvents = p.pendingGemEvents + PendingGemEvent(command, user.localId, kind, content, answers.toMap(), System.currentTimeMillis()))
        else p
    }

    private fun record(owner: String?, command: String, lesson: String? = null, payload: String = command,
        transform: (ProductState) -> ProductState): Boolean {
        if (!LearningRules.acceptsCommand(state.value, owner)) return false
        val scope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
        if (scope == null) return repository.recordLearning(owner, command, lesson, transform)
        val ref = lesson?.let { com.ahmadmol.enmath.core.data.StudentCatalog.ref(state.value, it) ?: return false }
        if (command in state.value.product.learningCommands && command !in state.value.product.commandReceipts)
            return repository.recordLearning(owner, command, lesson, transform)
        return repository.recordCommand(scope, ref, command, payload, transform)
    }

    fun logout(): Boolean = safely { repository.logout(); gems?.bind(state.value); true }

    fun complete(id: String, startedBy: String? = LearningRules.sessionOwner(state.value)) = completeRecorded(id, null, startedBy)

    fun completeInteractive(id: String, answers: Map<String, String>, startedBy: String?) = completeRecorded(id, answers.toMap(), startedBy)

    fun verifyLesson(id: String, answer: String, startedBy: String?) {
        if (!LearningRules.acceptsCommand(state.value, startedBy) ||
            !com.ahmadmol.enmath.core.localgems.LocalLearningGems.lessonVerified(state.value, id, answer)) return
        if (state.value.user?.localId != null) {
            val saved = safely { record(startedBy, "verify:$id", payload = "verify:$id:$answer") { p ->
                val next = p.copy(verifiedLessons = p.verifiedLessons + (id to answer))
                if (id in state.value.completed && "lesson:$id" in p.learningCommands)
                    pending(next, "verify:$id", "LESSON", id, mapOf("checkpoint" to answer))
                else next
            } }
            if (saved) replayPendingGems()
        } else gems?.verifyLesson(state.value, id, answer)
    }

    private fun completeRecorded(id: String, interactive: Map<String, String>?, startedBy: String?): Boolean {
        if (!LearningRules.acceptsCommand(state.value, startedBy) || state.value.lesson(id) == null) return false
        val fresh = id !in state.value.completed
        if (state.value.user?.localId != null && !fresh && id !in com.ahmadmol.enmath.core.data.StudentCatalog.completed(state.value)) {
            replayPendingGems()
            // An explicit revisit records current-edition completion without rewarding an imported completion.
            val edition = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)?.edition ?: return false
            return safely { record(startedBy, "revisited:$edition:$id", id) { it } }
        }
        val evidence = interactive ?: state.value.product.verifiedLessons[id]?.let { mapOf("checkpoint" to it) }.orEmpty()
        val saved = safely { record(startedBy, "lesson:$id", id, "lesson:$id:${org.json.JSONObject(java.util.TreeMap(interactive.orEmpty()))}") { p ->
            if (!fresh) p else pending(p.copy(activity = p.activity + ActivityItem(
                UUID.randomUUID().toString(), Copy("Lesson completed", "Ø¯Ø±Ø³ Ù…ÙƒØªÙ…Ù„"), System.currentTimeMillis(), 10)),
                "lesson:$id", "LESSON", id, evidence)
        } }
        if (saved) {
            if (state.value.user?.localId != null) replayPendingGems()
            else if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.completeLesson(state.value, id, interactive)
        }
        return saved
    }

    fun openLesson(id: String, startedBy: String? = LearningRules.sessionOwner(state.value)): Boolean {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return false
        if (state.value.lesson(id) == null) return false
        return safely {
            val scope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
            if (scope == null) { repository.openLesson(id); true }
            else com.ahmadmol.enmath.core.data.StudentCatalog.ref(state.value, id)?.let { repository.openScoped(scope, it) } == true
        }
    }

    fun theme(mode: ThemeMode) = repository.setTheme(mode)

    fun language(value: Language) = repository.setLanguage(value)

    fun product(startedBy: String? = LearningRules.sessionOwner(state.value), change: (ProductState) -> ProductState): Boolean {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return false
        return safely {
            val scope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
            if (scope == null) { repository.changeProduct(change); true } else repository.changeScoped(scope, change)
        }
    }

    fun bookmark(id: String, problem: Boolean = false, startedBy: String? = LearningRules.sessionOwner(state.value)) = product(startedBy) { p ->
        if (problem)
            p.copy(
                savedProblems =
                    if (id in p.savedProblems) p.savedProblems - id else p.savedProblems + id
            )
        else
            p.copy(
                savedLessons =
                    if (id in p.savedLessons) p.savedLessons - id else p.savedLessons + id
            )
    }

    @Synchronized
    fun recordPractice(setId: String, answers: Map<String, String>, seconds: Int,
        startedBy: String? = LearningRules.sessionOwner(state.value), commandId: String = UUID.randomUUID().toString()): Boolean {
        if (!LearningRules.acceptsCommand(state.value, startedBy)) return false
        if (commandId.isBlank()) return false
        val set = state.value.practiceSets.find { it.id == setId } ?: return false
        val captured = answers.toMap()
        if (commandId in state.value.product.learningCommands) {
            val old = state.value.product.attempts.find { it.commandId == commandId }
            val expectedScope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
            val same = old != null && old.setId == setId && old.answers == captured && old.seconds == seconds &&
                (expectedScope == null || (old.edition == expectedScope.edition && old.stage == expectedScope.stage))
            if (same) replayPendingGems()
            return same
        }
        val scope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
        val correct = set.questionIds.count { id -> state.value.questions.find { it.id == id }?.let { LearningRules.correct(it, captured[id].orEmpty()) } == true }
        val saved = safely { record(startedBy, commandId, payload = "PRACTICE:$setId:$seconds:${org.json.JSONObject(java.util.TreeMap(answers))}") { p ->
            pending(p.copy(attempts = p.attempts + Attempt(setId, captured, correct, set.questionIds.size, seconds, System.currentTimeMillis(), scope?.edition, scope?.stage, commandId),
                activity = p.activity + ActivityItem(UUID.randomUUID().toString(), Copy("Practice completed", "ØªØ¯Ø±ÙŠØ¨ Ù…ÙƒØªÙ…Ù„"),
                    System.currentTimeMillis(), (seconds / 60).coerceAtLeast(1))), commandId, "PRACTICE", setId, captured)
        } }
        if (saved) {
            if (state.value.user?.localId != null) replayPendingGems()
            else if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.practice(state.value, setId, captured)
        }
        return saved
    }

    fun submitAssignment(
        id: String,
        answers: Map<String, String>,
        seconds: Int,
        startedBy: String? = LearningRules.sessionOwner(state.value),
        commandId: String = UUID.randomUUID().toString()
    ): AssignmentSubmissionResult {
        val a = state.value.product.assignments.find { it.id == id }
            ?: return AssignmentSubmissionResult.AssignmentNotFound
        if (state.value.user?.type == AccountType.Guest) {
            return AssignmentSubmissionResult.GuestUser
        }
        if (!LearningRules.acceptsCommand(state.value, startedBy)) return AssignmentSubmissionResult.OwnerChanged
        val currentStudentId = state.value.activeStudentId
        if (commandId in state.value.product.learningCommands) {
            val scope = com.ahmadmol.enmath.core.data.StudentCatalog.scope(state.value)
            if (scope != null && !repository.recordCommand(scope, null, commandId,
                    "ASSIGNMENT:$id:$seconds:${org.json.JSONObject(java.util.TreeMap(answers))}") { it })
                return AssignmentSubmissionResult.CommandConflict
            replayPendingGems()
            return state.value.product.submissions.lastOrNull { it.assignmentId == id && it.studentId == currentStudentId }
                ?.let(AssignmentSubmissionResult::Submitted) ?: AssignmentSubmissionResult.OwnerChanged
        }
        val existingSubmissions = state.value.product.submissions.count {
            it.assignmentId == id && it.studentId == currentStudentId
        }
        if (existingSubmissions >= a.attempts) {
            return AssignmentSubmissionResult.AttemptsExhausted
        }
        if (!a.late && a.due < System.currentTimeMillis()) {
            return AssignmentSubmissionResult.DeadlinePassed
        }
        val correct =
            a.questionIds.count { q ->
                state.value.product.question(q)?.let {
                    LearningRules.correct(it, answers[q].orEmpty())
                } == true
            }
        val score = if (a.questionIds.isEmpty()) 0 else correct * a.points / a.questionIds.size
        val submission = Submission(
            id,
            currentStudentId,
            answers,
            score,
            seconds,
            System.currentTimeMillis(),
        )
        val saved = safely { record(startedBy, commandId, payload = "ASSIGNMENT:$id:$seconds:${org.json.JSONObject(java.util.TreeMap(answers))}") { p ->
            pending(p.copy(
                submissions = p.submissions + submission,
                notifications =
                    p.notifications +
                        AppNotification(
                            UUID.randomUUID().toString(),
                            Copy("Assignment submitted", "ØªÙ… ØªØ³Ù„ÙŠÙ… Ø§Ù„ÙˆØ§Ø¬Ø¨"),
                            a.title,
                            "assignments/$id/result",
                            false,
                            System.currentTimeMillis(),
                        ),
            ), commandId, "ASSIGNMENT", id, answers)
        } }
        if (!saved) return AssignmentSubmissionResult.StorageUnavailable
        if (state.value.user?.localId != null) replayPendingGems()
        else if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.assignment(state.value, id, answers)
        return AssignmentSubmissionResult.Submitted(submission)
    }

    fun history(expression: String, topic: String, startedBy: String? = LearningRules.sessionOwner(state.value)): String {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return ""
        if (state.value.user?.studyStage == StudyStage.G9) return ""
        val id = UUID.randomUUID().toString()
        val saved = product(startedBy) {
            it.copy(
                history =
                    listOf(HistoryItem(id, expression, topic, System.currentTimeMillis())) +
                        it.history.take(99)
            )
        }
        return if (saved) id else ""
    }

    fun saveGraph(expression: String, startedBy: String? = LearningRules.sessionOwner(state.value)): Boolean {
        if (!LearningRules.acceptsCommand(state.value, startedBy) || state.value.user?.studyStage == StudyStage.G9 ||
            !com.ahmadmol.enmath.features.explore.graphSupported(expression)) return false
        val captured = expression.trim()
        val existing = state.value.product.history.firstOrNull { it.topicId == "graph" && it.expression == captured }
        val item = existing ?: HistoryItem(UUID.randomUUID().toString(), captured, "graph", System.currentTimeMillis())
        return product(startedBy) { it.copy(history = if (existing == null) listOf(item) + it.history else it.history,
            savedProblems = it.savedProblems + item.id) }
    }

    fun search(query: String) = search(query, LearningRules.sessionOwner(state.value))

    fun search(query: String, startedBy: String?) {
        if (LearningRules.search(query).isNotBlank())
            product(startedBy) { it.copy(searches = (listOf(query.trim()) + it.searches).distinct().take(8)) }
    }

    fun createClass(name: String): String {
        val id = UUID.randomUUID().toString()
        val code = "EN-" + id.take(6).uppercase()
        product {
            it.copy(classes = it.classes + Classroom(id, Copy(name, name), emptyList(), code))
        }
        return id
    }

    fun publish(assignment: Assignment) {
        product {
            it.copy(
                assignments = it.assignments + assignment,
                notifications =
                    it.notifications +
                        AppNotification(
                            UUID.randomUUID().toString(),
                            Copy("New assignment", "ÙˆØ§Ø¬Ø¨ Ø¬Ø¯ÙŠØ¯"),
                            assignment.title,
                            "assignments/${assignment.id}",
                            false,
                            System.currentTimeMillis(),
                        ),
            )
        }
    }

    fun createResource(title: String, body: String, topic: String, kind: ResourceKind) {
        product {
            it.copy(
                resources =
                    it.resources +
                        TeacherResource(
                            UUID.randomUUID().toString(),
                            Copy(title, title),
                            topic,
                            kind,
                            Copy(body, body),
                            emptySet(),
                            System.currentTimeMillis(),
                        )
            )
        }
    }

    fun shareResource(id: String, classId: String) {
        product {
            it.copy(
                resources =
                    it.resources.map { r ->
                        if (r.id == id) r.copy(classIds = r.classIds + classId) else r
                    }
            )
        }
    }

    fun feedback(assignment: String, student: String, body: String) {
        product {
            it.copy(
                submissions =
                    it.submissions.map { s ->
                        if (s.assignmentId == assignment && s.studentId == student)
                            s.copy(feedback = Copy(body, body), reviewed = true)
                        else s
                    },
                notifications =
                    it.notifications +
                        AppNotification(
                            UUID.randomUUID().toString(),
                            Copy("Teacher feedback", "Ù…Ù„Ø§Ø­Ø¸Ø§Øª Ø§Ù„Ù…Ø¹Ù„Ù‘Ù…"),
                            Copy(body, body),
                            "assignments/$assignment/result",
                            false,
                            System.currentTimeMillis(),
                        ),
            )
        }
    }

    companion object {
        fun factory(repository: LearningRepository, context: Context) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AppViewModel(repository, context.applicationContext) as T
            }
    }
}
