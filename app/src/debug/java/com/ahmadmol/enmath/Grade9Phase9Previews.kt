package com.ahmadmol.enmath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*

/**
 * Phase 9 Final Acceptance Visual Previews:
 * Covers all requested states, subjects, paths, and accessibility font scale configurations.
 */

private fun previewState(dark: Boolean = false): LearningState {
    return LearningState(
        user = User(
            name = "نور",
            email = "",
            type = AccountType.Student,
            localId = "00000000-0000-0000-0000-000000000001",
            studyStage = StudyStage.G9
        ),
        language = Language.Arabic,
        theme = if (dark) ThemeMode.Dark else ThemeMode.Light,
        onboarded = true
    )
}

private fun previewRepo(subject: Grade9Subject?, chosen: Boolean): Grade9StudyRepository {
    val prefs = Grade9PreviewPreferences()
    val state = previewState()
    val repo = Grade9StudyRepository({ prefs }) { state }
    val scope = StudentCatalog.scope(state)!!
    if (subject != null && chosen) {
        repo.selectSubject(scope, subject)
    }
    return repo
}

private fun previewNatureRepo(answersCount: Int = 0): NaturePracticeRepository {
    val prefs = Grade9PreviewPreferences()
    val state = previewState()
    val repo = NaturePracticeRepository({ prefs }) { state }
    val scope = StudentCatalog.scope(state)!!
    if (answersCount > 0) {
        val good = listOf("2", "٣/٨", "true", "غير منتهية|دورية", "٥|integer", "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational")
        repo.start(scope)
        for (i in 0 until answersCount) {
            val qId = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"
            repo.submit(scope, qId, good[i])
            if (i < answersCount - 1) repo.move(scope, i + 1)
        }
        if (answersCount == 6) {
            repo.finish(scope)
        }
    }
    return repo
}

// 1. First-time Subject Choice (Light & Dark)
@Preview(name = "1. First-Time Subject Choice (Light)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9FirstTimeChoiceLight() {
    val state = previewState(dark = false)
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = null, chosen = false)
            )
        }
    }
}

@Preview(name = "1. First-Time Subject Choice (Dark)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9FirstTimeChoiceDark() {
    val state = previewState(dark = true)
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = null, chosen = false)
            )
        }
    }
}

// 2. Home after Algebra Choice (Light & Dark)
@Preview(name = "2. Home after Algebra Choice (Light)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9HomeAlgebraLight() {
    val state = previewState(dark = false)
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = Grade9Subject.ALGEBRA, chosen = true),
                naturePractice = previewNatureRepo(answersCount = 0)
            )
        }
    }
}

@Preview(name = "2. Home after Algebra Choice (Dark)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9HomeAlgebraDark() {
    val state = previewState(dark = true)
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = Grade9Subject.ALGEBRA, chosen = true),
                naturePractice = previewNatureRepo(answersCount = 0)
            )
        }
    }
}

// 3. Home after Geometry Choice (Light & Dark)
@Preview(name = "3. Home after Geometry Choice (Light)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9HomeGeometryLight() {
    val state = previewState(dark = false)
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = Grade9Subject.GEOMETRY, chosen = true)
            )
        }
    }
}

@Preview(name = "3. Home after Geometry Choice (Dark)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9HomeGeometryDark() {
    val state = previewState(dark = true)
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = previewRepo(subject = Grade9Subject.GEOMETRY, chosen = true)
            )
        }
    }
}

// 4. Algebra Full Path with all 6 Units
@Preview(name = "4. Algebra Full Path (6 Units)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9AlgebraFullPath() {
    val state = previewState(dark = false)
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9SubjectScreen(state = state, geometry = false, open = {})
        }
    }
}

// 5. Geometry Full Path with all 4 Units
@Preview(name = "5. Geometry Full Path (4 Units)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9GeometryFullPath() {
    val state = previewState(dark = false)
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Grade9SubjectScreen(state = state, geometry = true, open = {})
        }
    }
}

