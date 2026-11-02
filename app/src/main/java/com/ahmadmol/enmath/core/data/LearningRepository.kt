package com.ahmadmol.enmath.core.data

import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.flow.StateFlow

data class StudentLearningRecord(val completed: Set<String>, val product: ProductState)

data class LearningState(
    val user: User? = null,
    val generation: Long = 0,
    val catalogAvailable: Boolean = true,
    val reviewRequired: Boolean = false,
    val localGuestId: String? = null,
    val localProfiles: List<LocalLearner> = emptyList(),
    val acknowledgedGuestStages: Set<StudyStage> = emptySet(),
    val legacyCandidates: List<LegacyCandidate> = emptyList(),
    val storageIssue: String? = null,
    val studentRecordId: String? = null,
    val studentRecordOwners: Map<String, String> = emptyMap(),
    val studentRecords: Map<String, StudentLearningRecord> = emptyMap(),
    val completed: Set<String> = DemoContent.initiallyCompleted,
    val lastLessonId: String = "limits-practice",
    val theme: ThemeMode = ThemeMode.System,
    val language: Language = Language.Arabic,
    val onboarded: Boolean = false,
    val product: ProductState = ProductSeed.seed(),
) {
    val books
        get() = if (!catalogAvailable) emptyList() else StudentCatalog.catalog(user?.studyStage ?: StudyStage.BAC).books

    val topics
        get() = books.flatMap { it.topics }

    val lessons
        get() = books.flatMap { it.lessons }

    val students
        get() = ProductSeed.students

    val questions
        get() = if (!catalogAvailable) emptyList() else StudentCatalog.catalog(user?.studyStage ?: StudyStage.BAC).questions + (if (user?.localId == null) product.questions else emptyList())

    val practiceSets
        get() = if (!catalogAvailable) emptyList() else StudentCatalog.catalog(user?.studyStage ?: StudyStage.BAC).sets

    val examples
        get() = if (!catalogAvailable || user?.studyStage == StudyStage.G9) emptyList() else DemoContent.examples

    val dailyGoal
        get() = DemoContent.dailyGoal

    fun lesson(id: String) = lessons.find { it.id == id }

    fun topic(id: String) = topics.find { it.id == id }

    fun topicOf(id: String) = topics.find { it.lessons.any { l -> l.id == id } }

    val progress
        get() =
            LearningRules.stats(this).let { stats ->
                ProgressSnapshot(
                    completed.size,
                    lessons.size,
                    stats.exercises,
                    stats.week,
                    stats.streak,
                    DemoContent.dailyGoal,
                    product.activity.count {
                        it.timestamp / 86_400_000 == System.currentTimeMillis() / 86_400_000
                    },
                )
            }
}

interface LearningRepository {
    val state: StateFlow<LearningState>

    fun signIn(user: User)

    fun logout()

    fun finishOnboarding()

    fun completeLesson(id: String)

    fun openLesson(id: String)

    fun setTheme(mode: ThemeMode)

    fun setLanguage(language: Language)

    fun changeProduct(transform: (ProductState) -> ProductState)

    fun recordLearning(owner: String?, command: String, lesson: String? = null,
        transform: (ProductState) -> ProductState): Boolean {
        if (!LearningRules.acceptsCommand(state.value, owner) || command.isBlank()) return false
        if (command in state.value.product.learningCommands) return true
        if (lesson != null) completeLesson(lesson)
        changeProduct { transform(it).copy(learningCommands = it.learningCommands + command) }
        return true
    }

    fun openScoped(scope: StudentScope, ref: ContentRef): Boolean {
        if (!StudentCatalog.accepts(state.value, scope) || !StudentCatalog.accepts(state.value, ref)) return false
        openLesson(ref.lessonId); return true
    }
    fun changeScoped(scope: StudentScope, transform: (ProductState) -> ProductState): Boolean {
        if (!StudentCatalog.accepts(state.value, scope)) return false
        changeProduct(transform); return true
    }
    fun recordCommand(scope: StudentScope, ref: ContentRef?, command: String, payload: String,
        transform: (ProductState) -> ProductState): Boolean {
        if (!StudentCatalog.accepts(state.value, scope) || (ref != null && !StudentCatalog.accepts(state.value, ref))) return false
        return recordLearning(LearningRules.sessionOwner(state.value), command, ref?.lessonId, transform)
    }

    fun acknowledgeGem(owner: String, event: String) {
        if (LearningRules.commandOwner(state.value) == owner)
            changeProduct { it.copy(pendingGemEvents = it.pendingGemEvents.filterNot { e -> e.id == event }) }
    }
}
