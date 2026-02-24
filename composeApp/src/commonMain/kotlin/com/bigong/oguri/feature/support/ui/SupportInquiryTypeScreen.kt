package com.bigong.oguri.feature.support.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bigong.oguri.feature.common.ui.PlaceholderActionButton
import com.bigong.oguri.feature.common.ui.PlaceholderScreenFrame
import com.bigong.oguri.feature.common.ui.PlaceholderSpacingSmall

@Composable
fun SupportInquiryTypeRoute(
    onFeatureSuggestionClick: () -> Unit,
    onBugReportClick: () -> Unit,
    onOtherInquiryClick: () -> Unit,
) {
    SupportInquiryTypeScreen(
        onFeatureSuggestionClick = onFeatureSuggestionClick,
        onBugReportClick = onBugReportClick,
        onOtherInquiryClick = onOtherInquiryClick,
    )
}

@Composable
fun SupportInquiryTypeScreen(
    onFeatureSuggestionClick: () -> Unit,
    onBugReportClick: () -> Unit,
    onOtherInquiryClick: () -> Unit,
) {
    PlaceholderScreenFrame(
        screenTitleText = "문의 유형 선택",
        screenSubtitleText = "이후 메일 앱 실행 이벤트로 연결",
    ) {
        PlaceholderActionButton(
            labelText = "기능 제안",
            onClick = onFeatureSuggestionClick,
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
        PlaceholderActionButton(
            labelText = "오류 신고",
            onClick = onBugReportClick,
            emphasized = false,
        )
        Spacer(modifier = Modifier.height(PlaceholderSpacingSmall))
        PlaceholderActionButton(
            labelText = "기타 문의",
            onClick = onOtherInquiryClick,
            emphasized = false,
        )
    }
}
