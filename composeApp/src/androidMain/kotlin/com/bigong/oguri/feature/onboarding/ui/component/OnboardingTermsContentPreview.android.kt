package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true, widthDp = 360, heightDp = 420)
@Composable
private fun OnboardingTermsContentPreview() {
    OguriTheme {
        OnboardingTermsContent(
            isServiceTermsChecked = false,
            isPrivacyPolicyChecked = true,
            onServiceTermsToggle = {},
            onPrivacyPolicyToggle = {},
            onServiceTermsOpen = {},
            onPrivacyPolicyOpen = {},
            onAgreeAllClick = {},
        )
    }
}
