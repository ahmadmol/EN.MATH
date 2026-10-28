package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.LocalLearningGems
import com.ahmadmol.enmath.core.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.util.UUID

class BacSourceIntegrationTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        val preferences = { name: String -> files.getOrPut(name) { FixturePreferences() }.prefs }
        fun repo() = LocalDemoRepository(false, preferences)
        fun restart(): LocalDemoRepository { files.replaceAll { _, old -> FixturePreferences(old.file) }; return repo() }
    }
    private fun json(name: String): JSONObject {
        val dir = listOf(File("../docs/curriculum/bac/source"), File("docs/curriculum/bac/source")).first { it.isDirectory }
        return JSONObject(File(dir, name).readText())
    }
    private fun localState() = LearningState(user = User("Fixture", "", AccountType.Student,
        "00000000-0000-0000-0000-000000000001", StudyStage.BAC), completed = emptySet(), product = ProductState())

    @Test fun fullJsonCatalogHasSevenUnitsPerPartAndAll63ExactLessonAnchors() {
        val data = json("BAC_CURRICULUM_CATALOG.json")
        assertEquals(2, BacSourceCatalog.books.size)
        assertEquals(14, BacSourceCatalog.units.size)
        assertEquals(63, BacSourceCatalog.lessons.size)
        assertEquals(63, BacSourceCatalog.lessons.map { it.id }.distinct().size)
        for (volume in 1..2) {
            val book = BacSourceCatalog.books.single { it.volume == volume }
            assertEquals("book-$volume", book.existingBookId)
            assertEquals(if (volume == 1) "2023-2024" else "2021-2022", book.edition)
            assertEquals(if (volume == 1) 253 else 217, book.pdfPages)
            assertEquals(7, BacSourceCatalog.units.count { it.sourceId == book.id })
            assertEquals(if (volume == 1) 34 else 29, BacSourceCatalog.lessons.count { it.sourceId == book.id })
        }
        val units = data.getJSONArray("units")
        for (i in 0 until units.length()) {
            val u = units.getJSONObject(i); val actual = BacSourceCatalog.unit(u.getString("id"))!!
            assertEquals(u.getString("title_ar"), actual.title)
            assertEquals(u.getInt("start_printed_page"), actual.span.printedFrom)
            assertEquals(u.getInt("end_pdf_page"), actual.span.pdfTo)
            val lessons = u.getJSONArray("lessons")
            for (j in 0 until lessons.length()) {
                val l = lessons.getJSONObject(j); val entry = BacSourceCatalog.lesson(l.getString("id"))!!
                assertEquals(l.getString("title_ar"), entry.title)
                assertEquals(l.getString("source_book_id"), entry.sourceId)
                assertEquals(l.getString("source_unit_id"), entry.unitId)
                assertEquals(l.getInt("source_page_start"), entry.span.printedFrom)
                assertEquals(l.getInt("source_page_end"), entry.span.printedTo)
                assertEquals(l.getInt("pdf_page_start"), entry.span.pdfFrom)
                assertEquals(l.getInt("pdf_page_end"), entry.span.pdfTo)
                val book = BacSourceCatalog.book(entry.sourceId)!!
                assertEquals(book.printedOffset, entry.span.pdfFrom - entry.span.printedFrom)
                assertEquals(book.printedOffset, entry.span.pdfTo - entry.span.printedTo)
                assertEquals(SourcePublication.BLOCKED_EDITION_REVIEW, entry.publication)
            }
        }
    }

    @Test fun all470InventoryPagesAnd27SectionsReflectVerifiedCorrectionsAndRetainRawEvidence() {
        assertEquals(470, BacSourceCatalog.pages.size)
        BacSourceCatalog.books.forEach { book ->
            assertEquals((1..book.pdfPages).toList(), BacSourceCatalog.pages.filter { it.sourceId == book.id }.map { it.pdfPage }.sorted())
        }
        assertEquals(13, BacSourceCatalog.sections.count { it.kind == "activity" })
        assertEquals(14, BacSourceCatalog.sections.count { it.kind == "exercise" })
        assertTrue(BacSourceCatalog.unit("bac-1-u07")!!.span.valid)
        assertEquals(SourceSpan(217, 250, 218, 251), BacSourceCatalog.unit("bac-1-u07")!!.span)
        assertTrue(BacSourceCatalog.section("bac-1-u07-exercise")!!.span.valid)
        assertEquals(245, BacSourceCatalog.section("bac-1-u07-exercise")!!.span.pdfFrom)
        assertEquals(251, BacSourceCatalog.section("bac-1-u07-exercise")!!.span.pdfTo)
        assertEquals(4, BacSourceCatalog.pages.count { it.issues.isNotEmpty() })
        val page = BacSourceCatalog.pages.single { it.sourceId == "bac_sci_math_part1" && it.pdfPage == 220 }
        assertEquals("LESSON", page.estimatedRole)
        assertEquals("bac-1-u07", page.unitId)
        assertTrue("bac-1-u07-l01" in page.candidateIds)
        assertTrue(page.issues.isEmpty())
        (218..251).forEach { n -> assertEquals("bac-1-u07", BacSourceCatalog.pages.single { it.sourceId == "bac_sci_math_part1" && it.pdfPage == n }.unitId) }
        val a = BacSourceCatalog.lesson("bac-1-u05-l04")!!
        val b = BacSourceCatalog.lesson("bac-1-u05-l05")!!
        assertNotEquals(a.id, b.id)
        assertEquals(SourceSpan(163, 163, 164, 164), a.span)
        assertEquals(SourceSpan(163, 167, 164, 168), b.span)
        assertTrue(a.issues.isEmpty()); assertTrue(b.issues.isEmpty())
        val shared = BacSourceCatalog.pages.single { it.sourceId == a.sourceId && it.pdfPage == 164 }
        assertEquals(setOf(a.id, b.id), shared.sectionIds.toSet())
        (165..168).forEach { n ->
            val corrected = BacSourceCatalog.pages.single { it.sourceId == a.sourceId && it.pdfPage == n }
            assertEquals(listOf(b.id), corrected.sectionIds)
            assertEquals(listOf("INVENTORY_ANCHOR_MISMATCH"), corrected.issues)
        }
    }

    @Test fun sourceReferencesCannotCompleteAssessMintGemsOrImpersonateLegacyRefs() {
        val stores = Stores(); val repo = stores.repo(); val user = repo.createLocalProfile("BAC", StudyStage.BAC, false)
        val state = repo.state.value; val ticket = LearningRules.sessionOwner(state); val scope = StudentCatalog.scope(state)!!
        val app = AppViewModel(repo)
        BacSourceCatalog.lessons.forEach { source ->
            val ref = BacSourceCatalog.ref(source.id)!!
            assertTrue(StudentCatalog.acceptsReference(state, ref)); assertFalse(StudentCatalog.accepts(state, ref))
            assertNull(StudentCatalog.ref(state, source.id)); assertNull(state.lesson(source.id))
            assertFalse(app.complete(source.id, ticket)); assertFalse(app.openLesson(source.id, ticket))
            assertFalse(repo.recordCommand(scope, ref, "source:${source.id}", "payload") { it })
            assertFalse(repo.openScoped(scope, ref))
            assertFalse(LocalLearningGems.lessonVerified(state, source.id, "1"))
            assertFalse(app.recordPractice(source.id, emptyMap(), 10, ticket))
        }
        assertEquals(state, repo.state.value)
        assertEquals(user.id, state.user?.localId)
        assertEquals(0, BacSourceCatalog.reviewedLessonCount)
    }

    @Test fun all28DraftsStayUnitAlignedWithNoInventedLessonPageOrPublishedQuestion() {
        val items = json("BAC_ORIGINAL_PRACTICE_DRAFTS.json").getJSONArray("items")
        assertEquals(28, BacSourceCatalog.drafts.size)
        val s = localState()
        for (i in 0 until items.length()) {
            val item = items.getJSONObject(i)
            val draft = BacSourceCatalog.drafts.single { it.id == item.getString("id") }
            assertEquals(item.getString("source_unit_id"), draft.unitId); assertNull(draft.lessonId)
            assertNull(s.questions.find { it.id == draft.id })
            assertTrue(s.practiceSets.none { draft.id in it.questionIds })
            assertTrue(item.isNull("lesson_id")); assertFalse(item.getBoolean("grants_gems")); assertFalse(item.getBoolean("mastery_countable"))
        }
    }

    @Test fun plannerSourcePinIsIdempotentScopedPersistentAndNeverLearningEvidence() {
        val stores = Stores(); var repo = stores.repo(); val user = repo.createLocalProfile("BAC", StudyStage.BAC, false)
        val scope = StudentCatalog.scope(repo.state.value)!!
        val ref = BacSourceCatalog.ref("bac-1-u02-l02")!!
        val before = repo.state.value
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(scope, ref))
        assertEquals(PlanWrite.ALREADY_SAVED, repo.planner.pinSource(scope, ref))
        assertEquals(before, repo.state.value)
        val plan = (repo.planner.read(scope) as PlanRead.Ready).plan
        assertEquals(1, plan.tasks.size); assertEquals(ref, plan.tasks.single().lesson)
        val fake = before.copy(completed = setOf(ref.lessonId))
        assertEquals(0, repo.planner.measured(plan.tasks.single(), fake).done)
        listOf(ref.copy(stage = StudyStage.G9), ref.copy(edition = "2026-2027"), ref.copy(sourceId = "other"),
            ref.copy(sourceSha256 = "other"), ref.copy(revision = 99), ref.copy(kind = ContentKind.LESSON)).forEach {
            assertFalse(StudentCatalog.acceptsReference(repo.state.value, it))
            assertEquals(PlanWrite.INVALID, repo.planner.pinSource(scope, it))
        }
        repo = stores.restart()
        assertEquals(user.user(), repo.state.value.user)
        val resumedScope = StudentCatalog.scope(repo.state.value)!!
        assertEquals(plan, (repo.planner.read(resumedScope) as PlanRead.Ready).plan)
        assertEquals(PlanWrite.ALREADY_SAVED, repo.planner.pinSource(resumedScope, ref))
        repo.createLocalProfile("G9", StudyStage.G9, false)
        assertEquals(PlanWrite.OWNER_CHANGED, repo.planner.pinSource(resumedScope, ref))
        assertTrue((repo.planner.read(StudentCatalog.scope(repo.state.value)!!) as PlanRead.Ready).plan.tasks.isEmpty())
        repo.selectLocalProfile(user.id)
        assertEquals(PlanWrite.OWNER_CHANGED, repo.planner.pinSource(resumedScope, ref))
    }

    @Test fun oldDemoObjectsCompletionKeysAndTeacherStorageAreNotRecreatedOrRemapped() {
        val stores = Stores(); val repo = stores.repo()
        val global = stores.preferences("enmath_demo")
        ProductCodec.save(global, ProductSeed.seed())
        val teacherRaw = global.getString("product_v1", null)
        val user = repo.createLocalProfile("BAC", StudyStage.BAC, false)
        val app = AppViewModel(repo); val ticket = LearningRules.sessionOwner(repo.state.value)
        assertTrue(app.complete("limits-practice", ticket))
        val before = repo.state.value
        val oldKey = StudentCatalog.referenceKey(StudentCatalog.ref(before, "limits-practice")!!)
        assertEquals("BAC|bundled-bac-demo-v1|1|enmath-demo|limits-practice", oldKey)
        assertEquals(oldKey, before.product.completionRefs["limits-practice"])
        assertEquals(24, BacSourceCatalog.legacyRelations.size)
        assertEquals(DemoContent.books, before.books)
        before.lessons.forEach { assertSame(DemoContent.lesson(it.id), it) }
        assertTrue(BacSourceCatalog.lessons.none { it.id in before.completed })
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(StudentCatalog.scope(before)!!, BacSourceCatalog.ref("bac-1-u02-l02")!!))
        assertEquals(before, repo.state.value)
        assertEquals(teacherRaw, global.getString("product_v1", null))
        val restarted = stores.restart()
        assertEquals(user.user(), restarted.state.value.user)
        assertEquals(before.product, restarted.state.value.product)
        assertEquals(before.completed, restarted.state.value.completed)
    }

    @Test fun legacyPlannerPayloadAndDuplicateReceiptSurviveTheAdditiveRefCodec() {
        val stores = Stores(); var repo = stores.repo(); repo.createLocalProfile("BAC", StudyStage.BAC, true)
        var scope = StudentCatalog.scope(repo.state.value)!!
        val task = PlanTask(UUID.randomUUID().toString(), "old demo goal", "math", PlanPeriod.WEEK,
            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.ACHIEVEMENT, 1,
            lesson = StudentCatalog.ref(repo.state.value, "limits-practice"))
        assertEquals(PlanWrite.SAVED, repo.planner.save(scope, 0, "legacy-plan-command", task))
        val store = stores.files.entries.single { it.key.startsWith("enmath_plan_") }.value
        val raw = store.prefs.getString("plan", null)!!
        assertFalse(JSONObject(raw).getJSONArray("tasks").getJSONObject(0).getJSONObject("lesson").has("kind"))
        repo = stores.restart(); scope = StudentCatalog.scope(repo.state.value)!!
        assertEquals(PlanWrite.ALREADY_SAVED, repo.planner.save(scope, 0, "legacy-plan-command", task))
        assertEquals(raw, store.prefs.getString("plan", null))
    }

    @Test fun sourceSearchUsesActualIdsAndFiniteArabicAnswerNormalizationIsRetained() {
        assertTrue(BacSourceCatalog.search("التحليل التوافقي").any { it.route == "topic/bac-2-u06" })
        assertTrue(BacSourceCatalog.search("مركز الابعاد المتناسبه").any { it.route == "lesson/bac-2-u01-l05" })
        assertTrue(BacSourceCatalog.search("الجزء الأول").any { it.route == "book/book-1" })
        assertTrue(BacSourceCatalog.search("المصفوفات").isEmpty())
        assertTrue(BacSourceCatalog.search("   ").isEmpty())
        assertTrue(MathAnswer.matches("1/2", "١ / ٢")); assertTrue(MathAnswer.matches("-1", "−١"))
        assertFalse(MathAnswer.matches("1/2", "Infinity")); assertFalse(MathAnswer.matches("1", "NaN"))
        assertFalse(MathAnswer.matches("1", "1/0"))
    }
}
