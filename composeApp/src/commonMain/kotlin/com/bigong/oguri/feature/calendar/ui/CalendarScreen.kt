package com.bigong.oguri.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.util.extension.calculateFloatingBottomNavigationAdditionalBottomPadding
import com.bigong.oguri.core.util.extension.floatingNavigationBarsPadding
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.core.util.extension.oguriElevation
import com.bigong.oguri.feature.calendar.ui.component.CalendarLeaveDaysBottomSheet
import com.bigong.oguri.feature.calendar.ui.component.CalendarPeriodBottomSheet
import com.bigong.oguri.feature.calendar.ui.component.CalendarSkeletonContent
import com.bigong.oguri.feature.calendar.ui.component.calendarRecommendationSection
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_end_of_list_message
import oguri.composeapp.generated.resources.calendar_header_subtitle
import oguri.composeapp.generated.resources.calendar_header_title
import oguri.composeapp.generated.resources.ic_arrow_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val SCROLL_TOP_BUTTON_SIZE: Dp = 48.dp
private const val CALENDAR_RECOMMENDATION_CARD_FIRST_ITEM_INDEX: Int = 2

@Composable
fun CalendarScreen(
    calendarUiState: CalendarUiState,
    pagedPeriodCards: LazyPagingItems<CalendarPeriodCardUiModel>,
    savedStateByPeriodKey: Map<String, Boolean>,
    scrollToTopTrigger: Int,
    onLeaveDaysChanged: (Int) -> Unit,
    onPeriodFilterChanged: (Int, Int?) -> Unit,
    onCardClick: (Long) -> Unit,
    onSaveToggleClick: (Long) -> Unit,
    onDetailClick: (Long) -> Unit,
) {
    val isInitialLoading =
        pagedPeriodCards.loadState.refresh is LoadState.Loading &&
            pagedPeriodCards.itemCount == 0
    val isInitialError =
        pagedPeriodCards.loadState.refresh is LoadState.Error &&
            pagedPeriodCards.itemCount == 0
    val isAppending = pagedPeriodCards.loadState.append is LoadState.Loading

    if (isInitialLoading) {
        CalendarSkeletonContent()
        return
    }

    if (isInitialError) {
        NetworkErrorRetryContent(onRetryClick = pagedPeriodCards::retry)
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var restoredExpandedPeriodId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.animateScrollToItem(index = 0)
            restoredExpandedPeriodId = null
        }
    }
    LaunchedEffect(
        calendarUiState.expandedPeriodId,
        pagedPeriodCards.itemCount,
        pagedPeriodCards.loadState.refresh,
    ) {
        val expandedPeriodId = calendarUiState.expandedPeriodId ?: return@LaunchedEffect
        if (restoredExpandedPeriodId == expandedPeriodId || pagedPeriodCards.loadState.refresh is LoadState.Loading) {
            return@LaunchedEffect
        }

        val expandedPeriodIndex =
            pagedPeriodCards.itemSnapshotList.items.indexOfFirst { periodCard ->
                periodCard.id == expandedPeriodId
            }
        if (expandedPeriodIndex < 0) {
            return@LaunchedEffect
        }

        val lazyColumnItemIndex = CALENDAR_RECOMMENDATION_CARD_FIRST_ITEM_INDEX + expandedPeriodIndex
        val isExpandedPeriodVisible =
            listState.layoutInfo.visibleItemsInfo.any { visibleItem ->
                visibleItem.index == lazyColumnItemIndex
            }
        val isListRestoredToTop =
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        if (isListRestoredToTop && !isExpandedPeriodVisible) {
            listState.scrollToItem(index = lazyColumnItemIndex)
        }
        restoredExpandedPeriodId = expandedPeriodId
    }
    var listViewportBottomInWindow by remember { mutableStateOf(0f) }
    val shouldShowScrollTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 1 ||
                (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 280)
        }
    }
    val shouldShowEndHint by remember(listState.isScrollInProgress, pagedPeriodCards.loadState.append, pagedPeriodCards.itemCount) {
        derivedStateOf {
            !listState.canScrollForward &&
                listState.isScrollInProgress &&
                pagedPeriodCards.loadState.append is LoadState.NotLoading &&
                pagedPeriodCards.itemCount > 0
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Neutral5)
                    .statusBarsPadding()
                    .onGloballyPositioned { coordinates ->
                        listViewportBottomInWindow = coordinates.positionInWindow().y + coordinates.size.height
                    },
            contentPadding =
                PaddingValues(
                    bottom =
                        calculateFloatingBottomNavigationAdditionalBottomPadding(
                            hasFloatingBottomNavigation = true,
                            baseBottomPadding = 16.dp,
                        ),
                ),
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.calendar_header_title),
                    style = OguriTheme.typography.sectionTitle,
                    color = Neutral90,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.calendar_header_subtitle),
                    style = OguriTheme.typography.cardSubtitle,
                    color = Neutral50,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp),
                ) {
                    CalendarPeriodBottomSheet(
                        selectedYear = calendarUiState.selectedYear,
                        selectedMonth = calendarUiState.selectedMonth,
                        onPeriodFilterChanged = onPeriodFilterChanged,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    CalendarLeaveDaysBottomSheet(
                        leaveDays = calendarUiState.leaveDays,
                        onLeaveDaysChanged = onLeaveDaysChanged,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Neutral20)
                Spacer(modifier = Modifier.height(18.dp))
            }

            calendarRecommendationSection(
                pagedPeriodCards = pagedPeriodCards,
                savedStateByPeriodKey = savedStateByPeriodKey,
                expandedPeriodId = calendarUiState.expandedPeriodId,
                isLoadingNextPage = isAppending,
                listViewportBottomInWindow = listViewportBottomInWindow,
                onCardClick = onCardClick,
                onSaveToggleClick = onSaveToggleClick,
                onDetailClick = onDetailClick,
                onRequestScrollBy = { scrollByPixels ->
                    if (scrollByPixels <= 0f) {
                        return@calendarRecommendationSection
                    }
                    coroutineScope.launch {
                        listState.animateScrollBy(
                            value = scrollByPixels,
                            animationSpec = tween(durationMillis = 220),
                        )
                    }
                },
            )

            item(key = "calendar_recommendation_bottom_spacing") {
                Spacer(
                    modifier = Modifier.height(28.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = shouldShowEndHint,
            enter = fadeIn(animationSpec = tween(120)) + slideInVertically(animationSpec = tween(120)) { it / 2 },
            exit = fadeOut(animationSpec = tween(120)) + slideOutVertically(animationSpec = tween(120)) { it / 2 },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp),
        ) {
            Text(
                text = stringResource(Res.string.calendar_end_of_list_message),
                style = OguriTheme.typography.bodySmall,
                color = Neutral50,
            )
        }

        AnimatedVisibility(
            visible = shouldShowScrollTopButton,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .floatingNavigationBarsPadding(
                        hasFloatingBottomNavigation = true,
                        baseBottomPadding = 72.dp,
                    ).navigationBarsPadding(),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(SCROLL_TOP_BUTTON_SIZE)
                        .oguriElevation(elevation = 8.dp, shape = CircleShape)
                        .background(color = Neutral0.copy(alpha = 0.92f), shape = CircleShape)
                        .noRippleClickable(
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index = 0)
                                }
                            },
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_arrow_up),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Neutral90),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}
