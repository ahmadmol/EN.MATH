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

    fun logout(): Boolean = safely { repository.logout(); gems?.bind(state.value); true }

    fun complete(id: String, startedBy: String? = LearningRules.commandOwner(state.value)) = completeRecorded(id, null, startedBy)

    fun completeInteractive(id: String, answers: Map<String, String>, startedBy: String?) = completeRecorded(id, answers.toMap(), startedBy)

    fun verifyLesson(id: String, answer: String, startedBy: String?) {
        if (!LearningRules.acceptsCommand(state.value, startedBy) ||
            !com.ahmadmol.enmath.core.localgems.LocalLearningGems.lessonVerified(state.value, id, answer)) return
        if (state.value.user?.localId != null) {
            val saved = safely { repository.recordLearning(startedBy, "verify:$id") { p ->
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
        if (state.value.user?.localId != null && !fresh && "lesson:$id" !in state.value.product.learningCommands) {
            replayPendingGems()
            return true // imported completion is not new reward provenance
        }
        val evidence = interactive ?: state.value.product.verifiedLessons[id]?.let { mapOf("checkpoint" to it) }.orEmpty()
        val saved = safely { repository.recordLearning(startedBy, "lesson:$id", id) { p ->
            if (!fresh) p else pending(p.copy(activity = p.activity + ActivityItem(
                UUID.randomUUID().toString(), Copy("Lesson completed", "درس مكتمل"), System.currentTimeMillis(), 10)),
                "lesson:$id", "LESSON", id, evidence)
        } }
        if (saved) {
            if (state.value.user?.localId != null) replayPendingGems()
            else if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.completeLesson(state.value, id, interactive)
        }
        return saved
    }

    fun openLesson(id: String, startedBy: String? = LearningRules.commandOwner(state.value)): Boolean {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return false
        if (state.value.lesson(id) == null) return false
        return safely { repository.openLesson(id); true }
    }

    fun theme(mode: ThemeMode) = repository.setTheme(mode)

    fun language(value: Language) = repository.setLanguage(value)

    fun product(startedBy: String? = LearningRules.commandOwner(state.value), change: (ProductState) -> ProductState): Boolean {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return false
        return safely { repository.changeProduct(change); true }
    }

    fun bookmark(id: String, problem: Boolean = false, startedBy: String? = LearningRules.commandOwner(state.value)) = product(startedBy) { p ->
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
        startedBy: String? = LearningRules.commandOwner(state.value), commandId: String = UUID.randomUUID().toString()): Boolean {
        if (!LearningRules.acceptsCommand(state.value, startedBy)) return false
        if (commandId.isBlank()) return false
        val set = state.value.practiceSets.find { it.id == setId } ?: return false
        val captured = answers.toMap()
        val correct = set.questionIds.count { id -> ProductSeed.question(id)?.let { LearningRules.correct(it, captured[id].orEmpty()) } == true }
        val saved = safely { repository.recordLearning(startedBy, commandId) { p ->
            pending(p.copy(attempts = p.attempts + Attempt(setId, captured, correct, set.questionIds.size, seconds, System.currentTimeMillis()),
                activity = p.activity + ActivityItem(UUID.randomUUID().toString(), Copy("Practice completed", "تدريب مكتمل"),
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
        startedBy: String? = LearningRules.commandOwner(state.value),
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
        val saved = safely { repository.recordLearning(startedBy, commandId) { p ->
            pending(p.copy(
                submissions = p.submissions + submission,
                notifications =
                    p.notifications +
                        AppNotification(
                            UUID.randomUUID().toString(),
                            Copy("Assignment submitted", "تم تسليم الواجب"),
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

    fun history(expression: String, topic: String, startedBy: String? = LearningRules.commandOwner(state.value)): String {
        if (state.value.user?.type != AccountType.Teacher && !LearningRules.acceptsCommand(state.value, startedBy)) return ""
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

    fun search(query: String) {
        if (LearningRules.search(query).isNotBlank())
            product { it.copy(searches = (listOf(query.trim()) + it.searches).distinct().take(8)) }
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
                            Copy("New assignment", "واجب جديد"),
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
                            Copy("Teacher feedback", "ملاحظات المعلّم"),
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
