package com.ahmadmol.enmath

import androidx.lifecycle.SavedStateHandle
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.features.courses.interactive.*
import org.junit.Assert.*
import org.junit.Test

class LimitExplorationTest {
    @Test fun engineValuesApproachFiveFromBothSidesButHoleIsUndefined() {
        assertNull(LimitExploration.value(3.0))
        assertEquals(5.0, LimitExploration.curve(3.0), 0.0)
        assertEquals(4.99, LimitExploration.value(2.99)!!, 1e-10)
        assertEquals(5.01, LimitExploration.value(3.01)!!, 1e-10)
        for (hundredth in 200..400) {
            val x = hundredth / 100.0
            if (hundredth != 300) assertEquals(x + 2, LimitExploration.value(x)!!, 1e-10)
        }
    }

    @Test fun plottedCoordinatesUseIndependentAxesAndCorrectIntercept() {
        assertEquals(0f, LimitExploration.fractionX(0.0), 0f)
        assertEquals(1f, LimitExploration.fractionX(6.5), 0f)
        assertEquals(1f, LimitExploration.fractionY(0.0), 0f)
        assertEquals(0f, LimitExploration.fractionY(9.3), 0f)
        assertEquals((3 / 6.5).toFloat(), LimitExploration.fractionX(3.0), 1e-6f)
        assertEquals((1 - 5 / 9.3).toFloat(), LimitExploration.fractionY(5.0), 1e-6f)
        assertEquals(2.0, LimitExploration.curve(0.0), 0.0)
    }

    @Test fun sliderRoundsAndClampsAndFormatsWithLatinDecimal() {
        assertEquals(3f, LimitExploration.sliderValue(2.999f), 0f)
        assertEquals(2f, LimitExploration.sliderValue(-1f), 0f)
        assertEquals(4f, LimitExploration.sliderValue(5f), 0f)
        assertEquals("4.70", LimitExploration.format(4.7))
    }

    @Test fun explorationSurvivesNavigationAndRestorationWithoutGrantingProgress() {
        val saved = SavedStateHandle()
        val vm = InteractiveLessonViewModel(saved)
        val blocks = LimitsLessonSequence.blocks(LearningState())
        vm.move(1, blocks)
        vm.exploreX(3f)
        vm.move(2, blocks)
        vm.move(1, blocks)
        assertEquals(3f, vm.explorationX.value, 0f)
        val restored = InteractiveLessonViewModel(SavedStateHandle(saved.keys().associateWith { saved.get<Any?>(it) }))
        assertEquals(3f, restored.explorationX.value, 0f)
        assertTrue(restored.answers.value.isEmpty())
        assertFalse(restored.claimCompletion(blocks))
        restored.exploreX(Float.NaN)
        assertEquals(3f, restored.explorationX.value, 0f)
    }
}
