package com.bigong.oguri.feature.travelplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.feature.travelplanner.ui.component.TravelCitySearchDialog
import com.bigong.oguri.feature.travelplanner.ui.component.TravelInlineFailure
import com.bigong.oguri.feature.travelplanner.ui.component.TravelPlannerPage
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
import com.bigong.oguri.feature.travelplanner.ui.model.TravelPlannerUiState
import com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.common_retry_button
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_choose_city
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_city_jeju
import oguri.composeapp.generated.resources.travel_city_osaka
import oguri.composeapp.generated.resources.travel_city_paris
import oguri.composeapp.generated.resources.travel_city_seoul
import oguri.composeapp.generated.resources.travel_city_taipei
import oguri.composeapp.generated.resources.travel_city_tokyo
import oguri.composeapp.generated.resources.travel_city_unavailable
import oguri.composeapp.generated.resources.travel_create
import oguri.composeapp.generated.resources.travel_date_value
import oguri.composeapp.generated.resources.travel_default_title
import oguri.composeapp.generated.resources.travel_end_date
import oguri.composeapp.generated.resources.travel_invalid_dates
import oguri.composeapp.generated.resources.travel_matching_plan
import oguri.composeapp.generated.resources.travel_open_plan
import oguri.composeapp.generated.resources.travel_recommended_dates
import oguri.composeapp.generated.resources.travel_search_cities
import oguri.composeapp.generated.resources.travel_sign_in
import oguri.composeapp.generated.resources.travel_start_date
import oguri.composeapp.generated.resources.travel_time_zone
import oguri.composeapp.generated.resources.travel_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

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
    var timeZoneOverride by rememberSaveable { mutableStateOf<String?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var startMillis by rememberSaveable { mutableStateOf(source?.startDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var endMillis by rememberSaveable { mutableStateOf(source?.endDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()) }
    var isCityMenuOpen by remember { mutableStateOf(false) }
    var isTimeZoneMenuOpen by remember { mutableStateOf(false) }
    var isDatePickerForStart by rememberSaveable { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(state.recoveredCreation) {
        state.recoveredCreation?.let { details ->
            cityEdited = true
            cityInput = details.city
            timeZoneOverride = details.timeZone.id
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
            // The selected result resolves the time zone. The saved city name is entered by the user.
            cityInput = citySearch.query.trim().take(TravelPlanDetails.MAX_TEXT_LENGTH)
            timeZoneOverride = citySearch.timeZone?.takeIf { it in TimeZone.availableZoneIds }
            onCloseCitySearch()
        }
    }
    val cityName = cityInput.trim()
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
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (!state.creationReady && !state.creationRecoveryFailed) LinearProgressIndicator(Modifier.fillMaxWidth())
            Column {
                OutlinedTextField(
                    value = cityInput,
                    onValueChange = {
                        if (it.length <= TravelPlanDetails.MAX_TEXT_LENGTH) {
                            cityEdited = true
                            cityInput = it
                            timeZoneOverride = null
                        }
                    },
                    label = { Text(stringResource(Res.string.travel_city)) },
                    enabled = canEdit,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = onOpenCitySearch, enabled = canEdit) {
                    Text(stringResource(Res.string.travel_search_cities))
                }
                TextButton(onClick = { isCityMenuOpen = true }, enabled = canEdit) {
                    Text(stringResource(Res.string.travel_choose_city))
                }
                DropdownMenu(expanded = isCityMenuOpen, onDismissRequest = { isCityMenuOpen = false }) {
                    TravelCity.entries.forEach { city ->
                        val label = cityLabel(city)
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            cityEdited = true
                            cityInput = label
                            timeZoneOverride = null
                            isCityMenuOpen = false
                        })
                    }
                }
            }
            if (state.creationCityFailed) {
                Text(stringResource(Res.string.travel_city_unavailable))
                TextButton(onClick = onRetryDestination) { Text(stringResource(Res.string.common_retry_button)) }
            }
            val creationSource = state.recoveredCreation?.source ?: source
            val recommendedStart = creationSource?.startDate
            val recommendedEnd = creationSource?.endDate
            if (recommendedStart != null && recommendedEnd != null) {
                Text(
                    stringResource(Res.string.travel_recommended_dates, recommendedStart.toString(), recommendedEnd.toString()),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TravelDateButton(startLabel, startDate, canEdit) { isDatePickerForStart = true }
            TravelDateButton(endLabel, endDate, canEdit) { isDatePickerForStart = false }
            if (hasInvalidDates) {
                Text(
                    stringResource(Res.string.travel_invalid_dates, TravelDates.MAX_DAYS),
                    color = MaterialTheme.colorScheme.error,
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
            )
            OutlinedButton(onClick = { isTimeZoneMenuOpen = true }, enabled = canEdit, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (timeZoneId == null) {
                        stringResource(Res.string.travel_choose_time_zone)
                    } else {
                        stringResource(Res.string.travel_time_zone, timeZoneId)
                    },
                )
            }
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
                TextButton(onClick = { onOpenExisting(matchingPlan.id) }, enabled = canEdit) {
                    Text(stringResource(Res.string.travel_open_plan))
                }
            }
            Button(
                enabled = canEdit && dates != null && cityName.isNotEmpty() && cityTimeZone != null,
                onClick = {
                    if (dates != null && cityTimeZone != null) {
                        onCreate(TravelPlanDetails(cityName, cityTimeZone, title.trim().ifEmpty { defaultTitle }, dates, creationSource))
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
        TravelCitySearchDialog(citySearch, onSearchCities, onChooseCity, onCloseCitySearch)
    }

    if (isTimeZoneMenuOpen) {
        TravelTimeZonePicker(
            onDismiss = { isTimeZoneMenuOpen = false },
            onSelect = {
                timeZoneOverride = it
                isTimeZoneMenuOpen = false
            },
        )
    }

    isDatePickerForStart?.let { selectingStart ->
        TravelDatePicker(
            initialMillis = if (selectingStart) startMillis else endMillis ?: startMillis,
            onDismiss = { isDatePickerForStart = null },
            onSelected = {
                if (selectingStart) {
                    startMillis = it
                    if (endMillis == null) endMillis = it
                } else {
                    endMillis = it
                }
                isDatePickerForStart = null
            },
        )
    }
}

@Composable
private fun TravelTimeZonePicker(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val zones = remember { TimeZone.availableZoneIds.filter { '/' in it && !it.startsWith("Etc/") }.sorted() }
    val results = remember(zones, query) { zones.filter { it.replace('_', ' ').contains(query.trim(), ignoreCase = true) } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.travel_choose_time_zone)) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(Res.string.travel_choose_time_zone)) },
                    singleLine = true,
                )
                LazyColumn(Modifier.heightIn(max = 230.dp)) {
                    items(results, key = { it }) { zone ->
                        TextButton(onClick = { onSelect(zone) }) { Text(zone.replace('_', ' ')) }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) } },
    )
}

@Composable
private fun TravelDateButton(
    label: String,
    date: LocalDate?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Text(if (date == null) label else stringResource(Res.string.travel_date_value, label, date.toString()))
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
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = datePickerState.selectedDateMillis != null,
                onClick = { datePickerState.selectedDateMillis?.let(onSelected) },
            ) { Text(stringResource(Res.string.travel_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.travel_cancel)) }
        },
    ) {
        DatePicker(state = datePickerState, modifier = Modifier.verticalScroll(rememberScrollState()))
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
        },
    )
