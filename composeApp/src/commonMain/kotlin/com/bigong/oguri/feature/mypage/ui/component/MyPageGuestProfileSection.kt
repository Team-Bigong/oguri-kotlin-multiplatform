package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral90
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.img_profile
import org.jetbrains.compose.resources.painterResource

@Composable
fun MyPageGuestProfileSection(
    guestName: String,
    modifier: Modifier = Modifier,
) {
    val guestNamePrefix = "게스트"
    val guestNameSuffix = "님"
    val styledGuestNameText =
        buildAnnotatedString {
            append(guestName)
            if (guestName.startsWith(prefix = guestNamePrefix)) {
                addStyle(
                    style = OguriTheme.typography.cardTitle.copy(color = Mint70).toSpanStyle(),
                    start = 0,
                    end = guestNamePrefix.length,
                )
            }
            if (guestName.endsWith(suffix = guestNameSuffix)) {
                val suffixStartIndex = guestName.length - guestNameSuffix.length
                addStyle(
                    style = OguriTheme.typography.cardTitle.copy(color = Neutral90).toSpanStyle(),
                    start = suffixStartIndex,
                    end = guestName.length,
                )
            }
        }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Image(
            painter = painterResource(Res.drawable.img_profile),
            contentDescription = null,
        )
        Text(
            text = styledGuestNameText,
            style = OguriTheme.typography.cardTitle,
            color = Neutral90,
        )
    }
}
