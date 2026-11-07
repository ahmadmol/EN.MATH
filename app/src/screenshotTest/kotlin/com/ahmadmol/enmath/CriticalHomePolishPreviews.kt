package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.student.*

@Preview(name = "360_ar_dark_200", widthDp = 360, heightDp = 1400, locale = "ar", fontScale = 2f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "360_ar_light_200", widthDp = 360, heightDp = 1400, locale = "ar", fontScale = 2f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "393_ar_light_100", widthDp = 393, heightDp = 1400, locale = "ar", fontScale = 1f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "393_en_dark_150", widthDp = 393, heightDp = 1400, locale = "en", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "800_ar_dark_200", widthDp = 800, heightDp = 1400, locale = "ar", fontScale = 2f, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class CriticalHomeMatrix

/** Isolated fixture. Does not open a learner file or wallet. */
@Composable
private fun CriticalHomePreview() {
    val configuration = LocalConfiguration.current
    val language = if (configuration.locales[0].language == "ar") Language.Arabic else Language.English
    val repository = remember {
        val files = mutableMapOf<String, Grade9PreviewPreferences>()
        LocalDemoRepository(false) { files.getOrPut(it) { Grade9PreviewPreferences() } }.also { repo ->
            repo.createLocalProfile("PREVIEW · معاينة", StudyStage.G9, false)
            repo.studentExperience.selectAvatar(StudentCatalog.scope(repo.state.value)!!, "lama")
        }
    }
    LaunchedEffect(language) { repository.setLanguage(language) }
    val state by repository.state.collectAsState()
    val wide = configuration.screenWidthDp >= 768
    ENMathTheme(ThemeMode.System, language, true, StudyStage.G9, "lama") {
        Scaffold(bottomBar = { if (!wide) ModernStudentNavigation(Routes.Home) {} }) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (wide) ModernStudentRail(Routes.Home) {}
                Box(Modifier.weight(1f)) { ModernJourneyHome(state, {}, LocalGemsUiState(), repository) }
            }
        }
    }
}

@PreviewTest
@CriticalHomeMatrix
@Composable
fun CriticalG9Home() = CriticalHomePreview()
