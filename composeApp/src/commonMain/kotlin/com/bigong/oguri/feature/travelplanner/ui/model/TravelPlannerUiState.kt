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

import kotlin.math.abs

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
    val searchCity: String? = null,
    val createdPlanId: String? = null,
    val visitDraft: TravelVisitDraft? = null,
    val unappliedOrder: TravelVisitChange.Reorder? = null,
    val placeSearch: TravelPlaceSearchState? = null,
    val lodgingAddition: TravelLodgingAddition? = null,
    val overview: TravelOverviewState? = null,
    val dayPlaces: Map<String, TravelPlace> = emptyMap(),
    val attemptedDayPlaceIds: Set<String> = emptySet(),
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
    dayNumber !in 1..plan.summary.details.dates.dayCount

internal data class TravelVisitUndoAction(
    val originalRequestId: String,
    val dayNumber: Int,
    val title: String? = null,
    val position: Int = 0,
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

    fun useSavedValues(): TravelVisitDraft =
        visit?.let { TravelVisitDraft(it.dayNumber, it, placeTitle = placeTitle, structureRevision = structureRevision) } ?: this

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

internal data class TravelLodgingAddition(
    val change: TravelVisitChange.Add,
    val place: TravelPlace?,
    val offeredDays: List<Int>,
)

internal data class TravelPlaceSearchState(
    val query: String = "",
    val results: List<TravelPlace> = emptyList(),
    val selection: TravelPlaceSelection? = null,
    val isLoading: Boolean = false,
    val failure: TravelPlanFailure? = null,
) {
    fun select(placeId: String): TravelPlaceSearchState {
        if (selection?.place?.id == placeId) return this
        val place = results.firstOrNull { it.id == placeId } ?: return this
        return copy(selection = TravelPlaceSelection(place))
    }

    fun selectCategory(category: TravelVisitCategory?): TravelPlaceSearchState {
        val currentSelection = selection ?: return this
        return copy(selection = currentSelection.copy(category = category))
    }
}

internal data class TravelPlaceSelection(
    val place: TravelPlace,
    val category: TravelVisitCategory? = place.suggestedCategory ?: TravelVisitCategory.OTHER,
)

internal enum class TravelCity(
    val timeZone: TimeZone,
    val coordinates: TravelCoordinates,
    vararg val localizedNames: String,
) {
    SEOUL(TimeZone.of("Asia/Seoul"), TravelCoordinates(37.5665, 126.9780), "Seoul", "서울", "ソウル", "首尔"),
    JEJU(TimeZone.of("Asia/Seoul"), TravelCoordinates(33.4996, 126.5312), "Jeju", "Jeju City", "제주", "제주시", "제주도", "済州", "济州"),
    OSAKA(TimeZone.of("Asia/Tokyo"), TravelCoordinates(34.6937, 135.5023), "Osaka", "오사카", "大阪"),
    TOKYO(TimeZone.of("Asia/Tokyo"), TravelCoordinates(35.6762, 139.6503), "Tokyo", "도쿄", "東京", "东京"),
    TAIPEI(TimeZone.of("Asia/Taipei"), TravelCoordinates(25.0330, 121.5654), "Taipei", "타이베이", "台北"),
    HO_CHI_MINH(
        TimeZone.of("Asia/Ho_Chi_Minh"), TravelCoordinates(10.82302, 106.62965), "Ho Chi Minh City", "호치민", "ホーチミン", "胡志明市",
    ),
    SYDNEY(TimeZone.of("Australia/Sydney"), TravelCoordinates(-33.86785, 151.20732), "Sydney", "시드니", "シドニー", "悉尼"),
    BARCELONA(TimeZone.of("Europe/Madrid"), TravelCoordinates(41.38879, 2.15899), "Barcelona", "바르셀로나", "バルセロナ", "巴塞罗那"),
    PARIS(TimeZone.of("Europe/Paris"), TravelCoordinates(48.8566, 2.3522), "Paris", "파리", "パリ", "巴黎"),
    INCHEON(TimeZone.of("Asia/Seoul"), TravelCoordinates(37.46, 126.71), "Incheon", "인천", "仁川"),
    SUWON(TimeZone.of("Asia/Seoul"), TravelCoordinates(37.27, 127.03), "Suwon", "수원", "水原"),
    SEOGWIPO(TimeZone.of("Asia/Seoul"), TravelCoordinates(33.25, 126.56), "Seogwipo", "서귀포", "西帰浦", "西归浦"),
    AEWOL(TimeZone.of("Asia/Seoul"), TravelCoordinates(33.46, 126.32), "Aewol", "애월", "涯月"),
    KYOTO(TimeZone.of("Asia/Tokyo"), TravelCoordinates(35.01, 135.77), "Kyoto", "교토", "京都"),
    NARA(TimeZone.of("Asia/Tokyo"), TravelCoordinates(34.68, 135.80), "Nara", "나라", "奈良"),
    YOKOHAMA(TimeZone.of("Asia/Tokyo"), TravelCoordinates(35.44, 139.64), "Yokohama", "요코하마", "横浜", "横滨"),
    KAMAKURA(TimeZone.of("Asia/Tokyo"), TravelCoordinates(35.32, 139.55), "Kamakura", "가마쿠라", "鎌倉", "镰仓"),
    TAMSUI(TimeZone.of("Asia/Taipei"), TravelCoordinates(25.17, 121.44), "Tamsui", "단수이", "淡水"),
    KEELUNG(TimeZone.of("Asia/Taipei"), TravelCoordinates(25.13, 121.74), "Keelung", "지룽", "基隆"),
    VERSAILLES(TimeZone.of("Europe/Paris"), TravelCoordinates(48.80, 2.13), "Versailles", "베르사유", "ヴェルサイユ", "凡尔赛"),
    SAINT_GERMAIN_EN_LAYE(TimeZone.of("Europe/Paris"), TravelCoordinates(48.90, 2.09), "Saint-Germain-en-Laye", "생제르맹앙레", "サン＝ジェルマン＝アン＝レー", "圣日耳曼昂莱"),
    VUNG_TAU(TimeZone.of("Asia/Ho_Chi_Minh"), TravelCoordinates(10.35, 107.09), "Vung Tau", "붕따우", "ブンタウ", "头顿", "Vũng Tàu"),
    MY_THO(TimeZone.of("Asia/Ho_Chi_Minh"), TravelCoordinates(10.36, 106.36), "My Tho", "미토", "ミトー", "美拖", "Mỹ Tho"),
    BLUE_MOUNTAINS(TimeZone.of("Australia/Sydney"), TravelCoordinates(-33.71, 150.31), "Blue Mountains", "블루마운틴", "ブルー・マウンテンズ", "蓝山"),
    WOLLONGONG(TimeZone.of("Australia/Sydney"), TravelCoordinates(-34.43, 150.89), "Wollongong", "울런공", "ウロンゴン", "卧龙岗"),
    SITGES(TimeZone.of("Europe/Madrid"), TravelCoordinates(41.24, 1.81), "Sitges", "시체스", "シッチェス", "锡切斯"),
    GIRONA(TimeZone.of("Europe/Madrid"), TravelCoordinates(41.98, 2.82), "Girona", "지로나", "ジローナ", "赫罗纳"),
    ;

    companion object {
        private val nearbyGroups = listOf(
            listOf(SEOUL, INCHEON, SUWON),
            listOf(JEJU, SEOGWIPO, AEWOL),
            listOf(OSAKA, KYOTO, NARA),
            listOf(TOKYO, YOKOHAMA, KAMAKURA),
            listOf(TAIPEI, TAMSUI, KEELUNG),
            listOf(PARIS, VERSAILLES, SAINT_GERMAIN_EN_LAYE),
            listOf(HO_CHI_MINH, VUNG_TAU, MY_THO),
            listOf(SYDNEY, BLUE_MOUNTAINS, WOLLONGONG),
            listOf(BARCELONA, SITGES, GIRONA),
        )

        private const val PRESET_MATCH_TOLERANCE_DEGREES = 1.0

        fun named(name: String): TravelCity? =
            entries.firstOrNull { city -> city.localizedNames.any { it.equals(name.trim(), ignoreCase = true) } }

        fun centerOf(name: String): TravelCoordinates? = named(name.substringBefore(','))?.coordinates

        fun nearby(
            primaryCity: String,
            selectedCities: List<String>,
            catalogCities: Map<String, TravelCitySuggestion>,
        ): List<TravelCity> {
            val origin = resolvePreset(primaryCity, catalogCities[primaryCity]) ?: return emptyList()
            val selected = selectedCities.mapNotNull { resolvePreset(it, catalogCities[it]) }.toSet()
            return nearbyGroups.firstOrNull { origin in it }.orEmpty().filter { it != origin && it !in selected }
        }

        private fun resolvePreset(label: String, suggestion: TravelCitySuggestion?): TravelCity? {
            if (suggestion == null) return named(label)
            val location = suggestion.coordinates ?: return null
            val preset = named(suggestion.name) ?: return null
            // Search can return a city with the same name in another country.
            return preset.takeIf {
                abs(location.latitude - it.coordinates.latitude) <= PRESET_MATCH_TOLERANCE_DEGREES &&
                    abs(location.longitude - it.coordinates.longitude) <= PRESET_MATCH_TOLERANCE_DEGREES
            }
        }
    }
}

internal fun TravelPlanSummary.hasEnded(todayInCity: (TimeZone) -> LocalDate): Boolean =
    details.dates.endInclusive < todayInCity(details.timeZone)

internal fun suggestedTimeZone(city: String): String? =
    TravelCity.named(city)?.timeZone?.id ?: catalogueTimeZones[city]

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
