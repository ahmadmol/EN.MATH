package com.ahmadmol.enmath.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.ahmadmol.enmath.core.AssignmentSubmissionResult
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.*
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.assignments.*
import com.ahmadmol.enmath.features.courses.*
import com.ahmadmol.enmath.features.courses.interactive.*
import com.ahmadmol.enmath.features.explore.*
import com.ahmadmol.enmath.features.home.*
import com.ahmadmol.enmath.features.library.*
import com.ahmadmol.enmath.features.localgems.*
import com.ahmadmol.enmath.core.localgems.LocalGemsUiState
import com.ahmadmol.enmath.features.notifications.*
import com.ahmadmol.enmath.features.practice.*
import com.ahmadmol.enmath.features.profile.*
import com.ahmadmol.enmath.features.progress.*
import com.ahmadmol.enmath.features.reference.*
import com.ahmadmol.enmath.features.search.*
import com.ahmadmol.enmath.features.solver.*
import com.ahmadmol.enmath.features.student.*
import com.ahmadmol.enmath.features.teacher.*

fun NavGraphBuilder.studentGraph(
    nav: NavHostController,
    app: AppViewModel,
    solver: SolverViewModel,
    open: (String) -> Unit,
    logout: () -> Unit,
    guarded: (() -> Unit) -> Unit,
    switchRole: (() -> Unit)?,
) {
    fun route(path: String) {
        open(path)
    }
    fun openHistory(id: String) {
        val item = app.state.value.product.history.find { it.id == id } ?: return
        if (item.topicId == "graph") { solver.graph(item.expression); route("explore") }
        else route("solver/solution/$id")
    }
    fun lesson(id: String) {
        route("lesson/$id")
    }
    fun solve(expression: String, topic: String, owner: String?) {
        val id = app.history(expression, topic, owner)
        if (id.isNotEmpty()) route("solver/solution/$id")
    }
    composable(Routes.Home) {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val gems = app.gems?.ui?.collectAsStateWithLifecycle()?.value ?: LocalGemsUiState()
        DualStageHome(state, ::route, gems, (app.repository as? LocalDemoRepository)?.planner)
    }
    composable(Routes.Study) {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        ScopedStudyScreen(state, ::route)
    }
    composable("planner") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        (app.repository as? LocalDemoRepository)?.let { com.ahmadmol.enmath.features.planner.PlannerScreen(state, it.planner) }
    }
    composable("review/history") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        PreservedLearningHistory(state)
    }
    composable("review") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        RecordedProgressScreen(state, ::route, review = true)
    }
    composable(Routes.Book) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        if (state.user?.studyStage == StudyStage.G9 && state.catalogAvailable) {
            Grade9BookScreen(e.arguments?.getString("id").orEmpty(), ::route); return@composable
        }
        val ticket = LearningRules.sessionOwner(state)
        if (state.user?.studyStage == StudyStage.BAC && state.catalogAvailable && BacSourceCatalog.book(e.arguments?.getString("id").orEmpty()) != null) {
            BacSourceBookScreen(e.arguments?.getString("id").orEmpty(), ::route); return@composable
        }
        if (state.books.none { it.id == e.arguments?.getString("id") }) { StageContentUnavailable(); return@composable }
        BookDetailsScreen(
            e.arguments?.getString("id").orEmpty(),
            state.copy(completed = StudentCatalog.completed(state)),
            { route("topic/$it") },
            ::lesson,
        )
    }
    composable("study/catalog") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        ScopedStudyScreen(state, ::route)
    }
    composable("study/demo") {
        val state by app.state.collectAsStateWithLifecycle()
        ScopedStudyScreen(state, ::route, showDemo = true)
    }
    composable("demo/book/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        if (state.user?.studyStage != StudyStage.BAC) { StageContentUnavailable(); return@composable }
        BookDetailsScreen(e.arguments?.getString("id").orEmpty(), state.copy(completed = StudentCatalog.completed(state)),
            { route("topic/$it") }, ::lesson)
    }
    composable(Routes.Topic) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        if (state.user?.studyStage == StudyStage.BAC && state.catalogAvailable && BacSourceCatalog.unit(e.arguments?.getString("id").orEmpty()) != null) {
            BacSourceUnitScreen(e.arguments?.getString("id").orEmpty(), ::route, { nav.popBackStack() }); return@composable
        }
        ScopedStudyScreen(state, ::route, e.arguments?.getString("id").orEmpty())
    }
    composable(Routes.Lesson) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val id = e.arguments?.getString("id").orEmpty()
        if (state.user?.studyStage == StudyStage.G9 && state.catalogAvailable) {
            Grade9LessonScreen(id, state, ::route, (app.repository as? LocalDemoRepository)?.planner); return@composable
        }
        if (state.user?.studyStage == StudyStage.BAC && state.catalogAvailable && BacSourceCatalog.lesson(id) != null) {
            BacSourceLessonScreen(id, state, (app.repository as? LocalDemoRepository)?.planner, ::route, { nav.popBackStack() }); return@composable
        }
        if (state.lesson(id) == null) { StageContentUnavailable(); return@composable }
        val lessonOwner by rememberSaveable(id, "gems-lesson-owner") { mutableStateOf(ticket) }
        LaunchedEffect(id, state.user?.localId) { app.openLesson(id, lessonOwner) }
        if (id == LimitsLessonSequence.lessonId) {
            val vm: InteractiveLessonViewModel = viewModel(e)
            val interactiveOwner = vm.gemsOwner(ticket)
            InteractiveLimitsScreen(
                state,
                vm,
                { if (!app.completeInteractive(id, vm.answers.value, interactiveOwner)) vm.releaseCompletion() },
                { route("practice/${LimitsLessonSequence.practiceId}") },
            )
        } else
            LessonScreen(
                id = id,
                state = state,
                onComplete = { app.complete(id, lessonOwner) },
                onPractice = { route("practice/$it") },
                onNext = ::lesson,
                onBookmark = { app.bookmark(id, startedBy = ticket) },
                onVerified = { answer, owner -> app.verifyLesson(id, answer, owner) },
                onBack = { nav.popBackStack() },
                onSearch = { route("search") },
                onNotification = { route("notifications") },
            )
    }
    composable("practice/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val id = e.arguments?.getString("id").orEmpty()
        if (state.user?.studyStage == StudyStage.G9 && state.catalogAvailable) {
            Grade9PracticeStatusScreen(id); return@composable
        }
        if (state.user?.studyStage == StudyStage.BAC && state.catalogAvailable && (BacSourceCatalog.lesson(id) != null || BacSourceCatalog.section(id) != null)) {
            BacSourcePracticeStatus(id); return@composable
        }
        val set = state.practiceSets.find { it.id == id }
        if (set == null) { StageContentUnavailable(); return@composable }
        val vm: QuizViewModel = viewModel(e)
        val practiceOwner = vm.gemsOwner(ticket)
        val finish: (Map<String, String>, Int) -> Unit = { answers, seconds ->
            if (app.recordPractice(id, answers, seconds, practiceOwner, vm.commandId))
                nav.navigate("practice/$id/result") { popUpTo("practice/{id}") { inclusive = true } }
            else vm.releaseFinish()
        }
        if (id == LimitsLessonSequence.practiceId) LimitsPracticeScreen(state, vm, finish)
        else
            PracticeScreen(
                set?.questionIds.orEmpty(),
                set?.title?.text().orEmpty(),
                vm,
                product = state.product,
                onFinish = finish,
            )
    }
    composable("practice/{id}/result") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val id = e.arguments?.getString("id").orEmpty()
        SavedPracticeResultScreen(state, id, ::route)
    }
    composable(Routes.Solver) {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val owner = ticket
        if (state.user?.studyStage == StudyStage.G9) { StageContentUnavailable(); return@composable }
        SolverWorkspace(solver, state, { expression, topic ->
            val result = app.history(expression, topic, owner)
            if (result.isNotEmpty()) route("solver/solution/$result")
        }, { path ->
            if (path == "explore") solver.graph(solver.expression.value)
            route(path)
        })
    }
    composable("solver/scan") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val owner = ticket
        SolverScanScreen {
            if (LearningRules.acceptsCommand(app.state.value, owner)) {
                solver.edit(it)
                nav.popBackStack()
            }
        }
    }
    composable("solver/history") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        SolverHistoryScreen(
            state,
            ::openHistory,
            { id ->
                app.product(ticket) { p ->
                    p.copy(
                        history = p.history.filterNot { it.id == id },
                        savedProblems = p.savedProblems - id,
                    )
                }
            },
            { app.product(ticket) { it.copy(history = emptyList(), savedProblems = emptySet()) } },
            onBack = { nav.popBackStack() },
        )
    }
    composable("solver/solution/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        if (state.user?.studyStage == StudyStage.G9) { StageContentUnavailable(); return@composable }
        val id = e.arguments?.getString("id").orEmpty()
        val h = state.product.history.find { it.id == id }
        val solution = DemoSolverEngine().solve(h?.expression.orEmpty())
        ProductSolutionScreen(
            solution,
            state,
            id in state.product.savedProblems,
            { app.bookmark(id, true, ticket) },
            {
                if (LearningRules.acceptsCommand(app.state.value, ticket)) {
                solver.edit(solution.expression)
                nav.navigate(Routes.Solver) {
                    popUpTo(Routes.Solver)
                    launchSingleTop = true
                }
                }
            },
            ::lesson,
            {
                if (LearningRules.acceptsCommand(app.state.value, ticket)) {
                    solver.edit(it)
                    solver.solve()
                    solve(it, solver.solution.topicId, ticket)
                }
            },
        )
    }
    composable("explore") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        if (state.user?.studyStage == StudyStage.G9) StageContentUnavailable() else androidx.compose.runtime.key(ticket, solver.graphInput) {
            ExploreScreen(solver.graphInput) { expression ->
                app.saveGraph(expression, ticket)
            }
        }
    }
    composable(Routes.Progress) {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        RecordedProgressScreen(state, ::route)
    }
    composable("assignments") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        ProtectedContent(state, guarded) {
            AssignmentListScreen(state, { route("assignments/$it") })
        }
    }
    composable("assignments/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val id = e.arguments?.getString("id").orEmpty()
        ProtectedContent(state, guarded) {
            AssignmentDetailsScreen(
                id,
                state,
                { route("assignments/$id/work") },
                { route("assignments/$id/result") },
            )
        }
    }
    composable("assignments/{id}/work") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val id = e.arguments?.getString("id").orEmpty()
        val a = state.product.assignments.find { it.id == id }
        val vm: QuizViewModel = viewModel(e)
        val assignmentOwner = vm.gemsOwner(ticket)
        var rejectionReason by remember { mutableStateOf<AssignmentSubmissionResult?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }
        ProtectedContent(state, guarded) {
            Column(Modifier.fillMaxSize()) {
                rejectionReason?.let { reason ->
                    val message = when (reason) {
                        is AssignmentSubmissionResult.DeadlinePassed ->
                            if (state.language == Language.Arabic)
                                "انتهى موعد تسليم الواجب. تعذر حفظ التسليم."
                            else
                                "The assignment deadline has passed. Submission could not be saved."
                        is AssignmentSubmissionResult.AttemptsExhausted ->
                            if (state.language == Language.Arabic)
                                "استنفدت جميع المحاولات المتاحة لهذا الواجب."
                            else
                                "You have used all available attempts for this assignment."
                        is AssignmentSubmissionResult.GuestUser ->
                            if (state.language == Language.Arabic)
                                "الزائر لا يمكنه تسليم الواجبات. يرجى تسجيل الدخول."
                            else
                                "Guest users cannot submit assignments. Please sign in."
                        else ->
                            if (state.language == Language.Arabic)
                                "تعذر تسليم الواجب."
                            else
                                "Could not submit assignment."
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("assignment-rejection-banner")
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                Box(Modifier.weight(1f)) {
                    PracticeScreen(
                        a?.questionIds.orEmpty(),
                        a?.title?.text().orEmpty(),
                        vm,
                        hints = a?.hints == true,
                        assignment = true,
                        product = state.product,
                    ) { answers, seconds ->
                        if (isSubmitting) return@PracticeScreen
                        isSubmitting = true
                        val res = app.submitAssignment(id, answers, seconds, assignmentOwner, vm.commandId)
                        if (res is AssignmentSubmissionResult.Submitted) {
                            nav.navigate("assignments/$id/result") {
                                popUpTo("assignments/{id}/work") { inclusive = true }
                            }
                        } else {
                            isSubmitting = false
                            vm.releaseFinish()
                            rejectionReason = res
                        }
                    }
                }
            }
        }
    }
    composable("assignments/{id}/result") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        ProtectedContent(state, guarded) {
            AssignmentResultScreen(e.arguments?.getString("id").orEmpty(), state)
        }
    }
    composable("library") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        LibraryScreen(state, ::lesson, ::openHistory) {
            if (state.user?.studyStage == StudyStage.G9 && state.catalogAvailable)
                Grade9LibraryLinks(state, (app.repository as? LocalDemoRepository)?.planner, ::route)
            else BacSourceLibraryLinks(state, (app.repository as? LocalDemoRepository)?.planner, ::route)
        }
    }
    composable("search") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        SearchScreen(state, { app.search(it, ticket) }, ::route)
    }
    composable("notifications") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        NotificationsScreen(
            state,
            false,
            { id -> app.product(ticket) { it.copy(readNotifications = it.readNotifications + id) } },
            {
                app.product(ticket) {
                    it.copy(
                        readNotifications =
                            it.readNotifications +
                                it.notifications.filterNot { it.teacher }.map { it.id }
                    )
                }
            },
            ::route,
        )
    }
    composable(Routes.Profile) {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        val gems = app.gems?.ui?.collectAsStateWithLifecycle()?.value ?: LocalGemsUiState()
        ProductProfileScreen(state, false, open, logout, switchRole, app::language, gems, (app.repository as? LocalDemoRepository)?.planner)
    }
    composable("settings") {
        val state by app.state.collectAsStateWithLifecycle()
        val ticket = LearningRules.sessionOwner(state)
        SettingsScreen(
            state,
            app::theme,
            app::language,
            { enabled, reminder ->
                app.product(ticket) { it.copy(notificationsEnabled = enabled, reminder = reminder) }
            },
            logout,
        )
    }
}

