package com.bigong.oguri.feature.calendar.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint5
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral70
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.CalendarPeriod
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.calendar_month_day
import oguri.composeapp.generated.resources.calendar_period_range
import oguri.composeapp.generated.resources.ic_right_arrow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val RECOMMENDATION_CARD_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun CalendarRecommendationCard(
    period: CalendarPeriod,
    isSelected: Boolean,
    onClick: (CalendarPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startDateText = stringResource(Res.string.calendar_month_day, period.startDate.month.ordinal + 1, period.startDate.day)
    val endDateText = stringResource(Res.string.calendar_month_day, period.endDate.month.ordinal + 1, period.endDate.day)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = if (isSelected) Mint70 else Mint5,
                    shape = RECOMMENDATION_CARD_SHAPE,
                ).border(
                    width = 1.dp,
                    color = Mint70,
                    shape = RECOMMENDATION_CARD_SHAPE,
                ).noRippleClickable(onClick = { onClick(period) })
                .padding(start = 20.dp, end = 12.dp)
                .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.calendar_period_range, startDateText, endDateText),
            style = OguriTheme.typography.cardSubtitle,
            color = if (isSelected) Neutral0 else Neutral70,
        )
        Image(
            painter = painterResource(resource = Res.drawable.ic_right_arrow),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Neutral0),
            modifier = Modifier.size(24.dp),
        )
    }
}
