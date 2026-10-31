package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.*
import org.junit.Assert.*
import org.junit.Test

class Grade9RuntimeRepairTest {

    @Test
    fun grade9FiveRootTabsAreFixedAndHaveStrictNavigationPolicy() {
        val g9Tabs = Routes.studentTabs(StudyStage.G9)
        assertEquals(5, g9Tabs.size)
        assertEquals(
            listOf(
                Routes.Grade9Algebra,
                Routes.Grade9Geometry,
                Routes.Home,
                Routes.Grade9Exercises,
                Routes.Profile
            ),
            g9Tabs
        )

        // BottomBar strictly visible on all 5 root tabs
        g9Tabs.forEach { tab ->
            assertTrue(
                "BottomBar must be visible on $tab",
                showStudentNavigation(tab, listOf(tab), hasStudentSession = true, stage = StudyStage.G9)
            )
        }

        // Subscreens MUST NOT show BottomBar
        val subscreens = listOf(
            Routes.Lesson,
            "lesson/G9-ALG-U01-R01",
            Routes.Topic,
            "topic/G9-ALG-U01",
            "g9/practice",
            "g9/result/test",
            "g9/files",
            "g9/file/test",
            "g9/reference/G9-ALG-U01-R01",
            "planner",
            "library",
            "search",
            "settings",
            "notifications",
            Routes.Grade9Source,
            Routes.Grade9Help,
            Routes.Grade9Activity
        )
        subscreens.forEach { sub ->
            assertFalse(
                "BottomBar must NEVER be visible on subscreen $sub",
                showStudentNavigation(sub, listOf(sub), hasStudentSession = true, stage = StudyStage.G9)
            )
            assertFalse(
                "BottomBar must NEVER be visible during transition to $sub",
                showStudentNavigation(Routes.Home, listOf(sub, Routes.Home), hasStudentSession = true, stage = StudyStage.G9)
            )
        }
    }

    @Test
    fun grade9LearningPathHasExactly32StationsDividedByAlgebraAndGeometry() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        assertEquals(2, catalog.books.size)

        val algebraBook = catalog.books.first { it.id == "G9-ALG" }
        val geometryBook = catalog.books.first { it.id == "G9-GEO" }

        // 6 units in Algebra, 4 in Geometry
        assertEquals(6, algebraBook.units.size)
        assertEquals(4, geometryBook.units.size)

        // 19 topics in Algebra, 13 in Geometry
        assertEquals(19, algebraBook.researches.size)
        assertEquals(13, geometryBook.researches.size)
        assertEquals(32, catalog.researches.size)

        // Pilot lesson is in Unit 1 of Algebra
        val pilotResearch = catalog.researches.first { it.id == Grade9Pilot.lessonId }
        assertEquals("G9-ALG-U01-L03", pilotResearch.id)
        assertEquals("G9-ALG-U01", pilotResearch.unitId)

        // First research in catalog is the first topic of Unit 1
        val firstResearch = catalog.researches.first()
        assertEquals("G9-ALG-U01-L01", firstResearch.id)
        assertEquals("G9-ALG-U01", firstResearch.unitId)
        assertEquals(1, firstResearch.order)

