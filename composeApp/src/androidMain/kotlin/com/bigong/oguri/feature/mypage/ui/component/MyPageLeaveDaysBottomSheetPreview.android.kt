package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun MyPageLeaveDaysBottomSheetPreview() {
    OguriTheme {
        MyPageLeaveDaysBottomSheet(
            currentRemainingLeaveDays = 15,
            currentPreferredLeaveDays = 3,
            onDismissRequest = {},
            onSubmit = { _, _ -> },
        )
    }
}
