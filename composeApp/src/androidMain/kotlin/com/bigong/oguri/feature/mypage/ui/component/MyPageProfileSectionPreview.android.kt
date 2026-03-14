package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun MyPageProfileSectionPreview() {
    OguriTheme {
        MyPageProfileSection(
            nickname = "똘똘한 모험가",
            remainingLeaveDays = 15,
            preferredLeaveDays = 3,
            onEditLeaveDaysClick = {},
            modifier = Modifier,
        )
    }
}
