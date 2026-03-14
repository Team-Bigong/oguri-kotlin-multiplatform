package com.bigong.oguri.feature.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bigong.oguri.core.designsystem.KakaoYellow
import com.bigong.oguri.core.designsystem.Neutral100
import com.bigong.oguri.core.designsystem.Neutral5
import com.bigong.oguri.core.designsystem.Neutral50
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.core.util.extension.noRippleClickable
import com.bigong.oguri.feature.login.ui.component.LoginActionButton
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_app_text
import oguri.composeapp.generated.resources.ic_kakao_login
import oguri.composeapp.generated.resources.img_oguri_walking
import oguri.composeapp.generated.resources.login_guest_browse
import oguri.composeapp.generated.resources.login_kakao_with_account
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun LoginScreen(
    onKakaoLoginClick: () -> Unit,
    onAppleLoginClick: () -> Unit,
    onGuestBrowseClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(color = Neutral5)
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.img_oguri_walking),
            contentDescription = null,
            modifier = Modifier.size(size = 180.dp),
        )
        Image(
            painter = painterResource(resource = Res.drawable.ic_app_text),
            contentDescription = null,
            modifier = Modifier.size(width = 110.dp, height = 56.dp),
        )
        Spacer(modifier = Modifier.height(height = 32.dp))

        LoginActionButton(
            iconResource = Res.drawable.ic_kakao_login,
            titleText = stringResource(Res.string.login_kakao_with_account),
            backgroundColor = KakaoYellow,
            contentColor = Neutral100,
            onClick = onKakaoLoginClick,
        )

        Spacer(modifier = Modifier.height(height = 14.dp))
        Text(
            text = stringResource(Res.string.login_guest_browse),
            style = OguriTheme.typography.bodySmall,
            color = Neutral50,
            modifier = Modifier.noRippleClickable(onClick = onGuestBrowseClick),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenAndroidPreview() {
    OguriTheme {
        LoginScreen(
            onKakaoLoginClick = {},
            onAppleLoginClick = {},
            onGuestBrowseClick = {},
        )
    }
}
