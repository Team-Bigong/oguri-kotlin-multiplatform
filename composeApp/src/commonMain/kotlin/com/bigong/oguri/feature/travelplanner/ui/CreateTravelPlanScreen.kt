package com.bigong.oguri.feature.travelplanner.ui

import org.jetbrains.compose.resources.painterResource
import oguri.composeapp.generated.resources.ic_pen
import oguri.composeapp.generated.resources.ic_calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextField
import com.bigong.oguri.feature.travelplanner.ui.component.TravelBottomAction
import oguri.composeapp.generated.resources.calendar_month_day
import oguri.composeapp.generated.resources.travel_card_date_range
import oguri.composeapp.generated.resources.travel_create_heading
import oguri.composeapp.generated.resources.travel_create_description
import oguri.composeapp.generated.resources.travel_actual_dates
import oguri.composeapp.generated.resources.travel_create_recommendation
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.ic_travel_location
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.feature.travelplanner.ui.component.TravelCitySearchDialog
import com.bigong.oguri.feature.travelplanner.ui.component.cityLabel
import com.bigong.oguri.feature.travelplanner.ui.component.TravelFieldColors
import com.bigong.oguri.feature.travelplanner.ui.component.TravelIssueCard
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone
import oguri.composeapp.generated.resources.travel_add_city
import oguri.composeapp.generated.resources.travel_primary_city
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_retry
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_city_unavailable
import oguri.composeapp.generated.resources.travel_city_unavailable_message
import oguri.composeapp.generated.resources.travel_choose_city
import oguri.composeapp.generated.resources.travel_save_failed_label
import oguri.composeapp.generated.resources.travel_create
import oguri.composeapp.generated.resources.travel_creation_options
import oguri.composeapp.generated.resources.travel_default_title
import oguri.composeapp.generated.resources.travel_end_date
import oguri.composeapp.generated.resources.travel_invalid_dates
import oguri.composeapp.generated.resources.travel_matching_plan
import oguri.composeapp.generated.resources.travel_open_plan
import oguri.composeapp.generated.resources.travel_past_collapsed
import oguri.composeapp.generated.resources.travel_past_expanded
import oguri.composeapp.generated.resources.travel_recommended_dates
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.travel_sign_in
import oguri.composeapp.generated.resources.travel_start_date
import oguri.composeapp.generated.resources.travel_time_zone
import oguri.composeapp.generated.resources.travel_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun CreateTravelPlanScreen(
    state: TravelPlannerUiState,
    source: TravelPlanSource?,
    signedIn: Boolean,
    onBack: () -> Unit,
    onCreate: (TravelPlanDetails) -> Unit,
    onRetrySave: () -> Unit,
    onRetryRecovery: () -> Unit,
    onSignIn: () -> Unit,
    onOpenExisting: (String) -> Unit,
    onRetryDestination: () -> Unit,
    onOpenCitySearch: () -> Unit,
    onCloseCitySearch: () -> Unit,
    onSearchCities: (String) -> Unit,
    onChooseCity: (TravelCitySuggestion) -> Unit,
) {
    var cityInput by rememberSaveable { mutableStateOf("") }
    var cityEdited by rememberSaveable { mutableStateOf(false) }
    var selectedCities by rememberSaveable { mutableStateOf(listOf<String>()) }
    var catalogCities by rememberSaveable(stateSaver = CityCatalogSaver) {
        mutableStateOf(emptyMap<String, TravelCitySuggestion>())
    }
    var cityTimeZones by rememberSaveable { mutableStateOf(mapOf<String, String>()) }
    var title by rememberSaveable { mutableStateOf("") }
    var startMillis by rememberSaveable { mutableStateOf(source?.startDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var endMillis by rememberSaveable { mutableStateOf(source?.endDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var isDatePickerForStart by rememberSaveable { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(state.recoveredCreation) {
        state.recoveredCreation?.let { details ->
            cityEdited = true
            cityInput = details.city
            selectedCities = details.cities
            catalogCities = details.catalogCities
            cityTimeZones =
                details.catalogCities.mapNotNull { (label, city) -> city.timeZone?.let { label to it } }.toMap() +
                    (details.city to details.timeZone.id)
            title = details.title
            startMillis =
                details.dates.start
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
            endMillis =
                details.dates.endInclusive
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
        }
    }
    LaunchedEffect(state.creationCity) {
        val recommendedCity = state.creationCity ?: return@LaunchedEffect
        if (!cityEdited) cityInput = recommendedCity
    }
    val citySearch = state.citySearch
    LaunchedEffect(citySearch?.selected, citySearch?.loading) {
        if (citySearch?.selected != null && !citySearch.loading) {
            cityEdited = true
            val suggestion = citySearch.selected
            val chosen = catalogCities.values.firstOrNull { it.id == suggestion.id }?.label ?: suggestion.label
            val primary = selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) }
            selectedCities = (primary + chosen).distinct().take(10)
            if (chosen in selectedCities) catalogCities = catalogCities + (chosen to suggestion.copy(label = chosen))
            cityInput = selectedCities.first()
            citySearch.timeZone?.takeIf { it in TimeZone.availableZoneIds }?.let { cityTimeZones = cityTimeZones + (chosen to it) }
            onCloseCitySearch()
        }
    }
    val cityName = selectedCities.firstOrNull() ?: cityInput.trim()
    val timeZoneId = cityTimeZones[cityName] ?: suggestedTimeZone(cityName)
    val cityTimeZone = timeZoneId?.let(TimeZone::of)
    val defaultTitle =
        stringResource(Res.string.travel_default_title, catalogCities[cityName]?.name ?: cityName)
            .take(TravelPlanDetails.MAX_TEXT_LENGTH)
    val startDate = startMillis?.toTravelDate()
    val endDate = endMillis?.toTravelDate()
    val dates =
        if (startDate != null && endDate != null) {
            runCatching { TravelDates(startDate, endDate) }.getOrNull()
        } else {
            null
        }
    val hasInvalidDates = startDate != null && endDate != null && dates == null
    val canEdit = state.creationReady && !state.isSaving && !state.canRetrySave
    val matchingPlan =
        state.plans?.firstOrNull {
            val savedCity = it.details.catalogCities[it.details.city]
            val chosenCity = catalogCities[cityName]
            val sameCity = if (savedCity != null && chosenCity != null) savedCity.id == chosenCity.id else it.details.city.equals(cityName, ignoreCase = true)
            sameCity && it.details.dates == dates
        }
    val startLabel = stringResource(Res.string.travel_start_date)
    val endLabel = stringResource(Res.string.travel_end_date)
    val titleLabel = stringResource(Res.string.travel_title)

    val creationSource = state.recoveredCreation?.source ?: source
    val recommendedStart = creationSource?.startDate
    val recommendedEnd = creationSource?.endDate
    val colors = OguriTheme.colors
    TravelPlannerPage(
        title = stringResource(Res.string.travel_create),
        onBack = onBack,
        isSaving = state.isSaving,
        showTopDivider = false,
        modifier = Modifier.imePadding(),
    ) {
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            if (!state.creationReady && !state.creationRecoveryFailed) TravelLoading(Modifier.fillMaxWidth())
            Column(Modifier.padding(horizontal = 4.dp)) {
                Text(stringResource(Res.string.travel_create_heading), style = OguriTheme.typography.sectionTitle, color = colors.textPrimary)
                Text(stringResource(Res.string.travel_create_description), style = OguriTheme.typography.cardSubtitle, color = colors.textTertiary)
            }
            Spacer(Modifier.height(36.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.travel_city), style = OguriTheme.typography.labelMedium, color = colors.textTertiary)
                val cities = selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) }
                val cityShape = RoundedCornerShape(8.dp)
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 48.dp).clip(cityShape)
                        .background(colors.surfaceCard).border(1.dp, colors.borderStrong, cityShape)
                        .noRippleClickable(enabled = canEdit && cities.size < 10, onClick = onOpenCitySearch)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(painterResource(Res.drawable.ic_travel_location), null, Modifier.size(20.dp), tint = colors.textTertiary)
                    Text(
                        if (cities.isEmpty()) stringResource(Res.string.travel_choose_city) else cities.joinToString(" · ") { catalogCities[it]?.name ?: it },
                        Modifier.weight(1f),
                        style = travelTextStyle(15, 18),
                        color = if (cities.isEmpty()) colors.textTertiary else colors.textPrimary,
                    )
                    Text(stringResource(Res.string.travel_search_cities), style = OguriTheme.typography.labelMedium, color = colors.textTertiary)
                }
                if (cities.size > 1) {
                    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        cities.forEach { city ->
                            TravelTextButton(enabled = canEdit, onClick = {
                                selectedCities = cities - city
                                catalogCities = catalogCities - city
                                cityInput = selectedCities.firstOrNull().orEmpty()
                            }) { Text("${catalogCities[city]?.name ?: city} ×") }
                        }
                    }
                    Text(stringResource(Res.string.travel_primary_city), style = OguriTheme.typography.caption, color = colors.textTertiary)
                }
            }
            if (state.creationCityFailed) {
                TravelIssueCard(
                    title = stringResource(Res.string.travel_city_unavailable),
                    description = stringResource(Res.string.travel_city_unavailable_message),
                    onRetry = onRetryDestination.takeIf { canEdit },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(stringResource(Res.string.travel_actual_dates), style = OguriTheme.typography.labelMedium, color = colors.textTertiary)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TravelDateButton(startLabel, startDate, canEdit, Modifier.weight(1f)) { isDatePickerForStart = true }
                TravelDateButton(endLabel, endDate, canEdit, Modifier.weight(1f), isError = hasInvalidDates) { isDatePickerForStart = false }
            }
            if (recommendedStart != null && recommendedEnd != null) {
                Text(
                    stringResource(
                        Res.string.travel_create_recommendation,
                        stringResource(
                            Res.string.travel_card_date_range,
                            stringResource(Res.string.calendar_month_day, recommendedStart.month.ordinal + 1, recommendedStart.day),
                            stringResource(Res.string.calendar_month_day, recommendedEnd.month.ordinal + 1, recommendedEnd.day),
                        ),
                    ),
                    Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
                    style = OguriTheme.typography.caption, color = colors.textTertiary,
                )
            }
            if (hasInvalidDates) {
                Text(
                    stringResource(Res.string.travel_invalid_dates, TravelDates.MAX_DAYS),
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp),
                    style = OguriTheme.typography.caption,
                    color = OguriTheme.colors.error,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(titleLabel, style = OguriTheme.typography.labelMedium, color = colors.textTertiary)
            Spacer(Modifier.height(8.dp))
            TravelTextField(
                value = title,
                onValueChange = { title = it },
                limit = TravelPlanDetails.MAX_TEXT_LENGTH,
                singleLine = true,
                enabled = canEdit,
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = titleLabel },
                textStyle = travelTextStyle(15, 18).copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                decorationBox = { field ->
                    Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).background(colors.surfaceCard, RoundedCornerShape(8.dp))
                        .border(1.dp, colors.borderStrong, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                        if (title.isEmpty()) Text(if (cityName.isBlank()) titleLabel else defaultTitle, style = travelTextStyle(15, 18), color = colors.textTertiary)
                        field()
                    }
                },
            )
            state.failure?.takeUnless { it == TravelPlanFailure.SIGN_IN_REQUIRED }?.let {
                if (state.canRetrySave) {
                    TravelIssueCard(
                        title = stringResource(Res.string.travel_save_failed_label),
                        onRetry = onRetrySave.takeUnless { state.isSaving },
                        modifier = Modifier.padding(top = 24.dp),
                    )
                } else {
                    TravelInlineFailure(
                        it,
                        onRetry = onRetryRecovery.takeIf { state.creationRecoveryFailed },
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }
            if (matchingPlan != null) {
                val shape = RoundedCornerShape(8.dp)
                Column(
                    Modifier.fillMaxWidth().padding(top = 24.dp)
                        .background(OguriTheme.colors.mintContainer, shape).border(1.dp, OguriTheme.colors.borderSubtle, shape)
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(stringResource(Res.string.travel_matching_plan), style = OguriTheme.typography.cardSubtitle)
                    val range = travelCardDates(matchingPlan.details.dates, includeYear = false)
                    Text(
                        "${matchingPlan.details.title} · $range",
                        style = OguriTheme.typography.caption,
                        color = colors.textSecondary,
                    )
                    TravelTextButton(
                        onClick = { onOpenExisting(matchingPlan.id) },
                        modifier = Modifier.align(Alignment.End),
                        enabled = canEdit,
                    ) {
                        Text(stringResource(Res.string.travel_open_plan))
                    }
                }
            }
            if (state.failure == TravelPlanFailure.SIGN_IN_REQUIRED && signedIn) {
                TravelInlineFailure(
                    TravelPlanFailure.SIGN_IN_REQUIRED,
                    onRetry = onSignIn,
                    retryLabel = stringResource(Res.string.travel_sign_in),
                )
            }
        }
        TravelBottomAction(
            enabled = canEdit && dates != null && cityName.isNotEmpty() && cityTimeZone != null,
            onClick = {
                if (dates != null && cityTimeZone != null) {
                    val cities = selectedCities.ifEmpty { listOf(cityName) }
                    onCreate(
                        TravelPlanDetails(
                            cityName, cityTimeZone, title.trim().ifEmpty { defaultTitle }, dates, creationSource, cities,
                            catalogCities.filterKeys { it in cities },
                        ),
                    )
                }
            },
        ) {
            Text(stringResource(if (signedIn) Res.string.travel_create else Res.string.travel_sign_in))
        }
    }

    if (citySearch != null) {
        val currentCities = selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) }
        val nearbyCities = TravelCity.nearby(cityName, currentCities, catalogCities)
        TravelCitySearchDialog(
            state = citySearch,
            selectedCities = currentCities,
            nearbyCities = nearbyCities.map { cityLabel(it) },
            recommendedCities = listOf(
                TravelCity.SEOUL, TravelCity.JEJU, TravelCity.OSAKA, TravelCity.TOKYO, TravelCity.TAIPEI,
                TravelCity.PARIS, TravelCity.HO_CHI_MINH, TravelCity.SYDNEY, TravelCity.BARCELONA,
            ).map { cityLabel(it) },
            onQuery = onSearchCities,
            onChoose = onChooseCity,
            onChooseRecommendedCity = { city ->
                cityEdited = true
                selectedCities = (currentCities + city).distinct().take(10)
                cityInput = selectedCities.first()
                onCloseCitySearch()
            },
            onDismiss = onCloseCitySearch,
        )
    }

    if (isDatePickerForStart != null) com.bigong.oguri.feature.travelplanner.ui.component.TravelDateRangePicker(
        initial = dates, onDismiss = { isDatePickerForStart = null },
        recommendedStart = recommendedStart, recommendedEnd = recommendedEnd,
        onSelected = { range ->
            startMillis = range.start.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            endMillis = range.endInclusive.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            isDatePickerForStart = null
        },
    )
}

