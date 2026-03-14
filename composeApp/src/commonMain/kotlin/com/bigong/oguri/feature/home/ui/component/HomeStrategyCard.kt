package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint10
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.ui.component.SaveToggleButton
import com.bigong.oguri.core.util.extension.getStyledText
import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_strategy_day_off_hint
import oguri.composeapp.generated.resources.home_strategy_holiday_with
import oguri.composeapp.generated.resources.home_strategy_period
import oguri.composeapp.generated.resources.img_oguri_teacher
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeStrategyCard(
    selectedRank: Int,
    savedRankSet: Set<Int>,
    rankLabels: List<String>,
    currentPeriod: RecommendPeriod,
    onRankSelected: (Int) -> Unit,
    onSavedChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Mint10, shape = RoundedCornerShape(size = 12.dp)),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            RankSegmentedToggle(
                selectedRank = selectedRank,
                onRankSelected = onRankSelected,
                rankLabels = rankLabels,
                modifier = Modifier.weight(weight = 1f),
            )
            Spacer(modifier = Modifier.weight(weight = 1f))
            SaveToggleButton(
                checked = selectedRank in savedRankSet,
                onCheckedChange = onSavedChanged,
            )
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.align(alignment = Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text =
                        stringResource(
                            Res.string.home_strategy_period,
                            formatMonthDay(localDate = currentPeriod.startDate),
                            formatMonthDay(localDate = currentPeriod.endDate),
                        ),
                    style = OguriTheme.typography.heroTitle,
                    color = Mint70,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text =
                        stringResource(
                            Res.string.home_strategy_holiday_with,
                            currentPeriod.holiday.joinToString(separator = " · "),
                        ).getStyledText(
                            style =
                                TextStyle(
                                    color = Mint70,
                                    fontWeight = FontWeight.Bold,
                                ),
                            currentPeriod.holiday.joinToString(separator = " · "),
                        ),
                    style = OguriTheme.typography.cardSubtitle,
                    color = Neutral90,
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
                )
                Spacer(modifier = Modifier.height(24.dp))
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
    val dateTokens: List<String> = localDate.toString().split("-")
    val month: Int = dateTokens[1].toInt()
    val day: Int = dateTokens[2].toInt()
    return "${month}월 ${day}일"
}
