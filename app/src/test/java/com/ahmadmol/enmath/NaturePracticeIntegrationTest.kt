package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.AccountType
import com.ahmadmol.enmath.core.model.StudyStage
import com.ahmadmol.enmath.core.model.User
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files

class NaturePracticeIntegrationTest {

    private val files = mutableMapOf<String, FixturePreferences>()
    private val preferences = { name: String -> files.getOrPut(name) { FixturePreferences() }.prefs }
    private fun repo() = LocalDemoRepository(false, preferences)

    private fun studentScope(r: LocalDemoRepository): StudentScope {
        return StudentCatalog.scope(r.state.value)!!
    }

    @Test
    fun pilotAssetContainsExactSixQuestionsWithStrictContracts() {
        val assetFile = File("src/debug/assets/content/g9/nature_numbers_pilot.json")
        assertTrue("Asset file must exist", assetFile.exists())
        val jsonText = assetFile.readText(Charsets.UTF_8)
        val doc = org.json.JSONObject(jsonText)

        assertEquals(1, doc.getInt("schemaVersion"))
        assertEquals("G9-A-U01-L01", doc.getString("sourceLessonId"))
        assertEquals("DRAFT_NEEDS_TEACHER_REVIEW", doc.getString("contentStatus"))

        val questions = doc.getJSONArray("questions")
        assertEquals("Must contain exactly 6 questions", 6, questions.length())

        val ids = (0 until questions.length()).map { questions.getJSONObject(it).getString("id") }
        assertEquals(
            listOf(
                "G9-A-U01-L01-Q01",
                "G9-A-U01-L01-Q02",
                "G9-A-U01-L01-Q03",
                "G9-A-U01-L01-Q04",
                "G9-A-U01-L01-Q05",
                "G9-A-U01-L01-Q06"
            ),
            ids
        )

        val difficulties = (0 until questions.length()).map { questions.getJSONObject(it).getInt("difficulty") }
        assertEquals("Progression must be 1, 1, 2, 2, 3, 3", listOf(1, 1, 2, 2, 3, 3), difficulties)

        val interactions = (0 until questions.length()).map { questions.getJSONObject(it).getString("interaction") }
        assertEquals(
            listOf("single_choice", "short_input", "true_false", "fill_blank", "error_correction", "worked_problem"),
            interactions
        )
    }

