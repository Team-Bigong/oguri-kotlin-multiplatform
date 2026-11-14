package com.bigong.oguri.feature.travelplanner.ui.model

import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelCoordinates
import com.bigong.oguri.domain.model.TravelDraft
import com.bigong.oguri.domain.model.TravelPlace
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSummary
import com.bigong.oguri.domain.model.TravelVisit
import com.bigong.oguri.domain.model.TravelVisitCategory
import com.bigong.oguri.domain.model.TravelVisitChange
import com.bigong.oguri.domain.model.TravelVisitDetails
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

internal data class TravelPlannerUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val failure: TravelPlanFailure? = null,
    val canRetrySave: Boolean = false,
    val pendingSaveStored: Boolean = false,
    val isOffline: Boolean = false,
    val visitUndo: TravelVisitUndoAction? = null,
    val needsLatestPlan: Boolean = false,
    val plans: List<TravelPlanSummary>? = null,
    val resumePlan: TravelPlanSummary? = null,
    val resumeLoaded: Boolean = false,
    val creationReady: Boolean = false,
    val creationRecoveryFailed: Boolean = false,
    val recoveredCreation: TravelPlanDetails? = null,
    val creationCity: String? = null,
    val creationCityFailed: Boolean = false,
    val citySearch: TravelCitySearchState? = null,
    val plan: TravelPlan? = null,
    val selectedDay: Int = 1,
    val createdPlanId: String? = null,
    val visitDraft: TravelVisitDraft? = null,
    val unappliedOrder: TravelVisitChange.Reorder? = null,
    val placeSearch: TravelPlaceSearchState? = null,
    val overview: TravelOverviewState? = null,
    val dayPlaces: Map<String, TravelPlace> = emptyMap(),
    val dayPlacesLanguage: String? = null,
    val isLoadingDayPlaces: Boolean = false,
    val dayPlacesIncomplete: Boolean = false,
    val inspectedVisitId: String? = null,
    val selectedVisitId: String? = null,
    val listFocusRequest: Int = 0,
    val mapFocusedVisitId: String? = null,
    val viewportRequest: Int = 0,
)

internal fun TravelVisitChange.Reorder.isOutsideTrip(plan: TravelPlan): Boolean =
    dayNumber !in TravelPlan.UNASSIGNED_DAY..plan.summary.details.dates.dayCount

internal fun TravelVisitChange.Reorder.canApplyTo(plan: TravelPlan): Boolean =
    !isOutsideTrip(plan) &&
        structureRevision == plan.structureRevision &&
        visitIds.size == plan.visits.count { it.dayNumber == dayNumber } &&
        visitIds.toSet() == plan.visits.filter { it.dayNumber == dayNumber }.map(TravelVisit::id).toSet()

internal data class TravelVisitUndoAction(
    val originalRequestId: String,
    val dayNumber: Int,
    val title: String? = null,
)

internal data class TravelVisitDraft(
    val dayNumber: Int,
    val visit: TravelVisit? = null,
    val name: String = visit?.details?.name.orEmpty(),
    val placeTitle: String? = null,
    val structureRevision: Long = 0,
    val location: String = visit?.details?.location.orEmpty(),
    val localTime: String =
        visit
            ?.details
            ?.localTime
            ?.toString()
            .orEmpty(),
    val note: String = visit?.details?.note.orEmpty(),
    val showLatestComparison: Boolean = false,
    val category: TravelVisitCategory? = visit?.details?.category,
) {
    val hasInvalidTime: Boolean
        get() = localTime.isNotBlank() && parsedTime() == null

    fun forRecovery(): TravelDraft = TravelDraft(dayNumber, visit, structureRevision, name, location, localTime, note, showLatestComparison, category)

    fun toDetails(): TravelVisitDetails =
        TravelVisitDetails(
            name = name.trim().ifEmpty { null },
            location = location.trim().ifEmpty { null },
            localTime = parsedTime(),
            note = note.trim().ifEmpty { null },
            googlePlaceId = visit?.details?.googlePlaceId,
            category = category,
        )

    fun rebaseOn(latest: TravelVisit): TravelVisitDraft {
        val previous = visit ?: return this
        return copy(
            visit = latest,
            showLatestComparison = true,
            dayNumber = if (dayNumber == previous.dayNumber) latest.dayNumber else dayNumber,
            name = if (name == previous.details.name.orEmpty()) latest.details.name.orEmpty() else name,
            location = if (location == previous.details.location.orEmpty()) latest.details.location.orEmpty() else location,
            localTime =
                if (localTime ==
                    previous.details.localTime
                        ?.toString()
                        .orEmpty()
                ) {
                    latest.details.localTime
                        ?.toString()
                        .orEmpty()
                } else {
                    localTime
                },
            note = if (note == previous.details.note.orEmpty()) latest.details.note.orEmpty() else note,
            category = if (category == previous.details.category) latest.details.category else category,
        )
    }

    fun latestDifferences(): Set<TravelVisitField> {
        val latest = visit?.takeIf { showLatestComparison } ?: return emptySet()
        return buildSet {
            if (dayNumber != latest.dayNumber) add(TravelVisitField.DAY)
            if (name.trim() != latest.details.name.orEmpty()) add(TravelVisitField.NAME)
            if (location.trim() != latest.details.location.orEmpty()) add(TravelVisitField.LOCATION)
            if (note.trim() != latest.details.note.orEmpty()) add(TravelVisitField.NOTE)
            if (category != latest.details.category) add(TravelVisitField.CATEGORY)
        }
    }

    private fun parsedTime(): LocalTime? {
        if (!TIME_FORMAT.matches(localTime)) return null
        return runCatching { LocalTime.parse(localTime) }.getOrNull()
    }

    private companion object {
        val TIME_FORMAT: Regex = Regex("[0-9]{2}:[0-9]{2}")
    }
}

