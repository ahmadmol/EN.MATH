package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.localgems.*
import org.junit.Assert.*
import org.junit.Test

class WeeklyGemCardPresentationTest {
    private val now = 1_792_627_200_000L
    private val owner = "student@example.test"
    private val initial = LearningState(user = User("Student", owner, AccountType.Student))
    private val lessonIds = initial.lessons
        .filter { it.checkpoint.isNotBlank() && it.checkpointAnswer.isNotBlank() }
        .distinctBy { it.checkpoint.trim() to it.checkpointAnswer.trim() }
        .take(6).map { it.id }.toSet()
    private val state = initial.copy(completed = lessonIds)
    private val account = GemAccount(owner, now - 1, lessons = lessonIds)
    private val week = LocalGemsPolicy.week(now)
    private val review = checkNotNull(LocalLearningGems.weekly(account, state, now))
    private val ready = LocalGemsUiState(owner, account, review = review)
    private fun project(ui: LocalGemsUiState = ready, learning: LearningState? = state) =
        weeklyGemCardPresentation(ui, learning, now)
    private fun completedAccount() = account.copy(rewards = listOf(
        GemRewardEntry("WEEKLY:$week", GemReward.WEEKLY, 15, now,
            LocalGemsPolicy.day(now), week)
    ))
    private fun assertNoRequirements(p: WeeklyGemCardPresentation) {
        assertEquals(0, p.remainingLessons)
        assertFalse(p.detail(true).contains("المتبقي"))
        assertFalse(p.detail(false).contains("remaining"))
    }

    @Test fun completedWithNoReviewShowsCompletionInsteadOfRequirements() {
        val p = project(ready.copy(account = completedAccount(), review = null))
        assertEquals(WeeklyGemCardStatus.Completed, p.status)
        assertTrue(p.detail(true).contains("أنجزت"))
        assertTrue(p.detail(false).contains("complete"))
        assertNoRequirements(p)
    }

    @Test fun completedOverridesStaleReadyBusyAndErrorForTheSameOwner() {
        val p = project(ready.copy(account = completedAccount(), busy = true, error = "STORAGE_UNAVAILABLE"))
        assertEquals(WeeklyGemCardStatus.Completed, p.status)
        assertNoRequirements(p)
    }

    @Test fun priorWeekRewardDoesNotMarkThisWeekCompleted() {
        val previous = now - 7 * 86_400_000L
        val oldWeek = LocalGemsPolicy.week(previous)
        val oldReward = GemRewardEntry("WEEKLY:$oldWeek", GemReward.WEEKLY, 15,
            previous, LocalGemsPolicy.day(previous), oldWeek)
        assertEquals(WeeklyGemCardStatus.Ready,
            project(ready.copy(account = account.copy(rewards = listOf(oldReward)))).status)
    }

    @Test fun missingAccountIsLoading() {
        assertEquals(WeeklyGemCardStatus.Loading, project(ready.copy(account = null)).status)
    }

    @Test fun busyAccountDoesNotExposeReadyAction() {
        val p = project(ready.copy(busy = true))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun storageErrorIsDistinctFromMissingLessonsEvenWithoutAccount() {
        val p = project(ready.copy(account = null, error = "STORAGE_UNAVAILABLE"))
        assertEquals(WeeklyGemCardStatus.Error, p.status)
        assertTrue(p.detail(true).contains("تعذر"))
        assertNoRequirements(p)
    }

    @Test fun zeroVerifiedLessonsRequiresFive() {
        val p = project(ready.copy(account = account.copy(lessons = emptySet()), review = null))
        assertEquals(WeeklyGemCardStatus.NotEligible, p.status)
        assertEquals(5, p.remainingLessons)
        assertTrue(p.detail(false).contains("5 remaining"))
    }

    @Test fun fourQualifyingLessonsRequiresExactlyOne() {
        val p = project(ready.copy(account = account.copy(lessons = lessonIds.take(4).toSet()), review = null))
        assertEquals(WeeklyGemCardStatus.NotEligible, p.status)
        assertEquals(1, p.remainingLessons)
    }

    @Test fun fiveQualifyingLessonsWithoutReviewShowsLoadingNotAnotherLesson() {
        val p = project(ready.copy(account = account.copy(lessons = lessonIds.take(5).toSet()), review = null))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun moreThanFiveQualifyingLessonsDoesNotProduceNegativeRequirements() {
        assertEquals(6, lessonIds.size)
        val p = project(ready.copy(review = null))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun verifiedButUncompletedLessonsDoNotQualify() {
        val p = project(ready.copy(review = null), state.copy(completed = emptySet()))
        assertEquals(WeeklyGemCardStatus.NotEligible, p.status)
        assertEquals(5, p.remainingLessons)
    }

    @Test fun otherUiOwnerCannotExposeReadyOrCompletedData() {
        val p = project(ready.copy(owner = "other@example.test", account = completedAccount()))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun otherAccountOwnerCannotExposeReadyOrCompletedData() {
        val p = project(ready.copy(account = completedAccount().copy(owner = "other@example.test")))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun missingStudentIdentityDoesNotTrustUiOwnership() {
        listOf<AccountType?>(null, AccountType.Guest, AccountType.Teacher).forEach { role ->
            val learning = state.copy(user = role?.let { User("Other", owner, it) })
            assertEquals(WeeklyGemCardStatus.Error, project(learning = learning).status)
        }
        assertEquals(WeeklyGemCardStatus.Error, project(learning = null).status)
        assertEquals(WeeklyGemCardStatus.Error,
            project(learning = state.copy(user = User("Blank", " ", AccountType.Student))).status)
    }

    @Test fun ownershipUsesNormalizedCurrentStudentEmail() {
        val learning = state.copy(user = User("Student", " STUDENT@EXAMPLE.TEST ", AccountType.Student))
        assertEquals(WeeklyGemCardStatus.Ready, project(learning = learning).status)
    }

    @Test fun currentWeekReviewIsReadyAndUsesReadyCopy() {
        val p = project()
        assertEquals(WeeklyGemCardStatus.Ready, p.status)
        assertTrue(p.detail(false).contains("15 gems"))
        assertNoRequirements(p)
    }

    @Test fun staleReviewIsNotReadyForCurrentWeek() {
        val p = project(ready.copy(review = review.copy(week = LocalGemsPolicy.week(now - 7 * 86_400_000L))))
        assertEquals(WeeklyGemCardStatus.Loading, p.status)
        assertNoRequirements(p)
    }

    @Test fun projectionsFollowLoadingReadyAndCompletionWithoutRememberedState() {
        assertEquals(WeeklyGemCardStatus.Loading, project(ready.copy(account = null, review = null)).status)
        assertEquals(WeeklyGemCardStatus.Ready, project().status)
        assertEquals(WeeklyGemCardStatus.Completed,
            project(ready.copy(account = completedAccount(), review = null)).status)
    }
}