// 6. Five Lesson States Preview (Not Started, Started, Completed, Needs Review, Under Review)
@Preview(name = "6. Five Station States (Light)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9StationStates() {
    val catalog = SourceCurricula.forStage(StudyStage.G9)
    val natureResearch = catalog.researches.first { it.id == "G9-ALG-U01-L01" }
    val underReviewResearch = catalog.researches.first { it.id == "G9-ALG-U01-L02" }

    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Not Started (Available)
                PathStationNode(
                    research = natureResearch,
                    stationIndex = 1,
                    lastVisited = false,
                    open = {},
                    bias = 0.5f,
                    progressStatus = LessonProgressStatus.NOT_STARTED,
                    isSuggested = true
                )

                // 2. In Progress / Started
                PathStationNode(
                    research = natureResearch,
                    stationIndex = 1,
                    lastVisited = false,
                    open = {},
                    bias = 0.5f,
                    progressStatus = LessonProgressStatus.STARTED
                )

                // 3. Practice Completed
                PathStationNode(
                    research = natureResearch,
                    stationIndex = 1,
                    lastVisited = false,
                    open = {},
                    bias = 0.5f,
                    progressStatus = LessonProgressStatus.PRACTICE_COMPLETED
                )

                // 4. Needs Review
                PathStationNode(
                    research = natureResearch,
                    stationIndex = 1,
                    lastVisited = false,
                    open = {},
                    bias = 0.5f,
                    progressStatus = LessonProgressStatus.NEEDS_REVIEW
                )

                // 5. Content Unavailable / Under Review
                PathStationNode(
                    research = underReviewResearch,
                    stationIndex = 2,
                    lastVisited = false,
                    open = {},
                    bias = 0.5f,
                    progressStatus = LessonProgressStatus.NOT_STARTED
                )
            }
        }
    }
}

// 7. Progress Card after answering 2 questions
@Preview(name = "7. Progress Card (After 2 Questions)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9ProgressCardTwoQuestions() {
    val natureRepo = previewNatureRepo(answersCount = 2)
    val state = previewState()
    val scope = StudentCatalog.scope(state)!!
    val natureData = (natureRepo.read(scope) as NatureRead.Ready).data
    val progress = StudentLearningProgressEngine.computeGrade9Progress(natureData)

    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Box(Modifier.padding(16.dp)) {
                Grade9LearningProgressCard(progress = progress, open = {})
            }
        }
    }
}

// 8. Progress Card after completing practice (6 questions)
@Preview(name = "8. Progress Card (Practice Completed)", showBackground = true, widthDp = 380)
@Composable
fun PreviewPhase9ProgressCardCompleted() {
    val natureRepo = previewNatureRepo(answersCount = 6)
    val state = previewState()
    val scope = StudentCatalog.scope(state)!!
    val natureData = (natureRepo.read(scope) as NatureRead.Ready).data
    val progress = StudentLearningProgressEngine.computeGrade9Progress(natureData)

    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface {
            Box(Modifier.padding(16.dp)) {
                Grade9LearningProgressCard(progress = progress, open = {})
            }
        }
    }
}

// 9. Accessibility Font Scale Variations (100%, 150%, 200%)
@Preview(name = "9a. First-Time Choice (Scale 150%)", showBackground = true, widthDp = 380, fontScale = 1.5f)
@Composable
fun PreviewPhase9Scale150FirstTimeChoice() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f)) {
        PreviewPhase9FirstTimeChoiceLight()
    }
}

@Preview(name = "9b. First-Time Choice (Scale 200%)", showBackground = true, widthDp = 380, fontScale = 2.0f)
@Composable
fun PreviewPhase9Scale200FirstTimeChoice() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 2.0f)) {
        PreviewPhase9FirstTimeChoiceLight()
    }
}

@Preview(name = "9c. Home Algebra (Scale 150%)", showBackground = true, widthDp = 380, fontScale = 1.5f)
@Composable
fun PreviewPhase9Scale150HomeAlgebra() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f)) {
        PreviewPhase9HomeAlgebraLight()
    }
}

@Preview(name = "9d. Home Algebra (Scale 200%)", showBackground = true, widthDp = 380, fontScale = 2.0f)
@Composable
fun PreviewPhase9Scale200HomeAlgebra() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 2.0f)) {
        PreviewPhase9HomeAlgebraLight()
    }
}
