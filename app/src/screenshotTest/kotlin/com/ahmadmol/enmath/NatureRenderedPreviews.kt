package com.ahmadmol.enmath

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.student.*

/** Actual production composables and graders. Only stores are isolated in-memory preview fixtures.
 * Tall captures expose all content; they are not evidence of runtime scrolling or device execution.
 */
@Composable private fun NatureScene(position: Int, scene: String) {
    val context = LocalContext.current
    val state = remember { LearningState(user = User("معاينة", "", AccountType.Student,
        "00000000-0000-0000-0000-000000000001", StudyStage.G9), language = Language.Arabic,
        product = ProductState(), completed = emptySet()) }
    val bank = remember { NaturePracticeBank.load(context) }
    val repo = remember(position, scene) {
        val files = mutableMapOf<String, Grade9PreviewPreferences>()
        NaturePracticeRepository({ key -> files.getOrPut(key) { Grade9PreviewPreferences() } }) { state }.also { r ->
            val scope = StudentCatalog.scope(state)!!
            val good = listOf("2", "3/8", "true", "غير منتهية|دورية", "5|integer",
                "-3=integer|2/5=decimal|π=irrational|√49=integer|√2=irrational")
            val bad = listOf("0", "6/16", "false", "دورية", "-5|integer",
                "-3=irrational|2/5=decimal|π=irrational|√49=integer|√2=irrational")
            r.start(scope)
            for (i in 0 until position) { r.submit(scope, bank[i].id, good[i]); r.move(scope, i + 1) }
            when (scene) {
                "correct" -> r.submit(scope, bank[position].id, good[position])
                "wrong" -> r.submit(scope, bank[position].id, bad[position])
                "hint" -> r.hint(scope)
                "result" -> { r.hint(scope); r.submit(scope, bank[position].id, bad[position]); r.submit(scope, bank[position].id, good[position]); r.finish(scope) }
            }
        }
    }
    ENMathTheme(ThemeMode.System, Language.Arabic, true, StudyStage.G9) { Surface {
        val read = repo.read(StudentCatalog.scope(state)!!)
        if (scene == "result") NaturePracticeResultScreen(state, repo,
            (read as NatureRead.Ready).data.attempts.single().id, {}, {})
        else NaturePracticeContent(state, repo, bank, {}, {}, initialRead = read)
    } }
}

