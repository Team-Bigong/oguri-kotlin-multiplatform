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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.PlaceCard
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.feature.perioddetail.ui.component.PeriodDetailSkeletonContent
import com.bigong.oguri.feature.perioddetail.ui.component.PeriodDetailTopBar
import com.bigong.oguri.feature.perioddetail.ui.model.PeriodDetailUiState
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
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
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggleClick: () -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    val periodDetail = periodDetailUiState.periodDetail

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
            periodDetailUiState.isLoading -> {
                PeriodDetailSkeletonContent(
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            periodDetailUiState.isError || periodDetail == null -> {
                NetworkErrorRetryContent(
                    onRetryClick = onRetryClick,
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
                                modifier = Modifier.height(120.dp),
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
                    if (periodDetail.places.isEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = stringResource(Res.string.period_detail_empty_places_hint),
                                style = OguriTheme.typography.bodyMedium,
                                color = Neutral50,
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = periodDetail.places,
                            key = { _, place -> place.id },
                        ) { _, place ->
                            PlaceCard(
                                place = place,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { onPlaceClick(place.id) },
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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
