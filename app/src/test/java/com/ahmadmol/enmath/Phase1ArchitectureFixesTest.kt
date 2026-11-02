package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.LearningRules
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.data.ProductSeed
import com.ahmadmol.enmath.core.data.activeStudentId
import com.ahmadmol.enmath.core.model.AccountType
import com.ahmadmol.enmath.core.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class Phase1ArchitectureFixesTest {

    @Test
    fun activeStudentIdIsDerivedFromUserIdentity() {
        val ahmedState = LearningState(user = User("أحمد محمد", "ahmed@example.com", AccountType.Student))
        assertEquals("ahmed", ahmedState.activeStudentId)

        val saraState = LearningState(user = User("سارة علي", "sara@example.com", AccountType.Student))
        assertEquals("student:sara@example.com", saraState.activeStudentId)

        val guestState = LearningState(user = User("ضيف", "", AccountType.Guest))
        assertEquals("guest", guestState.activeStudentId)
    }

    @Test
    fun learningRulesTopicMasteryRespectsStudentIdentity() {
        val seed = ProductSeed.seed()
        val ahmedState = LearningState(user = User("Ahmed", "ahmed@example.com", AccountType.Student), product = seed)
        val ahmedMastery = LearningRules.topic(ahmedState, "derivatives")
        assertNotNull(ahmedMastery)

        val saraState = LearningState(user = User("Sara", "sara@example.com", AccountType.Student), completed = emptySet(), product = seed)
        val saraMastery = LearningRules.topic(saraState, "derivatives")
        assertNotNull(saraMastery)
        assertEquals(0, saraMastery.lessons)
    }

    @Test
    fun assignmentFilteringUsesActiveStudentId() {
        val seed = ProductSeed.seed()
        val ahmedState = LearningState(user = User("Ahmed", "ahmed@example.com", AccountType.Student), product = seed)
        val ahmedClasses = seed.classes.filter { ahmedState.activeStudentId in it.studentIds }
        assertEquals(1, ahmedClasses.size)

        val saraState = LearningState(user = User("Sara", "sara@example.com", AccountType.Student), product = seed)
        val saraClasses = seed.classes.filter { saraState.activeStudentId in it.studentIds }
        assertEquals(0, saraClasses.size)
    }
}
