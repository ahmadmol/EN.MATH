package com.ahmadmol.enmath.core.design

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ahmadmol.enmath.core.data.LearningState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.home.StudentHomeScreen

@Preview(name = "Arabic · Light", widthDp = 390, heightDp = 844)
@Composable
private fun ArabicHomePreview() {
    ENMathTheme(ThemeMode.Light, Language.Arabic) {
        Surface {
            StudentHomeScreen(LearningState(user = User("أحمد محمد", "", AccountType.Student)), {})
        }
    }
}

@Preview(name = "English · Dark", widthDp = 390, heightDp = 844)
@Composable
private fun EnglishHomePreview() {
    ENMathTheme(ThemeMode.Dark, Language.English) {
        Surface {
            StudentHomeScreen(
                LearningState(user = User("Ahmed Mohammad", "", AccountType.Student)),
                {},
            )
        }
    }
}
