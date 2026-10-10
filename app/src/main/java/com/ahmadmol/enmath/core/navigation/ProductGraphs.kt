package com.ahmadmol.enmath.core.navigation

import androidx.compose.runtime.*
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
import com.ahmadmol.enmath.features.explore.*
import com.ahmadmol.enmath.features.home.*
import com.ahmadmol.enmath.features.library.*
import com.ahmadmol.enmath.features.notifications.*
import com.ahmadmol.enmath.features.practice.*
import com.ahmadmol.enmath.features.profile.*
import com.ahmadmol.enmath.features.progress.*
import com.ahmadmol.enmath.features.reference.*
import com.ahmadmol.enmath.features.search.*
import com.ahmadmol.enmath.features.solver.*
import com.ahmadmol.enmath.features.teacher.*

fun NavGraphBuilder.studentGraph(
    nav: NavHostController,
    app: AppViewModel,
    solver: SolverViewModel,
    open: (String) -> Unit,
    logout: () -> Unit,
    guarded: (() -> Unit) -> Unit,
    switchRole: () -> Unit,
) {
    fun route(path: String) = open(path)
    fun lesson(id: String) {
        app.openLesson(id)
        route("lesson/$id")
    }
    fun solve(expression: String, topic: String) {
        val id = app.history(expression, topic)
        route("solver/solution/$id")
    }
    composable(Routes.Home) {
        val state by app.state.collectAsStateWithLifecycle()
        ReferenceHome(state, ::route)
    }
    composable(Routes.Study) {
        val state by app.state.collectAsStateWithLifecycle()
        ReferenceStudy(::route)
    }
    composable(Routes.Book) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        BookDetailsScreen(
            e.arguments?.getString("id").orEmpty(),
            state,
            { route("topic/$it") },
            ::lesson,
        )
    }
    composable(Routes.Topic) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        TopicLessonsScreen(
            e.arguments?.getString("id").orEmpty(),
            state,
            ::lesson,
            { route("practice/$it") },
        )
    }
    composable(Routes.Lesson) { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        LessonScreen(
            id,
            state,
            { app.complete(id) },
            { route("practice/$it") },
            ::lesson,
            { guarded { app.bookmark(id) } },
        )
    }
    composable("practice/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        val set = ProductSeed.sets.find { it.id == id }
        val vm: QuizViewModel = viewModel(e)
        PracticeScreen(
            set?.questionIds.orEmpty(),
            set?.title?.text().orEmpty(),
            vm,
            product = state.product,
        ) { answers, seconds ->
            app.recordPractice(id, answers, seconds)
            nav.navigate("practice/$id/result") { popUpTo("practice/{id}") { inclusive = true } }
        }
    }
    composable("practice/{id}/result") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        PracticeResultScreen(
            id,
            state,
            {
                nav.navigate("practice/$id") {
                    popUpTo("practice/{id}/result") { inclusive = true }
                }
            },
            { route("topic/${ProductSeed.sets.find {it.id==id}?.topicId}") },
        )
    }
    composable(Routes.Solver) {
        val state by app.state.collectAsStateWithLifecycle()
        ReferenceSolver()
    }
    composable("solver/scan") {
        val state by app.state.collectAsStateWithLifecycle()
        SolverScanScreen {
            solver.edit(it)
            nav.popBackStack()
        }
    }
    composable("solver/history") {
        val state by app.state.collectAsStateWithLifecycle()
        SolverHistoryScreen(
            state,
            { route("solver/solution/$it") },
            { id ->
                app.product { p ->
                    p.copy(
                        history = p.history.filterNot { it.id == id },
                        savedProblems = p.savedProblems - id,
                    )
                }
            },
            { app.product { it.copy(history = emptyList(), savedProblems = emptySet()) } },
        )
    }
    composable("solver/solution/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        val id = e.arguments?.getString("id").orEmpty()
        val h = state.product.history.find { it.id == id }
        val solution = DemoSolverEngine().solve(h?.expression.orEmpty())
        ProductSolutionScreen(
            solution,
            state,
            id in state.product.savedProblems,
            { guarded { app.bookmark(id, true) } },
            {
                solver.edit(solution.expression)
                nav.navigate(Routes.Solver) {
                    popUpTo(Routes.Solver)
                    launchSingleTop = true
                }
            },
            ::lesson,
            {
                solver.edit(it)
                solver.solve()
                solve(it, solver.solution.topicId)
            },
        )
    }
    composable("explore") {
        val state by app.state.collectAsStateWithLifecycle()
        ReferenceGraph()
    }
    composable(Routes.Progress) {
        val state by app.state.collectAsStateWithLifecycle()
        ProductProgressScreen(state)
    }
    composable("assignments") {
        val state by app.state.collectAsStateWithLifecycle()
        ProtectedContent(state, guarded) {
            AssignmentListScreen(state, { route("assignments/$it") })
        }
    }
    composable("assignments/{id}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
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
        val id = e.arguments?.getString("id").orEmpty()
        val a = state.product.assignments.find { it.id == id }
        val vm: QuizViewModel = viewModel(e)
        ProtectedContent(state, guarded) {
            PracticeScreen(
                a?.questionIds.orEmpty(),
                a?.title?.text().orEmpty(),
                vm,
                hints = a?.hints == true,
                assignment = true,
                product = state.product,
            ) { answers, seconds ->
                app.submitAssignment(id, answers, seconds)
                nav.navigate("assignments/$id/result") {
                    popUpTo("assignments/{id}/work") { inclusive = true }
                }
            }
        }
    }
    composable("assignments/{id}/result") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        ProtectedContent(state, guarded) {
            AssignmentResultScreen(e.arguments?.getString("id").orEmpty(), state)
        }
    }
    composable("library") {
        val state by app.state.collectAsStateWithLifecycle()
        ProtectedContent(state, guarded) {
            LibraryScreen(state, ::lesson, { route("solver/solution/$it") })
        }
    }
    composable("search") {
        val state by app.state.collectAsStateWithLifecycle()
        SearchScreen(state, app::search, ::route)
    }
    composable("notifications") {
        val state by app.state.collectAsStateWithLifecycle()
        NotificationsScreen(
            state,
            false,
            { id -> app.product { it.copy(readNotifications = it.readNotifications + id) } },
            {
                app.product {
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
        ReferenceProfile(state, app::language, switchRole)
    }
    composable("settings") {
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
}

@Composable
private fun ProtectedContent(
    state: LearningState,
    guarded: (() -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    if (state.user?.type == AccountType.Guest)
        ScreenPage {
            EmptyState(label(TextKey.t_e7bae5277c), label(TextKey.t_0656950f60))
            PrimaryButton(label(TextKey.t_604824dd52), { guarded {} })
        }
    else content()
}

fun NavGraphBuilder.teacherGraph(
    nav: NavHostController,
    app: AppViewModel,
    open: (String) -> Unit,
    logout: () -> Unit,
    switchRole: () -> Unit,
) {
    composable(Routes.TeacherHome) {
        TeacherReferenceDashboard(switchRole)
    }
    composable(TeacherReferenceRoutes.Home) {
        val state by app.state.collectAsStateWithLifecycle()
        TeacherReferenceHome(state, open)
    }
    composable(TeacherReferenceRoutes.Study) {
        TeacherReferenceStudy()
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
        ClassDetailsScreen(
            id,
            state,
            { open("teacher/classes/$id/students/$it") },
            { open("teacher/assignments/$it") },
        )
    }
    composable("teacher/classes/{id}/students/{student}") { e ->
        val state by app.state.collectAsStateWithLifecycle()
        StudentDetailsScreen(
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
        TeacherResourcesScreen(state, app::createResource, app::shareResource)
    }
    composable(Routes.TeacherProfile) {
        val state by app.state.collectAsStateWithLifecycle()
        ProductProfileScreen(state, true, open, logout)
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
