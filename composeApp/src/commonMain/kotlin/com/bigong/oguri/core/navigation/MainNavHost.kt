package com.bigong.oguri.core.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.bigong.oguri.core.deeplink.TravelInviteIntent
import com.bigong.oguri.core.di.AppGraph
import com.bigong.oguri.core.network.AuthTokenStore
import com.bigong.oguri.core.platform.PlatformBackGestureContainer
import com.bigong.oguri.core.platform.PlatformBackHandler
import com.bigong.oguri.core.ui.component.OguriSnackBarType
import com.bigong.oguri.core.ui.component.showOguriSnackbar
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
import com.bigong.oguri.feature.travelplanner.ui.TravelHomeGuide
import com.bigong.oguri.feature.travelplanner.ui.TravelInviteRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelMembersRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelPlanRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelPlannerViewModel
import com.bigong.oguri.feature.travelplanner.ui.TravelPlansRoute
import com.bigong.oguri.feature.travelplanner.ui.TravelResumeEntry
import com.bigong.oguri.feature.travelplanner.ui.component.TravelSignInRequired
import com.bigong.oguri.feature.webdocument.ui.WebDocumentRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.snackbar_onboarding_completed
import org.jetbrains.compose.resources.stringResource

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
    val onboardingCompletedMessage by rememberUpdatedState(stringResource(Res.string.snackbar_onboarding_completed))
    val travelSession by AuthTokenStore.sessions.collectAsStateWithLifecycle()
    val homeTravelViewModel by rememberUpdatedState(
        if (travelSession.tokens != null) {
            viewModel<TravelPlannerViewModel>(key = "travel-home-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() }
        } else {
            null
        },
    )

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

    fun showOnboardingCompletion() {
        navigationScope.launch {
            snackbarHostState.showOguriSnackbar(
                message = onboardingCompletedMessage,
                type = OguriSnackBarType.SUCCESS,
            )
        }
    }

    NavHost(
        navController = navigator.navHostController,
        startDestination = RouteModel.Splash,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(contentPaddingValues),
        enterTransition = { navigationEnter() },
        exitTransition = { navigationExit() },
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
                onCompleted = {
                    navigator.navigateToHomeFromOnboarding()
                    showOnboardingCompletion()
                },
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
                    onCompleted = {
                        finishTravelAuthentication()
                        showOnboardingCompletion()
                    },
                    onOpenWebDocument = navigator::navigateToWebDocument,
                )
            }
        }
        composable<RouteModel.Home> { entry ->
            val travelViewModel = homeTravelViewModel
            LaunchedEffect(entry, travelViewModel) {
                if (travelViewModel != null) {
                    entry.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { travelViewModel.refreshResumeIfNeeded() }
                }
            }
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
                    if (travelViewModel != null) {
                        { TravelResumeEntry(travelViewModel) { navigator.navigateToTravelPlan(it) } }
                    } else null,
                travelGuide = { TravelHomeGuide(travelViewModel) { navigator.navigateToCreateTravelPlan() } },
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
        composable<RouteModel.TravelInvite> { entry ->
            val token by TravelInviteIntent.token.collectAsStateWithLifecycle()
            val inviteViewModel =
                viewModel<com.bigong.oguri.feature.travelplanner.ui.TravelCollaborationViewModel>(
                    key = "travel-invite-${travelSession.version}",
                ) { appGraph.travelCollaborationViewModelProvider() }
            val closeInvite: () -> Unit = {
                if (token?.let(TravelInviteIntent::clear) != false) navigator.popBackStack()
            }
            val lifecycleState by entry.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
            val isCurrent = lifecycleState == Lifecycle.State.RESUMED
            PlatformBackHandler(enabled = isCurrent, onBack = closeInvite)
            PlatformBackGestureContainer(enabled = isCurrent, onBack = closeInvite) {
                TravelInviteRoute(
                    token = token,
                    signedIn = travelSession.tokens != null,
                    viewModel = inviteViewModel,
                    onSignIn = navigator::navigateToTravelLogin,
                    onBack = closeInvite,
                    onOpenPlans = {
                        if (token?.let(TravelInviteIntent::clear) != false) navigator.closeTravelInviteToPlans()
                    },
                    onJoined = {
                        if (token?.let(TravelInviteIntent::clear) == true) {
                            homeTravelViewModel?.invalidateResume()
                            navigator.openJoinedTravelPlan(it)
                        }
                    },
                )
            }
        }
        composable<RouteModel.TravelMembers> { entry ->
            if (travelSession.tokens == null) {
                TravelSignInRequired(onSignIn = navigator::navigateToTravelLogin, onBack = { navigator.popBackStack() })
                return@composable
            }
            val route = entry.toRoute<RouteModel.TravelMembers>()
            TravelMembersRoute(
                cityViewModel = viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() },
                planId = route.planId,
                dates = TravelDates(LocalDate.parse(route.startDate), LocalDate.parse(route.endDate)),
                onSelectDay = navigator::showTravelDay,
                viewModel = viewModel(key = "travel-members-${route.planId}-${travelSession.version}") { appGraph.travelCollaborationViewModelProvider() },
                onBack = { navigator.popBackStack() },
                onClosed = {
                    homeTravelViewModel?.invalidateResume(removedPlanId = route.planId)
                    navigator.closeManagedTravelPlan()
                },
                onDetailsChanged = { homeTravelViewModel?.invalidateResume() },
                onOpenSource = { source -> source.toRecommendationRoute()?.let(navigator::navigateToRouteModel) },
            )
        }
        composable<RouteModel.TravelPlans> {
            TravelPlansRoute(
                viewModel = viewModel(key = "travel-session-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() },
                onBack = null,
                signedIn = travelSession.tokens != null,
                onSignIn = navigator::navigateToTravelLogin,
                onCreate = { navigator.navigateToCreateTravelPlan() },
                onOpen = { navigator.navigateToTravelPlan(it) },
                onFindNextHoliday = navigator::openCalendarTab,
                modifier = Modifier.consumeWindowInsets(contentPaddingValues),
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
                onCreated = {
                    homeTravelViewModel?.invalidateResume()
                    navigator.navigateToTravelPlan(it, fromCreation = true)
                },
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
                onPlanViewed = {
                    homeTravelViewModel?.let {
                        if (it.state.value.resumePlan?.id != planId) it.invalidateResume()
                    }
                },
                onFindNextHoliday = {
                    navigator.openCalendarTab()
                    navigationScope.launch { travelViewModel.recordNextHolidayOpened(planId) }
                },
                onSignIn = navigator::navigateToTravelLogin,
                onBack = { navigator.popBackStack() },
                onOpenPlans = navigator::closeManagedTravelPlan,
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
            val travelViewModel = if (travelSession.tokens != null) {
                viewModel(key = "travel-destination-${travelSession.version}") { appGraph.travelPlannerViewModelProvider() }
            } else null
            val travelState = travelViewModel?.state?.collectAsStateWithLifecycle()?.value
            LaunchedEffect(travelViewModel) {
                travelViewModel?.loadCreationMatches()
            }
            PlaceDetailRoute(
                placeDetailViewModelProvider = appGraph.placeDetailViewModelProvider,
                snackbarHostState = snackbarHostState,
                placeId = route.placeId,
                startDate = route.startDate,
                endDate = route.endDate,
                onCreateTravelPlan = {
                    recordRecommendation(TravelRecommendationSource.CITY, TravelRecommendationEntry.CLICKED)
                    navigator.navigateToCreateTravelPlan(route.placeId, route.startDate, route.endDate)
                },
                matchingTravelPlans = travelState?.plans.orEmpty(),
                onOpenTravelPlan = { navigator.navigateToTravelPlan(it) },
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
                onCreateTravelPlan = {
                    recordRecommendation(TravelRecommendationSource.PERIOD, TravelRecommendationEntry.CLICKED)
                    navigator.navigateToCreateTravelPlan(startDate = route.startDate, endDate = route.endDate)
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
