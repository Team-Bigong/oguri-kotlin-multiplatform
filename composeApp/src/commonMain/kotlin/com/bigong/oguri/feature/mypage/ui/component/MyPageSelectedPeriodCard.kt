package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral20
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.Neutral60
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.Orange50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_trashcan
import oguri.composeapp.generated.resources.mypage_selected_period_date_range
import oguri.composeapp.generated.resources.mypage_selected_period_information
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val PERIOD_CARD_SHAPE = RoundedCornerShape(size = 8.dp)

@Composable
fun MyPageSelectedPeriodCard(
    period: MyPageSelectedPeriod,
    isCurrentYear: Boolean,
    onDeleteClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val yearColor = if (isCurrentYear) Mint70 else Orange50

    Column(
        modifier =
            modifier
                .width(170.dp)
                .background(color = Neutral0, shape = PERIOD_CARD_SHAPE)
                .border(width = 1.dp, color = Neutral20, shape = PERIOD_CARD_SHAPE)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = period.startDate.year.toString(),
                    style = OguriTheme.typography.labelMedium,
                    color = yearColor,
                )
                Text(
                    text =
                        stringResource(
                            Res.string.mypage_selected_period_date_range,
                            period.startDate.month.ordinal + 1,
                            period.startDate.day,
                            period.endDate.month.ordinal + 1,
                            period.endDate.day,
                        ),
                    style = OguriTheme.typography.bodyLarge,
                    color = Neutral90,
                )
            }
            Image(
                painter = painterResource(Res.drawable.ic_trashcan),
                contentDescription = null,
                modifier = Modifier.noRippleClickable(onClick = { onDeleteClick(period.id) }),
            )
        }
        Spacer(modifier = Modifier.padding(top = 8.dp))
        Text(
            text = stringResource(Res.string.mypage_selected_period_information, period.totalTripCount, period.dayOffCount),
            style = OguriTheme.typography.bodyMedium,
            color = Neutral60,
        )
    }
}
