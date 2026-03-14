package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun MyPageMenuSectionPreview() {
    OguriTheme {
        MyPageMenuSection(
            menuItems =
                listOf(
                    MyPageMenuItem(label = "건의하기", onClick = {}),
                    MyPageMenuItem(label = "서비스 이용약관", onClick = {}),
                    MyPageMenuItem(label = "개인정보 처리방침", onClick = {}),
                    MyPageMenuItem(label = "회원 탈퇴", onClick = {}),
                    MyPageMenuItem(label = "로그아웃", onClick = {}),
                ),
        )
    }
}
