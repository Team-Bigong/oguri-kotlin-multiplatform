package com.bigong.oguri.feature.travelplanner.ui.component

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelPlanDetails
import com.bigong.oguri.domain.model.TravelPlanFailure
import com.bigong.oguri.domain.model.TravelPlanSource
import com.bigong.oguri.domain.model.TravelRecentChanges
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.*
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_apply
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_change_day_reordered
import oguri.composeapp.generated.resources.travel_change_moved_now
import oguri.composeapp.generated.resources.travel_change_open_day
import oguri.composeapp.generated.resources.travel_change_owner
import oguri.composeapp.generated.resources.travel_change_removed_now
import oguri.composeapp.generated.resources.travel_change_trip
import oguri.composeapp.generated.resources.travel_change_visit_added
import oguri.composeapp.generated.resources.travel_change_visit_edited
import oguri.composeapp.generated.resources.travel_change_visit_removed
import oguri.composeapp.generated.resources.travel_change_visit_undone
import oguri.composeapp.generated.resources.travel_changed_place
import oguri.composeapp.generated.resources.travel_changes_trip
import oguri.composeapp.generated.resources.travel_choose_time_zone
import oguri.composeapp.generated.resources.travel_city
import oguri.composeapp.generated.resources.travel_dates_value
import oguri.composeapp.generated.resources.travel_details_keep_local_times
import oguri.composeapp.generated.resources.travel_details_move_unassigned
import oguri.composeapp.generated.resources.travel_edit_trip
import oguri.composeapp.generated.resources.travel_end_date
import oguri.composeapp.generated.resources.travel_no_recent_changes
import oguri.composeapp.generated.resources.travel_open_source
import oguri.composeapp.generated.resources.travel_recent_changes
import oguri.composeapp.generated.resources.travel_recommended_dates
import oguri.composeapp.generated.resources.travel_search_close
import oguri.composeapp.generated.resources.travel_source
import oguri.composeapp.generated.resources.travel_source_explanation
import oguri.composeapp.generated.resources.travel_start_date
import oguri.composeapp.generated.resources.travel_time_zone
import oguri.composeapp.generated.resources.travel_title
import oguri.composeapp.generated.resources.travel_trip_info
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun TravelDetailsDialog(
    plan: TravelPlan,
    busy: Boolean,
    failure: TravelPlanFailure?,
    onDismiss: () -> Unit,
    citySearch: com.bigong.oguri.feature.travelplanner.ui.model.TravelCitySearchState?,
    onOpenCitySearch: () -> Unit,
    onCloseCitySearch: () -> Unit,
    onSearchCities: (String) -> Unit,
    onChooseCity: (com.bigong.oguri.domain.model.TravelCitySuggestion) -> Unit,
    onReload: () -> Unit,
    onApply: (TravelPlanDetails, Int) -> Unit,
    canEdit: Boolean,
    startEditing: Boolean,
    onOpenSource: (TravelPlanSource) -> Unit,
) {
    val current = plan.summary.details
    var editing by remember(plan.summary.id) { mutableStateOf(startEditing) }
    val showEditor = canEdit && editing
    var title by remember(plan.revision) { mutableStateOf(current.title) }
    var cities by remember(plan.revision) { mutableStateOf(current.cities) }
    var zones by remember(plan.revision) { mutableStateOf(mapOf(current.city to current.timeZone.id)) }
    val city = cities.firstOrNull().orEmpty()
    LaunchedEffect(citySearch?.selected, citySearch?.loading) {
        if (citySearch?.selected != null && !citySearch.loading) {
            val label = citySearch.selected.label.take(TravelPlanDetails.MAX_TEXT_LENGTH)
            cities = (cities + label).distinct().take(10)
            citySearch.timeZone?.takeIf { it in TimeZone.availableZoneIds }?.let { zones = zones + (label to it) }
            onCloseCitySearch()
        }
    }
    var start by remember(plan.revision) { mutableStateOf(current.dates.start.toString()) }
    var end by remember(plan.revision) { mutableStateOf(current.dates.endInclusive.toString()) }
    var chooseDates by remember { mutableStateOf(false) }
    val dates = runCatching { TravelDates(LocalDate.parse(start), LocalDate.parse(end)) }.getOrNull()
    val timeZone = (zones[city] ?: com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone(city))?.let(TimeZone::of)
    val affected = if (dates == null) 0 else plan.visits.count { it.dayNumber > dates.dayCount }
    val valid =
        title.isNotBlank() &&
            city.isNotBlank() &&
            title.length <= TravelPlanDetails.MAX_TEXT_LENGTH &&
            city.length <= TravelPlanDetails.MAX_TEXT_LENGTH &&
            dates != null &&
            timeZone != null
    TravelFullScreenForm(
        shape = RoundedCornerShape(8.dp),
        containerColor = Neutral0,
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(if (showEditor) Res.string.travel_edit_trip else Res.string.travel_trip_info)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (showEditor) {
                    OutlinedTextField(
                        title,
                        { title = it },
                        label = { Text(stringResource(Res.string.travel_title)) },
                        enabled = !busy,
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        textStyle = OguriTheme.typography.bodyLarge,
                        colors = TravelFieldColors(),
                    )
                    cities.forEach { selected ->
                        TravelOutlineButton(enabled = !busy && cities.size > 1, onClick = { cities = cities - selected }, modifier = Modifier.fillMaxWidth()) {
                            Text(selected + if (cities.size > 1) " ×" else "")
                        }
                    }
                    TravelOutlineButton(onClick = onOpenCitySearch, enabled = !busy && cities.size < 10, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(Res.string.travel_add_city))
                    }
                    TravelOutlineButton(onClick = { chooseDates = true }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
                        dates?.let { Text(com.bigong.oguri.feature.travelplanner.ui.travelDateRange(it)) }
                    }
                    if (affected > 0) Text(stringResource(Res.string.travel_details_move_unassigned, affected))
                } else {
                    Text(current.title, style = OguriTheme.typography.cardTitle)
                    Text(current.cities.joinToString(" · "))
                    Text(
                        stringResource(
                            Res.string.travel_dates_value,
                            com.bigong.oguri.feature.travelplanner.ui.travelWeekDate(current.dates.start),
                            com.bigong.oguri.feature.travelplanner.ui.travelWeekDate(current.dates.endInclusive),
                        ),
                    )
                    current.source?.let { source ->
                        Text(stringResource(Res.string.travel_source), style = OguriTheme.typography.cardSubtitle)
                        if (source.startDate != null && source.endDate != null) {
                            Text(
                                stringResource(Res.string.travel_recommended_dates, com.bigong.oguri.feature.travelplanner.ui.travelWeekDate(source.startDate), com.bigong.oguri.feature.travelplanner.ui.travelWeekDate(source.endDate)),
                            )
                        }
                        Text(stringResource(Res.string.travel_source_explanation))
                        TravelTextButton(enabled = !busy, onClick = { onOpenSource(source) }) {
                            Text(stringResource(Res.string.travel_open_source))
                        }
                    }
                }
                if (failure != null) TravelInlineFailure(failure, onRetry = onReload.takeIf { failure == TravelPlanFailure.CONFLICT })
            }
        },
        confirmButton = {
            if (showEditor) {
                TravelPrimaryButton(enabled = !busy && valid && failure != TravelPlanFailure.CONFLICT, onClick = {
                    if (dates != null && timeZone != null) {
                        onApply(TravelPlanDetails(city.trim(), timeZone, title.trim(), dates, current.source, cities), affected)
                    }
                }) { Text(stringResource(Res.string.travel_apply)) }
            } else if (canEdit) {
                TravelPrimaryButton(enabled = !busy, onClick = { editing = true }) { Text(stringResource(Res.string.travel_edit_trip)) }
            }
        },
        dismissButton = {
            TravelTextButton(enabled = !busy, onClick = { if (showEditor) editing = false else onDismiss() }) {
                Text(stringResource(if (showEditor) Res.string.travel_cancel else Res.string.travel_search_close))
            }
        },
    )
    if (citySearch != null) TravelCitySearchDialog(
        state = citySearch, recommendedCities = current.cities,
        onQuery = onSearchCities, onChoose = onChooseCity,
        onChooseRecommendedCity = { cities = (cities + it).distinct().take(10); onCloseCitySearch() },
        onDismiss = onCloseCitySearch,
    )
    if (chooseDates) TravelDateRangePicker(dates, { chooseDates = false }) { range ->
        start = range.start.toString(); end = range.endInclusive.toString(); chooseDates = false
    }

}

