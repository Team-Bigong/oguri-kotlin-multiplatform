package com.bigong.oguri.feature.perioddetail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.PlaceCard
import com.bigong.oguri.core.ui.component.SkeletonBox
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.feature.perioddetail.ui.component.PeriodDetailSkeletonContent
import com.bigong.oguri.feature.perioddetail.ui.component.PeriodDetailTopBar
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_d_day_after
import oguri.composeapp.generated.resources.calendar_d_day_before
import oguri.composeapp.generated.resources.calendar_month_day
import oguri.composeapp.generated.resources.calendar_period_range
import oguri.composeapp.generated.resources.home_guide_match_places
import oguri.composeapp.generated.resources.home_guide_match_places_highlight
import oguri.composeapp.generated.resources.home_strategy_day_off_hint
import oguri.composeapp.generated.resources.home_strategy_holiday_with
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.img_oguri_parasol
import oguri.composeapp.generated.resources.period_detail_empty_places_hint
import oguri.composeapp.generated.resources.period_detail_holiday_fallback
import oguri.composeapp.generated.resources.period_detail_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PeriodDetailScreen(
    periodDetailUiState: PeriodDetailUiState,
    pagedPlaces: LazyPagingItems<Place>,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggleClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val periodDetail = periodDetailUiState.periodDetail
    val isInitialLoading =
        periodDetail == null &&
            (periodDetailUiState.isLoading || pagedPlaces.loadState.refresh is LoadState.Loading)
    val isInitialError =
        periodDetail == null &&
            pagedPlaces.loadState.refresh is LoadState.Error

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .navigationBarsPadding(),
    ) {
        PeriodDetailTopBar(
            titleText = stringResource(Res.string.period_detail_title),
            isSaved = periodDetailUiState.isSaved,
            onBackClick = onBackClick,
            onShareClick = if (periodDetail == null) ({}) else onShareClick,
            onSaveToggleClick = if (periodDetail == null) ({}) else onSaveToggleClick,
        )

        when {
            isInitialLoading -> {
                PeriodDetailSkeletonContent(
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            isInitialError -> {
                NetworkErrorRetryContent(
                    onRetryClick = onRetryClick,
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            periodDetail == null -> {
                PeriodDetailSkeletonContent(
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            else -> {
                val startDateText = periodDetail.startDate.toMonthDayText()
                val endDateText = periodDetail.endDate.toMonthDayText()
                val periodText = stringResource(Res.string.calendar_period_range, startDateText, endDateText)
                val holidayText =
                    periodDetail.holiday.joinToString(separator = ", ").ifBlank {
                        stringResource(Res.string.period_detail_holiday_fallback)
                    }
                val holidayDescriptionText = stringResource(Res.string.home_strategy_holiday_with, holidayText)
                val leaveDayHintText =
                    stringResource(Res.string.home_strategy_day_off_hint, periodDetail.dayOffCount, periodDetail.totalTripCount)
                val dDayText =
                    if (periodDetailUiState.dDay >= 0) {
                        stringResource(Res.string.calendar_d_day_before, periodDetailUiState.dDay)
                    } else {
                        stringResource(Res.string.calendar_d_day_after, -periodDetailUiState.dDay)
                    }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(count = 2),
                    modifier = Modifier.weight(weight = 1f),
                    verticalArrangement = Arrangement.spacedBy(space = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 18.dp),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = periodText,
                                    style = OguriTheme.typography.sectionTitle,
                                    color = Neutral100,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = dDayText,
                                    style = OguriTheme.typography.labelMedium,
                                    color = Mint70,
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text =
                                        holidayDescriptionText.getStyledText(
                                            style =
                                                OguriTheme.typography.cardSubtitle.copy(
                                                    color = Mint70,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                            holidayText,
                                        ),
                                    style = OguriTheme.typography.cardSubtitle,
                                    color = Neutral100,
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = leaveDayHintText,
                                    style = OguriTheme.typography.caption,
                                    color = Neutral50,
                                )
                            }
                            Image(
                                painter = painterResource(Res.drawable.img_oguri_parasol),
                                contentDescription = null,
                                modifier = Modifier.height(132.dp),
                            )
                        }
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        GuideHeader(
                            iconResource = Res.drawable.ic_plane,
                            titleText = stringResource(Res.string.home_guide_match_places),
                            highlightedText = stringResource(Res.string.home_guide_match_places_highlight),
                            subtitleText = null,
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                    if (pagedPlaces.itemCount == 0 && pagedPlaces.loadState.append !is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(Res.string.period_detail_empty_places_hint),
                                style = OguriTheme.typography.bodyMedium,
                                color = Neutral50,
                            )
                        }
                    } else {
                        for (index in 0 until pagedPlaces.itemCount) {
                            val place: Place = pagedPlaces[index] ?: continue
                            item(
                                key = place.id,
                                span = { GridItemSpan(1) },
                            ) {
                                PlaceCard(
                                    place = place,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onPlaceClick(place.id) },
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                    if (pagedPlaces.loadState.append is LoadState.Loading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SkeletonBox(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalDate.toMonthDayText(): String =
    stringResource(
        Res.string.calendar_month_day,
        month.ordinal + 1,
        day,
    )
