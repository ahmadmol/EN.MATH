package com.ahmadmol.enmath.core.data

import android.content.Context
import com.ahmadmol.enmath.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local demo session/preferences only. Never stores passwords or sends network requests. */
class LocalDemoRepository(context: Context) : LearningRepository {
    private val prefs = context.getSharedPreferences("enmath_demo", Context.MODE_PRIVATE)

    private fun <T : Enum<T>> enumValue(value: String?, values: Array<T>, fallback: T): T =
        values.find { it.name == value } ?: fallback

    private val _state =
        MutableStateFlow(
            LearningState(
                user =
                    prefs.getString("name", null)?.let {
                        User(
                            it,
                            prefs.getString("email", "").orEmpty(),
                            enumValue(
                                prefs.getString("role", null),
                                AccountType.entries.toTypedArray(),
                                AccountType.Guest,
                            ),
                        )
                    },
                completed =
                    prefs.getStringSet("completed", null)?.toSet()
                        ?: DemoContent.initiallyCompleted,
                lastLessonId = prefs.getString("last", "limits-practice").orEmpty(),
                theme =
                    enumValue(
                        prefs.getString("theme", null),
                        ThemeMode.entries.toTypedArray(),
                        ThemeMode.System,
                    ),
                language =
                    enumValue(
                        prefs.getString("language", null),
                        Language.entries.toTypedArray(),
                        Language.Arabic,
                    ),
                onboarded = prefs.getBoolean("onboarded", false),
                product = ProductCodec.load(prefs),
            )
        )
    override val state: StateFlow<LearningState> = _state.asStateFlow()

    private fun update(transform: (LearningState) -> LearningState) {
        val state = transform(_state.value)
        _state.value = state
        ProductCodec.save(prefs, state.product)
        prefs
            .edit()
            .apply {
                putString("name", state.user?.name)
                putString("email", state.user?.email)
                putString("role", state.user?.type?.name)
                putStringSet("completed", state.completed)
                putString("last", state.lastLessonId)
                putString("theme", state.theme.name)
                putString("language", state.language.name)
                putBoolean("onboarded", state.onboarded)
            }
            .apply()
    }

    override fun signIn(user: User) = update { it.copy(user = user) }

    override fun logout() = update { it.copy(user = null) }

    override fun finishOnboarding() = update { it.copy(onboarded = true) }

    override fun completeLesson(id: String) {
        if (DemoContent.lesson(id) != null)
            update { it.copy(completed = it.completed + id, lastLessonId = id) }
    }

    override fun openLesson(id: String) {
        if (DemoContent.lesson(id) != null) update { it.copy(lastLessonId = id) }
    }

    override fun setTheme(mode: ThemeMode) = update { it.copy(theme = mode) }

    override fun setLanguage(language: Language) = update { it.copy(language = language) }

    override fun changeProduct(transform: (ProductState) -> ProductState) = update {
        it.copy(product = transform(it.product))
    }
}
