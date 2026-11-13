package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_cancel
import oguri.composeapp.generated.resources.travel_overview
import oguri.composeapp.generated.resources.travel_select_day
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelDaySelector(
    dates: TravelDates,
    selectedDay: Int,
    enabled: Boolean,
    onSelectDay: (Int) -> Unit,
    onOverview: () -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth < 360.dp || fontScale > 1.3f) {
            OutlinedButton(
                onClick = { showPicker = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Text(travelDayLabel(dates, selectedDay))
            }
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(selectedDay, dates) {
                val index = if (selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount + 1 else selectedDay
                if (listState.layoutInfo.visibleItemsInfo.none { it.index == index }) listState.scrollToItem(index)
            }
            LazyRow(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    TextButton(onClick = onOverview, enabled = enabled) { Text(stringResource(Res.string.travel_overview)) }
                }
                items(dates.dayCount) { index ->
                    val day = index + 1
                    FilterChip(
                        selected = selectedDay == day,
                        onClick = { onSelectDay(day) },
                        enabled = enabled,
                        label = { Text(travelDayLabel(dates, day)) },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedDay == TravelPlan.UNASSIGNED_DAY,
                        onClick = { onSelectDay(TravelPlan.UNASSIGNED_DAY) },
                        enabled = enabled,
                        label = { Text(travelDayLabel(dates, TravelPlan.UNASSIGNED_DAY)) },
                    )
                }
            }
        }
    }
    if (showPicker) {
        val listState = rememberLazyListState()
        LaunchedEffect(selectedDay, dates) {
            listState.scrollToItem(if (selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount + 1 else selectedDay)
        }
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(Res.string.travel_select_day)) },
            text = {
                LazyColumn(state = listState, modifier = Modifier.heightIn(max = 360.dp)) {
                    item {
                        TextButton(enabled = enabled, onClick = {
                            showPicker = false
                            onOverview()
                        }) {
                            Text(stringResource(Res.string.travel_overview))
                        }
                    }
                    items(dates.dayCount + 1) { index ->
                        val day = if (index == dates.dayCount) TravelPlan.UNASSIGNED_DAY else index + 1
                        TextButton(
                            enabled = enabled,
                            onClick = {
                                showPicker = false
                                onSelectDay(day)
                            },
                            modifier = Modifier.fillMaxWidth().semantics { selected = selectedDay == day },
                        ) { Text(travelDayLabel(dates, day), Modifier.fillMaxWidth()) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.travel_cancel)) } },
        )
    }
}
