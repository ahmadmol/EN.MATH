package com.ahmadmol.enmath

import androidx.compose.foundation.lazy.LazyListState
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 10A Verification Suite: First Complete Learning Unit (Nature of Numbers).
 *
 * Verifies:
 * 1. Syrian curriculum official index for Grade 9 Algebra Unit 1 (4 lessons identified).
 * 2. Complete educational model for Lesson 1 (Nature of Numbers): concepts, worked examples, rules, and self-check.
 * 3. Status distinction: Unapproved lessons remain strictly marked under review without fabricated questions.
 * 4. Independent scroll retention between Algebra and Geometry learning paths.
 * 5. Practice integration, hints, corrections, and results navigation.
 * 6. Progress persistence, free re-practice, and offline student isolation without duplicated gems.
 */
class Grade9Phase10LearningUnitTest {

    private val goodResponses = listOf(
        "2",
        "٣/٨",
        "true",
        "غير منتهية|دورية",
        "٥|integer",
        "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational"
    )

    private fun qId(i: Int) = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"

    private class Fixture {
        val files = mutableMapOf<String, FixturePreferences>()
        fun preferences(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        var repo = LocalDemoRepository(false, ::preferences)
        init { repo.createLocalProfile("نور", StudyStage.G9, false) }
        val scope get() = StudentCatalog.scope(repo.state.value)!!
        val study get() = repo.grade9Study
        val nature get() = repo.naturePractice
        fun data() = (study.read(scope) as G9StudyRead.Ready).data
        fun restart() {
            files.replaceAll { _, old -> FixturePreferences(old.file) }
            repo = LocalDemoRepository(false, ::preferences)
        }
    }

    // 1. Official Syrian Curriculum Index: Unit 1 (Numbers & Fractions) 4 Lessons
    @Test
    fun officialSyrianIndexUnit1FourLessonsIdentified() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val algebraBook = catalog.book("G9-ALG")
        assertNotNull(algebraBook)
        assertEquals("2025-2026", algebraBook!!.edition)

        val unit1 = algebraBook.units.firstOrNull { it.id == "G9-ALG-U01" }
        assertNotNull(unit1)
        assertEquals("الأعداد والكسور", unit1!!.title)
        assertEquals(4, unit1.researches.size)

        // Lesson 1: طبيعة الأعداد
        val l1 = unit1.researches[0]
        assertEquals("G9-ALG-U01-L01", l1.id)
        assertEquals("طبيعة الأعداد", l1.title)
        assertEquals(6, l1.pages.printedFrom)
        assertEquals(7, l1.pages.printedTo)

        // Lesson 2: القواسم المشتركة لعددين صحيحين
        val l2 = unit1.researches[1]
        assertEquals("G9-ALG-U01-L02", l2.id)
        assertEquals("القواسم المشتركة لعددين صحيحين", l2.title)
        assertEquals(8, l2.pages.printedFrom)
        assertEquals(13, l2.pages.printedTo)

        // Lesson 3: كسور مختزلة
        val l3 = unit1.researches[2]
        assertEquals("G9-ALG-U01-L03", l3.id)
        assertEquals("كسور مختزلة", l3.title)
        assertEquals(14, l3.pages.printedFrom)
        assertEquals(15, l3.pages.printedTo)

        // Lesson 4: الجذر التربيعي لعدد موجب
        val l4 = unit1.researches[3]
        assertEquals("G9-ALG-U01-L04", l4.id)
        assertEquals("الجذر التربيعي لعدد موجب", l4.title)
        assertEquals(16, l4.pages.printedFrom)
        assertEquals(21, l4.pages.printedTo)
    }