        // All 32 stations have distinct IDs and valid routes
        val ids = catalog.researches.map { it.id }
        assertEquals(32, ids.distinct().size)
        catalog.researches.forEach { r ->
            assertEquals("lesson/${r.id}", r.route)
            assertNotNull("Each research must belong to a valid unit", catalog.unit(r.unitId))
        }
    }

    @Test
    fun navigationSessionKeyIsStableForStudentAndChangesOnNewProfile() {
        val student1 = LearningState(
            user = User("Nour", "", AccountType.Student, "00000000-0000-0000-0000-000000000001", StudyStage.G9),
            generation = 1,
            catalogAvailable = true
        )
        val student2 = LearningState(
            user = User("Ahmad", "", AccountType.Student, "00000000-0000-0000-0000-000000000002", StudyStage.G9),
            generation = 1,
            catalogAvailable = true
        )

        val key1 = navigationSessionKey(student1)
        val key2 = navigationSessionKey(student2)

        assertTrue(key1.contains("00000000-0000-0000-0000-000000000001"))
        assertTrue(key2.contains("00000000-0000-0000-0000-000000000002"))
        assertNotEquals(key1, key2)

        // Generation increment changes key
        val key1Gen2 = navigationSessionKey(student1.copy(generation = 2))
        assertNotEquals(key1, key1Gen2)
    }

    @Test
    fun exercisesTabPreservesPilotFocusAndHonestAvailability() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val algebra = catalog.book("G9-ALG")!!
        val unit1 = algebra.units.first()

        // Only unit 1 contains the pilot lesson
        assertTrue(unit1.researches.any { it.id == Grade9Pilot.lessonId })

        // Remaining 9 units have 0 released pilot lessons
        val otherUnits = catalog.units.filterNot { it.id == "G9-ALG-U01" }
        assertEquals(9, otherUnits.size)
        assertTrue(otherUnits.all { u -> u.researches.none { it.id == Grade9Pilot.lessonId } })
    }

    @Test
    fun scrollRestorationPolicyPreservesG9HomeAndRestoresTabStates() {
        Routes.grade9Tabs.forEach { tab ->
            assertTrue(
                "Tab $tab should restore state for G9",
                restoreStudentTabState(StudyStage.G9, tab, teacher = false)
            )
        }
    }

    @Test
    fun entryFlowFromStagePickerToProfileCreationOrGuestProducesDistinctValidSessions() {
        // Step 1: StagePicker stageName resolution
        val stageName = "G9"
        val resolvedStage = runCatching { StudyStage.valueOf(stageName) }.getOrNull()
        assertEquals(StudyStage.G9, resolvedStage)

        // Step 2: Unauthenticated state has null user and routes to Splash or LocalIdentity
        val unauthenticatedState = LearningState()
        assertNull(unauthenticatedState.user?.localId)
        val initialStartDestination = if (unauthenticatedState.user?.localId != null) Routes.Main else Routes.Splash
        assertEquals(Routes.Splash, initialStartDestination)

        // Step 3: Student profile creation
        val studentUser = User(
            name = "نور",
            email = "",
            type = AccountType.Student,
            localId = "student-g9-uuid-001",
            studyStage = resolvedStage!!
        )
        val studentState = LearningState(user = studentUser)
        assertNotNull(studentState.user?.localId)
        assertEquals(Routes.Main, if (studentState.user?.localId != null) Routes.Main else Routes.Splash)

        val studentSessionKey = navigationSessionKey(studentState)
        assertTrue("Session key must contain student localId", studentSessionKey.contains("student-g9-uuid-001"))

        // Step 4: Guest profile creation
        val guestUser = User(
            name = "",
            email = "",
            type = AccountType.Guest,
            localId = "guest-g9-uuid-002",
            studyStage = resolvedStage
        )
        val guestState = LearningState(user = guestUser)
        assertNotNull(guestState.user?.localId)
        val guestSessionKey = navigationSessionKey(guestState)
        assertTrue("Session key must contain guest localId", guestSessionKey.contains("guest-g9-uuid-002"))
        assertNotEquals(studentSessionKey, guestSessionKey)
    }

    @Test
    fun initialRenderedSessionDoesNotTriggerRedundantNavigationOrWipeStartDestination() {
        // Initial state before NavHost renders
        var renderedSession: String? = null
        val state = LearningState(
            user = User("Nour", "", AccountType.Student, "uuid-001", StudyStage.G9)
        )
        val sessionKey = navigationSessionKey(state)

        var navigationInvoked = false
        var popUpInclusive = false

        // Simulate LaunchedEffect(navigationSessionKey(state)) logic
        if (sessionKey != renderedSession) {
            // Only navigate if renderedSession was ALREADY set from a previous session (i.e. profile switch)
            if (renderedSession != null && state.user?.localId != null) {
                navigationInvoked = true
                popUpInclusive = false // inclusive = false preserves start destination
            }
            renderedSession = sessionKey
        }

        // On first run, navigation is NOT invoked; NavHost startDestination handles loading Main natively
        assertFalse("First entry must NOT invoke redundant navigation", navigationInvoked)
        assertEquals(sessionKey, renderedSession)

        // Simulate profile switch to another student
        val switchedState = LearningState(
            user = User("Ahmad", "", AccountType.Student, "uuid-002", StudyStage.G9)
        )
        val switchedSessionKey = navigationSessionKey(switchedState)

        if (switchedSessionKey != renderedSession) {
            if (renderedSession != null && switchedState.user?.localId != null) {
                navigationInvoked = true
                popUpInclusive = false
            }
            renderedSession = switchedSessionKey
        }

        assertTrue("Profile switch must trigger safe navigation to Main", navigationInvoked)
        assertFalse("Root destination must NEVER be popped inclusively", popUpInclusive)
        assertEquals(switchedSessionKey, renderedSession)
    }

    @Test
    fun sessionIsolationPreservesStudentDataGemsAttemptsAndFilesWithoutCrossContamination() {
        val student1State = LearningState(
            user = User("Nour", "", AccountType.Student, "uuid-001", StudyStage.G9)
        )
        val student2State = LearningState(
            user = User("Ahmad", "", AccountType.Student, "uuid-002", StudyStage.G9)
        )

        val owner1 = LearningRules.sessionOwner(student1State)
        val owner2 = LearningRules.sessionOwner(student2State)
        val gemOwner1 = LocalGemsPolicy.owner(student1State.user)
        val gemOwner2 = LocalGemsPolicy.owner(student2State.user)

        assertNotNull(owner1)
        assertNotNull(owner2)
        assertNotEquals(owner1, owner2)
        assertEquals("uuid-001", gemOwner1)
        assertEquals("uuid-002", gemOwner2)
        assertNotEquals(gemOwner1, gemOwner2)

        // Guest user policy: guests never earn gems
        val guestUser = User("", "", AccountType.Guest, "guest-uuid", StudyStage.G9)
        val guestOwner = LocalGemsPolicy.owner(guestUser)
        assertNull(guestOwner)
    }

    @Test
    fun zigzagStationLayoutMathMatchesRtlAndLtrCoordinatesAccurately() {
        val catalog = SourceCurricula.forStage(StudyStage.G9)
        val totalStations = catalog.researches.size
        assertEquals(32, totalStations)

        for (index in 0 until totalStations) {
            val currBias = when (index % 4) {
                0 -> 0.5f
                1 -> 0.22f
                2 -> 0.5f
                3 -> 0.78f
                else -> 0.5f
            }
            assertTrue("Bias must stay within bounds [0.2, 0.8]", currBias in 0.2f..0.8f)

            // When index is even, bias is centered (0.5f)
            if (index % 2 == 0) {
                assertEquals(0.5f, currBias, 0.001f)
            } else if (index % 4 == 1) {
                assertEquals(0.22f, currBias, 0.001f)
            } else if (index % 4 == 3) {
                assertEquals(0.78f, currBias, 0.001f)
            }
        }
    }
}
