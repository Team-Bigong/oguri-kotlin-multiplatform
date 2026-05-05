package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint60
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.MonthlyTopPeriod
import kotlinx.datetime.LocalDate
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_monthly_top_period_date
import oguri.composeapp.generated.resources.home_monthly_top_period_summary
import oguri.composeapp.generated.resources.ic_period_medal_first
import oguri.composeapp.generated.resources.ic_period_medal_second
import oguri.composeapp.generated.resources.ic_period_medal_third
import oguri.composeapp.generated.resources.ic_right_arrow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MonthlyTopPeriodCard(
    period: MonthlyTopPeriod,
    onClick: (MonthlyTopPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(size = 8.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = cardShape)
                .background(color = Neutral0, shape = cardShape)
                .border(width = 1.dp, color = Mint70, shape = cardShape)
                .noRippleClickable(onClick = { onClick(period) })
                .padding(start = 20.dp)
                .padding(top = 18.dp, bottom = 18.dp),
    ) {
        RankMedal(rank = period.rank)
        Spacer(modifier = Modifier.width(width = 18.dp))
        Column(
            modifier = Modifier.weight(weight = 1f),
        ) {
            Text(
                text =
                    stringResource(
                        Res.string.home_monthly_top_period_date,
                        period.startDate.toMonthDayText(),
                        period.endDate.toMonthDayText(),
                        period.totalTripCount,
                    ),
                style = OguriTheme.typography.cardSubtitle,
                color = Mint70,
            )
            Spacer(modifier = Modifier.height(height = 8.dp))
            Text(
                text =
                    stringResource(
                        Res.string.home_monthly_top_period_summary,
                        period.holidayCount,
                        period.dayOffCount,
                    ),
                style = OguriTheme.typography.labelSmall,
                color = Neutral50,
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(size = 48.dp)
                    .padding(all = 12.dp),
        ) {
            Image(
                painter = painterResource(resource = Res.drawable.ic_right_arrow),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Neutral100),
                modifier = Modifier.size(size = 24.dp),
            )
        }
    }
}

@Composable
private fun RankMedal(rank: Int) {
    val medalResource = rank.toMedalResource()
    val medalHeight = if (rank == FIRST_RANK) 32.dp else 24.dp

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier =
            Modifier
                .width(width = 24.dp)
                .height(height = medalHeight),
    ) {
        Image(
            painter = painterResource(resource = medalResource),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Mint60),
            modifier =
                Modifier
                    .width(width = 24.dp)
                    .height(height = medalHeight),
        )
        Text(
            text = rank.toString(),
            style = OguriTheme.typography.labelLarge,
            color = Neutral0,
            textAlign = TextAlign.Center,
            modifier =
                Modifier
                    .size(size = 24.dp),
        )
    }
}

private fun Int.toMedalResource(): DrawableResource =
    when (this) {
        FIRST_RANK -> Res.drawable.ic_period_medal_first
        SECOND_RANK -> Res.drawable.ic_period_medal_second
        else -> Res.drawable.ic_period_medal_third
    }

private fun LocalDate.toMonthDayText(): String = "${month.ordinal + 1}월 ${day}일"

private const val FIRST_RANK = 1
private const val SECOND_RANK = 2
