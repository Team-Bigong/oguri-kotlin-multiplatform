package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral60
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_pen
import oguri.composeapp.generated.resources.img_profile
import oguri.composeapp.generated.resources.mypage_profile_leave_days
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageProfileSection(
    nickname: String,
    remainingLeaveDays: Int,
    preferredLeaveDays: Int,
    onEditLeaveDaysClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.img_profile),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            val profileNameText =
                if (nickname.endsWith(suffix = NAME_SUFFIX)) {
                    nickname
                } else {
                    nickname + NAME_SUFFIX
                }
            Text(
                text = profileNameText,
                style = OguriTheme.typography.cardTitle,
                color = Mint70,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.mypage_profile_leave_days, remainingLeaveDays, preferredLeaveDays),
                    style = OguriTheme.typography.bodySmall,
                    color = Neutral60,
                )
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .noRippleClickable(onClick = onEditLeaveDaysClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_pen),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Neutral60),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

private const val NAME_SUFFIX = "님"
