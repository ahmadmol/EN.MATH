package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.grade9LatestMistakes
import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger

class Grade9PilotLearningTest {
    private class Fixture {
        val files = mutableMapOf<String, FixturePreferences>()
        fun preferences(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        var repo = LocalDemoRepository(false, ::preferences)
        init { repo.createLocalProfile("نور", StudyStage.G9, false) }
        val scope get() = StudentCatalog.scope(repo.state.value)!!
        val study get() = repo.grade9Study
        fun data() = (study.read(scope) as G9StudyRead.Ready).data
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) }; repo = LocalDemoRepository(false, ::preferences) }
        fun draft(id: String = "attempt-1") = G9PracticeDraft(id,
            Grade9Pilot.questions.associate { it.id to it.answer }, Grade9Pilot.questions.map { it.id }.toSet(), index = 3, started = 1000)
    }

    @Test fun pilotUsesTheExactSourceIdentityAndLeavesAllOtherCatalogContentUnpublished() {
        val source = G9SourceCatalog.lesson(Grade9Pilot.lessonId)!!
        assertEquals("كسور مختزلة", source.title)
        assertEquals(SourcePages(14, 15), source.pages)
        assertEquals("2025-2026", Grade9Pilot.ref.edition)
        assertEquals("b6a2d13da67f90afbc6df0ffde60de2827d9359021f2ce1924d2961fa1aed59f", Grade9Pilot.ref.sourceSha256)
        assertEquals(32, G9SourceCatalog.indexedLessons.size)
        assertEquals(0, G9SourceCatalog.readyLessonCount)
        assertTrue(Grade9Pilot.blocks.all { it.body.ar.isNotBlank() && it.body.en.isNotBlank() })
    }

    @Test fun everyPublishedAnswerIsIndependentlyEqualAndCoprime() {
        val all = Grade9Pilot.questions + Grade9Pilot.checkpoint
        assertEquals(all.size, all.map { it.id }.distinct().size)
        all.forEach { q ->
            val parts = q.answer.split('/').map(String::toBigInteger)
            assertEquals(BigInteger.ONE, parts[0].gcd(parts[1]))
            assertEquals(q.numerator.toBigInteger() * parts[1], q.denominator.toBigInteger() * parts[0])
            assertTrue(Grade9Pilot.correct(q, q.answer))
            assertFalse(Grade9Pilot.correct(q, "${parts[0] * BigInteger.TWO}/${parts[1] * BigInteger.TWO}"))
        }
        assertEquals(14, gcd(42, 70)); assertEquals(42, gcd(84, 126))
        assertEquals(42 * 5, 70 * 3); assertEquals(84 * 3, 126 * 2)
    }
    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    @Test fun exactGradingAcceptsArabicDigitsAndRejectsUnsafeOrUnreducedInput() {
        val q = Grade9Pilot.questions.first()
        listOf("3/4", " ٣ / ٤ ", "۳/۴", "3⁄4", "03/04").forEach { assertTrue(it, Grade9Pilot.correct(q, it)) }
        listOf("6/8", "0.75", "3/0", "0/4", "-3/-4", "3.0/4", "NaN", "3/4/1", "", "3e0/4", "3/4x", "9".repeat(1000)+"/4").forEach {
            assertFalse(it.take(20), Grade9Pilot.correct(q, it))
        }
        // Exercise hundreds of exact multiples rather than accepting an arbitrary floating tolerance.
        (1..200).forEach { k -> assertEquals(k == 1, Grade9Pilot.correct(q, "${3*k}/${4*k}")) }
    }

    @Test fun draftCheckpointAttemptAndResourcesSurviveRestartWithoutChangingExistingDocuments() {
        val f = Fixture(); val before = f.repo.state.value
        assertEquals(PlanWrite.SAVED, f.study.section(f.scope, 5))
        assertEquals(PlanWrite.SAVED, f.study.passCheckpoint(f.scope, "٣/٥"))
        val d = f.draft().copy(hints = setOf(Grade9Pilot.questions.first().id))
        assertEquals(PlanWrite.SAVED, f.study.saveDraft(f.scope, d))
        f.restart(); assertEquals(d, f.data().draft)
        assertEquals(PlanWrite.SAVED, f.study.finish(f.scope, d, 23, 5000))
        assertEquals(PlanWrite.SAVED, f.study.addFile(f.scope, StudentFile("pdf", "content://provider/item/1", "Notes.pdf", "application/pdf", 123)))
        assertEquals(PlanWrite.SAVED, f.study.filePage(f.scope, "pdf", 4))
        val data = f.data(); f.restart()
        assertEquals(data, f.data()); assertEquals(4, f.data().attempts.single().record.correct)
        assertNull(f.data().draft); assertEquals(4, f.data().files.single().page)
        assertEquals(before.user, f.repo.state.value.user)
        assertEquals(before.product, f.repo.state.value.product)
        assertEquals(before.completed, f.repo.state.value.completed)
        assertEquals(before.lastLessonId, f.repo.state.value.lastLessonId)
    }

    @Test fun duplicateSubmitCannotAddAnotherAttemptAndChangedPayloadIsRejected() {
        val f = Fixture(); val d = f.draft()
        assertEquals(PlanWrite.SAVED, f.study.finish(f.scope, d, 12, 1000))
        assertEquals(PlanWrite.ALREADY_SAVED, f.study.finish(f.scope, d, 12, 9999))
        assertEquals(PlanWrite.INVALID, f.study.finish(f.scope, d.copy(answers = emptyMap()), 12))
        assertEquals(PlanWrite.INVALID, f.study.finish(f.scope, d, 13))
        assertEquals(PlanWrite.INVALID, f.study.saveDraft(f.scope, d))
        assertEquals(1, f.data().attempts.size)
    }

    @Test fun wrongAnswersAndSkippedQuestionsProduceRealResultsAndLatestMistakes() {
        val f = Fixture(); val all = Grade9Pilot.questions
        val d = f.draft().copy(answers = mapOf(all[0].id to "6/8", all[1].id to "4/15", all[2].id to ""))
        assertEquals(PlanWrite.SAVED, f.study.finish(f.scope, d.copy(checked = d.answers.keys), 10))
        val a = f.data().attempts.single().record
        assertEquals(1, a.correct); assertEquals(4, a.total)
        assertEquals(listOf(all[0].id), grade9LatestMistakes(f.data()).map { it.first.id })
        assertEquals(PlanWrite.SAVED, f.study.finish(f.scope, f.draft("attempt-2"), 20))
        assertTrue(grade9LatestMistakes(f.data()).isEmpty()); assertEquals(2, f.data().attempts.size)
    }

    @Test fun staleOwnerGenerationBacAndWrongEditionCannotReadOrWrite() {
        val f = Fixture(); val owner = f.scope; val first = f.repo.state.value.user!!.localId!!
        f.study.passCheckpoint(owner, "3/5")
        f.repo.createLocalProfile("B", StudyStage.G9, true)
        assertEquals(G9StudyRead.OwnerChanged, f.study.read(owner))
        assertEquals(PlanWrite.OWNER_CHANGED, f.study.finish(owner, f.draft(), 1))
        assertEquals(G9StudyData(), f.data())
        f.repo.selectLocalProfile(first)
        assertEquals(PlanWrite.OWNER_CHANGED, f.study.passCheckpoint(owner, "3/5"))
        assertEquals("3/5", f.data().checkpoint)
        assertEquals(G9StudyRead.OwnerChanged, f.study.read(f.scope.copy(edition = "changed")))
        f.repo.createLocalProfile("Bac", StudyStage.BAC, false)
        assertEquals(G9StudyRead.OwnerChanged, f.study.read(f.scope))
        assertEquals(PlanWrite.OWNER_CHANGED, f.study.passCheckpoint(f.scope, "3/5"))
    }

    @Test fun corruptOrFutureDataIsNeverReplaced() {
        val f = Fixture(); f.study.section(f.scope, 1)
        val prefs = f.files.entries.single { it.key.startsWith("enmath_g9_study_") }.value.prefs
        listOf("{", "{\"version\":999}").forEach { raw ->
            prefs.edit().putString("study", raw).commit()
            assertEquals(G9StudyRead.StorageError, f.study.read(f.scope))
            assertEquals(PlanWrite.STORAGE_ERROR, f.study.passCheckpoint(f.scope, "3/5"))
            assertEquals(raw, prefs.getString("study", null))
        }
    }

    @Test fun failedCommitPreservesBytesAndForcesRestart() {
        val f = Fixture(); f.study.section(f.scope, 1)
        val prefs = f.files.entries.single { it.key.startsWith("enmath_g9_study_") }.value
        val bytes = prefs.file.readBytes(); prefs.fail = true
        assertEquals(PlanWrite.STORAGE_ERROR, f.study.finish(f.scope, f.draft(), 20))
        assertArrayEquals(bytes, prefs.file.readBytes())
        assertEquals(G9StudyRead.StorageError, f.study.read(f.scope))
        f.restart(); assertTrue(f.data().attempts.isEmpty())
        assertEquals(PlanWrite.SAVED, f.study.finish(f.scope, f.draft(), 20))
    }

    @Test fun invalidCommandsNeverCreateLearningEvidence() {
        val f = Fixture(); val before = f.data()
        assertEquals(PlanWrite.INVALID, f.study.passCheckpoint(f.scope, "6/10"))
        assertEquals(PlanWrite.INVALID, f.study.section(f.scope, -1))
        assertEquals(PlanWrite.INVALID, f.study.saveDraft(f.scope, f.draft().copy(index = 100)))
        assertEquals(PlanWrite.INVALID, f.study.finish(f.scope, f.draft().copy(answers = mapOf("Bac" to "3/4")), 5))
        assertEquals(PlanWrite.INVALID, f.study.finish(f.scope, f.draft(), -1))
        assertEquals(before, f.data())
    }

    @Test fun resourceLinksAreScopedValidatedAndRemovalNeverAltersOriginalOrWalletRecords() {
        val f = Fixture(); val file = StudentFile("one", "content://documents/local/1", "Note.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        assertEquals(PlanWrite.SAVED, f.study.addFile(f.scope, file))
        assertEquals(PlanWrite.ALREADY_SAVED, f.study.addFile(f.scope, file.copy(id = "two")))
        listOf(file.copy(uri = "file:///etc/private"), file.copy(uri = "https://example.test/file.pdf"),
            file.copy(uri = "content:///missing-authority"), file.copy(mime = "text/html"), file.copy(bytes = -1), file.copy(name = "bad\nname"), file.copy(page = -1)).forEach {
            assertEquals(PlanWrite.INVALID, f.study.addFile(f.scope, it))
        }
        assertEquals(1, f.data().files.size)
        assertEquals(PlanWrite.INVALID, f.study.filePage(f.scope, "missing", 2))
        assertEquals(PlanWrite.SAVED, f.study.removeFile(f.scope, "one"))
        assertTrue(f.data().files.isEmpty()); assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }
}
