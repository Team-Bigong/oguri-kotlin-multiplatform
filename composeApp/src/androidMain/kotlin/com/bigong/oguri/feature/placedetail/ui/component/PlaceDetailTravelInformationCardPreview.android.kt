package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.PlaceDetailExchangeRateInformation
import com.bigong.oguri.domain.model.PlaceDetailRecommendPeriod
import com.bigong.oguri.domain.model.PlaceDetailTravelInformation

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun PlaceDetailTravelInformationCardPreview() {
    OguriTheme {
        PlaceDetailTravelInformationCard(
            travelInformation =
                PlaceDetailTravelInformation(
                    exchangeRateInformation =
                        PlaceDetailExchangeRateInformation(
                            koreanWonAmount = 2400,
                            currencyUnit = 1,
                            currencyCode = "USD",
                            date = "2026-04-08",
                        ),
                    relativeCostIndex = 2.46,
                    averageTemperature = 16,
                    averagePrecipitation = 70.5,
                    recommendPeriod =
                        PlaceDetailRecommendPeriod(
                            startMonth = 6,
                            endMonth = 8,
                        ),
                ),
        )
    }
}
