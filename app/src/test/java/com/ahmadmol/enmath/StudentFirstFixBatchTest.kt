package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.AssignmentSubmissionResult
import com.ahmadmol.enmath.core.data.LearningRepository
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.localgems.LocalLearningGems
import com.ahmadmol.enmath.core.localgems.GemAccount
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.*
import org.junit.Test

class TestLearningRepository(initialState: LearningState = LearningState(user = User("Ahmed", "ahmed@example.com", AccountType.Student))) : LearningRepository {
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<LearningState> = _state.asStateFlow()

    override fun signIn(user: User) {
        _state.value = _state.value.copy(user = user)
    }

    override fun logout() {
        _state.value = _state.value.copy(user = null)
    }

    override fun finishOnboarding() {
        _state.value = _state.value.copy(onboarded = true)
    }

    override fun completeLesson(id: String) {
        _state.value = _state.value.copy(completed = _state.value.completed + id)
    }

    override fun openLesson(id: String) {
        _state.value = _state.value.copy(lastLessonId = id)
    }

    override fun setTheme(mode: ThemeMode) {
        _state.value = _state.value.copy(theme = mode)
    }

    override fun setLanguage(language: Language) {
        _state.value = _state.value.copy(language = language)
    }

    override fun changeProduct(transform: (ProductState) -> ProductState) {
        _state.value = _state.value.copy(product = transform(_state.value.product))
    }
}

class StudentFirstFixBatchTest {

    @Test
    fun assignmentSubmissionReturnsExplicitResultAndSavesOnlyOnSuccess() {
        val repo = TestLearningRepository()
        val vm = AppViewModel(repo)
        val state = repo.state.value
        val assignment = state.product.assignments.first()

        val initialCount = state.product.submissions.filter { it.assignmentId == assignment.id }.size

        // 1. Successful submission
        val validAnswers = mapOf("limits-q1" to "5")
        val res1 = vm.submitAssignment(assignment.id, validAnswers, 120)
        assertTrue("Expected Submitted result", res1 is AssignmentSubmissionResult.Submitted)

        val updatedSubmissions = repo.state.value.product.submissions.filter { it.assignmentId == assignment.id }
        assertEquals(initialCount + 1, updatedSubmissions.size)

        // 2. Max attempts exhausted
        // Create an assignment with 1 attempt max and submit twice
        val customAssignment = Assignment(
            id = "test-single-attempt",
            classId = "class-12a",
            title = Copy("Test Assignment", "واجب تجريبي"),
            description = Copy("Desc", "وصف"),
            due = System.currentTimeMillis() + 86400000L,
            attempts = 1,
            points = 10,
            questionIds = listOf("limits-q1"),
        )
        repo.changeProduct { it.copy(assignments = it.assignments + customAssignment) }

        val resSub1 = vm.submitAssignment(customAssignment.id, validAnswers, 60)
        assertTrue(resSub1 is AssignmentSubmissionResult.Submitted)

        val resSub2 = vm.submitAssignment(customAssignment.id, validAnswers, 60)
        assertTrue("Expected AttemptsExhausted", resSub2 is AssignmentSubmissionResult.AttemptsExhausted)

        // 3. Past deadline
        val expiredAssignment = Assignment(
            id = "test-expired",
            classId = "class-12a",
            title = Copy("Expired", "منتهي"),
            description = Copy("Desc", "وصف"),
            due = System.currentTimeMillis() - 86400000L,
            late = false,
            attempts = 3,
            points = 10,
            questionIds = listOf("limits-q1"),
        )
        repo.changeProduct { it.copy(assignments = it.assignments + expiredAssignment) }

        val resExpired = vm.submitAssignment(expiredAssignment.id, validAnswers, 60)
        assertTrue("Expected DeadlinePassed", resExpired is AssignmentSubmissionResult.DeadlinePassed)

        // 4. Guest user submission
        vm.signIn(User("Guest", "", AccountType.Guest))
        val resGuest = vm.submitAssignment(assignment.id, validAnswers, 60)
        assertTrue("Expected GuestUser", resGuest is AssignmentSubmissionResult.GuestUser)
    }

    @Test
    fun routesSelectedTabMapsDeepDestinationsToParentTabs() {
        assertEquals(Routes.Study, Routes.selectedTab("study/catalog"))
        assertEquals(Routes.Profile, Routes.selectedTab("gems/wallet"))
        assertEquals(Routes.Profile, Routes.selectedTab("gems/review"))
        assertEquals("explore", Routes.selectedTab("explore"))
    }

    @Test
    fun weeklyGemsRequiresFiveCompletedVerifiedLessons() {
        val state = LearningState()
        val owner = "ahmed@example.com"
        val account = GemAccount(owner, System.currentTimeMillis())

        // With 0 verified completed lessons, weekly review is null
        val reviewNull = LocalLearningGems.weekly(account, state, System.currentTimeMillis())
        assertNull(reviewNull)

        // Select 5 distinct lessons with valid checkpoints
        val fiveLessons = state.lessons
            .filter { it.checkpoint.isNotBlank() && it.checkpointAnswer.isNotBlank() }
            .distinctBy { it.checkpoint.trim() to it.checkpointAnswer.trim() }
            .take(5)
            .map { it.id }
            .toSet()
        assertEquals(5, fiveLessons.size)

        val updatedAccount = account.copy(lessons = fiveLessons)
        val updatedState = state.copy(completed = fiveLessons)

        val reviewReady = LocalLearningGems.weekly(updatedAccount, updatedState, System.currentTimeMillis())
        assertNotNull(reviewReady)
        assertEquals(5, reviewReady!!.questions.size)
    }
}
