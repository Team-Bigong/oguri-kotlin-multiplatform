package com.bigong.oguri.feature.travelplanner.ui

import org.jetbrains.compose.resources.painterResource
import oguri.composeapp.generated.resources.ic_pen
import oguri.composeapp.generated.resources.ic_calendar
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.feature.travelplanner.ui.component.TravelCitySearchDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelFieldColors
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelLoading
import com.bigong.oguri.feature.travelplanner.ui.component.TravelOutlineButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPrimaryButton
import com.bigong.oguri.feature.travelplanner.ui.component.TravelTextButton
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone
import oguri.composeapp.generated.resources.travel_add_city
import oguri.composeapp.generated.resources.travel_primary_city
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_city_barcelona
import oguri.composeapp.generated.resources.travel_city_ho_chi_minh
import oguri.composeapp.generated.resources.travel_city_jeju
import oguri.composeapp.generated.resources.travel_city_osaka
import oguri.composeapp.generated.resources.travel_city_paris
import oguri.composeapp.generated.resources.travel_city_seoul
import oguri.composeapp.generated.resources.travel_city_sydney
import oguri.composeapp.generated.resources.travel_city_taipei
import oguri.composeapp.generated.resources.travel_city_tokyo
import oguri.composeapp.generated.resources.travel_city_unavailable
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
    var cityTimeZones by rememberSaveable { mutableStateOf(mapOf<String, String>()) }
    var timeZoneOverride by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var startMillis by rememberSaveable { mutableStateOf(source?.startDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var endMillis by rememberSaveable { mutableStateOf(source?.endDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var showOptions by rememberSaveable { mutableStateOf(false) }
    var isTimeZoneMenuOpen by remember { mutableStateOf(false) }
    var isDatePickerForStart by rememberSaveable { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(state.recoveredCreation) {
        state.recoveredCreation?.let { details ->
            cityEdited = true
            cityInput = details.city
            selectedCities = details.cities
            timeZoneOverride = details.timeZone.id
            cityTimeZones = mapOf(details.city to details.timeZone.id)
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
            val chosen = citySearch.selected.label.take(TravelPlanDetails.MAX_TEXT_LENGTH)
            val primary = selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) }
            selectedCities = (primary + chosen).distinct().take(10)
            cityInput = selectedCities.first()
            citySearch.timeZone?.takeIf { it in TimeZone.availableZoneIds }?.let { cityTimeZones = cityTimeZones + (chosen to it) }
            timeZoneOverride = cityTimeZones[cityInput] ?: suggestedTimeZone(cityInput)
            onCloseCitySearch()
        }
    }
    val cityName = selectedCities.firstOrNull() ?: cityInput.trim()
    val timeZoneId = timeZoneOverride ?: suggestedTimeZone(cityName)
    val cityTimeZone = timeZoneId?.let(TimeZone::of)
    val defaultTitle = stringResource(Res.string.travel_default_title, cityName)
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
            it.details.city.equals(cityName, ignoreCase = true) && it.details.dates == dates
        }
    val startLabel = stringResource(Res.string.travel_start_date)
    val endLabel = stringResource(Res.string.travel_end_date)

    TravelPlannerPage(
        title = stringResource(Res.string.travel_create),
        onBack = onBack,
        isSaving = state.isSaving,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.creationReady && !state.creationRecoveryFailed) TravelLoading(Modifier.fillMaxWidth())
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.travel_city), style = OguriTheme.typography.labelMedium)
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cities = selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) }
                    cities.forEach { city ->
                        TravelTextButton(enabled = canEdit, onClick = {
                            selectedCities = cities - city
                            cityInput = selectedCities.firstOrNull().orEmpty()
                            timeZoneOverride = cityTimeZones[cityInput] ?: suggestedTimeZone(cityInput)
                        }) { Text("$city ×") }
                    }
                }
                TravelOutlineButton(onClick = onOpenCitySearch, enabled = canEdit && selectedCities.size < 10, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.travel_add_city))
                }
                if (selectedCities.size > 1) Text(stringResource(Res.string.travel_primary_city), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
            }
            if (state.creationCityFailed) {
                Text(stringResource(Res.string.travel_city_unavailable))
                TravelTextButton(onClick = onRetryDestination) { Text(stringResource(Res.string.common_retry_button)) }
            }
            val creationSource = state.recoveredCreation?.source ?: source
            val recommendedStart = creationSource?.startDate
            val recommendedEnd = creationSource?.endDate
            if (recommendedStart != null && recommendedEnd != null) {
                Text(
                    stringResource(Res.string.travel_recommended_dates, travelWeekDate(recommendedStart), travelWeekDate(recommendedEnd)),
                    style = OguriTheme.typography.bodySmall,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TravelDateButton(startLabel, startDate, canEdit) { isDatePickerForStart = true }
                TravelDateButton(endLabel, endDate, canEdit) { isDatePickerForStart = false }
            }
            if (hasInvalidDates) {
                Text(
                    stringResource(Res.string.travel_invalid_dates, TravelDates.MAX_DAYS),
                    color = com.bigong.oguri.core.designsystem.Orange50,
                )
            }
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= TravelPlanDetails.MAX_TEXT_LENGTH) title = it },
                    label = { Text(stringResource(Res.string.travel_title)) },
                    placeholder = { Text(defaultTitle) },
                    singleLine = true,
                    enabled = canEdit,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = OguriTheme.typography.bodyLarge,
                    colors = TravelFieldColors(),
                )
            state.failure?.takeUnless { it == TravelPlanFailure.SIGN_IN_REQUIRED }?.let {
                TravelInlineFailure(
                    it,
                    onRetry =
                        when {
                            state.creationRecoveryFailed -> onRetryRecovery
                            state.canRetrySave -> onRetrySave
                            else -> null
                        },
                )
            }
            if (matchingPlan != null) {
                Text(stringResource(Res.string.travel_matching_plan, matchingPlan.details.title))
                TravelTextButton(onClick = { onOpenExisting(matchingPlan.id) }, enabled = canEdit) {
                    Text(stringResource(Res.string.travel_open_plan))
                }
            }
            TravelPrimaryButton(
                enabled = canEdit && dates != null && cityName.isNotEmpty() && cityTimeZone != null,
                onClick = {
                    if (dates != null && cityTimeZone != null) {
                        onCreate(TravelPlanDetails(cityName, cityTimeZone, title.trim().ifEmpty { defaultTitle }, dates, creationSource, selectedCities.ifEmpty { listOf(cityName) }))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (signedIn) Res.string.travel_create else Res.string.travel_sign_in))
            }
            if (state.failure == TravelPlanFailure.SIGN_IN_REQUIRED && signedIn) {
                TravelInlineFailure(
                    TravelPlanFailure.SIGN_IN_REQUIRED,
                    onRetry = onSignIn,
                    retryLabel = stringResource(Res.string.travel_sign_in),
                )
            }
        }
    }

    if (citySearch != null) {
        TravelCitySearchDialog(
            state = citySearch,
            recommendedCities = TravelCity.entries.map { cityLabel(it) },
            onQuery = onSearchCities,
            onChoose = onChooseCity,
            onChooseRecommendedCity = { city ->
                cityEdited = true
                selectedCities = (selectedCities.ifEmpty { listOfNotNull(cityInput.takeIf { it.isNotBlank() }) } + city).distinct().take(10)
                cityInput = selectedCities.first()
                timeZoneOverride = null
                onCloseCitySearch()
            },
            onDismiss = onCloseCitySearch,
        )
    }

    if (isDatePickerForStart != null) com.bigong.oguri.feature.travelplanner.ui.component.TravelDateRangePicker(
        initial = dates, onDismiss = { isDatePickerForStart = null },
        onSelected = { range ->
            startMillis = range.start.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            endMillis = range.endInclusive.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            isDatePickerForStart = null
        },
    )
}

@Composable
private fun TravelDateButton(
    label: String,
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TravelOutlineButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(Res.drawable.ic_calendar), contentDescription = null, modifier = Modifier.size(20.dp))
            Text(label, Modifier.weight(1f), style = OguriTheme.typography.labelLarge)
            if (date != null) Text(travelWeekDate(date))
            Icon(painterResource(Res.drawable.ic_pen), contentDescription = null, modifier = Modifier.size(18.dp))
        }
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

@Composable
private fun cityLabel(city: TravelCity): String =
    stringResource(
        when (city) {
            TravelCity.SEOUL -> Res.string.travel_city_seoul
            TravelCity.JEJU -> Res.string.travel_city_jeju
            TravelCity.OSAKA -> Res.string.travel_city_osaka
            TravelCity.TOKYO -> Res.string.travel_city_tokyo
            TravelCity.TAIPEI -> Res.string.travel_city_taipei
            TravelCity.PARIS -> Res.string.travel_city_paris
            TravelCity.HO_CHI_MINH -> Res.string.travel_city_ho_chi_minh
            TravelCity.SYDNEY -> Res.string.travel_city_sydney
            TravelCity.BARCELONA -> Res.string.travel_city_barcelona
        },
    )
