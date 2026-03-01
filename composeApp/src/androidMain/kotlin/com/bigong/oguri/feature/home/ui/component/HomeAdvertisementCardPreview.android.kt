package com.bigong.oguri.feature.home.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.Advertisement
import com.bigong.oguri.domain.model.AdvertisementPlatform

@Preview(showBackground = true)
@Composable
private fun HomeAdvertisementCardPreview() {
    OguriTheme {
        HomeAdvertisementCard(
            advertisement = Advertisement(
                platform = AdvertisementPlatform.AGODA,
                url = "https://www.agoda.com/",
            ),
        )
    }
}
