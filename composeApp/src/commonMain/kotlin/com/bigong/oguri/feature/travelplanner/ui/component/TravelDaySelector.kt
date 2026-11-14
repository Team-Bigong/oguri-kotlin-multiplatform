package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
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
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_all_days_tab
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
            TravelOutlineButton(
                onClick = { showPicker = true },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            ) {
                Text(travelDayLabel(dates, selectedDay))
            }
        } else {
            val listState = rememberLazyListState()
            LaunchedEffect(selectedDay, dates) {
                val index = if (selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount else selectedDay - 1
                if (listState.layoutInfo.visibleItemsInfo.none { it.index == index }) listState.scrollToItem(index)
            }
            Row(Modifier.fillMaxWidth().padding(start = 12.dp)) {
                TravelTextButton(onClick = onOverview, enabled = enabled) {
                    Text(stringResource(Res.string.travel_all_days_tab))
                }
                LazyRow(
                    modifier = Modifier.weight(1f),
                state = listState,
                contentPadding = PaddingValues(start = 4.dp, end = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(dates.dayCount) { index ->
                    val day = index + 1
                    TravelDayChip(
                        label = travelDayLabel(dates, day),
                        selected = selectedDay == day,
                        enabled = enabled,
                        onClick = { onSelectDay(day) },
                    )
                }
                item {
                    TravelDayChip(
                        label = travelDayLabel(dates, TravelPlan.UNASSIGNED_DAY),
                        selected = selectedDay == TravelPlan.UNASSIGNED_DAY,
                        enabled = enabled,
                        onClick = { onSelectDay(TravelPlan.UNASSIGNED_DAY) },
                    )
                }
                }
            }
        }
    }
    if (showPicker) {
        val listState = rememberLazyListState()
        LaunchedEffect(selectedDay, dates) {
            listState.scrollToItem(if (selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount else selectedDay - 1)
        }
        AlertDialog(
            shape = RoundedCornerShape(8.dp),
            containerColor = Neutral0,
            onDismissRequest = { showPicker = false },
            title = { Text(stringResource(Res.string.travel_select_day)) },
            text = {
                Column {
                    TravelTextButton(enabled = enabled, onClick = {
                        showPicker = false
                        onOverview()
                    }) {
                        Text(stringResource(Res.string.travel_overview), color = OguriTheme.colors.textPrimary)
                    }
                    LazyColumn(state = listState, modifier = Modifier.heightIn(max = 360.dp)) {
                        items(dates.dayCount + 1) { index ->
                            val day = if (index == dates.dayCount) TravelPlan.UNASSIGNED_DAY else index + 1
                            val isSelected = selectedDay == day
                            TravelTextButton(
                                enabled = enabled,
                                onClick = {
                                    showPicker = false
                                    onSelectDay(day)
                                },
                                modifier = Modifier.fillMaxWidth().semantics { selected = isSelected },
                            ) {
                                Text(
                                    travelDayLabel(dates, day),
                                    modifier = Modifier.fillMaxWidth(),
                                    style = if (isSelected) OguriTheme.typography.labelLarge else OguriTheme.typography.bodyMedium,
                                    color = OguriTheme.colors.textPrimary,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = { TravelTextButton(onClick = { showPicker = false }) { Text(stringResource(Res.string.travel_cancel)) } },
        )
    }
}

@Composable
private fun TravelDayChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TravelTextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .background(if (selected) Mint40 else Neutral0, RoundedCornerShape(24.dp))
            .semantics { this.selected = selected },
    ) {
        Text(label, style = if (selected) OguriTheme.typography.labelLarge else OguriTheme.typography.bodyMedium)
    }
}
