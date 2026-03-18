package com.bigong.oguri.feature.calendar.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.feature.calendar.ui.component.CalendarLeaveDaysBottomSheet
import com.bigong.oguri.feature.calendar.ui.component.CalendarRecommendationSection
import com.bigong.oguri.feature.calendar.ui.component.CalendarSkeletonContent
import com.bigong.oguri.feature.calendar.ui.model.CalendarUiState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_header_subtitle
import oguri.composeapp.generated.resources.calendar_header_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun CalendarScreen(
    calendarUiState: CalendarUiState,
    onLeaveDaysChanged: (Int) -> Unit,
    onCardClick: (Long) -> Unit,
    onDetailClick: (Long) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetryClick: () -> Unit,
) {
    if (calendarUiState.isLoading && calendarUiState.periodCards.isEmpty()) {
        CalendarSkeletonContent()
        return
    }

    if (calendarUiState.isError && calendarUiState.periodCards.isEmpty()) {
        NetworkErrorRetryContent(onRetryClick = onRetryClick)
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var listViewportBottomInWindow by remember { mutableStateOf(0f) }
    val shouldShowEndHint by remember(calendarUiState.hasMorePage, calendarUiState.isLoadingNextPage) {
        androidx.compose.runtime.derivedStateOf {
            !listState.canScrollForward &&
                listState.isScrollInProgress &&
                !calendarUiState.isLoadingNextPage &&
                !calendarUiState.hasMorePage
        }
    }

    LaunchedEffect(
        listState,
        calendarUiState.hasMorePage,
        calendarUiState.isLoadingNextPage,
        calendarUiState.periodCards.size,
    ) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo
                .lastOrNull()
                ?.index
        }.distinctUntilChanged()
            .collect { lastVisibleIndex ->
                val targetIndex = listState.layoutInfo.totalItemsCount - 3
                if (
                    lastVisibleIndex != null &&
                    lastVisibleIndex >= targetIndex &&
                    calendarUiState.hasMorePage &&
                    !calendarUiState.isLoadingNextPage
                ) {
                    onLoadNextPage()
                }
            }
    }

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
                periodCards = calendarUiState.periodCards,
                expandedPeriodId = calendarUiState.expandedPeriodId,
                isLoadingNextPage = calendarUiState.isLoadingNextPage,
                showEndHint = shouldShowEndHint,
                listViewportBottomInWindow = listViewportBottomInWindow,
                onCardClick = onCardClick,
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
}
