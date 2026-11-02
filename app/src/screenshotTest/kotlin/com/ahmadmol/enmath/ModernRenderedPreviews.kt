package com.ahmadmol.enmath

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.android.tools.screenshot.PreviewTest
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.core.navigation.Routes
import com.ahmadmol.enmath.features.student.*

/** Real production components, isolated in-memory fixtures. Never opens a learner file or wallet. */
@Composable private fun ModernPreview(stage: StudyStage, scene: String) {
    val configuration = LocalConfiguration.current
    val language = if (configuration.locales[0].language == "ar") Language.Arabic else Language.English
    val repository = remember(stage) {
        val files = mutableMapOf<String, Grade9PreviewPreferences>()
        LocalDemoRepository(false) { files.getOrPut(it) { Grade9PreviewPreferences() } }.also { repo ->
            repo.createLocalProfile("PREVIEW · معاينة", stage, false)
            repo.studentExperience.selectAvatar(StudentCatalog.scope(repo.state.value)!!, "lama")
        }
    }
    LaunchedEffect(language) { repository.setLanguage(language) }
    val state by repository.state.collectAsState()
    val wide = configuration.screenWidthDp >= 768
    ENMathTheme(ThemeMode.System, language, true, stage, "lama") {
        Scaffold(bottomBar = { if (!wide) ModernStudentNavigation(Routes.Home) {} }) { padding ->
            Row(Modifier.fillMaxSize().padding(padding)) {
                if (wide) ModernStudentRail(Routes.Home) {}
                Box(Modifier.weight(1f)) {
                    when (scene) {
                        "tools" -> ModernToolsScreen(state) {}
                        "profile" -> ModernProfileScreen(state, repository, LocalGemsUiState(), {}, {}, {}, {})
                        else -> ModernJourneyHome(state, {}, LocalGemsUiState(), repository)
                    }
                }
            }
        }
    }
}

@PreviewTest @PolishMatrix @Composable fun ModernG9Home() = ModernPreview(StudyStage.G9, "home")
@PreviewTest @PolishMatrix @Composable fun ModernBacHome() = ModernPreview(StudyStage.BAC, "home")
@PreviewTest @PolishMatrix @Composable fun ModernTools() = ModernPreview(StudyStage.G9, "tools")
@PreviewTest @PolishMatrix @Composable fun ModernProfile() = ModernPreview(StudyStage.BAC, "profile")
