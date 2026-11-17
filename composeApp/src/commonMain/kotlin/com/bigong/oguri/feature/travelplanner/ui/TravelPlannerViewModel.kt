package com.bigong.oguri.feature.travelplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelPendingSave
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanException
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.domain.model.TravelUsageActivity
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.domain.model.TravelVisitChange
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.domain.repository.TravelPlaceRepository
import com.bigong.oguri.domain.repository.TravelPlanRepository
import com.bigong.oguri.domain.repository.TravelRecoveryRepository
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase
import com.bigong.oguri.domain.usecase.GetSearchAutocompleteListUseCase
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelOverviewState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlaceSearchState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitUndoAction
import com.bigong.oguri.feature.travelplanner.ui.model.toVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.withRefreshedPlan
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.toLocalDateTime

@Inject
class TravelPlannerViewModel(
    private val repository: TravelPlanRepository,
    private val placeRepository: TravelPlaceRepository,
    private val getPlaceDetail: GetPlaceDetailUseCase,
    private val recovery: TravelRecoveryRepository,
    private val searchDestinations: GetSearchAutocompleteListUseCase,
) : ViewModel() {
    private val mutableState: MutableStateFlow<TravelPlannerUiState> = MutableStateFlow(TravelPlannerUiState())
    internal val state: StateFlow<TravelPlannerUiState> = mutableState.asStateFlow()
    private var verifiedAt: kotlin.time.TimeMark? = null
    private var mutationGeneration: Long = 0
    private var citySearchJob: Job? = null
    private var searchJob: Job? = null
    private var pendingSave: TravelPendingSave? = null
    private var pendingPlanId: String? = null
    private val recoveryWriteMutex: Mutex = Mutex()
    private var draftWrite: Job? = null
    private var resumeRequest: Long = 0
    private var overviewRequest: Long = 0
    private var dayPlacesRequest: Long = 0
    private var addedPlaceForSave: TravelPlace? = null
    private var visitSaveConflictPlanId: String? = null

    internal fun recordActivity(
        planId: String,
        kind: TravelUsageActivity,
    ) {
        viewModelScope.launch { recordCurrentActivity(planId, kind) }
    }

    internal suspend fun recordNextHolidayOpened(planId: String) {
        recordCurrentActivity(planId, TravelUsageActivity.NEXT_HOLIDAY_OPENED)
    }

    private suspend fun recordCurrentActivity(
        planId: String,
        kind: TravelUsageActivity,
    ) {
        val current = state.value
        if (current.isOffline || current.plan?.summary?.id != planId) return
        try {
            repository.recordActivity(planId, kind)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Measurement is best effort and must not change access/edit/recovery state.
        }
    }

    internal suspend fun loadResume() {
        val request = ++resumeRequest
        val generation = mutationGeneration
        val plan =
            try {
                repository.resume()
            } catch (_: TravelPlanException) {
                null
            }
        currentCoroutineContext().ensureActive()
        if (request == resumeRequest && generation == mutationGeneration) {
            mutableState.update { it.copy(resumePlan = plan, resumeLoaded = true) }
        }
    }

    internal suspend fun loadCreationCity(destinationId: Long) {
        mutableState.update { it.copy(creationCityFailed = false) }
        try {
            val place = getPlaceDetail(destinationId)
            currentCoroutineContext().ensureActive()
            mutableState.update { it.copy(creationCity = place.city) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            mutableState.update { it.copy(creationCityFailed = true) }
        }
    }

    internal suspend fun recordView(
        planId: String,
        dayNumber: Int,
    ) {
        try {
            repository.recordView(planId, dayNumber)
        } catch (exception: TravelPlanException) {
            if (exception.failure == TravelPlanFailure.UNAVAILABLE) {
                forgetUnavailablePlan(planId)
            }
            // Optional view-record failures do not interrupt an otherwise accessible itinerary.
        }
    }

    internal fun openCitySearch() {
        mutableState.update { it.copy(citySearch = TravelCitySearchState()) }
    }

    internal fun closeCitySearch() {
        citySearchJob?.cancel()
        mutableState.update { it.copy(citySearch = null) }
    }

    internal fun searchCities(
        query: String,
        language: String,
    ) {
        if (state.value.citySearch == null) return
        citySearchJob?.cancel()
        val canSearch = query.trim().length >= MIN_SEARCH_LENGTH
        mutableState.update { it.copy(citySearch = TravelCitySearchState(query = query, loading = canSearch)) }
        if (!canSearch) return
        citySearchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MILLIS)
                try {
                    val suggestions = placeRepository.cities(query.trim(), language)
                    currentCoroutineContext().ensureActive()
                    mutableState.update { it.copy(citySearch = it.citySearch?.copy(suggestions = suggestions, loading = false)) }
                } catch (exception: TravelPlanException) {
                    currentCoroutineContext().ensureActive()
                    mutableState.update { it.copy(citySearch = it.citySearch?.copy(failure = exception.failure, loading = false)) }
                }
            }
    }

    internal fun chooseCity(suggestion: TravelCitySuggestion) {
        citySearchJob?.cancel()
        mutableState.update {
            it.copy(citySearch = it.citySearch?.copy(selected = suggestion, timeZone = null, loading = true, failure = null))
        }
        citySearchJob =
            viewModelScope.launch {
                val zone =
                    try {
                        suggestion.timeZone ?: placeRepository.cityTimeZone(suggestion.id)
                    } catch (_: TravelPlanException) {
                        null
                    }
                currentCoroutineContext().ensureActive()
                mutableState.update { it.copy(citySearch = it.citySearch?.copy(timeZone = zone, loading = false)) }
            }
    }

    internal suspend fun resolveCityTimeZone(city: String, language: String): String? {
        com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone(city)?.let { return it }
        return try {
            val matches = placeRepository.cities(city, language)
            val match =
                matches.singleOrNull { it.label.equals(city, ignoreCase = true) }
                    ?: matches.singleOrNull { it.name.equals(city, ignoreCase = true) }
                    ?: return null
            (match.timeZone ?: placeRepository.cityTimeZone(match.id))?.takeIf { it in kotlinx.datetime.TimeZone.availableZoneIds }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: TravelPlanException) {
            null
        }
    }

    internal suspend fun loadCreationMatches() {
        try {
            val plans = repository.list()
            mutableState.update { it.copy(plans = plans) }
        } catch (_: TravelPlanException) {
            // A failed suggestion lookup must not interrupt creation or a save retry.
        }
    }

    internal fun loadList() {
        load {
            val plans = repository.list()
            mutableState.update { it.copy(plans = plans, failure = null) }
        }
    }

    internal suspend fun recoverCreation() {
        if (state.value.creationReady) return
        mutableState.update { it.copy(creationRecoveryFailed = false, failure = null) }
        try {
            val stored = recovery.load(null)
            val pending = stored.pending as? TravelPendingSave.Create
            if (pendingSave == null && state.value.createdPlanId == null && pending != null) {
                pendingSave = pending
                pendingPlanId = null
                mutableState.update {
                    it.copy(recoveredCreation = pending.details, canRetrySave = true, failure = TravelPlanFailure.CONNECTION)
                }
            }
            mutableState.update { it.copy(creationReady = true) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            val failure = (exception as? TravelPlanException)?.failure ?: TravelPlanFailure.CONNECTION
            mutableState.update { it.copy(creationRecoveryFailed = true, failure = failure) }
        }
    }

    internal fun loadPlan(
        planId: String,
        rebaseDraft: Boolean = false,
    ) {
        load(allowPending = true) {
            val generation = mutationGeneration
            val wasLoaded = state.value.plan != null
            val stored = if (!wasLoaded || state.value.isOffline) recoverSafely { recovery.load(planId) } else null
            val plan =
                try {
                    repository.get(planId)
                } catch (exception: TravelPlanException) {
                    if (generation != mutationGeneration) return@load
                    if (exception.failure == TravelPlanFailure.UNAVAILABLE) forgetUnavailablePlan(planId)
                    val cached =
                        stored?.confirmedPlan
                            ?: state.value.plan?.takeIf { verifiedAt?.elapsedNow()?.let { age -> age <= OFFLINE_PLAN_TTL } == true }
                    if (exception.failure != TravelPlanFailure.CONNECTION) {
                        pendingSave = null
                        throw exception
                    }
                    if (stored?.pending != null) {
                        pendingPlanId = planId
                        pendingSave = stored.pending
                    }
                    val savedDay = cached?.let { snapshot ->
                        snapshot.lastViewedDay?.takeIf { it in 1..snapshot.summary.details.dates.dayCount }
                    }
                    mutableState.update {
                        it.copy(
                            plan = cached,
                            isOffline = true,
                            failure = TravelPlanFailure.CONNECTION,
                            visitDraft = it.visitDraft ?: stored?.draft?.toVisitDraft(),
                            unappliedOrder = if (wasLoaded) it.unappliedOrder else it.unappliedOrder ?: stored?.unappliedOrder,
                            canRetrySave = pendingSave != null,
                            pendingSaveStored = stored?.pending != null || it.pendingSaveStored,
                            selectedDay = if (wasLoaded) it.selectedDay else savedDay ?: it.selectedDay,
                            inspectedVisitId = it.inspectedVisitId.takeIf { cached != null },
                            selectedVisitId = it.selectedVisitId.takeIf { cached != null },
                            mapFocusedVisitId = it.mapFocusedVisitId.takeIf { cached != null },
                            placeSearch = it.placeSearch.takeIf { cached != null },
                            overview = it.overview.takeIf { cached != null },
                            dayPlaces = if (cached == null) emptyMap() else it.dayPlaces,
                        )
                    }
                    return@load
                }
            if (!rememberCurrentPlan(plan, generation)) return@load
            verifiedAt = TimeSource.Monotonic.markNow()
            if (stored?.pending is TravelPendingSave.Visit) {
                pendingPlanId = planId
                pendingSave = stored.pending
            }
            mutableState.update { current ->
                val dates = plan.summary.details.dates
                val today =
                    Clock.System
                        .now()
                        .toLocalDateTime(plan.summary.details.timeZone)
                        .date
                val initialDay =
                    plan.lastViewedDay?.takeIf { it in 1..dates.dayCount }
                        ?: if (today in dates.start..dates.endInclusive) {
                            (today.toEpochDays() - dates.start.toEpochDays()).toInt() + 1
                        } else {
                            1
                        }
                val recovered = stored?.draft?.toVisitDraft()
                val draft = current.visitDraft ?: recovered
                val updatedVisit = draft?.visit?.let { original -> plan.visits.find { it.id == original.id } }
                val wasRemoved = draft?.visit != null && updatedVisit == null
                val nextDraft =
                    when {
                        draft == null || !rebaseDraft -> draft
                        updatedVisit != null -> draft.rebaseOn(updatedVisit)
                        else -> draft
                    }?.let {
                        if (!rebaseDraft || wasRemoved) {
                            it
                        } else {
                            it.copy(
                                structureRevision = plan.structureRevision,
                                dayNumber = it.dayNumber.takeIf { day -> day in 1..dates.dayCount } ?: plan.summary.details.dates.dayCount,
                            )
                        }
                    }
                val changedStructure = nextDraft?.let { it.structureRevision != plan.structureRevision } == true
                val refreshed = if (current.plan == null) current.copy(selectedDay = initialDay) else current.withRefreshedPlan(plan)
                refreshed.copy(
                    plan = plan,
                    isOffline = false,
                    canRetrySave = pendingSave != null,
                    pendingSaveStored = stored?.pending != null || current.pendingSaveStored,
                    visitDraft = nextDraft,
                    unappliedOrder = if (wasLoaded) current.unappliedOrder else current.unappliedOrder ?: stored?.unappliedOrder,
                    inspectedVisitId = refreshed.inspectedVisitId.takeIf { nextDraft == null },
                    needsLatestPlan = pendingSave == null && (wasRemoved || changedStructure || (!rebaseDraft && current.needsLatestPlan)),
                    failure =
                        when {
                            pendingSave != null -> TravelPlanFailure.CONNECTION
                            wasRemoved -> TravelPlanFailure.VISIT_REMOVED
                            changedStructure -> TravelPlanFailure.CONFLICT
                            !rebaseDraft && current.needsLatestPlan -> TravelPlanFailure.CONFLICT
                            else -> null
                        },
                )
            }
            if (rebaseDraft && pendingSave == null) {
                draftWrite?.join()
                val snapshot = state.value.visitDraft?.forRecovery()
                recoveryWriteMutex.withLock {
                    if (generation == mutationGeneration) recoverSafely { recovery.saveDraft(planId, snapshot) }
                }
            }
        }
    }

    internal suspend fun refreshPlan(planId: String) {
        val before = state.value
        if (before.isLoading || before.isSaving) return
        if (before.isOffline || (before.plan == null && before.failure == TravelPlanFailure.CONNECTION)) {
            loadPlan(planId)
            return
        }
        val currentPlan = before.plan?.takeIf { it.summary.id == planId } ?: return
        val generation = mutationGeneration
        try {
            val updated = repository.getIfChanged(planId, currentPlan.revision)
            currentCoroutineContext().ensureActive()
            if (generation != mutationGeneration) return
            if (updated == null || updated.revision <= currentPlan.revision) {
                verifiedAt = TimeSource.Monotonic.markNow()
                return
            }
            if (!rememberCurrentPlan(updated, generation)) return
            verifiedAt = TimeSource.Monotonic.markNow()
            mutableState.update { current ->
                if (generation != mutationGeneration || current.plan == null || updated.revision <= current.plan.revision) {
                    current
                } else {
                    current.withRefreshedPlan(updated)
                }
            }
        } catch (exception: TravelPlanException) {
            currentCoroutineContext().ensureActive()
            if (generation != mutationGeneration) return
            if (exception.failure == TravelPlanFailure.UNAVAILABLE || exception.failure == TravelPlanFailure.SIGN_IN_REQUIRED) {
                if (exception.failure == TravelPlanFailure.UNAVAILABLE) {
                    forgetUnavailablePlan(planId)
                } else {
                    pendingSave = null
                    applyFailure(exception.failure)
                }
            } else if (exception.failure == TravelPlanFailure.CONNECTION) {
                mutableState.update { it.copy(isOffline = true) }
            }
        }
    }

    internal fun selectDay(dayNumber: Int) {
        mutableState.update {
            if (it.selectedDay == dayNumber) {
                it
            } else {
                it.copy(
                    selectedDay = dayNumber,
                    selectedVisitId = null,
                    mapFocusedVisitId = null,
                    dayPlaces = emptyMap(),
                    dayPlacesLanguage = null,
                    dayPlacesIncomplete = false,
                    isLoadingDayPlaces = false,
                    viewportRequest = it.viewportRequest + 1,
                )
            }
        }
    }

    internal suspend fun loadDayPlaces(
        planId: String,
        dayNumber: Int,
        language: String,
        hasPlaces: Boolean,
    ) {
        val current = state.value
        if (current.plan?.summary?.id != planId || current.selectedDay != dayNumber) return
        val request = ++dayPlacesRequest
        if (!hasPlaces || current.isOffline) {
            mutableState.update {
                it.copy(
                    dayPlaces = it.dayPlaces.takeIf { _ -> hasPlaces && it.dayPlacesLanguage == language }.orEmpty(),
                    dayPlacesLanguage = language,
                    isLoadingDayPlaces = false,
                    dayPlacesIncomplete = hasPlaces,
                )
            }
            return
        }

        fun isCurrentRequest(): Boolean {
            val state = state.value
            return request == dayPlacesRequest && !state.isOffline && state.selectedDay == dayNumber && state.plan?.summary?.id == planId
        }

        mutableState.update {
            it.copy(
                dayPlaces = it.dayPlaces.takeIf { _ -> it.dayPlacesLanguage == language }.orEmpty(),
                isLoadingDayPlaces = true,
                dayPlacesIncomplete = false,
            )
        }
        try {
            val result = placeRepository.forDay(planId, dayNumber, language)
            currentCoroutineContext().ensureActive()
            if (!isCurrentRequest()) return
            mutableState.update {
                it.copy(
                    dayPlaces = result.places,
                    dayPlacesLanguage = language,
                    isLoadingDayPlaces = false,
                    dayPlacesIncomplete = result.incomplete,
                )
            }
        } catch (exception: TravelPlanException) {
            currentCoroutineContext().ensureActive()
            if (!isCurrentRequest()) return
            when (exception.failure) {
                TravelPlanFailure.UNAVAILABLE -> forgetUnavailablePlan(planId)
                TravelPlanFailure.SIGN_IN_REQUIRED -> applyFailure(exception.failure)
                else -> mutableState.update { it.copy(isLoadingDayPlaces = false, dayPlacesIncomplete = true) }
            }
        }
    }

    internal suspend fun loadOverviewPlaces(
        planId: String,
        language: String,
    ) {
        val current = state.value
        val plan = current.plan?.takeIf { it.summary.id == planId } ?: return
        val generation = mutationGeneration
        val request = ++overviewRequest
        val placeIds = plan.visits.mapNotNull { it.details.googlePlaceId }.toSet()
        mutableState.update {
            val overviewPlaces =
                it.overview
                    ?.takeIf { overview -> overview.language == language }
                    ?.places
                    .orEmpty()
            val dayPlaces = if (it.dayPlacesLanguage == language) it.dayPlaces else emptyMap()
            val knownPlaces = (overviewPlaces + dayPlaces).filterKeys { id -> id in placeIds }
            it.copy(
                overview = TravelOverviewState(
                    knownPlaces,
                    language = language,
                    loading = !current.isOffline,
                    incomplete = current.isOffline && !knownPlaces.keys.containsAll(placeIds),
                ),
            )
        }
        if (current.isOffline) return
        try {
            val known =
                state.value.overview
                    ?.places
                    .orEmpty()
                    .keys
            val missing = placeIds - known
            for (batch in missing.toList().chunked(30)) {
                val result = placeRepository.forPlan(planId, language, batch)
                currentCoroutineContext().ensureActive()
                if (!isCurrentOverview(planId, generation, request)) return
                mutableState.update { current ->
                    val overview = current.overview ?: return@update current
                    current.copy(
                        overview =
                            overview.copy(
                                places = overview.places + result.places.filterKeys { it in batch },
                                incomplete = overview.incomplete || result.incomplete,
                            ),
                    )
                }
            }
            if (!isCurrentOverview(planId, generation, request)) return
            mutableState.update { current ->
                current.copy(
                    overview =
                        current.overview?.let {
                            it.copy(loading = false, incomplete = it.incomplete || !it.places.keys.containsAll(placeIds))
                        },
                )
            }
        } catch (exception: TravelPlanException) {
            currentCoroutineContext().ensureActive()
            if (!isCurrentOverview(planId, generation, request)) return
            when (exception.failure) {
                TravelPlanFailure.UNAVAILABLE -> forgetUnavailablePlan(planId)
                TravelPlanFailure.SIGN_IN_REQUIRED -> applyFailure(exception.failure)
                else -> mutableState.update { it.copy(overview = it.overview?.copy(loading = false, incomplete = true)) }
            }
        }
    }

    private fun isCurrentOverview(
        planId: String,
        generation: Long,
        request: Long,
    ): Boolean =
        generation == mutationGeneration &&
            request == overviewRequest &&
            !state.value.isOffline &&
            state.value.overview != null &&
            state.value.plan
                ?.summary
                ?.id == planId

    internal fun closeOverview() {
        overviewRequest++
        mutableState.update { it.copy(overview = null) }
    }

    internal fun selectOverviewVisit(visitId: String) {
        val visit =
            state.value.plan
                ?.visits
                ?.firstOrNull { it.id == visitId } ?: return
        selectDay(visit.dayNumber)
        mutableState.update {
            it.copy(
                selectedVisitId = visit.id,
                mapFocusedVisitId = visit.id,
                listFocusRequest = it.listFocusRequest + 1,
                viewportRequest = it.viewportRequest + 1,
                overview = null,
            )
        }
    }

    internal fun openPlaceSearch() {
        mutableState.update { it.copy(placeSearch = TravelPlaceSearchState(), failure = null) }
    }

    internal fun closePlaceSearch() {
        if (state.value.isSaving || pendingSave != null) return
        searchJob?.cancel()
        mutableState.update { it.copy(placeSearch = null, failure = null) }
    }

    internal fun selectSearchCity(city: String, language: String) {
        if (city !in state.value.plan?.summary?.details?.cities.orEmpty()) return
        val query = state.value.placeSearch?.query.orEmpty()
        mutableState.update { it.copy(searchCity = city, placeSearch = it.placeSearch?.copy(selected = null, results = emptyList())) }
        searchPlaces(query, language)
    }

    internal fun searchPlaces(
        query: String,
        language: String,
    ) {
        val planId =
            state.value.plan
                ?.summary
                ?.id ?: return
        val search = state.value.placeSearch ?: return
        val details = state.value.plan?.summary?.details ?: return
        val city = state.value.searchCity?.takeIf { it in details.cities } ?: details.city
        searchJob?.cancel()
        val canSearch = query.trim().length >= MIN_SEARCH_LENGTH
        mutableState.update {
            it.copy(placeSearch = search.copy(query = query, selected = null, results = emptyList(), isLoading = canSearch, failure = null))
        }
        if (!canSearch) return
        searchJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MILLIS)
                try {
                    val results = placeRepository.searchInCity(planId, query.trim(), language, city)
                    currentCoroutineContext().ensureActive()
                    mutableState.update { it.copy(placeSearch = it.placeSearch?.copy(results = results, isLoading = false)) }
                } catch (exception: TravelPlanException) {
                    currentCoroutineContext().ensureActive()
                    if (exception.failure == TravelPlanFailure.SIGN_IN_REQUIRED || exception.failure == TravelPlanFailure.UNAVAILABLE) {
                        if (exception.failure ==
                            TravelPlanFailure.UNAVAILABLE
                        ) {
                            forgetUnavailablePlan(planId)
                        } else {
                            applyFailure(exception.failure)
                        }
                    } else {
                        mutableState.update { it.copy(placeSearch = it.placeSearch?.copy(failure = exception.failure, isLoading = false)) }
                    }
                }
            }
    }

    internal fun selectPlace(place: TravelPlace?) {
        mutableState.update { it.copy(placeSearch = it.placeSearch?.copy(selected = place)) }
    }

    internal fun addPlace(
        place: TravelPlace,
        dayNumber: Int,
        category: TravelVisitCategory?,
        note: String,
    ) {
        changeVisit(
            change =
                TravelVisitChange.Add(
                    dayNumber,
                    TravelVisitDetails(
                        name = null,
                        location = null,
                        localTime = null,
                        note = note.trim().ifEmpty { null },
                        googlePlaceId = place.id,
                        category = category,
                    ),
                    state.value.plan?.structureRevision ?: return,
                ),
            selectedDayAfterSave = dayNumber,
            addedPlace = place,
        )
    }

    internal fun reorderVisits(
        day: Int,
        expectedOrderRevision: Long,
        expectedStructureRevision: Long,
        visitIds: List<String>,
    ): Boolean {
        val current = state.value
        val plan = current.plan ?: return false
        if (day != current.selectedDay ||
            plan.orderRevision(day) != expectedOrderRevision ||
            plan.structureRevision != expectedStructureRevision
        ) {
            return false
        }
        val originalIds = plan.visits.filter { it.dayNumber == day }.map(TravelVisit::id)
        if (visitIds == originalIds || visitIds.size != originalIds.size || visitIds.toSet() != originalIds.toSet()) return false
        return changeVisit(TravelVisitChange.Reorder(day, expectedOrderRevision, visitIds, expectedStructureRevision))
    }

    internal fun applyUnappliedOrder() {
        val current = state.value
        val order = current.unappliedOrder ?: return
        val plan = current.plan ?: return
        if (current.needsLatestPlan) return
        val rebasedVisitIds = order.rebasedVisitIds(plan) ?: return
        reorderVisits(order.dayNumber, plan.orderRevision(order.dayNumber), plan.structureRevision, rebasedVisitIds)
    }

    internal fun discardUnappliedOrder() {
        val current = state.value
        val planId = current.plan?.summary?.id ?: return
        if (current.unappliedOrder == null || current.isSaving || pendingSave != null) return
        val generation = mutationGeneration
        mutableState.update { it.copy(unappliedOrder = null) }
        viewModelScope.launch {
            recoveryWriteMutex.withLock {
                if (generation == mutationGeneration) recoverSafely { recovery.discardUnappliedOrder(planId) }
            }
        }
    }

    internal fun openExistingPlace(visit: TravelVisit) {
        if (state.value.visitDraft != null) updateVisitDraft(null)
        closePlaceSearch()
        selectVisitFromMap(visit.id)
    }

    internal fun selectVisitFromMap(visitId: String) {
        mutableState.update { current ->
            if (current.plan?.visits?.any { it.id == visitId && it.dayNumber == current.selectedDay } != true) {
                current
            } else {
                current.copy(selectedVisitId = visitId, listFocusRequest = current.listFocusRequest + 1)
            }
        }
    }

    internal fun fitMap() {
        mutableState.update { it.copy(mapFocusedVisitId = null, viewportRequest = it.viewportRequest + 1) }
    }

    internal fun create(details: TravelPlanDetails) {
        if (!state.value.creationReady || pendingSave != null || state.value.createdPlanId != null) return
        submit(null, TravelPendingSave.Create(newRequestId(), details))
    }

    internal fun inspectVisit(visitId: String) {
        val current = state.value
        if (current.isSaving || current.isLoading || current.canRetrySave || pendingSave != null || current.visitDraft != null) return
        val visit = current.plan?.visits?.firstOrNull { it.id == visitId && it.dayNumber == current.selectedDay } ?: return
        mutableState.update {
            it.copy(
                inspectedVisitId = visit.id,
                selectedVisitId = visit.id,
                mapFocusedVisitId = visit.id,
                viewportRequest = it.viewportRequest + 1,
            )
        }
    }

    internal fun closeInspection() {
        mutableState.update { it.copy(inspectedVisitId = null) }
    }

    internal fun openVisit(visit: TravelVisit?) {
        if (state.value.plan == null || state.value.isSaving || pendingSave != null) return
        mutableState.update {
            it.copy(
                inspectedVisitId = null,
                visitDraft =
                    TravelVisitDraft(
                        visit?.dayNumber ?: it.selectedDay,
                        visit,
                        placeTitle = it.dayPlaces[visit?.details?.googlePlaceId]?.name,
                        structureRevision = it.plan?.structureRevision ?: 0,
                    ),
                selectedVisitId = visit?.id ?: it.selectedVisitId,
                mapFocusedVisitId = visit?.id ?: it.mapFocusedVisitId,
                viewportRequest = if (visit == null) it.viewportRequest else it.viewportRequest + 1,
            )
        }
    }

    internal fun updateVisitDraft(draft: TravelVisitDraft?) {
        if (state.value.isSaving || pendingSave != null) return
        val planId =
            state.value.plan
                ?.summary
                ?.id ?: return
        mutableState.update {
            if (draft == null && it.failure == TravelPlanFailure.VISIT_REMOVED) {
                it.copy(visitDraft = null, failure = null, needsLatestPlan = false)
            } else {
                it.copy(visitDraft = draft)
            }
        }
        val generation = mutationGeneration
        draftWrite?.cancel()
        draftWrite =
            viewModelScope.launch {
                recoveryWriteMutex.withLock {
                    if (generation == mutationGeneration &&
                        state.value.plan
                            ?.summary
                            ?.id == planId
                    ) {
                        recoverSafely { recovery.saveDraft(planId, draft?.forRecovery()) }
                    }
                }
            }
    }

    internal fun applyVisit() {
        val draft = state.value.visitDraft ?: return
        if ((draft.visit?.details?.googlePlaceId == null && draft.name.isBlank())) return
        val details = draft.toDetails()
        val structureRevision = draft.structureRevision
        val change =
            if (draft.visit == null) {
                TravelVisitChange.Add(draft.dayNumber, details, structureRevision)
            } else {
                TravelVisitChange.Edit(draft.visit, draft.dayNumber, details, structureRevision)
            }
        changeVisit(change, selectedDayAfterSave = draft.dayNumber)
    }

    internal fun removeVisit() {
        val visit = state.value.visitDraft?.visit ?: return
        changeVisit(TravelVisitChange.Remove(visit, state.value.visitDraft?.structureRevision ?: return))
    }

    internal fun removeVisitFromList(visit: TravelVisit) {
        val current = state.value
        val plan = current.plan ?: return
        if (current.visitDraft != null || current.placeSearch != null || current.isOffline || current.canRetrySave) return
        changeVisit(TravelVisitChange.Remove(visit, plan.structureRevision))
    }

    internal fun moveVisit(
        visit: TravelVisit,
        offset: Int,
    ) {
        val plan = state.value.plan ?: return
        val visits =
            plan.visits
                .filter { it.dayNumber == visit.dayNumber }
                .map(TravelVisit::id)
                .toMutableList()
        val currentIndex = visits.indexOf(visit.id)
        val newIndex = currentIndex + offset
        if (currentIndex == -1 || newIndex !in visits.indices) return
        visits.add(newIndex, visits.removeAt(currentIndex))
        reorderVisits(visit.dayNumber, plan.orderRevision(visit.dayNumber), plan.structureRevision, visits)
    }

    internal fun undoVisit() {
        val undo = state.value.visitUndo ?: return
        changeVisit(TravelVisitChange.Undo(undo.originalRequestId), selectedDayAfterSave = undo.dayNumber)
    }

    internal fun retrySave() {
        val pending = pendingSave ?: return
        submit(pendingPlanId, pending, verifyAccess = true)
    }

    private fun changeVisit(
        change: TravelVisitChange,
        selectedDayAfterSave: Int = state.value.selectedDay,
        addedPlace: TravelPlace? = null,
    ): Boolean {
        val current = state.value
        val plan = current.plan ?: return false
        if (pendingSave != null || current.isLoading || current.isSaving || current.isOffline || current.needsLatestPlan) return false
        addedPlaceForSave = addedPlace
        submit(plan.summary.id, TravelPendingSave.Visit(newRequestId(), change, selectedDayAfterSave))
        return true
    }

    private fun applySavedVisit(
        updated: TravelPlan,
        request: TravelPendingSave.Visit,
    ) {
        val addedPlace = addedPlaceForSave
        val change = request.change
        val selectedDayAfterSave = request.selectedDay.takeIf { it in 1..updated.summary.details.dates.dayCount } ?: updated.summary.details.dates.dayCount
        mutableState.update { current ->
            val addedToVisibleDay = addedPlace != null && (change as? TravelVisitChange.Add)?.dayNumber == current.selectedDay
            val undo =
                when (change) {
                    is TravelVisitChange.Remove -> TravelVisitUndoAction(request.requestId, change.visit.dayNumber, change.visit.details.name ?: current.dayPlaces[change.visit.details.googlePlaceId]?.name, current.plan?.visits?.filter { it.dayNumber == change.visit.dayNumber }?.indexOfFirst { it.id == change.visit.id }?.coerceAtLeast(0) ?: 0)
                    is TravelVisitChange.Edit ->
                        if (change.dayNumber != change.visit.dayNumber) {
                            TravelVisitUndoAction(request.requestId, change.visit.dayNumber, change.visit.details.name ?: current.dayPlaces[change.visit.details.googlePlaceId]?.name, current.plan?.visits?.filter { it.dayNumber == change.visit.dayNumber }?.indexOfFirst { it.id == change.visit.id }?.coerceAtLeast(0) ?: 0)
                        } else {
                            current.visitUndo
                        }
                    is TravelVisitChange.Undo -> null
                    else -> current.visitUndo
                }
            current.copy(
                plan = updated,
                isOffline = false,
                visitUndo = undo,
                unappliedOrder = current.unappliedOrder.takeUnless { change is TravelVisitChange.Reorder && it?.dayNumber == change.dayNumber },
                selectedDay = selectedDayAfterSave,
                visitDraft = null,
                selectedVisitId =
                    current.selectedVisitId?.takeIf { id ->
                        updated.visits.any { it.id == id && it.dayNumber == selectedDayAfterSave }
                    },
                mapFocusedVisitId =
                    current.mapFocusedVisitId?.takeIf { id ->
                        !addedToVisibleDay &&
                            selectedDayAfterSave == current.selectedDay &&
                            updated.visits.any { it.id == id && it.dayNumber == selectedDayAfterSave }
                    },
                dayPlaces =
                    if (selectedDayAfterSave != current.selectedDay) {
                        emptyMap()
                    } else {
                        val ids =
                            updated.visits
                                .filter { it.dayNumber == current.selectedDay }
                                .mapNotNull { it.details.googlePlaceId }
                                .toSet()
                        val retained = current.dayPlaces.filterKeys { it in ids }
                        if (addedToVisibleDay) retained + (addedPlace.id to addedPlace) else retained
                    },
                dayPlacesIncomplete =
                    current.dayPlacesIncomplete &&
                        updated.visits.any {
                            it.dayNumber == selectedDayAfterSave && it.details.googlePlaceId != null
                        },
                viewportRequest =
                    if (selectedDayAfterSave != current.selectedDay || addedToVisibleDay) {
                        current.viewportRequest + 1
                    } else {
                        current.viewportRequest
                    },
                placeSearch = current.placeSearch.takeUnless { change is TravelVisitChange.Add },
            )
        }
    }

    private fun load(
        allowPending: Boolean = false,
        action: suspend () -> Unit,
    ) {
        if (state.value.isLoading || state.value.isSaving || (!allowPending && pendingSave != null)) return
        mutableState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                action()
            } catch (exception: TravelPlanException) {
                applyFailure(exception.failure)
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun submit(
        planId: String?,
        request: TravelPendingSave,
        verifyAccess: Boolean = false,
    ) {
        if (state.value.isSaving || state.value.isLoading) return
        val wasStored = pendingSave == request && state.value.pendingSaveStored
        pendingPlanId = planId
        pendingSave = request
        mutationGeneration++
        val generation = mutationGeneration
        mutableState.update { it.copy(isSaving = true, failure = null, canRetrySave = false, pendingSaveStored = wasStored) }
        viewModelScope.launch {
            var visitSaveAttempted = false
            try {
                draftWrite?.join()
                val canSubmit =
                    recoveryWriteMutex.withLock {
                        if (generation != mutationGeneration) return@withLock false
                        recovery.saveRequest(planId, request, state.value.visitDraft?.forRecovery())
                        generation == mutationGeneration
                    }
                if (!canSubmit) return@launch
                mutableState.update { it.copy(pendingSaveStored = true) }
                when (request) {
                    is TravelPendingSave.Create -> {
                        val created = repository.create(request.requestId, request.details)
                        if (generation != mutationGeneration) return@launch
                        mutableState.update { it.copy(createdPlanId = created.id) }
                    }
                    is TravelPendingSave.Visit -> {
                        val id = requireNotNull(planId)
                        if (verifyAccess) repository.get(id)
                        visitSaveAttempted = true
                        val updated = repository.changeVisit(id, request.requestId, request.change)
                        visitSaveAttempted = false
                        if (!rememberCurrentPlan(updated, generation)) return@launch
                        if (visitSaveConflictPlanId == id) {
                            visitSaveConflictPlanId = null
                            recordActivity(id, TravelUsageActivity.VISIT_CONFLICT_RECOVERED)
                        }
                        verifiedAt = TimeSource.Monotonic.markNow()
                        applySavedVisit(updated, request)
                    }
                }
                pendingSave = null
                recoveryWriteMutex.withLock {
                    if (generation == mutationGeneration) recoverSafely { recovery.finishRequest(planId, clearDraft = true) }
                }
                mutableState.update { it.copy(pendingSaveStored = false) }
            } catch (exception: TravelPlanException) {
                if (generation != mutationGeneration) return@launch
                if (planId != null && visitSaveAttempted) recordVisitSaveFailure(planId, exception.failure)
                val attemptedOrder = (request as? TravelPendingSave.Visit)?.change as? TravelVisitChange.Reorder
                val conflictedOrder = attemptedOrder.takeIf { exception.failure == TravelPlanFailure.CONFLICT }
                when (exception.failure) {
                    TravelPlanFailure.CONNECTION, TravelPlanFailure.SIGN_IN_REQUIRED -> Unit
                    TravelPlanFailure.UNAVAILABLE -> {
                        planId?.let { forgetUnavailablePlan(it) }
                    }
                    else -> {
                        pendingSave = null
                        recoveryWriteMutex.withLock {
                            if (generation == mutationGeneration) {
                                recoverSafely { recovery.finishRequest(planId, clearDraft = false, conflictedOrder = conflictedOrder) }
                            }
                        }
                        if (generation != mutationGeneration) return@launch
                        if (conflictedOrder != null) mutableState.update { it.copy(unappliedOrder = conflictedOrder) }
                    }
                }
                applyFailure(exception.failure)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (generation != mutationGeneration) return@launch
                if (planId != null && visitSaveAttempted) recordVisitSaveFailure(planId, TravelPlanFailure.CONNECTION)
                applyFailure(TravelPlanFailure.CONNECTION)
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun recordVisitSaveFailure(
        planId: String,
        failure: TravelPlanFailure,
    ) {
        when (failure) {
            TravelPlanFailure.SIGN_IN_REQUIRED, TravelPlanFailure.UNAVAILABLE -> Unit
            TravelPlanFailure.CONFLICT, TravelPlanFailure.VISIT_REMOVED -> {
                visitSaveConflictPlanId = planId
                recordActivity(planId, TravelUsageActivity.VISIT_SAVE_CONFLICT)
            }
            else -> recordActivity(planId, TravelUsageActivity.VISIT_SAVE_FAILED)
        }
    }

    private suspend fun rememberCurrentPlan(
        plan: TravelPlan,
        generation: Long,
    ): Boolean =
        recoveryWriteMutex.withLock {
            if (generation != mutationGeneration) return@withLock false
            recoverSafely { recovery.rememberPlan(plan) }
            generation == mutationGeneration
        }

    internal suspend fun forgetUnavailablePlan(planId: String) {
        mutationGeneration++
        visitSaveConflictPlanId = null
        pendingSave = null
        pendingPlanId = null
        applyFailure(TravelPlanFailure.UNAVAILABLE)
        val writing = draftWrite
        draftWrite = null
        withContext(NonCancellable) {
            writing?.cancelAndJoin()
            recoveryWriteMutex.withLock { recoverSafely { recovery.forget(planId) } }
        }
    }

    private suspend fun <Result> recoverSafely(block: suspend () -> Result): Result? =
        try {
            block()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }

    private fun applyFailure(failure: TravelPlanFailure) {
        mutableState.update {
            val current =
                when (failure) {
                    TravelPlanFailure.SIGN_IN_REQUIRED, TravelPlanFailure.UNAVAILABLE -> TravelPlannerUiState()
                    else -> it
                }
            current.copy(
                failure = failure,
                canRetrySave = pendingSave != null,
                pendingSaveStored = pendingSave != null && current.pendingSaveStored,
                visitUndo = current.visitUndo.takeUnless { failure == TravelPlanFailure.UNDO_UNAVAILABLE },
                needsLatestPlan =
                    current.needsLatestPlan || failure == TravelPlanFailure.CONFLICT || failure == TravelPlanFailure.VISIT_REMOVED,
            )
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS: Long = 350
        const val MIN_SEARCH_LENGTH: Int = 2
        val OFFLINE_PLAN_TTL = 15.minutes
    }

    @OptIn(ExperimentalUuidApi::class)
    private fun newRequestId(): String = Uuid.random().toString()
}
