package com.ahmadmol.enmath.features.profile

import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.Language
import com.ahmadmol.enmath.core.model.User
import com.ahmadmol.enmath.features.localgems.gemBalanceText

/** Read-only presentation of the bound ledger; never derives a wallet key from display data. */
internal fun profileGemBalanceText(ui: LocalGemsUiState, user: User?, language: Language): String {
    val owner = LocalGemsPolicy.owner(user)
    val balance = gemBalanceText(ui, owner, language)
    val ready = owner != null && ui.owner == owner && ui.account?.owner == owner && ui.error == null
    return if (ready) "$balance ${if (language == Language.Arabic) "جوهرة" else "gems"}" else balance
}