@PreviewTest @Preview(name="Q01_question_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_question_Light_150() = NatureScene(0, "question")

@PreviewTest @Preview(name="Q01_question_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_question_Light_200() = NatureScene(0, "question")

@PreviewTest @Preview(name="Q01_question_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_question_Dark_150() = NatureScene(0, "question")

@PreviewTest @Preview(name="Q01_question_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_question_Dark_200() = NatureScene(0, "question")

@PreviewTest @Preview(name="Q01_correct_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_correct_Light_150() = NatureScene(0, "correct")

@PreviewTest @Preview(name="Q01_correct_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_correct_Light_200() = NatureScene(0, "correct")

@PreviewTest @Preview(name="Q01_correct_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_correct_Dark_150() = NatureScene(0, "correct")

@PreviewTest @Preview(name="Q01_correct_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_correct_Dark_200() = NatureScene(0, "correct")

@PreviewTest @Preview(name="Q01_wrong_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_wrong_Light_150() = NatureScene(0, "wrong")

@PreviewTest @Preview(name="Q01_wrong_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_wrong_Light_200() = NatureScene(0, "wrong")

@PreviewTest @Preview(name="Q01_wrong_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_wrong_Dark_150() = NatureScene(0, "wrong")

@PreviewTest @Preview(name="Q01_wrong_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_wrong_Dark_200() = NatureScene(0, "wrong")

@PreviewTest @Preview(name="Q01_hint_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_hint_Light_150() = NatureScene(0, "hint")

@PreviewTest @Preview(name="Q01_hint_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q01_hint_Light_200() = NatureScene(0, "hint")

@PreviewTest @Preview(name="Q01_hint_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_hint_Dark_150() = NatureScene(0, "hint")

@PreviewTest @Preview(name="Q01_hint_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q01_hint_Dark_200() = NatureScene(0, "hint")

@PreviewTest @Preview(name="Q02_question_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_question_Light_150() = NatureScene(1, "question")

@PreviewTest @Preview(name="Q02_question_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_question_Light_200() = NatureScene(1, "question")

@PreviewTest @Preview(name="Q02_question_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_question_Dark_150() = NatureScene(1, "question")

@PreviewTest @Preview(name="Q02_question_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_question_Dark_200() = NatureScene(1, "question")

@PreviewTest @Preview(name="Q02_correct_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_correct_Light_150() = NatureScene(1, "correct")

@PreviewTest @Preview(name="Q02_correct_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_correct_Light_200() = NatureScene(1, "correct")

@PreviewTest @Preview(name="Q02_correct_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_correct_Dark_150() = NatureScene(1, "correct")

@PreviewTest @Preview(name="Q02_correct_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_correct_Dark_200() = NatureScene(1, "correct")

@PreviewTest @Preview(name="Q02_wrong_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_wrong_Light_150() = NatureScene(1, "wrong")

@PreviewTest @Preview(name="Q02_wrong_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_wrong_Light_200() = NatureScene(1, "wrong")

@PreviewTest @Preview(name="Q02_wrong_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_wrong_Dark_150() = NatureScene(1, "wrong")

@PreviewTest @Preview(name="Q02_wrong_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_wrong_Dark_200() = NatureScene(1, "wrong")

@PreviewTest @Preview(name="Q02_hint_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_hint_Light_150() = NatureScene(1, "hint")

@PreviewTest @Preview(name="Q02_hint_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q02_hint_Light_200() = NatureScene(1, "hint")

@PreviewTest @Preview(name="Q02_hint_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_hint_Dark_150() = NatureScene(1, "hint")

@PreviewTest @Preview(name="Q02_hint_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q02_hint_Dark_200() = NatureScene(1, "hint")

@PreviewTest @Preview(name="Q03_question_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_question_Light_150() = NatureScene(2, "question")

@PreviewTest @Preview(name="Q03_question_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_question_Light_200() = NatureScene(2, "question")

@PreviewTest @Preview(name="Q03_question_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_question_Dark_150() = NatureScene(2, "question")

@PreviewTest @Preview(name="Q03_question_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_question_Dark_200() = NatureScene(2, "question")

@PreviewTest @Preview(name="Q03_correct_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_correct_Light_150() = NatureScene(2, "correct")

@PreviewTest @Preview(name="Q03_correct_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_correct_Light_200() = NatureScene(2, "correct")

@PreviewTest @Preview(name="Q03_correct_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_correct_Dark_150() = NatureScene(2, "correct")

@PreviewTest @Preview(name="Q03_correct_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_correct_Dark_200() = NatureScene(2, "correct")

@PreviewTest @Preview(name="Q03_wrong_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_wrong_Light_150() = NatureScene(2, "wrong")

@PreviewTest @Preview(name="Q03_wrong_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_wrong_Light_200() = NatureScene(2, "wrong")

@PreviewTest @Preview(name="Q03_wrong_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_wrong_Dark_150() = NatureScene(2, "wrong")

@PreviewTest @Preview(name="Q03_wrong_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_wrong_Dark_200() = NatureScene(2, "wrong")

@PreviewTest @Preview(name="Q03_hint_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_hint_Light_150() = NatureScene(2, "hint")

@PreviewTest @Preview(name="Q03_hint_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q03_hint_Light_200() = NatureScene(2, "hint")

@PreviewTest @Preview(name="Q03_hint_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_hint_Dark_150() = NatureScene(2, "hint")

@PreviewTest @Preview(name="Q03_hint_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q03_hint_Dark_200() = NatureScene(2, "hint")

@PreviewTest @Preview(name="Q04_question_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_question_Light_150() = NatureScene(3, "question")

@PreviewTest @Preview(name="Q04_question_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_question_Light_200() = NatureScene(3, "question")

@PreviewTest @Preview(name="Q04_question_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_question_Dark_150() = NatureScene(3, "question")

@PreviewTest @Preview(name="Q04_question_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_question_Dark_200() = NatureScene(3, "question")

@PreviewTest @Preview(name="Q04_correct_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_correct_Light_150() = NatureScene(3, "correct")

@PreviewTest @Preview(name="Q04_correct_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_correct_Light_200() = NatureScene(3, "correct")

@PreviewTest @Preview(name="Q04_correct_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_correct_Dark_150() = NatureScene(3, "correct")

@PreviewTest @Preview(name="Q04_correct_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_correct_Dark_200() = NatureScene(3, "correct")

@PreviewTest @Preview(name="Q04_wrong_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_wrong_Light_150() = NatureScene(3, "wrong")

@PreviewTest @Preview(name="Q04_wrong_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_wrong_Light_200() = NatureScene(3, "wrong")

@PreviewTest @Preview(name="Q04_wrong_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_wrong_Dark_150() = NatureScene(3, "wrong")

@PreviewTest @Preview(name="Q04_wrong_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_wrong_Dark_200() = NatureScene(3, "wrong")

@PreviewTest @Preview(name="Q04_hint_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_hint_Light_150() = NatureScene(3, "hint")

@PreviewTest @Preview(name="Q04_hint_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q04_hint_Light_200() = NatureScene(3, "hint")

@PreviewTest @Preview(name="Q04_hint_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_hint_Dark_150() = NatureScene(3, "hint")

@PreviewTest @Preview(name="Q04_hint_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q04_hint_Dark_200() = NatureScene(3, "hint")

@PreviewTest @Preview(name="Q05_question_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_question_Light_150() = NatureScene(4, "question")

@PreviewTest @Preview(name="Q05_question_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_question_Light_200() = NatureScene(4, "question")

@PreviewTest @Preview(name="Q05_question_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_question_Dark_150() = NatureScene(4, "question")

@PreviewTest @Preview(name="Q05_question_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_question_Dark_200() = NatureScene(4, "question")

@PreviewTest @Preview(name="Q05_correct_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_correct_Light_150() = NatureScene(4, "correct")

@PreviewTest @Preview(name="Q05_correct_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_correct_Light_200() = NatureScene(4, "correct")

@PreviewTest @Preview(name="Q05_correct_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_correct_Dark_150() = NatureScene(4, "correct")

@PreviewTest @Preview(name="Q05_correct_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_correct_Dark_200() = NatureScene(4, "correct")

@PreviewTest @Preview(name="Q05_wrong_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_wrong_Light_150() = NatureScene(4, "wrong")

@PreviewTest @Preview(name="Q05_wrong_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_wrong_Light_200() = NatureScene(4, "wrong")

@PreviewTest @Preview(name="Q05_wrong_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_wrong_Dark_150() = NatureScene(4, "wrong")

@PreviewTest @Preview(name="Q05_wrong_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_wrong_Dark_200() = NatureScene(4, "wrong")

@PreviewTest @Preview(name="Q05_hint_Light_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_hint_Light_150() = NatureScene(4, "hint")

@PreviewTest @Preview(name="Q05_hint_Light_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q05_hint_Light_200() = NatureScene(4, "hint")

@PreviewTest @Preview(name="Q05_hint_Dark_150", widthDp=360, heightDp=2600, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_hint_Dark_150() = NatureScene(4, "hint")

@PreviewTest @Preview(name="Q05_hint_Dark_200", widthDp=360, heightDp=2600, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q05_hint_Dark_200() = NatureScene(4, "hint")

@PreviewTest @Preview(name="Q06_question_Light_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_question_Light_150() = NatureScene(5, "question")

@PreviewTest @Preview(name="Q06_question_Light_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_question_Light_200() = NatureScene(5, "question")

@PreviewTest @Preview(name="Q06_question_Dark_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_question_Dark_150() = NatureScene(5, "question")

@PreviewTest @Preview(name="Q06_question_Dark_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_question_Dark_200() = NatureScene(5, "question")

@PreviewTest @Preview(name="Q06_correct_Light_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_correct_Light_150() = NatureScene(5, "correct")

@PreviewTest @Preview(name="Q06_correct_Light_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_correct_Light_200() = NatureScene(5, "correct")

@PreviewTest @Preview(name="Q06_correct_Dark_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_correct_Dark_150() = NatureScene(5, "correct")

@PreviewTest @Preview(name="Q06_correct_Dark_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_correct_Dark_200() = NatureScene(5, "correct")

@PreviewTest @Preview(name="Q06_wrong_Light_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_wrong_Light_150() = NatureScene(5, "wrong")

@PreviewTest @Preview(name="Q06_wrong_Light_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_wrong_Light_200() = NatureScene(5, "wrong")

@PreviewTest @Preview(name="Q06_wrong_Dark_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_wrong_Dark_150() = NatureScene(5, "wrong")

@PreviewTest @Preview(name="Q06_wrong_Dark_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_wrong_Dark_200() = NatureScene(5, "wrong")

@PreviewTest @Preview(name="Q06_hint_Light_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_hint_Light_150() = NatureScene(5, "hint")

@PreviewTest @Preview(name="Q06_hint_Light_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Q06_hint_Light_200() = NatureScene(5, "hint")

@PreviewTest @Preview(name="Q06_hint_Dark_150", widthDp=360, heightDp=6500, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_hint_Dark_150() = NatureScene(5, "hint")

@PreviewTest @Preview(name="Q06_hint_Dark_200", widthDp=360, heightDp=6500, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Q06_hint_Dark_200() = NatureScene(5, "hint")

@PreviewTest @Preview(name="Results_Light_150", widthDp=360, heightDp=1800, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Results_Light_150() = NatureScene(5, "result")

@PreviewTest @Preview(name="Results_Light_200", widthDp=360, heightDp=1800, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_NO)
@Composable fun Nature_Results_Light_200() = NatureScene(5, "result")

@PreviewTest @Preview(name="Results_Dark_150", widthDp=360, heightDp=1800, locale="ar", fontScale=1.5f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Results_Dark_150() = NatureScene(5, "result")

@PreviewTest @Preview(name="Results_Dark_200", widthDp=360, heightDp=1800, locale="ar", fontScale=2f, uiMode=Configuration.UI_MODE_NIGHT_YES)
@Composable fun Nature_Results_Dark_200() = NatureScene(5, "result")

