package com.ahmadmol.enmath

import android.content.SharedPreferences
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.ENMathTheme
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.library.Grade9FilesScreen
import com.ahmadmol.enmath.features.profile.ProductProfileScreen
import com.ahmadmol.enmath.features.student.*
import com.ahmadmol.enmath.core.design.RefTopBar

/** Debug-only in-memory preview fixtures. No Android preferences, files, wallets or device data.
 * StudentSurfaceMatrix supplies Arabic/English, light/dark, 100/150/200%, 360/393dp previews.
 */
internal class Grade9PreviewPreferences : SharedPreferences {
    private val values = mutableMapOf<String, Any?>()
    override fun getAll(): MutableMap<String, *> = values.toMutableMap()
    override fun getString(k: String?, d: String?) = values[k] as? String ?: d
    override fun getStringSet(k: String?, d: MutableSet<String>?) = d
    override fun getInt(k: String?, d: Int) = values[k] as? Int ?: d
    override fun getLong(k: String?, d: Long) = values[k] as? Long ?: d
    override fun getFloat(k: String?, d: Float) = values[k] as? Float ?: d
    override fun getBoolean(k: String?, d: Boolean) = values[k] as? Boolean ?: d
    override fun contains(k: String?) = values.containsKey(k)
    override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) {}
    override fun edit() = object : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var clear = false
        override fun putString(k: String?, v: String?) = apply { if (k != null) pending[k] = v }
        override fun putStringSet(k: String?, v: MutableSet<String>?) = apply { if (k != null) pending[k] = v }
        override fun putInt(k: String?, v: Int) = apply { if (k != null) pending[k] = v }
        override fun putLong(k: String?, v: Long) = apply { if (k != null) pending[k] = v }
        override fun putFloat(k: String?, v: Float) = apply { if (k != null) pending[k] = v }
        override fun putBoolean(k: String?, v: Boolean) = apply { if (k != null) pending[k] = v }
        override fun remove(k: String?) = apply { if (k != null) pending[k] = null }
        override fun clear() = apply { clear = true }
        override fun commit(): Boolean { if (clear) values.clear(); pending.forEach { (k,v) -> if (v == null) values.remove(k) else values[k] = v }; return true }
        override fun apply() { commit() }
    }
}

