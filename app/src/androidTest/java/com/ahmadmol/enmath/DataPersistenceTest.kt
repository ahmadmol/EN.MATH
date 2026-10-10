package com.ahmadmol.enmath

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataPersistenceTest {
    @Test
    fun allUserGeneratedDemoDataSurvivesRepositoryReloadAndRoleSwitch() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("enmath_demo", 0).edit().clear().commit()
        val repo = LocalDemoRepository(context)
        repo.signIn(User("Ahmed", "ahmed@example.com", AccountType.Student))
        repo.completeLesson("derivatives-practice")
        repo.setLanguage(Language.English)
        repo.setTheme(ThemeMode.Dark)
        val p = repo.state.value.product
        val custom =
            Question(
                "custom-q",
                "derivatives",
                Copy("Find 2+2", "احسب 2+2"),
                "2+2",
                QuestionKind.Numeric,
                emptyList(),
                "4",
                Copy("Add", "اجمع"),
                Copy("2+2=4", "2+2=4"),
            )
        repo.changeProduct {
            it.copy(
                questions = listOf(custom),
                savedLessons = setOf("derivatives-practice"),
                savedProblems = setOf("history-1"),
                history = listOf(HistoryItem("history-1", "2x+4=10", "equations", 12345)),
                searches = listOf("الاشتقاق"),
                readNotifications = setOf("note-1"),
                classes = p.classes + Classroom("new-class", Copy("C", "ج"), emptyList(), "INVITE"),
                assignments =
                    p.assignments +
                        Assignment(
                            "new-assignment",
                            Copy("A", "و"),
                            Copy("D", "ش"),
                            "class-a",
                            listOf("custom-q"),
                            123456,
                        ),
                resources =
                    p.resources +
                        TeacherResource(
                            "new-resource",
                            Copy("R", "م"),
                            "derivatives",
                            ResourceKind.LessonNote,
                            Copy("Body", "محتوى"),
                            setOf("class-a"),
                            2345,
                        ),
            )
        }
        val expected = repo.state.value.product
        repo.logout()
        repo.signIn(User("Sara", "sara@example.com", AccountType.Teacher))
        val reloaded = LocalDemoRepository(context)
        assertEquals(expected, reloaded.state.value.product)
        assertEquals(AccountType.Teacher, reloaded.state.value.user?.type)
        assertEquals(Language.English, reloaded.state.value.language)
        assertEquals(ThemeMode.Dark, reloaded.state.value.theme)
        assertEquals(
            LearningRules.topic(repo.state.value, "derivatives"),
            LearningRules.topic(reloaded.state.value, "derivatives", "ahmed"),
        )
        assertTrue("derivatives-practice" in reloaded.state.value.completed)
        assertEquals("4", reloaded.state.value.product.question("custom-q")?.answer)
    }
}
