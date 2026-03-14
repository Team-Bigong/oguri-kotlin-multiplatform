package com.bigong.oguri.feature.mypage.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.MyPageInfo
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import com.bigong.oguri.domain.model.Place
import com.bigong.oguri.feature.mypage.ui.model.MyPageUiState
import kotlinx.datetime.LocalDate

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun MyPageScreenPreview() {
    OguriTheme {
        MyPageScreen(
            myPageUiState =
                MyPageUiState(
                    isLoading = false,
                    myPageInfo =
                        MyPageInfo(
                            nickname = "똘똘한 모험가",
                            remainingLeaveDays = 15,
                            preferredLeaveDays = 3,
                            selectedPeriods =
                                listOf(
                                    MyPageSelectedPeriod(
                                        id = 1L,
                                        startDate = LocalDate.parse("2026-02-28"),
                                        endDate = LocalDate.parse("2026-03-04"),
                                        totalTripCount = 3,
                                        dayOffCount = 2,
                                    ),
                                ),
                            savedPlaces =
                                listOf(
                                    Place(
                                        id = 1L,
                                        country = "필리핀",
                                        city = "보라카이",
                                        summary = "",
                                        thumbnailUrl = "https://media.triple.guide/triple-cms/c_limit,f_auto,h_1024,w_1024/74fdd210-d312-4aec-99de-d7900f4b95c0.jpeg",
                                    ),
                                ),
                        ),
                ),
            onRetryClick = {},
            onEditLeaveDaysClick = {},
            onDismissLeaveDaysBottomSheet = {},
            onSubmitLeaveDays = { _, _ -> },
            onDeleteScheduleClick = {},
            onDismissDeleteScheduleDialog = {},
            onConfirmDeleteSchedule = {},
            onDeleteSavedPlaceClick = {},
            onDismissDeleteSavedPlaceDialog = {},
            onConfirmDeleteSavedPlace = {},
            onSuggestClick = {},
            onTermsOfServiceClick = {},
            onPrivacyPolicyClick = {},
            onWithdrawClick = {},
            onLogoutClick = {},
            onDismissLogoutDialog = {},
            onConfirmLogout = {},
        )
    }
}