@Composable fun PilotPreview(screen: String) {
    val language = if (LocalConfiguration.current.locales[0].language == "ar") Language.Arabic else Language.English
    val state = LearningState(user = User("PREVIEW · نور", "", AccountType.Guest,
        "00000000-0000-0000-0000-000000000001", StudyStage.G9), completed = emptySet(), product = ProductState(), language = language)
    val repository = remember(screen, state) {
        val prefs = Grade9PreviewPreferences()
        Grade9StudyRepository({ prefs }) { state }.also { repo ->
            val scope = StudentCatalog.scope(state)!!
            if (screen in setOf("files", "resource-dialog")) {
                repo.addFile(scope, StudentFile("preview-pdf", "content://preview/document/1", "PREVIEW ملخص الجبر.pdf", "application/pdf", 184320))
                repo.addFile(scope, StudentFile("preview-word", "content://preview/document/2", "PREVIEW ورقة مراجعة.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", 24576))
            }
            if (screen == "example") repo.section(scope, 5)
            if (screen == "summary") repo.section(scope, 7)
            if (screen == "result" || screen == "activity") repo.finish(scope, G9PracticeDraft("preview-result",
                Grade9Pilot.questions.associate { it.id to if (it == Grade9Pilot.questions[0]) "6/8" else it.answer }), 25)
            if (screen == "correction" || screen == "hint") repo.saveDraft(scope,
                G9PracticeDraft("preview-practice", answers = mapOf(Grade9Pilot.questions.first().id to "6/8"),
                    checked = if (screen == "correction") setOf(Grade9Pilot.questions.first().id) else emptySet(),
                    hints = if (screen == "hint") setOf(Grade9Pilot.questions.first().id) else emptySet()))
        }
    }
    val dailyPlanner = remember(screen,state) {
        val files=mutableMapOf<String,Grade9PreviewPreferences>()
        PlannerRepository({key -> files.getOrPut(key){Grade9PreviewPreferences()} }) {state}.also { planner ->
            if(screen in setOf("home","goal","activity")) {
                val scope=StudentCatalog.scope(state)!!
                val task=PlanTask("00000000-0000-0000-0000-000000000010","PREVIEW مراجعة الكسور","الرياضيات",PlanPeriod.WEEK,
                    StudyPeriods.start(System.currentTimeMillis(),PlanPeriod.WEEK),PlanMode.TIME,1,minutes=15,lesson=Grade9Pilot.ref)
                planner.save(scope,0,"preview-create",task)
                if(screen!="home") {
                    planner.progress(scope,1,"preview-progress",task.id,0,5)
                    val changed=(planner.read(scope) as PlanRead.Ready).plan.tasks.single().copy(title="PREVIEW هدف المراجعة المعدل")
                    planner.save(scope,2,"preview-edit",changed)
                }
            }
        }
    }
    val dailyWorkspace=remember(screen,state) {
        val prefs=Grade9PreviewPreferences()
        SourceWorkspaceRepository({prefs}){state}.also { workspace ->
            val scope=StudentCatalog.scope(state)!!
            if(screen in setOf("library","favorites")) SourceCurricula.forStage(StudyStage.G9).units.first().researches.take(2).forEach {workspace.bookmark(scope,it.ref,true)}
        }
    }
    ENMathTheme(ThemeMode.System, language, true, StudyStage.G9) { Surface {
        val tab = when(screen) { "home", "home-geometry" -> "home"; "algebra" -> "g9/algebra"; "geometry" -> "g9/geometry"; "exercises" -> "g9/exercises"; "profile" -> "profile"; else -> null }
        Scaffold(bottomBar = { if (tab != null) Grade9BottomNav(tab, {}) }, topBar = {
            if (tab == null) RefTopBar(if (language == Language.Arabic) "التاسع" else "Grade 9", {}, null, null, 0)
        }) { insets -> Box(Modifier.padding(insets).imePadding()) {
        when (screen) {
            "home" -> Grade9LearningPath(state, {}, LocalGemsUiState(), dailyPlanner, dailyWorkspace, repository)
            "home-geometry" -> Grade9LearningPath(state, {}, LocalGemsUiState(), null, null, repository,
                initialPathIndex = 4 + SourceCurricula.forStage(StudyStage.G9).books.first().let { it.units.size + it.researches.size })
            "algebra" -> Grade9SubjectScreen(state, false, {})
            "geometry" -> Grade9SubjectScreen(state, true, {})
            "exercises" -> Grade9ExercisesScreen(state, {}, repository)
            "profile" -> ProductProfileScreen(state, false, {}, {})
            "practice", "correction", "hint" -> {
                val vm = remember { Grade9PracticeViewModel().also { it.bind(StudentCatalog.scope(state), repository) } }
                Grade9PracticeScreen(state, repository, vm, {})
            }
            "result" -> Grade9ResultScreen(state, repository, "preview-result", {})
            "skip-dialog" -> G9SkipDialog({}, {})
            "missing-file" -> com.ahmadmol.enmath.features.library.Grade9FileScreen(state, repository, "missing")
            "storage-error" -> Grade9Scaffold("preview-storage-error") { item { G9StudyError(G9StudyRead.StorageError) } }
            "loading" -> Grade9PracticeScreen(state, repository, remember { Grade9PracticeViewModel() }, {})
            "library" -> com.ahmadmol.enmath.features.library.LibraryScreen(state, {}, {}) {
                com.ahmadmol.enmath.features.library.Grade9FilesEntry({})
                com.ahmadmol.enmath.features.courses.SourceSavedLibrary(state,dailyWorkspace,{})
            }
            "activity", "activity-empty" -> Grade9ActivityScreen(state,repository,dailyPlanner,LocalGemsUiState(),{})
            "goal" -> Grade9GoalScreen(state,dailyPlanner,"00000000-0000-0000-0000-000000000010",{})
            "unit-plan" -> {
                val unit=SourceCurricula.forStage(StudyStage.G9).units.first()
                com.ahmadmol.enmath.features.courses.SourceUnitScreen(StudyStage.G9,unit.id,{}) {G9PinUnitAction(state,dailyPlanner,unit.id)}
            }
            "resource-dialog" -> com.ahmadmol.enmath.features.library.G9OrganizeFileDialog(
                StudentFile("preview-pdf","content://preview/document/1","PREVIEW ملف الجبر.pdf","application/pdf"),StudentCatalog.scope(state),repository,{})
            "favorites" -> Grade9Scaffold("preview-favorites") {item {com.ahmadmol.enmath.features.library.Grade9SavedReferences(state,dailyWorkspace,{})}}

            "files" -> Grade9FilesScreen(state, repository, {})
            "planner" -> {
                val files = remember { mutableMapOf<String, Grade9PreviewPreferences>() }
                val planner = remember { PlannerRepository({ name -> files.getOrPut(name) { Grade9PreviewPreferences() } }) { state } }
                com.ahmadmol.enmath.features.planner.PlannerScreen(state, planner,
                    initialRead = remember(planner) {
                        val scope = StudentCatalog.scope(state)!!
                        planner.save(scope, 0, "preview-goal", PlanTask("00000000-0000-0000-0000-000000000010",
                            "PREVIEW مراجعة القواسم المشتركة", "الرياضيات", PlanPeriod.WEEK,
                            StudyPeriods.start(System.currentTimeMillis(), PlanPeriod.WEEK), PlanMode.TIME, 1, minutes = 15))
                        planner.read(scope) })
            }
            else -> Grade9LessonScreen(state, repository, {})
        }
        } }
    } }
}

@StudentSurfaceMatrix @Composable private fun Phase2G9Home() = PilotPreview("home")
@StudentSurfaceMatrix @Composable private fun Phase2G9Algebra() = PilotPreview("algebra")
@StudentSurfaceMatrix @Composable private fun Phase2G9Geometry() = PilotPreview("geometry")
@StudentSurfaceMatrix @Composable private fun Phase2G9Exercises() = PilotPreview("exercises")
@StudentSurfaceMatrix @Composable private fun Phase2G9Profile() = PilotPreview("profile")
@StudentSurfaceMatrix @Composable private fun Phase2G9Lesson() = PilotPreview("lesson")
@StudentSurfaceMatrix @Composable private fun Phase2G9Example() = PilotPreview("example")
@StudentSurfaceMatrix @Composable private fun Phase2G9Summary() = PilotPreview("summary")
@StudentSurfaceMatrix @Composable private fun Phase2G9Practice() = PilotPreview("practice")
@StudentSurfaceMatrix @Composable private fun Phase2G9Result() = PilotPreview("result")
@StudentSurfaceMatrix @Composable private fun Phase2G9Files() = PilotPreview("files")
