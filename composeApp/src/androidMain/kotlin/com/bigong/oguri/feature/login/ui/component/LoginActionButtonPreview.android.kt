package com.bigong.oguri.feature.login.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_kakao_login

@Preview(showBackground = true)
@Composable
private fun LoginActionButtonPreview() {
    OguriTheme {
        LoginActionButton(
            iconResource = Res.drawable.ic_kakao_login,
            titleText = "카카오 계정으로 로그인",
            backgroundColor = Color(0xFFFBE100),
            contentColor = Color(0xFF191919),
            onClick = {},
        )
    }
}
