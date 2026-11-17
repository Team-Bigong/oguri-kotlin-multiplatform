package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.painterResource
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.designsystem.*
import com.bigong.oguri.feature.travelplanner.ui.TravelDateButton
import com.bigong.oguri.feature.travelplanner.ui.travelMonthDay
import com.bigong.oguri.feature.travelplanner.ui.travelCardDates
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.domain.model.TravelCitySuggestion
import com.bigong.oguri.feature.travelplanner.ui.model.TravelCity
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
    onChooseCity: (TravelCitySuggestion) -> Unit,
    onResolveCityTimeZone: suspend (String) -> String?,
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
    var catalogCities by remember(plan.revision) { mutableStateOf(current.catalogCities) }
    var zones by remember(plan.revision) {
        mutableStateOf(current.catalogCities.mapNotNull { (label, city) -> city.timeZone?.let { label to it } }.toMap() + (current.city to current.timeZone.id))
    }
    val city = cities.firstOrNull().orEmpty()
    var resolvingCity by remember { mutableStateOf(false) }
    var cityLookupFailed by remember { mutableStateOf(false) }
    var cityLookupAttempt by remember { mutableIntStateOf(0) }
    LaunchedEffect(city, showEditor, cityLookupAttempt, zones[city]) {
        cityLookupFailed = false
        resolvingCity = false
        if (showEditor && zones[city] == null && com.bigong.oguri.feature.travelplanner.ui.model.suggestedTimeZone(city) == null) {
            resolvingCity = true
            val zone = onResolveCityTimeZone(city)
            if (zone != null) zones = zones + (city to zone)
            cityLookupFailed = zone == null
            resolvingCity = false
        }
    }
    LaunchedEffect(citySearch?.selected, citySearch?.loading) {
        if (citySearch?.selected != null && !citySearch.loading) {
            val suggestion = citySearch.selected
            val label = catalogCities.values.firstOrNull { it.id == suggestion.id }?.label ?: suggestion.label
            cities = (cities + label).distinct().take(10)
            if (label in cities) catalogCities = catalogCities + (label to suggestion.copy(label = label))
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
        containerColor = OguriTheme.colors.surfaceCard,
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text(stringResource(if (showEditor) Res.string.travel_edit_trip else Res.string.travel_trip_info)) },
        showBottomActions = showEditor,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = if (showEditor) 20.dp else 12.dp),
        actions = {
            if (!showEditor && canEdit) TravelTextButton(enabled = !busy, onClick = { editing = true }) { Text(stringResource(Res.string.travel_edit_action)) }
            else Spacer(Modifier.size(48.dp))
        },
        text = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                if (showEditor) {
                    TravelPlaceField(title, { title = it }, Res.string.travel_title, Res.string.travel_title, TravelPlanDetails.MAX_TEXT_LENGTH, !busy)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.travel_city), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).background(OguriTheme.colors.surfaceCard, RoundedCornerShape(8.dp))
                            .border(1.dp, if (cityLookupFailed) Orange20 else OguriTheme.colors.borderStrong, RoundedCornerShape(8.dp))
                            .noRippleClickable(enabled = !busy && cities.size < 10, onClick = onOpenCitySearch).padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(painterResource(Res.drawable.ic_travel_pin), null, Modifier.size(20.dp), tint = OguriTheme.colors.textSecondary)
                            Text(cities.joinToString(" · ") { catalogCities[it]?.contextualName ?: it }, Modifier.weight(1f), style = travelTextStyle(15, 18))
                            Text(stringResource(Res.string.travel_search_cities), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        }
                        if (cities.size > 1) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                cities.forEach { selected ->
                                    TravelTextButton(enabled = !busy, onClick = { cities = cities - selected }) {
                                        Text("${catalogCities[selected]?.contextualName ?: selected} ×")
                                    }
                                }
                            }
                            Text(stringResource(Res.string.travel_primary_city), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        }
                    }
                    if (resolvingCity) TravelLoading(Modifier.fillMaxWidth())
                    if (cityLookupFailed) {
                        Text(stringResource(Res.string.travel_city_lookup_failed), style = OguriTheme.typography.bodySmall, color = OguriTheme.colors.error)
                        TravelTextButton(onClick = { cityLookupAttempt++ }, enabled = !busy) { Text(stringResource(Res.string.travel_retry)) }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(Res.string.travel_actual_dates), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TravelDateButton(stringResource(Res.string.travel_start_date), dates?.start, !busy, Modifier.weight(1f)) { chooseDates = true }
                            TravelDateButton(stringResource(Res.string.travel_end_date), dates?.endInclusive, !busy, Modifier.weight(1f)) { chooseDates = true }
                        }
                    }
                    if (dates != null && dates.dayCount < current.dates.dayCount) {
                        val shape = RoundedCornerShape(8.dp)
                        Column(Modifier.fillMaxWidth().background(Orange5, shape).border(1.dp, Orange20, shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(Res.string.travel_shortened_title, current.dates.dayCount, dates.dayCount), style = OguriTheme.typography.cardSubtitle)
                            if (affected > 0) {
                                val affectedDays = plan.visits.filter { it.dayNumber > dates.dayCount }.map { it.dayNumber }.distinct().sorted().map { day -> stringResource(Res.string.travel_day_number, day) }
                                val days = if (affectedDays.size == 2) stringResource(Res.string.travel_two_days, affectedDays[0], affectedDays[1]) else affectedDays.joinToString(", ")
                                Text(stringResource(Res.string.travel_shortened_visits, days, affected, dates.dayCount), style = OguriTheme.typography.caption, color = OguriTheme.colors.textSecondary)
                            }
                        }
                    }
                } else {
                    val shape = RoundedCornerShape(8.dp)
                    Column(Modifier.fillMaxWidth().clip(shape).background(OguriTheme.colors.surfaceCard).border(1.dp, OguriTheme.colors.borderStrong, shape)) {
                        Text(current.title, Modifier.fillMaxWidth().background(OguriTheme.colors.mint).padding(horizontal = 18.dp, vertical = 12.dp), style = OguriTheme.typography.cardTitle, color = travelAccentForeground)
                        Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(Res.string.travel_info_dates, travelCardDates(current.dates, includeYear = true), current.dates.dayCount), style = OguriTheme.typography.cardSubtitle)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(Res.drawable.ic_travel_pin), null, Modifier.size(14.dp), tint = OguriTheme.colors.textTertiary)
                                Text(current.cities.joinToString(" · ") { current.catalogCities[it]?.contextualName ?: it }, style = OguriTheme.typography.labelSmall, color = OguriTheme.colors.textTertiary)
                            }
                        }
                    }
                    current.source?.let { source ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(Res.drawable.ic_calendar), null, Modifier.size(24.dp), tint = OguriTheme.colors.textSecondary)
                                Text(stringResource(Res.string.travel_source), style = OguriTheme.typography.cardSubtitle)
                            }
                            Column(Modifier.fillMaxWidth().background(OguriTheme.colors.mintContainer, shape).border(1.dp, OguriTheme.colors.borderSubtle, shape).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (source.startDate != null && source.endDate != null) {
                                    Text(
                                        text = stringResource(
                                            Res.string.travel_card_date_range,
                                            travelMonthDay(source.startDate),
                                            travelMonthDay(source.endDate),
                                        ),
                                        style = OguriTheme.typography.cardSubtitle,
                                    )
                                }
                                Text(stringResource(Res.string.travel_source_explanation), style = OguriTheme.typography.caption, color = OguriTheme.colors.textTertiary)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    Row(
                                        Modifier.heightIn(min = 32.dp).semantics { role = Role.Button }.noRippleClickable(enabled = !busy, onClick = { onOpenSource(source) }),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(stringResource(Res.string.travel_open_source), style = OguriTheme.typography.labelLarge, color = if (busy) OguriTheme.colors.textTertiary else OguriTheme.colors.textPrimary)
                                        Icon(painterResource(Res.drawable.ic_travel_row_arrow), null, Modifier.size(16.dp), tint = OguriTheme.colors.textTertiary)
                                    }
                                }
                            }
                        }
                    }
                }
                if (failure != null) TravelInlineFailure(failure, onRetry = onReload.takeIf { failure == TravelPlanFailure.CONFLICT })
            }
        },
        confirmButton = {
            if (showEditor) {
                TravelPrimaryButton(modifier = Modifier.fillMaxWidth(), enabled = !busy && valid && failure != TravelPlanFailure.CONFLICT, onClick = {
                    if (dates != null && timeZone != null) {
                        onApply(
                            TravelPlanDetails(city.trim(), timeZone, title.trim(), dates, current.source, cities, catalogCities.filterKeys { it in cities }),
                            affected,
                        )
                    }
                }) { Text(stringResource(Res.string.travel_apply)) }
            } else if (canEdit) {
                TravelPrimaryButton(enabled = !busy, onClick = { editing = true }) { Text(stringResource(Res.string.travel_edit_trip)) }
            }
        },

    )
    if (citySearch != null) TravelCitySearchDialog(
        state = citySearch, recommendedCities = current.cities, selectedCities = cities,
        nearbyCities = TravelCity.nearby(city, cities, catalogCities).map { cityLabel(it) },
        onQuery = onSearchCities, onChoose = onChooseCity,
        onChooseRecommendedCity = { cities = (cities + it).distinct().take(10); onCloseCitySearch() },
        onDismiss = onCloseCitySearch,
    )
    if (chooseDates) TravelDateRangePicker(dates, { chooseDates = false }, onSelected = { range ->
        start = range.start.toString(); end = range.endInclusive.toString(); chooseDates = false
    }, recommendedStart = current.source?.startDate, recommendedEnd = current.source?.endDate)

}

