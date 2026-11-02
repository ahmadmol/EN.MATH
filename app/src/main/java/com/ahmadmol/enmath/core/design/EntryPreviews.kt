package com.ahmadmol.enmath.core.design

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.auth.LocalOnboarding
import com.ahmadmol.enmath.features.onboarding.SplashScreen

@Preview(name="S0 Splash", widthDp=393, heightDp=865)
@Composable private fun SplashReferencePreview() { ENMathTheme(ThemeMode.Light, Language.Arabic, true) { SplashScreen {} } }
@Preview(name="S1 Learn", widthDp=393, heightDp=865)
@Composable private fun LearnReferencePreview() { ENMathTheme(ThemeMode.Light, Language.Arabic, true) { LocalOnboarding(0) {} } }
@Preview(name="S2 Study", widthDp=393, heightDp=865)
@Composable private fun StudyReferencePreview() { ENMathTheme(ThemeMode.Light, Language.Arabic, true) { LocalOnboarding(1) {} } }
@Preview(name="S3 Progress", widthDp=393, heightDp=865)
@Composable private fun ProgressReferencePreview() { ENMathTheme(ThemeMode.Light, Language.Arabic, true) { LocalOnboarding(2) {} } }
@Preview(name="S4 Stage", widthDp=393, heightDp=865)
@Composable private fun StageReferencePreview() { ENMathTheme(ThemeMode.Light, Language.Arabic, true) { EntryBackdrop { ScreenPage(narrow=true) { StagePicker {} } } } }

@Composable private fun PreviewEntry(stage: StudyStage, dark: Boolean=false, english: Boolean=false) {
    ENMathTheme(if(dark) ThemeMode.Dark else ThemeMode.Light, if(english) Language.English else Language.Arabic, true) {
        EntryBackdrop { ScreenPage(narrow=true) { StageEntryContent(stage, back={}, guest={}, create={}, saved={}) } }
    }
}
@Preview(name="S5 G9", widthDp=393, heightDp=865)
@Preview(name="S5 G9 360 150%", widthDp=360, heightDp=800, fontScale=1.5f)
@Preview(name="S5 G9 360 200%", widthDp=360, heightDp=800, fontScale=2f)
@Composable private fun Grade9ReferencePreview() { PreviewEntry(StudyStage.G9) }
@Preview(name="S6 BAC", widthDp=393, heightDp=865)
@Preview(name="S6 BAC 360 200%", widthDp=360, heightDp=800, fontScale=2f)
@Composable private fun BacReferencePreview() { PreviewEntry(StudyStage.BAC) }
@Preview(name="S6 BAC dark", widthDp=393, heightDp=865)
@Composable private fun DarkEntryPreview() { PreviewEntry(StudyStage.BAC, dark=true) }
@Preview(name="S5 G9 English", widthDp=393, heightDp=865)
@Composable private fun EnglishEntryPreview() { PreviewEntry(StudyStage.G9, english=true) }
