package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 120)
@Composable
private fun MyPageGuestProfileSectionPreview() {
    OguriTheme {
        MyPageGuestProfileSection(
            guestName = "게스트님",
        )
    }
}