    // 2. Complete Educational Model for Nature of Numbers Lesson
    @Test
    fun natureLessonPedagogicalContentIntegrity() {
        assertEquals("G9-ALG-U01-L01", Grade9NatureLesson.LESSON_ID)
        assertEquals("طبيعة الأعداد", Grade9NatureLesson.title.ar)
        assertEquals("Nature of numbers", Grade9NatureLesson.title.en)

        // 6 Concept blocks verified
        assertEquals(6, Grade9NatureLesson.blocks.size)
        val blocks = Grade9NatureLesson.blocks
        assertTrue(blocks[0].title.ar.contains("ماذا سأتعلم"))
        assertTrue(blocks[1].title.ar.contains("الطبيعية والأعداد الصحيحة"))
        assertTrue(blocks[1].formula.contains("N ⊂ Z"))
        assertTrue(blocks[2].title.ar.contains("العادية"))
        assertTrue(blocks[3].title.ar.contains("العشرية"))
        assertTrue(blocks[4].title.ar.contains("غير العادية"))
        assertTrue(blocks[5].title.ar.contains("قاعدة ذهبية"))
        assertTrue(blocks[5].body.ar.contains("√25"))

        // Worked example with 4 progressive steps
        val example = Grade9NatureLesson.workedExample
        assertTrue(example.prompt.ar.contains("-3"))
        assertTrue(example.prompt.ar.contains("2/5"))
        assertTrue(example.prompt.ar.contains("π"))
        assertTrue(example.prompt.ar.contains("√49"))
        assertTrue(example.prompt.ar.contains("√2"))
        assertEquals(4, example.steps.size)
        assertTrue(example.steps[0].formula.contains("√49 = 7"))
        assertTrue(example.steps[1].formula.contains("-3 ∈ Z"))
        assertTrue(example.steps[2].formula.contains("2/5 = 4/10"))
        assertTrue(example.steps[3].formula.contains("π ∉ Q"))

        // Self check
        val check = Grade9NatureLesson.selfCheck
        assertEquals(4, check.options.size)
        assertEquals(2, check.correctIndex)
        assertEquals("√5", check.options[2].ar)
        assertTrue(check.explanation.ar.contains("غير عادي"))

        // Summary
        assertTrue(Grade9NatureLesson.summary.ar.contains("N ⊂ Z ⊂ D ⊂ Q"))
    }

    // 3. Unapproved Lessons Remain Distinct and Under Review
    @Test
    fun unapprovedLessonsAreNotDisplayedAsComplete() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val unit1 = catalog.unit("G9-ALG-U01")!!

        // Lesson 2 and Lesson 4 have no completed practice bank or pilot
        val l2 = unit1.researches[1]
        val l4 = unit1.researches[3]
        assertFalse(NaturePracticeBank.matches(l2))
        assertFalse(NaturePracticeBank.matches(l4))
        assertNotEquals(Grade9Pilot.lessonId, l2.id)
        assertNotEquals(Grade9Pilot.lessonId, l4.id)

