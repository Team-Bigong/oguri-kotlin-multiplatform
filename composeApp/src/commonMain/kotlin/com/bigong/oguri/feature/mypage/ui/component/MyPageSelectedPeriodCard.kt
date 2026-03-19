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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral30
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_trashcan
import oguri.composeapp.generated.resources.mypage_selected_period_information
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageSelectedPeriodCard(
    period: MyPageSelectedPeriod,
    dDayText: String,
    dDayColor: Color,
    dateRangeText: String,
    onDeleteClick: (Long) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .noRippleClickable(onClick = onClick)
                .background(color = Neutral0, shape = RoundedCornerShape(8.dp))
                .border(width = 1.dp, color = Neutral30, shape = RoundedCornerShape(8.dp))
                .padding(start = 18.dp, end = 8.dp)
                .padding(vertical = 18.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = dDayText,
                        style = OguriTheme.typography.labelMedium,
                        color = dDayColor,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateRangeText,
                        style = OguriTheme.typography.bodyLarge,
                        color = Neutral90,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(Res.string.mypage_selected_period_information, period.totalTripCount, period.dayOffCount),
                style = OguriTheme.typography.bodyMedium,
                color = Neutral50,
            )
        }
        Spacer(modifier = Modifier.width(18.dp))
        Image(
            painter = painterResource(Res.drawable.ic_trashcan),
            contentDescription = null,
            modifier =
                Modifier
                    .noRippleClickable(onClick = { onDeleteClick(period.id) })
                    .size(36.dp)
                    .padding(8.dp),
        )
    }
}
