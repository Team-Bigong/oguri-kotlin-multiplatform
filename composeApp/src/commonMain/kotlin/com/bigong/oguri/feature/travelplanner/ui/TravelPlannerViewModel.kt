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
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitChange
import com.bigong.oguri.domain.model.TravelVisitDetails
import com.bigong.oguri.domain.repository.TravelPlaceRepository
import com.bigong.oguri.domain.repository.TravelPlanRepository
import com.bigong.oguri.domain.repository.TravelRecoveryRepository
import com.bigong.oguri.domain.usecase.GetPlaceDetailUseCase
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlaceSearchState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.TravelVisitUndoAction
import com.bigong.oguri.feature.travelplanner.ui.model.toVisitDraft
import com.bigong.oguri.feature.travelplanner.ui.model.withRefreshedPlan
import dev.zacsweers.metro.Inject
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
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Inject
class TravelPlannerViewModel(
    private val repository: TravelPlanRepository,
    private val placeRepository: TravelPlaceRepository,
    private val getPlaceDetail: GetPlaceDetailUseCase,
    private val recovery: TravelRecoveryRepository,
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
    private var addedPlaceForSave: TravelPlace? = null

    internal suspend fun loadResume() {
        val plan =
            try {
                repository.resume()
            } catch (_: TravelPlanException) {
                null
            }
        mutableState.update { it.copy(resumePlan = plan) }
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
                        placeRepository.cityTimeZone(suggestion.id)
                    } catch (_: TravelPlanException) {
                        null
                    }
                currentCoroutineContext().ensureActive()
                mutableState.update { it.copy(citySearch = it.citySearch?.copy(timeZone = zone, loading = false)) }
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
                    if (cached == null) {
                        mutableState.update { it.copy(plan = null, visitDraft = null, isOffline = true) }
                        throw exception
                    }
                    val savedDay = cached.lastViewedDay?.takeIf { it in 0..cached.summary.details.dates.dayCount }
                    mutableState.update {
                        it.copy(
                            plan = cached,
                            isOffline = true,
                            failure = TravelPlanFailure.CONNECTION,
                            visitDraft = it.visitDraft ?: stored?.draft?.toVisitDraft(),
                            canRetrySave = pendingSave != null,
                            selectedDay = if (wasLoaded) it.selectedDay else savedDay ?: it.selectedDay,
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
                    plan.lastViewedDay?.takeIf { it in 0..dates.dayCount }
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
                                dayNumber = it.dayNumber.takeIf { day -> day in 0..dates.dayCount } ?: TravelPlan.UNASSIGNED_DAY,
                            )
                        }
                    }
                val changedStructure = nextDraft?.let { it.structureRevision != plan.structureRevision } == true
                current.copy(
                    plan = plan,
                    isOffline = false,
                    canRetrySave = pendingSave != null,
                    selectedDay =
                        if (current.plan ==
                            null
                        ) {
                            initialDay
                        } else {
                            current.selectedDay.takeIf { it in 0..dates.dayCount } ?: initialDay
                        },
                    visitDraft = nextDraft,
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
        }
    }

    internal suspend fun refreshPlan(planId: String) {
        val before = state.value
        if (before.isLoading || before.isSaving) return
        if (before.isOffline || (before.plan == null && before.failure == TravelPlanFailure.CONNECTION)) {
            loadPlan(planId)
            return
        }
        if (before.plan == null) return
        val generation = mutationGeneration
        try {
            val updated = repository.get(planId)
            currentCoroutineContext().ensureActive()
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
        if (!hasPlaces) {
            mutableState.update { it.copy(dayPlaces = emptyMap(), isLoadingDayPlaces = false, dayPlacesIncomplete = false) }
            return
        }
        mutableState.update { it.copy(isLoadingDayPlaces = true, dayPlacesIncomplete = false) }
        try {
            val result = placeRepository.forDay(planId, dayNumber, language)
            currentCoroutineContext().ensureActive()
            mutableState.update {
                if (it.selectedDay != dayNumber || it.plan?.summary?.id != planId) {
                    it
                } else {
                    it.copy(
                        dayPlaces = result.places,
                        isLoadingDayPlaces = false,
                        dayPlacesIncomplete = result.incomplete,
                    )
                }
            }
        } catch (exception: TravelPlanException) {
            currentCoroutineContext().ensureActive()
            if (exception.failure == TravelPlanFailure.SIGN_IN_REQUIRED || exception.failure == TravelPlanFailure.UNAVAILABLE) {
                if (exception.failure == TravelPlanFailure.UNAVAILABLE) forgetUnavailablePlan(planId) else applyFailure(exception.failure)
            } else {
                mutableState.update { it.copy(isLoadingDayPlaces = false, dayPlacesIncomplete = true) }
            }
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

    internal fun searchPlaces(
        query: String,
        language: String,
    ) {
        val planId =
            state.value.plan
                ?.summary
                ?.id ?: return
        val search = state.value.placeSearch ?: return
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
                    val results = placeRepository.search(planId, query.trim(), language)
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
    ) {
        changeVisit(
            change =
                TravelVisitChange.Add(
                    dayNumber,
                    TravelVisitDetails(null, null, null, null, place.id),
                    state.value.plan?.structureRevision ?: return,
                ),
            addedPlace = place,
        )
    }

    internal fun reorderVisits(visitIds: List<String>) {
        val current = state.value
        val plan = current.plan ?: return
        val day = current.selectedDay
        val originalIds = plan.visits.filter { it.dayNumber == day }.map(TravelVisit::id)
        if (visitIds == originalIds || visitIds.size != originalIds.size || visitIds.toSet() != originalIds.toSet()) return
        changeVisit(TravelVisitChange.Reorder(day, plan.orderRevision(day), visitIds, plan.structureRevision))
    }

    internal fun openExistingPlace(visit: TravelVisit) {
        closePlaceSearch()
        selectVisitFromMap(visit.id)
    }

    internal fun selectVisitFromMap(visitId: String) {
        mutableState.update { it.copy(selectedVisitId = visitId, listFocusRequest = it.listFocusRequest + 1) }
    }

    internal fun fitMap() {
        mutableState.update { it.copy(mapFocusedVisitId = null, viewportRequest = it.viewportRequest + 1) }
    }

    internal fun create(details: TravelPlanDetails) {
        if (!state.value.creationReady || pendingSave != null || state.value.createdPlanId != null) return
        submit(null, TravelPendingSave.Create(newRequestId(), details))
    }

    internal fun openVisit(visit: TravelVisit?) {
        if (state.value.plan == null || state.value.isSaving || pendingSave != null) return
        mutableState.update {
            it.copy(
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
        if ((draft.visit?.details?.googlePlaceId == null && draft.name.isBlank()) || draft.hasInvalidTime) return
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
        reorderVisits(visits)
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
    ) {
        val current = state.value
        val plan = current.plan ?: return
        if (pendingSave != null || current.isLoading || current.isSaving || current.isOffline || current.needsLatestPlan) return
        addedPlaceForSave = addedPlace
        submit(plan.summary.id, TravelPendingSave.Visit(newRequestId(), change, selectedDayAfterSave))
    }

    private fun applySavedVisit(
        updated: TravelPlan,
        request: TravelPendingSave.Visit,
    ) {
        val addedPlace = addedPlaceForSave
        val change = request.change
        val selectedDayAfterSave = request.selectedDay.takeIf { it in 0..updated.summary.details.dates.dayCount } ?: 1
        mutableState.update { current ->
            val addedToVisibleDay = addedPlace != null && (change as? TravelVisitChange.Add)?.dayNumber == current.selectedDay
            val undo =
                when (change) {
                    is TravelVisitChange.Remove -> TravelVisitUndoAction(request.requestId, change.visit.dayNumber)
                    is TravelVisitChange.Edit ->
                        if (change.dayNumber != change.visit.dayNumber) {
                            TravelVisitUndoAction(request.requestId, change.visit.dayNumber)
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
                placeSearch =
                    current.placeSearch?.let { search ->
                        search.copy(selected = null, lastAddedPlaceId = addedPlace?.id)
                    },
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
        pendingPlanId = planId
        pendingSave = request
        mutationGeneration++
        val generation = mutationGeneration
        mutableState.update { it.copy(isSaving = true, failure = null, canRetrySave = false) }
        viewModelScope.launch {
            try {
                draftWrite?.join()
                val canSubmit =
                    recoveryWriteMutex.withLock {
                        if (generation != mutationGeneration) return@withLock false
                        recovery.saveRequest(planId, request, state.value.visitDraft?.forRecovery())
                        generation == mutationGeneration
                    }
                if (!canSubmit) return@launch
                when (request) {
                    is TravelPendingSave.Create -> {
                        val created = repository.create(request.requestId, request.details)
                        if (generation != mutationGeneration) return@launch
                        mutableState.update { it.copy(createdPlanId = created.id) }
                    }
                    is TravelPendingSave.Visit -> {
                        val id = requireNotNull(planId)
                        if (verifyAccess) repository.get(id)
                        val updated = repository.changeVisit(id, request.requestId, request.change)
                        if (!rememberCurrentPlan(updated, generation)) return@launch
                        verifiedAt = TimeSource.Monotonic.markNow()
                        applySavedVisit(updated, request)
                    }
                }
                pendingSave = null
                recoverSafely { recovery.finishRequest(planId, clearDraft = true) }
            } catch (exception: TravelPlanException) {
                if (generation != mutationGeneration) return@launch
                when (exception.failure) {
                    TravelPlanFailure.CONNECTION, TravelPlanFailure.SIGN_IN_REQUIRED -> Unit
                    TravelPlanFailure.UNAVAILABLE -> {
                        planId?.let { forgetUnavailablePlan(it) }
                    }
                    else -> {
                        pendingSave = null
                        recoverSafely { recovery.finishRequest(planId, clearDraft = false) }
                    }
                }
                applyFailure(exception.failure)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                applyFailure(TravelPlanFailure.CONNECTION)
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
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

    private suspend fun forgetUnavailablePlan(planId: String) {
        mutationGeneration++
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
