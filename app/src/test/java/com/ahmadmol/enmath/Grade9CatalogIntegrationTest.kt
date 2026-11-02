package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class Grade9CatalogIntegrationTest {
    private class Stores {
        val files = mutableMapOf<String, FixturePreferences>()
        val preferences = { name: String -> files.getOrPut(name) { FixturePreferences() }.prefs }
        fun repo() = LocalDemoRepository(false, preferences)
        fun restart(): LocalDemoRepository {
            files.replaceAll { _, old -> FixturePreferences(old.file) }
            return repo()
        }
    }
    private fun source(name: String): JSONObject {
        val dir = listOf(File("../docs/curriculum/g9/source"), File("docs/curriculum/g9/source")).first { it.isDirectory }
        return JSONObject(File(dir, name).readText())
    }

    @Test fun compiledCatalogMatchesEveryImportedLessonAndAll200SourcePages() {
        val json = source("G9_CURRICULUM_CATALOG_INDEX.json")
        assertEquals(2, G9SourceCatalog.sources.size)
        assertEquals(10, G9SourceCatalog.units.size)
        assertEquals(32, G9SourceCatalog.indexedLessons.size)
        assertEquals(19, G9SourceCatalog.indexedLessons.count { it.ref.sourceBookId == "G9-ALG" })
        assertEquals(13, G9SourceCatalog.indexedLessons.count { it.ref.sourceBookId == "G9-GEO" })
        val units = json.getJSONArray("units")
        for (i in 0 until units.length()) {
            val u = units.getJSONObject(i)
            val actual = G9SourceCatalog.unit(u.getString("unitId"))!!
            assertEquals(u.getString("title"), actual.title)
            assertEquals(u.getInt("orderInBook"), actual.order)
            assertEquals(u.getJSONObject("reviewExercisePages").getInt("from"), actual.reviewPages.from)
            assertEquals(u.getJSONObject("reviewExercisePages").getInt("to"), actual.reviewPages.to)
            val lessons = u.getJSONArray("lessons")
            for (j in 0 until lessons.length()) {
                val l = lessons.getJSONObject(j)
                val entry = G9SourceCatalog.lesson(l.getString("lessonId"))!!
                assertEquals(l.getString("title"), entry.title)
                assertEquals(l.getString("topicSummaryOriginal"), entry.summary)
                assertEquals(l.getString("draftLearningObjectiveOriginal"), entry.draftObjective)
                assertEquals(l.getInt("order"), entry.order)
                assertEquals(actual.ref.copy(lessonId = entry.ref.lessonId), entry.ref)
                assertEquals(l.getJSONObject("sourcePages").getInt("from"), entry.pages.from)
                assertEquals(l.getJSONObject("sourcePages").getInt("to"), entry.pages.to)
            }
        }
        val pages = source("G9_SOURCE_PAGE_MAP_200_PAGES.json").getJSONArray("pageIndex")
        assertEquals(200, pages.length())
        assertEquals(200, G9SourceCatalog.pages.size)
        for (i in 0 until pages.length()) {
            val p = pages.getJSONObject(i)
            val actual = G9SourceCatalog.pages.single { it.ref.sourceBookId == p.getString("sourceBookId") && it.pdfPage == p.getInt("pdfPageNumberOneBased") }
            assertEquals(p.getString("pageRole"), actual.role)
            assertEquals(if (p.isNull("unitId")) null else p.getString("unitId"), actual.ref.unitId)
            assertEquals(if (p.isNull("lessonId")) null else p.getString("lessonId"), actual.ref.lessonId)
            assertFalse(actual.fullTextReviewed || actual.figuresReviewed || actual.exercisesTranscribed)
        }
        G9SourceCatalog.sources.forEach { source ->
            assertEquals((1..source.pages).toList(), G9SourceCatalog.pages.filter { it.ref.sourceBookId == source.ref.sourceBookId }.map { it.pdfPage }.sorted())
            assertEquals("2025-2026", source.ref.edition)
            assertEquals(64, source.ref.sourceSha256.length)
            assertEquals(StudyStage.G9, source.ref.stage)
        }
    }

    @Test fun draftsAndBookPatternsCannotBecomePublishedQuestionsOrSyntheticActivities() {
        val s = Stores(); val repo = s.repo(); val profile = repo.createLocalProfile("G9", StudyStage.G9, false)
        val state = repo.state.value
        assertTrue(state.lessons.isEmpty())
        assertTrue(state.questions.isEmpty()); assertTrue(state.practiceSets.isEmpty())
        assertEquals(10, G9SourceCatalog.patterns.size)
        assertEquals(BookElementKind.entries.toSet(), G9SourceCatalog.patterns.map { it.kind }.toSet())
        assertTrue(G9SourceCatalog.elements.isEmpty())
        assertEquals(200, G9SourceCatalog.pageReviews.size)
        assertEquals(32, G9SourceCatalog.draftReviews.size)
        G9SourceCatalog.indexedLessons.forEach { entry ->
            assertEquals(ContentStatus.INDEX_ONLY, entry.status)
            assertTrue(G9SourceCatalog.visibleElements(entry.ref).isEmpty())
        }
        val json = source("G9_ORIGINAL_EXERCISE_SEEDS_32.json").getJSONArray("items")
        for (i in 0 until json.length()) {
            val d = json.getJSONObject(i)
            val review = G9SourceCatalog.draftReviews.single { it.id == d.getString("itemId") }
            assertEquals(d.getString("lessonId"), review.ref.lessonId)
            assertNull(review.pdfPage); assertNull(review.sourceExerciseNumber); assertNull(review.figureRequired)
            assertEquals(ReviewStatus.PENDING, review.mathReview); assertEquals(ReviewStatus.PENDING, review.rightsReview)
            assertFalse(AppViewModel(repo).recordPractice(review.id, emptyMap(), 30, LearningRules.sessionOwner(state)))
        }
        assertEquals(0, G9SourceCatalog.readyLessonCount)
        assertEquals(0, G9SourceCatalog.educationalCoveragePercent)
    }

    @Test fun allSourceReferencesRejectLearningCommandsAndRewards() {
        val stores = Stores(); val repo = stores.repo(); repo.createLocalProfile("G9", StudyStage.G9, false)
        val state = repo.state.value; val app = AppViewModel(repo)
        val ticket = LearningRules.sessionOwner(state); val scope = StudentCatalog.scope(state)!!
        G9SourceCatalog.indexedLessons.forEach { source ->
            val id = source.ref.lessonId!!; val ref = G9SourceCatalog.ref(id)!!
            assertTrue(StudentCatalog.acceptsReference(state, ref)); assertFalse(StudentCatalog.accepts(state, ref))
            assertNull(StudentCatalog.ref(state, id)); assertNull(state.lesson(id))
            assertFalse(app.complete(id, ticket)); assertFalse(app.openLesson(id, ticket))
            assertFalse(app.completeInteractive(id, mapOf("q" to "1"), ticket))
            assertFalse(app.recordPractice(id, emptyMap(), 30, ticket))
            assertFalse(repo.recordCommand(scope, ref, "source:$id", "payload") { it })
            assertFalse(repo.openScoped(scope, ref))
            assertFalse(LocalLearningGems.lessonVerified(state, id, "1"))
        }
        assertFalse(app.complete("limits-practice", ticket))
        assertFalse(app.recordPractice(ProductSeed.sets.first().id, emptyMap(), 30, ticket))
        assertEquals(state, repo.state.value)
        assertTrue(StudentCatalog.completed(state.copy(completed = setOf("G9-ALG-U01-L01", "limits-practice"))).isEmpty())
    }

    @Test fun currentPlannerPinsIdempotentlyAndPersistsWithoutLearningWrites() {
        val stores = Stores(); var repo = stores.repo(); val user = repo.createLocalProfile("G9", StudyStage.G9, false)
        val state = repo.state.value; val scope = StudentCatalog.scope(state)!!
        val ref = G9SourceCatalog.ref("G9-ALG-U01-L01")!!
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(scope, ref))
        assertEquals(PlanWrite.ALREADY_SAVED, repo.planner.pinSource(scope, ref))
        val plan = (repo.planner.read(scope) as PlanRead.Ready).plan
        assertEquals(ref, plan.tasks.single().lesson)
        assertEquals(state, repo.state.value)
        assertEquals(0, repo.planner.measured(plan.tasks.single(), state.copy(completed=setOf(ref.lessonId))).done)
        listOf(ref.copy(stage=StudyStage.BAC), ref.copy(edition="2026-2027"), ref.copy(sourceId="wrong"),
            ref.copy(sourceSha256="wrong"), ref.copy(revision=99), ref.copy(kind=ContentKind.LESSON)).forEach {
            assertFalse(StudentCatalog.acceptsReference(state, it))
            assertEquals(PlanWrite.INVALID, repo.planner.pinSource(scope, it))
        }
        repo = stores.restart()
        assertEquals(user.user(), repo.state.value.user)
        assertEquals(state.product, repo.state.value.product)
        assertEquals(plan, (repo.planner.read(StudentCatalog.scope(repo.state.value)!!) as PlanRead.Ready).plan)
    }

    @Test fun stageSwitchPreservesBacDemoCommandsAndKeepsPlansAndSourceIdsSeparate() {
        val stores = Stores(); var repo = stores.repo()
        val bac = repo.createLocalProfile("same", StudyStage.BAC, false)
        val app = AppViewModel(repo); val ticket = LearningRules.sessionOwner(repo.state.value)
        assertTrue(app.complete("limits-practice", ticket))
        val bacState = repo.state.value
        val bacScope = StudentCatalog.scope(bacState)!!; val bacRef = BacSourceCatalog.ref("bac-1-u02-l02")!!
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(bacScope, bacRef))
        val bacPlan = (repo.planner.read(bacScope) as PlanRead.Ready).plan
        val g9 = repo.createLocalProfile("same", StudyStage.G9, false)
        val scope = StudentCatalog.scope(repo.state.value)!!; val ref = G9SourceCatalog.ref("G9-ALG-U01-L01")!!
        assertEquals(PlanWrite.OWNER_CHANGED, repo.planner.pinSource(bacScope, ref))
        assertEquals(PlanWrite.INVALID, repo.planner.pinSource(scope, bacRef))
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(scope, ref))
        val g9Product = repo.state.value.product
        repo = stores.restart()
        assertEquals(g9.user(), repo.state.value.user); assertEquals(g9Product, repo.state.value.product)
        repo.selectLocalProfile(bac.id)
        assertEquals(bac.user(), repo.state.value.user)
        assertEquals(bacState.product, repo.state.value.product); assertEquals(bacState.completed, repo.state.value.completed)
        assertEquals(bacPlan, (repo.planner.read(StudentCatalog.scope(repo.state.value)!!) as PlanRead.Ready).plan)
        assertEquals(DemoContent.books, repo.state.value.books)
        repo.state.value.lessons.forEach { assertSame(DemoContent.lesson(it.id), it) }
        assertEquals(24, repo.state.value.lessons.size)
        assertEquals(PlanWrite.INVALID, repo.planner.pinSource(StudentCatalog.scope(repo.state.value)!!, ref))
        assertEquals(PlanWrite.OWNER_CHANGED, repo.planner.pinSource(scope, ref))
    }

    @Test fun searchFindsBookUnitLessonAndSummariesWithDisjointCatalogs() {
        assertTrue(G9SourceCatalog.search("الهندسة").any { it.route == "book/G9-GEO" })
        assertTrue(G9SourceCatalog.search("الأعداد والكسور").any { it.route == "topic/G9-ALG-U01" })
        assertTrue(G9SourceCatalog.search("اقليدس").any { it.route == "lesson/G9-ALG-U01-L02" })
        assertTrue(G9SourceCatalog.search("النهايات").isEmpty()); assertTrue(G9SourceCatalog.search("   ").isEmpty())
        assertTrue(G9SourceCatalog.indexedLessons.none { g -> BacSourceCatalog.lesson(g.ref.lessonId!!) != null })
        assertEquals(2, StudentCatalog.catalog(StudyStage.G9).grade9SourceIndex.size)
        assertTrue(StudentCatalog.catalog(StudyStage.G9).sourceIndex.isEmpty())
        assertEquals(2, StudentCatalog.catalog(StudyStage.BAC).sourceIndex.size)
        assertTrue(StudentCatalog.catalog(StudyStage.BAC).grade9SourceIndex.isEmpty())
        assertEquals(10, G9SourceCatalog.units.size); assertEquals(32, G9SourceCatalog.indexedLessons.size)
        assertEquals(14, BacSourceCatalog.units.size); assertEquals(63, BacSourceCatalog.lessons.size)
    }

    @Test fun teacherGlobalStorageIsByteIdenticalAfterBrowsingPlanningAndRestart() {
        val stores = Stores(); val global = stores.preferences("enmath_demo")
        val teacher = ProductSeed.seed(); ProductCodec.save(global, teacher)
        val original = global.getString("product_v1", null)
        val repo = stores.repo(); val g9 = repo.createLocalProfile("G9", StudyStage.G9, false)
        assertEquals(PlanWrite.SAVED, repo.planner.pinSource(StudentCatalog.scope(repo.state.value)!!,
            G9SourceCatalog.ref("G9-ALG-U01-L01")!!))
        assertEquals(original, global.getString("product_v1", null)); assertEquals(teacher, ProductCodec.load(global))
        assertEquals(g9.user(), stores.restart().state.value.user)
        assertEquals(original, global.getString("product_v1", null))
    }
}
