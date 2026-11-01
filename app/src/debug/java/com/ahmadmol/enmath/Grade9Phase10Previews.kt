package com.ahmadmol.enmath

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * Phase 10A Final Acceptance Visual Previews: Complete Learning Unit (Nature of Numbers).
 * Covers:
 * 1. Lesson Explanation Screen (Light / Dark, 100% / 150% / 200%).
 * 2. Interactive Practice Screen with direct lesson explanation linkage.
 * 3. Practice Result Screen with lesson explanation review action.
 * 4. Home Roadmap with progress card and dual lesson / practice actions.
 */

private fun phase10PreviewState(dark: Boolean = false): LearningState {
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

private fun phase10NatureRepo(completed: Boolean = false): Pair<NaturePracticeRepository, String?> {
    val prefs = Grade9PreviewPreferences()
    val state = phase10PreviewState()
    val repo = NaturePracticeRepository({ prefs }) { state }
    val scope = StudentCatalog.scope(state)!!
    repo.start(scope)
    var attemptId: String? = null
    if (completed) {
        val good = listOf(
            "2",
            "٣/٨",
            "true",
            "غير منتهية|دورية",
            "٥|integer",
            "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational"
        )
        for (i in 0..5) {
            val qId = "G9-A-U01-L01-Q${(i + 1).toString().padStart(2, '0')}"
            repo.submit(scope, qId, good[i])
            if (i < 5) repo.move(scope, i + 1)
        }
        val fin = repo.finish(scope)
        attemptId = fin.resultId
    }
    return repo to attemptId
}

private fun phase10StudyRepo(): Grade9StudyRepository {
    val prefs = Grade9PreviewPreferences()
    val state = phase10PreviewState()
    val repo = Grade9StudyRepository({ prefs }) { state }
    val scope = StudentCatalog.scope(state)!!
    repo.selectSubject(scope, Grade9Subject.ALGEBRA)
    return repo
}

@Preview(name = "Phase 10A - Lesson Explanation - Light", group = "Lesson Explanation", showBackground = true, widthDp = 390, heightDp = 1800)
@Composable
fun PreviewPhase10LessonExplanationLight() {
    val state = phase10PreviewState(dark = false)
    val (natureRepo, _) = remember { phase10NatureRepo(completed = false) }
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NatureLessonScreen(
                state = state,
                repository = natureRepo,
                open = {},
                goBack = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Lesson Explanation - Dark", group = "Lesson Explanation", showBackground = true, widthDp = 390, heightDp = 1800)
@Composable
fun PreviewPhase10LessonExplanationDark() {
    val state = phase10PreviewState(dark = true)
    val (natureRepo, _) = remember { phase10NatureRepo(completed = true) }
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NatureLessonScreen(
                state = state,
                repository = natureRepo,
                open = {},
                goBack = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Lesson Explanation - Scale 150%", group = "Lesson Explanation", showBackground = true, widthDp = 390, heightDp = 2400)
@Composable
fun PreviewPhase10LessonExplanationScale150() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 1.5f)) {
        PreviewPhase10LessonExplanationLight()
    }
}

@Preview(name = "Phase 10A - Lesson Explanation - Scale 200%", group = "Lesson Explanation", showBackground = true, widthDp = 390, heightDp = 3000)
@Composable
fun PreviewPhase10LessonExplanationScale200() {
    CompositionLocalProvider(LocalDensity provides Density(1f, 2.0f)) {
        PreviewPhase10LessonExplanationDark()
    }
}

@Preview(name = "Phase 10A - Practice Screen with Lesson Link - Light", group = "Practice", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PreviewPhase10PracticeScreenLight() {
    val state = phase10PreviewState(dark = false)
    val (natureRepo, _) = remember { phase10NatureRepo(completed = false) }
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NaturePracticeScreen(
                state = state,
                repository = natureRepo,
                openResult = {},
                goBack = {},
                openLesson = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Practice Screen with Lesson Link - Dark", group = "Practice", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PreviewPhase10PracticeScreenDark() {
    val state = phase10PreviewState(dark = true)
    val (natureRepo, _) = remember { phase10NatureRepo(completed = false) }
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NaturePracticeScreen(
                state = state,
                repository = natureRepo,
                openResult = {},
                goBack = {},
                openLesson = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Practice Result Screen - Light", group = "Result", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PreviewPhase10ResultScreenLight() {
    val state = phase10PreviewState(dark = false)
    val (natureRepo, attemptId) = remember { phase10NatureRepo(completed = true) }
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NaturePracticeResultScreen(
                state = state,
                repository = natureRepo,
                id = attemptId.orEmpty(),
                backToPath = {},
                retry = {},
                openLesson = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Practice Result Screen - Dark", group = "Result", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PreviewPhase10ResultScreenDark() {
    val state = phase10PreviewState(dark = true)
    val (natureRepo, attemptId) = remember { phase10NatureRepo(completed = true) }
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NaturePracticeResultScreen(
                state = state,
                repository = natureRepo,
                id = attemptId.orEmpty(),
                backToPath = {},
                retry = {},
                openLesson = {}
            )
        }
    }
}

@Preview(name = "Phase 10A - Home Progress Card with Dual Actions - Light", group = "Home", showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
fun PreviewPhase10HomeProgressLight() {
    val state = phase10PreviewState(dark = false)
    val studyRepo = remember { phase10StudyRepo() }
    val (natureRepo, _) = remember { phase10NatureRepo(completed = true) }
    ENMathTheme(ThemeMode.Light, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = studyRepo,
                naturePractice = natureRepo
            )
        }
    }
}

@Preview(name = "Phase 10A - Home Progress Card with Dual Actions - Dark", group = "Home", showBackground = true, widthDp = 390, heightDp = 1000)
@Composable
fun PreviewPhase10HomeProgressDark() {
    val state = phase10PreviewState(dark = true)
    val studyRepo = remember { phase10StudyRepo() }
    val (natureRepo, _) = remember { phase10NatureRepo(completed = false) }
    ENMathTheme(ThemeMode.Dark, Language.Arabic, true, StudyStage.G9) {
        Surface(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Grade9LearningPath(
                state = state,
                open = {},
                gems = LocalGemsUiState(),
                planner = null,
                workspace = null,
                grade9Study = studyRepo,
                naturePractice = natureRepo
            )
        }
    }
}
