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
import com.ahmadmol.enmath.core.data.question
import com.ahmadmol.enmath.core.model.*
import java.util.UUID

class AppViewModel(val repository: LearningRepository, context: Context? = null) : ViewModel() {
    val gems = context?.let { LocalGemsCoordinator.create(it, viewModelScope) }
    init { gems?.bind(repository.state.value) }
    val state = repository.state
    private val demoSwitchAllowed = context?.applicationInfo?.flags?.let {
        it and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    } == true
    fun canSwitchDemoRole() = DemoRolePolicy.target(state.value.user, demoSwitchAllowed) != null
    fun switchDemoRole(): AccountType? {
        val current=state.value.user ?: return null
        val next=DemoRolePolicy.target(current, demoSwitchAllowed) ?: return null
        signIn(current.copy(type=next))
        return next
    }

    fun signIn(user: User) { repository.signIn(user); gems?.bind(state.value) }

    fun logout() { repository.logout(); gems?.bind(state.value) }

    fun complete(id: String, startedBy: String? = null) = completeRecorded(id, null, startedBy)

    fun completeInteractive(id: String, answers: Map<String, String>, startedBy: String?) = completeRecorded(id, answers.toMap(), startedBy)

    fun verifyLesson(id: String, answer: String, startedBy: String?) {
        if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.verifyLesson(state.value, id, answer)
    }

    private fun completeRecorded(id: String, interactive: Map<String, String>?, startedBy: String?) {
        val fresh = id !in state.value.completed
        repository.completeLesson(id)
        if (fresh)
            repository.changeProduct {
                it.copy(
                    activity =
                        it.activity +
                            ActivityItem(
                                UUID.randomUUID().toString(),
                                Copy("Lesson completed", "درس مكتمل"),
                                System.currentTimeMillis(),
                                10,
                            )
                )
            }
        if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.completeLesson(state.value, id, interactive)
    }

    fun openLesson(id: String) = repository.openLesson(id)

    fun theme(mode: ThemeMode) = repository.setTheme(mode)

    fun language(value: Language) = repository.setLanguage(value)

    fun product(change: (ProductState) -> ProductState) = repository.changeProduct(change)

    fun bookmark(id: String, problem: Boolean = false) = product { p ->
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

    fun recordPractice(setId: String, answers: Map<String, String>, seconds: Int, startedBy: String? = null) {
        val set = ProductSeed.sets.find { it.id == setId } ?: return
        val correct =
            set.questionIds.count { id ->
                ProductSeed.question(id)?.let {
                    LearningRules.correct(it, answers[id].orEmpty())
                } == true
            }
        product {
            it.copy(
                attempts =
                    it.attempts +
                        Attempt(
                            setId,
                            answers,
                            correct,
                            set.questionIds.size,
                            seconds,
                            System.currentTimeMillis(),
                        ),
                activity =
                    it.activity +
                        ActivityItem(
                            UUID.randomUUID().toString(),
                            Copy("Practice completed", "تدريب مكتمل"),
                            System.currentTimeMillis(),
                            (seconds / 60).coerceAtLeast(1),
                        ),
            )
        }
        if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.practice(state.value, setId, answers)
    }

    fun submitAssignment(id: String, answers: Map<String, String>, seconds: Int, startedBy: String? = null) {
        val a = state.value.product.assignments.find { it.id == id } ?: return
        if (
            state.value.user?.type == AccountType.Guest ||
                state.value.product.submissions.count {
                    it.assignmentId == id && it.studentId == "ahmed"
                } >= a.attempts ||
                (!a.late && a.due < System.currentTimeMillis())
        )
            return
        val correct =
            a.questionIds.count { q ->
                state.value.product.question(q)?.let {
                    LearningRules.correct(it, answers[q].orEmpty())
                } == true
            }
        val score = if (a.questionIds.isEmpty()) 0 else correct * a.points / a.questionIds.size
        product {
            it.copy(
                submissions =
                    it.submissions +
                        Submission(
                            id,
                            "ahmed",
                            answers,
                            score,
                            seconds,
                            System.currentTimeMillis(),
                        ),
                notifications =
                    it.notifications +
                        AppNotification(
                            UUID.randomUUID().toString(),
                            Copy("Assignment submitted", "تم تسليم الواجب"),
                            a.title,
                            "assignments/$id/result",
                            false,
                            System.currentTimeMillis(),
                        ),
            )
        }
        if (LocalGemsPolicy.attributable(state.value.user, startedBy)) gems?.assignment(state.value, id, answers)
    }

    fun history(expression: String, topic: String): String {
        val id = UUID.randomUUID().toString()
        product {
            it.copy(
                history =
                    listOf(HistoryItem(id, expression, topic, System.currentTimeMillis())) +
                        it.history.take(99)
            )
        }
        return id
    }

    fun search(query: String) {
        if (query.isNotBlank())
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