@Composable
internal fun TravelDateButton(
    label: String,
    date: LocalDate?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = OguriTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier.heightIn(min = 64.dp).clip(shape).background(colors.surfaceCard)
            .border(1.dp, if (isError) com.bigong.oguri.core.designsystem.Orange20 else colors.borderStrong, shape).noRippleClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = OguriTheme.typography.caption, color = colors.textTertiary)
        Text(date?.let { travelWeekDate(it) }.orEmpty(), style = OguriTheme.typography.cardSubtitle, color = colors.textPrimary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TravelDatePicker(
    initialMillis: Long?,
    onDismiss: () -> Unit,
    onSelected: (Long) -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    val colors = OguriTheme.colors
    val pickerColors = DatePickerDefaults.colors(
        containerColor = colors.backgroundPrimary,
        selectedDayContainerColor = colors.mintContainer,
        selectedDayContentColor = colors.textPrimary,
        todayContentColor = colors.textPrimary,
        todayDateBorderColor = colors.textSecondary,
    )
    DatePickerDialog(
        colors = pickerColors,
        onDismissRequest = onDismiss,
        confirmButton = {
            TravelTextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = { datePickerState.selectedDateMillis?.let(onSelected) },
            ) { Text(stringResource(Res.string.travel_apply)) }
        },
        dismissButton = {
            TravelTextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) }
        },
    ) {
        DatePicker(state = datePickerState, colors = pickerColors, modifier = Modifier.verticalScroll(rememberScrollState()))
    }
}

private fun Long.toTravelDate(): LocalDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date

private val CityCatalogSaver = Saver<Map<String, TravelCitySuggestion>, String>(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString(it) },
)
