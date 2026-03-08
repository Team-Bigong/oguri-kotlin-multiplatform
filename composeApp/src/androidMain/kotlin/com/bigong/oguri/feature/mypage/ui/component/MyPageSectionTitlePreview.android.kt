package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import oguri.composeapp.generated.resources.Res
import oguri.composeapp.generated.resources.ic_diary

@Preview(showBackground = true)
@Composable
private fun MyPageSectionTitlePreview() {
    OguriTheme {
        MyPageSectionTitle(
            iconResource = Res.drawable.ic_diary,
            titleText = "내가 고른 휴가 일정",
        )
    }
}
