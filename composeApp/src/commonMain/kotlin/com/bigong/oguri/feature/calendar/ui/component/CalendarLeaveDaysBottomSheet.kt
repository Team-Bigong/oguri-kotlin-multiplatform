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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.btn_exit
import oguri.composeapp.generated.resources.calendar_leave_days_chip_text
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_done
import oguri.composeapp.generated.resources.calendar_leave_days_sheet_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val MIN_LEAVE_DAYS = 1
private const val MAX_LEAVE_DAYS = 30
private const val PICKER_VISIBLE_ITEM_COUNT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarLeaveDaysBottomSheet(
    leaveDays: Int,
    onLeaveDaysChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    var selectedLeaveDays by rememberSaveable(leaveDays) { mutableIntStateOf(leaveDays.coerceIn(MIN_LEAVE_DAYS, MAX_LEAVE_DAYS)) }

    CalendarFilterChip(
        text = stringResource(Res.string.calendar_leave_days_chip_text, leaveDays),
        onClick = {
            selectedLeaveDays = leaveDays.coerceIn(MIN_LEAVE_DAYS, MAX_LEAVE_DAYS)
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
                        text = stringResource(Res.string.calendar_leave_days_sheet_title),
                        style = OguriTheme.typography.cardTitle,
                        color = Neutral90,
                    )
                    Image(
                        painter = painterResource(resource = Res.drawable.btn_exit),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp).noRippleClickable(onClick = { isBottomSheetVisible = false }),
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LeaveDaysNumberPicker(
                    value = selectedLeaveDays,
                    onValueChange = { selectedLeaveDays = it },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(color = Mint70, shape = RoundedCornerShape(size = 8.dp))
                            .noRippleClickable(
                                onClick = {
                                    onLeaveDaysChanged(selectedLeaveDays)
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
private fun LeaveDaysNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = PICKER_VISIBLE_ITEM_COUNT,
    itemHeight: Dp = 42.dp,
) {
    val centerOffset = (visibleItemCount - 1) / 2
    val pickerState = rememberLazyListState(initialFirstVisibleItemIndex = (value - MIN_LEAVE_DAYS).coerceAtLeast(0))
    val itemHeightPx = with(LocalDensity.current) { itemHeight.roundToPx() }

    LaunchedEffect(pickerState, itemHeightPx) {
        snapshotFlow {
            pickerState.firstVisibleItemIndex to pickerState.firstVisibleItemScrollOffset
        }.map { (firstVisibleItemIndex, firstVisibleItemScrollOffset) ->
            nearestPickerIndex(
                firstVisibleItemIndex = firstVisibleItemIndex,
                firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
                itemHeightPx = itemHeightPx,
            )
        }.distinctUntilChanged()
            .collect { nearestIndex ->
                val day = (nearestIndex + MIN_LEAVE_DAYS).coerceIn(MIN_LEAVE_DAYS, MAX_LEAVE_DAYS)
                if (day != value) {
                    onValueChange(day)
                }
            }
    }

    LaunchedEffect(pickerState.isScrollInProgress, itemHeightPx) {
        if (!pickerState.isScrollInProgress) {
            val nearestIndex =
                nearestPickerIndex(
                    firstVisibleItemIndex = pickerState.firstVisibleItemIndex,
                    firstVisibleItemScrollOffset = pickerState.firstVisibleItemScrollOffset,
                    itemHeightPx = itemHeightPx,
                )
            pickerState.scrollToItem(index = nearestIndex)
        }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemCount),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(color = Mint50.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)),
        )
        LazyColumn(
            state = pickerState,
            horizontalAlignment = Alignment.CenterHorizontally,
            flingBehavior = pickerSnapFlingBehavior(pickerState),
        ) {
            items(centerOffset) {
                Spacer(modifier = Modifier.height(itemHeight))
            }
            items(MAX_LEAVE_DAYS - MIN_LEAVE_DAYS + 1) { index ->
                val day = index + MIN_LEAVE_DAYS
                val isSelected = day == value
                Text(
                    text = day.toString(),
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
}

private fun nearestPickerIndex(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    itemHeightPx: Int,
): Int {
    val nextIndexOffset = if (firstVisibleItemScrollOffset >= itemHeightPx / 2) 1 else 0
    return (firstVisibleItemIndex + nextIndexOffset).coerceIn(0, MAX_LEAVE_DAYS - MIN_LEAVE_DAYS)
}

@Composable
private fun pickerSnapFlingBehavior(listState: LazyListState): FlingBehavior =
    rememberSnapFlingBehavior(
        lazyListState = listState,
        snapPosition = SnapPosition.Center,
    )
