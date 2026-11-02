package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.LocalLearningGems
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.explore.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import kotlin.math.*

class CatalogAcceptanceTest {
    private fun state(stage: StudyStage=StudyStage.BAC)=LearningState(user=User("Fixture","",AccountType.Guest,
        "00000000-0000-0000-0000-000000000001",stage),completed=emptySet(),product=ProductState())
    @Test fun everyLessonAndQuestionResolvesThroughActualCatalogMembership() {
        for(stage in StudyStage.entries) {
            val s=state(stage)
            assertEquals(s.lessons.size,s.lessons.map { it.id }.distinct().size)
            assertEquals(s.questions.size,s.questions.map { it.id }.distinct().size)
            s.lessons.forEach { lesson ->
                assertTrue(StudentCatalog.accepts(s,StudentCatalog.ref(s,lesson.id)!!))
                assertNotNull(StudentCatalog.practiceForLesson(s,lesson.id))
                assertFalse(StudentCatalog.catalog(stage).source.official)
            }
            s.practiceSets.forEach { set ->
                assertNotNull(s.topic(set.topicId)); assertTrue(set.questionIds.isNotEmpty())
                assertEquals(set.questionIds.size,set.questionIds.distinct().size)
                set.questionIds.forEach { id -> assertNotNull(s.questions.find { it.id==id && it.topicId==set.topicId }) }
            }
            s.questions.forEach { assertNotNull(StudentCatalog.practiceForQuestion(s,it.id)) }
        }
    }
    @Test fun bundledNumericalAnswersMatchIndependentArithmeticAndCalculusExamples() {
        val primary=mapOf("limits" to 3.0+2,"derivatives" to 2.0*3,"integrals" to 1.0.pow(2)-0.0,
            "matrices" to 1.0*1-0*0,"probability" to 1.0/2,"geometry" to 2.0.pow(3),"logarithms" to ln(1.0),"sequences" to 2.0*2+1)
        val secondary=mapOf("limits" to 2.0.pow(2),"derivatives" to 3.0*2.0.pow(2),"integrals" to 1.0.pow(3)-0.0,
            "matrices" to 1.0*3-2*0,"probability" to 1.0/6,"geometry" to sqrt(1.0+4+4),"logarithms" to ln(E),"sequences" to 2.0*3+1)
        val s=state()
        for(q in s.questions) {
            val index=q.id.substringAfterLast("-q").toInt()
            val expected=when(index) {
                3 -> "true"; 5 -> "false"; 6 -> secondary.getValue(q.topicId).toString()
                4 -> when(q.topicId) { "derivatives" -> "2x"; "integrals" -> "x^2+C"; "sequences" -> "2"; else -> primary.getValue(q.topicId).toString() }
                else -> primary.getValue(q.topicId).toString()
            }
            assertTrue("${q.id}: ${q.expression}",MathAnswer.matches(expected,q.answer))
            assertTrue(LearningRules.correct(q,q.answer))
            assertFalse(LearningRules.correct(q,"42"))
            assertTrue(q.explanation.en.isNotBlank() && q.explanation.ar.isNotBlank())
        }
        s.topics.forEach { topic -> topic.lessons.filter { it.checkpoint.isNotBlank() }.forEach { lesson ->
            assertTrue("${lesson.id} checkpoint",MathAnswer.matches(primary.getValue(topic.id).toString(),lesson.checkpointAnswer))
        } }
    }
    @Test fun checkpointsAndPracticeShareFiniteArabicAndFractionGrading() {
        assertTrue(MathAnswer.matches("0.5","١ / ٢")); assertTrue(MathAnswer.matches("5","۵"))
        assertTrue(MathAnswer.matches("6","٦٫٠")); assertTrue(MathAnswer.matches("-1","−١"))
        assertFalse(MathAnswer.matches("1/0","1/0")); assertFalse(MathAnswer.matches("1","1/0")); assertFalse(MathAnswer.matches("1","NaN"))
        assertFalse(MathAnswer.matches("1","Infinity")); assertFalse(MathAnswer.matches("1","0.99"))
        val s=state();val lesson=s.lessons.first { it.checkpointAnswer=="5" }
        assertTrue(LocalLearningGems.lessonVerified(s,lesson.id,"٥"))
    }
    @Test fun graphGrammarIsBoundedAndDoesNotInventUnsupportedCurves() {
        val engine=DemoGraphEngine()
        assertTrue(graphSupported("x^2-4"));assertEquals(5.0,engine.evaluate("x^2-4",3.0)!!,1e-9)
        assertEquals(1.0,engine.evaluate("sin(x)",PI/2)!!,1e-9)
        assertNull(engine.evaluate("sqrt(x)",-1.0));assertNull(engine.evaluate("tan(x)",PI/2))
        assertFalse(graphSupported("ln(x)"));assertFalse(graphSupported("eval('x')"))
    }
    @Test fun saveGraphIsAtomicIdempotentScopedAndSurvivesRestart() {
        val files=mutableMapOf<String,FixturePreferences>()
        val preferences={name:String -> files.getOrPut(name) { FixturePreferences() }.prefs}
        var r=LocalDemoRepository(false,preferences);val a=r.createLocalProfile("A",StudyStage.BAC,true)
        val ticket=LearningRules.sessionOwner(r.state.value);val app=AppViewModel(r)
        assertTrue(app.saveGraph("x^2",ticket));assertTrue(app.saveGraph("x^2",ticket))
        assertEquals(1,r.state.value.product.history.size);assertEquals(1,r.state.value.product.savedProblems.size)
        r.createLocalProfile("B",StudyStage.BAC,false);assertFalse(app.saveGraph("x",ticket));assertTrue(r.state.value.product.history.isEmpty())
        r.selectLocalProfile(a.id);files.replaceAll { _,f -> FixturePreferences(f.file) };r=LocalDemoRepository(false,preferences)
        assertEquals("graph",r.state.value.product.history.single().topicId)
        assertEquals(setOf(r.state.value.product.history.single().id),r.state.value.product.savedProblems)
        assertTrue(r.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun exportMeasuredCoverageWithoutClaimingOfficialOrPhoneAcceptance() {
        val output=File("build/reports/student-content").apply { mkdirs() }
        fun csv(value:Any?)="\""+value.toString().replace("\"","\"\"")+"\""
        val rows=mutableListOf(listOf("stage","book","topic","lesson","title","edition","source","availability","official","practice","question_count","phone_qa"))
        val summaries=mutableListOf(listOf("stage","book","units","lessons","unique_questions","practice_sets","official_coverage","phone_qa"))
        for(stage in StudyStage.entries) {
            val s=state(stage);val catalog=StudentCatalog.catalog(stage)
            if(s.books.isEmpty()) { rows+=listOf(stage,"","","","",catalog.edition,catalog.source.id,catalog.availability,false,"",0,"NOT_RUN").map { it.toString() }
                summaries+=listOf(stage,"",0,0,0,0,"BLOCKED_SOURCE","NOT_RUN").map { it.toString() } }
            s.books.forEach { book ->
                val topics=book.topics.map { it.id }.toSet()
                summaries+=listOf(stage,book.id,book.topics.size,book.lessons.size,s.questions.count { it.topicId in topics },s.practiceSets.count { it.topicId in topics },"UNVERIFIED_OFFICIAL_EDITION","NOT_RUN").map { it.toString() }
                book.topics.forEach { topic -> topic.lessons.forEach { l ->
                    val set=StudentCatalog.practiceForLesson(s,l.id)
                    rows+=listOf(stage,book.id,topic.id,l.id,l.title.en,catalog.edition,catalog.source.id,catalog.availability,catalog.source.official,set?.id.orEmpty(),set?.questionIds?.size ?: 0,"NOT_RUN").map { it.toString() }
                } }
            }
        }
        File(output,"lessons.csv").writeText(rows.joinToString("\n") { it.joinToString(",",transform=::csv) }+"\n")
        File(output,"summary.csv").writeText(summaries.joinToString("\n") { it.joinToString(",",transform=::csv) }+"\n")
        assertEquals(2,state().books.size);assertTrue(state(StudyStage.G9).books.isEmpty())
    }
}
