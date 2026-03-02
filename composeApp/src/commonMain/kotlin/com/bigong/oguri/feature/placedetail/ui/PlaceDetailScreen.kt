package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.AdvertisementCard
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.SaveToggleButton
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.Experience
import com.bigong.oguri.feature.home.ui.component.HomeErrorContent
import com.bigong.oguri.feature.home.ui.component.HomeLoadingContent
import com.bigong.oguri.feature.placedetail.ui.component.ExperienceCard
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailDescriptionSection
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailImagePager
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailTopBar
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_error_retry
import oguri.composeapp.generated.resources.home_loading
import oguri.composeapp.generated.resources.ic_binoculars
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.place_detail_country
import oguri.composeapp.generated.resources.place_detail_flight_title
import oguri.composeapp.generated.resources.place_detail_flight_title_highlight
import oguri.composeapp.generated.resources.place_detail_relevant_places
import oguri.composeapp.generated.resources.place_detail_relevant_places_highlight
import oguri.composeapp.generated.resources.place_detail_relevant_places_subtitle
import oguri.composeapp.generated.resources.place_detail_section_experience
import oguri.composeapp.generated.resources.place_detail_section_experience_highlight
import oguri.composeapp.generated.resources.place_detail_section_experience_subtitle
import org.jetbrains.compose.resources.stringResource

private const val PLACE_DETAIL_TITLE_ITEM_KEY = "place_detail_title"

@Composable
fun PlaceDetailScreen(
    placeDetailUiState: PlaceDetailUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onShareClick: () -> Unit,
    onSaveToggleClick: () -> Unit,
    onUrlClick: (String) -> Unit,
    onPlaceClick: (Long) -> Unit,
) {
    if (placeDetailUiState.isLoading) {
        HomeLoadingContent(message = stringResource(Res.string.home_loading))
        return
    }

    val placeDetail = placeDetailUiState.placeDetail
    if (placeDetailUiState.isError || placeDetail == null) {
        val errorText = stringResource(Res.string.home_error_retry)
        HomeErrorContent(
            message = errorText,
            retryText = errorText,
            onRetryClick = onRetryClick,
        )
        return
    }

    val lazyListState = rememberLazyListState()
    val isTopBarCollapsed by rememberPlaceDetailTopBarCollapsedState(listState = lazyListState)

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().background(Neutral5),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            item {
                PlaceDetailImagePager(imageUrls = placeDetail.thumbnailUrls)
            }
            item(key = PLACE_DETAIL_TITLE_ITEM_KEY) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = placeDetail.city,
                            style = OguriTheme.typography.sectionTitle,
                            color = Neutral100,
                        )
                        Text(
                            text = stringResource(Res.string.place_detail_country, placeDetail.country),
                            style = OguriTheme.typography.labelMedium,
                            color = Neutral40,
                        )
                    }
                    SaveToggleButton(
                        checked = placeDetailUiState.isSaved,
                        onCheckedChange = { onSaveToggleClick() },
                    )
                }
            }
            item {
                PlaceDetailDescriptionSection(
                    description = placeDetail.description,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(22.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_binoculars,
                    titleText = stringResource(Res.string.place_detail_section_experience),
                    highlightedText = stringResource(Res.string.place_detail_section_experience_highlight),
                    subtitleText = stringResource(Res.string.place_detail_section_experience_subtitle),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(14.dp))
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = placeDetail.experiences, key = { experience: Experience -> experience.title }) { experience: Experience ->
                        ExperienceCard(
                            experience = experience,
                            onClick = onUrlClick,
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_plane,
                    titleText = stringResource(Res.string.place_detail_flight_title),
                    highlightedText = stringResource(Res.string.place_detail_flight_title_highlight),
                    subtitleText = null,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                AdvertisementCard(
                    advertisement =
                        Advertisement(
                            platform = AdvertisementPlatform.SKYSCANNER,
                            url = placeDetail.flightUrl,
                        ),
                    titleText = stringResource(Res.string.place_detail_flight_title),
                    highlightedText = stringResource(Res.string.place_detail_flight_title_highlight),
                    highlightedColor = Mint70,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = onUrlClick,
                )
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_plane,
                    titleText = stringResource(Res.string.place_detail_relevant_places),
                    highlightedText = stringResource(Res.string.place_detail_relevant_places_highlight),
                    subtitleText = stringResource(Res.string.place_detail_relevant_places_subtitle),
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(14.dp))
                PlaceHorizontalCarousel(
                    places = placeDetail.relevantPlaces,
                    onPlaceClick = { place -> onPlaceClick(place.id) },
                )
            }
        }

        PlaceDetailTopBar(
            city = placeDetail.city,
            isSaved = placeDetailUiState.isSaved,
            isCollapsed = isTopBarCollapsed,
            onBackClick = onBackClick,
            onShareClick = onShareClick,
            onSaveToggleClick = onSaveToggleClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun rememberPlaceDetailTopBarCollapsedState(
    listState: LazyListState,
): androidx.compose.runtime.State<Boolean> {
    return remember(listState) {
        derivedStateOf {
            val titleItemInfo = listState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                itemInfo.key == PLACE_DETAIL_TITLE_ITEM_KEY
            }

            if (titleItemInfo == null) {
                listState.firstVisibleItemIndex > 1
            } else {
                titleItemInfo.offset <= 90
            }
        }
    }
}
