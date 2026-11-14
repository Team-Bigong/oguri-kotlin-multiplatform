package com.bigong.oguri.core.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.deeplink.TravelInviteIntent
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.network.isDebugPlatformBuild
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.domain.model.DisplayThemeMode
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.repository.TravelRecommendationEntry
import com.bigong.oguri.domain.repository.TravelRecommendationSource
import com.bigong.oguri.feature.calendar.ui.CalendarRoute
import com.bigong.oguri.feature.calendar.ui.CalendarViewModel
import com.bigong.oguri.feature.home.ui.HomeRoute
import com.bigong.oguri.feature.home.ui.HomeViewModel
import com.bigong.oguri.feature.login.ui.LoginRoute
import com.bigong.oguri.feature.mypage.ui.DisplaySettingsRoute
import com.bigong.oguri.feature.mypage.ui.MyPageRoute
import com.bigong.oguri.feature.mypage.ui.MyPageViewModel
import com.bigong.oguri.feature.onboarding.ui.OnboardingRoute
import com.bigong.oguri.feature.perioddetail.ui.PeriodDetailRoute
import com.bigong.oguri.feature.photodetail.ui.PhotoDetailRoute
import com.bigong.oguri.feature.placedetail.ui.PlaceDetailRoute
import com.bigong.oguri.feature.search.ui.SearchRoute
import com.bigong.oguri.feature.splash.ui.SplashRoute
import com.bigong.oguri.feature.travelplanner.ui.CreateTravelPlanRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelInviteRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelMembersRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelPlanRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelPlansRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelResumeEntry
import com.bigong.oguri.feature.travelplanner.ui.component.TravelSignInRequired
import com.bigong.oguri.feature.webdocument.ui.WebDocumentRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

private const val NAVIGATION_FADE_DURATION_MILLIS = 180
private const val NAVIGATION_SLIDE_DURATION_MILLIS = 240

