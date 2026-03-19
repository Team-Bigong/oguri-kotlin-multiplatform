package com.bigong.oguri.feature.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.calendar.ui.component.CalendarLeaveDaysBottomSheet
import com.bigong.oguri.feature.calendar.ui.component.CalendarRecommendationSection
import com.bigong.oguri.feature.calendar.ui.component.CalendarSkeletonContent
import com.bigong.oguri.feature.calendar.ui.model.CalendarPeriodCardUiModel
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_header_subtitle
import oguri.composeapp.generated.resources.calendar_header_title
import oguri.composeapp.generated.resources.ic_arrow_up
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val SCROLL_TOP_BUTTON_SIZE: Dp = 48.dp

@Composable
fun CalendarScreen(
    calendarUiState: CalendarUiState,
    pagedPeriodCards: LazyPagingItems<CalendarPeriodCardUiModel>,
    savedStateByPeriodKey: Map<String, Boolean>,
    onLeaveDaysChanged: (Int) -> Unit,
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
    var listViewportBottomInWindow by remember { mutableStateOf(0f) }
    val shouldShowScrollTopButton by remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex > 1 ||
                (listState.firstVisibleItemIndex == 1 && listState.firstVisibleItemScrollOffset > 280)
        }
    }
    val shouldShowEndHint by remember(pagedPeriodCards.loadState.append) {
        androidx.compose.runtime.derivedStateOf {
            !listState.canScrollForward &&
                listState.isScrollInProgress &&
                pagedPeriodCards.loadState.append is LoadState.NotLoading
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
                CalendarLeaveDaysBottomSheet(
                    leaveDays = calendarUiState.leaveDays,
                    onLeaveDaysChanged = onLeaveDaysChanged,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Neutral20)
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                CalendarRecommendationSection(
                    pagedPeriodCards = pagedPeriodCards,
                    savedStateByPeriodKey = savedStateByPeriodKey,
                    expandedPeriodId = calendarUiState.expandedPeriodId,
                    isLoadingNextPage = isAppending,
                    showEndHint = shouldShowEndHint,
                    listViewportBottomInWindow = listViewportBottomInWindow,
                    onCardClick = onCardClick,
                    onSaveToggleClick = onSaveToggleClick,
                    onDetailClick = onDetailClick,
                    onRequestScrollBy = { scrollByPixels ->
                        if (scrollByPixels <= 0f) {
                            return@CalendarRecommendationSection
                        }
                        coroutineScope.launch {
                            listState.animateScrollBy(
                                value = scrollByPixels,
                                animationSpec = tween(durationMillis = 220),
                            )
                        }
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        AnimatedVisibility(
            visible = shouldShowScrollTopButton,
            enter = fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = fadeOut(animationSpec = tween(durationMillis = 120)),
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
                    .navigationBarsPadding(),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(SCROLL_TOP_BUTTON_SIZE)
                        .shadow(elevation = 8.dp, shape = CircleShape)
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
