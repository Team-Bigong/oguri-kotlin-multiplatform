package com.bigong.oguri.feature.mypage.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.bigong.oguri.core.designsystem.OguriTheme
import com.bigong.oguri.domain.model.MyPageSelectedPeriod
import kotlinx.datetime.LocalDate

@Preview(showBackground = true)
@Composable
private fun MyPageSelectedPeriodSectionPreview() {
    OguriTheme {
        MyPageSelectedPeriodSection(
            selectedPeriods =
                listOf(
                    MyPageSelectedPeriod(
                        id = 1L,
                        startDate = LocalDate.parse("2026-02-28"),
                        endDate = LocalDate.parse("2026-03-04"),
                        totalTripCount = 3,
                        dayOffCount = 2,
                    ),
                    MyPageSelectedPeriod(
                        id = 2L,
                        startDate = LocalDate.parse("2027-02-28"),
                        endDate = LocalDate.parse("2027-03-04"),
                        totalTripCount = 3,
                        dayOffCount = 2,
                    ),
                ),
            onDeleteClick = {},
        )
    }
}
