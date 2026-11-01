package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.design.tr
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*

@Preview(name = "Light 100% AR", widthDp = 360, locale = "ar", fontScale = 1.0f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Light 150% AR", widthDp = 360, locale = "ar", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Light 200% AR", widthDp = 360, locale = "ar", fontScale = 2.0f, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark 100% AR", widthDp = 360, locale = "ar", fontScale = 1.0f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Dark 150% AR", widthDp = 360, locale = "ar", fontScale = 1.5f, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Dark 200% AR", widthDp = 360, locale = "ar", fontScale = 2.0f, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class ProgressVisualMatrix

private fun previewResearch(): SourceResearch =
    SourceCurricula.forStage(StudyStage.G9).books.first().units.first().researches.first()

@Composable
private fun ProgressVisualScene(data: NaturePracticeData, title: String) {
    val progress = StudentLearningProgressEngine.computeGrade9Progress(data)
    val state = LearningState(
        user = User("طالب المعاينة", "", AccountType.Student, "00000000-0000-0000-0000-000000000001", StudyStage.G9),
        language = Language.Arabic
    )
    val research = previewResearch()

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 1. Home Progress Card
            Grade9LearningProgressCard(progress = progress, open = {})

            // 2. Station Circle Node
            Text(
                text = tr("Lesson Station Circle:", "حالة دائرة الدرس في المسار:"),
                style = MaterialTheme.typography.labelMedium
            )
            PathStationNode(
                research = research,
                stationIndex = 0,
                lastVisited = false,
                open = {},
                bias = 0.5f,
                prevBias = 0.5f,
                progressStatus = progress.natureLessonStatus
            )

            // 3. Evidence / Progress Screen Section
            Grade9NatureEvidenceSection(
                state = state,
                repository = null,
                open = {},
                mistakes = false
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Case 1: New student who hasn't started practice
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_01_NewStudentNotStarted() {
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = NaturePracticeData(),
            title = "1. طالب جديد لم يبدأ أي تدريب"
        )
    }
}

// ----------------------------------------------------------------------------
// Case 2: Student started practice in progress (draft active)
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_02_StartedInProgress() {
    val allIds = StudentLearningProgressEngine.ALL_NATURE_IDS
    val data = NaturePracticeData(
        draft = NatureDraft(
            id = "d-preview-started",
            position = 2,
            answers = mapOf(allIds[0] to "2", allIds[1] to "3/8"),
            passed = setOf(allIds[0], allIds[1]),
            missed = mapOf(allIds[1] to 1),
            hints = setOf(allIds[0])
        )
    )
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = data,
            title = "2. طالب بدأ التدريب ولم يكمله (2 من 6)"
        )
    }
}

// ----------------------------------------------------------------------------
// Case 3: Student completed Nature of Numbers practice
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_03_PracticeCompleted() {
    val allIds = StudentLearningProgressEngine.ALL_NATURE_IDS
    val data = NaturePracticeData(
        attempts = listOf(
            NatureAttempt(
                id = "att-preview-completed",
                answers = allIds.associateWith { "ans" },
                missed = emptyMap(),
                hints = emptySet(),
                startedAt = 1000L,
                finishedAt = 2000L
            )
        )
    )
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = data,
            title = "3. طالب أكمل تدريب طبيعة الأعداد (100% من أول محاولة)"
        )
    }
}

// ----------------------------------------------------------------------------
// Case 4: Student improved performance between sessions
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_04_StudentImproved() {
    val allIds = StudentLearningProgressEngine.ALL_NATURE_IDS
    val data = NaturePracticeData(
        attempts = listOf(
            NatureAttempt(
                id = "att-session-1",
                answers = allIds.associateWith { "ans" },
                missed = mapOf(allIds[0] to 1, allIds[1] to 1, allIds[2] to 1), // 3 first-try
                hints = emptySet(),
                startedAt = 1000L,
                finishedAt = 2000L
            ),
            NatureAttempt(
                id = "att-session-2",
                answers = allIds.associateWith { "ans" },
                missed = emptyMap(), // 6 first-try -> improved!
                hints = emptySet(),
                startedAt = 3000L,
                finishedAt = 4000L
            )
        )
    )
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = data,
            title = "4. طالب تحسن أداؤه بين جلستين (من 3 إلى 6)"
        )
    }
}

// ----------------------------------------------------------------------------
// Case 5: Student needs review (high errors / hints)
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_05_NeedsReview() {
    val allIds = StudentLearningProgressEngine.ALL_NATURE_IDS
    val data = NaturePracticeData(
        attempts = listOf(
            NatureAttempt(
                id = "att-session-review",
                answers = allIds.associateWith { "ans" },
                missed = mapOf(allIds[0] to 2, allIds[1] to 2, allIds[2] to 1),
                hints = setOf(allIds[0], allIds[1], allIds[2]),
                startedAt = 1000L,
                finishedAt = 2000L
            )
        )
    )
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = data,
            title = "5. طالب يحتاج مراجعة لطيفة"
        )
    }
}

// ----------------------------------------------------------------------------
// Case 6: Insufficient data for percentage (0 attempted questions)
// ----------------------------------------------------------------------------
@ProgressVisualMatrix
@Composable
fun Preview_06_InsufficientData() {
    val data = NaturePracticeData(
        draft = NatureDraft(id = "d-zero", position = 0)
    )
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) {
        ProgressVisualScene(
            data = data,
            title = "6. حالة عدم كفاية البيانات لاحتساب النسبة"
        )
    }
}