@Composable
private fun ProtectedContent(
    state: LearningState,
    guarded: (() -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    if (state.user?.type == AccountType.Guest && state.user.localId == null)
        ScreenPage {
            EmptyState(label(TextKey.t_e7bae5277c), label(TextKey.t_0656950f60))
            PrimaryButton(label(TextKey.t_604824dd52), { guarded {} })
        }
    else content()
}


@Composable
private fun StageContentUnavailable() {
    ScreenPage { EmptyState(tr("Stage content unavailable", "محتوى المرحلة غير متاح"),
        tr("This content belongs to another stage. Your file and progress were not changed.",
            "هذا المحتوى يخص مرحلة أخرى. لم يتغير ملفك أو تقدمك.")) }
}

fun NavGraphBuilder.teacherGraph(
    nav: NavHostController,
    app: AppViewModel,
    open: (String) -> Unit,
    logout: () -> Unit,
    switchRole: (() -> Unit)?,
) {
    composable(Routes.TeacherHome) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherHome(state, open, switchRole)
    }
    composable(TeacherReferenceRoutes.Home) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherHome(state, open, switchRole)
    }
    composable(TeacherReferenceRoutes.Study) {
        val state by app.state.collectAsStateWithLifecycle()
        StudyScreen(
            state,
            { open("teacher/reference/book/$it") },
            { open("teacher/reference/lesson/$it") },
            { path ->
                open(if (path == "explore") TeacherReferenceRoutes.Lab else Routes.TeacherResources)
            },
        )
    }
    composable(TeacherReferenceRoutes.Solver) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherSolverWorkspace(app, state, open)
    }
    composable(TeacherReferenceRoutes.Lab) { ExploreScreen() }
    composable("teacher/reference/study/catalog") {
        val state by app.state.collectAsStateWithLifecycle()
        StudyScreen(
            state,
            { open("teacher/reference/book/$it") },
            { open("teacher/reference/lesson/$it") },
            { path ->
                open(
                    when (path) {
                        "assignments" -> Routes.TeacherAssignments
                        "explore" -> TeacherReferenceRoutes.Lab
                        else -> Routes.TeacherResources
                    }
                )
            },
        )
    }
    composable("teacher/reference/book/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        BookDetailsScreen(
            e.arguments?.getString("id").orEmpty(),
            state,
            { open("teacher/reference/topic/$it") },
            { open("teacher/reference/lesson/$it") },
        )
    }
    composable("teacher/reference/topic/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        ReferencePage("screen-teacher-topic") {
            val topic = state.topic(e.arguments?.getString("id").orEmpty())
            RefHeading(topic?.title?.text().orEmpty())
            topic?.lessons?.forEach { lesson ->
                RefButton(
                    lesson.title.text(),
                    { open("teacher/reference/lesson/${lesson.id}") },
                    selected = false,
                )
            }
        }
    }
    composable(TeacherReferenceRoutes.Lesson) { entry ->
        val state by app.state.collectAsStateWithLifecycle()
        TeacherReferenceLesson(entry.arguments?.getString("id").orEmpty(), state, open)
    }
    composable(Routes.TeacherClasses) {
        val state by app.state.collectAsStateWithLifecycle()
        ClassesScreen(
            state,
            {
                val id = app.createClass(it)
                open("teacher/classes/$id")
            },
            { open("teacher/classes/$it") },
        )
    }
    composable("teacher/classes/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        TeacherClassDetails(
            id,
            state,
            { open("teacher/classes/$id/students/$it") },
            { open("teacher/assignments/$it") },
        )
    }
    composable("teacher/classes/{id}/students/{student}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        TeacherStudentDetails(
            e.arguments?.getString("id").orEmpty(),
            e.arguments?.getString("student").orEmpty(),
            state,
            { open("teacher/assignments/$it/responses/${e.arguments?.getString("student")}") },
        )
    }
    composable(Routes.TeacherAssignments) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherAssignmentsScreen(
            state,
            { open("teacher/assignments/new") },
            { open("teacher/assignments/$it") },
        )
    }
    composable("teacher/assignments/new") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val vm: AssignmentBuilderViewModel = viewModel(e)
        AssignmentBuilderScreen(
            state,
            vm,
            { q -> app.product { it.copy(questions = it.questions + q) } },
            { a ->
                app.publish(a)
                nav.navigate("teacher/assignments/${a.id}") {
                    popUpTo("teacher/assignments/new") { inclusive = true }
                }
            },
        )
    }
    composable("teacher/assignments/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        TeacherResultsScreen(id, state, { open("teacher/assignments/$id/responses/$it") })
    }
    composable("teacher/assignments/{id}/responses/{student}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        AssignmentResultScreen(
            e.arguments?.getString("id").orEmpty(),
            state,
            e.arguments?.getString("student").orEmpty(),
            { body ->
                app.feedback(
                    e.arguments?.getString("id").orEmpty(),
                    e.arguments?.getString("student").orEmpty(),
                    body,
                )
            },
        )
    }
    composable(Routes.TeacherResources) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherResourcesScreen(state, app::createResource, app::shareResource, open)
    }
    composable(Routes.TeacherProfile) {
        val state by app.state.collectAsStateWithLifecycle()
        ProductProfileScreen(state, true, open, logout, switchRole, app::language)
    }
    composable("teacher/settings") {
        val state by app.state.collectAsStateWithLifecycle()
        SettingsScreen(
            state,
            app::theme,
            app::language,
            { enabled, reminder ->
                app.product { it.copy(notificationsEnabled = enabled, reminder = reminder) }
            },
            logout,
        )
    }
    composable("teacher/notifications") {
        val state by app.state.collectAsStateWithLifecycle()
        NotificationsScreen(
            state,
            true,
            { id -> app.product { it.copy(readNotifications = it.readNotifications + id) } },
            {
                app.product {
                    it.copy(
                        readNotifications =
                            it.readNotifications +
                                it.notifications.filter { it.teacher }.map { it.id }
                    )
                }
            },
            open,
        )
    }
}