@Composable
fun MainNavHost(
    appGraph: AppGraph,
    homeViewModelProvider: () -> HomeViewModel,
    calendarViewModelProvider: () -> CalendarViewModel,
    myPageViewModelProvider: () -> MyPageViewModel,
    navigator: MainNavigator,
    snackbarHostState: SnackbarHostState,
    contentPaddingValues: PaddingValues,
    homeTabReselectTrigger: Int,
    calendarTabReselectTrigger: Int,
    myPageTabReselectTrigger: Int,
    onLoginSucceeded: () -> Unit,
    onTravelAuthenticationFinished: () -> Unit,
    onLoggedOut: () -> Unit,
    onWithdrawCompleted: () -> Unit,
    currentDisplayThemeMode: DisplayThemeMode,
    onDisplayThemeModeChange: (DisplayThemeMode) -> Unit,
) {
    val navigationScope = rememberCoroutineScope()
    val travelEnabled = isDebugPlatformBuild()
    val travelSession by AuthTokenStore.sessions.collectAsStateWithLifecycle()
    fun recordRecommendation(source: TravelRecommendationSource, entry: TravelRecommendationEntry) {
        navigationScope.launch {
            try {
                appGraph.travelRecommendationUsage.record(source, entry)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // Measurement must not block navigation.
            }
        }
    }

    fun finishTravelAuthentication() {
        val previous = navigator.navHostController.previousBackStackEntry
        val creation = previous?.takeIf { it.destination.hasRoute<RouteModel.CreateTravelPlan>() }?.toRoute<RouteModel.CreateTravelPlan>()
        onTravelAuthenticationFinished()
        if (navigator.popBackStack() && creation != null) {
            val source =
                when {
                    creation.destinationId != null -> TravelRecommendationSource.CITY
                    creation.startDate != null && creation.endDate != null -> TravelRecommendationSource.PERIOD
                    else -> null
                }
            if (source != null) recordRecommendation(source, TravelRecommendationEntry.LOGIN_RESUMED)
        }
    }

    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPaddingValues),
        enterTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            val initialTabIndex = bottomNavigationTabIndex(routeText = initialRoute)
            val targetTabIndex = bottomNavigationTabIndex(routeText = targetRoute)
            val isBottomNavigationTabTransition = initialTabIndex != null && targetTabIndex != null

            if (isBottomNavigationTabTransition) {
                val isForward = targetTabIndex > initialTabIndex
                slideInHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    initialOffsetX = { fullWidth -> if (isForward) fullWidth else -fullWidth },
                ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            } else {
                slideInHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    initialOffsetX = { fullWidth -> fullWidth },
                ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            }
        },
        exitTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            val initialTabIndex = bottomNavigationTabIndex(routeText = initialRoute)
            val targetTabIndex = bottomNavigationTabIndex(routeText = targetRoute)
            val isBottomNavigationTabTransition = initialTabIndex != null && targetTabIndex != null

            if (isBottomNavigationTabTransition) {
                val isForward = targetTabIndex > initialTabIndex
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    targetOffsetX = { fullWidth -> if (isForward) -fullWidth else fullWidth },
                ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            } else {
                slideOutHorizontally(
                    animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                    targetOffsetX = { fullWidth -> -fullWidth / 3 },
                ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
            }
        },
        popEnterTransition = {
            slideInHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
            ) + fadeIn(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(durationMillis = NAVIGATION_SLIDE_DURATION_MILLIS),
                targetOffsetX = { fullWidth -> fullWidth },
            ) + fadeOut(animationSpec = tween(durationMillis = NAVIGATION_FADE_DURATION_MILLIS))
        },
    ) {
        composable<RouteModel.Splash> {
            SplashRoute(
                splashViewModelProvider = appGraph.splashViewModelProvider,
                onNavigateToLogin = navigator::navigateToLoginFromSplash,
                onNavigateToOnboarding = navigator::navigateToOnboardingFromSplash,
                onNavigateToHome = navigator::navigateToHomeFromSplash,
            )
        }
        composable<RouteModel.Login> {
            LoginRoute(
                loginViewModelProvider = appGraph.loginViewModelProvider,
                snackbarHostState = snackbarHostState,
                onLoginSucceeded = onLoginSucceeded,
                onLoginCompleted = { isOnboardingCompleted ->
                    if (isOnboardingCompleted) {
                        navigator.navigateToHomeFromLogin()
                    } else {
                        navigator.navigateToOnboardingFromLogin()
                    }
                },
                onGuestBrowseClick = navigator::navigateToHomeFromLogin,
            )
        }
        composable<RouteModel.Onboarding> {
            OnboardingRoute(
                onboardingViewModelProvider = appGraph.onboardingViewModelProvider,
                snackbarHostState = snackbarHostState,
                onBackClick = navigator::navigateToLogin,
                onHomeClick = navigator::navigateToHomeFromOnboarding,
                onOpenWebDocument = navigator::navigateToWebDocument,
            )
        }
        composable<RouteModel.TravelLogin> {
            LoginRoute(
                loginViewModelProvider = appGraph.loginViewModelProvider,
                snackbarHostState = snackbarHostState,
                onLoginSucceeded = {},
                onLoginCompleted = { completed ->
                    if (completed) {
                        finishTravelAuthentication()
                    } else {
                        navigator.navigateToTravelOnboarding()
                    }
                },
                onGuestBrowseClick = { navigator.popBackStack() },
                isReturnToTravel = true,
            )
        }
        composable<RouteModel.TravelOnboarding> {
            val cancel = {
                AuthTokenStore.clearTokens()
                navigator.popBackStack()
                Unit
            }
            PlatformBackGestureContainer(enabled = true, onBack = cancel) {
                OnboardingRoute(
                    onboardingViewModelProvider = appGraph.onboardingViewModelProvider,
                    snackbarHostState = snackbarHostState,
                    onBackClick = cancel,
                    onHomeClick = {
                        finishTravelAuthentication()
                    },
                    onOpenWebDocument = navigator::navigateToWebDocument,
                )
            }
        }
        composable<RouteModel.Home> {
            HomeRoute(
                homeViewModel = homeViewModelProvider(),
                snackbarHostState = snackbarHostState,
                scrollToTopTrigger = homeTabReselectTrigger,
                onPlaceClick = navigator::navigateToPlaceDetail,
                onPeriodClick = navigator::navigateToPeriodDetail,
                onMoveToCalendarClick = {
                    val calendarDestination =
                        bottomNavigationDestinations.first { destination ->
                            destination.routeModel == RouteModel.Calendar
                        }
                    navigator.navigateToBottomNavigationDestination(calendarDestination)
                },
                onSearchClick = navigator::navigateToSearch,
                travelResume =
                    if (travelEnabled && travelSession.tokens != null) {
                        {
                            TravelResumeEntry(
                                viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() },
                            ) { navigator.navigateToTravelPlan(it) }
                        }
                    } else {
                        null
                    },
                onLoginRequired = navigator::navigateToLogin,
            )
        }
        composable<RouteModel.Search> {
            SearchRoute(
                searchViewModelProvider = appGraph.searchViewModelProvider,
                onBackClick = { navigator.popBackStack() },
                onSearchClick = {},
                onPlaceClick = { placeId ->
                    navigator.navigateToPlaceDetail(
                        placeId = placeId,
                        startDate = null,
                        endDate = null,
                    )
                },
                onSuggestionClick = { navigator.navigateToWebDocument(WebDocumentType.SUGGESTION) },
            )
        }
        composable<RouteModel.Calendar> {
            CalendarRoute(
                calendarViewModel = calendarViewModelProvider(),
                snackbarHostState = snackbarHostState,
                scrollToTopTrigger = calendarTabReselectTrigger,
                onLoginRequired = navigator::navigateToLogin,
                onOpenPeriodDetail = navigator::navigateToPeriodDetail,
            )
        }
        composable<RouteModel.TravelInvite> {
            val token by TravelInviteIntent.token.collectAsStateWithLifecycle()
            val inviteViewModel =
                viewModel<com.bigong.oguri.feature.travelplanner.ui.TravelCollaborationViewModel>(
                    key = "travel-invite-${travelSession.version}",
                ) { appGraph.travelCollaborationViewModelProvider() }
            TravelInviteRoute(
                token = token,
                signedIn = travelSession.tokens != null,
                viewModel = inviteViewModel,
                onSignIn = navigator::navigateToTravelLogin,
                onBack = {
                    token?.let(TravelInviteIntent::clear)
                    navigator.popBackStack()
                },
                onJoined = {
                    token?.let(TravelInviteIntent::clear)
                    navigator.openJoinedTravelPlan(it)
                },
            )
        }
        composable<RouteModel.TravelMembers> { entry ->
            if (travelSession.tokens == null) {
                TravelSignInRequired(onSignIn = navigator::navigateToTravelLogin, onBack = { navigator.popBackStack() })
                return@composable
            }
            val route = entry.toRoute<RouteModel.TravelMembers>()
            TravelMembersRoute(
                planId = route.planId,
                dates = TravelDates(LocalDate.parse(route.startDate), LocalDate.parse(route.endDate)),
                onSelectDay = navigator::showTravelDay,
                viewModel = viewModel(key = "travel-members-${travelSession.version}") { appGraph.travelCollaborationViewModelProvider() },
                reminderViewModel =
                    viewModel(
                        key = "travel-reminder-${travelSession.version}",
                    ) { appGraph.travelReminderViewModelProvider() },
                onBack = { navigator.popBackStack() },
                onClosed = navigator::closeManagedTravelPlan,
                onOpenSource = { source -> source.toRecommendationRoute()?.let(navigator::navigateToRouteModel) },
            )
        }
        composable<RouteModel.TravelPlans> {
            if (travelSession.tokens == null) {
                TravelSignInRequired(onSignIn = navigator::navigateToTravelLogin, onBack = { navigator.popBackStack() })
                return@composable
            }
            TravelPlansRoute(
                viewModel = viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() },
                onBack = { navigator.popBackStack() },
                signedIn = travelSession.tokens != null,
                onSignIn = navigator::navigateToTravelLogin,
                onCreate = { navigator.navigateToCreateTravelPlan() },
                onOpen = { navigator.navigateToTravelPlan(it) },
            )
        }
        composable<RouteModel.CreateTravelPlan> { entry ->
            if (travelSession.tokens == null) {
                TravelSignInRequired(onSignIn = navigator::navigateToTravelLogin, onBack = { navigator.popBackStack() })
                return@composable
            }
            val route = entry.toRoute<RouteModel.CreateTravelPlan>()
            CreateTravelPlanRoute(
                viewModel = viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() },
                destinationId = route.destinationId,
                recommendedStartDate = route.startDate,
                recommendedEndDate = route.endDate,
                signedIn = travelSession.tokens != null,
                onSignIn = navigator::navigateToTravelLogin,
                onOpenExisting = { navigator.navigateToTravelPlan(it, fromCreation = true) },
                onBack = { navigator.popBackStack() },
                onCreated = { navigator.navigateToTravelPlan(it, fromCreation = true) },
            )
        }
        composable<RouteModel.TravelPlan> { entry ->
            if (travelSession.tokens == null) {
                TravelSignInRequired(onSignIn = navigator::navigateToTravelLogin, onBack = { navigator.popBackStack() })
                return@composable
            }
            val planId = entry.toRoute<RouteModel.TravelPlan>().planId
            val travelViewModel =
                viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() }
            TravelPlanRoute(
                collaborationViewModel =
                    viewModel(
                        key = "travel-changes-${travelSession.version}",
                    ) { appGraph.travelCollaborationViewModelProvider() },
                viewModel = travelViewModel,
                planId = planId,
                requestedDay = entry.savedStateHandle.getStateFlow(TRAVEL_DAY_REQUEST, null),
                onDayConsumed = { entry.savedStateHandle.set<Int?>(TRAVEL_DAY_REQUEST, null) },
                onMembers = { start, end -> navigator.navigateToTravelMembers(planId, start, end) },
                onFindNextHoliday = {
                    navigator.openCalendarTab()
                    navigationScope.launch { travelViewModel.recordNextHolidayOpened(planId) }
                },
                onSignIn = navigator::navigateToTravelLogin,
                onBack = { navigator.popBackStack() },
            )
        }
        composable<RouteModel.MyPage> {
            MyPageRoute(
                myPageViewModel = myPageViewModelProvider(),
                snackbarHostState = snackbarHostState,
                scrollToTopTrigger = myPageTabReselectTrigger,
                onOpenSuggestion = { navigator.navigateToWebDocument(WebDocumentType.SUGGESTION) },
                onOpenTermsOfService = { navigator.navigateToWebDocument(WebDocumentType.TERMS_OF_SERVICE) },
                onOpenPrivacyPolicy = { navigator.navigateToWebDocument(WebDocumentType.PRIVACY_POLICY) },
                onOpenDisplaySettings = navigator::navigateToDisplaySettings,
                onOpenTravelPlans = navigator::navigateToTravelPlans.takeIf { travelEnabled },
                onLoggedOut = onLoggedOut,
                onWithdrawCompleted = onWithdrawCompleted,
                onLoginRequired = navigator::navigateToLogin,
                onPeriodClick = navigator::navigateToPeriodDetail,
                onSavedPlaceClick = { placeId ->
                    navigator.navigateToPlaceDetail(
                        placeId = placeId,
                        startDate = null,
                        endDate = null,
                    )
                },
            )
        }
        composable<RouteModel.DisplaySettings> {
            DisplaySettingsRoute(
                currentDisplayThemeMode = currentDisplayThemeMode,
                onDisplayThemeModeChange = onDisplayThemeModeChange,
                onBackClick = { navigator.popBackStack() },
            )
        }
        composable<RouteModel.PlaceDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PlaceDetail>()
            PlaceDetailRoute(
                placeDetailViewModelProvider = appGraph.placeDetailViewModelProvider,
                snackbarHostState = snackbarHostState,
                placeId = route.placeId,
                startDate = route.startDate,
                endDate = route.endDate,
                onCreateTravelPlan =
                    if (travelEnabled) {
                        {
                            recordRecommendation(TravelRecommendationSource.CITY, TravelRecommendationEntry.CLICKED)
                            navigator.navigateToCreateTravelPlan(route.placeId, route.startDate, route.endDate)
                        }
                    } else {
                        null
                    },
                onTravelEntryVisible = { recordRecommendation(TravelRecommendationSource.CITY, TravelRecommendationEntry.EXPOSED) },
                onBackClick = { navigator.popBackStack() },
                onLoginRequired = navigator::navigateToLogin,
                onPlaceClick = { targetPlaceId ->
                    navigator.navigateToPlaceDetail(
                        placeId = targetPlaceId,
                        startDate = route.startDate,
                        endDate = route.endDate,
                    )
                },
                onPhotoClick = { imageUrls, initialPage ->
                    navigator.navigateToPhotoDetail(
                        imageUrls = imageUrls,
                        initialPage = initialPage,
                    )
                },
            )
        }
        composable<RouteModel.PhotoDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PhotoDetail>()
            PhotoDetailRoute(
                imageUrls = route.imageUrls,
                initialPage = route.initialPage,
                onBackClick = { navigator.popBackStack() },
            )
        }
        composable<RouteModel.PeriodDetail> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.PeriodDetail>()
            PeriodDetailRoute(
                periodDetailViewModelProvider = appGraph.periodDetailViewModelProvider,
                snackbarHostState = snackbarHostState,
                startDate = route.startDate,
                endDate = route.endDate,
                onCreateTravelPlan =
                    if (travelEnabled) {
                        {
                            recordRecommendation(TravelRecommendationSource.PERIOD, TravelRecommendationEntry.CLICKED)
                            navigator.navigateToCreateTravelPlan(startDate = route.startDate, endDate = route.endDate)
                        }
                    } else {
                        null
                    },
                onTravelEntryVisible = { recordRecommendation(TravelRecommendationSource.PERIOD, TravelRecommendationEntry.EXPOSED) },
                onBackClick = { navigator.popBackStack() },
                onLoginRequired = navigator::navigateToLogin,
                onPlaceClick = { placeId ->
                    navigator.navigateToPlaceDetail(
                        placeId = placeId,
                        startDate = route.startDate,
                        endDate = route.endDate,
                    )
                },
            )
        }
        composable<RouteModel.WebDocument> { navBackStackEntry ->
            val route = navBackStackEntry.toRoute<RouteModel.WebDocument>()
            WebDocumentRoute(
                documentType = route.documentType,
                onBackClick = { navigator.popBackStack() },
            )
        }
    }
}

private fun bottomNavigationTabIndex(routeText: String?): Int? {
    if (routeText.isNullOrBlank()) {
        return null
    }
    val index =
        bottomNavigationDestinations.indexOfFirst { destination ->
            routeText == destination.routeSerialName || routeText.startsWith(destination.routeSerialName)
        }
    return if (index >= 0) index else null
}
