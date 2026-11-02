package com.ahmadmol.enmath

import com.ahmadmol.enmath.core.data.LocalLearner
import com.ahmadmol.enmath.core.localgems.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.profile.profileGemBalanceText
import org.junit.Assert.*
import org.junit.Test

class ProfileGemsPresentationTest {
    private val fresh = LocalLearner("00000000-0000-4000-8000-000000000001", "Student", StudyStage.BAC, false)
    private fun ledger(owner: String) = LocalGemsPolicy.award(GemAccount(owner, 0), GemReward.LESSON, "fixture", 1)
    private fun text(user: User?, ui: LocalGemsUiState) = profileGemBalanceText(ui, user, Language.English)

    @Test fun newLocalProfileWithBlankEmailDisplaysOnlyLedgerBalance() {
        val user = fresh.user()
        assertEquals("", user.email)
        val account = ledger(fresh.id)
        assertEquals("10 gems", text(user, LocalGemsUiState(fresh.id, account)))
        assertEquals("0 gems", text(user, LocalGemsUiState(fresh.id, GemAccount(fresh.id, 0))))
    }

    @Test fun legacyProfileUsesPreservedWalletKeyInsteadOfUuidOrContact() {
        val key = "legacy-wallet@example.test"
        val user = fresh.copy(legacyWalletKey = key).user().copy(email = "different@example.test")
        val account = ledger(key)
        assertEquals("10 gems", text(user, LocalGemsUiState(key, account)))
        assertEquals("Loading…", text(user, LocalGemsUiState(fresh.id, ledger(fresh.id))))
        assertEquals(account, ledger(key)) // presentation did not alter the ledger
    }

    @Test fun guestAndMissingSessionNeverDisplayPriorAccountOrRemainLoading() {
        val stale = LocalGemsUiState(fresh.id, ledger(fresh.id))
        assertEquals("N/A", text(fresh.copy(guest = true).user(), stale))
        assertEquals("N/A", text(null, stale))
        assertEquals("غير متاح", profileGemBalanceText(stale, fresh.copy(guest = true).user(), Language.Arabic))
    }

    @Test fun accountSwitchHidesPreviousBalanceUntilNewOwnerArrives() {
        val other = fresh.copy(id = "00000000-0000-4000-8000-000000000002").user()
        assertEquals("Loading…", text(other, LocalGemsUiState(fresh.id, ledger(fresh.id))))
        assertEquals("0 gems", text(other, LocalGemsUiState(other.localId, GemAccount(other.localId!!, 0))))
    }

    @Test fun mismatchedAccountPayloadIsHiddenEvenWhenEnvelopeMatches() {
        assertEquals("Loading…", text(fresh.user(), LocalGemsUiState(fresh.id, ledger("foreign"))))
    }

    @Test fun loadingAndStorageFailureAreStatusesWithoutCurrencySuffix() {
        assertEquals("Loading…", text(fresh.user(), LocalGemsUiState(fresh.id)))
        assertEquals("Error", text(fresh.user(), LocalGemsUiState(fresh.id, ledger(fresh.id), error = "STORAGE_UNAVAILABLE")))
    }
}
