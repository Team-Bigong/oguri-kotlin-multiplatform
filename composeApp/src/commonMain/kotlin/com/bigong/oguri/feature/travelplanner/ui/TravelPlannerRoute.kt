package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.domain.model.TravelUsageActivity
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOverviewSheet
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlaceSearchSheet
import com.bigong.oguri.feature.travelplanner.ui.component.TravelRecentChangesDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelSignInRequired
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.datetime.LocalDate

@Composable
internal fun TravelPlansRoute(
    viewModel: TravelPlannerViewModel,
    onBack: () -> Unit,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel, signedIn) { if (signedIn) viewModel.loadList() }
    TravelPlansScreen(state, signedIn, onSignIn, onBack, onCreate, onOpen, viewModel::loadList)
}

@Composable
internal fun CreateTravelPlanRoute(
    viewModel: TravelPlannerViewModel,
    destinationId: Long?,
    recommendedStartDate: String?,
    recommendedEndDate: String?,
    signedIn: Boolean,
    onSignIn: () -> Unit,
    onOpenExisting: (String) -> Unit,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var destinationRetry by remember { mutableIntStateOf(0) }
    var creationRecoveryRetry by remember { mutableIntStateOf(0) }
    val source =
        remember(destinationId, recommendedStartDate, recommendedEndDate) {
            val start = recommendedStartDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val end = recommendedEndDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            val hasDates = start != null && end != null && start <= end
            if (destinationId == null && !hasDates) {
                null
            } else {
                TravelPlanSource(destinationId, start.takeIf { hasDates }, end.takeIf { hasDates })
            }
        }
    LaunchedEffect(viewModel, signedIn, destinationId, destinationRetry) {
        if (signedIn && destinationId != null) viewModel.loadCreationCity(destinationId)
    }
    LaunchedEffect(viewModel, signedIn) { if (signedIn) viewModel.loadCreationMatches() }
    LaunchedEffect(viewModel, signedIn, creationRecoveryRetry) { if (signedIn) viewModel.recoverCreation() }
    LaunchedEffect(state.createdPlanId) {
        state.createdPlanId?.let(onCreated)
    }
    if (!signedIn) {
        TravelSignInRequired(onSignIn, onBack)
        return
    }
    CreateTravelPlanScreen(
        state = state,
        source = source,
        signedIn = signedIn,
        onBack = onBack,
        onCreate = viewModel::create,
        onOpenExisting = onOpenExisting,
        onSignIn = onSignIn,
        onRetrySave = viewModel::retrySave,
        onRetryRecovery = { creationRecoveryRetry++ },
        onRetryDestination = { destinationRetry++ },
        onOpenCitySearch = viewModel::openCitySearch,
        onCloseCitySearch = viewModel::closeCitySearch,
        onSearchCities = { viewModel.searchCities(it, Locale.current.toLanguageTag()) },
        onChooseCity = viewModel::chooseCity,
    )
}

