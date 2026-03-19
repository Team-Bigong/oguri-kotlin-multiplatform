package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.Mint70
import com.bigong.oguri.core.designsystem.Neutral0
import com.bigong.oguri.core.designsystem.Neutral40
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.img_oguri_hello
import oguri.composeapp.generated.resources.login_required_button
import oguri.composeapp.generated.resources.mypage_guest_login_message
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyPageGuestSection(
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.img_oguri_hello),
            contentDescription = null,
            modifier = Modifier.size(128.dp),
        )
        Text(
            text = stringResource(Res.string.mypage_guest_login_message),
            style = OguriTheme.typography.bodyMedium,
            color = Neutral40,
            modifier = Modifier.padding(top = 14.dp),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .background(color = Mint70, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .noRippleClickable(onClick = onLoginClick)
                    .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.login_required_button),
                style = OguriTheme.typography.cardTitle,
                color = Neutral0,
            )
        }
    }
}
