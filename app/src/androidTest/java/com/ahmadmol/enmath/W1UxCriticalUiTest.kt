package com.ahmadmol.enmath

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.profile.ProductProfileScreen
import com.ahmadmol.enmath.features.student.StudentLearningHome
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** Composable-only fixtures. Does not create, clear or migrate any application data. */
class W1UxCriticalUiTest {
    @get:Rule val ui = createComposeRule()
    private val student = User("Local", "", AccountType.Student,
        "00000000-0000-4000-8000-000000000021", StudyStage.BAC)

    @Test fun profileShowsLedgerThenGuestStatusAndKeepsWalletRoute() {
        val state = mutableStateOf(LearningState(user = student, language = Language.English))
        val owner = LocalGemsPolicy.owner(student)!!
        val account = LocalGemsPolicy.award(GemAccount(owner, 0), GemReward.LESSON, "fixture", 1)
        var route = ""
        ui.setContent {
            ENMathTheme(ThemeMode.Light, Language.English, student = true) {
                ProductProfileScreen(state.value, false, { route = it }, {},
                    gems = LocalGemsUiState(owner, account))
            }
        }
        ui.onNodeWithTag("profile-gems").assert(hasText("10 gems", substring = true))
            .performScrollTo().performClick()
        ui.runOnIdle { assertEquals("gems/wallet", route); state.value = state.value.copy(user = student.copy(type = AccountType.Guest)) }
        ui.onNodeWithTag("profile-gems").assert(hasText("N/A", substring = true))
        ui.onNodeWithText("Loading…", substring = true).assertDoesNotExist()
    }

    @Test fun homeHasNoSyntheticRankOrChestAndExistingWalletActionStillWorks() {
        var route = ""
        ui.setContent {
            ENMathTheme(ThemeMode.Light, Language.English, student = true) {
                StudentLearningHome(LearningState(user = student, language = Language.English), { route = it })
            }
        }
        ui.onNodeWithText("Rank 25").assertDoesNotExist()
        ui.onNodeWithText("Gems Chest Reward").assertDoesNotExist()
        ui.onNodeWithText("🎁").assertDoesNotExist()
        ui.onNodeWithTag("home-gems-badge").performScrollTo().performClick()
        ui.runOnIdle { assertEquals("gems/wallet", route) }
    }
}
