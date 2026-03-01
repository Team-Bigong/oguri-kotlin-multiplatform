package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.getColoredText
import com.bigong.oguri.core.ui.component.SaveToggleButton
import com.bigong.oguri.domain.model.RecommendPeriod
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_strategy_day_off_hint
import oguri.composeapp.generated.resources.home_strategy_holiday_with
import oguri.composeapp.generated.resources.home_strategy_period
import oguri.composeapp.generated.resources.img_oguri_parasol
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFD6E2E2), shape = RoundedCornerShape(size = 12.dp))
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
    ) {
        RankSegmentedToggle(
            selectedRank = selectedRank,
            onRankSelected = onRankSelected,
            rankLabels = rankLabels,
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.align(alignment = Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(
                        Res.string.home_strategy_period,
                        formatMonthDay(localDate = currentPeriod.startDate),
                        formatMonthDay(localDate = currentPeriod.endDate),
                    ),
                    style = OguriTheme.typography.heroTitle,
                    color = Color(0xFF43B9A8),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(
                        Res.string.home_strategy_holiday_with,
                        currentPeriod.holiday.joinToString(separator = " · "),
                    ).getColoredText(
                        color = Color(0xFF43B9A8),
                        currentPeriod.holiday.joinToString(separator = " · "),
                    ),
                    style = OguriTheme.typography.cardTitle,
                    color = Color(0xFF1F3B40),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        Res.string.home_strategy_day_off_hint,
                        currentPeriod.dayOffCount,
                        currentPeriod.totalTripCount,
                    ),
                    style = OguriTheme.typography.bodyMedium,
                    color = Color(0xFF8B98A6),
                )
            }

            SaveToggleButton(
                checked = selectedRank in savedRankSet,
                onCheckedChange = onSavedChanged,
                modifier = Modifier.align(alignment = Alignment.TopEnd),
            )

            Image(
                painter = painterResource(Res.drawable.img_oguri_parasol),
                contentDescription = null,
                modifier = Modifier
                    .size(size = 84.dp)
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
