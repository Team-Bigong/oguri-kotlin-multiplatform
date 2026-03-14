package com.bigong.oguri.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_alert

@Preview(showBackground = true)
@Composable
private fun ConfirmAlertDialogPreview() {
    OguriTheme {
        ConfirmAlertDialog(
            iconResource = Res.drawable.ic_alert,
            titleText = "여행지를 삭제할까요?",
            messageText = "삭제된 정보는 복구되지 않습니다.",
            confirmButtonText = "확인",
            cancelButtonText = "취소",
            onDismissRequest = {},
            onConfirmClick = {},
            onCancelClick = {},
        )
    }
}
