package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.*
import com.ahmadmol.enmath.features.student.*
import com.ahmadmol.enmath.features.planner.GoalEditor
import com.ahmadmol.enmath.features.profile.ProductProfileScreen
import com.ahmadmol.enmath.features.library.LibraryScreen
import com.ahmadmol.enmath.features.explore.ExploreScreen
import com.ahmadmol.enmath.features.solver.*

// Preview fixtures only; no repository, wallet, learner file or production preferences are opened.
@Preview(name="360 ar light 100pct", widthDp=360, heightDp=850, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 ar light 150pct", widthDp=360, heightDp=850, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 ar light 200pct", widthDp=360, heightDp=850, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 ar dark 100pct", widthDp=360, heightDp=850, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360 ar dark 150pct", widthDp=360, heightDp=850, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360 ar dark 200pct", widthDp=360, heightDp=850, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360 en light 100pct", widthDp=360, heightDp=850, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 en light 150pct", widthDp=360, heightDp=850, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 en light 200pct", widthDp=360, heightDp=850, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360 en dark 100pct", widthDp=360, heightDp=850, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360 en dark 150pct", widthDp=360, heightDp=850, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360 en dark 200pct", widthDp=360, heightDp=850, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 ar light 100pct", widthDp=393, heightDp=850, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 ar light 150pct", widthDp=393, heightDp=850, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 ar light 200pct", widthDp=393, heightDp=850, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 ar dark 100pct", widthDp=393, heightDp=850, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 ar dark 150pct", widthDp=393, heightDp=850, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 ar dark 200pct", widthDp=393, heightDp=850, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 en light 100pct", widthDp=393, heightDp=850, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 en light 150pct", widthDp=393, heightDp=850, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 en light 200pct", widthDp=393, heightDp=850, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393 en dark 100pct", widthDp=393, heightDp=850, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 en dark 150pct", widthDp=393, heightDp=850, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393 en dark 200pct", widthDp=393, heightDp=850, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
annotation class StudentSurfaceMatrix

@Composable private fun PreviewStudent(stage: StudyStage, content: @Composable (LearningState) -> Unit) {
    val language = if (LocalConfiguration.current.locales[0].language == "ar") Language.Arabic else Language.English
    val state = LearningState(user=User("Preview", "", AccountType.Guest,
        localId="00000000-0000-0000-0000-000000000001", studyStage=stage),
        language=language, completed=emptySet(), product=ProductState())
    ENMathTheme(ThemeMode.System, language, student=true, stage=stage) { Surface { content(state) } }
}

@StudentSurfaceMatrix
@Composable private fun G9HomePreview() { PreviewStudent(StudyStage.G9) { s -> DualStageHome(s, {}, com.ahmadmol.enmath.core.localgems.LocalGemsUiState(), null) } }

@StudentSurfaceMatrix
@Composable private fun G9StudyPreview() { PreviewStudent(StudyStage.G9) { s -> SourceStudyScreen(StudyStage.G9, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9BookPreview() { PreviewStudent(StudyStage.G9) { s -> SourceBookScreen(StudyStage.G9, SourceCurricula.forStage(StudyStage.G9).books.first().id, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9UnitPreview() { PreviewStudent(StudyStage.G9) { s -> SourceUnitScreen(StudyStage.G9, SourceCurricula.forStage(StudyStage.G9).units.first().id, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9ResearchPreview() { PreviewStudent(StudyStage.G9) { s -> SourceResearchScreen(SourceCurricula.forStage(StudyStage.G9).researches.first().id, s, null, null, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9PendingPreview() { PreviewStudent(StudyStage.G9) { s -> SourceExercisePending(StudyStage.G9, SourceCurricula.forStage(StudyStage.G9).researches.first().id) } }

@StudentSurfaceMatrix
@Composable private fun G9ProgressPreview() { PreviewStudent(StudyStage.G9) { s -> SourceProgressScreen(s, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9ProfilePreview() { PreviewStudent(StudyStage.G9) { s -> ProductProfileScreen(s, false, {}, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9LibraryPreview() { PreviewStudent(StudyStage.G9) { s -> LibraryScreen(s, {}, {}) { SourceSavedLibrary(s, null, {}) } } }

@StudentSurfaceMatrix
@Composable private fun G9GraphPreview() { PreviewStudent(StudyStage.G9) { s -> ExploreScreen() } }

@StudentSurfaceMatrix
@Composable private fun G9SolverPreview() { PreviewStudent(StudyStage.G9) { s -> SolverWorkspace(remember { SolverViewModel(androidx.lifecycle.SavedStateHandle()) }, s, { _, _ -> }, {}) } }

@StudentSurfaceMatrix
@Composable private fun G9GoalEditorPreview() { PreviewStudent(StudyStage.G9) { s -> GoalEditor(s, null, PlanPeriod.WEEK, 1793188800000L, {}, { _, _ -> PlanWrite.INVALID }) } }

@StudentSurfaceMatrix
@Composable private fun G9ReviewPreview() { PreviewStudent(StudyStage.G9) { s -> SourceReviewHub(s, "all", {}) } }

@StudentSurfaceMatrix
@Composable private fun BACHomePreview() { PreviewStudent(StudyStage.BAC) { s -> DualStageHome(s, {}, com.ahmadmol.enmath.core.localgems.LocalGemsUiState(), null) } }

@StudentSurfaceMatrix
@Composable private fun BACStudyPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceStudyScreen(StudyStage.BAC, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACBookPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceBookScreen(StudyStage.BAC, SourceCurricula.forStage(StudyStage.BAC).books.first().id, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACUnitPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceUnitScreen(StudyStage.BAC, SourceCurricula.forStage(StudyStage.BAC).units.first().id, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACResearchPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceResearchScreen(SourceCurricula.forStage(StudyStage.BAC).researches.first().id, s, null, null, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACPendingPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceExercisePending(StudyStage.BAC, SourceCurricula.forStage(StudyStage.BAC).researches.first().id) } }

@StudentSurfaceMatrix
@Composable private fun BACProgressPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceProgressScreen(s, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACProfilePreview() { PreviewStudent(StudyStage.BAC) { s -> ProductProfileScreen(s, false, {}, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACLibraryPreview() { PreviewStudent(StudyStage.BAC) { s -> LibraryScreen(s, {}, {}) { SourceSavedLibrary(s, null, {}) } } }

@StudentSurfaceMatrix
@Composable private fun BACGraphPreview() { PreviewStudent(StudyStage.BAC) { s -> ExploreScreen() } }

@StudentSurfaceMatrix
@Composable private fun BACSolverPreview() { PreviewStudent(StudyStage.BAC) { s -> SolverWorkspace(remember { SolverViewModel(androidx.lifecycle.SavedStateHandle()) }, s, { _, _ -> }, {}) } }

@StudentSurfaceMatrix
@Composable private fun BACGoalEditorPreview() { PreviewStudent(StudyStage.BAC) { s -> GoalEditor(s, null, PlanPeriod.WEEK, 1793188800000L, {}, { _, _ -> PlanWrite.INVALID }) } }

@StudentSurfaceMatrix
@Composable private fun BACReviewPreview() { PreviewStudent(StudyStage.BAC) { s -> SourceReviewHub(s, "all", {}) } }
