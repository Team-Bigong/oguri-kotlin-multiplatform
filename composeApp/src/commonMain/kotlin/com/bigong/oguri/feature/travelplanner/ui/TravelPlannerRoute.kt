package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlaceSearchSheet
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
    LaunchedEffect(viewModel, destinationId, destinationRetry) {
        if (destinationId != null) viewModel.loadCreationCity(destinationId)
    }
    LaunchedEffect(viewModel, signedIn) { if (signedIn) viewModel.loadCreationMatches() }
    LaunchedEffect(viewModel, signedIn) { if (signedIn) viewModel.recoverCreation() }
    LaunchedEffect(state.createdPlanId) {
        state.createdPlanId?.let(onCreated)
    }
    CreateTravelPlanScreen(
        state = state,
        source = source,
        signedIn = signedIn,
        onBack = onBack,
        onCreate = { details -> if (signedIn) viewModel.create(details) else onSignIn() },
        onOpenExisting = onOpenExisting,
        onSignIn = onSignIn,
        onRetrySave = viewModel::retrySave,
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
    planId: String,
    requestedDay: StateFlow<Int?>,
    onDayConsumed: () -> Unit,
    onMembers: (String, String) -> Unit,
    onFindNextHoliday: () -> Unit,
    onSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
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
    LaunchedEffect(viewModel, planId, state.selectedDay, plan?.summary?.id) {
        if (plan != null) {
            delay(250)
            viewModel.recordView(planId, state.selectedDay)
        }
    }
    LaunchedEffect(viewModel, planId, state.selectedDay, language, placeIds, detailsRetry) {
        viewModel.loadDayPlaces(planId, state.selectedDay, language, hasPlaces = placeIds.isNotEmpty())
    }
    TravelPlanScreen(
        state = state,
        onBack = onBack,
        onReload = { viewModel.loadPlan(planId, rebaseDraft = true) },
        onRetrySave = viewModel::retrySave,
        onUndoVisit = viewModel::undoVisit,
        onSelectDay = viewModel::selectDay,
        onOpenVisit = viewModel::openVisit,
        onMoveVisit = viewModel::moveVisit,
        onReorderVisits = viewModel::reorderVisits,
        onDraftChange = viewModel::updateVisitDraft,
        onApplyVisit = viewModel::applyVisit,
        onRemoveVisit = viewModel::removeVisit,
        onSearchPlaces = viewModel::openPlaceSearch,
        onMarkerClick = viewModel::selectVisitFromMap,
        onFitMap = viewModel::fitMap,
        onFindNextHoliday = onFindNextHoliday,
        onMembers = {
            plan
                ?.summary
                ?.details
                ?.dates
                ?.let { onMembers(it.start.toString(), it.endInclusive.toString()) }
        },
        onSignIn = onSignIn,
        onRetryPlaces = { detailsRetry++ },
    )
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
