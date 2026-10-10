package com.ahmadmol.enmath.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.navigation
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.LocalDemoRepository
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.auth.*
import com.ahmadmol.enmath.features.courses.*
import com.ahmadmol.enmath.features.onboarding.*
import com.ahmadmol.enmath.features.reference.*
import com.ahmadmol.enmath.features.solver.*
import com.ahmadmol.enmath.features.teacher.TeacherReferenceRoutes
import com.ahmadmol.enmath.features.teacher.TeacherReferenceBottomNav

@Composable
fun ENMathApp() {
    val context = LocalContext.current.applicationContext
    val repository = remember { LocalDemoRepository(context) }
    val app: AppViewModel = viewModel(factory = AppViewModel.factory(repository))
    val auth: AuthViewModel = viewModel()
    val solver: SolverViewModel = viewModel()
    val state by app.state.collectAsStateWithLifecycle()
    ENMathTheme(state.theme, state.language) {
        val nav = rememberNavController()
        val backStack by nav.currentBackStackEntryAsState()
        val route = backStack?.destination?.route
        val selectedTab = Routes.selectedTab(route)
        val teacher = state.user?.type == AccountType.Teacher
        val tabs = if (teacher) Routes.teacherTabs else Routes.tabs
        val inMain = selectedTab in tabs && route !in Routes.focus
        val teacherReference = teacher && route in TeacherReferenceRoutes.screens
        val reference = teacherReference ||
            !teacher &&
                route in listOf(Routes.Home, Routes.Study, Routes.Solver, Routes.Profile, "explore")
        if (reference) {
            val activity = androidx.activity.compose.LocalActivity.current
            SideEffect {
                activity?.window?.let { window ->
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                        .apply {
                            isAppearanceLightStatusBars = false
                            isAppearanceLightNavigationBars = false
                        }
                }
            }
        }
        var accountPrompt by remember { mutableStateOf(false) }
        val topTitle =
            when (route) {
                Routes.Home -> "EN.MATH"
                Routes.Study -> label(TextKey.t_30593f37e9)
                Routes.Solver -> label(TextKey.t_28fd8d2a78)
                Routes.Progress -> label(TextKey.t_693fec8aa7)
                Routes.Profile -> label(TextKey.t_e269f67032)
                Routes.Book -> label(TextKey.t_66df4d3113)
                Routes.Topic -> label(TextKey.t_686ffa1014)
                Routes.Lesson -> label(TextKey.t_553359b746)
                Routes.Solution,
                "solver/solution/{id}" -> label(TextKey.t_ada94e31a4)
                "practice/{id}" -> label(TextKey.t_054b6afa54)
                "practice/{id}/result" -> label(TextKey.t_7b1a0e1b9f)
                "explore" -> label(TextKey.t_a0fc50de5a)
                "solver/history" -> label(TextKey.t_ae1c6ca083)
                "solver/scan" -> label(TextKey.t_af36e1e442)
                "assignments",
                "assignments/{id}" -> label(TextKey.t_f6202fc5ef)
                "assignments/{id}/work" -> label(TextKey.t_a311f1396c)
                "assignments/{id}/result" -> label(TextKey.t_4b58e315cf)
                "library" -> label(TextKey.t_455904a5d7)
                "search" -> label(TextKey.t_6bd3834813)
                "notifications",
                "teacher/notifications" -> label(TextKey.t_bda3e17468)
                "settings",
                "teacher/settings" -> label(TextKey.t_f3f3f790e0)
                Routes.TeacherHome -> label(TextKey.t_55d0767369)
                Routes.TeacherClasses,
                "teacher/classes/{id}" -> label(TextKey.t_2ade063f4e)
                "teacher/classes/{id}/students/{student}" -> label(TextKey.t_34ebd8f633)
                Routes.TeacherAssignments,
                "teacher/assignments/{id}" -> label(TextKey.t_4b3bb23d90)
                "teacher/assignments/new" -> label(TextKey.t_4c5461762c)
                "teacher/assignments/{id}/responses/{student}" -> label(TextKey.t_d4db0600b7)
                Routes.TeacherResources -> label(TextKey.t_7eb98c7118)
                Routes.TeacherProfile -> label(TextKey.t_e5cfa4402b)
                else -> "EN.MATH"
            }
        fun clearNavigate(destination: String) {
            nav.navigate(destination) {
                popUpTo(nav.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
        fun tab(destination: String) {
            nav.navigate(destination) {
                popUpTo(
                    if (app.state.value.user?.type == AccountType.Teacher) Routes.TeacherHome
                    else Routes.Home
                ) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
        fun openLesson(id: String) {
            app.openLesson(id)
            nav.navigate("lesson/$id") { launchSingleTop = true }
        }
        fun signIn() {
            val user = auth.user()
            app.signIn(user)
            auth.clearSecrets()
            clearNavigate(if (user.type == AccountType.Teacher) Routes.TeacherMain else Routes.Main)
        }
        fun open(path: String) {
            if (
                path in
                    (if (app.state.value.user?.type == AccountType.Teacher) Routes.teacherTabs + TeacherReferenceRoutes.screens
                    else Routes.tabs)
            )
                tab(path)
            else nav.navigate(path) { launchSingleTop = true }
        }
        fun logout() {
            app.logout()
            auth.reset()
            solver.reset()
            clearNavigate(Routes.Started)
        }
        fun switchDemoRole() {
            val current = app.state.value.user ?: return
            val next = if (current.type == AccountType.Teacher) AccountType.Student else AccountType.Teacher
            app.signIn(current.copy(type = next))
            auth.chooseRole(next)
            clearNavigate(if (next == AccountType.Teacher) Routes.TeacherMain else Routes.Main)
        }
        fun guarded(action: () -> Unit) {
            if (app.state.value.user?.type == AccountType.Guest) accountPrompt = true else action()
        }
        LaunchedEffect(route, state.user?.type) {
            val public =
                route in
                    listOf(
                        Routes.Splash,
                        Routes.Onboarding,
                        Routes.Started,
                        Routes.Login,
                        Routes.AccountChoice,
                        Routes.RegisterStudent,
                        Routes.RegisterTeacher,
                        Routes.Verify,
                        Routes.Forgot,
                        Routes.NewPassword,
                    )
            if (route != null && !public) {
                if (state.user == null) clearNavigate(Routes.Started)
                else if (teacher && !route.startsWith("teacher")) clearNavigate(Routes.TeacherMain)
                else if (!teacher && route.startsWith("teacher")) clearNavigate(Routes.Main)
            }
        }
        BoxWithConstraints {
            val wide = maxWidth >= 768.dp
            Scaffold(
                containerColor =
                    if (reference) RefPalette.background else MaterialTheme.colorScheme.background,
                topBar = {
                    if (
                        !reference &&
                            route != Routes.Splash &&
                            route != Routes.Onboarding &&
                            route != Routes.Started
                    ) {
                        AppTopBar(
                            topTitle,
                            if (route !in tabs) ({ nav.popBackStack() }) else null,
                            if (selectedTab in tabs && !teacher) ({ open("search") }) else null,
                            if (selectedTab in tabs)
                                ({
                                    open(if (teacher) "teacher/notifications" else "notifications")
                                })
                            else null,
                            state.product.notifications.count {
                                it.teacher == teacher && it.id !in state.product.readNotifications
                            },
                        )
                    }
                },
                bottomBar = {
                    if (teacherReference) TeacherReferenceBottomNav(route, ::open)
                    else if (reference) RefBottomNav(route, ::open)
                    else if (inMain && !wide)
                        NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                            tabs.forEach { destination ->
                                NavigationBarItem(
                                    selected = selectedTab == destination,
                                    onClick = { tab(destination) },
                                    icon = { Icon(tabIcon(destination), null) },
                                    label = { Text(tabLabel(destination)) },
                                    modifier = Modifier.testTag("nav-$destination"),
                                )
                            }
                        }
                },
            ) { padding ->
                Row(Modifier.fillMaxSize().padding(padding).imePadding()) {
                    if (inMain && wide && !reference)
                        NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                            Spacer(Modifier.height(Space.xl))
                            tabs.forEach { destination ->
                                NavigationRailItem(
                                    selected = selectedTab == destination,
                                    onClick = { tab(destination) },
                                    icon = { Icon(tabIcon(destination), null) },
                                    label = { Text(tabLabel(destination)) },
                                    modifier = Modifier.testTag("nav-$destination"),
                                )
                            }
                        }
                    NavHost(
                        navController = nav,
                        startDestination = Routes.Splash,
                        modifier = Modifier.weight(1f),
                        enterTransition = { fadeIn(tween(180)) },
                        exitTransition = { fadeOut(tween(120)) },
                    ) {
                        composable(Routes.Splash) {
                            SplashScreen {
                                clearNavigate(
                                    when {
                                        teacher -> Routes.TeacherMain
                                        state.user != null -> Routes.Main
                                        state.onboarded -> Routes.Started
                                        else -> Routes.Onboarding
                                    }
                                )
                            }
                        }
                        composable(Routes.Onboarding) {
                            OnboardingScreen {
                                repository.finishOnboarding()
                                clearNavigate(Routes.Started)
                            }
                        }
                        composable(Routes.Started) {
                            GetStartedScreen(
                                onRole = {
                                    auth.chooseRole(it)
                                    nav.navigate(Routes.Login)
                                },
                                onGuest = {
                                    app.signIn(
                                        User(
                                            if (state.language == Language.Arabic) "ضيف"
                                            else "Guest",
                                            "",
                                            AccountType.Guest,
                                        )
                                    )
                                    clearNavigate(Routes.Main)
                                },
                            )
                        }
                        composable(Routes.Login) {
                            LoginScreen(
                                auth,
                                ::signIn,
                                { nav.navigate(Routes.AccountChoice) },
                                {
                                    auth.clearSecrets()
                                    nav.navigate(Routes.Forgot)
                                },
                            )
                        }
                        composable(Routes.AccountChoice) {
                            AccountChoiceScreen { role ->
                                auth.chooseRole(role)
                                nav.navigate(
                                    if (role == AccountType.Teacher) Routes.RegisterTeacher
                                    else Routes.RegisterStudent
                                )
                            }
                        }
                        composable(Routes.RegisterStudent) {
                            RegistrationScreen(auth, false) { nav.navigate(Routes.Verify) }
                        }
                        composable(Routes.RegisterTeacher) {
                            RegistrationScreen(auth, true) { nav.navigate(Routes.Verify) }
                        }
                        composable(Routes.Verify) {
                            VerificationScreen(auth) {
                                if (auth.resetFlow.value) {
                                    auth.clearSecrets()
                                    nav.navigate(Routes.NewPassword)
                                } else signIn()
                            }
                        }
                        composable(Routes.Forgot) {
                            ForgotPasswordScreen(auth) { nav.navigate(Routes.Verify) }
                        }
                        composable(Routes.NewPassword) {
                            NewPasswordScreen(auth) {
                                nav.navigate(Routes.Login) {
                                    popUpTo(Routes.Login)
                                    launchSingleTop = true
                                }
                            }
                        }
                        navigation(startDestination = Routes.Home, route = Routes.Main) {
                            studentGraph(nav, app, solver, ::open, ::logout, ::guarded, ::switchDemoRole)
                        }
                        navigation(
                            startDestination = Routes.TeacherHome,
                            route = Routes.TeacherMain,
                        ) {
                            teacherGraph(nav, app, ::open, ::logout, ::switchDemoRole)
                        }
                    }
                }
            }
        }
        if (accountPrompt)
            AccountPrompt(
                { accountPrompt = false },
                {
                    accountPrompt = false
                    auth.chooseRole(AccountType.Student)
                    clearNavigate(Routes.RegisterStudent)
                },
            )
    }
}

@Composable
private fun tabLabel(route: String) =
    when (route) {
        Routes.TeacherHome -> label(TextKey.t_bc2753a2c0)
        Routes.TeacherClasses -> label(TextKey.t_2ade063f4e)
        Routes.TeacherAssignments -> label(TextKey.t_f6202fc5ef)
        Routes.TeacherResources -> label(TextKey.t_7eb98c7118)
        Routes.TeacherProfile -> label(TextKey.t_e269f67032)
        Routes.Home -> label(TextKey.t_b5a4c86040)
        Routes.Study -> label(TextKey.t_30593f37e9)
        Routes.Solver -> label(TextKey.t_6e8911a0d6)
        Routes.Progress -> label(TextKey.t_693fec8aa7)
        else -> label(TextKey.t_e269f67032)
    }

private fun tabIcon(route: String): ImageVector =
    when (route) {
        Routes.TeacherHome -> Icons.Outlined.Dashboard
        Routes.TeacherClasses -> Icons.Outlined.Groups
        Routes.TeacherAssignments -> Icons.AutoMirrored.Outlined.Assignment
        Routes.TeacherResources -> Icons.Outlined.Folder
        Routes.TeacherProfile -> Icons.Outlined.Person
        Routes.Home -> Icons.Outlined.Home
        Routes.Study -> Icons.Outlined.School
        Routes.Solver -> Icons.Outlined.Calculate
        Routes.Progress -> Icons.AutoMirrored.Outlined.TrendingUp
        else -> Icons.Outlined.Person
    }
