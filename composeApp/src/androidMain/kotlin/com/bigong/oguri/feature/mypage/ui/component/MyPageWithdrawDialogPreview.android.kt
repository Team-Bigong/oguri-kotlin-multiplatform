package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MyPageWithdrawDialogPreview() {
    OguriTheme {
        val targetPhrase = "오구리의 회원을 탈퇴하겠습니다"
        var inputText by remember { mutableStateOf("") }
        MyPageWithdrawDialog(
            inputText = inputText,
            targetPhrase = targetPhrase,
            isConfirmEnabled = inputText == targetPhrase,
            onInputChange = { changedText -> inputText = changedText },
            onDismissRequest = {},
            onConfirmClick = {},
        )
    }
}