@Composable
internal fun TravelPlanRoute(
    viewModel: TravelPlannerViewModel,
    collaborationViewModel: TravelCollaborationViewModel,
    planId: String,
    requestedDay: StateFlow<Int?>,
    onDayConsumed: () -> Unit,
    onMembers: (String, String) -> Unit,
    onFindNextHoliday: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val changesState by collaborationViewModel.state.collectAsStateWithLifecycle()
    var showOverview by remember(planId) { mutableStateOf(false) }
    var overviewRetry by remember(planId) { mutableIntStateOf(0) }
    var showChanges by remember(planId) { mutableStateOf(false) }
    LaunchedEffect(viewModel, planId, changesState.failure) {
        if (changesState.failure == TravelPlanFailure.UNAVAILABLE) viewModel.forgetUnavailablePlan(planId)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val language = Locale.current.toLanguageTag()
    var detailsRetry by remember { mutableIntStateOf(0) }
    val plan = state.plan
    val dayOverride by requestedDay.collectAsStateWithLifecycle()
    LaunchedEffect(dayOverride, plan?.summary?.id) {
        val day = dayOverride
        if (day != null && plan != null) {
            if (day in 0..plan.summary.details.dates.dayCount) viewModel.selectDay(day)
            onDayConsumed()
        }
    }
    val placeIds =
        plan
            ?.visits
            .orEmpty()
            .asSequence()
            .filter { it.dayNumber == state.selectedDay }
            .mapNotNull { it.details.googlePlaceId }
            .toSet()
    LaunchedEffect(viewModel, planId, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.loadPlan(planId)
            while (isActive) {
                delay(5_000)
                viewModel.refreshPlan(planId)
            }
        }
    }
    LaunchedEffect(viewModel, planId, lifecycle, plan?.summary?.id, state.isOffline) {
        if (plan != null && !state.isOffline) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.recordActivity(planId, TravelUsageActivity.PLAN_DISPLAYED)
                kotlinx.coroutines.awaitCancellation()
            }
        }
    }
    LaunchedEffect(viewModel, planId, state.selectedDay, plan?.summary?.id) {
        if (plan != null) {
            delay(250)
            viewModel.recordView(planId, state.selectedDay)
        }
    }
    LaunchedEffect(viewModel, planId, state.selectedDay, language, placeIds, detailsRetry) {
        viewModel.loadDayPlaces(planId, state.selectedDay, language, hasPlaces = placeIds.isNotEmpty())
    }
    val overviewPlaceIds =
        plan
            ?.visits
            .orEmpty()
            .mapNotNull { it.details.googlePlaceId }
            .toSet()
    LaunchedEffect(viewModel, planId, overviewPlaceIds, showOverview, language, overviewRetry) {
        if (showOverview) viewModel.loadOverviewPlaces(planId, language) else viewModel.closeOverview()
    }
    TravelPlanScreen(
        state = state,
        onBack = onBack,
        onReload = { viewModel.loadPlan(planId, rebaseDraft = true) },
        onRetrySave = viewModel::retrySave,
        onUndoVisit = viewModel::undoVisit,
        onSelectDay = { day ->
            viewModel.selectDay(day)
            viewModel.recordActivity(planId, TravelUsageActivity.DAY_SELECTED)
        },
        onOverview = { showOverview = true },
        onOpenVisit = viewModel::openVisit,
        onInspectVisit = viewModel::inspectVisit,
        onCloseInspection = viewModel::closeInspection,
        onMoveVisit = viewModel::moveVisit,
        onReorderVisits = viewModel::reorderVisits,
        onDraftChange = viewModel::updateVisitDraft,
        onApplyVisit = viewModel::applyVisit,
        onRemoveVisit = viewModel::removeVisit,
        onSearchPlaces = viewModel::openPlaceSearch,
        onMarkerClick = { id ->
            viewModel.selectVisitFromMap(id)
            viewModel.recordActivity(planId, TravelUsageActivity.PIN_SELECTED)
        },
        onDirectionsOpened = { viewModel.recordActivity(planId, TravelUsageActivity.DIRECTIONS_OPENED) },
        onFitMap = viewModel::fitMap,
        onFindNextHoliday = onFindNextHoliday,
        onMembers = {
            plan
                ?.summary
                ?.details
                ?.dates
                ?.let { onMembers(it.start.toString(), it.endInclusive.toString()) }
        },
        onRecentChanges = {
            showChanges = true
            collaborationViewModel.recentChanges(planId)
        },
        onSignIn = onSignIn,
        onRetryPlaces = { detailsRetry++ },
    )
    if (plan != null && showOverview) {
        state.overview?.let { overview ->
            LaunchedEffect(planId) { viewModel.recordActivity(planId, TravelUsageActivity.OVERVIEW_VIEWED) }
            TravelOverviewSheet(
                plan = plan,
                state = overview,
                canAdd = !state.isSaving && !state.isLoading && !state.canRetrySave && !state.needsLatestPlan && !state.isOffline,
                onDismiss = {
                    showOverview = false
                    viewModel.closeOverview()
                },
                onRetry = { overviewRetry++ },
                onSelectVisit = { id ->
                    showOverview = false
                    viewModel.selectOverviewVisit(id)
                    viewModel.recordActivity(planId, TravelUsageActivity.PIN_SELECTED)
                },
                onAdd = { day ->
                    showOverview = false
                    viewModel.closeOverview()
                    viewModel.selectDay(day)
                    viewModel.openPlaceSearch()
                },
            )
        }
    }
    if (plan != null && showChanges) {
        LaunchedEffect(changesState.recentChanges, changesState.busy, changesState.failure) {
            if (changesState.recentChanges != null && !changesState.busy && changesState.failure == null) {
                viewModel.recordActivity(planId, TravelUsageActivity.CHANGES_VIEWED)
            }
        }
        val dismissChanges = {
            if (!changesState.busy) changesState.recentChanges?.let { collaborationViewModel.dismissChanges(planId, it.throughRevision) }
            showChanges = false
        }
        TravelRecentChangesDialog(
            changes = changesState.recentChanges,
            dates = plan.summary.details.dates,
            busy = changesState.busy,
            failure = changesState.failure,
            onRetry = { collaborationViewModel.recentChanges(planId) },
            onDismiss = dismissChanges,
            onSelectDay = { day ->
                viewModel.selectDay(day)
                viewModel.recordActivity(planId, TravelUsageActivity.DAY_SELECTED)
                dismissChanges()
            },
        )
    }
    if (plan != null) {
        TravelPlaceSearchSheet(
            state = state,
            plan = plan,
            onQuery = { viewModel.searchPlaces(it, language) },
            onSelect = viewModel::selectPlace,
            onAdd = viewModel::addPlace,
            onShowVisit = viewModel::openExistingPlace,
            onDismiss = viewModel::closePlaceSearch,
            onRetrySave = viewModel::retrySave,
            onReload = { viewModel.loadPlan(planId, rebaseDraft = true) },
            onManualAdd = {
                viewModel.closePlaceSearch()
                viewModel.openVisit(null)
            },
        )
    }
}
