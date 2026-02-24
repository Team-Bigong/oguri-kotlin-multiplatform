package com.bigong.oguri.feature.mypage.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderInfoCard
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingMedium

@Composable
fun MyPageRoute(
    onSupportInquiryClick: () -> Unit,
) {
    MyPageScreen(onSupportInquiryClick = onSupportInquiryClick)
}

@Composable
fun MyPageScreen(
    onSupportInquiryClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "마이페이지",
        screenSubtitleText = "계정 / 전략 설정 / Pro / 지원",
    ) {
        PlaceholderInfoCard(
            lines = listOf(
                "남은 연차 수정",
                "근무 형태 변경",
                "Pro 시작하기",
                "건의하기",
            ),
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingMedium))
        PlaceholderActionButton(
            labelText = "문의 유형 선택으로 이동",
            onClick = onSupportInquiryClick,
            emphasized = false,
        )
    }
}
