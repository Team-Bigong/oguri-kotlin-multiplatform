package com.bigong.oguri.feature.home.ui.component

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint60
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.MonthlyTopPeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.home_monthly_top_period_date
import oguri.composeapp.generated.resources.home_monthly_top_period_summary
import oguri.composeapp.generated.resources.ic_right_arrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun MonthlyTopPeriodCard(
    period: MonthlyTopPeriod,
    onClick: (MonthlyTopPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(size = 8.dp)
    val currentYear =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
    val periodDateColor =
        if (period.startDate.year > currentYear || period.endDate.year > currentYear) {
            Orange50
        } else {
            Mint70
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Neutral0, shape = cardShape)
                .border(width = 1.dp, color = Mint70, shape = cardShape)
                .noRippleClickable(onClick = { onClick(period) })
                .padding(start = 20.dp)
                .padding(vertical = 14.dp),
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
                color = periodDateColor,
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
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .width(width = 24.dp)
                .height(height = 48.dp),
    ) {
        if (rank == FIRST_RANK) {
            FirstRankMedal(rank = rank)
        } else {
            CircleRankMedal(rank = rank)
        }
    }
}

@Composable
private fun FirstRankMedal(rank: Int) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier =
            Modifier
                .width(width = 24.dp)
                .height(height = 32.dp),
    ) {
        Canvas(
            modifier =
                Modifier
                    .width(width = 12.dp)
                    .height(height = 14.dp)
                    .align(alignment = Alignment.BottomCenter),
        ) {
            val ribbonPath =
                Path().apply {
                    moveTo(x = 0f, y = 0f)
                    lineTo(x = size.width, y = 0f)
                    lineTo(x = size.width, y = size.height)
                    lineTo(x = size.width / 2f, y = size.height - FIRST_RANK_RIBBON_NOTCH_HEIGHT)
                    lineTo(x = 0f, y = size.height)
                    close()
                }
            drawPath(path = ribbonPath, color = Mint60)
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(size = 24.dp)
                    .background(color = Neutral0, shape = CircleShape)
                    .border(width = 2.dp, color = Mint60, shape = CircleShape)
                    .padding(all = 4.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(size = 16.dp)
                        .background(color = Mint60, shape = CircleShape),
            ) {
                Text(
                    text = rank.toString(),
                    style = OguriTheme.typography.labelMedium,
                    color = Neutral0,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun CircleRankMedal(rank: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(size = 24.dp)
                .background(color = Mint60, shape = CircleShape),
    ) {
        Text(
            text = rank.toString(),
            style = OguriTheme.typography.labelMedium,
            color = Neutral0,
            textAlign = TextAlign.Center,
        )
    }
}

private fun LocalDate.toMonthDayText(): String = "${month.ordinal + 1}월 ${day}일"

private const val FIRST_RANK = 1
private const val FIRST_RANK_RIBBON_NOTCH_HEIGHT = 4f
