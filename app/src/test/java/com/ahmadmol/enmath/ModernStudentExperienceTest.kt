package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ModernStudentExperienceTest {
    private fun bank() = OriginalDraftBank.parse(File("src/debug/assets/content/g9_original_draft_v1.json").readText())
    private class Fixture {
        val files = mutableMapOf<String, FixturePreferences>()
        fun prefs(name: String) = files.getOrPut(name) { FixturePreferences() }.prefs
        var repo = LocalDemoRepository(false, ::prefs)
        val events = mutableMapOf<String, Long>()
        var modern = ModernStudentRepository(::prefs, { repo.state.value }, { events.toMap() })
        init { repo.createLocalProfile("A", StudyStage.G9, false) }
        val scope get() = StudentCatalog.scope(repo.state.value)!!
        fun data() = (modern.read(scope) as ModernRead.Ready).data
        fun restart() { files.replaceAll { _, old -> FixturePreferences(old.file) }; repo = LocalDemoRepository(false, ::prefs); modern = ModernStudentRepository(::prefs, { repo.state.value }, { events.toMap() }) }
        fun stored() = files.getValue("enmath_student_experience_v1_${scope.ownerId}")
    }
    @Test fun all192FullRecordsMapTo32ExistingLessonsWithoutChangingCatalogIds() {
        val bank = bank(); val catalog = SourceCurricula.forStage(StudyStage.G9)
        assertEquals(192, bank.questions.size); assertEquals(192, bank.questions.map { it.id }.toSet().size)
        assertEquals(catalog.researches.map { it.id }.toSet(), bank.mappings.map { it.sourceId }.toSet())
        assertEquals(listOf(6, 4), catalog.books.map { it.units.size }); assertEquals(listOf(19, 13), catalog.books.map { it.researches.size })
        bank.mappings.forEach { m -> assertEquals(m.unitId, catalog.research(m.sourceId)!!.unitId); assertEquals(6, bank.forLesson(m.sourceId).size) }
        bank.questions.forEach { q -> val raw = JSONObject(q.sourceProvenance)
            assertEquals(q.id, raw.getString("id")); assertTrue(q.prompt.isNotBlank()); assertTrue(q.explanation.isNotBlank()); assertTrue(q.hint.isNotBlank())
            assertEquals("draft_needs_teacher_review", q.reviewStatus); assertFalse(raw.getBoolean("officialExamVerified"))
        }
    }
    @Test fun allSixInteractionsAndAllDifficultyLevelsAreRetainedPerLesson() {
        bank().questions.groupBy { it.sourceLessonId }.values.forEach {
            assertEquals(DraftInteraction.entries.toSet(), it.map { q -> q.interaction }.toSet())
            assertEquals(listOf(1,1,2,2,3,3), it.map { q -> q.difficulty })
        }
    }
    @Test fun referenceAnswersAreCorrectOrExplicitlyAwaitTeacherReview() {
        bank().questions.forEach { q ->
            val answer = if (q.interaction == DraftInteraction.TRUE_FALSE) q.booleanAnswer.toString() else q.answer
            assertNotEquals(q.id, DraftGrade.INCORRECT, OriginalDraftGrader.grade(q, answer))
            if (q.gradingMode == "structured_or_teacher_review_required") assertEquals(DraftGrade.NEEDS_TEACHER_REVIEW, OriginalDraftGrader.grade(q, answer))
        }
    }
    @Test fun numericMarkingRejectsNonfiniteAndUnreducedAnswersAndNormalizesArabicDigits() {
        val q = bank().questions.single { it.id == "G9-A-U01-L03-Q02" }
        assertEquals(DraftGrade.CORRECT, OriginalDraftGrader.grade(q, "٣/٥"))
        listOf("6/10", "0.6", "NaN", "Infinity", "3/0", "-3/-5").forEach { assertEquals(DraftGrade.INCORRECT, OriginalDraftGrader.grade(q,it)) }
        val symbolic = bank().questions.single { it.id == "G9-A-U02-L02-Q02" }
        assertEquals(DraftGrade.NEEDS_TEACHER_REVIEW, OriginalDraftGrader.grade(symbolic, "6x+12"))
    }
    @Test fun invalidMappingDuplicateQuestionAndApprovalTamperingFailClosed() {
        val text = File("src/debug/assets/content/g9_original_draft_v1.json").readText()
        val doc = JSONObject(text); doc.getJSONArray("lessonMapping").getJSONObject(0).put("sourceUnitId", "G9-ALG-U02")
        assertThrows(IllegalArgumentException::class.java) { OriginalDraftBank.parse(doc.toString()) }
        val duplicate = JSONObject(text); duplicate.getJSONArray("questions").put(1, duplicate.getJSONArray("questions").getJSONObject(0))
        assertThrows(IllegalArgumentException::class.java) { OriginalDraftBank.parse(duplicate.toString()) }
        assertThrows(IllegalArgumentException::class.java) { OriginalDraftBank.parse(text.replace("DEBUG_QA_ONLY", "APPROVED")) }
    }
    @Test fun sixPreviouslyIntegratedIdsAreReusedAndOnly186AreNew() {
        val all = JSONObject(File("src/debug/assets/content/g9_original_draft_v1.json").readText())
        val provenance = all.getJSONObject("importProvenance")
        assertEquals(6, provenance.getJSONArray("previouslyIntegratedIds").length()); assertEquals(186, provenance.getInt("newlyIntegratedCount"))
        val old = JSONObject(File("src/debug/assets/content/g9/nature_numbers_pilot.json").readText()).getJSONArray("questions")
        (0 until old.length()).forEach { i -> val q = old.getJSONObject(i); assertEquals(q.getString("prompt"), bank().questions.single { it.id == q.getString("id") }.prompt) }
    }
    @Test fun eachStudentAndGuestKeepTheirOwnAvatarAfterRestart() {
        val f = Fixture(); val a = f.scope.ownerId
        assertNull(f.data().selectedAvatarId); assertEquals(ModernWrite.SAVED, f.modern.selectAvatar(f.scope,"muadh"))
        f.repo.createLocalProfile("B",StudyStage.G9,false); val b=f.scope.ownerId
        assertNull(f.data().selectedAvatarId); f.modern.selectAvatar(f.scope,"lama")
        f.restart(); assertEquals("lama",f.data().selectedAvatarId)
        f.repo.selectLocalProfile(a); assertEquals("muadh",f.data().selectedAvatarId)
        f.repo.createLocalProfile("",StudyStage.G9,true); assertNull(f.data().selectedAvatarId)
        f.repo.selectLocalProfile(b); assertEquals("lama",f.data().selectedAvatarId)
    }
    @Test fun allNinePoseFallbacksKeepTheSameAvatarIdentity() {
        StudentAvatars.ids.forEach { id -> AvatarPose.entries.forEach { assertEquals(id, StudentAvatars.poseOwner(id,it)) } }
        assertNull(StudentAvatars.poseOwner("not-an-avatar",AvatarPose.CORRECT))
    }
    @Test fun staleGenerationWrongOwnerAndStageCannotReadOrWrite() {
        val f=Fixture(); val old=f.scope; f.modern.selectAvatar(old,"waseem")
        f.repo.createLocalProfile("B",StudyStage.BAC,false)
        assertEquals(ModernRead.OwnerChanged,f.modern.read(old)); assertEquals(ModernWrite.OWNER_CHANGED,f.modern.selectAvatar(old,"lama"))
        assertNull(f.data().selectedAvatarId); f.repo.selectLocalProfile(old.ownerId)
        assertEquals(ModernRead.OwnerChanged,f.modern.read(old)); assertEquals("waseem",f.data().selectedAvatarId)
    }
    @Test fun invalidAvatarNeverChangesThePreviousChoice() {
        val f=Fixture(); f.modern.selectAvatar(f.scope,"muadh"); val before=f.stored().file.readBytes()
        assertEquals(ModernWrite.INVALID,f.modern.selectAvatar(f.scope,"unknown")); assertArrayEquals(before,f.stored().file.readBytes())
    }
    @Test fun failedCommitPreservesBytesPoisonsInstanceAndRestartsWithPreviousValue() {
        val f=Fixture(); f.modern.selectAvatar(f.scope,"muadh"); val original=f.stored().file.readBytes(); f.stored().fail=true
        assertEquals(ModernWrite.STORAGE_ERROR,f.modern.selectAvatar(f.scope,"lama")); assertEquals(ModernRead.StorageError,f.modern.read(f.scope))
        assertArrayEquals(original,f.stored().file.readBytes()); f.restart(); assertEquals("muadh",f.data().selectedAvatarId)
    }
    @Test fun corruptAndFutureDocumentsArePreserved() {
        val f=Fixture(); f.modern.selectAvatar(f.scope,"muadh")
        listOf("{", "{\"version\":999}").forEach { raw -> f.stored().prefs.edit().putString("document",raw).commit()
            assertEquals(ModernRead.StorageError,f.modern.read(f.scope)); assertNotEquals(ModernWrite.SAVED,f.modern.selectAvatar(f.scope,"lama")); assertEquals(raw,f.stored().prefs.getString("document",null)) }
    }
    @Test fun scrollAnchorAndMotionPreferenceSurviveRestartWithoutLearningCredit() {
        val f=Fixture(); val id=SourceCurricula.forStage(StudyStage.G9).researches.last().id
        f.modern.rememberStation(f.scope,id); f.modern.reducedMotion(f.scope,true); f.restart()
        assertEquals(id,f.data().resumeLesson); assertTrue(f.data().reducedMotion); assertTrue(f.data().events.isEmpty())
        assertTrue(f.repo.state.value.completed.isEmpty()); assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun dailyDeduplicationReplayAndGapsPreserveHistory() {
        val f=Fixture(); val now=1_800_000_000_000L; val day=86_400_000L
        f.events.putAll(mapOf("one" to now-day*2,"two" to now-day,"three" to now,"four" to now+1))
        assertEquals(ModernWrite.SAVED,f.modern.refreshLearningDays(f.scope,now+1)); assertEquals(4,f.data().events.size)
        assertEquals(3,LearningStreak.current(f.data().events,now+1,f.data().zone))
        assertEquals(ModernWrite.ALREADY_SAVED,f.modern.refreshLearningDays(f.scope,now+1)); f.restart(); assertEquals(4,f.data().events.size)
        assertEquals(0,LearningStreak.current(f.data().events,now+day*3,f.data().zone)); assertEquals(4,f.data().events.size)
    }
    @Test fun timezoneIsCapturedAndMidnightUsesTheStoredZone() {
        val at=1_800_000_000_000L
        assertNotEquals(LearningStreak.day(at,"Pacific/Kiritimati"),LearningStreak.day(at,"Pacific/Honolulu"))
        val f=Fixture(); f.modern.selectAvatar(f.scope,"muadh"); val zone=f.data().zone; f.restart(); assertEquals(zone,f.data().zone)
    }
    @Test fun futureEventsAndBackwardClockDoNotManufactureLearningDays() {
        val f=Fixture(); val now=1_800_000_000_000L
        f.events["real"]=now; f.modern.refreshLearningDays(f.scope,now)
        f.events["future"]=now+86_400_000L
        assertEquals(ModernWrite.ALREADY_SAVED,f.modern.refreshLearningDays(f.scope,now)); assertEquals(1,f.data().events.size)
        assertEquals(ModernWrite.CLOCK_REVIEW,f.modern.refreshLearningDays(f.scope,now-600_000)); assertEquals(1,f.data().events.size)
    }
    @Test fun browsingAndQaDraftsDoNotCreditStreakOrGems() {
        val f=Fixture(); val bank=bank(); val before=f.repo.state.value
        val ref=SourceCurricula.forStage(StudyStage.G9).researches.first().ref
        f.repo.sourceWorkspace.visit(f.scope,ref); f.repo.sourceWorkspace.bookmark(f.scope,ref,true)
        f.repo.studentExperience.startQa(f.scope,"G9-ALG-U01-L02",bank)
        f.repo.studentExperience.refreshLearningDays(f.scope)
        assertTrue((f.repo.studentExperience.read(f.scope) as ModernRead.Ready).data.events.isEmpty())
        assertEquals(before.completed,f.repo.state.value.completed); assertEquals(before.product.pendingGemEvents,f.repo.state.value.product.pendingGemEvents)
        assertEquals(EnergyMode.UNLIMITED,StudentEnergy().mode)
    }
    @Test fun completedOriginalPracticeFeedsStreakWithoutChangingRewardLedger() {
        val f=Fixture(); val q=Grade9Pilot.questions.first()
        f.repo.grade9Study.finish(f.scope,G9PracticeDraft("real-attempt",mapOf(q.id to q.answer),checked=setOf(q.id)),20)
        assertEquals(ModernWrite.SAVED,f.repo.studentExperience.refreshLearningDays(f.scope))
        assertEquals(1,(f.repo.studentExperience.read(f.scope) as ModernRead.Ready).data.events.size)
        assertTrue(f.repo.state.value.product.pendingGemEvents.isEmpty())
    }
    @Test fun qaWrongAnswersHintsDraftResumeAndDeferredJudgmentAreRetained() {
        val f=Fixture(); val bank=bank(); val lesson="G9-ALG-U01-L02"; val qs=bank.forLesson(lesson)
        f.modern.startQa(f.scope,lesson,bank)
        f.modern.answerQa(f.scope,lesson,bank,"2"); f.modern.submitQa(f.scope,lesson,bank); f.modern.hintQa(f.scope,lesson,bank)
        assertEquals(ModernWrite.ALREADY_SAVED,f.modern.submitQa(f.scope,lesson,bank))
        assertEquals(DraftGrade.INCORRECT,f.data().drafts.single().submissions.single().grade)
        assertEquals(ModernWrite.INVALID,f.modern.moveQa(f.scope,lesson,bank,1))
        f.modern.answerQa(f.scope,lesson,bank,qs[0].answer); f.modern.submitQa(f.scope,lesson,bank); f.modern.moveQa(f.scope,lesson,bank,1)
        f.modern.answerQa(f.scope,lesson,bank,"١٤"); f.restart()
        val d=f.data().drafts.single(); assertEquals("١٤",d.answers[qs[1].id]); assertEquals(2,d.submissions.size); assertTrue(qs[0].id in d.hints)
        assertEquals(1,d.position)
    }
    @Test fun qaSessionCompletionKeepsRealCountsAndNoDuplicateAcceptedSubmissions() {
        val f=Fixture(); val bank=bank(); val lesson="G9-ALG-U01-L02"; val qs=bank.forLesson(lesson)
        f.modern.startQa(f.scope,lesson,bank)
        qs.forEachIndexed { index,q ->
            f.modern.answerQa(f.scope,lesson,bank,if(q.interaction==DraftInteraction.TRUE_FALSE) q.booleanAnswer.toString() else q.answer)
            f.modern.submitQa(f.scope,lesson,bank); assertEquals(ModernWrite.ALREADY_SAVED,f.modern.submitQa(f.scope,lesson,bank))
            if(index<5) f.modern.moveQa(f.scope,lesson,bank,index+1)
        }
        assertEquals(ModernWrite.SAVED,f.modern.finishQa(f.scope,lesson,bank)); assertTrue(f.data().drafts.isEmpty())
        val result=f.data().results.single(); assertEquals(6,result.submissions.size); assertEquals(3,result.independentCorrect)
        assertEquals(3,result.submissions.count { it.grade==DraftGrade.NEEDS_TEACHER_REVIEW }); assertNotNull(result.finishedAt)
        f.modern.startQa(f.scope,lesson,bank); assertNotEquals(result.id,f.data().drafts.single().id); assertEquals(listOf(result),f.data().results)
    }
    @Test fun changingAcceptedAnswerRequiresNewValidationAndIndependentLessonsResumeSeparately() {
        val f=Fixture(); val bank=bank(); val lesson="G9-ALG-U01-L02"; val q=bank.forLesson(lesson).first()
        f.modern.startQa(f.scope,lesson,bank); f.modern.answerQa(f.scope,lesson,bank,q.answer); f.modern.submitQa(f.scope,lesson,bank)
        f.modern.answerQa(f.scope,lesson,bank,"changed"); assertEquals(ModernWrite.INVALID,f.modern.moveQa(f.scope,lesson,bank,1))
        f.modern.startQa(f.scope,"G9-GEO-U01-L01",bank); assertEquals(2,f.data().drafts.size)
        f.restart(); assertEquals("changed",f.data().drafts.single { it.lessonId==lesson }.answers[q.id])
    }
    @Test fun allUnitsHaveCorrectLazyListTargetsAndPathIsDeterministicInBothDirections() {
        for(stage in StudyStage.entries) {
            val catalog=SourceCurricula.forStage(stage); var index=4
            catalog.units.forEach { assertEquals(index,JourneyLayout.unitIndex(catalog,it.id)); index+=1+it.researches.size }
            assertNull(JourneyLayout.unitIndex(catalog,"missing"))
        }
        for(width in listOf(328f,361f,608f)) for(index in 0..94) {
            val ltr=JourneyLayout.center(width,200f,index,false); val rtl=JourneyLayout.center(width,200f,index,true)
            assertEquals(width,ltr+rtl,.001f); assertTrue(ltr in 100f..(width-100f)); assertEquals(ltr,JourneyLayout.center(width,200f,index+4,false),.001f)
        }
        assertEquals(listOf(0f,-1f,0f,1f,0f), (0..4).map(JourneyLayout::bias))
    }
    @Test fun bothStagesUseFiveModernTabsAndAllLegacyDestinationsRemainRoutable() {
        StudyStage.entries.forEach { assertEquals(listOf("study","tools","home","progress","profile"),Routes.studentTabs(it)) }
        assertEquals("study",Routes.selectedTab(Routes.Grade9Algebra)); assertEquals("study",Routes.selectedTab(Routes.Grade9Geometry))
        assertEquals("tools",Routes.selectedTab(Routes.Grade9Exercises)); assertEquals("progress",Routes.selectedTab("review"))
        assertEquals("profile",Routes.selectedTab(Routes.Avatar)); assertEquals("home",Routes.studentTabs(StudyStage.G9)[2])
    }
    @Test fun accountSwitchDuringCommitCannotPublishAnotherStudentsSuccessState() {
        val f=Fixture(); val a=f.scope.ownerId
        f.modern.selectAvatar(f.scope,"muadh")
        f.stored().afterCommit={ f.repo.createLocalProfile("B",StudyStage.G9,false) }
        assertEquals(ModernWrite.OWNER_CHANGED,f.modern.selectAvatar(f.scope,"lama"))
        assertNull(f.data().selectedAvatarId)
        f.repo.selectLocalProfile(a); assertEquals("lama",f.data().selectedAvatarId)
    }
    @Test fun dstBoundariesUseCalendarDaysRatherThanTwentyFourHourIntervals() {
        val format=java.text.SimpleDateFormat("yyyy-MM-dd HH:mm",java.util.Locale.ROOT).apply { timeZone=java.util.TimeZone.getTimeZone("UTC") }
        val a=format.parse("2026-10-24 22:30")!!.time
        val b=format.parse("2026-10-25 23:30")!!.time
        assertEquals(25*60*60*1000L,b-a)
        assertEquals(1L,LearningStreak.day(b,"Europe/Berlin")-LearningStreak.day(a,"Europe/Berlin"))
    }
    @Test fun bacProviderUsesExistingDemoQuestionsAndNeverConvertsThe63SourceReferences() {
        val f=Fixture(); f.repo.createLocalProfile("Bac",StudyStage.BAC,false)
        val state=f.repo.state.value; val provider=BacDemoExerciseSource(state)
        state.practiceSets.forEach { set -> assertEquals(set.questionIds,provider.forSet(set.id).map { it.id }) }
        SourceCurricula.forStage(StudyStage.BAC).researches.forEach { assertTrue(provider.forLesson(it.id).isEmpty()) }
        assertTrue(BacDemoExerciseSource(state.copy(user=state.user!!.copy(studyStage=StudyStage.G9))).forSet(state.practiceSets.first().id).isEmpty())
    }
    @Test fun profilePreferenceWriteFailuresAreReportedWithoutCrashingOrDeletingRecords() {
        val f=Fixture(); val before=f.repo.state.value
        f.files.getValue("enmath_demo").fail=true
        com.ahmadmol.enmath.core.AppViewModel(f.repo).theme(ThemeMode.Dark)
        assertEquals("LOCAL_STORAGE_UNAVAILABLE",f.repo.state.value.storageIssue)
        assertEquals(before.completed,f.repo.state.value.completed); assertEquals(before.product,f.repo.state.value.product)
    }
    @Test fun workedStepsKeepParagraphsWhenAnotherStepChangesAndAfterRestart() {
        val parts=listOf("المعطيات\n60 و84", "طريقة الحل\nالقاسم المشترك\nثم القسمة", "12 حزمة\nوالتحقق")
        assertEquals(parts,WorkedDraftResponse.decode(WorkedDraftResponse.encode(parts)))
        val modified=WorkedDraftResponse.decode(WorkedDraftResponse.encode(parts)).toMutableList().apply { this[2]="نتيجة جديدة" }
        assertEquals(parts[1],WorkedDraftResponse.decode(WorkedDraftResponse.encode(modified))[1])
        val f=Fixture(); val bank=bank(); val lesson="G9-ALG-U01-L02"
        f.modern.startQa(f.scope,lesson,bank); f.modern.answerQa(f.scope,lesson,bank,WorkedDraftResponse.encode(parts)); f.restart()
        assertEquals(parts,WorkedDraftResponse.decode(f.data().drafts.single().answers.values.single()))
    }
}
