package com.bigong.oguri.feature.travelplanner.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.TravelDates
import com.bigong.oguri.domain.model.TravelPlan
import com.bigong.oguri.feature.travelplanner.ui.travelDayLabel
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.travel_all_days_tab
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TravelDaySelector(
    dates: TravelDates,
    selectedDay: Int,
    enabled: Boolean,
    onSelectDay: (Int) -> Unit,
    onOverview: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(selectedDay, dates) {
        val index = if (selectedDay == TravelPlan.UNASSIGNED_DAY) dates.dayCount else selectedDay - 1
        if (listState.layoutInfo.visibleItemsInfo.none { it.index == index }) listState.scrollToItem(index)
    }
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(49.dp),
        state = listState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 0.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(dates.dayCount) { index ->
            val day = index + 1
            TravelChoiceChip(travelDayLabel(dates, day), selectedDay == day, enabled, { onSelectDay(day) })
        }
        item {
            TravelTextButton(onClick = onOverview, enabled = enabled) {
                Text(stringResource(Res.string.travel_all_days_tab))
            }
        }
    }
}
