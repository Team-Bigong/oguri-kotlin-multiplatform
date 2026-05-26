package com.bigong.oguri.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.ui.component.AdvertisementCard
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.core.util.extension.calculateFloatingBottomNavigationAdditionalBottomPadding
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.feature.home.ui.component.HomeGreetingSection
import com.bigong.oguri.feature.home.ui.component.HomeLogoHeader
import com.bigong.oguri.feature.home.ui.component.HomeMoreRecommendationButton
import com.bigong.oguri.feature.home.ui.component.HomeSkeletonContent
import com.bigong.oguri.feature.home.ui.component.HomeStrategyCard
import com.bigong.oguri.feature.home.ui.component.MonthlyTopPeriodCard
import com.bigong.oguri.feature.home.ui.component.WeeklyTopPlaceCarousel
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_cta_more_recommend
import oguri.composeapp.generated.resources.home_greeting_name
import oguri.composeapp.generated.resources.home_greeting_question
import oguri.composeapp.generated.resources.home_guide_match_places
import oguri.composeapp.generated.resources.home_guide_match_places_highlight
import oguri.composeapp.generated.resources.home_guide_monthly_top_periods
import oguri.composeapp.generated.resources.home_guide_monthly_top_periods_highlight
import oguri.composeapp.generated.resources.home_guide_trip_products
import oguri.composeapp.generated.resources.home_guide_trip_products_highlight
import oguri.composeapp.generated.resources.home_guide_weekly_top_places
import oguri.composeapp.generated.resources.home_guide_weekly_top_places_highlight
import oguri.composeapp.generated.resources.home_hint_monthly_top_periods
import oguri.composeapp.generated.resources.home_hint_place_cards
import oguri.composeapp.generated.resources.home_hint_trip_products
import oguri.composeapp.generated.resources.home_hint_weekly_top_places
import oguri.composeapp.generated.resources.home_more_recommendation_subtitle
import oguri.composeapp.generated.resources.home_tab_rank_one
import oguri.composeapp.generated.resources.home_tab_rank_three
import oguri.composeapp.generated.resources.home_tab_rank_two
import oguri.composeapp.generated.resources.ic_fire
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.ic_shopping_bag
import oguri.composeapp.generated.resources.ic_trophy
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onRankSelected: (Int) -> Unit,
    onSavedChanged: (Boolean) -> Unit,
    onRetryClick: () -> Unit,
    onAdvertisementClick: (Advertisement, Int) -> Unit,
    onPlaceClick: (Long, String?, String?) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onMoveToCalendarClick: () -> Unit,
    onSearchClick: () -> Unit,
    scrollToTopTrigger: Int,
) {
    if (homeUiState.isLoading) {
        HomeSkeletonContent()
        return
    }

    if (homeUiState.isError || homeUiState.recommendPeriods.isEmpty()) {
        NetworkErrorRetryContent(onRetryClick = onRetryClick)
        return
    }

    val currentPeriod =
        homeUiState.recommendPeriods.firstOrNull { recommendPeriod ->
            recommendPeriod.rank == homeUiState.selectedRank
        } ?: homeUiState.recommendPeriods.first()

    val rankLabels =
        listOf(
            stringResource(Res.string.home_tab_rank_one),
            stringResource(Res.string.home_tab_rank_two),
            stringResource(Res.string.home_tab_rank_three),
        )
    val listState = rememberLazyListState()

    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            listState.animateScrollToItem(index = 0)
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .statusBarsPadding(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(weight = 1f),
            contentPadding =
                PaddingValues(
                    bottom =
                        calculateFloatingBottomNavigationAdditionalBottomPadding(
                            hasFloatingBottomNavigation = true,
                            baseBottomPadding = 24.dp,
                        ),
                ),
        ) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                HomeLogoHeader(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onSearchClick = onSearchClick,
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                HomeGreetingSection(
                    nameText = stringResource(Res.string.home_greeting_name),
                    questionText = stringResource(Res.string.home_greeting_question),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
                HomeStrategyCard(
                    selectedRank = homeUiState.selectedRank,
                    savedRankSet = homeUiState.savedRankSet,
                    rankLabels = rankLabels,
                    currentPeriod = currentPeriod,
                    onRankSelected = onRankSelected,
                    onSavedChanged = onSavedChanged,
                    onClick = {
                        onPeriodClick(
                            currentPeriod.startDate.toString(),
                            currentPeriod.endDate.toString(),
                        )
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_plane,
                    titleText = stringResource(Res.string.home_guide_match_places),
                    highlightedText = stringResource(Res.string.home_guide_match_places_highlight),
                    subtitleText = stringResource(Res.string.home_hint_place_cards),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(18.dp))
                key(homeUiState.selectedRank) {
                    PlaceHorizontalCarousel(
                        places = currentPeriod.places,
                        onPlaceClick = { place ->
                            onPlaceClick(
                                place.id,
                                currentPeriod.startDate.toString(),
                                currentPeriod.endDate.toString(),
                            )
                        },
                    )
                }
            }
            if (homeUiState.weeklyTopPlaces.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    GuideHeader(
                        iconResource = Res.drawable.ic_fire,
                        titleText = stringResource(Res.string.home_guide_weekly_top_places),
                        highlightedText = stringResource(Res.string.home_guide_weekly_top_places_highlight),
                        subtitleText = stringResource(Res.string.home_hint_weekly_top_places),
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    WeeklyTopPlaceCarousel(
                        places = homeUiState.weeklyTopPlaces,
                        onPlaceClick = { place ->
                            onPlaceClick(place.id, null, null)
                        },
                    )
                }
            }
            if (homeUiState.monthlyTopPeriods.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    GuideHeader(
                        iconResource = Res.drawable.ic_trophy,
                        titleText = stringResource(Res.string.home_guide_monthly_top_periods),
                        highlightedText = stringResource(Res.string.home_guide_monthly_top_periods_highlight),
                        subtitleText = stringResource(Res.string.home_hint_monthly_top_periods),
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
                items(
                    count = homeUiState.monthlyTopPeriods.size,
                    key = { index ->
                        val period = homeUiState.monthlyTopPeriods[index]
                        "${period.startDate}_${period.endDate}_${period.rank}"
                    },
                ) { index ->
                    val period = homeUiState.monthlyTopPeriods[index]
                    Spacer(modifier = Modifier.height(18.dp))
                    MonthlyTopPeriodCard(
                        period = period,
                        onClick = { selectedPeriod ->
                            onPeriodClick(
                                selectedPeriod.startDate.toString(),
                                selectedPeriod.endDate.toString(),
                            )
                        },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_shopping_bag,
                    titleText = stringResource(Res.string.home_guide_trip_products),
                    highlightedText = stringResource(Res.string.home_guide_trip_products_highlight),
                    subtitleText = stringResource(Res.string.home_hint_trip_products),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            items(
                count = currentPeriod.advertisements.size,
                key = { index ->
                    val advertisement = currentPeriod.advertisements[index]
                    "${advertisement.platform.name}:${advertisement.url}:$index"
                },
            ) { index ->
                val advertisement = currentPeriod.advertisements[index]
                Spacer(modifier = Modifier.height(18.dp))
                AdvertisementCard(
                    advertisement = advertisement,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = {
                        onAdvertisementClick(advertisement, index)
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(height = 28.dp))
                HomeMoreRecommendationButton(
                    subtitleText = stringResource(Res.string.home_more_recommendation_subtitle),
                    text = stringResource(Res.string.home_cta_more_recommend),
                    onClick = onMoveToCalendarClick,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(
                    modifier = Modifier.height(height = 24.dp),
                )
            }
        }
    }
}
