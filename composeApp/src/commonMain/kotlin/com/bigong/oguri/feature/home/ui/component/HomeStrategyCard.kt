package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.ui.component.SaveToggleButton
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_strategy_day_off_hint
import oguri.composeapp.generated.resources.home_strategy_holiday_with
import oguri.composeapp.generated.resources.home_strategy_holiday_with_next_year
import oguri.composeapp.generated.resources.home_strategy_period
import oguri.composeapp.generated.resources.img_oguri_teacher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun HomeStrategyCard(
    selectedRank: Int,
    savedRankSet: Set<Int>,
    rankLabels: List<String>,
    currentPeriod: RecommendPeriod,
    onRankSelected: (Int) -> Unit,
    onSavedChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentYear =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
    val isOutOfCurrentYear = currentPeriod.startDate.year != currentYear || currentPeriod.endDate.year != currentYear
    val periodTextColor = if (isOutOfCurrentYear) Orange50 else Mint70
    val holidayHighlightColor = if (isOutOfCurrentYear) Orange50 else Mint70
    val holidayNamesText = currentPeriod.holiday.joinToString(separator = " · ")
    val holidayHighlightText =
        if (isOutOfCurrentYear) {
            stringResource(
                Res.string.home_strategy_holiday_with_next_year,
                holidayNamesText,
            )
        } else {
            holidayNamesText
        }
    val holidayDescriptionText =
        stringResource(
            Res.string.home_strategy_holiday_with,
            holidayHighlightText,
        )

    Box(
        modifier =
            modifier
                .noRippleClickable(onClick = onClick)
                .fillMaxWidth()
                .background(color = Mint10, shape = RoundedCornerShape(size = 12.dp)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .align(alignment = Alignment.TopStart)
                        .padding(horizontal = 12.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                RankSegmentedToggle(
                    selectedRank = selectedRank,
                    onRankSelected = onRankSelected,
                    rankLabels = rankLabels,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                stringResource(
                                    Res.string.home_strategy_period,
                                    formatMonthDay(localDate = currentPeriod.startDate),
                                    formatMonthDay(localDate = currentPeriod.endDate),
                                ),
                            style = OguriTheme.typography.heroTitle,
                            color = periodTextColor,
                        )
                        Spacer(modifier = Modifier.weight(weight = 1f))
                        SaveToggleButton(
                            checked = selectedRank in savedRankSet,
                            onCheckedChange = onSavedChanged,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.padding(end = 86.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(
                            text =
                                holidayDescriptionText.getStyledText(
                                    style =
                                        TextStyle(
                                            color = holidayHighlightColor,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                    holidayHighlightText,
                                ),
                            style = OguriTheme.typography.cardSubtitle,
                            color = Neutral90,
                            textAlign = TextAlign.Start,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text =
                                stringResource(
                                    Res.string.home_strategy_day_off_hint,
                                    currentPeriod.dayOffCount,
                                    currentPeriod.totalTripCount,
                                ),
                            style = OguriTheme.typography.caption,
                            color = Neutral50,
                            textAlign = TextAlign.Start,
                        )
                    }
                }
            }

            Image(
                painter = painterResource(Res.drawable.img_oguri_teacher),
                contentDescription = null,
                modifier =
                    Modifier
                        .offset(x = 6.dp, y = 12.dp)
                        .width(74.dp)
                        .align(alignment = Alignment.BottomEnd),
            )
        }
    }
}

private fun formatMonthDay(localDate: LocalDate): String {
    val dateTokens = localDate.toString().split("-")
    val month = dateTokens[1].toInt()
    val day = dateTokens[2].toInt()
    return "${month}월 ${day}일"
}
