package com.bigong.oguri.feature.onboarding.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme

@Preview(showBackground = true)
@Composable
private fun OnboardingDayOffInputFieldPreview() {
    OguriTheme {
        OnboardingDayOffInputField(
            labelText = "남은 연차",
            value = "21",
            unitText = "일",
            warningText = null,
            onValueChange = {},
            onInputCommitted = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingDayOffInputFieldWarningPreview() {
    OguriTheme {
        OnboardingDayOffInputField(
            labelText = "남은 연차",
            value = "41",
            unitText = "일",
            warningText = "남은 연차는 40일 이하로 입력해 주세요.",
            onValueChange = {},
            onInputCommitted = {},
        )
    }
}