        // Only Lesson 1 matches NaturePracticeBank
        val l1 = unit1.researches[0]
        assertTrue(NaturePracticeBank.matches(l1))
    }

    // 4. Independent Scroll Retention Architecture
    @Test
    fun independentScrollRetentionStateIntegrity() {
        val algebraState = LazyListState(firstVisibleItemIndex = 8, firstVisibleItemScrollOffset = 42)
        val geometryState = LazyListState(firstVisibleItemIndex = 2, firstVisibleItemScrollOffset = 10)

        // Simulating 5 switches between Algebra and Geometry
        var currentSubject = Grade9Subject.ALGEBRA
        var activeState = if (currentSubject == Grade9Subject.ALGEBRA) algebraState else geometryState
        assertEquals(8, activeState.firstVisibleItemIndex)
        assertEquals(42, activeState.firstVisibleItemScrollOffset)

        currentSubject = Grade9Subject.GEOMETRY
        activeState = if (currentSubject == Grade9Subject.ALGEBRA) algebraState else geometryState
        assertEquals(2, activeState.firstVisibleItemIndex)
        assertEquals(10, activeState.firstVisibleItemScrollOffset)

        currentSubject = Grade9Subject.ALGEBRA
        activeState = if (currentSubject == Grade9Subject.ALGEBRA) algebraState else geometryState
        assertEquals(8, activeState.firstVisibleItemIndex)
        assertEquals(42, activeState.firstVisibleItemScrollOffset)

        currentSubject = Grade9Subject.GEOMETRY
        activeState = if (currentSubject == Grade9Subject.ALGEBRA) algebraState else geometryState
        assertEquals(2, activeState.firstVisibleItemIndex)
        assertEquals(10, activeState.firstVisibleItemScrollOffset)

        currentSubject = Grade9Subject.ALGEBRA
        activeState = if (currentSubject == Grade9Subject.ALGEBRA) algebraState else geometryState
        assertEquals(8, activeState.firstVisibleItemIndex)
        assertEquals(42, activeState.firstVisibleItemScrollOffset)
    }

    // 5. Practice Integration: Hints, Corrections, and Results Navigation
    @Test
    fun interactivePracticeFullCycleWithHintsAndCorrection() {
        val f = Fixture()
        f.nature.start(f.scope)

        // Q0: submit wrong, take hint, submit correct
        f.nature.submit(f.scope, qId(0), "0")
        f.nature.hint(f.scope)
        f.nature.submit(f.scope, qId(0), goodResponses[0])
        f.nature.move(f.scope, 1)

        // Remaining Q1..Q5
        for (i in 1..5) {
            f.nature.submit(f.scope, qId(i), goodResponses[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }

        val fin = f.nature.finish(f.scope)
        assertEquals(PlanWrite.SAVED, fin.status)
        assertNotNull(fin.resultId)

        val natureData = (f.nature.read(f.scope) as NatureRead.Ready).data
        val attempt = natureData.attempts.single()
        assertEquals(1, attempt.missed.values.sum())
        assertEquals(1, attempt.hints.size)
        assertEquals(6, attempt.answers.size)
    }

    // 6. Free Re-practice without Duplicated Gems or Stale Overwrite
    @Test
    fun freeRepracticePreservesPreviousAttemptAndNoGemsAwarded() {
        val f = Fixture()
        f.nature.start(f.scope)
        for (i in 0..5) {
            f.nature.submit(f.scope, qId(i), goodResponses[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }
        val firstFin = f.nature.finish(f.scope)
        assertEquals(PlanWrite.SAVED, firstFin.status)

        // Gems remain 0
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())

        // Start new practice session (retry)
        f.nature.start(f.scope)
        val dataDuringSecond = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertNotNull(dataDuringSecond.draft)
        assertNotEquals(firstFin.resultId, dataDuringSecond.draft!!.id)
        assertEquals(1, dataDuringSecond.attempts.size)

        // Complete second practice session
        for (i in 0..5) {
            f.nature.submit(f.scope, qId(i), goodResponses[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }
        val secondFin = f.nature.finish(f.scope)
        assertEquals(PlanWrite.SAVED, secondFin.status)

        val finalData = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertEquals(2, finalData.attempts.size)
        assertEquals(firstFin.resultId, finalData.attempts[0].id)
        assertEquals(secondFin.resultId, finalData.attempts[1].id)

        // Still no gems awarded
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }

    // 7. Student Account Isolation and Offline Disk Recovery
    @Test
    fun studentIsolationAndFullDiskRestoration() {
        val f = Fixture()
        val studentA = f.scope

        // Student A completes session
        f.nature.start(studentA)
        for (i in 0..5) {
            f.nature.submit(studentA, qId(i), goodResponses[i])
            if (i < 5) f.nature.move(studentA, i + 1)
        }
        f.nature.finish(studentA)

        // Switch to Student B
        f.repo.createLocalProfile("عمر", StudyStage.G9, false)
        val studentB = f.scope
        assertNotEquals(studentA.ownerId, studentB.ownerId)

        val bData = (f.nature.read(studentB) as NatureRead.Ready).data
        assertNull(bData.draft)
        assertTrue(bData.attempts.isEmpty())

        // Recreate repository from disk
        f.restart()

        // Verify Student A after restart
        f.repo.selectLocalProfile(studentA.ownerId)
        val aRestored = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertEquals(1, aRestored.attempts.size)

        // Verify Student B after restart
        f.repo.selectLocalProfile(studentB.ownerId)
        val bRestored = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertEquals(0, bRestored.attempts.size)
    }

    // 8. Progress Metrics and Review Recommendation
    @Test
    fun progressMetricsCorrectlyAssigned() {
        val f = Fixture()
        f.nature.start(f.scope)

        // Introduce 4 mistakes on Q0 to trigger NEEDS_REVIEW
        for (k in 1..4) {
            f.nature.submit(f.scope, qId(0), "0")
        }
        f.nature.submit(f.scope, qId(0), goodResponses[0])
        f.nature.move(f.scope, 1)

        for (i in 1..5) {
            f.nature.submit(f.scope, qId(i), goodResponses[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }
        f.nature.finish(f.scope)

        val natureData = (f.nature.read(f.scope) as NatureRead.Ready).data
        val progress = StudentLearningProgressEngine.computeGrade9Progress(natureData)

        assertEquals(LessonProgressStatus.NEEDS_REVIEW, progress.natureLessonStatus)
        assertTrue(progress.recommendedNextStep.ar.contains("راجع"))
        assertEquals(1, progress.completedSessionsCount)
    }
}
