package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*

@Preview(name="360_ar_light_100", widthDp=360, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_ar_light_150", widthDp=360, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_ar_light_200", widthDp=360, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_ar_dark_100", widthDp=360, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_ar_dark_150", widthDp=360, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_ar_dark_200", widthDp=360, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_en_light_100", widthDp=360, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_en_light_150", widthDp=360, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_en_light_200", widthDp=360, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="360_en_dark_100", widthDp=360, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_en_dark_150", widthDp=360, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="360_en_dark_200", widthDp=360, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_ar_light_100", widthDp=393, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_ar_light_150", widthDp=393, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_ar_light_200", widthDp=393, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_ar_dark_100", widthDp=393, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_ar_dark_150", widthDp=393, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_ar_dark_200", widthDp=393, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_en_light_100", widthDp=393, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_en_light_150", widthDp=393, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_en_light_200", widthDp=393, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="393_en_dark_100", widthDp=393, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_en_dark_150", widthDp=393, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="393_en_dark_200", widthDp=393, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_ar_light_100", widthDp=800, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_ar_light_150", widthDp=800, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_ar_light_200", widthDp=800, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_ar_dark_100", widthDp=800, heightDp=2200, locale="ar", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_ar_dark_150", widthDp=800, heightDp=2200, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_ar_dark_200", widthDp=800, heightDp=2200, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_en_light_100", widthDp=800, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_en_light_150", widthDp=800, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_en_light_200", widthDp=800, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Preview(name="800_en_dark_100", widthDp=800, heightDp=2200, locale="en", fontScale=1f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_en_dark_150", widthDp=800, heightDp=2200, locale="en", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Preview(name="800_en_dark_200", widthDp=800, heightDp=2200, locale="en", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
annotation class PolishMatrix

@Composable private fun PolishScene(scene: String) {
    val context = LocalContext.current
    val language = if (LocalConfiguration.current.locales[0].language == "ar") Language.Arabic else Language.English
    val stage = if (scene == "home") StudyStage.BAC else StudyStage.G9
    val state = remember(language, stage) { LearningState(user = User("QA", "", AccountType.Student,
        "00000000-0000-0000-0000-000000000001", stage), language = language, completed = emptySet(), product = ProductState()) }
    val stores = remember { mutableMapOf<String, Grade9PreviewPreferences>() }
    val nature = remember(state) { NaturePracticeRepository({ stores.getOrPut(it) { Grade9PreviewPreferences() } }) { state } }
    ENMathTheme(ThemeMode.System, language, true, stage) { Surface {
        when (scene) {
            "home" -> BaccalaureateRoadmapHomeScreen(state, {}, LocalGemsUiState())
            "lesson" -> NatureLessonScreen(state, nature, {})
            "practice" -> NaturePracticeContent(state, nature, NaturePracticeBank.load(context), {}, {},
                initialRead = nature.start(StudentCatalog.scope(state)!!))
        }
    } }
}

@PreviewTest @PolishMatrix @Composable fun PolishBacHome() = PolishScene("home")
@PreviewTest @PolishMatrix @Composable fun PolishNatureLesson() = PolishScene("lesson")
@PreviewTest @PolishMatrix @Composable fun PolishNaturePractice() = PolishScene("practice")
