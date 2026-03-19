package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun OnboardingTermsRowCheckedPreview() {
    OguriTheme {
        OnboardingTermsRow(
            text = "서비스 이용약관 (필수)",
            isChecked = true,
            onToggle = {},
            onOpenDetail = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingTermsRowUncheckedPreview() {
    OguriTheme {
        OnboardingTermsRow(
            text = "개인정보 처리방침 (필수)",
            isChecked = false,
            onToggle = {},
            onOpenDetail = {},
        )
    }
}
