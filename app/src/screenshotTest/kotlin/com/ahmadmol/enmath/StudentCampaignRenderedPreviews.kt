package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.courses.SourceStudyScreen
import com.ahmadmol.enmath.features.student.*

@Preview(name="360_ar_light_100", widthDp=360, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_ar_dark_150", widthDp=360, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_ar_light_200", widthDp=360, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_en_light_150", widthDp=393, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_en_dark_200", widthDp=393, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_ar_dark_200", widthDp=800, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
annotation class CampaignMatrix

/** In-memory preview data, production composables; no real student or device files. */
@Composable private fun CampaignScene(scene: String) {
    val language = if (LocalConfiguration.current.locales[0].language == "ar") Language.Arabic else Language.English
    val stage = if (scene == "bac") StudyStage.BAC else StudyStage.G9
    val state = remember(language, stage) { LearningState(user = User("QA", "", AccountType.Student,
        "00000000-0000-0000-0000-000000000001", stage), language = language,
        completed = emptySet(), product = ProductState()) }
    val scope = StudentCatalog.scope(state)!!
    val files = remember { mutableMapOf<String, Grade9PreviewPreferences>() }
    val nature = remember(state) {
        NaturePracticeRepository({ files.getOrPut(it) { Grade9PreviewPreferences() } }) { state }.also { repo ->
            if (stage == StudyStage.G9) {
                val ids = StudentLearningProgressEngine.ALL_NATURE_IDS
                val good = listOf("2", "3/8", "true", "غير منتهية|دورية", "5|integer",
                    "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational")
                repo.start(scope)
                repo.hint(scope); repo.submit(scope, ids[0], "0")
                for (i in 0..5) {
                    repo.submit(scope, ids[i], good[i])
                    if (i < 5) repo.move(scope, i + 1)
                }
                repo.finish(scope)
                repo.start(scope); repo.submit(scope, ids[0], good[0])
            }
        }
    }
    val study = remember(state) {
        Grade9StudyRepository({ files.getOrPut(it) { Grade9PreviewPreferences() } }) { state }.also {
            if (stage == StudyStage.G9) it.selectSubject(scope, Grade9Subject.GEOMETRY)
        }
    }
    ENMathTheme(ThemeMode.System, language, true, stage) { Surface {
        when (scene) {
            "progress" -> Grade9LearningProgressCard(StudentLearningProgressEngine.computeGrade9Progress(
                (nature.read(scope) as NatureRead.Ready).data), {}, Modifier.padding(16.dp))
            "evidence" -> Grade9NatureEvidenceSection(state, nature, {})
            "review" -> Grade9NatureEvidenceSection(state, nature, {}, mistakes = true)
            "geometry" -> Grade9LearningPath(state, {}, LocalGemsUiState(), null, null, study, naturePractice = nature)
            "error" -> NatureProgressReadError()
            "bac" -> SourceStudyScreen(StudyStage.BAC, {})
        }
    } }
}

@PreviewTest @CampaignMatrix @Composable fun CampaignProgress() = CampaignScene("progress")
@PreviewTest @CampaignMatrix @Composable fun CampaignEvidence() = CampaignScene("evidence")
@PreviewTest @CampaignMatrix @Composable fun CampaignReview() = CampaignScene("review")
@PreviewTest @CampaignMatrix @Composable fun CampaignGeometry() = CampaignScene("geometry")
@PreviewTest @CampaignMatrix @Composable fun CampaignReadError() = CampaignScene("error")
@PreviewTest @CampaignMatrix @Composable fun CampaignBacBooks() = CampaignScene("bac")
