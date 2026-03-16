package com.bigong.oguri.feature.placedetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.AdvertisementCard
import com.bigong.oguri.core.ui.component.GuideHeader
import com.bigong.oguri.core.ui.component.NetworkErrorRetryContent
import com.bigong.oguri.core.ui.component.PlaceHorizontalCarousel
import com.bigong.oguri.core.ui.component.SaveToggleButton
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import com.bigong.oguri.domain.model.Experience
import com.bigong.oguri.feature.home.ui.component.HomeLoadingContent
import com.bigong.oguri.feature.placedetail.ui.component.ExperienceCard
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailDescriptionSection
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailImagePager
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailShareButton
import com.bigong.oguri.feature.placedetail.ui.component.PlaceDetailTopBar
import com.bigong.oguri.feature.placedetail.ui.model.PlaceDetailUiState
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_loading
import oguri.composeapp.generated.resources.ic_binoculars
import oguri.composeapp.generated.resources.ic_plane
import oguri.composeapp.generated.resources.ic_ticket
import oguri.composeapp.generated.resources.place_detail_country
import oguri.composeapp.generated.resources.place_detail_flight_card_title
import oguri.composeapp.generated.resources.place_detail_flight_title
import oguri.composeapp.generated.resources.place_detail_flight_title_highlight
import oguri.composeapp.generated.resources.place_detail_relevant_places
import oguri.composeapp.generated.resources.place_detail_relevant_places_highlight
import oguri.composeapp.generated.resources.place_detail_section_experience
import oguri.composeapp.generated.resources.place_detail_section_experience_highlight
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
        NetworkErrorRetryContent(onRetryClick = onRetryClick)
        return
    }

    val lazyListState = rememberLazyListState()
    val isTopBarCollapsed by rememberPlaceDetailTopBarCollapsedState(listState = lazyListState)
    var maxExperienceCardHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val uniformExperienceCardHeight =
        if (maxExperienceCardHeightPx > 0) {
            with(density) { maxExperienceCardHeightPx.toDp() }
        } else {
            null
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Neutral5)
                .navigationBarsPadding(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                PlaceDetailImagePager(imageUrls = placeDetail.thumbnailUrls)
            }
            item(key = PLACE_DETAIL_TITLE_ITEM_KEY) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 12.dp),
                    ) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        PlaceDetailShareButton(
                            onShareClick = onShareClick,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                        SaveToggleButton(
                            checked = placeDetailUiState.isSaved,
                            onCheckedChange = { onSaveToggleClick() },
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PlaceDetailDescriptionSection(
                    description = placeDetail.description,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_binoculars,
                    titleText = stringResource(Res.string.place_detail_section_experience),
                    highlightedText = stringResource(Res.string.place_detail_section_experience_highlight),
                    subtitleText = null,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                Spacer(modifier = Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = placeDetail.experiences, key = { experience: Experience -> experience.title }) { experience: Experience ->
                        ExperienceCard(
                            experience = experience,
                            uniformHeight = uniformExperienceCardHeight,
                            onMeasuredHeight = { measuredHeightPx ->
                                if (measuredHeightPx > maxExperienceCardHeightPx) {
                                    maxExperienceCardHeightPx = measuredHeightPx
                                }
                            },
                            onClick = onUrlClick,
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(28.dp))
                GuideHeader(
                    iconResource = Res.drawable.ic_ticket,
                    titleText = stringResource(Res.string.place_detail_flight_title),
                    highlightedText = stringResource(Res.string.place_detail_flight_title_highlight),
                    subtitleText = null,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
                Spacer(modifier = Modifier.height(18.dp))
                AdvertisementCard(
                    advertisement =
                        Advertisement(
                            platform = AdvertisementPlatform.SKYSCANNER,
                            url = placeDetail.flightUrl,
                        ),
                    titleText = stringResource(Res.string.place_detail_flight_card_title, placeDetail.city),
                    highlightedText = placeDetail.city,
                    highlightedColor = Mint70,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = onUrlClick,
                )
            }
            if (placeDetail.relevantPlaces.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(28.dp))
                    GuideHeader(
                        iconResource = Res.drawable.ic_plane,
                        titleText = stringResource(Res.string.place_detail_relevant_places),
                        highlightedText = stringResource(Res.string.place_detail_relevant_places_highlight),
                        subtitleText = null,
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    PlaceHorizontalCarousel(
                        places = placeDetail.relevantPlaces,
                        onPlaceClick = { place -> onPlaceClick(place.id) },
                    )
                }
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
private fun rememberPlaceDetailTopBarCollapsedState(listState: LazyListState): State<Boolean> =
    remember(listState) {
        derivedStateOf {
            val titleItemInfo =
                listState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                    itemInfo.key == PLACE_DETAIL_TITLE_ITEM_KEY
                }

            if (titleItemInfo == null) {
                listState.firstVisibleItemIndex > 1
            } else {
                titleItemInfo.offset <= 90
            }
        }
    }
