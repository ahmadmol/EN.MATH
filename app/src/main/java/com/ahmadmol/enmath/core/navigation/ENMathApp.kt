package com.ahmadmol.enmath.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.navigation
import com.ahmadmol.enmath.core.AppViewModel
import com.ahmadmol.enmath.core.data.LocalDemoRepository
import com.ahmadmol.enmath.core.design.*
import com.ahmadmol.enmath.core.design.tokens.ResourcePackPresentation
import com.ahmadmol.enmath.core.model.*
import com.ahmadmol.enmath.features.auth.*
import com.ahmadmol.enmath.features.courses.*
import com.ahmadmol.enmath.features.onboarding.*
import com.ahmadmol.enmath.features.reference.*
import com.ahmadmol.enmath.features.solver.*
import com.ahmadmol.enmath.features.teacher.TeacherReferenceBottomNav
import com.ahmadmol.enmath.features.teacher.TeacherReferenceRoutes
import com.ahmadmol.enmath.features.localgems.LocalGemsScreen
import com.ahmadmol.enmath.features.localgems.LocalWeeklyReviewScreen
import com.ahmadmol.enmath.core.localgems.LocalGemsPolicy

private class LocalRepositoryHolder(context: android.content.Context) : androidx.lifecycle.ViewModel() {
    val result = runCatching { LocalDemoRepository(context.applicationContext) }
}

