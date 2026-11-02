package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test

/**
 * Phase 9 Verification Suite: Student Learning Journey Redesign.
 * Covers all 17 required verification items on JVM runtime without emulator or ADB.
 */
class Grade9Phase9JourneyTest {

    private val good = listOf(
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

    // 1. First-time Algebra Selection
    @Test
    fun firstTimeAlgebraSelection() {
        val f = Fixture()
        assertNull(f.data().activeSubject)
        assertFalse(f.data().subjectChosen)

        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        assertEquals(Grade9Subject.ALGEBRA, f.data().activeSubject)
        assertTrue(f.data().subjectChosen)
    }

    // 2. First-time Geometry Selection
    @Test
    fun firstTimeGeometrySelection() {
        val f = Fixture()
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.GEOMETRY))
        assertEquals(Grade9Subject.GEOMETRY, f.data().activeSubject)
        assertTrue(f.data().subjectChosen)
    }

    // 3. Subject Persistence across App Recreations
    @Test
    fun subjectPersistsAcrossAppRestarts() {
        val f = Fixture()
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.GEOMETRY))
        f.restart()
        assertEquals(Grade9Subject.GEOMETRY, f.data().activeSubject)
        assertTrue(f.data().subjectChosen)
    }

    // 4. Switching Subject without Loss of Progress
    @Test
    fun switchingSubjectPreservesProgressAndAttempts() {
        val f = Fixture()
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        f.nature.start(f.scope)
        for (i in 0..5) {
            f.nature.submit(f.scope, qId(i), good[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }
        assertEquals(PlanWrite.SAVED, f.nature.finish(f.scope).status)

        val natureDataBefore = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertEquals(1, natureDataBefore.attempts.size)

        // Switch to Geometry
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.GEOMETRY))
        assertEquals(Grade9Subject.GEOMETRY, f.data().activeSubject)

        // Switch back to Algebra
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        assertEquals(Grade9Subject.ALGEBRA, f.data().activeSubject)

        // Progress intact
        val natureDataAfter = (f.nature.read(f.scope) as NatureRead.Ready).data
        assertEquals(1, natureDataAfter.attempts.size)
        assertEquals(natureDataBefore.attempts.first().id, natureDataAfter.attempts.first().id)
    }

    // 5. Isolation between Two Students
    @Test
    fun isolationBetweenTwoStudents() {
        val f = Fixture()
        val studentA = f.scope
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(studentA, Grade9Subject.ALGEBRA))

        // Create student B
        f.repo.createLocalProfile("سامي", StudyStage.G9, false)
        val studentB = f.scope
        assertNotEquals(studentA.ownerId, studentB.ownerId)

        assertNull(f.data().activeSubject)
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(studentB, Grade9Subject.GEOMETRY))
        assertEquals(Grade9Subject.GEOMETRY, f.data().activeSubject)

        // Switch back to Student A
        f.repo.selectLocalProfile(studentA.ownerId)
        assertEquals(Grade9Subject.ALGEBRA, f.data().activeSubject)
    }

    // 6. Correct Units and Topics Count for Each Subject
    @Test
    fun correctUnitsAndTopicsCount() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val algebraBook = catalog.books.first { it.id == "G9-ALG" }
        val geometryBook = catalog.books.first { it.id == "G9-GEO" }

        // Algebra: 6 units, 19 topics
        assertEquals(6, algebraBook.units.size)
        assertEquals(19, algebraBook.researches.size)

        // Geometry: 4 units, 13 topics
        assertEquals(4, geometryBook.units.size)
        assertEquals(13, geometryBook.researches.size)

        // Total 32 indexed topics
        assertEquals(32, catalog.researches.size)
    }

    // 7. Opening Nature of Numbers from Station
    @Test
    fun openingNatureOfNumbersFromStation() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val natureResearch = catalog.researches.first { it.id == "G9-ALG-U01-L01" }
        assertTrue(NaturePracticeBank.matches(natureResearch))
        assertEquals("G9-ALG-U01-L01", natureResearch.id)
    }

    // 8. Progress Updates after Answering Questions
    @Test
    fun progressUpdatesAfterTwoQuestions() {
        val f = Fixture()
        f.nature.start(f.scope)
        f.nature.submit(f.scope, qId(0), good[0])
        f.nature.move(f.scope, 1)
        f.nature.submit(f.scope, qId(1), good[1])

        val natureData = (f.nature.read(f.scope) as NatureRead.Ready).data
        val progress = StudentLearningProgressEngine.computeGrade9Progress(natureData)

        assertEquals(LessonProgressStatus.STARTED, progress.natureLessonStatus)
        assertEquals(2f / 6f, progress.availablePracticeFraction, 0.001f)
        assertTrue(progress.availablePracticeProgressText.ar.contains("2 من 6"))
    }

    // 9. Practice Completed Updates Circle State
    @Test
    fun practiceCompletedUpdatesCircleState() {
        val f = Fixture()
        f.nature.start(f.scope)
        for (i in 0..5) {
            f.nature.submit(f.scope, qId(i), good[i])
            if (i < 5) f.nature.move(f.scope, i + 1)
        }
        val fin = f.nature.finish(f.scope)
        assertEquals(PlanWrite.SAVED, fin.status)

        val natureData = (f.nature.read(f.scope) as NatureRead.Ready).data
        val progress = StudentLearningProgressEngine.computeGrade9Progress(natureData)

        assertEquals(LessonProgressStatus.PRACTICE_COMPLETED, progress.natureLessonStatus)
        assertEquals(1.0f, progress.availablePracticeFraction, 0.001f)
    }

    // 10. Backward Compatibility with Existing Documents
    @Test
    fun backwardCompatibilityWithExistingDocuments() {
        val f = Fixture()
        // Save initial section to create a valid document
        assertEquals(PlanWrite.SAVED, f.study.section(f.scope, 1))

        // Read raw document and remove activeSubject and subjectChosen fields to simulate legacy format
        val prefs = f.files.entries.single { it.key.startsWith("enmath_g9_study_") }.value.prefs
        val raw = prefs.getString("study", null)!!
        val doc = org.json.JSONObject(raw)
        doc.remove("activeSubject")
        doc.remove("subjectChosen")
        prefs.edit().putString("study", doc.toString()).commit()

        val read = f.study.read(f.scope) as G9StudyRead.Ready
        assertNull(read.data.activeSubject)
        assertFalse(read.data.subjectChosen)
    }

    // 11. Content Safety: Unapproved Topics Stay Unavailable
    @Test
    fun unapprovedTopicsStayUnavailable() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val unreleased = catalog.researches.filter { it.id != "G9-ALG-U01-L01" && it.id != Grade9Pilot.lessonId }
        assertTrue(unreleased.isNotEmpty())
        unreleased.forEach { research ->
            assertFalse(NaturePracticeBank.matches(research))
            assertNotEquals(Grade9Pilot.lessonId, research.id)
        }
    }

    // 12. Scientific Baccalaureate Remains Isolated
    @Test
    fun baccalaureateRemainsIsolated() {
        val f = Fixture()
        f.repo.createLocalProfile("طالب بكالوريا", StudyStage.BAC, false)
        val bacScope = StudentCatalog.scope(f.repo.state.value)!!
        assertEquals(StudyStage.BAC, bacScope.stage)

        // G9 Study store rejects Baccalaureate scope
        assertEquals(G9StudyRead.OwnerChanged, f.study.read(bacScope))
        assertEquals(PlanWrite.OWNER_CHANGED, f.study.selectSubject(bacScope, Grade9Subject.ALGEBRA))
    }

    // 13. Gems Ledger and Study Plan Unaffected by Subject Switching
    @Test
    fun gemsAndStudyPlanUnaffectedBySubjectSwitch() {
        val f = Fixture()
        val initialPlan = f.repo.planner.read(f.scope)
        assertTrue(initialPlan is PlanRead.Ready)

        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.GEOMETRY))

        val postPlan = f.repo.planner.read(f.scope)
        assertEquals(initialPlan, postPlan)
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }

    // 14. Dual Stage Model Verification
    @Test
    fun dualStageModelIntegrity() {
        assertEquals(StudyStage.G9, StudyStage.valueOf("G9"))
        assertEquals(StudyStage.BAC, StudyStage.valueOf("BAC"))
    }

    // 15. Research Identifiers Order and Mapping
    @Test
    fun researchIdentifiersOrderAndIntegrity() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        catalog.books.forEach { book ->
            book.units.forEach { unit ->
                assertTrue(unit.researches.isNotEmpty())
                unit.researches.forEachIndexed { idx, r ->
                    assertEquals(idx + 1, r.order)
                    assertTrue(r.id.startsWith(book.id))
                }
            }
        }
    }

    // 16. Subject Enum Safety
    @Test
    fun subjectEnumSafety() {
        assertEquals(Grade9Subject.ALGEBRA, Grade9Subject.valueOf("ALGEBRA"))
        assertEquals(Grade9Subject.GEOMETRY, Grade9Subject.valueOf("GEOMETRY"))
        assertEquals(2, Grade9Subject.values().size)
    }

    // 17. Safe Handling of Re-selection of Same Subject
    @Test
    fun reselectionOfSameSubjectIsIdempotent() {
        val f = Fixture()
        assertEquals(PlanWrite.SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        assertEquals(PlanWrite.ALREADY_SAVED, f.study.selectSubject(f.scope, Grade9Subject.ALGEBRA))
        assertEquals(Grade9Subject.ALGEBRA, f.data().activeSubject)
        assertTrue(f.data().subjectChosen)
    }
}
