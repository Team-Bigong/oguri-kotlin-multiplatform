package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.consumeVerticalDragForBottomSheetContent
import com.bigong.oguri.core.util.extension.noRippleClickable
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_exit
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_done
import oguri.composeapp.generated.resources.calendar_period_chip_year_all
import oguri.composeapp.generated.resources.calendar_period_chip_year_month
import oguri.composeapp.generated.resources.calendar_period_filter_all
import oguri.composeapp.generated.resources.calendar_period_filter_month_label
import oguri.composeapp.generated.resources.calendar_period_filter_sheet_title
import oguri.composeapp.generated.resources.calendar_period_filter_year_label
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val PICKER_VISIBLE_ITEM_COUNT = 3
private const val MAXIMUM_YEAR_RANGE = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPeriodBottomSheet(
    selectedYear: Int,
    selectedMonth: Int?,
    onPeriodFilterChanged: (Int, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentYear = rememberCurrentYear()
    val currentMonth = rememberCurrentMonth()
    val yearOptions = remember(currentYear) { (currentYear..(currentYear + MAXIMUM_YEAR_RANGE)).toList() }
    val selectedYearInRange = selectedYear.coerceIn(yearOptions.first(), yearOptions.last())
    var pickerYear by rememberSaveable(selectedYear) { mutableIntStateOf(selectedYearInRange) }
    val monthOptions =
        remember(pickerYear, currentYear, currentMonth) {
            buildMonthOptions(
                selectedYear = pickerYear,
                currentYear = currentYear,
                currentMonth = currentMonth,
            )
        }
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var pickerMonth by rememberSaveable(selectedYear, selectedMonth) {
        mutableStateOf(selectedMonth?.takeIf { month: Int -> month in monthOptions.filterNotNull() })
    }
    LaunchedEffect(monthOptions) {
        if (pickerMonth !in monthOptions) {
            pickerMonth = null
        }
    }

    val chipText =
        if (selectedMonth == null) {
            stringResource(Res.string.calendar_period_chip_year_all, selectedYearInRange)
        } else {
            stringResource(Res.string.calendar_period_chip_year_month, selectedYearInRange, selectedMonth)
        }

    CalendarFilterChip(
        text = chipText,
        onClick = {
            pickerYear = selectedYearInRange
            val openMonthOptions =
                buildMonthOptions(
                    selectedYear = selectedYearInRange,
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                )
            pickerMonth = selectedMonth?.takeIf { month: Int -> month in openMonthOptions.filterNotNull() }
            isBottomSheetVisible = true
        },
        modifier = modifier,
    )

    if (isBottomSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { isBottomSheetVisible = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            dragHandle = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier =
                            Modifier
                                .size(width = 68.dp, height = 4.dp)
                                .background(color = Neutral50, shape = RoundedCornerShape(size = 100.dp)),
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            },
            containerColor = Neutral0,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .consumeVerticalDragForBottomSheetContent()
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(Res.string.calendar_period_filter_sheet_title),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral90,
                    )
                    Image(
                        painter = painterResource(resource = Res.drawable.btn_exit),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Neutral50),
                        modifier = Modifier.size(36.dp).noRippleClickable(onClick = { isBottomSheetVisible = false }),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PeriodFilterPicker(
                    yearOptions = yearOptions,
                    monthOptions = monthOptions,
                    selectedYear = pickerYear,
                    selectedMonth = pickerMonth,
                    onYearChanged = { changedYear: Int ->
                        pickerYear = changedYear
                    },
                    onMonthChanged = { changedMonth: Int? ->
                        pickerMonth = changedMonth
                    },
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = Mint70, shape = RoundedCornerShape(size = 8.dp))
                            .noRippleClickable(
                                onClick = {
                                    onPeriodFilterChanged(pickerYear, pickerMonth)
                                    isBottomSheetVisible = false
                                },
                            ).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(Res.string.calendar_leave_days_sheet_done),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral0,
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodFilterPicker(
    yearOptions: List<Int>,
    monthOptions: List<Int?>,
    selectedYear: Int,
    selectedMonth: Int?,
    onYearChanged: (Int) -> Unit,
    onMonthChanged: (Int?) -> Unit,
) {
    val selectedYearIndex = yearOptions.indexOf(selectedYear).coerceAtLeast(0)
    val selectedMonthIndex = monthOptions.indexOf(selectedMonth).coerceAtLeast(0)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.calendar_period_filter_year_label),
                style = OguriTheme.typography.labelMedium,
                color = Neutral50,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(Res.string.calendar_period_filter_month_label),
                style = OguriTheme.typography.labelMedium,
                color = Neutral50,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(42.dp * PICKER_VISIBLE_ITEM_COUNT),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .background(color = Mint50.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)),
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                SingleColumnPicker(
                    items = yearOptions.map { year -> year.toString() },
                    selectedIndex = selectedYearIndex,
                    onIndexChanged = { changedIndex: Int ->
                        onYearChanged(yearOptions[changedIndex])
                    },
                    modifier = Modifier.weight(1f),
                )
                SingleColumnPicker(
                    items =
                        monthOptions.map { month: Int? ->
                            month?.toString() ?: stringResource(Res.string.calendar_period_filter_all)
                        },
                    selectedIndex = selectedMonthIndex,
                    onIndexChanged = { changedIndex: Int ->
                        onMonthChanged(monthOptions[changedIndex])
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun SingleColumnPicker(
    items: List<String>,
    selectedIndex: Int,
    onIndexChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = PICKER_VISIBLE_ITEM_COUNT,
    itemHeight: Dp = 42.dp,
) {
    val centerOffset = (visibleItemCount - 1) / 2
    val pickerState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex.coerceAtLeast(0))
    val itemHeightPx = with(LocalDensity.current) { itemHeight.roundToPx() }

    LaunchedEffect(pickerState, itemHeightPx, items.size) {
        snapshotFlow {
            pickerState.firstVisibleItemIndex to pickerState.firstVisibleItemScrollOffset
        }.map { (firstVisibleItemIndex, firstVisibleItemScrollOffset) ->
            nearestPickerIndex(
                firstVisibleItemIndex = firstVisibleItemIndex,
                firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
                itemHeightPx = itemHeightPx,
                maximumIndex = items.lastIndex,
            )
        }.distinctUntilChanged()
            .collect { nearestIndex ->
                if (nearestIndex != selectedIndex) {
                    onIndexChanged(nearestIndex)
                }
            }
    }

    LaunchedEffect(pickerState.isScrollInProgress, itemHeightPx, items.size) {
        if (!pickerState.isScrollInProgress) {
            val nearestIndex =
                nearestPickerIndex(
                    firstVisibleItemIndex = pickerState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = pickerState.firstVisibleItemScrollOffset,
                    itemHeightPx = itemHeightPx,
                    maximumIndex = items.lastIndex,
                )
            pickerState.scrollToItem(index = nearestIndex)
        }
    }

    LaunchedEffect(selectedIndex) {
        pickerState.scrollToItem(index = selectedIndex)
    }

    LazyColumn(
        state = pickerState,
        horizontalAlignment = Alignment.CenterHorizontally,
        flingBehavior = pickerSnapFlingBehavior(pickerState),
        modifier = modifier,
    ) {
        items(centerOffset) {
            Spacer(modifier = Modifier.height(itemHeight))
        }
        items(items.size) { index: Int ->
            val isSelected = index == selectedIndex
            Text(
                text = items[index],
                style = OguriTheme.typography.bodyLarge,
                color = if (isSelected) Neutral90 else Neutral40,
                modifier = Modifier.height(itemHeight).fillMaxWidth().wrapContentHeight(Alignment.CenterVertically),
                textAlign = TextAlign.Center,
            )
        }
        items(centerOffset) {
            Spacer(modifier = Modifier.height(itemHeight))
        }
    }
}

private fun nearestPickerIndex(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    itemHeightPx: Int,
    maximumIndex: Int,
): Int {
    val nextIndexOffset = if (firstVisibleItemScrollOffset >= itemHeightPx / 2) 1 else 0
    return (firstVisibleItemIndex + nextIndexOffset).coerceIn(0, maximumIndex)
}

@Composable
private fun pickerSnapFlingBehavior(listState: LazyListState): FlingBehavior =
    rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center,
    )

@Composable
private fun rememberCurrentYear(): Int {
    val today =
        kotlin.time.Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    return today.year
}

@Composable
private fun rememberCurrentMonth(): Int {
    val today =
        kotlin.time.Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    return today.month.ordinal + 1
}

private fun buildMonthOptions(
    selectedYear: Int,
    currentYear: Int,
    currentMonth: Int,
): List<Int?> {
    val visibleMonths: IntRange =
        if (selectedYear == currentYear) {
            currentMonth..12
        } else {
            1..12
        }
    return listOf(null) + visibleMonths.toList()
}
