package com.ahmadmol.enmath

import com.ahmadmol.enmath.features.reference.BottomNavLayoutDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomNavigationLayoutTest {

    @Test
    fun bottomNavRowHeightIsStrictlyBounded() {
        assertEquals(68f, BottomNavLayoutDefaults.BarContentHeight.value, 0.001f)
        assertEquals(78f, BottomNavLayoutDefaults.TotalBarHeight.value, 0.001f)
    }

    @Test
    fun itemContentFitsInsideBarAcrossFontScales() {
        // Font Scale 1.0x
        val h1 = BottomNavLayoutDefaults.estimatedContentHeightDp(1.0f)
        assertTrue("Content height at 1.0x font scale ($h1) must be <= 68dp", h1 <= 68f)

        // Font Scale 1.5x
        val h1_5 = BottomNavLayoutDefaults.estimatedContentHeightDp(1.5f)
        assertTrue("Content height at 1.5x font scale ($h1_5) must be <= 68dp", h1_5 <= 68f)

        // Font Scale 2.0x
        val h2 = BottomNavLayoutDefaults.estimatedContentHeightDp(2.0f)
        assertTrue("Content height at 2.0x font scale ($h2) must be <= 68dp", h2 <= 68f)
    }

    @Test
    fun centerFloatingButtonMetricsAreValid() {
        assertEquals(52f, BottomNavLayoutDefaults.CenterCircleSize.value, 0.001f)
        assertEquals(10f, BottomNavLayoutDefaults.FloatingTopPadding.value, 0.001f)
    }
}