    @Test
    fun repositorySessionLifecycleAndProgressPersistence() {
        val r = repo()
        val profile = r.createLocalProfile("نور", StudyStage.G9, true)
        r.selectLocalProfile(profile.id)

        val natureRepo = r.naturePractice
        val scope = studentScope(r)

        // 1. Initial read should be ready with empty data
        val initialRead = natureRepo.read(scope)
        assertTrue(initialRead is NatureRead.Ready)
        assertNull((initialRead as NatureRead.Ready).data.draft)
        assertTrue(initialRead.data.attempts.isEmpty())

        // 2. Start session creates a draft
        val startRead = natureRepo.start(scope)
        assertTrue(startRead is NatureRead.Ready)
        val draft = (startRead as NatureRead.Ready).data.draft
        assertNotNull(draft)
        assertEquals(0, draft?.position)
        assertTrue(draft?.passed?.isEmpty() == true)

        // 3. Question 1: Submit wrong then correct
        val q1Id = "G9-A-U01-L01-Q01"
        val wrongQ1 = natureRepo.submit(scope, q1Id, "0")
        assertEquals(PlanWrite.SAVED, wrongQ1.status)
        assertEquals(false, wrongQ1.correct)

        val draftAfterWrong = (natureRepo.read(scope) as NatureRead.Ready).data.draft!!
        assertEquals(1, draftAfterWrong.missed[q1Id])
        assertFalse(draftAfterWrong.passed.contains(q1Id))

        // Request hint without penalty
        val hintStatus = natureRepo.hint(scope)
        assertEquals(PlanWrite.SAVED, hintStatus)
        val draftAfterHint = (natureRepo.read(scope) as NatureRead.Ready).data.draft!!
        assertTrue(draftAfterHint.hints.contains(q1Id))

        // Submit correct
        val correctQ1 = natureRepo.submit(scope, q1Id, "2")
        assertEquals(PlanWrite.SAVED, correctQ1.status)
        assertEquals(true, correctQ1.correct)
        val draftAfterCorrect1 = (natureRepo.read(scope) as NatureRead.Ready).data.draft!!
        assertTrue(draftAfterCorrect1.passed.contains(q1Id))

        // 4. Move to Q2
        val moveQ2 = natureRepo.move(scope, 1)
        assertEquals(PlanWrite.SAVED, moveQ2)

        // Q2: Reduced fraction (test 6/16 rejected, 3/8 accepted)
        val q2Id = "G9-A-U01-L01-Q02"
        val unreduced = natureRepo.submit(scope, q2Id, "6/16")
        assertEquals(false, unreduced.correct)

        val reducedArabicDigits = natureRepo.submit(scope, q2Id, "٣/٨")
        assertEquals(true, reducedArabicDigits.correct)

        // 5. Complete remaining questions
        natureRepo.move(scope, 2)
        natureRepo.submit(scope, "G9-A-U01-L01-Q03", "true")

        natureRepo.move(scope, 3)
        natureRepo.submit(scope, "G9-A-U01-L01-Q04", "غير منتهية|دورية")

        natureRepo.move(scope, 4)
        natureRepo.submit(scope, "G9-A-U01-L01-Q05", "5|integer")

        natureRepo.move(scope, 5)
        val q6Answer = NaturePracticeGrader.serializeCategories(
            mapOf("-3" to "integer", "2/5" to "decimal", "π" to "irrational", "√49" to "integer", "√2" to "irrational")
        )
        natureRepo.submit(scope, "G9-A-U01-L01-Q06", q6Answer)

        // 6. Finish session
        val finishOp = natureRepo.finish(scope)
        assertEquals(PlanWrite.SAVED, finishOp.status)
        assertNotNull(finishOp.resultId)

        // Draft is now cleared, attempt is recorded
        val postFinish = (natureRepo.read(scope) as NatureRead.Ready).data
        assertNull(postFinish.draft)
        assertEquals(1, postFinish.attempts.size)
        val attempt = postFinish.attempts.first()
        assertEquals(finishOp.resultId, attempt.id)
        assertTrue(attempt.hints.contains(q1Id))
        assertTrue((attempt.missed[q1Id] ?: 0) >= 1)
        assertTrue((attempt.missed[q2Id] ?: 0) >= 1)
    }

    @Test
    fun multiStudentIsolationPreventsCrossContamination() {
        val r = repo()
        val s1 = r.createLocalProfile("طالب 1", StudyStage.G9, false)
        val s2 = r.createLocalProfile("طالب 2", StudyStage.G9, false)

        // Select Student 1, start session and answer Q1
        r.selectLocalProfile(s1.id)
        val scope1 = studentScope(r)
        val startOp = r.naturePractice.start(scope1)
        assertTrue(startOp is NatureRead.Ready)
        val submitOp = r.naturePractice.submit(scope1, "G9-A-U01-L01-Q01", "2")
        assertEquals(PlanWrite.SAVED, submitOp.status)
        assertEquals(true, submitOp.correct)

        // Switch to Student 2
        r.selectLocalProfile(s2.id)
        val scope2 = studentScope(r)
        val readS2 = r.naturePractice.read(scope2)
        assertTrue(readS2 is NatureRead.Ready)
        // Student 2 has no draft and no answers
        assertNull((readS2 as NatureRead.Ready).data.draft)
        assertTrue(readS2.data.attempts.isEmpty())

        // Student 2 should not be able to write to Student 1's scope
        val unauthorizedOp = r.naturePractice.submit(scope1, "G9-A-U01-L01-Q01", "2")
        assertEquals(PlanWrite.OWNER_CHANGED, unauthorizedOp.status)

        // Verify Student 1's data remained intact
        r.selectLocalProfile(s1.id)
        val activeScope1 = studentScope(r)
        val readS1 = (r.naturePractice.read(activeScope1) as NatureRead.Ready).data
        assertNotNull(readS1.draft)
        assertTrue(readS1.draft!!.passed.contains("G9-A-U01-L01-Q01"))
    }
}
