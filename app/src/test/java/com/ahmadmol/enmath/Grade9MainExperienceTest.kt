package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import com.ahmadmol.enmath.features.student.grade9Resume
import com.ahmadmol.enmath.features.student.grade9SubjectUnits
import org.junit.Assert.*
import org.junit.Test

class Grade9MainExperienceTest {
    private fun repository(): LocalDemoRepository {
        val files = mutableMapOf<String, FixturePreferences>()
        return LocalDemoRepository(false) { files.getOrPut(it) { FixturePreferences() }.prefs }
            .also { it.createLocalProfile("Nour", StudyStage.G9, false) }
    }

    @Test fun grade9NavigationIsDistinctAndFocusScreensNeverShowTabsDuringTransitions() {
        assertEquals(listOf("g9/algebra", "g9/geometry", "home", "g9/exercises", "profile"), Routes.studentTabs(StudyStage.G9))
        assertEquals(Routes.tabs, Routes.studentTabs(StudyStage.BAC))
        assertEquals(Routes.tabs, Routes.studentTabs(null))
        Routes.grade9Tabs.forEach { tab ->
            assertTrue(showStudentNavigation(tab, listOf(tab), true, StudyStage.G9))
            listOf(Routes.Lesson, Routes.Topic, Routes.Solver, "explore", "planner", "search",
                "library", "practice/{id}", "practice/{id}/result?attempt={attempt}", Routes.Grade9Source, Routes.Grade9Help,
                "g9/practice", "g9/result/{id}", "g9/files", "g9/file/{id}", "g9/reference/{id}").forEach { detail ->
                assertFalse(showStudentNavigation(detail, listOf(detail), true, StudyStage.G9))
                assertFalse(showStudentNavigation(tab, listOf(detail, tab), true, StudyStage.G9))
            }
            assertFalse(showStudentNavigation(tab, listOf(tab), false, StudyStage.G9))
        }
        assertFalse(showStudentNavigation(Routes.Home, emptyList(), true, StudyStage.G9))
        assertFalse(showStudentNavigation(Routes.Home, listOf(null, Routes.Home), true, StudyStage.G9))
        assertFalse(showStudentNavigation(Routes.Grade9Algebra, listOf(Routes.Grade9Algebra), true, StudyStage.BAC))
    }

    @Test fun subjectSearchKeepsExactCurriculumSequenceAndNeverIncludesOtherSubjects() {
        val algebra = grade9SubjectUnits(false)
        val geometry = grade9SubjectUnits(true)
        assertEquals(listOf(4, 3, 3, 3, 2, 4), algebra.map { it.researches.size })
        assertEquals(listOf(4, 3, 3, 3), geometry.map { it.researches.size })
        assertEquals(G9SourceCatalog.units.map { it.ref.unitId }, (algebra + geometry).map { it.id })
        (algebra + geometry).forEach { unit ->
            val isGeometry = unit.bookId == "G9-GEO"
            unit.researches.forEach { research ->
                assertTrue(grade9SubjectUnits(isGeometry, research.title).any { it.id == unit.id })
            }
        }
        assertTrue(grade9SubjectUnits(false, "النسب المثلثية").isEmpty())
        assertTrue(grade9SubjectUnits(true, "معادلات ومتراجحات").isEmpty())
        assertTrue(grade9SubjectUnits(false, "النهايات").isEmpty())
        assertEquals(listOf("G9-ALG-U01"), grade9SubjectUnits(false, "  أَعْدَاد   وَكُسُور  ").map { it.id })
    }

    @Test fun grade9RestoresTheLongHomeWhileBacAndTeacherKeepTheirExistingPolicy() {
        Routes.grade9Tabs.forEach { assertTrue(restoreStudentTabState(StudyStage.G9, it, false)) }
        assertFalse(restoreStudentTabState(StudyStage.BAC, Routes.Home, false))
        assertFalse(restoreStudentTabState(null, Routes.Home, false))
        assertFalse(restoreStudentTabState(null, Routes.TeacherHome, true))
        assertFalse(restoreStudentTabState(StudyStage.G9, Routes.TeacherHome, true))
        (Routes.tabs - Routes.Home).forEach { assertTrue(restoreStudentTabState(StudyStage.BAC, it, false)) }
        (Routes.teacherTabs - Routes.TeacherHome).forEach { assertTrue(restoreStudentTabState(null, it, true)) }
    }

    @Test fun resumeIsAnExistingSourceReferenceAndNeverUsesArbitraryFallbackOrBacIds() {
        val repo = repository()
        val state = repo.state.value
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        assertNull(grade9Resume(state, null))
        assertNull(grade9Resume(state, "missing"))
        assertNull(grade9Resume(state, BacSourceCatalog.lessons.first().id))
        catalog.researches.forEach { assertEquals(it, grade9Resume(state, it.id)) }
        assertNull(grade9Resume(state.copy(catalogAvailable = false), catalog.researches.first().id))
        assertNull(grade9Resume(state.copy(storageIssue = "unreadable"), catalog.researches.first().id))
        repo.createLocalProfile("Bac", StudyStage.BAC, false)
        assertNull(grade9Resume(repo.state.value, catalog.researches.first().id))
    }

    @Test fun visitingAndSavingEveryStationCannotCreateProgressGemsOrPracticeResults() {
        val repo = repository()
        val before = repo.state.value
        val scope = StudentCatalog.scope(before)!!
        SourceCurricula.forStage(StudyStage.G9).researches.forEach { research ->
            assertEquals(PlanWrite.SAVED, repo.sourceWorkspace.visit(scope, research.ref))
            assertEquals(PlanWrite.SAVED, repo.sourceWorkspace.bookmark(scope, research.ref, true))
        }
        val after = repo.state.value
        assertEquals(before, after)
        assertTrue(StudentCatalog.completed(after).isEmpty())
        assertTrue(StudentCatalog.attempts(after).isEmpty())
        assertTrue(after.practiceSets.isEmpty())
        assertTrue(after.questions.isEmpty())
        assertEquals(32, (repo.sourceWorkspace.read(scope) as SourceWorkspaceRead.Ready).workspace.savedIds.size)
        assertEquals(0, G9SourceCatalog.readyLessonCount)
    }
}
