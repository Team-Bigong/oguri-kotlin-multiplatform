package com.bigong.oguri.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.RecommendPeriod
import com.bigong.oguri.feature.home.ui.component.HomeErrorContent
import com.bigong.oguri.feature.home.ui.component.HomeGreetingSection
import com.bigong.oguri.feature.home.ui.component.HomeGuideHeader
import com.bigong.oguri.feature.home.ui.component.HomeLoadingContent
import com.bigong.oguri.feature.home.ui.component.HomeLogoHeader
import com.bigong.oguri.feature.home.ui.component.HomeMoreRecommendationButton
import com.bigong.oguri.feature.home.ui.component.HomePlaceCard
import com.bigong.oguri.feature.home.ui.component.HomeProductCard
import com.bigong.oguri.feature.home.ui.component.HomeStrategyCard
import com.bigong.oguri.feature.home.ui.model.HomeUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_ad_activity_title
import oguri.composeapp.generated.resources.home_ad_activity_title_highlight
import oguri.composeapp.generated.resources.home_ad_hotel_title
import oguri.composeapp.generated.resources.home_ad_hotel_title_highlight
import oguri.composeapp.generated.resources.home_ad_plane_title
import oguri.composeapp.generated.resources.home_ad_plane_title_highlight
import oguri.composeapp.generated.resources.home_cta_more_recommend
import oguri.composeapp.generated.resources.home_error_retry
import oguri.composeapp.generated.resources.home_greeting_name
import oguri.composeapp.generated.resources.home_greeting_question
import oguri.composeapp.generated.resources.home_guide_match_places
import oguri.composeapp.generated.resources.home_guide_match_places_highlight
import oguri.composeapp.generated.resources.home_guide_trip_products
import oguri.composeapp.generated.resources.home_guide_trip_products_highlight
import oguri.composeapp.generated.resources.home_hint_place_cards
import oguri.composeapp.generated.resources.home_hint_trip_products
import oguri.composeapp.generated.resources.home_loading
import oguri.composeapp.generated.resources.home_tab_rank_one
import oguri.composeapp.generated.resources.home_tab_rank_three
import oguri.composeapp.generated.resources.home_tab_rank_two
import oguri.composeapp.generated.resources.ic_binoculars
import oguri.composeapp.generated.resources.ic_shopping_bag
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    homeUiState: HomeUiState,
    onRankSelected: (Int) -> Unit,
    onSavedChanged: (Boolean) -> Unit,
    onRetryClick: () -> Unit,
) {
    if (homeUiState.isLoading) {
        HomeLoadingContent(message = stringResource(Res.string.home_loading))
        return
    }

    if (homeUiState.isError || homeUiState.recommendPeriods.isEmpty()) {
        val errorText: String = stringResource(Res.string.home_error_retry)
        HomeErrorContent(
            message = errorText,
            retryText = errorText,
            onRetryClick = onRetryClick,
        )
        return
    }

    val currentPeriod: RecommendPeriod = homeUiState.recommendPeriods.firstOrNull { recommendPeriod: RecommendPeriod ->
        recommendPeriod.rank == homeUiState.selectedRank
    } ?: homeUiState.recommendPeriods.first()

    val rankLabels: List<String> = listOf(
        stringResource(Res.string.home_tab_rank_one),
        stringResource(Res.string.home_tab_rank_two),
        stringResource(Res.string.home_tab_rank_three),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F8))
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
    ) {
        LazyColumn(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 18.dp),
        ) {
            item {
                HomeLogoHeader()
            }
            item {
                HomeGreetingSection(
                    nameText = stringResource(Res.string.home_greeting_name),
                    questionText = stringResource(Res.string.home_greeting_question),
                )
            }
            item {
                HomeStrategyCard(
                    selectedRank = homeUiState.selectedRank,
                    savedRankSet = homeUiState.savedRankSet,
                    rankLabels = rankLabels,
                    currentPeriod = currentPeriod,
                    onRankSelected = onRankSelected,
                    onSavedChanged = onSavedChanged,
                )
            }
            item {
                HomeGuideHeader(
                    iconResource = Res.drawable.ic_binoculars,
                    titleText = stringResource(Res.string.home_guide_match_places),
                    highlightedText = stringResource(Res.string.home_guide_match_places_highlight),
                    subtitleText = stringResource(Res.string.home_hint_place_cards),
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(space = 10.dp)) {
                    items(items = currentPeriod.places, key = { place -> place.id }) { place ->
                        HomePlaceCard(
                            place = place,
                            modifier = Modifier
                                .width(width = 154.dp),
                        )
                    }
                }
            }
            item {
                HomeGuideHeader(
                    iconResource = Res.drawable.ic_shopping_bag,
                    titleText = stringResource(Res.string.home_guide_trip_products),
                    highlightedText = stringResource(Res.string.home_guide_trip_products_highlight),
                    subtitleText = stringResource(Res.string.home_hint_trip_products),
                )
            }
            items(items = currentPeriod.advertisements) { advertisement: Advertisement ->
                HomeProductCard(
                    imageUrl = advertisement.url,
                    titleText = advertisementTitleText(advertisement = advertisement),
                    highlightedText = advertisementHighlightedText(advertisement = advertisement),
                )
            }
            item {
                Spacer(modifier = Modifier.height(height = 10.dp))
                HomeMoreRecommendationButton(text = stringResource(Res.string.home_cta_more_recommend))
                Spacer(modifier = Modifier.height(height = 14.dp))
            }
        }
    }
}

@Composable
private fun advertisementTitleText(advertisement: Advertisement): String {
    return when (advertisement.platform) {
        "hotel" -> stringResource(Res.string.home_ad_hotel_title)
        "plane" -> stringResource(Res.string.home_ad_plane_title)
        else -> stringResource(Res.string.home_ad_activity_title)
    }
}

@Composable
private fun advertisementHighlightedText(advertisement: Advertisement): String {
    return when (advertisement.platform) {
        "hotel" -> stringResource(Res.string.home_ad_hotel_title_highlight)
        "plane" -> stringResource(Res.string.home_ad_plane_title_highlight)
        else -> stringResource(Res.string.home_ad_activity_title_highlight)
    }
}
