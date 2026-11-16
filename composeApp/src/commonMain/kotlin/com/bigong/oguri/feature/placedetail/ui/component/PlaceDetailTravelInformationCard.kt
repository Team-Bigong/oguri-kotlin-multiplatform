package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint50
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.util.extension.toCurrencyDisplayName
import com.bigong.oguri.domain.model.PlaceDetailTravelInformation
import com.bigong.oguri.feature.travelplanner.ui.component.travelTextStyle
import kotlin.math.roundToInt
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.place_detail_information_cost_index
import oguri.composeapp.generated.resources.place_detail_information_cost_index_source
import oguri.composeapp.generated.resources.place_detail_information_exchange_rate
import oguri.composeapp.generated.resources.place_detail_information_recommend_period
import oguri.composeapp.generated.resources.place_detail_information_weather
import oguri.composeapp.generated.resources.place_detail_precipitation_clear
import oguri.composeapp.generated.resources.place_detail_precipitation_frequent_rain
import oguri.composeapp.generated.resources.place_detail_precipitation_heavy_rain
import oguri.composeapp.generated.resources.place_detail_precipitation_mostly_clear
import oguri.composeapp.generated.resources.place_detail_precipitation_often_rainy_season
import oguri.composeapp.generated.resources.place_detail_precipitation_sometimes_rain
import org.jetbrains.compose.resources.stringResource

private const val CLEAR_PRECIPITATION_MAXIMUM = 30.0
private const val MOSTLY_CLEAR_PRECIPITATION_MAXIMUM = 60.0
private const val SOMETIMES_RAIN_PRECIPITATION_MAXIMUM = 100.0
private const val FREQUENT_RAIN_PRECIPITATION_MAXIMUM = 200.0
private const val HEAVY_RAIN_PRECIPITATION_MAXIMUM = 300.0
private const val DECIMAL_SCALE = 100

@Composable
fun PlaceDetailTravelInformationCard(
    travelInformation: PlaceDetailTravelInformation,
    modifier: Modifier = Modifier,
) {
    val recommendPeriod = travelInformation.recommendPeriod
    val exchangeRateInformation = travelInformation.exchangeRateInformation
    val recommendPeriodText =
        stringResource(
            Res.string.place_detail_information_recommend_period,
            recommendPeriod.startMonth,
            recommendPeriod.endMonth,
        )
    val weatherText =
        stringResource(
            Res.string.place_detail_information_weather,
            travelInformation.averageTemperature,
            precipitationMessage(averagePrecipitation = travelInformation.averagePrecipitation),
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Mint5, shape = RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = Mint50, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TravelInformationRow(
            text = recommendPeriodText,
        )

        TravelInformationRow(
            text = weatherText,
        )

        TravelInformationRow(
            text =
                stringResource(
                    Res.string.place_detail_information_exchange_rate,
                    formatIntegerWithThousandsSeparator(value = exchangeRateInformation.koreanWonAmount),
                    exchangeRateInformation.currencyUnit,
                    exchangeRateInformation.currencyCode.toCurrencyDisplayName(),
                ),
            supportingText = formatIsoDate(date = exchangeRateInformation.date),
        )

        TravelInformationRow(
            text =
                stringResource(
                    Res.string.place_detail_information_cost_index,
                    formatDecimal(value = travelInformation.relativeCostIndex),
                ),
            supportingText = stringResource(Res.string.place_detail_information_cost_index_source),
        )
    }
}

@Composable
private fun TravelInformationRow(
    text: String,
    supportingText: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 22.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 8.dp).size(6.dp).background(Mint70, CircleShape))
        Text(
            text = if (supportingText == null) text else "$text ($supportingText)",
            style = travelTextStyle(14, 17),
            color = Neutral100,
        )
    }
}

@Composable
private fun precipitationMessage(averagePrecipitation: Double): String =
    when {
        averagePrecipitation < CLEAR_PRECIPITATION_MAXIMUM -> stringResource(Res.string.place_detail_precipitation_clear)
        averagePrecipitation < MOSTLY_CLEAR_PRECIPITATION_MAXIMUM -> stringResource(Res.string.place_detail_precipitation_mostly_clear)
        averagePrecipitation < SOMETIMES_RAIN_PRECIPITATION_MAXIMUM -> stringResource(Res.string.place_detail_precipitation_sometimes_rain)
        averagePrecipitation < FREQUENT_RAIN_PRECIPITATION_MAXIMUM -> stringResource(Res.string.place_detail_precipitation_frequent_rain)
        averagePrecipitation < HEAVY_RAIN_PRECIPITATION_MAXIMUM -> stringResource(Res.string.place_detail_precipitation_heavy_rain)
        else -> stringResource(Res.string.place_detail_precipitation_often_rainy_season)
    }

private fun formatIsoDate(date: String): String = date.replace(oldChar = '-', newChar = '.')

private fun formatDecimal(value: Double): String {
    val roundedValue = (value * DECIMAL_SCALE).roundToInt()
    val integerPart = roundedValue / DECIMAL_SCALE
    val fractionalPart = roundedValue % DECIMAL_SCALE
    val formattedFractionalPart = fractionalPart.toString().padStart(length = 2, padChar = '0').trimEnd('0')
    return if (formattedFractionalPart.isEmpty()) {
        integerPart.toString()
    } else {
        "$integerPart.$formattedFractionalPart"
    }
}

private fun formatIntegerWithThousandsSeparator(value: Int): String {
    val isNegative = value < 0
    val digits = value.toString().removePrefix("-")
    val reversedGroups =
        digits
            .reversed()
            .chunked(size = 3)
            .joinToString(separator = ",")
            .reversed()
    return if (isNegative) "-$reversedGroups" else reversedGroups
}