@Composable
fun ENMathApp(providedNav: NavHostController? = null) {
    val context = LocalContext.current.applicationContext
    val holder: LocalRepositoryHolder = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = LocalRepositoryHolder(context) as T
    })
    val repositoryResult = holder.result
    if (repositoryResult.isFailure) {
        val failure = repositoryResult.exceptionOrNull()
        if (failure !is com.ahmadmol.enmath.core.data.CorruptLocalData &&
            failure !is com.ahmadmol.enmath.core.data.LocalDataWriteFailed) throw requireNotNull(failure)
        ENMathTheme(ThemeMode.System, Language.Arabic, student = true) {
            Surface(Modifier.fillMaxSize()) {
                Column(Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
                    Text("تعذّر قراءة البيانات المحلية — Local data needs recovery", style = MaterialTheme.typography.titleLarge)
                    Text("حُفظت البيانات الأصلية دون استبدال. أغلق التطبيق واطلب المساعدة لاستعادتها. لا تمسح بيانات التطبيق.\nOriginal data was preserved. Close the app and seek recovery support; do not clear app storage.")
                }
            }
        }
        return
    }
    val repository = repositoryResult.getOrThrow()
    val app: AppViewModel = viewModel(factory = AppViewModel.factory(repository, context))
    val demoAllowed = context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    val auth: AuthViewModel = viewModel(factory = AuthViewModel.factory(demoAllowed))
    val solver: SolverViewModel = viewModel()

    val state by app.state.collectAsStateWithLifecycle()
    val nav = providedNav ?: key(navigationSessionKey(state)) { rememberNavController() }
    var initialSplash by rememberSaveable { mutableStateOf(state.user == null && !state.onboarded) }
    if (initialSplash) {
        ENMathTheme(state.theme, state.language, student = state.user?.type != AccountType.Teacher) {
            SplashScreen { initialSplash = false }
        }
        return
    }
    if (state.user == null && !state.onboarded) {
        ENMathTheme(state.theme, state.language, student = true) {
            LocalOnboarding { repository.finishOnboarding() }
        }
        return
    }
    var demoEntry by rememberSaveable { mutableStateOf(false) }
    var renderedProfileId by rememberSaveable { mutableStateOf<String?>(null) }
    if (state.user?.localId == null && state.user?.type != AccountType.Teacher && (!demoEntry || state.user != null)) {
        ENMathTheme(state.theme, state.language, student = true) {
            LocalIdentityScreen(state,
                create = { name, stage, guest -> repository.createLocalProfile(name, stage, guest); app.refreshIdentity() },
                select = { id -> repository.selectLocalProfile(id); app.refreshIdentity() },
                importLegacy = { source, stage, name -> repository.importConfirmedLegacy(source, stage, name); app.refreshIdentity() },
                acknowledgeGuest = repository::acknowledgeGuestNotice,
                debugEntry = if (demoAllowed) ({
                    if (app.logout()) { auth.chooseRole(AccountType.Teacher); demoEntry = true }
                }) else null)
        }
        return
    }
    LaunchedEffect(state.user?.localId) {
        app.refreshIdentity()
        val id = state.user?.localId
        if (id != renderedProfileId) {
            solver.reset()
            if (id != null) {
                nav.navigate(Routes.Main) {
                    popUpTo(nav.graph.id) { inclusive = true }; launchSingleTop = true
                }
                Routes.tabs.forEach { nav.clearBackStack(it) }
            }
            renderedProfileId = id
        }
    }

    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
        app.gems?.refresh(app.state.value)
        app.replayPendingGems()
    }
    ENMathTheme(state.theme, state.language, student = state.user?.type != AccountType.Teacher) {
        if (state.storageIssue != null) AlertDialog(onDismissRequest = {},
            title = { Text(tr("Storage needs attention", "التخزين يحتاج مراجعة")) },
            text = { Text(tr("The latest operation was not acknowledged. Restart before retrying; do not clear app data.",
                "لم يُؤكَّد حفظ العملية الأخيرة. أعد التشغيل قبل المحاولة؛ لا تمسح بيانات التطبيق.")) },
            confirmButton = {})

        val backStack by nav.currentBackStackEntryAsState()
        val visibleEntries by nav.visibleEntries.collectAsStateWithLifecycle()
        val route = backStack?.destination?.route
        val selectedTab = Routes.selectedTab(route)
        val gemOwner = LocalGemsPolicy.owner(state.user)
        val gemSnackbar = remember(gemOwner) { androidx.compose.material3.SnackbarHostState() }
        LaunchedEffect(gemOwner, state.language) {
            app.gems?.notices?.collect { event ->
                if (event.owner == gemOwner && event.owner == LocalGemsPolicy.owner(app.state.value.user)) {
                    gemSnackbar.showSnackbar(if (state.language == Language.Arabic) "أحسنت! +${event.units} جواهر" else "Well done! +${event.units} gems")
                }
            }
        }
        val teacher = state.user?.type == AccountType.Teacher
        val tabs = if (teacher) Routes.teacherTabs else Routes.tabs
        val inMain = route in tabs
        val studentNavigationVisible = showStudentNavigation(
            route, visibleEntries.filterNot { it.destination is androidx.navigation.NavGraph }
                .map { it.destination.route }, state.user != null,
        )
        val teacherReference =
            teacher && route != Routes.TeacherHome && route in TeacherReferenceRoutes.screens
        val reference = false // All existing destinations share the approved app theme.
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
                "study/catalog" -> if (state.language == Language.Arabic) "كتالوج الكتب" else "Books Catalog"
                "gems/wallet" -> if (state.language == Language.Arabic) "جواهري" else "My Gems"
                "gems/review" -> if (state.language == Language.Arabic) "مراجعة الأسبوع" else "Weekly Review"
                Routes.Solver -> label(TextKey.t_28fd8d2a78)
                "planner" -> if (state.language == Language.Arabic) "خطتي الدراسية" else "My study plan"
                "review" -> if (state.language == Language.Arabic) "مراجعة الأخطاء" else "Mistake review"
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
                restoreState = destination != Routes.Home && destination != Routes.TeacherHome
            }
        }
        fun openLesson(id: String) {
            app.openLesson(id)
            nav.navigate("lesson/$id") { launchSingleTop = true }
        }
        fun signIn() {
            val user = auth.user()
            if (!app.signIn(user)) return
            auth.clearSecrets()
            clearNavigate(if (user.type == AccountType.Teacher) Routes.TeacherMain else Routes.Main)
        }
        fun open(path: String) {
            if (
                path in
                    (if (app.state.value.user?.type == AccountType.Teacher)
                        Routes.teacherTabs + TeacherReferenceRoutes.screens
                    else Routes.tabs)
            )
                tab(path)
            else nav.navigate(path) { launchSingleTop = true }
        }
        fun logout() {
            if (!app.logout()) return
            demoEntry = false
            renderedProfileId = null
            auth.reset()
            solver.reset()
            clearNavigate(Routes.Started)
        }
        fun switchDemoRole() {
            val next = app.switchDemoRole() ?: return
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
                else if (teacher && !route.startsWith("teacher") && route != "gems/wallet") clearNavigate(Routes.TeacherMain)
                else if (!teacher && route.startsWith("teacher")) clearNavigate(Routes.Main)
            }
        }
        ResourcePackPresentation(enabled = true) {
            BoxWithConstraints {
                val wide = maxWidth >= 768.dp
                val isPublic = route in listOf(
                    Routes.Splash, Routes.Onboarding, Routes.Started, Routes.Login,
                    Routes.AccountChoice, Routes.RegisterStudent, Routes.RegisterTeacher,
                    Routes.Verify, Routes.Forgot, Routes.NewPassword
                )
                val hasSelfHeader = studentOwnsHeader(route, backStack?.arguments?.getString("id")) &&
                    (teacher || route !in listOf(Routes.Topic, Routes.Progress))
                val showSubHeader = !isPublic && !inMain && route != null && !hasSelfHeader

                Scaffold(
                    snackbarHost = { androidx.compose.material3.SnackbarHost(gemSnackbar) },
                    containerColor =
                        if (reference) RefPalette.background
                        else MaterialTheme.colorScheme.background,
                    bottomBar = {
                        if (teacherReference) TeacherReferenceBottomNav(route, ::open)
                        else if (reference) RefBottomNav(route, ::open)
                        else if (teacher && inMain && !wide)
                            TeacherReferenceBottomNav(route, ::open, dark = false)
                        else if (!teacher && studentNavigationVisible && !wide)
                            RefBottomNav(
                                selectedTab,
                                ::open,
                                dark = false,
                            )
                    },
                ) { padding ->
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .imePadding()
                    ) {
                        if (showSubHeader) {
                            if (!teacher) {
                                RefTopBar(topTitle, { nav.popBackStack() },
                                    if (route != "search") ({ open("search") }) else null,
                                    if (route != "notifications") ({ open("notifications") }) else null,
                                    state.product.notifications.count { !it.teacher && it.id !in state.product.readNotifications })
                            } else if (route in listOf("study/catalog", "gems/wallet", Routes.Book, "practice/{id}")) {
                                PremiumSubScreenTopBar(topTitle, { nav.popBackStack() },
                                    if (!teacher) ({ open("search") }) else null,
                                    { open(if (teacher) "teacher/notifications" else "notifications") },
                                    state.product.notifications.count { it.teacher == teacher && it.id !in state.product.readNotifications },
                                    circular = route == "practice/{id}")
                            } else {
                            AppTopBar(
                                title = topTitle,
                                onBack = { nav.popBackStack() },
                                onSearch = if (!teacher) ({ open("search") }) else null,
                                onNotifications = ({
                                    open(if (teacher) "teacher/notifications" else "notifications")
                                }),
                                unread = state.product.notifications.count {
                                    it.teacher == teacher && it.id !in state.product.readNotifications
                                }
                            )
                            }
                        }
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            if (inMain && wide && !reference && (teacher || studentNavigationVisible)) {
                                NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                                    Spacer(Modifier.height(Space.xl))
                                    tabs.forEach { destination ->
                                        NavigationRailItem(
                                            selected = selectedTab == destination,
                                            onClick = { tab(destination) },
                                            icon = { Icon(tabIcon(destination), null) },
                                            label = { Text(tabLabel(destination)) },
                                            colors =
                                                NavigationRailItemDefaults.colors(
                                                    selectedIconColor =
                                                        MaterialTheme.colorScheme.primary,
                                                    indicatorColor =
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                ),
                                            modifier =
                                                Modifier.testTag(
                                                    "${if (teacher) "teacher-nav-" else "nav-"}$destination"
                                                ),
                                        )
                                    }
                                }
                            }
                        NavHost(
                            navController = nav,
                            startDestination = if (state.user?.localId != null) Routes.Main else Routes.Splash,
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
                            composable("gems/wallet") {
                                app.gems?.let { LocalGemsScreen(state, it) { logout() } }
                            }
                            composable("gems/review") {
                                app.gems?.let { LocalWeeklyReviewScreen(state, it) { nav.popBackStack() } }
                            }
                            navigation(startDestination = Routes.Home, route = Routes.Main) {
                                studentGraph(
                                    nav,
                                    app,
                                    solver,
                                    ::open,
                                    ::logout,
                                    ::guarded,
                                    if (app.canSwitchDemoRole()) ::switchDemoRole else null,
                                )
                            }
                            navigation(
                                startDestination = Routes.TeacherHome,
                                route = Routes.TeacherMain,
                            ) {
                                teacherGraph(nav, app, ::open, ::logout, if (app.canSwitchDemoRole()) ::switchDemoRole else null)
                            }
                        }
                    }
                }
            }
        }
    }
        if (accountPrompt)
            ResourcePackPresentation(enabled = true) {
                AlertDialog(onDismissRequest = { accountPrompt = false },
                    title = { Text(tr("A local student file is needed", "يلزم ملف طالب محلي")) },
                    text = { Text(tr("Guests can learn and plan locally. Cloud accounts are not connected. You can keep this guest file and choose a separate local student file.", "يمكن للزائر التعلم والتخطيط محليًا. الحسابات السحابية غير متصلة. يمكنك الاحتفاظ بملف الزائر واختيار ملف طالب محلي مستقل.")) },
                    confirmButton = { TextButton({ accountPrompt = false; logout() }) { Text(tr("Choose a local file", "اختيار ملف محلي")) } },
                    dismissButton = { TextButton({ accountPrompt = false }) { Text(tr("Back", "رجوع")) } })
            }
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
        "explore" -> label(TextKey.t_a0fc50de5a)
        Routes.Profile -> label(TextKey.t_e269f67032)
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
        "explore" -> Icons.AutoMirrored.Outlined.ShowChart
        Routes.Profile -> Icons.Outlined.Person
        else -> Icons.Outlined.Person
    }
