package com.bigong.oguri.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.InfoBlue
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_ad_activity_title
import oguri.composeapp.generated.resources.home_ad_activity_title_highlight
import oguri.composeapp.generated.resources.home_ad_hotel_title
import oguri.composeapp.generated.resources.home_ad_hotel_title_highlight
import oguri.composeapp.generated.resources.home_ad_plane_title
import oguri.composeapp.generated.resources.home_ad_plane_title_highlight
import oguri.composeapp.generated.resources.ic_advertisement
import oguri.composeapp.generated.resources.img_advertisement_activity_1
import oguri.composeapp.generated.resources.img_advertisement_activity_2
import oguri.composeapp.generated.resources.img_advertisement_activity_3
import oguri.composeapp.generated.resources.img_advertisement_hotel_1
import oguri.composeapp.generated.resources.img_advertisement_hotel_2
import oguri.composeapp.generated.resources.img_advertisement_plane_1
import oguri.composeapp.generated.resources.img_advertisement_plane_2
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdvertisementCard(
    advertisement: Advertisement,
    modifier: Modifier = Modifier,
    titleText: String? = null,
    highlightedText: String? = null,
    highlightedColor: Color? = null,
    onClick: (String) -> Unit = {},
) {
    val imageResources = advertisementImageResources(advertisement = advertisement)
    val stableImageIndex = advertisementStableImageIndex(advertisement = advertisement, candidateSize = imageResources.size)
    val imageResource = imageResources.getOrNull(stableImageIndex)
    val resolvedTitleText = titleText ?: advertisementDefaultTitleText(advertisement = advertisement)
    val resolvedHighlightedText = highlightedText ?: advertisementDefaultHighlightedText(advertisement = advertisement)
    val resolvedHighlightColor = highlightedColor ?: advertisementDefaultHighlightColor(advertisement = advertisement)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 8.dp))
                .noRippleClickable(onClick = { onClick(advertisement.url) })
                .background(
                    color = Mint10,
                    shape = RoundedCornerShape(size = 8.dp),
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height = 128.dp),
        ) {
            if (imageResource != null) {
                Image(
                    painter = painterResource(imageResource),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 8.dp,
                                    topEnd = 8.dp,
                                ),
                            ),
                )
            }
            Image(
                painter = painterResource(Res.drawable.ic_advertisement),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.padding(top = 4.dp, end = 6.dp).align(Alignment.TopEnd),
            )
        }
        Text(
            text =
                resolvedTitleText.getStyledText(
                    style = TextStyle(color = resolvedHighlightColor, fontWeight = FontWeight.Bold),
                    resolvedHighlightedText,
                ),
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun advertisementDefaultTitleText(advertisement: Advertisement): String =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> stringResource(Res.string.home_ad_hotel_title)
        AdvertisementPlatform.SKYSCANNER -> stringResource(Res.string.home_ad_plane_title)
        AdvertisementPlatform.KLOOK -> stringResource(Res.string.home_ad_activity_title)
        AdvertisementPlatform.UNKNOWN -> ""
    }

@Composable
private fun advertisementDefaultHighlightedText(advertisement: Advertisement): String =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> stringResource(Res.string.home_ad_hotel_title_highlight)
        AdvertisementPlatform.SKYSCANNER -> stringResource(Res.string.home_ad_plane_title_highlight)
        AdvertisementPlatform.KLOOK -> stringResource(Res.string.home_ad_activity_title_highlight)
        AdvertisementPlatform.UNKNOWN -> ""
    }

@Composable
private fun advertisementImageResources(advertisement: Advertisement): List<DrawableResource> =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> {
            listOf(
                Res.drawable.img_advertisement_hotel_1,
                Res.drawable.img_advertisement_hotel_2,
            )
        }

        AdvertisementPlatform.SKYSCANNER -> {
            listOf(
                Res.drawable.img_advertisement_plane_1,
                Res.drawable.img_advertisement_plane_2,
            )
        }

        AdvertisementPlatform.KLOOK -> {
            listOf(
                Res.drawable.img_advertisement_activity_1,
                Res.drawable.img_advertisement_activity_2,
                Res.drawable.img_advertisement_activity_3,
            )
        }

        AdvertisementPlatform.UNKNOWN -> {
            listOf()
        }
    }

private fun advertisementDefaultHighlightColor(advertisement: Advertisement): Color =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> InfoBlue
        AdvertisementPlatform.SKYSCANNER -> Mint70
        AdvertisementPlatform.KLOOK -> Orange50
        AdvertisementPlatform.UNKNOWN -> Neutral100
    }

private fun advertisementStableImageIndex(
    advertisement: Advertisement,
    candidateSize: Int,
): Int {
    if (candidateSize <= 1) {
        return 0
    }
    val stableHash = "${advertisement.platform.name}:${advertisement.url}".hashCode()
    return ((stableHash % candidateSize) + candidateSize) % candidateSize
}