internal data class TravelPlaceSearchState(
    val query: String = "",
    val results: List<TravelPlace> = emptyList(),
    val selected: TravelPlace? = null,
    val isLoading: Boolean = false,
    val failure: TravelPlanFailure? = null,
)

internal enum class TravelCity(
    val timeZone: TimeZone,
    val coordinates: TravelCoordinates,
    vararg val localizedNames: String,
) {
    SEOUL(TimeZone.of("Asia/Seoul"), TravelCoordinates(37.5665, 126.9780), "Seoul", "서울", "ソウル", "首尔"),
    JEJU(TimeZone.of("Asia/Seoul"), TravelCoordinates(33.4996, 126.5312), "Jeju", "제주", "済州", "济州"),
    OSAKA(TimeZone.of("Asia/Tokyo"), TravelCoordinates(34.6937, 135.5023), "Osaka", "오사카", "大阪"),
    TOKYO(TimeZone.of("Asia/Tokyo"), TravelCoordinates(35.6762, 139.6503), "Tokyo", "도쿄", "東京", "东京"),
    TAIPEI(TimeZone.of("Asia/Taipei"), TravelCoordinates(25.0330, 121.5654), "Taipei", "타이베이", "台北"),
    HO_CHI_MINH(
        TimeZone.of("Asia/Ho_Chi_Minh"), TravelCoordinates(10.82302, 106.62965), "Ho Chi Minh City", "호치민", "ホーチミン", "胡志明市",
    ),
    SYDNEY(TimeZone.of("Australia/Sydney"), TravelCoordinates(-33.86785, 151.20732), "Sydney", "시드니", "シドニー", "悉尼"),
    BARCELONA(TimeZone.of("Europe/Madrid"), TravelCoordinates(41.38879, 2.15899), "Barcelona", "바르셀로나", "バルセロナ", "巴塞罗那"),
    PARIS(TimeZone.of("Europe/Paris"), TravelCoordinates(48.8566, 2.3522), "Paris", "파리", "パリ", "巴黎"),
    ;

    companion object {
        fun centerOf(name: String): TravelCoordinates? =
            entries.firstOrNull { city -> city.localizedNames.any { it.equals(name.substringBefore(',').trim(), ignoreCase = true) } }?.coordinates
    }
}

internal fun TravelPlanSummary.hasEnded(todayInCity: (TimeZone) -> LocalDate): Boolean =
    details.dates.endInclusive < todayInCity(details.timeZone)

internal fun suggestedTimeZone(city: String): String? =
    TravelCity.entries
        .firstOrNull { preset -> preset.localizedNames.any { it.equals(city, ignoreCase = true) } }
        ?.timeZone
        ?.id ?: catalogueTimeZones[city]

private val catalogueTimeZones: Map<String, String> =
    mapOf(
        "후쿠오카" to "Asia/Tokyo",
        "삿포로" to "Asia/Tokyo",
        "세부" to "Asia/Manila",
        "보라카이" to "Asia/Manila",
        "보홀" to "Asia/Manila",
        "상하이" to "Asia/Shanghai",
        "베이징" to "Asia/Shanghai",
        "칭다오" to "Asia/Shanghai",
        "마드리드" to "Europe/Madrid",
        "LA" to "America/Los_Angeles",
        "샌프란시스코" to "America/Los_Angeles",
        "뉴욕" to "America/New_York",
        "다낭" to "Asia/Ho_Chi_Minh",
        "브리즈번" to "Australia/Brisbane",
        "멜버른" to "Australia/Melbourne",
        "니스" to "Europe/Paris",
        "부산" to "Asia/Seoul",
        "제주도" to "Asia/Seoul",
    )

internal data class TravelCitySearchState(
    val query: String = "",
    val suggestions: List<TravelCitySuggestion> = emptyList(),
    val selected: TravelCitySuggestion? = null,
    val timeZone: String? = null,
    val loading: Boolean = false,
    val failure: TravelPlanFailure? = null,
)

internal fun TravelDraft.toVisitDraft(): TravelVisitDraft =
    TravelVisitDraft(
        dayNumber,
        original,
        name,
        structureRevision = structureRevision,
        location = location,
        localTime = localTime,
        note = note,
        showLatestComparison = showLatestComparison,
        category = category,
    )

internal enum class TravelVisitField { DAY, NAME, LOCATION, TIME, NOTE, CATEGORY }

internal data class TravelOverviewState(
    val places: Map<String, TravelPlace> = emptyMap(),
    val loading: Boolean = true,
    val incomplete: Boolean = false,
    val language: String? = null,
)
