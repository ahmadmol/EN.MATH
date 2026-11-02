package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.library.*
import com.ahmadmol.enmath.features.student.Grade9PracticeViewModel
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.After
import org.junit.Test
import java.io.FileNotFoundException
import java.io.IOException

class Grade9Phase3RegressionTest {
    private val jobs = mutableListOf<CoroutineScope>()
    private val gates = mutableListOf<CompletableDeferred<Unit>>()
    private fun model(): Grade9PracticeViewModel {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined).also(jobs::add)
        val gate = CompletableDeferred<Unit>().also(gates::add)
        return Grade9PracticeViewModel(Dispatchers.Unconfined, scope) { gate.await() }
    }
    private fun autosaves() { gates.forEach { it.complete(Unit) } }
    @After fun cleanup() { jobs.forEach { it.cancel() } }
    private class Fixture {
        val files = mutableMapOf<String, FixturePreferences>()
        fun preferences(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        var repo = LocalDemoRepository(false, ::preferences)
        init { repo.createLocalProfile("نور", StudyStage.G9, false) }
        val scope get() = StudentCatalog.scope(repo.state.value)!!
        fun data() = (repo.grade9Study.read(scope) as G9StudyRead.Ready).data
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) }; repo = LocalDemoRepository(false, ::preferences) }
    }
    @Test fun uncheckedInputAutosavesAfterDebounceAndResumesAfterRestart() {
        val f = Fixture(); val before = f.repo.state.value
        val vm = model(); vm.bind(f.scope, f.repo.grade9Study)
        vm.edit("٣/")
        assertNull(f.data().draft)
        autosaves()
        assertEquals("٣/", f.data().draft!!.answers[Grade9Pilot.questions.first().id])
        assertTrue(f.data().attempts.isEmpty()); assertEquals(before.product, f.repo.state.value.product)
        f.restart()
        val resumed = model(); resumed.bind(f.scope, f.repo.grade9Study)
        assertEquals(vm.draft.value, resumed.draft.value)
    }
    @Test fun checkingCancelsOldSaveAndCannotBeUndoneByAStaleSnapshot() {
        val f = Fixture(); val vm = model(); vm.bind(f.scope, f.repo.grade9Study)
        vm.edit("3/4"); val earlier = vm.draft.value!!
        vm.check(); autosaves()
        val checked = f.data().draft!!
        assertEquals(setOf(Grade9Pilot.questions.first().id), checked.checked)
        assertTrue(checked.sequence > earlier.sequence)
        assertEquals(PlanWrite.INVALID, f.repo.grade9Study.saveDraft(f.scope, earlier))
        assertEquals(checked, f.data().draft)
        vm.edit("6/8"); vm.hint(); assertEquals(checked, vm.draft.value)
    }
    @Test fun flushPersistsInputAndNavigationRequiresAnExplicitCheckedOrSkippedAnswer() {
        val f = Fixture(); val vm = model(); vm.bind(f.scope, f.repo.grade9Study)
        vm.next(); assertEquals(0, vm.draft.value!!.index); assertNull(vm.finish())
        vm.edit("4/5"); vm.flushDraft(); autosaves()
        assertEquals("4/5", f.data().draft!!.answers[Grade9Pilot.questions.first().id])
        vm.check(); vm.next(); assertEquals(1, vm.draft.value!!.index)
        assertTrue(f.data().attempts.isEmpty())
    }
    @Test fun savedHintIsIdempotentAndSurvivesRestart() {
        val f = Fixture(); val vm = model(); vm.bind(f.scope, f.repo.grade9Study)
        vm.hint(); val snapshot = f.data().draft; vm.hint()
        assertEquals(snapshot, f.data().draft); f.restart()
        assertEquals(setOf(Grade9Pilot.questions.first().id), f.data().draft!!.hints)
    }
    @Test fun completedSessionHasOneReceiptAndANewRetryCannotBeOverwrittenByOldDraft() {
        val f = Fixture(); val vm = model(); vm.bind(f.scope, f.repo.grade9Study)
        Grade9Pilot.questions.forEachIndexed { i, q -> vm.edit(q.answer); vm.check(); if (i < 3) vm.next() }
        val old = vm.draft.value!!; val result = vm.finish()
        assertNotNull(result); assertNull(vm.finish()); autosaves()
        assertEquals(1, f.data().attempts.size); assertEquals(4, f.data().attempts.single().record.correct)
        assertEquals(PlanWrite.INVALID, f.repo.grade9Study.saveDraft(f.scope, old))
        val retry = model(); retry.bind(f.scope, f.repo.grade9Study)
        assertNotEquals(old.id, retry.draft.value!!.id)
        retry.edit("1/2"); retry.flushDraft()
        assertEquals(1, f.data().attempts.size); assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun profileSwitchCancelsPendingInputAndResetsTheSession() {
        val f = Fixture(); val owner = f.scope
        val vm = model(); vm.bind(owner, f.repo.grade9Study); vm.edit("3/4")
        f.repo.createLocalProfile("Other", StudyStage.G9, true)
        vm.bind(f.scope, f.repo.grade9Study); autosaves()
        assertTrue(vm.draft.value!!.answers.isEmpty()); assertFalse(vm.finished)
        assertEquals(G9StudyData(), f.data())
        assertEquals(G9StudyRead.OwnerChanged, f.repo.grade9Study.read(owner))
    }
    @Test fun anotherActiveSessionCannotReplaceAnUnfinishedDraft() {
        val f = Fixture(); val first = G9PracticeDraft("first")
        assertEquals(PlanWrite.SAVED, f.repo.grade9Study.saveDraft(f.scope, first))
        assertEquals(PlanWrite.INVALID, f.repo.grade9Study.saveDraft(f.scope, G9PracticeDraft("other", sequence = 100)))
        assertEquals(PlanWrite.INVALID, f.repo.grade9Study.finish(f.scope, G9PracticeDraft("other"), 1))
        assertEquals(first, f.data().draft)
    }
    @Test fun staleFinalizeCannotReplaceNewerAnswersOrClearTheActiveDraft() {
        val f = Fixture(); val old = G9PracticeDraft("active")
        f.repo.grade9Study.saveDraft(f.scope, old)
        val current = old.copy(sequence = 2, answers = mapOf(Grade9Pilot.questions.first().id to "3/4"))
        f.repo.grade9Study.saveDraft(f.scope, current)
        assertEquals(PlanWrite.INVALID, f.repo.grade9Study.finish(f.scope, old, 1))
        assertEquals(current, f.data().draft); assertTrue(f.data().attempts.isEmpty())
    }
    @Test fun templatesDoNotActivateDraftGradersOrChangeTheSourceReleaseGate() {
        assertEquals(8, Grade9Pilot.content.sectionCount); assertEquals(Grade9Pilot.ref, Grade9Pilot.content.ref)
        assertTrue(Grade9Pilot.questionTemplates.all { it.hasImplementedGrader() })
        val draft = Grade9Pilot.questionTemplates.first().copy(contract = G9AnswerContract.ManualRubric(listOf(Copy("Justify", "برّر"))))
        assertFalse(draft.hasImplementedGrader()); assertEquals(0, G9SourceCatalog.readyLessonCount)
    }
    @Test fun legacyDraftWithoutSequenceRemainsReadableAndCanBeAdvanced() {
        val f = Fixture(); val first = G9PracticeDraft("existing")
        f.repo.grade9Study.saveDraft(f.scope, first)
        val prefs = f.files.entries.single { it.key.startsWith("enmath_g9_study_") }.value.prefs
        val json = org.json.JSONObject(prefs.getString("study", null)!!)
        json.getJSONObject("draft").remove("sequence")
        prefs.edit().putString("study", json.toString()).commit(); f.restart()
        assertEquals(0L, f.data().draft!!.sequence)
        assertEquals(PlanWrite.SAVED, f.repo.grade9Study.saveDraft(f.scope, f.data().draft!!.copy(sequence = 1, answers = mapOf(Grade9Pilot.questions.first().id to "3/4"))))
    }
    @Test fun resourceSearchCombinesArabicWordsAndTypeWithoutChangingTheList() {
        val files = listOf(StudentFile("p", "content://provider/1", "مُلَخّص الجبر.pdf", "application/pdf"),
            StudentFile("w", "content://provider/2", "ورقة الجبر.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
        assertEquals(listOf("p"), grade9Files(files, "ملخص   الجبر", G9FileFilter.ALL).map { it.id })
        assertEquals(listOf("w"), grade9Files(files, "الجبر", G9FileFilter.WORD).map { it.id })
        assertTrue(grade9Files(files, "هندسة", G9FileFilter.PDF).isEmpty()); assertEquals(2, files.size)
    }
    @Test fun readerFailuresHaveActionableCategoriesAndFileSizesAreHonest() {
        assertEquals(G9FileFailure.PERMISSION, grade9FileFailure(SecurityException()))
        assertEquals(G9FileFailure.MISSING, grade9FileFailure(FileNotFoundException()))
        assertEquals(G9FileFailure.PROVIDER, grade9FileFailure(IOException()))
        assertEquals(G9FileFailure.INVALID, grade9FileFailure(IllegalArgumentException()))
        assertEquals("—", grade9FileSize(null)); assertEquals("1.0 MB", grade9FileSize(1048576))
    }
}
