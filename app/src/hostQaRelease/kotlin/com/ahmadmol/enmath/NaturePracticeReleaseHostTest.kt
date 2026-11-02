package com.ahmadmol.enmath

import android.content.pm.ApplicationInfo
import com.ahmadmol.enmath.core.data.NaturePracticeBank
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.IOException

/** Actual Release unit-test resource package, without an Activity or any device. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "ar-rSY")
class NaturePracticeReleaseHostTest {
    @Test fun releaseIsNotDebuggableAndCannotLoadTheUnapprovedPilotAsset() {
        val context = RuntimeEnvironment.getApplication()
        assertEquals(0, context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)
        assertThrows(IOException::class.java) { NaturePracticeBank.load(context) }
        assertFalse(context.assets.list("content/g9").orEmpty().contains("nature_numbers_pilot.json"))
    }
}
