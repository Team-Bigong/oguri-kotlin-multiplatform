package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.InfoBlue
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.ui.component.NetworkImage
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
import oguri.composeapp.generated.resources.url_activity_1
import oguri.composeapp.generated.resources.url_hotel_1
import oguri.composeapp.generated.resources.url_plane_1
import org.jetbrains.compose.resources.stringResource

private val HOME_ADVERTISEMENT_CARD_CORNER_RADIUS = 8.dp

@Composable
fun HomeAdvertisementCard(
    advertisement: Advertisement,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit = {},
) {
    val imageUrl: String = advertisementImageUrl(advertisement = advertisement)
    val titleText: String = advertisementTitleText(advertisement = advertisement)
    val highlightedText: String = advertisementHighlightedText(advertisement = advertisement)
    val highlightedColor: Color = advertisementHighlightColor(advertisement = advertisement)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = HOME_ADVERTISEMENT_CARD_CORNER_RADIUS))
                .noRippleClickable(onClick = { onClick(advertisement.url) })
                .background(
                    color = Mint10,
                    shape = RoundedCornerShape(size = HOME_ADVERTISEMENT_CARD_CORNER_RADIUS),
                ),
    ) {
        NetworkImage(
            imageUrl = imageUrl,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height = 128.dp),
        )
        Text(
            text =
                titleText.getStyledText(
                    style = TextStyle(color = highlightedColor, fontWeight = FontWeight.Bold),
                    highlightedText,
                ),
            style = OguriTheme.typography.cardSubtitle,
            color = Neutral100,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
    }
}

@Composable
private fun advertisementTitleText(advertisement: Advertisement): String =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> stringResource(Res.string.home_ad_hotel_title)
        AdvertisementPlatform.SKYSCANNER -> stringResource(Res.string.home_ad_plane_title)
        AdvertisementPlatform.KLOOK -> stringResource(Res.string.home_ad_activity_title)
        AdvertisementPlatform.UNKNOWN -> ""
    }

@Composable
private fun advertisementHighlightedText(advertisement: Advertisement): String =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> stringResource(Res.string.home_ad_hotel_title_highlight)
        AdvertisementPlatform.SKYSCANNER -> stringResource(Res.string.home_ad_plane_title_highlight)
        AdvertisementPlatform.KLOOK -> stringResource(Res.string.home_ad_activity_title_highlight)
        AdvertisementPlatform.UNKNOWN -> ""
    }

@Composable
private fun advertisementImageUrl(advertisement: Advertisement): String =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> stringResource(Res.string.url_hotel_1)
        AdvertisementPlatform.SKYSCANNER -> stringResource(Res.string.url_plane_1)
        AdvertisementPlatform.KLOOK -> stringResource(Res.string.url_activity_1)
        AdvertisementPlatform.UNKNOWN -> ""
    }

private fun advertisementHighlightColor(advertisement: Advertisement): Color =
    when (advertisement.platform) {
        AdvertisementPlatform.AGODA -> InfoBlue
        AdvertisementPlatform.SKYSCANNER -> Mint70
        AdvertisementPlatform.KLOOK -> Orange50
        AdvertisementPlatform.UNKNOWN -> Neutral100
    }
