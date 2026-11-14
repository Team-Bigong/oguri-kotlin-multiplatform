package com.bigong.oguri.feature.placedetail.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint40
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral60
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.toCurrencyDisplayName
import com.bigong.oguri.domain.model.PlaceDetailTravelInformation
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.place_detail_information_cost_index
import oguri.composeapp.generated.resources.place_detail_information_cost_index_source
import oguri.composeapp.generated.resources.place_detail_information_exchange_rate
import oguri.composeapp.generated.resources.place_detail_information_recommend_period
import oguri.composeapp.generated.resources.place_detail_information_recommend_period_highlight
import oguri.composeapp.generated.resources.place_detail_information_weather
import oguri.composeapp.generated.resources.place_detail_precipitation_clear
import oguri.composeapp.generated.resources.place_detail_precipitation_frequent_rain
import oguri.composeapp.generated.resources.place_detail_precipitation_heavy_rain
import oguri.composeapp.generated.resources.place_detail_precipitation_mostly_clear
import oguri.composeapp.generated.resources.place_detail_precipitation_often_rainy_season
import oguri.composeapp.generated.resources.place_detail_precipitation_sometimes_rain
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

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
    val recommendPeriodHighlightedText =
        stringResource(
            Res.string.place_detail_information_recommend_period_highlight,
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
                .border(width = 1.dp, color = Mint40, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TravelInformationRow(
            text = recommendPeriodText,
            highlightedText = recommendPeriodHighlightedText,
        )

        TravelInformationRow(
            text = weatherText,
            highlightedText = "${travelInformation.averageTemperature}°C",
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
    highlightedText: String? = null,
) {
    val bodyStyle = OguriTheme.typography.bodyMedium
    val supportingStyle = OguriTheme.typography.labelSmall
    val highlightedStyle = SpanStyle(fontWeight = FontWeight.SemiBold)

    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 8.dp).size(6.dp).background(Mint70, CircleShape))
        Text(
            text =
                buildAnnotatedString {
                    withStyle(style = ParagraphStyle(lineHeight = bodyStyle.lineHeight)) {
                        withStyle(style = bodyStyle.toSpanStyle()) {
                            appendWithHighlight(
                                text = text,
                                highlightedText = highlightedText,
                                highlightedStyle = highlightedStyle,
                            )
                        }
                        if (supportingText != null) {
                            append(" ")
                            withStyle(
                                style =
                                    supportingStyle
                                        .toSpanStyle()
                                        .merge(SpanStyle(color = Neutral60)),
                            ) {
                                append("(")
                                append(supportingText)
                                append(")")
                            }
                        }
                    }
                },
            style = bodyStyle,
            color = Neutral100,
        )
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendWithHighlight(
    text: String,
    highlightedText: String?,
    highlightedStyle: SpanStyle,
) {
    if (highlightedText.isNullOrEmpty()) {
        append(text)
        return
    }

    val highlightedStartIndex = text.indexOf(string = highlightedText)
    if (highlightedStartIndex == -1) {
        append(text)
        return
    }

    val highlightedEndIndex = highlightedStartIndex + highlightedText.length
    append(text.substring(startIndex = 0, endIndex = highlightedStartIndex))
    withStyle(style = highlightedStyle) {
        append(text.substring(startIndex = highlightedStartIndex, endIndex = highlightedEndIndex))
    }
    append(text.substring(startIndex = highlightedEndIndex))
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
